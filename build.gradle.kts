// Dependency version locking. Tries to make builds more reliable to be reproducible, and allow automatic upgrades easily.
// Learn more: https://github.com/peter-evans/gradle-auto-dependency-updates
dependencyLocking { lockAllConfigurations() }

buildscript {
    configurations.classpath {
        // part of dependency locking. allows locking plugins.
        resolutionStrategy.activateDependencyLocking()
    }
}
plugins {
    // android gradle plugin has a limit to what is supported by IDE. 
    // https://developer.android.com/studio/releases#android_gradle_plugin_and_android_studio_compatibility
    id("com.android.application").version("8.0.1").apply(false)
    id("com.android.library").version("8.0.1").apply(false)
    // Android Jetpack compose only supports certain versions of Kotlin. We must use hard-coded version to stay compatible with it.
    // Use this chart to see when the version gets increased: https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    id("org.jetbrains.kotlin.android").version("1.8.21").apply(false)
    kotlin("plugin.serialization").version("1.8.21").apply(false)
}