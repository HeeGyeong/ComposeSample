package com.example.composesample.presentation.example.ui.bottomsheet

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModalBottomSheetUI(
    onBackButtonClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
    )
    val containerHeight = remember { mutableStateOf(0) }
    val sheetContentHeight = remember { mutableStateOf(0) }

    Log.d("ModalBottomSheetUI", "-----------------------------------------------------")
//    Log.d("ModalBottomSheetUI", "bottomState.currentValue : ${bottomState.currentValue} ? ${bottomState.currentValue.name} ?? ${bottomState.currentValue.ordinal}")
//    Log.d("ModalBottomSheetUI", "bottomState.targetValue : ${bottomState.targetValue}")
//    Log.d("ModalBottomSheetUI", "bottomState.progress : ${bottomState.progress}")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        ModalBottomSheetLayout(
            modifier = Modifier.onSizeChanged { size -> // MaxSize라서 처음부터 다 그려둔다
                containerHeight.value = size.height
                Log.d("ModalBottomSheetUI", "containerHeight.value ? ${containerHeight.value}")
            },
            scrimColor = Color.Black.copy(alpha = 0.6f),
            sheetState = bottomState,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetBackgroundColor = Color.Transparent,
            sheetContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size -> // MaxSize라서 처음부터 다 그려둔다
                            sheetContentHeight.value = size.height
                            Log.d(
                                "ModalBottomSheetUI",
                                "sheetContentHeight.value ? ${sheetContentHeight.value}"
                            )
                        }
//                        .fillMaxWidth()
//                        .fillMaxHeight(fraction = 0.8f) // Expanded 일때의 높이 제한.
                ) {
                    ModalExpandedBottomSheet()
                }
            },
        ) {
            BackgroundScreen(
                onButtonClick = {
                    coroutineScope.launch {
                        bottomState.show()
                    }
                },
                onBackButtonClick = onBackButtonClick
            )
        }
    }
}

@Composable
fun BackgroundScreen(
    onButtonClick: () -> Unit,
    onBackButtonClick: () -> Unit,
) {
    Log.d("ModalBottomSheetUI", "BackgroundScreen Call")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                IconButton(
                    onClick = {
                        onBackButtonClick.invoke()
                    }
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.DarkGray),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    modifier = Modifier.padding(20.dp),
                    onClick = {
                        onButtonClick.invoke()
                    }
                ) {
                    Text(text = "BS State Change")
                }
            }
        }
    }
}
