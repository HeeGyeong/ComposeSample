package com.example.composesample.presentation.example.component.architecture.development.tracing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.tracing.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class TraceScenario(val label: String, val prefix: String) {
    COROUTINE("① traceCoroutine 8개", "coroutine"),
    SYNC("② 동기 구간을 delay 너머로", "sync"),
    FLOW("③ 흐름 연결 4가지", "flow"),
}

data class TraceScenarioResult(
    val scenario: TraceScenario,
    val lines: List<String>,
    val fileName: String,
    val fileBytes: Long,
    val packets: Int,
)

/** 전역 Tracer 와 라이브러리 기본 초기화가 남기는 파일 — 매니페스트에서 초기화를 뺐는지 화면에서 확인한다 */
data class GlobalTracerState(val isStub: Boolean, val profilerTraceFiles: Int)

/**
 * tracing 2.0 시나리오를 실행할 때마다 새 트레이스 파일 하나를 만들고(이전 파일은 지운다), 닫은 뒤 그 파일을 다시 읽어 분석한다.
 * Application 컨텍스트는 TraceDriver(프로세스 이름)와 filesDir·noBackupFilesDir 에 쓴다.
 */
class CoroutineTracingViewModel(application: Application) : AndroidViewModel(application) {

    private val _result = MutableStateFlow<TraceScenarioResult?>(null)
    val result: StateFlow<TraceScenarioResult?> = _result.asStateFlow()
    private val _running = MutableStateFlow(false)
    val running: StateFlow<Boolean> = _running.asStateFlow()
    private val _globalState = MutableStateFlow(readGlobalState())
    val globalState: StateFlow<GlobalTracerState> = _globalState.asStateFlow()

    fun run(scenario: TraceScenario) {
        if (_running.value) return
        _running.value = true
        viewModelScope.launch {
            try {
                _result.value = withContext(Dispatchers.Default) { runScenario(scenario) }
                _globalState.value = readGlobalState()
            } finally {
                _running.value = false
            }
        }
    }

    private suspend fun runScenario(scenario: TraceScenario): TraceScenarioResult {
        val app = getApplication<Application>()
        File(app.filesDir, TRACE_DIR).listFiles()?.forEach { it.delete() }
        val session = InProcessTraceSession(app, scenario.prefix)
        var coroutineRuns: List<CoroutineRun> = emptyList()
        var syncRuns: List<SyncRun> = emptyList()
        try {
            when (scenario) {
                TraceScenario.COROUTINE -> coroutineRuns = runCoroutineSlices(session.tracer)
                TraceScenario.SYNC -> syncRuns = runSyncAcrossSuspend(session.tracer)
                TraceScenario.FLOW -> runFlowCases(session.tracer)
            }
        } finally {
            // 큐를 비우고 파일을 닫아야 끝까지 읽힌다
            session.close()
        }
        val content = parseTraceFile(session.file.readBytes())
        val lines = when (scenario) {
            TraceScenario.COROUTINE -> coroutineSliceLines(coroutineRuns, content)
            TraceScenario.SYNC -> syncLines(syncRuns, content)
            TraceScenario.FLOW -> flowCaseLines(content)
        }
        return TraceScenarioResult(scenario, lines, session.file.name, session.file.length(), content.packets)
    }

    private fun readGlobalState(): GlobalTracerState {
        val app = getApplication<Application>()
        val files = File(app.noBackupFilesDir, "perfetto_traces").listFiles()?.size ?: 0
        return GlobalTracerState(isStub = Tracer.global === Tracer.getStubTracer(), profilerTraceFiles = files)
    }
}
