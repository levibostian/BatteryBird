// Dependency version locking. Tries to make builds more reliable to be reproducible, and allow automatic upgrades easily.
// Learn more: https://github.com/peter-evans/gradle-auto-dependency-updates
// buildscript classpath is intentionally NOT locked: Gradle strictly pins the kotlin-stdlib/reflect versions that match its own embedded Kotlin on the buildscript classpath, so every Gradle version bump makes the committed buildscript-gradle.lockfile stale and breaks ALL builds (CI + Renovate's lockfile update) until a human regenerates it. Leaving the classpath unlocked lets Gradle's own strict constraint win, so dependency-update PRs pass CI without manual lockfile fixes. App/store module lockfiles below keep reproducibility for the actual dependency graph.

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
    id("com.android.application").version("9.4.1").apply(false)
    id("com.android.library").version("9.4.1").apply(false)

    // Android Jetpack compose only supports certain versions of Kotlin. We must use hard-coded version to stay compatible with it.
    // Use this chart to see when the version gets increased: https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    val kotlinVersion = "2.4.20"
    id("org.jetbrains.kotlin.plugin.compose").version(kotlinVersion).apply(false)
    kotlin("plugin.serialization").version(kotlinVersion).apply(false)
    kotlin("multiplatform").version(kotlinVersion).apply(false)
    id("com.android.kotlin.multiplatform.library").version("9.4.1").apply(false)
    id("app.cash.sqldelight").version("2.4.0").apply(false)
}