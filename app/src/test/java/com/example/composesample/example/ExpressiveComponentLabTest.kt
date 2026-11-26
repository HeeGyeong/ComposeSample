package com.example.composesample.example

import androidx.compose.material3.WavyProgressIndicatorDefaults
import com.example.composesample.presentation.example.component.ui.material3.Rate
import com.example.composesample.presentation.example.component.ui.material3.HeroItem
import com.example.composesample.presentation.example.component.ui.material3.HeroProbe
import com.example.composesample.presentation.example.component.ui.material3.SplitRow
import com.example.composesample.presentation.example.component.ui.material3.WavyCase
import com.example.composesample.presentation.example.component.ui.material3.WavyRun
import com.example.composesample.presentation.example.component.ui.material3.classifyFrames
import com.example.composesample.presentation.example.component.ui.material3.compositionReadCostsMore
import com.example.composesample.presentation.example.component.ui.material3.defaultWavyAmplitude
import com.example.composesample.presentation.example.component.ui.material3.expectedClock
import com.example.composesample.presentation.example.component.ui.material3.expectedDraws
import com.example.composesample.presentation.example.component.ui.material3.expectedHeroVisible
import com.example.composesample.presentation.example.component.ui.material3.heroMasksMatch
import com.example.composesample.presentation.example.component.ui.material3.layoutAllLarge
import com.example.composesample.presentation.example.component.ui.material3.leadingAbsorbed
import com.example.composesample.presentation.example.component.ui.material3.oneDecimal
import com.example.composesample.presentation.example.component.ui.material3.opticalOffsetDp
import com.example.composesample.presentation.example.component.ui.material3.sameHeights
import com.example.composesample.presentation.example.component.ui.material3.trailingKeptWidth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * M3 1.5.0 Expressive 보강(#188)의 기대 · 판정 · 식.
 * 실제 그리기 · 시계 · 배치는 계측 테스트(Material3ExpressiveTest)가 본다.
 */
class ExpressiveComponentLabTest {

    @Test
    fun 기본_진폭_식은_라이브러리_람다와_같다() {
        val library = WavyProgressIndicatorDefaults.indicatorAmplitude
        listOf(0f, 0.05f, 0.1f, 0.1001f, 0.5f, 0.9499f, 0.95f, 0.97f, 1f).forEach { p ->
            assertEquals("progress $p", library(p), defaultWavyAmplitude(p), 0f)
        }
        assertEquals(0f, defaultWavyAmplitude(0.1f), 0f)
        assertEquals(1f, defaultWavyAmplitude(0.11f), 0f)
        assertEquals(0f, defaultWavyAmplitude(0.95f), 0f)
    }

    @Test
    fun 선형은_높이_0_이어도_시계가_돌고_원형은_멈춘다() {
        assertEquals(Rate.Continuous, expectedDraws(WavyCase.LinearMid))
        assertEquals(Rate.Continuous, expectedClock(WavyCase.LinearMid))
        listOf(WavyCase.LinearDone, WavyCase.LinearEarly).forEach {
            assertEquals("$it 그리기", Rate.None, expectedDraws(it))
            assertEquals("$it 시계", Rate.Continuous, expectedClock(it))
        }
        assertEquals(Rate.None, expectedDraws(WavyCase.LinearStill))
        assertEquals(Rate.None, expectedClock(WavyCase.LinearStill))
        assertEquals(Rate.Continuous, expectedClock(WavyCase.CircularMid))
        assertEquals(Rate.None, expectedDraws(WavyCase.CircularDone))
        assertEquals(Rate.None, expectedClock(WavyCase.CircularDone))
        assertEquals(Rate.None, expectedClock(WavyCase.Classic))
        // 그리기는 0 인데 시계만 도는 줄은 선형 끝 두 줄뿐
        val clockOnly = WavyCase.entries.filter { expectedDraws(it) == Rate.None && expectedClock(it) == Rate.Continuous }
        assertEquals(listOf(WavyCase.LinearEarly, WavyCase.LinearDone), clockOnly)
    }

    @Test
    fun 프레임_범주() {
        // 2초 창 → 초당 3 = 6 이상이면 계속
        assertEquals(Rate.None, classifyFrames(0, 2000))
        assertEquals(Rate.Continuous, classifyFrames(6, 2000))
        assertNull(classifyFrames(5, 2000))
        // 선형 100% — 그린 프레임 0 · 콜백 계속
        val run = WavyRun(callbacks = 40, rendered = 0, windowMillis = 2000)
        assertTrue(run.matches(WavyCase.LinearDone))
        assertFalse(run.matches(WavyCase.CircularDone))
        assertTrue(WavyRun(callbacks = 0, rendered = 0, windowMillis = 2000).matches(WavyCase.CircularDone))
    }

    @Test
    fun 광학_중앙_보정은_모서리_차의_0_11배() {
        assertEquals(-1.76f, opticalOffsetDp(4f, 20f, 12f, 12f), 0.001f)
        assertEquals(0f, opticalOffsetDp(20f, 20f, 12f, 12f), 0f)
        assertEquals(1.76f, opticalOffsetDp(20f, 4f, 12f, 12f), 0.001f)
        // 패딩 범위로 제한
        assertEquals(-1f, opticalOffsetDp(0f, 100f, 1f, 1f), 0f)
    }

    @Test
    fun 스플릿_배치_판정() {
        val rows = listOf(
            SplitRow(320, 150f, 48f, 40f, 40f),
            SplitRow(220, 150f, 48f, 40f, 40f),
            SplitRow(150, 92f, 48.5f, 58f, 58f)
        )
        assertTrue(trailingKeptWidth(rows))
        assertTrue(leadingAbsorbed(rows))
        assertTrue(sameHeights(rows))
        assertFalse("넓은 무대에서 줄지 않으면", leadingAbsorbed(rows.map { it.copy(leadingWidthDp = 150f) }))
        assertFalse(trailingKeptWidth(rows + SplitRow(100, 50f, 40f, 58f, 58f)))
        assertFalse(sameHeights(listOf(SplitRow(150, 92f, 48f, 58f, 40f))))
    }

    @Test
    fun 히어로_보이는_칸() {
        assertEquals(listOf(0, 1, 2), expectedHeroVisible(0, 6))
        assertEquals(listOf(1, 2, 3), expectedHeroVisible(2, 6))
        assertEquals(listOf(3, 4, 5), expectedHeroVisible(5, 6))
        assertEquals(listOf(0, 1), expectedHeroVisible(1, 2))
    }

    @Test
    fun 히어로_마스크_판정() {
        // 큰 칸 500px · 작은 칸 범위 80~112px
        val items = listOf(
            HeroItem(0, 500, 100f, 80f, 500f),
            HeroItem(1, 500, 500f, 80f, 500f),
            HeroItem(2, 500, 100f, 80f, 500f),
            HeroItem(3, 500, 400f, 80f, 500f) // 화면 밖 칸의 남은 값 — 판정에서 빠진다
        )
        assertTrue(layoutAllLarge(items))
        assertTrue(heroMasksMatch(items, 1, 6))
        assertFalse("선택이 2 면 3 이 작은 칸이어야", heroMasksMatch(items, 2, 6))
        assertFalse(layoutAllLarge(listOf(HeroItem(0, 100, 100f, 80f, 500f))))
        assertTrue(compositionReadCostsMore(48, 2))
        assertTrue("느린 에뮬 — 몇 프레임뿐이어도", compositionReadCostsMore(6, 0))
        assertFalse(compositionReadCostsMore(2, 0))
        assertFalse(compositionReadCostsMore(20, 10))
    }

    @Test
    fun 히어로_프로브는_배치와_그리기를_모두_거친_칸만() {
        val probe = HeroProbe()
        probe.placed(0, 500)
        probe.drawn(0, 500f, 80f, 500f)
        probe.drawn(1, 100f, 80f, 500f) // 배치 기록 없음
        probe.placed(2, 500)
        assertEquals(listOf(HeroItem(0, 500, 500f, 80f, 500f)), probe.snapshot())
    }

    @Test
    fun 숫자_표기() {
        assertEquals("-1.8", oneDecimal(-1.76f))
        assertEquals("0.0", oneDecimal(0.04f))
        assertEquals("48.5", oneDecimal(48.5f))
    }
}
