package com.example.composesample.ui.base

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
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

    BottomAppBar(
        cutoutShape = MaterialTheme.shapes.small.copy(
            CornerSize(percent = 50)
        ),
        backgroundColor = Color.DarkGray,
        content = {
            BottomNavigation() {
                BottomNavigationItem(
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

                BottomNavigationItem(
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

                BottomNavigationItem(
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

                BottomNavigationItem(
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