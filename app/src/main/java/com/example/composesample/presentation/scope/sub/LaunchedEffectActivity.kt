package com.example.composesample.presentation.scope.sub

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composesample.presentation.scope.LaunchedScreen
import com.example.composesample.ui.base.BottomBar
import com.example.composesample.ui.base.DrawerItem
import com.example.composesample.ui.base.SetSystemUI
import com.example.composesample.ui.base.TopBar
import java.util.concurrent.CancellationException

@ExperimentalAnimationApi
class LaunchedEffectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SetSystemUI()

            val scaffoldState = rememberScaffoldState()
            val scope = rememberCoroutineScope()

            val viewModel = viewModel<LaunchedEffectViewModel>()

            // implementation "androidx.compose.runtime:runtime-livedata:$compose_version"
            val isGo by viewModel.isGo.observeAsState()

            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopBar("Launched Activity", scaffoldState, scope)
                },
                bottomBar = {
                    BottomBar()
                },
                content = {
                    if (isGo == true) {
                        // LaunchedEffect 사용 시, 최초 실행을 제외하고 param 값이 변경될 때 취소되고 재 시작 된다.
                        // param 의 갯수는 제한되어 있지 않다.
                        LaunchedEffect(isGo) {
                            try {
                                scaffoldState.snackbarHostState
                                    .showSnackbar("input text : go")
                            } catch (e: CancellationException) {
                                Log.e("CancelText", "in catch")
                            }
                        }
                    }

                    LaunchedScreen(viewModel)
                },
                drawerContent = {
                    DrawerItem(scaffoldState, scope)
                },
                drawerGesturesEnabled = false
            )
        }
    }
}