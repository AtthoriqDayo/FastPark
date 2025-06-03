package com.example.fastpark.viewmodel // Pastikan package sesuai

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil
import com.example.fastpark.data.ParkingSession // Pastikan import ini benar
import com.example.fastpark.data.User // Pastikan User data class juga diimport jika belum

class WorkerParkingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState

    private val DEFAULT_PRICE_PER_HOUR = 2000.0
    private val DEFAULT_PARKING_AREA = "MainLot-Default"
    private val MINIMUM_FEE = 2000.0

    data class ScanUiState(
        val isLoading: Boolean = false,
        val message: String? = null,
        val isError: Boolean = false
    )

    fun processScannedUserId(scannedUid: String) { // Diubah ke scannedUid
        viewModelScope.launch {
            _uiState.value = ScanUiState(isLoading = true)
            val currentWorkerId = auth.currentUser?.uid

            Log.d("WorkerVM", "Processing User UID: $scannedUid by Worker: $currentWorkerId")

            try {
                val activeSessionQuery = db.collection("parking_sessions")
                    .whereEqualTo("uid", scannedUid) // DIUBAH: dari "userId" ke "uid"
                    .whereEqualTo("status", "active")
                    .limit(1)
                    .get()
                    .await()

                if (!activeSessionQuery.isEmpty) {
                    val activeSessionDoc = activeSessionQuery.documents[0]
                    // Pastikan ParkingSession diimport dan memiliki konstruktor yang sesuai
                    val session = activeSessionDoc.toObject(ParkingSession::class.java)
                    if (session?.entryTimestamp != null && session.uid == scannedUid) { // Tambahan check session.uid
                        handleExit(activeSessionDoc.id, session, currentWorkerId, scannedUid)
                    } else if (session?.uid != scannedUid && session?.entryTimestamp != null) {
                        Log.e("WorkerVM", "Sesi aktif ditemukan untuk uid ${session?.uid} bukan untuk $scannedUid yang di-scan.")
                        _uiState.value = ScanUiState(message = "Error: Sesi aktif yang ditemukan tidak cocok dengan QR Code user.", isError = true)
                    }
                    else {
                        Log.e("WorkerVM", "Sesi aktif ${activeSessionDoc.id} tidak memiliki entryTimestamp atau UID tidak valid.")
                        _uiState.value = ScanUiState(message = "Error: Sesi aktif tidak valid (missing entry time or invalid UID).", isError = true)
                    }
                } else {
                    handleEntry(scannedUid, currentWorkerId)
                }
            } catch (e: Exception) {
                Log.e("WorkerVM", "Error processing scan for $scannedUid: ${e.message}", e)
                _uiState.value = ScanUiState(message = "Error: ${e.message}", isError = true)
            }
        }
    }

    private suspend fun handleEntry(userUid: String, workerId: String?) { // Diubah ke userUid
        val parkingAreaForNewSession = DEFAULT_PARKING_AREA
        val pricePerHourForNewSession = DEFAULT_PRICE_PER_HOUR

        val newSession = ParkingSession(
            uid = userUid, // DIUBAH: dari userId ke uid
            parkingArea = parkingAreaForNewSession,
            pricePerHour = pricePerHourForNewSession,
            entryTimestamp = Timestamp.now(),
            status = "active",
            workerId_entry = workerId,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
            // Pastikan semua field yang relevan di ParkingSession diinisialisasi
        )

        try {
            val docRef = db.collection("parking_sessions").add(newSession).await()
            Log.d("WorkerVM", "Sesi parkir ${docRef.id} dimulai untuk user $userUid.")
            _uiState.value = ScanUiState(message = "User $userUid MASUK. Sesi ID: ${docRef.id}")
            // TODO: Logika buka palang masuk
        } catch (e: Exception) {
            Log.e("WorkerVM", "Error memulai sesi untuk user $userUid: ${e.message}", e)
            _uiState.value = ScanUiState(message = "Error memulai sesi: ${e.message}", isError = true)
        }
    }

    private suspend fun handleExit(sessionId: String, sessionData: ParkingSession, workerId: String?, scannedUid: String) { // Diubah ke scannedUid
        val entryTimestamp = sessionData.entryTimestamp!! // Asumsi sudah dicek tidak null
        val exitTimestamp = com.google.firebase.Timestamp.now() // Gunakan Timestamp.now() agar konsisten
        val durationMillis = exitTimestamp.toDate().time - entryTimestamp.toDate().time
        val durationMinutes = durationMillis / (1000 * 60)

        val pricePerHour = sessionData.pricePerHour ?: DEFAULT_PRICE_PER_HOUR
        var feeCalculated = ceil(durationMinutes / 60.0) * pricePerHour
        if (feeCalculated < MINIMUM_FEE) feeCalculated = MINIMUM_FEE
        if (feeCalculated < 0) feeCalculated = MINIMUM_FEE

        val sessionRef = db.collection("parking_sessions").document(sessionId)
        // Dokumen user di koleksi "users" tetap diakses dengan scannedUid (yang merupakan Firebase Auth UID)
        val userRef = db.collection("users").document(scannedUid)

        try {
            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                // Pastikan User data class diimport dan digunakan jika perlu,
                // atau akses field "balance" langsung jika tidak menggunakan toObject(User::class.java)
                val currentUserBalance = userSnapshot.getDouble("balance") ?: 0.0

                if (currentUserBalance < feeCalculated) {
                    Log.w("WorkerVM", "Saldo user $scannedUid tidak cukup: $currentUserBalance < $feeCalculated")
                    transaction.update(sessionRef, mapOf(
                        "exitTimestamp" to exitTimestamp,
                        "durationMinutes" to durationMinutes,
                        "feeCalculated" to feeCalculated,
                        "status" to "completed", // Saldo tidak cukup, menunggu pembayaran
                        "workerId_exit" to workerId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ))
                    _uiState.value = ScanUiState(message = "User $scannedUid KELUAR. Biaya Rp ${"%,.0f".format(feeCalculated)}. SALDO TIDAK CUKUP!", isError = true)
                    return@runTransaction null
                }

                val newBalance = currentUserBalance - feeCalculated
                transaction.update(userRef, "balance", newBalance)

                transaction.update(sessionRef, mapOf(
                    "exitTimestamp" to exitTimestamp,
                    "durationMinutes" to durationMinutes,
                    "feeCalculated" to feeCalculated,
                    "status" to "paid", // Lunas
                    "workerId_exit" to workerId,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                _uiState.value = ScanUiState(message = "User $scannedUid KELUAR. Biaya Rp ${"%,.0f".format(feeCalculated)}. Saldo dipotong.")
                null
            }.await()
            Log.d("WorkerVM", "Proses keluar untuk sesi $sessionId selesai.")
            // TODO: Logika buka palang keluar
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.ABORTED) {
                Log.w("WorkerVM", "Transaksi keluar dibatalkan untuk user $scannedUid: ${e.message}")
                if (_uiState.value.message == null || !_uiState.value.isError) { // Hanya update jika belum ada pesan error saldo
                    _uiState.value = ScanUiState(message = "Transaksi keluar gagal: ${e.message}", isError = true)
                }
            } else {
                Log.e("WorkerVM", "Error transaksi keluar untuk sesi $sessionId: ${e.message}", e)
                _uiState.value = ScanUiState(message = "Error transaksi keluar: ${e.message}", isError = true)
            }
        }
        catch (e: Exception) {
            Log.e("WorkerVM", "Error tak terduga saat keluar untuk sesi $sessionId: ${e.message}", e)
            _uiState.value = ScanUiState(message = "Error tak terduga: ${e.message}", isError = true)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, isError = false)
    }
}