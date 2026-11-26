package com.example.composesample.presentation.example.component.ui.material3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Printer
import android.view.FrameMetrics
import android.view.View
import android.view.Window
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.composesample.presentation.example.component.ui.visibility.inWindow
import com.example.composesample.presentation.example.component.ui.visibility.isVsyncDispatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.roundToLong

/*
 * M3 1.5.0 Expressive 정식 컴포넌트 보강(#188) — Wavy 진행 표시 · SplitButtonLayout · HorizontalFloatingToolbar ·
 * HorizontalCenteredHeroCarousel 을 실물로 두고 잰다(카드는 ExpressiveComponentCards.kt).
 * 위쪽(사례 · 기대 · 판정 · 식)은 Android 없이 단위 테스트(ExpressiveComponentLabTest)로 검증한다.
 */

// ==================== ① Wavy 진행 표시 — 그린 프레임과 시계 ====================

/** 재는 지시기의 모양 — Classic 은 기존(파도 없는) LinearProgressIndicator */
enum class WavyShape { Linear, Circular, Classic }

/** 재는 줄 — 진행값과 파도 속도(stillWave = waveSpeed 0.dp) */
enum class WavyCase(val label: String, val shape: WavyShape, val progress: Float, val stillWave: Boolean) {
    LinearMid("선형 50%", WavyShape.Linear, 0.5f, false),
    LinearEarly("선형 5%", WavyShape.Linear, 0.05f, false),
    LinearDone("선형 100%", WavyShape.Linear, 1f, false),
    LinearStill("선형 50% · waveSpeed 0", WavyShape.Linear, 0.5f, true),
    CircularMid("원형 50%", WavyShape.Circular, 0.5f, false),
    CircularDone("원형 100%", WavyShape.Circular, 1f, false),
    Classic("기존 LinearProgressIndicator 50%", WavyShape.Classic, 0.5f, false)
}

/**
 * M3 1.5.0 WavyProgressIndicatorDefaults.indicatorAmplitude 와 같은 식 — 10% 이하 · 95% 이상이면 파도 높이 0.
 * 단위 테스트가 라이브러리의 실제 람다와 대조한다(라이브러리가 바뀌면 거기서 깨진다)
 */
fun defaultWavyAmplitude(progress: Float): Float = if (progress <= 0.1f || progress >= 0.95f) 0f else 1f

/** 창 하나에서 본 빈도 — 쉬지 않고 계속이냐 0 이냐 */
enum class Rate(val label: String) {
    Continuous("계속"),
    None("0")
}

/** 그리기 기대 — 파도 높이가 0 이거나 파도가 멈추면, 값이 그대로인 동안 다시 그리지 않는다 */
fun expectedDraws(case: WavyCase): Rate = when {
    case.shape == WavyShape.Classic -> Rate.None
    case.stillWave -> Rate.None
    defaultWavyAmplitude(case.progress) == 0f -> Rate.None
    else -> Rate.Continuous
}

/**
 * 시계(Recomposer 가 프레임마다 깨어나는가 — 메인 스레드의 Choreographer 프레임 콜백) 기대 — material3 1.5.0 소스:
 * - 선형(LinearWavyProgressModifiers.updateOffsetAnimation): waveSpeed · wavelength > 0 이면 파도 오프셋 무한 애니메이션을 띄운다.
 *   파도 높이는 보지 않는다 → 0% · 100% 처럼 그리지 않는 동안에도 매 프레임 오프셋을 쓴다(그리기는 높이 0 이면 오프셋을 안 읽는다)
 * - 원형(CircularWavyProgressModifiers.startOffsetAnimation): `!isDrawingWave()` 면 시작하지 않고, 높이 애니메이션이 0 에 닿으면 멈춘다
 */
fun expectedClock(case: WavyCase): Rate = when (case.shape) {
    WavyShape.Classic -> Rate.None
    WavyShape.Linear -> if (case.stillWave) Rate.None else Rate.Continuous
    WavyShape.Circular ->
        if (case.stillWave || defaultWavyAmplitude(case.progress) == 0f) Rate.None else Rate.Continuous
}

/**
 * 초당 이만큼 이상이면 '계속'. #177 은 VSync 수의 25% 를 썼지만, 파도를 그리는 동안 Pixel Tablet API 35 에뮬
 * (2560×1600 소프트웨어 렌더, debug)은 초당 5 프레임 남짓이라 그 기준을 못 넘는다. 무대에는 지시기 말고 깨우는 것이 없어
 * (글자 입력 커서 · 다른 애니메이션 없음) 낮은 문턱으로도 '가끔'과 '계속'이 갈린다
 */
