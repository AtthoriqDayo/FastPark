package com.example.fastpark.screens.workers

import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

// Define the ParkingSession data class.
data class ParkingSession(
    var sessionId: String = "",
    val status: String = "",
    val entryTimestamp: Timestamp? = null,
)

@Composable
fun ParkingStatsScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Real-Time Active Parking Users Chart
        Text(
            text = "Pengguna Sedang Parkir",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RealTimeParkingChart() // This will be BarChart

        Spacer(modifier = Modifier.height(32.dp))

        // Daily Paid Parking Users Chart
        Text(
            text = "Data Parkir",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DailyParkingChart() // This will remain LineChart
    }
}

@Composable
fun RealTimeParkingChart() {
    val activeSessions = remember { mutableStateOf<List<ParkingSession>>(emptyList()) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        firestore.collection("parking_sessions")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("RealTimeChart", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ParkingSession::class.java)?.apply {
                        sessionId = doc.id
                    }
                } ?: emptyList()

                activeSessions.value = sessions
            }
    }

    val activeUserCount = activeSessions.value.size

    // For BarChart, we usually represent categories.
    // For a single "active user count", we can use one BarEntry.
    val chartEntries = listOf(BarEntry(0f, activeUserCount.toFloat())) // Use BarEntry

    val barDataSet = BarDataSet(chartEntries, "Pengguna").apply { // Use BarDataSet
        color = Color.Blue.toArgb()
        valueTextSize = 10f
        // Removed LineChart specific properties like setDrawCircles, mode, circleRadius, circleColors
    }

    val barData = BarData(barDataSet) // Use BarData

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { context ->
                BarChart(context).apply { // Changed to BarChart
                    data = barData
                    description.isEnabled = false
                    legend.isEnabled = true
                    setFitBars(true) // Add back for BarChart

                    xAxis.isEnabled = false // No specific labels needed for a single bar
                    axisRight.isEnabled = false // Disable right Y-axis

                    axisLeft.apply {
                        axisMinimum = 0f
                        granularity = 1f
                        setDrawLabels(true)
                    }
                    invalidate()
                }
            },
            update = { barChart -> // Changed to barChart
                val updatedChartEntries = listOf(BarEntry(0f, activeUserCount.toFloat())) // Use BarEntry
                val updatedBarDataSet = BarDataSet(updatedChartEntries, "Pengguna").apply { // Use BarDataSet
                    color = Color.Blue.toArgb()
                    valueTextSize = 10f
                }
                val updatedBarData = BarData(updatedBarDataSet) // Use BarData

                barChart.data = updatedBarData
                barChart.notifyDataSetChanged()
                barChart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

@Composable
fun DailyParkingChart() {
    val dailySessions = remember { mutableStateOf<List<ParkingSession>>(emptyList()) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        firestore.collection("parking_sessions")
            .whereEqualTo("status", "paid")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DailyChart", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ParkingSession::class.java)?.apply {
                        sessionId = doc.id
                    }
                } ?: emptyList()

                dailySessions.value = sessions
            }
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sessionsGroupedByDay = dailySessions.value
        .groupBy {
            it.entryTimestamp?.toDate()?.let { date ->
                dateFormat.format(date)
            }
        }
        .filterKeys { it != null }

    val sortedSessionsGroupedByDay = sessionsGroupedByDay.entries.sortedBy { it.key }
    val dates = sortedSessionsGroupedByDay.map { it.key }

    val chartEntries = sortedSessionsGroupedByDay.mapIndexed { index, entry ->
        Entry(index.toFloat(), entry.value.size.toFloat())
    }

    val lineDataSet = LineDataSet(chartEntries, "Pengguna").apply {
        color = Color.Green.toArgb()
        valueTextSize = 10f
        setDrawCircles(true)
        setDrawValues(true)
        mode = LineDataSet.Mode.CUBIC_BEZIER
        lineWidth = 2f
        circleRadius = 4f
        circleColors = listOf(Color.Green.toArgb())
    }

    val lineData = LineData(lineDataSet)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                        granularity = 1f
                        setDrawLabels(true)
                    }
                    invalidate()
                }
            },
            update = { lineChart ->
                val updatedChartEntries = sortedSessionsGroupedByDay.mapIndexed { index, entry ->
                    Entry(index.toFloat(), entry.value.size.toFloat())
                }
                val updatedLineDataSet = LineDataSet(updatedChartEntries, "Pengguna").apply {
                    color = Color.Green.toArgb()
                    valueTextSize = 10f
                    setDrawCircles(true)
                    setDrawValues(true)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    lineWidth = 2f
                    circleRadius = 4f
                    circleColors = listOf(Color.Green.toArgb())
                }
                val updatedLineData = LineData(updatedLineDataSet)

                lineChart.data = updatedLineData
                lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                lineChart.xAxis.labelCount = dates.size
                lineChart.notifyDataSetChanged()
                lineChart.invalidate()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}