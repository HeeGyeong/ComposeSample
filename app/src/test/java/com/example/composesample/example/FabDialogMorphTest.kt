package com.example.composesample.example

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.composesample.presentation.example.component.ui.layout.animation.MORPH_SCRIM_MAX_ALPHA
import com.example.composesample.presentation.example.component.ui.layout.animation.MorphFadePhase
import com.example.composesample.presentation.example.component.ui.layout.animation.TextLayoutTally
import com.example.composesample.presentation.example.component.ui.layout.animation.describeLineCounts
import com.example.composesample.presentation.example.component.ui.layout.animation.morphContentAlpha
import com.example.composesample.presentation.example.component.ui.layout.animation.morphDialogRect
import com.example.composesample.presentation.example.component.ui.layout.animation.morphFabRect
import com.example.composesample.presentation.example.component.ui.layout.animation.morphFabRectInOverlay
import com.example.composesample.presentation.example.component.ui.layout.animation.morphFadePhase
import com.example.composesample.presentation.example.component.ui.layout.animation.morphFrame
import com.example.composesample.presentation.example.component.ui.layout.animation.morphHandoverError
import com.example.composesample.presentation.example.component.ui.layout.animation.morphIconAlpha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * FAB → 다이얼로그 모핑 예제의 계산 검증 — 화면이 쓰는 함수를 그대로 부른다.
 * given : FAB·다이얼로그 사각형(px) · 진행값 p · 창/박스 크기 · 박스의 창 위치
 * when : 한 프레임 계산 · 내용 페이드 구간 판정 · 다이얼로그 크기 제한 · 넘김 오차
 * then : 양 끝은 FAB/다이얼로그 값 그대로, 창을 나누면 0.33~0.55 는 빈 구간, 보정하면 넘김 오차 0
 */
class FabDialogMorphTest {

    private val fab = Rect(900f, 2000f, 1054f, 2154f)
    private val dialog = Rect(100f, 800f, 980f, 1570f)

    @Test
    fun `진행값 0 과 1 은 FAB 과 다이얼로그 값 그대로다`() {
        val start = morphFrame(0f, fab, dialog, splitFade = true)
        assertRect(fab, start.bounds)
        assertEquals(16f, start.cornerDp, 0.001f)
        assertEquals(1f, start.iconAlpha, 0f)
        assertEquals(0f, start.contentAlpha, 0f)
        assertEquals(0f, start.scrimAlpha, 0f)

        val end = morphFrame(1f, fab, dialog, splitFade = true)
        assertRect(dialog, end.bounds)
        assertEquals(28f, end.cornerDp, 0.001f)
        assertEquals(0f, end.iconAlpha, 0f)
        assertEquals(1f, end.contentAlpha, 0f)
        assertEquals(MORPH_SCRIM_MAX_ALPHA, end.scrimAlpha, 0f)
    }

    @Test
    fun `중간 프레임은 모든 값이 같은 진행값으로 선형 보간된다`() {
        val half = morphFrame(0.5f, fab, dialog, splitFade = true)
        assertRect(Rect(500f, 1400f, 1017f, 1862f), half.bounds)
        assertEquals(22f, half.cornerDp, 0.001f)
        assertEquals(0.5f, half.colorFraction, 0f)
        assertEquals(0.16f, half.scrimAlpha, 0.0001f)
        // 범위 밖 진행값은 양 끝으로 자른다(spring 이 넘쳐도 표면이 다이얼로그보다 커지지 않는다)
        assertRect(dialog, morphFrame(1.2f, fab, dialog, splitFade = true).bounds)
    }

    @Test
    fun `창을 나누면 아이콘은 3분의 1 까지 사라지고 내용은 0_55 부터 나타난다 — 사이는 빈 구간`() {
        assertEquals(0f, morphIconAlpha(1f / 3f, splitFade = true), 0.0001f)
        assertEquals(0.5f, morphIconAlpha(1f / 6f, splitFade = true), 0.0001f)
        assertEquals(0f, morphContentAlpha(0.55f, splitFade = true), 0.0001f)
        assertEquals(0.5f, morphContentAlpha(0.775f, splitFade = true), 0.0001f)

        assertEquals(MorphFadePhase.ICON_ONLY, phaseAt(0.2f, split = true))
        assertEquals(MorphFadePhase.NEITHER, phaseAt(0.34f, split = true))
        assertEquals(MorphFadePhase.NEITHER, phaseAt(0.55f, split = true))
        assertEquals(MorphFadePhase.CONTENT_ONLY, phaseAt(0.56f, split = true))
        // 0 < p < 1 어디에서도 둘이 함께 보이지 않는다
        (1 until 100).forEach { i -> assertNotEquals(MorphFadePhase.BOTH, phaseAt(i / 100f, split = true)) }
    }

