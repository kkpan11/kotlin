/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extra

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.config.AnalysisFlags
import org.jetbrains.kotlin.config.ExplicitApiMode
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.diagnostics.visibilityModifier
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.analysis.checkers.*
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.context.findClosest
import org.jetbrains.kotlin.fir.analysis.checkers.syntax.FirDeclarationSyntaxChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.scopes.ProcessorAction
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtDeclaration

object RedundantVisibilityModifierSyntaxChecker : FirDeclarationSyntaxChecker<FirDeclaration, KtDeclaration>() {

    override fun checkPsiOrLightTree(
        element: FirDeclaration,
        source: KtSourceElement,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        if (element is FirPropertyAccessor || element is FirValueParameter) {
            return
        }

        if (element is FirConstructor && element.source?.kind is KtFakeSourceElementKind) {
            return
        }

        when (element) {
            is FirProperty -> checkPropertyAndReport(element, context, reporter)
            else -> {
                val defaultVisibility = element.symbol.resolvedStatus?.defaultVisibility ?: Visibilities.DEFAULT_VISIBILITY
                checkElementAndReport(element, defaultVisibility, context, reporter)
            }
        }
    }

    private fun checkPropertyAndReport(
        property: FirProperty,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        var setterImplicitVisibility: Visibility? = null

        property.setter?.let { setter ->
            val defaultVisibility = setter.symbol.resolvedStatus.defaultVisibility
            val visibility = setter.implicitVisibility(context, defaultVisibility)
            setterImplicitVisibility = visibility
            checkElementAndReport(setter, visibility, property.symbol, context, reporter)
        }

        property.getter?.let { getter ->
            checkElementAndReport(getter, getter.symbol.resolvedStatus.defaultVisibility, property.symbol, context, reporter)
        }

        property.backingField?.let { field ->
            checkElementAndReport(field, field.symbol.resolvedStatus.defaultVisibility, property.symbol, context, reporter)
        }

        if (property.canMakeSetterMoreAccessible(setterImplicitVisibility)) {
            return
        }

        checkElementAndReport(property, property.symbol.resolvedStatus.defaultVisibility, context, reporter)
    }

    private fun checkElementAndReport(
        element: FirDeclaration,
        defaultVisibility: Visibility,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) = checkElementAndReport(
        element,
        defaultVisibility,
        context.findClosest(),
        context,
        reporter
    )

    private fun checkElementAndReport(
        element: FirDeclaration,
        defaultVisibility: Visibility,
        containingDeclarationSymbol: FirBasedSymbol<*>?,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) = checkElementWithImplicitVisibilityAndReport(
        element,
        element.implicitVisibility(context, defaultVisibility),
        containingDeclarationSymbol,
        context,
        reporter
    )

    private fun checkElementWithImplicitVisibilityAndReport(
        element: FirDeclaration,
        implicitVisibility: Visibility,
        containingDeclarationSymbol: FirBasedSymbol<*>?,
        context: CheckerContext,
        reporter: DiagnosticReporter
    ) {
        if (element.source?.kind is KtFakeSourceElementKind && !element.isPropertyFromParameter) {
            return
        }

        if (element !is FirMemberDeclaration) {
            return
        }

        val explicitVisibility = element.source?.explicitVisibility
        val isHidden = explicitVisibility.isEffectivelyHiddenBy(containingDeclarationSymbol)
        if (isHidden) {
            reportElement(element, context, reporter)
            return
        }

        // In explicit API mode, `public` is explicitly required.
        val explicitApiMode = context.languageVersionSettings.getFlag(AnalysisFlags.explicitApiMode)
        if (explicitApiMode != ExplicitApiMode.DISABLED && explicitVisibility == Visibilities.Public) {
            return
        }

        if (explicitVisibility == implicitVisibility) {
            reportElement(element, context, reporter)
        }
    }

    private val FirElement.isPropertyFromParameter: Boolean
        get() = this is FirProperty && source?.kind == KtFakeSourceElementKind.PropertyFromParameter

