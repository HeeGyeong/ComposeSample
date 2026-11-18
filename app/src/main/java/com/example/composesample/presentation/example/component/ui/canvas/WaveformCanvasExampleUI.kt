package com.example.composesample.presentation.example.component.ui.canvas

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// ==================== 상수 ====================

/** 신호원(센서/장비)의 고정 샘플레이트. 화면 주사율과 무관한 값이라는 점이 이 예제의 출발점 */
private const val WAVE_SAMPLE_RATE = 250

/** 화면 한 폭에 담는 시간(초) */
private const val WAVE_WINDOW_SECONDS = 4

/** 링 버퍼 용량 = 250Hz × 4초 = 1000 샘플 */
private const val WAVE_CAPACITY = WAVE_SAMPLE_RATE * WAVE_WINDOW_SECONDS

/** Sweep 모드에서 커서 앞쪽을 비워 두는 샘플 수(지우개 폭) */
private const val WAVE_SWEEP_GAP = 24

/** 앱 복귀 등으로 프레임이 크게 밀렸을 때 한 프레임에 밀어 넣을 최대 샘플 수 */
private const val WAVE_MAX_SAMPLES_PER_FRAME = 64

// ==================== 신호 / 렌더 모델 ====================

/**
 * 생성할 신호 종류.
 *
 * @param baseline 캔버스 높이 대비 기준선 위치(0=위, 1=아래)
 * @param gain 캔버스 높이 대비 진폭 배율
 */
private enum class WaveformSignal(
    val label: String,
    val color: Color,
    val baseline: Float,
    val gain: Float,
    val description: String,
) {
    ECG(
        label = "ECG",
        color = Color(0xFF69F0AE),
        baseline = 0.72f,
        gain = 0.58f,
        description = "심전도 — P파·QRS 복합파·T파를 가우시안 합으로 합성. R 스파이크가 한 박동의 기준점",
    ),
    PPG(
        label = "PPG",
        color = Color(0xFF40C4FF),
        baseline = 0.78f,
        gain = 0.62f,
        description = "맥파(광용적맥파) — 수축기 피크 뒤에 중복맥(dicrotic notch) 봉우리가 따라오는 완만한 파형",
    ),
    SINE(
        label = "Sine",
        color = Color(0xFFFFD740),
        baseline = 0.5f,
        gain = 0.38f,
        description = "기준 사인파 — 샘플레이트/시간축이 맞게 흐르는지 눈으로 검증할 때 쓰는 대조군",
    ),
    NOISE(
        label = "Noise",
        color = Color(0xFFFF8A80),
        baseline = 0.5f,
        gain = 0.38f,
        description = "백색 잡음 — 필터 없이 원신호를 그대로 그렸을 때의 모습",
    ),
}

/** 파형을 화면에 흘리는 방식 */
private enum class WaveformMode(val label: String, val description: String) {
    SWEEP(
        label = "Sweep",
        description = "커서가 좌→우로 훑으며 지난 파형을 덮어씀(병원 환자 모니터 방식). 버퍼 인덱스가 곧 화면 x좌표라 이동 비용 0",
    ),
    SCROLL(
        label = "Scroll",
        description = "최신 샘플이 항상 오른쪽 끝에 오도록 파형 전체가 우→좌로 흐름(오실로스코프 방식). 시간 순서가 직관적",
    ),
}

/**
 * 고정 크기 원형(ring) 버퍼.
 *
 * 실시간 파형은 초당 수백 개 샘플이 들어오므로 `List.add`/`drop` 로 매번 리스트를 다시 만들면
 * 프레임마다 할당·복사가 발생한다. 여기서는 [FloatArray] 한 개를 끝까지 재사용하고
 * [head] 인덱스만 순환시켜 쓰기 비용을 O(1)·할당 0으로 만든다.
 *
 * 의도적으로 스냅샷 상태(mutableStateListOf)가 아닌 평범한 배열이다.
 * 샘플 하나하나를 관찰 대상으로 만들면 초당 250번의 스냅샷 쓰기가 발생하는데,
 * 화면은 프레임당 한 번만 다시 그리면 되므로 갱신 신호는 [WaveformController.head] 하나로 충분하다.
 */
private class WaveformRingBuffer(val capacity: Int) {
    val samples = FloatArray(capacity)

    /** 다음에 쓸 위치(= Sweep 모드의 커서 위치) */
    var head = 0
        private set

    /** 지금까지 채워진 샘플 수(최대 capacity) */
    var filled = 0
        private set

    fun push(value: Float) {
        samples[head] = value
        head = (head + 1) % capacity
        if (filled < capacity) filled++
    }

    fun clear() {
        samples.fill(0f)
        head = 0
        filled = 0
    }
}

