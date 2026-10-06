package com.example.composesample.presentation.example.component.system.platform.display

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.Display
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.FrameRateCategory
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.preferredFrameRate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.composesample.BuildConfig
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * 디스플레이 주사율 제어 & 프레임 간격 측정 예제
 * - 기기가 지원하는 디스플레이 모드(해상도·주사율)를 읽고, 창(Window) 단위로 원하는 모드를 요청한 뒤
 *   DisplayListener 로 실제로 바뀌었는지 확인한다. 요청은 힌트라서 사용자 설정·절전·발열이 이긴다.
 * - withFrameNanos 로 프레임 간격을 직접 재서 평균 FPS 가 숨기는 끊김(늦은 프레임 비율·p95/p99·최악 간격)을 드러낸다.
 * - Compose 1.12 Modifier.preferredFrameRate 는 API 35+ 에서만 View.requestedFrameRate 로 전달된다는 것을 함께 본다.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 프레임 간격 측정부 (Compose·Android 밖에서 단독으로 검증 가능) ====================

/** 늦은 프레임 기준 — 기대 간격의 1.5배(60Hz 면 25ms). 한 번의 vsync 를 놓친 프레임부터 걸린다. */
internal const val LATE_FRAME_FACTOR = 1.5f

/** 이보다 벌어진 간격은 프레임이 아니라 "화면이 멈춰 있던 시간"(백그라운드·화면 꺼짐)이라 버린다 */
internal const val PAUSE_GAP_NANOS = 1_000_000_000L

private const val NANOS_PER_SECOND = 1_000_000_000f
private const val NANOS_PER_MILLI = 1_000_000f

/** 2초 창 하나의 집계 결과 */
internal data class FramePacingReport(
    /** 몇 번째 창인지 — 화면 표의 key(행을 밀어 넣을 때 기존 행을 다시 레이아웃하지 않게) */
    val sequence: Int,
    val frames: Int,
    val fps: Float,
    val lateCount: Int,
    val latePercent: Float,
    val p95Ms: Float,
    val p99Ms: Float,
    val worstMs: Float,
    val refreshRateHz: Float
)

/** nearest-rank 백분위 — 정렬된 배열에서 ceil(p × n) 번째 값 */
internal fun percentileOfSorted(sorted: LongArray, fraction: Double): Long {
    require(sorted.isNotEmpty()) { "빈 배열의 백분위는 없다" }
    val rank = ceil(fraction * sorted.size).toInt().coerceIn(1, sorted.size)
    return sorted[rank - 1]
}

/** 프레임 간격(ns) 앞쪽 count 개를 집계한다. FPS 는 "프레임 수 ÷ 실제 흐른 시간"이다. */
internal fun summarizeIntervals(
    intervalsNanos: LongArray,
    count: Int,
    refreshRateHz: Float,
    sequence: Int = 0
): FramePacingReport? {
    if (count <= 0 || refreshRateHz <= 0f) return null
    val sorted = intervalsNanos.copyOf(count).also { it.sort() }
    val totalNanos = sorted.sum()
    val lateThreshold = NANOS_PER_SECOND / refreshRateHz * LATE_FRAME_FACTOR
    val late = sorted.count { it > lateThreshold }
    return FramePacingReport(
        sequence = sequence,
        frames = count,
        fps = count * NANOS_PER_SECOND / totalNanos,
        lateCount = late,
        latePercent = late * 100f / count,
        p95Ms = percentileOfSorted(sorted, 0.95) / NANOS_PER_MILLI,
        p99Ms = percentileOfSorted(sorted, 0.99) / NANOS_PER_MILLI,
        worstMs = sorted.last() / NANOS_PER_MILLI,
        refreshRateHz = refreshRateHz
    )
}

/**
 * 프레임 시각을 받아 간격을 쌓고, 창 길이(기본 2초)가 차면 집계를 돌려준다.
 * 화면 그래프용으로 최근 간격을 링 버퍼에 따로 남긴다. 메인 스레드에서만 쓴다.
 */
