package com.example.fastpark.screens.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fastpark.data.ParkingSession
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.ParkingManagementUiState
import com.example.fastpark.viewmodel.ParkingManagementViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingManagementScreen(
    navController: NavController,
    parkingViewModel: ParkingManagementViewModel = viewModel()
) {
    val uiState by parkingViewModel.uiState.collectAsState()
    val searchQuery by parkingViewModel.searchQuery.collectAsState()
    val selectedSession by parkingViewModel.selectedSession.collectAsState()

    // Menampilkan Dialog Detail jika ada sesi yang dipilih
    if (selectedSession != null) {
        ParkingDetailDialog(
            session = selectedSession!!,
            onDismiss = { parkingViewModel.onDialogDismiss() }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Parkir Aktif") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepRed,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { parkingViewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Cari berdasarkan UID atau Area Parkir...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { parkingViewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true
            )

            // Konten (List, Loading, Empty, Error)
            when (val state = uiState) {
                is ParkingManagementUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is ParkingManagementUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.sessions, key = { it.sessionId }) { session ->
                            ActiveParkingItem(
                                session = session,
                                onClick = { parkingViewModel.onSessionSelected(session) }
                            )
                        }
                    }
                }
                is ParkingManagementUiState.Empty -> {
                    Text("Tidak ada kendaraan yang sedang parkir.", modifier = Modifier.padding(16.dp))
                }
                is ParkingManagementUiState.Error -> {
                    Text(state.message, modifier = Modifier.padding(16.dp), color = Color.Red)
                }
            }
        }
    }
}

// Composable untuk setiap item di daftar
@Composable
fun ActiveParkingItem(session: ParkingSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.parkingArea,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "User: ${session.uid}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            val formattedTime = session.entryTimestamp?.toDate()?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: "N/A"
            Text(
                text = "Masuk: $formattedTime",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Composable untuk Dialog Detail
@Composable
fun ParkingDetailDialog(session: ParkingSession, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Detail Sesi Parkir", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow("User ID:", session.uid)
                DetailRow("Area Parkir:", session.parkingArea)
                DetailRow("Tarif per Jam:", "Rp ${"%,.0f".format(session.pricePerHour)}")

                val entryTime = session.entryTimestamp?.toDate()?.let {
                    SimpleDateFormat("dd/MM/yyyy, HH:mm:ss", Locale.getDefault()).format(it)
                } ?: "N/A"
                DetailRow("Waktu Masuk:", entryTime)
                DetailRow("Petugas Masuk:", session.workerId_entry ?: "N/A")

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Tutup")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(120.dp)
        )
        Text(text = value)
    }
}