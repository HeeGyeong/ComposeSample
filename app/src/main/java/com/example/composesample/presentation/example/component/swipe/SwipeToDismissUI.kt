package com.example.composesample.presentation.example.component.swipe

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
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
    val coroutineScope = rememberCoroutineScope()
    var cardItemWidth by remember { mutableStateOf(0f) }
    val startDismissEventRatio = 0.15f
    val endDismissEventRatio = 0.60f
    val textData = "Swipe To Dismiss Item"

    // Scroll 이벤트를 확인하기 위한 Dummy Item
    val items by remember {
        mutableStateOf(
            listOf(
                SwipeItem(item = textData, key = 0),
                SwipeItem(item = textData, key = 1),
                SwipeItem(item = textData, key = 2),
                SwipeItem(item = textData, key = 3),
                SwipeItem(item = textData, key = 4),
                SwipeItem(item = textData, key = 5),
                SwipeItem(item = textData, key = 6),
                SwipeItem(item = textData, key = 7),
                SwipeItem(item = textData, key = 8),
                SwipeItem(item = textData, key = 9),
                SwipeItem(item = textData, key = 10),
                SwipeItem(item = textData, key = 11),
                SwipeItem(item = textData, key = 12),
                SwipeItem(item = textData, key = 13),
                SwipeItem(item = textData, key = 14),
                SwipeItem(item = textData, key = 15),
                SwipeItem(item = textData, key = 16),
                SwipeItem(item = textData, key = 17),
                SwipeItem(item = textData, key = 18),
                SwipeItem(item = textData, key = 19),
                SwipeItem(item = textData, key = 20),
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
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

        itemsIndexed(items) { index, data ->
            var isDismissItem by remember { mutableStateOf(false) }
            var isFavoriteItem by remember { mutableStateOf(false) }
            val dismissState = rememberDismissState(
                confirmStateChange = { dismissValue ->
                    when (dismissValue) {
                        DismissValue.DismissedToEnd -> {
                            data.isFavorite = data.isFavorite.not()
                            isFavoriteItem = data.isFavorite
                            false // Start to End Event는 무시한다.
                        }

                        DismissValue.DismissedToStart -> {
                            coroutineScope.launch {
                                // delay를 주지 않으면, Animation 도중에 view가 사라져서 어색함.
                                delay(500L)
                                val deleteItem = items.indexOfFirst {
                                    it.key == data.key
                                }

                                if (deleteItem != -1) {
                                    data.isDismiss = true
                                    isDismissItem = true
                                }
                            }
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
                // View에 보이지 않던 item이 다시 보일 때, re-composable이 발생하면서 View를 초기화 시키기 때문에, 다시 설정 필요함.
                isFavoriteItem = data.isFavorite
                isDismissItem = data.isDismiss

                snapshotFlow { dismissState.offset.value }
                    .collect {
                        enableDismissDirection = when {
                            it > cardItemWidth * startDismissEventRatio -> DismissDirection.StartToEnd
                            it < -cardItemWidth * endDismissEventRatio -> DismissDirection.EndToStart
                            else -> null
                        }
                    }
            })

            SwipeToDismiss(
                state = dismissState,
                dismissThresholds = { direction ->
                    FractionalThreshold( // Deprecated
                        if (direction == DismissDirection.StartToEnd) {
                            startDismissEventRatio
                        } else {
                            endDismissEventRatio
                        }
                    )
                },
                directions = setOf(
                    DismissDirection.StartToEnd,
                    DismissDirection.EndToStart
                ), // dismiss Animation이 가능한 부분 설정.
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
                            remember { Animatable(if (enableDismissDirection) 0.8f else 1f) }

                        LaunchedEffect(key1 = Unit, block = {
                            if (enableDismissDirection) {
                                backgroundSize.snapTo(0f) // 크기를 0으로 설정한다. 이 때, 애니메이션은 다 취소된다.
                                launch {
                                    backgroundSize.animateTo(1f, animationSpec = tween(400))
                                } // 크기를 100%로 설정한다. 애니메이션을 추가한다.

                                iconSize.snapTo(0.8f) // 아이콘 사이즈를 80%로 설정 한다. 이 때, 애니메이션은 다 취소된다.
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
                                    .padding(horizontal = 20.dp)
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
                                            contentDescription = "favorite icon"
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
                    // isDismissItem 만 사용하면 천천히 스크롤했을 때 이슈 발생.
                    // recomposable이 발생할 때, false로 설정 후 true로 변경되면서 아래로 아이템 크기만큼 스크롤.
                    val cardModifier = if (data.isDismiss || isDismissItem) {
                        Modifier
                            .fillMaxWidth()
                            .height(0.dp)
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    }

                    Card(
                        modifier = cardModifier
                            .onGloballyPositioned { coordinates ->
                                // LazyColumn의 실제 너비를 측정하여 변수에 저장
                                cardItemWidth = coordinates.size.width.toFloat()
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.padding(end = 12.dp),
                                    text = "${data.item}[${data.key}]",
                                )

                                if (isFavoriteItem) {
                                    Image(
                                        modifier = Modifier.size(16.dp),
                                        painter = rememberVectorPainter(image = Icons.Default.Star),
                                        colorFilter = ColorFilter.tint(Color.Black),
                                        contentDescription = "favorite icon"
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

data class SwipeItem(
    val item: String,
    var key: Int,
    var isFavorite: Boolean = false,
    var isDismiss: Boolean = false,
)