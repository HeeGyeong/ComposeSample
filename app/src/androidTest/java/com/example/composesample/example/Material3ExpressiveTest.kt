package com.example.composesample.example

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingToolbarState
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberFloatingToolbarState
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.example.component.ui.material3.Rate
import com.example.composesample.presentation.example.component.ui.material3.HERO_COUNT
import com.example.composesample.presentation.example.component.ui.material3.HERO_SCROLL_MILLIS
import com.example.composesample.presentation.example.component.ui.material3.HeroProbe
import com.example.composesample.presentation.example.component.ui.material3.HeroReadPlace
import com.example.composesample.presentation.example.component.ui.material3.HeroStage
import com.example.composesample.presentation.example.component.ui.material3.SPLIT_HEIGHT_DP
import com.example.composesample.presentation.example.component.ui.material3.SPLIT_INNER_CORNER_DP
import com.example.composesample.presentation.example.component.ui.material3.SPLIT_STAGE_WIDTHS_DP
import com.example.composesample.presentation.example.component.ui.material3.SplitButtonCard
import com.example.composesample.presentation.example.component.ui.material3.SplitRow
import com.example.composesample.presentation.example.component.ui.material3.ToolbarStage
import com.example.composesample.presentation.example.component.ui.material3.WavyCase
import com.example.composesample.presentation.example.component.ui.material3.WavyLabState
import com.example.composesample.presentation.example.component.ui.material3.WavyProgressCard
import com.example.composesample.presentation.example.component.ui.material3.WavySubject
import com.example.composesample.presentation.example.component.ui.material3.compositionReadCostsMore
import com.example.composesample.presentation.example.component.ui.material3.expectedClock
import com.example.composesample.presentation.example.component.ui.material3.expectedDraws
import com.example.composesample.presentation.example.component.ui.material3.expectedHeroVisible
import com.example.composesample.presentation.example.component.ui.material3.heroMasksMatch
import com.example.composesample.presentation.example.component.ui.material3.layoutAllLarge
import com.example.composesample.presentation.example.component.ui.material3.leadingAbsorbed
import com.example.composesample.presentation.example.component.ui.material3.opticalOffsetDp
import com.example.composesample.presentation.example.component.ui.material3.sameHeights
import com.example.composesample.presentation.example.component.ui.material3.trailingKeptWidth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

/**
 * M3 1.5.0 Expressive 정식 컴포넌트(#188) — 예제 화면의 무대를 그대로 띄워 잰다.
 * given : Wavy 7줄 · SplitButton 무대 3폭 + 해제/checked 쌍 · 바닥 툴바 + 목록 30줄 · Hero 캐러셀 6칸
 * when : 시계를 멈춘 채 프레임을 넘기고 / 목록을 밀고 / 캐러셀을 넘긴다
 * then : 선형 Wavy 는 높이 0 이어도 시계가 돌고 원형은 멈춘다, trailing 폭은 그대로 · leading 이 준다,
 *        툴바는 부모 밖까지 나간다, 캐러셀 칸은 모두 큰 폭 · 컴포지션에서 읽으면 프레임마다 리컴포즈
 */
class Material3ExpressiveTest {

    @get:Rule
    val rule = createComposeRule()

    private fun dp(px: Float): Float = px / rule.density.density

    /** 잘리지 않은 자리(px) — getBoundsInRoot 는 스크롤 뷰포트 같은 조상 clip 으로 잘라 화면 밖 노드는 0 이 된다(폰 API 28 에서 걸림) */
    private data class Box(val left: Float, val width: Float, val height: Float) {
        val centerX: Float get() = left + width / 2
    }

    private fun box(tag: String): Box {
        val node = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        return Box(node.positionInRoot.x, node.size.width.toFloat(), node.size.height.toFloat())
    }

    @Test
    fun 선형_Wavy_는_파도_높이_0_이어도_시계가_돌고_원형은_멈춘다() {
        // 자동 진행을 켜 두면 테스트 환경의 InfiniteAnimationPolicy 가 무한 애니메이션을 취소한다 → setContent 전에 끈다
        rule.mainClock.autoAdvance = false
        var case by mutableStateOf(WavyCase.LinearMid)
        rule.setContent {
            key(case) { WavySubject(case) }
        }
        val observed = WavyCase.entries.associateWith { c ->
            case = c
            rule.mainClock.advanceTimeBy(1000)
            rule.waitForIdle()
            // 테스트 환경은 뷰 트리에 Recomposer 태그를 달지 않는다(앱은 창 루트에 단다) → 실행 중인 Recomposer 목록에서 본다
            var pending = false
            rule.runOnUiThread { pending = Recomposer.runningRecomposers.value.any { it.hasPendingWork } }
            pending
        }
        WavyCase.entries.forEach { c ->
            assertEquals("$c 시계 $observed", expectedClock(c) == Rate.Continuous, observed.getValue(c))
        }
    }