    private fun reportElement(element: FirDeclaration, context: CheckerContext, reporter: DiagnosticReporter) {
        reporter.reportOn(element.source, FirErrors.REDUNDANT_VISIBILITY_MODIFIER, context)
    }

    private fun FirProperty.canMakeSetterMoreAccessible(setterImplicitVisibility: Visibility?): Boolean {
        if (!isOverride) {
            return false
        }

        if (!hasSetterWithImplicitVisibility) {
            return false
        }

        if (setterImplicitVisibility == null) {
            return false
        }

        return setterImplicitVisibility != visibility
    }

    private val FirProperty.hasSetterWithImplicitVisibility: Boolean
        get() {
            val theSetter = setter ?: return false

            if (source?.lighterASTNode == theSetter.source?.lighterASTNode) {
                return true
            }

            val theSource = theSetter.source ?: return true
            return theSource.explicitVisibility == null
        }

    private fun Visibility?.isEffectivelyHiddenBy(declaration: FirBasedSymbol<*>?): Boolean {
        if (this == null || this == Visibilities.Protected) {
            return false
        }
        val effectiveVisibility = when (declaration) {
            is FirCallableSymbol<*> -> declaration.effectiveVisibility
            is FirClassLikeSymbol<*> -> declaration.effectiveVisibility
            else -> return false
        }
        val containerVisibility = effectiveVisibility.toVisibility()

        if (containerVisibility == Visibilities.Local && this == Visibilities.Internal) {
            return true
        }

        val difference = this.compareTo(containerVisibility) ?: return false
        return difference > 0
    }

    private fun FirDeclaration.implicitVisibility(context: CheckerContext, defaultVisibility: Visibility): Visibility {
        return when {
            this is FirPropertyAccessor
                    && isSetter
                    && context.containingDeclarations.last() is FirClassSymbol
                    && propertySymbol.isOverride -> findPropertyAccessorVisibility(this, context)

            this is FirPropertyAccessor -> propertySymbol.visibility

            this is FirConstructor -> {
                val classSymbol = this.getContainingClassSymbol()
                if (
                    classSymbol is FirRegularClassSymbol
                    && (classSymbol.isEnumClass || classSymbol.isSealed)
                ) {
                    Visibilities.Private
                } else {
                    defaultVisibility
                }
            }

            this is FirSimpleFunction
                    && context.containingDeclarations.last() is FirClassSymbol
                    && this.isOverride -> findFunctionVisibility(this, context)

            this is FirProperty
                    && context.containingDeclarations.last() is FirClassSymbol
                    && this.isOverride -> findPropertyVisibility(this, context)

            else -> defaultVisibility
        }
    }

    private fun findBiggestVisibility(
        processSymbols: ((FirCallableSymbol<*>) -> ProcessorAction) -> Unit
    ): Visibility {
        var current: Visibility = Visibilities.Private

        processSymbols {
            val difference = Visibilities.compare(current, it.visibility)

            if (difference != null && difference < 0) {
                current = it.visibility
            }

            ProcessorAction.NEXT
        }

        return current
    }

    private fun findPropertyAccessorVisibility(accessor: FirPropertyAccessor, context: CheckerContext): Visibility {
        val propertySymbol = accessor.propertySymbol
        return findBiggestVisibility { checkVisibility ->
            propertySymbol.processOverriddenPropertiesWithActionSafe(context) { property ->
                checkVisibility(property.setterSymbol ?: property)
            }
        }
    }

    private fun findPropertyVisibility(property: FirProperty, context: CheckerContext): Visibility {
        return findBiggestVisibility {
            property.symbol.processOverriddenPropertiesWithActionSafe(context, it)
        }
    }

    private fun findFunctionVisibility(function: FirSimpleFunction, context: CheckerContext): Visibility {
        return findBiggestVisibility {
            function.symbol.processOverriddenFunctionsWithActionSafe(context, it)
        }
    }
}

val KtSourceElement.explicitVisibility: Visibility?
    get() {
        val visibilityModifier = treeStructure.visibilityModifier(lighterASTNode)
        return (visibilityModifier?.tokenType as? KtModifierKeywordToken)?.toVisibilityOrNull()
    }
