import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "earth.levi.app"
    compileSdk = 33
    compileSdkPreview = "UpsideDownCake"

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

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
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
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:[2023.04.01,)")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    // Compose: UI framework
    implementation("androidx.compose.material3:material3")
    // Compose: Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Compose: UI Tests
    //androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    //debugImplementation("androidx.compose.ui:ui-test-manifest")
    // Copose: Optional - Integration with activities
    //implementation("androidx.activity:activity-compose:1.6.1")
    // Compose: Optional - Integration with ViewModels
    // implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    // Kotlin datetime util
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:[0.4.0,)")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:[2.0.3,)")

    // Coroutine support for Shared Prefs
    implementation("com.fredporciuncula:flow-preferences:[1.9.1,)")

    // AndroidX
    implementation("androidx.core:core-ktx:[1.7,)")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:[2.6,)")
    implementation("androidx.activity:activity-compose:[1.7,)")

    // WorkManager 
    implementation("androidx.work:work-runtime-ktx:[2.8,)")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:[1.5,)")

    // junit, espresso
    testImplementation("junit:junit:[4.13,)")
    androidTestImplementation("androidx.test.ext:junit:[1.1,)")
    androidTestImplementation("androidx.test.espresso:espresso-core:[3.5,)")
}

// enables dependency locking for dependencies
dependencyLocking { lockAllConfigurations() }