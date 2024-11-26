package com.example.mobile_dev_project

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    var priceUnit by remember { mutableStateOf("Dag") }
    var availabilityStart by remember { mutableStateOf(LocalDate.now()) }
    var availabilityEnd by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var photoUrl by remember { mutableStateOf("") }
    
    LaunchedEffect(toestelId) {
        db.collection("toestellen").document(toestelId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    if (data != null) {
                        name = data["name"] as? String ?: ""
                        description = data["description"] as? String ?: ""
                        price = (data["price"] as? Number)?.toString() ?: ""
                        priceUnit = data["priceUnit"] as? String ?: "Dag"
                        photoUrl = data["photoUrl"] as? String ?: ""

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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 16.dp)
        )

Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Toestel Naam", color = Color.White) },
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
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Beschrijving", color = Color.White) },
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
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Prijs (€)", color = Color.White) },
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
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(selected = priceUnit == "Uur", onClick = { priceUnit = "Uur" })
            Text("Uur", color = Color(0xFF4CAF50))
            RadioButton(selected = priceUnit == "Dag", onClick = { priceUnit = "Dag" })
            Text("Dag", color = Color(0xFF4CAF50))
            RadioButton(selected = priceUnit == "Week", onClick = { priceUnit = "Week" })
            Text("Week", color = Color(0xFF4CAF50))
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

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Foto URL", color = Color.White) },
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
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && description.isNotEmpty() && price.isNotEmpty()) {
                    if (availabilityEnd.isBefore(availabilityStart)) {
                        Toast.makeText(
                            context,
                            "Einddatum moet na startdatum liggen",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val startDateMap = mapOf(
                        "year" to availabilityStart.year.toLong(),
                        "monthValue" to availabilityStart.monthValue.toLong(),
                        "dayOfMonth" to availabilityStart.dayOfMonth.toLong()
                    )

                    val endDateMap = mapOf(
                        "year" to availabilityEnd.year.toLong(),
                        "monthValue" to availabilityEnd.monthValue.toLong(),
                        "dayOfMonth" to availabilityEnd.dayOfMonth.toLong()
                    )

                    val updatedToestel = hashMapOf<String, Any>(
                        "name" to name,
                        "description" to description,
                        "price" to (price.toDoubleOrNull() ?: 0.0),
                        "priceUnit" to priceUnit,
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Opslaan", color = Color.White)
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