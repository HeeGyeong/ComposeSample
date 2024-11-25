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

private data class SSEUIState(
    val messageList: List<SSEMessage> = emptyList(),
    val isConnected: Boolean = false
)

private sealed class SSEMessage {
    data class Connected(val message: String = "연결됨") : SSEMessage()
    data class Comment(val message: String) : SSEMessage()
    data class CollectedChars(val chars: String) : SSEMessage()
    data class Disconnected(val message: String = "연결 종료") : SSEMessage()
    data class Error(val message: String) : SSEMessage()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SSEExampleUI(
    onBackEvent: () -> Unit
) {
    val uiState = remember { mutableStateOf(SSEUIState()) }
    val currentChars = remember { mutableStateOf("") }
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
                        messageList = uiState.value.messageList + SSEMessage.Connected()
                    )
                }
            }

            override fun onClosed() {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = false,
                        messageList = uiState.value.messageList + 
                            SSEMessage.CollectedChars(currentChars.value) +
                            SSEMessage.Disconnected()
                    )
                }
            }

            override fun onMessage(event: String, messageEvent: MessageEvent) {
                scope.launch {
                    try {
                        val firstChar = messageEvent.data.firstOrNull()?.toString() ?: ""
                        currentChars.value += firstChar
                        
                        if (currentChars.value.length >= 10) {
                            closeConnection.invoke()
                        }
                    } catch (e: Exception) {
                        uiState.value = uiState.value.copy(
                            messageList = uiState.value.messageList + 
                                SSEMessage.Error("파싱 에러: ${e.message}")
                        )
                    }
                }
            }

            override fun onComment(comment: String) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        messageList = uiState.value.messageList + 
                            SSEMessage.Comment("주석: $comment")
                    )
                }
            }

            override fun onError(t: Throwable) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = false,
                        messageList = uiState.value.messageList + 
                            SSEMessage.Error("에러: ${t.message}")
                    )
                }
            }
        }
    }

    val startConnection = remember {
        {
            currentChars.value = ""
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
                            messageList = uiState.value.messageList + 
                                SSEMessage.Error("연결 시작 에러: ${e.message}")
                        )
                    }
                }
            }
        }
    }

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
            Button(
                onClick = {
                    if (!uiState.value.isConnected) startConnection()
                    else closeConnection()
                }
            ) {
                Text(if (uiState.value.isConnected) "연결 종료" else "위키피디아 실시간 업데이트 보기")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.value.messageList) { message ->
                    when (message) {
                        is SSEMessage.Connected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color(0xFFE8F5E9)
                        )
                        is SSEMessage.Comment -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color.White
                        )
                        is SSEMessage.CollectedChars -> CollectedCharsCard(chars = message.chars)
                        is SSEMessage.Disconnected -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color(0xFFFFF3E0)
                        )
                        is SSEMessage.Error -> StatusMessageCard(
                            message = message.message,
                            backgroundColor = Color(0xFFFFEBEE)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusMessageCard(
    message: String,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 4.dp,
        backgroundColor = backgroundColor
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