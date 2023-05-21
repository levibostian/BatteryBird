import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
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
    implementation("androidx.core:core-ktx:[1.7,)")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:[2.6,)")
    implementation("androidx.activity:activity-compose:[1.7,)")
    
    // Compose 
    val compose_ui_version = "[1.4,)"
    implementation("androidx.compose.material:material:[1.2,)")
    implementation("androidx.compose.ui:ui:$compose_ui_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_ui_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_ui_version")
    
    // WorkManager 
    implementation("androidx.work:work-runtime-ktx:[2.8,)")

    // junit, espresso
    testImplementation("junit:junit:[4.13,)")
    androidTestImplementation("androidx.test.ext:junit:[1.1,)")
    androidTestImplementation("androidx.test.espresso:espresso-core:[3.5,)")
}

// enables dependency locking for dependencies
dependencyLocking { lockAllConfigurations() }