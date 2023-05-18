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
    id("org.jetbrains.kotlin.android").version("[1.7,)").apply(false)
}