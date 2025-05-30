package com.example.fastpark.screens.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fastpark.R
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed


enum class MainPage {
    HOME, SCAN, SETTINGS
}

@Composable
fun BottomBar(
    selectedPage: MainPage,
    onMenuClick: (MainPage) -> Unit
) {
    Box( // Bungkus bar agar ikon bisa keluar tanpa mendorong bar
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // Tetap di bawah meski ikon naik
                .background(
                    brush = Brush.verticalGradient(listOf(DeepRed, BrightRed)),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .height(70.dp), // Tinggi asli BottomBar, tetap stabil
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainMenuItem(
                iconResId = R.drawable.home,
                label = "Home",
                isSelected = selectedPage == MainPage.HOME
            ) { onMenuClick(MainPage.HOME) }

            MainMenuItem(
                iconResId = R.drawable.scan,
                label = "Scan",
                isSelected = selectedPage == MainPage.SCAN
            ) { onMenuClick(MainPage.SCAN) }

            MainMenuItem(
                iconResId = R.drawable.setting,
                label = "Settings",
                isSelected = selectedPage == MainPage.SETTINGS
            ) { onMenuClick(MainPage.SETTINGS) }
        }
    }
}


@Composable
private fun MainMenuItem(
    iconResId: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // animasi ukuran seperti sebelumnya
    val size by animateDpAsState(
        targetValue = if (isSelected) 48.dp else 36.dp,
        label = "sizeAnim"
    )
    // animasi ketinggian (ikon & teks ikut semuanya)
    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-16).dp else 0.dp, // naik 16 dp
        label = "offsetAnim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = offsetY)            // 👈 kunci “naik–turun”
            .zIndex(if (isSelected) 1f else 0f) // tampil di atas bar saat naik
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(y = offsetY)
                .zIndex(if (isSelected) 1f else 0f)
                .size(size)
                .clip(RoundedCornerShape(100))
                .background(if (isSelected) BrightRed else Color.Transparent)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(100)
                )
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(text = label, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
@Preview(showBackground = true)
fun BottomBarPreview() {
        Box(
            modifier = Modifier
                .background(Color.Black) // agar bar merah terlihat jelas
                .padding(top = 32.dp) // beri ruang untuk ikon naik ke atas
        ) {
            BottomBar(
                selectedPage = MainPage.HOME,
                onMenuClick = {}
            )
        }
}




