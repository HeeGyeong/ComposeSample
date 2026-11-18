package com.example.composesample.presentation.example.component.ui.canvas

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.FrameMetrics
import android.view.Window
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Collections
import kotlin.coroutines.resume

/**
 * 그리기 무효화가 다시 기록하는 범위(#182) — WaveformCanvasExampleUI 의 4번 카드가 쓴다.
 * 위쪽(변형 · 기대 · 판정 · 중앙값 · 로그 줄)은 Android 클래스 없이 단위 테스트(RecordScopeLabTest)로 검증한다.
 *
 * 근거(ui 1.12.1 · foundation 1.12.1 · material3 1.4.0 소스):
 * - DrawModifierNode.invalidateDraw → requireCoordinator(Nodes.Any).invalidateLayer() — 자기 coordinator 에 레이어가 없으면
 *   wrappedBy 를 따라 올라가 처음 만나는 레이어를 invalidate 한다. 드로우 람다 안의 상태 읽기도 그 레이어의 drawBlock 안에서
 *   관찰된다(onCommitAffectingLayer = layer.invalidate) → 그 레이어에 담긴 '레이어 없는 노드' 전부가 다시 기록된다
 * - 끝까지 없으면 owner.invalidateRootLayer() 지만, RootMeasurePolicy 가 루트 자식을 placeRelativeWithLayer 로 놓으므로
 *   그때 다시 기록되는 것은 화면 콘텐츠 전체다(원문의 '화면 루트까지')
 * - 레이어를 만드는 것: graphicsLayer · clip(shape)(= graphicsLayer(shape, clip = true)) · 그림자 있는 Surface/Card(그리고 그 clip) ·
 *   verticalScroll 콘텐츠 · LazyColumn 아이템(LazyListMeasuredItem 이 항상 placeWithLayer) → 이 화면에서는 카드에서 멈춘다
 * - 다시 기록되는 레이어 안의 자식 레이어는 더럽지 않으면 기록을 재사용한다(GraphicsLayerOwnerLayer.updateDisplayList 는 isDirty 일 때만)
 * - graphicsLayer 는 LayoutModifierNode 라 '자기 뒤' 노드들을 레이어 안에 둔다 → drawBehind 보다 앞에 와야 경계가 된다
 */

/** 움직이는 글로우에 레이어를 주는 방법 */
internal enum class GlowLayer(val label: String, val code: String) {
    NONE("레이어 없음", ".size(48.dp).drawBehind { tick }"),
    LAYER_BEFORE("graphicsLayer() 앞", ".size(48.dp).graphicsLayer().drawBehind { tick }"),
    LAYER_AFTER("graphicsLayer() 뒤", ".size(48.dp).drawBehind { tick }.graphicsLayer()"),
    CLIP_BEFORE("clip() 앞", ".size(48.dp).clip(CircleShape).drawBehind { tick }")
}

/** 한 창에서 그리기 횟수를 세는 노드 */
internal enum class RecordTarget(val label: String) {
    GLOW("글로우"),
    PLAIN_NEIGHBOR("이웃(레이어 없음)"),
    LAYERED_NEIGHBOR("이웃(자기 레이어)")
}

/** 이웃이 그리는 양 — 다시 기록되는 비용이 '무엇이 같은 레이어에 있느냐'에 따라 달라짐을 보인다 */
internal enum class NeighborLoad(val tiles: Int, val label: String) {
    LIGHT(1, "이웃 타일 1개"),
    HEAVY(400, "이웃 타일 400개")
}

internal enum class RecordVerdict(val label: String) {
    EVERY_FRAME("매 프레임 다시 기록"),
    REUSED("재사용(0회)")
}

/** 소스로 본 기대 — 글로우는 늘 매 프레임, 자기 레이어가 있는 이웃은 늘 재사용, 레이어 없는 이웃은 글로우에 경계가 없을 때만 다시 기록 */
internal fun expectedVerdict(layer: GlowLayer, target: RecordTarget): RecordVerdict = when (target) {
    RecordTarget.GLOW -> RecordVerdict.EVERY_FRAME
    RecordTarget.LAYERED_NEIGHBOR -> RecordVerdict.REUSED
    RecordTarget.PLAIN_NEIGHBOR -> when (layer) {
        GlowLayer.NONE, GlowLayer.LAYER_AFTER -> RecordVerdict.EVERY_FRAME
        GlowLayer.LAYER_BEFORE, GlowLayer.CLIP_BEFORE -> RecordVerdict.REUSED
    }
}

