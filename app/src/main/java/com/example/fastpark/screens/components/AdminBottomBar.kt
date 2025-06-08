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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.People
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

object AdminDestinations {
    const val ACCOUNT_MANAGEMENT_ROUTE = "admin_account_management"
    const val FINANCIAL_STATS_ROUTE = "admin_financial_stats"
    const val CUSTOMER_STATS_ROUTE = "admin_customer_stats"
    const val PARKING_MANAGEMENT_ROUTE = "admin_parking_management"
}

enum class AdminMainPage(val label: String, val icon: ImageVector, val route: String) {
    ACCOUNT_MANAGEMENT("Akun", Icons.Default.People, AdminDestinations.ACCOUNT_MANAGEMENT_ROUTE),
    FINANCIAL_STATS("Keuangan", Icons.Default.AttachMoney, AdminDestinations.FINANCIAL_STATS_ROUTE),
    CUSTOMER_STATS("Pelanggan", Icons.Default.Group, AdminDestinations.CUSTOMER_STATS_ROUTE),
    PARKING_MANAGEMENT("Parkir", Icons.Default.LocalParking, AdminDestinations.PARKING_MANAGEMENT_ROUTE)
}


@Composable
fun AdminBottomBar(
    selectedAdminPage: AdminMainPage,
    onPageSelected: (AdminMainPage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ){
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
                AdminMainPage.values().forEach { page ->
                    AdminMainMenuItem(
                        imageVector = page.icon,
                        label = page.label,
                        isSelected = selectedAdminPage == page
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