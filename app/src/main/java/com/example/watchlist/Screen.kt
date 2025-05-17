package com.example.watchlist

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Home      : Screen("home",      Icons.Filled.Home,         "Home")
    object Favorites : Screen("favorites", Icons.Filled.Favorite,     "Favorites")
    object Watchlist : Screen("watchlist", Icons.Filled.PlaylistAdd, "Watchlist")
    object Profile   : Screen("profile",   Icons.Filled.Person,       "Profile")
}
