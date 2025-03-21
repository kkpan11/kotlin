/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cli.common.messages

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.*
import com.intellij.psi.util.PsiFormatUtil
import org.jetbrains.kotlin.KtRealPsiSourceElement
import org.jetbrains.kotlin.analyzer.AbstractAnalyzerWithCompilerReport
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.DiagnosticUtils.sortedDiagnostics
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.fir.builder.FirSyntaxErrors
import org.jetbrains.kotlin.load.java.components.TraceBasedErrorReporter
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.checkers.OptInUsageChecker
import org.jetbrains.kotlin.resolve.jvm.JvmBindingContextSlices

class AnalyzerWithCompilerReport(
    private val messageCollector: MessageCollector,
    private val languageVersionSettings: LanguageVersionSettings,
    private val renderDiagnosticName: Boolean
) : AbstractAnalyzerWithCompilerReport {
    override val targetEnvironment: TargetEnvironment
        get() = CompilerEnvironment

    override lateinit var analysisResult: AnalysisResult

    constructor(configuration: CompilerConfiguration) : this(
        configuration.messageCollector,
        configuration.languageVersionSettings,
        configuration.getBoolean(CLIConfigurationKeys.RENDER_DIAGNOSTIC_INTERNAL_NAME)
    )

    private fun reportIncompleteHierarchies() {
        val bindingContext = analysisResult.bindingContext
        val classes = bindingContext.getKeys(TraceBasedErrorReporter.INCOMPLETE_HIERARCHY)
        if (!classes.isEmpty()) {
            val message = StringBuilder(
                "Supertypes of the following classes cannot be resolved. " +
                        "Please make sure you have the required dependencies in the classpath:\n"
            )
            for (descriptor in classes) {
                val fqName = DescriptorUtils.getFqName(descriptor).asString()
                val unresolved = bindingContext.get(TraceBasedErrorReporter.INCOMPLETE_HIERARCHY, descriptor)
                assert(unresolved != null && !unresolved.isEmpty()) {
                    "Incomplete hierarchy should be reported with names of unresolved superclasses: $fqName"
                }
                message.append("    class ").append(fqName)
                    .append(", unresolved supertypes: ").append(unresolved!!.joinToString())
                    .append("\n")
            }
            messageCollector.report(ERROR, message.toString())
        }
    }

    private fun reportAlternativeSignatureErrors() {
        val bc = analysisResult.bindingContext
        val descriptorsWithErrors = bc.getKeys(JvmBindingContextSlices.LOAD_FROM_JAVA_SIGNATURE_ERRORS)
        if (!descriptorsWithErrors.isEmpty()) {
            val message = StringBuilder("The following Java entities have annotations with wrong Kotlin signatures:\n")
            for (descriptor in descriptorsWithErrors) {
                val declaration = DescriptorToSourceUtils.descriptorToDeclaration(descriptor)
                assert(declaration is PsiModifierListOwner)

                val errors = bc.get(JvmBindingContextSlices.LOAD_FROM_JAVA_SIGNATURE_ERRORS, descriptor)
                assert(errors != null && !errors.isEmpty())

                val externalName = PsiFormatUtil.getExternalName(declaration as PsiModifierListOwner)
                message.append(externalName).append(":\n")

                for (error in errors!!) {
                    message.append("    ").append(error).append("\n")
                }
            }
            messageCollector.report(ERROR, message.toString())
        }
    }

    private fun reportSyntaxErrors(files: Collection<KtFile>) {
        for (file in files) {
            reportSyntaxErrors(file, messageCollector)
        }
    }

    class SyntaxErrorReport(val isHasErrors: Boolean, val isAllErrorsAtEof: Boolean)

    override fun hasErrors(): Boolean =
        messageCollector.hasErrors()

    override fun analyzeAndReport(files: Collection<KtFile>, analyze: () -> AnalysisResult) {
        analysisResult = analyze()
        if (!analysisResult.isError()) {
            OptInUsageChecker.checkCompilerArguments(
                analysisResult.moduleDescriptor, languageVersionSettings,
                reportError = { message -> messageCollector.report(ERROR, message) },
                reportWarning = { message -> messageCollector.report(WARNING, message) }
            )
        }
        reportSyntaxErrors(files)
        reportDiagnostics(analysisResult.bindingContext.diagnostics, messageCollector, renderDiagnosticName)
        reportIncompleteHierarchies()
        reportAlternativeSignatureErrors()
    }

    private class MyDiagnostic<E : PsiElement>(
        psiElement: E, factory: DiagnosticFactory0<E>, val message: String
    ) : SimpleDiagnostic<E>(psiElement, factory, Severity.ERROR) {

        override val isValid: Boolean = true
    }

    companion object {

        fun convertSeverity(severity: Severity): CompilerMessageSeverity = when (severity) {
            Severity.INFO -> INFO
            Severity.ERROR -> ERROR
            Severity.WARNING -> WARNING
            Severity.FIXED_WARNING -> FIXED_WARNING
        }

        private val SYNTAX_ERROR_FACTORY = DiagnosticFactory0.create<PsiErrorElement>(Severity.ERROR)

        private fun reportDiagnostic(diagnostic: Diagnostic, reporter: DiagnosticMessageReporter, renderDiagnosticName: Boolean): Boolean {
            if (!diagnostic.isValid) return false

            val message = (diagnostic as? MyDiagnostic<*>)?.message ?: DefaultErrorMessages.render(diagnostic)
            val textToRender = when (renderDiagnosticName) {
                true -> "[${diagnostic.factoryName}] $message"
                false -> message
            }

            reporter.report(
                diagnostic,
                diagnostic.psiFile,
                textToRender
            )

            return diagnostic.severity == Severity.ERROR
        }

        fun reportDiagnostics(
            unsortedDiagnostics: GenericDiagnostics<*>,
            reporter: DiagnosticMessageReporter,
            renderDiagnosticName: Boolean
        ): Boolean {
            var hasErrors = false
            val diagnostics = sortedDiagnostics(unsortedDiagnostics.all().filterIsInstance<Diagnostic>())
            for (diagnostic in diagnostics) {
                hasErrors = hasErrors or reportDiagnostic(diagnostic, reporter, renderDiagnosticName)
            }
            return hasErrors
        }

        fun reportDiagnostics(
            diagnostics: GenericDiagnostics<*>,
            messageCollector: MessageCollector,
            renderInternalDiagnosticName: Boolean
        ): Boolean {
            return reportDiagnostics(diagnostics, DefaultDiagnosticReporter(messageCollector), renderInternalDiagnosticName).also {
                reportSpecialErrors(
                    diagnostics.any { it.factory == Errors.INCOMPATIBLE_CLASS },
                    diagnostics.any { it.factory == Errors.PRE_RELEASE_CLASS },
                    diagnostics.any { it.factory == Errors.IR_WITH_UNSTABLE_ABI_COMPILED_CLASS },
                    messageCollector,
                )
            }
        }

        fun reportSpecialErrors(
            hasIncompatibleClasses: Boolean,
            hasPrereleaseClasses: Boolean,
            hasUnstableClasses: Boolean,
            messageCollector: MessageCollector,
        ) {
            if (hasIncompatibleClasses) {
                messageCollector.report(
                    ERROR,
                    "Incompatible classes were found in dependencies. " +
                            "Remove them from the classpath or use '-Xskip-metadata-version-check' to suppress errors"
                )
            }

            if (hasPrereleaseClasses) {
                messageCollector.report(
                    ERROR,
                    "Pre-release declarations were found in dependencies. Please exclude the dependencies with such declarations " +
                            "and recompile with a release compiler, or use '-Xskip-prerelease-check' to suppress errors. " +
                            "Note that in the latter case the compiled declarations will also be marked as pre-release."
                )
            }

            if (hasUnstableClasses) {
                messageCollector.report(
                    ERROR,
                    "Classes compiled by an unstable version of the Kotlin compiler were found in dependencies. " +
                            "Remove them from the classpath or use '-Xallow-unstable-dependencies' to suppress errors"
                )
            }
        }

        // Reports K1 diagnostics ([org.jetbrains.kotlin.diagnostics.SimpleDiagnostic])
        fun reportSyntaxErrors(file: PsiElement, reporter: DiagnosticMessageReporter): SyntaxErrorReport {
            return reportSyntaxErrors(file) { element, message ->
                val diagnostic = MyDiagnostic(element, SYNTAX_ERROR_FACTORY, message)
                AnalyzerWithCompilerReport.reportDiagnostic(diagnostic, reporter, renderDiagnosticName = false)
            }
        }

        // Reports K2 diagnostics ([org.jetbrains.kotlin.diagnostics.KtDiagnostic])
        fun reportSyntaxErrors(file: PsiElement, diagnosticCollector: BaseDiagnosticsCollector): SyntaxErrorReport {
            return reportSyntaxErrors(file) { element, message ->
                @OptIn(InternalDiagnosticFactoryMethod::class)
                val diagnostic = FirSyntaxErrors.SYNTAX.on(
                    KtRealPsiSourceElement(element),
                    message,
                    positioningStrategy = null,
                    LanguageVersionSettingsImpl.DEFAULT, // syntax errors couldn't be suppressed anyway
                )
                val context = object : DiagnosticContext {
                    override val containingFilePath: String?
                        get() = file.containingFile.virtualFile?.path

                    override fun isDiagnosticSuppressed(diagnostic: KtDiagnostic): Boolean {
                        return false
                    }

                    override val languageVersionSettings: LanguageVersionSettings
                        get() = LanguageVersionSettingsImpl.DEFAULT
                }
                diagnosticCollector.report(diagnostic, context)
            }
        }

        private fun reportSyntaxErrors(
            file: PsiElement,
            createAndReportSyntaxError: (PsiErrorElement, message: String) -> Unit,
        ): SyntaxErrorReport {
            class ErrorReportingVisitor : AnalyzingUtils.PsiErrorElementVisitor() {
                var hasErrors = false
                var allErrorsAtEof = true

                private fun PsiElement.isAtEof(): Boolean {
                    var element = this
                    while (true) {
                        element = element.nextSibling ?: return true
                        if (element !is PsiWhiteSpace || element !is PsiComment) return false
                    }
                }

                override fun visitErrorElement(element: PsiErrorElement) {
                    val description = element.errorDescription
                    if (allErrorsAtEof && !element.isAtEof()) {
                        allErrorsAtEof = false
                    }
                    hasErrors = true
                    createAndReportSyntaxError(
                        element,
                        if (StringUtil.isEmpty(description)) "Syntax error" else description
                    )
                }
            }

            val visitor = ErrorReportingVisitor()

            file.accept(visitor)

            return SyntaxErrorReport(visitor.hasErrors, visitor.allErrorsAtEof)
        }

        fun reportSyntaxErrors(file: PsiElement, messageCollector: MessageCollector): SyntaxErrorReport {
            return reportSyntaxErrors(file, DefaultDiagnosticReporter(messageCollector))
        }
    }
}
