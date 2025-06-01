// File: MainActivity.kt (sudah Anda miliki, pastikan setContent benar)
package com.example.fastpark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.fastpark.navigation.MainScreen // Pastikan impor ini benar
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

// import com.google.firebase.auth.ktx.auth // Tidak perlu instance auth di sini
// import com.google.firebase.ktx.Firebase // Tidak perlu instance auth di sini

// private lateinit var auth: FirebaseAuth // Pindahkan ke AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FirebaseApp.initializeApp(this) // Biasanya sudah otomatis jika plugin google-services ada
        // dan tidak ada proses Firebase lain sebelum ini.
        // Jika Anda memiliki beberapa proses, ini mungkin diperlukan.
        // Inisialisasi di ViewModel atau Application class lebih umum.

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            MainScreen() // Panggil NavHost utama Anda di sini
        }
    }
}