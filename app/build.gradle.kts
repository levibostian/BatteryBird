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

    // AndroidX
    //implementation("androidx.core:core-ktx:[1.7,1.12)") // 1.12 requires target API 34. Update when 34 becomes stable
    //implementation("androidx.lifecycle:lifecycle-runtime-ktx:[2.6,)")

    // Compose
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3") // Compose: Android Studio Preview support
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1") // Compose: Optional - Integration with ViewModels
    // Compose: UI Tests
    //androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    //debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Copose: Optional - Integration with activities
    //implementation("androidx.activity:activity-compose")

    // Kotlin datetime util
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:[0.4.0,)")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:[2.0.3,)")

    // WorkManager 
    implementation("androidx.work:work-runtime-ktx:[2.8,)")
    testImplementation("androidx.work:work-testing:[2.8,)")

    // junit, espresso, etc for testing
    testImplementation("junit:junit:[4.13,)")
    testImplementation("androidx.test.ext:junit:[1.1,)")
    testImplementation("org.robolectric:robolectric:[4.5.1,)")
    testImplementation("androidx.test:core:[1.5.0,)")
    testImplementation("androidx.test:runner:[1.5.2,)")
    testImplementation("androidx.test:rules:[1.5.0,)")
    testImplementation(kotlin("test")) // kotlin test assertions
    testImplementation("io.mockk:mockk:[1.12.0,)")
}

// enables dependency locking for dependencies
dependencyLocking { lockAllConfigurations() }
