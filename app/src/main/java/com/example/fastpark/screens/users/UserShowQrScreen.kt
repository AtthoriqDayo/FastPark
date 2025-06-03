// File: UserShowQrScreen.kt
package com.example.fastpark.screens.users // Atau package yang sesuai

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.fastpark.screens.theme.DeepRed // atau warna tema Anda
import com.example.fastpark.viewmodel.AuthViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserShowQrScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val userData by authViewModel.userData.observeAsState()
    val userId = userData?.uid // Data yang akan di-encode ke QR

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    LaunchedEffect(userId) {
        if (!userId.isNullOrEmpty()) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) { // Operasi pembuatan bitmap di background thread
                    val sizeInPixels = with(density) { 256.dp.toPx() }.toInt() // Ukuran QR code dalam pixel
                    qrBitmap = generateQrCodeBitmap(userId, sizeInPixels, sizeInPixels)
                }
            }
        } else {
            qrBitmap = null // Hapus bitmap jika userId tidak ada
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QR Code Saya") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepRed, // Sesuaikan warna
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code Pengguna",
                    modifier = Modifier
                        .size(256.dp) // Ukuran tampilan QR code
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tunjukkan kode ini untuk keperluan parkir.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            } else if (userId.isNullOrEmpty() && userData != null) {
                // User data sudah termuat tapi ID kosong (seharusnya tidak terjadi untuk user yang login)
                Text("Tidak dapat membuat QR Code: ID Pengguna tidak valid.")
            }
            else {
                // Menunggu data user atau userId
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Memuat QR Code...")
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "ID Pengguna: ${userId ?: "Tidak tersedia"}",
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

private fun generateQrCodeBitmap(content: String, width: Int, height: Int): Bitmap? {
    val qrCodeWriter = QRCodeWriter()
    try {
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        // Anda bisa menambahkan margin di sini jika perlu
        // hints[EncodeHintType.MARGIN] = 2

        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}