/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/fir/tree/tree-generator/Readme.md.
// DO NOT MODIFY IT MANUALLY.

package org.jetbrains.kotlin.fir.expressions

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirImplementationDetail
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeProjection
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor

/**
 * Generated from: [org.jetbrains.kotlin.fir.tree.generator.FirTree.integerLiteralOperatorCall]
 */
abstract class FirIntegerLiteralOperatorCall : FirFunctionCall() {
    @UnresolvedExpressionTypeAccess
    abstract override val coneTypeOrNull: ConeKotlinType?
    abstract override val annotations: List<FirAnnotation>
    abstract override val contextArguments: List<FirExpression>
    abstract override val typeArguments: List<FirTypeProjection>
    abstract override val explicitReceiver: FirExpression?
    abstract override val source: KtSourceElement?
    abstract override val nonFatalDiagnostics: List<ConeDiagnostic>
    abstract override val argumentList: FirArgumentList
    abstract override val calleeReference: FirNamedReference
    abstract override val origin: FirFunctionCallOrigin
    abstract override val dispatchReceiver: FirExpression?
    abstract override val extensionReceiver: FirExpression?

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R =
        visitor.visitIntegerLiteralOperatorCall(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E : FirElement, D> transform(transformer: FirTransformer<D>, data: D): E =
        transformer.transformIntegerLiteralOperatorCall(this, data) as E

    abstract override fun replaceConeTypeOrNull(newConeTypeOrNull: ConeKotlinType?)

    abstract override fun replaceAnnotations(newAnnotations: List<FirAnnotation>)

    abstract override fun replaceContextArguments(newContextArguments: List<FirExpression>)

    abstract override fun replaceTypeArguments(newTypeArguments: List<FirTypeProjection>)

    abstract override fun replaceExplicitReceiver(newExplicitReceiver: FirExpression?)

    @FirImplementationDetail
    abstract override fun replaceSource(newSource: KtSourceElement?)

    abstract override fun replaceNonFatalDiagnostics(newNonFatalDiagnostics: List<ConeDiagnostic>)

    abstract override fun replaceArgumentList(newArgumentList: FirArgumentList)

    abstract override fun replaceCalleeReference(newCalleeReference: FirNamedReference)

    abstract override fun replaceCalleeReference(newCalleeReference: FirReference)

    abstract override fun replaceDispatchReceiver(newDispatchReceiver: FirExpression?)

    abstract override fun replaceExtensionReceiver(newExtensionReceiver: FirExpression?)

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirIntegerLiteralOperatorCall

    abstract override fun <D> transformTypeArguments(transformer: FirTransformer<D>, data: D): FirIntegerLiteralOperatorCall

    abstract override fun <D> transformExplicitReceiver(transformer: FirTransformer<D>, data: D): FirIntegerLiteralOperatorCall

    abstract override fun <D> transformCalleeReference(transformer: FirTransformer<D>, data: D): FirIntegerLiteralOperatorCall

    abstract fun <D> transformDispatchReceiver(transformer: FirTransformer<D>, data: D): FirIntegerLiteralOperatorCall

    abstract fun <D> transformExtensionReceiver(transformer: FirTransformer<D>, data: D): FirIntegerLiteralOperatorCall
}
