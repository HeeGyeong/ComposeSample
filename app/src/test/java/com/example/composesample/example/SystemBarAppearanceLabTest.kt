package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.layout.insets.BarContent
import com.example.composesample.presentation.example.component.ui.layout.insets.BarFlags
import com.example.composesample.presentation.example.component.ui.layout.insets.BarRequest
import com.example.composesample.presentation.example.component.ui.layout.insets.BarRequestRegistry
import com.example.composesample.presentation.example.component.ui.layout.insets.BarRequestStack
import com.example.composesample.presentation.example.component.ui.layout.insets.BarWindow
import com.example.composesample.presentation.example.component.ui.layout.insets.DeclarationMode
import com.example.composesample.presentation.example.component.ui.layout.insets.SaveRestoreDeclaration
import com.example.composesample.presentation.example.component.ui.layout.insets.StagePage
import com.example.composesample.presentation.example.component.ui.layout.insets.StageScenario
import com.example.composesample.presentation.example.component.ui.layout.insets.SwapOrder
import com.example.composesample.presentation.example.component.ui.layout.insets.simulateScenario
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 시스템 바 아이콘 선언 예제 — 저장·복원과 요청 스택
 *
 * 기대값은 구현에서 옮겨 오지 않고 손으로 따라간 값이다. 원래 값은 두 에뮬레이터(API 30 · 35)의 이 앱 창과 같은
 * '둘 다 밝은 아이콘(false)'.
 */
class SystemBarAppearanceLabTest {

    private val off = BarFlags(lightStatus = false, lightNavigation = false)

    private class FakeWindow(override var flags: BarFlags) : BarWindow

    /** 단계마다 실제 상태바 값만 */
    private fun statusSteps(scenario: StageScenario, mode: DeclarationMode, order: SwapOrder): List<Boolean> =
        simulateScenario(scenario, mode, order, off).map { it.third.lightStatus }

    // ---- ⓐ 저장·복원 ----

    @Test
    fun `저장·복원은 나가는 화면이 먼저 정리되면 맞다`() {
        // ① 어두운 헤더(밝은 아이콘 = false) → 밝은 사진(true) → 홈(원래 false)
        assertEquals(listOf(false, true, false), statusSteps(StageScenario.DarkThenLight, DeclarationMode.SaveRestore, SwapOrder.LeaveFirst))
        // ② 밝은 사진(true) → 어두운 헤더(false) → 홈(false)
        assertEquals(listOf(true, false, false), statusSteps(StageScenario.LightThenDark, DeclarationMode.SaveRestore, SwapOrder.LeaveFirst))
    }

    @Test
    fun `저장·복원은 들어오는 화면이 먼저 돌면 새 화면의 요청을 잃는다`() {
        // 밝은 사진이 false 를 저장하고 true 적용 → 어두운 헤더 onDispose 가 false 로 되돌림 → 밝은 사진 위에 밝은 아이콘
        val steps = statusSteps(StageScenario.DarkThenLight, DeclarationMode.SaveRestore, SwapOrder.EnterFirst)
        assertEquals(listOf(false, false, false), steps)
        assertEquals(true, simulateScenario(StageScenario.DarkThenLight, DeclarationMode.SaveRestore, SwapOrder.EnterFirst, off)[1].second.lightStatus)
    }

    @Test
    fun `저장·복원은 들어오는 화면이 먼저 돌면 나간 뒤에도 남의 값을 되돌려 놓는다`() {
        // 어두운 헤더가 '밝은 사진이 적용한 true' 를 원래 값으로 저장 → 홈으로 나가며 true 를 되돌려 놓는다
        val steps = statusSteps(StageScenario.LightThenDark, DeclarationMode.SaveRestore, SwapOrder.EnterFirst)
        assertEquals(listOf(true, false, true), steps)
    }

    @Test
    fun `저장·복원 선언은 들어오지 않았으면 되돌리지 않는다`() {
        val window = FakeWindow(off)
        assertNull(SaveRestoreDeclaration(window, BarRequest(statusBar = BarContent.Dark)).leave())
        assertEquals(off, window.flags)
    }

    // ---- ⓑ 요청 스택 ----

