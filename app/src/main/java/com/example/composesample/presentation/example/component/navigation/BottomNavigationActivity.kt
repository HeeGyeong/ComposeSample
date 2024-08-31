package com.example.composesample.presentation.example.component.navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import com.example.composesample.presentation.legacy.base.SetSystemUI

@ExperimentalAnimationApi
class BottomNavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            Column {
                Text(
                    text = "Bottom Navigation"
                )
            }
        }
    }
}