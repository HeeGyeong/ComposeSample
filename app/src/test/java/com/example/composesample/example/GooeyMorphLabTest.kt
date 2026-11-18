package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.graphics.GooeyLivePagePreset
import com.example.composesample.presentation.example.component.ui.graphics.GooeyMorph
import com.example.composesample.presentation.example.component.ui.graphics.GooeyMorphConfig
import com.example.composesample.presentation.example.component.ui.graphics.InkAnalysis
import com.example.composesample.presentation.example.component.ui.graphics.MorphClock
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GooeyTextMorph 예제의 계산 검증 — 원문(Juan Silva, MorphText.kt)의 기본값 기준.
 * given : morphSequence 0.5 · blurBase 8 · blurMax 40 · gain 18 · bias −7
 * when : 진행 분할 · 곡선 · 임계 · 프레임 루프 · 픽셀 분석을 계산한다
 * then : 원문이 적은 값(앞 1/4 버팀, t = 0.5 → 8dp, t = 0.25 → 24dp, 상한 t = 1/6, 알파 15 근처 이상 255)과 같다
 */
class GooeyMorphLabTest {

    private val config = GooeyMorphConfig()

    @Test
    fun `morphSequence 0_5 면 앞 1_4 동안 옛 단어가 그대로 버틴다`() {
        assertEquals(0.25f, GooeyMorph.hold(0.5f), 0f)
        assertEquals(1f, GooeyMorph.outgoingT(0.25f, 0.5f), 1e-6f)
        assertEquals(0f, GooeyMorph.incomingT(0.25f, 0.5f), 1e-6f)
        // 끝에서는 뒤집힌다
        assertEquals(0f, GooeyMorph.outgoingT(1f, 0.5f), 1e-6f)
        assertEquals(1f, GooeyMorph.incomingT(1f, 0.5f), 1e-6f)
        // 0.5 를 넘는 sequence 는 0.499 에서 멈춘다(원문 라이브 값 7.24 도 같은 동작)
        assertEquals(0.499f, GooeyMorph.hold(7.24f), 0f)
    }

    @Test
    fun `블러는 t 1 에서 0 · 0_5 에서 base · 0_25 에서 3배 · 1_6 에서 상한`() {
        assertEquals(0f, GooeyMorph.blurDp(1f, 8f, 40f), 1e-5f)
        assertEquals(8f, GooeyMorph.blurDp(0.5f, 8f, 40f), 1e-5f)
        assertEquals(24f, GooeyMorph.blurDp(0.25f, 8f, 40f), 1e-5f)
        assertEquals(1f / 6f, GooeyMorph.capT(8f, 40f), 1e-6f)
        assertEquals(40f, GooeyMorph.blurDp(1f / 6f, 8f, 40f), 1e-3f)
        assertEquals(40f, GooeyMorph.blurDp(0f, 8f, 40f), 0f) // t 하한 0.001 로 0 나눗셈 없음
    }

    @Test
    fun `알파 t^0_4 는 반쯤 사라져도 76퍼센트로 진하다`() {
        assertEquals(1f, GooeyMorph.alpha(1f), 0f)
        assertEquals(0.7579f, GooeyMorph.alpha(0.5f), 1e-4f)
        assertEquals(0f, GooeyMorph.alpha(0f), 0f)
        assertTrue(GooeyMorph.alpha(0.25f) > 0.25f)
    }

