// NavigationGraph.kt
package com.example.watchlist

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    navController: NavHostController,
    isDarkMode: Boolean,
    currentLanguage: String,
    onThemeToggle: (Boolean) -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } }, onSignUpClick = { navController.navigate("signup") }) }
        composable("signup") { SignUpScreen(onBackClick = { navController.popBackStack() }, onSignUpSuccess = { navController.navigate("home") { popUpTo("signup") { inclusive = true } } }, onLoginClick = { navController.navigate("login") { popUpTo("signup") { inclusive = true } } }) }
        composable("home") { HomeScreen(navController) }
        composable("favorites") { FavoritesScreen(navController) }
        composable("watchlist") { WatchlistScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                isDarkMode = isDarkMode,
                currentLanguage = currentLanguage,
                onThemeToggle = onThemeToggle,
                onLanguageSelected = onLanguageSelected
            )
        }
    }
}
