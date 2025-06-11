package com.example.fastpark.screens.admin

// Firebase imports
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

// Define the User data class without registrationTimestamp
data class User(
    var userId: String = "",
    val role: String = "",
    // Tidak ada field timestamp lagi di sini
    // Tambahkan field lain yang ada di dokumen user Anda, misalnya:
    // val name: String = "",
    // val email: String = "",
)

@Composable
fun CustomerStatisticsScreen() {
    val totalCustomers = remember { mutableIntStateOf(0) }
    val firestore = FirebaseFirestore.getInstance()

    // Perhitungan Pelanggan Baru Bulan Ini tidak dapat dilakukan tanpa timestamp
    // Kita akan menampilkannya sebagai "Tidak Tersedia" atau "N/A"

    LaunchedEffect(Unit) {
        firestore.collection("users")
            .whereEqualTo("role", "user") // Filter untuk user dengan role "user"
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CustomerStats", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.apply {
                        userId = doc.id
                    }
                } ?: emptyList()

                // Hitung Total Pelanggan Terdaftar
                totalCustomers.intValue = users.size
                Log.d("CustomerStats", "Total Customers: ${totalCustomers.intValue}")

                // Log peringatan karena "Pelanggan Baru Bulan Ini" tidak bisa dihitung
                Log.w("CustomerStats", "Monthly New Customers cannot be calculated without a 'registrationTimestamp' field in User data.")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Statistik Pelanggan", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Total Pelanggan Terdaftar: ${totalCustomers.intValue}",
            style = MaterialTheme.typography.titleLarge
        )
/*        Text(
            // Tampilkan sebagai tidak tersedia karena tidak ada timestamp
            "Pelanggan Baru Bulan Ini: Tidak Tersedia",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Pesan untuk grafik, karena grafik pertumbuhan membutuhkan data waktu
        Text(
            text = "Grafik Pertumbuhan Pelanggan",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )
        Text(
            "Grafik pertumbuhan pelanggan tidak dapat ditampilkan tanpa data waktu (misalnya 'registrationTimestamp') di koleksi pengguna.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red, // Memberikan indikasi jelas
            modifier = Modifier.padding(16.dp)
        )
        // Grafik CustomerGrowthChart dihapus karena tidak relevan tanpa timestamp
*/    }
}

// Fungsi CustomerGrowthChart Dihapus karena tidak bisa dibuat tanpa data waktu