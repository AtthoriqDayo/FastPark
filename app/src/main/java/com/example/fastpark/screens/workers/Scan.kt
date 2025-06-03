// File: ScanScreen.kt
package com.example.fastpark.screens.workers // Sesuaikan dengan package Anda

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
// import androidx.compose.foundation.Image // Logo dihapus, import ini tidak perlu lagi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.size // Tidak perlu untuk logo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
// import androidx.compose.ui.res.painterResource // Tidak perlu untuk logo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
// import com.example.fastpark.R // Tidak perlu R untuk logo jika sudah dihapus
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
    var lastRawScannedValue by remember { mutableStateOf<String?>(null) } // Bisa digunakan untuk menampilkan hasil scan terakhir jika perlu
    var showSuccessDialog by remember { mutableStateOf(false) }
    var scannedUserIdForDialog by remember { mutableStateOf<String?>(null) }
    // 'isScanningActive' sekarang HANYA menandakan bahwa kita siap memproses hasil scan dari callback
    var isScanningActive by remember { mutableStateOf(false) }

    val uiState by workerViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentLifecycleState by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            currentLifecycleState = event.targetState
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val decoratedBarcodeView = remember {
        DecoratedBarcodeView(context).apply {
            barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            setStatusText("")
            // Pratinjau akan di-resume/pause oleh DisposableEffect di bawah berdasarkan lifecycle global
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Log.w("ScanScreen", "Izin kamera ditolak.")
            } else {
                Log.d("ScanScreen", "Izin kamera diberikan.")
                // Jika izin baru diberikan dan layar sudah RESUMED, pratinjau akan aktif via DisposableEffect
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d("ScanScreen", "Izin diberikan, status RESUMED, meresume pratinjau.")
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

    // Mengelola siklus hidup pratinjau kamera (resume/pause DecoratedBarcodeView)
    DisposableEffect(lifecycleOwner, hasCameraPermission, decoratedBarcodeView) {
        val observer = LifecycleEventObserver { owner: LifecycleOwner, event: Lifecycle.Event ->
            if (hasCameraPermission) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        Log.d("ScanScreen", "Lifecycle ON_RESUME, meresume pratinjau kamera.")
                        decoratedBarcodeView.resume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d("ScanScreen", "Lifecycle ON_PAUSE, menghentikan pratinjau kamera.")
                        decoratedBarcodeView.pause()
                    }
                    else -> { /* No-op untuk event lain */ }
                }
            } else {
                // Jika tidak ada izin, pastikan kamera di-pause
                Log.d("ScanScreen", "Tidak ada izin kamera, memastikan pratinjau dihentikan pada event $event.")
                decoratedBarcodeView.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d("ScanScreen", "ScanScreen onDispose, menghentikan pratinjau dan menghapus observer.")
            decoratedBarcodeView.pause()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.message) {
        val currentMessage = uiState.message
        if (currentMessage != null) {
            val shouldShowSnackbar = !(currentMessage.startsWith("Proses untuk User ID") && !uiState.isError)
            if (shouldShowSnackbar) {
                snackbarHostState.showSnackbar(
                    message = currentMessage,
                    duration = if (uiState.isError) SnackbarDuration.Long else SnackbarDuration.Short
                )
            }
            workerViewModel.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo telah dihapus

        if (hasCameraPermission) {
            Spacer(modifier = Modifier.height(20.dp)) // Memberi sedikit jarak dari atas

            Text(
                // Teks ini sekarang lebih fokus pada status tombol scan
                text = if (isScanningActive) "Siap memindai..." else "Arahkan kamera dan tekan 'Pindai QR Code'.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp) // Anda bisa sesuaikan tinggi area kamera
                    .padding(bottom = 16.dp)
            ) {
                AndroidView(
                    factory = { decoratedBarcodeView },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        // Pastikan view di-resume jika kondisi terpenuhi saat update/recompose
                        if (hasCameraPermission && currentLifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
                            view.resume()
                        } else {
                            view.pause()
                        }
                    }
                )
                // Callback pemindaian hanya dipasang dan aktif jika isScanningActive = true
                DisposableEffect(key1 = decoratedBarcodeView, key2 = isScanningActive, key3 = uiState.isLoading) {
                    if (isScanningActive) {
                        Log.d("ScanScreen", "isScanningActive=true. Memasang callback decodeContinuous.")
                        decoratedBarcodeView.decodeContinuous { result ->
                            // Hanya proses jika isScanningActive masih true dan tidak sedang loading
                            if (!isScanningActive || uiState.isLoading) {
                                return@decodeContinuous
                            }
                            result.text?.let { barCodeOrQr ->
                                if (barCodeOrQr.isNotBlank()) {
                                    Log.d("ScanScreen", "QR Terbaca: $barCodeOrQr. Memproses...")
                                    lastRawScannedValue = barCodeOrQr // Simpan jika perlu
                                    isScanningActive = false // PENTING: Nonaktifkan setelah scan berhasil

                                    scannedUserIdForDialog = barCodeOrQr
                                    workerViewModel.processScannedUserId(barCodeOrQr)
                                    showSuccessDialog = true
                                }
                            }
                        }
                    } else {
                        Log.d("ScanScreen", "isScanningActive=false. Callback decodeContinuous tidak aktif.")
                        // Jika library tidak otomatis stop decoding saat callback di-detach/re-attach,
                        // mungkin perlu decoratedBarcodeView.barcodeView.stopDecoding() di sini atau di onDispose.
                        // Namun, karena callback baru dipasang saat isScanningActive=true,
                        // dan di dalamnya ada cek `if (!isScanningActive)`, ini seharusnya cukup.
                    }
                    onDispose {
                        // Bisa tambahkan logika pembersihan jika perlu saat key berubah dan efek di-dispose
                        // Contoh: decoratedBarcodeView.barcodeView.stopDecoding() // jika library butuh ini
                        Log.d("ScanScreen", "DisposableEffect untuk decodeContinuous di-dispose. isScanningActive: $isScanningActive")
                    }
                }
            }

            if (uiState.isLoading && !isScanningActive) { // Tampilkan loading hanya jika proses backend berjalan
                CircularProgressIndicator()
                Text("Memproses data...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            } else {
                Button(
                    onClick = {
                        if (!isScanningActive) { // Cegah klik ganda jika sudah aktif (meski isScanningActive akan cepat false)
                            Log.d("ScanScreen", "Tombol 'Pindai QR Code' ditekan.")
                            // Bersihkan state sebelum scan baru jika perlu
                            lastRawScannedValue = null
                            scannedUserIdForDialog = null
                            // workerViewModel.clearMessage() // Jika ada pesan error lama yg ingin dihapus
                            isScanningActive = true // AKTIFKAN mode siap memproses scan
                        }
                    },
                    // Tombol selalu aktif jika izin ada dan tidak sedang dalam proses backend setelah scan
                    enabled = hasCameraPermission && !uiState.isLoading
                ) {
                    Text("Pindai QR Code")
                }
            }

        } else { // Jika tidak ada izin kamera
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Izin kamera diperlukan untuk fitur pemindaian QR code.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Berikan Izin Kamera")
            }
        }

        if (showSuccessDialog && scannedUserIdForDialog != null) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    // scannedUserIdForDialog = null // Biarkan null sampai scan baru
                },
                title = { Text("Pemindaian Berhasil") },
                text = { Text("User ID '$scannedUserIdForDialog' berhasil dipindai.") },
                confirmButton = {
                    TextButton(onClick = {
                        showSuccessDialog = false
                        // scannedUserIdForDialog = null
                    }) {
                        Text("OK")
                    }
                }
            )
        }

        Spacer(Modifier.weight(1f))

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}