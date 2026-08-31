package com.example.composesample.presentation.example.component.system.platform.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 센서 퓨전 나침반 예제
 * - TYPE_ROTATION_VECTOR 퓨전 센서 하나만 구독해 방위각을 얻는 4단계 파이프라인을 실측으로 보여준다.
 * - 화면 회전 보정(remapCoordinateSystem)과, 359°→0° 랩어라운드에서 바늘이 역주행하는
 *   저역통과 필터의 함정을 센서 없이도 재현되는 결정론적 데모로 대조한다.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 순수 계산부 (센서 이벤트와 무관하게 단독 검증 가능) ====================

/** 0~360 범위로 정규화. 음수 방위각(-π~π 변환 결과)도 여기서 흡수한다. */
private fun normalizeDegrees(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f

/**
 * 화면 회전(Surface.ROTATION_*)에 맞는 월드 축 매핑.
 * 센서 좌표계는 "기기의 자연 방향"에 고정돼 있어서, 화면이 돌아간 만큼 축을 되돌려줘야 한다.
 */
private fun worldAxesFor(displayRotation: Int): Pair<Int, Int> = when (displayRotation) {
    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
}

/**
 * 회전 벡터 → 화면 회전이 보정된 방위각(0~360°).
 * 축 재매핑이 실패하면(입력 행렬이 유효하지 않은 경우) null 을 돌려 이번 이벤트를 버린다.
 */
private fun azimuthDegreesOf(rotationVector: FloatArray, displayRotation: Int): Float? {
    if (rotationVector.size < 3) return null

    // 회전 벡터의 정의는 3~4개 성분이지만 실제 기기는 더 긴 배열을 보내기도 한다(SM-A725F/Android 13 = 5개).
    // getRotationMatrixFromVector 는 앞 3~4개만 읽으므로 원본을 그대로 넘긴다 —
    // 실측상 5개 원본과 앞 4개만 잘라낸 배열의 회전 행렬이 완전히 동일했고,
    // 자르면 초당 수십 번 배열만 새로 할당된다.
    val rotationMatrix = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

    val remapped = FloatArray(9)
    val (worldX, worldY) = worldAxesFor(displayRotation)
    if (!SensorManager.remapCoordinateSystem(rotationMatrix, worldX, worldY, remapped)) return null

    val orientation = FloatArray(3)
    SensorManager.getOrientation(remapped, orientation)

    // orientation[0] = azimuth(라디안, -π~π). 도로 바꾼 뒤 0~360 으로 정규화한다.
    return normalizeDegrees(Math.toDegrees(orientation[0].toDouble()).toFloat())
}

/** 나이브 저역통과 — 각도를 그냥 실수로 보간한다. 359°→1° 구간에서 바늘이 한 바퀴 역주행한다. */
private fun naiveLowPass(previous: Float, raw: Float, alpha: Float): Float =
    previous + alpha * (raw - previous)

/** 최단 경로 저역통과 — 두 각도의 차이를 -180~180 으로 정규화한 뒤 보간해 랩어라운드를 흡수한다. */
private fun shortestPathLowPass(previous: Float, raw: Float, alpha: Float): Float {
    val delta = ((raw - previous + 540f) % 360f) - 180f
    return normalizeDegrees(previous + alpha * delta)
}

private val CARDINAL_LABELS = listOf("북", "북동", "동", "남동", "남", "남서", "서", "북서")

/** 방위각 → 8방위 한글 이름. 22.5° 를 더해 반올림 경계를 각 방위의 중앙에 맞춘다. */
private fun cardinalOf(degrees: Float): String =
    CARDINAL_LABELS[((normalizeDegrees(degrees + 22.5f)) / 45f).toInt() % CARDINAL_LABELS.size]

private fun accuracyLabel(accuracy: Int): String = when (accuracy) {
    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "HIGH (신뢰 가능)"
    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "MEDIUM (보정 권장)"
    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "LOW (8자 캘리브레이션 필요)"
    SensorManager.SENSOR_STATUS_UNRELIABLE -> "UNRELIABLE (사용 불가)"
    SensorManager.SENSOR_STATUS_NO_CONTACT -> "NO_CONTACT (측정 불가)"
    else -> "UNKNOWN($accuracy)"
}

private fun rotationLabel(displayRotation: Int): String = when (displayRotation) {
    Surface.ROTATION_90 -> "ROTATION_90"
    Surface.ROTATION_180 -> "ROTATION_180"
    Surface.ROTATION_270 -> "ROTATION_270"
    else -> "ROTATION_0"
}

// ==================== 센서 구독 ====================

/** 한 번의 측정 결과. 원시 값과 필터 값을 함께 들고 있어야 떨림 차이를 눈으로 비교할 수 있다. */
private data class CompassReading(
    val rawDegrees: Float = 0f,
    val filteredDegrees: Float = 0f,
    val accuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE,
    val eventCount: Int = 0,
    val hasFix: Boolean = false
)

/** 첫 이벤트에는 이전 값이 없으므로 원시 값을 그대로 초기 필터 값으로 삼는다. */
private fun CompassReading.next(raw: Float, alpha: Float): CompassReading = copy(
    rawDegrees = raw,
    filteredDegrees = if (hasFix) shortestPathLowPass(filteredDegrees, raw, alpha) else raw,
    eventCount = eventCount + 1,
    hasFix = true
)

/**
 * 화면 회전 값. LocalConfiguration 은 기기를 돌릴 때 새 인스턴스로 내려오므로,
 * 이를 remember 키로 걸어두면 회전 직후 축 매핑이 자동으로 다시 계산된다.
 */
@Composable
private fun rememberDisplayRotation(): Int {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) {
        ContextCompat.getDisplayOrDefault(context).rotation
    }
}

