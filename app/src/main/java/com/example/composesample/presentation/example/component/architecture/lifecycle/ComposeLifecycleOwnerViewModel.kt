package com.example.composesample.presentation.example.component.architecture.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Composable 범위 LifecycleOwner 예제의 상태 보관소.
 *
 * - 페이지별 누적 틱: "보이지 않는 페이지에서 작업이 도는가"를 시간이 아니라 **센 값**으로 보여준다
 * - 이벤트 로그: 페이지/자식 owner 가 받은 Lifecycle 이벤트를 순서대로 쌓는다
 *
 * 페이지가 화면 밖으로 멀어져 컴포지션에서 빠져도 누적값이 남아야 비교가 되므로 UI 가 아닌 여기에 둔다.
 */
class ComposeLifecycleOwnerViewModel : ViewModel() {

    data class LogEntry(val seq: Int, val source: String, val event: String)

    private val _ticks = MutableStateFlow(List(PAGE_COUNT) { 0 })
    val ticks: StateFlow<List<Int>> = _ticks.asStateFlow()

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    private var seq = 0

    fun tick(page: Int) {
        _ticks.update { current -> current.mapIndexed { index, value -> if (index == page) value + 1 else value } }
    }

    fun record(source: String, event: Lifecycle.Event) = record(source, event.name)

    fun record(source: String, event: String) {
        seq += 1
        val entry = LogEntry(seq, source, event)
        // 최신 항목이 위로 오도록 앞에 붙이고, 오래된 항목은 잘라낸다
        _logs.update { (listOf(entry) + it).take(MAX_LOGS) }
    }

    fun resetTicks() {
        _ticks.value = List(PAGE_COUNT) { 0 }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    companion object {
        const val PAGE_COUNT = 4
        private const val MAX_LOGS = 40
    }
}
