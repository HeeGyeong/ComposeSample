package com.example.composesample.presentation.example.component.ui.shader

import android.content.Context
import android.graphics.RuntimeShader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.provider.Settings
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.boldMarkup
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * 스크래치 아트 예제 — 매 프레임 바뀌는 배경 위에서 '긁기'를 하려면 그리는 순서를 뒤집어야 한다.
 * - 배경(셰이더)을 통째로 그리고, 그 위 검은 판을 오프스크린 레이어에 그린 뒤 획마다 BlendMode.Clear 로 구멍을 낸다.
 * - 배경은 ShaderBrush 하위 클래스가 AGSL RuntimeShader 를 돌려준다(API 33+, 미만은 정적 그라데이션).
 *   ShaderBrush 는 크기가 같으면 createShader 를 다시 부르지 않는다 → 기울기가 바뀌면 uniform 만 바꾼다.
 * - 기울기는 GAME_ROTATION_VECTOR(없으면 가속도계) → 저역 통과 · 작은 변화 무시 → State. drawBehind 안에서만 읽어
 *   그리기 단계만 다시 돈다. 시스템 애니메이션이 꺼져 있으면 움직이지 않는다.
 * - 센서 → 기울기 계산과 필터는 같은 폴더의 ScratchFoilLab.kt(JVM 단위 테스트 ScratchFoilLabTest 와 공유).
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "스크래치 아트" 섹션 참고.
 */
@Composable
fun ScratchFoilExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val hub = remember { TiltSensorHub(context.getSystemService(SensorManager::class.java)) }
    var animationsOn by remember { mutableStateOf(true) }

    // 화면이 보일 때마다: 애니메이션 설정을 다시 읽고, 그때 자세를 중립으로 센서를 켠다. 안 보이면 끈다.
    LifecycleResumeEffect(hub) {
        animationsOn = animationsEnabled(context)
        if (animationsOn) hub.start(view.display?.rotation ?: Surface.ROTATION_0) else hub.freeze()
        onPauseOrDispose { hub.stop() }
    }

    // BlogExampleActivity 는 회전을 직접 처리한다(다시 만들어지지 않아 onResume 도 없다) →
    // 구성이 바뀌면 화면 회전을 다시 읽어 축을 바꾸고, 그 자세를 새 중립으로 잡는다
    val orientation = LocalConfiguration.current.orientation
    LaunchedEffect(orientation) {
        hub.updateRotation(view.display?.rotation ?: Surface.ROTATION_0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "스크래치 아트 — BlendMode.Clear · ShaderBrush",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { ScratchBoardCard(hub, animationsOn) }
            item { LayerCompareCard() }
            item { BrushLifecycleCard() }
            item { TiltReadCard(animationsOn) }
            item { TiltFilterCard(hub) }
            item { ChecklistCard() }
        }
    }
}

// ==================== 배경 브러시 ====================

/**
 * 짧은 홀로그램 셰이더 — 원문(약 100줄)의 네 겹 중 박막 간섭 색과 빛 띠만 남겼다.
 * 굴곡은 fbm 도메인 워핑 대신 sin 두 개, 글리터는 뺐다(셰이더 수학은 이 예제의 범위 밖).
 */
private const val FOIL_SHADER = """uniform float2 resolution;
uniform float2 tilt;

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / resolution;
    // 1) 굴곡 — 위치마다 다른 '막 두께'의 바탕
    float relief = 0.5 * sin(uv.x * 7.0 + 1.5 * sin(uv.y * 5.0)) + 0.5 * sin(uv.y * 9.0 - uv.x * 4.0);
    // 2) 박막 간섭 — 두께를 첫 간섭 차수(0.45~1.95)에 두고 RGB 를 파장(650·530·450nm) 비율의 cos 로
    float thickness = clamp(1.2 + 0.35 * relief + 0.4 * tilt.x - 0.3 * tilt.y + 0.25 * (uv.x - uv.y), 0.45, 1.95);
    float3 film = 0.5 + 0.5 * cos(6.2831853 * (thickness * float3(1.0, 1.226, 1.444) + float3(0.0, 0.08, 0.16)));
    film = 0.45 + 0.55 * film;
    // 3) 빛 띠 — 기울기를 따라 움직이는 대각선, screen 합성 1 - (1 - a)(1 - b)
    float d = 0.5 * (uv.x + uv.y) - (0.5 + 0.45 * tilt.x + 0.25 * tilt.y);
    float band = 0.75 * exp(-d * d * 120.0) + 0.25 * exp(-d * d * 10.0);
    float3 color = 1.0 - (1.0 - film) * (1.0 - band);
    return half4(half3(color), 1.0);
}"""

