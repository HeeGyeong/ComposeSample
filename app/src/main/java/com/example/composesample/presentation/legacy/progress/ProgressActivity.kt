package com.example.composesample.presentation.legacy.progress

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.composesample.presentation.legacy.base.BottomBar
import com.example.composesample.presentation.legacy.base.DrawerItem
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.legacy.base.TopBar

@ExperimentalAnimationApi
class ProgressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopBar("ProgressActivity", scaffoldState, scope)
                },
                bottomBar = {
                    BottomBar()
                },
                content = {
                    Progress()
                },
                drawerContent = {
                    DrawerItem(scaffoldState, scope)
                },
                drawerGesturesEnabled = false
            )
        }
    }
}