/** 기준(글로우는 그린 프레임 수, 이웃은 글로우 그리기 수)의 이 비율 이상이면 '매 프레임' */
internal const val EVERY_FRAME_RATIO = 0.8f

/** 센 수를 범주로 — 0 이면 재사용, 기준의 80% 이상이면 매 프레임, 그 사이는 null(판정 불가 — 다른 무언가가 가끔 다시 기록시킨다) */
internal fun classifyRecord(count: Int, reference: Int): RecordVerdict? = when {
    count == 0 -> RecordVerdict.REUSED
    reference > 0 && count >= reference * EVERY_FRAME_RATIO -> RecordVerdict.EVERY_FRAME
    else -> null
}

/** 중앙값(나노초 → 밀리초). 표본이 없으면 null */
internal fun medianMillis(nanos: List<Long>): Double? {
    if (nanos.isEmpty()) return null
    val sorted = nanos.sorted()
    val mid = sorted.size / 2
    val median = if (sorted.size % 2 == 1) sorted[mid].toDouble() else (sorted[mid - 1] + sorted[mid]) / 2.0
    return median / 1_000_000.0
}

/** 창 하나의 결과 */
internal data class RecordWindowResult(
    val layer: GlowLayer,
    val load: NeighborLoad,
    val frames: Int,
    val glowDraws: Int,
    val plainDraws: Int,
    val layeredDraws: Int,
    val drawMillisMedian: Double?,
    val windowMillis: Long,
    val touched: Boolean
) {
    fun count(target: RecordTarget): Int = when (target) {
        RecordTarget.GLOW -> glowDraws
        RecordTarget.PLAIN_NEIGHBOR -> plainDraws
        RecordTarget.LAYERED_NEIGHBOR -> layeredDraws
    }

    fun verdict(target: RecordTarget): RecordVerdict? =
        classifyRecord(count(target), if (target == RecordTarget.GLOW) frames else glowDraws)

    /** 세 노드 모두 소스로 본 기대와 같은가 — 재는 동안 화면을 건드렸으면 판정하지 않는다 */
    val matchesExpectation: Boolean
        get() = !touched && RecordTarget.entries.all { verdict(it) == expectedVerdict(layer, it) }

    /** adb 로 결과를 모으는 줄(태그 RecordScope) */
    fun probeLine(): String =
        "layer=${layer.name} load=${load.name} frames=$frames glow=$glowDraws plain=$plainDraws layered=$layeredDraws " +
            "drawMedianUs=${drawMillisMedian?.let { (it * 1000).toInt() } ?: -1} window=$windowMillis touched=$touched ok=$matchesExpectation"
}

/** 같은 부하의 레이어 없음 ↔ graphicsLayer() 앞 중앙값 차이(ms) — 둘 중 하나라도 없으면 null */
internal fun savedMillis(results: List<RecordWindowResult>, load: NeighborLoad): Double? {
    val none = results.firstOrNull { it.load == load && it.layer == GlowLayer.NONE }?.drawMillisMedian ?: return null
    val layered = results.firstOrNull { it.load == load && it.layer == GlowLayer.LAYER_BEFORE }?.drawMillisMedian ?: return null
    return none - layered
}

/** 변형을 바꾼 뒤 레이어 생성 · 첫 기록이 끝나기를 기다리는 시간 · 창 · 늦은 FrameMetrics 보고 대기 */
internal const val RECORD_SETTLE_MILLIS = 800L
internal const val RECORD_WINDOW_MILLIS = 2_000L
internal const val RECORD_LATE_REPORT_MILLIS = 300L

// ==================== 여기부터 Android ====================

/**
 * 그리기 람다가 올리는 횟수 — 상태가 아닌 평범한 필드다(상태로 두면 그 쓰기가 다시 무효화를 부른다).
 * touched 는 창 동안 카드를 건드렸는지(스크롤 · 탭이 같은 카드를 다시 기록시킨다).
 */
