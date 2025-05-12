package com.example.watchlist

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {

                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = {

                    navController.popBackStack("login", inclusive = false)
                },
                onLoginClick = {

                    navController.popBackStack()
                }
            )
        }
        composable("home") {
            HomeScreen(navController)
        }
    }
}

