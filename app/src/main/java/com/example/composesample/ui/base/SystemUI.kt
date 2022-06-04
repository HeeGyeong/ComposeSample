package com.example.composesample.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SetSystemUI() {
    // Gradle .. implementation "com.google.accompanist:accompanist-systemuicontroller:0.17.0"
    val systemUiController = rememberSystemUiController()

    // Top + Bottom System UI
//    systemUiController.setSystemBarsColor(Color.Blue)
    // Top System UI
    systemUiController.setStatusBarColor(Color.Black)
    // Bottom System UI
    systemUiController.setNavigationBarColor(Color.Yellow)
}