/** 0.5초 주기로만 갱신하는 표시용 지표 */
private data class WaveformStats(
    val fps: Int = 0,
    val samplesPerFrame: Int = 0,
    val filled: Int = 0,
)

/**
 * 파형 화면의 상태 소유자.
 *
 * LazyColumn 아이템이 화면 밖으로 나가면 remember 가 폐기되므로, 버퍼와 생성 루프는
 * 최상위 컴포저블에 두어 스크롤과 무관하게 신호가 이어지도록 한다.
 */
@Stable
private class WaveformController {
    val buffer = WaveformRingBuffer(WAVE_CAPACITY)

    /**
     * 드로우 단계 전용 구독 지점.
     * 캔버스 람다 안에서만 읽으므로, 프레임마다 값이 바뀌어도 리컴포지션 없이 재드로우만 발생한다.
     */
    val head = mutableIntStateOf(0)
    val filled = mutableIntStateOf(0)

    var signal by mutableStateOf(WaveformSignal.ECG)
    var mode by mutableStateOf(WaveformMode.SWEEP)
    var bpm by mutableFloatStateOf(72f)
    var noise by mutableFloatStateOf(0.02f)
    var running by mutableStateOf(true)

    /** 지표는 초당 2회만 갱신 — 매 프레임 갱신하면 파형과 무관한 리컴포지션이 초당 60회 발생 */
    var stats by mutableStateOf(WaveformStats())

    fun clear() {
        buffer.clear()
        head.intValue = 0
        filled.intValue = 0
        stats = WaveformStats()
    }
}

// ==================== 화면 ====================

