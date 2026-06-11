package com.example.composesample.presentation.legacy.scope.sub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.composesample.presentation.legacy.scope.CoroutineScreen
import com.example.composesample.presentation.legacy.scope.CoroutineTempScreen
import com.example.composesample.presentation.legacy.base.BottomBar
import com.example.composesample.presentation.legacy.base.DrawerItem
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.legacy.base.TopBar

@ExperimentalAnimationApi
class RememberCoroutineActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            var changeUI by remember { mutableStateOf(1) }

            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = false,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerItem(drawerState, scope)
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopBar("ProgressActivity", drawerState, scope)
                    },
                    bottomBar = {
                        BottomBar()
                    },
                    content = {
                        when (changeUI) {
                            1 -> {
                                // scope를 넣지 않으면 FirstScreen 생성 시 rememberCoroutineScope()를 호출함.
                                CoroutineScreen(snackbarHostState,
                                    changeState = {
                                        changeUI = 2
                                    }
                                )
                            }
                            2 -> {
                                // lifecycleScope은 Activity의 생명주기를 따름.
                                CoroutineScreen(snackbarHostState,
                                    changeState = {
                                        changeUI = 3
                                    },
                                    lifecycleScope
                                )
                            }
                            else -> {
                                CoroutineTempScreen()
                            }
                        }
                    }
                )
            }
        }
    }
}