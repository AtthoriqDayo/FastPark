package com.example.fastpark.screens.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPage(
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val dashboardData by dashboardViewModel.dashboardData.collectAsState()
    val isLoading by dashboardViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dasbor Admin") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            dashboardData?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Ringkasan Hari Ini", style = MaterialTheme.typography.titleLarge)

                    // Card Utama: Parkir Aktif
                    MainSummaryCard(
                        count = data.currentlyParking,
                        label = "Kendaraan Sedang Parkir"
                    )

                    // Grid untuk statistik harian
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                            maximumFractionDigits = 0
                        }
                        DashboardStatCard(
                            label = "Pendapatan Hari Ini",
                            value = currencyFormat.format(data.todayRevenue),
                            icon = Icons.Default.MonetizationOn,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            label = "Parkir Selesai",
                            value = data.todayCompletedSessions.toString(),
                            icon = Icons.Default.TaskAlt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    DashboardStatCard(
                        label = "Pengguna Baru Hari Ini",
                        value = data.todayNewUsers.toString(),
                        icon = Icons.Default.People,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun MainSummaryCard(count: Int, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CarRental, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DashboardStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}