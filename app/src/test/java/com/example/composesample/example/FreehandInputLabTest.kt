package com.example.composesample.example

import androidx.compose.ui.geometry.Offset
import com.example.composesample.presentation.example.component.ui.canvas.PredictionSample
import com.example.composesample.presentation.example.component.ui.canvas.append
import com.example.composesample.presentation.example.component.ui.canvas.circlePositionAt
import com.example.composesample.presentation.example.component.ui.canvas.maxCircleDeviation
import com.example.composesample.presentation.example.component.ui.canvas.predictionReport
import com.example.composesample.presentation.example.component.ui.canvas.replayFrames
import com.example.composesample.presentation.example.component.ui.canvas.startStroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Freehand Drawing 입력 기록 — 화면과 같은 함수를 JVM 에서 돌려 판정을 고정한다.
 * 기대값은 구현이 아니라 기하(정 n각형과 외접원의 거리 r(1 − cos(π/n)))와 시간 비율에서 계산한다.
 * 실제 Compose 가 historical 을 넘겨주는지는 기기에서만 잰다(MotionEvent 가 필요하다).
 */
class FreehandInputLabTest {

    private val center = Offset(400f, 300f)
    private val radius = 200f

    /** 원에 내접하는 정 n각형의 꼭짓점(닫힌 선 — 첫 점을 끝에 한 번 더). */
    private fun regularPolygon(n: Int) = (0..n).map { k ->
        val angle = 2 * PI * k / n
        Offset((center.x + radius * cos(angle)).toFloat(), (center.y + radius * sin(angle)).toFloat())
    }

    private fun sagitta(n: Int) = (radius * (1 - cos(PI / n))).toFloat()

    @Test
    fun `획은 손가락이 닿은 점 하나로 시작한다`() {
        val stroke = startStroke(Offset(10f, 20f))
        assertEquals(listOf(Offset(10f, 20f)), stroke.points)
        assertEquals(listOf(Offset(10f, 20f)), stroke.framePoints)
        assertEquals(0, stroke.events)
    }

    @Test
    fun `historical 은 position 앞에 순서대로 쌓이고 비교선에는 position 만 쌓인다`() {
        val a = Offset(1f, 1f)
        val b = Offset(2f, 2f)
        val c = Offset(3f, 3f)
        val d = Offset(4f, 4f)
        val stroke = startStroke(Offset.Zero)
            .append(historical = listOf(a, b), position = c)
            .append(historical = emptyList(), position = d)

        assertEquals(listOf(Offset.Zero, a, b, c, d), stroke.points)
        assertEquals(listOf(Offset.Zero, c, d), stroke.framePoints)
        assertEquals(2, stroke.events)
        assertEquals(2, stroke.historicalTotal)
        assertEquals(2, stroke.maxHistoricalPerEvent)
    }

    @Test
    fun `240Hz 150ms 원을 60Hz 로 묶으면 닿는 샘플 1개 뒤에 4개씩 9프레임`() {
        val frames = replayFrames(center, radius, durationMs = 150, sampleHz = 240, frameHz = 60)

        // 150ms × 240Hz = 36 구간 → 샘플 37개, 150ms × 60Hz = 9 프레임
        assertEquals(1 + 9, frames.size)
        assertEquals(1, frames.first().size)
        frames.drop(1).forEach { assertEquals(4, it.size) }
        assertEquals(37, frames.sumOf { it.size })
        // 프레임 안의 시각은 이전 경계 뒤 ~ 이번 경계까지: 프레임 1 = 4·8·13·17ms (1/240초 간격 반올림)
        assertEquals(listOf(4L, 8L, 13L, 17L), frames[1].map { it.timeMillis })
        // 한 바퀴 — 시작점과 끝점이 같다
        assertEquals(0f, (frames.first().first().position - frames.last().last().position).getDistance(), 1e-3f)
    }

    @Test
    fun `주사율이 샘플레이트로 나누어떨어지지 않아도 샘플은 빠짐없이 한 번씩 들어간다`() {
        val frames = replayFrames(center, radius, durationMs = 150, sampleHz = 240, frameHz = 90)

        // 150ms × 90Hz = 13.5 → 14 프레임, 프레임당 2~3개(240/90 = 2.67)
        assertEquals(1 + 14, frames.size)
        assertEquals(37, frames.sumOf { it.size })
        assertTrue(frames.drop(1).all { it.size in 2..3 })
        val times = frames.flatten().map { it.timeMillis }
        assertEquals(times.sorted(), times)
        assertEquals(times.distinct(), times)
    }

