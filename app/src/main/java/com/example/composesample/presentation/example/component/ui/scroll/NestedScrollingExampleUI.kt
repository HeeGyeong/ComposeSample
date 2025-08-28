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
import androidx.compose.ui.geometry.Offset
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
 */
@Composable
fun NestedScrollingExampleUI(
    onBackEvent: () -> Unit,
) {
    val toolbarHeight = 200.dp
    val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }

    val toolbarOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    val preScrollCount = remember { mutableIntStateOf(0) }
    val postScrollCount = remember { mutableIntStateOf(0) }
    val preFlingCount = remember { mutableIntStateOf(0) }
    val postFlingCount = remember { mutableIntStateOf(0) }

    val lastCalledFunction = remember { mutableStateOf("None") }
    val lastScrollDelta = remember { mutableFloatStateOf(0f) }
    val lastFlingVelocity = remember { mutableFloatStateOf(0f) }

    val preScrollConsumed = remember { mutableFloatStateOf(0f) }
    val postScrollConsumed = remember { mutableFloatStateOf(0f) }
    val childScrollConsumed = remember { mutableFloatStateOf(0f) }
    val currentScrollDirection = remember { mutableStateOf("없음") }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                preScrollCount.intValue++
                lastCalledFunction.value = "onPreScroll (자식 처리 전)"
                lastScrollDelta.floatValue = available.y

                currentScrollDirection.value = if (available.y > 0) "⬇️ 아래로" else "⬆️ 위로"

                val delta = available.y

                if (delta < 0) {
                    val preConsume = delta * 0.3f
                    preScrollConsumed.floatValue = abs(preConsume)

                    val newOffset = toolbarOffsetHeightPx.floatValue + preConsume
                    toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)

                    return Offset(0f, preConsume)
                } else {
                    preScrollConsumed.floatValue = 0f
                    return Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                postScrollCount.intValue++
                lastCalledFunction.value = "onPostScroll (자식 처리 후)"

                val childConsumed = consumed.y
                childScrollConsumed.floatValue = abs(childConsumed)

                if (available.y > 0) {
                    val postConsume = available.y
                    postScrollConsumed.floatValue = abs(postConsume)

                    val newOffset = toolbarOffsetHeightPx.floatValue + postConsume
                    toolbarOffsetHeightPx.floatValue = newOffset.coerceIn(-toolbarHeightPx, 0f)

                    return Offset(0f, postConsume)
                } else {
                    postScrollConsumed.floatValue = 0f
                    return Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                preFlingCount.intValue++
                lastCalledFunction.value = "onPreFling (자식 플링 전)"
                lastFlingVelocity.floatValue = available.y

                if (abs(available.y) > 1000) {
                    return Velocity(0f, available.y * 0.3f)
                }

                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                postFlingCount.intValue++
                lastCalledFunction.value = "onPostFling (자식 플링 후)"
                lastFlingVelocity.floatValue = available.y

                return Velocity.Zero
            }
        }
    }

    val lazyListState = rememberLazyListState()

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

        ScrollStatusCard(
            preScrollCount = preScrollCount.intValue,
            postScrollCount = postScrollCount.intValue,
            preFlingCount = preFlingCount.intValue,
            postFlingCount = postFlingCount.intValue,
            lastCalledFunction = lastCalledFunction.value,
            lastScrollDelta = lastScrollDelta.floatValue,
            lastFlingVelocity = lastFlingVelocity.floatValue,
            preScrollConsumed = preScrollConsumed.floatValue,
            postScrollConsumed = postScrollConsumed.floatValue,
            childScrollConsumed = childScrollConsumed.floatValue,
            currentScrollDirection = currentScrollDirection.value
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
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
                                text = "좀 더 디테일한 항목 확인을 위해서 컨텐츠를 늘립니다.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "이정도 되는 크기는 되어야 확인이 편합니다",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

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
    lastFlingVelocity: Float,
    preScrollConsumed: Float,
    postScrollConsumed: Float,
    childScrollConsumed: Float,
    currentScrollDirection: String
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
                text = "스크롤 처리 순서: Pre → LazyColumn → Post",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )

            Text(
                text = "현재 스크롤: $currentScrollDirection",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text(
                text = "💡 LazyColumn이 끝에 도달해도 Pre → LazyColumn(0px) → Post 순서 유지",
                fontSize = 10.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 실제 소비량 표시
            ScrollConsumptionBar(
                label = "1️⃣ onPreScroll (30% 먼저 소비)",
                consumed = preScrollConsumed,
                color = Color(0xFFE53E3E),
                count = preScrollCount
            )

            Spacer(modifier = Modifier.height(6.dp))

            ScrollConsumptionBar(
                label = "2️⃣ LazyColumn (자식 처리)",
                consumed = childScrollConsumed,
                color = Color(0xFF38A169),
                count = 0
            )

            Spacer(modifier = Modifier.height(6.dp))

            ScrollConsumptionBar(
                label = "3️⃣ onPostScroll (나머지 처리)",
                consumed = postScrollConsumed,
                color = Color(0xFF3182CE),
                count = postScrollCount
            )

            Spacer(modifier = Modifier.height(8.dp))

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
private fun ScrollConsumptionBar(
    label: String,
    consumed: Float,
    color: Color,
    count: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            if (count > 0) {
                Text(
                    text = "호출: $count",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        val maxConsumption = 100f
        val consumptionRatio = (consumed / maxConsumption).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Color.Gray.copy(alpha = 0.2f),
                    RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(consumptionRatio)
                    .height(6.dp)
                    .background(
                        color,
                        RoundedCornerShape(3.dp)
                    )
            )
        }

        Text(
            text = "소비: ${consumed.toInt()}px",
            fontSize = 8.sp,
            color = color,
            modifier = Modifier.padding(top = 1.dp)
        )
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
                    text = "스크롤 처리 순서",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "실시간 소비량으로 확인하세요",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pre(30%) → LazyColumn → Post(나머지)",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
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