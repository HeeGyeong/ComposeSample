package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.rememberImeState

/**
 * Compose Version 1.4.0-alpha04 이하의 버전을 사용하면
 * Keyboard가 정상적으로 동작하지 않는 이슈를 가지고 있는 Compose 함수.
 *
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyColumnIssueExampleUI(onBackEvent: () -> Unit) {
    val imeState = rememberImeState()
    val imeBottomPadding = WindowInsets.ime.getBottom(LocalDensity.current)
    val density = LocalDensity.current
    val imeBottomPaddingDp = with(density) { imeBottomPadding.toDp() }

    LazyColumn(
        modifier = Modifier
            .padding(bottom = if (imeState.value) imeBottomPaddingDp else 0.dp)
    ) {
        stickyHeader {
            MainHeader(
                title = "Lazy Column Keyboard Issue",
                onBackIconClicked = onBackEvent
            )
        }

        repeat(15) {
            item {
                InputBoxContent()

                HorizontalDivider(
                    thickness = 3.dp,
                    color = Color.Green
                )
            }
        }
    }
}

@Composable
fun InputBoxContent() {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        value = "", onValueChange = {}
    )
}