@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.gradle.targets.native.tasks.artifact

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinArtifact
import org.jetbrains.kotlin.gradle.dsl.KotlinArtifactConfig
import org.jetbrains.kotlin.gradle.dsl.KotlinArtifactsExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.BITCODE_EMBEDDING_DEPRECATION_MESSAGE
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.DEPRECATED_TARGET_MESSAGE
import org.jetbrains.kotlin.konan.target.KonanTarget
import javax.inject.Inject

abstract class KotlinArtifactsExtensionImpl @Inject constructor(project: Project) : KotlinArtifactsExtension {

    override val artifactConfigs = project.objects.domainObjectSet(KotlinArtifactConfig::class.java)
    override val artifacts = project.objects.namedDomainObjectSet(KotlinArtifact::class.java)
    override val Native = project.objects.newInstance(KotlinNativeArtifactDSLImpl::class.java, project)

    val RELEASE = NativeBuildType.RELEASE
    val DEBUG = NativeBuildType.DEBUG

    @Deprecated(BITCODE_EMBEDDING_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    @Suppress("DEPRECATION_ERROR")
    class BitcodeEmbeddingModeDsl {
        val DISABLE = org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.DISABLE
        val BITCODE = org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.BITCODE
        val MARKER = org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.MARKER
    }

    @Suppress("DEPRECATION_ERROR")
    @Deprecated(BITCODE_EMBEDDING_DEPRECATION_MESSAGE, level = DeprecationLevel.ERROR)
    @JvmField
    val EmbedBitcodeMode = BitcodeEmbeddingModeDsl()

    val androidX64 = KonanTarget.ANDROID_X64
    val androidX86 = KonanTarget.ANDROID_X86
    val androidArm32 = KonanTarget.ANDROID_ARM32
    val androidArm64 = KonanTarget.ANDROID_ARM64
    val iosArm64 = KonanTarget.IOS_ARM64
    val iosX64 = KonanTarget.IOS_X64
    val iosSimulatorArm64 = KonanTarget.IOS_SIMULATOR_ARM64
    val watchosArm32 = KonanTarget.WATCHOS_ARM32
    val watchosArm64 = KonanTarget.WATCHOS_ARM64
    val watchosX64 = KonanTarget.WATCHOS_X64
    val watchosSimulatorArm64 = KonanTarget.WATCHOS_SIMULATOR_ARM64
    val watchosDeviceArm64 = KonanTarget.WATCHOS_DEVICE_ARM64
    val tvosArm64 = KonanTarget.TVOS_ARM64
    val tvosX64 = KonanTarget.TVOS_X64
    val tvosSimulatorArm64 = KonanTarget.TVOS_SIMULATOR_ARM64
    val linuxX64 = KonanTarget.LINUX_X64
    val mingwX64 = KonanTarget.MINGW_X64
    val macosX64 = KonanTarget.MACOS_X64
    val macosArm64 = KonanTarget.MACOS_ARM64
    val linuxArm64 = KonanTarget.LINUX_ARM64


    @Deprecated(DEPRECATED_TARGET_MESSAGE, level = DeprecationLevel.ERROR)
    val linuxArm32Hfp = KonanTarget.LINUX_ARM32_HFP

}