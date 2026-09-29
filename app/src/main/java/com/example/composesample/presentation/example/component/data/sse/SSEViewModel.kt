package com.example.composesample.presentation.example.component.data.sse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesample.util.ConstValue.SSEWikiURL
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.RetryDelayStrategy
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * Wikimedia 스트림은 앱을 식별할 수 없는 User-Agent(기본값인 okhttp/…, Java/… 포함)를 403 으로 거절한다.
 * 연락처(저장소 URL)를 담은 UA 를 직접 보내야 연결된다 — 정책 문서는 같은 폴더 exampleGuide.kt 참고.
 */
private const val SSE_USER_AGENT = "ComposeSample/1.0 (https://github.com/HeeGyeong/ComposeSample)"

class SSEViewModel() : ViewModel() {
    /**
     * SSE(Server-Sent Events) 연결 상태와 메시지를 관리하는 상태 클래스
     *
     * @property messageList 수신된 모든 메시지들의 리스트
     * @property isConnected 현재 SSE 연결 상태
     */
    data class SSEUIState(
        val messageList: List<SSEMessage> = emptyList(),
        val isConnected: Boolean = false,
    )

    /**
     * SSE로부터 수신되는 다양한 타입의 메시지를 정의하는 sealed class
     *
     * - Connected: 연결 성공 메시지
     * - Comment: 서버로부터 받은 주석 메시지
     * - CollectedChars: 수집된 문자들을 표시하는 메시지 (각 연결 사이클마다 하나씩 존재)
     * - Disconnected: 연결 종료 메시지
     * - Error: 에러 메시지
     */
    sealed class SSEMessage {
        data class Connected(val message: String = "연결됨") : SSEMessage()
        data class Comment(val message: String) : SSEMessage()
        data class CollectedChars(
            val chars: String,
            val cycleId: Int,  // 각 연결 사이클을 구분하기 위한 ID
        ) : SSEMessage()

        data class Disconnected(val message: String = "연결 종료") : SSEMessage()
        data class Error(val message: String) : SSEMessage()
    }

    private val _loadMoreFlag = MutableStateFlow(false)
    val loadMoreFlag = _loadMoreFlag.asStateFlow()

    fun updateLoadMoreFlag(isLoadMore: Boolean) {
        _loadMoreFlag.update { isLoadMore }
    }

    private val _currentChars = MutableStateFlow("")
    val currentChars = _currentChars.asStateFlow()

    private val _cycleCount = MutableStateFlow(0)
    val cycleCount = _cycleCount.asStateFlow()

    fun updateCurrentChars(value: String) {
        _currentChars.update { it + value }
    }

    fun incrementCycleCount() {
        _cycleCount.update { it + 1 }
    }

    private val _uiState = MutableStateFlow(SSEUIState())
    val uiState: StateFlow<SSEUIState> = _uiState.asStateFlow()

    // 4.0 부터 EventSource 는 자체 스레드를 만들지 않는다 — 콜백 방식은 BackgroundEventSource 가 담당한다
    private var eventSourceHolder: BackgroundEventSource? = null

