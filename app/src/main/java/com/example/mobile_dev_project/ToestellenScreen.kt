package com.example.mobile_dev_project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ToestellenScreen(
    onNavigateToAddToestel: () -> Unit,
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
                    snapshot.documents.forEach { document ->
                        document.toObject(Toestel::class.java)?.let { toestellen.add(it) }
                    }
                }
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
                ToestelCard(toestel)
            }
        }
    }
}

@Composable
fun ToestelCard(toestel: Toestel) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                text = "Beschikbaar van ${toestel.availabilityFrom} tot ${toestel.availabilityTo}",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

data class Toestel(
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val priceUnit: String = "",
    val availabilityFrom: String = "",
    val availabilityTo: String = "",
    val photoUrl: String = "",
    val userId: String = "" // Added to link toestel with user
)