@Composable
fun WaveformCanvasExampleUI(onBackEvent: () -> Unit) {
    val controller = remember { WaveformController() }
    // 4번 카드의 측정 결과도 화면 최상위에 — 카드가 스크롤로 폐기돼도(LazyColumn 아이템) 표가 남는다
    val recordScopeResults = remember { mutableStateOf<Map<NeighborLoad, List<RecordWindowResult>>>(emptyMap()) }

    // 신호 생성 루프는 리스트 아이템이 아니라 화면 최상위에 둔다(스크롤로 카드가 폐기돼도 파형 유지)
    LaunchedEffect(Unit) {
        runWaveformLoop(controller)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Realtime Waveform Canvas",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { WaveformOverviewCard() }
            item { WaveformRendererCard(controller) }
            item { WaveformRingBufferCard() }
            item { WaveformDrawPhaseCard() }
            item { WaveformRecordScopeCard(recordScopeResults) }
            item { WaveformSignalModelCard() }
            item { WaveformSummaryCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun WaveformOverviewCard() {
    WaveformCard {
        Text(
            text = "실시간 파형 렌더링 (순수 Compose Canvas)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "환자 모니터·오실로스코프처럼 멈추지 않고 흘러가는 파형은 애니메이션이 아니라 " +
                    "'데이터 스트림을 화면에 계속 밀어 넣는 일'입니다. 외부 라이브러리 없이 구현할 때 " +
                    "실제로 부딪히는 문제는 세 가지입니다.\n\n" +
                    "① 고정 샘플레이트 ↔ 가변 프레임레이트: 센서는 250Hz로 일정하게 보내는데 화면은 " +
                    "60/90/120Hz로 제각각 → 프레임마다 '몇 개를 밀어 넣을지' 계산해야 시간축이 안 틀어집니다.\n" +
                    "② 버퍼: 초당 250개가 들어오는 데이터를 리스트로 add/drop 하면 프레임마다 재할당이 발생합니다.\n" +
                    "③ 갱신 경로: 파형이 바뀔 때마다 리컴포지션이 돌면 캔버스와 무관한 트리까지 다시 실행됩니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
    }
}

// ==================== 2. 핵심 실동작 ====================

@Composable
private fun WaveformRendererCard(controller: WaveformController) {
    // 이 카드 자체는 관찰 상태를 읽지 않는다 → 최초 1회만 컴포지션되고,
    // 변화는 아래 세 자식이 각자 필요한 범위에서만 처리한다.
    WaveformCard {
        Text(
            text = "1. 실시간 파형 (링 버퍼 + Canvas)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "신호 종류와 렌더 모드를 바꿔가며 같은 버퍼가 어떻게 다르게 그려지는지 비교해 보세요. " +
                    "컨트롤·캔버스·지표를 별도 컴포저블로 나눠, 슬라이더 조작은 컨트롤만 / 파형 갱신은 드로우만 " +
                    "다시 돌도록 범위를 좁혔습니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        WaveformControls(controller)
        Spacer(modifier = Modifier.height(12.dp))
        WaveformSurface(controller)
        Spacer(modifier = Modifier.height(8.dp))
        WaveformStatsRow(controller)
    }
}

@Composable
private fun WaveformControls(controller: WaveformController) {
    val signal = controller.signal
    val mode = controller.mode

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        WaveformSignal.entries.forEach { item ->
            WaveformChip(
                label = item.label,
                selected = signal == item,
                onClick = { controller.signal = item }
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = signal.description,
        fontSize = 11.sp,
        color = Color(0xFF757575),
        lineHeight = 15.sp
    )

    Spacer(modifier = Modifier.height(10.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WaveformMode.entries.forEach { item ->
            WaveformChip(
                label = item.label,
                selected = mode == item,
                onClick = { controller.mode = item }
            )
        }
        WaveformChip(
            label = if (controller.running) "일시정지" else "재개",
            selected = false,
            onClick = { controller.running = !controller.running }
        )
        WaveformChip(
            label = "초기화",
            selected = false,
            onClick = { controller.clear() }
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = mode.description,
        fontSize = 11.sp,
        color = Color(0xFF757575),
        lineHeight = 15.sp
    )

    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "심박수 ${controller.bpm.roundToInt()} BPM · 박동 주기 ${
            "%.2f".format(60f / controller.bpm)
        }초",
        fontSize = 12.sp,
        color = Color(0xFF424242)
    )
    Slider(
        value = controller.bpm,
        onValueChange = { controller.bpm = it },
        valueRange = 30f..180f
    )
    Text(
        text = "잡음 ${(controller.noise * 100).roundToInt()}%",
        fontSize = 12.sp,
        color = Color(0xFF424242)
    )
    Slider(
        value = controller.noise,
        onValueChange = { controller.noise = it },
        valueRange = 0f..0.3f
    )
}

@Composable
private fun WaveformSurface(controller: WaveformController) {
    // Path 는 remember 로 한 번만 만들고 매 프레임 reset() 후 재사용(프레임당 할당 0)
    val path = remember { Path() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B1418))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // ↓ 여기서 읽는 상태는 전부 '드로우 단계 구독'이다.
            //   값이 바뀌면 이 캔버스만 다시 그려지고, 상위 컴포저블은 리컴포지션되지 않는다.
            val head = controller.head.intValue
            val filled = controller.filled.intValue
            val signal = controller.signal
            val mode = controller.mode

            drawWaveformGrid()
            drawWaveformTrace(
                buffer = controller.buffer,
                head = head,
                filled = filled,
                mode = mode,
                signal = signal,
                path = path
            )
        }
    }
}

@Composable
private fun WaveformStatsRow(controller: WaveformController) {
    val stats = controller.stats
    Text(
        text = "표시 FPS ${stats.fps} · 프레임당 샘플 ${stats.samplesPerFrame}개 · " +
                "버퍼 ${stats.filled}/$WAVE_CAPACITY (${
                    "%.1f".format(stats.filled / WAVE_SAMPLE_RATE.toFloat())
                }초분)",
        fontSize = 11.sp,
        color = Color(0xFF546E7A)
    )
    Text(
        text = "샘플레이트는 ${WAVE_SAMPLE_RATE}Hz 고정 — 프레임당 샘플 수는 표시 FPS에 반비례해 자동으로 맞춰집니다.",
        fontSize = 11.sp,
        color = Color(0xFF9E9E9E),
        lineHeight = 15.sp
    )
}

// ==================== 3. 링 버퍼 ====================

@Composable
private fun WaveformRingBufferCard() {
    WaveformCard {
        Text(
            text = "2. 링 버퍼 + 샘플레이트 분리",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "샘플은 FloatArray 하나에 계속 덮어쓰고 head 인덱스만 순환시킵니다. " +
                    "리스트를 다시 만들지 않으므로 프레임당 할당이 0이고, 오래된 샘플을 지우는 비용도 없습니다.\n\n" +
                    "프레임마다 밀어 넣을 샘플 개수는 withFrameNanos 로 잰 dt 에서 계산합니다. " +
                    "소수부(carry)를 다음 프레임으로 이월하는 것이 핵심 — 매 프레임 1개씩 넣으면 " +
                    "120Hz 기기에서 파형이 2배 빨리 흐르게 됩니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        WaveformCodeBlock(
            code = """
                // 쓰기 O(1) · 재할당 0
                fun push(value: Float) {
                    samples[head] = value
                    head = (head + 1) % capacity
                    if (filled < capacity) filled++
                }

                // 프레임 → 샘플 개수 변환(소수부 이월)
                carry += dt * SAMPLE_RATE          // 60fps·250Hz → 4.16
                val count = carry.toInt()          // 이번 프레임 4개
                carry -= count                     // 0.16 은 다음 프레임으로
            """.trimIndent()
        )
    }
}

// ==================== 4. 드로우 단계 읽기 대조 ====================

/** 컴포지션/드로우 실행 횟수 카운터(스냅샷 상태가 아니라 평범한 홀더) */
private class WaveformPhaseCounters {
    var compositions = 0
    var draws = 0
}

@Composable
private fun WaveformDrawPhaseCard() {
    val tick = remember { mutableIntStateOf(0) }
    val composeSide = remember { WaveformPhaseCounters() }
    val drawSide = remember { WaveformPhaseCounters() }
    var statsRefresh by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { if (running) tick.intValue++ }
        }
    }
    // 지표만 0.5초 주기로 갱신 — 이 갱신분(초당 2회)은 양쪽 모두에 동일하게 포함된다
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            statsRefresh++
        }
    }

    WaveformCard {
        Text(
            text = "3. 어디서 상태를 읽느냐 — 리컴포지션 vs 재드로우",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "같은 프레임 카운터를 위쪽은 컴포저블 본문에서, 아래쪽은 Canvas 드로우 람다 안에서 읽습니다. " +
                    "본문에서 읽으면 프레임마다 컴포지션이 다시 실행되고, 드로우 람다에서 읽으면 " +
                    "컴포지션은 그대로 둔 채 그리기만 무효화됩니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        WaveformPhaseSurface(
            label = "❌ 컴포지션 단계에서 읽기",
            tick = tick,
            readInComposition = true,
            counters = composeSide,
            barColor = Color(0xFFFF8A80)
        )
        Spacer(modifier = Modifier.height(10.dp))
        WaveformPhaseSurface(
            label = "✅ 드로우 단계에서 읽기",
            tick = tick,
            readInComposition = false,
            counters = drawSide,
            barColor = Color(0xFF69F0AE)
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "컴포지션 ${composeSide.compositions}회 / 드로우 ${composeSide.draws}회  ← ❌ 방식",
            fontSize = 12.sp,
            color = Color(0xFFD32F2F)
        )
        Text(
            text = "컴포지션 ${drawSide.compositions}회 / 드로우 ${drawSide.draws}회  ← ✅ 방식",
            fontSize = 12.sp,
            color = Color(0xFF2E7D32)
        )
        Text(
            text = "지표 갱신 #$statsRefresh (0.5초 주기) — 이 갱신 때문에 양쪽 다 초당 2회는 " +
                    "리컴포지션됩니다. 그 몫을 빼고 보면 ❌ 쪽만 프레임 수만큼(초당 60회) 늘어납니다.",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WaveformChip(
                label = if (running) "정지" else "시작",
                selected = false,
                onClick = { running = !running }
            )
            WaveformChip(
                label = "카운터 초기화",
                selected = false,
                onClick = {
                    composeSide.compositions = 0
                    composeSide.draws = 0
                    drawSide.compositions = 0
                    drawSide.draws = 0
                }
            )
        }
    }
}

