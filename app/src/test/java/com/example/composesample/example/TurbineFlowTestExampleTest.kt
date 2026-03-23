package com.example.composesample.example

import app.cash.turbine.test
import com.example.composesample.presentation.example.component.architecture.development.test.FlowTestEvent
import com.example.composesample.presentation.example.component.architecture.development.test.FlowTestUiState
import com.example.composesample.presentation.example.component.architecture.development.test.TurbineFlowTestViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Coroutine Flow Testing 패턴 비교 예제
 *
 * given : 테스트 사전 준비
 * when  : 테스트 동작 실행
 * then  : 결과 검증
 *
 * 핵심: StateFlow는 .value로 상태별 독립 테스트
 *       SharedFlow(이벤트)는 Turbine으로 검증
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TurbineFlowTestExampleTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TurbineFlowTestViewModel

    @Before
    fun setup() {
        // StandardTestDispatcher로 교체해 명시적 진행 제어
        Dispatchers.setMain(testDispatcher)
        viewModel = TurbineFlowTestViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 기초 패턴: StateFlow 상태별 독립 테스트 ──────────────────────────────

    @Test
    fun `초기 상태는 Empty이다`() = runTest {
        // given: ViewModel 초기화 직후
        // then
        assertEquals(FlowTestUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `fetchData 호출 직후 Loading 상태가 된다`() = runTest {
        // given
        assertEquals(FlowTestUiState.Empty, viewModel.uiState.value)

        // when: fetchData 호출 후 delay() 이전까지만 실행
        viewModel.fetchData()
        runCurrent()

        // then: delay 이전이므로 Loading 상태
        assertEquals(FlowTestUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `fetchData 완료 후 Success 상태가 된다`() = runTest {
        // given
        assertEquals(FlowTestUiState.Empty, viewModel.uiState.value)

        // when: 모든 코루틴이 완료될 때까지 진행
        viewModel.fetchData(shouldFail = false)
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assertTrue(state is FlowTestUiState.Success, "Success 상태여야 하지만: $state")
        assertEquals(3, (state as FlowTestUiState.Success).items.size)
    }

    @Test
    fun `fetchData 실패 시 Error 상태가 된다`() = runTest {
        // when
        viewModel.fetchData(shouldFail = true)
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assertTrue(state is FlowTestUiState.Error, "Error 상태여야 하지만: $state")
    }

    // ── ❌ Turbine 과명세화 안티패턴 (참고용, 실제로는 권장하지 않음) ───────────

    @Test
    fun `안티패턴 - Turbine awaitItem으로 StateFlow 전체 흐름 검증`() = runTest {
        // ❌ 이 패턴의 문제점:
        // 1. Loading 상태를 생략하는 리팩토링만 해도 이 테스트가 실패
        // 2. 구현 세부사항(중간 상태 개수)에 의존하게 됨
        // 3. StateFlow의 버퍼 특성상 빠른 업데이트 시 항목을 놓칠 수 있음
        viewModel.uiState.test {
            assertEquals(FlowTestUiState.Empty, awaitItem()) // 초기값

            viewModel.fetchData()
            assertEquals(FlowTestUiState.Loading, awaitItem()) // Loading
            advanceUntilIdle()
            val finalState = awaitItem() // Success
            assertTrue(finalState is FlowTestUiState.Success)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── ✅ Turbine이 적합한 경우: SharedFlow 단방향 이벤트 ────────────────────

    @Test
    fun `fetchData 성공 시 ShowToast 이벤트가 발행된다`() = runTest {
        // SharedFlow는 .value가 없으므로 Turbine이 적합
        viewModel.events.test {
            // when
            viewModel.fetchData(shouldFail = false)
            advanceUntilIdle()

            // then: 이벤트 수신 확인
            val event = awaitItem()
            assertTrue(event is FlowTestEvent.ShowToast)
            assertEquals("로딩 완료", (event as FlowTestEvent.ShowToast).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetchData 실패 시 ShowToast 이벤트가 발행된다`() = runTest {
        viewModel.events.test {
            // when
            viewModel.fetchData(shouldFail = true)
            advanceUntilIdle()

            // then
            val event = awaitItem()
            assertTrue(event is FlowTestEvent.ShowToast)
            assertEquals("오류가 발생했습니다", (event as FlowTestEvent.ShowToast).message)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
