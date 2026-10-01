package com.example.composesample.presentation.example.component.data.sse

import android.os.SystemClock
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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.SSEConfig
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Wikimedia 스트림은 앱을 식별할 수 없는 User-Agent(기본값인 okhttp/…, Java/… 포함)를 403 으로 거절한다.
 * 연락처(저장소 URL)를 담은 UA 를 직접 보내야 연결된다 — 정책 문서는 같은 폴더 exampleGuide.kt 참고.
 */
private const val SSE_USER_AGENT = "ComposeSample/1.0 (https://github.com/HeeGyeong/ComposeSample)"

class SSEViewModel() : ViewModel() {

    /** 같은 스트림을 받는 두 구현 — 콜백(okhttp-eventsource) vs Flow(Ktor SSE) */
    enum class SSEImpl(val label: String) {
        EVENTSOURCE("okhttp-eventsource (콜백)"),
        KTOR("Ktor SSE (Flow)")
    }

    /**
     * 한 번의 연결에서 잰 값. 스레드 수는 이름으로 묶어 센다 — 연결 중(3번째 이벤트 때)과 종료 1초 뒤.
     * 첫 이벤트까지의 시간은 네트워크에 따라 달라지므로 참고값이다.
     */
    data class RunStats(
        val impl: SSEImpl,
        val firstEventMs: Long? = null,
        val events: Int = 0,
        val firstEventFields: String? = null,
        val threadsBefore: String? = null,
        val threadsDuring: String? = null,
        val threadsAfter: String? = null,
        val afterStopEvents: Int = 0,
        val endedBy: String? = null
    )

    private val _selectedImpl = MutableStateFlow(SSEImpl.EVENTSOURCE)
    val selectedImpl = _selectedImpl.asStateFlow()

    fun selectImpl(impl: SSEImpl) {
        if (!_uiState.value.isConnected) _selectedImpl.value = impl
    }

    private val _stats = MutableStateFlow<Map<SSEImpl, RunStats>>(emptyMap())
    val stats = _stats.asStateFlow()

    /** Ktor SSE 플러그인의 기본값 — 아무 설정도 하지 않은 SSEConfig 를 그대로 읽는다 */
    val ktorDefaults: String = SSEConfig().let {
        "reconnectionTime=${it.reconnectionTime} · maxReconnectionAttempts=${it.maxReconnectionAttempts}"
    }

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

    /**
     * 종료를 결정한 순간 세운다. close() 는 IO 코루틴에서 비동기로 처리되므로 그 사이에도 콜백이 계속 오는데,
     * 참조(eventSourceHolder)가 비워지기 전까지 기다리면 그 메시지가 반영된다(실측: 10개에서 멈췄는데 11개 반영).
     */
    @Volatile
    private var eventSourceStopRequested = false

    fun startSSEConnection(subUrl: String) {
        startStats(SSEImpl.EVENTSOURCE)
        eventSourceStopRequested = false
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
        // Ktor 경로는 코루틴 하나라 Job 을 취소하면 연결이 닫힌다(close() 를 부를 대상이 따로 없다)
        ktorJob?.let { job ->
            if (job.isActive) {
                ktorStopRequested = true
                job.cancel(CancellationException("사용자 종료"))
            }
            return
        }
        eventSourceStopRequested = true
        viewModelScope.launch(Dispatchers.IO) {
            // 10글자 이후 들어오는 메시지마다 종료를 요청하므로, 참조를 먼저 비워 한 번만 닫는다
            val holder = eventSourceHolder ?: return@launch
            eventSourceHolder = null
            updateStats(SSEImpl.EVENTSOURCE) { it.copy(endedBy = it.endedBy ?: "close() 호출") }
            measureThreadsAfter(SSEImpl.EVENTSOURCE)
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

    /**
     * 연결된 채로 화면을 나가면 viewModelScope 가 먼저 취소돼 closeSSEConnection() 이 더는 돌지 않는다.
     * 그러면 스트림·이벤트 스레드가 남아 계속 수신하므로 여기서 직접 닫는다.
     * close() 는 실행기 종료를 최대 1초씩 기다리므로 메인 스레드를 막지 않도록 별도 스레드에서 호출한다.
     */
    override fun onCleared() {
        // Ktor 경로의 Job 은 viewModelScope 와 함께 이미 취소됐다 — 클라이언트(엔진 자원)만 닫는다
        ktorClientOrNull?.close()
        val holder = eventSourceHolder ?: return
        eventSourceHolder = null
        thread(name = "sse-close") {
            try {
                holder.close()
            } catch (e: Exception) {
                Log.e("SSE", "Error closing connection on clear: ${e.message}")
            }
        }
    }

    // ==================== Ktor SSE (Flow) ====================

    private var ktorClientOrNull: HttpClient? = null

    /** okhttp-eventsource 와 같은 OkHttp 위에서 비교하려고 OkHttp 엔진을 쓴다. SSE 설정은 기본값 그대로 */
    private val ktorClient: HttpClient
        get() = ktorClientOrNull ?: HttpClient(OkHttp) { install(SSE) }.also { ktorClientOrNull = it }

    private var ktorJob: Job? = null
    @Volatile
    private var ktorStopRequested = false

    /**
     * 같은 Wikimedia 스트림을 Ktor 로 받는다. 이벤트는 incoming(Flow)으로 오고, 10개를 받으면 블록을 빠져나오며
     * 세션이 닫힌다. 중간에 멈추려면 Job 을 취소한다 — 콜백 쪽처럼 close() 를 부를 대상과 종료 플래그가 따로 없다.
     */
    fun startKtorConnection(subUrl: String) {
        if (ktorJob?.isActive == true) return
        startStats(SSEImpl.KTOR)
        _currentChars.value = ""
        ktorStopRequested = false
        val sseUrl = "$SSEWikiURL?tab=$subUrl"
        ktorJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                ktorClient.sse(
                    urlString = sseUrl,
                    request = { header(HttpHeaders.UserAgent, SSE_USER_AGENT) }
                ) {
                    _uiState.update {
                        it.copy(isConnected = true, messageList = it.messageList + SSEMessage.Connected("연결됨 (Ktor)"))
                    }
                    incoming.take(10).collect { event ->
                        if (ktorStopRequested) {
                            updateStats(SSEImpl.KTOR) { it.copy(afterStopEvents = it.afterStopEvents + 1) }
                            return@collect
                        }
                        recordEvent(SSEImpl.KTOR) {
                            "event=${event.event} · id=${event.id?.take(60)}… · data ${event.data?.length ?: 0}자 · retry=${event.retry}"
                        }
                        appendFirstChar(event.data?.firstOrNull()?.toString().orEmpty(), sseUrl)
                    }
                }
                updateStats(SSEImpl.KTOR) { it.copy(endedBy = it.endedBy ?: "10개 수신 → 블록 종료") }
            } catch (e: CancellationException) {
                updateStats(SSEImpl.KTOR) { it.copy(endedBy = it.endedBy ?: "Job 취소") }
                throw e
            } catch (t: Throwable) {
                _uiState.update { it.copy(messageList = it.messageList + SSEMessage.Error("에러(Ktor): ${t.message}")) }
            } finally {
                _uiState.update {
                    it.copy(isConnected = false, messageList = it.messageList + SSEMessage.Disconnected("연결 종료 (Ktor)"))
                }
                ktorJob = null
                measureThreadsAfter(SSEImpl.KTOR)
            }
        }
    }

