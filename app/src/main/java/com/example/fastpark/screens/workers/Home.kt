package com.example.fastpark.screens.workers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.screens.components.BannerSection
import com.example.fastpark.screens.components.HomeMenu
import com.example.fastpark.screens.components.MenuGrid
import com.example.fastpark.screens.components.SearchUserBar
import com.example.fastpark.screens.components.WorkerHeader
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "User",
) {
    var selectedMenu by remember { mutableStateOf(HomeMenu.PARKING) }
    var searchQuery by remember { mutableStateOf("") }

    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FloatingActionButton(
                    onClick = { /* TODO: Aksi ketika FAB diklik */
                        println("Floating Action Button clicked!")
                    },
                    containerColor = BrightRed,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick Action Button",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aksi Cepat",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DeepRed, BrightRed)
                        ),
                        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                    )
                    .padding(top = 50.dp, bottom = 24.dp)
            ) {
                WorkerHeader(userName)

                Spacer(Modifier.height(12.dp))

                SearchUserBar(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        searchQuery = newQuery
                        isSearching = newQuery.isNotEmpty()
                        // TODO: Anda bisa memicu pencarian data di sini
                        println("Searching for: $newQuery")
                    },
                    onSearchClose = {
                        isSearching = false
                        searchQuery = ""
                        println("Search bar closed.")
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (isSearching) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Mulai ketik untuk mencari..." else "Menampilkan hasil untuk: \"$searchQuery\"",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    // TODO: Di sini Anda akan menampilkan daftar hasil pencarian, mungkin menggunakan LazyColumn
                    // Contoh: LazyColumn { items(filteredSearchResults) { item -> Text(item.name) } }
                }
            } else {


                Spacer(Modifier.height(20.dp)) // Jarak antara header dan BalanceCard

                BannerSection() // BalanceCard Anda

                Spacer(Modifier.height(20.dp))

                Spacer(Modifier.height(24.dp))
                MenuGrid { selectedMenu = it  }

                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    when (selectedMenu) {
                        HomeMenu.PARKING -> ParkingScreen()
                        HomeMenu.MAIL    -> MailScreen()
                        HomeMenu.HISTORY -> HistoryScreen()
                        HomeMenu.CHART   -> ChartScreen()
                    }
                }
            }
        }
    }
}

@Composable fun ParkingScreen() = CenterText("Isi Parking")
@Composable fun MailScreen()    = CenterText("Isi Mail")
@Composable fun HistoryScreen() = CenterText("Isi History")
@Composable fun ChartScreen()   = CenterText("Isi Chart")

@Composable
private fun CenterText(text: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text)
}