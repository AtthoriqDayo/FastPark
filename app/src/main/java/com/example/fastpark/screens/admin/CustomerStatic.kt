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
import com.example.fastpark.viewmodel.AccountViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun CustomerStatisticsScreen(
    accountViewModel: AccountViewModel = viewModel()
) {
    val stats by accountViewModel.accountStats.collectAsState()
    val isLoading by accountViewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            stats?.let { data ->
                // Kartu Statistik Ringkasan
                AccountSummaryCards(
                    totalUsers = data.totalUsers,
                    newUsersThisMonth = data.newUsersThisMonth
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Kartu Chart
                UserGrowthChartCard(
                    title = "Pertumbuhan Pengguna per Bulan",
                    growthData = data.userGrowthByMonth
                )
            }
        }
    }
}

@Composable
fun AccountSummaryCards(totalUsers: Int, newUsersThisMonth: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            label = "Total Pengguna",
            value = totalUsers.toString(),
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Baru Bulan Ini",
            value = newUsersThisMonth.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun UserGrowthChartCard(title: String, growthData: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (growthData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Text("Data pertumbuhan belum tersedia")
                }
            } else {
                // Bar Chart
                AndroidView(
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setDrawValueAboveBar(true)

                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            xAxis.granularity = 1f
                            xAxis.typeface = Typeface.DEFAULT_BOLD

                            axisLeft.axisMinimum = 0f
                            axisRight.isEnabled = false
                        }
                    },
                    update = { barChart ->
                        val entries = growthData.values.mapIndexed { index, value ->
                            BarEntry(index.toFloat(), value.toFloat())
                        }
                        val labels = growthData.keys.toList()

                        val dataSet = BarDataSet(entries, "Pengguna Baru").apply {
                            color = DeepRed.toArgb()
                            valueTextColor = Color.Black.toArgb()
                            valueTextSize = 12f
                        }

                        barChart.data = BarData(dataSet)
                        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        barChart.xAxis.labelCount = labels.size.coerceAtMost(6)
                        barChart.invalidate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }
        }
    }
}