package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.background.workmanager.DEFAULT_BACKOFF_MILLIS
import com.example.composesample.presentation.example.component.system.background.workmanager.MIN_PERIODIC_MILLIS
import com.example.composesample.presentation.example.component.system.background.workmanager.ScheduleLane
import com.example.composesample.presentation.example.component.system.background.workmanager.ScheduleSnapshot
import com.example.composesample.presentation.example.component.system.background.workmanager.expectedNextRun
import com.example.composesample.presentation.example.component.system.background.workmanager.formatSigned
import com.example.composesample.presentation.example.component.system.background.workmanager.nextSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * '주기 작업 실행 시각' 탭의 계산 검증 — work-runtime 2.12.0 WorkSpec.calculateNextRunTime 의 주기 작업 분기를 옮긴 것.
 * given : 등록·실행 끝 시각, 주기·flex, 오버라이드, 재시도
 * when : 다음 실행 시각 / 다음 벽시계 슬롯 계산
 * then : 화면이 실측값과 맞춰 볼 기대값
 */
class PeriodicScheduleCoreTest {

    private val utc = TimeZone.getTimeZone("UTC")
    private val minute = 60_000L
    private val interval = 30 * minute

    private fun at(hour: Int, min: Int, sec: Int, ms: Int = 0): Long = Calendar.getInstance(utc).apply {
        clear()
        set(2026, Calendar.OCTOBER, 22, hour, min, sec)
        set(Calendar.MILLISECOND, ms)
    }.timeInMillis

    @Test
    fun `주기만 쓰면 첫 실행은 등록 즉시, 그 뒤는 실행 끝 + 주기`() {
        val enqueued = at(9, 40, 0)
        assertEquals(enqueued, expectedNextRun(null, periodCount = 0, lastEnqueueTime = enqueued, intervalMillis = interval))
        val runEnd = at(9, 40, 7, 523)
        assertEquals(at(10, 10, 7, 523), expectedNextRun(null, periodCount = 1, lastEnqueueTime = runEnd, intervalMillis = interval))
    }

    @Test
    fun `flex 가 있으면 첫 실행만 주기 − flex 뒤`() {
        val enqueued = at(9, 40, 0)
        val first = expectedNextRun(null, 0, enqueued, interval, flexMillis = 10 * minute)
        assertEquals(enqueued + 20 * minute, first)
        val afterRun = expectedNextRun(null, 1, at(10, 0, 1), interval, flexMillis = 10 * minute)
        assertEquals(at(10, 30, 1), afterRun)
    }

    @Test
    fun `오버라이드는 첫 실행 전엔 그대로, 그 뒤엔 실행 끝 + 15분 아래로 못 당긴다`() {
        val now = at(9, 40, 0)
        assertEquals(now + minute, expectedNextRun(now + minute, 0, now, interval))
        val runEnd = at(9, 40, 3)
        assertEquals(runEnd + MIN_PERIODIC_MILLIS, expectedNextRun(now + minute, 1, runEnd, interval))
        assertEquals(at(10, 30, 0), expectedNextRun(at(10, 30, 0), 1, runEnd, interval))
    }

    @Test
    fun `재시도는 지수 백오프, 오버라이드가 있으면 그것이 앞선다`() {
        val runEnd = at(9, 40, 3)
        assertEquals(runEnd + DEFAULT_BACKOFF_MILLIS, expectedNextRun(null, 0, runEnd, interval, backedOff = true, runAttemptCount = 1))
        assertEquals(runEnd + 4 * DEFAULT_BACKOFF_MILLIS, expectedNextRun(null, 0, runEnd, interval, backedOff = true, runAttemptCount = 3))
        val custom = runEnd + 3 * minute
        assertEquals(custom, expectedNextRun(custom, 0, runEnd, interval, backedOff = true, runAttemptCount = 1))
    }

    @Test
    fun `다음 슬롯은 지금 + 16분 이후의 첫 30분 정각`() {
        val gap = 16 * minute
        assertEquals(at(10, 0, 0), nextSlot(at(9, 40, 7, 523), 30, gap, utc))
        assertEquals(at(10, 30, 0), nextSlot(at(9, 44, 1), 30, gap, utc))
        // 정확히 정각에 닿으면 그 정각
        assertEquals(at(10, 0, 0), nextSlot(at(9, 44, 0), 30, gap, utc))
    }

    @Test
    fun `스냅샷은 3초 이내면 일치로 본다`() {
        val base = ScheduleSnapshot(ScheduleLane.A, "x", 0, "ENQUEUED", 0, interval, interval, 0, nextScheduleAt = 10_000, expected = 8_500, expectedReason = "")
        assertTrue(base.matches)
        assertFalse(base.copy(nextScheduleAt = 20_000).matches)
        assertEquals("+1초", formatSigned(1_500))
        assertEquals("−2분 5초", formatSigned(-125_000))
    }
}
