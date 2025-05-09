/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.impl

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.isSynthetic
import org.jetbrains.kotlin.fir.isEnumEntries
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.scopes.DelicateScopeAPI
import org.jetbrains.kotlin.fir.scopes.FirContainingNamesAwareScope
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.SmartList

abstract class FirClassDeclaredMemberScope(val classId: ClassId) : FirContainingNamesAwareScope() {
    override val scopeOwnerLookupNames: List<String> = SmartList(classId.asFqNameString())

    @DelicateScopeAPI
    abstract override fun withReplacedSessionOrNull(newSession: FirSession, newScopeSession: ScopeSession): FirClassDeclaredMemberScope?
}

class FirClassDeclaredMemberScopeImpl(
    val useSiteSession: FirSession,
    private val klass: FirClass,
    private val existingNamesForLazyNestedClassifierScope: List<Name>?,
) : FirClassDeclaredMemberScope(klass.classId) {
    // Note: this kind of scope contains both static and non-static classifiers
    // Some discussion about it can be found in KT-62023
    private val nestedClassifierScope: FirContainingNamesAwareScope? = if (existingNamesForLazyNestedClassifierScope != null) {
        lazyNestedClassifierScope(useSiteSession, klass.symbol.classId, existingNamesForLazyNestedClassifierScope)
    } else {
        useSiteSession.nestedClassifierScope(klass)
    }

    /**
     * This index should be lazy to avoid SOE for Java classes as [FirClass.declarations] in this case may lead to it
     *
     * Issues: KT-72660
     */
    private val callablesIndex: Map<Name, List<FirCallableSymbol<*>>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val result = mutableMapOf<Name, MutableList<FirCallableSymbol<*>>>()
        for (declaration in klass.declarations) {
            if (declaration !is FirCallableDeclaration) continue

            val name = when (declaration) {
                is FirConstructor -> SpecialNames.INIT
                is FirVariable -> when {
                    declaration.isSynthetic || declaration.isEnumEntries(klass) && !klass.supportsEnumEntries -> continue
                    else -> declaration.name
                }

                is FirSimpleFunction -> declaration.name
                else -> continue
            }

            result.getOrPut(name) { mutableListOf() } += declaration.symbol
        }

        result
    }

    private val FirClass.supportsEnumEntries get() = useSiteSession.enumEntriesSupport.canSynthesizeEnumEntriesFor(this)

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        if (name == SpecialNames.INIT) return
        processCallables(name, processor)
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        processCallables(SpecialNames.INIT, processor)
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        processCallables(name, processor)
    }

    private inline fun <reified D : FirCallableSymbol<*>> processCallables(
        name: Name,
        processor: (D) -> Unit
    ) {
        val symbols = callablesIndex[name] ?: emptyList()
        for (symbol in symbols) {
            if (symbol is D) {
                processor(symbol)
            }
        }
    }

    override fun processClassifiersByNameWithSubstitution(
        name: Name,
        processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit
    ) {
        nestedClassifierScope?.processClassifiersByNameWithSubstitution(name, processor)
    }

    override fun getCallableNames(): Set<Name> {
        return callablesIndex.keys
    }

    override fun getClassifierNames(): Set<Name> {
        return nestedClassifierScope?.getClassifierNames().orEmpty()
    }

    @DelicateScopeAPI
    override fun withReplacedSessionOrNull(newSession: FirSession, newScopeSession: ScopeSession): FirClassDeclaredMemberScopeImpl {
        return FirClassDeclaredMemberScopeImpl(newSession, klass, existingNamesForLazyNestedClassifierScope)
    }
}
