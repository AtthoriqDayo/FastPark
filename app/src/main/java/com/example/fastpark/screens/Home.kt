package com.example.fastpark.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fastpark.screens.components.BannerSection
import com.example.fastpark.screens.components.HomeMenu
import com.example.fastpark.screens.components.MenuGrid
import com.example.fastpark.screens.components.ParkingMenu
import com.example.fastpark.screens.components.SearchBar
import com.example.fastpark.screens.components.StatusHeader
import com.example.fastpark.ui.theme.BrightRed
import com.example.fastpark.ui.theme.DeepRed


@Composable
fun HomeScreen(
    userName: String = "User"
) {
    var selectedMenu by remember { mutableStateOf(HomeMenu.PARKING) }
    var query by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ─── HEADER ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DeepRed, BrightRed)
                    ),
                    shape = RoundedCornerShape(
                        bottomStart = 30.dp,
                        bottomEnd  = 30.dp
                    )
                )
                .padding(top = 50.dp, bottom = 24.dp)
        ) {
            StatusHeader(userName = userName)

            Spacer(Modifier.height(20.dp))

            SearchBar(
                query         = query,
                onQueryChange = { query = it },
                modifier      = Modifier
                    .padding(horizontal = 16.dp)
                    .height(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        BannerSection()

        Spacer(Modifier.height(24.dp))
        MenuGrid(
            selectedMenu = selectedMenu,
            onMenuClick  = { selectedMenu = it }
        )

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedMenu) {
                HomeMenu.PARKING -> ParkingMenu(
                    onTambahSlot = { /* TODO: Tambah slot parkir */ },
                    onDaftarSlot = { /* TODO: Lihat daftar slot */ },
                    onSlotBooking = { /* TODO: Slot dibooking */ },
                    onHapusSlot = { /* TODO: Hapus slot */ }
                )
                HomeMenu.MAIL    -> MailScreen()
                HomeMenu.HISTORY -> HistoryScreen()
                HomeMenu.CHART   -> ChartScreen()
            }
        }
    }
}


@Preview(
    showBackground = true,      // kotak putih di belakang konten
    showSystemUi  = true,       // status bar, nav bar
    name = "Home – default"
)
@Composable
fun HomeScreenPreview() {
        HomeScreen(userName = "Ihwal Marhamdi")
}
//Ini Contoh Halaman Dummy
@Composable fun MailScreen()    = CenterText("Isi Mail")
@Composable fun HistoryScreen() = CenterText("Isi History")
@Composable fun ChartScreen()   = CenterText("Isi Chart")

@Composable
private fun CenterText(text: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text)
}