@Composable
private fun WaveformPhaseSurface(
    label: String,
    tick: IntState,
    readInComposition: Boolean,
    counters: WaveformPhaseCounters,
    barColor: Color,
) {
    counters.compositions++

    // readInComposition 이면 본문에서 읽는다 → 값이 바뀔 때마다 이 컴포저블 전체가 다시 실행
    val composedTick = if (readInComposition) tick.intValue else 0

    Column {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF424242))
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0B1418))
        ) {
            counters.draws++
            // readInComposition=false 일 때만 드로우 람다 안에서 상태를 읽는다
            val frame = if (readInComposition) composedTick else tick.intValue
            val barWidth = 8.dp.toPx()
            val x = (frame % 120) / 120f * (size.width - barWidth)
            drawRect(
                color = barColor,
                topLeft = Offset(x, 0f),
                size = Size(barWidth, size.height)
            )
        }
    }
}

// ==================== 5. 다시 기록하는 범위 ====================

/**
 * 드로우 단계에서만 읽어도 '무엇이 다시 기록되느냐'는 레이어가 정한다(#182).
 * 글로우의 그리기 무효화는 가장 가까운 레이어까지 올라가 그 안의 레이어 없는 노드를 모두 다시 기록한다 —
 * 같은 내용을 그리는 이웃 둘(레이어 없음 · 자기 레이어)의 그리기 횟수와 프레임마다의 DRAW_DURATION 을 변형 넷에서 잰다.
 * 횟수 · 판정 · 측정기는 RecordScopeLab.kt, 이웃 타일 루프는 RecordScopeDrawing.kt(HotSwan 제외).
 */
