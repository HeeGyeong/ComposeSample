package com.example.composesample.presentation.legacy.movie

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
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalAnimationApi
class MovieActivity : ComponentActivity() {

    private val viewModel: MovieViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()

            // Runtime Error.
//            val viewModel = viewModel<MovieViewModel>()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopBar("Movie Activity", scaffoldState, scope)
                },
                bottomBar = {
                    BottomBar()
                },
                content = {
                    MovieScreen(viewModel, scope)
                },
                drawerContent = {
                    DrawerItem(scaffoldState, scope)
                },
                drawerGesturesEnabled = false
            )
        }
    }
}