const val FRAME_CONTINUOUS_PER_SECOND = 3f

/** 센 수를 범주로 — 0 도 계속도 아니면 null(판정 불가 — 다른 무언가가 가끔 깨운다) */
fun classifyFrames(count: Int, windowMillis: Long): Rate? {
    if (count == 0) return Rate.None
    return if (count >= windowMillis / 1000f * FRAME_CONTINUOUS_PER_SECOND) Rate.Continuous else null
}

/** 한 줄을 잰 결과 — callbacks = 메인 Looper 의 VSync 디스패치(시계), rendered = 창이 그려 보낸 프레임(FrameMetrics) */
data class WavyRun(
    val callbacks: Int,
    val rendered: Int,
    val windowMillis: Long
) {
    val drawRate: Rate? get() = classifyFrames(rendered, windowMillis)
    val clockRate: Rate? get() = classifyFrames(callbacks, windowMillis)
    fun matches(case: WavyCase): Boolean = drawRate == expectedDraws(case) && clockRate == expectedClock(case)
}

/** 측정 창 · 앞에서 버리는 시간(첫 컴포지션 · 배치 · 그리기) · 창 뒤 늦게 오는 FrameMetrics 보고 대기 */
const val WAVY_WINDOW_MILLIS = 2000L
const val WAVY_WARMUP_MILLIS = 600L
const val WAVY_LATE_REPORT_MILLIS = 300L

/** 상태가 아닌 평범한 필드 — 세는 행위가 다시 그리기 · 리컴포지션을 부르지 않게 */
class PlainCounter {
    var count = 0
}

/**
 * Wavy 카드의 측정 — 화면 전체를 지시기 하나만 있는 무대로 바꿔(목록을 컴포지션에서 뺀다) 줄마다 프레임 콜백과 그린 프레임을 센다.
 * 목록을 남겨 두면 다른 카드의 무한 애니메이션 · 커서가 같은 창을 깨워 섞인다.
 * 지시기를 감싼 레이어의 그리기 람다로 세지 않는 까닭: 선형은 안에 clipToBounds(= graphicsLayer(clip = true)) 레이어가 있어
 * 파도가 움직여도 그 레이어만 다시 기록한다 — 바깥 람다는 0 이었다(원형은 레이어가 없어 바깥에서 보인다)
 */
class WavyLabState {
    /** null 이 아니면 화면이 그 줄의 무대 */
    var stage by mutableStateOf<WavyCase?>(null)
        private set
    var running by mutableStateOf(false)
        private set
    val results = mutableStateMapOf<WavyCase, WavyRun>()
    private var job: Job? = null

    /** 화면 수준 scope 로 띄운다 — 카드의 scope 는 무대로 바뀌며 목록이 빠질 때 함께 취소된다 */
    fun start(scope: CoroutineScope, view: View) {
        if (running) return
        job = scope.launch { runAll(view) }
    }

    fun stop() {
        job?.cancel()
    }

    private suspend fun runAll(view: View) {
        val window = view.findWindow() ?: return
        running = true
        results.clear()
        val meter = WavyFrameMeter(window)
        try {
            for (case in WavyCase.entries) {
                stage = case
                val (callbacks, rendered, millis) = meter.measure()
                results[case] = WavyRun(callbacks, rendered, millis)
            }
        } finally {
            meter.close()
            stage = null
            running = false
        }
    }
}

private fun View.findWindow(): Window? {
    var c: Context? = context
    while (c is ContextWrapper) {
        if (c is Activity) return c.window
        c = c.baseContext
    }
    return null
}

/**
 * 창 하나 동안 프레임 콜백(메인 Looper 메시지 로그의 VSync 디스패치)과 그린 프레임(FrameMetrics)을 센다 — #177 FrameWindowMeter 와 같은 방식.
 * 창의 시작 · 끝은 메인 Handler 메시지로 정한다: Compose 메인 디스패처는 코루틴을 이어 갈 때마다 프레임 콜백도 요청해서,
 * 창 안에서 측정 코루틴이 깨면 그 자체로 콜백이 하나 생긴다
 */
