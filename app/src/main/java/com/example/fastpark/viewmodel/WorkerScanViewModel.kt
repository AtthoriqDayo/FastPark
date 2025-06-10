// File: viewmodel/WorkerScanViewModel.kt (DIGABUNGKAN)
package com.example.fastpark.viewmodel

// Import Ktor
// Import kotlinx.serialization
// Import Coroutines
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Data class untuk request body, bisa diletakkan di sini atau di file model terpisah
@Serializable
data class ValidateTokenRequest(val token: String)

// Data class untuk response dari API
@Serializable
data class ApiResponse(
    val status: String,
    val message: String,
    val action: String? = null
)

class WorkerScanViewModel : ViewModel() {

    // --- Bagian Networking (sebelumnya di KtorClient.kt & ApiService.kt) ---

    // Konfigurasi Klien Ktor langsung di dalam ViewModel
    private val httpClient = HttpClient(CIO) {
        // Plugin untuk mengubah JSON menjadi data class Kotlin secara otomatis
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // Penting jika server mengirim field ekstra
            })
        }

        // Plugin untuk logging (sangat membantu saat debugging)
        install(Logging) {
            level = LogLevel.BODY // Menampilkan detail request & response di Logcat
        }
    }

    // GANTI DENGAN URL & API KEY ANDA YANG SEBENARNYA!
    private val VALIDATE_TOKEN_URL = "https://validatedynamicqrtoken-tmasj2diia-uc.a.run.app"
    private val API_KEY = "AIzaSyDY-ivKK4yMzTRoW-aHXeVUQtF3NHAw7vM" // Ganti dengan API Key Anda

    // --- Bagian State Management (tetap sama) ---

    private val _scanResultState = MutableStateFlow<ScanResultState>(ScanResultState.Idle)
    val scanResultState: StateFlow<ScanResultState> = _scanResultState

    sealed class ScanResultState {
        object Idle : ScanResultState()
        object Loading : ScanResultState()
        data class Success(val response: ApiResponse) : ScanResultState()
        data class Error(val message: String) : ScanResultState()
    }

    // --- Fungsi Utama ---

    fun validateToken(scannedToken: String) {
        viewModelScope.launch {
            _scanResultState.value = ScanResultState.Loading

            // Validasi konfigurasi sebelum request
            if (VALIDATE_TOKEN_URL.contains("YOUR_URL") || API_KEY.contains("RAHASIA")) {
                _scanResultState.value = ScanResultState.Error("URL atau API Key belum dikonfigurasi di ViewModel.")
                return@launch
            }

            try {
                // Pemanggilan API Ktor dilakukan langsung di sini
                val response: ApiResponse = httpClient.post(VALIDATE_TOKEN_URL) {
                    contentType(ContentType.Application.Json)
                    header("X-API-Key", API_KEY)
                    setBody(ValidateTokenRequest(token = scannedToken))
                }.body() // Ktor akan otomatis mengubah response JSON ke data class ApiResponse

                if (response.status == "sukses") {
                    Log.d("WorkerScanVM", "Validasi sukses: ${response.message}")
                    _scanResultState.value = ScanResultState.Success(response)
                } else {
                    Log.w("WorkerScanVM", "Validasi gagal dari server: ${response.message}")
                    _scanResultState.value = ScanResultState.Error(response.message)
                }

            } catch (e: ResponseException) {
                // Menangani error HTTP (seperti 4xx Not Found, 5xx Server Error)
                val errorBody = try { e.response.body<ApiResponse>().message } catch (_: Exception) { e.message }
                Log.e("WorkerScanVM", "Error HTTP ${e.response.status.value}: $errorBody", e)
                _scanResultState.value = ScanResultState.Error(errorBody ?: "Error dari server.")
            } catch (e: Exception) {
                // Menangani error lain seperti masalah jaringan (tidak ada internet)
                Log.e("WorkerScanVM", "Gagal validasi token: ${e.message}", e)
                _scanResultState.value = ScanResultState.Error("Terjadi kesalahan jaringan atau server.")
            }
        }
    }

    fun clearState() {
        _scanResultState.value = ScanResultState.Idle
    }

    // Penting: Tutup Ktor client saat ViewModel tidak lagi digunakan untuk melepas resource
    override fun onCleared() {
        super.onCleared()
        httpClient.close()
        Log.d("WorkerScanVM", "Ktor httpClient closed.")
    }
}