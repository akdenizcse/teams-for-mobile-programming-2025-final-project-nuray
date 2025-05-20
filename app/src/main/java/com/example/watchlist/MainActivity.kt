package com.example.watchlist

import android.content.Context
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.example.watchlist.ui.theme.WatchListTheme

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val compatLocales: LocaleListCompat = AppCompatDelegate.getApplicationLocales()
        val nativeLocales = LocaleList.forLanguageTags(compatLocales.toLanguageTags())
        val config = newBase.resources.configuration.apply {
            setLocales(nativeLocales)
        }
        val ctx = newBase.createConfigurationContext(config)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(true) }
            var currentLanguageTag by rememberSaveable { mutableStateOf("en") }

            WatchListTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                NavigationGraph(
                    navController          = navController,
                    isDarkMode             = isDarkMode,
                    currentLanguageTag     = currentLanguageTag,
                    onThemeToggle          = { isDarkMode = it },
                    onLanguageSelected     = { langTag ->
                        val locales = LocaleListCompat.forLanguageTags(langTag)
                        AppCompatDelegate.setApplicationLocales(locales)
                        currentLanguageTag = langTag
                    }
                )
            }
        }
    }
}
