package com.example.composesample.presentation.example.component.lazycolumn

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.component.sse.CollectedCharsCard
import com.example.composesample.presentation.example.component.sse.SSEViewModel
import com.example.composesample.presentation.example.component.sse.StatusMessageCard
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel


/**
 * SSE 예제의 메인 Composable 함수
 * 위키피디아의 실시간 업데이트를 SSE를 통해 수신하고 표시
 *
 * @param onBackEvent 뒤로가기 버튼 클릭 시 호출될 콜백
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReverseLazyColumnExampleUI(onBackEvent: () -> Unit) {
//    UseReverseLayoutFlag(onBackEvent)
    UseNormalLayoutStyle(onBackEvent)
}

@Composable
fun UseNormalLayoutStyle(onBackEvent: () -> Unit) {
    val sseViewModel: SSEViewModel = koinViewModel()
    val uiState = sseViewModel.uiState.collectAsState().value
    val clickCount = remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val loadMoreFlag = sseViewModel.loadMoreFlag.collectAsState().value

    // 새 아이템이 추가될 때 스크롤 가장 아래로
    LaunchedEffect(uiState.messageList.size) {
        if (uiState.messageList.size > 1) {
            Log.d(
                "SSE",
                "uiState.messageList.size change[$loadMoreFlag] : ${uiState.messageList.size} "
            )

            if (loadMoreFlag) {
                listState.animateScrollToItem(0)
            } else {
                listState.animateScrollToItem(uiState.messageList.size - 1)
            }
        }
    }

    // 첫 번째 아이템이 보일 때 감지
    LaunchedEffect(listState) {
        var previousIndex = -1
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { currentIndex ->
                if (previousIndex > 0 && currentIndex == 0) {
                    Log.d(
                        "SSE",
                        "First item became visible from $previousIndex to $currentIndex"
                    )
                    if (!uiState.isConnected) {
                        sseViewModel.updateLoadMoreFlag(true)
                        clickCount.value += 1
                        sseViewModel.incrementCycleCount()
                        sseViewModel.startSSEConnection("${clickCount.value}&reverseItem=true")
                    } else {
                        sseViewModel.closeSSEConnection()
                        sseViewModel.updateLoadMoreFlag(false)
                    }
                }
                previousIndex = currentIndex
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "Reverse Lazy Column Example",
            onBackIconClicked = onBackEvent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!uiState.isConnected) {
                        sseViewModel.updateLoadMoreFlag(false)
                        clickCount.value += 1
                        sseViewModel.incrementCycleCount()
                        sseViewModel.startSSEConnection(clickCount.value.toString())
                    } else {
                        sseViewModel.closeSSEConnection()
                        sseViewModel.updateLoadMoreFlag(false)
                    }
                }
            ) {
                Text(
                    if (uiState.isConnected) {
                        "SSE 연결 종료"
                    } else {
                        "SSE 연결\n(위키피디아 실시간 업데이트 보기)"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
            ) {
                items(uiState.messageList) { message ->
                    when (message) {
                        is SSEViewModel.SSEMessage.Connected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Gray
                        )

                        is SSEViewModel.SSEMessage.Comment -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.White
                        )

                        is SSEViewModel.SSEMessage.CollectedChars -> CollectedCharsCard(chars = message.chars)
                        is SSEViewModel.SSEMessage.Disconnected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Cyan
                        )

                        is SSEViewModel.SSEMessage.Error -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UseReverseLayoutFlag(onBackEvent: () -> Unit) {
    val sseViewModel: SSEViewModel = koinViewModel()
    val uiState = sseViewModel.uiState.collectAsState().value
    val clickCount = remember { mutableStateOf(0) }
    val listState = rememberLazyListState()


    // 새 아이템이 추가될 때 스크롤 위치 유지
    LaunchedEffect(uiState.messageList.size) {
        if (uiState.messageList.size > 0 && listState.firstVisibleItemIndex == 0) {
            listState.scrollToItem(uiState.messageList.size - 1)
        }
    }

    // 첫 번째 아이템이 보일 때 감지
    LaunchedEffect(listState) {
        var previousIndex = -1
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { currentIndex ->
                if (previousIndex > 0 && currentIndex == 0) {
                    Log.d(
                        "SSE",
                        "First item became visible from $previousIndex to $currentIndex"
                    )
//                    if (!uiState.isConnected) {
//                        clickCount.value += 1
//                        sseViewModel.incrementCycleCount()
//                        sseViewModel.startSSEConnection(clickCount.value)
//                    } else {
//                        sseViewModel.closeSSEConnection()
//                    }
                }
                previousIndex = currentIndex
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
        MainHeader(
            title = "Reverse Lazy Column Example",
            onBackIconClicked = onBackEvent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!uiState.isConnected) {
                        clickCount.value += 1
                        sseViewModel.incrementCycleCount()
                        sseViewModel.startSSEConnection(clickCount.value.toString())
                    } else {
                        sseViewModel.closeSSEConnection()
                    }
                }
            ) {
                Text(
                    if (uiState.isConnected) {
                        "SSE 연결 종료"
                    } else {
                        "SSE 연결\n(위키피디아 실시간 업데이트 보기)"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState,
                reverseLayout = true
            ) {
                items(uiState.messageList) { message ->
                    when (message) {
                        is SSEViewModel.SSEMessage.Connected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Gray
                        )

                        is SSEViewModel.SSEMessage.Comment -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.White
                        )

                        is SSEViewModel.SSEMessage.CollectedChars -> CollectedCharsCard(chars = message.chars)
                        is SSEViewModel.SSEMessage.Disconnected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Cyan
                        )

                        is SSEViewModel.SSEMessage.Error -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.Red
                        )
                    }
                }
            }
        }
    }
}