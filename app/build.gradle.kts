plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "app"
    compileSdk = 33

    defaultConfig {
        applicationId = "earth.levi.bluetoothbattery"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("ANDROID_SIGNING_KEY_FILE_PATH") ?: "/fake/path") // gradle throws error in file() if env var not found. provide default vale to avoid error during development
            keyAlias = "upload"
            storePassword = System.getenv("ANDROID_SIGNING_KEY_STORE_PASSWORD")
            keyPassword = System.getenv("ANDROID_SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug" // allows you to have prod and debug builds of app on same device
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // desugaring required by kotlinx datetime because android SDK version is lower then 26
        // https://github.com/Kotlin/kotlinx-datetime#using-in-your-projects
        // https://developer.android.com/studio/write/java8-support#library-desugaring
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        // This maps to kotlin compiler version https://developer.android.com/jetpack/androidx/releases/compose-kotlin
        kotlinCompilerExtensionVersion = "1.4.8"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    sourceSets {
        getByName("main") {
            java.setSrcDirs(listOf("src/main/kotlin"))
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true // from: https://robolectric.org/getting-started/
        }
    }
}

dependencies {
    implementation(project(mapOf("path" to ":store")))

    // Compose
    implementation("androidx.navigation:navigation-compose:+")
    implementation("androidx.compose.material3:material3:+")
    implementation("androidx.compose.ui:ui-tooling-preview:+") // Compose: Android Studio Preview support
    debugImplementation("androidx.compose.ui:ui-tooling:+")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:+") // Compose: Optional - Integration with ViewModels
    // Compose: UI Tests
    //androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    //debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Copose: Optional - Integration with activities

    // Kotlin datetime util
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:+")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:+")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:+")
    testImplementation("androidx.work:work-testing:+")

    // junit, espresso, etc for testing
    testImplementation("junit:junit:+")
    testImplementation("androidx.test.ext:junit:+")
    testImplementation("org.robolectric:robolectric:+")
    testImplementation("androidx.test:core:+")
    testImplementation("androidx.test:runner:+")
    testImplementation("androidx.test:rules:+")
    testImplementation(kotlin("test")) // kotlin test assertions
    testImplementation("io.mockk:mockk:+")
}
