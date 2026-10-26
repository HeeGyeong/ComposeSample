package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.security.SpComponent
import com.example.composesample.presentation.example.component.system.security.bundleKeyLabel
import com.example.composesample.presentation.example.component.system.security.describeError
import com.example.composesample.presentation.example.component.system.security.kernelLineSupported
import com.example.composesample.presentation.example.component.system.security.missingFixes
import com.example.composesample.presentation.example.component.system.security.monthsBehind
import com.example.composesample.presentation.example.component.system.security.pickOne
import com.example.composesample.presentation.example.component.system.security.severitySummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.UndeclaredThrowableException

/**
 * Security State 예제의 순수 로직 검증.
 * given : 기기·게시 SPL 문자열, 심각도별 CVE 집합, 예외 사슬
 * when : 개월 차이 / 아직 없는 수정 / 요약·라벨
 * then : 화면에 보일 값
 */
class SecurityStateLabTest {

    @Test
    fun `날짜형 SPL 은 연월로 개월 차이를 센다`() {
        assertEquals(19, monthsBehind("2025-03-01", "2026-10-01"))
        assertEquals(0, monthsBehind("2026-10-05", "2026-10-01"))
        assertNull(monthsBehind("5.4.86", "5.4.302"))
    }

    @Test
    fun `게시 SPL 기준 수정에서 기기 SPL 기준 수정을 빼면 아직 없는 수정`() {
        val atDevice = mapOf("CRITICAL" to setOf("CVE-2024-0001"), "HIGH" to setOf("CVE-2024-0002"))
        val atPublished = mapOf(
            "CRITICAL" to setOf("CVE-2024-0001", "CVE-2026-1000"),
            "HIGH" to setOf("CVE-2024-0002"),
            "MODERATE" to setOf("CVE-2026-2000")
        )
        val missing = missingFixes(atPublished, atDevice)
        assertEquals(mapOf("CRITICAL" to setOf("CVE-2026-1000"), "MODERATE" to setOf("CVE-2026-2000")), missing)
        assertFalse(missing.containsKey("HIGH"))
    }

    @Test
    fun `요약은 심각도 순서이고 0건은 뺀다`() {
        val map = mapOf("HIGH" to setOf("a", "b"), "CRITICAL" to setOf("c"), "LOW" to emptySet())
        assertEquals("CRITICAL 1 · HIGH 2", severitySummary(map))
        assertEquals("없음", severitySummary(emptyMap()))
    }

    @Test
    fun `시연용 CVE 는 가장 심각한 쪽에서 정렬해 하나`() {
        assertEquals("CVE-2026-0002", pickOne(mapOf("HIGH" to setOf("CVE-2025-9"), "CRITICAL" to setOf("CVE-2026-0003", "CVE-2026-0002"))))
        assertNull(pickOne(emptyMap()))
    }

    @Test
    fun `Bundle 키 라벨 — 알려진 키 외에는 패키지 버전`() {
        assertEquals("시스템 SPL", bundleKeyLabel("system_spl"))
        assertEquals("패키지 버전 · com.google.android.modulemetadata", bundleKeyLabel("com.google.android.modulemetadata"))
    }

    @Test
    fun `예외는 원인 사슬에서 메시지가 있는 가장 안쪽으로 설명한다`() {
        val wrapped = UndeclaredThrowableException(IllegalStateException("Kernel version not available."))
        assertEquals("IllegalStateException: Kernel version not available.", describeError(wrapped))
    }

    @Test
    fun `커널은 CVE 집계 대상이 아니다`() {
        assertTrue(SpComponent.SYSTEM.cveCapable)
        assertTrue(SpComponent.SYSTEM_MODULES.cveCapable)
        assertFalse(SpComponent.KERNEL.cveCapable)
    }

    @Test
    fun `커널 계열이 게시 LTS 목록에 없으면 지원 종료`() {
        val published = listOf("5.4.254", "5.15.211", "5.10.257")
        assertEquals(false, kernelLineSupported("4.14.190", published))
        assertEquals(true, kernelLineSupported("5.10.198", published))
        assertNull(kernelLineSupported("unknown", published))
    }
}
