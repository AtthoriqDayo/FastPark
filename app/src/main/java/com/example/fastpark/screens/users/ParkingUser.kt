package com.example.fastpark.screens.users

// File: UserParkingStatusScreen.kt (Contoh)
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.viewmodel.ParkingViewModel

@Composable
fun UserParkingStatusScreen(parkingViewModel: ParkingViewModel = viewModel()) {

    // Ambil state dari ViewModel
    val uiState by parkingViewModel.uiState.collectAsState()

    // Gunakan 'when' untuk menampilkan UI yang sesuai dengan state saat ini
    when (val state = uiState) {
        is ParkingViewModel.ParkingUiState.Loading -> {
            // Tampilkan UI loading, misalnya CircularProgressIndicator
            CircularProgressIndicator()
            Text("Memuat status parkir...")
        }
        is ParkingViewModel.ParkingUiState.NoActiveSession -> {
            // Tampilkan UI yang memberitahu pengguna tidak sedang parkir
            Text("Anda sedang tidak parkir.")
        }
        is ParkingViewModel.ParkingUiState.ActiveSession -> {
            // Tampilkan informasi sesi parkir yang sedang berjalan
            Column {
                Text("Parkir Aktif")
                Text("Durasi: ${state.duration}")
                Text("Estimasi Biaya: Rp ${"%,.0f".format(state.estimatedCost)}")
            }
        }
        is ParkingViewModel.ParkingUiState.Error -> {
            // Tampilkan pesan error
            Text("Error: ${state.message}", color = Color.Red)
        }
    }
}
