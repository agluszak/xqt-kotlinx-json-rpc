object BuildConfiguration {
    /**
     * The version of the JVM to target by the Kotlin compiler.
     */
    val jvmTarget = System.getProperty("jvm.target") ?: "11"

    /**
     * Should the build process download node-js if it is not present? (default: true)
     */
    val downloadNodeJs = System.getProperty("nodejs.download") != "false"

    /**
     * The name of the Operating System the build is running on.
     */
    val hostOsName = System.getProperty("os.name")
}
