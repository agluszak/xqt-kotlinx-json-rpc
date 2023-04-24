// Copyright (C) 2023 Reece H. Dunn. SPDX-License-Identifier: Apache-2.0

/**
 * Versions of the various plugins and libraries used by the project.
 */
object Dependency {
    /**
     * The `dokka-base` library.
     */
    val DokkaBase = "org.jetbrains.dokka:dokka-base:${Version.Dependency.DokkaBase}"

    /**
     * The `junit-jupiter-api` library.
     */
    val JUnitJupiterApi = "org.junit.jupiter:junit-jupiter-api:${Version.Dependency.JUnit}"

    /**
     * The `kotlinx-serialization-json` library.
     */
    val KotlinxSerializationJson =
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${Version.Dependency.KotlinxSerializationJson}"
}