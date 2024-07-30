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

val itemHeight = 64.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DragAndDropExampleUI(
    onBackButtonClick: () -> Unit
) {
    var items by remember { mutableStateOf(List(20) { "Item $it" }) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
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
                    dragOffset += offset.y

                    val firstVisibleItemIndex = listState.firstVisibleItemIndex
                    val lastVisibleItemIndex =
                        firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size - 1
                    val centerIndex = (firstVisibleItemIndex + lastVisibleItemIndex) / 2

                    val currentIndex = draggedItemIndex ?: return@DraggableItem

                    // Drag 위치에 따라 targetIndex를 현재 위치 +-1 처리한다.
                    val targetIndex =
                        (currentIndex + (dragOffset / itemHeightPx).toInt())
                            .coerceIn(0, items.lastIndex) // list의 index 범위를 넘어가면 범위 내로 조정한다.


                    // dragOffset의 변화량을 조절. itemHeightPx을 그대로 사용하기엔 변화량이 너무 크다.
                    val changeDragOffset = // 변경 수치 * +-1
                        (itemHeightPx - itemHeightPx / 10) * (targetIndex - currentIndex).sign

                    // drag를 통해 index가 변화하면 list를 갱신한다.
                    if (targetIndex != currentIndex) {
                        items = items.toMutableList().apply {
                            add(targetIndex, removeAt(currentIndex))
                        }
                        draggedItemIndex = targetIndex
                        dragOffset -= changeDragOffset
                    }

                    coroutineScope.launch {
                        if (draggedItemIndex != null) {
                            // 아래로 스크롤
                            if (dragOffset > 0) {
                                // 아래로 스크롤을 시작하는 기준점
                                if (draggedItemIndex!! >= centerIndex) {
                                    // 스크롤되는 정도가 offset의 변화보다 작아야 더 자연스러운 애니메이션을 보여준다.
                                    listState.scrollBy(changeDragOffset / 2)
                                }
                            }
                            // 위로 스크롤
                            else {
                                // 위로 스크롤을 시작하는 기준점
                                if (draggedItemIndex!! <= centerIndex) {
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
            .height(itemHeight)
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