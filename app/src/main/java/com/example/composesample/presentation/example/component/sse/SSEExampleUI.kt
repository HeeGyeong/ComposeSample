package com.example.composesample.presentation.example.component.sse

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SSEExampleUI(
    onBackEvent: () -> Unit
) {
    // 상태를 remember로 감싸고 stable한 데이터 구조 사용
    val uiState = remember {
        mutableStateOf(
            SSEUIState(
                statusMessages = emptyList(),
                collectedChars = "",
                isConnected = false
            )
        )
    }
    
    // EventSource를 remember로 관리하여 불필요한 재생성 방지
    val eventSourceHolder = remember { mutableStateOf<EventSource?>(null) }
    val scope = rememberCoroutineScope()

    // 이벤트 핸들러를 stable하게 유지
    val eventHandler = remember {
        object : EventHandler {
            override fun onOpen() {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = true,
                        statusMessages = listOf("연결됨")
                    )
                }
            }

            override fun onClosed() {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = false,
                        statusMessages = listOf("연결 종료")
                    )
                }
            }

            override fun onMessage(event: String, messageEvent: MessageEvent) {
                scope.launch {
                    try {
                        val firstChar = messageEvent.data.firstOrNull()?.toString() ?: ""
                        uiState.value = uiState.value.copy(
                            collectedChars = uiState.value.collectedChars + firstChar
                        )
                    } catch (e: Exception) {
                        uiState.value = uiState.value.copy(
                            statusMessages = listOf("파싱 에러: ${e.message}")
                        )
                    }
                }
            }

            override fun onError(t: Throwable) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = false,
                        statusMessages = listOf("에러: ${t.message}")
                    )
                }
            }

            override fun onComment(comment: String) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        statusMessages = listOf("주석: $comment")
                    )
                }
            }
        }
    }

    // 연결 관련 함수들을 remember로 캐싱
    val closeConnection = remember {
        {
            scope.launch(Dispatchers.IO) {
                try {
                    eventSourceHolder.value?.close()
                    eventSourceHolder.value = null
                } catch (e: Exception) {
                    Log.e("SSE", "Error closing connection: ${e.message}")
                }
            }
        }
    }

    val startConnection = remember {
        {
            scope.launch(Dispatchers.IO) {
                try {
                    eventSourceHolder.value = EventSource.Builder(
                        eventHandler,
                        URI.create("https://stream.wikimedia.org/v2/stream/recentchange")
                    )
                        .reconnectTime(Duration.ofSeconds(3))
                        .build()
                    eventSourceHolder.value?.start()
                } catch (e: Exception) {
                    Log.e("SSE", "Error starting connection: ${e.message}")
                    withContext(Dispatchers.Main) {
                        uiState.value = uiState.value.copy(
                            statusMessages = listOf("연결 시작 에러: ${e.message}")
                        )
                    }
                }
            }
        }
    }

    SSEContent(
        uiState = uiState.value,
        onBackEvent = {
            closeConnection()
            onBackEvent()
        },
        onConnectionToggle = {
            if (!uiState.value.isConnected) {
                startConnection()
            } else {
                closeConnection()
                uiState.value = uiState.value.copy(collectedChars = "")
            }
        }
    )
}

// UI 상태를 담는 데이터 클래스
private data class SSEUIState(
    val statusMessages: List<String>,
    val collectedChars: String,
    val isConnected: Boolean
)

// UI 컴포넌트를 별도 함수로 분리
@Composable
private fun SSEContent(
    uiState: SSEUIState,
    onBackEvent: () -> Unit,
    onConnectionToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MainHeader(
            title = "SSE Example",
            onBackIconClicked = onBackEvent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onConnectionToggle) {
                Text(if (uiState.isConnected) "연결 종료" else "위키피디아 실시간 업데이트 보기")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.statusMessages) { message ->
                    StatusMessageCard(message = message)
                }

                if (uiState.collectedChars.isNotEmpty()) {
                    item {
                        CollectedCharsCard(chars = uiState.collectedChars)
                    }
                }
            }

            if (uiState.statusMessages.isEmpty() && uiState.collectedChars.isEmpty()) {
                EmptyStateMessage()
            }
        }
    }
}

@Composable
private fun StatusMessageCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        backgroundColor = when {
            message.startsWith("에러") -> Color(0xFFFFEBEE)
            message.startsWith("연결됨") -> Color(0xFFE8F5E9)
            message.startsWith("연결 종료") -> Color(0xFFFFF3E0)
            else -> Color.White
        }
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun CollectedCharsCard(chars: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Text(
            text = "수집된 첫 글자들: $chars",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun EmptyStateMessage() {
    Text(
        text = "연결 버튼을 눌러 위키피디아의 실시간 업데이트를 확인하세요",
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center,
        color = Color.Gray
    )
} 