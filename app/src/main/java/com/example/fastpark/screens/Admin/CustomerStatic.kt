// File: app/src/main/java/com/example/fastpark/screens/admin/CustomerStatisticsScreen.kt
package com.example.fastpark.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomerStatisticsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Statistik Pelanggan", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text("Total Pelanggan Terdaftar: 1.234", style = MaterialTheme.typography.titleLarge)
        Text("Pelanggan Baru Bulan Ini: 56", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        // TODO: Tambahkan grafik pertumbuhan pelanggan, demografi, dll.
        Text("Grafik Pelanggan (Placeholder)", color = Color.LightGray)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text("Area Grafik")
        }
    }
}