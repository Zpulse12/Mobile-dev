package com.example.mobile_dev_project

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditToestelScreen(
    toestelId: String,
    onToestelUpdated: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var availabilityStart by remember { mutableStateOf(LocalDate.now()) }
    var availabilityEnd by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var photoUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    LaunchedEffect(toestelId) {
        db.collection("toestellen").document(toestelId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    if (data != null) {
                        name = data["name"] as? String ?: ""
                        description = data["description"] as? String ?: ""
                        price = when (val priceValue = data["price"]) {
                            is String -> priceValue
                            is Number -> priceValue.toString()
                            else -> ""
                        }
                        photoUrl = data["photoUrl"] as? String ?: ""
                        selectedCategory = data["category"] as? String ?: ""

                        // Parse dates
                        val startDateMap = data["availabilityStart"] as? Map<*, *>
                        val endDateMap = data["availabilityEnd"] as? Map<*, *>

                        if (startDateMap != null) {
                            availabilityStart = LocalDate.of(
                                (startDateMap["year"] as Long).toInt(),
                                (startDateMap["monthValue"] as Long).toInt(),
                                (startDateMap["dayOfMonth"] as Long).toInt()
                            )
                        }

                        if (endDateMap != null) {
                            availabilityEnd = LocalDate.of(
                                (endDateMap["year"] as Long).toInt(),
                                (endDateMap["monthValue"] as Long).toInt(),
                                (endDateMap["dayOfMonth"] as Long).toInt()
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditToestelScreen", "Error loading toestel: ${e.message}")
                Toast.makeText(context, "Error loading toestel", Toast.LENGTH_SHORT).show()
            }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun showDatePicker(
        initialDate: LocalDate,
        onDateSelected: (LocalDate) -> Unit
    ) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Toestel Bewerken",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Naam") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF4CAF50)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschrijving") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF4CAF50)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Prijs Informatie",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Prijs per dag") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = availabilityStart.format(dateFormatter),
                onValueChange = { },
                readOnly = true,
                label = { Text("Beschikbaar van", color = Color.Black) },
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showDatePicker(availabilityStart) { date ->
                            availabilityStart = date
                        }
                    },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    disabledContainerColor = Color.Black,
                    cursorColor = Color(0xFF4CAF50),
                    focusedIndicatorColor = Color(0xFF4CAF50),
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color.White,
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = availabilityEnd.format(dateFormatter),
                onValueChange = { },
                readOnly = true,
                label = { Text("Beschikbaar tot", color = Color.Black) },
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showDatePicker(availabilityEnd) { date ->
                            availabilityEnd = date
                        }
                    },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    disabledContainerColor = Color.Black,
                    cursorColor = Color(0xFF4CAF50),
                    focusedIndicatorColor = Color(0xFF4CAF50),
                    unfocusedIndicatorColor = Color.Gray,
                    focusedLabelColor = Color(0xFF4CAF50),
                    unfocusedLabelColor = Color.White,
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Categorie",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

        OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Foto URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF4CAF50)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (name.isNotEmpty() && description.isNotEmpty() && 
                    price.isNotEmpty() && selectedCategory.isNotEmpty()) {
                    if (price.toDoubleOrNull() == null) {
                        Toast.makeText(
                            context,
                            "Prijs moet een geldig nummer zijn",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (availabilityEnd.isBefore(availabilityStart)) {
                        Toast.makeText(
                            context,
                            "Einddatum moet na startdatum liggen",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val startDateMap = mapOf(
                        "year" to availabilityStart.year,
                        "monthValue" to availabilityStart.monthValue,
                        "dayOfMonth" to availabilityStart.dayOfMonth
                    )

                    val endDateMap = mapOf(
                        "year" to availabilityEnd.year,
                        "monthValue" to availabilityEnd.monthValue,
                        "dayOfMonth" to availabilityEnd.dayOfMonth
                    )

                    val updatedToestel = hashMapOf(
                        "name" to name,
                        "description" to description,
                        "price" to price,
                        "category" to selectedCategory,
                        "availabilityStart" to startDateMap,
                        "availabilityEnd" to endDateMap,
                        "photoUrl" to photoUrl,
                        "userId" to userId
                    )

                    updateToestelInFirestore(db, toestelId, updatedToestel) {
                        onToestelUpdated()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Alle velden zijn verplicht",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "Opslaan",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

private fun updateToestelInFirestore(
    db: FirebaseFirestore,
    toestelId: String,
    toestel: HashMap<String, Any>,
    onComplete: () -> Unit
) {
    db.collection("toestellen").document(toestelId)
        .update(toestel)
        .addOnSuccessListener {
            Log.d("EditToestelScreen", "Toestel updated successfully")
            onComplete()
        }
        .addOnFailureListener { e ->
            Log.e("EditToestelScreen", "Error updating toestel: ${e.message}")
        }
} 