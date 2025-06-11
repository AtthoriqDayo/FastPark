package com.example.fastpark.screens.admin

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

// MPAndroidChart imports
import com.github.mikephil.charting.charts.LineChart // Correct import
import com.github.mikephil.charting.components.XAxis // Correct import
import com.github.mikephil.charting.data.Entry // Correct import
import com.github.mikephil.charting.data.LineData // Correct import
import com.github.mikephil.charting.data.LineDataSet // Correct import for LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter // Correct import

// Firebase imports
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

// Date formatting imports
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Define the ParkingSession data class.
data class ParkingSession(
    var sessionId: String = "",
    val status: String = "",
    val entryTimestamp: Timestamp? = null,
    val feeCalculated: Double = 0.0,
)

@Composable
fun FinancialStatisticsScreen() {
    val scrollState = rememberScrollState()
    val totalRevenue = remember { mutableDoubleStateOf(0.0) }
    val monthlyRevenue = remember { mutableDoubleStateOf(0.0) }
    val firestore = FirebaseFirestore.getInstance()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    LaunchedEffect(Unit) {
        firestore.collection("parking_sessions")
            .whereEqualTo("status", "paid")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FinancialStats", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ParkingSession::class.java)?.apply {
                        sessionId = doc.id
                    }
                } ?: emptyList()

                // Calculate Total Revenue
                totalRevenue.doubleValue = sessions.sumOf { it.feeCalculated }
                Log.d("FinancialStats", "Total Revenue: ${totalRevenue.doubleValue}")

                // Calculate Monthly Revenue
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                monthlyRevenue.doubleValue = sessions
                    .filter { session ->
                        session.entryTimestamp?.toDate()?.let { date ->
                            val cal = Calendar.getInstance().apply { time = date }
                            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                        } ?: false
                    }
                    .sumOf { it.feeCalculated }
                Log.d("FinancialStats", "Monthly Revenue: ${monthlyRevenue.doubleValue}")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Statistik Keuangan", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Total Pendapatan: ${currencyFormat.format(totalRevenue.doubleValue)}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "Pendapatan Bulan Ini: ${currencyFormat.format(monthlyRevenue.doubleValue)}",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Grafik Pendapatan Harian",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )
        DailyRevenueChart()
    }
}

@Composable
fun DailyRevenueChart() {
    val dailyRevenue = remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        firestore.collection("parking_sessions")
            .whereEqualTo("status", "paid")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DailyRevenueChart", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ParkingSession::class.java)?.apply {
                        sessionId = doc.id
                    }
                } ?: emptyList()

                Log.d("DailyRevenueChart", "Fetched ${sessions.size} paid sessions.")

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val revenueGroupedByDay = sessions
                    .filter { it.entryTimestamp != null }
                    .groupBy {
                        dateFormat.format(it.entryTimestamp!!.toDate())
                    }
                    .mapValues { (_, sessionsInDay) ->
                        sessionsInDay.sumOf { it.feeCalculated }
                    }

                dailyRevenue.value = revenueGroupedByDay
                Log.d("DailyRevenueChart", "Daily Revenue Data: ${dailyRevenue.value}")
                if (dailyRevenue.value.isEmpty()) {
                    Log.w("DailyRevenueChart", "No daily revenue data to display. Check Firestore 'paid' sessions with 'entryTimestamp' and 'feeCalculated'.")
                }
            }
    }

    val sortedDailyRevenue = dailyRevenue.value.entries.sortedBy { it.key }
    val dates = sortedDailyRevenue.map { it.key }

    val chartEntries = sortedDailyRevenue.mapIndexed { index, entry ->
        Entry(index.toFloat(), entry.value.toFloat())
    }

    val lineDataSet = LineDataSet(chartEntries, "Pendapatan").apply {
        color = Color.Red.toArgb()
        valueTextSize = 10f
        setDrawCircles(true)
        setDrawValues(true)
        mode = LineDataSet.Mode.CUBIC_BEZIER
        lineWidth = 2f
        circleRadius = 4f
        circleColors = listOf(Color.Red.toArgb())
    }

    val lineData = LineData(lineDataSet)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (chartEntries.isEmpty()) {
            Text(
                "Tidak ada data pendapatan untuk ditampilkan.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        data = lineData
                        description.isEnabled = false
                        legend.isEnabled = true

                        xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(dates)
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelCount = dates.size
                            setCenterAxisLabels(true)
                            setDrawLabels(true)
                            setLabelRotationAngle(45f)
                        }

                        axisRight.isEnabled = false
                        axisLeft.apply {
                            axisMinimum = 0f
                            setDrawLabels(true)
                        }
                        invalidate()
                    }
                },
                update = { lineChart ->
                    val updatedChartEntries = sortedDailyRevenue.mapIndexed { index, entry ->
                        Entry(index.toFloat(), entry.value.toFloat())
                    }
                    val updatedLineDataSet = LineDataSet(updatedChartEntries, "Pendapatan").apply {
                        color = Color.Red.toArgb()
                        valueTextSize = 10f
                        setDrawCircles(true)
                        setDrawValues(true)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        lineWidth = 2f
                        circleRadius = 4f
                        circleColors = listOf(Color.Red.toArgb())
                    }
                    val updatedLineData = LineData(updatedLineDataSet)

                    lineChart.data = updatedLineData
                    lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                    lineChart.xAxis.labelCount = dates.size
                    lineChart.notifyDataSetChanged()
                    lineChart.invalidate()

                    lineChart.axisLeft.apply {
                        axisMinimum = 0f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
    }
}