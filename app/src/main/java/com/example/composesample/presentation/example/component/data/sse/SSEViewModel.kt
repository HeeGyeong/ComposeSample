package com.example.composesample.presentation.example.component.data.sse

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesample.util.ConstValue.Companion.SSEWikiURL
import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.TimeUnit

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
        _currentChars.value += value
    }

    fun incrementCycleCount() {
        _cycleCount.value += 1
    }

    private val _uiState = MutableStateFlow(SSEUIState())
    val uiState: StateFlow<SSEUIState> = _uiState.asStateFlow()

    private var eventSourceHolder: EventSource? = null

    fun startSSEConnection(subUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                 _currentChars.value = ""
                val sseUrl = "$SSEWikiURL?tab=$subUrl"
                Log.d("SSE", "Connecting to SSE URL: $sseUrl")

                eventSourceHolder = EventSource.Builder(
                    createEventHandler(),
                    URI.create(sseUrl)
                )
                    .reconnectTime(3, TimeUnit.SECONDS)
                    .build()
                eventSourceHolder?.start()
            } catch (e: Exception) {
                Log.e("SSE", "Error starting connection: ${e.message}")
            }
        }
    }

    fun closeSSEConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                eventSourceHolder?.close()
                eventSourceHolder = null
            } catch (e: Exception) {
                Log.e("SSE", "Error closing connection: ${e.message}")
            }
        }
    }

    private fun createEventHandler(): EventHandler = object : EventHandler {
        override fun onOpen() {
            viewModelScope.launch {
                Log.d("SSE", "eventHandler onOpen")
                Log.d("SSE", "Connected to: ${eventSourceHolder?.uri}")
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
                // Handle message
                try {
                    val inputUrl = eventSourceHolder?.uri.toString()

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