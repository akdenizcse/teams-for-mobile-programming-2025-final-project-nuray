package com.example.watchlist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightPurple,
    secondary = DeepPurple,
    tertiary = SoftPink,
    background = DeepPurple,
    surface = Color.Black,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = SoftPink,
    onSurface = SoftPink
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPurple,
    secondary = LightPurple,
    tertiary = SoftPink,
    background = SoftPink,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DeepPurple,
    onSurface = DeepPurple
)

@Composable
fun WatchListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}