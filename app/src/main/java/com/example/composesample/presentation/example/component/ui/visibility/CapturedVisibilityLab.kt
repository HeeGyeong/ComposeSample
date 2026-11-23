package com.example.composesample.presentation.example.component.ui.visibility

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * CapturedAnimatedVisibility(#184) — CapturedAnimatedVisibilityExampleUI 의 측정.
 * 위쪽(사례 · 기대 · 판정 · 통계)은 Android 클래스 없이 단위 테스트(CapturedVisibilityLabTest)로 검증한다.
 */

// ==================== 1번 카드: 닫으면서 선택을 지운다 ====================

/** 상세 패널에 띄우는 곡 — 색은 ARGB(Compose Color 로는 화면에서 바꾼다) */
internal data class Track(val id: Int, val title: String, val artist: String, val length: String, val coverArgb: Long)

internal val sampleTracks = listOf(
    Track(1, "새벽 세 시의 신호", "Low Orbit", "3:42", 0xFF5C6BC0),
    Track(2, "바다 쪽 창문", "Harbor Lights", "4:05", 0xFF26A69A),
    Track(3, "느린 계단", "Paper Moon", "2:58", 0xFFEF6C00)
)

/** 패널을 닫는 세 방법 — 셋 다 같은 visible · 선택 상태를 읽는다 */
internal enum class ExitVariant(val label: String) {
    AnimatedNull("① AnimatedVisibility · 선택을 바로 null"),
    AnimatedHold("② AnimatedVisibility · 마지막 값 붙들기"),
    Captured("③ CapturedAnimatedVisibility · 선택을 바로 null")
}

/** 닫는 순서 — 늦은 visible 은 선택을 지우는 곳과 visible 을 내리는 곳이 다른 흔한 모양(ViewModel 두 흐름 · LaunchedEffect 로 따라 내리기) */
internal enum class CloseOrder(val label: String) {
    Together("같은 프레임에 함께"),
    SelectionFirst("선택 먼저 · visible 은 ${LATE_VISIBLE_MILLIS}ms 뒤")
}

internal const val LATE_VISIBLE_MILLIS = 150L

/** 원문 예시의 퇴장 시간(tween(800)) 과 눈으로 따라가기 쉬운 느린 값 */
internal val exitMillisChoices = listOf(800L, 2_000L)

internal const val ENTER_MILLIS = 300

/** 퇴장이 끝난 뒤 결과를 모으기 전 기다리는 시간 */
internal const val RESULT_SETTLE_MILLIS = 300L

/** 측정한 시간을 퇴장 시간에 견준 범주 */
internal enum class Span(val label: String) {
    Instant("바로"),
    Full("끝까지")
}

internal const val INSTANT_PERCENT = 30
internal const val FULL_PERCENT = 70

/** 퇴장 시간의 30% 아래면 바로, 70% 이상이면 끝까지, 사이는 판정 불가(null) — 경계가 float 오차로 흔들리지 않게 정수로 비교 */
internal fun classifySpan(millis: Long, exitMillis: Long): Span? = when {
    millis * 100 < exitMillis * INSTANT_PERCENT -> Span.Instant
    millis * 100 >= exitMillis * FULL_PERCENT -> Span.Full
    else -> null
}

/**
 * 닫기를 누른 뒤 카드가 자리를 차지한 시간.
 * - ① 은 나가는 동안에도 내용을 컴포즈한다 → 선택이 null 인 내용은 그릴 것도 크기도 없다
 * - ③ 은 visible 이 false 가 된 그 컴포지션에서 내용을 빈 람다로 바꾸고, 지난 프레임에 기록해 둔 레이어를 그린다.
 *   선택이 먼저 비면 그 사이 프레임에 '빈 내용' 이 측정 · 배치돼 크기 0 이 되고 그것을 들고 나간다
 */
internal fun expectedShown(variant: ExitVariant, order: CloseOrder): Span = when (variant) {
    ExitVariant.AnimatedNull -> Span.Instant
    ExitVariant.AnimatedHold -> Span.Full
    ExitVariant.Captured -> if (order == CloseOrder.Together) Span.Full else Span.Instant
}

/** visible 을 내린 뒤 내용 람다가 컴포지션에서 빠진 시점 — ① · ② 는 퇴장이 끝날 때, ③ 은 그 자리에서 */
internal fun expectedReleased(variant: ExitVariant): Span =
    if (variant == ExitVariant.Captured) Span.Instant else Span.Full

/** 패널 하나를 한 번 닫은 결과 */
internal data class ExitResult(
    val variant: ExitVariant,
    val order: CloseOrder,
    val exitMillis: Long,
    /** 닫기를 누른 뒤 패널 높이가 처음 0 이 될 때까지(null = 끝내 0 이 안 됐다) */
    val shownMillis: Long?,
    /** 그 사이 높이가 바뀐 레이아웃 횟수 — 줄어드는 프레임 수 */
    val shrinkFrames: Int,
    /** visible 을 내린 뒤 내용 람다의 DisposableEffect 가 풀릴 때까지(null = 기록 없음) */
    val releasedMillis: Long?
) {
    val shownSpan: Span? get() = shownMillis?.let { classifySpan(it, exitMillis) }
    val releasedSpan: Span? get() = releasedMillis?.let { classifySpan(it, exitMillis) }
    val matches: Boolean
        get() = shownSpan == expectedShown(variant, order) && releasedSpan == expectedReleased(variant)
}

/**
 * 패널 하나의 기록 — 레이아웃(onSizeChanged) · 컴포지션(onDispose) 콜백에서 쓰는 평범한 필드다.
 * 상태로 두면 기록하는 쓰기가 다시 프레임을 부른다.
 */
internal class ExitTrace {
    private var recording = false
    private var clickNanos = 0L
    private var hideNanos = 0L
    private var firstZeroNanos = 0L
    private var shrinkFrames = 0
    private var disposedNanos = 0L

    /** 닫기를 누른 순간 — 이 뒤의 높이 · 해제만 센다 */
    fun begin(nowNanos: Long) {
        recording = true
        clickNanos = nowNanos
        hideNanos = nowNanos
        firstZeroNanos = 0L
        shrinkFrames = 0
        disposedNanos = 0L
    }

    /** visible 을 false 로 쓴 순간(같은 프레임에 닫으면 begin 과 같다) */
    fun onHide(nowNanos: Long) {
        hideNanos = nowNanos
    }

    fun onHeight(heightPx: Int, nowNanos: Long) {
        if (!recording || firstZeroNanos != 0L) return
        if (heightPx > 0) shrinkFrames++ else firstZeroNanos = nowNanos
    }

    fun onContentDisposed(nowNanos: Long) {
        if (recording && disposedNanos == 0L) disposedNanos = nowNanos
    }

    fun finish(variant: ExitVariant, order: CloseOrder, exitMillis: Long): ExitResult {
        recording = false
        return ExitResult(
            variant = variant,
            order = order,
            exitMillis = exitMillis,
            shownMillis = if (firstZeroNanos == 0L) null else (firstZeroNanos - clickNanos) / NANOS_PER_MILLI,
            shrinkFrames = shrinkFrames,
            releasedMillis = if (disposedNanos == 0L) null else ((disposedNanos - hideNanos) / NANOS_PER_MILLI).coerceAtLeast(0L)
        )
    }
}

/** ② 의 마지막 값 — 상태가 아닌 필드라 컴포지션 중에 써도 다시 그리기를 부르지 않는다 */
internal class LastValue<T : Any> {
    var value: T? = null
}

/** 1번 카드 상태 — 화면 루트에 둔다(카드가 스크롤로 빠졌다 돌아와도 결과가 남게) */
internal class CloseLabState {
    var selected by mutableStateOf<Track?>(sampleTracks[0])
        private set
    var visible by mutableStateOf(true)
        private set
    var order by mutableStateOf(CloseOrder.Together)
    var exitMillis by mutableLongStateOf(exitMillisChoices[0])
    var busy by mutableStateOf(false)
        private set
    var results by mutableStateOf<List<ExitResult>>(emptyList())
        private set
    val traces: Map<ExitVariant, ExitTrace> = ExitVariant.entries.associateWith { ExitTrace() }

    fun open(track: Track) {
        if (busy) return
        selected = track
        visible = true
    }

    /** 닫고, 퇴장이 끝나면 세 패널의 결과를 모은다 */
    suspend fun close() {
        if (busy || !visible) return
        busy = true
        try {
            val order = order
            val exit = exitMillis
            val clicked = System.nanoTime()
            traces.values.forEach { it.begin(clicked) }
            selected = null
            if (order == CloseOrder.SelectionFirst) delay(LATE_VISIBLE_MILLIS)
            val hidden = System.nanoTime()
            visible = false
            traces.values.forEach { it.onHide(hidden) }
            delay(exit + RESULT_SETTLE_MILLIS)
            results = ExitVariant.entries.map { traces.getValue(it).finish(it, order, exit) }
        } finally {
            busy = false
        }
    }
}

// ==================== 2번 카드: 나가는 동안 · 다시 열 때 ====================

internal enum class LiveVariant(val label: String) {
    Animated("AnimatedVisibility"),
    Captured("Captured…")
}

internal enum class LiveAction(val label: String) {
    Close("닫기"),
    CloseReopen("닫고 ${REOPEN_AFTER_MILLIS}ms 뒤 다시 열기")
}

internal const val LIVE_EXIT_MILLIS = 2_000L
internal const val REOPEN_AFTER_MILLIS = 800L

/** 나가는 동안 스피너는 계속 그려지나 — AnimatedVisibility 는 내용이 살아 있고, Captured 는 그림만 남는다 */
internal fun expectedDrawRate(variant: LiveVariant): FrameRate =
    if (variant == LiveVariant.Animated) FrameRate.Continuous else FrameRate.None

/** 나가는 도중 다시 열면 내용의 remember 가 이어지나 — Captured 는 내용을 버렸으니 처음부터 */
internal fun expectedKept(variant: LiveVariant): Boolean = variant == LiveVariant.Animated

internal data class LiveResult(
    val variant: LiveVariant,
    val action: LiveAction,
    /** 창 안에서 내용의 스피너 그리기 람다가 불린 횟수 */
    val exitDraws: Int,
    val windowMillis: Long,
    val refreshHz: Float,
    /** 닫기 전 · 다시 연 뒤 내용이 remember 한 번호(다시 열지 않으면 null) */
    val serialBefore: Int,
    val serialAfter: Int?
) {
    val drawRate: FrameRate? get() = classifyRate(exitDraws, windowMillis, refreshHz)
    val kept: Boolean? get() = serialAfter?.let { it == serialBefore }
    val matches: Boolean
        get() = drawRate == expectedDrawRate(variant) && (serialAfter == null || kept == expectedKept(variant))
}

/** 패널 하나의 기록 — draws 는 그리기 람다에서, currentSerial 은 SideEffect 에서 쓰는 평범한 필드 */
internal class LiveTrace {
    var draws = 0
        private set
    var currentSerial = 0
    private var nextSerial = 0

    fun onDraw() {
        draws++
    }

    /** 내용이 새로 컴포즈될 때 remember 가 한 번 부른다 */
    fun newSerial(): Int = ++nextSerial
}

/** 2번 카드 상태 */
internal class LiveLabState {
    var visible by mutableStateOf(true)
        private set
    var busy by mutableStateOf(false)
        private set
    var results by mutableStateOf<List<LiveResult>>(emptyList())
        private set
    val traces: Map<LiveVariant, LiveTrace> = LiveVariant.entries.associateWith { LiveTrace() }

    fun open() {
        if (!busy) visible = true
    }

    suspend fun run(action: LiveAction, refreshHz: Float) {
        if (busy || !visible) return
        busy = true
        try {
            val serialBefore = traces.mapValues { it.value.currentSerial }
            val drawsBefore = traces.mapValues { it.value.draws }
            val start = System.nanoTime()
            visible = false
            delay(if (action == LiveAction.Close) LIVE_EXIT_MILLIS else REOPEN_AFTER_MILLIS)
            val windowMillis = (System.nanoTime() - start) / NANOS_PER_MILLI
            val draws = traces.mapValues { it.value.draws - drawsBefore.getValue(it.key) }
            if (action == LiveAction.CloseReopen) {
                visible = true
                delay(ENTER_MILLIS + RESULT_SETTLE_MILLIS)
            }
            results = LiveVariant.entries.map { variant ->
                LiveResult(
                    variant = variant,
                    action = action,
                    exitDraws = draws.getValue(variant),
                    windowMillis = windowMillis,
                    refreshHz = refreshHz,
                    serialBefore = serialBefore.getValue(variant),
                    serialAfter = if (action == LiveAction.CloseReopen) traces.getValue(variant).currentSerial else null
                )
            }
        } finally {
            busy = false
        }
    }
}

// ==================== 3번 카드: 원문 주장 — 나가는 동안 프레임 시간 ====================

internal enum class BenchContent(val label: String) {
    Static("정적 카드"),
    Spinner("스피너 있는 카드")
}

internal enum class BenchVariant(val label: String) {
    Animated("AnimatedVisibility"),
    Captured("CapturedAnimatedVisibility")
}

/** 방법마다 퇴장 횟수 — 방법을 번갈아 돌려 기기 상태의 흐름(발열 · JIT)이 한쪽에만 쌓이지 않게 한다 */
internal const val BENCH_ROUNDS = 8
internal const val BENCH_EXIT_MILLIS = 800L
internal const val BENCH_SETTLE_MILLIS = 700L
internal const val BENCH_GAP_MILLIS = 250L
internal const val BENCH_LATE_REPORT_MILLIS = 300L

/** 대략 시간(초) */
internal fun benchSeconds(): Int =
    ((BENCH_SETTLE_MILLIS + BENCH_EXIT_MILLIS + BENCH_GAP_MILLIS) * BENCH_ROUNDS * BenchVariant.entries.size / 1000L).toInt()

/** FrameMetrics 한 프레임 — ui = 입력 + 애니메이션(Compose 리컴포지션) + 측정 · 배치 + 그리기 */
internal data class FrameSample(val vsyncNanos: Long, val totalNanos: Long, val uiNanos: Long)

internal data class ExitWindow(val variant: BenchVariant, val startNanos: Long, val endNanos: Long)

internal fun median(values: List<Long>): Long? {
    if (values.isEmpty()) return null
    val sorted = values.sorted()
    val mid = sorted.size / 2
    return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2
}

/** 한 방법의 퇴장 프레임 — 첫 프레임(Captured 는 이 프레임에 내용 컴포지션을 버린다)은 따로 센다 */
internal data class VariantFrames(
    val variant: BenchVariant,
    val windows: Int,
    val frames: Int,
    val medianTotalNanos: Long?,
    val medianUiNanos: Long?,
    val firstFrameMedianTotalNanos: Long?,
    val firstFrameMedianUiNanos: Long?
)

/** 창 안(겨냥한 VSync 기준)의 프레임을 방법별로 모은다 */
internal fun summarizeFrames(samples: List<FrameSample>, windows: List<ExitWindow>): Map<BenchVariant, VariantFrames> =
    BenchVariant.entries.associateWith { variant ->
        val mine = windows.filter { it.variant == variant }
        val firsts = mutableListOf<FrameSample>()
        val rest = mutableListOf<FrameSample>()
        mine.forEach { window ->
            val frames = samples.filter { it.vsyncNanos >= window.startNanos && it.vsyncNanos < window.endNanos }
                .sortedBy { it.vsyncNanos }
            frames.firstOrNull()?.let { firsts += it }
            rest += frames.drop(1)
        }
        VariantFrames(
            variant = variant,
            windows = mine.size,
            frames = firsts.size + rest.size,
            medianTotalNanos = median(rest.map { it.totalNanos }),
            medianUiNanos = median(rest.map { it.uiNanos }),
            firstFrameMedianTotalNanos = median(firsts.map { it.totalNanos }),
            firstFrameMedianUiNanos = median(firsts.map { it.uiNanos })
        )
    }

/** 나노초 → "1.23" (ms, 소수 둘째 자리 버림) */
internal fun formatMillis(nanos: Long?): String {
    if (nanos == null) return "-"
    val hundredths = nanos / 10_000L
    return "${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')}"
}

/** 3.456 → "3.5", -0.04 → "-0.0" 대신 "0.0" (소수 첫째 자리 반올림) */
internal fun formatPercent(value: Double?): String {
    if (value == null) return "-"
    val tenths = kotlin.math.round(value * 10).toLong()
    val sign = if (tenths < 0) "-" else ""
    val abs = kotlin.math.abs(tenths)
    return "$sign${abs / 10}.${abs % 10}"
}

/** Captured 가 AnimatedVisibility 보다 몇 % 빠른가(음수면 느림) */
internal fun percentFaster(animated: Long?, captured: Long?): Double? {
    if (animated == null || captured == null || animated <= 0L) return null
    return (animated - captured) * 100.0 / animated
}

internal data class BenchReport(
    val content: BenchContent,
    val frames: Map<BenchVariant, VariantFrames>,
    /** 측정 중 화면을 건드렸다 — 스크롤 · 물결이 프레임을 바꿔 결과가 무효 */
    val touched: Boolean
) {
    val totalFaster: Double?
        get() = percentFaster(frames[BenchVariant.Animated]?.medianTotalNanos, frames[BenchVariant.Captured]?.medianTotalNanos)
    val uiFaster: Double?
        get() = percentFaster(frames[BenchVariant.Animated]?.medianUiNanos, frames[BenchVariant.Captured]?.medianUiNanos)
}

/** 3번 카드 상태 — touched 는 입력 콜백에서 쓰는 평범한 필드 */
internal class ExitBenchState {
    var content by mutableStateOf(BenchContent.Static)
    var variant by mutableStateOf(BenchVariant.Animated)
        private set
    var visible by mutableStateOf(true)
        private set
    var running by mutableStateOf(false)
        private set
    var status by mutableStateOf("대기 — 버튼을 누르면 두 방법을 ${BENCH_ROUNDS}번씩 번갈아 닫는다(약 ${benchSeconds()}초)")
        private set
    var runToken by mutableIntStateOf(0)
        private set
    val reports = mutableStateMapOf<BenchContent, BenchReport>()
    private var touched = false

    fun onUserTouch() {
        if (running) touched = true
    }

    fun request() {
        if (!running) runToken++
    }

    suspend fun execute(recorder: ExitFrameRecorder) {
        val content = content
        running = true
        touched = false
        val windows = mutableListOf<ExitWindow>()
        try {
            recorder.start()
            val total = BENCH_ROUNDS * BenchVariant.entries.size
            var step = 0
            repeat(BENCH_ROUNDS) { round ->
                val order = if (round % 2 == 0) BenchVariant.entries else BenchVariant.entries.reversed()
                order.forEach { next ->
                    step++
                    status = "$step/$total ${next.label} — 재는 중, 화면을 건드리지 마세요"
                    variant = next
                    visible = true
                    delay(BENCH_SETTLE_MILLIS)
                    val start = System.nanoTime()
                    visible = false
                    delay(BENCH_EXIT_MILLIS)
                    windows += ExitWindow(next, start, start + BENCH_EXIT_MILLIS * NANOS_PER_MILLI)
                    delay(BENCH_GAP_MILLIS)
                }
            }
            delay(BENCH_LATE_REPORT_MILLIS)
            val report = BenchReport(content, summarizeFrames(recorder.stop(), windows), touched)
            reports[content] = report
            status = if (report.touched) "끝 — 재는 중 화면을 건드려 이 결과는 무효" else "끝 — ${content.label} 결과를 아래에 적었다"
        } finally {
            recorder.stop()
            running = false
            variant = BenchVariant.Animated
            visible = true
        }
    }
}

/**
 * 겨냥한 VSync 시각(FrameMetrics.INTENDED_VSYNC_TIMESTAMP)이 API 26 부터라 그 아래에서는 3번 카드를 돌리지 않는다.
 * debug 변형은 HotSwan 때문에 minSdk 26 이라 lintDebug 가 이 비교를 ObsoleteSdkInt 로 센다 — release(minSdk 24)에는 필요한 검사
 */
internal val frameMetricsSupported: Boolean
    @SuppressLint("ObsoleteSdkInt")
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * 앱 창의 FrameMetrics 를 모은다 — 겨냥한 VSync 시각으로 창 안 프레임만 고른다(API 26+, 아래에서는 아무것도 모으지 않는다).
 * 리스너는 전용 HandlerThread 에서 받는다 — 메인 스레드에서 받으면 받는 일이 측정하는 프레임에 끼어든다.
 */
internal class ExitFrameRecorder(private val window: Window) {
    private val thread = HandlerThread("captured-visibility-frames").apply { start() }
    private val handler = Handler(thread.looper)
    private val samples = ArrayList<FrameSample>()
    private var listening = false

    @SuppressLint("ObsoleteSdkInt")
    private val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ui = metrics.getMetric(FrameMetrics.INPUT_HANDLING_DURATION) +
                metrics.getMetric(FrameMetrics.ANIMATION_DURATION) +
                metrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION) +
                metrics.getMetric(FrameMetrics.DRAW_DURATION)
            val sample = FrameSample(
                vsyncNanos = metrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP),
                totalNanos = metrics.getMetric(FrameMetrics.TOTAL_DURATION),
                uiNanos = ui
            )
            synchronized(samples) { samples += sample }
        }
    }

    fun start() {
        synchronized(samples) { samples.clear() }
        if (listening) return
        window.addOnFrameMetricsAvailableListener(listener, handler)
        listening = true
    }

    /** 붙인 적 없는 리스너를 떼면 IllegalArgumentException 이라 listening 으로 거른다 */
    fun stop(): List<FrameSample> {
        if (listening) {
            window.removeOnFrameMetricsAvailableListener(listener)
            listening = false
        }
        return synchronized(samples) { samples.toList() }
    }

    fun close() {
        stop()
        thread.quitSafely()
    }
}

private const val NANOS_PER_MILLI = 1_000_000L
