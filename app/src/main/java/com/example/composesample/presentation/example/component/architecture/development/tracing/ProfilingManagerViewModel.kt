package com.example.composesample.presentation.example.component.architecture.development.tracing

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.os.Debug
import android.os.ProfilingResult
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.StackSamplingRequestBuilder
import androidx.core.os.registerForAllProfilingResults
import androidx.core.os.requestProfiling
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

enum class LegacyTraceMode(val label: String) {
    NONE("추적 없이"),
    SAMPLING("startMethodTracingSampling (1ms 샘플링)"),
    INSTRUMENT("startMethodTracing (모든 호출 기록)"),
}

/** 레거시(앱 프로세스 안) 추적 한 번 — 같은 부하를 메인 스레드에서 돌린 시간 · 추적하는 동안 늘어난 스레드 · 파일 */
data class LegacyTraceRun(
    val mode: LegacyTraceMode,
    val workloadMs: Long,
    val addedThreads: List<String>,
    val fileName: String?,
    val fileBytes: Long,
) {
    /** 버퍼가 가득 차면 그 뒤 호출은 기록되지 않는다(파일 크기가 버퍼 크기에 닿음) */
    val bufferFull: Boolean get() = fileBytes >= LEGACY_BUFFER_BYTES
}

/** ProfilingResult 를 화면용 값으로 옮긴 것 — API 35 타입을 버전 분기 밖으로 내보내지 않는다 */
data class ProfilingOutcome(
    val tag: String,
    val errorCode: Int,
    val errorMessage: String?,
    val filePath: String?,
    val fileBytes: Long,
    /** 요청부터 콜백까지(전역 수신기로 받은 것은 -1) */
    val elapsedMs: Long,
)

data class StackSamplingRun(
    val outcome: ProfilingOutcome,
    val workloadMs: Long,
    val addedThreads: List<String>,
    val summary: StackSampleSummary?,
    val threadNames: Map<Int, String?>,
)

/**
 * 같은 부하(큰 JSON 을 Gson 으로 메인 스레드에서 파싱)를 두 방식으로 기록한다.
 * - 레거시: Debug.startMethodTracing* — 앱 프로세스 안의 ART 추적기가 기록하고 앱이 파일을 쓴다
 * - ProfilingManager(API 35+): 시스템의 Perfetto(traced_perf)가 기록하고, 끝나면 앱 files/profiling 에 결과를 넘겨준다
 * Application 컨텍스트는 filesDir 와 ProfilingManager 시스템 서비스 조회에 쓴다.
 */
class ProfilingManagerViewModel(application: Application) : AndroidViewModel(application) {

    val apiLevel: Int = Build.VERSION.SDK_INT
    val supported: Boolean = apiLevel >= Build.VERSION_CODES.VANILLA_ICE_CREAM

    private val _running = MutableStateFlow<String?>(null)
    val running: StateFlow<String?> = _running.asStateFlow()
    private val _legacy = MutableStateFlow<List<LegacyTraceRun>>(emptyList())
    val legacy: StateFlow<List<LegacyTraceRun>> = _legacy.asStateFlow()
    private val _sampling = MutableStateFlow<StackSamplingRun?>(null)
    val sampling: StateFlow<StackSamplingRun?> = _sampling.asStateFlow()
    private val _burst = MutableStateFlow<List<ProfilingOutcome>>(emptyList())
    val burst: StateFlow<List<ProfilingOutcome>> = _burst.asStateFlow()
    private val _history = MutableStateFlow<List<ProfilingOutcome>>(emptyList())
    val history: StateFlow<List<ProfilingOutcome>> = _history.asStateFlow()
    private val _resultFiles = MutableStateFlow(countResultFiles())
    val resultFiles: StateFlow<Pair<Int, Long>> = _resultFiles.asStateFlow()

