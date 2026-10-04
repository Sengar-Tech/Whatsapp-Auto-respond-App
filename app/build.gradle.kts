plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)             // KSP generates Room DAO code at compile time
}

android {
    namespace   = "com.yourfirm.autoreply"
    compileSdk  = 35

    defaultConfig {
        applicationId   = "com.yourfirm.autoreply"
        minSdk          = 26            // Android 8.0 — needed for NotificationListenerService extras
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true      // Shrink + obfuscate code for Play Store
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true              // Generates type-safe binding classes for every XML layout
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.fragment.ktx)

    // Lifecycle (ViewModel + coroutine scope in fragments)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)

    // Room — local SQLite database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)             // Generates DAO implementation code

    // Retrofit + OkHttp — HTTP client for Meta Cloud API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Encrypted SharedPreferences — stores Meta API token securely
    implementation(libs.security.crypto)

    // Coroutines — async work without callbacks
    implementation(libs.coroutines.android)
}