internal class FramePacingMeter(
    private val windowNanos: Long = 2_000_000_000L,
    capacity: Int = 1024,
    recentCapacity: Int = 90
) {
    private val intervals = LongArray(capacity)
    private var count = 0
    private var windowElapsed = 0L
    private var lastFrameNanos = -1L
    private var reportSequence = 0

    private val recent = LongArray(recentCapacity)
    private var recentHead = 0
    var recentSize = 0
        private set

    /** 오래된 것부터 i 번째 최근 간격(ns) */
    fun recentAt(index: Int): Long {
        val start = (recentHead - recentSize + recent.size) % recent.size
        return recent[(start + index) % recent.size]
    }

    /** 다음 프레임부터 창을 새로 시작한다(모드가 바뀌었거나 부하를 바꿨을 때 — 서로 다른 조건이 한 창에 섞이지 않게) */
    fun resetWindow() {
        count = 0
        windowElapsed = 0L
    }

    /** 측정을 처음부터 다시 시작한다 — 직전 프레임 시각까지 잊는다 */
    fun restart() {
        resetWindow()
        lastFrameNanos = -1L
        recentHead = 0
        recentSize = 0
    }

    fun record(frameNanos: Long, refreshRateHz: Float): FramePacingReport? {
        val last = lastFrameNanos
        lastFrameNanos = frameNanos
        if (last < 0) return null
        val interval = frameNanos - last
        if (interval <= 0L) return null
        if (interval > PAUSE_GAP_NANOS) {
            resetWindow()
            return null
        }
        recent[recentHead] = interval
        recentHead = (recentHead + 1) % recent.size
        if (recentSize < recent.size) recentSize++

        if (count < intervals.size) intervals[count++] = interval
        windowElapsed += interval
        if (windowElapsed < windowNanos) return null
        val report = summarizeIntervals(intervals, count, refreshRateHz, ++reportSequence)
        resetWindow()
        return report
    }
}

/** 프레임 콜백 안에서 메인 스레드를 붙잡아 실제 앱의 무거운 컴포지션·측정을 흉내 낸다 */
internal enum class FrameLoad(val label: String) {
    NONE("부하 없음"),
    SPIKE("60프레임마다 50ms 멈춤"),
    STEADY("매 프레임 13ms 작업");

    fun busyMillis(frameIndex: Long): Int = when (this) {
        NONE -> 0
        SPIKE -> if (frameIndex % 60L == 59L) 50 else 0
        STEADY -> 13
    }
}

private fun busyWait(millis: Int) {
    if (millis <= 0) return
    val end = System.nanoTime() + millis * 1_000_000L
    while (System.nanoTime() < end) {
        // sleep 과 달리 CPU 를 실제로 쓰며 메인 스레드를 붙잡는다
    }
}

// ==================== 디스플레이·창 제어부 ====================

internal data class DisplayModeInfo(
    val modeId: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
    /** 끊김 없이(seamless) 전환할 수 있는 다른 주사율 — API 31+ */
    val alternativeRates: List<Float>?
)

internal data class DisplaySnapshot(
    val modes: List<DisplayModeInfo>,
    val activeModeId: Int,
    val refreshRate: Float
)

private fun Display.Mode.toInfo(): DisplayModeInfo = DisplayModeInfo(
    modeId = modeId,
    width = physicalWidth,
    height = physicalHeight,
    refreshRate = refreshRate,
    alternativeRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alternativeRefreshRates.toList()
    } else {
        null
    }
)

private fun Display.snapshot(): DisplaySnapshot = DisplaySnapshot(
    modes = supportedModes.map { it.toInfo() }.sortedBy { it.modeId },
    activeModeId = mode.modeId,
    refreshRate = refreshRate
)

/** 시스템이 요청을 거절할 수 있는 조건들 */
internal data class SystemConditions(
    val powerSaveMode: Boolean,
    /** API 29+ 발열 상태, 미만은 null */
    val thermalStatus: String?,
    val sustainedSupported: Boolean
)

private fun readConditions(context: Context): SystemConditions {
    val pm = context.getSystemService(PowerManager::class.java)
    val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        thermalStatusName(pm.currentThermalStatus)
    } else {
        null
    }
    return SystemConditions(
        powerSaveMode = pm.isPowerSaveMode,
        thermalStatus = thermal,
        sustainedSupported = pm.isSustainedPerformanceModeSupported
    )
}

private fun thermalStatusName(status: Int): String = when (status) {
    PowerManager.THERMAL_STATUS_NONE -> "NONE"
    PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
    PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
    PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
    PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
    PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
    PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
    else -> "알 수 없음($status)"
}

