package com.example.fastpark.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ParkingMenu(
    onTambahSlot: () -> Unit,
    onDaftarSlot: () -> Unit,
    onSlotBooking: () -> Unit,
    onHapusSlot: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val buttons = listOf(
            "Tambah slot Parkir" to onTambahSlot,
            "Daftar Slot" to onDaftarSlot,
            "Slot di Booking" to onSlotBooking,
            "Hapus slot" to onHapusSlot
        )

        buttons.forEach { (label, action) ->
            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Black)
            }
        }
    }
}