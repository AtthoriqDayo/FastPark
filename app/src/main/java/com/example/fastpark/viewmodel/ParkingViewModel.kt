package com.example.fastpark.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date // Untuk kalkulasi timer
import com.example.fastpark.data.ParkingSession // Pastikan path import ini benar

class ParkingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _activeSession = MutableStateFlow<ParkingSession?>(null)
    val activeSession: StateFlow<ParkingSession?> = _activeSession

    private val _parkingDuration = MutableStateFlow<String>("00:00:00")
    val parkingDuration: StateFlow<String> = _parkingDuration

    private val _estimatedCost = MutableStateFlow<Double>(0.0)
    val estimatedCost: StateFlow<Double> = _estimatedCost

    private var sessionListener: ListenerRegistration? = null

    // Tarif parkir per jam (contoh sederhana, bisa lebih kompleks)
    private val HOURLY_RATE = 1000.0 // Anda menggunakan 1000.0 di kode Anda

    init {
        observeActiveParkingSession()
    }

    private fun observeActiveParkingSession() {
        val userId = auth.currentUser?.uid // Jika Anda beralih ke 'uid' di Firestore, sesuaikan juga query ini
        if (userId == null) {
            _activeSession.value = null
            _parkingDuration.value = "00:00:00" // Reset saat tidak ada user
            _estimatedCost.value = 0.0        // Reset saat tidak ada user
            return
        }

        sessionListener?.remove()

        sessionListener = firestore.collection("parking_sessions")
            .whereEqualTo("userId", userId) // ATAU "uid" jika Anda sudah ganti di Firestore & model
            .whereEqualTo("status", "active")
            .limit(1)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    _activeSession.value = null
                    _parkingDuration.value = "00:00:00"
                    _estimatedCost.value = 0.0
                    // Pertimbangkan untuk menampilkan error ke UI di sini jika perlu
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val session = snapshots.documents[0].toObject(ParkingSession::class.java)
                    _activeSession.value = session
                    // Panggil calculateDurationAndCost hanya jika sesi benar-benar berubah
                    // atau jika sesi adalah null sebelumnya dan sekarang ada isinya.
                    // Ini untuk menghindari kalkulasi ulang yang tidak perlu jika ada update minor lain pada dokumen
                    // Namun, untuk timer real-time, pemanggilan di sini sudah benar.
                    calculateDurationAndCost(session)
                } else {
                    _activeSession.value = null
                    _parkingDuration.value = "00:00:00"
                    _estimatedCost.value = 0.0
                }
            }
    }

    @SuppressLint("DefaultLocale")
    private fun calculateDurationAndCost(session: ParkingSession?) {
        viewModelScope.launch {
            val currentSession = _activeSession.value // Gunakan nilai terkini dari StateFlow untuk perbandingan di loop

            if (session?.entryTimestamp != null) {
                val entryTimeMillis = session.entryTimestamp.toDate().time

                // Loop untuk timer real-time selama sesi aktif
                while (session.status == "active" && currentSession?.entryTimestamp == session.entryTimestamp && currentSession.status == "active") {
                    val currentTimeMillis = Date().time
                    val diffMillis = currentTimeMillis - entryTimeMillis

                    if (diffMillis < 0) {
                        _parkingDuration.value = "00:00:00"
                        _estimatedCost.value = 0.0
                        kotlinx.coroutines.delay(1000)
                        continue
                    }

                    val seconds = (diffMillis / 1000) % 60
                    val minutes = (diffMillis / (1000 * 60)) % 60
                    val hours = (diffMillis / (1000 * 60 * 60))

                    _parkingDuration.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    val hoursParked = diffMillis / (1000.0 * 60.0 * 60.0)
                    _estimatedCost.value = hoursParked * HOURLY_RATE

                    kotlinx.coroutines.delay(1000)
                    // Update currentSession di dalam loop jika _activeSession bisa berubah oleh listener lain secara bersamaan
                    // namun karena listener Firestore ada di ViewModel ini juga, seharusnya aman.
                    // Untuk lebih aman jika ada modifikasi _activeSession dari luar, bisa baca ulang:
                    // if (_activeSession.value?.status != "active" || _activeSession.value?.entryTimestamp != session.entryTimestamp) break
                }

                // Setelah loop selesai (sesi tidak aktif lagi atau sesi berubah)
                // Pastikan kita menggunakan data 'session' yang paling baru yang mungkin sudah diupdate oleh Firestore listener
                val latestSessionData = _activeSession.value

                if (latestSessionData != null && latestSessionData.status != "active") {
                    val localEntryTimestamp = latestSessionData.entryTimestamp // Ambil dari data sesi terbaru
                    val localExitTimestamp = latestSessionData.exitTimestamp // Ambil dari data sesi terbaru

                    if (localExitTimestamp != null && localEntryTimestamp != null) {
                        val finalDiffMillis = localExitTimestamp.toDate().time - localEntryTimestamp.toDate().time
                        val finalSeconds = (finalDiffMillis / 1000) % 60
                        val finalMinutes = (finalDiffMillis / (1000 * 60)) % 60
                        val finalHours = (finalDiffMillis / (1000 * 60 * 60))
                        _parkingDuration.value = String.format("%02d:%02d:%02d", finalHours, finalMinutes, finalSeconds)

                        val currentParkingFee = latestSessionData.parkingFee // Ambil dari data sesi terbaru
                        _estimatedCost.value = currentParkingFee ?: ((finalDiffMillis / (1000.0 * 60.0 * 60.0)) * HOURLY_RATE)
                    } else {
                        // Sesi tidak aktif tapi tidak ada exit/entry timestamp yang valid, reset jika belum sesuai
                        if(latestSessionData.status != "active" && _parkingDuration.value != "00:00:00") {
                            _parkingDuration.value = "00:00:00"
                            _estimatedCost.value = 0.0
                        }
                    }
                } else if (latestSessionData == null) { // Jika sesi menjadi null setelah loop
                    _parkingDuration.value = "00:00:00"
                    _estimatedCost.value = 0.0
                }
                // Jika status masih active tapi loop berhenti karena sesi berubah (entryTimestamp beda),
                // listener akan memanggil calculateDurationAndCost lagi dengan sesi baru.

            } else { // Jika session awal null atau tidak ada entryTimestamp
                _parkingDuration.value = "00:00:00"
                _estimatedCost.value = 0.0
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionListener?.remove() // Hentikan listener saat ViewModel dihancurkan
    }
}