package com.example.composesample.presentation.example.component.data.download

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 이어받기·분할 다운로드 예제의 ViewModel — 서버(MockEngine)와 다운로더를 함께 가진다.
 * 수동 다운로드(2번 카드)와 시나리오 표(3·4번 카드)는 서버·클라이언트·폴더를 따로 써서 서로의 파일·서버 상태를 건드리지 않는다.
 * 파일은 cacheDir/range-download 아래 — 일시정지하면 .part·.state 가 남아 프로세스가 죽어도 이어받을 수 있다.
 */
class RangeDownloadViewModel(application: Application) : AndroidViewModel(application) {

    data class ServerUi(val version: Int, val etag: String, val rangeSupported: Boolean, val headAllowed: Boolean, val dropKiB: Int)

    data class RunResult(
        val sizeLine: String,
        val versionLine: String,
        val elapsedMs: Long,
        val kibPerSecond: Long,
        val restarts: Int,
        val retries: Int,
        val receivedKiB: Long
    )

    val server = FakeFileServer()
    private val client = HttpClient(server.engine())
    private val dir = File(application.cacheDir, "range-download").apply { mkdirs() }
    private val downloader = RangeDownloader(client, dir, log = ::log, onProgress = { _segments.value = it })

    private val scenarioServer = FakeFileServer()
    private val scenarioClient = HttpClient(scenarioServer.engine())
    private val scenarioDownloader = RangeDownloader(scenarioClient, File(dir, "scenario").apply { mkdirs() })

    private val _serverUi = MutableStateFlow(serverUi())
    val serverUi: StateFlow<ServerUi> = _serverUi.asStateFlow()

    private val _hits = MutableStateFlow<List<ServerHit>>(emptyList())
    val hits: StateFlow<List<ServerHit>> = _hits.asStateFlow()

    private val _resolve = MutableStateFlow<ResolveResult?>(null)
    val resolve: StateFlow<ResolveResult?> = _resolve.asStateFlow()

    private val _options = MutableStateFlow(ClientOptions())
    val options: StateFlow<ClientOptions> = _options.asStateFlow()

    private val _segments = MutableStateFlow<List<SegmentState>>(emptyList())
    val segments: StateFlow<List<SegmentState>> = _segments.asStateFlow()

    private val _status = MutableStateFlow("대기")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _result = MutableStateFlow<RunResult?>(null)
    val result: StateFlow<RunResult?> = _result.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _files = MutableStateFlow<List<String>>(emptyList())
    val files: StateFlow<List<String>> = _files.asStateFlow()

    private val _stateJson = MutableStateFlow<String?>(null)
    val stateJson: StateFlow<String?> = _stateJson.asStateFlow()

    private val _scenarioRows = MutableStateFlow<List<ScenarioResult>>(emptyList())
    val scenarioRows: StateFlow<List<ScenarioResult>> = _scenarioRows.asStateFlow()

    private val _trapRows = MutableStateFlow<List<ScenarioResult>>(emptyList())
    val trapRows: StateFlow<List<ScenarioResult>> = _trapRows.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private var downloadJob: Job? = null

    init {
        server.onHit = { hit -> _hits.update { (listOf(hit) + it).take(12) } }
        // 이전 실행이 남긴 .part·.state 가 있으면 그대로 보여 준다(프로세스가 죽은 뒤 이어받기)
        viewModelScope.launch(Dispatchers.IO) {
            downloader.loadState()?.let {
                _segments.value = it.segments
                _status.value = "저장된 상태 있음(${it.doneBytes * 100 / it.size}%) — 재개 가능"
            }
        }
        refreshFiles()
    }

    private fun serverUi() = ServerUi(server.version, server.etag, server.rangeSupported, server.headAllowed, server.dropAfterBytes / 1024)

    private fun log(line: String) {
        _logs.update { (listOf(line) + it).take(12) }
    }

    // ==================== 서버 스위치 ====================

    fun setRangeSupported(enabled: Boolean) {
        server.rangeSupported = enabled
        _serverUi.value = serverUi()
    }

    fun setHeadAllowed(enabled: Boolean) {
        server.headAllowed = enabled
        _serverUi.value = serverUi()
    }

    fun setDrop(enabled: Boolean) {
        server.dropAfterBytes = if (enabled) 300 * 1024 else 0
        _serverUi.value = serverUi()
    }

    fun replaceServerFile() {
        server.replaceFile()
        _serverUi.value = serverUi()
        log("서버 파일 교체 → ETag ${server.etag}")
    }

    // ==================== 클라이언트 옵션 ====================

