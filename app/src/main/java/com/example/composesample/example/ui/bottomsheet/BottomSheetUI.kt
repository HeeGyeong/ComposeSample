package com.example.composesample.example.ui.bottomsheet

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    val heightSample = remember { mutableStateOf(0.dp) }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

    //scaffoldState.bottomSheetState.targetValue
    LaunchedEffect(key1 = scaffoldState.bottomSheetState.targetValue) {
        Log.d("dataCheck", "scaffoldState.bottomSheetState.targetValue = ${scaffoldState.bottomSheetState.targetValue}")
        if (scaffoldState.bottomSheetState.targetValue == BottomSheetValue.Collapsed) {
            visibleBs.value = false
            heightSample.value = 0.dp
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
                        visible = visibleBs.value,
                        enter = fadeIn(),
                        exit = fadeOut()
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
        sheetPeekHeight = heightSample.value // default Height
    ) {
        BottomSheetDebugScreen(
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
                            heightSample.value = 0.dp
                        }
                    }
                }
            },
        )
    }
}

@Composable
@ExperimentalMaterialApi
fun BottomSheetDebugScreen(
    scaffoldState: BottomSheetScaffoldState,
    onToggle: () -> Unit,
) {
    val offset = scaffoldState.bottomSheetState.requireOffset()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(offset.dp),
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

        Text("scaffoldState.bottomSheetState.requireOffset() = ${scaffoldState.bottomSheetState.requireOffset()}")
        Text("scaffoldState.bottomSheetState.requireOffset().dp = ${scaffoldState.bottomSheetState.requireOffset().dp}")

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