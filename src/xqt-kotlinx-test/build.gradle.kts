// Copyright (C) 2023 Reece H. Dunn. SPDX-License-Identifier: Apache-2.0
@file:Suppress("KDocMissingDocumentation")

import io.github.rhdunn.gradle.dsl.*
import io.github.rhdunn.gradle.maven.SupportedVariants
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

buildscript {
    dependencies {
        classpath(Dependency.DokkaBase)
    }
}

plugins {
    kotlin("multiplatform") version Version.Plugin.KotlinMultiplatform
    kotlin("plugin.serialization") version Version.Plugin.KotlinSerialization
    id("org.jetbrains.dokka") version Version.Plugin.Dokka
    id("signing")
}

// region Kotlin Multiplatform (Common)

kotlin.sourceSets {
    commonMain.kotlin.srcDir("commonMain")
}

// endregion
// region Kotlin JVM

val supportedJvmVariants = BuildConfiguration.jvmVariants(project)

val javaVersion = BuildConfiguration.javaVersion(project)
if (javaVersion !in ProjectMetadata.BuildTargets.JvmTargets)
    throw GradleException("The specified jvm.target is not in the configured project metadata.")

lateinit var javaTarget: KotlinJvmTarget
ProjectMetadata.BuildTargets.JvmTargets.forEach { jvmTarget ->
    val jvmName = supportedJvmVariants.jvmPublication(jvmTarget, javaVersion) ?: return@forEach
    val target: KotlinJvmTarget = kotlin.jvm(jvmName) {
        compilations.all {
            kotlinOptions.jvmTarget = jvmTarget.toString()
        }

        if (jvmTarget == javaVersion) {
            withJava()
        }

        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jvmTarget.majorVersion.toInt())
        }
    }

    if (jvmTarget == javaVersion)
        javaTarget = target
}

if (supportedJvmVariants !== SupportedVariants.None) {
    kotlin.sourceSets {
        jvmMain(javaTarget).kotlin.srcDir("jvmMain")

        jvmMain(javaTarget).dependencies {
            implementation(Dependency.JUnitJupiterApi)
        }
    }
}

// endregion
// region Kotlin JS

rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().download = BuildConfiguration.nodeJsDownload(project)
}

kotlin.js(KotlinJsCompilerType.BOTH).browser {
}

kotlin.js(KotlinJsCompilerType.BOTH).nodejs {
}

kotlin.sourceSets {
    jsMain.kotlin.srcDir("jsMain")
}

// endregion
// region Kotlin Native

val supportedKonanVariants = BuildConfiguration.konanVariants(project)
val konanBuildTarget = BuildConfiguration.konanTarget(project)

lateinit var nativeTarget: KotlinNativeTarget
ProjectMetadata.BuildTargets.KonanTargets.forEach { konanTarget ->
    val nativeName = supportedKonanVariants.nativePublication(konanTarget, konanBuildTarget) ?: return@forEach

    // https://kotlinlang.org/docs/native-target-support.html
    val target = when (konanTarget) {
        KonanTarget.ANDROID_ARM32 -> kotlin.androidNativeArm32(nativeName) // Tier 3
        KonanTarget.ANDROID_ARM64 -> kotlin.androidNativeArm64(nativeName) // Tier 3
        KonanTarget.ANDROID_X64 -> kotlin.androidNativeX64(nativeName) // Tier 3
        KonanTarget.ANDROID_X86 -> kotlin.androidNativeX86(nativeName) // Tier 3
        KonanTarget.IOS_ARM32 -> kotlin.iosArm32(nativeName) // Deprecated, to be removed in 1.9.20
        KonanTarget.IOS_ARM64 -> kotlin.iosArm64(nativeName) // Tier 2
        KonanTarget.IOS_SIMULATOR_ARM64 -> kotlin.iosSimulatorArm64(nativeName) // Tier 1
        KonanTarget.IOS_X64 -> kotlin.iosX64(nativeName) // Tier 1
        KonanTarget.LINUX_ARM32_HFP -> kotlin.linuxArm32Hfp(nativeName) // Deprecated, to be removed in 1.9.20
        KonanTarget.LINUX_ARM64 -> kotlin.linuxArm64(nativeName) // Tier 2
        KonanTarget.LINUX_MIPS32 -> kotlin.linuxMips32(nativeName) // Deprecated, to be removed in 1.9.20
        KonanTarget.LINUX_MIPSEL32 -> kotlin.linuxMipsel32(nativeName) // Deprecated, to be removed in 1.9.20
        KonanTarget.LINUX_X64 -> kotlin.linuxX64(nativeName) // Tier 1 ; native host
        KonanTarget.MACOS_ARM64 -> kotlin.macosArm64(nativeName) // Tier 1 ; native host
        KonanTarget.MACOS_X64 -> kotlin.macosX64(nativeName) // Tier 1 ; native host
        KonanTarget.MINGW_X64 -> kotlin.mingwX64(nativeName) // Tier 3 ; native host
        KonanTarget.MINGW_X86 -> kotlin.mingwX86(nativeName) // Deprecated, to be removed in 1.9.20
        KonanTarget.TVOS_ARM64 -> kotlin.tvosArm64(nativeName) // Tier 2
        KonanTarget.TVOS_SIMULATOR_ARM64 -> kotlin.tvosSimulatorArm64(nativeName) // Tier 2
        KonanTarget.TVOS_X64 -> kotlin.tvosX64(nativeName) // Tier 2
        KonanTarget.WASM32 -> kotlin.wasm32(nativeName) // Deprecated, to be removed in 1.9.20
        KonanTarget.WATCHOS_ARM32 -> kotlin.watchosArm32(nativeName) // Tier 2
        KonanTarget.WATCHOS_ARM64 -> kotlin.watchosArm64(nativeName) // Tier 2
        KonanTarget.WATCHOS_SIMULATOR_ARM64 -> kotlin.watchosSimulatorArm64(nativeName) // Tier 2
        KonanTarget.WATCHOS_X64 -> kotlin.watchosX64(nativeName) // Tier 2
        KonanTarget.WATCHOS_X86 -> kotlin.watchosX86(nativeName) // Deprecated, to be removed in 1.9.20
        is KonanTarget.ZEPHYR -> throw GradleException("Kotlin/Native build target 'zephyr' is not supported.")
    }

    if (konanTarget == konanBuildTarget)
        nativeTarget = target
}

if (supportedKonanVariants !== SupportedVariants.None) {
    kotlin.sourceSets {
        nativeMain(nativeTarget).kotlin.srcDir("nativeMain")
    }
}

// endregion
// region Documentation

tasks.withType<DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        suppress.set(true)
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        suppress.set(true)
    }
}

// endregion
