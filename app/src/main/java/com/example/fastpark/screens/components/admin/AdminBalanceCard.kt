package com.example.fastpark.screens.components.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminBalanceCard(
    totalRevenue: String = "Rp 1.234.567", // Contoh data
    totalUsers: String = "500 Pengguna",  // Contoh data
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), // Warna abu-abu terang
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ringkasan Dashboard",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = "Data statistik penting untuk Anda.",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column {
                    Text("Total Pendapatan", fontSize = 12.sp, color = Color.Gray)
                    Text(totalRevenue, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                }
                Column {
                    Text("Jumlah Pengguna", fontSize = 12.sp, color = Color.Gray)
                    Text(totalUsers, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                }
            }
        }
    }
}