private val FALLBACK_COLORS = listOf(Color(0xFF26C6DA), Color(0xFF7E57C2), Color(0xFFEC407A), Color(0xFFFFCA28))

/** 배경 브러시 공통 — tilt 를 바꾸면 다음 그리기에 반영된다. */
private abstract class TiltBrush(protected val stats: FoilDrawStats) : ShaderBrush() {
    abstract var tilt: Offset
}

/**
 * 셰이더 하나를 필드로 들고, createShader 는 해상도만 넣어 같은 객체를 돌려준다.
 * ShaderBrush.applyTo 가 크기가 바뀔 때만 createShader 를 부르므로 기울기 변화는 uniform 갱신뿐이다.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class FoilShaderBrush(stats: FoilDrawStats) : TiltBrush(stats) {
    private val shader = RuntimeShader(FOIL_SHADER).also { stats.shaderCompiles++ }

    override var tilt: Offset = Offset.Zero
        set(value) {
            if (field == value) return
            field = value
            shader.setFloatUniform("tilt", value.x, value.y)
        }

    override fun createShader(size: Size): Shader {
        stats.createShaderCalls++
        shader.setFloatUniform("resolution", size.width, size.height)
        return shader
    }
}

/** API 33 미만 — 원문처럼 기울기에 반응하지 않는 정적 그라데이션(움직이는 그라데이션 흉내는 원문에서 뺐다). */
private class GradientShaderBrush(stats: FoilDrawStats) : TiltBrush(stats) {
    override var tilt: Offset = Offset.Zero

    override fun createShader(size: Size): Shader {
        stats.createShaderCalls++
        return LinearGradientShader(
            from = Offset.Zero,
            to = Offset(size.width, size.height),
            colors = FALLBACK_COLORS
        )
    }
}

private val isFoilSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

private fun newTiltBrush(stats: FoilDrawStats): TiltBrush =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) FoilShaderBrush(stats) else GradientShaderBrush(stats)

// ==================== 기울기 센서 ====================

private enum class TiltSource(val label: String) {
    GAME_ROTATION_VECTOR("TYPE_GAME_ROTATION_VECTOR (자이로 + 가속도, 나침반 없음)"),
    ACCELEROMETER("TYPE_ACCELEROMETER (대체)"),
    NONE("기울기 센서 없음")
}

/**
 * 센서 → 필터 → 상태. 리스너를 컴포저블 안 익명 객체로 두지 않고 상태 홀더로 모았다.
 * 이벤트는 메인 스레드로 온다(핸들러 미지정) → tilt 쓰기는 메인 스레드.
 */
private class TiltSensorHub(private val sensorManager: SensorManager?) : SensorEventListener {
    val tilt = mutableStateOf(Offset.Zero)
    val filter = TiltFilter()

    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val source: TiltSource = when (sensor?.type) {
        Sensor.TYPE_GAME_ROTATION_VECTOR -> TiltSource.GAME_ROTATION_VECTOR
        Sensor.TYPE_ACCELEROMETER -> TiltSource.ACCELEROMETER
        else -> TiltSource.NONE
    }

    private var rotation = Surface.ROTATION_0