internal class RecordScopeCounters {
    var glow = 0
    var plain = 0
    var layered = 0
    var touched = false
}

/**
 * 창 하나 동안 그린 프레임 수와 프레임마다의 DRAW_DURATION(UI 스레드가 표시 목록을 기록한 시간)을 모은다.
 * 창의 시작 · 끝은 메인 Handler 메시지로 정한다(#177 FrameWindowMeter 와 같은 이유 — 코루틴을 깨우면 그 자체가 프레임을 부른다).
 */
internal class RecordScopeMeter(private val window: Window) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val thread = HandlerThread("record-scope-frames").apply { start() }
    private val reportHandler = Handler(thread.looper)
    private val drawNanos: MutableList<Long> = Collections.synchronizedList(mutableListOf())

    @Volatile
    private var startNanos = Long.MAX_VALUE

    @Volatile
    private var endNanos = Long.MAX_VALUE

    private var listening = false
    private var pending: List<Runnable> = emptyList()

    // debug 변형은 HotSwan 때문에 minSdk 26 이라 lintDebug 가 아래 가드를 ObsoleteSdkInt 로 센다 — release(minSdk 24)에는 필요한 가드
    @SuppressLint("ObsoleteSdkInt")
    private val listener = Window.OnFrameMetricsAvailableListener { _, metrics, _ ->
        val counted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vsync = metrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP)
            vsync in startNanos until endNanos
        } else {
            // API 24 · 25 는 겨냥한 VSync 시각이 없어 리스너를 붙여 둔 동안 온 보고를 모두 센다
            true
        }
        if (counted) drawNanos += metrics.getMetric(FrameMetrics.DRAW_DURATION)
    }

    /** 안정 대기 → 창 → 늦은 보고 대기. 취소되면(카드가 화면에서 빠지면) 메시지 · 리스너를 그 자리에서 뗀다 */
    suspend fun measure(layer: GlowLayer, load: NeighborLoad, counters: RecordScopeCounters): RecordWindowResult =
        suspendCancellableCoroutine { cont ->
            var glowAt = 0
            var plainAt = 0
            var layeredAt = 0
            var glow = 0
            var plain = 0
            var layered = 0
            var windowMillis = 0L
            val finish = Runnable {
                stopListening()
                pending = emptyList()
                val samples = synchronized(drawNanos) { drawNanos.toList() }
                cont.resume(
                    RecordWindowResult(
                        layer = layer,
                        load = load,
                        frames = samples.size,
                        glowDraws = glow,
                        plainDraws = plain,
                        layeredDraws = layered,
                        drawMillisMedian = medianMillis(samples),
                        windowMillis = windowMillis,
                        touched = counters.touched
                    )
                )
            }
            val end = Runnable {
                endNanos = System.nanoTime()
                glow = counters.glow - glowAt
                plain = counters.plain - plainAt
                layered = counters.layered - layeredAt
                windowMillis = (endNanos - startNanos) / 1_000_000L
                mainHandler.postDelayed(finish, RECORD_LATE_REPORT_MILLIS)
            }
            val start = Runnable {
                drawNanos.clear()
                counters.touched = false
                glowAt = counters.glow
                plainAt = counters.plain
                layeredAt = counters.layered
                endNanos = Long.MAX_VALUE
                startNanos = System.nanoTime()
                startListening()
                mainHandler.postDelayed(end, RECORD_WINDOW_MILLIS)
            }
            pending = listOf(start, end, finish)
            mainHandler.postDelayed(start, RECORD_SETTLE_MILLIS)
            cont.invokeOnCancellation {
                if (Looper.myLooper() == Looper.getMainLooper()) cancelPending() else mainHandler.post { cancelPending() }
            }
        }

    /** 카드가 사라질 때 — 남은 메시지 · 리스너 · 스레드를 모두 정리한다 */
    fun close() {
        cancelPending()
        thread.quitSafely()
    }

    private fun cancelPending() {
        pending.forEach { mainHandler.removeCallbacks(it) }
        pending = emptyList()
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
}
