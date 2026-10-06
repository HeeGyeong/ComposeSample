package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.platform.display.FramePacingMeter
import com.example.composesample.presentation.example.component.system.platform.display.FrameLoad
import com.example.composesample.presentation.example.component.system.platform.display.percentileOfSorted
import com.example.composesample.presentation.example.component.system.platform.display.summarizeIntervals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * DisplayRefreshRateExample 의 프레임 간격 집계 검증
 * given : 테스트를 위한 사전 준비를 설정하는 단계
 * when : 실제로 테스트하고자 하는 동작을 실행하는 단계
 * then : 실행 결과를 검증하는 단계
 */
class DisplayRefreshRateExampleTest {

    private val frame60 = 16_666_667L

    @Test
    fun `nearest-rank 백분위는 ceil(p x n) 번째 값이다`() {
        // Given: 1..100
        val sorted = LongArray(100) { (it + 1).toLong() }

        // When & Then
        assertEquals(95L, percentileOfSorted(sorted, 0.95))
        assertEquals(99L, percentileOfSorted(sorted, 0.99))
        assertEquals(100L, percentileOfSorted(sorted, 1.0))
        assertEquals(1L, percentileOfSorted(LongArray(1) { 1L }, 0.99))
    }

    @Test
    fun `50ms 멈춤 두 번은 평균 FPS 를 조금만 떨어뜨리지만 p99 와 최악 간격에 드러난다`() {
        // Given: 60Hz 정상 프레임 118개 + 50ms 멈춤 2개
        val intervals = LongArray(120) { frame60 }
        intervals[30] = 50_000_000L
        intervals[90] = 50_000_000L

        // When
        val report = summarizeIntervals(intervals, intervals.size, 60f)

        // Then
        assertNotNull(report)
        report!!
        assertEquals(2, report.lateCount)
        // 120프레임 ÷ (118 × 16.67ms + 2 × 50ms = 2.067s) = 58.1 — 60 에서 2칸도 안 떨어진다
        assertEquals(58.1f, report.fps, 0.1f)
        assertEquals(16.7f, report.p95Ms, 0.1f)
        assertEquals(50f, report.p99Ms, 0.01f)
        assertEquals(50f, report.worstMs, 0.01f)
    }

    @Test
    fun `늦은 프레임 기준은 주사율을 따른다 - 같은 22ms 가 60Hz 에서는 정상, 90Hz 에서는 늦음`() {
        // Given: 22.2ms 간격(90Hz 에서 vsync 하나를 놓친 간격)
        val intervals = LongArray(10) { 22_222_222L }

        // When
        val at60 = summarizeIntervals(intervals, intervals.size, 60f)!!
        val at90 = summarizeIntervals(intervals, intervals.size, 90f)!!

        // Then: 60Hz 기준 25ms 미만, 90Hz 기준 16.7ms 초과
        assertEquals(0, at60.lateCount)
        assertEquals(10, at90.lateCount)
    }

    @Test
    fun `측정기는 2초 창이 차면 집계를 내고 1초 넘는 간격은 버린다`() {
        // Given
        val meter = FramePacingMeter(windowNanos = 2_000_000_000L)
        var now = 0L
        var report = meter.record(now, 60f)
        assertNull(report)

        // When: 1.5초 동안 프레임 → 2초 공백(백그라운드) → 다시 2초
        repeat(90) {
            now += frame60
            assertNull(meter.record(now, 60f))
        }
        now += 2_000_000_000L
        assertNull(meter.record(now, 60f))
        var frames = 0
        while (report == null) {
            now += frame60
            frames++
            report = meter.record(now, 60f)
        }

        // Then: 공백 앞의 90프레임은 창에 섞이지 않았다
        assertEquals(120, frames)
        assertEquals(120, report.frames)
        assertEquals(60f, report.fps, 0.1f)
        assertEquals(90, meter.recentSize)
    }

    @Test
    fun `스파이크 부하는 60프레임마다 한 번만 메인 스레드를 붙잡는다`() {
        // When
        val busy = (0L until 120L).map { FrameLoad.SPIKE.busyMillis(it) }

        // Then
        assertEquals(2, busy.count { it > 0 })
        assertEquals(50, busy[59])
        assertEquals(13, FrameLoad.STEADY.busyMillis(0))
        assertEquals(0, FrameLoad.NONE.busyMillis(59))
    }
}
