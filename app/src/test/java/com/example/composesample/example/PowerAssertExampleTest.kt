@file:OptIn(ExperimentalPowerAssert::class)

package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.test.CheckFailure
import com.example.composesample.presentation.example.component.architecture.development.test.powerCheck
import org.junit.Test
import kotlin.powerassert.ExperimentalPowerAssert
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Power-Assert 예제 — 단위 테스트 컴파일(debugUnitTest)에 플러그인이 실제로 적용됐는지 검증한다.
 *
 * app/build.gradle 의 powerAssert.compilationFilter 에서 debugUnitTest 를 빼면
 * 첫 두 테스트의 메시지에서 식 도식이 사라져 실패한다(기본 필터 TESTS 로 두었을 때도 마찬가지).
 */
class PowerAssertExampleTest {

    /** kotlin.test.assertEquals 호출이 변환돼 실패 메시지에 식과 중간값이 붙는다 */
    @Test
    fun assertEquals_failureMessageContainsDiagram() {
        val prices = listOf(4500, 6800, 2300)
        val error = assertFailsWith<AssertionError> { assertEquals(13000, prices.sum()) }
        val message = error.message.orEmpty()
        assertTrue(message.contains("prices.sum()") && message.contains("13600"), message)
    }

    /** 로컬 JVM 테스트는 단언이 켜진 채(-ea) 돌아 kotlin.assert 가 실제로 검사한다 — ART 와 다른 점 */
    @Test
    fun kotlinAssert_isCheckedOnJvmTests() {
        val items = listOf(1, 2, 3)
        val error = assertFailsWith<AssertionError> { assert(items.sum() == 7) }
        assertTrue(error.message.orEmpty().contains("items.sum()"))
    }

    /** 테스트 컴파일에서 앱의 @PowerAssert 함수를 부르면 호출부가 변환돼 설명이 채워진다 */
    @Test
    fun powerCheck_fromTestCompilation_hasExplanation() {
        val total = 3
        val failure = assertFailsWith<CheckFailure> { powerCheck(total > 5) }
        assertNotNull(failure.explanation)
    }
}
