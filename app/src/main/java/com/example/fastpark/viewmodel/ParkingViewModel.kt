package com.example.fastpark.viewmodel

import android.annotation.SuppressLint
import android.util.Log
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

    // Tarif parkir per jam
    private val HOURLY_RATE = 1000.0

    init {
        observeActiveParkingSession()
    }

    private fun observeActiveParkingSession() {
        val userUid = auth.currentUser?.uid
        if (userUid == null) {
            _activeSession.value = null
            _parkingDuration.value = "00:00:00"
            _estimatedCost.value = 0.0
            return
        }

        sessionListener?.remove()

        sessionListener = firestore.collection("parking_sessions")
            .whereEqualTo("uid", userUid) // Menggunakan "uid"
            .whereEqualTo("status", "active")
            .limit(1)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ParkingVM_User", "Error mendengarkan sesi aktif: ", e)
                    _activeSession.value = null
                    _parkingDuration.value = "00:00:00"
                    _estimatedCost.value = 0.0
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val sessionDoc = snapshots.documents[0]
                    Log.d("ParkingVM_User", "Sesi aktif Firestore DITEMUKAN: ID Dokumen = ${sessionDoc.id}, Data = ${sessionDoc.data}")
                    val session = sessionDoc.toObject(ParkingSession::class.java)
                    _activeSession.value = session
                    calculateDurationAndCost(session)
                } else {
                    Log.d("ParkingVM_User", "Tidak ada sesi aktif yang ditemukan di Firestore untuk user $userUid.")
                    _activeSession.value = null
                    calculateDurationAndCost(null)
                }
            }
    }

    @SuppressLint("DefaultLocale")
    private fun calculateDurationAndCost(session: ParkingSession?) {
        viewModelScope.launch {
            if (session?.entryTimestamp != null && session.status == "active") {
                Log.d("ParkingVM_User", "Sesi aktif terdeteksi (UID: ${session.uid}). Mereset timer dan biaya untuk memulai perhitungan baru.")
                _parkingDuration.value = "00:00:00"
                _estimatedCost.value = 0.0

                val entryTimeMillis = session.entryTimestamp.toDate().time

                while (_activeSession.value?.uid == session.uid && _activeSession.value?.status == "active") {
                    if (_activeSession.value?.entryTimestamp != session.entryTimestamp) {
                        Log.w("ParkingVM_User", "EntryTimestamp global berubah untuk sesi aktif, menghentikan loop timer lama untuk sesi ${session.uid}.")
                        break
                    }

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
                }

                if (_activeSession.value?.uid == session.uid && _activeSession.value?.status != "active") {
                    Log.d("ParkingVM_User", "Sesi (UID: ${session.uid}) tidak lagi aktif setelah loop timer. Menampilkan nilai final jika ada.")
                    displayFinalSessionState(_activeSession.value)
                }

            } else if (session != null && session.status != "active") {
                Log.d("ParkingVM_User", "Sesi tidak aktif terdeteksi (UID: ${session.uid}, Status: ${session.status}). Menampilkan nilai final.")
                displayFinalSessionState(session)
            } else {
                Log.d("ParkingVM_User", "Sesi null atau tidak ada entry timestamp. Mereset tampilan timer/biaya.")
                _parkingDuration.value = "00:00:00"
                _estimatedCost.value = 0.0
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun displayFinalSessionState(finalSession: ParkingSession?) {
        // PERBAIKAN: Membaca exitTimestamp dan entryTimestamp ke variabel lokal
        val localExitTimestamp = finalSession?.exitTimestamp
        val localEntryTimestamp = finalSession?.entryTimestamp

        if (localEntryTimestamp != null && localExitTimestamp != null) {
            // Menggunakan variabel lokal yang nilainya stabil dan sudah di-null-check
            val finalDiffMillis = localExitTimestamp.toDate().time - localEntryTimestamp.toDate().time
            if (finalDiffMillis >= 0) {
                val finalSeconds = (finalDiffMillis / 1000) % 60
                val finalMinutes = (finalDiffMillis / (1000 * 60)) % 60
                val finalHours = (finalDiffMillis / (1000 * 60 * 60))
                _parkingDuration.value = String.format("%02d:%02d:%02d", finalHours, finalMinutes, finalSeconds)

                val currentParkingFee = finalSession.parkingFee // Bisa juga dibaca ke var lokal jika sering diakses/mutable
                _estimatedCost.value = currentParkingFee ?: ((finalDiffMillis / (1000.0 * 60.0 * 60.0)) * HOURLY_RATE)
            } else {
                Log.w("ParkingVM_User", "Durasi parkir final negatif untuk sesi UID: ${finalSession.uid}. Exit: $localExitTimestamp, Entry: $localEntryTimestamp")
                _parkingDuration.value = "Error Durasi"
                _estimatedCost.value = finalSession.parkingFee ?: 0.0
            }
        } else {
            Log.d("ParkingVM_User", "Sesi UID: ${finalSession?.uid} status ${finalSession?.status}, tapi exit/entry timestamp tidak lengkap untuk kalkulasi final.")
            _parkingDuration.value = "N/A"
            _estimatedCost.value = finalSession?.parkingFee ?: 0.0
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionListener?.remove()
        Log.d("ParkingVM_User", "ParkingViewModel onCleared, listener removed.")
    }
}