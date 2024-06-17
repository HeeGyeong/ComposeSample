package com.example.composesample.presentation.legacy.sub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.*
import com.example.composesample.presentation.legacy.base.BottomBar
import com.example.composesample.presentation.legacy.base.DrawerItem
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.legacy.base.TopBar

@ExperimentalAnimationApi
class SubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopBar("SubActivity", scaffoldState, scope)
                },
                bottomBar = {
                    BottomBar()
                },
                content = {
                    MainContent()
                },
                drawerContent = {
                    DrawerItem(scaffoldState, scope)
                },
                drawerGesturesEnabled = false
            )
        }
    }
}