    @Test
    fun 값이_그대로면_파도가_움직이는_줄만_그림이_바뀐다() {
        rule.mainClock.autoAdvance = false
        var case by mutableStateOf(WavyCase.LinearMid)
        rule.setContent {
            key(case) {
                Box(Modifier.testTag("subject").background(Color.White).padding(8.dp)) { WavySubject(case) }
            }
        }
        val moved = WavyCase.entries.associateWith { c ->
            case = c
            rule.mainClock.advanceTimeBy(1000)
            val a = capture()
            rule.mainClock.advanceTimeBy(250) // 기본 파도 속도 = 1초에 한 파장 → 1/4 파장
            val b = capture()
            differingPixels(a, b)
        }
        WavyCase.entries.forEach { c ->
            val expected = expectedDraws(c) == Rate.Continuous
            assertEquals("$c 바뀐 픽셀 $moved", expected, moved.getValue(c) > 20)
        }
    }

    private fun capture(): ImageBitmap = rule.onNodeWithTag("subject").captureToImage()

    private fun differingPixels(a: ImageBitmap, b: ImageBitmap): Int {
        val pa = a.toPixelMap()
        val pb = b.toPixelMap()
        if (pa.width != pb.width || pa.height != pb.height) return Int.MAX_VALUE
        var n = 0
        for (y in 0 until pa.height) for (x in 0 until pa.width) {
            val ca = pa[x, y]
            val cb = pb[x, y]
            if (abs(ca.red - cb.red) + abs(ca.green - cb.green) + abs(ca.blue - cb.blue) > 0.1f) n++
        }
        return n
    }

