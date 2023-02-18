package com.example.composesample.hash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.composesample.ui.base.BottomBar
import com.example.composesample.ui.base.DrawerItem
import com.example.composesample.ui.base.SetSystemUI
import com.example.composesample.ui.base.TopBar

@ExperimentalAnimationApi
class HashTagActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopBar("HashTag", scaffoldState, scope)
                },
                bottomBar = {
                    BottomBar()
                },
                content = {
                    HashTagUI()
                },
                drawerContent = {
                    DrawerItem(scaffoldState, scope)
                },
                drawerGesturesEnabled = false
            )
        }
    }
}