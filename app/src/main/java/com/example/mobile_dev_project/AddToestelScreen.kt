package com.example.mobile_dev_project

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
    var availabilityFrom by remember { mutableStateOf("") }
    var availabilityTo by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

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

        TextField(
            value = availabilityFrom,
            onValueChange = { availabilityFrom = it },
            label = { Text("Beschikbaar van", color = Color.White) },
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
            value = availabilityTo,
            onValueChange = { availabilityTo = it },
            label = { Text("Beschikbaar tot", color = Color.White) },
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
                    val toestel = Toestel(
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        priceUnit = priceUnit,
                        availabilityFrom = availabilityFrom,
                        availabilityTo = availabilityTo,
                        photoUrl = photoUrl,
                        userId = userId
                    )
                    addToestelToFirestore(db, toestel) {
                        onToestelAdded()
                    }
                } else {
                    Log.e("AddToestelScreen", "All fields are required.")
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

