plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services") // Plugin Google Services
}

android {
    namespace = "com.example.fastpark"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fastpark"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.firebase.appcheck.playintegrity)
    // Firebase BoM (Bill of Materials)
    implementation(platform(libs.firebase.bom.v3310)) // Gunakan versi terbaru

    // Firebase Authentication
    implementation(libs.firebase.auth.ktx)

    // Firebase Realtime Database atau Firestore (pilih salah satu)
    implementation(libs.firebase.database.ktx) // Untuk Realtime Database
    // atau
    implementation(libs.firebase.firestore.ktx) // Untuk Firestore

    implementation (libs.play.services.auth)
    implementation (libs.google.firebase.auth.ktx)
    implementation(libs.androidx.constraintlayout)


    implementation(libs.androidx.core.ktx.v190) // Versi bisa berbeda
    implementation(libs.androidx.lifecycle.runtime.ktx.v261) // Versi bisa berbeda
    implementation(libs.androidx.activity.compose.v170) // Versi bisa berbeda
    implementation(platform(libs.androidx.compose.bom.v20230300)) // Versi bisa berbeda
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3) // Atau material jika Anda menggunakan Material 2
    implementation(libs.androidx.runtime.livedata) // Untuk observeAsState
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Untuk viewModel()
    implementation (libs.com.google.firebase.firebase.auth.ktx)
    implementation (libs.androidx.lifecycle.runtime.compose)


}