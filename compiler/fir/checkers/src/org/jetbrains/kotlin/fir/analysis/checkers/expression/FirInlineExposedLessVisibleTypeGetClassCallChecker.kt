/*
 * Copyright 2010-2025 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.type.FirInlineExposedLessVisibleTypeChecker
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.types.resolvedType

object FirInlineExposedLessVisibleTypeGetClassCallChecker : FirGetClassCallChecker(MppCheckerKind.Platform) {
    context(c: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirGetClassCall) {
        val inlineFunctionBodyContext = c.inlineFunctionBodyContext ?: return
        FirInlineExposedLessVisibleTypeChecker.check(expression.argument.resolvedType, expression.argument.source, inlineFunctionBodyContext)
    }
}