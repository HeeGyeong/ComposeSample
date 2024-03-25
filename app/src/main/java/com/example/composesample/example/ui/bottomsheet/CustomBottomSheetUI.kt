package com.example.composesample.example.ui.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

@Composable
fun CustomBottomSheetUI(
    onBackButtonClick: () -> Unit
) {
    val customBottomSheetVisible = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        BackgroundScreen(
            onButtonClick = {
                customBottomSheetVisible.value = true
            },
            onBackButtonClick = onBackButtonClick
        )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // BottomSheet
            CustomBottomSheetComponent(
                visible = customBottomSheetVisible
            )
        }
    }
}