    fun startSSEConnection(subUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                 _currentChars.value = ""
                val sseUrl = "$SSEWikiURL?tab=$subUrl"
                Log.d("SSE", "Connecting to SSE URL: $sseUrl")

                // 헤더·URI 는 ConnectStrategy, 재연결 지연은 RetryDelayStrategy 로 옮겨졌다(3.x 의 headers/reconnectTime)
                val connectStrategy = ConnectStrategy.http(URI.create(sseUrl))
                    .header("User-Agent", SSE_USER_AGENT)
                val eventSourceBuilder = EventSource.Builder(connectStrategy)
                    .retryDelayStrategy(
                        RetryDelayStrategy.defaultStrategy().initialDelay(3, TimeUnit.SECONDS)
                    )
                eventSourceHolder = BackgroundEventSource.Builder(
                    createEventHandler(),
                    eventSourceBuilder
                ).build()
                eventSourceHolder?.start()
            } catch (e: Exception) {
                Log.e("SSE", "Error starting connection: ${e.message}")
            }
        }
    }

    fun closeSSEConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            // 10글자 이후 들어오는 메시지마다 종료를 요청하므로, 참조를 먼저 비워 한 번만 닫는다
            val holder = eventSourceHolder ?: return@launch
            eventSourceHolder = null
            try {
                holder.close()
            } catch (e: Exception) {
                Log.e("SSE", "Error closing connection: ${e.message}")
            }
            // 4.0+ BackgroundEventSource 는 close() 이후의 이벤트를 버리므로 호출자가 닫으면 onClosed 가 오지 않는다
            // (3.x 는 왔다). 연결 종료 상태는 여기서 직접 반영하고, onClosed 는 서버가 끊은 경우만 처리한다
            _uiState.update {
                it.copy(
                    isConnected = false,
                    messageList = it.messageList + SSEMessage.Disconnected()
                )
            }
        }
    }

    private fun createEventHandler(): BackgroundEventHandler = object : BackgroundEventHandler {
        override fun onOpen() {
            viewModelScope.launch {
                Log.d("SSE", "eventHandler onOpen")
                Log.d("SSE", "Connected to: ${eventSourceHolder?.eventSource?.origin}")
                _uiState.update {
                    it.copy(
                        isConnected = true,
                        messageList = uiState.value.messageList + SSEMessage.Connected()
                    )
                }
            }
        }

        override fun onClosed() {
            viewModelScope.launch {
                Log.d("SSE", "eventHandler onClosed")
                _uiState.update {
                    it.copy(
                        isConnected = false,
                        messageList = uiState.value.messageList + SSEMessage.Disconnected()
                    )
                }
            }
        }

        override fun onMessage(event: String, messageEvent: MessageEvent) {
            viewModelScope.launch {
                // 종료를 요청한 뒤 이미 큐에 들어와 있던 메시지는 버린다 — 반영하면 종료 카드 뒤에 수집 카드가 다시 붙는다
                if (eventSourceHolder == null) return@launch
                // Handle message
                try {
                    val inputUrl = eventSourceHolder?.eventSource?.origin.toString()

                    // 메시지에서 첫 글자만 추출
                    val firstChar = messageEvent.data.firstOrNull()?.toString() ?: ""
                    updateCurrentChars(firstChar)

                    // 현재 사이클의 CollectedChars 메시지만 업데이트
                    val updatedList = if (inputUrl.contains("reverseItem=true")) {
                        listOf(
                            SSEMessage.CollectedChars(
                                chars = currentChars.value,
                                cycleId = cycleCount.value
                            )
                        ) + uiState.value.messageList.filterNot {
                            it is SSEMessage.CollectedChars &&
                                    it.cycleId == cycleCount.value
                        }
                    } else {
                        uiState.value.messageList.filterNot {
                            it is SSEMessage.CollectedChars &&
                                    it.cycleId == cycleCount.value
                        } + SSEMessage.CollectedChars(
                            chars = currentChars.value,
                            cycleId = cycleCount.value
                        )
                    }

                    _uiState.update {
                        it.copy(
                            messageList = updatedList
                        )
                    }

                    // 10개 글자를 수집하면 연결 종료
                    if (currentChars.value.length >= 10) {
                        closeSSEConnection()
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            messageList = uiState.value.messageList +
                                    SSEMessage.Error("파싱 에러: ${e.message}")
                        )
                    }
                }
            }
        }

        override fun onComment(comment: String) {
            viewModelScope.launch {
                Log.d("SSE", "eventHandler onComment : $comment")
                _uiState.update {
                    it.copy(
                        messageList = uiState.value.messageList +
                                SSEMessage.Comment("주석: $comment")
                    )
                }
            }
        }

        override fun onError(t: Throwable) {
            viewModelScope.launch {
                Log.d("SSE", "eventHandler onError : $t")
                _uiState.update {
                    it.copy(
                        isConnected = false,
                        messageList = uiState.value.messageList +
                                SSEMessage.Error("에러: ${t.message}")
                    )
                }
            }
        }
    }

}