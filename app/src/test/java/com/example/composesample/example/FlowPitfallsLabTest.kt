package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.flow.ARTICLE_VALUE
import com.example.composesample.presentation.example.component.architecture.development.flow.CancelCase
import com.example.composesample.presentation.example.component.architecture.development.flow.CatchCase
import com.example.composesample.presentation.example.component.architecture.development.flow.CollectMode
import com.example.composesample.presentation.example.component.architecture.development.flow.FAILING_VALUE
import com.example.composesample.presentation.example.component.architecture.development.flow.ShareCase
import com.example.composesample.presentation.example.component.architecture.development.flow.runCancelCase
import com.example.composesample.presentation.example.component.architecture.development.flow.runCatchCase
import com.example.composesample.presentation.example.component.architecture.development.flow.runShareCase
import com.example.composesample.presentation.example.component.architecture.development.flow.runTwoCollectors
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Flow 함정 예제 — 화면과 같은 측정 함수를 가상 시간(runTest)에서 돌려 판정을 고정한다.
 * 화면은 실제 시간·Main 디스패처에서 같은 함수를 부르고, 결과(횟수·받은 값)는 시간에 기대지 않는다.
 */
class FlowPitfallsLabTest {

    @Test
    fun `콜드 Flow 를 두 곳에서 수집하면 업스트림이 두 번 돌고 두 화면이 다른 응답을 받는다`() = runTest {
        val result = runTwoCollectors(CollectMode.COLD)

        assertEquals(2, result.fetchCount)
        assertEquals(setOf("v1", "v2"), setOf(result.header, result.profile))
    }

    @Test
    fun `shareIn 으로 공유하면 업스트림은 한 번이고 두 화면이 같은 응답을 받는다`() = runTest {
        val result = runTwoCollectors(CollectMode.SHARED)

        assertEquals(1, result.fetchCount)
        assertEquals("v1", result.header)
        assertEquals("v1", result.profile)
    }

    @Test
    fun `replay 0 이면 늦게 붙은 구독자는 아무것도 받지 못한다`() = runTest {
        val result = runShareCase(ShareCase.LATE_NO_REPLAY)

        assertEquals(1, result.fetchCount)
        assertEquals("v1", result.first)
        assertEquals(emptyList(), result.second)
    }

    @Test
    fun `replay 1 이면 늦게 붙은 구독자가 캐시된 값을 받고 업스트림은 다시 돌지 않는다`() = runTest {
        val result = runShareCase(ShareCase.LATE_REPLAY)

        assertEquals(1, result.fetchCount)
        assertEquals(listOf("v1"), result.second)
    }

    @Test
    fun `구독자가 0 이 된 뒤 다시 붙으면 옛 값을 먼저 받고 업스트림이 다시 돈다`() = runTest {
        val result = runShareCase(ShareCase.RESUBSCRIBE)

        assertEquals(2, result.fetchCount)
        assertEquals(listOf("v1", "v2"), result.second)
    }

    @Test
    fun `stopTimeout 안에 다시 붙으면 업스트림이 다시 돌지 않는다`() = runTest {
        val result = runShareCase(ShareCase.RESUBSCRIBE_WITHIN_TIMEOUT)

        assertEquals(1, result.fetchCount)
        assertEquals(listOf("v1"), result.second)
    }

    @Test
    fun `replayExpiration 0 이면 재구독자는 옛 값 없이 새 응답만 받는다`() = runTest {
        val result = runShareCase(ShareCase.RESUBSCRIBE_EXPIRED_REPLAY)

        assertEquals(2, result.fetchCount)
        assertEquals(listOf("v2"), result.second)
    }

    @Test
    fun `catch 는 아래(하류) onEach 의 예외를 잡지 않는다`() = runTest {
        val result = runCatchCase(CatchCase.CATCH_THEN_ON_EACH, FAILING_VALUE)

        assertEquals(0, result.handlerCalls)
        assertEquals(emptyList(), result.received)
        assertEquals("IllegalStateException: Boom!", result.escaped)
    }

    @Test
    fun `onEach 를 catch 위로 옮기면 catch 가 잡아 대체 값을 내보낸다`() = runTest {
        val result = runCatchCase(CatchCase.ON_EACH_THEN_CATCH, FAILING_VALUE)

        assertEquals(1, result.handlerCalls)
        assertEquals(listOf(-1), result.received)
        assertNull(result.escaped)
    }

    @Test
    fun `collect 람다의 예외는 어떤 catch 도 잡지 않는다`() = runTest {
        val result = runCatchCase(CatchCase.THROW_IN_COLLECT, FAILING_VALUE)

        assertEquals(0, result.handlerCalls)
        assertEquals("IllegalStateException: Boom!", result.escaped)
    }

    @Test
    fun `기사 원문 값 1000 은 어느 순서에서도 예외가 나지 않는다`() = runTest {
        CatchCase.entries.forEach { case ->
            val result = runCatchCase(case, ARTICLE_VALUE)

            assertEquals(listOf(ARTICLE_VALUE), result.received, case.name)
            assertEquals(0, result.handlerCalls, case.name)
            assertNull(result.escaped, case.name)
        }
    }

    @Test
    fun `수집 코루틴이 취소되면 catch 는 불리지 않는다`() = runTest {
        val result = runCancelCase(CancelCase.COLLECTOR_CANCELLED)

        assertEquals(0, result.handlerCalls)
        assertEquals(listOf(1), result.received)
        assertEquals("수집 코루틴이 취소로 끝남", result.outcome)
    }

    @Test
    fun `withTimeout 이 수집을 감싸면 시간 초과는 catch 를 지나 바깥으로 나간다`() = runTest {
        val result = runCancelCase(CancelCase.TIMEOUT_AROUND_COLLECT)

        assertEquals(0, result.handlerCalls)
        assertEquals(emptyList(), result.received)
        assertEquals("TimeoutCancellationException 이 withTimeout 밖으로 나옴", result.outcome)
    }

    @Test
    fun `withTimeout 이 상류 안이면 catch 가 TimeoutCancellationException 을 잡는다`() = runTest {
        val result = runCancelCase(CancelCase.TIMEOUT_INSIDE_UPSTREAM)

        assertEquals(1, result.handlerCalls)
        assertEquals("TimeoutCancellationException", result.handlerSaw)
        assertEquals(listOf(-1), result.received)
        assertEquals("정상 완료", result.outcome)
    }

    @Test
    fun `catch 에서 CancellationException 을 다시 던지면 launch 가 조용히 취소로 끝난다`() = runTest {
        val result = runCancelCase(CancelCase.RETHROW_IN_HANDLER)

        assertEquals(1, result.handlerCalls)
        assertEquals(emptyList(), result.received)
        assertEquals("launch 가 취소로 끝남(부모에 실패 전파 없음)", result.outcome)
    }
}
