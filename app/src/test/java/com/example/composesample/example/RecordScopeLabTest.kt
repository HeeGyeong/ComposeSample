package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.canvas.EVERY_FRAME_RATIO
import com.example.composesample.presentation.example.component.ui.canvas.GlowLayer
import com.example.composesample.presentation.example.component.ui.canvas.NeighborLoad
import com.example.composesample.presentation.example.component.ui.canvas.RecordTarget
import com.example.composesample.presentation.example.component.ui.canvas.RecordVerdict
import com.example.composesample.presentation.example.component.ui.canvas.RecordWindowResult
import com.example.composesample.presentation.example.component.ui.canvas.classifyRecord
import com.example.composesample.presentation.example.component.ui.canvas.expectedVerdict
import com.example.composesample.presentation.example.component.ui.canvas.medianMillis
import com.example.composesample.presentation.example.component.ui.canvas.savedMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Waveform 4번 카드(#182 다시 기록하는 범위)의 기대 · 판정 · 중앙값 검증.
 * given : 글로우 변형 4(레이어 없음 · graphicsLayer 앞 · 뒤 · clip 앞)와 이웃 둘(레이어 없음 · 자기 레이어)
 * when : 소스로 본 기대와 2초 창의 횟수를 범주로 바꾼다
 * then : 레이어 없는 이웃만, 글로우에 경계가 없을 때(없음 · 뒤) 매 프레임 다시 기록된다
 */
class RecordScopeLabTest {

    private fun result(
        layer: GlowLayer,
        frames: Int = 120,
        glow: Int = 120,
        plain: Int = 0,
        layered: Int = 0,
        median: Double? = 0.2,
        touched: Boolean = false
    ) = RecordWindowResult(layer, NeighborLoad.LIGHT, frames, glow, plain, layered, median, 2_000, touched)

    @Test
    fun `레이어 없는 이웃은 경계가 없을 때(없음 · 뒤)만 매 프레임 다시 기록`() {
        val every = RecordVerdict.EVERY_FRAME
        val reused = RecordVerdict.REUSED
        assertEquals(every, expectedVerdict(GlowLayer.NONE, RecordTarget.PLAIN_NEIGHBOR))
        assertEquals(every, expectedVerdict(GlowLayer.LAYER_AFTER, RecordTarget.PLAIN_NEIGHBOR))
        assertEquals(reused, expectedVerdict(GlowLayer.LAYER_BEFORE, RecordTarget.PLAIN_NEIGHBOR))
        assertEquals(reused, expectedVerdict(GlowLayer.CLIP_BEFORE, RecordTarget.PLAIN_NEIGHBOR))
        // 글로우는 늘 매 프레임, 자기 레이어가 있는 이웃은 늘 재사용
        GlowLayer.entries.forEach {
            assertEquals(every, expectedVerdict(it, RecordTarget.GLOW))
            assertEquals(reused, expectedVerdict(it, RecordTarget.LAYERED_NEIGHBOR))
        }
    }

    @Test
    fun `0 은 재사용 · 기준의 80퍼센트 이상은 매 프레임 · 그 사이는 판정 불가`() {
        assertEquals(RecordVerdict.REUSED, classifyRecord(0, 120))
        assertEquals(RecordVerdict.EVERY_FRAME, classifyRecord(96, 120))
        assertEquals(RecordVerdict.EVERY_FRAME, classifyRecord(121, 120))
        assertNull(classifyRecord(95, 120))
        // 0.5초마다 한 번 깨는 것(2초에 4)은 매 프레임이 아니다
        assertNull(classifyRecord(4, 120))
        // 기준이 0 이면 0 이 아닌 수는 판정하지 않는다
        assertNull(classifyRecord(3, 0))
        assertEquals(0.8f, EVERY_FRAME_RATIO)
    }

    @Test
    fun `창 결과 — 글로우는 그린 프레임, 이웃은 글로우 그리기를 기준으로 판정`() {
        val none = result(GlowLayer.NONE, plain = 120)
        assertTrue(none.matchesExpectation)
        assertEquals(RecordVerdict.EVERY_FRAME, none.verdict(RecordTarget.PLAIN_NEIGHBOR))

        val before = result(GlowLayer.LAYER_BEFORE)
        assertTrue(before.matchesExpectation)

        // graphicsLayer() 앞인데 이웃이 다시 기록되면 기대와 다르다
        assertFalse(result(GlowLayer.LAYER_BEFORE, plain = 118).matchesExpectation)
        // 글로우가 거의 안 그려졌으면(프레임의 절반) 판정 불가
        assertFalse(result(GlowLayer.NONE, glow = 60, plain = 60).matchesExpectation)
        // 건드린 창은 판정하지 않는다
        assertFalse(result(GlowLayer.NONE, plain = 120, touched = true).matchesExpectation)
    }

    @Test
    fun `중앙값은 홀수 · 짝수 표본 모두 나노초에서 밀리초로`() {
        assertNull(medianMillis(emptyList()))
        assertEquals(0.3, medianMillis(listOf(500_000L, 100_000L, 300_000L))!!, 1e-9)
        assertEquals(0.25, medianMillis(listOf(100_000L, 200_000L, 300_000L, 400_000L))!!, 1e-9)
    }

    @Test
    fun `레이어 없음과 graphicsLayer 앞의 중앙값 차이 · 로그 줄`() {
        val rows = listOf(
            result(GlowLayer.NONE, plain = 120, median = 0.45),
            result(GlowLayer.LAYER_BEFORE, median = 0.20)
        )
        assertEquals(0.25, savedMillis(rows, NeighborLoad.LIGHT)!!, 1e-9)
        assertNull(savedMillis(rows, NeighborLoad.HEAVY))
        assertNull(savedMillis(listOf(result(GlowLayer.NONE, median = null), result(GlowLayer.LAYER_BEFORE)), NeighborLoad.LIGHT))
        assertEquals(
            "layer=NONE load=LIGHT frames=120 glow=120 plain=120 layered=0 drawMedianUs=450 window=2000 touched=false ok=true",
            rows[0].probeLine()
        )
    }
}
