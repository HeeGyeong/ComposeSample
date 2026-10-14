package com.example.composesample.presentation.example.component.system.platform.display

import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * FrameMetrics 단계별 프레임 시간 — DisplayRefreshRateExampleUI 의 5번 카드가 쓴다.
 * 위쪽 집계(FrameStageMeter)는 Android 클래스 없이 단위 테스트(FrameStageMeterTest)로 검증한다.
 */

// ==================== 단계 정의와 집계 (Android 밖에서 단독으로 검증 가능) ====================

/** FrameMetrics 가 나눠 주는 단계 — 파이프라인 순서(UI 스레드 → RenderThread → GPU) */
internal enum class FrameStage(val label: String, val where: String) {
    DELAY("시작 지연", "UI"),
    INPUT("입력", "UI"),
    ANIMATION("애니메이션", "UI"),
    LAYOUT("측정·배치", "UI"),
    DRAW("그리기 기록", "UI"),
    SYNC("동기화", "UI→RT"),
    COMMAND("명령 발행", "RT"),
    SWAP("버퍼 교체", "RT"),
    GPU("GPU", "GPU")
}

private val STAGE_COUNT = FrameStage.entries.size
private const val NANOS_IN_MILLI = 1_000_000f

/** 2초 창 하나의 단계별 집계 */
internal data class FrameStageReport(
    val sequence: Int,
    /** 집계한 프레임 수(새 창 레이아웃의 첫 프레임은 뺀다) */
    val frames: Int,
    /** 리스너가 늦어 시스템이 건너뛴 보고 수(dropCountSinceLastInvocation 합) */
    val droppedReports: Int,
    /** FIRST_DRAW_FRAME — 창이 새로 그려진 첫 프레임은 원래 느려서 끊김 판정에서 뺀다 */
    val firstDrawFrames: Int,
    /** 판정 기준 평균(ms) — API 31+ 는 프레임마다 DEADLINE, 그 미만은 1/주사율 */
    val limitMs: Float,
    val usesDeadline: Boolean,
    /** TOTAL_DURATION 이 기준을 넘은 프레임 */
    val overLimit: Int,
    val totalAverageMs: Float,
    val totalWorstMs: Float,
    /** FrameStage 순서 */
    val averageMs: List<Float>,
    val maxMs: List<Float>,
    /** TOTAL 이 가장 길었던 프레임의 단계별 시간 */
    val worstFrameMs: List<Float>,
    /** 기준을 넘은 프레임을 가장 오래 붙잡은 단계별 개수 */
    val overByStage: List<Int>
)

/** 가장 오래 걸린 단계의 순번 — 같으면 파이프라인 앞쪽 */
internal fun dominantStage(stagesNanos: LongArray): Int {
    var best = 0
    for (i in 1 until stagesNanos.size) {
        if (stagesNanos[i] > stagesNanos[best]) best = i
    }
    return best
}

/**
 * 프레임마다 단계별 시간을 받아 창 길이(기본 2초)가 차면 집계를 돌려준다.
 * 한 스레드(FrameMetrics 리스너의 HandlerThread)에서만 부른다.
 */
internal class FrameStageMeter(private val windowNanos: Long = 2_000_000_000L) {
    private val sums = LongArray(STAGE_COUNT)
    private val maxes = LongArray(STAGE_COUNT)
    private val worst = LongArray(STAGE_COUNT)
    private val overByStage = IntArray(STAGE_COUNT)
    private var frames = 0
    private var dropped = 0
    private var firstDraw = 0
    private var over = 0
    private var totalSum = 0L
    private var totalWorst = 0L
    private var limitSum = 0L
    private var deadlineFrames = 0
    private var windowStart = -1L
    private var sequence = 0

    /** 다음 프레임부터 창을 새로 시작한다(부하를 바꿨을 때 — 서로 다른 조건이 한 창에 섞이지 않게) */
    fun reset() {
        sums.fill(0L)
        maxes.fill(0L)
        worst.fill(0L)
        overByStage.fill(0)
        frames = 0
        dropped = 0
        firstDraw = 0
        over = 0
        totalSum = 0L
        totalWorst = 0L
        limitSum = 0L
        deadlineFrames = 0
        windowStart = -1L
    }

