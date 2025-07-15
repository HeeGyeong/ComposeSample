package com.example.composesample.presentation.example.component.ui.scroll

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.Velocity
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
 * 5. 모든 NestedScrollConnection 함수들의 동작 확인
 */
@Composable
fun NestedScrollingExampleUI(
    onBackEvent: () -> Unit,
) {
    val toolbarHeight = 200.dp
    val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }

    // 툴바 오프셋 상태
    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    // 각 함수 호출 횟수 추적
    val preScrollCount = remember { mutableIntStateOf(0) }
    val postScrollCount = remember { mutableIntStateOf(0) }
    val preFlingCount = remember { mutableIntStateOf(0) }
    val postFlingCount = remember { mutableIntStateOf(0) }

    // 마지막 호출된 함수 추적
    val lastCalledFunction = remember { mutableStateOf("None") }
    val lastScrollDelta = remember { mutableFloatStateOf(0f) }
    val lastFlingVelocity = remember { mutableFloatStateOf(0f) }

    // 네스티드 스크롤 연결 설정
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                preScrollCount.intValue++
                lastCalledFunction.value = "onPreScroll (자식 처리 전)"
                lastScrollDelta.floatValue = available.y

                val delta = available.y

                // 위로 스크롤할 때만 여기서 처리 (자식에게 전달하기 전에 툴바 숨기기)
                if (delta < 0 && toolbarOffsetHeightPx.floatValue > -toolbarHeightPx) {
                    val newOffset = toolbarOffsetHeightPx.floatValue + delta
                    toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)

                    // 툴바가 아직 숨겨지지 않았다면 스크롤을 여기서 소비 (자식에게 전달 안함)
                    val consumed =
                        toolbarOffsetHeightPx.floatValue - (toolbarOffsetHeightPx.floatValue - delta)
                    return androidx.compose.ui.geometry.Offset(0f, consumed)
                }

                // 아래로 스크롤하거나 툴바가 이미 숨겨진 경우 자식에게 전달
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                postScrollCount.intValue++
                lastCalledFunction.value = "onPostScroll (자식 처리 후)"
                lastScrollDelta.floatValue = available.y

                // 자식이 처리하지 못한 나머지 스크롤만 여기서 처리
                // 아래로 스크롤할 때 & 자식이 더 이상 스크롤할 수 없을 때만 툴바 보이기
                if (available.y > 0 && toolbarOffsetHeightPx.floatValue < 0) {
                    val delta = available.y
                    val newOffset = toolbarOffsetHeightPx.floatValue + delta
                    toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)

                    // 실제로 소비한 스크롤 양 반환
                    return androidx.compose.ui.geometry.Offset(
                        0f,
                        newOffset - (toolbarOffsetHeightPx.floatValue - delta)
                    )
                }

                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                preFlingCount.intValue++
                lastCalledFunction.value = "onPreFling"
                lastFlingVelocity.floatValue = available.y

                // 빠른 스크롤 시 툴바 완전히 숨기기/보이기
                if (abs(available.y) > 1000) {
                    toolbarOffsetHeightPx.floatValue = if (available.y < 0) {
                        -toolbarHeightPx // 위로 빠르게 스크롤: 완전히 숨기기
                    } else {
                        0f // 아래로 빠르게 스크롤: 완전히 보이기
                    }
                }

                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                postFlingCount.intValue++
                lastCalledFunction.value = "onPostFling"
                lastFlingVelocity.floatValue = available.y

                return Velocity.Zero
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

        // 함수 호출 상태 표시
        ScrollStatusCard(
            preScrollCount = preScrollCount.intValue,
            postScrollCount = postScrollCount.intValue,
            preFlingCount = preFlingCount.intValue,
            postFlingCount = postFlingCount.intValue,
            lastCalledFunction = lastCalledFunction.value,
            lastScrollDelta = lastScrollDelta.floatValue,
            lastFlingVelocity = lastFlingVelocity.floatValue
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
                                text = when {
                                    index < 10 -> "천천히 스크롤해보세요 (onPreScroll, onPostScroll 확인)"
                                    index < 20 -> "빠르게 플링해보세요 (onPreFling, onPostFling 확인)"
                                    else -> "다양한 스크롤 패턴을 시도해보세요!"
                                },
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

@SuppressLint("DefaultLocale")
@Composable
private fun ScrollStatusCard(
    preScrollCount: Int,
    postScrollCount: Int,
    preFlingCount: Int,
    postFlingCount: Int,
    lastCalledFunction: String,
    lastScrollDelta: Float,
    lastFlingVelocity: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "NestedScrollConnection 함수 호출 상태",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScrollFunctionStatus("onPreScroll", preScrollCount, Color(0xFF4CAF50))
                ScrollFunctionStatus("onPostScroll", postScrollCount, Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScrollFunctionStatus("onPreFling", preFlingCount, Color(0xFFFF9800))
                ScrollFunctionStatus("onPostFling", postFlingCount, Color(0xFFE91E63))
            }

            if (lastCalledFunction != "None") {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "마지막 호출: $lastCalledFunction",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                if (lastCalledFunction.contains("Scroll")) {
                    Text(
                        text = "Delta: ${String.format("%.1f", lastScrollDelta)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else if (lastCalledFunction.contains("Fling")) {
                    Text(
                        text = "Velocity: ${String.format("%.1f", lastFlingVelocity)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollFunctionStatus(
    functionName: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = functionName,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = count.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
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