package com.example.composesample.example

import androidx.compose.ui.geometry.Offset
import com.example.composesample.presentation.example.component.ui.canvas.dashCovers
import com.example.composesample.presentation.example.component.ui.canvas.dashSplitMismatch
import com.example.composesample.presentation.example.component.ui.canvas.polarToCart
import com.example.composesample.presentation.example.component.ui.canvas.ribbonPoints
import com.example.composesample.presentation.example.component.ui.canvas.segmentIndexAt
import com.example.composesample.presentation.example.component.ui.canvas.slopeSegments
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * RibbonModifierExample 의 경로 계산 검증 — 600×120 px 내용, 선 굵기 12 px(반지름 72 px) 기준.
 * given : 원을 끌며 만든 나선 점 목록(PathMeasure 대신 점 사이 직선으로 길이·접선을 잰다)
 * when : 접선 y 부호로 구간을 나누고, 원문 두 번 그리기(phase 0 / phase 첫 구간)를 구간과 대조한다
 * then : 기본 시작 각(−90°)은 구간이 모두 반 바퀴로 같아 phase 트릭이 맞고, 시작 각을 옮기면 앞 그리기가 어긋난다
 */
class RibbonPathTest {

    private val width = 600f
    private val height = 120f
    private val radius = height / 2f + 12f

    /** 점 사이 직선 기준의 길이·접선 — 앱은 PathMeasure 로 같은 값을 얻는다 */
    private class PolylineMeasure(private val points: List<Offset>) {
        private val cumulative = FloatArray(points.size).also { acc ->
            for (i in 1 until points.size) acc[i] = acc[i - 1] + (points[i] - points[i - 1]).getDistance()
        }
        val length: Float get() = cumulative.last()

        fun tangentY(distance: Float): Float {
            val i = cumulative.indexOfFirst { it >= distance }.coerceIn(1, points.lastIndex)
            val chord = points[i] - points[i - 1]
            val chordLength = chord.getDistance()
            return if (chordLength == 0f) 0f else chord.y / chordLength
        }
    }

    private fun segmentsFor(turns: Float, startAngle: Float, resolution: Int = 500): Pair<Float, FloatArray> {
        val measure = PolylineMeasure(
            ribbonPoints(
                start = Offset(0f, height / 2f),
                end = Offset(width, height / 2f),
                radius = radius,
                turns = turns,
                startAngle = startAngle
            )
        )
        return measure.length to slopeSegments(measure.length, resolution, measure::tangentY)
    }

    @Test
    fun `극좌표 0도는 오른쪽 90도는 화면 위쪽이다`() {
        val right = polarToCart(degrees = 0f, distance = 10f)
        val up = polarToCart(degrees = 90f, distance = 10f)
        assertEquals(10f, right.x, 1e-4f)
        assertEquals(0f, right.y, 1e-4f)
        assertEquals(0f, up.x, 1e-4f)
        assertEquals(-10f, up.y, 1e-4f) // 화면 y 는 아래로 커지므로 위쪽은 음수
    }

    @Test
    fun `기본값 7바퀴 시작 각 -90도는 반 바퀴 구간 14개이고 길이가 표본 간격 안에서 같다`() {
        val (length, segments) = segmentsFor(turns = 7f, startAngle = -90f)
        val step = length / 500f
        assertEquals(14, segments.size)
        assertEquals(length, segments.sum(), 1e-2f)
        segments.forEach { assertEquals(length / 14f, it, step * 1.01f) }
    }

    @Test
    fun `시작 각 0도면 구간이 15개이고 처음과 끝 4분의 1 바퀴 둘을 합쳐야 반 바퀴다`() {
        val (length, segments) = segmentsFor(turns = 7f, startAngle = 0f)
        val step = length / 500f
        val half = length / 14f
        assertEquals(15, segments.size)
        assertEquals(half, segments[1], step * 1.01f)
        // 반 바퀴끼리는 대칭이라 같지만 4분의 1 바퀴는 원이 진행 방향을 거슬러 도는 쪽(첫 구간)이 짧다
        assertTrue("첫 구간 ${segments.first()} / 반 바퀴 $half", segments.first() < half / 2f)
        assertTrue("끝 구간 ${segments.last()} / 반 바퀴 $half", segments.last() > half / 2f)
        assertEquals(half, segments.first() + segments.last(), step * 2.02f)
    }

