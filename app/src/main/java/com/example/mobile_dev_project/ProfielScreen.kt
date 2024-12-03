import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mobile_dev_project.Toestel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.*
import com.example.mobile_dev_project.cancelRental
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import android.util.Log


data class User(
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val description: String? = null,
    val street: String = "",
    val houseNumber: String = "",
    val postalCode: String = "",
    val city: String = "",
    val country: String = "België"
)

@Composable
fun ProfielScreen(onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var street by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("België") }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject<User>()
                    user?.let {
                        username = it.username
                        email = it.email
                        profilePictureUrl = it.profilePictureUrl ?: ""
                        description = it.description ?: ""
                        street = it.street
                        houseNumber = it.houseNumber
                        postalCode = it.postalCode
                        city = it.city
                        country = it.country
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color(0xFFF7F7F7)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            item {
                CircularProgressIndicator()
            }
        } else {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Welkom, $username",
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF3E3E3E)
                    )
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_edit),
                            contentDescription = "Bewerken",
                            tint = if (isEditing) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                ) {
                    if (profilePictureUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profilePictureUrl,
                            contentDescription = "Profielfoto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profielfoto",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    OutlinedTextField(
                        value = profilePictureUrl,
                        onValueChange = { profilePictureUrl = it },
                        label = { Text("Profielfoto URL") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }

                TextSection("Beschrijving", description, isEditing) { description = it }
                
                AdresSection(
                    street = street,
                    houseNumber = houseNumber,
                    postalCode = postalCode,
                    city = city,
                    country = country,
                    isEditing = isEditing,
                    onStreetChange = { street = it },
                    onHouseNumberChange = { houseNumber = it },
                    onPostalCodeChange = { postalCode = it },
                    onCityChange = { city = it },
                    onCountryChange = { country = it }
                )

                if (isEditing) {
                    Button(
                        onClick = {
                            isSaving = true
                            saveUserData(
                                db = db,
                                userId = userId,
                                username = username,
                                email = email,
                                profilePictureUrl = profilePictureUrl,
                                description = description,
                                street = street,
                                houseNumber = houseNumber,
                                postalCode = postalCode,
                                city = city,
                                country = country
                            ) {
                                isSaving = false
                                isEditing = false
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier.padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Opslaan", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                this@LazyColumn.item {
                    RentedToestellen(userId = userId)
                }
            }
        }
    }
}

@Composable
fun TextSection(label: String, text: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 4.dp)
        )
        if (isEditing) {
            BasicTextField(
                value = text,
                onValueChange = onValueChange,
                textStyle = TextStyle(fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        } else {
            Text(
                text = text,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun AdresSection(
    street: String,
    houseNumber: String,
    postalCode: String,
    city: String,
    country: String,
    isEditing: Boolean,
    onStreetChange: (String) -> Unit,
    onHouseNumberChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onCountryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Adres",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = street,
                    onValueChange = onStreetChange,
                    label = { Text("Straat") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = houseNumber,
                    onValueChange = onHouseNumberChange,
                    label = { Text("Nr") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = onPostalCodeChange,
                    label = { Text("Postcode") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("Gemeente") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = country,
                onValueChange = onCountryChange,
                label = { Text("Land") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("$street $houseNumber")
                    Text("$postalCode $city")
                    Text(country)
                }
            }
        }
    }
}

private fun saveUserData(
    db: FirebaseFirestore,
    userId: String,
    username: String,
    email: String,
    profilePictureUrl: String,
    description: String,
    street: String,
    houseNumber: String,
    postalCode: String,
    city: String,
    country: String,
    onComplete: () -> Unit
) {
    val user = hashMapOf(
        "username" to username,
        "email" to email,
        "profilePictureUrl" to profilePictureUrl,
        "description" to description,
        "street" to street,
        "houseNumber" to houseNumber,
        "postalCode" to postalCode,
        "city" to city,
        "country" to country
    )

    db.collection("users").document(userId).set(user)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { onComplete() }
}

@Composable
fun RentedToestellen(userId: String) {
    var rentedToestellen by remember { mutableStateOf<List<RentedToestel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(userId, refreshTrigger) {
        db.collection("rentals")
            .whereEqualTo("renterId", userId)
            .get()
            .addOnSuccessListener { rentalSnapshot ->
                val rentedToestelsList = mutableListOf<RentedToestel>()
                var completedQueries = 0
                val totalQueries = rentalSnapshot.size()

                if (totalQueries == 0) {
                    rentedToestellen = emptyList()
                    isLoading = false
                    return@addOnSuccessListener
                }

                rentalSnapshot.documents.forEach { rentalDoc ->
                    val toestelId = rentalDoc.getString("toestelId") ?: ""
                    val startDateMap = rentalDoc.get("startDate") as? Map<*, *>
                    val endDateMap = rentalDoc.get("endDate") as? Map<*, *>

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

                    db.collection("toestellen")
                        .document(toestelId)
                        .get()
                        .addOnSuccessListener { toestelDoc ->
                            val toestel = toestelDoc.data?.let {
                                Toestel(
                                    id = toestelDoc.id,
                                    name = it["name"] as? String ?: "",
                                    description = it["description"] as? String ?: "",
                                    price = (it["price"] as? Number)?.toDouble() ?: 0.0,
                                    photoUrl = it["photoUrl"] as? String ?: "",
                                    userId = it["userId"] as? String ?: ""
                                )
                            }

                            toestel?.let {
                                rentedToestelsList.add(
                                    RentedToestel(
                                        rental = Rental(
                                            id = rentalDoc.id,
                                            startDate = startDate,
                                            endDate = endDate
                                        ),
                                        toestel = it
                                    )
                                )
                            }
                            
                            completedQueries++
                            if (completedQueries == totalQueries) {
                                rentedToestellen = rentedToestelsList
                                isLoading = false
                            }
                        }
                }
            }
    }

    Column {
        rentedToestellen.forEach { rentedToestel ->
            RentedToestelCard(
                rentedToestel = rentedToestel,
                onRefresh = { refreshTrigger += 1 }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun RentedToestelCard(
    rentedToestel: RentedToestel,
    onRefresh: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    
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
            if (rentedToestel.toestel.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = rentedToestel.toestel.photoUrl,
                    contentDescription = "Toestel foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.FillWidth
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = rentedToestel.toestel.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            
            Text(
                text = "Gehuurd van ${rentedToestel.rental.startDate.format(dateFormatter)} " +
                      "tot ${rentedToestel.rental.endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4CAF50)
            )
            
            Text(
                text = "€${String.format("%.2f", rentedToestel.toestel.price)} per dag",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { 
                    db.collection("rentals")
                        .document(rentedToestel.rental.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Huur geannuleerd", Toast.LENGTH_SHORT).show()
                            onRefresh()
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Annuleer Huur", color = Color.White)
            }
        }
    }
}

data class Rental(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class RentedToestel(
    val rental: Rental,
    val toestel: Toestel
)

