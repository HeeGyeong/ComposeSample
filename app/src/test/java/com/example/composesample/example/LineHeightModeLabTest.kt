package com.example.composesample.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.example.component.ui.text.InkRows
import com.example.composesample.presentation.example.component.ui.text.LH_BIG_FONT_SP
import com.example.composesample.presentation.example.component.ui.text.LH_FONT_SP
import com.example.composesample.presentation.example.component.ui.text.LH_LINE_HEIGHT_SP
import com.example.composesample.presentation.example.component.ui.text.LineBox
import com.example.composesample.presentation.example.component.ui.text.LineInkMath
import com.example.composesample.presentation.example.component.ui.text.LineMode
import com.example.composesample.presentation.example.component.ui.text.LineSample
import com.example.composesample.presentation.example.component.ui.text.LineWrap
import com.example.composesample.presentation.example.component.ui.text.ModeMeasure
import com.example.composesample.presentation.example.component.ui.text.SAMPLE_MYANMAR
import com.example.composesample.presentation.example.component.ui.text.SAMPLE_TIBETAN
import com.example.composesample.presentation.example.component.ui.text.SampleMeasure
import com.example.composesample.presentation.example.component.ui.text.isolateLine
import com.example.composesample.presentation.example.component.ui.text.lineModeStyle
import com.example.composesample.presentation.example.component.ui.text.oneDecimal
import com.example.composesample.presentation.example.component.ui.text.perLineExpectation
import com.example.composesample.presentation.example.component.ui.text.pxToDpText
import com.example.composesample.presentation.example.component.ui.text.ratioText
import com.example.composesample.presentation.example.component.ui.text.sampleText
import com.example.composesample.presentation.example.component.ui.text.tallerThanExpected
import com.example.composesample.presentation.example.component.ui.text.withCharSpans
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 줄 높이 모드 비교(#186)의 계산 · 예문 구성 · 픽셀 셈.
 * 실제 줄 높이 · 물려받기 · 화면 잉크 대조는 계측 테스트(LineHeightModeTest)가 본다.
 */
class LineHeightModeLabTest {

    @Test
    fun 모드별_스타일은_Natural_만_줄_높이가_없고_나머지는_Trim_None() {
        val natural = lineModeStyle(LineMode.Natural)
        assertTrue(natural.lineHeight.isUnspecified)
        assertNull(natural.lineHeightStyle)
        for (mode in LineMode.entries - LineMode.Natural) {
            val style = lineModeStyle(mode)
            assertEquals(LH_LINE_HEIGHT_SP.sp, style.lineHeight)
            assertEquals(mode.mode, style.lineHeightStyle?.mode)
            assertEquals(LineHeightStyle.Trim.None, style.lineHeightStyle?.trim)
            assertEquals(LineHeightStyle.Alignment.Proportional, style.lineHeightStyle?.alignment)
        }
    }

    @Test
    fun 예문은_줄바꿈_방식만_다르고_큰_글씨는_span_하나() {
        for (sample in LineSample.entries) {
            val hard = sampleText(sample, LineWrap.HardBreaks)
            val soft = sampleText(sample, LineWrap.SoftWrap)
            assertTrue("$sample 은 \\n 으로 나뉜다", hard.text.count { it == '\n' } >= 3)
            assertFalse("$sample 자동 줄바꿈은 \\n 없음", soft.text.contains('\n'))
            val bigSpans = { t: androidx.compose.ui.text.AnnotatedString -> t.spanStyles.filter { it.item.fontSize == LH_BIG_FONT_SP.sp } }
            if (sample == LineSample.Scripts) {
                assertTrue(hard.text.contains(SAMPLE_MYANMAR) && hard.text.contains(SAMPLE_TIBETAN))
                assertTrue(bigSpans(hard).isEmpty())
            } else {
                assertEquals(1, bigSpans(hard).size)
                assertEquals("9,900", bigSpans(hard).single().let { hard.text.substring(it.start, it.end) })
                assertEquals(1, bigSpans(soft).size)
            }
        }
        // 큰 글씨 위치 — 둘째 줄 / 첫 줄
        val second = sampleText(LineSample.SizeSecond, LineWrap.HardBreaks)
        assertEquals(1, second.text.split('\n').indexOfFirst { it.contains("9,900") })
        val first = sampleText(LineSample.SizeFirst, LineWrap.HardBreaks)
        assertEquals(0, first.text.split('\n').indexOfFirst { it.contains("9,900") })
    }

    @Test
    fun 글자마다_span_은_글자_수만큼_같은_크기로() {
        val soft = sampleText(LineSample.SizeSecond, LineWrap.SoftWrap)
        val chars = withCharSpans(soft)
        assertEquals(soft.text, chars.text)
        val perChar = chars.spanStyles.filter { it.end - it.start == 1 && it.item.fontSize.isSp }
        assertEquals(soft.text.length, perChar.size)
        val big = soft.spanStyles.single { it.item.fontSize == LH_BIG_FONT_SP.sp }
        for (span in perChar) {
            val expected = if (span.start in big.start until big.end) LH_BIG_FONT_SP else LH_FONT_SP
            assertEquals(expected.sp, span.item.fontSize)
        }
        // 큰 글씨 span(굵게)은 그대로 남는다
        assertTrue(chars.spanStyles.any { it.start == big.start && it.end == big.end && it.item.fontWeight != null })
        assertEquals(LineWrap.SoftWrapCharSpans.let { sampleText(LineSample.SizeSecond, it).text }, soft.text)
    }

    @Test
    fun 한_줄만_남기면_앞뒤가_투명() {
        val text = sampleText(LineSample.SizeSecond, LineWrap.HardBreaks)
        val start = text.text.indexOf('\n') + 1
        val end = text.text.indexOf('\n', start) + 1
        val isolated = isolateLine(text, start, end)
        assertEquals(text.text, isolated.text)
        val hidden = isolated.spanStyles.filter { it.item.color == Color.Transparent }
        assertEquals(listOf(0 to start, end to text.length), hidden.map { it.start to it.end })
        // 첫 줄 · 끝 줄은 한쪽만
        assertEquals(1, isolateLine(text, 0, start).spanStyles.count { it.item.color == Color.Transparent })
        assertEquals(1, isolateLine(text, end, text.length).spanStyles.count { it.item.color == Color.Transparent })
    }

    @Test
    fun PerLine_기대는_줄마다_자연과_요청_중_큰_쪽() {
        assertEquals(listOf(42f, 71f, 42f), perLineExpectation(listOf(40f, 71f, 40f), 42))
        assertEquals(listOf(2, 3), tallerThanExpected(listOf(42f, 71f, 71f, 71f), listOf(42f, 71f, 42f, 42f)))
        assertEquals("1px 차는 허용", emptyList<Int>(), tallerThanExpected(listOf(43f), listOf(42f)))
        assertEquals("짧은 쪽 목록 밖은 보지 않는다", emptyList<Int>(), tallerThanExpected(listOf(42f, 90f), listOf(42f)))
    }

    private fun measure(mode: LineMode, heights: List<Float>, starts: List<Int> = heights.indices.map { it * 10 }): ModeMeasure {
        var top = 0f
        val lines = heights.mapIndexed { i, h -> LineBox(starts[i], starts[i] + 10, top, top + h).also { top += h } }
        return ModeMeasure(mode, lines, lines.map { InkRows(it.top.toInt() + 2, it.bottom.toInt() - 2) }, List(lines.size - 1) { 0 }, top, true)
    }

    @Test
    fun 물려받은_줄은_PerLine_이_기대보다_높은_줄() {
        val natural = measure(LineMode.Natural, listOf(40f, 71f, 40f, 40f))
        val per = measure(LineMode.PerLine, listOf(42f, 71f, 71f, 71f))
        val byMode = LineMode.entries.associateWith { if (it == LineMode.PerLine) per else natural }
        val sample = SampleMeasure(LineSample.SizeSecond, LineWrap.SoftWrap, byMode, 42)
        assertTrue(sample.sameBreaks)
        assertEquals(listOf(42f, 71f, 42f, 42f), sample.perLineExpected)
        assertEquals(listOf(2, 3), sample.inheritedLines)
        // 줄바꿈이 다르면 줄끼리 비교하지 않는다
        val moved = measure(LineMode.PerLine, listOf(42f, 71f, 71f, 71f), listOf(0, 12, 20, 30))
        val other = SampleMeasure(LineSample.SizeSecond, LineWrap.SoftWrap, byMode + (LineMode.PerLine to moved), 42)
        assertFalse(other.sameBreaks)
        assertEquals(emptyList<Int>(), other.inheritedLines)
    }

    @Test
    fun 상자_밖_잉크와_잉크_간격() {
        val lines = listOf(LineBox(0, 5, 0f, 42f), LineBox(5, 10, 42f, 84f))
        // 둘째 줄 잉크가 상자 위로 10px(32), 첫 줄 잉크 아래는 36 → 간격 -4
        val m = ModeMeasure(LineMode.Fixed, lines, listOf(InkRows(9, 36), InkRows(32, 84)), listOf(7), 84f, true)
        assertEquals(10f, m.overflowTop(1))
        assertEquals(0f, m.overflowBottom(1))
        assertEquals(10f, m.maxOverflow)
        assertEquals(listOf<Float?>(-4f), m.inkGaps)
        assertEquals(-4f, m.minInkGap)
        assertEquals(7, m.totalCollisions)
        // 잉크 없는 줄은 0 · 간격 없음
        val empty = ModeMeasure(LineMode.Fixed, lines, listOf(null, InkRows(50, 80)), listOf(0), 84f, true)
        assertEquals(0f, empty.overflow(0))
        assertNull(empty.minInkGap)
    }

    @Test
    fun 잉크_행과_겹친_픽셀() {
        val w = 4
        val h = 6
        val none = IntArray(w * h)
        assertNull(LineInkMath.inkRows(none, w, h))
        val a = IntArray(w * h).also {
            it[1 * w + 2] = 0xFF000000.toInt() // 1행
            it[3 * w + 0] = 0x40000000 // 3행, 알파 64
            it[4 * w + 1] = 0x10000000 // 4행, 알파 16 — 문턱 아래
        }
        assertEquals(1 to 4, LineInkMath.inkRows(a, w, h))
        val b = IntArray(w * h).also {
            it[1 * w + 2] = 0xFF000000.toInt() // a 와 같은 자리
            it[3 * w + 0] = 0xFF000000.toInt() // a 는 알파 64 — 겹침 문턱(96) 아래
        }
        assertEquals(1, LineInkMath.overlap(a, b))
        assertEquals(2, LineInkMath.overlap(a, b, threshold = 64))
    }

    @Test
    fun 숫자_표기는_로캘과_무관하게_반올림() {
        assertEquals("21.0", pxToDpText(42f, 2f))
        assertEquals("35.5", pxToDpText(71f, 2f))
        assertEquals("-2.5", pxToDpText(-5f, 2f))
        assertEquals("0.1", oneDecimal(0.05))
        assertEquals("6.8", ratioText(808, 119))
        assertNull(ratioText(5, 0))
    }
}
