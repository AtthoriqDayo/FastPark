package com.example.fastpark.screens.components.admin

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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SettingsSuggest
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
import com.example.fastpark.screens.theme.BrightRed

enum class AdminMainPage(val label: String, val icon: ImageVector) {
    HOME_ADMIN("Home", Icons.Default.Home),
    ACCOUNT_MANAGEMENT("Akun", Icons.Default.People),
    SETTINGS("Pengaturan", Icons.Default.SettingsSuggest)
}


@Composable
fun AdminBottomBar(
    selectedPage: AdminMainPage, // Halaman yang sedang dipilih
    onPageSelected: (AdminMainPage) -> Unit, // Callback saat halaman baru dipilih
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
                    .height(70.dp), // Tinggi Row konten BottomBar
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminMainPage.entries.forEach { page ->
                    AdminMainMenuItem(
                        imageVector = page.icon,
                        label = page.label,
                        isSelected = selectedPage == page
                    ) { onPageSelected(page) }
                }
            }
        }
    }
}


@Composable
private fun AdminMainMenuItem(
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
            .zIndex(if (isSelected) 1f else 0f) // Ikon terpilih di atas yang lain
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(100))
                .background(Color.Transparent) // Tetap transparan untuk background ikon
                .border( // Border juga transparan, hanya ada jika Anda ingin efek visual border
                    width = 0.dp, // Ubah menjadi 3.dp dan berikan warna jika ingin border
                    color = Color.Transparent,
                    shape = RoundedCornerShape(100)
                )
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = label,
                tint = if (isSelected) BrightRed else Color.Black, // Warna ikon
                modifier = Modifier.size(size)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) BrightRed else Color.Black // Warna teks label
        )
    }
}