private class WavyFrameMeter(private val window: Window) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val thread = HandlerThread("wavy-frames").apply { start() }
    private val reportHandler = Handler(thread.looper)
    private val rendered = AtomicInteger()

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
    private val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
        val counted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            inWindow(metrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP), startNanos, endNanos)
        } else {
            true // API 24 · 25 는 겨냥한 VSync 시각이 없어 붙여 둔 동안 온 보고를 모두 센다
        }
        if (counted) rendered.incrementAndGet()
    }

    /** 준비 대기 → 창 → 늦은 보고 대기. (콜백, 그린 프레임, 창 ms) */
    suspend fun measure(): Triple<Int, Int, Long> = suspendCancellableCoroutine { cont ->
        var millis = 0L
        val finish = Runnable {
            stopListening()
            pending = emptyList()
            cont.resume(Triple(callbacks, rendered.get(), millis))
        }
        val end = Runnable {
            stopLogging()
            endNanos = System.nanoTime()
            millis = (endNanos - startNanos) / 1_000_000L
            mainHandler.postDelayed(finish, WAVY_LATE_REPORT_MILLIS)
        }
        val start = Runnable {
            callbacks = 0
            rendered.set(0)
            endNanos = Long.MAX_VALUE
            startNanos = System.nanoTime()
            startListening()
            Looper.getMainLooper().setMessageLogging(printer)
            logging = true
            mainHandler.postDelayed(end, WAVY_WINDOW_MILLIS)
        }
        pending = listOf(start, end, finish)
        mainHandler.postDelayed(start, WAVY_WARMUP_MILLIS)
        cont.invokeOnCancellation { mainHandler.post { cancelPending() } }
    }

    fun close() {
        cancelPending()
        thread.quitSafely()
    }

    private fun cancelPending() {
        pending.forEach { mainHandler.removeCallbacks(it) }
        pending = emptyList()
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

    /** 이 앱의 Looper Printer 사용처는 #177 계측기와 이것뿐이고 둘은 같은 화면에 함께 있지 않다 — 창이 끝나면 null 로 되돌린다 */
    private fun stopLogging() {
        if (!logging) return
        Looper.getMainLooper().setMessageLogging(null)
        logging = false
    }
}

// ==================== ② SplitButtonLayout — 폭 우선순위 · 같은 높이 · 광학 중앙 ====================

/** M3 1.5.0 HorizontalCenterOptically.kt 의 CenterOpticallyCoefficient */
const val CENTER_OPTICALLY_COEFFICIENT = 0.11f

/**
 * 광학 중앙 보정(dp) — 시작 · 끝 모서리 반지름 평균의 차 × 0.11, 안쪽 패딩 범위로 제한.
 * 음수면 시작 쪽(왼쪽)으로 민다 — 둥근 끝 쪽이 시각적으로 가벼워 내용을 반대로 옮긴다
 */
fun opticalOffsetDp(startCornerDp: Float, endCornerDp: Float, maxStartDp: Float, maxEndDp: Float): Float =
    (CENTER_OPTICALLY_COEFFICIENT * (startCornerDp - endCornerDp)).coerceIn(-maxStartDp, maxEndDp)

/** 작은(40dp) 스플릿 버튼 trailing — 안쪽 모서리 4dp(SplitButtonSmallTokens.InnerCornerCornerSize = ExtraSmall), 바깥 full */
const val SPLIT_HEIGHT_DP = 40f
const val SPLIT_INNER_CORNER_DP = 4f

/** 무대 폭(dp) — 넉넉함 · 보통 · 좁음 */
val SPLIT_STAGE_WIDTHS_DP = listOf(320, 220, 150)

/** 무대 하나의 배치(dp) */
data class SplitRow(
    val stageDp: Int,
    val leadingWidthDp: Float,
    val trailingWidthDp: Float,
    val leadingHeightDp: Float,
    val trailingHeightDp: Float
)

/** trailing 이 먼저 재지므로 무대가 좁아져도 trailing 폭은 그대로다 */
fun trailingKeptWidth(rows: List<SplitRow>, toleranceDp: Float = 1f): Boolean =
    rows.isNotEmpty() && rows.all { abs(it.trailingWidthDp - rows[0].trailingWidthDp) <= toleranceDp }

/** 줄어드는 것은 leading — 무대 폭 순(넓은 → 좁은)으로 leading 폭이 늘지 않고, 가장 좁은 무대에서 더 좁다 */
fun leadingAbsorbed(rows: List<SplitRow>, toleranceDp: Float = 1f): Boolean {
    val sorted = rows.sortedByDescending { it.stageDp }
    return sorted.size >= 2 &&
        sorted.zipWithNext().all { (wide, narrow) -> narrow.leadingWidthDp <= wide.leadingWidthDp + toleranceDp } &&
        sorted.last().leadingWidthDp < sorted.first().leadingWidthDp - toleranceDp
}

