package com.example.composesample.example

import com.example.composesample.presentation.example.component.data.api.FailoverSituation
import com.example.composesample.presentation.example.component.data.api.runFailoverSituation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * KtorAdvancedConfigExample 4번 카드(오리진 페일오버)의 정책 검증 — 원문의 "검증할 상황" 표를 그대로 실행한다.
 * given : 상황마다 새 MockEngine(오리진 두 대) · 새 상태 기계 · 가짜 시계
 * when : 실제 Ktor 클라이언트에 페일오버 플러그인을 설치해 요청
 * then : 응답·오리진별 호출 수·상태 기계의 최종 상태가 기대와 같다
 */
class OriginFailoverTest {

    @Test
    fun `원문 표의 모든 상황이 기대대로 동작한다`() {
        runBlocking {
            FailoverSituation.entries.forEach { situation ->
                val run = runFailoverSituation(situation)
                assertTrue("${situation.situation} → ${run.actual}\n${run.log.joinToString("\n")}", run.passed)
            }
        }
    }

    @Test
    fun `실제로 쓴 오리진을 확인하지 않으면 동시 실패가 새 보조 오리진까지 떨어뜨린다`() {
        runBlocking {
            // Given: 동시 실패 상황, 확인 없는 상태 기계
            // When
            val run = runFailoverSituation(FailoverSituation.CONCURRENT, checkUsedOrigin = false)

            // Then: 옛 주 오리진 실패가 보조를 Unavailable 로 만든다
            assertFalse(run.actual, run.passed)
            assertTrue(run.actual, run.actual.contains("UNAVAILABLE"))
        }
    }
}
