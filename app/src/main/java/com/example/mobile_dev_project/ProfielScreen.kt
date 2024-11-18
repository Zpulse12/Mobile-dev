import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

data class User(
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val address: String? = null,
    val description: String? = null
)

@Composable
fun ProfielScreen(modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

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
                        address = it.address ?: ""
                        description = it.description ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color(0xFFF7F7F7)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
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

            Card(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                shape = CircleShape,
                elevation = 8.dp
            ) {
                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (isEditing) {
                TextField(
                    value = profilePictureUrl,
                    onValueChange = { profilePictureUrl = it },
                    label = { Text("Profielfoto URL") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White
                    )
                )
            }

            TextSection("Beschrijving", description, isEditing) { description = it }
            TextSection("Adres", address, isEditing) { address = it }

            if (!isEditing) {
                ApparatuurVerhuurSection()
            }

            if (isEditing) {
                Button(
                    onClick = {
                        isSaving = true
                        saveUserData(db, userId, username, email, profilePictureUrl, address, description) {
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
fun ApparatuurVerhuurSection() {
    val verhuurItems = listOf(
        "Canon EOS R5 Camera",
        "DJI Phantom 4 Drone",
        "MacBook Pro 16\"",
        "Sony WH-1000XM4 Headphones",
        "GoPro Hero 9 Black",
        "Canon EF 50mm f/1.8 Lens"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Text(
            text = "Beschikbare Apparatuur",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        verhuurItems.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_camera),
                        contentDescription = "Apparaat icoon",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    )
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
    address: String,
    description: String,
    onComplete: () -> Unit
) {
    val user = hashMapOf(
        "username" to username,
        "email" to email,
        "profilePictureUrl" to profilePictureUrl,
        "address" to address,
        "description" to description
    )

    db.collection("users").document(userId).set(user)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { onComplete() }
}
