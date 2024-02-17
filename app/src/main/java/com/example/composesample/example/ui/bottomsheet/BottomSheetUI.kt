package com.example.composesample.example.ui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
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
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )

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
                // 열려있을 때 UI
                ExpandedBottomSheet()

                CollapsedBottomSheet( // 닫혀있을 때 UI
                    isCollapsed = scaffoldState.bottomSheetState.isCollapsed,
                    currentFraction = scaffoldState.bottomSheetState.progress == 1f,
                    onSheetClick = {
                        coroutineScope.launch {
                            if (scaffoldState.bottomSheetState.isCollapsed) {
                                scaffoldState.bottomSheetState.expand()
                            } else {
                                scaffoldState.bottomSheetState.collapse()
                            }
                        }
                    }
                )
            }
        },
        sheetPeekHeight = 72.dp // default Height
    ) {
        BottomSheetDebugScreen(
            scaffoldState = scaffoldState,
            onToggle = {
                coroutineScope.launch {
                    if (scaffoldState.bottomSheetState.isCollapsed) {
                        scaffoldState.bottomSheetState.expand()
                    } else {
                        scaffoldState.bottomSheetState.collapse()
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