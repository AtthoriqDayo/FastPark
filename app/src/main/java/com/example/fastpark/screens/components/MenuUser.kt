package com.example.fastpark.screens.components

// Hapus import Image jika tidak lagi digunakan secara langsung di HomeMenuItem
// import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons // Import ini
import androidx.compose.material.icons.filled.History // Import ikon History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalParking // Import ikon Parking
import androidx.compose.material.icons.filled.Mail // Import ikon Mail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon // Import Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Pertahankan enum HomeUserMenu yang ada di sini
enum class HomeUserMenu { HOMEUSER, PARKING, MAIL, HISTORY }

@Composable
fun MenuUser(
    selectedMenu: HomeUserMenu,
    onMenuClick: (HomeUserMenu) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeMenuItem(
                    imageVector = Icons.Default.Home,
                    label = "Home",
                    modifier = Modifier.weight(1f),
                    onClick = { onMenuClick(HomeUserMenu.HOMEUSER) }
                )
                HomeMenuItem(
                    imageVector = Icons.Default.LocalParking,
                    label = "Parking",
                    modifier = Modifier.weight(1f),
                    onClick = { onMenuClick(HomeUserMenu.PARKING) }
                )
                HomeMenuItem(
                    imageVector = Icons.Default.Mail,
                    label = "Mail",
                    modifier = Modifier.weight(1f),
                    onClick = { onMenuClick(HomeUserMenu.MAIL) }
                )
                HomeMenuItem(
                    imageVector = Icons.Default.History,
                    label = "History",
                    modifier = Modifier.weight(1f),
                    onClick = { onMenuClick(HomeUserMenu.HISTORY) }
                )
            }
        }
    }
}

@Composable
private fun HomeMenuItem(
    imageVector: ImageVector,
    label: String,
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
            tint = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}