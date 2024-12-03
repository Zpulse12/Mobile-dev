package com.example.mobile_dev_project

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign

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
                                val startDateMap = data["availabilityStart"] as? Map<*, *>
                                val endDateMap = data["availabilityEnd"] as? Map<*, *>

                                val startDate = if (startDateMap != null) {
                                    LocalDate.of(
                                        (startDateMap["year"] as Long).toInt(),
                                        (startDateMap["month"] as Long).toInt(),
                                        (startDateMap["day"] as Long).toInt()
                                    )
                                } else LocalDate.now()

                                val endDate = if (endDateMap != null) {
                                    LocalDate.of(
                                        (endDateMap["year"] as Long).toInt(),
                                        (endDateMap["month"] as Long).toInt(),
                                        (endDateMap["day"] as Long).toInt()
                                    )
                                } else LocalDate.now()

                                val toestel = Toestel(
                                    id = document.id,
                                    name = data["name"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    price = (data["price"] as? Number)?.toDouble() ?: 0.0,
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
            .whereEqualTo("name", toestel.name)
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
                    isHomeScreen = false,
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
    isHomeScreen: Boolean = false,
    onDelete: (Toestel) -> Unit = {},
    onEdit: (Toestel) -> Unit = {},
    onRent: (Toestel) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (toestel.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = toestel.photoUrl,
                    contentDescription = "Toestel foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.FillWidth
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = toestel.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = toestel.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "€${String.format("%.2f", toestel.price)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )

            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            Text(
                text = "Beschikbaar van ${toestel.availabilityStart.format(dateFormatter)} tot ${toestel.availabilityEnd.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )

            if (isHomeScreen) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { onRent(toestel) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Huur dit toestel", color = Color.White)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onEdit(toestel) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Bewerk",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    IconButton(
                        onClick = { onDelete(toestel) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Verwijder",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RentDialog(
    toestel: Toestel,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var rentedDates by remember { mutableStateOf<List<Pair<LocalDate, LocalDate>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val db = FirebaseFirestore.getInstance()

    // Fetch existing rentals
    LaunchedEffect(toestel.id) {
        db.collection("rentals")
            .whereEqualTo("toestelId", toestel.id)
            .get()
            .addOnSuccessListener { documents ->
                rentedDates = documents.mapNotNull { doc ->
                    try {
                        val startDateMap = doc.get("startDate") as? Map<*, *>
                        val endDateMap = doc.get("endDate") as? Map<*, *>
                        
                        if (startDateMap != null && endDateMap != null) {
                            val start = LocalDate.of(
                                (startDateMap["year"] as Long).toInt(),
                                (startDateMap["month"] as Long).toInt(),
                                (startDateMap["day"] as Long).toInt()
                            )
                            val end = LocalDate.of(
                                (endDateMap["year"] as Long).toInt(),
                                (endDateMap["month"] as Long).toInt(),
                                (endDateMap["day"] as Long).toInt()
                            )
                            Pair(start, end)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        title = { 
            Text(
                "Huur ${toestel.name}",
                color = Color.White
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Selected Range Display
                if (startDate != null || endDate != null) {
                    Text(
                        text = "Geselecteerde periode:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = when {
                            startDate != null && endDate != null -> "${startDate!!.format(DateTimeFormatter.ofPattern("d MMM"))} - ${endDate!!.format(DateTimeFormatter.ofPattern("d MMM"))}"
                            startDate != null -> "${startDate!!.format(DateTimeFormatter.ofPattern("d MMM"))} - ..."
                            else -> "... - ${endDate!!.format(DateTimeFormatter.ofPattern("d MMM"))}"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calendar Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Z", "M", "D", "W", "D", "V", "Z").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                // Calendar Grid
                val currentMonth = if (startDate != null) startDate!!.month else LocalDate.now().month
                val daysInMonth = currentMonth.length(false)
                val firstDayOfMonth = LocalDate.of(LocalDate.now().year, currentMonth, 1)
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

                // Function to check if a date is available
                fun isDateAvailable(date: LocalDate): Boolean {
                    // Check if date is within toestel's availability period
                    if (date.isBefore(toestel.availabilityStart) || date.isAfter(toestel.availabilityEnd)) {
                        return false
                    }
                    
                    // Check if date is not already rented
                    return !rentedDates.any { (start, end) -> 
                        date in start..end 
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(300.dp)
                ) {
                    // Empty spaces for first week
                    items(firstDayOfWeek) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }

                    // Days of the month
                    items(daysInMonth) { day ->
                        val date = LocalDate.of(LocalDate.now().year, currentMonth, day + 1)
                        val isAvailable = isDateAvailable(date)
                        val isSelected = when {
                            startDate == null && endDate == null -> false
                            endDate == null -> date == startDate
                            else -> date in (startDate!!..endDate!!)
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    when {
                                        isSelected -> Color(0xFF4CAF50)
                                        !isAvailable -> Color.DarkGray
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                                .clickable(enabled = isAvailable) {
                                    when {
                                        startDate == null -> startDate = date
                                        endDate == null && date > startDate -> {
                                            // Check if all dates in the range are available
                                            val allDatesInRange = startDate!!.datesUntil(date.plusDays(1)).toList()
                                            if (allDatesInRange.all { isDateAvailable(it) }) {
                                                endDate = date
                                            } else {
                                                // Show error or handle invalid range
                                                errorMessage = "Niet alle dagen in deze periode zijn beschikbaar"
                                            }
                                        }
                                        else -> {
                                            startDate = date
                                            endDate = null
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (day + 1).toString(),
                                color = when {
                                    isSelected -> Color.Black
                                    !isAvailable -> Color.Gray
                                    else -> Color.White
                                }
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (startDate != null && endDate != null) {
                        onConfirm(startDate!!, endDate!!)
                    } else {
                        errorMessage = "Selecteer een start- en einddatum"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.Black
                )
            ) {
                Text("Bevestig")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Annuleer")
            }
        }
    )
}

data class Toestel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
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