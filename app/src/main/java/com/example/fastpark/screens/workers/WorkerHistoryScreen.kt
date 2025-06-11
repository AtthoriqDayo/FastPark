package com.example.fastpark.screens.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.data.ParkingSession
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.FullHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHistoryScreen(
    historyViewModel: FullHistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Riwayat Parkir", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepRed
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = uiState) {
                is FullHistoryViewModel.HistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FullHistoryViewModel.HistoryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.sessions) { session ->
                            HistoryItem(session = session)
                        }
                    }
                }
                is FullHistoryViewModel.HistoryUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        message = "Belum Ada Riwayat Parkir"
                    )
                }
                is FullHistoryViewModel.HistoryUiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.ErrorOutline,
                        message = state.message
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(session: ParkingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.parkingArea ?: "Lokasi Tidak Diketahui",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepRed
            )
            Spacer(modifier = Modifier.height(8.dp))

            val formattedDate = session.entryTimestamp?.toDate()?.let {
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(it)
            } ?: "Tanggal tidak tersedia"
            val formattedDateOut = session.exitTimestamp?.toDate()?.let {
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(it)
            } ?: "Tanggal tidak tersedia"

            Text("Waktu Masuk: $formattedDate", fontSize = 14.sp, color = Color.Gray)
            Text("Waktu Keluar: $formattedDateOut", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Biaya",
                    fontSize = 16.sp
                )
                Text(
                    text = "Rp ${"%,.0f".format(session.feeCalculated)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}