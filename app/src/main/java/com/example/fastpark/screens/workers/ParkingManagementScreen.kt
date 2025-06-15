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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.data.ParkingSession
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.ParkingManagementUiState
import com.example.fastpark.viewmodel.ParkingManagementViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// --- Constants ---
private object R {
    object string {
        const val screen_title = "Parkir Aktif"
        const val search_placeholder = "Cari UID atau Plat Nomor..."
        const val search_icon_desc = "Search Icon"
        const val clear_search_desc = "Clear Search"
        const val empty_state_message = "Tidak ada kendaraan yang sedang parkir."
        const val user_label = "User: "
        const val entry_time_label = "Masuk: "
        const val dialog_title = "Detail Sesi Parkir"
        const val dialog_close_button = "Tutup"
        const val user_id_label = "User ID:"
        const val parking_area_label = "Area Parkir:"
        const val price_per_hour_label = "Tarif per Jam:"
        const val entry_worker_label = "Petugas Masuk:"
        const val unavailable = "N/A"
    }
}

/**
 * Layar untuk mengelola sesi parkir yang sedang aktif.
 * Memungkinkan pencarian dan melihat detail sesi.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingManagementScreen(
    parkingViewModel: ParkingManagementViewModel = viewModel()
) {
    val uiState by parkingViewModel.uiState.collectAsState()
    val searchQuery by parkingViewModel.searchQuery.collectAsState()
    val selectedSession by parkingViewModel.selectedSession.collectAsState()

    // Menampilkan Dialog Detail jika ada sesi yang dipilih
    selectedSession?.let { session ->
        ParkingDetailDialog(
            session = session,
            onDismiss = { parkingViewModel.onDialogDismiss() }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(R.string.screen_title) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = DeepRed
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { parkingViewModel.onSearchQueryChange(it) }
            )

            when (val state = uiState) {
                is ParkingManagementUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ParkingManagementUiState.Success -> {
                    ActiveParkingList(
                        sessions = state.sessions,
                        onItemClick = { parkingViewModel.onSessionSelected(it) }
                    )
                }
                is ParkingManagementUiState.Empty -> {
                    InfoState(message = R.string.empty_state_message)
                }
                is ParkingManagementUiState.Error -> {
                    InfoState(message = state.message, isError = true)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text(R.string.search_placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = R.string.search_icon_desc) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = R.string.clear_search_desc)
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun ActiveParkingList(sessions: List<ParkingSession>, onItemClick: (ParkingSession) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions, key = { it.sessionId }) { session ->
            ActiveParkingItem(
                session = session,
                onClick = { onItemClick(session) }
            )
        }
    }
}

@Composable
private fun ActiveParkingItem(session: ParkingSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.noPlat ?: R.string.unavailable,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${R.string.user_label}${session.uid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                text = "${R.string.entry_time_label}${formatTime(session.entryTimestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ParkingDetailDialog(session: ParkingSession, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(R.string.dialog_title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(R.string.user_id_label, session.uid)
                DetailRow(R.string.parking_area_label, session.parkingArea)
                DetailRow(R.string.price_per_hour_label, formatCurrency(session.pricePerHour))
                DetailRow("Waktu Masuk:", formatDateTime(session.entryTimestamp))
                DetailRow(R.string.entry_worker_label, session.workerId_entry ?: R.string.unavailable)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(R.string.dialog_close_button)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun InfoState(message: String, icon: ImageVector = Icons.Default.Info, isError: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = if (isError) MaterialTheme.colorScheme.error else Color.Gray
            )
        }
    }
}

// --- Helper Functions ---

private fun formatTime(timestamp: Timestamp?): String {
    return timestamp?.toDate()?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
    } ?: R.string.unavailable
}

private fun formatDateTime(timestamp: Timestamp?): String {
    return timestamp?.toDate()?.let {
        SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault()).format(it)
    } ?: R.string.unavailable
}

private fun formatCurrency(amount: Double?): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    return format.format(amount ?: 0.0)
}