    fun start(displayRotation: Int) {
        rotation = displayRotation
        filter.recalibrate()
        tilt.value = Offset.Zero
        // GAME(약 20ms) — UI(약 60ms)는 빛이 계단처럼 움직인다. 작은 변화는 필터가 버리므로 다시 그리기는 늘지 않는다.
        sensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    /** 화면이 돌았을 때 — 축 대응이 바뀌므로 지금 자세를 새 중립으로. */
    fun updateRotation(displayRotation: Int) {
        if (displayRotation == rotation) return
        rotation = displayRotation
        filter.recalibrate()
        tilt.value = Offset.Zero
    }

    /** 시스템 애니메이션 꺼짐 — 센서를 켜지 않고 중립에 멈춘다. */
    fun freeze() {
        tilt.value = Offset.Zero
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val up = if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            upVectorFromRotationVector(event.values)
        } else {
            upVectorFromAccelerometer(event.values)
        }
        filter.offer(toScreenFrame(up, rotation))?.let { tilt.value = it }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

/** 개발자 옵션 '애니메이터 길이 배율' 0 = 애니메이션 끔(접근성 '애니메이션 삭제'도 같은 값). */
private fun animationsEnabled(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f

// ==================== 긁은 자국 ====================

/** 획은 Path 를 그대로 늘리고 version 만 올린다 → 그리기 블록이 version 을 읽어 그리기 단계만 다시 돈다. */
private class ScratchStrokes {
    private val paths = ArrayList<Path>()
    private var version by mutableIntStateOf(0)

    /** 화면 집계용(상태 아님). */
    val count: Int get() = paths.size

    fun start(at: Offset) {
        // 같은 점으로 lineTo — 둥근 끝(StrokeCap.Round)이 점 하나를 지운다(탭만 해도 긁힌다)
        paths += Path().apply {
            moveTo(at.x, at.y)
            lineTo(at.x, at.y)
        }
        version++
    }

    fun extend(to: Offset) {
        val path = paths.lastOrNull() ?: return
        path.lineTo(to.x, to.y)
        version++
    }

    fun clear() {
        paths.clear()
        version++
    }

    fun forEachPath(block: (Path) -> Unit) {
        // 이 읽기로 그리기 블록이 version 을 관찰한다. 0 = 아직 긁은 적 없음
        if (version == 0) return
        paths.forEach(block)
    }
}

private val SCRATCH_COVER = Color(0xFF151515)

private fun scratchStroke(width: Float) = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

// ==================== 0. 개요 ====================

@Composable
private fun OverviewCard() {
    SectionCard(title = "획에 '색'을 줄 수 없는 배경") {
        BodyText(
            "참고 글(exampleGuide.kt)의 첫 버전은 획마다 그 자리의 배경색으로 칠했다. 배경이 그라데이션처럼 고정이면 되지만, " +
                "**매 프레임 바뀌는 홀로그램 배경에서는 획이 가질 색이 없다.** 그래서 순서를 뒤집었다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "// 원문(간추림) — 배경은 따로 통째로, 검은 판은 레이어 안에서 획마다 구멍\n" +
                "drawIntoCanvas { it.saveLayer(surfaceSize.toRect(), Paint()) }\n" +
                "drawRect(surfaceColor, size = surfaceSize)\n" +
                "strokes.forEach { drawPath(it, Color.Black, blendMode = BlendMode.Clear) }\n" +
                "drawIntoCanvas { it.restore() }"
        )
        Spacer(modifier = Modifier.height(6.dp))
        TableRow("카드", "보는 것", isHeader = true)
        TableRow("1. 긁어 보기", "기울기로 움직이는 셰이더 배경 위 검은 판 — 그리기 · 리컴포지션 횟수")
        TableRow("2. 레이어", "saveLayer · Offscreen · 레이어 없음에서 Clear 가 지우는 범위")
        TableRow("3. ShaderBrush", "createShader · 셰이더 컴파일이 언제 다시 일어나나")
        TableRow("4. 읽는 위치", "기울기를 drawBehind 안에서 읽을 때와 컴포지션에서 읽을 때")
        TableRow("5. 필터", "저역 통과 · 작은 변화 무시가 다시 그리기를 얼마나 줄이나")
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            if (isFoilSupported) {
                "이 기기는 API ${Build.VERSION.SDK_INT} — AGSL RuntimeShader 배경을 쓴다."
            } else {
                "이 기기는 API ${Build.VERSION.SDK_INT} — AGSL(API 33+)이 없어 정적 그라데이션 배경을 쓴다. 원문 앱도 같은 선택을 했다."
            }
        )
    }
}

// ==================== 1. 긁어 보기 ====================

@Composable
private fun ScratchBoardCard(hub: TiltSensorHub, animationsOn: Boolean) {
    val stats = remember { FoilDrawStats() }
    val brush = remember { newTiltBrush(stats) }
    val strokes = remember { ScratchStrokes() }
    var board by remember { mutableStateOf(stats.snapshot()) }
    var sensor by remember { mutableStateOf(SensorCounts(0, 0)) }
    var strokeCount by remember { mutableIntStateOf(0) }

    // 횟수는 일반 필드 → 0.5초마다 상태로 옮긴다. 이 카드만 다시 그려지고 보드는 건너뛴다(인자가 같다).
    LaunchedEffect(Unit) {
        while (true) {
            board = stats.snapshot()
            sensor = SensorCounts(hub.filter.received, hub.filter.emitted)
            strokeCount = strokes.count
            delay(500)
        }
    }

    SectionCard(title = "1. 긁어 보기 — 배경은 통째로, 획은 구멍") {
        BodyText(
            if (isFoilSupported) {
                "검은 판을 손가락으로 긁어 보세요. 기기를 기울이면 긁힌 자리 아래의 빛과 색이 움직인다."
            } else {
                "검은 판을 손가락으로 긁어 보세요. 이 기기(API 33 미만)의 배경은 기울여도 움직이지 않는 정적 그라데이션이다."
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ScratchBoard(brush, hub.tilt, strokes, stats)
        }
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "다시 덮기", color = Color(0xFF455A64)) { strokes.clear() }
        Spacer(modifier = Modifier.height(6.dp))
        ResultRow("배경", if (isFoilSupported) "AGSL RuntimeShader + ShaderBrush" else "정적 그라데이션 (API 33 미만)")
        ResultRow("기울기", if (animationsOn) hub.source.label else "시스템 애니메이션 꺼짐 → 센서를 켜지 않고 정지")
        ResultRow("센서", "받음 ${sensor.received} · 내보냄 ${sensor.emitted}")
        ResultRow("보드", "그리기 ${board.draws} · 리컴포지션 ${board.recompositions} · createShader ${board.createShaderCalls} · 획 $strokeCount")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "리컴포지션은 첫 컴포지션을 포함한 수다. 긁기와 기울기는 **그리기만 늘리고 리컴포지션은 늘리지 않는다** — " +
                "둘 다 drawBehind 안에서만 읽기 때문이다."
        )
    }
}

private data class SensorCounts(val received: Int, val emitted: Int)

@Composable
private fun ScratchBoard(
    brush: TiltBrush,
    tilt: State<Offset>,
    strokes: ScratchStrokes,
    stats: FoilDrawStats
) {
    SideEffect { stats.recompositions++ }
    Box(
        modifier = Modifier
            .widthIn(max = MAX_SAMPLE_WIDTH)
            .fillMaxWidth()
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                stats.draws++
                // 배경 — 기울기 State 를 여기서만 읽는다
                brush.tilt = tilt.value
                drawRect(brush)
                // 검은 판 — 레이어 안에서만 Clear 가 지운다(레이어 밖 배경은 그대로)
                drawIntoCanvas { it.saveLayer(size.toRect(), Paint()) }
                drawRect(SCRATCH_COVER)
                val stroke = scratchStroke(SCRATCH_WIDTH.toPx())
                strokes.forEachPath { drawPath(it, Color.Black, style = stroke, blendMode = BlendMode.Clear) }
                drawIntoCanvas { it.restore() }
            }
            .pointerInput(strokes) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    strokes.start(down.position)
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        if (change.positionChanged()) {
                            // 소비해야 바깥 LazyColumn 이 스크롤로 가져가지 않는다
                            change.consume()
                            strokes.extend(change.position)
                        }
                    }
                }
            }
    )
}

