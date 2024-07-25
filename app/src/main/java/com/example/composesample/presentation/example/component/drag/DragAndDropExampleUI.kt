package com.example.composesample.presentation.example.component.drag

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sign

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DragAndDropExampleUI(
    onBackButtonClick: () -> Unit
) {
    var items by remember { mutableStateOf(List(20) { "Item $it" }) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    val itemHeight = 64.dp
    val itemHeightPx = with(density) { itemHeight.toPx() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
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
                    // offset에 따른 item index 변경 로직
                    dragOffset += offset.y

                    val firstVisibleItemIndex = listState.firstVisibleItemIndex
                    val lastVisibleItemIndex =
                        firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size - 1
                    val currentIndex = draggedItemIndex ?: return@DraggableItem
                    val targetIndex =
                        (currentIndex + (dragOffset / itemHeightPx).toInt()).coerceIn(
                            0,
                            items.lastIndex
                        )
                    val changeDragOffset =
                        (itemHeightPx - itemHeightPx / 10) * (targetIndex - currentIndex).sign

                    if (targetIndex != currentIndex) {
                        items = items.toMutableList().apply {
                            add(targetIndex, removeAt(currentIndex))
                        }
                        draggedItemIndex = targetIndex
                        dragOffset -= changeDragOffset
                    }

                    // 자동 스크롤 로직 -> 기본적인 자연스러운 스크롤. 최상단 아이템, 최하단 아이템에서 문제 발생
                    coroutineScope.launch {
                        if (draggedItemIndex != null) {
                            // 아래로 스크롤
                            if (dragOffset > 0) {
                                if (draggedItemIndex!! >= (firstVisibleItemIndex + lastVisibleItemIndex) / 2) {
                                    listState.scrollBy(changeDragOffset / 2)
                                }
                            }
                            // 위로 스크롤
                            else {
                                if (draggedItemIndex!! <= (firstVisibleItemIndex + lastVisibleItemIndex) / 2) {
                                    listState.scrollBy(changeDragOffset / 2)
                                }
                            }
                        }
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
            .height(64.dp)
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
    ) {
        Text(
            text = item,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Gray)
        )
    }
}