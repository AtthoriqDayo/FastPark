// File: ScanScreen.kt
package com.example.fastpark.screens.workers // Sesuaikan dengan package Anda

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.viewmodel.WorkerParkingViewModel // Pastikan import path ViewModel Anda benar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

@Composable
fun ScanScreen(
    workerViewModel: WorkerParkingViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var lastRawScannedValue by remember { mutableStateOf<String?>(null) }

    val uiState by workerViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val decoratedBarcodeView = remember {
        DecoratedBarcodeView(context).apply {
            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            setStatusText("")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Log.w("ScanScreen", "Izin kamera ditolak.")
                // Anda bisa memanggil workerViewModel.updateMessage("Izin kamera ditolak", true)
                // jika ingin pesan ditampilkan melalui SnackbarHost dari ViewModel
            } else {
                Log.d("ScanScreen", "Izin kamera diberikan.")
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    decoratedBarcodeView.resume()
                }
            }
        }
    )

    LaunchedEffect(key1 = Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(key1 = lifecycleOwner, key2 = hasCameraPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (hasCameraPermission) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> decoratedBarcodeView.resume()
                    Lifecycle.Event.ON_PAUSE -> decoratedBarcodeView.pause()
                    else -> { /* No-op */ }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d("ScanScreen", "ScanScreen onDispose, pausing scanner.")
            decoratedBarcodeView.pause()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Perbaikan untuk menampilkan Snackbar dengan aman:
    LaunchedEffect(uiState.message) {
        val currentMessage = uiState.message // Baca ke variabel lokal
        if (currentMessage != null) {       // Gunakan variabel lokal untuk null check
            snackbarHostState.showSnackbar(
                message = currentMessage,       // Gunakan variabel lokal di sini
                duration = if (uiState.isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
            workerViewModel.clearMessage() // Bersihkan pesan di ViewModel setelah ditampilkan
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (hasCameraPermission) {
            Text(
                "Arahkan kamera ke QR code pengguna.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AndroidView(
                    factory = { decoratedBarcodeView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        if (hasCameraPermission && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            view.resume()
                        }
                    }
                )
                DisposableEffect(key1 = decoratedBarcodeView, key2 = uiState.isLoading) {
                    decoratedBarcodeView.decodeContinuous { result ->
                        if (uiState.isLoading) return@decodeContinuous
                        result.text?.let { barCodeOrQr ->
                            if (barCodeOrQr.isNotBlank()) {
                                lastRawScannedValue = barCodeOrQr
                                Log.d("ScanScreen", "QR Scanned. Value: $barCodeOrQr. Processing...")
                                workerViewModel.processScannedUserId(barCodeOrQr)
                            }
                        }
                    }
                    onDispose { /* No-op */ }
                }
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
                Text("Memproses...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            } else {
                lastRawScannedValue?.let {
                    if (uiState.message == null && !uiState.isLoading) {
                        Text("User ID Terbaca: $it", modifier = Modifier.padding(top = 16.dp))
                    }
                }
            }

        } else {
            Text(
                "Izin kamera diperlukan untuk melakukan scan QR code.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Berikan Izin Kamera")
            }
            // Jika Anda ingin menampilkan pesan error izin langsung dari ViewModel:
            // val currentMessage = uiState.message
            // if (currentMessage != null && uiState.isError && !hasCameraPermission /* kondisi spesifik */) {
            //     Text(currentMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            // }
        }

        Spacer(Modifier.weight(1f))

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}