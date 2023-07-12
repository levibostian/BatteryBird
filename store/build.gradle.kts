plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("app.cash.sqldelight")
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

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
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("app.cash.sqldelight:native-driver:+")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "earth.levi.batterybird.store"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
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
        }
    }
}
