// KODE LENGKAP - GANTI SELURUH FILE INI
package com.example.fastpark.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fastpark.navigation.AppDestinations
import com.example.fastpark.screens.components.BalanceCard
import com.example.fastpark.screens.components.SearchUserBar
import com.example.fastpark.screens.components.StatusHeader
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUserScreen(
    userName: String = "User",
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    // State dari ViewModel (dari file kedua)
    val userData by authViewModel.userData.observeAsState()
    val paymentUrl by authViewModel.paymentUrl.observeAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.error.collectAsState()

    // State untuk fungsionalitas pencarian (dari file pertama)
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Menampilkan dialog jika ada error dari ViewModel
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.clearError() },
            title = { Text("Terjadi Kesalahan") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { authViewModel.clearError() }) { Text("Tutup") }
            }
        )
    }

    // Tampilkan WebView jika paymentUrl ada, ini akan menutupi layar lainnya
    if (paymentUrl != null) {
        val webViewState = rememberWebViewState(url = paymentUrl!!)
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize(),
            onDispose = {
                // Bersihkan URL saat WebView ditutup agar kembali ke layar utama
                authViewModel.clearPaymentUrl()
            }
        )
    } else {
        // Tampilan utama jika tidak sedang dalam proses pembayaran
        Scaffold(
            floatingActionButton = {
                // Tombol Top Up cepat (dari file kedua)
                FloatingActionButton(
                    onClick = {
                        // Contoh aksi cepat: Top Up Rp 10.000
                        authViewModel.requestTopUpPayment(10000, "M2")
                    },
                    containerColor = BrightRed,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(Icons.Default.Add, "Quick Top Up", tint = Color.White)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                // Header (StatusHeader & SearchUserBar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(colors = listOf(DeepRed, BrightRed)),
                            shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                        )
                        .padding(top = 50.dp, bottom = 24.dp)
                ) {
                    StatusHeader(
                        userName = userData?.displayName ?: userName,
                        onSettingsClick = { navController.navigate(AppDestinations.USER_SETTINGS_ROUTE) },
                        onShowQrClick = { navController.navigate(AppDestinations.USER_SHOW_QR_ROUTE) }
                    )
                    Spacer(Modifier.height(12.dp))
                    // Search bar interaktif (logika dari file pertama)
                    SearchUserBar(
                        query = searchQuery,
                        onQueryChange = { newQuery ->
                            searchQuery = newQuery
                            isSearching = newQuery.isNotEmpty()
                        },
                        onSearchClose = {
                            isSearching = false
                            searchQuery = ""
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Konten kondisional berdasarkan status pencarian (logika dari file pertama)
                if (isSearching) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Menampilkan hasil untuk: \"$searchQuery\"",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    // Konten utama jika tidak mencari (konten dari file kedua)
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        // Panggil BalanceCard dengan data dinamis dan aksi
                        BalanceCard(
                            balance = userData?.balance ?: 0.0,
                            onTopUpClick = {
                                // Memulai proses top up dengan nominal default Rp 50.000
                                authViewModel.requestTopUpPayment(50000, "M2")
                            },
                            onTransferClick = {
                                // TODO: Navigasi ke halaman transfer
                            }
                        )

                        // Tampilkan loading indicator jika isLoading
                        if (isLoading) {
                            Box(modifier = Modifier.fillMaxSize().padding(top = 32.dp), contentAlignment = Alignment.TopCenter) {
                                CircularProgressIndicator()
                            }
                        }

                        // Sisa konten halaman Anda bisa ditaruh di sini
                    }
                }
            }
        }
    }
}