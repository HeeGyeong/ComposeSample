package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.platform.display.FrameStage
import com.example.composesample.presentation.example.component.system.platform.display.FrameStageMeter
import com.example.composesample.presentation.example.component.system.platform.display.dominantStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DisplayRefreshRateExample 5번 카드(FrameMetrics 단계별 프레임 시간)의 집계 검증
 * given : 단계별 시간(ns)을 FrameStage 순서로 만든 가짜 프레임
 * when : FrameStageMeter 에 2초 창을 채운다
 * then : 평균·최대·최악 프레임·기준 초과 프레임과 그 프레임을 붙잡은 단계를 센다
 */
class FrameStageMeterTest {

    private val ms = 1_000_000L
    private val budget60 = 16_666_667L

    private fun stages(vararg pairs: Pair<FrameStage, Long>): LongArray =
        LongArray(FrameStage.entries.size).also { arr -> pairs.forEach { (stage, nanos) -> arr[stage.ordinal] = nanos } }

    /** 프레임 n 개를 16.7ms 간격으로 넣고, 창이 닫히며 나온 첫 집계를 돌려준다 */
    private fun FrameStageMeter.feed(
        n: Int,
        deadline: Long = 0L,
        frame: (index: Int) -> Pair<LongArray, Long>
    ) = (0 until n).firstNotNullOfOrNull { i ->
        val (stage, total) = frame(i)
        record(stage, total, deadline, budget60, firstDrawFrame = false, droppedSinceLast = 0, nowNanos = i * budget60)
    }

    @Test
    fun `가장 오래 걸린 단계를 고르고 같으면 앞 단계다`() {
        assertEquals(FrameStage.DRAW.ordinal, dominantStage(stages(FrameStage.ANIMATION to 3 * ms, FrameStage.DRAW to 13 * ms)))
        assertEquals(FrameStage.ANIMATION.ordinal, dominantStage(stages(FrameStage.ANIMATION to 5 * ms, FrameStage.DRAW to 5 * ms)))
    }

    @Test
    fun `2초가 차기 전에는 집계가 없다`() {
        val meter = FrameStageMeter()
        val report = meter.feed(100) { stages(FrameStage.DRAW to 2 * ms) to 4 * ms }
        assertNull(report) // 100 프레임 × 16.7ms = 1.67초
    }

    @Test
    fun `60프레임마다 50ms 멈춤은 평균은 낮고 최대·최악 프레임이 애니메이션 단계로 잡힌다`() {
        // Given: 평소 애니메이션 1ms · 그리기 2ms, 60번째마다 애니메이션 50ms(4번 카드의 SPIKE 와 같은 모양)
        val meter = FrameStageMeter()

        // When
        val report = meter.feed(130) { i ->
            val anim = if (i % 60 == 59) 50 * ms else 1 * ms
            stages(FrameStage.ANIMATION to anim, FrameStage.DRAW to 2 * ms) to (anim + 4 * ms)
        }
        assertNotNull(report)
        report!!

        // Then: 창 121 프레임 중 2개가 기준(1/60)을 넘고, 둘 다 애니메이션이 붙잡았다
        assertEquals(121, report.frames)
        assertEquals(2, report.overLimit)
        assertEquals(2, report.overByStage[FrameStage.ANIMATION.ordinal])
        assertEquals(50f, report.maxMs[FrameStage.ANIMATION.ordinal], 0.01f)
        assertEquals(50f, report.worstFrameMs[FrameStage.ANIMATION.ordinal], 0.01f)
        assertEquals(54f, report.totalWorstMs, 0.01f)
        assertTrue("평균 애니메이션 ${report.averageMs[FrameStage.ANIMATION.ordinal]}", report.averageMs[FrameStage.ANIMATION.ordinal] < 2f)
        assertFalse(report.usesDeadline)
        assertEquals(16.67f, report.limitMs, 0.01f)
    }

    @Test
    fun `DEADLINE 이 오면 1 나누기 주사율 대신 그 값으로 판정한다`() {
        // Given: TOTAL 20ms — 1/60(16.7ms)은 넘지만 DEADLINE 25ms 는 넘지 않는다
        val meter = FrameStageMeter()
        val report = meter.feed(130, deadline = 25 * ms) {
            stages(FrameStage.DRAW to 13 * ms, FrameStage.COMMAND to 4 * ms) to 20 * ms
        }!!

        assertTrue(report.usesDeadline)
        assertEquals(25f, report.limitMs, 0.01f)
        assertEquals(0, report.overLimit)
        assertEquals(13f, report.averageMs[FrameStage.DRAW.ordinal], 0.01f)
    }

    @Test
    fun `첫 그리기 프레임은 판정에서 빼고 개수만 센다`() {
        val meter = FrameStageMeter()
        val first = meter.record(
            stages(FrameStage.LAYOUT to 80 * ms), 120 * ms, 0L, budget60,
            firstDrawFrame = true, droppedSinceLast = 3, nowNanos = 0L
        )
        assertNull(first)
        val report = meter.feed(130) { stages(FrameStage.DRAW to 2 * ms) to 4 * ms }!!
        assertEquals(1, report.firstDrawFrames)
        assertEquals(3, report.droppedReports)
        assertEquals(0, report.overLimit)
        assertEquals(0f, report.maxMs[FrameStage.LAYOUT.ordinal], 0.01f)
    }

    @Test
    fun `reset 은 창을 비우고 다음 프레임부터 새로 잰다`() {
        val meter = FrameStageMeter()
        meter.feed(100) { stages(FrameStage.ANIMATION to 30 * ms) to 34 * ms }
        meter.reset()
        val report = meter.feed(130) { stages(FrameStage.DRAW to 2 * ms) to 4 * ms }!!
        assertEquals(0, report.overLimit)
        assertEquals(0f, report.maxMs[FrameStage.ANIMATION.ordinal], 0.01f)
    }
}
