package com.example.fastpark.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.fastpark.R
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.ParkingViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserParkingStatusScreen(
    navController: NavController,
    parkingViewModel: ParkingViewModel = viewModel()
) {

    val uiState by parkingViewModel.uiState.collectAsState()
    val context = LocalContext.current


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Status Parkir Saya",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White // Pastikan judul berwarna putih untuk kontras
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // PERBAIKAN UTAMA: Pastikan paddingValues diterapkan sebagai modifier PERTAMA
        // pada root composable di dalam konten Scaffold.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues) // <--- PASTIKAN INI ADA DAN DITERAPKAN DENGAN BENAR
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is ParkingViewModel.ParkingUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(64.dp),
                        color = DeepRed
                    )
                    Text(
                        text = "Memuat status parkir...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 16.dp),
                        color = Color.Gray
                    )
                }

                is ParkingViewModel.ParkingUiState.NoActiveSession -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.clang_bang_shakehead)
                            .decoderFactory(ImageDecoderDecoder.Factory())
                            .build(),
                        contentDescription = "Tidak ada sesi parkir aktif",
                        modifier = Modifier.size(250.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = "Anda sedang tidak parkir.",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Mulai sesi parkir baru untuk melihat status di sini.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                is ParkingViewModel.ParkingUiState.ActiveSession -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.clang_bang_run)
                            .decoderFactory(ImageDecoderDecoder.Factory())
                            .build(),
                        contentDescription = "Sesi parkir aktif",
                        modifier = Modifier.size(250.dp),
                        contentScale = ContentScale.Fit
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Parkir Aktif",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepRed,
                            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                        )
                        Text(
                            text = "Durasi: ${state.duration}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Estimasi Biaya: Rp ${"%,.0f".format(state.estimatedCost)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                is ParkingViewModel.ParkingUiState.Error -> {
                    // Tampilan saat terjadi error
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.Red,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}