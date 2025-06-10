// File: ScanScreen.kt (VERSI FINAL YANG SUDAH DIPERBAIKI)
package com.example.fastpark.screens.workers

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.viewmodel.WorkerScanViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

@Composable
fun ScanScreen(
    workerViewModel: WorkerScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val uiState by workerViewModel.scanResultState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    // LaunchedEffect untuk menampilkan hasil dari ViewModel di Snackbar sudah benar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is WorkerScanViewModel.ScanResultState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.response.message,
                    duration = SnackbarDuration.Long
                )
                workerViewModel.clearState()
            }
            is WorkerScanViewModel.ScanResultState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "Error: ${state.message}",
                    duration = SnackbarDuration.Long
                )
                workerViewModel.clearState()
            }
            else -> {}
        }
    }

    // --- PERUBAHAN UTAMA: GUNAKAN SCAFFOLD SEBAGAI PEMBUNGKUS UTAMA ---
    Scaffold(
        // Letakkan SnackbarHost di slot yang disediakan oleh Scaffold
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // Semua konten UI Anda sekarang berada di dalam content lambda dari Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Gunakan padding dari Scaffold agar konten tidak tertutup
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasCameraPermission) {
                Text("Arahkan kamera ke QR Code pengguna")
                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.size(300.dp)) {
                    AndroidView(factory = {
                        DecoratedBarcodeView(context).apply {
                            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
                            setStatusText("")
                            resume()
                            decodeSingle { result ->
                                result.text?.let { barCodeOrQr ->
                                    if (uiState !is WorkerScanViewModel.ScanResultState.Loading) {
                                        workerViewModel.validateToken(barCodeOrQr)
                                    }
                                }
                            }
                        }
                    })
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState is WorkerScanViewModel.ScanResultState.Loading) {
                    CircularProgressIndicator()
                    Text("Memvalidasi...", modifier = Modifier.padding(top = 8.dp))
                } else {
                    Text("Menunggu pemindaian...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

            } else {
                Text("Izin kamera diperlukan untuk memindai QR code.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Berikan Izin")
                }
            }
        }
    }
}