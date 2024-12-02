package com.example.mobile_dev_project

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ToestellenScreen(
    onNavigateToAddToestel: () -> Unit,
    onNavigateToEditToestel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    val toestellen = remember { mutableStateListOf<Toestel>() }

    // Load toestellen from Firestore
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("toestellen")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    toestellen.clear()
                    snapshot.documents.forEach { document ->
                        try {
                            val data = document.data
                            if (data != null) {
                                // Get the date maps from Firestore
                                val startDateMap = data["availabilityStart"] as? Map<*, *>
                                val endDateMap = data["availabilityEnd"] as? Map<*, *>

                                // Parse the dates
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
                                    availabilityStart = startDate,
                                    availabilityEnd = endDate,
                                    photoUrl = data["photoUrl"] as? String ?: "",
                                    userId = data["userId"] as? String ?: ""
                                )
                                toestellen.add(toestel)
                            }
                        } catch (e: Exception) {
                            Log.e("ToestellenScreen", "Error parsing toestel: ${e.message}", e)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ToestellenScreen", "Error loading toestellen", e)
                }
        }
    }

    fun deleteToestel(toestel: Toestel) {
        db.collection("toestellen")
            .whereEqualTo("name", toestel.name) // Assuming 'name' is unique; adjust if necessary
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    db.collection("toestellen").document(document.id).delete()
                }
                toestellen.remove(toestel)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { onNavigateToAddToestel() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("+ Toestel toevoegen", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(toestellen) { toestel ->
                ToestelCard(
                    toestel = toestel,
                    onDelete = { deleteToestel(it) },
                    onEdit = { onNavigateToEditToestel(it.id) }
                )
            }
        }
    }
}

@Composable
fun ToestelCard(
    toestel: Toestel,
    showActions: Boolean = true,
    onDelete: (Toestel) -> Unit = {},
    onEdit: (Toestel) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.foundation.Image(
                    painter = coil.compose.rememberAsyncImagePainter(toestel.photoUrl),
                    contentDescription = "Toestel Foto",
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = toestel.name,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${toestel.price} € per ${toestel.priceUnit}",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Categorie: ${toestel.category}",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (showActions) {
                    Row {
                        IconButton(
                            onClick = { onEdit(toestel) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Toestel",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        IconButton(
                            onClick = { onDelete(toestel) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Toestel",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Beschrijving:",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = toestel.description,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            Text(
                text = "Beschikbaar van ${toestel.availabilityStart.format(dateFormatter)} tot ${toestel.availabilityEnd.format(dateFormatter)}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


data class Toestel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val priceUnit: String = "Dag",
    val category: String = "",
    val availabilityStart: LocalDate = LocalDate.now(),
    val availabilityEnd: LocalDate = LocalDate.now(),
    val photoUrl: String = "",
    val userId: String = ""
)

object Categories {
    val list = listOf(
        "Keukenapparatuur",
        "Tuingereedschap",
        "Schoonmaakapparatuur",
        "Gereedschap",
        "Electronica",
        "Sport & Spel",
        "Feest & Events",
        "Overige"
    )
}

object PriceUnits {
    val list = listOf(
        "Uur",
        "Dag",
        "Week",
        "Maand"
    )
}