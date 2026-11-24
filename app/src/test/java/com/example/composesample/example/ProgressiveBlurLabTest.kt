package com.example.composesample.example

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.example.component.ui.graphics.BlurPixelMath
import com.example.composesample.presentation.example.component.ui.graphics.EdgeChoice
import com.example.composesample.presentation.example.component.ui.graphics.EdgeResult
import com.example.composesample.presentation.example.component.ui.graphics.EdgeStats
import com.example.composesample.presentation.example.component.ui.graphics.GLASS_FADE_DP
import com.example.composesample.presentation.example.component.ui.graphics.GLASS_HEADER_DP
import com.example.composesample.presentation.example.component.ui.graphics.GLASS_MAX_RADIUS_DP
import com.example.composesample.presentation.example.component.ui.graphics.GalleryResult
import com.example.composesample.presentation.example.component.ui.graphics.GlassRun
import com.example.composesample.presentation.example.component.ui.graphics.GlassVariant
import com.example.composesample.presentation.example.component.ui.graphics.ReturnRun
import com.example.composesample.presentation.example.component.ui.graphics.ReturnVariant
import com.example.composesample.presentation.example.component.ui.graphics.SpecKind
import com.example.composesample.presentation.example.component.ui.graphics.SpecSupport
import com.example.composesample.presentation.example.component.ui.graphics.fixed2
import com.example.composesample.presentation.example.component.ui.graphics.galleryRadiusChoices
import com.example.composesample.presentation.example.component.ui.graphics.glassProgress
import com.example.composesample.presentation.example.component.ui.graphics.glassStops
import com.example.composesample.presentation.example.component.ui.graphics.specFor
import com.example.composesample.presentation.example.component.ui.graphics.specSupport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Modifier.blur { } 예제(#149)의 API 경계 · 스크롤 진행 · stop · 픽셀 계산 · 판정.
 * 실제로 흐려지는지 · 리컴포지션 · 공유 요소 전환은 계측 테스트(ProgressiveBlurTest)가 본다.
 */
class ProgressiveBlurLabTest {

    @Test
    fun API_경계_균일은_31_나머지는_33() {
        for (k in SpecKind.entries) assertEquals(SpecSupport.IgnoredBelow31, specSupport(k, 30))
        assertEquals(SpecSupport.Applied, specSupport(SpecKind.Uniform, 31))
        for (k in SpecKind.entries - SpecKind.Uniform) {
            assertEquals(SpecSupport.IdentityBelow33, specSupport(k, 32))
            assertEquals(SpecSupport.Applied, specSupport(k, 33))
        }
    }

    @Test
    fun 갤러리_판정은_예상과_측정이_같은가() {
        assertTrue(GalleryResult(SpecKind.Vertical, SpecSupport.Applied, 0.32).matchesExpectation)
        assertFalse("적용돼야 하는데 거의 그대로면 다름", GalleryResult(SpecKind.Vertical, SpecSupport.Applied, 0.95).matchesExpectation)
        assertTrue(GalleryResult(SpecKind.Vertical, SpecSupport.IgnoredBelow31, 0.99).matchesExpectation)
        assertFalse("무시돼야 하는데 흐려졌으면 다름", GalleryResult(SpecKind.Radial, SpecSupport.IdentityBelow33, 0.3).matchesExpectation)
    }

    @Test
    fun 모든_스펙은_화면의_반경_선택지에서_만들어진다() {
        // stop 개수(2~16) · 음수 반경 검사를 화면 선택지가 어기지 않는지 — 만들기만 하고 효과는 만들지 않는다
        for (k in SpecKind.entries) for (r in galleryRadiusChoices) specFor(k, r, DpSize(300.dp, 150.dp))
    }

    @Test
    fun 스크롤_진행은_헤더_높이에서_1() {
        assertEquals(0f, glassProgress(0, 0, 144f))
        assertEquals(0.5f, glassProgress(0, 72, 144f))
        assertEquals(1f, glassProgress(0, 500, 144f))
        assertEquals("첫 칸이 지나가면 1", 1f, glassProgress(1, 0, 144f))
        assertEquals(1f, glassProgress(0, 10, 0f))
    }

    @Test
    fun 헤더_띠는_반경_r_로_흐리고_fade_아래는_0() {
        val stops = glassStops(1f, 320f)
        assertEquals(4, stops.size)
        assertEquals(listOf(0f, GLASS_HEADER_DP / 320f, (GLASS_HEADER_DP + GLASS_FADE_DP) / 320f, 1f), stops.map { it.fraction })
        assertEquals(listOf(GLASS_MAX_RADIUS_DP.dp, GLASS_MAX_RADIUS_DP.dp, 0.dp, 0.dp), stops.map { it.radius })
        // 진행 0 이면 반경 0(맨 위에서는 흐리지 않는다)
        assertTrue(glassStops(0f, 320f).all { it.radius == 0.dp })
        // 레이어가 낮으면 fade 끝이 1 로 잘린다
        val short = glassStops(1f, 100f)
        assertEquals(0.72f, short[1].fraction, 0.0001f)
        assertEquals(1f, short[2].fraction)
        // 높이 0(아직 측정 전)에는 두 stop
        assertEquals(2, glassStops(1f, 0f).size)
    }

    @Test
    fun 선명도는_평평하면_0_줄무늬는_크고_번지면_줄어든다() {
        val w = 40
        val h = 10
        val flat = IntArray(w * h) { 0xFF808080.toInt() }
        assertEquals(0.0, BlurPixelMath.sharpness(flat, w, h), 0.0)
        val stripes = IntArray(w * h) { i -> if ((i % w) % 2 == 0) 0xFF000000.toInt() else 0xFFFFFFFF.toInt() }
        val soft = IntArray(w * h) { i -> if ((i % w) % 2 == 0) 0xFF606060.toInt() else 0xFFA0A0A0.toInt() }
        val sharp = BlurPixelMath.sharpness(stripes, w, h)
        val blurred = BlurPixelMath.sharpness(soft, w, h)
        assertTrue("줄무늬 $sharp", sharp > 100)
        assertTrue("번진 줄무늬 $blurred < $sharp", blurred < sharp / 3)
        assertEquals(0.0, BlurPixelMath.sharpness(stripes, 1, 1), 0.0)
    }

    @Test
    fun 가장자리_통계는_바깥_번짐과_안쪽_띠_밝기() {
        // 10×10, 여백 2 — 안쪽 6×6 은 검정, 바깥은 흰색
        val w = 10
        val white = 0xFFFFFFFF.toInt()
        val px = IntArray(w * w) { i ->
            val x = i % w
            val y = i / w
            if (x in 2 until 8 && y in 2 until 8) 0xFF000000.toInt() else white
        }
        val clean = BlurPixelMath.edgeStats(px, w, w, marginPx = 2, ringPx = 1, background = white)
        assertEquals(0, clean.spill)
        assertEquals(0.0, clean.ringLuminance, 0.0)
        // 바깥에 회색 두 점(번짐), 안쪽 띠 한 점을 흰색으로(가장자리가 비침)
        px[0] = 0xFF808080.toInt()
        px[9 * w + 9] = 0xFF808080.toInt()
        px[2 * w + 2] = white
        val smeared = BlurPixelMath.edgeStats(px, w, w, marginPx = 2, ringPx = 1, background = white)
        assertEquals(2, smeared.spill)
        assertTrue(smeared.ringLuminance > 0.0)
        val result = EdgeResult(EdgeChoice.Unbounded, clean, smeared)
        assertEquals(2, result.newSpill)
        assertTrue(result.ringBrighter > 0.0)
        assertEquals("줄어든 번짐은 0 으로", 0, EdgeResult(EdgeChoice.Rectangle, smeared, clean).newSpill)
        assertEquals(EdgeStats(0, 0.0), clean)
    }

    @Test
    fun satin_glass_판정() {
        assertTrue(GlassRun(GlassVariant.Composition, 12, 11, 4, 11).matchesExpectation)
        assertFalse("컴포지션에서 읽는데 리컴포즈가 없으면 다름", GlassRun(GlassVariant.Composition, 12, 0, 4, 11).matchesExpectation)
        assertTrue(GlassRun(GlassVariant.Block, 12, 0, 11, 11).matchesExpectation)
        assertFalse("블록에서 읽는데 리컴포즈하면 다름", GlassRun(GlassVariant.Block, 12, 1, 11, 11).matchesExpectation)
        assertFalse("블록이 거의 안 돌면 효과가 안 바뀐 것", GlassRun(GlassVariant.Block, 12, 0, 2, 11).matchesExpectation)
    }

    @Test
    fun 돌아오는_칸의_급변은_전환_중과_끝난_첫_프레임의_차() {
        val run = ReturnRun(ReturnVariant.Overlay, reference = 20.0, during = 22.8, firstAfter = 0.8, after150 = 0.8, after800 = 0.8)
        assertEquals(1.14, run.ratio(run.during)!!, 0.0001)
        assertEquals(0.04, run.ratio(run.firstAfter)!!, 0.0001)
        assertEquals(1.10, run.jump!!, 0.0001)
        assertNull(ReturnRun(ReturnVariant.InPlace, 20.0, null, 0.8, 0.8, 0.8).jump)
    }

    @Test
    fun 숫자_표기는_로캘과_무관하게_반올림한다() {
        assertEquals("0.03", fixed2(0.025))
        assertEquals("1.10", fixed2(1.1))
        assertEquals("0.8", fixed2(0.8, 1))
        assertEquals("97.2", fixed2(97.24, 1))
        assertEquals("-0.06", fixed2(-0.056))
        assertEquals("0.00", fixed2(-0.001))
    }
}
