package com.example.composesample.presentation.example.component.ui.scroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlin.math.abs

/**
 * Nested Scrolling Example
 *
 * NestedScrollConnection을 활용하여 다음 기능들을 구현:
 * 1. Collapsing Toolbar 효과
 * 2. 스크롤에 따른 헤더 투명도 변화
 * 3. 중첩된 스크롤 동작 제어
 * 4. 스크롤 상태에 따른 UI 변화
 */
@Composable
fun NestedScrollingExampleUI(
    onBackEvent: () -> Unit,
) {
    val toolbarHeight = 200.dp
    val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }

    // 툴바 오프셋 상태
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    // 네스티드 스크롤 연결 설정
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx.floatValue + delta
                toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)

                // 툴바가 완전히 숨겨지지 않았다면 스크롤 소비
                return if (toolbarOffsetHeightPx.floatValue > -toolbarHeightPx && delta < 0) {
                    androidx.compose.ui.geometry.Offset(0f, delta)
                } else {
                    androidx.compose.ui.geometry.Offset.Zero
                }
            }
        }
    }

    val lazyListState = rememberLazyListState()

    // 투명도 계산
    val alpha = remember(toolbarOffsetHeightPx.floatValue) {
        1f - (abs(toolbarOffsetHeightPx.floatValue) / toolbarHeightPx)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Nested Scrolling Example",
            onBackIconClicked = onBackEvent
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            // 스크롤 가능한 콘텐츠
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(top = toolbarHeight),
                modifier = Modifier.fillMaxSize()
            ) {
                items(50) { index ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        elevation = 4.dp,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "항목 ${index + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "이것은 스크롤 가능한 콘텐츠입니다. " +
                                        "위로 스크롤하면 툴바가 사라지고, " +
                                        "아래로 스크롤하면 툴바가 나타납니다.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Collapsing Toolbar
            CollapsingToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(toolbarHeight)
                    .offset {
                        androidx.compose.ui.unit.IntOffset(
                            x = 0,
                            y = toolbarOffsetHeightPx.floatValue.toInt()
                        )
                    }
                    .alpha(alpha),
            )
        }
    }
}

@Composable
private fun CollapsingToolbar(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = 8.dp,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6200EE),
                            Color(0xFF3700B3)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Nested Scrolling",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "스크롤하여 툴바 효과를 확인하세요",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = " 스크롤하여 숨기기/보이기 ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NestedScrollingExamplePreview() {
    NestedScrollingExampleUI(
        onBackEvent = {}
    )
} 