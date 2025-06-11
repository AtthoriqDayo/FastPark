package com.example.fastpark.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fastpark.data.ParkingSession
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Enum untuk pilihan rentang waktu
enum class Timeframe(val days: Int, val label: String) {
    SEVEN_DAYS(7, "7 Hari"),
    ONE_MONTH(30, "1 Bulan"),
    ALL_TIME(0, "Total")
}

// Data class untuk menampung hasil olahan data chart
data class ChartData(
    val entries: Map<String, Double> = emptyMap(),
    val totalRevenue: Double = 0.0,
    val totalSessions: Int = 0
)

class FinancialViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _selectedTimeframe = MutableStateFlow(Timeframe.SEVEN_DAYS)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    private val _chartData = MutableStateFlow<ChartData?>(null)
    val chartData: StateFlow<ChartData?> = _chartData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchDataForSelectedTimeframe()
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _selectedTimeframe.value = timeframe
        fetchDataForSelectedTimeframe()
    }

    private fun fetchDataForSelectedTimeframe() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                if (_selectedTimeframe.value != Timeframe.ALL_TIME) {
                    calendar.add(Calendar.DAY_OF_YEAR, -_selectedTimeframe.value.days)
                }
                val startTime = if (_selectedTimeframe.value == Timeframe.ALL_TIME) null else Timestamp(calendar.time)

                val query = firestore.collection("parking_sessions")
                    .whereEqualTo("status", "paid")
                    .let {
                        if (startTime != null) it.whereGreaterThanOrEqualTo("exitTimestamp", startTime) else it
                    }
                    .orderBy("exitTimestamp", Query.Direction.ASCENDING)

                val snapshot = query.get().await()
                val sessions = snapshot.toObjects(ParkingSession::class.java)

                processChartData(sessions)

            } catch (e: Exception) {
                _chartData.value = ChartData() // Reset data on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun processChartData(sessions: List<ParkingSession>) {
        val totalRevenue = sessions.sumOf { it.feeCalculated ?: 0.0 }
        val totalSessions = sessions.size

        val entries = if (_selectedTimeframe.value == Timeframe.ALL_TIME) {
            // Group by month for "All Time"
            val monthFormat = SimpleDateFormat("MMM ''yy", Locale("id", "ID"))
            sessions.filter { it.exitTimestamp != null && it.feeCalculated != null }
                .groupBy { monthFormat.format(it.exitTimestamp!!.toDate()) }
                .mapValues { entry -> entry.value.sumOf { it.feeCalculated!! } }
        } else {
            // Group by day for "7/30 Days"
            val dayFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))
            sessions.filter { it.exitTimestamp != null && it.feeCalculated != null }
                .groupBy { dayFormat.format(it.exitTimestamp!!.toDate()) }
                .mapValues { entry -> entry.value.sumOf { it.feeCalculated!! } }
        }

        _chartData.value = ChartData(
            entries = entries,
            totalRevenue = totalRevenue,
            totalSessions = totalSessions
        )
    }
}