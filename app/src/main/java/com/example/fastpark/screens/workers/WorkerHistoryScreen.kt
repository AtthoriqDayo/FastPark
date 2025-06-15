package com.example.fastpark.screens.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.data.ParkingSession
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.FullHistoryViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// --- Constants ---
private const val SCREEN_TITLE = "Riwayat Parkir"
private const val DIALOG_TITLE = "Detail Riwayat Parkir"
private const val CLOSE_BUTTON = "Tutup"
private const val UNKNOWN_LOCATION = "Lokasi Tidak Diketahui"
private const val UNAVAILABLE_DATE = "N/A"
private const val UNAVAILABLE_PlAT = "N/A"
private const val ENTRY_TIME_LABEL = "Waktu Masuk: "
private const val EXIT_TIME_LABEL = "Waktu Keluar: "
private const val TOTAL_FEE_LABEL = "Total Biaya"
private const val EMPTY_HISTORY_MESSAGE = "Belum Ada Riwayat Parkir"

/**
 * Menampilkan layar utama yang berisi daftar riwayat sesi parkir.
 * Kini dilengkapi dengan pop-up detail saat item diklik.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHistoryScreen(
    historyViewModel: FullHistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()
    // State untuk mengelola sesi parkir yang dipilih untuk ditampilkan di dialog
    var selectedSession by remember { mutableStateOf<ParkingSession?>(null) }

    // Menampilkan dialog jika ada sesi yang dipilih
    selectedSession?.let { session ->
        HistoryDetailDialog(
            session = session,
            onDismiss = { selectedSession = null } // Menutup dialog
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(SCREEN_TITLE, color = DeepRed) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,

                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is FullHistoryViewModel.HistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is FullHistoryViewModel.HistoryUiState.Success -> {
                    HistoryList(
                        sessions = state.sessions,
                        // Meneruskan aksi klik untuk memperbarui state
                        onItemClick = { session -> selectedSession = session }
                    )
                }
                is FullHistoryViewModel.HistoryUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Default.Inbox,
                        message = EMPTY_HISTORY_MESSAGE
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

/**
 * Menampilkan daftar sesi parkir dalam sebuah LazyColumn.
 * @param onItemClick Aksi yang dijalankan saat sebuah item diklik.
 */
@Composable
private fun HistoryList(
    sessions: List<ParkingSession>,
    onItemClick: (ParkingSession) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions) { session ->
            HistoryItem(
                session = session,
                onClick = { onItemClick(session) }
            )
        }
    }
}

/**
 * Menampilkan satu item dalam daftar riwayat parkir yang dapat diklik.
 * @param onClick Aksi yang dijalankan saat Card diklik.
 */
@Composable
private fun HistoryItem(session: ParkingSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Membuat Card dapat diklik
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.noPlat ?: UNAVAILABLE_PlAT,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = DeepRed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$ENTRY_TIME_LABEL${formatTimestamp(session.entryTimestamp)}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "$EXIT_TIME_LABEL${formatTimestamp(session.exitTimestamp)}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TOTAL_FEE_LABEL,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatCurrency(session.feeCalculated),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


@Composable
private fun HistoryDetailDialog(session: ParkingSession, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(DIALOG_TITLE, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("ID Sesi:", session.sessionId)
                DetailRow("Plat Nomor:", session.noPlat ?: "-")
                DetailRow("Lokasi Parkir:", session.parkingArea ?: UNKNOWN_LOCATION)
                DetailRow("Waktu Masuk:", formatTimestamp(session.entryTimestamp))
                DetailRow("Waktu Keluar:", formatTimestamp(session.exitTimestamp))
                DetailRow("Nama Pelanggan:", session.displayName ?: "-")
                DetailRow("Status:", session.status)
                DetailRow("Total Biaya:", formatCurrency(session.feeCalculated), isHighlight = true)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(CLOSE_BUTTON)
            }
        }
    )
}


@Composable
private fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.5f),
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun EmptyState(icon: ImageVector, message: String) {
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

private fun formatTimestamp(timestamp: Timestamp?): String {
    return timestamp?.toDate()?.let { date ->
        SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale("id", "ID")).format(date)
    } ?: UNAVAILABLE_DATE
}

private fun formatCurrency(amount: Double?): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    return format.format(amount ?: 0.0)
}