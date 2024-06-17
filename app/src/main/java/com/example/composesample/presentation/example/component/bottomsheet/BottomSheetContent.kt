package com.example.composesample.presentation.example.component.bottomsheet

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.util.noRippleClickable
import com.example.composesample.util.noRippleSingleClickable
import com.example.composesample.presentation.MainHeader

/**
 * BottomSheet Component
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExpandedBottomSheet(
    scaffoldState: BottomSheetScaffoldState,
    visible: MutableState<Boolean>
) {
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .background(color = Color.DarkGray)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        repeat(1) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(horizontal = 20.dp),
                                text = "BS Contents 채워넣기",
                                textAlign = TextAlign.Center,
                            )

                            if (visible.value) {
                                Text("scaffoldState.bottomSheetState.targetValue = ${scaffoldState.bottomSheetState.targetValue}")
                                Text("scaffoldState.currentFraction = ${scaffoldState.currentFraction}")

                                Text("scaffoldState.bottomSheetState.isCollapsed = ${scaffoldState.bottomSheetState.isCollapsed}")
                                Text("scaffoldState.bottomSheetState.isExpanded = ${scaffoldState.bottomSheetState.isExpanded}")

                                Text("scaffoldState.bottomSheetState.requireOffset() = ${scaffoldState.bottomSheetState.requireOffset()}")
                                Text("scaffoldState.bottomSheetState.requireOffset().dp = ${scaffoldState.bottomSheetState.requireOffset().dp}")

                                Text("scaffoldState.bottomSheetState.progress = ${scaffoldState.bottomSheetState.progress}")
                                Text("scaffoldState.bottomSheetState.progress.dp = ${scaffoldState.bottomSheetState.progress.dp}")

                                Text("scaffoldState.bottomSheetState.currentValue = ${scaffoldState.bottomSheetState.currentValue}")
                                Text("scaffoldState.bottomSheetState.currentValue.ordinal = ${scaffoldState.bottomSheetState.currentValue.ordinal}")
                                Text("scaffoldState.bottomSheetState.currentValue.name = ${scaffoldState.bottomSheetState.currentValue.name}")
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MainHeader(
                        title = "Compose Example Project",
                        onBackIconClicked = { },
                        onLeftIconContent = { }
                    )
                }
            }
        }
    }
}


@Composable
fun CollapsedBottomSheet(
    currentFraction: Float,
    onSheetClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // sheetPeekHeight 과 동일하다.
            .background(MaterialTheme.colors.primary)
            .graphicsLayer(alpha = 1f - currentFraction)
            .noRippleClickable(
                onClick = onSheetClick,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            text = "This is a View where you can raise BottomSheet.\nIt goes up even if you click on it.",
        )
    }
}

/**
 * Modal BottomSheet Component
 */
@Composable
fun ModalExpandedBottomSheet() {
    val localDensity = LocalDensity.current
    var sheetContentHeight by remember { mutableStateOf(0f) }
    Log.d("ModalBottomSheetUI", "ModalExpandedBottomSheet Call $sheetContentHeight")


    Surface {
        Column(modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates -> // MaxSize라서 처음부터 다 그려둔다
//                columnHeightPx = coordinates.size.height.toFloat()
//                columnHeightDp = with(localDensity) { coordinates.size.height.toDp() }

                Log.d( // 현재 보여지고 있는 BottomSheet의 Height.
                    "ModalBottomSheetUI",
                    "onGloballyPositioned ? :: ${coordinates.boundsInWindow().height}"
                )
                sheetContentHeight = coordinates.boundsInWindow().height
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .background(color = Color.DarkGray)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 36.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        repeat(10) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(horizontal = 20.dp),
                                text = "BS Contents 채워넣기",
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 36.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    MainHeader(
                        title = "Compose Example Project",
                        onBackIconClicked = { },
                        onLeftIconContent = { }
                    )
                }
            }
        }
    }
}

/**
 * Custom BottomSheet Component
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BoxScope.CustomBottomSheetComponent(
    visible: MutableState<Boolean>,
) {
    AnimatedVisibility(
        visible = visible.value,
        modifier = Modifier
            .background(color = Color(0x33000000))
            .fillMaxSize()
            .noRippleSingleClickable {
                visible.value = false
            },
        enter = fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)),
    ) {
        Box {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(
                            initialOffsetY = { height -> height },
                            animationSpec = tween()
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { height -> height },
                            animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                        )
                    ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                backgroundColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            modifier = Modifier.padding(vertical = 10.dp),
                            text = "title",
                            fontSize = 18.sp
                        )

                        Text(
                            modifier = Modifier.padding(
                                top = 6.dp,
                                bottom = 10.dp,
                            ),
                            text = "subTitle",
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier
                                .height(88.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    onClick = {
                                        visible.value = false
                                    }
                                ) {
                                    Text(text = "닫기")
                                }
                            }
                        }
                    }
                }
            }
        }

        BackHandler(enabled = true, onBack = {
            visible.value = false
        })
    }
}