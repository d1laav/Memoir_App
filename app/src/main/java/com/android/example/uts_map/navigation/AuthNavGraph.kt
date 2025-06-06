package com.android.example.uts_map.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.example.uts_map.ui.screen.journey.NewEntryScreen
import com.android.example.uts_map.ui.screen.login.LoginScreen
import com.android.example.uts_map.ui.screen.main.MainScreen
import com.android.example.uts_map.ui.screen.register.RegisterScreen
import com.android.example.uts_map.ui.screen.welcome.WelcomeScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AuthNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") },
                onGoogleLoginSucces = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onSignOut = {
                    navController.navigate("welcome") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}