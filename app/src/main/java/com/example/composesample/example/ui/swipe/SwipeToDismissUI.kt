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
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
                val dismissState = rememberDismissState()

                val indexDefaultColor = listOf(
                    Color.Green, Color.Blue, Color.Red
                )
                val indexDismissedToEndColor = listOf(
                    Color.Blue, Color.Red, Color.Green,
                )
                val indexDismissedToStartColor = listOf(
                    Color.Red, Color.Green, Color.Blue,
                )

                val color by animateColorAsState(
                    when (dismissState.targetValue) {
                        DismissValue.Default -> indexDefaultColor[index]
                        DismissValue.DismissedToEnd -> indexDismissedToEndColor[index]
                        DismissValue.DismissedToStart -> indexDismissedToStartColor[index]
                    }, label = ""
                )

                SwipeToDismiss(
                    state = dismissState,
                    background = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = color)
                        )
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
