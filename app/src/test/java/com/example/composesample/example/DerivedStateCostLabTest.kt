package com.example.composesample.example

import android.os.Trace
import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ControlledComposition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.structuralEqualityPolicy
import com.example.composesample.presentation.example.component.architecture.state.COMPUTE_WRITES
import com.example.composesample.presentation.example.component.architecture.state.ComputeReaders
import com.example.composesample.presentation.example.component.architecture.state.ComputeScenario
import com.example.composesample.presentation.example.component.architecture.state.ComputeSetup
import com.example.composesample.presentation.example.component.architecture.state.CountingTypefaceLoader
import com.example.composesample.presentation.example.component.architecture.state.DERIVED_PROPERTIES_PER_PART
import com.example.composesample.presentation.example.component.architecture.state.DerivedCostConfig
import com.example.composesample.presentation.example.component.architecture.state.DerivedCostCounters
import com.example.composesample.presentation.example.component.architecture.state.DerivedCostLabState
import com.example.composesample.presentation.example.component.architecture.state.LabFontSpec
import com.example.composesample.presentation.example.component.architecture.state.PaywallPackages
import com.example.composesample.presentation.example.component.architecture.state.PaywallPartState
import com.example.composesample.presentation.example.component.architecture.state.StateApi
import com.example.composesample.presentation.example.component.architecture.state.TapCost
import com.example.composesample.presentation.example.component.architecture.state.computedTapLowerBound
import com.example.composesample.presentation.example.component.architecture.state.expectedFor
import com.example.composesample.presentation.example.component.architecture.state.expectedPerWrite
import com.example.composesample.presentation.example.component.architecture.state.expectedTapCost
import com.example.composesample.presentation.example.component.architecture.state.tapMatches
import com.example.composesample.presentation.example.component.architecture.state.writeInsideCalculation
import com.example.composesample.presentation.example.component.architecture.state.paywallParts
import com.example.composesample.presentation.example.component.architecture.state.readPart
import com.example.composesample.presentation.example.component.architecture.state.resolve
import com.example.composesample.presentation.example.component.architecture.state.resolveLook
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

/**
 * derivedStateOf 비용 Lab — 기대값은 runtime 1.12.1 DerivedState.kt · RecomposeScopeImpl.kt 규칙으로 계산한 [expectedTapCost].
 * 실제 컴포지션 테스트는 Recomposer 루프 없이 한 단계씩 다시 구성한다(아래 [StepComposition]).
 * 폰트 로드는 레이아웃에서 일어나 JVM 에서는 0 이다 — 기기 실측으로만 확인한다.
 */
class DerivedStateCostLabTest {

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

    // ==================== 실제 컴포지션 ====================

    @Test
    fun `실제 컴포지션 — 8 구성 모두 탭 비용이 예상과 같다`() {
        for (eager in listOf(false, true)) for (policy in listOf(false, true)) for (equals in listOf(false, true)) {
            val config = DerivedCostConfig(plainCount = 20, eagerReads = eager, basePolicy = policy, fontEquals = equals)
            val paywall = ComposedPaywall(config)
            // 월간 → 연간 → 월간: 두 방향 모두 같은 비용
            for (target in listOf(PaywallPackages.Annual, PaywallPackages.Monthly)) {
                val measured = paywall.tap(target)
                assertEquals(config.toString(), expectedTapCost(config, true).copy(fontLoads = 0), measured)
            }
            paywall.dispose()
        }
    }

    @Test
    fun `실제 컴포지션 — 이미 선택한 플랜을 다시 누르면 아무 일도 없다`() {
        val config = DerivedCostConfig(plainCount = 20, eagerReads = true)
        val paywall = ComposedPaywall(config)
        assertEquals(TapCost(0, 0, 0, 0), paywall.tap(PaywallPackages.Monthly))
        assertEquals(expectedTapCost(config, false), paywall.tap(PaywallPackages.Monthly))
        paywall.dispose()
    }

