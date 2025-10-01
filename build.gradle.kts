plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.cs2battlegame"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cs2battlegame"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material:material:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.animation:animation:1.5.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.animation:animation:1.5.4")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")

    // Compose
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")
    implementation("androidx.compose.material:material:1.5.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.6.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling:1.5.3")
}
