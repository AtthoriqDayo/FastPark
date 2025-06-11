package com.example.fastpark.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


/**
 * Merepresentasikan struktur satu dokumen di dalam koleksi 'parking_sessions' di Firestore.
 * Nama field di sini harus sama persis dengan nama field di Firestore.
 */
data class ParkingSession(
    var sessionId: String = "",
    // Informasi Pengguna & Status
    val uid: String = "",
    val status: String = "active", // Contoh: "active", "completed"

    // Informasi Area & Tarif
    val parkingArea: String = "", // Contoh: "MainLot-A1"
    val pricePerHour: Double = 0.0,

    // Informasi Waktu
    @ServerTimestamp val entryTimestamp: Timestamp? = null,
    var exitTimestamp: Timestamp? = null,
    var durationMinutes: Long? = null,

    // Informasi Biaya & Petugas
    var feeCalculated: Double? = null,
    val workerId_entry: String? = null,
    var workerId_exit: String? = null,

    // Timestamps untuk Audit
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null
) {
    /**
     * Konstruktor kosong ini WAJIB ada.
     * Firestore menggunakannya untuk mengubah data dari database menjadi objek Kotlin.
     */
    constructor() : this(
        uid = "",
        status = "active",
        parkingArea = "",
        pricePerHour = 0.0,
        entryTimestamp = null,
        exitTimestamp = null,
        durationMinutes = null,
        feeCalculated = null,
        workerId_entry = null,
        workerId_exit = null,
        createdAt = null,
        updatedAt = null
    ){
        // Helper function to convert Firebase Timestamp to Date
        // This extension function helps with easier date formatting.
        // It is generally good practice to place such extensions in a utility file
        // or directly within the data class if it's tightly coupled.
        fun Timestamp.toDate(): Date {
            return this.toDate()
        }
    }

}