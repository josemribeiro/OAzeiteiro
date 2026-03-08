package com.example.projeto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projeto.ui.screens.LoginScreen
import com.example.projeto.ui.screens.MainScreen
import com.example.projeto.ui.screens.MyProfileScreen
import com.example.projeto.ui.screens.RestaurantScreen
import com.example.projeto.ui.screens.SettingsScreen
import com.example.projeto.ui.screens.WelcomeScreen
import com.example.projeto.ui.theme.buildTheme
import com.example.projeto.viewmodel.AuthStatusVM

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            buildTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacaoAppFun()
                }
            }
        }
    }
}

@Composable
fun NavegacaoAppFun() {
    val navController = rememberNavController()
    val authStatusVM: AuthStatusVM = viewModel()
    val estadoUI by authStatusVM.estadoUI.collectAsState()

    // Reativar o LaunchedEffect para navegação automática após login
    LaunchedEffect(estadoUI.isLoggedIn) {
        if (estadoUI.isLoggedIn) {
            navController.navigate("main") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (estadoUI.isLoggedIn) "main" else "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("login") },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onContinueClick = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") },
                onGoogleLogin = {},
                authStatusVM = authStatusVM,
                isRegisterMode = false
            )
        }

        composable("register") {
            LoginScreen(
                onContinueClick = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("login") },
                onGoogleLogin = { },
                authStatusVM = authStatusVM,
                isRegisterMode = true
            )
        }

        composable("main") {
            MainScreen(
                onRestaurantClick = { restaurantId ->
                    navController.navigate("restaurant/$restaurantId")
                },
                onRecommendationClick = { navController.navigate("recommendation") },
                onMyProfileClick = { navController.navigate("profile") }
            )
        }

        composable("restaurant/{restaurantId}") { navBackStackEntry ->
            val restaurantId = navBackStackEntry.arguments?.getString("restaurantId") ?: ""
            RestaurantScreen(
                restaurantId = restaurantId,
                onMainClick = { navController.navigate("main") },
                onProfileClick = { navController.navigate("profile") }
            )
        }

        composable("profile") {
            MyProfileScreen(
                onMainClick = { navController.navigate("main") },
                onSettingsClick = { navController.navigate("settings") },
                onEditProfileClick = {}
            )
        }

        composable("settings") {
            SettingsScreen(
                onMainClick = { navController.navigate("main") },
                onProfileClick = { navController.navigate("profile") },
                onContentSettingsClick = {},
                onPrivacyClick = {},
                onPasswordClick = { navController.navigate("change_password") },
                onLogoutClick = {
                    authStatusVM.logOut()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccountClick = {
                    authStatusVM.deleteAccount(
                        onSuccess = {
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onError = { error ->
                            println("Erro ao apagar conta: $error")
                            navController.navigate("welcome") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            )
        }
    }
}