    /**
     * stagesNanos 는 FrameStage 순서(없는 단계는 0), deadlineNanos 는 API 31+ DEADLINE(없으면 0),
     * budgetNanos 는 DEADLINE 이 없을 때의 기준(1/주사율), nowNanos 는 창 길이를 재는 단조 시각.
     */
    fun record(
        stagesNanos: LongArray,
        totalNanos: Long,
        deadlineNanos: Long,
        budgetNanos: Long,
        firstDrawFrame: Boolean,
        droppedSinceLast: Int,
        nowNanos: Long
    ): FrameStageReport? {
        if (windowStart < 0) windowStart = nowNanos
        dropped += droppedSinceLast
        if (firstDrawFrame) {
            firstDraw++
        } else {
            frames++
            for (i in 0 until STAGE_COUNT) {
                sums[i] += stagesNanos[i]
                if (stagesNanos[i] > maxes[i]) maxes[i] = stagesNanos[i]
            }
            totalSum += totalNanos
            if (totalNanos > totalWorst) {
                totalWorst = totalNanos
                stagesNanos.copyInto(worst)
            }
            val limit = if (deadlineNanos > 0) {
                deadlineFrames++
                deadlineNanos
            } else {
                budgetNanos
            }
            limitSum += limit
            if (totalNanos > limit) {
                over++
                overByStage[dominantStage(stagesNanos)]++
            }
        }
        if (frames == 0 || nowNanos - windowStart < windowNanos) return null
        val report = FrameStageReport(
            sequence = ++sequence,
            frames = frames,
            droppedReports = dropped,
            firstDrawFrames = firstDraw,
            limitMs = limitSum / frames / NANOS_IN_MILLI,
            usesDeadline = deadlineFrames == frames,
            overLimit = over,
            totalAverageMs = totalSum / frames / NANOS_IN_MILLI,
            totalWorstMs = totalWorst / NANOS_IN_MILLI,
            averageMs = sums.map { it / frames / NANOS_IN_MILLI },
            maxMs = maxes.map { it / NANOS_IN_MILLI },
            worstFrameMs = worst.map { it / NANOS_IN_MILLI },
            overByStage = overByStage.toList()
        )
        reset()
        windowStart = nowNanos
        return report
    }
}

// ==================== Window 에 붙이는 수집기 ====================

/**
 * Window 에 FrameMetrics 리스너를 붙여 별도 HandlerThread 에서 받는다. 보고 객체는 콜백 안에서만 유효해 그 자리에서 숫자만 꺼낸다.
 * 2초 창 집계가 차면 메인 스레드로 넘겨 report 상태에 싣는다 — 컴포저블 밖 상태 홀더라 HotSwan 의 지역 변수 쓰기 함정이 없다.
 * 보고는 렌더링된 프레임마다 온다(Window.OnFrameMetricsAvailableListener 문서) — 콜백이 늦으면 dropCountSinceLastInvocation 으로 건너뛴 수를 알려 준다.
 */
internal class FrameMetricsRecorder(private val window: Window) : Window.OnFrameMetricsAvailableListener {
    private val thread = HandlerThread("frame-metrics").apply { start() }
    private val handler = Handler(thread.looper)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val meter = FrameStageMeter()
    private val stages = LongArray(STAGE_COUNT)
    private var started = false

    /** 부하를 바꿀 때마다 올린다 — 바꾸기 전 창의 집계가 메인 큐에 남아 있다가 새 화면에 실리지 않게 */
    @Volatile
    private var generation = 0

    /** DEADLINE 이 없는(API 31 미만) 기기의 기준 — 메인 스레드가 주사율이 바뀔 때 고친다 */
    @Volatile
    var budgetNanos: Long = 16_666_667L

    var report by mutableStateOf<FrameStageReport?>(null)
        private set

    fun start() {
        if (started) return
        started = true
        window.addOnFrameMetricsAvailableListener(this, handler)
    }

    /** 리스너를 떼고 스레드를 닫는다 — 붙인 적 없는 리스너를 떼면 IllegalArgumentException 이라 started 로 거른다 */
    fun stop() {
        if (started) window.removeOnFrameMetricsAvailableListener(this)
        started = false
        thread.quitSafely()
    }

    /** 부하를 바꿨을 때 — 지금 창을 버리고 화면 값도 비운다 */
    fun resetWindow() {
        generation++
        report = null
        handler.post { meter.reset() }
    }

    override fun onFrameMetricsAvailable(window: Window, frameMetrics: FrameMetrics, dropCountSinceLastInvocation: Int) {
        stages[FrameStage.DELAY.ordinal] = frameMetrics.getMetric(FrameMetrics.UNKNOWN_DELAY_DURATION)
        stages[FrameStage.INPUT.ordinal] = frameMetrics.getMetric(FrameMetrics.INPUT_HANDLING_DURATION)
        stages[FrameStage.ANIMATION.ordinal] = frameMetrics.getMetric(FrameMetrics.ANIMATION_DURATION)
        stages[FrameStage.LAYOUT.ordinal] = frameMetrics.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION)
        stages[FrameStage.DRAW.ordinal] = frameMetrics.getMetric(FrameMetrics.DRAW_DURATION)
        stages[FrameStage.SYNC.ordinal] = frameMetrics.getMetric(FrameMetrics.SYNC_DURATION)
        stages[FrameStage.COMMAND.ordinal] = frameMetrics.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION)
        stages[FrameStage.SWAP.ordinal] = frameMetrics.getMetric(FrameMetrics.SWAP_BUFFERS_DURATION)
        // GPU_DURATION · DEADLINE 은 API 31 에 생겼다
        val sinceS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        stages[FrameStage.GPU.ordinal] = if (sinceS) frameMetrics.getMetric(FrameMetrics.GPU_DURATION) else 0L
        val deadline = if (sinceS) frameMetrics.getMetric(FrameMetrics.DEADLINE) else 0L
        val gen = generation
        meter.record(
            stagesNanos = stages,
            totalNanos = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION),
            deadlineNanos = deadline,
            budgetNanos = budgetNanos,
            firstDrawFrame = frameMetrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 1L,
            droppedSinceLast = dropCountSinceLastInvocation,
            nowNanos = System.nanoTime()
        )?.let { next -> mainHandler.post { if (gen == generation) report = next } }
    }
}
