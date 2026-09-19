plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("app.cash.sqldelight")
}

kotlin {
    android {
        namespace = "earth.levi.batterybird.store"
        compileSdk = 36
        minSdk = 21

        // legacy androidUnitTest -> androidHostTest in AGP9 new DSL. Mirrors app's unit test config.
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvmToolchain(21)

    // Apply the default hierarchy template to create iOS source sets automatically
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0-0.6.x-compat")
                implementation("app.cash.sqldelight:coroutines-extensions:2.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0") // required by sqldelight coroutines-extensions
            }
        }
        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain = getByName("androidMain") {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:2.4.0")
            }
        }
        val androidHostTest = getByName("androidHostTest") // there is also androidDeviceTest (instrumented)
        val iosMain = getByName("iosMain") {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:2.4.0")
            }
        }
        val iosTest = getByName("iosTest")
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("earth.levi.batterybird.store")
            // define what version of sqlite to enable some features of sqldelight: https://github.com/cashapp/sqldelight/issues/1436
            // find version of sqlite can use for Android: https://developer.android.com/reference/android/database/sqlite/package-summary
            // for ios: https://stackoverflow.com/questions/14288128/what-version-of-sqlite-does-sqlite-provide
            dialect("app.cash.sqldelight:sqlite-3-24-dialect:2.4.0")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}