/** 0 이면 "요청 없음" — 시스템이 정한다 */
private fun Window.requestDisplayMode(modeId: Int) {
    attributes = attributes.apply { preferredDisplayModeId = modeId }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Compose 의 FrameRateCategory 선택지 — 숫자 투표와 카테고리 투표는 따로 집계된다 */
private enum class ComposeFrameRateChoice(val label: String) {
    NONE("지정 안 함"),
    FPS_30("30fps"),
    FPS_60("60fps"),
    NORMAL("Normal"),
    HIGH("High")
}

private fun Modifier.preferredFrameRateOf(choice: ComposeFrameRateChoice): Modifier = when (choice) {
    ComposeFrameRateChoice.NONE -> this
    ComposeFrameRateChoice.FPS_30 -> preferredFrameRate(30f)
    ComposeFrameRateChoice.FPS_60 -> preferredFrameRate(60f)
    ComposeFrameRateChoice.NORMAL -> preferredFrameRate(FrameRateCategory.Normal)
    ComposeFrameRateChoice.HIGH -> preferredFrameRate(FrameRateCategory.High)
}

/** View.requestedFrameRate 는 API 35 에 생겼다. 음수는 카테고리 상수(-1 NO_PREFERENCE · -2 LOW · -3 NORMAL · -4 HIGH) */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
private fun View.requestedFrameRateText(): String {
    val rate = requestedFrameRate
    return when {
        rate.isNaN() -> "NaN (DEFAULT — 투표 없음)"
        rate == View.REQUESTED_FRAME_RATE_CATEGORY_NO_PREFERENCE -> "NO_PREFERENCE"
        rate == View.REQUESTED_FRAME_RATE_CATEGORY_LOW -> "LOW"
        rate == View.REQUESTED_FRAME_RATE_CATEGORY_NORMAL -> "NORMAL"
        rate == View.REQUESTED_FRAME_RATE_CATEGORY_HIGH -> "HIGH"
        else -> "${rate.oneDecimal()} fps"
    }
}

// ==================== 화면 ====================

@Composable
fun DisplayRefreshRateExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Display Refresh Rate",
            onBackIconClicked = onBackEvent
        )
        DisplayRefreshRateContent()
    }
}

@Composable
private fun DisplayRefreshRateContent() {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(context) { context.findActivity() }
    // 이 화면이 그려지는 디스플레이. ContextCompat 이 API 30 전후 차이(Context.display / WindowManager)를 감춰 준다
    val display = remember(context) { ContextCompat.getDisplayOrDefault(context) }

    var snapshot by remember { mutableStateOf(display.snapshot()) }
    var conditions by remember { mutableStateOf(readConditions(context)) }
    var requestedModeId by remember { mutableIntStateOf(0) }
    var sustainedOn by remember { mutableStateOf(false) }
    val modeLog = remember { mutableStateListOf<String>() }

    var measuring by remember { mutableStateOf(true) }
    var load by remember { mutableStateOf(FrameLoad.NONE) }
    val meter = remember { FramePacingMeter() }
    val reports = remember { mutableStateListOf<FramePacingReport>() }
    // 그래프가 매 프레임 다시 그려지도록 하는 신호 — Canvas 의 그리기 단계에서만 읽는다(리컴포지션 없음)
    val frameTick = remember { mutableLongStateOf(0L) }

    // ① 디스플레이 변화 감시 — 요청이 실제로 반영됐는지는 여기서만 알 수 있다
    DisposableEffect(display) {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val startedAt = System.currentTimeMillis()
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayChanged(displayId: Int) {
                if (displayId != display.displayId) return
                val next = display.snapshot()
                val prev = snapshot
                if (prev.activeModeId != next.activeModeId || prev.refreshRate != next.refreshRate) {
                    val seconds = (System.currentTimeMillis() - startedAt) / 1000f
                    modeLog.add(
                        0,
                        "+${seconds.oneDecimal()}s  mode ${prev.activeModeId}→${next.activeModeId} · " +
                            "${prev.refreshRate.roundToInt()}→${next.refreshRate.roundToInt()}Hz"
                    )
                    if (modeLog.size > 8) modeLog.removeAt(modeLog.lastIndex)
                    // 주사율이 바뀌면 기대 간격이 달라지므로 한 창에 섞지 않는다
                    meter.resetWindow()
                }
                snapshot = next
            }

            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
        }
        displayManager.registerDisplayListener(listener, Handler(Looper.getMainLooper()))
        onDispose { displayManager.unregisterDisplayListener(listener) }
    }

    // ② 창 속성은 Activity 단위다. BlogExampleActivity 는 모든 예제가 함께 쓰므로 떠날 때 원래 값으로 되돌린다
    DisposableEffect(activity) {
        val window = activity?.window
        val originalModeId = window?.attributes?.preferredDisplayModeId ?: 0
        onDispose {
            window?.requestDisplayMode(originalModeId)
            window?.setSustainedPerformanceMode(false)
        }
    }

    // ③ 절전·발열 상태는 콜백 없이 2초마다 다시 읽는다
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(2_000)
            conditions = readConditions(context)
        }
    }

    // ④ 측정 루프 — 화면 수준에 둬서 그래프 카드가 스크롤로 사라져도 측정은 계속된다
    LaunchedEffect(measuring, load) {
        // 멈출 때는 마지막 그래프를 남겨 두고, 다시 시작하거나 부하를 바꿀 때만 지운다
        if (!measuring) return@LaunchedEffect
        meter.restart()
        var frameIndex = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                meter.record(frameNanos, snapshot.refreshRate)?.let { report ->
                    reports.add(0, report)
                    if (reports.size > 6) reports.removeAt(reports.lastIndex)
                }
                frameTick.longValue = frameNanos
                busyWait(load.busyMillis(frameIndex++))
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ConceptCard() }
        item { DisplayModesCard(snapshot = snapshot, conditions = conditions, modeLog = modeLog) }
        item {
            ModeRequestCard(
                snapshot = snapshot,
                requestedModeId = requestedModeId,
                windowAvailable = activity != null,
                onRequest = { modeId ->
                    requestedModeId = modeId
                    activity?.window?.requestDisplayMode(modeId)
                }
            )
        }
        item {
            FramePacingCard(
                measuring = measuring,
                load = load,
                reports = reports,
                meter = meter,
                frameTick = frameTick,
                refreshRate = snapshot.refreshRate,
                onMeasuringChange = { measuring = it },
                onLoadChange = { load = it }
            )
        }
        item { ComposeFrameRateCard(view = view) }
        item {
            SustainedPerformanceCard(
                supported = conditions.sustainedSupported,
                enabled = sustainedOn,
                windowAvailable = activity != null,
                onToggle = { on ->
                    sustainedOn = on
                    activity?.window?.setSustainedPerformanceMode(on)
                }
            )
        }
        item { PitfallCard() }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun ConceptCard() {
    SectionCard(title = "1. 주사율은 누가 정하나") {
        BodyText(
            "디스플레이는 몇 가지 \"모드\"(해상도 + 주사율)를 지원하고, 앱은 그중 하나를 원한다고 요청할 수 있을 뿐이다. " +
                "최종 결정은 시스템이 한다 — 사용자 설정(예: 삼성 '화면 움직임 부드럽게'), 절전 모드, 발열, " +
                "같은 화면에 있는 다른 창의 요청이 모두 투표에 들어간다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        KeyValueRow("요청하는 곳", "API · 쓰임새", isHeader = true)
        KeyValueRow("Window", "preferredDisplayModeId (23) · preferredRefreshRate (21) — 창 전체")
        KeyValueRow("Surface", "Surface.setFrameRate (30) — SurfaceView·게임·동영상")
        KeyValueRow("View", "View.requestedFrameRate (35) — Compose preferredFrameRate 가 쓰는 길")
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "주사율이 오르면 프레임 예산은 줄어든다: 60Hz 16.7ms → 90Hz 11.1ms → 120Hz 8.3ms. " +
                "그래서 \"평균 FPS\" 만 보면 놓치는 끊김을 프레임 간격으로 직접 재 본다(4번 카드)."
        )
    }
}

