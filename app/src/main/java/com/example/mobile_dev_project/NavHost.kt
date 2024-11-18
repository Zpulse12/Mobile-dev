package com.example.mobile_dev_project

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("login") {
            LoginScreen(
                auth = auth,
                onLoginSuccess = { navController.navigate("home") }
            )
        }
        composable("register") {
            RegisterScreen(
                auth = auth,
                db = FirebaseFirestore.getInstance(),
                onRegisterSuccess = { navController.navigate("welcome") }
            )
        }
        composable("home") {
            MainScreen(
                onLogoutClick = {
                    auth.signOut()
                    navController.navigate("welcome") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToAddToestel = { navController.navigate("addToestel") }
            )
        }
        composable("addToestel") {
            AddToestelScreen(
                onToestelAdded = { navController.popBackStack() }
            )
        }
    }
}