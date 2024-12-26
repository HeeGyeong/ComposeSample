package com.example.composesample.util.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

@Composable
fun Toast(stream: MutableStateFlow<String>) {
    val message = stream.collectAsState().value
    if (message.isEmpty()) return

    ToastComponent(
        text = message,
        visibleChanged = { stream.update { "" } }
    )
}

@Composable
fun ToastComponent(text: String, visibleChanged: () -> Unit = {}) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(coroutineScope) {
        delay(1000L)
        visible = false
        delay(500L)
        visibleChanged.invoke()
    }

    AnimatedVisibility(
        modifier = Modifier
            .padding(top = 20.dp, start = 32.dp, end = 32.dp)
            .zIndex(1f),
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Card(
                shape = RoundedCornerShape(10.dp),
                backgroundColor = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        }
    }
}