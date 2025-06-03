package com.example.fastpark.screens.components

// Import DarkGray jika ingin menggunakan warna abu-abu gelap untuk ikon tidak aktif
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fastpark.screens.theme.BrightRed

enum class MainPage {
    HOME, SCAN, SETTINGS
}

@Composable
fun BottomBar(
    selectedPage: MainPage,
    onMenuClick: (MainPage) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp + 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainMenuItem(
                    imageVector = Icons.Default.Home,
                    label = "Home",
                    isSelected = selectedPage == MainPage.HOME
                ) { onMenuClick(MainPage.HOME) }

                MainMenuItem(
                    imageVector = Icons.Default.QrCodeScanner,
                    label = "Scan",
                    isSelected = selectedPage == MainPage.SCAN
                ) { onMenuClick(MainPage.SCAN) }

                MainMenuItem(
                    imageVector = Icons.Default.Settings,
                    label = "Settings",
                    isSelected = selectedPage == MainPage.SETTINGS
                ) { onMenuClick(MainPage.SETTINGS) }
            }
        }
    }
}


@Composable
private fun MainMenuItem(
    imageVector: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size by animateDpAsState(
        targetValue = if (isSelected) 45.dp else 38.dp,
        label = "sizeAnim"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-16).dp else 0.dp,
        label = "offsetAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = offsetY)
            .zIndex(if (isSelected) 1f else 0f)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size) // Ukuran Box dianimasikan
                .clip(RoundedCornerShape(100))
                .background(if (isSelected) Color.Transparent else Color.Transparent)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color.Transparent else Color.Transparent,
                    shape = RoundedCornerShape(100)
                )
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = label,
                // --- PERUBAHAN DI SINI: Warna ikon berdasarkan isSelected ---
                tint = if (isSelected) BrightRed else Color.Black, // Putih saat aktif, DarkGray saat tidak
                modifier = Modifier.size(size) // --- PERUBAHAN DI SINI: Ukuran ikon ikut animasi Box ---
            )
        }
        Text(text = label, fontSize = 12.sp, color = Color.Black) // Warna teks tetap hitam (atau bisa disesuaikan juga)
    }
}

@Composable
@Preview(showBackground = true)
fun BottomBarPreview() {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .padding(top = 32.dp)
    ) {
        BottomBar(
            selectedPage = MainPage.HOME,
            onMenuClick = {}
        )
    }
}