package com.example.fastpark.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.fastpark.screens.components.BottomBar
import com.example.fastpark.screens.components.MainPage

@Composable
fun MainScreen() {
    var selectedPage by remember { mutableStateOf(MainPage.HOME) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (selectedPage) {
                    MainPage.HOME     -> HomeScreen()
                    MainPage.SCAN     -> ScanScreen()
                    MainPage.SETTINGS -> SettingsScreen()
                }
            }


            BottomBar(selectedPage = selectedPage) { selectedPage = it }
        }
    }
}
