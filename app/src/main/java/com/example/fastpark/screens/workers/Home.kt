package com.example.fastpark.screens.workers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fastpark.R
import com.example.fastpark.screens.components.BannerSection
import com.example.fastpark.screens.components.HomeMenu
import com.example.fastpark.screens.components.MenuGrid
import com.example.fastpark.screens.theme.BrightRed
import com.example.fastpark.screens.theme.DeepRed



@Composable
fun HomeScreen(
    userName: String = "User"
) {
    var selectedMenu by remember { mutableStateOf(HomeMenu.PARKING) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.user),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(48.dp)
                        .border(1.dp, Color.White, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Hi, Selamat datang", color = Color.White, fontSize = 14.sp)
                    Text(userName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { /* TODO: search query */ },
                placeholder = {
                    Text(
                        "Cari sesuatu…",
                        style = TextStyle(textAlign = TextAlign.Start)
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, RoundedCornerShape(20.dp)),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                textStyle = TextStyle(
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )
            )

        }

        Spacer(Modifier.height(24.dp))
        BannerSection()

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

@Preview(
    showBackground = true,      // kotak putih di belakang konten
    showSystemUi  = true,       // status bar, nav bar
    name = "Home – default"
)
@Composable
fun HomeScreenPreview() {
    HomeScreen(userName = "Ihwal Marhamdi")
}

@Composable fun ParkingScreen() = CenterText("Isi Parking")
@Composable fun MailScreen()    = CenterText("Isi Mail")
@Composable fun HistoryScreen() = CenterText("Isi History")
@Composable fun ChartScreen()   = CenterText("Isi Chart")

@Composable
private fun CenterText(text: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text)
}