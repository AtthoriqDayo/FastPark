package com.example.fastpark.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.R

enum class HomeMenu { PARKING, MAIL, HISTORY, CHART }

@Composable
fun MenuGrid(
    selectedMenu: HomeMenu,
    onMenuClick: (HomeMenu) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HomeMenuItem(R.drawable.scooter, "Parking", isSelected = selectedMenu == HomeMenu.PARKING) {
            onMenuClick(HomeMenu.PARKING)
        }
        HomeMenuItem(R.drawable.mail, "Mail", isSelected = selectedMenu == HomeMenu.MAIL) {
            onMenuClick(HomeMenu.MAIL)
        }
        HomeMenuItem(R.drawable.history, "History", isSelected = selectedMenu == HomeMenu.HISTORY) {
            onMenuClick(HomeMenu.HISTORY)
        }
        HomeMenuItem(R.drawable.chart, "Chart", isSelected = selectedMenu == HomeMenu.CHART) {
            onMenuClick(HomeMenu.CHART)
        }
    }
}


@Composable
private fun HomeMenuItem(
    iconResId: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Image(
            painter = painterResource(id = iconResId), // Perbaikan di sini
            contentDescription = label,
            modifier = Modifier
                .size(36.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            modifier = if (isSelected) Modifier
                .padding(top = 4.dp)
                .drawBehind {
                    val strokeWidth = 4.dp.toPx()
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth
                    )
                } else Modifier.padding(top = 4.dp)
        )
    }
}



