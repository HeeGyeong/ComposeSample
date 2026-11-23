package com.example.composesample.example

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.composesample.presentation.example.component.ui.visibility.ENTER_MILLIS
import com.example.composesample.presentation.example.component.ui.visibility.ExitPanel
import com.example.composesample.presentation.example.component.ui.visibility.ExitVariant
import com.example.composesample.presentation.example.component.ui.visibility.LATE_VISIBLE_MILLIS
import com.example.composesample.presentation.example.component.ui.visibility.LivePanel
import com.example.composesample.presentation.example.component.ui.visibility.LiveTrace
import com.example.composesample.presentation.example.component.ui.visibility.LiveVariant
import com.example.composesample.presentation.example.component.ui.visibility.REOPEN_AFTER_MILLIS
import com.example.composesample.presentation.example.component.ui.visibility.Track
import com.example.composesample.presentation.example.component.ui.visibility.sampleTracks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * CapturedAnimatedVisibility(#184) — 예제 화면의 패널을 그대로 띄우고 시계를 멈춘 채 프레임 단위로 본다.
 * given : 같은 선택 · visible 을 읽는 세 패널(① AnimatedVisibility + null · ② 마지막 값 붙들기 · ③ Captured)
 * when : 같은 프레임에 닫기 / 선택 먼저 비우고 늦게 닫기 / 나가는 도중 다시 열기
 * then : ③ 만 내용(의미 노드)을 바로 놓고도 그림으로 끝까지 나가고, 늦은 visible 이면 ③ 도 빈 칸, 다시 열면 ③ 의 내용은 처음부터
 */
class CapturedAnimatedVisibilityTest {

    @get:Rule
    val rule = createComposeRule()

    private val track = sampleTracks[0]

    private fun height(variant: ExitVariant): Int =
        rule.onNodeWithTag("exitPanel-${variant.name}").fetchSemanticsNode().size.height

    private fun titles(): Int = rule.onAllNodesWithText(track.title).fetchSemanticsNodes().size

    /** 표지 색(0xFF5C6BC0)이 흰 바탕과 섞인 픽셀 — 파랑이 빨강보다 30 이상 큰 점. 카드 바탕(EEF0F8)은 10 차이라 안 걸린다 */
    private fun coverPixels(variant: ExitVariant): Int {
        val pixels = rule.onNodeWithTag("exitPanel-${variant.name}").captureToImage().toPixelMap()
        var count = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            val c = pixels[x, y]
            if ((c.blue - c.red) * 255 > 30 && c.alpha > 0.5f) count++
        }
        return count
    }

    private fun setPanels(visible: () -> Boolean, selected: () -> Track?) {
        rule.setContent {
            Column {
                ExitVariant.entries.forEach { variant ->
                    ExitPanel(variant = variant, visible = visible(), track = selected(), exitMillis = EXIT, trace = null)
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun 같은_프레임에_닫으면_Captured_만_내용을_바로_놓고도_끝까지_나간다() {
        var visible by mutableStateOf(true)
        var selected by mutableStateOf<Track?>(track)
        setPanels({ visible }, { selected })
        val full = ExitVariant.entries.associateWith { height(it) }
        assertTrue("열린 높이 $full", full.values.all { it > 0 })
        assertEquals(3, titles())
        val openCover = coverPixels(ExitVariant.Captured)
        assertTrue("열린 ③ 의 표지 픽셀 $openCover", openCover > 0)

        rule.mainClock.autoAdvance = false
        selected = null
        visible = false
        rule.mainClock.advanceTimeByFrame()
        // 닫은 첫 프레임 — ③ 은 내용이 이미 컴포지션에 없는데도 기록해 둔 레이어로 표지를 그린다(페이드 시작이라 거의 불투명)
        val firstFrameCover = coverPixels(ExitVariant.Captured)
        assertTrue("③ 첫 프레임 표지 $firstFrameCover / 열린 $openCover", firstFrameCover * 10 >= openCover * 8)
        assertEquals("① 은 닫는 프레임에 빈 내용 → 높이 0", 0, height(ExitVariant.AnimatedNull))
        assertEquals("② 는 붙든 값으로 아직 그대로", full[ExitVariant.AnimatedHold], height(ExitVariant.AnimatedHold))
        assertEquals("③ 은 마지막 크기 그대로", full[ExitVariant.Captured], height(ExitVariant.Captured))
        assertEquals("내용(제목 노드)이 남은 것은 ② 뿐", 1, titles())

        rule.mainClock.advanceTimeBy(EXIT / 2L)
        val hold = height(ExitVariant.AnimatedHold)
        val captured = height(ExitVariant.Captured)
        assertTrue("② 반쯤 접힘 $hold / ${full[ExitVariant.AnimatedHold]}", hold in 1 until full.getValue(ExitVariant.AnimatedHold))
        assertTrue("③ 반쯤 접힘 $captured / ${full[ExitVariant.Captured]}", captured in 1 until full.getValue(ExitVariant.Captured))
        assertEquals("② 와 ③ 은 같은 퇴장", hold, captured)
        assertEquals(1, titles())

        rule.mainClock.advanceTimeBy(EXIT.toLong())
        assertTrue("퇴장 끝 — 모두 0", ExitVariant.entries.all { height(it) == 0 })
        assertEquals(0, titles())
    }

    @Test
    fun 선택을_먼저_비우고_늦게_닫으면_Captured_도_빈_칸() {
        var visible by mutableStateOf(true)
        var selected by mutableStateOf<Track?>(track)
        setPanels({ visible }, { selected })
        val holdFull = height(ExitVariant.AnimatedHold)

        rule.mainClock.autoAdvance = false
        selected = null
        rule.mainClock.advanceTimeByFrame()
        assertEquals("선택이 비자마자 ③ 의 내용이 높이 0 으로 측정된다", 0, height(ExitVariant.Captured))
        rule.mainClock.advanceTimeBy(LATE_VISIBLE_MILLIS)
        visible = false
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(EXIT / 2L)
        assertEquals("③ 은 높이 0 을 들고 나간다", 0, height(ExitVariant.Captured))
        assertEquals("① 도 빈 칸", 0, height(ExitVariant.AnimatedNull))
        val hold = height(ExitVariant.AnimatedHold)
        assertTrue("② 만 붙든 값으로 접히는 중 $hold / $holdFull", hold in 1 until holdFull)
    }

    @Test
    fun 나가는_도중_다시_열면_AnimatedVisibility_는_이어지고_Captured_는_처음부터() {
        val traces = LiveVariant.entries.associateWith { LiveTrace() }
        var visible by mutableStateOf(true)
        rule.setContent {
            Column {
                LiveVariant.entries.forEach { variant ->
                    LivePanel(
                        variant = variant,
                        visible = visible,
                        trace = traces.getValue(variant),
                        exitMillis = 2_000,
                        showSpinner = false
                    )
                }
            }
        }
        rule.waitForIdle()
        assertEquals(2, rule.onAllNodesWithText("내용 #1").fetchSemanticsNodes().size)
        // 두 패널의 버튼을 한 번씩 — 내용 안 remember 상태
        rule.onAllNodesWithText("눌러 보기 0")[0].performClick()
        rule.onAllNodesWithText("눌러 보기 0")[0].performClick()
        rule.waitForIdle()
        assertEquals(2, rule.onAllNodesWithText("눌러 보기 1").fetchSemanticsNodes().size)

        rule.mainClock.autoAdvance = false
        visible = false
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(REOPEN_AFTER_MILLIS)
        assertEquals("나가는 동안 내용이 남은 것은 AnimatedVisibility 뿐", 1, rule.onAllNodesWithText("내용 #1").fetchSemanticsNodes().size)
        assertEquals(1, rule.onAllNodesWithText("눌러 보기 1").fetchSemanticsNodes().size)

        visible = true
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(ENTER_MILLIS + 100L)
        assertEquals("AnimatedVisibility 는 같은 내용", 1, traces.getValue(LiveVariant.Animated).currentSerial)
        assertEquals("Captured 는 새 내용", 2, traces.getValue(LiveVariant.Captured).currentSerial)
        assertEquals(1, rule.onAllNodesWithText("내용 #1").fetchSemanticsNodes().size)
        assertEquals(1, rule.onAllNodesWithText("내용 #2").fetchSemanticsNodes().size)
        assertEquals("AnimatedVisibility 의 누른 횟수는 그대로", 1, rule.onAllNodesWithText("눌러 보기 1").fetchSemanticsNodes().size)
        assertEquals("Captured 의 누른 횟수는 0 부터", 1, rule.onAllNodesWithText("눌러 보기 0").fetchSemanticsNodes().size)
    }

    private companion object {
        const val EXIT = 800
    }
}
