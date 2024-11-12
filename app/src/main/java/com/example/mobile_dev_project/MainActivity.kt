package com.example.mobile_dev_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            var currentScreen by remember { mutableStateOf("welcome") }

            when (currentScreen) {
                "welcome" -> WelcomeScreen(
                    onLoginClick = { currentScreen = "login" },
                    onRegisterClick = { currentScreen = "register" }
                )
                "login" -> LoginScreen(
                    auth = auth,
                    onLoginSuccess = { currentScreen = "home" }
                )
                "register" -> RegisterScreen(
                    auth = auth,
                    db = db,
                    onRegisterSuccess = { currentScreen = "welcome" }
                )
                "home" -> MainScreen(
                    onLogoutClick = {
                        auth.signOut()
                        currentScreen = "welcome"
                    }
                )
            }
        }
    }
}