@Composable
private fun WaveformRecordScopeCard(resultsState: MutableState<Map<NeighborLoad, List<RecordWindowResult>>>) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivity()?.window }
    val meter = remember(window) { window?.let { RecordScopeMeter(it) } }
    DisposableEffect(meter) {
        onDispose { meter?.close() }
    }
    val counters = remember { RecordScopeCounters() }
    val tick = remember { mutableIntStateOf(0) }
    var glowLayer by remember { mutableStateOf(GlowLayer.NONE) }
    var load by remember { mutableStateOf(NeighborLoad.LIGHT) }
    var measuring by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf("") }
    var results by resultsState
    val scope = rememberCoroutineScope()

    // 글로우를 움직이는 프레임 카운터 — 드로우 람다 안에서만 읽는다(3번 카드의 ✅ 방식)
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { tick.intValue++ }
        }
    }

    WaveformCard {
        Column(
            modifier = Modifier.pointerInput(Unit) {
                // 재는 동안 카드를 건드리면(탭 · 스크롤) 그 자체가 카드를 다시 기록시킨다 → 그 창은 판정하지 않는다
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        counters.touched = true
                    }
                }
            }
        ) {
            Text(
                text = "4. 다시 기록하는 범위 — graphicsLayer() 가 경계",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "드로우 람다에서만 읽어도 끝이 아닙니다. 그리기 무효화는 노드 트리를 거슬러 올라가 가장 가까운 레이어(RenderNode)를 " +
                    "다시 기록하고, 그 레이어에 함께 담긴 '레이어 없는' 이웃의 그리기까지 다시 돕니다. 왼쪽 글로우는 매 프레임 움직이고, " +
                    "가운데 · 오른쪽 이웃은 같은 타일을 그리되 오른쪽만 자기 graphicsLayer() 가 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            RecordScopeStage(glowLayer = glowLayer, load = load, tick = tick, counters = counters)

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "글로우의 레이어", fontSize = 11.sp, color = Color(0xFF757575))
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlowLayer.entries.forEach { option ->
                    WaveformChip(label = option.label, selected = glowLayer == option, onClick = { if (!measuring) glowLayer = option })
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NeighborLoad.entries.forEach { option ->
                    WaveformChip(label = option.label, selected = load == option, onClick = { if (!measuring) load = option })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            WaveformCodeBlock(code = "Box(Modifier${glowLayer.code})")

            Spacer(modifier = Modifier.height(10.dp))
            WaveformChip(
                label = when {
                    meter == null -> "창을 찾지 못해 잴 수 없음"
                    measuring -> progress
                    else -> "${load.label} — 네 가지 차례로 재기(약 12초)"
                },
                selected = measuring,
                onClick = {
                    val activeMeter = meter ?: return@WaveformChip
                    if (measuring) return@WaveformChip
                    measuring = true
                    val chosen = glowLayer
                    val measuredLoad = load
                    scope.launch {
                        try {
                            val rows = mutableListOf<RecordWindowResult>()
                            GlowLayer.entries.forEachIndexed { index, option ->
                                glowLayer = option
                                progress = "재는 중 ${index + 1}/${GlowLayer.entries.size} — ${option.label} (화면을 건드리지 마세요)"
                                val result = activeMeter.measure(option, measuredLoad, counters)
                                Log.i(RECORD_SCOPE_TAG, result.probeLine())
                                rows += result
                            }
                            // 결과는 네 창이 모두 끝난 뒤 한 번에 — 창 사이에 표가 자라면 뒤 변형일수록 다시 기록할 내용이 많아진다
                            results = results + (measuredLoad to rows.toList())
                        } finally {
                            glowLayer = chosen
                            measuring = false
                        }
                    }
                }
            )

            NeighborLoad.entries.forEach { measuredLoad ->
                results[measuredLoad]?.let { rows -> RecordScopeResults(load = measuredLoad, rows = rows) }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "이 화면에서는 다시 기록이 카드 안에서 멈춥니다 — 카드(Surface)의 clip · 그림자와 LazyColumn 아이템이 이미 레이어라서입니다" +
                    "(clip(shape) 은 graphicsLayer(shape, clip = true) 와 같다). 원문의 '화면 루트까지'는 이런 경계가 하나도 없을 때 화면 콘텐츠 전체 레이어까지 " +
                    "올라간다는 뜻이고, 레이어가 여기저기 이미 있어서 문제가 잘 드러나지 않습니다. graphicsLayer() 는 자기 '뒤' 노드들을 레이어에 담으므로 " +
                    "drawBehind 보다 앞에 둬야 경계가 됩니다. release 실측(Pixel 4 API 30 · Pixel Tablet API 35 에뮬): 그리기 중앙값 타일 1개 0.24~0.30 → 0.05~0.06ms, " +
                    "400개 0.38~0.42 → 0.06ms(원문 '~0.2ms' 와 같은 크기). 타일 1개로도 차이가 큰 것은 같은 카드의 글 · 칩까지 함께 다시 기록되기 때문입니다 — 아끼는 양은 '같은 레이어에 무엇이 있느냐'가 정합니다.",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 15.sp
            )
        }
    }
}

