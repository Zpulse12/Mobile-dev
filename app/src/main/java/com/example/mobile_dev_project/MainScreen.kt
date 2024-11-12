package com.example.mobile_dev_project

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(onLogoutClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            BottomNavBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { paddingValues ->
        when (selectedTab) {
            "home" -> HomeScreen(onLogoutClick = onLogoutClick, modifier = Modifier.padding(paddingValues))
            "map" -> MapScreen(modifier = Modifier.padding(paddingValues))
            "devices" -> ToestellenScreen(modifier = Modifier.padding(paddingValues))
            "profile" -> ProfielScreen(modifier = Modifier.padding(paddingValues))
        }
    }
}