package com.example.composesample.presentation.legacy.base

import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
fun BottomBar() {
    val selectedItem = remember { mutableStateOf("") }

    // M3 BottomAppBar 에는 cutoutShape(FAB docking)이 없다.
    BottomAppBar(
        containerColor = Color.DarkGray,
        content = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Filled.Favorite, "")
                    },
                    label = { Text(text = "Favorite") },
                    selected = selectedItem.value == "favorite",
                    onClick = {
                        selectedItem.value = "favorite"
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    icon = {
                        Icon(Icons.Filled.Home, "")
                    },
                    label = { Text(text = "Home") },
                    selected = selectedItem.value == "Home",
                    onClick = {
                        selectedItem.value = "Home"
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    icon = {
                        Icon(Icons.Filled.Menu, "")
                    },


                    label = { Text(text = "Menu") },
                    selected = selectedItem.value == "Menu",
                    onClick = {
                        selectedItem.value = "Menu"
                    },
                    alwaysShowLabel = false
                )

                NavigationBarItem(
                    icon = {
                        Icon(Icons.Filled.MoreVert, "")
                    },
                    label = { Text(text = "MoreVert") },
                    selected = selectedItem.value == "MoreVert",
                    onClick = {
                        selectedItem.value = "MoreVert"
                    },
                    alwaysShowLabel = false
                )
            }
        }
    )
}