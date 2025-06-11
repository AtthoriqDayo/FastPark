// File: UserShowQrScreen.kt
package com.example.fastpark.screens.users

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.set
import androidx.navigation.NavHostController
import com.example.fastpark.R
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Color as AndroidColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserShowQrScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val dynamicToken by authViewModel.dynamicQrToken.collectAsState()
    val errorMessage by authViewModel.error.collectAsState()
    val userData by authViewModel.userData.observeAsState()

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val density = LocalDensity.current
    val context = LocalContext.current
    val logoResId = R.drawable.logo // Ganti jika nama file logo Anda berbeda

    DisposableEffect(authViewModel) {
        authViewModel.startDynamicQrTokenUpdates()
        onDispose {
            authViewModel.stopDynamicQrTokenUpdates()
        }
    }

    LaunchedEffect(dynamicToken, density, context, logoResId) {
        if (!dynamicToken.isNullOrEmpty()) {
            val generatedBitmap = withContext(Dispatchers.IO) {
                val sizeInPixels = with(density) { 256.dp.toPx() }.toInt()
                generateQrCodeBitmapWithLogo(
                    content = dynamicToken!!,
                    width = sizeInPixels,
                    height = sizeInPixels,
                    context = context,
                    logoResId = logoResId
                )
            }
            qrBitmap = generatedBitmap
        } else {
            qrBitmap = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QR Code Saya") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
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
                    contentDescription = "QR Code Pengguna Dinamis dengan Logo",
                    modifier = Modifier.size(256.dp).padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Kode ini akan diperbarui secara otomatis untuk keamanan Anda.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else if (!errorMessage.isNullOrEmpty()) {
                Icon(Icons.Default.Warning, "Error Icon", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Gagal Memuat QR Code", fontSize = 18.sp, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(4.dp))
                Text(errorMessage!!, fontSize = 14.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    authViewModel.clearError()
                    authViewModel.startDynamicQrTokenUpdates()
                }) { Text("Coba Lagi") }
            } else {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Menghasilkan QR Code Dinamis...")
            }
            if (userData != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("ID Pengguna: ${userData?.uid ?: "Tidak tersedia"}", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.7f))
            }
        }
    }
}

private fun generateQrCodeBitmapWithLogo(
    content: String,
    width: Int,
    height: Int,
    context: Context,
    logoResId: Int,
    logoMarginInPx: Int = 10
): Bitmap? {
    val qrCodeWriter = QRCodeWriter()
    try {
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.Q
        hints[EncodeHintType.MARGIN] = 1

        val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
        val qrBitmap = createBitmap(width, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                qrBitmap[x, y] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
            }
        }

        ContextCompat.getDrawable(context, logoResId)?.let { drawable ->
            val originalLogoBitmap = drawable.toBitmap()
            val originalLogoWidth = originalLogoBitmap.width
            val originalLogoHeight = originalLogoBitmap.height
            if (originalLogoWidth == 0 || originalLogoHeight == 0) return qrBitmap

            val targetLogoWidth = width / 6
            val targetLogoHeight = (originalLogoHeight.toFloat() / originalLogoWidth.toFloat() * targetLogoWidth).toInt()
            if (targetLogoWidth <= 0 || targetLogoHeight <= 0) return qrBitmap

            val scaledLogoBitmap = originalLogoBitmap.scale(targetLogoWidth, targetLogoHeight)
            val canvas = Canvas(qrBitmap)
            val xLogo = (width - targetLogoWidth) / 2f
            val yLogo = (height - targetLogoHeight) / 2f

            if (logoMarginInPx > 0) {
                val paintBackground = Paint().apply {
                    color = AndroidColor.WHITE
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val bgRect = RectF(
                    xLogo - logoMarginInPx,
                    yLogo - logoMarginInPx,
                    xLogo + targetLogoWidth + logoMarginInPx,
                    yLogo + targetLogoHeight + logoMarginInPx
                )
                val cornerRadius = logoMarginInPx * 0.5f
                canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, paintBackground)
            }

            canvas.drawBitmap(scaledLogoBitmap, xLogo, yLogo, null)
        }
        return qrBitmap
    } catch (e: Exception) {
        Log.e("generateQrCodeWithLogo", "Error generating QR code with logo: ${e.message}", e)
        return null
    }
}