private const val RECORD_SCOPE_TAG = "RecordScope"

@Composable
private fun RecordScopeStage(glowLayer: GlowLayer, load: NeighborLoad, tick: IntState, counters: RecordScopeCounters) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        RecordScopeSlot(label = "글로우\n(매 프레임)") {
            Box(
                modifier = Modifier.glow(glowLayer) {
                    counters.glow++
                    drawGlow(tick.intValue)
                }
            )
        }
        RecordScopeSlot(label = "이웃\n레이어 없음") {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .drawBehind {
                        counters.plain++
                        drawNeighborTiles(load.tiles, Color(0xFF90A4AE))
                    }
            )
        }
        RecordScopeSlot(label = "이웃\n자기 레이어") {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer()
                    .drawBehind {
                        counters.layered++
                        drawNeighborTiles(load.tiles, Color(0xFF80CBC4))
                    }
            )
        }
    }
}

@Composable
private fun RecordScopeSlot(label: String, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        content()
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color(0xFF616161), lineHeight = 13.sp)
    }
}

/** 변형마다 다른 수식어 순서 — 같은 drawBehind 를 레이어 앞 · 뒤 · 없이 둔다 */
private fun Modifier.glow(layer: GlowLayer, onDraw: DrawScope.() -> Unit): Modifier {
    val sized = size(48.dp)
    return when (layer) {
        GlowLayer.NONE -> sized.drawBehind(onDraw)
        GlowLayer.LAYER_BEFORE -> sized.graphicsLayer().drawBehind(onDraw)
        GlowLayer.LAYER_AFTER -> sized.drawBehind(onDraw).graphicsLayer()
        GlowLayer.CLIP_BEFORE -> sized.clip(CircleShape).drawBehind(onDraw)
    }
}

private fun DrawScope.drawGlow(frame: Int) {
    val phase = (frame % 90) / 90f
    val pulse = 0.5f + 0.5f * sin(phase * 2f * PI.toFloat())
    val radius = size.minDimension / 2f
    drawCircle(color = Color(0xFFFFD740).copy(alpha = 0.25f + 0.35f * pulse), radius = radius * (0.6f + 0.4f * pulse))
    drawCircle(color = Color(0xFFFFAB00), radius = radius * 0.35f)
}