private val SCRATCH_WIDTH = 26.dp

/** 태블릿 · 가로 화면에서 보드와 비교 상자가 화면 높이를 넘지 않게. */
private val MAX_SAMPLE_WIDTH = 480.dp

// ==================== 2. 레이어 비교 ====================

private enum class LayerMode(val label: String) {
    SAVE_LAYER("saveLayer\n(원문)"),
    OFFSCREEN("Offscreen\n레이어"),
    NONE("레이어\n없음")
}

@Composable
private fun LayerCompareCard() {
    val stats = remember { FoilDrawStats() }
    val brush = remember { newTiltBrush(stats) }

    SectionCard(title = "2. Clear 는 어디까지 지우나 — 레이어가 경계") {
        BodyText(
            "같은 배경 · 같은 검은 판 · 같은 획을 세 방식으로 그렸다. BlendMode.Clear 는 **지금 그리는 대상(레이어)의 픽셀을 투명하게** 만든다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        // SectionCard 의 content 는 ColumnScope 가 아니다 → Box 로 가운데 정렬
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier
                    .widthIn(max = MAX_SAMPLE_WIDTH)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayerMode.entries.forEach { mode ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LayerSample(mode, brush)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TableRow("방식", "결과", isHeader = true)
        TableRow("saveLayer", "레이어 안의 검은 판만 지워진다 → 획 자리에 배경이 보인다")
        TableRow("Offscreen", "검은 판 Box 에 graphicsLayer(CompositingStrategy.Offscreen) — 같은 결과, Compose 식 표현")
        TableRow("레이어 없음", "배경 · 카드까지 같은 버퍼에서 지워져 획 자리가 검다. 상자의 clip(graphicsLayer)으로는 막지 못한다 — 오프스크린 버퍼가 아니다")
    }
}

@Composable
private fun LayerSample(mode: LayerMode, brush: TiltBrush) {
    val boxModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(0.8f)
        .clip(RoundedCornerShape(8.dp))
    when (mode) {
        LayerMode.SAVE_LAYER -> Box(
            modifier = boxModifier.drawBehind {
                drawRect(brush)
                drawIntoCanvas { it.saveLayer(size.toRect(), Paint()) }
                drawRect(SCRATCH_COVER)
                drawFixedScratch()
                drawIntoCanvas { it.restore() }
            }
        )

        LayerMode.OFFSCREEN -> Box(modifier = boxModifier.drawBehind { drawRect(brush) }) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawBehind {
                        drawRect(SCRATCH_COVER)
                        drawFixedScratch()
                    }
            )
        }

        LayerMode.NONE -> Box(
            modifier = boxModifier.drawBehind {
                drawRect(brush)
                drawRect(SCRATCH_COVER)
                drawFixedScratch()
            }
        )
    }
}

