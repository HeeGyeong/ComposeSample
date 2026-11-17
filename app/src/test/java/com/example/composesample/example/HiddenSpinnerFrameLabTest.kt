package com.example.composesample.example

import android.os.Trace
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import com.example.composesample.presentation.example.component.ui.visibility.FrameCase
import com.example.composesample.presentation.example.component.ui.visibility.FrameCounter
import com.example.composesample.presentation.example.component.ui.visibility.FrameRate
import com.example.composesample.presentation.example.component.ui.visibility.FrameWindowResult
import com.example.composesample.presentation.example.component.ui.visibility.HiddenSpinnerFrameLab
import com.example.composesample.presentation.example.component.ui.visibility.HideTechnique
import com.example.composesample.presentation.example.component.ui.visibility.caseOf
import com.example.composesample.presentation.example.component.ui.visibility.classifyRate
import com.example.composesample.presentation.example.component.ui.visibility.expectedRate
import com.example.composesample.presentation.example.component.ui.visibility.inWindow
import com.example.composesample.presentation.example.component.ui.visibility.isVsyncDispatch
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 숨긴 스피너 프레임 Lab(#177) — 기대 표 · 판정 · 로그 줄 판별과, 무한 트랜지션이 프레임을 요청하는 조건.
 * 그리기 무효화 · 창이 그린 프레임은 UI 노드가 있어야 해서 기기 실측으로만 확인한다.
 */
class HiddenSpinnerFrameLabTest {

    @Before
    fun mockTrace() {
        // runtime-android 의 컴포지션은 android.os.Trace 를 부른다 — 단위 테스트의 android.jar 는 구현이 없다
        mockkStatic(Trace::class)
        every { Trace.beginSection(any()) } returns Unit
        every { Trace.endSection() } returns Unit
    }

    @After
    fun unmockTrace() {
        unmockkStatic(Trace::class)
    }

    // ==================== 기대 표 ====================

    @Test
    fun `기대 — 보임과 alpha(0f) 는 셋 다 계속, 배치 안 함은 콜백만, 컴포지션에서 뺀 둘은 셋 다 0`() {
        val every = FrameRate.Continuous
        val none = FrameRate.None
        val table = mapOf(
            FrameCase.Shown to listOf(every, every, every),
            FrameCase.AlphaZero to listOf(every, every, every),
            FrameCase.NotPlaced to listOf(every, none, none),
            FrameCase.IfRemoved to listOf(none, none, none),
            FrameCase.AnimatedHidden to listOf(none, none, none)
        )
        FrameCase.entries.forEach { case ->
            assertEquals(case.label, table.getValue(case), FrameCounter.entries.map { expectedRate(case, it) })
        }
    }

    @Test
    fun `지금 상태 — 보이면 방법과 상관없이 기준 줄, 숨기면 방법마다 제 줄`() {
        HideTechnique.entries.forEach { assertEquals(FrameCase.Shown, caseOf(it, visible = true)) }
        assertEquals(FrameCase.AlphaZero, caseOf(HideTechnique.Alpha, visible = false))
        assertEquals(FrameCase.NotPlaced, caseOf(HideTechnique.NotPlaced, visible = false))
        assertEquals(FrameCase.IfRemoved, caseOf(HideTechnique.IfVisible, visible = false))
        assertEquals(FrameCase.AnimatedHidden, caseOf(HideTechnique.Animated, visible = false))
        // 숨김 줄은 제 방법을 기억한다 — '차례로 재기' 가 이 값으로 방법을 고른다
        FrameCase.entries.filter { !it.visible }.forEach { assertEquals(it, caseOf(it.technique, visible = false)) }
    }

    // ==================== 판정 ====================

    @Test
    fun `판정 — 0 은 정확히 0, 창 VSync 의 4분의 1 이상은 계속, 그 사이는 판정 불가`() {
        assertEquals(FrameRate.None, classifyRate(0, 5_000, 60f))
        assertEquals(FrameRate.Continuous, classifyRate(303, 5_000, 60f)) // 원문의 302~303
        assertEquals(FrameRate.Continuous, classifyRate(148, 5_000, 60f)) // swiftshader debug 실측 — VSync 의 절반
        assertEquals(FrameRate.Continuous, classifyRate(75, 5_000, 60f)) // 300 의 1/4
        assertNull(classifyRate(74, 5_000, 60f))
        assertNull(classifyRate(10, 5_000, 60f)) // 0.5초마다 깜빡이는 커서
        // 주사율에 비례 — 120 Hz 는 기준도 두 배
        assertNull(classifyRate(149, 5_000, 120f))
        assertEquals(FrameRate.Continuous, classifyRate(150, 5_000, 120f))
    }

    @Test
    fun `창 — 겨냥한 VSync 가 시작 이상 끝 미만인 프레임만`() {
        assertTrue(inWindow(100, 100, 200))
        assertTrue(inWindow(199, 100, 200))
        assertFalse(inWindow(99, 100, 200))
        assertFalse(inWindow(200, 100, 200))
    }

    @Test
    fun `결과 — 한 칸이라도 범주가 다르거나 세는 중 건드렸으면 예상과 같다고 하지 않는다`() {
        val alpha = FrameWindowResult(
            case = FrameCase.AlphaZero, callbacks = 301, rendered = 300, droppedReports = 0,
            spinnerDraws = 300, windowMillis = 5_000, refreshHz = 60f, touched = false
        )
        assertTrue(alpha.allMatch)
        assertFalse(alpha.copy(touched = true).allMatch)
        // 배치 안 함 줄에 그린 프레임이 있으면 그 칸만 틀린다
        val notPlaced = alpha.copy(case = FrameCase.NotPlaced, rendered = 300, spinnerDraws = 0)
        assertTrue(notPlaced.matches(FrameCounter.Callbacks))
        assertFalse(notPlaced.matches(FrameCounter.Rendered))
        assertTrue(notPlaced.matches(FrameCounter.SpinnerDraws))
        assertFalse(notPlaced.allMatch)
        assertTrue(notPlaced.copy(rendered = 0).allMatch)
    }

    // ==================== 로그 줄 ====================

    @Test
    fun `로그 줄 — VSync 로 시작한 Choreographer 디스패치만 프레임 콜백으로 센다`() {
        VSYNC_LINES.forEach { assertTrue(it, isVsyncDispatch(it)) }
        OTHER_LINES.forEach { assertFalse(it, isVsyncDispatch(it)) }
    }

    // ==================== 상태 홀더 ====================

    @Test
    fun `요청 — 지금 상태는 한 줄, 차례로는 다섯 줄을 표 순서대로`() {
        val lab = HiddenSpinnerFrameLab()
        lab.technique = HideTechnique.NotPlaced
        lab.visible = false
        lab.requestCurrent()
        assertEquals(1, lab.runToken)
        assertEquals(listOf(FrameCase.NotPlaced), lab.plan)
        lab.requestAll()
        assertEquals(2, lab.runToken)
        assertEquals(FrameCase.entries, lab.plan)
        // 측정 창 밖의 터치는 표시하지 않는다
        lab.onUserTouch()
        assertFalse(lab.touchedInWindow)
        lab.measuring = true
        lab.onUserTouch()
        assertTrue(lab.touchedInWindow)
    }

    // ==================== 무한 트랜지션이 프레임을 요청하는 조건 (실제 컴포지션) ====================

    @Test
    fun `읽는 곳이 없어도 컴포지션에 있는 무한 트랜지션은 매 틱 프레임을 요청하고 값을 쓴다 — 빼면 0`() {
        val show = mutableStateOf(true)
        val harness = FrameClockComposition {
            if (show.value) {
                // 값을 어디에서도 읽지 않는다 — 배치되지 않아 그려지지 않는 스피너와 같은 처지
                val transition = rememberInfiniteTransition(label = "spinner")
                transition.animateFloat(0f, 360f, infiniteRepeatable(tween(1_000)), label = "rotation")
            }
        }
        try {
            val composed = harness.tick(60)
            assertEquals(60, composed.requests)
            assertEquals(60, composed.framesWithWrites)

            show.value = false
            harness.step()
            val removed = harness.tick(60)
            assertEquals(0, removed.requests)
            assertEquals(0, removed.framesWithWrites)
        } finally {
            harness.dispose()
        }
    }

    private data class TickReport(val requests: Int, val framesWithWrites: Int)

    /**
     * Recomposer 의 프레임 루프 없이 컴포지션을 한 단계씩 진행하고, 효과(LaunchedEffect)가 기다리는 프레임 시계를 직접 민다.
     * 효과 컨텍스트에 Unconfined 를 주면 sendFrame 이 기다리던 코루틴을 그 자리에서 이어 가 다음 요청까지 등록한다 — 틱마다 결정적.
     * (Recomposer.effectCoroutineContext 의 프레임 시계는 Recomposer 가 붙인 BroadcastFrameClock)
     */
    private class FrameClockComposition(content: @Composable () -> Unit) {
        private val recomposer = Recomposer(Dispatchers.Unconfined)
        private val clock = recomposer.effectCoroutineContext[MonotonicFrameClock] as BroadcastFrameClock
        private val modified = mutableSetOf<Any>()
        private var writesThisFrame = 0
        private val handle = Snapshot.registerApplyObserver { changed, _ ->
            modified.addAll(changed)
            writesThisFrame += changed.size
        }
        private val composition = ControlledComposition(UnitApplier(), recomposer).apply { setContent(content) }
        private var frameTimeNanos = 0L

        init {
            // 첫 컴포지션 뒤 DisposableEffect 가 애니메이션을 등록하며 쓴 상태를 반영한다
            repeat(3) { step() }
        }

        /** 시계에 기다리는 코루틴이 있을 때만 프레임을 보낸다 — 그 수가 곧 프레임 요청 수 */
        fun tick(frames: Int): TickReport {
            var requests = 0
            var framesWithWrites = 0
            repeat(frames) {
                writesThisFrame = 0
                if (clock.hasAwaiters) {
                    requests++
                    frameTimeNanos += FRAME_NANOS
                    clock.sendFrame(frameTimeNanos)
                }
                step()
                if (writesThisFrame > 0) framesWithWrites++
            }
            return TickReport(requests, framesWithWrites)
        }

        fun step() {
            Snapshot.sendApplyNotifications()
            val changes = modified.toSet()
            modified.clear()
            if (changes.isEmpty()) return
            composition.recordModificationsOf(changes)
            val snapshot = Snapshot.takeMutableSnapshot(
                readObserver = { composition.recordReadOf(it) },
                writeObserver = { composition.recordWriteOf(it) }
            )
            val shouldApply = try {
                snapshot.enter { composition.recompose() }.also { snapshot.apply().check() }
            } finally {
                snapshot.dispose()
            }
            // Recomposer 처럼 recompose 가 true(적용할 변경 있음)일 때만 적용한다
            if (shouldApply) {
                composition.applyChanges()
                composition.applyLateChanges()
                composition.changesApplied()
            }
        }

        fun dispose() {
            handle.dispose()
            composition.dispose()
            recomposer.cancel()
        }
    }

    private class UnitApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }

    private companion object {
        const val FRAME_NANOS = 16_666_667L

        /** Pixel 4 API 30 에뮬에서 받은 줄(Looper.loop 가 메시지마다 Printer 에 넘기는 모양) */
        val VSYNC_LINES = listOf(
            ">>>>> Dispatching to Handler (android.view.Choreographer\$FrameHandler) {13b6827} " +
                "android.view.Choreographer\$FrameDisplayEventReceiver@5316ad4: 0"
        )

        val OTHER_LINES = listOf(
            // Compose 메인 디스패처의 Handler 쪽 — 같은 dispatch 가 요청한 Choreographer 콜백은 위 줄로 따로 온다
            ">>>>> Dispatching to Handler (android.os.Handler) {bd0ab79} " +
                "androidx.compose.ui.platform.AndroidUiDispatcher\$dispatchCallback\$1@4ae10be: 0",
            // 같은 메시지의 끝 줄은 세지 않는다
            "<<<<< Finished to Handler (android.view.Choreographer\$FrameHandler) {13b6827} " +
                "android.view.Choreographer\$FrameDisplayEventReceiver@5316ad4",
            ""
        )
    }
}
