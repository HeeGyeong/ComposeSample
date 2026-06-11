package com.example.composesample.presentation.legacy.movie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.composesample.presentation.legacy.base.BottomBar
import com.example.composesample.presentation.legacy.base.DrawerItem
import com.example.composesample.presentation.legacy.base.SetSystemUI
import com.example.composesample.presentation.legacy.base.TopBar
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalAnimationApi
class MovieActivity : ComponentActivity() {

    private val viewModel: MovieViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            // Runtime Error.
//            val viewModel = viewModel<MovieViewModel>()

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
                    topBar = {
                        TopBar("Movie Activity", drawerState, scope)
                    },
                    bottomBar = {
                        BottomBar()
                    },
                    content = {
                        MovieScreen(viewModel, scope)
                    }
                )
            }
        }
    }
}