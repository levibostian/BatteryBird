plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // Apply the default hierarchy template to create iOS source sets automatically
    applyDefaultHierarchyTemplate()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "store"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:+")
                implementation("app.cash.sqldelight:coroutines-extensions:+")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:+") // required by sqldelight coroutines-extensions
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:android-driver:+")
            }
        }
        val androidUnitTest by getting // there is also androidInstrumentedTest
        val iosMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:native-driver:+")
            }
        }
        val iosTest by getting
    }
}

android {
    namespace = "earth.levi.batterybird.store"
    compileSdk = 36
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("earth.levi.batterybird.store")
            // define what version of sqlite to enable some features of sqldelight: https://github.com/cashapp/sqldelight/issues/1436
            // find version of sqlite can use for Android: https://developer.android.com/reference/android/database/sqlite/package-summary
            // for ios: https://stackoverflow.com/questions/14288128/what-version-of-sqlite-does-ios-provide
            dialect("app.cash.sqldelight:sqlite-3-24-dialect:+")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
        }
    }
}
