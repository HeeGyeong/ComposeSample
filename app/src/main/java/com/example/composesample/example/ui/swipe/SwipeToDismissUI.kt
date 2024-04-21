package com.example.composesample.example.ui.swipe

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SwipeToDismissUI(
    onBackButtonClick: () -> Unit
) {
    SimpleSwipeToDismissUI(onBackButtonClick = onBackButtonClick)
}

// 아이템 dismiss 시 완전히 view에서 제거 처리 해야함.
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SimpleSwipeToDismissUI(
    onBackButtonClick: () -> Unit
) {
    LazyColumn {
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "")
                    }
                }
            }
        }

        item {
            repeat(3) { index ->
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        when (it) {
                            DismissValue.DismissedToEnd -> {
                                false // Start to End Event는 무시한다.
                            }

                            DismissValue.DismissedToStart -> {
                                true
                            }

                            else -> {
                                false
                            }
                        }
                    }
                )

                var enableDismissDirection: DismissDirection? by remember {
                    mutableStateOf(null)
                }

                LaunchedEffect(key1 = Unit, block = {
                    snapshotFlow { dismissState.offset.value }
                        .collect {
                            // width가 200이 되는 순간 이벤트 활성화. width를 구해서 비율로 계산하는게 가장 정확.
                            enableDismissDirection = when {
                                it > 200 -> DismissDirection.StartToEnd
                                it < -200 -> DismissDirection.EndToStart
                                else -> null
                            }
                        }
                })

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                    dismissThresholds = { direction ->
                        FractionalThreshold( // Deprecated
                            if (direction == DismissDirection.StartToEnd) 0.20f else 0.20f
                        )
                    },
                    background = {
                        AnimatedContent(
                            targetState = Pair(
                                dismissState.dismissDirection, // dismissDirection
                                enableDismissDirection != null // enableDismissDirection
                            ),
                            transitionSpec = {
                                fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                            }, label = "좌우 아이템 Animation"
                        ) { (dismissDirection, enableDismissDirection) ->
                            val backgroundSize =
                                remember { Animatable(if (enableDismissDirection) 0f else 1f) }
                            val iconSize =
                                remember { Animatable(if (enableDismissDirection) .8f else 1f) }

                            LaunchedEffect(key1 = Unit, block = {
                                if (enableDismissDirection) {
                                    backgroundSize.snapTo(0f) // 크기를 0으로 설정한다. 이 때, 애니메이션은 다 취소된다.
                                    launch {
                                        backgroundSize.animateTo(1f, animationSpec = tween(400))
                                    } // 크기를 100%로 설정한다. 애니메이션을 추가한다.

                                    iconSize.snapTo(.8f) // 아이콘 사이즈를 80%로 설정 한다. 이 때, 애니메이션은 다 취소된다.
                                    iconSize.animateTo(
                                        1.25f,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    ) // 크게 보이게 하고,
                                    iconSize.animateTo(1f) // 원래 크기로 돌아온다.
                                }
                            })

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(
                                        // 크기에 따라서 Circle Animation을 설정한다.
                                        // LaunchedEffect 에서 0, 1을 변경한다.
                                        CircleAnimationPath(
                                            backgroundSize.value,
                                            dismissDirection == DismissDirection.StartToEnd
                                        )
                                    )
                                    .background(
                                        // 백그라운드 색상을 아이콘 색상으로 반전시킨다.
                                        color = when (dismissDirection) {
                                            DismissDirection.StartToEnd -> if (enableDismissDirection) {
                                                Color(0xFFFFFF00)
                                            } else {
                                                Color.Black
                                            }

                                            DismissDirection.EndToStart -> if (enableDismissDirection) {
                                                Color(0xFF8B0000)
                                            } else {
                                                Color.Black
                                            }

                                            else -> Color.Transparent
                                        },
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(
                                            // 드래깅 방향에 따라서 아이콘을 보여준다.
                                            when (dismissDirection) {
                                                DismissDirection.StartToEnd -> Alignment.CenterStart
                                                else -> Alignment.CenterEnd
                                            }
                                        )
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                        .scale(iconSize.value),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 아이콘 색상을 백그라운드 색상으로 반전시킨다.
                                    when (dismissDirection) {
                                        DismissDirection.StartToEnd -> {
                                            Image(
                                                painter = rememberVectorPainter(image = Icons.Default.Star),
                                                colorFilter = ColorFilter.tint(
                                                    if (enableDismissDirection) {
                                                        Color.Black
                                                    } else {
                                                        Color(0xFFFFFF00)
                                                    }
                                                ),
                                                contentDescription = "star icon"
                                            )
                                        }

                                        DismissDirection.EndToStart -> {
                                            Image(
                                                painter = rememberVectorPainter(image = Icons.Default.Delete),
                                                colorFilter = ColorFilter.tint(
                                                    if (enableDismissDirection) {
                                                        Color.Black
                                                    } else {
                                                        Color(0xFF8B0000)
                                                    }
                                                ),
                                                contentDescription = "dismiss icon"
                                            )
                                        }

                                        else -> {}
                                    }
                                }
                            }
                        }
                    },
                    dismissContent = {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "Swipe To Dismiss Item",
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}