package com.example.composesample.presentation.example.component.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composesample.presentation.legacy.base.SetSystemUI

@ExperimentalAnimationApi
class BottomNavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Box(
                        modifier = Modifier
                            .background(color = Color.White)
                            .padding(top = 10.dp, bottom = 10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = {
                                    finish()
                                }
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "")
                            }
                        }
                    }
                },
                bottomBar = {
                    BottomNavigationBar(navController = navController)
                }
            ) { innerPadding ->
                MainContentComponent(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            SetSystemUI()
        }
    }
}

@Composable
fun MainContentComponent(navController: NavHostController, modifier: Modifier) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { NavigationView("home") }
        composable("search") { NavigationView("search") }
        composable("profile") { NavigationView("profile") }
        composable("settings") { NavigationView("settings") }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )
    var selectedItem by remember { mutableStateOf(0) }

    BottomNavigation(
        backgroundColor = Color.Gray,
        elevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.route)
                }
            )
        }
    }
}

@Composable
fun NavigationView(text: String) {
    Column {
        Text(
            text = "Bottom Navigation : $text"
        )
    }
}