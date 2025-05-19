// ui/theme/Theme.kt
package com.example.watchlist.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary        = LightPrimary,
    secondary      = LightSecondary,
    tertiary       = LightTertiary,
    background     = LightBackground,
    surface        = LightSurface,
    onPrimary      = Color.White,
    onSecondary    = Color.White,
    onTertiary     = Color.White,
    onBackground   = LightPrimary,
    onSurface      = LightPrimary
)

private val DarkColors = darkColorScheme(
    primary        = DarkPrimary,
    secondary      = DarkSecondary,
    tertiary       = DarkTertiary,
    background     = DarkBackground,
    surface        = DarkSurface,
    onPrimary      = Color.White,
    onSecondary    = Color.White,
    onTertiary     = Color.White,
    onBackground   = DarkTertiary,
    onSurface      = DarkTertiary
)

@Composable
fun WatchListTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
