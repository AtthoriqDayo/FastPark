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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.R

enum class HomeMenu { PARKING, MAIL, HISTORY, CHART }

@Composable
fun MenuGrid(
    onMenuClick: (HomeMenu) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HomeMenuItem(R.drawable.scooter, "Parking") { onMenuClick(HomeMenu.PARKING) }
        HomeMenuItem(R.drawable.mail,    "Mail")    { onMenuClick(HomeMenu.MAIL) }
        HomeMenuItem(R.drawable.history, "History") { onMenuClick(HomeMenu.HISTORY) }
        HomeMenuItem(R.drawable.chart,   "Chart")   { onMenuClick(HomeMenu.CHART) }
    }
}

@Composable
private fun HomeMenuItem(
    iconResId: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = label,
            modifier = Modifier.size(36.dp)
        )
        Text(label, fontSize = 12.sp)
    }
}