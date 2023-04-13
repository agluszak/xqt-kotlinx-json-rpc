import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    kotlin("multiplatform") version Version.kotlin
    kotlin("plugin.serialization") version Version.kotlin
    id("maven-publish")
}

group = "io.github.rhdunn"
version = "0.1-SNAPSHOT"

rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().download = BuildConfiguration.downloadNodeJs
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = BuildConfiguration.jvmTarget
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform() // JUnit 5
        }
    }

    js(BOTH) {
        browser {
            testTask {
                useKarma {
                    when (System.getProperty("js.browser")) {
                        "Chrome" -> useChromeHeadless()
                        "Chrome Canary" -> useChromeCanaryHeadless()
                        "Chromium" -> useChromiumHeadless()
                        "Firefox" -> useFirefoxHeadless()
                        "Firefox Aurora" -> useFirefoxAuroraHeadless()
                        "Firefox Developer" -> useFirefoxDeveloperHeadless()
                        "Firefox Nightly" -> useFirefoxNightlyHeadless()
                        "Phantom JS" -> usePhantomJS()
                        "Safari" -> useSafari()
                        else -> when (BuildConfiguration.hostOs) {
                            HostOs.MacOsX -> useSafari()
                            else -> useFirefoxHeadless()
                        }
                    }
                }
            }
        }

        nodejs {
        }
    }

    val nativeTarget = when(BuildConfiguration.hostOs) {
        HostOs.Windows -> mingwX64("native")
        HostOs.Linux -> linuxX64("native")
        HostOs.MacOsX -> macosX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.kotlinSerialization}")
            }
        }
        val commonTest by getting {
            kotlin.srcDir("commonTest")
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":src:xqt-kotlinx-test"))
            }
        }

        val jvmMain by getting {
            kotlin.srcDir("jvmMain")
        }
        val jvmTest by getting {
            kotlin.srcDir("jvmTest")
        }

        val jsMain by getting {
            kotlin.srcDir("jsMain")
        }
        val jsTest by getting {
            kotlin.srcDir("jsTest")
        }

        val nativeMain by getting {
            kotlin.srcDir("nativeMain")
            when (BuildConfiguration.hostOs) {
                HostOs.Windows -> kotlin.srcDir("windowsMain")
                else -> kotlin.srcDir("posixMain")
            }
        }
        val nativeTest by getting {
            kotlin.srcDir("nativeTest")
        }
    }
}