// ==================== 2. 이 기기의 디스플레이 모드 ====================

@Composable
private fun DisplayModesCard(
    snapshot: DisplaySnapshot,
    conditions: SystemConditions,
    modeLog: List<String>
) {
    SectionCard(title = "2. 이 기기의 디스플레이 모드") {
        BodyText(
            "Display.supportedModes 가 돌려준 모드 목록이다. 지금 쓰는 모드(Display.mode)는 굵게, " +
                "주사율은 Display.refreshRate 값이다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CompareRow("mode", "해상도 · 주사율", "seamless 전환 (API 31+)", isHeader = true)
        snapshot.modes.forEach { mode ->
            val active = mode.modeId == snapshot.activeModeId
            CompareRow(
                key = (if (active) "▶ " else "  ") + "id ${mode.modeId}",
                left = "${mode.width}×${mode.height} · ${mode.refreshRate.oneDecimal()}Hz",
                right = when {
                    mode.alternativeRates == null -> "API 31 미만"
                    mode.alternativeRates.isEmpty() -> "없음"
                    else -> mode.alternativeRates.joinToString { "${it.oneDecimal()}Hz" }
                },
                bold = active
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "목록은 사용자 설정에 따라 걸러진다. SM-A725F 실측: '화면 움직임 부드럽게: 표준'에서는 dumpsys 에 있는 " +
                "90Hz 모드가 빠지고 60Hz 하나만 오며, 적응형에서는 두 개가 다 온다. seamless 칸도 dumpsys 에는 " +
                "[60]/[90] 이 있지만 앱에는 빈 배열로 왔다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueRow("현재 주사율", "${snapshot.refreshRate.oneDecimal()} Hz (프레임 예산 ${(1000f / snapshot.refreshRate).oneDecimal()} ms)")
        KeyValueRow("절전 모드", if (conditions.powerSaveMode) "켜짐 — 고주사율이 막힐 수 있다" else "꺼짐")
        KeyValueRow("발열 상태", conditions.thermalStatus ?: "API 29 미만 — 읽을 수 없음")
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "모드 전환 기록 (DisplayListener)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        if (modeLog.isEmpty()) {
            CaptionText("아직 바뀐 적 없음 — 3번 카드에서 모드를 요청하거나 적응형 설정에서 화면을 멈춰 두면 기록된다.")
        } else {
            modeLog.forEach { CodeText(it) }
        }
    }
}

// ==================== 3. 창에서 모드 요청하기 ====================

@Composable
private fun ModeRequestCard(
    snapshot: DisplaySnapshot,
    requestedModeId: Int,
    windowAvailable: Boolean,
    onRequest: (Int) -> Unit
) {
    // 요청 1.5초 뒤 실제 모드와 비교해 반영 여부를 판정한다.
    // 코루틴은 시작 시점의 인자를 붙잡으므로, 1.5초 뒤의 최신 스냅샷은 rememberUpdatedState 로 읽는다
    val latestSnapshot by rememberUpdatedState(snapshot)
    var verdict by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(requestedModeId) {
        verdict = null
        if (requestedModeId == 0) return@LaunchedEffect
        delay(1_500)
        val now = latestSnapshot
        verdict = if (now.activeModeId == requestedModeId) {
            "✅ 반영됨 — 지금 mode ${now.activeModeId} · ${now.refreshRate.oneDecimal()}Hz"
        } else {
            "⚠️ 반영 안 됨 — 지금 mode ${now.activeModeId}. 요청은 힌트다: 사용자 설정(표준/60Hz 고정)·" +
                "절전·발열이 이긴다"
        }
    }

    SectionCard(title = "3. 창에서 모드 요청하기") {
        BodyText(
            "WindowManager.LayoutParams.preferredDisplayModeId 에 모드 id 를 넣는다. 0 은 \"요청 없음\"(시스템이 정함)이다. " +
                "모드 id 대신 preferredRefreshRate 로 주사율 값만 줄 수도 있다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeText("window.attributes = window.attributes.apply {\n    preferredDisplayModeId = modeId\n}")
        Spacer(modifier = Modifier.height(8.dp))
        ChipRow {
            FilterChip(
                selected = requestedModeId == 0,
                onClick = { onRequest(0) },
                enabled = windowAvailable,
                label = { Text("요청 없음 (0)", fontSize = 11.sp) }
            )
            snapshot.modes.forEach { mode ->
                FilterChip(
                    selected = requestedModeId == mode.modeId,
                    onClick = { onRequest(mode.modeId) },
                    enabled = windowAvailable,
                    label = { Text("id ${mode.modeId} · ${mode.refreshRate.roundToInt()}Hz", fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueRow("요청한 모드", if (requestedModeId == 0) "없음 (0)" else "id $requestedModeId")
        KeyValueRow("실제 모드", "id ${snapshot.activeModeId} · ${snapshot.refreshRate.oneDecimal()}Hz")
        verdict?.let {
            Spacer(modifier = Modifier.height(6.dp))
            BodyText(it)
        }
        if (!windowAvailable) {
            CaptionText("Activity 를 찾지 못해 창 속성을 바꿀 수 없다.")
        }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "창 속성은 Activity 단위다. 이 화면을 떠나면 DisposableEffect 가 들어올 때의 값으로 되돌린다 — " +
                "되돌리지 않으면 같은 Activity 의 다른 예제까지 이 요청을 물려받는다."
        )
    }
}

// ==================== 4. 프레임 간격 재기 ====================

@Composable
private fun FramePacingCard(
    measuring: Boolean,
    load: FrameLoad,
    reports: List<FramePacingReport>,
    meter: FramePacingMeter,
    frameTick: MutableLongState,
    refreshRate: Float,
    onMeasuringChange: (Boolean) -> Unit,
    onLoadChange: (FrameLoad) -> Unit
) {
    SectionCard(title = "4. 프레임 간격 재기") {
        BodyText(
            "withFrameNanos 가 넘겨주는 프레임 시각의 차이를 2초 창으로 모아 FPS · 늦은 프레임 비율" +
                "(기대 간격 × ${LATE_FRAME_FACTOR} 초과) · p95 · p99 · 최악 간격을 낸다. 평균 FPS 는 멀쩡한데 " +
                "최악 간격이 튀는 경우를 부하로 직접 만들어 본다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DemoButton(
                text = if (measuring) "측정 멈춤" else "측정 시작",
                color = if (measuring) Color(0xFF757575) else Color(0xFF1976D2),
                onClick = { onMeasuringChange(!measuring) }
            )
            Spacer(modifier = Modifier.width(10.dp))
            CaptionText(
                if (BuildConfig.DEBUG) {
                    "debug 빌드 — 이 화면은 HotSwan 에서 제외했지만 디버거블 오버헤드가 남는다. 비교는 release(AOT) 로."
                } else {
                    "release 빌드 — adb 설치 직후는 JIT 상태라 드물게 도는 경로가 느리다(7번 카드)"
                }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        ChipRow {
            FrameLoad.entries.forEach { option ->
                FilterChip(
                    selected = load == option,
                    onClick = { onLoadChange(option) },
                    label = { Text(option.label, fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        IntervalChart(meter = meter, frameTick = frameTick, refreshRate = refreshRate)
        Spacer(modifier = Modifier.height(8.dp))

        CompareRow("창(최신 위)", "FPS · 늦은 프레임", "p95 · p99 · 최악 (ms)", isHeader = true)
        if (reports.isEmpty()) {
            CaptionText(if (measuring) "첫 2초 창을 모으는 중…" else "측정이 멈춰 있다.")
        } else {
            reports.forEach { report ->
                // key 가 없으면 맨 위에 행을 넣을 때 아래 행들이 전부 새 문자열을 받아 Text 18개를 다시 레이아웃한다.
                // 그 프레임 하나가 50ms 를 넘겨(실측) 측정 UI 가 측정값을 오염시켰다 → 행을 창 번호로 고정한다
                key(report.sequence) {
                    CompareRow(
                        key = "#${report.sequence} ${report.refreshRateHz.roundToInt()}Hz",
                        left = "${report.fps.oneDecimal()} · ${report.latePercent.oneDecimal()}% (${report.lateCount})",
                        right = "${report.p95Ms.oneDecimal()} · ${report.p99Ms.oneDecimal()} · ${report.worstMs.oneDecimal()}"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "1초 넘게 벌어진 간격은 백그라운드·화면 꺼짐으로 보고 버린다. 모드가 바뀌거나 부하를 바꾸면 창을 새로 시작한다."
        )
    }
}

@Composable
private fun IntervalChart(
    meter: FramePacingMeter,
    frameTick: MutableLongState,
    refreshRate: Float
) {
    val scaleMs = 50f
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            // 매 프레임 다시 그리는 영역을 자기 레이어로 떼어, 화면의 다른 카드까지 다시 기록하지 않게 한다
            .graphicsLayer()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
    ) {
        // 그리기 단계에서만 읽는다 → 매 프레임 다시 그려도 리컴포지션은 일어나지 않는다
        frameTick.longValue
        val expectedMs = 1000f / refreshRate
        val lateMs = expectedMs * LATE_FRAME_FACTOR
        fun yOf(ms: Float): Float = size.height * (1f - (ms / scaleMs).coerceAtMost(1f))

        drawLine(
            color = Color(0xFF90A4AE),
            start = Offset(0f, yOf(expectedMs)),
            end = Offset(size.width, yOf(expectedMs)),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
        drawLine(
            color = Color(0xFFE57373),
            start = Offset(0f, yOf(lateMs)),
            end = Offset(size.width, yOf(lateMs)),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )

        val slots = 90
        val barWidth = size.width / slots
        val count = meter.recentSize
        for (i in 0 until count) {
            val ms = meter.recentAt(i) / NANOS_PER_MILLI
            val x = size.width - (count - i) * barWidth
            val top = yOf(ms)
            drawRect(
                color = if (ms > lateMs) Color(0xFFE53935) else Color(0xFF43A047),
                topLeft = Offset(x, top),
                size = Size(barWidth * 0.8f, size.height - top)
            )
        }
    }
    Row(modifier = Modifier.padding(top = 4.dp)) {
        CaptionText("회색 점선 = 기대 간격 ${(1000f / refreshRate).oneDecimal()}ms · 빨간 점선 = 늦은 프레임 기준 · 위 끝 = ${scaleMs.roundToInt()}ms")
    }
}

// ==================== 5. Compose preferredFrameRate ====================

@Composable
private fun ComposeFrameRateCard(view: View) {
    var choice by remember { mutableStateOf(ComposeFrameRateChoice.NONE) }
    var readout by remember { mutableStateOf("") }
    val transition = rememberInfiniteTransition(label = "frameRateBox")
    // State 로 받아 레이아웃 단계(offset 람다)에서만 읽는다 — 매 프레임 리컴포지션을 만들지 않는다
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing), RepeatMode.Reverse),
        label = "frameRateBoxProgress"
    )

    // API 35+ 에서만 Compose 가 View 에 값을 싣는다. 그리기가 끝난 뒤에 반영되므로 주기적으로 읽는다
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
        LaunchedEffect(choice) {
            while (isActive) {
                delay(500)
                readout = view.requestedFrameRateText()
            }
        }
    }

    SectionCard(title = "5. Compose preferredFrameRate") {
        BodyText(
            "Compose 1.12 의 Modifier.preferredFrameRate(Float | FrameRateCategory) 는 자기 레이어에 값을 적어 두고, " +
                "그 레이어가 다시 그려지는 프레임마다 투표한다. AndroidComposeView 가 한 프레임의 투표를 모아 숫자는 가장 큰 값, " +
                "카테고리는 High 가 Normal 을 이기도록 합친 뒤 View.requestedFrameRate 로 넘기고 비운다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ChipRow {
            ComposeFrameRateChoice.entries.forEach { option ->
                FilterChip(
                    selected = choice == option,
                    onClick = { choice = option },
                    label = { Text(option.label, fontSize = 11.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 숫자 투표는 그 레이어가 "다시 그려질 때"(updateDisplayList)만 나간다. 그래서 레이어는 제자리에 두고
        // 내용(원의 위치)만 매 프레임 다시 그린다. 레이어를 offset 으로 옮기면 다시 그리지 않아 숫자 투표가 안 나가고,
        // 대신 move() 가 High 카테고리를 자동으로 투표한다(GraphicsLayerOwnerLayer).
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .preferredFrameRateOf(choice)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
        ) {
            val radius = 14.dp.toPx()
            val travel = size.width - radius * 2 - 8.dp.toPx()
            drawCircle(
                color = Color(0xFF1976D2),
                radius = radius,
                center = Offset(4.dp.toPx() + radius + travel * progress.value, size.height / 2)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            KeyValueRow("View.requestedFrameRate", readout.ifEmpty { "읽는 중…" })
            CaptionText(
                "숫자 투표는 ComposeView 자신에, 카테고리 투표는 Compose 가 몰래 붙인 1×1 자식 View 에 실린다 — " +
                    "그래서 Normal/High 를 골라도 위 값은 NaN 으로 보인다. 같은 원을 offset 으로 레이어째 옮기면 " +
                    "30fps 를 골라도 NaN 이다(다시 그리지 않으니 숫자 투표가 안 나간다)."
            )
        } else {
            BodyText(
                "⚠️ 이 기기는 API ${Build.VERSION.SDK_INT} 다. AndroidComposeView 의 isArrEnabled 가 " +
                    "SDK_INT >= 35 일 때만 참이라, 무엇을 골라도 플랫폼에 전달되지 않는다(4번 측정값도 그대로다). " +
                    "API 35 미만에서는 3번의 창 요청이 유일한 길이다."
            )
        }
    }
}

// ==================== 6. 지속 성능 모드 ====================

@Composable
private fun SustainedPerformanceCard(
    supported: Boolean,
    enabled: Boolean,
    windowAvailable: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SectionCard(title = "6. 지속 성능 모드") {
        BodyText(
            "Window.setSustainedPerformanceMode(true) 는 \"짧게 치솟았다 떨어지는\" 클럭 대신 오래 유지할 수 있는 " +
                "낮은 클럭을 달라는 요청이다. 발열로 클럭이 꺾일 때마다 늦은 프레임이 생기는 게임·지도에 쓴다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueRow(
            "지원 여부",
            "isSustainedPerformanceModeSupported = " + if (supported) "true" else "false — 켜도 무시된다"
        )
        OptionSwitch(
            label = "setSustainedPerformanceMode",
            checked = enabled,
            enabled = windowAvailable,
            onCheckedChange = onToggle
        )
        CaptionText("화면을 떠나면 false 로 되돌린다. 효과는 4번 측정을 길게(수 분) 돌려 발열 구간에서 비교해야 보인다.")
    }
}

// ==================== 7. 함정 정리 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "7. 실기기에서 걸리는 것들") {
        PitfallRow(
            "요청은 힌트다",
            "사용자 설정이 먼저다. 삼성 '화면 움직임 부드럽게: 표준'에서는 90Hz 모드가 supportedModes 에서 아예 빠져 " +
                "요청할 수조차 없다. 적응형에서는 60·90 요청이 1.5초 안에 반영됐다(SM-A725F). 절전·발열도 같은 쪽에 선다."
        )
        PitfallRow(
            "창 속성은 Activity 단위",
            "preferredDisplayModeId 는 Compose 화면이 아니라 창에 붙는다. 이 예제는 떠날 때 되돌린다 — " +
                "60Hz 를 요청한 채 헤더 뒤로가기로 목록에 돌아오면 같은 Activity 인데도 90Hz 로 돌아온다(실측)."
        )
        PitfallRow(
            "평균 FPS 는 끊김을 숨긴다",
            "60프레임마다 50ms 멈춤: FPS 59.8 → 57.8 로 두 칸 떨어질 뿐인데 p99·최악 간격은 50ms 다(60Hz 실측). " +
                "늦은 프레임 비율·p99·최악 간격을 같이 본다."
        )
        PitfallRow(
            "주사율이 오르면 예산이 준다",
            "매 프레임 13ms 작업: 60Hz 에서는 59.8fps·늦은 프레임 0% 였는데 90Hz(11.1ms)에서는 69fps·약 30% 로 떨어졌다. " +
                "고주사율은 공짜가 아니다."
        )
        PitfallRow(
            "release · AOT 로 잰다",
            "debug 는 디버거블 오버헤드가 섞인다(HotSwan 재작성 아래에서는 그래프 그리기만 44ms 라 이 화면을 제외해 뒀다). " +
                "release 도 adb 설치 직후에는 JIT 상태라, 2초에 한 번 도는 표 갱신 경로가 차가워 창마다 프레임 하나를 놓쳤다 — " +
                "adb shell cmd package compile -m speed -f <패키지> 뒤에는 0 이다."
        )
        PitfallRow(
            "측정 UI 가 측정을 오염시킨다",
            "표 맨 위에 행을 넣을 때 key 가 없으면 아래 행이 전부 새 문자열을 받아 그 프레임이 50ms 를 넘겼다. " +
                "매 프레임 다시 그리는 그래프는 graphicsLayer 로 떼고, 움직이는 원은 컴포지션이 아니라 그리기 단계에서 읽어 " +
                "평상시 그리기 약 6ms → 2ms 이하 · 애니메이션 단계 3.5ms → 1.3ms 이하가 됐다(release, framestats 중앙값)."
        )
        PitfallRow(
            "preferredFrameRate 는 API 35+ · 다시 그려질 때만",
            "API 35 미만은 무동작이다(API 33 에서 30fps 를 골라도 간격 11.1ms 그대로). API 35 에뮬레이터에서는 30/60 이 " +
                "ComposeView 에, Normal/High 가 숨은 자식 View 에 -3/-4 로 실렸다. 투표는 레이어가 다시 그려지는 프레임에만 나가므로 " +
                "스크롤로 사라지거나 내용이 멈추면 사라진다."
        )
        PitfallRow(
            "움직이면 High 가 자동으로 붙는다",
            "API 35+ 에서 레이아웃 위치가 바뀌는 프레임마다 Compose 가 High 를 투표한다. 원을 offset 으로 레이어째 옮기자 " +
                "30fps 를 골라도 숫자 투표는 끝내 안 나가고 자식 View 에 High(-4)만 남았다 — Normal 을 골라도 High 가 이긴다. " +
                "스크롤 중 고주사율로 오르는 이유이기도 하다."
        )
        PitfallRow(
            "seamless 전환",
            "alternativeRefreshRates 에 서로 들어 있는 모드끼리는 끊김 없이 바뀐다. 다만 SM-A725F 는 dumpsys 에 있는 값을 " +
                "앱에는 빈 배열로 넘겼다 — 앱이 받은 값만으로 단정하지 않는다."
        )
    }
}

// ==================== 공통 요소 ====================

private fun Float.oneDecimal(): String = String.format(Locale.US, "%.1f", this)

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        content()
    }
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color(0xFF424242),
        lineHeight = 19.sp
    )
}

@Composable
private fun CaptionText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF757575),
        lineHeight = 16.sp
    )
}

@Composable
private fun CodeText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp))
            .padding(8.dp),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        lineHeight = 14.sp
    )
}

@Composable
private fun KeyValueRow(
    key: String,
    value: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = key,
            modifier = Modifier.width(130.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun CompareRow(
    key: String,
    left: String,
    right: String,
    isHeader: Boolean = false,
    bold: Boolean = false
) {
    val weight = if (isHeader || bold) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = key,
            modifier = Modifier.width(96.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = weight,
            color = Color(0xFF424242)
        )
        listOf(left, right).forEach { value ->
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                fontWeight = weight,
                color = Color(0xFF616161)
            )
        }
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}

@Composable
private fun OptionSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF424242)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1976D2))
        )
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
