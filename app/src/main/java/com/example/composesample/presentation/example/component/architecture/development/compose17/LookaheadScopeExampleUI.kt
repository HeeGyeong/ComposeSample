package com.example.composesample.presentation.example.component.architecture.development.compose17

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.getTextStyle

/**
 * LookaheadScope Example
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LookaheadScopeExampleUI(onBackButtonClick: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            onBackButtonClick.invoke()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }

                    Text(
                        text = "LookaheadScope 애니메이션",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = getTextStyle(20)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "✨ 안정화된 LookaheadScope로 자동 애니메이션",
                style = getTextStyle(18),
                color = Color.Blue
            )

            Spacer(modifier = Modifier.height(16.dp))

            SizeChangeAnimationExample()

            Spacer(modifier = Modifier.height(24.dp))

            PositionChangeAnimationExample()

            Spacer(modifier = Modifier.height(24.dp))

            ComplexAnimationExample()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SizeChangeAnimationExample() {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📏 자동 크기 변경 애니메이션",
                style = getTextStyle(16),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "LookaheadScope는 레이아웃 변경을 미리 계산하여 부드러운 애니메이션을 제공합니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // animateBounds로 직접 애니메이션
            LookaheadScope {
                Box(
                    modifier = Modifier
                        .width(if (expanded) 300.dp else 150.dp)
                        .height(if (expanded) 150.dp else 75.dp)
                        .animateBounds(
                            lookaheadScope = this@LookaheadScope,
                            boundsTransform = { _, _ -> 
                                spring(dampingRatio = 0.8f, stiffness = 400f)
                            }
                        )
                        .background(Color.Blue, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (expanded) "확장됨!" else "축소됨",
                        color = Color.White,
                        style = getTextStyle(14)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (expanded) "축소하기" else "확장하기",
                    style = getTextStyle(14)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun PositionChangeAnimationExample() {
    var isRight by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📍 자동 위치 변경 애니메이션",
                style = getTextStyle(16),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "요소의 위치가 변경될 때 자동으로 애니메이션이 적용됩니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // animateBounds로 위치 애니메이션 
            LookaheadScope {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .offset(x = if (isRight) 200.dp else 0.dp)
                            .animateBounds(
                                lookaheadScope = this@LookaheadScope,
                                boundsTransform = { _, _ -> 
                                    spring(dampingRatio = 0.7f, stiffness = 300f)
                                }
                            )
                            .background(Color.Green, RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📦",
                            style = getTextStyle(20)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { isRight = !isRight },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRight) "왼쪽으로 이동" else "오른쪽으로 이동",
                    style = getTextStyle(14)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ComplexAnimationExample() {
    var state by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎭 복합 애니메이션 (크기 + 위치 + 색상)",
                style = getTextStyle(16),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "여러 속성이 동시에 변경되어도 자연스러운 애니메이션을 제공합니다.",
                style = getTextStyle(14),
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 복합 애니메이션 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                LookaheadScope {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = when (state) {
                            0 -> Arrangement.Start
                            1 -> Arrangement.Center
                            else -> Arrangement.End
                        },
                        verticalAlignment = when (state) {
                            0 -> Alignment.Top
                            1 -> Alignment.CenterVertically
                            else -> Alignment.Bottom
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    when (state) {
                                        0 -> 50.dp
                                        1 -> 80.dp
                                        else -> 60.dp
                                    }
                                )
                                .animateBounds(
                                    lookaheadScope = this@LookaheadScope,
                                    boundsTransform = { _, _ -> 
                                        spring(dampingRatio = 0.6f, stiffness = 200f)
                                    }
                                )
                                .background(
                                    when (state) {
                                        0 -> Color.Red
                                        1 -> Color.Blue
                                        else -> Color.Green
                                    },
                                    RoundedCornerShape(
                                        when (state) {
                                            0 -> 8.dp
                                            1 -> 40.dp
                                            else -> 16.dp
                                        }
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (state) {
                                    0 -> "🔴"
                                    1 -> "🔵"
                                    else -> "🟢"
                                },
                                style = getTextStyle(20)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { state = 0 },
                    enabled = state != 0
                ) {
                    Text("상태 1", style = getTextStyle(12))
                }

                Button(
                    onClick = { state = 1 },
                    enabled = state != 1
                ) {
                    Text("상태 2", style = getTextStyle(12))
                }

                Button(
                    onClick = { state = 2 },
                    enabled = state != 2
                ) {
                    Text("상태 3", style = getTextStyle(12))
                }
            }
        }
    }
}