/** 고정 획 — 가운데를 지나는 물결 하나와 세로선 하나(가운데 픽셀이 늘 획 위에 있다). */
private fun DrawScope.drawFixedScratch() {
    val path = Path()
    val segments = 40
    for (i in 0..segments) {
        val t = i / segments.toFloat()
        val x = size.width * (0.1f + 0.8f * t)
        val y = size.height * (0.5f + 0.22f * sin(2f * PI.toFloat() * t))
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.moveTo(size.width * 0.5f, size.height * 0.15f)
    path.lineTo(size.width * 0.5f, size.height * 0.85f)
    drawPath(path, Color.Black, style = scratchStroke(14.dp.toPx()), blendMode = BlendMode.Clear)
}

// ==================== 3. ShaderBrush 수명 ====================

@Composable
private fun BrushLifecycleCard() {
    val stats = remember { FoilDrawStats() }
    var rememberBrush by remember { mutableStateOf(true) }
    var tall by remember { mutableStateOf(false) }
    val tick = remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<BrushResult?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun measure(action: String, block: suspend () -> Unit) {
        if (running) return
        running = true
        scope.launch {
            stats.reset()
            block()
            // 마지막 상태 변경이 그려질 때까지 두 프레임 기다린 뒤 센다
            repeat(2) { withFrameNanos { } }
            result = BrushResult(action, rememberBrush, stats.snapshot())
            running = false
        }
    }

    SectionCard(title = "3. ShaderBrush — 셰이더는 크기마다 한 번") {
        BodyText(
            "원문은 '캔버스 크기가 같은 동안 같은 셰이더를 쓰는' ShaderBrush 를 만들었다. 이 동작은 **ShaderBrush 의 기본**이다 — " +
                "applyTo 가 크기를 비교해 바뀔 때만 createShader 를 부른다. 단, **브러시 객체가 살아 있을 때만**."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "// ui-graphics 1.12.1 ShaderBrush.applyTo (간추림)\n" +
                "if (transformShader == null || createdSize != size) {\n" +
                "    shader = createShader(size)\n" +
                "    createdSize = size\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = rememberBrush,
                onClick = { rememberBrush = true },
                label = { Text("remember 한 브러시", fontSize = 11.sp) },
                enabled = !running
            )
            FilterChip(
                selected = !rememberBrush,
                onClick = { rememberBrush = false },
                label = { Text("매번 새 브러시", fontSize = 11.sp) },
                enabled = !running
            )
        }
        BrushLifecyclePreview(rememberBrush, tall, tick, stats)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "리컴포즈 10회", color = Color(0xFF5E35B1)) {
                measure("리컴포즈 10회") {
                    repeat(10) {
                        tick.intValue++
                        withFrameNanos { }
                    }
                }
            }
            DemoButton(text = "크기 바꾸기", color = Color(0xFF00897B)) {
                measure("크기 바꾸기") { tall = !tall }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val r = result
        if (r == null) {
            CaptionText("버튼을 누르면 그 동작 동안의 횟수를 센다.")
        } else {
            ResultRow("측정", "${r.action} · ${if (r.remembered) "remember 한 브러시" else "매번 새 브러시"}")
            ResultRow(
                "횟수",
                "리컴포지션 ${r.counts.recompositions} · createShader ${r.counts.createShaderCalls} · " +
                    "셰이더 컴파일 ${if (isFoilSupported) r.counts.shaderCompiles.toString() else "—"} · 그리기 ${r.counts.draws}"
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "'리컴포즈 n' 글자를 상자 **안**에 두었을 때는 remember 한 브러시도 그리기가 10 이었다(createShader 는 0). " +
                "그리기는 레이어 단위라 같은 레이어(clip) 안의 글자가 바뀌면 drawBehind 도 다시 돈다 — 그래서 글자를 밖으로 뺐다."
        )
    }
}

private data class BrushResult(val action: String, val remembered: Boolean, val counts: FoilDrawSnapshot)

@Composable
private fun BrushLifecyclePreview(
    rememberBrush: Boolean,
    tall: Boolean,
    tick: State<Int>,
    stats: FoilDrawStats
) {
    // tick 을 이 함수 본문에서 읽는다 → 값이 바뀌면 이 함수가 다시 실행된다
    val count = tick.value
    SideEffect { stats.recompositions++ }
    val remembered = remember { newTiltBrush(stats) }
    val brush = if (rememberBrush) remembered else newTiltBrush(stats)
    // 글자는 상자 밖에 — 같은 레이어(clip) 안에서 글자가 바뀌면 레이어째 다시 그려 drawBehind 도 다시 돈다
    Text(text = "리컴포즈 $count", fontSize = 12.sp, color = Color(0xFF616161), modifier = Modifier.padding(top = 8.dp))
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .height(if (tall) 140.dp else 96.dp)
            .clip(RoundedCornerShape(8.dp))
            .drawBehind {
                stats.draws++
                drawRect(brush)
            }
    )
}

