/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators

import org.jetbrains.kotlin.generators.impl.SimpleTestClassModelTestAllFilesPresentMethodGenerator
import org.jetbrains.kotlin.generators.impl.SimpleTestMethodGenerator
import org.jetbrains.kotlin.generators.impl.SingleClassTestModelAllFilesPresentedMethodGenerator
import org.jetbrains.kotlin.generators.impl.TransformingTestMethodGenerator
import org.jetbrains.kotlin.generators.model.*
import org.jetbrains.kotlin.generators.util.GeneratorsFileUtil
import org.jetbrains.kotlin.test.TestMetadata
import org.jetbrains.kotlin.test.util.KtTestUtil
import org.jetbrains.kotlin.utils.Printer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File
import java.io.IOException

private val METHOD_GENERATORS = listOf(
    SimpleTestClassModelTestAllFilesPresentMethodGenerator,
    SimpleTestMethodGenerator,
    SingleClassTestModelAllFilesPresentedMethodGenerator,
    TransformingTestMethodGenerator,
)

class NewTestGeneratorImpl(
    additionalMethodGenerators: List<MethodGenerator<Nothing>>
) : TestGenerator(METHOD_GENERATORS + additionalMethodGenerators) {

    private val GENERATED_FILES = HashSet<String>()

    private fun Printer.generateMetadata(testDataSource: TestEntityModel) {
        val dataString = testDataSource.dataString
        if (dataString != null) {
            println("@TestMetadata(\"", dataString, "\")")
        }
    }

    private fun Printer.generateTestAnnotation() {
        println("@Test")
    }

    private fun Printer.generateNestedAnnotation(isNested: Boolean) {
        if (isNested) {
            println("@Nested")
        }
    }

    private fun Printer.generateTestDataPath(testClassModel: TestClassModel) {
        val dataPathRoot = testClassModel.dataPathRoot
        if (dataPathRoot != null) {
            println("@TestDataPath(\"", dataPathRoot, "\")")
        }
    }

    private fun Printer.generateParameterAnnotations(testClassModel: TestClassModel) {
        for (annotationModel in testClassModel.annotations) {
            annotationModel.generate(this)
            println()
        }
    }

    private fun Printer.generateTags(testEntityModel: TestEntityModel) {
        for (tag in testEntityModel.tags) {
            println("@Tag(\"$tag\")")
        }
    }

    private fun Printer.generateSuppressAllWarnings() {
        println("@SuppressWarnings(\"all\")")
    }

    override fun generateAndSave(testClass: TestGroup.TestClass, dryRun: Boolean, mainClassName: String?): GenerationResult {
        val generatorInstance = TestGeneratorInstance(
            testClass.baseDir,
            testClass.suiteTestClassName,
            testClass.baseTestClassName,
            testClass.testModels,
            methodGenerators,
            mainClassName,
        )
        return generatorInstance.generateAndSave(dryRun)
    }

    private inner class TestGeneratorInstance(
        baseDir: String,
        suiteTestClassFqName: String,
        baseTestClassFqName: String,
        private val testClassModels: Collection<TestClassModel>,
        private val methodGenerators: Map<MethodModel.Kind, MethodGenerator<*>>,
        private val mainClassName: String?
    ) {
        private val baseTestClassPackage: String = baseTestClassFqName.substringBeforeLast('.', "")
        private val baseTestClassName: String = baseTestClassFqName.substringAfterLast('.', baseTestClassFqName)
        private val suiteClassPackage: String = suiteTestClassFqName.substringBeforeLast('.', baseTestClassPackage)
        private val suiteClassName: String = suiteTestClassFqName.substringAfterLast('.', suiteTestClassFqName)
        private val testSourceFilePath: String =
            baseDir + "/" + this.suiteClassPackage.replace(".", "/") + "/" + this.suiteClassName + ".java"

        init {
            if (!GENERATED_FILES.add(testSourceFilePath)) {
                throw IllegalArgumentException("Same test file already generated in current session: $testSourceFilePath")
            }
        }

        @Throws(IOException::class)
        fun generateAndSave(dryRun: Boolean): GenerationResult {
            val generatedCode = generate()

            val testSourceFile = File(testSourceFilePath)
            val changed = if (!dryRun) {
                GeneratorsFileUtil.writeFileIfContentChanged(testSourceFile, generatedCode, false)
            } else {
                GeneratorsFileUtil.isFileContentChangedIgnoringLineSeparators(testSourceFile, generatedCode)
            }
            return GenerationResult(changed, testSourceFilePath)
        }

        private fun generate(): String {
            val out = StringBuilder()
            val p = Printer(out, indentUnit = Printer.TWO_SPACE_INDENT)

            val copyright = File("license/COPYRIGHT_HEADER.txt").takeIf { it.exists() }?.readText() ?: ""
            p.println(copyright)
            p.println()
            p.println("package $suiteClassPackage;")
            p.println()
            p.println("import com.intellij.testFramework.TestDataPath;")
            p.println("import ${KtTestUtil::class.java.canonicalName};")

            for (clazz in testClassModels.flatMapTo(mutableSetOf()) { classModel -> classModel.imports }) {
                val realName = when (clazz) {
                    TransformingTestMethodModel.TransformerFunctionsClassPlaceHolder::class.java ->
                        "org.jetbrains.kotlin.test.utils.TransformersFunctions"
                    else -> clazz.canonicalName
                }
                p.println("import $realName;")
            }

            if (suiteClassPackage != baseTestClassPackage) {
                p.println("import $baseTestClassPackage.$baseTestClassName;")
            }

            p.println("import ${TestMetadata::class.java.canonicalName};")

            // Don't import an unneeded `@Nested` annotation to avoid unused import warnings in the IDE.
            if (testClassModels.requiresNestedAnnotation()) {
                p.println("import ${Nested::class.java.canonicalName};")
            }

            p.println("import ${Test::class.java.canonicalName};")
            if (testClassModels.any { it.containsTags() }) {
                p.println("import ${Tag::class.java.canonicalName};")
            }
            p.println()
            p.println("import java.io.File;")
            p.println("import java.util.regex.Pattern;")
            p.println()
            p.println("/** This class is generated by {@link ", mainClassName, "}. DO NOT MODIFY MANUALLY */")

            p.generateSuppressAllWarnings()

            val model: TestClassModel
            if (testClassModels.size == 1) {
                model = object : DelegatingTestClassModel(testClassModels.single()) {
                    override val name: String
                        get() = suiteClassName
                }
            } else {
                model = object : TestClassModel() {
                    override val innerTestClasses: Collection<TestClassModel>
                        get() = testClassModels

                    override val methods: Collection<MethodModel>
                        get() = emptyList()

                    override val isEmpty: Boolean
                        get() = false

                    override val name: String
                        get() = suiteClassName

                    override val dataString: String?
                        get() = null

                    override val dataPathRoot: String?
                        get() = null

                    override val annotations: Collection<AnnotationModel>
                        // models have same annotations, so either distinct() or intersect() yield same result
                        get() = testClassModels.flatMap { it.annotations }.distinct()

                    override val imports: Set<Class<*>>
                        get() = super.imports

                    override val tags: List<String>
                        // models have same tags, so either distinct() or intersect() yield same result
                        get() = testClassModels.flatMap { it.tags }.distinct()
                }
            }

            generateTestClass(
                p, model, false,
                deepEmptyModels = when {
                    model.innerTestClasses.any { !it.generateEmptyTestClasses } -> model.assessDeepEmptiness()
                    else -> null
                },
            )
            return out.toString()
        }

        private val TestClassModel.generateEmptyTestClasses get() = this !is SimpleTestClassModel || generateEmptyTestClasses

        private fun TestClassModel.assessDeepEmptiness(): Set<TestClassModel> {
            val result = mutableSetOf<TestClassModel>()
            val reversedBfsInners = collectThisAndInnerClassesInBfsOrder().asReversed()

            for (model in reversedBfsInners) {
                val isDeeplyEmpty = model.methods.size == 1 && model.innerTestClasses.all { it in result }

                if (isDeeplyEmpty) {
                    result.add(model)
                }
            }

            return result
        }

        private fun TestClassModel.collectThisAndInnerClassesInBfsOrder(): List<TestClassModel> {
            val result = mutableListOf(this)
            var index = 0

            while (index < result.size) {
                val testClassModel = result[index]

                for (inner in testClassModel.innerTestClasses) {
                    result.add(inner)
                }

                index++
            }

            return result
        }

        private fun generateTestClass(
            p: Printer,
            testClassModel: TestClassModel,
            isNested: Boolean,
            /**
             * Makes test class models that don't have actual `test` methods
             * and thus will not be generated.
             * Used for test runners that have a high change of not running many
             * of the tests lying in the directories they are generated for.
             */
            deepEmptyModels: Set<TestClassModel>?,
        ) {
            p.generateNestedAnnotation(isNested)
            p.generateTags(testClassModel)
            p.generateMetadata(testClassModel)
            p.generateTestDataPath(testClassModel)
            p.generateParameterAnnotations(testClassModel)

            val extendsClause = if (!isNested) " extends $baseTestClassName" else ""

            p.println("public class ${testClassModel.name}$extendsClause {")
            p.pushIndent()

            val testMethods = testClassModel.methods
            val innerTestClasses = testClassModel.innerTestClasses

            var first = true

            val transformers = testClassModel.predefinedNativeTransformers(false)

            if (transformers.isNotEmpty()) {
                p.println("public ${testClassModel.name}() {")
                p.pushIndent()
                transformers.forEach { (path, transformer) -> p.println("register(\"$path\", $transformer);") }
                p.popIndent()
                p.println("}")
                first = false
            }

            for (methodModel in testMethods) {
                if (methodModel is RunTestMethodModel) continue // should also skip its imports
                if (!methodModel.shouldBeGenerated()) continue // should also skip its imports

                if (first) {
                    first = false
                } else {
                    p.println()
                }

                generateTestMethod(p, methodModel)
            }

            for (innerTestClass in innerTestClasses) {
                if (deepEmptyModels != null && innerTestClass in deepEmptyModels) {
                    continue
                }

                if (!innerTestClass.isEmpty) {
                    if (first) {
                        first = false
                    } else {
                        p.println()
                    }

                    generateTestClass(p, innerTestClass, true, deepEmptyModels)
                }
            }

            p.popIndent()
            p.println("}")
        }

        private fun generateTestMethod(p: Printer, methodModel: MethodModel) {
            val generator = methodGenerators.getValue(methodModel.kind)

            if (methodModel.isTestMethod()) {
                p.generateTestAnnotation()
                p.generateTags(methodModel)
                p.generateMetadata(methodModel)
            }
            generator.hackyGenerateSignature(methodModel, p)
            p.printWithNoIndent(" {")
            p.println()

            p.pushIndent()

            generator.hackyGenerateBody(methodModel, p)

            p.popIndent()
            p.println("}")
        }

        private fun <T : MethodModel> MethodGenerator<T>.hackyGenerateBody(method: MethodModel, p: Printer) {
            @Suppress("UNCHECKED_CAST")
            generateBody(method as T, p)
        }

        private fun <T : MethodModel> MethodGenerator<T>.hackyGenerateSignature(method: MethodModel, p: Printer) {
            @Suppress("UNCHECKED_CAST")
            generateSignature(method as T, p)
        }
    }

    private fun Collection<TestClassModel>.requiresNestedAnnotation(): Boolean {
        // Multiple test class models are generated as inner (nested) test class models of a fake root test class model, see
        // `TestGeneratorInstance.generate`.
        return size > 1 || singleOrNull()?.requiresNestedAnnotation() == true
    }

    private fun TestClassModel.requiresNestedAnnotation(): Boolean = innerTestClasses.isNotEmpty()

    private fun TestEntityModel.containsTags(): Boolean {
        if (this.tags.isNotEmpty()) return true
        if (this is TestClassModel) {
            if (innerTestClasses.any { it.containsTags() }) return true
            if (methods.any { it.containsTags() }) return true
        }
        return false
    }

    private fun TestClassModel.predefinedNativeTransformers(recursive: Boolean): List<Pair<String, String>> =
        methods.mapNotNull { method ->
            (method as? TransformingTestMethodModel)
                ?.takeIf { it.registerInConstructor && it.shouldBeGenerated() }
                ?.let { it.source.file.invariantSeparatorsPath to it.transformer }
        } + if (recursive) innerTestClasses.flatMap { it.predefinedNativeTransformers(true) } else listOf()
}
