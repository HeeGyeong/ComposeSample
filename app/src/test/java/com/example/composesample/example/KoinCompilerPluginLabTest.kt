package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.di.RuntimeGap
import com.example.composesample.presentation.example.component.architecture.development.di.resolveCheckoutGraph
import com.example.composesample.presentation.example.component.architecture.development.di.runGap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.context.GlobalContext

/**
 * Koin Compiler Plugin 예제 — 기대값은 구현이 아니라 Koin 의 정의 의미로 쓴다:
 * single 은 그래프 안에서 하나, factory 는 요청마다 새것, nullable 매개변수는 정의가 없으면 null, 기본값 매개변수는 Kotlin 기본값.
 * 이 테스트가 돈다는 것 자체가 플러그인이 single<T>() 를 바꿔 넣었다는 뜻이다 — 플러그인 없이는 NotImplementedError(측정).
 * 예외 사슬은 정확히 비교한다 — debug 의 HotSwan 이 Koin 예외(자바 기준 checked)를 UndeclaredThrowableException 으로 한 겹 감싸던 것을
 * 이 비교로 잡았다(app/build.gradle 의 hotSwanCompiler.exclude 로 release 와 같게 맞춤).
 */
class KoinCompilerPluginLabTest {

    @Test
    fun `플러그인 DSL 그래프 - single 은 공유되고 factory 는 매번 새것`() {
        val report = resolveCheckoutGraph()
        assertTrue("CheckoutService 의 repository 는 그래프의 OrderRepository 와 같은 인스턴스", report.serviceRepositoryIsShared)
        assertTrue("PriceCalculator 의 clock 도 그래프의 CartClock 과 같은 인스턴스", report.calculatorClockIsShared)
        assertTrue("factory 는 요청마다 새 인스턴스", report.factoryGivesNewInstance)
    }

    @Test
    fun `정의가 없는 nullable 은 null, 기본값 매개변수는 Kotlin 기본값`() {
        val report = resolveCheckoutGraph()
        assertTrue(report.retryCacheIsNull)
        assertEquals(3, report.retryMaxRetries)
    }

    @Test
    fun `격리 그래프라 전역 Koin 은 시작되지 않는다`() {
        resolveCheckoutGraph()
        RuntimeGap.entries.forEach { runGap(it) }
        assertNull(GlobalContext.getOrNull())
    }

    @Test
    fun `한정자 불일치는 빌드를 통과하고 실행 중에 정의 없음으로 실패한다`() {
        val result = runGap(RuntimeGap.QUALIFIER)
        // Koin 은 정의를 만들다 난 실패를 InstanceCreationException 으로 감싼다 — 원인은 remote 한정자의 정의 없음
        assertTrue(result.wrong, result.wrong.startsWith("실패 · InstanceCreationException ← NoDefinitionFoundException\n"))
        assertTrue(result.wrong, result.wrong.contains("type 'LabCache' and qualifier 'remote'"))
        assertEquals("local 로 요청하면 같은 정의가 풀린다", "성공 · LabCache", result.right)
    }

    @Test
    fun `parametersOf 를 빠뜨리면 실행 중에 실패하고 넘기면 그 값으로 만든다`() {
        val result = runGap(RuntimeGap.PARAMETERS)
        assertTrue(result.wrong, result.wrong.startsWith("실패 · InstanceCreationException ← NoParameterFoundException\n"))
        assertEquals("성공 · orderId=A-1001", result.right)
    }
}
