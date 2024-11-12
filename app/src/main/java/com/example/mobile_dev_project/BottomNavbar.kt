package com.example.mobile_dev_project

import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    BottomNavigation(
        backgroundColor = Color.Black,
        contentColor = Color.White,
        modifier = Modifier.height(72.dp)
    ) {
        BottomNavigationItem(
            icon = {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = if (selectedTab == "home") Color(0xFF4CAF50) else Color.White)
            },
            label = { Text("Home") },
            selected = selectedTab == "home",
            onClick = { onTabSelected("home") }
        )
        BottomNavigationItem(
            icon = {
                Icon(Icons.Filled.LocationOn, contentDescription = "Map", tint = if (selectedTab == "map") Color(0xFF4CAF50) else Color.White)
            },
            label = { Text("Map") },
            selected = selectedTab == "map",
            onClick = { onTabSelected("map") }
        )
        BottomNavigationItem(
            icon = {
                Icon(Icons.Filled.Build, contentDescription = "Devices", tint = if (selectedTab == "devices") Color(0xFF4CAF50) else Color.White)
            },
            label = { Text("Toestellen") },
            selected = selectedTab == "devices",
            onClick = { onTabSelected("devices") }
        )
        BottomNavigationItem(
            icon = {
                Icon(Icons.Filled.Person, contentDescription = "Profile", tint = if (selectedTab == "profile") Color(0xFF4CAF50) else Color.White)
            },
            label = { Text("Profiel") },
            selected = selectedTab == "profile",
            onClick = { onTabSelected("profile") }
        )
    }
}
