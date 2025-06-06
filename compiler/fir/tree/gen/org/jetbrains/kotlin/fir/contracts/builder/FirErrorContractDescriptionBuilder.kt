/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// This file was generated automatically. See compiler/fir/tree/tree-generator/Readme.md.
// DO NOT MODIFY IT MANUALLY.

@file:Suppress("DuplicatedCode", "unused")

package org.jetbrains.kotlin.fir.contracts.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.contracts.FirErrorContractDescription
import org.jetbrains.kotlin.fir.contracts.impl.FirErrorContractDescriptionImpl
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic

@FirBuilderDsl
class FirErrorContractDescriptionBuilder {
    var source: KtSourceElement? = null
    var diagnostic: ConeDiagnostic? = null

    fun build(): FirErrorContractDescription {
        return FirErrorContractDescriptionImpl(
            source,
            diagnostic,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildErrorContractDescription(init: FirErrorContractDescriptionBuilder.() -> Unit = {}): FirErrorContractDescription {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
    }
    return FirErrorContractDescriptionBuilder().apply(init).build()
}
