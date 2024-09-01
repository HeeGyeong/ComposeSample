package com.example.composesample.presentation.example.component.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings

sealed class BottomNavItem(
    var title: String,
    var icon: androidx.compose.ui.graphics.vector.ImageVector,
    var route: String
) {
    object Home : BottomNavItem("Home", Icons.Filled.Home, "home")

    object Search : BottomNavItem("Search", Icons.Filled.Search, "search")

    object Profile : BottomNavItem("Profile", Icons.Filled.Person, "profile")

    object Settings : BottomNavItem("Settings", Icons.Filled.Settings, "settings")
}