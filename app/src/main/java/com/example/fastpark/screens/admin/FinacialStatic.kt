package com.example.fastpark.screens.admin

import android.graphics.Typeface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.FinancialViewModel
import com.example.fastpark.viewmodel.Timeframe
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.NumberFormat
import java.util.*


@Composable
fun FinancialStatisticsScreen(
    financialViewModel: FinancialViewModel = viewModel()
) {
    val selectedTimeframe by financialViewModel.selectedTimeframe.collectAsState()
    val chartData by financialViewModel.chartData.collectAsState()
    val isLoading by financialViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tombol Pilihan Rentang Waktu
        SegmentedButton(
            selected = selectedTimeframe,
            onSelected = { financialViewModel.selectTimeframe(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            chartData?.let { data ->
                // Kartu Statistik Ringkasan
                SummaryCards(
                    totalRevenue = data.totalRevenue,
                    totalSessions = data.totalSessions
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Kartu Chart
                LineChartCard(
                    title = "Pendapatan ${selectedTimeframe.label}",
                    chartData = data.entries
                )
            }
        }
    }
}

@Composable
fun SegmentedButton(selected: Timeframe, onSelected: (Timeframe) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        Timeframe.entries.forEach { timeframe ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.baseShape,
                onClick = { onSelected(timeframe) },
                selected = timeframe == selected
            ) {
                Text(timeframe.label)
            }
        }
    }
}

@Composable
fun SummaryCards(totalRevenue: Double, totalSessions: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }

        SummaryCard(
            label = "Total Pendapatan",
            value = currencyFormat.format(totalRevenue),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Total Parkir",
            value = totalSessions.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
fun LineChartCard(title: String, chartData: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (chartData.isEmpty()) {
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

                            // Pengaturan Sumbu X (Bawah)
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            xAxis.granularity = 1f
                            xAxis.typeface = Typeface.DEFAULT_BOLD

                            // Pengaturan Sumbu Y (Kiri)
                            axisLeft.setDrawGridLines(true)
                            axisLeft.axisMinimum = 0f
                            axisLeft.typeface = Typeface.DEFAULT

                            // Pengaturan Sumbu Y (Kanan)
                            axisRight.isEnabled = false
                        }
                    },

                    update = { lineChart ->
                        val entries = chartData.values.mapIndexed { index, value ->
                            Entry(index.toFloat(), value.toFloat())
                        }
                        val labels = chartData.keys.toList()

                        val dataSet = LineDataSet(entries, "Pendapatan").apply {
                            color = DeepRed.toArgb()
                            valueTextColor = Color.Black.toArgb()
                            valueTextSize = 10f
                            setDrawValues(false)
                            mode = LineDataSet.Mode.CUBIC_BEZIER // <-- INI PENYEBAB MASALAHNYA
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
                        lineChart.xAxis.labelCount = labels.size.coerceAtMost(7)
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