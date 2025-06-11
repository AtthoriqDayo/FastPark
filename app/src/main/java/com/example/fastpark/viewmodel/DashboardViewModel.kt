package com.example.fastpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

// Data class untuk menampung semua data yang dibutuhkan dasbor
data class DashboardData(
    val todayRevenue: Double = 0.0,
    val todayCompletedSessions: Int = 0,
    val todayNewUsers: Int = 0,
    val currentlyParking: Int = 0
)

class DashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _dashboardData = MutableStateFlow<DashboardData?>(null)
    val dashboardData: StateFlow<DashboardData?> = _dashboardData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchDashboardData()
    }

    fun fetchDashboardData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Tentukan waktu awal hari ini (jam 00:00:00)
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfToday = Timestamp(calendar.time)

                // 1. Ambil data parkir aktif
                val activeSessionsSnapshot = firestore.collection("parking_sessions")
                    .whereEqualTo("status", "active")
                    .get().await()
                val currentlyParking = activeSessionsSnapshot.size()

                // 2. Ambil data parkir yang selesai hari ini
                val completedSessionsSnapshot = firestore.collection("parking_sessions")
                    .whereEqualTo("status", "completed")
                    .whereGreaterThanOrEqualTo("exitTimestamp", startOfToday)
                    .get().await()
                val todayCompletedSessions = completedSessionsSnapshot.size()
                val todayRevenue = completedSessionsSnapshot.documents.sumOf {
                    it.getDouble("feeCalculated") ?: 0.0
                }

                // 3. Ambil data pengguna baru hari ini
                val newUsersSnapshot = firestore.collection("users")
                    .whereGreaterThanOrEqualTo("createdAt", startOfToday)
                    .get().await()
                val todayNewUsers = newUsersSnapshot.size()

                // Update state dengan semua data yang sudah dihitung
                _dashboardData.value = DashboardData(
                    currentlyParking = currentlyParking,
                    todayCompletedSessions = todayCompletedSessions,
                    todayRevenue = todayRevenue,
                    todayNewUsers = todayNewUsers
                )

            } catch (e: Exception) {
                // Handle error
                _dashboardData.value = DashboardData()
            } finally {
                _isLoading.value = false
            }
        }
    }
}