// File: ParkingViewModel.kt (DITULIS ULANG SESUAI ALUR BARU)
package com.example.fastpark.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.floor

class ParkingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // State baru untuk UI Pengguna menggunakan Sealed Class agar lebih terstruktur
    private val _uiState = MutableStateFlow<ParkingUiState>(ParkingUiState.Loading)
    val uiState: StateFlow<ParkingUiState> = _uiState

    private var sessionListener: ListenerRegistration? = null

    // Tarif parkir per jam (pastikan sama dengan yang ada di backend jika perlu)
    private val HOURLY_RATE = 2000.0

    /**
     * Sealed class untuk merepresentasikan semua kemungkinan state UI.
     * Ini membuat kode di Composable Anda lebih bersih dan aman.
     */
    sealed class ParkingUiState {
        object Loading : ParkingUiState() // State saat data sedang dimuat
        object NoActiveSession : ParkingUiState() // State jika tidak ada sesi parkir
        data class ActiveSession(
            val entryTime: Timestamp,
            val duration: String = "00:00:00",
            val estimatedCost: Double = 0.0
        ) : ParkingUiState() // State jika ada sesi parkir aktif
        data class Error(val message: String) : ParkingUiState() // State jika terjadi error
    }

    init {
        // Mulai memantau sesi parkir saat ViewModel dibuat
        observeActiveParkingSession()
    }

    fun observeActiveParkingSession() {
        val userUid = auth.currentUser?.uid
        if (userUid == null) {
            _uiState.value = ParkingUiState.NoActiveSession
            Log.w("ParkingVM_User", "User tidak login, tidak bisa memantau sesi.")
            return
        }

        _uiState.value = ParkingUiState.Loading
        sessionListener?.remove() // Hapus listener lama jika ada

        // --- PERUBAHAN UTAMA DI SINI ---
        // Kita sekarang melakukan query ke koleksi "parking_sessions"
        // untuk mencari dokumen dengan uid pengguna DAN status "active".
        val query = firestore.collection("parking_sessions")
            .whereEqualTo("uid", userUid)
            .whereEqualTo("status", "active") // Filter berdasarkan status
            .limit(1) // Asumsi pengguna hanya punya 1 sesi aktif, ambil 1 saja.

        sessionListener = query.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.e("ParkingVM_User", "Error mendengarkan parking_sessions: ", e)
                _uiState.value = ParkingUiState.Error("Gagal memuat status parkir.")
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                // Dokumen DITEMUKAN -> Pengguna sedang parkir aktif
                val activeSessionDoc = querySnapshot.documents.first() // Ambil dokumen pertama dari hasil query
                // Menggunakan "entryTimestamp" sesuai screenshot Anda
                val entryTimestamp = activeSessionDoc.getTimestamp("entryTimestamp")

                if (entryTimestamp != null) {
                    Log.d("ParkingVM_User", "Sesi aktif ditemukan untuk user $userUid.")
                    startDurationTimer(entryTimestamp)
                } else {
                    Log.e("ParkingVM_User", "Sesi aktif ditemukan tapi field 'entryTimestamp' tidak ada.")
                    _uiState.value = ParkingUiState.Error("Data sesi aktif tidak valid.")
                }
            } else {
                // Dokumen TIDAK DITEMUKAN -> Tidak ada sesi parkir aktif
                Log.d("ParkingVM_User", "Tidak ada sesi aktif untuk user $userUid.")
                _uiState.value = ParkingUiState.NoActiveSession
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun startDurationTimer(entryTime: Timestamp) {
        viewModelScope.launch {
            _uiState.value = ParkingUiState.ActiveSession(entryTime)

            // Loop ini akan terus berjalan untuk memperbarui durasi dan biaya setiap detik
            // selama state UI masih ActiveSession dan waktu masuknya sama.
            while (_uiState.value is ParkingUiState.ActiveSession && (_uiState.value as ParkingUiState.ActiveSession).entryTime == entryTime) {
                val currentState = _uiState.value as ParkingUiState.ActiveSession
                val entryTimeMillis = entryTime.toDate().time
                val diffMillis = Date().time - entryTimeMillis

                if (diffMillis < 0) { // Menghindari durasi negatif jika ada perbedaan waktu perangkat
                    delay(1000)
                    continue
                }

                // Kalkulasi Jam, Menit, Detik
                val hours = (diffMillis / (1000 * 60 * 60))
                val minutes = (diffMillis / (1000 * 60)) % 60
                val seconds = (diffMillis / 1000) % 60
                val durationStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                // Kalkulasi estimasi biaya berdasarkan jam yang telah berlalu (pembulatan ke bawah)
                // Ditambah 1 untuk selalu menghitung jam berjalan sebagai jam penuh.
                val hoursParked = floor(diffMillis / (1000.0 * 60.0 * 60.0))
                val estimatedCost = (hoursParked + 1) * HOURLY_RATE

                // Update state dengan durasi dan biaya baru
                _uiState.value = currentState.copy(
                    duration = durationStr,
                    estimatedCost = estimatedCost
                )

                delay(1000) // Tunggu 1 detik sebelum kalkulasi ulang
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionListener?.remove() // Penting untuk membersihkan listener agar tidak ada memory leak
        Log.d("ParkingVM_User", "ParkingViewModel onCleared, listener removed.")
    }
}