// ==================== 4. 기울기를 읽는 위치 ====================

private enum class TiltReadMode(val label: String) {
    DRAW("drawBehind 안에서 읽기"),
    COMPOSITION("컴포지션에서 읽기")
}

@Composable
private fun TiltReadCard(animationsOn: Boolean) {
    val stats = remember { FoilDrawStats() }
    val brush = remember { newTiltBrush(stats) }
    val tilt = remember { mutableStateOf(Offset.Zero) }
    var mode by remember { mutableStateOf(TiltReadMode.DRAW) }
    var result by remember { mutableStateOf<TiltReadResult?>(null) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    SectionCard(title = "4. 기울기는 그리기 단계에서만 읽는다") {
        BodyText(
            "기울기 State 를 어디서 읽느냐에 따라 무효화 범위가 다르다. 8자 궤적(원문 앱이 공유 영상에서 쓴 움직임)을 " +
                "$FIGURE_EIGHT_FRAMES 프레임 동안 흘려 보내고 횟수를 센다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "// 원문 — 그리기 단계만 무효화\n" +
                ".drawBehind {\n" +
                "    backgroundBrush.tilt = tilt.value\n" +
                "    drawRect(backgroundBrush)\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TiltReadMode.entries.forEach { m ->
                FilterChip(
                    selected = mode == m,
                    onClick = { mode = m },
                    label = { Text(m.label, fontSize = 11.sp) },
                    // 재생 중 모드를 바꾸면 두 방식의 횟수가 섞인다
                    enabled = !running
                )
            }
        }
        TiltPreview(mode, brush, tilt, stats)
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "8자 궤적 재생", color = Color(0xFF1565C0)) {
            if (running || !animationsOn) return@DemoButton
            running = true
            val playedMode = mode
            scope.launch {
                stats.reset()
                for (i in 1..FIGURE_EIGHT_FRAMES) {
                    withFrameNanos { }
                    tilt.value = figureEightTilt(i / FIGURE_EIGHT_FRAMES.toFloat())
                }
                repeat(2) { withFrameNanos { } }
                result = TiltReadResult(playedMode, stats.snapshot())
                running = false
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val r = result
        when {
            !animationsOn -> CaptionText("시스템 애니메이션이 꺼져 있어 재생하지 않는다(원문 앱도 이때는 홀로그램을 멈춘다).")
            r == null -> CaptionText("모드를 고르고 재생하면 그 동안의 횟수를 센다.")
            else -> {
                ResultRow("측정", "${r.mode.label} · 기울기 변경 $FIGURE_EIGHT_FRAMES 회")
                ResultRow("횟수", "리컴포지션 ${r.counts.recompositions} · 그리기 ${r.counts.draws} · createShader ${r.counts.createShaderCalls}")
            }
        }
    }
}

private data class TiltReadResult(val mode: TiltReadMode, val counts: FoilDrawSnapshot)

@Composable
private fun TiltPreview(
    mode: TiltReadMode,
    brush: TiltBrush,
    tilt: State<Offset>,
    stats: FoilDrawStats
) {
    SideEffect { stats.recompositions++ }
    val drawModifier = when (mode) {
        TiltReadMode.DRAW -> Modifier.drawBehind {
            stats.draws++
            brush.tilt = tilt.value
            drawRect(brush)
        }

        TiltReadMode.COMPOSITION -> {
            // 본문에서 읽음 → 값마다 이 함수가 다시 실행되고, 새 값을 잡은 새 람다로 그리기도 다시
            val current = tilt.value
            Modifier.drawBehind {
                stats.draws++
                brush.tilt = current
                drawRect(brush)
            }
        }
    }
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(drawModifier)
    )
}

