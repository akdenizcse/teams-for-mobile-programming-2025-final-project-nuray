package com.example.watchlist

import android.content.Context
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.rememberNavController
import com.example.watchlist.ui.theme.WatchListTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val compatLocales: LocaleListCompat = AppCompatDelegate.getApplicationLocales()
        val nativeLocales = LocaleList.forLanguageTags(compatLocales.toLanguageTags())
        val config = newBase.resources.configuration.apply { setLocales(nativeLocales) }
        val ctx = newBase.createConfigurationContext(config)
        super.attachBaseContext(ctx)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(true) }
            WatchListTheme(darkTheme = isDarkMode) {
                EntryWithSplash(onThemeToggle = { isDarkMode = it })
            }
        }
    }
}

@Composable
private fun EntryWithSplash(onThemeToggle: (Boolean) -> Unit) {
    var showContent by remember { mutableStateOf(false) }
    var logoVisible by remember { mutableStateOf(false) }

    val scaleAnim by animateFloatAsState(
        targetValue = if (logoVisible) 1.2f else 0.8f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
    )
    val alphaAnim by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700)
    )

    LaunchedEffect(Unit) {
        delay(300)
        logoVisible = true
        delay(1000)
        logoVisible = false
        delay(200)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = logoVisible,
            enter = fadeIn(tween(0)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.cinecue),
                    contentDescription = null,
                    modifier = Modifier
                        .size(250.dp)
                        .scale(scaleAnim)
                        .alpha(alphaAnim)
                )
            }
        }

        if (showContent) {
            MainApp(onThemeToggle)
        }
    }
}

@Composable
private fun MainApp(onThemeToggle: (Boolean) -> Unit) {
    var isDarkMode by rememberSaveable { mutableStateOf(true) }
    var currentLanguageTag by rememberSaveable { mutableStateOf("en") }
    val navController = rememberNavController()

    WatchListTheme(darkTheme = isDarkMode) {
        NavigationGraph(
            navController = navController,
            isDarkMode = isDarkMode,
            currentLanguageTag = currentLanguageTag,
            onThemeToggle = {
                isDarkMode = it
                onThemeToggle(it)
            },
            onLanguageSelected = { langTag ->
                val locales = LocaleListCompat.forLanguageTags(langTag)
                AppCompatDelegate.setApplicationLocales(locales)
                currentLanguageTag = langTag
            }
        )
    }
}