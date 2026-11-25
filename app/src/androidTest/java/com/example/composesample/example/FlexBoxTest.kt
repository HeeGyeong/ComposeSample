package com.example.composesample.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FLEX_DEMO_WIDTH_DP
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlexBoxMaxHost
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlowRowMaxHost
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FxChipBox
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FxTags
import com.example.composesample.presentation.example.component.ui.layout.flexbox.PassCounter
import com.example.composesample.presentation.example.component.ui.layout.flexbox.PlacementRecorder
import com.example.composesample.presentation.example.component.ui.layout.flexbox.growWidths
import com.example.composesample.presentation.example.component.ui.layout.flexbox.linesOf
import com.example.composesample.presentation.example.component.ui.layout.flexbox.recordPlacement
import com.example.composesample.presentation.example.component.ui.layout.flexbox.samePlacement
import com.example.composesample.presentation.example.component.ui.layout.flexbox.shrinkWidths
import com.example.composesample.presentation.example.component.ui.layout.flexbox.summarize
import com.example.composesample.presentation.example.component.ui.layout.flexbox.weightWidths
import com.example.composesample.presentation.example.component.ui.layout.flexbox.widthsMatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose FlexBox(#187) — 같은 칸을 FlowRow 와 FlexBox 로 놓고 onPlaced 로 읽은 자리로 비교한다.
 * given : 폭 280dp 무대, 두 단어 칩 8개 · 세 칸 grow/shrink/weight · fillMaxWidth 칩 하나
 * when : FlowRow 를 FlexBox 로 바꾸고 maxItems 를 바꾼다
 * then : 기본 config 는 한 줄에 눌리고 Wrap 을 주면 FlowRow 와 같은 자리, maxItemsInEachLine 은 리컴포지션 없이 다시 측정,
 *        grow · shrink 는 식대로, weight 는 기본 크기를 무시, fillMaxWidth 는 줄의 남는 공간만 채운다
 */
class FlexBoxTest {

    @get:Rule
    val rule = createComposeRule()

    private val stageModifier = Modifier.width(FLEX_DEMO_WIDTH_DP.dp)

    private fun stagePx(): Int = with(rule.density) { FLEX_DEMO_WIDTH_DP.dp.roundToPx() }

    @Test
    fun 기본_config_는_한_줄에_눌리고_Wrap_을_주면_FlowRow_와_같다() {
        val flow = PlacementRecorder()
        val noWrap = PlacementRecorder()
        val wrap = PlacementRecorder()
        rule.setContent {
            Column {
                FlowRow(stageModifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(flow, i)) }
                }
                FlexBox(stageModifier, config = FlexBoxConfig { gap(6.dp) }) {
                    FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(noWrap, i)) }
                }
                FlexBox(stageModifier, config = FlexBoxConfig { wrap(FlexWrap.Wrap); gap(6.dp) }) {
                    FxTags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(wrap, i)) }
                }
            }
        }
        rule.waitForIdle()
        val f = flow.snapshot(FxTags.size)!!
        val n = noWrap.snapshot(FxTags.size)!!
        val w = wrap.snapshot(FxTags.size)!!
        assertTrue("FlowRow 는 줄바꿈 ${linesOf(f).size}", linesOf(f).size >= 2)
        val s = summarize(n, f.map { it.width }, stagePx())
        assertEquals("기본은 한 줄", 1, s.lines)
        assertTrue("눌리거나 넘친다 $s", s.squeezed.isNotEmpty() && s.overflow.isNotEmpty())
        assertTrue("Wrap 은 FlowRow 와 같은 자리", samePlacement(f, w))
    }

    @Test
    fun maxItemsInEachLine_은_같은_자리이고_리컴포지션_없이_다시_측정한다() {
        val maxItems = mutableIntStateOf(3)
        val flow = PlacementRecorder()
        val flex = PlacementRecorder()
        val counter = PassCounter()
        rule.setContent {
            Column {
                androidx.compose.foundation.layout.Box(stageModifier) { FlowRowMaxHost(maxItems.intValue, flow, counter) }
                androidx.compose.foundation.layout.Box(stageModifier) { FlexBoxMaxHost(maxItems, flex, counter) }
            }
        }
        rule.waitForIdle()
        val before = counter.snapshot()
        for (n in listOf(1, 2, 4, 5, 8)) {
            rule.runOnIdle { maxItems.intValue = n }
            rule.waitForIdle()
            assertTrue("n = $n", samePlacement(flow.snapshot(FxTags.size)!!, flex.snapshot(FxTags.size)!!))
        }
        val d = counter.snapshot() - before
        assertEquals("FlowRow 호스트는 바뀔 때마다 리컴포즈 $d", 5, d.flowCompositions)
        assertEquals("FlexBox 호스트는 리컴포즈 0 $d", 0, d.flexCompositions)
        assertTrue("FlexBox 는 다시 측정 $d", d.flexMeasures >= 5)
    }

    @Test
    fun grow_shrink_는_식대로_weight_는_기본_크기를_보지_않는다() {
        val grow = PlacementRecorder()
        val weight = PlacementRecorder()
        val shrink = PlacementRecorder()
        rule.setContent {
            Column {
                FlexBox(stageModifier) {
                    FxChipBox("60dp", Modifier.recordPlacement(grow, 0).width(60.dp))
                    FxChipBox("grow 1", Modifier.recordPlacement(grow, 1).flex { basis(60.dp); grow(1f) })
                    FxChipBox("grow 2", Modifier.recordPlacement(grow, 2).flex { basis(60.dp); grow(2f) })
                }
                FlowRow(stageModifier) {
                    FxChipBox("60dp", Modifier.recordPlacement(weight, 0).width(60.dp))
                    FxChipBox("weight 1", Modifier.recordPlacement(weight, 1).weight(1f))
                    FxChipBox("weight 2", Modifier.recordPlacement(weight, 2).weight(2f))
                }
                FlexBox(stageModifier) {
                    FxChipBox("shrink 1", Modifier.recordPlacement(shrink, 0).flex { basis(140.dp) })
                    FxChipBox("shrink 1", Modifier.recordPlacement(shrink, 1).flex { basis(140.dp) })
                    FxChipBox("shrink 2", Modifier.recordPlacement(shrink, 2).flex { basis(140.dp); shrink(2f) })
                }
            }
        }
        rule.waitForIdle()
        val d = rule.density.density
        val stage = FLEX_DEMO_WIDTH_DP
        fun px(dp: List<Float>) = dp.map { it * d }
        val g = grow.snapshot(3)!!.map { it.width }
        val wgt = weight.snapshot(3)!!.map { it.width }
        val s = shrink.snapshot(3)!!.map { it.width }
        assertTrue("grow $g", widthsMatch(g, px(growWidths(listOf(60f, 60f, 60f), listOf(0f, 1f, 2f), stage, 0f))))
        assertTrue("weight $wgt", widthsMatch(wgt, px(weightWidths(listOf(60f, null, null), listOf(0f, 1f, 2f), stage, 0f))))
        assertTrue("shrink $s", widthsMatch(s, px(shrinkWidths(listOf(140f, 140f, 140f), listOf(1f, 1f, 2f), stage, 0f))))
        assertTrue("같은 1:2 라도 weight 와 grow 는 다르다", kotlin.math.abs(g[1] - wgt[1]) > 10 * d)
    }

    @Test
    fun fillMaxWidth_는_FlowRow_에서_한_줄_FlexBox_에서_남는_공간() {
        val tags = listOf("전체", "할인", "신상품", "무료 배송", "오늘 출발")
        val fill = 2
        val flow = PlacementRecorder()
        val flex = PlacementRecorder()
        rule.setContent {
            Column {
                FlowRow(stageModifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    tags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(flow, i).let { if (i == fill) it.fillMaxWidth() else it }) }
                }
                FlexBox(stageModifier, config = FlexBoxConfig { wrap(FlexWrap.Wrap); gap(6.dp) }) {
                    tags.forEachIndexed { i, t -> FxChipBox(t, Modifier.recordPlacement(flex, i).let { if (i == fill) it.fillMaxWidth() else it }) }
                }
            }
        }
        rule.waitForIdle()
        val stage = stagePx()
        val f = flow.snapshot(tags.size)!!
        val x = flex.snapshot(tags.size)!!
        assertEquals("FlowRow: 무대 폭 가득", stage, f[fill].width)
        assertEquals("FlowRow: 그 줄에 혼자", 1, linesOf(f).first { fill in it }.size)
        val flexLine = linesOf(x).first { fill in it }
        assertTrue("FlexBox: 다른 칸과 같은 줄 $flexLine", flexLine.size > 1)
        assertTrue("FlexBox: 무대보다 좁다", x[fill].width < stage)
        assertTrue("FlexBox: 줄 끝이 무대 끝(남는 공간을 채움)", kotlin.math.abs(x[flexLine.last()].right - stage) <= 1)
    }
}