    @Test
    fun `임계 행렬은 RGB 를 그대로 두고 알파 행만 gain · bias`() {
        val m = GooeyMorph.thresholdMatrix(18f, -7f)
        assertArrayEquals(
            floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 18f, -7f),
            m,
            0f
        )
    }

    @Test
    fun `0~255 척도면 18 · −7 은 알파 0_39 이하 0 · 14_6 이상 255`() {
        assertEquals(7f / 18f, GooeyMorph.cutoff(18f, -7f), 1e-6f)
        assertEquals(262f / 18f, GooeyMorph.saturation(18f, -7f), 1e-4f)
        assertEquals(173f, GooeyMorph.thresholdAlpha(10f, 18f, -7f), 1e-4f)
        assertEquals(255f, GooeyMorph.thresholdAlpha(20f, 18f, -7f), 0f)
        // 같은 값을 −1~1 척도로 읽으면 전부 0
        assertEquals(0f, GooeyMorph.thresholdAlphaNormalizedOffset(20f, 18f, -7f), 0f)
        // 라이브 페이지 값
        assertEquals(9.8077f, GooeyMorph.cutoff(GooeyLivePagePreset.thresholdGain, GooeyLivePagePreset.thresholdBias), 1e-3f)
        assertEquals(75.192f, GooeyMorph.saturation(GooeyLivePagePreset.thresholdGain, GooeyLivePagePreset.thresholdBias), 1e-2f)
    }

    @Test
    fun `견본 두 척도의 예상은 서로 갈린다`() {
        // 입력 128 · gain 1
        assertEquals(64f, GooeyMorph.thresholdAlpha(128f, 1f, -64f), 0f)
        assertEquals(0f, GooeyMorph.thresholdAlphaNormalizedOffset(128f, 1f, -64f), 0f)
        assertEquals(127.75f, GooeyMorph.thresholdAlpha(128f, 1f, -0.25f), 1e-4f)
        assertEquals(64.25f, GooeyMorph.thresholdAlphaNormalizedOffset(128f, 1f, -0.25f), 1e-4f)
    }

    @Test
    fun `프레임 루프는 1초 모핑 · 0_25초 쉼 뒤 다음 단어`() {
        val clock = MorphClock(config)
        assertFalse(clock.advance(0.5f, 4))
        assertEquals(0.5f, clock.fraction, 0f)
        assertFalse(clock.advance(0.5f, 4))
        assertEquals(1f, clock.fraction, 0f)
        assertTrue(clock.cooling)
        // 쉼 0.25초 — 0.2초까지는 그대로, 0.3초째 다음 단어
        assertFalse(clock.advance(0.1f, 4))
        assertFalse(clock.advance(0.1f, 4))
        assertTrue(clock.advance(0.1f, 4))
        assertEquals(1, clock.index)
        assertEquals(0f, clock.fraction, 0f)
        assertFalse(clock.cooling)
    }

    @Test
    fun `네 단어를 돌고 처음으로 돌아온다 · 단어가 하나면 움직이지 않는다`() {
        val clock = MorphClock(config)
        var switches = 0
        repeat(4 * 80) { if (clock.advance(1f / 60f, 4)) switches++ }
        assertEquals(4, switches)
        assertEquals(0, clock.index)

        val single = MorphClock(config)
        assertFalse(single.advance(1f, 1))
        assertEquals(0f, single.fraction, 0f)
    }

    @Test
    fun `멈춰서 끌면 쉼을 버리고 그 진행으로`() {
        val clock = MorphClock(config)
        repeat(61) { clock.advance(1f / 60f, 4) }
        assertTrue(clock.cooling)
        clock.seek(0.4f)
        assertFalse(clock.cooling)
        assertEquals(0.4f, clock.fraction, 0f)
        clock.seek(3f)
        assertEquals(1f, clock.fraction, 0f)
    }

    @Test
    fun `덮임은 흰 바탕과 잉크 사이 위치 — 가장 차이 큰 채널로`() {
        val ink = 0xFF1A237E.toInt() // R 0x1A 가 가장 어둡다
        assertEquals(0f, InkAnalysis.coverage(0xFFFFFFFF.toInt(), ink), 0f)
        assertEquals(1f, InkAnalysis.coverage(ink, ink), 0f)
        // 반쯤 섞은 R: 255 − (255 − 26) × 0.5 = 140.5
        val half = (0xFF shl 24) or (140 shl 16) or (0x91 shl 8) or 0xBE
        assertEquals(0.5f, InkAnalysis.coverage(half, ink), 0.01f)
    }

    @Test
    fun `덩어리는 8방향 연결 · 4px 미만은 뺀다`() {
        // 7×3 — 왼쪽 2×2, 오른쪽 2×2 대각선으로 이어진 덩어리, 외딴 1px
        val w = 7
        val h = 3
        val grid = arrayOf(
            "XX.....",
            "XX.XX.X",
            "..XX..."
        )
        val ink = BooleanArray(w * h) { grid[it / w][it % w] == 'X' }
        // (1,1) 과 (2,2) 가 대각선으로 붙어 왼쪽 · 가운데가 한 덩어리, (6,1) 은 1px 라 제외
        assertEquals(1, InkAnalysis.countBlobs(ink, w, h))
        assertEquals(2, InkAnalysis.countBlobs(ink, w, h, minPixels = 1))
    }

    @Test
    fun `분석은 테두리에 닿은 잉크와 옅은 안개를 따로 센다`() {
        val white = 0xFFFFFFFF.toInt()
        val black = 0xFF000000.toInt()
        val faint = 0xFFF0F0F0.toInt() // 덮임 15/255 ≈ 0.06
        val w = 4
        val h = 4
        val pixels = IntArray(w * h) { white }
        pixels[0] = black // 테두리
        pixels[5] = black
        pixels[6] = black
        pixels[9] = black
        pixels[10] = faint
        val stats = InkAnalysis.analyze(pixels, w, h, black)
        assertEquals(4, stats.inkPixels)
        assertEquals(1, stats.blobs)
        assertEquals(1, stats.borderInk)
        assertEquals(1, stats.hazePixels)
        assertEquals(1, stats.centerColumnInk) // 가운데 세로줄 x = 2 에는 (2,1) 하나
    }

    @Test
    fun `흰 바탕 검정 견본의 알파 읽기`() {
        // 검정 α 128 을 흰 바탕에 덮으면 127
        val gray = (0xFF shl 24) or (127 shl 16) or (127 shl 8) or 127
        assertEquals(128f, InkAnalysis.blackAlphaOnWhite(intArrayOf(gray, gray)), 0f)
    }
}