@Composable
private fun RecordScopeResults(load: NeighborLoad, rows: List<RecordWindowResult>) {
    Spacer(modifier = Modifier.height(10.dp))
    Text(text = "${load.label} — 2초 창마다", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF424242))
    rows.forEach { row ->
        val plain = row.verdict(RecordTarget.PLAIN_NEIGHBOR)
        Text(
            text = "${if (row.matchesExpectation) "✓" else "✗"} ${row.layer.label}: 프레임 ${row.frames} · 글로우 ${row.glowDraws} · " +
                "이웃 ${row.plainDraws} · 이웃(레이어) ${row.layeredDraws} · 그리기 중앙값 " +
                (row.drawMillisMedian?.let { "%.3f".format(Locale.ROOT, it) + "ms" } ?: "—") +
                (if (row.touched) " · 건드림(판정 안 함)" else ""),
            fontSize = 11.sp,
            color = when (plain) {
                RecordVerdict.EVERY_FRAME -> Color(0xFFD32F2F)
                RecordVerdict.REUSED -> Color(0xFF2E7D32)
                null -> Color(0xFF757575)
            },
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
    savedMillis(rows, load)?.let { saved ->
        Text(
            text = "레이어 없음 → graphicsLayer() 앞: 프레임당 그리기 " + "%.3f".format(Locale.ROOT, saved) + "ms 차이",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ==================== 6. 신호 합성 ====================

@Composable
private fun WaveformSignalModelCard() {
    WaveformCard {
        Text(
            text = "5. 신호 합성 방식",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "실제 센서 대신, 한 박동 안의 위치(phase 0~1)를 입력으로 받는 순수 함수로 파형을 만듭니다. " +
                    "ECG는 P파·Q·R·S·T 다섯 개의 가우시안을 더한 것이고, PPG는 수축기 피크와 중복맥 봉우리 " +
                    "두 개의 합입니다. phase 는 dt 로 누적하므로 BPM 슬라이더를 움직여도 파형이 튀지 않고 " +
                    "주기만 부드럽게 바뀝니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        WaveformCodeBlock(
            code = """
                // 한 박동 안의 위치 phase(0~1) → 신호값
                WaveformSignal.ECG ->
                    gaussian(phase, 0.18f, 0.028f,  0.12f) +  // P파
                    gaussian(phase, 0.30f, 0.008f, -0.10f) +  // Q
                    gaussian(phase, 0.33f, 0.009f,  1.00f) +  // R 스파이크
                    gaussian(phase, 0.37f, 0.012f, -0.28f) +  // S
                    gaussian(phase, 0.56f, 0.045f,  0.30f)    // T파

                // phase 는 누적식이라 BPM 이 바뀌어도 불연속이 없다
                phase += (1f / SAMPLE_RATE) / (60f / bpm)
            """.trimIndent()
        )
    }
}

// ==================== 7. 정리 ====================

@Composable
private fun WaveformSummaryCard() {
    WaveformCard {
        Text(
            text = "정리",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• 시간축은 프레임이 아니라 dt 로 계산한다 — 주사율이 다른 기기에서도 같은 속도로 흐른다.\n" +
                    "• 고빈도 데이터는 스냅샷 상태로 만들지 않는다 — 배열에 담고, 갱신 신호 하나만 상태로 둔다.\n" +
                    "• 그 갱신 신호는 드로우 람다 안에서 읽는다 — 컴포지션을 건너뛰고 그리기만 다시 한다.\n" +
                    "• 매 프레임 그리는 노드는 graphicsLayer() 를 drawBehind 앞에 — 같은 레이어의 이웃까지 다시 기록되지 않는다" +
                    "(카드 · clip · Lazy 아이템이 이미 경계라서 빠뜨려도 잘 안 보인다).\n" +
                    "• 화면에 숫자를 띄워야 한다면 주기를 낮춘다 — 매 프레임 갱신하는 텍스트 한 줄이 " +
                    "캔버스보다 비쌀 수 있다.\n" +
                    "• Sweep/Scroll 은 같은 버퍼를 인덱스로 읽느냐 나이(age)로 읽느냐의 차이일 뿐이다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
    }
}

// ==================== 공용 UI 조각 ====================

@Composable
private fun WaveformCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun WaveformChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF1976D2) else Color(0xFFE3F2FD))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF1976D2),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WaveformCodeBlock(code: String) {
    Text(
        text = code,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFE0E0E0),
        lineHeight = 17.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp)
    )
}

// ==================== 신호 생성 루프 ====================

/**
 * 프레임 클럭에 맞춰 신호를 생성해 링 버퍼에 밀어 넣는다.
 *
 * withFrameNanos 의 dt 로 '이번 프레임에 몇 개를 넣을지'를 계산하므로,
 * 화면 주사율이 60/90/120Hz 중 무엇이든 샘플레이트는 [WAVE_SAMPLE_RATE] 로 일정하다.
 */
private suspend fun runWaveformLoop(controller: WaveformController) {
    var lastNs = 0L
    var phase = 0f          // 한 박동 안의 위치(0~1)
    var carry = 0f          // 프레임 간 이월되는 샘플 소수부
    var frames = 0
    var window = 0f         // 지표 갱신 창(초)
    var lastCount = 0

    while (true) {
        withFrameNanos { ns ->
            val dt = if (lastNs == 0L) 0f else (ns - lastNs) / 1_000_000_000f
            lastNs = ns

            // 백그라운드 복귀 등으로 dt 가 크게 튀면 그 프레임은 버린다(파형이 순간 도약하는 것 방지)
            if (dt > 0f && dt < 0.5f) {
                if (controller.running) {
                    carry += dt * WAVE_SAMPLE_RATE
                    val want = carry.toInt()
                    carry -= want
                    // 밀린 만큼 전부 넣지 않고 상한을 둔다(넘치는 샘플은 버림)
                    val count = want.coerceAtMost(WAVE_MAX_SAMPLES_PER_FRAME)

                    val period = 60f / controller.bpm
                    val step = (1f / WAVE_SAMPLE_RATE) / period
                    val signal = controller.signal
                    val noise = controller.noise

                    repeat(count) {
                        phase += step
                        while (phase >= 1f) phase -= 1f
                        val value = sampleAt(signal, phase) +
                                (Random.nextFloat() - 0.5f) * noise * 2f
                        controller.buffer.push(value)
                    }
                    lastCount = count

                    // 드로우 단계에서 읽히는 갱신 신호(리컴포지션 아님)
                    controller.head.intValue = controller.buffer.head
                    controller.filled.intValue = controller.buffer.filled
                }

                frames++
                window += dt
                if (window >= 0.5f) {
                    controller.stats = WaveformStats(
                        fps = (frames / window).roundToInt(),
                        samplesPerFrame = lastCount,
                        filled = controller.buffer.filled
                    )
                    frames = 0
                    window = 0f
                }
            }
        }
    }
}