    @Test
    fun `정 n각형이 원에서 벗어나는 최대 거리는 r(1 - cos(π÷n))`() {
        listOf(4, 9, 36).forEach { n ->
            assertEquals("n=$n", sagitta(n), maxCircleDeviation(regularPolygon(n), center, radius), 0.01f)
        }
    }

    @Test
    fun `재생 원을 position 만 쌓으면 9각형, historical 까지 쌓으면 36각형`() {
        val frames = replayFrames(center, radius, durationMs = 150, sampleHz = 240, frameHz = 60)
        var stroke = startStroke(frames.first().first().position)
        frames.drop(1).forEach { batch ->
            stroke = stroke.append(historical = batch.dropLast(1).map { it.position }, position = batch.last().position)
        }

        assertEquals(10, stroke.framePoints.size)
        assertEquals(37, stroke.points.size)
        assertEquals(27, stroke.historicalTotal)
        assertEquals(3, stroke.maxHistoricalPerEvent)
        // 반지름 200px: 9각형 12.1px vs 36각형 0.76px
        assertEquals(sagitta(9), maxCircleDeviation(stroke.framePoints, center, radius), 0.01f)
        assertEquals(sagitta(36), maxCircleDeviation(stroke.points, center, radius), 0.01f)
    }

    @Test
    fun `점 하나(탭)는 원 위에 있으면 벗어난 거리 0`() {
        val onCircle = Offset(center.x + radius, center.y)
        assertEquals(0f, maxCircleDeviation(listOf(onCircle), center, radius), 1e-3f)
        assertEquals(0f, maxCircleDeviation(emptyList(), center, radius), 0f)
    }

    // ---- 다음 위치 예측(#171 ②) ----

    @Test
    fun `원 위치 — 0ms 는 오른쪽 끝, 1_4 지나면 아래(화면 좌표 시계 방향), 끝을 넘으면 이어 간다`() {
        val c = Offset(100f, 100f)
        val start = circlePositionAt(c, 50f, 0.0)
        assertEquals(150f, start.x, 1e-4f)
        assertEquals(100f, start.y, 1e-4f)
        val quarter = circlePositionAt(c, 50f, 37.5)
        assertEquals(100f, quarter.x, 1e-3f)
        assertEquals(150f, quarter.y, 1e-3f)
        val beyond = circlePositionAt(c, 50f, 150.0 + 37.5)
        assertEquals(quarter.x, beyond.x, 1e-3f)
        assertEquals(quarter.y, beyond.y, 1e-3f)
    }

    @Test
    fun `완벽한 예측은 오차 0, 예측 없음은 그만큼 뒤처진다`() {
        val c = Offset(0f, 0f)
        val r = 100f
        val current = circlePositionAt(c, r, 50.0)
        val target = circlePositionAt(c, r, 70.0)
        val report = predictionReport(
            listOf(PredictionSample(50, current, target, 70)),
            c, r
        )
        assertEquals(1, report.predictions)
        assertEquals(20.0, report.meanLookaheadMs, 1e-9)
        assertEquals(0f, report.meanPredictedError, 1e-3f)
        // 20ms = 원의 2/15 바퀴 → 현을 잇는 거리 2r·sin(π·20/150)
        val chord = (2 * r * sin(PI * 20 / 150)).toFloat()
        assertEquals(chord, report.meanStaleError, 1e-2f)
    }

    @Test
    fun `예측 없는 이벤트는 세되 점수에서 빼고, 손 뗀 뒤를 겨냥한 예측도 뺀다`() {
        val c = Offset(0f, 0f)
        val samples = listOf(
            PredictionSample(0, Offset(100f, 0f), null, null),
            PredictionSample(140, Offset(0f, 0f), Offset(0f, 0f), 170), // 150ms 넘음
        )
        val report = predictionReport(samples, c, 100f)
        assertEquals(2, report.events)
        assertEquals(1, report.predictions)
        assertEquals(0.0, report.meanLookaheadMs, 0.0)
        assertEquals(0f, report.meanPredictedError, 0f)
    }
}
