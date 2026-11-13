package com.example.composesample.presentation.example.component.architecture.development.tracing

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.os.Debug
import android.os.Handler
import android.os.Looper
import android.os.ProfilingResult
import android.os.Process
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.JavaHeapDumpRequestBuilder
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

/** ProfilingManager Java 힙 덤프 한 번 */
data class HeapDumpRun(
    val outcome: ProfilingOutcome,
    val gcFirst: Boolean,
    /** 덤프를 요청한 순간 레지스트리에 있던 리스너 수 — 앱이 아는 값 */
    val registryCount: Int,
    /** 요청 직전 0.3초 동안 메인 스레드의 가장 긴 간격(평소 값) */
    val idleStallMs: Long,
    /** 요청부터 결과까지 메인 스레드의 가장 긴 간격 */
    val dumpStallMs: Long,
    val summary: HeapDumpSummary?,
    val parseMs: Long,
)

/** 앱 안 Debug.dumpHprofData 한 번 — 파일은 크기만 재고 바로 지운다 */
data class HprofRun(
    val registryCount: Int,
    val durationMs: Long,
    val idleStallMs: Long,
    val dumpStallMs: Long,
    val fileBytes: Long,
    val error: String?,
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
    private val _heapDump = MutableStateFlow<HeapDumpRun?>(null)
    val heapDump: StateFlow<HeapDumpRun?> = _heapDump.asStateFlow()
    private val _hprof = MutableStateFlow<HprofRun?>(null)
    val hprof: StateFlow<HprofRun?> = _hprof.asStateFlow()
    private val _registryCount = MutableStateFlow(ScreenListenerRegistry.size)
    val registryCount: StateFlow<Int> = _registryCount.asStateFlow()
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

    /** 레지스트리는 앱 전체 객체라 화면이 바뀌어도 남는다 — 화면이 열고 닫은 뒤 다시 읽는다 */
    fun refreshRegistry() {
        _registryCount.value = ScreenListenerRegistry.size
    }

    fun clearRegistry() {
        ScreenListenerRegistry.clear()
        refreshRegistry()
    }

    fun requestHeapDump(gcFirst: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        launchExclusive("힙 덤프") { heapDump(gcFirst) }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun heapDump(gcFirst: Boolean) {
        val app = getApplication<Application>()
        // 덤프에는 아직 수거되지 않은 객체도 들어간다 — 버린 리스너를 세지 않으려면 먼저 GC 를 요청한다(보장은 아니다)
        if (gcFirst) withContext(Dispatchers.Default) { Runtime.getRuntime().gc() }
        val registry = ScreenListenerRegistry.size
        val meter = MainThreadStallMeter()
        meter.start()
        delay(STALL_IDLE_WINDOW_MS)
        val idle = meter.restart()
        val outcome = try {
            ProfilingApi35.requestJavaHeapDump(app, "cs174")
        } finally {
            meter.stop()
        }
        val start = SystemClock.uptimeMillis()
        val summary = outcome.filePath?.let { path ->
            withContext(Dispatchers.Default) { parseJavaHeapDump(File(path).readBytes(), setOf(LEAKED_LISTENER_CLASS)) }
        }
        _heapDump.value = HeapDumpRun(
            outcome = outcome,
            gcFirst = gcFirst,
            registryCount = registry,
            idleStallMs = idle,
            dumpStallMs = meter.maxGapMs,
            summary = summary,
            parseMs = SystemClock.uptimeMillis() - start,
        )
        _resultFiles.value = countResultFiles()
    }

    /** 앱 안 hprof — 백그라운드 스레드에서 불러도 덤프하는 동안 앱의 모든 스레드가 멈춘다 */
    fun dumpHprofInApp() = launchExclusive("hprof") {
        val app = getApplication<Application>()
        val file = File(app.cacheDir, HPROF_FILE)
        val registry = ScreenListenerRegistry.size
        // 수백 MB 를 쓴다 — 남은 공간이 모자라면 시도하지 않는다. 다른 앱의 지울 수 있는 캐시까지 여유로 세는 getAllocatableBytes 는
        // 쓰지 않는다(측정 중 공간이 차자 installd 가 다른 앱 캐시를 비웠다) — 실제로 비어 있는 공간만 본다
        @SuppressLint("UsableSpace")
        val free = app.cacheDir.usableSpace
        if (free < HPROF_MIN_FREE_BYTES) {
            _hprof.value = HprofRun(registry, 0, 0, 0, 0, "남은 공간 ${free / MB}MB — ${HPROF_MIN_FREE_BYTES / MB}MB 이상일 때만 실행")
            return@launchExclusive
        }
        val meter = MainThreadStallMeter()
        meter.start()
        delay(STALL_IDLE_WINDOW_MS)
        val idle = meter.restart()
        var error: String? = null
        var duration = 0L
        try {
            duration = withContext(Dispatchers.IO) {
                val t0 = SystemClock.uptimeMillis()
                Debug.dumpHprofData(file.absolutePath)
                SystemClock.uptimeMillis() - t0
            }
            // 덤프가 끝난 뒤 밀려 있던 메인 스레드 일이 처리될 시간을 둔다
            delay(STALL_SETTLE_MS)
        } catch (e: java.io.IOException) {
            error = e.toString()
        } finally {
            meter.stop()
        }
        val bytes = file.length()
        file.delete()
        _hprof.value = HprofRun(registry, duration, idle, meter.maxGapMs, bytes, error)
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
        const val STALL_IDLE_WINDOW_MS = 300L
        const val STALL_SETTLE_MS = 100L
        const val HPROF_FILE = "cs174.hprof"
        const val MB = 1024L * 1024
        /** 기기 · 빌드에 따라 hprof 가 350~560MB 였다(API 35 에뮬 debug) */
        const val HPROF_MIN_FREE_BYTES = 2_048L * MB
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

    suspend fun requestJavaHeapDump(context: Context, tag: String): ProfilingOutcome =
        suspendCancellableCoroutine { cont ->
            val signal = CancellationSignal()
            val request = JavaHeapDumpRequestBuilder()
                .setTag(tag)
                .setCancellationSignal(signal)
                .build()
            val start = System.nanoTime()
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

/**
 * 메인 스레드가 얼마나 멈췄는지 — 5ms 마다 깨어나는 심장 박동의 가장 긴 간격.
 * 메인 스레드에서만 쓰고 읽는다(뷰모델 코루틴은 Dispatchers.Main).
 */
private class MainThreadStallMeter {
    private val handler = Handler(Looper.getMainLooper())
    private var last = 0L
    private var running = false
    var maxGapMs = 0L
        private set

    private val tick = object : Runnable {
        override fun run() {
            val now = SystemClock.uptimeMillis()
            if (last != 0L) maxGapMs = maxOf(maxGapMs, now - last)
            last = now
            if (running) handler.postDelayed(this, TICK_MS)
        }
    }

    fun start() {
        running = true
        last = 0L
        maxGapMs = 0L
        handler.post(tick)
    }

    /** 지금까지의 가장 긴 간격을 돌려주고 새로 잰다 */
    fun restart(): Long {
        val gap = maxGapMs
        maxGapMs = 0L
        return gap
    }

    fun stop() {
        running = false
        handler.removeCallbacks(tick)
    }

    private companion object {
        const val TICK_MS = 5L
    }
}
