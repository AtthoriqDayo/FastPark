package com.example.fastpark.screens.workers

import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fastpark.screens.theme.DeepRed
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Data class tetap sama
data class ParkingSession(
    var sessionId: String = "",
    val status: String = "",
    val entryTimestamp: Timestamp? = null,
)

@Composable
fun ParkingStatsScreen() {
    val scrollState = rememberScrollState()
    val activeSessionsCount = remember { mutableStateOf(0) }
    val todaysTotalSessions = remember { mutableStateOf(0) }

    // Mengambil data untuk summary cards
    FetchParkingSummaryData(
        onActiveCountUpdate = { activeSessionsCount.value = it },
        onTodaysTotalUpdate = { todaysTotalSessions.value = it }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. Kartu Ringkasan (Mirip SummaryCards di FinancialStatisticsScreen)
        SummaryCards(
            activeUsers = activeSessionsCount.value,
            totalToday = todaysTotalSessions.value
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Kartu Chart untuk Riwayat Parkir Hari Ini
        TodaysParkingHistoryChartCard()
    }
}

@Composable
fun FetchParkingSummaryData(
    onActiveCountUpdate: (Int) -> Unit,
    onTodaysTotalUpdate: (Int) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()

    // Listener untuk pengguna aktif
    LaunchedEffect(Unit) {
        firestore.collection("parking_sessions")
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ParkingStatsScreen", "Listen failed for active sessions.", e)
                    return@addSnapshotListener
                }
                onActiveCountUpdate(snapshot?.size() ?: 0)
            }
    }

    // Listener untuk total parkir hari ini
    LaunchedEffect(Unit) {
        val (startOfDay, endOfDay) = getTodayTimestamps()
        firestore.collection("parking_sessions")
            .whereGreaterThanOrEqualTo("entryTimestamp", startOfDay)
            .whereLessThanOrEqualTo("entryTimestamp", endOfDay)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ParkingStatsScreen", "Listen failed for today's total sessions.", e)
                    return@addSnapshotListener
                }
                onTodaysTotalUpdate(snapshot?.size() ?: 0)
            }
    }
}


@Composable
fun SummaryCards(activeUsers: Int, totalToday: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            label = "Pengguna Aktif",
            value = activeUsers.toString(),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Total Parkir Hari Ini",
            value = totalToday.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TodaysParkingHistoryChartCard() {
    val dailySessions = remember { mutableStateOf<List<ParkingSession>>(emptyList()) }
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val (startOfDay, endOfDay) = getTodayTimestamps()

        firestore.collection("parking_sessions")
            .whereGreaterThanOrEqualTo("entryTimestamp", startOfDay)
            .whereLessThanOrEqualTo("entryTimestamp", endOfDay)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DailyChart", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ParkingSession::class.java)
                } ?: emptyList()

                dailySessions.value = sessions
            }
    }

    // Mengelompokkan sesi per jam untuk hari ini
    val hourFormat = SimpleDateFormat("HH:00", Locale.getDefault())
    val sessionsGroupedByHour = dailySessions.value
        .groupBy {
            it.entryTimestamp?.toDate()?.let { date ->
                hourFormat.format(date)
            }
        }
        .mapValues { it.value.size }

    // Membuat data untuk 24 jam, mengisi dengan 0 jika tidak ada data
    val hourlyData = (0..23).associate {
        val hourString = String.format(Locale.getDefault(), "%02d:00", it)
        val count = sessionsGroupedByHour[hourString] ?: 0
        hourString to count.toDouble()
    }

    val sortedHourlyData = hourlyData.toSortedMap()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Riwayat Parkir Hari Ini (per Jam)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (sortedHourlyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data untuk ditampilkan")
                }
            } else {
                AndroidView(
                    factory = { context ->
                        LineChart(context).apply {
                            // Pengaturan Umum Chart
                            description.isEnabled = false
                            isDragEnabled = true
                            setScaleEnabled(true)
                            setPinchZoom(true)
                            legend.isEnabled = false

                            // Sumbu X
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            xAxis.granularity = 1f
                            xAxis.typeface = Typeface.DEFAULT_BOLD
                            xAxis.setLabelRotationAngle(45f)


                            // Sumbu Y Kiri
                            axisLeft.setDrawGridLines(true)
                            axisLeft.axisMinimum = 0f
                            axisLeft.granularity = 1f
                            axisLeft.typeface = Typeface.DEFAULT

                            // Sumbu Y Kanan
                            axisRight.isEnabled = false
                        }
                    },
                    update = { lineChart ->
                        val entries = sortedHourlyData.values.mapIndexed { index, value ->
                            Entry(index.toFloat(), value.toFloat())
                        }
                        val labels = sortedHourlyData.keys.toList()

                        val dataSet = LineDataSet(entries, "Jumlah Parkir").apply {
                            color = DeepRed.toArgb()
                            valueTextColor = Color.Black.toArgb()
                            valueTextSize = 10f
                            setDrawValues(false) // Sembunyikan nilai di atas titik
                            mode = LineDataSet.Mode.CUBIC_BEZIER
                            lineWidth = 2.5f
                            setDrawCircles(true)
                            setCircleColor(DeepRed.toArgb())
                            circleRadius = 4f
                            setDrawCircleHole(false)
                            setDrawFilled(true)
                            fillColor = DeepRed.toArgb()
                            fillAlpha = 50
                        }

                        lineChart.data = LineData(dataSet)
                        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        lineChart.xAxis.labelCount = labels.size.coerceAtMost(12) // Batasi jumlah label agar tidak tumpang tindih
                        lineChart.invalidate() // Refresh chart
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}

// Fungsi helper untuk mendapatkan timestamp awal dan akhir hari ini
fun getTodayTimestamps(): Pair<Timestamp, Timestamp> {
    val calendar = Calendar.getInstance()
    // Awal hari
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = Timestamp(calendar.time)

    // Akhir hari
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfDay = Timestamp(calendar.time)

    return Pair(startOfDay, endOfDay)
}