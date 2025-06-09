package com.example.fastpark.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.screens.theme.BrightRed

@Composable
fun BalanceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp), // Kurangi padding horizontal dan vertikal
        shape = RoundedCornerShape(12.dp), // Sedikit kurangi radius sudut
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp) // Sedikit kurangi elevasi
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Kurangi padding internal
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left section: Logo, Saldo, and Amount
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp) // Perkecil ukuran logo
                        .background((BrightRed), RoundedCornerShape(6.dp)), // Sesuaikan sudut logo
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        color = Color.White,
                        fontSize = 18.sp, // Perkecil ukuran font logo
                        style = MaterialTheme.typography.titleSmall // Sesuaikan style typography
                    )
                }

                Spacer(modifier = Modifier.width(10.dp)) // Kurangi jarak

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Saldo",
                            color = Color.Gray,
                            fontSize = 12.sp, // Perkecil ukuran font "Saldo"
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(3.dp)) // Kurangi jarak
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye, // Ganti ke ikon mata
                            contentDescription = "Show balance",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp) // Perkecil ukuran ikon mata
                        )
                    }
                    Text(
                        text = "Rp500",
                        color = Color.Black,
                        fontSize = 24.sp, // Perkecil ukuran font jumlah saldo
                        style = MaterialTheme.typography.titleLarge // Sesuaikan style typography
                    )
                }
            }

            // Right section: Top Up and Transfer buttons
            Row {
                FloatingActionButton(
                    onClick = { /* Handle Top Up click */ },
                    modifier = Modifier.size(48.dp), // Perkecil ukuran FAB
                    containerColor = (BrightRed),
                    shape = RoundedCornerShape(10.dp) // Sesuaikan sudut FAB
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Top Up",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp) // Perkecil ukuran ikon FAB
                    )
                }

                Spacer(modifier = Modifier.width(6.dp)) // Kurangi jarak antar tombol

                FloatingActionButton(
                    onClick = { /* Handle Transfer click */ },
                    modifier = Modifier.size(48.dp),
                    containerColor = (BrightRed),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = "Top Up",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp) // Perkecil ukuran ikon FAB
                    )
                }
            }
        }
    }
}