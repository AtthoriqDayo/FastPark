// File: ParkingSession.kt
// (Pastikan berada di package yang benar, misal: com.example.fastpark.data)
package com.example.fastpark.data // Sesuaikan jika package Anda berbeda

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ParkingSession(
    val uid: String = "",
    @ServerTimestamp val entryTimestamp: Timestamp? = null,
    var exitTimestamp: Timestamp? = null,
    var status: String = "",             // "active", "completed", "paid"

    // TAMBAHKAN ATAU PASTIKAN FIELD INI ADA:
    var parkingLocation: String? = null, // Untuk ditampilkan di UserParkingStatusScreen
    var parkingFee: Double? = null,      // Untuk ditampilkan di UserParkingStatusScreen & digunakan di ViewModel

    // Field lain yang mungkin sudah Anda miliki atau butuhkan untuk konsistensi:
    val parkingArea: String? = null,      // Ini mungkin sama atau berhubungan dengan parkingLocation
    val pricePerHour: Double? = null,     // Jika Anda menyimpannya per sesi
    var durationMinutes: Long? = null,
    var feeCalculated: Double? = null,   // Ini bisa jadi sama dengan parkingFee
    val workerId_entry: String? = null,
    var workerId_exit: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp var updatedAt: Timestamp? = null
) {
    // Konstruktor tanpa argumen diperlukan untuk deserialisasi Firestore
    constructor() : this(
        uid = "",
        entryTimestamp = null,
        exitTimestamp = null,
        status = "",
        parkingLocation = null, // Inisialisasi
        parkingFee = null,      // Inisialisasi
        parkingArea = null,
        pricePerHour = null,
        durationMinutes = null,
        feeCalculated = null,
        workerId_entry = null,
        workerId_exit = null,
        createdAt = null,
        updatedAt = null
    )
}