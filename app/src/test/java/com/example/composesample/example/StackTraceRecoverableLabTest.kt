package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.concurrency.BothWaysFailure
import com.example.composesample.presentation.example.component.architecture.development.concurrency.FieldOnlyFailure
import com.example.composesample.presentation.example.component.architecture.development.concurrency.LibraryCopyProbe
import com.example.composesample.presentation.example.component.architecture.development.concurrency.NameOnlyFailure
import com.example.composesample.presentation.example.component.architecture.development.concurrency.OptOutFailure
import com.example.composesample.presentation.example.component.architecture.development.concurrency.RecoverableCase
import com.example.composesample.presentation.example.component.architecture.development.concurrency.RecoverableFailure
import com.example.composesample.presentation.example.component.architecture.development.concurrency.measureLibraryCopy
import com.example.composesample.presentation.example.component.architecture.development.concurrency.newFailure
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * 코루틴 스택 트레이스 예제 — stdlib StackTraceRecoverable 카드
 *
 * Gradle 단위 테스트는 assertion 을 켜므로 kotlinx.coroutines 의 DEBUG 가 켜지고 복구가 실제로 동작한다.
 * 화면(안드로이드, 복구 꺼짐)은 라이브러리의 복사 함수를 직접 불러 판정을 보여주고,
 * 여기서는 그 판정이 실제 복구 경로(withContext 를 넘는 예외)의 결과와 같은지 확인한다.
 */
class StackTraceRecoverableLabTest {

    /** 대조군 — 필드가 없고 (String) 생성자가 있는 평범한 예외는 반사 생성자로 복사된다. */
    class PlainForTest(message: String) : RuntimeException(message)

    /** 메서드를 private 으로 두면 getMethod(공개 메서드만 찾음)가 못 찾는다. */
    class PrivateNameFailure(message: String, val requestId: String) : RuntimeException(message) {
        @Suppress("unused")
        private fun copyForStackTraceRecovery(): PrivateNameFailure =
            PrivateNameFailure(message ?: "", requestId).also { it.initCause(this) }
    }

    private suspend fun throwAcrossDispatchers(failure: Throwable): Nothing =
        withContext(Dispatchers.IO) { withContext<Nothing>(Dispatchers.Default) { throw failure } }

    private fun caughtThroughRecovery(failure: Throwable): Throwable = runBlocking {
        try {
            throwAcrossDispatchers(failure)
        } catch (caught: Throwable) {
            caught
        }
    }

    @Test
    fun `단위 테스트에서는 assertion 이 켜져 복구가 동작한다`() {
        assertTrue(CoroutineName::class.java.desiredAssertionStatus())
        assertTrue(LibraryCopyProbe.isAvailable)
    }

    @Test
    fun `필드만 있는 예외는 복사되지 않는다`() {
        val original = FieldOnlyFailure("실패", "req-42")
        assertNull(LibraryCopyProbe.copyOf(original))
        assertSame(original, caughtThroughRecovery(original))
    }

    @Test
    fun `StackTraceRecoverable 은 필드를 유지한 사본에 원본을 cause 로 단다`() {
        val original = RecoverableFailure("실패", "req-42")

        val caught = caughtThroughRecovery(original)

        assertNotSame(original, caught)
        assertSame(original, caught.cause)
        assertEquals("req-42", (caught as RecoverableFailure).requestId)
        assertTrue(caught.stackTrace.any { it.className.startsWith("_COROUTINE") })
    }

    @Test
    fun `둘 다 구현하면 createCopy 가 이긴다`() {
        val caught = caughtThroughRecovery(BothWaysFailure("실패"))
        assertEquals("createCopy", (caught as BothWaysFailure).copiedBy)
    }

    @Test
    fun `null 을 돌려주면 반사 생성자로 넘어가지 않고 복사하지 않는다`() {
        // 같은 모양(필드 없음 + String 생성자)의 평범한 예외는 복사된다
        assertNotNull(LibraryCopyProbe.copyOf(PlainForTest("실패")))

        val original = OptOutFailure("실패")
        assertNull(LibraryCopyProbe.copyOf(original))
        assertSame(original, caughtThroughRecovery(original))
    }

    @Test
    fun `인터페이스가 아니라 공개 메서드 이름으로 찾는다`() {
        val named = NameOnlyFailure("실패", "req-42")
        assertEquals("req-42", (caughtThroughRecovery(named) as NameOnlyFailure).requestId)
        assertSame(named, caughtThroughRecovery(named).cause)

        val hidden = PrivateNameFailure("실패", "req-42")
        assertNull(LibraryCopyProbe.copyOf(hidden))
    }

    @Test
    fun `화면이 보여주는 판정은 실제 복구 경로 결과와 같다`() {
        RecoverableCase.entries.forEach { case ->
            val measured = measureLibraryCopy(case)
            val original = case.newFailure()
            val caught = caughtThroughRecovery(original)
            assertEquals(measured.copied, caught !== original, case.name)
            assertEquals(measured.causeIsOriginal, caught.cause === original, case.name)
        }
    }
}
