// Dependency version locking. Tries to make builds more reliable to be reproducible, and allow automatic upgrades easily.
// Learn more: https://github.com/peter-evans/gradle-auto-dependency-updates

buildscript {
    configurations.classpath {
        resolutionStrategy.activateDependencyLocking() // part of dependency locking. allows locking for buildscript.
    }
}

allprojects {
    dependencyLocking { lockAllConfigurations() } // enables dependency locking for all modules in the project. Except for buildscript dependencies.

    configurations.all {
        resolutionStrategy {
            // Filters dependency versions based on criteria.
            // Called a Component Selection Rule: https://docs.gradle.org/current/userguide/dynamic_versions.html#sec:component_selection_rules
            componentSelection {
                all {
                    // We want stable (or release candidate), only.
                    // You can make exclusions if you want by adding conditionals for candidate group, name, version, etc.
                    if (candidate.version.contains("-alpha") || candidate.version.contains("-beta")) {
                        reject("version contains alpha or beta") // version is determiend by the highest version not rejected.
                    }
                }
            }
        }
    }
}

plugins {
    // android gradle plugin has a limit to what is supported by IDE. 
    // https://developer.android.com/studio/releases#android_gradle_plugin_and_android_studio_compatibility
    id("com.android.application").version("8.0.2").apply(false)
    id("com.android.library").version("8.0.2").apply(false)

    // Android Jetpack compose only supports certain versions of Kotlin. We must use hard-coded version to stay compatible with it.
    // Use this chart to see when the version gets increased: https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    val kotlinVersion = "1.8.21"
    id("org.jetbrains.kotlin.android").version(kotlinVersion).apply(false)
    kotlin("plugin.serialization").version(kotlinVersion).apply(false)
    kotlin("multiplatform").version(kotlinVersion).apply(false)
    kotlin("native.cocoapods").version(kotlinVersion).apply(false)
    id("app.cash.sqldelight").version("+").apply(false)
}