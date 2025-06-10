// File: viewmodel/WorkerParkingViewModel.kt
package com.example.fastpark.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class WorkerParkingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    data class ScanUiState(
        val isLoading: Boolean = false,
        val message: String? = null,
        val isError: Boolean = false
    )

    // Fungsi ini menggantikan `processScannedUserId`
    fun processScannedToken(scannedToken: String) {
        viewModelScope.launch {
            _uiState.value = ScanUiState(isLoading = true)
            Log.d("WorkerVM", "Memvalidasi token: $scannedToken")

            // GANTI DENGAN URL & API KEY ANDA YANG SEBENARNYA!
            val functionUrl = "https://validatedynamicqrtoken-tmasj2diia-uc.a.run.app"
            val apiKey = "AIzaSyDY-ivKK4yMzTRoW-aHXeVUQtF3NHAw7vM" // Ganti dengan API Key kustom Anda

            // Validasi sederhana sebelum memanggil server
            if (functionUrl.contains("YOUR_URL") || apiKey.contains("RAHASIA")) {
                _uiState.value = ScanUiState(message = "Error: Konfigurasi ViewModel Petugas belum lengkap.", isError = true)
                return@launch
            }

            val client = OkHttpClient()
            try {
                // Membuat request body JSON
                val jsonRequestBody = JSONObject().put("token", scannedToken).toString()
                val requestBody = jsonRequestBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                // Membuat request ke Cloud Function
                val request = Request.Builder()
                    .url(functionUrl)
                    .header("X-API-Key", apiKey) // Mengirim API Key kustom
                    .post(requestBody)
                    .build()

                // Menjalankan request di background thread
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                val responseBodyString = response.body?.string()

                if (response.isSuccessful && responseBodyString != null) {
                    val jsonResponse = JSONObject(responseBodyString)
                    val status = jsonResponse.optString("status")
                    val messageFromServer = jsonResponse.optString("message", "Aksi berhasil.")

                    if (status == "sukses") {
                        val action = jsonResponse.optString("action")
                        val userInfo = jsonResponse.optJSONObject("user_info")
                        val displayName = userInfo?.optString("displayName", "Pengguna") ?: "Pengguna"

                        val successMessage = when(action) {
                            "CHECKED_IN" -> "MASUK: Selamat datang, $displayName!"
                            "CHECKED_OUT" -> "KELUAR: Sampai jumpa, $displayName!"
                            else -> messageFromServer
                        }

                        Log.d("WorkerVM", "Validasi sukses: $successMessage")
                        _uiState.value = ScanUiState(message = successMessage, isError = false)
                    } else {
                        Log.w("WorkerVM", "Validasi gagal dari server: $messageFromServer")
                        _uiState.value = ScanUiState(message = "Gagal: $messageFromServer", isError = true)
                    }
                } else {
                    val serverErrorMessage = try {
                        JSONObject(responseBodyString ?: "{}").optString("message", "Error dari server (Kode: ${response.code})")
                    } catch (e: Exception) {
                        "Error dari server (Kode: ${response.code})"
                    }
                    Log.e("WorkerVM", "Error HTTP memanggil Cloud Function: ${response.code}, Body: $responseBodyString")
                    _uiState.value = ScanUiState(message = serverErrorMessage, isError = true)
                }

            } catch (e: Exception) {
                Log.e("WorkerVM", "Pengecualian saat memanggil Cloud Function: ${e.message}", e)
                _uiState.value = ScanUiState(message = "Error: Terjadi kesalahan jaringan.", isError = true)
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, isError = false)
    }
}