package com.example.composesample.presentation.example.component.drag

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DragAndDropExampleUI(
    onBackButtonClick: () -> Unit
) {
    var items by remember { mutableStateOf(List(20) { "Item $it" }) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Column {
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
        }

        itemsIndexed(items) { index, item ->
            DraggableItem(
                item = item,
                dragOffset = if (index == draggedItemIndex) dragOffset else 0f,
                onDragStart = { draggedItemIndex = index },
                onDragEnd = {
                    draggedItemIndex = null
                    dragOffset = 0f
                },
                onDrag = { offset ->
                    dragOffset += offset.y
                    val currentIndex = draggedItemIndex ?: return@DraggableItem
                    val targetIndex =
                        (currentIndex + (dragOffset / 100).toInt()).coerceIn(0, items.lastIndex)
                    if (targetIndex != currentIndex) {
                        items = items.toMutableList().apply {
                            add(targetIndex, removeAt(currentIndex))
                        }
                        draggedItemIndex = targetIndex
                        dragOffset -= 220f * (targetIndex - currentIndex).sign // 아이템 높이에 따라 조정
                    }
                }
            )
        }
    }
}

@Composable
fun DraggableItem(
    item: String,
    dragOffset: Float,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .composed {
                Modifier
                    .graphicsLayer(translationY = dragOffset)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            }
                        )
                    }
            }
            .onSizeChanged { size ->
                Log.d("listItemHeight", "size : ${size.height}")
            }
    ) {
        Text(
            text = item,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        )
    }
}