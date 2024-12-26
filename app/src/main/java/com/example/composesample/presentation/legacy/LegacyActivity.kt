package com.example.composesample.presentation.legacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Surface
import androidx.compose.ui.graphics.Color
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.legacy.main.AppbarSample

@ExperimentalAnimationApi
class LegacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                color = Color.White
            ) {
                SetSystemUI()
                AppbarSample(
                    title = "AppBar Main",
                    onCloseEvent = {
                        this.finish()
                    }
                )
            }
        }
    }
}