package com.example.mobile_dev_project

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import android.location.Geocoder
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val toestellen = remember { mutableStateListOf<Toestel>() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedRadius by remember { mutableStateOf(5) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var userAddress by remember { mutableStateOf("") }
    var toestelAddresses by remember { mutableStateOf(mapOf<String, String>()) }

    val filteredToestellen = remember(searchQuery, selectedCategory, toestellen) {
        toestellen.filter { toestel ->
            val matchesSearch = if (searchQuery.isEmpty()) {
                true
            } else {
                toestel.name.contains(searchQuery, ignoreCase = true) ||
                toestel.description.contains(searchQuery, ignoreCase = true)
            }
            
            val matchesCategory = if (selectedCategory == null) {
                true
            } else {
                toestel.category == selectedCategory
            }
            
            matchesSearch && matchesCategory
        }
    }

    LaunchedEffect(Unit) {
        db.collection("toestellen")
            .get()
            .addOnSuccessListener { snapshot ->
                toestellen.clear()
                for (document in snapshot.documents) {
                    try {
                        val data = document.data
                        if (data != null) {
                            val startDateMap = data["availabilityStart"] as? Map<*, *>
                            val endDateMap = data["availabilityEnd"] as? Map<*, *>

                            val startDate = if (startDateMap != null) {
                                LocalDate.of(
                                    (startDateMap["year"] as Long).toInt(),
                                    (startDateMap["monthValue"] as Long).toInt(),
                                    (startDateMap["dayOfMonth"] as Long).toInt()
                                )
                            } else LocalDate.now()

                            val endDate = if (endDateMap != null) {
                                LocalDate.of(
                                    (endDateMap["year"] as Long).toInt(),
                                    (endDateMap["monthValue"] as Long).toInt(),
                                    (endDateMap["dayOfMonth"] as Long).toInt()
                                )
                            } else LocalDate.now()

                            val toestel = Toestel(
                                id = document.id,
                                name = data["name"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                                priceUnit = data["priceUnit"] as? String ?: "Dag",
                                category = data["category"] as? String ?: "",
                                availabilityStart = LocalDate.parse(data["availabilityStart"] as? String ?: LocalDate.now().toString()),
                                availabilityEnd = LocalDate.parse(data["availabilityEnd"] as? String ?: LocalDate.now().plusDays(1).toString()),
                                photoUrl = data["photoUrl"] as? String ?: "",
                                userId = data["userId"] as? String ?: ""
                            )
                            toestellen.add(toestel)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Error parsing toestel: ${e.message}")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeScreen", "Error loading toestellen", e)
            }
    }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userAddress = document.getString("address") ?: ""
                }
        }

        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val addresses = mutableMapOf<String, String>()
                for (document in snapshot.documents) {
                    val address = document.getString("address")
                    if (address != null) {
                        addresses[document.id] = address
                    }
                }
                toestelAddresses = addresses
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Beschikbare Toestellen",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Zoek toestellen...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Alle") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(Categories.list) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Zoekradius: ${selectedRadius}km",
                color = Color.White
            )
            
            Button(
                onClick = { showRadiusDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Wijzig Radius")
            }
        }

        LazyColumn {
            items(filteredToestellen) { toestel ->
                ToestelCard(
                    toestel = toestel,
                    showActions = false
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Log uit", color = Color.White)
        }
    }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = { Text("Kies Zoekradius") },
            text = {
                Column {
                    Slider(
                        value = selectedRadius.toFloat(),
                        onValueChange = { selectedRadius = it.toInt() },
                        valueRange = 1f..50f,
                        steps = 48,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50)
                        )
                    )
                    Text("${selectedRadius}km")
                }
            },
            confirmButton = {
                TextButton(onClick = { showRadiusDialog = false }) {
                    Text("OK", color = Color(0xFF4CAF50))
                }
            }
        )
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