    @Test
    fun `요청 스택은 두 순서 모두 보이는 화면의 요청을 따른다`() {
        for (order in SwapOrder.entries) {
            assertEquals(listOf(false, true, false), statusSteps(StageScenario.DarkThenLight, DeclarationMode.Stack, order), "$order")
            assertEquals(listOf(true, false, false), statusSteps(StageScenario.LightThenDark, DeclarationMode.Stack, order), "$order")
        }
    }

    @Test
    fun `요청 스택은 바마다 가장 최근에 그 바를 지정한 요청이 이긴다`() {
        val window = FakeWindow(off)
        val stack = BarRequestStack(window)
        stack.put("screen", BarRequest(statusBar = BarContent.Dark, navigationBar = BarContent.Dark))
        assertEquals(BarFlags(lightStatus = true, lightNavigation = true), window.flags)
        // 상태바만 지정한 오버레이 — 내비 바는 아래 화면의 요청이 그대로
        stack.put("overlay", BarRequest(statusBar = BarContent.Light))
        assertEquals(BarFlags(lightStatus = false, lightNavigation = true), window.flags)
        stack.remove("overlay")
        assertEquals(BarFlags(lightStatus = true, lightNavigation = true), window.flags)
        stack.remove("screen")
        assertEquals(off, window.flags)
    }

    @Test
    fun `요청 스택은 오래된 요청이 먼저 빠져도 최근 요청을 지킨다`() {
        val window = FakeWindow(off)
        val stack = BarRequestStack(window)
        stack.put("A", BarRequest(statusBar = BarContent.Dark))
        stack.put("B", BarRequest(statusBar = BarContent.Light))
        assertEquals(false, window.flags.lightStatus)
        stack.remove("A")
        assertEquals(false, window.flags.lightStatus)
        stack.put("C", BarRequest(statusBar = BarContent.Dark))
        stack.remove("B")
        assertEquals(true, window.flags.lightStatus)
    }

    @Test
    fun `요청 스택은 이미 있는 요청을 다시 넣어도 순서를 바꾸지 않는다`() {
        val window = FakeWindow(off)
        val stack = BarRequestStack(window)
        stack.put("A", BarRequest(statusBar = BarContent.Dark))
        stack.put("B", BarRequest(statusBar = BarContent.Light))
        // A 가 다시 그려지며 같은 요청을 넣어도 맨 위로 오지 않는다
        stack.put("A", BarRequest(statusBar = BarContent.Dark))
        assertEquals(false, window.flags.lightStatus)
        assertEquals(listOf<Any>("A", "B"), stack.entries.map { it.first })
    }

    @Test
    fun `레지스트리는 창마다 따로 쌓고 비면 다음 첫 요청 때 원래 값을 다시 읽는다`() {
        val activity = FakeWindow(off)
        val dialog = FakeWindow(off)
        val registry = BarRequestRegistry()
        registry.put(activity, activity, "screen", BarRequest(statusBar = BarContent.Dark))
        registry.put(dialog, dialog, "dialog", BarRequest(statusBar = BarContent.Light, navigationBar = BarContent.Dark))
        assertEquals(BarFlags(lightStatus = true, lightNavigation = false), activity.flags)
        assertEquals(BarFlags(lightStatus = false, lightNavigation = true), dialog.flags)

        registry.remove(dialog, "dialog")
        assertNull(registry.stackOf(dialog))
        registry.remove(activity, "screen")
        assertEquals(off, activity.flags)

        // 스택이 없는 동안 바뀐 값이 다음 스택의 원래 값이 된다
        activity.flags = BarFlags(lightStatus = true, lightNavigation = false)
        registry.put(activity, activity, "screen", BarRequest(navigationBar = BarContent.Dark))
        registry.remove(activity, "screen")
        assertEquals(BarFlags(lightStatus = true, lightNavigation = false), activity.flags)
    }

    @Test
    fun `선언 없는 화면은 저장·복원도 하지 않는다`() {
        assertEquals(false, StagePage.Home.declares)
        assertEquals(true, StagePage.DarkHeader.declares)
        assertEquals(true, StagePage.LightPhoto.declares)
    }
}
