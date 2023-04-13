import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    kotlin("multiplatform") version Version.kotlin
    kotlin("plugin.serialization") version Version.kotlin
}

rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsRootExtension>().download = BuildConfiguration.downloadNodeJs
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = BuildConfiguration.jvmTarget
        }
        withJava()
    }

    js(BOTH) {
        browser {
        }

        nodejs {
        }
    }

    val nativeTarget = when {
        BuildConfiguration.hostOsName == "Mac OS X" -> macosX64("native")
        BuildConfiguration.hostOsName == "Linux" -> linuxX64("native")
        BuildConfiguration.hostOsName.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("commonMain")
        }

        val jvmMain by getting {
            kotlin.srcDir("jvmMain")
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:${Version.junit}")
            }
        }

        val jsMain by getting {
            kotlin.srcDir("jsMain")
        }

        val nativeMain by getting {
            kotlin.srcDir("nativeMain")
        }
    }
}
