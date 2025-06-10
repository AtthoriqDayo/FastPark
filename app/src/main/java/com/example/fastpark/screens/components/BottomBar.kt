package com.example.fastpark.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.screens.theme.BrightRed

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
            .padding(horizontal = 10.dp),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun MainMenuItem(
    imageVector: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .sizeIn(minWidth = 80.dp, minHeight = 80.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = label,
            modifier = Modifier.size(36.dp),
            tint = if (isSelected) BrightRed else Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) BrightRed else Color.Black
        )
    }
}