    fun setConnections(count: Int) = _options.update { it.copy(connections = count) }

    fun setSpeed(bytesPerSecond: Long) = _options.update { it.copy(bytesPerSecond = bytesPerSecond) }

    fun setResumeCheck(check: ResumeCheck) = _options.update { it.copy(resumeCheck = check) }

    // ==================== 수동 다운로드 ====================

    fun resolveNow() {
        viewModelScope.launch {
            guardedIo { _resolve.value = downloader.resolve(RANGE_FILE_URL) }
        }
    }

    fun start() = launchDownload("받는 중") { downloader.start(RANGE_FILE_URL, _options.value) }

    fun resume() = launchDownload("이어받는 중") { downloader.resume(_options.value) }

    /** 일시정지: 받던 청크까지 쓰고 멈춘다 — .part 와 .state 가 남는다 */
    fun pause() = downloader.requestPause()

    /** 취소: 멈춘 뒤 .part·.state 를 지운다 — 이어받을 수 없다 */
    fun cancel() {
        viewModelScope.launch {
            downloader.requestPause()
            downloadJob?.join()
            withContext(Dispatchers.IO) { downloader.clearPartial() }
            _segments.value = emptyList()
            _status.value = "취소 — 부분 파일·상태 파일 삭제"
            refreshFiles()
        }
    }

    private fun launchDownload(label: String, block: suspend () -> RunOutcome) {
        if (downloadJob?.isActive == true) return
        _result.value = null
        _status.value = label
        val startedAt = SystemClock.elapsedRealtime()
        downloadJob = viewModelScope.launch {
            refreshFiles()
            var result: RunOutcome = RunOutcome.Failed("오류")
            guardedIo { result = block() }
            val outcome = result
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            _status.value = when (outcome) {
                is RunOutcome.Completed -> "완료"
                is RunOutcome.Paused -> "일시정지 — ${outcome.state.doneBytes * 100 / outcome.state.size}%"
                is RunOutcome.Failed -> "실패: ${outcome.reason}"
            }
            if (outcome is RunOutcome.Completed) {
                val version = withContext(Dispatchers.IO) { (1..server.version).firstOrNull { server.crcOfVersion(it) == outcome.crc } }
                _result.value = RunResult(
                    sizeLine = "${outcome.size} B (서버 ${RANGE_FILE_SIZE} B)",
                    versionLine = version?.let { "CRC 가 v$it 내용과 일치" + if (it == server.version) "" else " — 서버는 지금 v${server.version}" } ?: "어느 버전과도 CRC 불일치 — 손상",
                    elapsedMs = elapsed,
                    kibPerSecond = if (elapsed > 0) outcome.receivedBytes * 1000 / elapsed / 1024 else 0,
                    restarts = outcome.restarts,
                    retries = outcome.retries,
                    receivedKiB = outcome.receivedBytes / 1024
                )
            }
            refreshFiles()
        }
    }

    /** IO 에서 실행하고 오류는 기록만 — 취소는 다시 던진다. 제네릭으로 두면 HotSwan 이 핫 리로드 불가로 건너뛴다 */
    private suspend fun guardedIo(block: suspend () -> Unit) {
        try {
            withContext(Dispatchers.IO) { block() }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            log("오류: ${e.javaClass.simpleName} ${e.message}")
        }
    }

    fun refreshFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _files.value = dir.listFiles().orEmpty().filter { it.isFile }.sortedBy { it.name }.map { "${it.name} · ${it.length()} B" }
            _stateJson.value = downloader.stateJson()
        }
    }

    // ==================== 시나리오 ====================

    fun runScenarioTable() = runBusy {
        val rows = mutableListOf<ScenarioResult>()
        for (check in ResumeCheck.entries) for (timing in ReplaceTiming.entries) {
            rows += runResumeScenario(scenarioServer, scenarioDownloader, check, timing)
            _scenarioRows.value = rows.toList()
        }
    }

    fun runTraps() = runBusy {
        val rows = mutableListOf<ScenarioResult>()
        for (trap in Trap.entries) for (careful in listOf(false, true)) {
            rows += runTrap(scenarioServer, scenarioDownloader, trap, careful)
            _trapRows.value = rows.toList()
        }
    }

    private fun runBusy(block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true
        viewModelScope.launch {
            guardedIo { block() }
            withContext(Dispatchers.IO) { scenarioDownloader.clearAll() }
            _busy.value = false
        }
    }

    override fun onCleared() {
        downloader.requestPause()
        client.close()
        scenarioClient.close()
    }
}
