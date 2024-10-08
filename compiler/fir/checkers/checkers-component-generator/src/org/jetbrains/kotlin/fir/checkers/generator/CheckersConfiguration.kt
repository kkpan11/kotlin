/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

class CheckersConfiguration(
    val aliases: Map<KClass<*>, Pair<String, Boolean>>,
    val additionalCheckers: MutableMap<String, String>,
    val visitAlso: Map<KClass<*>, String>,
) {
    val parentsMap: Map<KClass<*>, List<KClass<*>>>

    init {
        val parents: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
        for (firKClass in aliases.keys) {
            val allParents = mutableListOf<KClass<*>>()
            bfs(
                firKClass,
                childrenExtractor = { it.allSuperclasses }
            ) {
                if (it in aliases) {
                    allParents += it
                }
                true
            }
            parents[firKClass] = allParents
        }
        parentsMap = parents
    }
}

private fun <T> bfs(start: T, childrenExtractor: (T) -> Collection<T>, process: (T) -> Boolean) {
    val queue = ArrayDeque<T>()
    val visited = mutableSetOf<T>()
    val levels = mutableMapOf(start to 0)
    queue.addLast(start)
    var levelToStop: Int? = null
    while (queue.isNotEmpty()) {
        val element = queue.removeFirst()
        if (!visited.add(element)) continue
        val level = levels.getValue(element)
        if (levelToStop != null && level > levelToStop) continue
        val shouldContinue = if (level > 0) process(element) else true
        if (shouldContinue) {
            val children = childrenExtractor(element)
            for (child in children) {
                levels[child] = level + 1
                queue.addLast(child)
            }
        } else {
            levelToStop = level
        }
    }
}
