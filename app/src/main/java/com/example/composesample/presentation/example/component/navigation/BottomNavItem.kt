package com.example.composesample.presentation.example.component.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings

/**
 * Navigation Object
 *
 * 해당 Class에 선언한 경우에만 Navigation 가능
 */
sealed class BottomNavItem(
    var title: String,
    var icon: androidx.compose.ui.graphics.vector.ImageVector,
    var route: String
) {
    data object Home :
        BottomNavItem(NavigationType.HOME, Icons.Filled.Home, NavigationType.HOME)

    data object Search :
        BottomNavItem(NavigationType.SEARCH, Icons.Filled.Search, NavigationType.SEARCH)

    data object Profile :
        BottomNavItem(NavigationType.PROFILE, Icons.Filled.Person, NavigationType.PROFILE)

    data object Settings :
        BottomNavItem(NavigationType.SETTINGS, Icons.Filled.Settings, NavigationType.SETTINGS)
}

class NavigationType {
    companion object {
        const val HOME = "home"
        const val SEARCH = "search"
        const val PROFILE = "profile"
        const val SETTINGS = "settings"
    }
}