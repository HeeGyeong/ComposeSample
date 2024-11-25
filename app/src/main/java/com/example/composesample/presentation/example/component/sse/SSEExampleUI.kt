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

/**
 * SSE(Server-Sent Events) 연결 상태와 메시지를 관리하는 상태 클래스
 * @property messageList 수신된 모든 메시지들의 리스트
 * @property isConnected 현재 SSE 연결 상태
 */
private data class SSEUIState(
    val messageList: List<SSEMessage> = emptyList(),
    val isConnected: Boolean = false
)

/**
 * SSE로부터 수신되는 다양한 타입의 메시지를 정의하는 sealed class
 * - Connected: 연결 성공 메시지
 * - Comment: 서버로부터 받은 주석 메시지
 * - CollectedChars: 수집된 문자들을 표시하는 메시지 (각 연결 사이클마다 하나씩 존재)
 * - Disconnected: 연결 종료 메시지
 * - Error: 에러 메시지
 */
private sealed class SSEMessage {
    data class Connected(val message: String = "연결됨") : SSEMessage()
    data class Comment(val message: String) : SSEMessage()
    data class CollectedChars(
        val chars: String,
        val cycleId: Int  // 각 연결 사이클을 구분하기 위한 ID
    ) : SSEMessage()
    data class Disconnected(val message: String = "연결 종료") : SSEMessage()
    data class Error(val message: String) : SSEMessage()
}

/**
 * SSE 예제의 메인 Composable 함수
 * 위키피디아의 실시간 업데이트를 SSE를 통해 수신하고 표시
 * @param onBackEvent 뒤로가기 버튼 클릭 시 호출될 콜백
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SSEExampleUI(onBackEvent: () -> Unit) {
    // UI 상태 관리를 위한 상태 변수들
    val uiState = remember { mutableStateOf(SSEUIState()) }
    val currentChars = remember { mutableStateOf("") }  // 현재 사이클에서 수집 중인 문자들
    val cycleCount = remember { mutableStateOf(0) }     // 연결 사이클 카운터
    val eventSourceHolder = remember { mutableStateOf<EventSource?>(null) }  // SSE 연결 객체
    val scope = rememberCoroutineScope()

    /**
     * SSE 연결을 종료하는 함수
     * 안전하게 연결을 종료하고 예외 처리를 수행
     */
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

    /**
     * SSE 이벤트를 처리하는 핸들러
     * 각각의 이벤트 타입에 따라 적절한 처리를 수행
     */
    val eventHandler = remember {
        object : EventHandler {
            // 연결이 성공적으로 열렸을 때 호출
            override fun onOpen() {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = true,
                        messageList = uiState.value.messageList + SSEMessage.Connected()
                    )
                }
            }

            // 연결이 종료되었을 때 호출
            override fun onClosed() {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        isConnected = false,
                        messageList = uiState.value.messageList + SSEMessage.Disconnected()
                    )
                }
            }

            // 서버로부터 메시지를 수신했을 때 호출
            override fun onMessage(event: String, messageEvent: MessageEvent) {
                scope.launch {
                    try {
                        // 메시지에서 첫 글자만 추출
                        val firstChar = messageEvent.data.firstOrNull()?.toString() ?: ""
                        currentChars.value += firstChar
                        
                        // 현재 사이클의 CollectedChars 메시지만 업데이트
                        val updatedList = uiState.value.messageList.filterNot { 
                            it is SSEMessage.CollectedChars && 
                            it.cycleId == cycleCount.value 
                        } + SSEMessage.CollectedChars(
                            chars = currentChars.value,
                            cycleId = cycleCount.value
                        )
                        
                        uiState.value = uiState.value.copy(
                            messageList = updatedList
                        )
                        
                        // 10개 글자를 수집하면 연결 종료
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

            // 서버로부터 주석을 수신했을 때 호출
            override fun onComment(comment: String) {
                scope.launch {
                    uiState.value = uiState.value.copy(
                        messageList = uiState.value.messageList + 
                            SSEMessage.Comment("주석: $comment")
                    )
                }
            }

            // 에러가 발생했을 때 호출
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

    /**
     * 새로운 SSE 연결을 시작하는 함수
     * 위키피디아의 실시간 업데이트 스트림에 연결
     */
    val startConnection = remember {
        {
            currentChars.value = ""  // 문자 수집 초기화
            cycleCount.value += 1    // 새로운 사이클 시작
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

/**
 * 상태 메시지를 표시하는 카드 Composable
 * @param message 표시할 메시지
 * @param backgroundColor 카드의 배경색
 */
@Composable
private fun StatusMessageCard(message: String, backgroundColor: Color) {
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

/**
 * 수집된 문자들을 표시하는 카드 Composable
 * @param chars 수집된 문자열
 */
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