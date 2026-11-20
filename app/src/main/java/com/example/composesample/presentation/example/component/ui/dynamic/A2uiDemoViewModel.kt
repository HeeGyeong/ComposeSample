package com.example.composesample.presentation.example.component.ui.dynamic

import android.app.Application
import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.a2ui.model.processor.A2uiMessageProcessor
import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.a2ui.model.protocol.A2uiClientErrorMessage
import androidx.a2ui.model.protocol.A2uiClientEventMessage
import androidx.a2ui.model.protocol.A2uiException
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.material3.a2ui.catalog.materialA2uiBasicCatalogV1
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 화면 기록 한 줄 */
internal data class A2uiLogLine(val kind: Kind, val text: String) {
    enum class Kind(val label: String) {
        Sent("보냄"),
        Rejected("거절"),
        Event("이벤트 →"),
        Reply("← 답"),
        Error("오류")
    }
}

/**
 * A2UI 메시지 처리기를 들고 있는 ViewModel(#120) — 공식 안내대로 처리기는 ViewModel 에, 처리 루프(collectMessages)는
 * 백그라운드 디스패처에서 돈다. 에이전트는 [A2uiDemoScript] 의 대본으로 흉내 내며 네트워크 호출은 없다.
 *
 * 파싱과 처리 루프:
 * - 파싱은 processInput 대신 parse → processMessage 를 직접 부른다 — 파서는 모든 예외를 A2uiValidationException 으로 감싸고,
 *   processInput 은 그것을 A2uiClientErrorMessage 로 바꿔 outboundEvents(에이전트 쪽)로 보낸다. 이 예제는 거절을 화면 기록에
 *   바로 남기려고 직접 잡는다(1.0.0-alpha02 소스 — 직접 잡는 쪽도 A2uiException 밖의 예외까지 기록만 하고 넘긴다).
 * - 처리 루프의 예외를 잡는다 — 엔진의 surface 처리는 A2uiException 만 잡고, 나머지는 collectMessages 를 끝낸다.
 *   잡지 않으면 viewModelScope 에서 앱이 죽는다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class A2uiDemoViewModel(application: Application) : ViewModel() {

    // 기본 카탈로그(Material 3) — 미디어 렌더러는 자리 표시만, 함수(openUrl 등)는 기본 구현
    private val catalog = materialA2uiBasicCatalogV1(
        image = MaterialA2uiBasicCatalogV1Defaults.image(PlaceholderImageRenderer),
        video = MaterialA2uiBasicCatalogV1Defaults.video(PlaceholderVideoRenderer),
        audioPlayer = MaterialA2uiBasicCatalogV1Defaults.audioPlayer(PlaceholderAudioRenderer),
        functions = A2uiBasicCatalogV1.Functions(application)
    )
    private val parser = A2uiMessageParser()

    private val processorFlow = MutableStateFlow(A2uiMessageProcessor(listOf(catalog)))
    private var loopJob: Job? = null
    private var outboundJob: Job? = null

    /** 처리기가 지금 들고 있는 surface 들 — 처리기를 새로 만들면 새 처리기의 것으로 바뀐다 */
    val surfaces: StateFlow<List<A2uiSurfaceModel>> = processorFlow
        .flatMapLatest { it.activeSurfaces }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _log = MutableStateFlow<List<A2uiLogLine>>(emptyList())
    val log: StateFlow<List<A2uiLogLine>> = _log.asStateFlow()

    private val _nextStep = MutableStateFlow(0)
    val nextStep: StateFlow<Int> = _nextStep.asStateFlow()

    /** 처리 루프가 예외로 멈췄으면 그 내용 — '처음부터' 로 새 처리기를 만든다 */
    private val _loopStopped = MutableStateFlow<String?>(null)
    val loopStopped: StateFlow<String?> = _loopStopped.asStateFlow()

    /** 마지막으로 보낸(또는 거절된) JSON — 화면 아래에 그대로 보인다 */
    private val _lastJson = MutableStateFlow("")
    val lastJson: StateFlow<String> = _lastJson.asStateFlow()

    init {
        start(processorFlow.value)
    }

    fun sendNextStep() {
        val index = _nextStep.value
        val step = A2uiDemoScript.steps.getOrNull(index) ?: return
        if (send(step.json, step.label)) _nextStep.value = index + 1
    }

    fun sendRemainingSteps() {
        while (_nextStep.value < A2uiDemoScript.steps.size) {
            val before = _nextStep.value
            sendNextStep()
            if (_nextStep.value == before) break
        }
    }

    fun sendBadCase(case: A2uiDemoScript.BadCase) {
        send(case.json, "틀린 메시지 — ${case.label}")
    }

    /** 처리기를 새로 만든다 — 옛 루프를 끝내면 엔진이 그 처리기의 surface 를 정리한다 */
    fun reset() {
        loopJob?.cancel()
        outboundJob?.cancel()
        _log.value = emptyList()
        _nextStep.value = 0
        _loopStopped.value = null
        _lastJson.value = ""
        val fresh = A2uiMessageProcessor(listOf(catalog))
        processorFlow.value = fresh
        start(fresh)
    }

    /** 파싱해 큐에 넣는다. 거절되면 기록만 하고 false */
    private fun send(json: String, label: String): Boolean {
        _lastJson.value = json
        return try {
            processorFlow.value.processMessage(parser.parse(json))
            append(A2uiLogLine.Kind.Sent, label)
            true
        } catch (e: A2uiException) {
            append(A2uiLogLine.Kind.Rejected, "$label — 파서가 거절(${e.code}): ${e.message}")
            false
        } catch (e: RuntimeException) {
            append(A2uiLogLine.Kind.Rejected, "$label — 예상 밖 예외 ${e::class.java.simpleName}: ${e.message}")
            false
        }
    }

    private fun start(processor: A2uiMessageProcessor) {
        loopJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                processor.collectMessages()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val reason = "${e::class.java.simpleName}: ${e.message}"
                _loopStopped.value = reason
                append(A2uiLogLine.Kind.Error, "처리 루프가 멈췄다 — $reason ('처음부터' 로 새 처리기)")
            }
        }
        outboundJob = viewModelScope.launch {
            processor.outboundEvents.collect { message ->
                when (message) {
                    is A2uiClientEventMessage -> onEvent(message)
                    is A2uiClientErrorMessage ->
                        append(A2uiLogLine.Kind.Error, "클라이언트 오류 ${message.code} (${message.surfaceId}): ${message.message}")
                }
            }
        }
    }

    /** 가짜 에이전트 — 주문 이벤트에 상태 줄을 바꾸는 updateDataModel 로 답한다(실제 에이전트의 왕복 지연 흉내 0.4초) */
    private suspend fun onEvent(event: A2uiClientEventMessage) {
        append(A2uiLogLine.Kind.Event, "${event.type} (컴포넌트 ${event.componentId}) 문맥 ${event.context}")
        if (event.type != A2uiDemoScript.ORDER_EVENT) return
        delay(REPLY_DELAY_MILLIS)
        val reply = A2uiDemoScript.orderReply(event.context)
        if (send(reply, "에이전트 답 — /status 갱신")) {
            append(A2uiLogLine.Kind.Reply, "updateDataModel /status")
        }
    }

    private fun append(kind: A2uiLogLine.Kind, text: String) {
        _log.update { (it + A2uiLogLine(kind, text)).takeLast(MAX_LOG) }
    }

    private companion object {
        const val REPLY_DELAY_MILLIS = 400L
        const val MAX_LOG = 14
    }
}
