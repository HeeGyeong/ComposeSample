package com.example.composesample.example

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.example.component.ui.graphics.BlurPixelMath
import com.example.composesample.presentation.example.component.ui.graphics.GlassLabState
import com.example.composesample.presentation.example.component.ui.graphics.GlassStage
import com.example.composesample.presentation.example.component.ui.graphics.GlassVariant
import com.example.composesample.presentation.example.component.ui.graphics.RETURN_SCROLL_OFFSET_DP
import com.example.composesample.presentation.example.component.ui.graphics.RETURN_TARGET_INDEX
import com.example.composesample.presentation.example.component.ui.graphics.ReturnLabState
import com.example.composesample.presentation.example.component.ui.graphics.ReturnStage
import com.example.composesample.presentation.example.component.ui.graphics.ReturnVariant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Modifier.blur { } 예제(#149) — 화면의 무대를 그대로 띄우고 캡처한 픽셀의 선명도(이웃 밝기 차)로 본다.
 * given : satin glass 목록 · 세로 그라데이션 블러 · 가장자리 처리 · 헤더 밑으로 돌아오는 공유 요소
 * when : 목록을 스크롤하고, 칸을 열었다 시계를 멈춘 채 돌아온다
 * then : 블록에서 읽으면 리컴포지션 0, 위는 선명 · 아래는 흐림(API 33+), Unbounded 만 번짐,
 *        오버레이로 돌아오는 칸은 전환이 끝나는 프레임에 한꺼번에 흐려지고 원문 해법은 끝난 뒤 서서히 흐려진다
 */
class ProgressiveBlurTest {

    @get:Rule
    val rule = createComposeRule()

    private fun capture(tag: String): ImageBitmap = rule.onNodeWithTag(tag).captureToImage()

    private fun sharpness(image: ImageBitmap, x: Int, y: Int, w: Int, h: Int): Double {
        val buffer = IntArray(w * h)
        image.readPixels(buffer, x, y, w, h)
        return BlurPixelMath.sharpness(buffer, w, h)
    }

    @Test
    fun 블록에서_스크롤을_읽으면_리컴포지션_없이_블록만_다시_돈다() {
        val lab = GlassLabState().apply { variant = GlassVariant.Block }
        rule.setContent { GlassStage(lab) }
        rule.waitForIdle()
        val recompositions = lab.hostRecompositions.count
        val blockRuns = lab.blockRuns
        val step = with(rule.density) { 40.dp.toPx() }
        repeat(5) {
            rule.runOnIdle { runBlocking { lab.listState.scrollBy(step) } }
            rule.waitForIdle()
        }
        assertEquals(recompositions, lab.hostRecompositions.count)
        assertTrue("블러 블록 ${lab.blockRuns - blockRuns}번", lab.blockRuns - blockRuns >= 5)
    }

    @Test
    fun 컴포지션에서_읽으면_스크롤마다_리컴포즈한다() {
        val lab = GlassLabState().apply { variant = GlassVariant.Composition }
        rule.setContent { GlassStage(lab) }
        rule.waitForIdle()
        val recompositions = lab.hostRecompositions.count
        val step = with(rule.density) { 10.dp.toPx() }
        repeat(5) {
            rule.runOnIdle { runBlocking { lab.listState.scrollBy(step) } }
            rule.waitForIdle()
        }
        assertEquals(recompositions + 5, lab.hostRecompositions.count)
    }

    @Test
    fun 세로_그라데이션은_위는_선명하고_아래는_흐리다() {
        rule.setContent {
            Box(
                Modifier
                    .testTag("gradient")
                    .size(300.dp, 200.dp)
                    .blur { blurRadiusSpec = BlurRadiusSpec.verticalGradient(0.dp, 16.dp) }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val step = 6.dp.toPx()
                    var x = 0f
                    var i = 0
                    while (x < size.width) {
                        drawRect(if (i % 2 == 0) Color.Black else Color.White, Offset(x, 0f), Size(step / 2f, size.height))
                        x += step / 2f
                        i++
                    }
                }
            }
        }
        rule.waitForIdle()
        val image = capture("gradient")
        val quarter = image.height / 4
        val top = sharpness(image, 0, 0, image.width, quarter)
        val bottom = sharpness(image, 0, image.height - quarter, image.width, quarter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertTrue("위 $top · 아래 $bottom", bottom < top * 0.5)
        } else {
            // API 31~32 는 identity, 31 미만은 무시 — 위아래가 같다
            assertEquals(top, bottom, top * 0.05)
        }
    }

    @Test
    fun Unbounded_만_칸_밖으로_번진다() {
        assumeTrue("Modifier.blur 는 API 31+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val edges = listOf("rect" to BlurredEdgeTreatment.Rectangle, "unbounded" to BlurredEdgeTreatment.Unbounded)
        rule.setContent {
            Column {
                edges.forEach { (name, edge) ->
                    Box(Modifier.testTag("edge-$name").background(Color.White).padding(24.dp)) {
                        Box(
                            Modifier
                                .size(120.dp, 60.dp)
                                .blur {
                                    blurRadiusSpec = BlurRadiusSpec.uniform(16.dp)
                                    edgeTreatment = edge
                                }
                                .background(Color(0xFF283593))
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
        val margin = with(rule.density) { 24.dp.roundToPx() }
        val spill = edges.associate { (name, _) ->
            val image = capture("edge-$name")
            val pixels = IntArray(image.width * image.height)
            image.readPixels(pixels)
            name to BlurPixelMath.edgeStats(pixels, image.width, image.height, margin, 3, android.graphics.Color.WHITE).spill
        }
        assertEquals("Rectangle 은 자른다", 0, spill["rect"])
        assertTrue("Unbounded 번짐 ${spill["unbounded"]}", spill.getValue("unbounded") > 1000)
    }

    /** 목표 칸 자리(창 좌표)를 무대 캡처 안의 좌표로 */
    private fun targetRegion(lab: ReturnLabState): Rect {
        val stage = rule.onNodeWithTag("returnStage").fetchSemanticsNode().boundsInWindow
        return lab.targetBounds.bounds.translate(-stage.left, -stage.top).deflate(4f)
    }

    private fun regionSharpness(region: Rect): Double {
        val image = capture("returnStage")
        val x = region.left.roundToInt().coerceAtLeast(0)
        val y = region.top.roundToInt().coerceAtLeast(0)
        val w = region.width.roundToInt().coerceAtMost(image.width - x)
        val h = region.height.roundToInt().coerceAtMost(image.height - y)
        return sharpness(image, x, y, w, h)
    }

    private class ReturnSamples(val reference: Double, val during: Double, val firstAfter: Double, val after800: Double)

    private fun runReturn(variant: ReturnVariant): ReturnSamples {
        val lab = ReturnLabState().apply { this.variant = variant }
        lateinit var scope: CoroutineScope
        rule.setContent {
            scope = rememberCoroutineScope()
            ReturnStage(lab)
        }
        rule.waitForIdle()
        val offsetPx = with(rule.density) { RETURN_SCROLL_OFFSET_DP.dp.roundToPx() }
        rule.runOnIdle { runBlocking { lab.placeTargetUnderHeader(offsetPx) } }
        rule.waitForIdle()
        val region = targetRegion(lab)
        rule.runOnIdle { runBlocking { lab.blurAmount.snapTo(0f) } }
        rule.waitForIdle()
        val reference = regionSharpness(region)
        rule.runOnIdle { runBlocking { lab.blurAmount.snapTo(1f) } }
        rule.runOnIdle { lab.selected = RETURN_TARGET_INDEX }
        rule.waitForIdle()

        rule.mainClock.autoAdvance = false
        rule.runOnIdle { scope.launch { lab.back() } }
        // 전환 700ms — 85%(595ms), 끝난 직후(720ms, 원문 해법의 블러가 올라가기 전), 끝나고 900ms 뒤
        rule.mainClock.advanceTimeBy(595)
        val during = regionSharpness(region)
        rule.mainClock.advanceTimeBy(125)
        val firstAfter = regionSharpness(region)
        rule.mainClock.advanceTimeBy(900)
        val after800 = regionSharpness(region)
        rule.mainClock.autoAdvance = true
        return ReturnSamples(reference, during, firstAfter, after800)
    }

    @Test
    fun 오버레이로_돌아온_칸은_전환이_끝나는_프레임에_한꺼번에_흐려진다() {
        assumeTrue("Modifier.blur 는 API 31+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val s = runReturn(ReturnVariant.Overlay)
        assertTrue("전환 85% 선명도 ${s.during / s.reference}", s.during / s.reference > 0.7)
        assertTrue("끝난 뒤 ${s.firstAfter / s.reference}", s.firstAfter / s.reference < 0.3)
    }

    @Test
    fun 원문_해법은_끝난_순간_선명하고_뒤에_흐려진다() {
        assumeTrue("Modifier.blur 는 API 31+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val s = runReturn(ReturnVariant.FadeInAfter)
        assertTrue("전환 85% ${s.during / s.reference}", s.during / s.reference > 0.7)
        assertTrue("끝난 직후 ${s.firstAfter / s.reference}", s.firstAfter / s.reference > 0.6)
        assertTrue("800ms 뒤 ${s.after800 / s.reference}", s.after800 / s.reference < 0.3)
    }

    @Test
    fun 오버레이를_안_쓰면_전환_중에도_흐리다() {
        assumeTrue("Modifier.blur 는 API 31+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        val s = runReturn(ReturnVariant.InPlace)
        assertTrue("전환 85% ${s.during / s.reference}", s.during / s.reference < 0.3)
    }
}