    @Test
    fun `실제 컴포지션 — 한 인스턴스를 다섯 스코프가 읽으면 한 번, 스코프마다 인스턴스면 다섯 번 계산한다`() {
        val selection = mutableStateOf("A")
        var sharedCalcs = 0
        var ownCalcs = 0
        var recompositions = 0
        val shared = derivedStateOf { sharedCalcs++; selection.value == "Z" }
        val own = List(5) { derivedStateOf { ownCalcs++; selection.value == "Z" } }
        val composition = StepComposition {
            repeat(5) { i -> key(i) { TwoReads(shared, own[i]) { recompositions++ } } }
        }
        sharedCalcs = 0; ownCalcs = 0; recompositions = 0
        Snapshot.withMutableSnapshot { selection.value = "B" }
        composition.step()
        assertEquals(1, sharedCalcs)
        assertEquals(5, ownCalcs)
        // 둘 다 false → false — 다시 계산했지만 아무도 다시 그리지 않는다
        assertEquals(0, recompositions)
        composition.dispose()
    }

    @Test
    fun `실제 컴포지션 — computedStateOf 8 구성 — 리컴포지션은 같고 기반 = 속성 ≥ 하한`() {
        for (eager in listOf(false, true)) for (policy in listOf(false, true)) for (equals in listOf(false, true)) {
            val derived = DerivedCostConfig(plainCount = 20, eagerReads = eager, basePolicy = policy, fontEquals = equals)
            val config = derived.copy(stateApi = StateApi.Computed)
            val paywall = ComposedPaywall(config)
            for (target in listOf(PaywallPackages.Annual, PaywallPackages.Monthly)) {
                val measured = paywall.tap(target)
                val expected = expectedFor(config, true)
                assertTrue("$config $measured ≥ $expected", tapMatches(config, measured, expected, ignoreFontLoads = true))
                // 캐시가 없어 기반 해석은 derivedStateOf 보다 많다(기반을 속성 넷이 나눠 쓰지 못한다)
                assertTrue("$config", measured.resolutions > expectedTapCost(derived, true).resolutions)
            }
            assertEquals(TapCost(0, 0, 0, 0), paywall.tap(PaywallPackages.Monthly))
            paywall.dispose()
        }
    }

    @Test
    fun `실제 컴포지션 — 같은 쓰기를 두 API 로, 4 모양 모두 예상과 같다`() {
        for (scenario in ComputeScenario.entries) for (api in StateApi.entries) {
            val setup = ComputeSetup(scenario, api)
            val composition = StepComposition { ComputeReaders(setup) }
            val before = setup.counters.snapshot()
            repeat(COMPUTE_WRITES) {
                Snapshot.withMutableSnapshot { setup.write() }
                composition.step()
            }
            assertEquals("$scenario $api", expectedPerWrite(scenario, api) * COMPUTE_WRITES, setup.counters.snapshot() - before)
            composition.dispose()
        }
    }

    @Test
    fun `계산 안에서 쓰기 — computedStateOf 는 IllegalStateException, derivedStateOf 는 그대로 쓴다`() {
        assertTrue(writeInsideCalculation(StateApi.Computed).startsWith("IllegalStateException"))
        assertEquals("예외 없음 — 계산 안의 쓰기가 반영됐다(target = 1)", writeInsideCalculation(StateApi.Derived))
    }

    @Test
    fun `computedStateOf 하한 — 다시 그린 칸 × 5 + 건너뛴 칸 × 8, 정책 스위치는 효과 없음`() {
        val base = DerivedCostConfig(plainCount = 20, stateApi = StateApi.Computed)
        assertEquals(TapCost(15, 15, 3, 0), computedTapLowerBound(base, true))
        assertEquals(TapCost(175, 175, 3, 0), computedTapLowerBound(base.copy(eagerReads = true), true))
        assertEquals(TapCost(115, 115, 23, 23), computedTapLowerBound(base.copy(eagerReads = true, fontEquals = false), true))
        assertEquals(computedTapLowerBound(base, true), computedTapLowerBound(base.copy(basePolicy = true), true))
        assertEquals(TapCost(0, 0, 0, 0), computedTapLowerBound(base, false))
        // 판정: 리컴포지션 · 폰트 로드는 같아야 하고, 기반 = 속성이며 하한 이상
        val expected = computedTapLowerBound(base, true)
        assertTrue(tapMatches(base, TapCost(18, 18, 3, 0), expected))
        assertFalse(tapMatches(base, TapCost(18, 17, 3, 0), expected))
        assertFalse(tapMatches(base, TapCost(14, 14, 3, 0), expected))
        assertFalse(tapMatches(base, TapCost(18, 18, 4, 0), expected))
        // derivedStateOf 는 정확히 같아야 한다
        val derived = base.copy(stateApi = StateApi.Derived)
        assertTrue(tapMatches(derived, TapCost(3, 12, 3, 0), expectedFor(derived, true)))
        assertFalse(tapMatches(derived, TapCost(4, 12, 3, 0), expectedFor(derived, true)))
    }

