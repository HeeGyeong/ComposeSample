package com.example.composesample.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.example.composesample.presentation.example.component.ui.text.LH_PREVIEW_WIDTH_DP
import com.example.composesample.presentation.example.component.ui.text.LineInkMath
import com.example.composesample.presentation.example.component.ui.text.LineMode
import com.example.composesample.presentation.example.component.ui.text.LineSample
import com.example.composesample.presentation.example.component.ui.text.LineWrap
import com.example.composesample.presentation.example.component.ui.text.SampleMeasure
import com.example.composesample.presentation.example.component.ui.text.isolateLine
import com.example.composesample.presentation.example.component.ui.text.lineModeStyle
import com.example.composesample.presentation.example.component.ui.text.measureSample
import com.example.composesample.presentation.example.component.ui.text.previewWidthPx
import com.example.composesample.presentation.example.component.ui.text.sampleText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

/**
 * 줄 높이 모드(#186) — 같은 문단을 모드마다 배치하고 줄마다 따로 그려 잰다.
 * given : 큰 글씨 · 키 큰 문자가 섞인 문단, 줄마다 \n 또는 한 문단 자동 줄바꿈(+ 글자마다 span 대조군)
 * when : lineHeight 21sp 를 Fixed · Minimum · PerLine 으로 준다
 * then : \n 이면 PerLine 은 큰 줄만 늘리고 Fixed 는 눌러 잉크가 상자 밖으로, 자동 줄바꿈이면 큰 줄 다음 줄이
 *        높이를 물려받고, 글자마다 span 으로 구간을 끊으면 물려받지 않는다
 */
class LineHeightModeTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val density = Density(context)
    private val resolver = createFontFamilyResolver(context)
    private val widthPx = previewWidthPx(density)

    private fun measure(sample: LineSample, wrap: LineWrap): SampleMeasure =
        measureSample(sample, wrap, widthPx, density, resolver)

    private fun heights(m: SampleMeasure, mode: LineMode) = m.byMode.getValue(mode).lines.map { it.height }

    @Test
    fun 줄마다_줄바꿈이면_PerLine_은_큰_줄만_늘리고_Fixed_는_눌러_잉크가_나간다() {
        val m = measure(LineSample.SizeSecond, LineWrap.HardBreaks)
        val req = m.requestedPx.toFloat()
        val natural = heights(m, LineMode.Natural)
        assertTrue("둘째 줄이 큰 줄 $natural", natural[1] > req + 1f)
        // PerLine = 줄마다 max(자연, 요청)
        heights(m, LineMode.PerLine).forEachIndexed { i, h -> assertEquals("PerLine $i", m.perLineExpected[i], h, 1f) }
        assertEquals(emptyList<Int>(), m.inheritedLines)
        assertTrue(m.perLine.maxOverflow < 1f)
        // Fixed = 모든 줄 요청 높이, 큰 줄 잉크가 상자 밖으로
        heights(m, LineMode.Fixed).forEach { assertEquals(req, it, 1f) }
        val fixed = m.byMode.getValue(LineMode.Fixed)
        assertTrue("Fixed 큰 줄 상자 밖 ${fixed.overflow(1)}", fixed.overflow(1) >= 2f)
        assertTrue("Fixed 잉크 간격 ${fixed.minInkGap} < PerLine ${m.perLine.minInkGap}", fixed.minInkGap!! < m.perLine.minInkGap!!)
    }

    @Test
    fun 큰_줄이_첫_줄이면_Minimum_은_모든_줄을_그_높이로() {
        val m = measure(LineSample.SizeFirst, LineWrap.HardBreaks)
        val first = heights(m, LineMode.Natural)[0]
        assertTrue(first > m.requestedPx + 1f)
        heights(m, LineMode.Minimum).forEach { assertEquals(first, it, 1f) }
        val per = heights(m, LineMode.PerLine)
        assertEquals(first, per[0], 1f)
        per.drop(1).forEach { assertEquals(m.requestedPx.toFloat(), it, 1f) }
        assertTrue(m.perLine.height < m.byMode.getValue(LineMode.Minimum).height)
    }

    @Test
    fun 키_큰_문자_줄은_PerLine_에서만_늘어난다() {
        val m = measure(LineSample.Scripts, LineWrap.HardBreaks)
        val per = m.perLine
        val grown = per.lines.indices.filter { per.lines[it].height > m.requestedPx + 1f }
        // 티베트어 줄은 API 28 · 35 에뮬 모두 늘었다(미얀마어는 35 에서만)
        assertTrue("늘어난 줄 ${per.lines.map { it.height }}", grown.isNotEmpty())
        heights(m, LineMode.Fixed).forEach { assertEquals(m.requestedPx.toFloat(), it, 1f) }
        assertEquals(emptyList<Int>(), m.inheritedLines)
        // 늘어난 줄의 상자 = 그 줄의 자연(글꼴 메트릭) 높이 — 잉크보다 크다
        val natural = heights(m, LineMode.Natural)
        grown.forEach { i ->
            assertEquals(natural[i], per.lines[i].height, 1f)
            val ink = per.ink[i]!!
            assertTrue("상자 ${per.lines[i].height} > 잉크 ${ink.bottom - ink.top}", per.lines[i].height > ink.bottom - ink.top)
        }
    }

    @Test
    fun 자동_줄바꿈이면_큰_줄_다음_줄이_높이를_물려받는다() {
        for (sample in LineSample.entries) {
            val m = measure(sample, LineWrap.SoftWrap)
            assertTrue("$sample 줄바꿈 위치가 같아야 비교", m.sameBreaks)
            val natural = heights(m, LineMode.Natural)
            val tall = natural.indexOfFirst { it > m.requestedPx + 1f }
            assertTrue("$sample 큰 줄 $natural", tall >= 0)
            val inherited = m.inheritedLines
            assertTrue("$sample 물려받은 줄 $inherited", inherited.isNotEmpty())
            assertTrue("$sample 큰 줄 뒤에서만", inherited.all { it > tall })
            // 물려받은 높이 = 앞의 큰 줄 높이, 자연 배치(lineHeight 없음)는 물려받지 않는다
            inherited.forEach { assertEquals(heights(m, LineMode.PerLine)[tall], heights(m, LineMode.PerLine)[it], 1f) }
            inherited.forEach { assertTrue(natural[it] < natural[tall] - 1f) }
        }
        // 큰 글씨가 첫 줄이면 PerLine 이 Minimum 과 같다
        val first = measure(LineSample.SizeFirst, LineWrap.SoftWrap)
        heights(first, LineMode.PerLine).zip(heights(first, LineMode.Minimum)).forEach { (p, n) -> assertEquals(n, p, 1f) }
    }

    @Test
    fun 글자마다_span_으로_구간을_끊으면_물려받지_않는다() {
        for (sample in LineSample.entries) {
            val m = measure(sample, LineWrap.SoftWrapCharSpans)
            assertTrue(m.sameBreaks)
            assertEquals("$sample", emptyList<Int>(), m.inheritedLines)
            heights(m, LineMode.PerLine).forEachIndexed { i, h -> assertEquals("$sample $i", m.perLineExpected[i], h, 1f) }
        }
    }

    @Test
    fun 투명_span_은_줄_상자를_바꾸지_않는다() {
        for (wrap in LineWrap.entries) for (sample in LineSample.entries) {
            val m = measure(sample, wrap)
            m.byMode.values.forEach { assertTrue("$sample $wrap ${it.mode}", it.isolationKeptLayout) }
        }
    }

    @Test
    fun 오프스크린_잉크는_화면에_그린_잉크와_같다() {
        val m = measure(LineSample.SizeSecond, LineWrap.HardBreaks)
        val fixed = m.byMode.getValue(LineMode.Fixed)
        val line = fixed.lines[1]
        val text = isolateLine(sampleText(LineSample.SizeSecond, LineWrap.HardBreaks), line.start, line.end)
        val margin = 24.dp
        rule.setContent {
            Box(Modifier.testTag("stage").background(Color.White).padding(vertical = margin)) {
                BasicText(text, Modifier.width(LH_PREVIEW_WIDTH_DP.dp), style = lineModeStyle(LineMode.Fixed))
            }
        }
        rule.waitForIdle()
        val image = rule.onNodeWithTag("stage").captureToImage()
        val pixels = IntArray(image.width * image.height)
        image.readPixels(pixels)
        // 흰 바탕의 검은 글자 → 어두운 정도를 알파로
        val alpha = IntArray(pixels.size) { i ->
            val p = pixels[i]
            val lum = (((p shr 16) and 0xFF) * 299 + ((p shr 8) and 0xFF) * 587 + (p and 0xFF) * 114) / 1000
            (255 - lum) shl 24
        }
        val (top, bottom) = LineInkMath.inkRows(alpha, image.width, image.height)!!
        val marginPx = with(density) { margin.roundToPx() }
        val ink = fixed.ink[1]!!
        assertTrue("화면 ${top - marginPx}..${bottom - marginPx} vs 오프스크린 ${ink.top}..${ink.bottom}", abs(top - marginPx - ink.top) <= 2 && abs(bottom - marginPx - ink.bottom) <= 2)
        // 화면에서도 큰 줄 잉크가 상자 위로 나간다
        assertTrue(top - marginPx < line.top - 1)
    }
}
