package com.example.composesample.presentation.example.component.ui.shader

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 스크래치 아트 예제의 순수 로직 — 화면(ScratchFoilExampleUI)과 JVM 단위 테스트(ScratchFoilLabTest)가 함께 쓴다.
 * - 센서 값 → 기기 좌표계의 '위쪽' 벡터 → 화면 좌표계 기울기
 * - 기울기 필터: 저역 통과 + 보이지 않는 변화 무시 + 화면이 보일 때 자세를 중립으로
 * - 비교용 고정 입력(탁자 위 잡음 · 천천히 기울이기)과 8자 궤적
 */

/** 기울기 1 = 중립에서 위쪽 벡터가 0.5(약 30°) 움직인 만큼. 셰이더 uniform 은 -1..1 로 받는다. */
const val TILT_GAIN = 2f

/** 저역 통과 계수 — 새 값 쪽으로 15% 씩 다가간다. */
const val TILT_ALPHA = 0.15f

/** 이보다 작은 변화는 내보내지 않는다(기울기 단위, 약 0.15°). 탁자 위 잡음으로 다시 그리지 않으려는 값. */
const val MIN_VISIBLE_CHANGE = 0.005f

/** 8자 궤적 재생 프레임 수(60Hz 기준 2초). */
const val FIGURE_EIGHT_FRAMES = 120

/**
 * TYPE_GAME_ROTATION_VECTOR 값(사원수 x, y, z[, w]) → 기기 좌표계에서 본 세계의 위쪽 방향.
 * SensorManager.getRotationMatrixFromVector 가 만드는 회전 행렬의 셋째 행과 같다.
 * w 가 없는 기기(값 3개)는 단위 사원수 조건으로 계산한다.
 */
fun upVectorFromRotationVector(values: FloatArray): FloatArray {
    val x = values[0]
    val y = values[1]
    val z = values[2]
    val w = if (values.size >= 4) values[3] else sqrt((1f - x * x - y * y - z * z).coerceAtLeast(0f))
    return floatArrayOf(
        2f * (x * z - y * w),
        2f * (y * z + x * w),
        1f - 2f * (x * x + y * y)
    )
}

/** TYPE_ACCELEROMETER 값(m/s²) → 위쪽 방향. 가만히 있으면 중력 반대 방향을 재므로 정규화만 한다. */
fun upVectorFromAccelerometer(values: FloatArray): FloatArray {
    val norm = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    if (norm == 0f) return floatArrayOf(0f, 0f, 1f)
    return floatArrayOf(values[0] / norm, values[1] / norm, values[2] / norm)
}

/**
 * 기기 좌표계(x 오른쪽, y 기기 위쪽, z 화면 밖) → 화면 좌표계(같은 방향 규칙, 화면 기준).
 * rotation 은 Display.getRotation() 값(Surface.ROTATION_0..270 = 0..3). ROTATION_90 = 기기를 반시계로 90° 돌린 것 →
 * 화면 오른쪽 = 기기 -y, 화면 위쪽 = 기기 +x(remapCoordinateSystem(AXIS_Y, AXIS_MINUS_X) 와 같은 대응).
 */
fun toScreenFrame(up: FloatArray, rotation: Int): FloatArray = when (rotation) {
    1 -> floatArrayOf(-up[1], up[0], up[2])
    2 -> floatArrayOf(-up[0], -up[1], up[2])
    3 -> floatArrayOf(up[1], -up[0], up[2])
    else -> floatArrayOf(up[0], up[1], up[2])
}

/**
 * 기울기 필터 — 원문의 두 줄(`smoothed += (target - smoothed) * alpha`, 보이지 않는 변화면 null)에
 * '처음 받은 자세가 중립'을 더했다. offer 가 null 이면 상태를 바꾸지 않는다 → 다시 그리지도 않는다.
 *
 * 기울기는 위쪽 벡터의 x · y 성분이 아니라 **중립 자세에 수직인 두 축**에 투영해 잰다.
 * 세워 든 기기(위쪽 = 화면 y)를 앞뒤로 θ 기울이면 y 성분은 1 - cosθ 만큼만 바뀌어 거의 반응하지 않는다.
 */
class TiltFilter(
    private val alpha: Float = TILT_ALPHA,
    private val minVisibleChange: Float = MIN_VISIBLE_CHANGE
) {
    private var neutral: FloatArray? = null
    private var axisX = floatArrayOf(1f, 0f, 0f)
    private var axisY = floatArrayOf(0f, 1f, 0f)
    private var smoothed = Offset.Zero
    private var lastEmitted = Offset.Zero

    var received = 0
        private set
    var emitted = 0
        private set

    /** 화면이 다시 보일 때 부른다 — 다음 값이 새 중립이 된다. 횟수는 화면 집계용이라 그대로 둔다. */
    fun recalibrate() {
        neutral = null
        smoothed = Offset.Zero
        lastEmitted = Offset.Zero
    }

    /** up = 화면 좌표계의 위쪽 단위 벡터. 보일 만한 변화면 중립 기준 기울기(-1..1, y 는 화면 아래쪽 +), 아니면 null. */
    fun offer(up: FloatArray): Offset? {
        received++
        val base = neutral ?: up.copyOf().also { setNeutral(it) }
        val dx = up[0] - base[0]
        val dy = up[1] - base[1]
        val dz = up[2] - base[2]
        val along = dx * axisX[0] + dy * axisX[1] + dz * axisX[2]
        val across = dx * axisY[0] + dy * axisY[1] + dz * axisY[2]
        val target = (Offset(along, -across) * TILT_GAIN).clampTilt()
        smoothed += (target - smoothed) * alpha
        if ((smoothed - lastEmitted).getDistance() < minVisibleChange) return null
        lastEmitted = smoothed
        emitted++
        return smoothed
    }

    /** 축 = 화면 x 를 중립에 수직으로 투영한 것, 그리고 중립 × 그 축(누운 기기면 화면 y, 세운 기기면 화면 밖 반대). */
    private fun setNeutral(n: FloatArray) {
        neutral = n
        var x = floatArrayOf(1f - n[0] * n[0], -n[0] * n[1], -n[0] * n[2])
        if (length(x) < 1e-3f) x = cross(floatArrayOf(0f, 1f, 0f), n)
        axisX = normalize(x)
        axisY = cross(n, axisX)
    }
}

