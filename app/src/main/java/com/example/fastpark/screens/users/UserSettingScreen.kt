
// File: UserSettingScreen.kt
package com.example.fastpark.screens.users // Atau package yang sesuai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fastpark.navigation.AppDestinations // Pastikan import ini benar
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel

@Composable
fun UserSettingScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userData by authViewModel.userData.observeAsState()
    val userName = userData?.displayName ?: "Pengguna"
    val userEmail = userData?.email ?: "Tidak ada email"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Sesuaikan tinggi jika perlu
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DeepRed, BrightRed) // Atau warna tema pengguna
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 30.dp)
                    .clickable { navController.popBackStack() }, // Tombol kembali
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pengaturan Pengguna",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // Informasi pengguna di header
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top= 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(80.dp), // Ukuran Avatar sedikit lebih kecil
                    tint = Color.Black // Avatar putih di header merah
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(userEmail, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Jarak setelah header (tidak perlu offset negatif jika avatar di dalam header)

        // Menu buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserSettingButton("Profil Anda") { /* TODO: Navigasi ke edit profil pengguna */ }
            UserSettingButton("Notifikasi") { /* TODO: Navigasi ke pengaturan notifikasi */ }
            UserSettingButton("Keamanan Akun") { /* TODO: Navigasi ke pengaturan keamanan */ }
            UserSettingButton("Tentang Aplikasi") { /* TODO: Navigasi ke halaman tentang */ }
            UserSettingButton("Logout") {
                authViewModel.signOut()
                // Navigasi ke halaman login dan bersihkan backstack
                navController.navigate(AppDestinations.LOGIN_ROUTE) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
    }
}

@Composable
fun UserSettingButton(text: String, onClick: () -> Unit) { // Bisa pakai ulang dari worker atau buat versi user
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)) // Warna berbeda sedikit
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}