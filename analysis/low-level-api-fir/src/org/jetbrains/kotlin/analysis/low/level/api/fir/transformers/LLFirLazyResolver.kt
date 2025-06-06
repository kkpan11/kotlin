/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.transformers

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets.LLFirPartialBodyResolveTarget
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.targets.LLFirResolveTarget
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.checkCanceled
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.checkPhase
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.utils.exceptions.requireWithAttachment

/**
 * This class is responsible for [LLFirResolveTarget] resolution and "is resolved" check after that.
 *
 * @see LLFirLazyResolverRunner
 * @see LLFirTargetResolver
 */
internal sealed class LLFirLazyResolver(val resolverPhase: FirResolvePhase) {
    fun resolve(target: LLFirResolveTarget) {
        val resolver = createTargetResolver(target)
        requireWithAttachment(
            resolverPhase == resolver.resolverPhase,
            {
                """
                Phase mismatch between ${this::class.simpleName} and ${resolver::class.simpleName}.
                The resolver phase is ${resolver.resolverPhase}, but $resolverPhase is expected
                """.trimIndent()
            },
        )

        resolver.resolveDesignation()

        if (target !is LLFirPartialBodyResolveTarget) {
            target.forEachTarget(::checkIsResolved)
        }

        checkCanceled()
    }

    protected abstract fun createTargetResolver(target: LLFirResolveTarget): LLFirTargetResolver

    fun checkIsResolved(target: FirElementWithResolveState) {
        target.checkPhase(resolverPhase)
        phaseSpecificCheckIsResolved(target)
        checkNestedDeclarationsAreResolved(target)
    }

    /**
     * Check that phase-specific conditions are met
     * Will be performed to resolved declaration and its nested declarations
     * @see checkNestedDeclarationsAreResolved
     */
    protected abstract fun phaseSpecificCheckIsResolved(target: FirElementWithResolveState)

    private fun checkNestedDeclarationsAreResolved(target: FirElementWithResolveState) {
        if (target !is FirDeclaration) return

        checkFunctionParametersAreResolved(target)
        checkVariableSubDeclarationsAreResolved(target)
        checkTypeParametersAreResolved(target)
        checkReceiversAreResolved(target)
    }

    private fun checkReceiversAreResolved(declaration: FirDeclaration) {
        when (declaration) {
            is FirCallableDeclaration -> {
                declaration.receiverParameter?.let(::checkIsResolved)
                declaration.contextParameters.forEach(::checkIsResolved)
            }

            is FirScript -> declaration.receivers.forEach(::checkIsResolved)
            is FirRegularClass -> declaration.contextParameters.forEach(::checkIsResolved)
            is FirDanglingModifierList -> declaration.contextParameters.forEach(::checkIsResolved)
            else -> {}
        }
    }

    private fun checkVariableSubDeclarationsAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirVariable) return

        declaration.getter?.let(::checkIsResolved)
        declaration.setter?.let(::checkIsResolved)
        declaration.backingField?.let(::checkIsResolved)
    }

    private fun checkFunctionParametersAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirFunction) return

        declaration.valueParameters.forEach(::checkIsResolved)
    }

    private fun checkTypeParametersAreResolved(declaration: FirDeclaration) {
        if (declaration !is FirTypeParameterRefsOwner) return

        for (parameter in declaration.typeParameters) {
            if (parameter !is FirTypeParameter) continue
            checkIsResolved(parameter)
        }
    }
}
