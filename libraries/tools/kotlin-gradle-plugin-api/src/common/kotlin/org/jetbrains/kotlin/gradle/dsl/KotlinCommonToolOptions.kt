// DO NOT EDIT MANUALLY!
// Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
// To regenerate run 'generateGradleOptions' task
@file:Suppress("RemoveRedundantQualifierName", "Deprecation", "Deprecation_Error", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dsl

/**
 * Common options for all Kotlin platforms' compilations and tools.
 */
@OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
@Deprecated(
    message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
    level = DeprecationLevel.ERROR,
)
interface KotlinCommonToolOptions {

    @OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
    @Deprecated(
        message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
        level = DeprecationLevel.ERROR,
    )
    /**
     * @suppress
     */
    val options: org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerToolOptions

    /**
     * Report an error if there are any warnings.
     *
     * Default value: false
     */
    @OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
    @Deprecated(
        message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
        level = DeprecationLevel.WARNING,
    )
    var allWarningsAsErrors: kotlin.Boolean
        get() = options.allWarningsAsErrors.get()
        set(value) = options.allWarningsAsErrors.set(value)

    /**
     * Don't generate any warnings.
     *
     * Default value: false
     */
    @OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
    @Deprecated(
        message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
        level = DeprecationLevel.WARNING,
    )
    var suppressWarnings: kotlin.Boolean
        get() = options.suppressWarnings.get()
        set(value) = options.suppressWarnings.set(value)

    /**
     * Enable verbose logging output.
     *
     * Default value: false
     */
    @OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
    @Deprecated(
        message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
        level = DeprecationLevel.WARNING,
    )
    var verbose: kotlin.Boolean
        get() = options.verbose.get()
        set(value) = options.verbose.set(value)

    /**
     * A list of additional compiler arguments
     *
     * Default value: emptyList<String>()
     */
    @OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
    @Deprecated(
        message = org.jetbrains.kotlin.gradle.dsl.KOTLIN_OPTIONS_DEPRECATION_MESSAGE,
        level = DeprecationLevel.WARNING,
    )
    var freeCompilerArgs: kotlin.collections.List<kotlin.String>
        get() = options.freeCompilerArgs.get()
        set(value) = options.freeCompilerArgs.set(value)
}
