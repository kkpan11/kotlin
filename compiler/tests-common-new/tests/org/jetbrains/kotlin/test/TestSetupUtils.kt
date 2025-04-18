/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import org.jetbrains.kotlin.config.CommonConfigurationKeys.USE_FIR
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.compilerConfigurationProvider
import java.io.File

/**
 * For proper initialization of idea services those two properties should
 *   be set in environment of test. You can setup them manually via build
 *   system of run configurations or just `initIdeaConfiguration` before
 *   running tests using abilities of core test framework you use
 */
fun initIdeaConfiguration() {
    System.setProperty("idea.home", computeHomeDirectory())
    System.setProperty("idea.ignore.disabled.plugins", "true")
}

private fun computeHomeDirectory(): String {
    val userDir = System.getProperty("user.dir")
    return File(userDir ?: ".").canonicalPath
}

fun <T> runWithEnablingFirUseOption(testServices: TestServices, module: TestModule, lambda: () -> T): T {
    val compilerConfiguration = testServices.compilerConfigurationProvider.getCompilerConfiguration(module)
    compilerConfiguration.put(USE_FIR, true)
    val previousLanguageSettings = compilerConfiguration.languageVersionSettings
    compilerConfiguration.languageVersionSettings = object : LanguageVersionSettings by previousLanguageSettings {
        override val languageVersion: LanguageVersion
            get() = LanguageVersion.LATEST_STABLE
    }
    val result = lambda()
    compilerConfiguration.languageVersionSettings = previousLanguageSettings
    compilerConfiguration.put(USE_FIR, false)
    return result
}