/** 두 버튼의 높이가 무대마다 같다 */
fun sameHeights(rows: List<SplitRow>, toleranceDp: Float = 1f): Boolean =
    rows.isNotEmpty() && rows.all { abs(it.leadingHeightDp - it.trailingHeightDp) <= toleranceDp }

// ==================== ④ HorizontalCenteredHeroCarousel — 보이는 칸 · 마스크 · 읽는 자리 ====================

/** 가운데 큰 칸 캐러셀에서 선택한 칸이 있을 때 보이는 칸(작은 · 큰 · 작은) — 양 끝에서는 시작 · 끝에 붙는다 */
fun expectedHeroVisible(selected: Int, count: Int): List<Int> = when {
    count < 3 -> (0 until count).toList()
    selected <= 0 -> listOf(0, 1, 2)
    selected >= count - 1 -> listOf(count - 3, count - 2, count - 1)
    else -> listOf(selected - 1, selected, selected + 1)
}

/** 칸 하나의 실측(px) — 배치 폭은 onPlaced, 마스크 폭 · 최소 · 최대는 그리기 단계에서 carouselItemDrawInfo */
data class HeroItem(val index: Int, val layoutWidth: Int, val maskWidth: Float, val minSize: Float, val maxSize: Float)

/** 배치 폭은 모두 큰 칸 폭 — 작은 칸은 잘라서(마스크) 보일 뿐 */
fun layoutAllLarge(items: List<HeroItem>, tolerancePx: Float = 1.5f): Boolean =
    items.isNotEmpty() && items.all { abs(it.layoutWidth - it.maxSize) <= tolerancePx }

/** 선택한 칸은 큰 칸(maxSize), 이웃은 작은 칸 범위 */
fun heroMasksMatch(items: List<HeroItem>, selected: Int, count: Int, tolerancePx: Float = 1.5f): Boolean {
    val visible = expectedHeroVisible(selected, count)
    val byIndex = items.associateBy { it.index }
    return visible.all { i ->
        val item = byIndex[i] ?: return false
        if (i == selected) abs(item.maskWidth - item.maxSize) <= tolerancePx
        else item.maskWidth < item.maxSize - tolerancePx && item.maskWidth >= item.minSize - tolerancePx
    }
}

/** 칸마다 그리기 단계에서 적어 두는 실측 — 평범한 필드(그리기에서 상태를 쓰면 다시 그리기 고리) */
class HeroProbe {
    var compositions = 0
    private val layoutWidths = HashMap<Int, Int>()
    private val masks = HashMap<Int, FloatArray>()

    fun placed(index: Int, width: Int) {
        layoutWidths[index] = width
    }

    fun drawn(index: Int, maskWidth: Float, minSize: Float, maxSize: Float) {
        masks[index] = floatArrayOf(maskWidth, minSize, maxSize)
    }

    /** 배치와 그리기를 모두 거친 칸 — 화면 밖으로 나간 칸은 마지막 값이 남아 있을 수 있어 보이는 칸만 판정에 쓴다 */
    fun snapshot(): List<HeroItem> = masks.keys.sorted().mapNotNull { i ->
        val w = layoutWidths[i] ?: return@mapNotNull null
        val m = masks.getValue(i)
        HeroItem(i, w, m[0], m[1], m[2])
    }
}

/** 칸 내용이 carouselItemDrawInfo 를 읽는 자리 */
enum class HeroReadPlace(val label: String) {
    Composition("컴포지션에서 읽기"),
    Draw("그리기에서 읽기")
}

/**
 * 두 자리를 한 칸 넘기기로 잰 칸 컴포지션 수 — 그리기에서 읽으면 새로 들어온 칸만 컴포즈된다.
 * 넘기는 동안의 프레임 수는 기기 속도에 달려 있어(느린 에뮬은 몇 프레임) 절대 문턱은 낮게 둔다
 */
fun compositionReadCostsMore(composition: Int, draw: Int): Boolean = composition >= 3 && composition > draw * 3

// ==================== 공통 표기 ====================

/** 소수 첫째 자리(로캘 무관 · 반올림) */
fun oneDecimal(value: Float): String {
    val tenths = (value * 10.0).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    val a = abs(tenths)
    return "$sign${a / 10}.${a % 10}"
}
