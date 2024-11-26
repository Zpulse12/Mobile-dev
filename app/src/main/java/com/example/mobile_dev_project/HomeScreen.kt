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
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun HomeScreen(onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    val toestellen = remember { mutableStateListOf<Toestel>() }

    LaunchedEffect(Unit) {
        db.collection("toestellen")
            .get()
            .addOnSuccessListener { snapshot ->
                toestellen.clear()
                snapshot.documents.forEach { document ->
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
                                priceUnit = data["priceUnit"] as? String ?: "",
                                availabilityStart = startDate,
                                availabilityEnd = endDate,
                                photoUrl = data["photoUrl"] as? String ?: "",
                                userId = data["userId"] as? String ?: ""
                            )
                            toestellen.add(toestel)
                        }
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Error parsing toestel: ${e.message}", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeScreen", "Error loading toestellen", e)
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

        LazyColumn {
            items(toestellen) { toestel ->
                ToestelCard(
                    toestel = toestel,
                    showActions = false
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
