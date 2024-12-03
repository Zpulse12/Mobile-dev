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
import androidx.compose.material.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

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
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                ) {
                    Text("Log uit", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))
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
                elevation = 2.dp,
                backgroundColor = Color(0xFFF0F0F0)
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
