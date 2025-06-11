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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BalanceCard(
    balance: Double,
    onTopUpClick: () -> Unit,
    onTransferClick: () -> Unit // Menambahkan aksi untuk tombol transfer
) {
    // Format angka menjadi format mata uang Rupiah yang lebih rapi
    val localeID = Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    // Menghapus ",00" di akhir untuk tampilan yang lebih bersih
    val formattedBalance = numberFormat.format(balance).replace(",00", "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bagian Kiri: Logo dan Info Saldo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(BrightRed, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A", // Bisa diganti dengan inisial nama pengguna
                        color = Color.White,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Saldo",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.RemoveRedEye,
                            contentDescription = "Show balance",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = formattedBalance, // Menampilkan saldo dinamis yang sudah diformat
                        color = Color.Black,
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Bagian Kanan: Tombol Aksi (Top Up & Transfer)
            Row {
                // Tombol Top Up
                FloatingActionButton(
                    onClick = { onTopUpClick() }, // Memanggil fungsi dari parameter
                    modifier = Modifier.size(48.dp),
                    containerColor = BrightRed,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Top Up",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tombol Transfer
                FloatingActionButton(
                    onClick = { onTransferClick() }, // Memanggil fungsi dari parameter
                    modifier = Modifier.size(48.dp),
                    containerColor = BrightRed,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CallMade,
                        contentDescription = "Transfer", // Deskripsi diubah menjadi "Transfer"
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}