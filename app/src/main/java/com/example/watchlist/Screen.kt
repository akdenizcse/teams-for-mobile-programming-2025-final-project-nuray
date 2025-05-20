// Screen.kt
package com.example.watchlist

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val icon: ImageVector,
    @StringRes val titleRes: Int
) {
    object Home      : Screen("home",      Icons.Filled.Home,         R.string.home_title)
    object Favorites : Screen("favorites", Icons.Filled.Favorite,     R.string.favorites_title)
    object Watchlist : Screen("watchlist", Icons.Filled.PlaylistAdd, R.string.watchlist_title)
    object Profile   : Screen("profile",   Icons.Filled.Person,       R.string.profile_title)
}
