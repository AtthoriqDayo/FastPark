package com.example.fastpark.screens.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.R
import com.example.fastpark.ui.theme.BrightRed
import com.example.fastpark.ui.theme.DeepRed


enum class MainPage {
    HOME, SCAN, SETTINGS
}

@Composable
fun BottomBar(
    selectedPage: MainPage,
    onMenuClick: (MainPage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(
                brush = Brush.verticalGradient
                    (listOf(DeepRed, BrightRed))
            ),
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

@Composable
private fun MainMenuItem(
    iconResId: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val size by animateDpAsState(targetValue = if (isSelected) 48.dp else 36.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(100))
                .background(if (isSelected) Color.White else Color.Transparent)
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