    // ==================== 공통 처리 · 측정 ====================

    /** 메시지 첫 글자를 이번 사이클의 수집 카드에 붙인다 — 두 구현이 같은 처리를 쓴다 */
    private fun appendFirstChar(firstChar: String, inputUrl: String) {
        updateCurrentChars(firstChar)
        val updatedList = if (inputUrl.contains("reverseItem=true")) {
            listOf(
                SSEMessage.CollectedChars(chars = currentChars.value, cycleId = cycleCount.value)
            ) + uiState.value.messageList.filterNot {
                it is SSEMessage.CollectedChars && it.cycleId == cycleCount.value
            }
        } else {
            uiState.value.messageList.filterNot {
                it is SSEMessage.CollectedChars && it.cycleId == cycleCount.value
            } + SSEMessage.CollectedChars(chars = currentChars.value, cycleId = cycleCount.value)
        }
        _uiState.update { it.copy(messageList = updatedList) }
    }

    private var runStartedAt = 0L

    private fun startStats(impl: SSEImpl) {
        runStartedAt = SystemClock.elapsedRealtime()
        _stats.update { it + (impl to RunStats(impl = impl, threadsBefore = threadSnapshot())) }
    }

    /** 이벤트 하나를 센다 — 첫 이벤트는 시간·필드를, 세 번째는 그때의 스레드 수를 남긴다 */
    private fun recordEvent(impl: SSEImpl, fields: () -> String) {
        updateStats(impl) { current ->
            val count = current.events + 1
            current.copy(
                events = count,
                firstEventMs = current.firstEventMs ?: (SystemClock.elapsedRealtime() - runStartedAt),
                firstEventFields = current.firstEventFields ?: fields(),
                threadsDuring = if (count == 3) threadSnapshot() else current.threadsDuring
            )
        }
    }

    private fun updateStats(impl: SSEImpl, transform: (RunStats) -> RunStats) {
        _stats.update { map -> map[impl]?.let { map + (impl to transform(it)) } ?: map }
    }

    /** 종료 1초 뒤 스레드 수 — 연결에 쓰던 스레드가 정리됐는지 본다 */
    private fun measureThreadsAfter(impl: SSEImpl) {
        viewModelScope.launch(Dispatchers.Default) {
            delay(1_000)
            updateStats(impl) { it.copy(threadsAfter = threadSnapshot()) }
        }
    }

    /** 이름으로 묶은 스레드 수 — eventsource 는 라이브러리 실행기, OkHttp 는 호출 스레드 */
    private fun threadSnapshot(): String {
        val names = Thread.getAllStackTraces().keys.map { it.name }
        val eventSource = names.count { it.contains("eventsource", ignoreCase = true) }
        val okHttp = names.count { it.startsWith("OkHttp") }
        val ktor = names.count { it.contains("ktor", ignoreCase = true) }
        return "eventsource $eventSource · OkHttp $okHttp · ktor $ktor"
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
                if (eventSourceHolder == null || eventSourceStopRequested) {
                    updateStats(SSEImpl.EVENTSOURCE) { it.copy(afterStopEvents = it.afterStopEvents + 1) }
                    return@launch
                }
                recordEvent(SSEImpl.EVENTSOURCE) {
                    "event=${messageEvent.eventName} · lastEventId=${messageEvent.lastEventId?.take(60)}… · data ${messageEvent.data.length}자"
                }
                // Handle message
                try {
                    val inputUrl = eventSourceHolder?.eventSource?.origin.toString()

                    // 메시지에서 첫 글자만 추출해 이번 사이클의 수집 카드에 붙인다
                    val firstChar = messageEvent.data.firstOrNull()?.toString() ?: ""
                    appendFirstChar(firstChar, inputUrl)

                    // 10개 글자를 수집하면 연결 종료
                    if (currentChars.value.length >= 10) {
                        updateStats(SSEImpl.EVENTSOURCE) { it.copy(endedBy = it.endedBy ?: "10개 수신 → close() 호출") }
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