package com.example.composesample.util

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun rememberImeState(): State<Boolean> {
    val imeState = remember {
        mutableStateOf(false)
    }

    // 해당 API는 안정적이지 않음. 따라서 어노테이션 추가
    val windowInsets = WindowInsets.isImeVisible

    LaunchedEffect(windowInsets) {
        snapshotFlow { windowInsets }
            .collect { isVisible ->
                imeState.value = isVisible
            }
    }

    return imeState
}
