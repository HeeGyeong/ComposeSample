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
    val uiState = remember {
        mutableStateOf(
            SSEUIState(
                statusMessages = emptyList(),
                collectedChars = "",
                isConnected = false
            )
        )
    }
    
    val eventSourceHolder = remember { mutableStateOf<EventSource?>(null) }
    val scope = rememberCoroutineScope()

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
                        statusMessages = uiState.value.statusMessages + "연결 종료"
                    )
                }
            }

            override fun onMessage(event: String, messageEvent: MessageEvent) {
                scope.launch {
                    try {
                        val firstChar = messageEvent.data.firstOrNull()?.toString() ?: ""
                        val newCollectedChars = uiState.value.collectedChars + firstChar
                        
                        uiState.value = uiState.value.copy(
                            collectedChars = newCollectedChars
                        )
                        
                        if (newCollectedChars.length >= 10) {
                            closeConnection.invoke()
                        }
                    } catch (e: Exception) {
                        uiState.value = uiState.value.copy(
                            statusMessages = uiState.value.statusMessages + "파싱 에러: ${e.message}"
                        )
                    }
                }
            }

            override fun onError(t: Throwable) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = false,
                        statusMessages = uiState.value.statusMessages + "에러: ${t.message}"
                    )
                }
            }

            override fun onComment(comment: String) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        statusMessages = uiState.value.statusMessages + "주석: $comment"
                    )
                }
            }
        }
    }

    val startConnection = remember {
        {
            scope.launch(Dispatchers.IO) {
                try {
                    withContext(Dispatchers.Main) {
                        uiState.value = uiState.value.copy(
                            collectedChars = ""
                        )
                    }
                    
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
                            statusMessages = uiState.value.statusMessages + "연결 시작 에러: ${e.message}"
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
                // 연결 시작과 일반 메시지들
                items(uiState.statusMessages.filter { !it.startsWith("연결 종료") }) { message ->
                    StatusMessageCard(message = message)
                }

                // 수집된 글자들 표시
                if (uiState.collectedChars.isNotEmpty()) {
                    item {
                        CollectedCharsCard(chars = uiState.collectedChars)
                    }
                }

                // 연결 종료 메시지를 마지막에 표시
                items(uiState.statusMessages.filter { it.startsWith("연결 종료") }) { message ->
                    StatusMessageCard(message = message)
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