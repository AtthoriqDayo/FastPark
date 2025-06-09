package com.example.fastpark.screens.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fastpark.screens.theme.BrightRed // Pastikan BrightRed didefinisikan di sini atau import dari tempat lain

// --- DEFINISI ENUM MAINPAGE ---
enum class MainPage(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SCAN("Scan", Icons.Default.QrCodeScanner),
    SETTINGS("Setting", Icons.Default.Settings)
}

// --- DEFINISI COMPOSABLE BOTTOMBAR ---
@Composable
fun BottomBar(
    selectedPage: MainPage, // Parameter untuk halaman yang sedang dipilih
    onPageSelected: (MainPage) -> Unit, // Callback saat halaman baru dipilih
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp + 16.dp), // Total tinggi termasuk offset ikon
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp), // Tinggi Row konten BottomBar
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Iterasi melalui semua entri dalam enum MainPage
                MainPage.entries.forEach { page ->
                    // Memanggil MainMenuItem untuk setiap halaman
                    MainMenuItem(
                        imageVector = page.icon,   // Mengambil ikon dari enum
                        label = page.label,       // Mengambil label dari enum
                        isSelected = selectedPage == page, // Menentukan apakah item ini yang sedang dipilih
                        onClick = { onPageSelected(page) } // Meneruskan callback saat item diklik
                    )
                }
            }
        }
    }
}

// --- DEFINISI COMPOSABLE MAINMENUITEM ---
@Composable
private fun MainMenuItem(
    imageVector: ImageVector, // Ikon untuk item menu
    label: String,            // Teks label untuk item menu
    isSelected: Boolean,      // Menunjukkan apakah item ini sedang dipilih
    onClick: () -> Unit       // Callback saat item diklik
) {
    // Animasi ukuran ikon saat dipilih/tidak dipilih
    val size by animateDpAsState(
        targetValue = if (isSelected) 45.dp else 38.dp,
        label = "sizeAnim"
    )
    // Animasi offset vertikal ikon saat dipilih/tidak dipilih
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-16).dp else 0.dp,
        label = "offsetAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = offsetY) // Mengatur offset vertikal
            .zIndex(if (isSelected) 1f else 0f) // Menempatkan ikon terpilih di atas
            .clickable { onClick() } // Membuat item bisa diklik
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size) // Mengatur ukuran Box
                .clip(RoundedCornerShape(100)) // Membuat bentuk lingkaran
                .background(Color.Transparent) // Latar belakang transparan
                .border( // Border, saat ini tidak terlihat karena width 0.dp
                    width = 0.dp,
                    color = Color.Transparent,
                    shape = RoundedCornerShape(100)
                )
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = label,
                tint = if (isSelected) BrightRed else Color.Black, // Warna ikon berubah saat dipilih
                modifier = Modifier.size(size) // Ukuran ikon sama dengan Box
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) BrightRed else Color.Black // Warna teks berubah saat dipilih
        )
    }
}