    @Test
    fun `창을 나누지 않으면 진행 내내 아이콘과 내용이 겹친다`() {
        assertEquals(MorphFadePhase.BOTH, phaseAt(0.01f, split = false))
        assertEquals(MorphFadePhase.BOTH, phaseAt(0.5f, split = false))
        assertEquals(MorphFadePhase.BOTH, phaseAt(0.99f, split = false))
        assertEquals(0.5f, morphIconAlpha(0.5f, splitFade = false), 0f)
        assertEquals(0.5f, morphContentAlpha(0.5f, splitFade = false), 0f)
    }

    @Test
    fun `다이얼로그는 320x280dp 가운데이고 여백이 24dp 보다 좁으면 줄인다`() {
        // 넓은 창 — 그대로(밀도 2.75, 1080x2400)
        assertRect(Rect(100f, 815f, 980f, 1585f), morphDialogRect(1080f, 2400f, 2.75f))
        // 좁은 창 300dp — 너비 300 - 24×2 = 252dp
        assertEquals(252f, morphDialogRect(300f, 800f, 1f).width, 0.001f)
        // 낮은 상자 300dp(카드 미리보기) — 높이도 252dp 로(원문은 너비만 줄인다)
        assertEquals(252f, morphDialogRect(400f, 300f, 1f).height, 0.001f)
    }

    @Test
    fun `FAB 은 박스 아래 끝 모서리에서 16dp 안쪽 56dp 이고 RTL 이면 왼쪽이다`() {
        assertRect(Rect(256f, 656f, 368f, 768f), morphFabRect(400f, 800f, 2f, rtl = false))
        assertRect(Rect(32f, 656f, 144f, 768f), morphFabRect(400f, 800f, 2f, rtl = true))
    }

    @Test
    fun `박스의 창 위치를 더해야 넘김 오차가 0 이다`() {
        val boxInWindow = Offset(0f, 63f) // 상태 바 아래에서 시작하는 예제 Box
        val fabInBox = morphFabRect(1080f, 2253f, 2.75f, rtl = false)
        val treeFabOnScreen = boxInWindow + fabInBox.topLeft // 전체 화면 창 — 창 좌표 = 화면 좌표
        val overlayRoot = Offset.Zero // 창 원점에 고정한 Popup

        val corrected = morphFabRectInOverlay(fabInBox, boxInWindow, correct = true)
        assertEquals(Offset.Zero, morphHandoverError(treeFabOnScreen, overlayRoot, corrected.topLeft))

        val uncorrected = morphFabRectInOverlay(fabInBox, boxInWindow, correct = false)
        assertEquals(Offset(0f, -63f), morphHandoverError(treeFabOnScreen, overlayRoot, uncorrected.topLeft))
    }

    @Test
    fun `본문 배치 기록은 횟수와 연속 중복을 뺀 줄 수를 남긴다`() {
        val tally = TextLayoutTally()
        listOf(9, 9, 7, 3, 3).forEach(tally::record)
        assertEquals(5, tally.layouts)
        assertEquals(listOf(9, 7, 3), tally.lineCounts)
        tally.reset()
        assertEquals(0, tally.layouts)
        assertEquals(emptyList<Int>(), tally.lineCounts)

        assertEquals("-", describeLineCounts(emptyList()))
        assertEquals("3", describeLineCounts(listOf(3)))
        assertEquals("30→20→12→…→4→3 (8가지)", describeLineCounts(listOf(30, 20, 12, 9, 7, 5, 4, 3)))
    }

    private fun phaseAt(p: Float, split: Boolean): MorphFadePhase =
        morphFadePhase(morphIconAlpha(p, split), morphContentAlpha(p, split))

    private fun assertRect(expected: Rect, actual: Rect) {
        assertEquals("left", expected.left, actual.left, 0.01f)
        assertEquals("top", expected.top, actual.top, 0.01f)
        assertEquals("right", expected.right, actual.right, 0.01f)
        assertEquals("bottom", expected.bottom, actual.bottom, 0.01f)
    }
}
