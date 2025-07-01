package com.example.composesample.presentation.example.component.architecture.development.init

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun InitTestExampleUI(
    onBackButtonClick: () -> Unit
) {
    val initTestViewModel: InitTestViewModel = koinViewModel()

    val initLoadingFlag = initTestViewModel.isInitLoading.collectAsStateWithLifecycle()

    // ui가 다시 그려질 때 마다 호출 된다.
    LaunchedEffect(key1 = Unit) {
        initTestViewModel.changeLaunchedEffectLoading()
    }

    println("initLoading flag : ${initLoadingFlag.value}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        MainHeader(
            title = "Init Test Example",
            onBackIconClicked = onBackButtonClick
        )
    }
}