private fun length(v: FloatArray) = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])

private fun normalize(v: FloatArray): FloatArray {
    val l = length(v)
    return floatArrayOf(v[0] / l, v[1] / l, v[2] / l)
}

private fun cross(a: FloatArray, b: FloatArray) = floatArrayOf(
    a[1] * b[2] - a[2] * b[1],
    a[2] * b[0] - a[0] * b[2],
    a[0] * b[1] - a[1] * b[0]
)

private fun Offset.clampTilt(): Offset = Offset(x.coerceIn(-1f, 1f), y.coerceIn(-1f, 1f))

/** 8자 궤적 — 원문 앱이 GIF 대신 MP4 를 만들 때 빛이 따라가게 한 기울기. t 는 0..1. */
fun figureEightTilt(t: Float): Offset {
    val angle = 2f * PI.toFloat() * t
    return Offset(0.8f * sin(angle), 0.4f * sin(2f * angle))
}

// ==================== 필터 비교 — 고정 입력 ====================

enum class FilterScenario(val label: String) {
    TABLE("탁자 위(잡음만)"),
    SLOW_TILT("천천히 20° 기울인 뒤 멈춤")
}

enum class FilterSetting(val label: String, val alpha: Float, val minVisibleChange: Float) {
    RAW("필터 없음", 1f, 0f),
    LOW_PASS("저역 통과만", TILT_ALPHA, 0f),
    LOW_PASS_AND_THRESHOLD("저역 통과 + 문턱", TILT_ALPHA, MIN_VISIBLE_CHANGE)
}

/** emittedWhileStill = 기기가 움직이지 않는 구간(탁자 전체 · 기울이기의 뒤 절반)에서 내보낸 수. */
data class FilterRun(
    val received: Int,
    val emitted: Int,
    val emittedWhileStill: Int,
    val last: Offset
)

const val FILTER_SAMPLES = 200

/** 가속도계 잡음 폭(정규화 단위) — 탁자 위 기기의 0.03 m/s² 안팎. */
const val SENSOR_NOISE = 0.003f

private const val FILTER_SEED = 167

/** 같은 시드 → JVM 과 기기에서 같은 잡음. 값은 화면 좌표계의 위쪽 벡터(누운 기기를 화면 x 축 방향으로 기울이기). */
fun filterInput(scenario: FilterScenario): List<FloatArray> {
    val random = Random(FILTER_SEED)
    fun noise() = (random.nextFloat() * 2f - 1f) * SENSOR_NOISE
    val ramp = FILTER_SAMPLES / 2
    val maxAngle = 20.0 * PI / 180.0
    return List(FILTER_SAMPLES) { i ->
        val angle = when (scenario) {
            FilterScenario.TABLE -> 0.0
            FilterScenario.SLOW_TILT -> maxAngle * (i.coerceAtMost(ramp).toDouble() / ramp)
        }
        floatArrayOf(sin(angle).toFloat() + noise(), noise(), cos(angle).toFloat())
    }
}

fun runFilterCase(scenario: FilterScenario, setting: FilterSetting): FilterRun {
    val filter = TiltFilter(setting.alpha, setting.minVisibleChange)
    val stillFrom = if (scenario == FilterScenario.TABLE) 0 else FILTER_SAMPLES / 2
    var whileStill = 0
    var last = Offset.Zero
    filterInput(scenario).forEachIndexed { i, raw ->
        val out = filter.offer(raw) ?: return@forEachIndexed
        last = out
        if (i >= stillFrom) whileStill++
    }
    return FilterRun(filter.received, filter.emitted, whileStill, last)
}

// ==================== 그리기 집계 ====================

/**
 * 화면에서 세는 횟수 — 상태가 아니라 일반 필드다(그리기 중에 상태를 쓰면 그 상태를 읽는 곳이 다시 무효화된다).
 * 화면은 0.5초마다 snapshot() 을 상태로 옮겨 보여 준다.
 */
class FoilDrawStats {
    var createShaderCalls = 0
    var shaderCompiles = 0
    var draws = 0
    var recompositions = 0

    fun reset() {
        createShaderCalls = 0
        shaderCompiles = 0
        draws = 0
        recompositions = 0
    }

    fun snapshot() = FoilDrawSnapshot(createShaderCalls, shaderCompiles, draws, recompositions)
}

data class FoilDrawSnapshot(
    val createShaderCalls: Int,
    val shaderCompiles: Int,
    val draws: Int,
    val recompositions: Int
)
