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
    var messages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isConnected by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var eventSource: EventSource? by remember { mutableStateOf(null) }

    val eventHandler = remember {
        object : EventHandler {
            override fun onOpen() {
                scope.launch {
                    isConnected = true
                    messages = messages + "연결됨"
                }
            }

            override fun onClosed() {
                scope.launch {
                    isConnected = false
                    messages = messages + "연결 종료"
                }
            }

            override fun onMessage(event: String, messageEvent: MessageEvent) {
                scope.launch {
                    try {
                        messages = messages + "수신: ${messageEvent.data}"
                        Log.d("SSE", "Event: $event, Data: ${messageEvent.data}")
                    } catch (e: Exception) {
                        messages = messages + "파싱 에러: ${e.message}"
                    }
                }
            }

            override fun onError(t: Throwable) {
                scope.launch {
                    messages = messages + "에러: ${t.message}"
                    isConnected = false
                }
            }

            override fun onComment(comment: String) {
                scope.launch {
                    messages = messages + "주석: $comment"
                }
            }
        }
    }

    // 연결 종료를 처리하는 함수
    val closeConnection = {
        scope.launch(Dispatchers.IO) {
            try {
                eventSource?.close()
                eventSource = null
            } catch (e: Exception) {
                Log.e("SSE", "Error closing connection: ${e.message}")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MainHeader(
            title = "SSE Example",
            onBackIconClicked = {
                closeConnection()
                onBackEvent()
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!isConnected) {
                        scope.launch(Dispatchers.IO) {
                            try {
                                eventSource = EventSource.Builder(
                                    eventHandler,
                                    URI.create("https://stream.wikimedia.org/v2/stream/recentchange")
                                )
                                    .reconnectTime(Duration.ofSeconds(3))
                                    .build()
                                eventSource?.start()
                            } catch (e: Exception) {
                                Log.e("SSE", "Error starting connection: ${e.message}")
                                withContext(Dispatchers.Main) {
                                    messages = messages + "연결 시작 에러: ${e.message}"
                                }
                            }
                        }
                    } else {
                        closeConnection()
                    }
                }
            ) {
                Text(if (isConnected) "연결 종료" else "위키피디아 실시간 업데이트 보기")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(messages) { message ->
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
            }

            if (messages.isEmpty()) {
                Text(
                    text = "연결 버튼을 눌러 위키피디아의 실시간 업데이트를 확인하세요",
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    }
} 