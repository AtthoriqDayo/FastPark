package com.example.fastpark.screens.components.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.ManageAccounts
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


enum class AdminMenu(val label: String, val icon: ImageVector) {
    HOME("Dashboard", Icons.Default.HomeWork),
    CUSTOMER_STATS("Account", Icons.Default.ManageAccounts),
    FINANCIAL_MANAGEMENT("Financial", Icons.Default.AttachMoney),
    //PARKING_MANAGEMENT("Management", Icons.Default.DirectionsCar)
}

@Composable
fun MenuAdmin(
    onMenuItemClick: (AdminMenu) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminMenu.entries.forEach { item ->
                    AdminGridItem(
                        menuItem = item,
                        modifier = Modifier.weight(1f),
                        onClick = { onMenuItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminGridItem(
    menuItem: AdminMenu,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = menuItem.icon,
            contentDescription = menuItem.label,
            modifier = Modifier.size(30.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = menuItem.label,
            fontSize = 12.sp,
            color = Color.Black
        )
    }
}
