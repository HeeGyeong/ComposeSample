package com.example.composesample.example.ui.swipe

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeToDismissUI(
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
                                true
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

                val indexDefaultColor = listOf(
                    Color.DarkGray, Color.Black, Color.LightGray
                )
                val indexDismissedToEndColor = listOf(
                    Color.Black, Color.LightGray, Color.DarkGray,
                )
                val indexDismissedToStartColor = listOf(
                    Color.LightGray, Color.DarkGray, Color.Black,
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
                    /*dismissThresholds = { direction ->
                        FractionalThreshold( // Deprecated
                            if (direction == DismissDirection.StartToEnd) 0.20f else 0.20f
                        )
                    },*/
                    background = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                DismissValue.Default -> indexDefaultColor[index]
                                DismissValue.DismissedToEnd -> indexDismissedToEndColor[index]
                                DismissValue.DismissedToStart -> indexDismissedToStartColor[index]
                            }, label = ""
                        )

                        val alignment = when (dismissState.targetValue) {
                            DismissValue.Default -> Alignment.Center
                            DismissValue.DismissedToStart -> Alignment.CenterEnd
                            DismissValue.DismissedToEnd -> Alignment.CenterStart
                        }
                        val icon = when (dismissState.targetValue) {
                            DismissValue.Default -> Icons.Default.Home
                            DismissValue.DismissedToStart -> Icons.Default.Delete
                            DismissValue.DismissedToEnd -> Icons.Default.Done
                        }

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(color = color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Localized description",
                                modifier = Modifier.scale(1f),
                                tint = if (dismissState.targetValue == DismissValue.Default) {
                                    Color.Transparent
                                } else {
                                    Color.White
                                }
                            )
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