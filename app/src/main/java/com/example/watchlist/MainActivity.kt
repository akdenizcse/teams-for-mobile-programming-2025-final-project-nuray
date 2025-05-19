// MainActivity.kt
package com.example.watchlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.example.watchlist.ui.theme.WatchListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(true) }
            var currentLanguage by rememberSaveable { mutableStateOf("English") }
            WatchListTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                NavigationGraph(
                    navController = navController,
                    isDarkMode = isDarkMode,
                    currentLanguage = currentLanguage,
                    onThemeToggle = { isDarkMode = it },
                    onLanguageSelected = { currentLanguage = it }
                )
            }
        }
    }
}