    @Test
    fun 진행값을_바꿔도_미리보기는_리컴포즈되지_않는다() {
        val lab = WavyLabState()
        rule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) { WavyProgressCard(lab, onMeasure = {}) }
        }
        listOf("5%", "97%", "30%", "100%").forEach { rule.onNodeWithText(it).performClick() }
        rule.waitForIdle()
        rule.onNodeWithText("진행값 바꾼 횟수 4 · 미리보기 리컴포지션 0회").assertExists()
    }

    @Test
    fun 스플릿은_trailing_폭을_지키고_좁으면_leading_이_줄며_높이를_맞춘다() {
        rule.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) { SplitButtonCard() }
        }
        rule.waitForIdle()
        val rows = SPLIT_STAGE_WIDTHS_DP.map { w ->
            val lead = box("lead-$w")
            val trail = box("trail-$w")
            SplitRow(w, dp(lead.width), dp(trail.width), dp(lead.height), dp(trail.height))
        }
        assertTrue("trailing $rows", trailingKeptWidth(rows))
        assertTrue("leading $rows", leadingAbsorbed(rows))
        assertTrue("높이 $rows", sameHeights(rows))

        // 광학 중앙 — 해제는 0.11 × (4 − 20) = −1.76dp, checked 는 0 (배치가 px 반올림이라 ±0.6dp)
        val full = SPLIT_HEIGHT_DP / 2
        listOf(false to opticalOffsetDp(SPLIT_INNER_CORNER_DP, full, 12f, 12f), true to 0f).forEach { (checked, expected) ->
            val tag = if (checked) "checked" else "unchecked"
            val surface = box("surface-$tag")
            val icon = box("icon-$tag")
            assertEquals("$tag 아이콘 중심 − 버튼 중심 (아이콘 $icon · 버튼 $surface)", expected, dp(icon.centerX - surface.centerX), 0.6f)
        }
    }

    @Test
    fun 툴바는_목록이_민_만큼_숨고_부모_밖으로_나가며_반_넘게_내려야_돌아온다() {
        lateinit var state: FloatingToolbarState
        var clip by mutableStateOf(false)
        rule.setContent {
            state = rememberFloatingToolbarState()
            Column {
                ToolbarStage(expanded = true, onToggle = {}, clip = clip, state = state)
                Text("아래 내용", Modifier.testTag("below").height(60.dp))
            }
        }
        rule.waitForIdle()
        val stage = rule.onNodeWithTag("toolbarStage").getBoundsInRoot()
        // 잘리지 않은 자리 — getBoundsInRoot 는 조상 clip 으로 잘라 다 숨으면 빈 사각형이 된다
        fun editTopPx(): Float = rule.onNodeWithContentDescription("편집").fetchSemanticsNode().positionInRoot.y
        val top0 = editTopPx()
        assertEquals(0f, state.offset, 0f)

        rule.onNodeWithTag("toolbarList").performTouchInput { swipeUp(startY = bottom - 20f, endY = top + 20f, durationMillis = 600) }
        rule.waitForIdle()
        // 손을 떼면 다 숨기거나 다 보이게 맞춘다 — 한 번 크게 밀면 다 숨긴다
        assertEquals("다 숨음", state.offsetLimit, state.offset, 1f)
        val top1 = editTopPx()
        assertEquals("버튼이 offset 만큼 내려간다", -state.offset, top1 - top0, 1.5f)
        assertTrue("부모 아래 끝 밖 ${dp(top1)} vs ${stage.bottom}", dp(top1) >= stage.bottom.value - 0.5f)
        // clip 이 없으면 나간 툴바가 상자 아래 내용과 겹친다
        val edit = rule.onNodeWithContentDescription("편집").getBoundsInRoot()
        val below = rule.onNodeWithTag("below").getBoundsInRoot()
        assertTrue("clip 없이 겹침 $edit / $below", edit.top < below.bottom && edit.bottom > below.top)
        // clip 을 켜면 같은 자리라도 보이는 영역이 없다
        clip = true
        rule.waitForIdle()
        val clipped = rule.onNodeWithContentDescription("편집").getBoundsInRoot()
        assertTrue("clip 하면 잘림 $clipped", (clipped.right - clipped.left).value * (clipped.bottom - clipped.top).value == 0f)

        // 조금(60px) 내리면 반도 안 나와 다시 숨는다 — 손을 떼면 collapsedFraction 0.5 를 기준으로 다 숨기거나 다 보인다
        rule.onNodeWithTag("toolbarList").performTouchInput { swipeDown(startY = centerY - 30f, endY = centerY + 30f, durationMillis = 300) }
        rule.waitForIdle()
        assertEquals("조금 내리면 다시 숨음", state.offsetLimit, state.offset, 1f)
        // 숨는 거리의 반을 넘게 내리면 나온다
        rule.onNodeWithTag("toolbarList").performTouchInput { swipeDown(startY = top + 20f, endY = bottom - 20f, durationMillis = 600) }
        rule.waitForIdle()
        assertEquals("크게 내리면 나온다", 0f, state.offset, 1f)
        assertEquals(top0, editTopPx(), 1.5f)
    }

    @Test
    fun 히어로_칸은_모두_큰_칸_폭으로_배치되고_선택_칸만_큰_마스크() {
        val probe = HeroProbe()
        lateinit var state: CarouselState
        lateinit var scope: CoroutineScope
        rule.setContent {
            state = rememberCarouselState { HERO_COUNT }
            scope = rememberCoroutineScope()
            Box(Modifier.width(360.dp)) { HeroStage(state, HeroReadPlace.Draw, probe) }
        }
        rule.waitForIdle()
        listOf(0, 2, HERO_COUNT - 1).forEach { selected ->
            rule.runOnUiThread { scope.launch { state.scrollToItem(selected) } }
            rule.waitForIdle()
            val visible = expectedHeroVisible(selected, HERO_COUNT)
            val items = probe.snapshot().filter { it.index in visible }
            assertEquals("선택 $selected 보이는 칸", visible, items.map { it.index })
            assertTrue("배치 폭 $items", layoutAllLarge(items))
            assertTrue("마스크 $items", heroMasksMatch(items, selected, HERO_COUNT))
        }
    }

    @Test
    fun 칸에서_drawInfo_를_컴포지션에서_읽으면_넘기는_프레임마다_리컴포즈된다() {
        val probe = HeroProbe()
        var place by mutableStateOf(HeroReadPlace.Composition)
        lateinit var state: CarouselState
        lateinit var scope: CoroutineScope
        rule.setContent {
            state = rememberCarouselState { HERO_COUNT }
            scope = rememberCoroutineScope()
            Box(Modifier.width(360.dp)) { HeroStage(state, place, probe) }
        }
        val counts = HeroReadPlace.entries.associateWith { p ->
            place = p
            rule.runOnUiThread { scope.launch { state.scrollToItem(0) } }
            rule.waitForIdle()
            probe.compositions = 0
            rule.runOnUiThread { scope.launch { state.animateScrollToItem(1, tween(HERO_SCROLL_MILLIS, easing = LinearEasing)) } }
            rule.waitForIdle()
            probe.compositions
        }
        android.util.Log.i("M3Expressive", "hero compositions $counts")
        assertTrue("칸 컴포지션 $counts", compositionReadCostsMore(counts.getValue(HeroReadPlace.Composition), counts.getValue(HeroReadPlace.Draw)))
        assertTrue("1.5초 넘기기 · 프레임 16ms — 컴포지션에서 읽으면 수십 번 $counts", counts.getValue(HeroReadPlace.Composition) >= 30)
    }
}