/** 가우시안 봉우리 — 중심 center, 폭 width, 높이 amp */
private fun gaussian(phase: Float, center: Float, width: Float, amp: Float): Float {
    val d = (phase - center) / width
    return amp * exp(-0.5f * d * d)
}

/** 한 박동 안의 위치(phase 0~1)에 해당하는 신호값 */
private fun sampleAt(signal: WaveformSignal, phase: Float): Float = when (signal) {
    WaveformSignal.ECG ->
        gaussian(phase, 0.18f, 0.028f, 0.12f) +   // P파
                gaussian(phase, 0.30f, 0.008f, -0.10f) +  // Q
                gaussian(phase, 0.33f, 0.009f, 1.00f) +   // R 스파이크
                gaussian(phase, 0.37f, 0.012f, -0.28f) +  // S
                gaussian(phase, 0.56f, 0.045f, 0.30f)     // T파

    WaveformSignal.PPG ->
        gaussian(phase, 0.26f, 0.090f, 1.00f) +   // 수축기 피크
                gaussian(phase, 0.50f, 0.070f, 0.32f)     // 중복맥 봉우리

    WaveformSignal.SINE -> sin(phase * 2f * PI.toFloat())

    WaveformSignal.NOISE -> (Random.nextFloat() - 0.5f) * 1.6f
}

// ==================== 렌더링 헬퍼 ====================

/** 모니터 눈금 격자 — 0.2초 간격 세로선(1초마다 진하게) + 8등분 가로선 */
private fun DrawScope.drawWaveformGrid() {
    val minorColor = Color(0xFF16302B)
    val majorColor = Color(0xFF23514A)
    val cols = WAVE_WINDOW_SECONDS * 5
    val rows = 8

    for (i in 0..cols) {
        val x = size.width * i / cols
        drawLine(
            color = if (i % 5 == 0) majorColor else minorColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = if (i % 5 == 0) 1.4f else 0.8f
        )
    }
    for (i in 0..rows) {
        val y = size.height * i / rows
        drawLine(
            color = if (i % 4 == 0) majorColor else minorColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = if (i % 4 == 0) 1.4f else 0.8f
        )
    }
}

/**
 * 링 버퍼의 샘플을 하나의 Path 로 이어 그린다.
 *
 * - SCROLL: 가장 오래된 샘플부터 순서대로 이어 최신 샘플을 오른쪽 끝에 배치
 * - SWEEP: 버퍼 인덱스를 그대로 화면 x 좌표로 사용하고, 커서 앞쪽 [WAVE_SWEEP_GAP] 구간만 비움
 */
private fun DrawScope.drawWaveformTrace(
    buffer: WaveformRingBuffer,
    head: Int,
    filled: Int,
    mode: WaveformMode,
    signal: WaveformSignal,
    path: Path,
) {
    if (filled == 0) return

    val capacity = buffer.capacity
    val stepX = size.width / (capacity - 1)
    val baseY = size.height * signal.baseline
    val gain = size.height * signal.gain

    path.reset()

    when (mode) {
        WaveformMode.SCROLL -> {
            val start = (head - filled + capacity) % capacity
            // 최신 샘플이 항상 오른쪽 끝에 오도록 시작 x 를 밀어 준다(버퍼가 덜 찼을 때도 동일)
            val offsetX = size.width - (filled - 1) * stepX
            for (i in 0 until filled) {
                val value = buffer.samples[(start + i) % capacity]
                val x = offsetX + i * stepX
                val y = baseY - value * gain
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
        }

        WaveformMode.SWEEP -> {
            var penDown = false
            for (i in 0 until capacity) {
                // 아직 한 번도 쓰이지 않은 구간
                if (filled < capacity && i >= filled) {
                    penDown = false
                    continue
                }
                // 커서 바로 앞 구간은 비워 둬서 '지우개'처럼 보이게 한다
                if ((i - head + capacity) % capacity < WAVE_SWEEP_GAP) {
                    penDown = false
                    continue
                }
                val x = i * stepX
                val y = baseY - buffer.samples[i] * gain
                if (penDown) {
                    path.lineTo(x, y)
                } else {
                    path.moveTo(x, y)
                    penDown = true
                }
            }
        }
    }

    drawPath(
        path = path,
        color = signal.color,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    if (mode == WaveformMode.SWEEP) {
        val cursorX = head * stepX
        drawLine(
            color = signal.color.copy(alpha = 0.5f),
            start = Offset(cursorX, 0f),
            end = Offset(cursorX, size.height),
            strokeWidth = 1.5f
        )
    }
}