    @Composable
    private fun TwoReads(a: State<Boolean>, b: State<Boolean>, onRecompose: () -> Unit) {
        androidx.compose.runtime.SideEffect { onRecompose() }
        a.value
        b.value
    }

    // ==================== 스냅숏 규칙 ====================

    @Test
    fun `부르지 않은 람다가 읽을 상태는 의존성이 아니다`() {
        val selection = mutableStateOf("A")
        var calcs = 0
        val eager = derivedStateOf { calcs++; selection.value.length }
        val lazy = derivedStateOf { calcs++; keepUncalled { selection.value } }
        eager.value; lazy.value
        calcs = 0
        Snapshot.withMutableSnapshot { selection.value = "B" }
        lazy.value
        assertEquals(0, calcs)
        eager.value
        assertEquals(1, calcs)
    }

    @Test
    fun `정책 없는 기반은 같은 값이어도 새 레코드라 위층을 모두 다시 계산하고 정책이 있으면 건너뛴다`() {
        for (policy in listOf(false, true)) {
            val selection = mutableStateOf("A")
            var base = 0
            var upper = 0
            val calculation = { base++; selection.value; "same" }
            val baseState = if (policy) derivedStateOf(structuralEqualityPolicy(), calculation) else derivedStateOf(calculation)
            val uppers = List(6) { i -> derivedStateOf { upper++; baseState.value + i } }
            uppers.forEach { it.value }
            base = 0; upper = 0
            Snapshot.withMutableSnapshot { selection.value = "B" }
            uppers.forEach { it.value }
            assertEquals("policy=$policy", 1, base)
            assertEquals("policy=$policy", if (policy) 0 else 6, upper)
        }
    }

    @Test
    fun `equals 없는 폰트는 같은 명세여도 다른 FontFamily 다`() {
        val loader = CountingTypefaceLoader {}
        val spec = LabFontSpec("lab-serif")
        assertEquals(spec.resolve(true, loader), spec.resolve(true, loader))
        assertNotEquals(spec.resolve(false, loader), spec.resolve(false, loader))
        // 로더가 다르면 equals 가 있어도 다른 폰트(원문 LocalFileFont 도 fileTypefaceLoader 를 비교)
        assertNotEquals(spec.resolve(true, loader), spec.resolve(true, CountingTypefaceLoader {}))
        val policy = structuralEqualityPolicy<Any>()
        assertFalse(policy.equivalent(spec.resolve(false, loader), spec.resolve(false, loader)))
    }

    // ==================== 순수 로직 ====================

    @Test
    fun `해석은 그 조건을 검사하는 분기에서만 입력을 부른다`() {
        val spec = LabFontSpec("f")
        var selectedCalls = 0
        var annualCalls = 0
        val isSelected = { selectedCalls++; true }
        val annual = { annualCalls++; true }
        val parts = paywallParts(plainCount = 2).associateBy { it.id }

        resolveLook(parts.getValue(3), spec, isSelected, annual)
        assertEquals(0 to 0, selectedCalls to annualCalls)

        val card = resolveLook(parts.getValue(0), spec, isSelected, annual)
        assertEquals(1 to 0, selectedCalls to annualCalls)
        assertTrue(card.emphasized && card.bold)

        val footer = resolveLook(parts.getValue(2), spec, isSelected, annual)
        assertEquals(1 to 1, selectedCalls to annualCalls)
        assertTrue(footer.emphasized)
        assertFalse(footer.bold)
    }

