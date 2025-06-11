package com.example.fastpark.screens.components.SettingScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.fastpark.screens.theme.DeepRed
import com.example.fastpark.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userData by authViewModel.userData.observeAsState()
    var displayName by remember { mutableStateOf(userData?.displayName ?: "") }
    val email = userData?.email ?: "Tidak ada email"

    // Update state 'displayName' jika data dari ViewModel berubah
    LaunchedEffect(userData) {
        userData?.let {
            displayName = it.displayName ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Anda") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false, // Tetap gunakan ini untuk menonaktifkan field
                    colors = TextFieldDefaults.colors(
                        // Warna untuk text, label, dan container
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        disabledContainerColor = Color.Transparent,

                        // --- PERUBAHAN UTAMA DI SINI ---
                        // Gunakan 'disabledIndicatorColor' untuk mengatur warna border saat nonaktif
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        // TODO: Panggil fungsi ViewModel untuk menyimpan perubahan
                        // authViewModel.updateDisplayName(displayName)
                        navController.popBackStack() // Kembali setelah menyimpan
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Perubahan")
                }
            }
        }
    )
}