package com.example.composesample.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.dp
import com.example.composesample.util.component.ValueSlider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * ValueSlider(DEPRECATION-113-01) 가 M3 1.5 에서 deprecated 된 Slider(value, onValueChange) 와 같게 움직이는지 —
 * 두 슬라이더에 같은 바깥 값 · 같은 입력을 주고 접근성 진행 정보(현재 값 · 범위)를 나란히 비교한다.
 * given : 바깥 상태 하나를 공유하는 옛 Slider 와 ValueSlider
 * when : 바깥에서 값 바꾸기 · 범위 바꾸기 · 사용자 입력(setProgress · 스와이프) · 입력을 무시하는 호출자
 * then : 매 단계 두 슬라이더의 진행 정보가 같고, 바깥 값이 정답이다(호출자가 무시하면 값이 돌아간다)
 */
class ValueSliderTest {

    @get:Rule
    val rule = createComposeRule()

    private fun SemanticsNodeInteraction.range(): ProgressBarRangeInfo =
        fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]

    private fun assertSame(expected: Float, expectedRange: ClosedFloatingPointRange<Float>) {
        rule.waitForIdle()
        val old = rule.onNodeWithTag(OLD).range()
        val new = rule.onNodeWithTag(NEW).range()
        assertEquals("옛 Slider 값", expected, old.current, 0.0001f)
        assertEquals("ValueSlider 값", expected, new.current, 0.0001f)
        assertEquals(old.range, new.range)
        assertEquals(old.steps, new.steps)
        assertEquals(expectedRange, new.range)
    }

    @Suppress("DEPRECATION")
    @Test
    fun 바깥_값_범위_사용자_입력이_옛_Slider_와_같다() {
        var value by mutableFloatStateOf(0.2f)
        var range by mutableStateOf(0f..1f)
        var steps by mutableStateOf(0)
        var accept by mutableStateOf(true)
        rule.setContent {
            Column {
                Slider(
                    value = value,
                    onValueChange = { if (accept) value = it },
                    valueRange = range,
                    steps = steps,
                    modifier = Modifier.width(300.dp).testTag(OLD),
                )
                ValueSlider(
                    value = value,
                    onValueChange = { if (accept) value = it },
                    valueRange = range,
                    steps = steps,
                    modifier = Modifier.width(300.dp).testTag(NEW),
                )
            }
        }
        assertSame(0.2f, 0f..1f)

        // 1) 바깥에서 값 바꾸기(리셋 버튼 · 플레이어 위치) — rememberSliderState(value) 치환이면 여기서 0.2 에 멈춘다
        value = 0.8f
        assertSame(0.8f, 0f..1f)

        // 2) 범위 바꾸기(영상 길이 · 최대값이 나중에 정해지는 경우) — 상태를 새로 만든다
        range = 0f..10f
        value = 7f
        assertSame(7f, 0f..10f)

        // 3) 눈금 수 바꾸기 + 눈금 사이 값은 눈금으로 붙는다
        steps = 9
        value = 3.4f
        rule.waitForIdle()
        val snappedOld = rule.onNodeWithTag(OLD).range().current
        assertEquals(snappedOld, rule.onNodeWithTag(NEW).range().current, 0.0001f)

        // 4) 사용자 입력 — 새 슬라이더를 움직이면 바깥 값이 바뀌고 옛 슬라이더도 따라온다
        steps = 0
        rule.onNodeWithTag(NEW).performSemanticsAction(SemanticsActions.SetProgress) { it(5f) }
        assertSame(5f, 0f..10f)
        rule.onNodeWithTag(NEW).performTouchInput { swipeRight() }
        rule.waitForIdle()
        assertEquals(10f, value, 0.0001f)
        assertSame(10f, 0f..10f)

        // 5) 호출자가 입력을 무시하면 값은 바깥 값으로 남는다(제어형)
        accept = false
        rule.onNodeWithTag(NEW).performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        rule.onNodeWithTag(OLD).performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        assertSame(10f, 0f..10f)
    }

    private companion object {
        const val OLD = "old"
        const val NEW = "new"
    }
}
