package com.example.fastpark.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeatureSection(
    onFeatureClick: (String) -> Unit = {}
) {
    val features = listOf(
        "Pemakaian Parkir",
        "Daftar User",
        "Slot di Booking",
        "Logos Exit"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        features.forEach { feature ->
            Button(
                onClick = { onFeatureClick(feature) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = feature)
            }
        }
    }
}