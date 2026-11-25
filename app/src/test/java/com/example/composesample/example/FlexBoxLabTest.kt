package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.layout.flexbox.PassCounter
import com.example.composesample.presentation.example.component.ui.layout.flexbox.PlacedBox
import com.example.composesample.presentation.example.component.ui.layout.flexbox.growWidths
import com.example.composesample.presentation.example.component.ui.layout.flexbox.linesOf
import com.example.composesample.presentation.example.component.ui.layout.flexbox.oneDecimalText
import com.example.composesample.presentation.example.component.ui.layout.flexbox.samePlacement
import com.example.composesample.presentation.example.component.ui.layout.flexbox.shrinkWidths
import com.example.composesample.presentation.example.component.ui.layout.flexbox.summarize
import com.example.composesample.presentation.example.component.ui.layout.flexbox.weightWidths
import com.example.composesample.presentation.example.component.ui.layout.flexbox.widthsMatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Compose FlexBox 보강(#187)의 배분 식 · 줄 묶기 · 배치 비교.
 * 실제 배치(FlowRow 와 자리 · 리컴포지션 · fill)는 계측 테스트(FlexBoxTest)가 본다.
 */
class FlexBoxLabTest {

    @Test
    fun grow_는_basis_위에_남는_공간을_비율로_더한다() {
        val w = growWidths(listOf(60f, 60f, 60f), listOf(0f, 1f, 2f), 280f, 0f)
        assertEquals(60f, w[0], 0.01f)
        assertEquals(60f + 100f / 3, w[1], 0.01f)
        assertEquals(60f + 200f / 3, w[2], 0.01f)
        // 간격만큼 남는 공간이 준다 · 남는 공간이 없으면 그대로
        assertEquals(60f + 90f / 3, growWidths(listOf(60f, 60f, 60f), listOf(0f, 1f, 2f), 280f, 5f)[1], 0.01f)
        assertEquals(listOf(200f, 200f), growWidths(listOf(200f, 200f), listOf(1f, 1f), 280f, 0f))
    }

    @Test
    fun shrink_는_shrink_곱하기_basis_비율로_줄인다() {
        val w = shrinkWidths(listOf(140f, 140f, 140f), listOf(1f, 1f, 2f), 280f, 0f)
        assertEquals(listOf(105f, 105f, 70f), w)
        // basis 가 다르면 shrink 가 같아도 큰 칸이 더 준다
        val uneven = shrinkWidths(listOf(200f, 100f), listOf(1f, 1f), 240f, 0f)
        assertEquals(160f, uneven[0], 0.01f)
        assertEquals(80f, uneven[1], 0.01f)
        assertEquals("넘치지 않으면 그대로", listOf(100f, 100f), shrinkWidths(listOf(100f, 100f), listOf(1f, 1f), 280f, 0f))
    }

    @Test
    fun weight_는_고정_칸을_뺀_나머지를_비율로() {
        val w = weightWidths(listOf(60f, null, null), listOf(0f, 1f, 2f), 280f, 0f)
        assertEquals(60f, w[0], 0.01f)
        assertEquals(220f / 3, w[1], 0.01f)
        assertEquals(440f / 3, w[2], 0.01f)
        // 같은 비율이라도 grow 와 다르다
        assertTrue(abs(w[1] - growWidths(listOf(60f, 60f, 60f), listOf(0f, 1f, 2f), 280f, 0f)[1]) > 10f)
    }

    private fun abs(v: Float) = kotlin.math.abs(v)

    @Test
    fun 줄은_위쪽_좌표가_같은_칸끼리() {
        val boxes = listOf(
            PlacedBox(0, 0, 50, 20), PlacedBox(56, 1, 50, 20), // 1px 차는 같은 줄
            PlacedBox(0, 26, 50, 20), PlacedBox(112, 0, 50, 20),
        )
        assertEquals(listOf(listOf(0, 1, 3), listOf(2)), linesOf(boxes))
        assertEquals(emptyList<List<Int>>(), linesOf(emptyList()))
    }

    @Test
    fun 요약은_줄_수_좁아진_칸_무대_밖_칸() {
        val boxes = listOf(PlacedBox(0, 0, 40, 30), PlacedBox(46, 0, 60, 20), PlacedBox(112, 0, 80, 20))
        val s = summarize(boxes, naturalWidths = listOf(60, 60, 80), containerWidth = 150)
        assertEquals(1, s.lines)
        assertEquals(listOf(0), s.squeezed)
        assertEquals(listOf(2), s.overflow)
    }

    @Test
    fun 같은_자리_비교는_1px_까지() {
        val a = listOf(PlacedBox(0, 0, 50, 20), PlacedBox(56, 0, 50, 20))
        assertTrue(samePlacement(a, listOf(PlacedBox(1, 0, 50, 20), PlacedBox(56, 1, 49, 20))))
        assertFalse(samePlacement(a, listOf(PlacedBox(0, 0, 50, 20), PlacedBox(58, 0, 50, 20))))
        assertFalse("칸 수가 다르면", samePlacement(a, a.take(1)))
        assertTrue(widthsMatch(listOf(187, 254), listOf(186.7f, 253.3f)))
        assertFalse(widthsMatch(listOf(187, 250), listOf(186.7f, 253.3f)))
    }

    @Test
    fun 카운터_차이와_숫자_표기() {
        val c = PassCounter()
        val before = c.snapshot()
        c.flowCompositions += 2
        c.flexMeasures += 3
        assertEquals(PassCounter.Snapshot(2, 0, 0, 3), c.snapshot() - before)
        assertEquals("93.3", oneDecimalText(93.333f))
        assertEquals("-0.6", oneDecimalText(-0.56f))
        assertEquals("0.0", oneDecimalText(0.04f))
    }
}