// ==================== 5. 기울기 필터 ====================

@Composable
private fun TiltFilterCard(hub: TiltSensorHub) {
    val runs = remember {
        FilterScenario.entries.map { scenario ->
            scenario to FilterSetting.entries.map { setting -> setting to runFilterCase(scenario, setting) }
        }
    }
    var live by remember { mutableStateOf(SensorCounts(0, 0)) }
    LaunchedEffect(Unit) {
        while (true) {
            live = SensorCounts(hub.filter.received, hub.filter.emitted)
            delay(500)
        }
    }

    SectionCard(title = "5. 흔들리는 센서 · 탁자 위 기기") {
        BodyText(
            "센서 값은 떨린다. 원문은 모든 값을 저역 통과에 거르고, **눈에 안 보일 만큼 작은 변화는 내보내지 않는다** — " +
                "탁자 위 기기는 화면을 다시 그리지 않게 된다. 같은 시드의 고정 입력 $FILTER_SAMPLES 개를 세 설정에 넣었다(문턱 = MIN_VISIBLE_CHANGE)."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "smoothed += (target - smoothed) * alpha        // alpha = $TILT_ALPHA\n" +
                "if ((smoothed - lastEmitted).getDistance() < MIN_VISIBLE_CHANGE) return null  // $MIN_VISIBLE_CHANGE"
        )
        Spacer(modifier = Modifier.height(6.dp))
        runs.forEach { (scenario, settings) ->
            Text(
                text = scenario.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F),
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            )
            TableRow("설정", "내보냄 / 받음 · 멈춘 동안 내보냄", isHeader = true)
            settings.forEach { (setting, run) ->
                TableRow(setting.label, "${run.emitted} / ${run.received} · ${run.emittedWhileStill}")
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        ResultRow("이 기기", "받음 ${live.received} · 내보냄 ${live.emitted} (1번 카드와 같은 센서)")
        CaptionText(
            "화면이 다시 보일 때(onResume)의 자세가 중립이다 — 누워서 보든 앉아서 보든 그때 자세에서 기울인 만큼만 움직인다. " +
                "기울기는 위쪽 벡터의 x · y 성분이 아니라 중립 자세에 수직인 두 축으로 잰다(세워 든 기기에서 앞뒤 기울기를 놓치지 않는다)."
        )
    }
}

// ==================== 6. 정리 ====================

@Composable
private fun ChecklistCard() {
    SectionCard(title = "정리 — 매 프레임 바뀌는 배경 위에 그릴 때") {
        PitfallRow(
            "획이 배경 색을 들고 있지 않은가?",
            "배경이 움직이면 그 색은 다음 프레임에 틀린다. 배경을 통째로 그리고, 위 판에 구멍을 낸다."
        )
        PitfallRow(
            "Clear 가 지울 범위를 레이어로 막았는가?",
            "saveLayer · restore 또는 graphicsLayer(CompositingStrategy.Offscreen). 없으면 배경까지 지운다."
        )
        PitfallRow(
            "브러시를 remember 했는가?",
            "ShaderBrush 의 크기별 캐시는 브러시 객체 안에 있다. 매번 새로 만들면 그때마다 createShader · 셰이더 컴파일."
        )
        PitfallRow(
            "자주 바뀌는 값을 어디서 읽는가?",
            "drawBehind(그리기 단계) 안에서 읽으면 리컴포지션 없이 다시 그리기만 한다."
        )
        PitfallRow(
            "센서가 쉬지 않고 다시 그리게 하지 않는가?",
            "저역 통과 + 작은 변화 무시, 보이지 않으면 해제(onPause), 시스템 애니메이션 꺼짐이면 정지."
        )
    }
}

// ==================== 공용 UI ====================

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
private fun BodyText(text: String) {
    Text(text = text.boldMarkup(), fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text.boldMarkup(), fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

/** 코드는 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(110.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(64.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
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