/**
 * TYPE_ROTATION_VECTOR 구독.
 * 리스너 등록/해제를 컴포저블의 진입/이탈과 일치시켜, 화면을 벗어나면 센서가 계속 돌지 않게 한다.
 */
@Composable
private fun rememberCompassReading(alpha: Float, displayRotation: Int): State<CompassReading> {
    val context = LocalContext.current
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    val rotationSensor = remember(sensorManager) {
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }
    val reading: MutableState<CompassReading> = remember { mutableStateOf(CompassReading()) }

    // 필터 계수가 바뀌었다고 리스너를 다시 등록할 이유는 없다. 최신 값만 콜백에서 읽는다.
    val currentAlpha by rememberUpdatedState(alpha)

    DisposableEffect(sensorManager, rotationSensor, displayRotation) {
        val manager = sensorManager
        val targetSensor = rotationSensor
        if (manager == null || targetSensor == null) {
            return@DisposableEffect onDispose { }
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val raw = azimuthDegreesOf(event.values, displayRotation) ?: return
                reading.value = reading.value.next(raw = raw, alpha = currentAlpha)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                reading.value = reading.value.copy(accuracy = accuracy)
            }
        }

        manager.registerListener(listener, targetSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { manager.unregisterListener(listener) }
    }

    return reading
}

// ==================== 화면 ====================

