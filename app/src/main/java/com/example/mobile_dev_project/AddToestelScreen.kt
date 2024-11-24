package com.example.mobile_dev_project

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToestelScreen(onToestelAdded: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    // State variables for form inputs
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priceUnit by remember { mutableStateOf("Dag") }
    var availabilityStart by remember { mutableStateOf(LocalDate.now()) }
    var availabilityEnd by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var photoUrl by remember { mutableStateOf("") }

    val context = LocalContext.current
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
            text = "Toestel Toevoegen",
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
                    
                    val toestel = Toestel(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        priceUnit = priceUnit,
                        availabilityStart = availabilityStart,
                        availabilityEnd = availabilityEnd,
                        photoUrl = photoUrl,
                        userId = userId
                    )
                    addToestelToFirestore(db, toestel) {
                        onToestelAdded()
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
            Text("Toevoegen", color = Color.White)
        }
    }
}

// Firestore saving logic
private fun addToestelToFirestore(db: FirebaseFirestore, toestel: Toestel, onComplete: () -> Unit) {
    db.collection("toestellen").add(toestel)
        .addOnSuccessListener {
            Log.d("AddToestelToFirestore", "Toestel added to Firestore: $toestel")
            onComplete()
        }
        .addOnFailureListener { e ->
            Log.e("AddToestelToFirestore", "Error adding toestel: ${e.message}")
        }
}

