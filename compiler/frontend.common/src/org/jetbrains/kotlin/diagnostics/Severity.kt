/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

enum class Severity {
    INFO,
    ERROR,
    WARNING,

    /**
     * see [org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.FIXED_WARNING]
     */
    FIXED_WARNING,
}