@Composable
fun SensorFusionCompassExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "센서 퓨전 나침반",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FusionPipelineCard() }
            item { LiveCompassCard() }
            item { WrapAroundCard() }
            item { RemapCoordinateCard() }
            item { CompassPitfallCard() }
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun FusionPipelineCard() {
    SensorSectionCard(title = "1. 왜 퓨전 센서 하나만 구독하는가") {
        BodyText(
            "나침반은 \"기기 상단이 북쪽에서 몇 도 돌아가 있는가\"(방위각)만 알면 된다. " +
                "이 값을 얻는 경로는 세 가지가 있는데, 실제로 쓸 만한 것은 하나뿐이다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        SensorTableRow("구성", "센서", "북쪽 기준", isHeader = true)
        SensorTableRow(
            "직접 합성",
            "ACCELEROMETER + MAGNETIC_FIELD",
            "있음 / 흔들림·자기 간섭에 그대로 노출"
        )
        SensorTableRow(
            "ROTATION_VECTOR",
            "퓨전 센서 1개",
            "있음 / 자이로까지 합쳐 플랫폼이 보정"
        )
        SensorTableRow(
            "GAME_ROTATION_VECTOR",
            "자기장 제외",
            "없음 / 자기 간섭은 없지만 나침반 불가"
        )

        Spacer(modifier = Modifier.height(12.dp))
        BodyText("ROTATION_VECTOR 이벤트 하나가 방위각이 되기까지는 4단계를 거친다.")
        Spacer(modifier = Modifier.height(8.dp))

        PipelineStep(
        "1",
        "onSensorChanged(event.values)",
        "회전 벡터 성분 — 정의는 3~4개지만 실기기에서 5개가 오기도 한다(측정: SM-A725F). 뒤쪽 여분은 읽히지 않는다"
    )
        PipelineStep("2", "getRotationMatrixFromVector(R, v)", "3x3 회전 행렬 R — 반환값이 없는 void 메서드다")
        PipelineStep("3", "remapCoordinateSystem(R, X, Y, outR)", "화면 회전 보정 (4번 카드)")
        PipelineStep("4", "getOrientation(outR, o) → o[0]", "방위각(라디안, -π~π) → 도 변환 후 0~360 정규화")
    }
}

// ==================== 2. 실측 데모 ====================

@Composable
private fun LiveCompassCard() {
    val context = LocalContext.current
    val displayRotation = rememberDisplayRotation()
    val rotationSensor = remember(context) {
        (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
            ?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    var alpha by remember { mutableFloatStateOf(0.15f) }
    val readingState = rememberCompassReading(alpha = alpha, displayRotation = displayRotation)

    SensorSectionCard(title = "2. 실측 — 회색(원시) vs 빨강(필터)") {
        if (rotationSensor == null) {
            NoticeBox(
                "이 기기에서는 TYPE_ROTATION_VECTOR 를 찾지 못했다(에뮬레이터에서 흔하다). " +
                    "아래 3번 랩어라운드 데모는 센서 없이도 그대로 동작한다."
            )
            return@SensorSectionCard
        }

        BodyText(
            "다이얼 전체가 방위각만큼 반대로 돌아가므로 빨간 바늘은 항상 실제 북쪽을 가리킨다. " +
                "회색 실선은 필터를 거치지 않은 원시 방위각이라, 기기를 가만히 들고만 있어도 미세하게 떨린다."
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CompassDial(
                readingState = readingState,
                modifier = Modifier.size(220.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        CompassReadout(readingState)

        Spacer(modifier = Modifier.height(12.dp))
        BodyText("필터 계수 α — 작을수록 부드럽지만 반응이 늦다.")
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlphaButton("0.05 (둔함)", 0.05f, alpha) { alpha = it }
            AlphaButton("0.15 (기본)", 0.15f, alpha) { alpha = it }
            AlphaButton("0.50 (민감)", 0.50f, alpha) { alpha = it }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText("센서: ${rotationSensor.name} / ${rotationSensor.vendor}")
    }
}

@Composable
private fun AlphaButton(
    label: String,
    value: Float,
    selected: Float,
    onSelect: (Float) -> Unit
) {
    val isSelected = selected == value
    Button(
        onClick = { onSelect(value) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color(0xFFE0E0E0),
            contentColor = if (isSelected) Color.White else Color(0xFF424242)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontSize = 11.sp)
    }
}

/** 측정값 표시. State 를 그대로 받아 이 컴포저블 안에서만 읽어, 상위 카드가 매 이벤트마다 재구성되지 않게 한다. */
@Composable
private fun CompassReadout(readingState: State<CompassReading>) {
    val reading = readingState.value
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F8E9), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "%.1f° %s".format(reading.filteredDegrees, cardinalOf(reading.filteredDegrees)),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText("원시 %.1f° / 필터 %.1f°".format(reading.rawDegrees, reading.filteredDegrees))
        CaptionText("정확도 ${accuracyLabel(reading.accuracy)}")
        CaptionText("수신 이벤트 ${reading.eventCount}건")
    }
}

@Composable
private fun CompassDial(
    readingState: State<CompassReading>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        // draw 단계에서 값을 읽으므로, 센서 이벤트는 재구성이 아니라 재드로우만 유발한다.
        val reading = readingState.value
        val radius = min(size.width, size.height) / 2f - 12f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = Color(0xFFF5F5F5), radius = radius, center = center)
        drawCircle(
            color = Color(0xFFBDBDBD),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        // 상단 고정 인덱스 — 기기가 향한 방향(진행 방향)을 가리킨다.
        drawIndexMark(center = center, radius = radius)

        // 회색 원시 바늘: 다이얼 회전 밖에서 그려 화면 기준 -원시각 위치에 둔다.
        drawNeedleLine(
            center = center,
            length = radius * 0.82f,
            degrees = -reading.rawDegrees,
            color = Color(0xFF9E9E9E),
            strokeWidth = 2f
        )

        // 다이얼(눈금·방위 문자·빨간 바늘)은 방위각만큼 반대로 회전시킨다.
        rotate(degrees = -reading.filteredDegrees, pivot = center) {
            drawCompassRose(center = center, radius = radius, textMeasurer = textMeasurer)
            drawNeedleLine(
                center = center,
                length = radius * 0.72f,
                degrees = 0f,
                color = Color(0xFFD32F2F),
                strokeWidth = 6f
            )
            drawNeedleLine(
                center = center,
                length = radius * 0.72f,
                degrees = 180f,
                color = Color(0xFF616161),
                strokeWidth = 6f
            )
        }

        drawCircle(color = Color(0xFF424242), radius = 6f, center = center)
    }
}

// ==================== 3. 랩어라운드 함정 ====================

private data class WrapStep(val raw: Float, val naive: Float, val shortest: Float)

// 북쪽(0°)을 통과하도록 만든 결정론적 시퀀스 — 센서 없이도 같은 결과가 재현된다.
private val WRAP_SEQUENCE = listOf(348f, 352f, 356f, 359f, 3f, 7f, 11f, 15f)
private const val WRAP_ALPHA = 0.25f

private fun wrapSeries(alpha: Float): List<WrapStep> {
    var naive = WRAP_SEQUENCE.first()
    var shortest = WRAP_SEQUENCE.first()
    return WRAP_SEQUENCE.map { raw ->
        naive = naiveLowPass(naive, raw, alpha)
        shortest = shortestPathLowPass(shortest, raw, alpha)
        WrapStep(raw = raw, naive = naive, shortest = shortest)
    }
}

@Composable
private fun WrapAroundCard() {
    val series = remember { wrapSeries(WRAP_ALPHA) }
    var playToken by remember { mutableIntStateOf(0) }
    var visibleSteps by remember { mutableIntStateOf(series.size) }

    LaunchedEffect(playToken) {
        if (playToken == 0) return@LaunchedEffect
        visibleSteps = 0
        while (visibleSteps < series.size) {
            delay(350)
            visibleSteps++
        }
    }

    val current = series.getOrNull(visibleSteps - 1)

    SensorSectionCard(title = "3. 359°→3° 에서 바늘이 역주행하는 이유") {
        BodyText(
            "저역통과 필터는 보통 previous + α × (raw − previous) 한 줄이다. " +
                "그런데 방위각은 359° 다음이 0° 인 원형 값이라, 이 뺄셈이 −356 같은 값을 만들면서 " +
                "바늘이 짧은 쪽이 아니라 반대편으로 한 바퀴 돌아간다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MiniDial(
                label = "나이브",
                degrees = current?.naive ?: WRAP_SEQUENCE.first(),
                color = Color(0xFFD32F2F)
            )
            MiniDial(
                label = "최단 경로",
                degrees = current?.shortest ?: WRAP_SEQUENCE.first(),
                color = Color(0xFF2E7D32)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        SensorTableRow("원시", "나이브", "최단 경로", isHeader = true)
        series.take(visibleSteps).forEach { step ->
            SensorTableRow(
                "%.0f°".format(step.raw),
                "%.1f°".format(step.naive),
                "%.1f°".format(step.shortest)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { playToken++ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(text = "한 스텝씩 재생", fontSize = 12.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            "// 차이를 -180~180 으로 정규화하면 항상 짧은 쪽으로 돈다\n" +
                "val delta = ((raw - previous + 540f) % 360f) - 180f\n" +
                "filtered = normalizeDegrees(previous + alpha * delta)\n\n" +
                "// 같은 문제를 sin/cos 성분을 각각 필터링해 푸는 방법도 있다\n" +
                "sinAvg += alpha * (sin(rad) - sinAvg)\n" +
                "cosAvg += alpha * (cos(rad) - cosAvg)\n" +
                "filtered = degrees(atan2(sinAvg, cosAvg))"
        )
        Spacer(modifier = Modifier.height(8.dp))
        CaptionText(
            "α=${WRAP_ALPHA} 로 위 8스텝을 돌리면 나이브 방식은 %.1f° 에서 끝난다 — 실제 방위(15°)와 정반대다."
                .format(series.last().naive)
        )
    }
}

@Composable
private fun MiniDial(label: String, degrees: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val radius = min(size.width, size.height) / 2f - 4f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = Color(0xFFF5F5F5), radius = radius, center = center)
            drawCircle(
                color = Color(0xFFBDBDBD),
                radius = radius,
                center = center,
                style = Stroke(width = 2f)
            )
            drawNeedleLine(
                center = center,
                length = radius * 0.8f,
                degrees = -degrees,
                color = color,
                strokeWidth = 5f
            )
            drawCircle(color = Color(0xFF424242), radius = 4f, center = center)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
        Text(text = "%.1f°".format(degrees), fontSize = 11.sp, color = color)
    }
}

// ==================== 4. 좌표계 보정 ====================

@Composable
private fun RemapCoordinateCard() {
    val displayRotation = rememberDisplayRotation()
    val (worldX, worldY) = worldAxesFor(displayRotation)

    SensorSectionCard(title = "4. 화면을 돌리면 축도 돌려줘야 한다") {
        BodyText(
            "센서 좌표계는 화면이 아니라 기기의 자연 방향에 고정돼 있다. " +
                "가로로 눕히면 사용자가 보는 \"위쪽\"과 센서가 아는 +Y 축이 90° 어긋나므로, " +
                "회전 행렬을 그대로 쓰면 방위각도 90° 어긋난다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "현재 화면 회전: ${rotationLabel(displayRotation)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            CaptionText("적용 중인 축: X=${axisLabel(worldX)}, Y=${axisLabel(worldY)}")
            CaptionText("기기를 가로로 눕히면 이 줄이 바뀐다.")
        }

        Spacer(modifier = Modifier.height(12.dp))
        SensorTableRow("화면 회전", "X 축", "Y 축", isHeader = true)
        SensorTableRow("ROTATION_0", "AXIS_X", "AXIS_Y")
        SensorTableRow("ROTATION_90", "AXIS_Y", "AXIS_MINUS_X")
        SensorTableRow("ROTATION_180", "AXIS_MINUS_X", "AXIS_MINUS_Y")
        SensorTableRow("ROTATION_270", "AXIS_MINUS_Y", "AXIS_X")

        Spacer(modifier = Modifier.height(12.dp))
        CodeBlock(
            "// LocalConfiguration 은 회전 시 새 인스턴스로 내려오므로 remember 키로 쓴다\n" +
                "val rotation = ContextCompat.getDisplayOrDefault(context).rotation\n" +
                "SensorManager.remapCoordinateSystem(rotationMatrix, worldX, worldY, remapped)\n" +
                "SensorManager.getOrientation(remapped, orientation)"
        )
    }
}

private fun axisLabel(axis: Int): String = when (axis) {
    SensorManager.AXIS_X -> "AXIS_X"
    SensorManager.AXIS_Y -> "AXIS_Y"
    SensorManager.AXIS_MINUS_X -> "AXIS_MINUS_X"
    SensorManager.AXIS_MINUS_Y -> "AXIS_MINUS_Y"
    else -> "AXIS($axis)"
}

// ==================== 5. 주의사항 ====================

@Composable
private fun CompassPitfallCard() {
    SensorSectionCard(title = "5. 실기기에서만 드러나는 것들") {
        PitfallRow(
            "리스너 해제",
            "DisposableEffect 의 onDispose 에서 unregisterListener 를 호출하지 않으면 화면을 벗어나도 센서가 계속 돌아 배터리를 먹는다."
        )
        PitfallRow(
            "캘리브레이션",
            "onAccuracyChanged 가 LOW/UNRELIABLE 을 주면 값이 통째로 틀어져 있다. 기기를 8자로 흔들어 자기 센서를 재보정하도록 안내해야 한다."
        )
        PitfallRow(
            "자기 간섭",
            "무선 충전 패드·스피커·차량 대시보드 근처에서는 자기장이 왜곡돼 방위각이 수십 도씩 틀어진다."
        )
        PitfallRow(
            "자북 vs 진북",
            "여기서 얻는 값은 자북 기준이다. 지도의 진북에 맞추려면 GeomagneticField(위도, 경도, 고도, 시각).getDeclination() 만큼 더해야 한다."
        )
        PitfallRow(
            "갱신 주기",
            "SENSOR_DELAY_UI 는 화면 표시에 충분하다. SENSOR_DELAY_FASTEST 는 이벤트가 훨씬 잦아 배터리만 더 쓴다."
        )
        PitfallRow(
            "에뮬레이터",
            "회전 벡터 센서가 없거나 합성값만 주는 경우가 많다. 이 예제의 2번 카드는 실기기에서 확인해야 한다."
        )
    }
}

// ==================== Canvas 헬퍼 ====================

/** 화면 위쪽(-Y)을 0° 로 두고 시계 방향으로 도는 각도 → 좌표 */
private fun pointOnCircle(center: Offset, radius: Float, degrees: Float): Offset {
    val radians = Math.toRadians((degrees - 90f).toDouble())
    return Offset(
        x = center.x + (cos(radians) * radius).toFloat(),
        y = center.y + (sin(radians) * radius).toFloat()
    )
}

private fun DrawScope.drawNeedleLine(
    center: Offset,
    length: Float,
    degrees: Float,
    color: Color,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start = center,
        end = pointOnCircle(center = center, radius = length, degrees = degrees),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawIndexMark(center: Offset, radius: Float) {
    drawLine(
        color = Color(0xFF1976D2),
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y - radius + 16f),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawCompassRose(
    center: Offset,
    radius: Float,
    textMeasurer: TextMeasurer
) {
    for (angle in 0 until 360 step 15) {
        val isMajor = angle % 45 == 0
        val tickLength = if (isMajor) 14f else 7f
        drawLine(
            color = if (isMajor) Color(0xFF616161) else Color(0xFFBDBDBD),
            start = pointOnCircle(center, radius, angle.toFloat()),
            end = pointOnCircle(center, radius - tickLength, angle.toFloat()),
            strokeWidth = if (isMajor) 3f else 1.5f
        )
    }

    val labels = listOf(0f to "N", 90f to "E", 180f to "S", 270f to "W")
    labels.forEach { (angle, text) ->
        val isNorth = text == "N"
        drawCenteredText(
            textMeasurer = textMeasurer,
            text = text,
            center = pointOnCircle(center, radius - 32f, angle),
            color = if (isNorth) Color(0xFFD32F2F) else Color(0xFF424242)
        )
    }
}

private fun DrawScope.drawCenteredText(
    textMeasurer: TextMeasurer,
    text: String,
    center: Offset,
    color: Color
) {
    val style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    val layout = textMeasurer.measure(text = text, style = style)
    drawText(
        textMeasurer = textMeasurer,
        text = text,
        topLeft = Offset(
            x = center.x - layout.size.width / 2f,
            y = center.y - layout.size.height / 2f
        ),
        style = style
    )
}

// ==================== 공통 UI 조각 ====================

@Composable
private fun SensorSectionCard(
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
private fun SensorTableRow(
    first: String,
    second: String,
    third: String,
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
            modifier = Modifier.width(84.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.width(96.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = third,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun PipelineStep(index: String, api: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(0xFF1976D2), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = index, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = api,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF212121)
            )
            CaptionText(description)
        }
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = "• $title",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 17.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun NoticeBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFFE65100),
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFB2EBF2),
            lineHeight = 16.sp
        )
    }
}
