package com.example.composesample.presentation.example.component.ui.layout.bottomsheet

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetUI(
    onBackButtonClick: () -> Unit
) {
    val visibleBs = remember { mutableStateOf(false) }
    val heightSample = remember { mutableStateOf(30.dp) } // default Height
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    //scaffoldState.bottomSheetState.targetValue
    LaunchedEffect(key1 = scaffoldState.bottomSheetState.targetValue) {
        Log.d(
            "dataCheck",
            "scaffoldState.bottomSheetState.targetValue = ${scaffoldState.bottomSheetState.targetValue}"
        )
        if (scaffoldState.bottomSheetState.targetValue == BottomSheetValue.Collapsed) {
            visibleBs.value = false
            heightSample.value = 30.dp
            scaffoldState.bottomSheetState.collapse()
        }
    }

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            Box(
                modifier = Modifier
                    .background(color = Color.DarkGray)
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
        },
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.8f)
            ) {
                Column {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { fullHeight -> fullHeight },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        ),
                        /*enter = fadeIn(),
                        exit = fadeOut()*/
                    ) {
                        // 열려있을 때 UI
                        ExpandedBottomSheet(
                            scaffoldState = scaffoldState,
                            visible = visibleBs
                        )

                        CollapsedBottomSheet( // 닫혀있을 때 UI
                            currentFraction = scaffoldState.currentFraction,
                            onSheetClick = {
                                coroutineScope.launch {
                                    if (scaffoldState.bottomSheetState.isCollapsed) {
                                        scaffoldState.bottomSheetState.expand()
                                    } else {
                                        visibleBs.value = false
                                        scaffoldState.bottomSheetState.collapse()
                                        heightSample.value = 0.dp
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        sheetPeekHeight = heightSample.value, // default Height
    ) {
        BottomSheetDataCheckUI(
            scaffoldState = scaffoldState,
            onToggle = {
                if (!visibleBs.value) {
                    visibleBs.value = true
                    heightSample.value = 500.dp//72.dp
                } else {
                    coroutineScope.launch {
                        if (scaffoldState.bottomSheetState.isCollapsed) {
                            scaffoldState.bottomSheetState.expand()
                        } else {
                            visibleBs.value = false
                            scaffoldState.bottomSheetState.collapse()
                            heightSample.value = 30.dp
                        }
                    }
                }
            },
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun BottomSheetDataCheckUI(
    scaffoldState: BottomSheetScaffoldState,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier.padding(20.dp),
            onClick = onToggle
        ) {
            Text(text = "Expanded / Collapsed BS Button")
        }

        Text("scaffoldState.bottomSheetState.targetValue = ${scaffoldState.bottomSheetState.targetValue}")
        Text("scaffoldState.currentFraction = ${scaffoldState.currentFraction}")

        Text("scaffoldState.bottomSheetState.isCollapsed = ${scaffoldState.bottomSheetState.isCollapsed}")
        Text("scaffoldState.bottomSheetState.isExpanded = ${scaffoldState.bottomSheetState.isExpanded}")

        Text("scaffoldState.bottomSheetState.progress = ${scaffoldState.bottomSheetState.progress}")
        Text("scaffoldState.bottomSheetState.progress.dp = ${scaffoldState.bottomSheetState.progress.dp}")

        Text("scaffoldState.bottomSheetState.currentValue = ${scaffoldState.bottomSheetState.currentValue}")
        Text("scaffoldState.bottomSheetState.currentValue.ordinal = ${scaffoldState.bottomSheetState.currentValue.ordinal}")
        Text("scaffoldState.bottomSheetState.currentValue.name = ${scaffoldState.bottomSheetState.currentValue.name}")

    }
}

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetScaffoldState.currentFraction: Float
    get() {
        val fraction = bottomSheetState.progress
        val targetValue = bottomSheetState.targetValue
        val currentValue = bottomSheetState.currentValue

        return when {
            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Collapsed -> 0f
            currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Expanded -> 1f
            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Expanded -> fraction
            else -> 1f - fraction
        }
    }