package com.example.fastpark.screens.users

// Import yang diperlukan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.fastpark.screens.components.HomeUserMenu
import com.example.fastpark.screens.components.MenuUser
import com.example.fastpark.viewmodel.AuthViewModel


@Composable fun ParkingUserScreen() = CenterTextUser("Isi Parking User Screen")
@Composable fun MailUserScreen()    = CenterTextUser("Isi Mail User Screen")
@Composable fun HistoryUserScreen() = CenterTextUser("Isi History User Screen")

@Composable
private fun CenterTextUser(text: String) = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text)
}


@Composable
fun MainUser(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var selectedPage by remember { mutableStateOf(HomeUserMenu.HOMEUSER) } // Default ke PARKING

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)
            ) {
                when (selectedPage) {
                    HomeUserMenu.HOMEUSER -> HomeUserScreen()
                    HomeUserMenu.PARKING -> ParkingUserScreen()
                    HomeUserMenu.MAIL -> MailUserScreen()
                    HomeUserMenu.HISTORY -> HistoryUserScreen()
                }
            }
            MenuUser(selectedMenu = selectedPage) { newSelectedMenu ->
                selectedPage = newSelectedMenu
            }
        }
    }
}