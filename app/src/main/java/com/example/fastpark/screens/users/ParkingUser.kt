package com.example.fastpark.screens.users

// File: UserParkingStatusScreen.kt (Contoh)
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.fastpark.viewmodel.ParkingViewModel

@Composable
fun UserParkingStatusScreen(parkingViewModel: ParkingViewModel = viewModel()) {
    val activeSession by parkingViewModel.activeSession.collectAsState()
    val duration by parkingViewModel.parkingDuration.collectAsState()
    val estimatedCost by parkingViewModel.estimatedCost.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (activeSession == null && duration == "00:00:00") { // Kondisi awal atau tidak ada sesi
            Text("Anda tidak sedang parkir saat ini.", style = MaterialTheme.typography.headlineSmall)
            // Anda bisa tambahkan ProgressIndicator jika masih loading awal
        } else if (activeSession != null && activeSession?.status == "active") {
            Text("Status Parkir: Aktif", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            activeSession?.entryTimestamp?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                Text("Waktu Masuk: ${sdf.format(it.toDate())}", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Durasi Parkir:", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
            Text(duration, fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Estimasi Biaya:", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
            Text("Rp ${"%,.0f".format(estimatedCost)}", fontSize = 24.sp, style = MaterialTheme.typography.headlineSmall)

            activeSession?.parkingLocation?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Lokasi Parkir: $it", fontSize = 16.sp)
            }
        } else if (activeSession != null && activeSession?.status != "active") {
            // Menampilkan info sesi yang sudah selesai (jika diperlukan)
            Text("Sesi Parkir Selesai", style = MaterialTheme.typography.headlineSmall)
            activeSession?.entryTimestamp?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                Text("Waktu Masuk: ${sdf.format(it.toDate())}", fontSize = 16.sp)
            }
            activeSession?.exitTimestamp?.let {
                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                Text("Waktu Keluar: ${sdf.format(it.toDate())}", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Durasi: $duration", fontSize = 18.sp)
            Text("Total Biaya: Rp ${"%,.0f".format(activeSession?.parkingFee ?: estimatedCost)}", fontSize = 20.sp)

        } else {
            // State default jika activeSession masih null tapi duration belum 00:00:00 (loading)
            CircularProgressIndicator()
            Text("Memuat data sesi parkir...")
        }
    }
}