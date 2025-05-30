/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.print.symbol

import org.jetbrains.kotlin.generators.tree.*
import org.jetbrains.kotlin.generators.tree.AbstractField.SymbolFieldRole
import org.jetbrains.kotlin.generators.tree.printer.FunctionParameter
import org.jetbrains.kotlin.generators.tree.printer.ImportCollectingPrinter
import org.jetbrains.kotlin.generators.tree.printer.printFunctionDeclaration
import org.jetbrains.kotlin.generators.tree.printer.printKDoc
import org.jetbrains.kotlin.generators.util.printBlock
import org.jetbrains.kotlin.ir.generator.IrSymbolTree
import org.jetbrains.kotlin.ir.generator.IrTree.rootElement
import org.jetbrains.kotlin.ir.generator.declaredSymbolVisitorType
import org.jetbrains.kotlin.ir.generator.model.Element
import org.jetbrains.kotlin.ir.generator.model.symbol.Symbol
import org.jetbrains.kotlin.ir.generator.model.symbol.findFieldsWithSymbols
import org.jetbrains.kotlin.ir.generator.model.symbol.symbolVisitorMethodName
import org.jetbrains.kotlin.ir.generator.referencedSymbolVisitorType
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

internal abstract class AbstractSymbolVisitorPrinter(
    private val printer: ImportCollectingPrinter,
    val elements: List<Element>,
    val roles: List<SymbolFieldRole>,
) {
    abstract val symbolVisitorType: ClassRef<*>

    abstract val implementationKind: ImplementationKind

    open val symbolVisitorSuperTypes: List<ClassRef<*>>
        get() = emptyList()

    protected open fun shouldPrintMethodForSymbol(symbolClass: Symbol, role: SymbolFieldRole): Boolean = true

    private fun ImportCollectingPrinter.printMethod(symbolClass: Symbol, role: SymbolFieldRole) {
        val containerParameter = FunctionParameter("container", rootElement)
        val symbolParameter = FunctionParameter("symbol", symbolClass)
        printFunctionDeclaration(
            symbolVisitorMethodName(symbolClass, role),
            parameters = listOf(containerParameter, symbolParameter),
            returnType = StandardTypes.unit,
            override = symbolVisitorSuperTypes.isNotEmpty(),
        )
        printMethodImplementation(containerParameter, symbolParameter, symbolClass, role)
    }

    protected open fun ImportCollectingPrinter.printMethodImplementation(
        containerParameter: FunctionParameter,
        symbolParameter: FunctionParameter,
        symbolClass: Symbol,
        role: SymbolFieldRole
    ) {
        if (symbolClass.subElements.isNotEmpty()) {
            printBlock {
                print("when (", symbolParameter.name, ")")
                printBlock {
                    for (subSymbol in symbolClass.subElements) {
                        println(
                            "is ",
                            subSymbol.render(),
                            " -> ",
                            symbolVisitorMethodName(subSymbol, role),
                            "(",
                            containerParameter.name,
                            ", ",
                            symbolParameter.name,
                            ")"
                        )
                    }
                }
            }
        } else {
            println(" { visit${role.baseName}Symbol(", containerParameter.name, ", ", symbolParameter.name, ") }")
        }
    }

    protected open fun ImportCollectingPrinter.printAdditionalDeclarations() {}

    fun printSymbolVisitor() {
        printer.run {
            printKDoc(
                buildString {
                    append("Auto-generated by [${this@AbstractSymbolVisitorPrinter::class.qualifiedName}]")
                },
            )
            assert(symbolVisitorType.kind == implementationKind.typeKind) { "Type kind mismatch" }
            print(implementationKind.title, " ", symbolVisitorType.simpleName)
            symbolVisitorSuperTypes.ifNotEmpty {
                print(joinToString(prefix = " : ") { it.render() + if (it.kind == TypeKind.Class) "()" else "" })
            }
            printBlock {
                for (role in roles) {
                    val fieldsAndSymbols = findFieldsWithSymbols(elements, role)
                    val symbols = fieldsAndSymbols.keys.flatMap { it.elementDescendantsAndSelfDepthFirst() }.distinct()
                    for (symbolType in symbols) {
                        if (!shouldPrintMethodForSymbol(symbolType, role)) continue
                        println()
                        printMethod(symbolType, role)
                    }
                }
                printAdditionalDeclarations()
            }
        }
    }

    protected val SymbolFieldRole.baseName: String
        get() = when (this) {
            SymbolFieldRole.DECLARED -> "Declared"
            SymbolFieldRole.REFERENCED -> "Referenced"
        }

    protected fun ImportCollectingPrinter.printBaseVisitMethodDeclaration(
        role: SymbolFieldRole?,
        override: Boolean
    ) {
        printFunctionDeclaration(
            "visit${role?.baseName ?: ""}Symbol",
            parameters = listOf(
                FunctionParameter("container", rootElement),
                FunctionParameter("symbol", IrSymbolTree.rootElement)
            ),
            returnType = StandardTypes.unit,
            override = override,
        )
    }
}

internal class DeclaredSymbolVisitorInterfacePrinter(
    printer: ImportCollectingPrinter,
    elements: List<Element>,
    override val symbolVisitorType: ClassRef<*>,
) : AbstractSymbolVisitorPrinter(printer, elements, roles = listOf(SymbolFieldRole.DECLARED)) {
    override val implementationKind: ImplementationKind
        get() = ImplementationKind.Interface

    override fun ImportCollectingPrinter.printAdditionalDeclarations() {
        println()
        printBaseVisitMethodDeclaration(
            role = SymbolFieldRole.DECLARED,
            override = false,
        )
        println()
    }
}

internal class ReferencedSymbolVisitorInterfacePrinter(
    printer: ImportCollectingPrinter,
    elements: List<Element>,
    override val symbolVisitorType: ClassRef<*>,
) : AbstractSymbolVisitorPrinter(printer, elements, roles = listOf(SymbolFieldRole.REFERENCED)) {
    override val implementationKind: ImplementationKind
        get() = ImplementationKind.Interface

    override fun ImportCollectingPrinter.printAdditionalDeclarations() {
        println()
        printBaseVisitMethodDeclaration(
            role = SymbolFieldRole.REFERENCED,
            override = false,
        )
        println()
    }
}

internal class SymbolVisitorInterfacePrinter(
    printer: ImportCollectingPrinter,
    elements: List<Element>,
    override val symbolVisitorType: ClassRef<*>,
) : AbstractSymbolVisitorPrinter(printer, elements, roles = emptyList()) {
    override val implementationKind: ImplementationKind
        get() = ImplementationKind.Interface

    override val symbolVisitorSuperTypes: List<ClassRef<*>>
        get() = listOf(declaredSymbolVisitorType, referencedSymbolVisitorType)

    override fun ImportCollectingPrinter.printAdditionalDeclarations() {
        printBaseVisitMethodDeclaration(
            role = null,
            override = false,
        )
        println()
        for (role in SymbolFieldRole.entries) {
            println()
            printBaseVisitMethodDeclaration(
                role = role,
                override = true,
            )
            println(" { visitSymbol(container, symbol) }")
        }
    }
}