    @Test
    fun `반 바퀴 미만이면 구간이 1개뿐이다`() {
        val (_, segments) = segmentsFor(turns = 0.4f, startAngle = -90f)
        assertEquals(1, segments.size)
    }

    @Test
    fun `표본이 적으면 구간 경계가 밀린다`() {
        val (length, coarse) = segmentsFor(turns = 7f, startAngle = -90f, resolution = 40)
        val maxError = coarse.maxOf { kotlin.math.abs(it - length / 14f) }
        // 40 등분이면 표본 간격이 반 바퀴의 35% 남짓 — 경계가 그만큼 밀린 구간이 생긴다
        assertTrue("최대 오차 ${maxError / (length / 14f)}", maxError > length / 14f * 0.2f)
    }

    @Test
    fun `dashCovers 는 짝수 칸 ON 이고 Android 처럼 홀수 개면 마지막 칸을 버린다`() {
        val intervals = floatArrayOf(10f, 20f, 30f) // 30 은 버려져 주기가 30 이 된다
        assertTrue(dashCovers(intervals, 0f, 5f))
        assertFalse(dashCovers(intervals, 0f, 15f))
        assertTrue(dashCovers(intervals, 0f, 35f)) // 35 mod 30 = 5 → 다시 ON
        assertFalse(dashCovers(intervals, 10f, 0f)) // phase 10 → OFF 칸에서 시작
    }

    @Test
    fun `원문 phase 트릭은 바퀴 수와 무관하게 기본 시작 각에선 맞는다`() {
        listOf(3f, 5f, 7f, 9f).forEach { turns ->
            val (_, segments) = segmentsFor(turns = turns, startAngle = -90f)
            val mismatch = dashSplitMismatch(segments)
            assertEquals(0f, mismatch.back, 1e-3f)
            // 남는 것은 표본 간격(길이/500)만큼의 경계 오차뿐
            assertTrue("${turns}바퀴 앞 어긋남 ${mismatch.front}", mismatch.front < 0.02f)
        }
    }

    @Test
    fun `시작 각을 옮기면 첫 구간이 짧아져 앞 그리기가 어긋난다`() {
        val front = listOf(-45f, 0f, 45f).map { dashSplitMismatch(segmentsFor(7f, it).second).front }
        // 첫 구간이 짧을수록(−45° 반 바퀴의 72% → 0° 44% → 45° 22%) phase 로 민 패턴이 실제 구간에서 더 벗어난다
        assertTrue("시작 각별 앞 어긋남 $front", front[0] > 0.2f && front[1] > 0.4f && front[2] > 0.6f)
        assertTrue("어긋남은 첫 구간이 짧을수록 크다 $front", front[0] < front[1] && front[1] < front[2])
    }

    @Test
    fun `구간이 홀수 개면 마지막 칸이 버려져 뒤 그리기의 꼬리도 어긋난다`() {
        val (_, segments) = segmentsFor(turns = 7f, startAngle = 45f)
        assertEquals(15, segments.size)
        // 버려진 마지막 구간(뒤) 자리에서 패턴이 처음부터 되풀이 — ON 은 첫 구간 길이만큼뿐
        assertTrue(dashSplitMismatch(segments).back > 0.02f)
    }

    @Test
    fun `segmentIndexAt 은 누적 길이로 구간 번호를 찾는다`() {
        val segments = floatArrayOf(10f, 20f, 30f)
        assertEquals(0, segmentIndexAt(segments, 0f))
        assertEquals(1, segmentIndexAt(segments, 10f))
        assertEquals(2, segmentIndexAt(segments, 59f))
        assertEquals(2, segmentIndexAt(segments, 60f))
    }
}