    private val json: String by lazy { buildUsersJson(PROFILE_USERS) }
    private val legacyJson: String by lazy { buildUsersJson(LEGACY_USERS) }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // 전역 수신기 — 이 앱 프로세스로 오는 결과를 모두 받는다(요청한 것 · 트리거 · 앱이 죽어 못 받았던 것)
            viewModelScope.launch {
                ProfilingApi35.allResults(application).collect { outcome ->
                    _history.value = listOf(outcome) + _history.value
                    _resultFiles.value = countResultFiles()
                }
            }
        }
    }

    fun runLegacy(mode: LegacyTraceMode) = launchExclusive(mode.label) {
        val app = getApplication<Application>()
        val dir = File(app.filesDir, LEGACY_DIR).apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "legacy_${mode.name.lowercase()}.trace")
        val text = legacyJson
        withContext(Dispatchers.Default) { parseUsersWithGson(text) }
        val before = currentThreadNames()
        when (mode) {
            LegacyTraceMode.NONE -> Unit
            LegacyTraceMode.SAMPLING -> Debug.startMethodTracingSampling(file.absolutePath, LEGACY_BUFFER_BYTES, 1_000)
            LegacyTraceMode.INSTRUMENT -> Debug.startMethodTracing(file.absolutePath, LEGACY_BUFFER_BYTES)
        }
        // 추적 스레드는 만들어진 뒤에 이름이 붙는다 — 바로 읽으면 프로세스 이름(e.composesample)으로 보였다(API 30 에뮬)
        delay(THREAD_NAME_SETTLE_MS)
        val during = currentThreadNames()
        val ms = try {
            backgroundWorkload(text)
        } finally {
            if (mode != LegacyTraceMode.NONE) Debug.stopMethodTracing()
        }
        val run = LegacyTraceRun(
            mode = mode,
            workloadMs = ms,
            addedThreads = addedThreads(before, during),
            fileName = if (mode == LegacyTraceMode.NONE) null else file.name,
            fileBytes = if (mode == LegacyTraceMode.NONE) 0 else file.length(),
        )
        _legacy.value = _legacy.value.filter { it.mode != mode } + run
    }

    fun requestStackSampling() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        launchExclusive("스택 샘플링") { stackSampling() }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun stackSampling() = coroutineScope {
        val app = getApplication<Application>()
        val warm = json
        withContext(Dispatchers.Main) { parseUsersWithGson(warm) }
        val before = currentThreadNames()
        val pending = async { ProfilingApi35.requestStackSampling(app, "cs172") }
        // 기록이 시작됐다는 신호는 없다 — 공식 가이드처럼 잠깐 기다린 뒤 부하를 올린다
        delay(START_PADDING_MS)
        val during = currentThreadNames()
        val ms = mainThreadWorkload()
        val outcome = pending.await()
        val summary = outcome.filePath?.let { path ->
            withContext(Dispatchers.Default) { parseStackSampleFile(File(path).readBytes(), Process.myPid()) }
        }
        val names = summary?.samplesByTid?.associate { (tid, _) -> tid to threadNameOf(tid) } ?: emptyMap()
        _sampling.value = StackSamplingRun(outcome, ms, addedThreads(before, during), summary, names)
        _resultFiles.value = countResultFiles()
    }

    /** 곧바로 두 번 요청 — 두 번째가 어떤 오류 코드로 거절되는지 */
    fun requestTwice() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        launchExclusive("연달아 두 번") { burst() }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun burst() = coroutineScope {
        val app = getApplication<Application>()
        val first = async { ProfilingApi35.requestStackSampling(app, "burst1", durationMs = 2_000) }
        val second = async { ProfilingApi35.requestStackSampling(app, "burst2", durationMs = 2_000) }
        _burst.value = listOf(first.await(), second.await())
        _resultFiles.value = countResultFiles()
    }

    fun deleteResultFiles() {
        File(getApplication<Application>().filesDir, PROFILING_DIR).listFiles()?.forEach { it.delete() }
        _resultFiles.value = countResultFiles()
    }

    private fun launchExclusive(label: String, block: suspend () -> Unit) {
        if (_running.value != null) return
        _running.value = label
        viewModelScope.launch {
            try {
                block()
            } finally {
                _running.value = null
            }
        }
    }

    /** 레거시 카드 부하 — 추적은 프로세스 전체에 걸리므로 어느 스레드에서 돌려도 되고, 화면이 멈추지 않게 백그라운드에서 */
    private suspend fun backgroundWorkload(text: String): Long = withContext(Dispatchers.Default) {
        val start = System.nanoTime()
        repeat(LEGACY_PARSE_ROUNDS) { parseUsersWithGson(text) }
        (System.nanoTime() - start) / 1_000_000
    }

    /** 원문 사례 그대로 — withContext(Dispatchers.IO) 없이 메인 스레드에서 파싱한다(그동안 화면이 멈춘다) */
    private suspend fun mainThreadWorkload(): Long {
        val text = json
        return withContext(Dispatchers.Main) {
            val start = System.nanoTime()
            repeat(PROFILE_PARSE_ROUNDS) { parseUsersWithGson(text) }
            (System.nanoTime() - start) / 1_000_000
        }
    }

    private fun countResultFiles(): Pair<Int, Long> {
        val files = File(getApplication<Application>().filesDir, PROFILING_DIR).listFiles() ?: emptyArray()
        return files.size to files.sumOf { it.length() }
    }

    private companion object {
        const val LEGACY_DIR = "legacy_traces"
        /** ProfilingManager 가 결과를 넘겨주는 앱 디렉터리(files/profiling) */
        const val PROFILING_DIR = "profiling"
        const val START_PADDING_MS = 300L
        const val THREAD_NAME_SETTLE_MS = 100L
    }
}

/** API 35 타입(ProfilingResult · 요청 빌더)은 이 객체 안에서만 다룬다 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
private object ProfilingApi35 {

    suspend fun requestStackSampling(
        context: Context,
        tag: String,
        durationMs: Int = STACK_SAMPLING_DURATION_MS,
    ): ProfilingOutcome = suspendCancellableCoroutine { cont ->
        val signal = CancellationSignal()
        val request = StackSamplingRequestBuilder()
            .setDurationMs(durationMs)
            .setSamplingFrequencyHz(STACK_SAMPLING_FREQUENCY_HZ)
            .setTag(tag)
            .setCancellationSignal(signal)
            .build()
        val start = System.nanoTime()
        // 콜백 실행기로 새 스레드를 만들지 않는다 — 앱 프로세스 스레드 수 비교가 흐려진다
        requestProfiling(context, request, ContextCompat.getMainExecutor(context)) { result ->
            if (cont.isActive) cont.resume(result.toOutcome((System.nanoTime() - start) / 1_000_000))
        }
        cont.invokeOnCancellation { signal.cancel() }
    }

    fun allResults(context: Context): Flow<ProfilingOutcome> =
        registerForAllProfilingResults(context).map { it.toOutcome(-1) }

    private fun ProfilingResult.toOutcome(elapsedMs: Long) = ProfilingOutcome(
        tag = tag.orEmpty(),
        errorCode = errorCode,
        errorMessage = errorMessage,
        filePath = resultFilePath,
        fileBytes = resultFilePath?.let { File(it).length() } ?: 0,
        elapsedMs = elapsedMs,
    )
}
