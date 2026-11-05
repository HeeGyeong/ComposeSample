package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToLong
import kotlin.math.sin

/**
 * Freehand Drawing 예제의 입력 기록 로직 — 화면(FreehandDrawingExampleUI)과 JVM 단위 테스트(FreehandInputLabTest)가 함께 쓴다.
 * - 포인터 이벤트 하나에 묶여 온 중간 샘플(historical) + 마지막 위치(position)를 순서대로 획에 쌓는다
 * - 같은 획을 '위치만' 쌓았을 때의 점도 함께 남겨 두 선을 겹쳐 비교한다
 * - 재생용 곡선: 고정 샘플레이트로 만든 원 1바퀴를 화면 프레임 단위로 묶는다(시스템의 MotionEvent 묶음과 같은 모양)
 */

/** 재생 곡선 — 원 1바퀴를 이 시간 안에 그린다(빠른 'o' 한 획 정도). */
const val REPLAY_DURATION_MS = 150

/** 재생 곡선의 터치 샘플레이트(Hz) — 화면 주사율보다 빠른 터치 센서를 흉내 낸다. */
const val REPLAY_SAMPLE_HZ = 240

/** 재생 곡선을 묶는 화면 프레임레이트(Hz). */
const val REPLAY_FRAME_HZ = 60

/**
 * 한 획의 입력 기록.
 * @param points 손가락이 닿은 점 + 모든 샘플(historical 포함) — 실제로 그리는 선
 * @param framePoints 손가락이 닿은 점 + 이벤트마다 마지막 위치만 — `change.position` 만 쌓았을 때의 선
 * @param events 받은 이동 이벤트 수(대략 프레임 수)
 * @param historicalTotal 이벤트들에 묶여 온 중간 샘플 합
 * @param maxHistoricalPerEvent 한 이벤트에 묶여 온 중간 샘플의 최댓값
 */
data class StrokeInput(
    val points: List<Offset>,
    val framePoints: List<Offset>,
    val events: Int = 0,
    val historicalTotal: Int = 0,
    val maxHistoricalPerEvent: Int = 0,
)

/** 손가락이 닿은 위치에서 획을 시작한다(터치 슬롭을 기다리지 않는다). */
fun startStroke(down: Offset) = StrokeInput(points = listOf(down), framePoints = listOf(down))

/**
 * 이동 이벤트 하나를 쌓는다.
 * historical 은 오래된 순서이고 모두 position 보다 앞선 시각이다 → historical 다음에 position.
 */
fun StrokeInput.append(historical: List<Offset>, position: Offset) = copy(
    points = points + historical + position,
    framePoints = framePoints + position,
    events = events + 1,
    historicalTotal = historicalTotal + historical.size,
    maxHistoricalPerEvent = max(maxHistoricalPerEvent, historical.size)
)

/** 재생 곡선의 샘플 하나 — 획 시작부터의 시간(ms)과 캔버스 좌표. */
data class TimedSample(val timeMillis: Long, val position: Offset)

/**
 * 원 1바퀴(오른쪽 끝에서 시작해 시계 방향)를 [sampleHz] 로 샘플링하고 [frameHz] 프레임 단위로 묶는다.
 * - 반환값[0] = 손가락이 닿는 샘플 1개(t = 0)
 * - 반환값[1..] = 프레임마다 묶인 샘플들 — 마지막 것이 그 이벤트의 position, 앞의 것들이 historical
 * 프레임 경계에 걸친 샘플은 그 경계가 속한 프레임에 넣는다(시각 (이전 경계, 이번 경계]).
 */
fun replayFrames(
    center: Offset,
    radius: Float,
    durationMs: Int = REPLAY_DURATION_MS,
    sampleHz: Int = REPLAY_SAMPLE_HZ,
    frameHz: Int = REPLAY_FRAME_HZ,
): List<List<TimedSample>> {
    val sampleCount = durationMs * sampleHz / 1000
    val samples = (0..sampleCount).map { k ->
        val fraction = k.toDouble() / sampleCount
        val angle = 2 * PI * fraction
        TimedSample(
            // 샘플 k 의 시각 = k / sampleHz 초(정수 ms 로 반올림)
            timeMillis = (k * 1000.0 / sampleHz).roundToLong(),
            position = Offset(
                (center.x + radius * cos(angle)).toFloat(),
                (center.y + radius * sin(angle)).toFloat()
            )
        )
    }
    val frameCount = ceil(durationMs * frameHz / 1000.0).toInt()
    val frames = mutableListOf(listOf(samples.first()))
    var next = 1
    for (j in 1..frameCount) {
        // 프레임 경계 j 의 시각(정수 연산 — 경계에 딱 걸친 샘플이 부동소수 오차로 다음 프레임에 밀리지 않게)
        val batch = mutableListOf<TimedSample>()
        while (next <= sampleCount && next.toLong() * frameHz <= j.toLong() * sampleHz) {
            batch += samples[next]
            next++
        }
        if (batch.isNotEmpty()) frames += batch
    }
    return frames
}

/**
 * 선(점을 직선으로 이은 것)이 원에서 가장 멀리 벗어난 거리(px).
 * 각 선분을 [subdivisions] 등분한 점들에서 |중심까지 거리 − 반지름| 의 최댓값을 잰다 — 짝수 등분이면 선분 가운데가 들어간다.
 */
fun maxCircleDeviation(points: List<Offset>, center: Offset, radius: Float, subdivisions: Int = 16): Float {
    if (points.isEmpty()) return 0f
    var worst = abs((points.first() - center).getDistance() - radius)
    for (i in 1 until points.size) {
        val a = points[i - 1]
        val b = points[i]
        for (s in 1..subdivisions) {
            val p = a + (b - a) * (s.toFloat() / subdivisions)
            worst = max(worst, abs((p - center).getDistance() - radius))
        }
    }
    return worst
}
