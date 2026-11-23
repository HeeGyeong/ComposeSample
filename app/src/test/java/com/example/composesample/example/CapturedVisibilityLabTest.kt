package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.visibility.BenchVariant
import com.example.composesample.presentation.example.component.ui.visibility.CloseOrder
import com.example.composesample.presentation.example.component.ui.visibility.ExitResult
import com.example.composesample.presentation.example.component.ui.visibility.ExitTrace
import com.example.composesample.presentation.example.component.ui.visibility.ExitVariant
import com.example.composesample.presentation.example.component.ui.visibility.ExitWindow
import com.example.composesample.presentation.example.component.ui.visibility.FrameRate
import com.example.composesample.presentation.example.component.ui.visibility.FrameSample
import com.example.composesample.presentation.example.component.ui.visibility.LiveAction
import com.example.composesample.presentation.example.component.ui.visibility.LiveResult
import com.example.composesample.presentation.example.component.ui.visibility.LiveTrace
import com.example.composesample.presentation.example.component.ui.visibility.LiveVariant
import com.example.composesample.presentation.example.component.ui.visibility.Span
import com.example.composesample.presentation.example.component.ui.visibility.classifySpan
import com.example.composesample.presentation.example.component.ui.visibility.expectedDrawRate
import com.example.composesample.presentation.example.component.ui.visibility.expectedKept
import com.example.composesample.presentation.example.component.ui.visibility.expectedReleased
import com.example.composesample.presentation.example.component.ui.visibility.expectedShown
import com.example.composesample.presentation.example.component.ui.visibility.formatMillis
import com.example.composesample.presentation.example.component.ui.visibility.formatPercent
import com.example.composesample.presentation.example.component.ui.visibility.median
import com.example.composesample.presentation.example.component.ui.visibility.percentFaster
import com.example.composesample.presentation.example.component.ui.visibility.summarizeFrames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * CapturedAnimatedVisibility 예제(#184)의 기대 표 · 판정 · 통계.
 * 패널 높이 · 내용 해제 · 다시 열기의 실제 동작은 UI 노드가 있어야 해서 계측 테스트(CapturedAnimatedVisibilityTest)가 본다.
 */
class CapturedVisibilityLabTest {

    private val ms = 1_000_000L

    @Test
    fun 퇴장_시간에_견준_범주는_30퍼센트_아래_바로_70퍼센트_이상_끝까지() {
        assertEquals(Span.Instant, classifySpan(0, 800))
        assertEquals(Span.Instant, classifySpan(239, 800))
        assertNull(classifySpan(240, 800))
        assertNull(classifySpan(559, 800))
        assertEquals(Span.Full, classifySpan(560, 800))
        assertEquals(Span.Full, classifySpan(1_150, 800))
    }

    @Test
    fun 기대_표_선택을_먼저_비우면_Captured_도_바로_사라진다() {
        assertEquals(Span.Instant, expectedShown(ExitVariant.AnimatedNull, CloseOrder.Together))
        assertEquals(Span.Full, expectedShown(ExitVariant.AnimatedHold, CloseOrder.Together))
        assertEquals(Span.Full, expectedShown(ExitVariant.Captured, CloseOrder.Together))
        assertEquals(Span.Instant, expectedShown(ExitVariant.AnimatedNull, CloseOrder.SelectionFirst))
        assertEquals(Span.Full, expectedShown(ExitVariant.AnimatedHold, CloseOrder.SelectionFirst))
        assertEquals(Span.Instant, expectedShown(ExitVariant.Captured, CloseOrder.SelectionFirst))
        // 내용 해제는 닫는 순서와 무관 — AnimatedVisibility 는 퇴장 끝, Captured 는 그 자리
        assertEquals(Span.Full, expectedReleased(ExitVariant.AnimatedNull))
        assertEquals(Span.Full, expectedReleased(ExitVariant.AnimatedHold))
        assertEquals(Span.Instant, expectedReleased(ExitVariant.Captured))
    }

    @Test
    fun 기록은_닫은_뒤만_세고_처음_0_이후의_높이는_버린다() {
        val trace = ExitTrace()
        trace.onHeight(300, 1 * ms) // begin 전 — 무시
        trace.onContentDisposed(2 * ms) // begin 전 — 무시
        trace.begin(10 * ms)
        trace.onHide(10 * ms)
        trace.onContentDisposed(27 * ms) // Captured: 첫 프레임에 해제
        listOf(280, 240, 180, 90, 20).forEachIndexed { index, height -> trace.onHeight(height, (30 + index * 160) * ms) }
        trace.onHeight(0, 815 * ms)
        trace.onHeight(120, 900 * ms) // 다시 연 뒤 — 무시
        trace.onContentDisposed(950 * ms) // 두 번째 해제 — 무시
        val result = trace.finish(ExitVariant.Captured, CloseOrder.Together, 800)
        assertEquals(805L, result.shownMillis)
        assertEquals(5, result.shrinkFrames)
        assertEquals(17L, result.releasedMillis)
        assertTrue(result.matches)
    }

    @Test
    fun 늦은_visible_이면_해제는_visible_을_내린_때부터_잰다() {
        val trace = ExitTrace()
        trace.begin(0)
        trace.onHeight(0, 20 * ms) // 선택이 비자마자 높이 0
        trace.onHide(150 * ms)
        trace.onContentDisposed(170 * ms)
        val captured = trace.finish(ExitVariant.Captured, CloseOrder.SelectionFirst, 800)
        assertEquals(20L, captured.shownMillis)
        assertEquals(0, captured.shrinkFrames)
        assertEquals(20L, captured.releasedMillis)
        assertTrue(captured.matches)
        // 같은 숫자를 '같은 프레임에 닫기' 로 읽으면 Captured 는 끝까지 나가야 했다 → 어긋남
        assertFalse(captured.copy(order = CloseOrder.Together).matches)
    }

    @Test
    fun 높이가_끝내_0_이_안_되거나_해제가_없으면_어긋남() {
        val trace = ExitTrace()
        trace.begin(0)
        trace.onHeight(200, 16 * ms)
        val result = trace.finish(ExitVariant.AnimatedHold, CloseOrder.Together, 800)
        assertNull(result.shownMillis)
        assertNull(result.releasedMillis)
        assertFalse(result.matches)
        // 애매한 구간(30~70%)도 어긋남
        val vague = ExitResult(ExitVariant.AnimatedHold, CloseOrder.Together, 800, 400, 10, 805)
        assertNull(vague.shownSpan)
        assertFalse(vague.matches)
    }

    @Test
    fun 나가는_동안_그리기와_다시_연_뒤_번호로_판정한다() {
        assertEquals(FrameRate.Continuous, expectedDrawRate(LiveVariant.Animated))
        assertEquals(FrameRate.None, expectedDrawRate(LiveVariant.Captured))
        assertTrue(expectedKept(LiveVariant.Animated))
        assertFalse(expectedKept(LiveVariant.Captured))

        // 60 Hz · 800ms 창 = VSync 48 → 12 이상이면 계속
        val animated = LiveResult(LiveVariant.Animated, LiveAction.CloseReopen, 40, 800, 60f, 1, 1)
        val captured = LiveResult(LiveVariant.Captured, LiveAction.CloseReopen, 0, 800, 60f, 1, 2)
        assertTrue(animated.matches)
        assertTrue(captured.matches)
        assertEquals(true, animated.kept)
        assertEquals(false, captured.kept)
        // Captured 가 나가는 동안 한 번이라도 그렸거나, AnimatedVisibility 가 번호를 새로 받았다면 어긋남
        assertFalse(captured.copy(exitDraws = 1).matches)
        assertFalse(animated.copy(serialAfter = 2).matches)
        // 닫기만 하면 번호는 보지 않는다
        val closeOnly = LiveResult(LiveVariant.Captured, LiveAction.Close, 0, 2_000, 60f, 3, null)
        assertNull(closeOnly.kept)
        assertTrue(closeOnly.matches)
    }

    @Test
    fun 내용_번호는_새로_컴포즈될_때마다_하나씩() {
        val trace = LiveTrace()
        assertEquals(1, trace.newSerial())
        assertEquals(2, trace.newSerial())
        repeat(3) { trace.onDraw() }
        assertEquals(3, trace.draws)
    }

    @Test
    fun 중앙값() {
        assertNull(median(emptyList()))
        assertEquals(5L, median(listOf(9, 1, 5)))
        assertEquals(4L, median(listOf(9, 1, 3, 5)))
    }

    @Test
    fun 창_안_프레임을_방법별로_모으고_첫_프레임은_따로() {
        val windows = listOf(
            ExitWindow(BenchVariant.Animated, 1_000 * ms, 1_800 * ms),
            ExitWindow(BenchVariant.Captured, 3_000 * ms, 3_800 * ms)
        )
        val samples = listOf(
            FrameSample(990 * ms, 99 * ms, 99 * ms), // 창 앞 — 버림
            FrameSample(1_016 * ms, 20 * ms, 9 * ms), // Animated 첫 프레임
            FrameSample(1_033 * ms, 10 * ms, 4 * ms),
            FrameSample(1_050 * ms, 12 * ms, 6 * ms),
            FrameSample(1_800 * ms, 99 * ms, 99 * ms), // 창 끝(제외)
            FrameSample(3_033 * ms, 8 * ms, 3 * ms),
            FrameSample(3_016 * ms, 30 * ms, 14 * ms) // 순서가 섞여 와도 VSync 로 첫 프레임을 고른다
        )
        val summary = summarizeFrames(samples, windows)
        val animated = summary.getValue(BenchVariant.Animated)
        assertEquals(1, animated.windows)
        assertEquals(3, animated.frames)
        assertEquals(11 * ms, animated.medianTotalNanos)
        assertEquals(5 * ms, animated.medianUiNanos)
        assertEquals(20 * ms, animated.firstFrameMedianTotalNanos)
        val captured = summary.getValue(BenchVariant.Captured)
        assertEquals(2, captured.frames)
        assertEquals(8 * ms, captured.medianTotalNanos)
        assertEquals(30 * ms, captured.firstFrameMedianTotalNanos)
        assertEquals(14 * ms, captured.firstFrameMedianUiNanos)
    }

    @Test
    fun 빠른_비율과_표시_형식() {
        assertEquals(7.5, percentFaster(1_000, 925)!!, 1e-9)
        assertEquals(-10.0, percentFaster(1_000, 1_100)!!, 1e-9)
        assertNull(percentFaster(null, 1))
        assertNull(percentFaster(0, 1))
        assertEquals("1.23", formatMillis(1_239_999))
        assertEquals("0.05", formatMillis(50_000))
        assertEquals("-", formatMillis(null))
        assertEquals("7.5", formatPercent(7.46))
        assertEquals("-3.0", formatPercent(-2.96))
        assertEquals("0.0", formatPercent(-0.04))
        assertEquals("-", formatPercent(null))
    }
}
