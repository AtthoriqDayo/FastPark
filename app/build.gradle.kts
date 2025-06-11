plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services") // Plugin Google Services
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
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
    implementation(libs.androidx.appcompat)
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
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.functions.ktx)

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

    implementation(libs.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation (libs.com.google.firebase.firebase.auth.ktx)
    implementation (libs.androidx.lifecycle.runtime.compose)
    implementation (libs.zxing.android.embedded)
    implementation (libs.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.barcode.scanning) // Cek versi terbaru

    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx.v280)
    implementation(libs.okhttp)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio) // Engine untuk Ktor, cocok untuk coroutines
    implementation(libs.ktor.client.content.negotiation) // Untuk parsing JSON
    implementation(libs.ktor.serialization.kotlinx.json) // Plugin JSON
    implementation(libs.ktor.client.logging)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.accompanist.webview)
    implementation (libs.mpandroidchart)


}