    @Test
    fun `예상 비용 — 300 칸 옛 코드는 칸 수에 비례하고 고친 코드는 선택 의존 칸만`() {
        val old = expectedTapCost(DerivedCostConfig(plainCount = 300, eagerReads = true, fontEquals = false), true)
        assertEquals(TapCost(303, 303 * DERIVED_PROPERTIES_PER_PART, 303, 303), old)
        val fixed = expectedTapCost(DerivedCostConfig(plainCount = 300), true)
        assertEquals(TapCost(3, 3 * DERIVED_PROPERTIES_PER_PART, 3, 0), fixed)
        // 람다로 고친 뒤에는 equals 를 꺼도 탭 비용이 같다(원문: 193 ms vs 184 ms, 한 프레임 안)
        assertEquals(TapCost(3, 12, 3, 3), expectedTapCost(DerivedCostConfig(plainCount = 300, fontEquals = false), true))
        // 미리 읽어도 정책이 있으면 위층 재계산 · 리컴포지션은 선택 의존 칸만
        assertEquals(
            TapCost(303, 12, 3, 3),
            expectedTapCost(DerivedCostConfig(plainCount = 300, eagerReads = true, basePolicy = true, fontEquals = false), true)
        )
    }

    @Test
    fun `화면 상태 — 기록은 최근 6 개, 예상은 탭 시점 구성으로`() {
        val lab = DerivedCostLabState()
        lab.config = DerivedCostConfig(plainCount = 20, eagerReads = true)
        repeat(8) { i ->
            lab.select(if (i % 2 == 0) PaywallPackages.Annual else PaywallPackages.Monthly)
            lab.publish()
        }
        assertEquals(6, lab.reports.size)
        assertEquals(8, lab.reports.first().seq)
        assertEquals(expectedTapCost(lab.config, true), lab.reports.first().expected)
        lab.select(PaywallPackages.Monthly)
        lab.publish()
        assertEquals(TapCost(0, 0, 0, 0), lab.reports.first().expected)
        // publish 를 두 번 불러도 한 번만 기록
        lab.publish()
        assertEquals(9, lab.reports.first().seq)
        assertEquals(8, lab.reports[1].seq)
    }

    // ==================== 하네스 ====================

    /** Lab 과 같은 칸 상태 + [readPart] 로 읽는 컴포지션 */
    private class ComposedPaywall(config: DerivedCostConfig) {
        private val selected = mutableStateOf(PaywallPackages.Monthly)
        private val counters = DerivedCostCounters()
        private val loader = CountingTypefaceLoader { counters.fontLoads++ }
        private val states = paywallParts(config.plainCount).map { PaywallPartState(it, selected, config, counters, loader) }
        private val composition = StepComposition {
            states.forEach { state -> key(state.part.id) { PartReader(state, counters) } }
        }

        fun tap(packageId: String): TapCost {
            val before = counters.snapshot()
            Snapshot.withMutableSnapshot { selected.value = packageId }
            composition.step()
            return counters.snapshot() - before
        }

        fun dispose() = composition.dispose()
    }

    private class UnitApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(index: Int, instance: Unit) = Unit
        override fun insertBottomUp(index: Int, instance: Unit) = Unit
        override fun remove(index: Int, count: Int) = Unit
        override fun move(from: Int, to: Int, count: Int) = Unit
        override fun onClear() = Unit
    }

    /**
     * Recomposer 의 프레임 루프 없이 한 단계씩 다시 구성한다. 순서는 Recomposer 와 같다:
     * 바뀐 상태 전달 → 컴포지션의 읽기 · 쓰기 관찰자를 건 스냅숏 안에서 recompose → (변경이 있으면) 적용.
     * (관찰자 없이 recompose 하면 다시 그린 스코프가 구독을 잃는다 — 프로브에서 확인)
     */
    private class StepComposition(content: @Composable () -> Unit) {
        private val recomposer = Recomposer(EmptyCoroutineContext)
        private val modified = mutableSetOf<Any>()
        private val handle = Snapshot.registerApplyObserver { changed, _ -> modified.addAll(changed) }
        private val composition = ControlledComposition(UnitApplier(), recomposer).apply { setContent(content) }

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
            // Recomposer 처럼 recompose 가 true(적용할 변경 있음)일 때만 적용한다 — false 면 recompose 가 이미 정리했다
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
}

@Composable
private fun PartReader(state: PaywallPartState, counters: DerivedCostCounters) {
    readPart(state, counters)
}

/** 람다를 받기만 하고 부르지 않는다 */
private fun keepUncalled(read: () -> String): Int = if (read === NEVER) 0 else 1

private val NEVER: () -> String = { "" }
