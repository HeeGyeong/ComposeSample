package com.example.composesample.presentation.legacy.cal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.presentation.legacy.base.BottomBar
import com.example.composesample.presentation.legacy.base.DrawerItem
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.legacy.base.TopBar

@ExperimentalAnimationApi
class CalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()
            val viewModel = viewModel<CalViewModel>()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopBar("CalActivity", scaffoldState, scope)
                },
                bottomBar = {
                    BottomBar()
                },
                content = {
                    AddCounter(viewModel)
                },
                drawerContent = {
                    DrawerItem(scaffoldState, scope)
                },
                drawerGesturesEnabled = false
            )
        }
    }
}