package com.example.composesample.presentation.example.component.ui.visibility

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Printer
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * 숨긴 스피너가 만드는 프레임(#177 · #183) — VisibilityExampleUI 의 3번 카드가 쓴다.
 * 위쪽(사례 · 기대 · 판정 · 로그 줄 판별)은 Android 클래스 없이 단위 테스트(HiddenSpinnerFrameLabTest)로 검증한다.
 */

// ==================== 사례와 기대 (Android 밖에서 단독으로 검증 가능) ====================

/** 측정 묶음 — 1은 #177(컴포지션 · 배치를 바꾸는 방법), 2는 #183(놓는 곳 · 목록 위치만 바꾸는 방법) */
internal enum class CaseGroup(val label: String) {
    Remove("숨기는 방법"),
    Elsewhere("안 보이는 곳에 두기")
}

/** 스피너를 숨기는 방법 */
internal enum class HideTechnique(val label: String, val group: CaseGroup) {
    Alpha("alpha(0f)", CaseGroup.Remove),
    NotPlaced("배치 안 함", CaseGroup.Remove),
    IfVisible("if (visible)", CaseGroup.Remove),
    Animated("AnimatedVisibility", CaseGroup.Remove),
    Covered("불투명 덮개", CaseGroup.Elsewhere),
    DialogCovered("Dialog 덮개", CaseGroup.Elsewhere),
    OffWindow("창 밖 offset", CaseGroup.Elsewhere),
    ZeroSize("크기 0", CaseGroup.Elsewhere),
    LazyReleased("Lazy 지나간 칸", CaseGroup.Elsewhere),
    LazyLastPass("Lazy 한 번에 넘긴 칸", CaseGroup.Elsewhere),
    LazyAhead("Lazy 미리 가져온 칸", CaseGroup.Elsewhere),
    LazyKept("캐시 창 안 칸", CaseGroup.Elsewhere)
}

/** 5초 창 하나에서 세는 것 */
internal enum class FrameCounter(val label: String) {
    /** 메인 스레드가 VSync 에 깨어 Choreographer 프레임을 돈 횟수 */
    Callbacks("프레임 콜백"),

    /** 창이 실제로 그려 보낸 프레임(FrameMetrics 보고) */
    Rendered("그린 프레임"),

    /** 스피너 노드의 그리기 람다가 불린 횟수 */
    SpinnerDraws("스피너 그리기")
}

/**
 * 기대 — 프레임 수 자체는 주사율 · 기기 속도에 따라 달라서 '쉬지 않고 계속이냐 0 이냐' 로 비교한다
 * (swiftshader 에뮬레이터 debug 빌드는 60 Hz 에서 5초에 약 150 — VSync 마다 못 그려도 쉬지 않는다)
 */
internal enum class FrameRate(val label: String) {
    Continuous("계속"),
    None("0")
}

/** 측정 줄 — 보임(기준) 1 + 숨김 12(1번 묶음 4 · 2번 묶음 8). 숨김 줄은 '차례로 재기' 에서 매번 보임 상태에서 숨겨 퇴장 경로를 거친다 */
internal enum class FrameCase(val technique: HideTechnique, val visible: Boolean, val label: String) {
    Shown(HideTechnique.Alpha, true, "보임 (기준)"),
    AlphaZero(HideTechnique.Alpha, false, "alpha(0f)"),
    NotPlaced(HideTechnique.NotPlaced, false, "배치 안 함"),
    IfRemoved(HideTechnique.IfVisible, false, "if (visible)"),
    AnimatedHidden(HideTechnique.Animated, false, "AnimatedVisibility"),
    Covered(HideTechnique.Covered, false, "불투명 덮개"),
    DialogCovered(HideTechnique.DialogCovered, false, "Dialog 덮개"),
    OffWindow(HideTechnique.OffWindow, false, "창 밖 offset"),
    ZeroSize(HideTechnique.ZeroSize, false, "크기 0"),
    LazyReleased(HideTechnique.LazyReleased, false, "Lazy 지나간 칸"),
    LazyLastPass(HideTechnique.LazyLastPass, false, "Lazy 한 번에 넘긴 칸"),
    LazyAhead(HideTechnique.LazyAhead, false, "Lazy 미리 가져온 칸"),
    LazyKept(HideTechnique.LazyKept, false, "캐시 창 안 칸")
}

/** 지금 상태가 어느 줄인지 — 보이면 방법과 상관없이 기준 줄 */
internal fun caseOf(technique: HideTechnique, visible: Boolean): FrameCase =
    if (visible) FrameCase.Shown else FrameCase.entries.first { !it.visible && it.technique == technique }

/** 묶음 하나를 차례로 잴 때의 줄 — 기준 줄을 앞에 둔다 */
internal fun casesOf(group: CaseGroup): List<FrameCase> =
    listOf(FrameCase.Shown) + FrameCase.entries.filter { !it.visible && it.technique.group == group }

/**
 * 근거(ui 1.12.1 · animation-core 1.12.1 · material3 1.4.0 소스):
 * - 무한 트랜지션은 컴포지션에 있는 한 매 프레임 프레임을 다시 요청한다(InfiniteTransition.run 의 LaunchedEffect 루프) → 프레임 콜백
 * - 스피너 Canvas 는 애니메이션 값을 그리기 단계에서 읽는다 → 그려지는 동안은 값이 바뀔 때마다 그리기 무효화 → 창이 새 프레임
 * - alpha(0f) = graphicsLayer(alpha = 0f) — 알파 0 이라고 기록을 건너뛰는 지름길은 없다
 * - 배치되지 않은 노드는 그리지 않는다 → 읽는 곳이 없어 창은 쉬지만 시계는 돈다
 * - if · AnimatedVisibility(퇴장 끝) 는 컴포지션에서 빠진다 → LaunchedEffect 취소 → 아무것도 없다
 * - (#183) 그리기 경로(NodeCoordinator.draw · LayoutNodeDrawScope.draw · GraphicsLayer.record)에 가림 · 화면 밖 · 크기 0 검사가 없다
 *   → 덮개(같은 창 · 다른 창) · 창 밖 offset · 크기 0 은 배치된 채라 보임과 같다
 * - (#183) Lazy 화면 밖 칸은 배치되지 않는다 → 그리기 0. 컴포지션에 남으면 시계는 돈다 — 마지막 측정이 컴포즈해 보고 버린 칸
 *   (LazyListMeasure 'offscreen and will not be visible' — 그 측정에서 쓴 칸이라 SubcomposeLayout 이 활성으로 둔다),
 *   기본 미리 가져오기(스크롤 방향 다음 칸), 캐시 창 keepAroundItems. 측정이 더 돌면 재사용 풀로 가며 비활성화(effect 취소)
 */
internal fun expectedRate(case: FrameCase, counter: FrameCounter): FrameRate = when (case) {
    FrameCase.Shown, FrameCase.AlphaZero -> FrameRate.Continuous
    FrameCase.Covered, FrameCase.DialogCovered, FrameCase.OffWindow, FrameCase.ZeroSize -> FrameRate.Continuous
    FrameCase.NotPlaced, FrameCase.LazyLastPass, FrameCase.LazyAhead, FrameCase.LazyKept ->
        if (counter == FrameCounter.Callbacks) FrameRate.Continuous else FrameRate.None
    FrameCase.IfRemoved, FrameCase.AnimatedHidden, FrameCase.LazyReleased -> FrameRate.None
}

/**
 * 창 안 VSync 수에 이 비율 이상이면 '계속' — 느린 기기도 넘고, 가끔 깨는 것(0.5초마다 깜빡이는 커서는 5초에 10)은 못 넘는다
 */
internal const val CONTINUOUS_RATIO = 0.25f

/** 센 수를 범주로 — 0 도 아니고 계속에도 못 미치면 null(판정 불가 — 다른 무언가가 가끔 깨운다) */
internal fun classifyRate(count: Int, windowMillis: Long, refreshHz: Float): FrameRate? {
    if (count == 0) return FrameRate.None
    val vsyncs = windowMillis / 1000f * refreshHz
    return if (count >= vsyncs * CONTINUOUS_RATIO) FrameRate.Continuous else null
}

/** Looper 메시지 로그에서 VSync 로 시작한 Choreographer 프레임 디스패치를 알아보는 표식 */
internal const val VSYNC_DISPATCH_MARK = "Choreographer\$FrameDisplayEventReceiver"

/** Looper 가 메시지마다 남기는 ">>>>> Dispatching to …" 줄 가운데 Choreographer 프레임인 것 */
internal fun isVsyncDispatch(line: String): Boolean =
    line.startsWith(">>>>> Dispatching") && line.contains(VSYNC_DISPATCH_MARK)

/** API 26+ 는 프레임이 겨냥한 VSync 시각으로 창 안 프레임만 센다 — FrameMetrics 보고는 렌더가 끝난 뒤 늦게 온다 */
internal fun inWindow(intendedVsyncNanos: Long, startNanos: Long, endNanos: Long): Boolean =
    intendedVsyncNanos in startNanos until endNanos

/** 창 하나의 결과 */
internal data class FrameWindowResult(
    val case: FrameCase,
    val callbacks: Int,
    val rendered: Int,
    /** 리스너가 늦어 시스템이 건너뛴 보고 수(dropCountSinceLastInvocation 합) — 그린 프레임에 더하지 않고 따로 보인다 */
    val droppedReports: Int,
    val spinnerDraws: Int,
    val windowMillis: Long,
    val refreshHz: Float,
    /** 창 안에서 화면을 건드렸다 — 스크롤 · 물결이 프레임을 만들어 결과가 무효 */
    val touched: Boolean
) {
    fun count(counter: FrameCounter): Int = when (counter) {
        FrameCounter.Callbacks -> callbacks
        FrameCounter.Rendered -> rendered
        FrameCounter.SpinnerDraws -> spinnerDraws
    }

    fun matches(counter: FrameCounter): Boolean =
        classifyRate(count(counter), windowMillis, refreshHz) == expectedRate(case, counter)

    val allMatch: Boolean
        get() = !touched && FrameCounter.entries.all { matches(it) }

    /** logcat 한 줄(태그 HiddenSpinner) — 기기 측정 스크립트가 읽는다 */
    fun probeLine(): String =
        "case=${case.name} callbacks=$callbacks rendered=$rendered draws=$spinnerDraws dropped=$droppedReports " +
            "window=$windowMillis hz=${refreshHz.roundToInt()} touched=$touched ok=$allMatch"
}

/** 측정 창 시간 — 원문과 같은 5초. 안정 대기는 버튼 물결 · 칩 · 퇴장 애니메이션이 끝나기를 기다린다 */
internal const val WINDOW_MILLIS = 5_000L
internal const val SETTLE_MILLIS = 1_500L
internal const val SHOW_BEFORE_HIDE_MILLIS = 800L

/** 창이 끝난 뒤 늦게 오는 FrameMetrics 보고를 기다리는 시간 */
internal const val LATE_REPORT_MILLIS = 300L

/** 차례로 재기의 대략 시간(10초 단위 반올림) — 숨김 줄은 보임 상태를 먼저 거친다 */
internal fun estimatedSeconds(cases: List<FrameCase>): Int {
    val millis = cases.sumOf { case ->
        val showFirst = if (case.visible || cases.size == 1) 0L else SHOW_BEFORE_HIDE_MILLIS
        showFirst + SETTLE_MILLIS + WINDOW_MILLIS + LATE_REPORT_MILLIS
    }
    return ((millis + 5_000L) / 10_000L * 10L).toInt()
}

private const val LOG_TAG = "HiddenSpinner"

// ==================== 화면 상태 ====================

/**
 * 카드의 상태 홀더 — 컴포저블 밖 객체라 HotSwan 의 지역 변수 쓰기 함정이 없다.
 * spinnerDraws · touchedInWindow · measuring 은 그리기 · 입력에서 바꾸는 평범한 필드다(상태로 두면 그 쓰기가 다시 프레임을 부른다).
 */
internal class HiddenSpinnerFrameLab {
    var technique by mutableStateOf(HideTechnique.Alpha)
    var visible by mutableStateOf(true)
    var status by mutableStateOf("대기 — 버튼을 누르면 1.5초 기다린 뒤 5초를 센다")
        private set
    var running by mutableStateOf(false)
        private set

    /** 실행 요청 — 바뀔 때마다 카드의 LaunchedEffect 가 계획을 돈다 */
    var runToken by mutableIntStateOf(0)
        private set
    var plan: List<FrameCase> = emptyList()
        private set
    val results = mutableStateMapOf<FrameCase, FrameWindowResult>()

    var spinnerDraws = 0
    var measuring = false
    var touchedInWindow = false

    fun onSpinnerDraw() {
        spinnerDraws++
    }

    fun onUserTouch() {
        if (measuring) touchedInWindow = true
    }

    fun requestCurrent() = request(listOf(caseOf(technique, visible)))

    fun requestGroup(group: CaseGroup) = request(casesOf(group))

    private fun request(cases: List<FrameCase>) {
        if (running) return
        plan = cases
        runToken++
    }

    /** 계획을 차례로 돈다 — 상태를 바꾸는 쓰기는 전부 안정 대기 전에 끝낸다 */
    suspend fun execute(meter: FrameWindowMeter, refreshHz: Float) {
        val cases = plan
        if (cases.isEmpty()) return
        running = true
        try {
            cases.forEachIndexed { index, case ->
                status = "${index + 1}/${cases.size} ${case.label} — 세는 중, 화면을 건드리지 마세요"
                if (cases.size > 1 && !case.visible) {
                    technique = case.technique
                    visible = true
                    delay(SHOW_BEFORE_HIDE_MILLIS)
                }
                technique = case.technique
                visible = case.visible
                val result = meter.measure(case, refreshHz, this)
                results[case] = result
                Log.i(LOG_TAG, result.probeLine())
            }
            val done = cases.mapNotNull { results[it] }
            val matched = done.count { it.allMatch }
            status = "끝 — ${done.size}줄 가운데 예상과 같음 $matched"
        } finally {
            // 끝났든 카드가 빠져 취소됐든 계획을 비운다 — 카드가 다시 컴포즈될 때 같은 runToken 으로 다시 돌지 않게
            plan = emptyList()
            running = false
        }
    }
}

// ==================== 창 하나를 세는 계측기 ====================

/**
 * 프레임 콜백 · 그린 프레임 · 스피너 그리기를 한 창 동안 센다.
 *
 * 창의 시작 · 끝은 메인 Handler 메시지로 정한다. Compose 의 메인 디스패처(AndroidUiDispatcher)는 코루틴을 이어 갈 때마다
 * Choreographer 프레임 콜백도 요청하므로(dispatch → postFrameCallback), 창 안에서 코루틴이 깨면 측정 자체가 프레임 콜백 하나를 만든다.
 */
internal class FrameWindowMeter(private val window: Window) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val thread = HandlerThread("hidden-spinner-frames").apply { start() }
    private val reportHandler = Handler(thread.looper)
    private val rendered = AtomicInteger()
    private val dropped = AtomicInteger()

    @Volatile
    private var startNanos = Long.MAX_VALUE

    @Volatile
    private var endNanos = Long.MAX_VALUE

    /** 메인 스레드에서만 바꾼다 */
    private var callbacks = 0
    private var logging = false
    private var listening = false
    private var pending: List<Runnable> = emptyList()

    private val printer = Printer { line -> if (isVsyncDispatch(line)) callbacks++ }

    // debug 변형은 HotSwan 때문에 minSdk 26 이라 lintDebug 가 아래 가드를 ObsoleteSdkInt 로 센다 — release(minSdk 24)에는 필요한 가드
    @SuppressLint("ObsoleteSdkInt")
    private val listener = Window.OnFrameMetricsAvailableListener { _, metrics, dropCount ->
        dropped.addAndGet(dropCount)
        val counted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            inWindow(metrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP), startNanos, endNanos)
        } else {
            // API 24 · 25 는 겨냥한 VSync 시각이 없어 리스너를 붙여 둔 동안 온 보고를 모두 센다
            true
        }
        if (counted) rendered.incrementAndGet()
    }

    /** 안정 대기 → 창 → 늦은 보고 대기. 취소되면(카드가 화면에서 빠지면) 로그 · 리스너를 그 자리에서 뗀다 */
    suspend fun measure(case: FrameCase, refreshHz: Float, lab: HiddenSpinnerFrameLab): FrameWindowResult =
        suspendCancellableCoroutine { cont ->
            var drawsAtStart = 0
            var drawsInWindow = 0
            var windowMillis = 0L
            val finish = Runnable {
                stopListening()
                pending = emptyList()
                cont.resume(
                    FrameWindowResult(
                        case = case,
                        callbacks = callbacks,
                        rendered = rendered.get(),
                        droppedReports = dropped.get(),
                        spinnerDraws = drawsInWindow,
                        windowMillis = windowMillis,
                        refreshHz = refreshHz,
                        touched = lab.touchedInWindow
                    )
                )
            }
            val end = Runnable {
                stopLogging()
                endNanos = System.nanoTime()
                lab.measuring = false
                drawsInWindow = lab.spinnerDraws - drawsAtStart
                windowMillis = (endNanos - startNanos) / 1_000_000L
                mainHandler.postDelayed(finish, LATE_REPORT_MILLIS)
            }
            val start = Runnable {
                callbacks = 0
                rendered.set(0)
                dropped.set(0)
                lab.touchedInWindow = false
                lab.measuring = true
                drawsAtStart = lab.spinnerDraws
                endNanos = Long.MAX_VALUE
                startNanos = System.nanoTime()
                startListening()
                Looper.getMainLooper().setMessageLogging(printer)
                logging = true
                mainHandler.postDelayed(end, WINDOW_MILLIS)
            }
            pending = listOf(start, end, finish)
            mainHandler.postDelayed(start, SETTLE_MILLIS)
            cont.invokeOnCancellation {
                if (Looper.myLooper() == Looper.getMainLooper()) cancelPending(lab) else mainHandler.post { cancelPending(lab) }
            }
        }

    /** 카드가 사라질 때 — 남은 메시지 · 로그 · 리스너 · 스레드를 모두 정리한다 */
    fun close() {
        pending.forEach { mainHandler.removeCallbacks(it) }
        pending = emptyList()
        stopLogging()
        stopListening()
        thread.quitSafely()
    }

    private fun cancelPending(lab: HiddenSpinnerFrameLab) {
        pending.forEach { mainHandler.removeCallbacks(it) }
        pending = emptyList()
        lab.measuring = false
        stopLogging()
        stopListening()
    }

    private fun startListening() {
        if (listening) return
        window.addOnFrameMetricsAvailableListener(listener, reportHandler)
        listening = true
    }

    /** 붙인 적 없는 리스너를 떼면 IllegalArgumentException 이라 listening 으로 거른다 */
    private fun stopListening() {
        if (!listening) return
        window.removeOnFrameMetricsAvailableListener(listener)
        listening = false
    }

    /** 이 앱은 다른 Looper Printer 를 쓰지 않는다(setMessageLogging 사용처 0) — 창이 끝나면 null 로 되돌린다 */
    private fun stopLogging() {
        if (!logging) return
        Looper.getMainLooper().setMessageLogging(null)
        logging = false
    }
}
