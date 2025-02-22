package com.example.composesample.presentation.example.component.init

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun InitTestExampleUI(
    onBackButtonClick: () -> Unit
) {
    val initTestViewModel: InitTestViewModel = koinViewModel()
    val launchedEffectLoading = initTestViewModel.isLaunchedEffectLoading.collectAsState().value

    LaunchedEffect(key1 = Unit) {
        println("Launched flag before: $launchedEffectLoading")
        initTestViewModel.changeLaunchedEffectLoading()
        println("Launched flag after: $launchedEffectLoading")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        MainHeader(
            title = "Text Shimmer Example",
            onBackIconClicked = onBackButtonClick
        )
    }
}