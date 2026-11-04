package com.example.composesample.example

import androidx.compose.material3.DatePickerState
import com.example.composesample.presentation.example.component.ui.material3.canonicalUtcMidnight
import com.example.composesample.presentation.example.component.ui.material3.combineRight
import com.example.composesample.presentation.example.component.ui.material3.combineWrong
import com.example.composesample.presentation.example.component.ui.material3.isSelectableRight
import com.example.composesample.presentation.example.component.ui.material3.isSelectableWrong
import com.example.composesample.presentation.example.component.ui.material3.localMidnightMillis
import com.example.composesample.presentation.example.component.ui.material3.summarizeRange
import com.example.composesample.presentation.example.component.ui.material3.twelveHourLabel
import com.example.composesample.presentation.example.component.ui.material3.utcDateOf
import com.example.composesample.presentation.example.component.ui.material3.utcMidnightMillis
import com.example.composesample.presentation.example.component.ui.material3.wallClockOf
import com.example.composesample.presentation.example.component.ui.material3.weekRules
import com.example.composesample.presentation.example.component.ui.material3.zonedDateOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/**
 * M3 날짜·시간 선택기 예제 — 기대값은 구현이 아니라 달력 사실로 쓴다:
 * 2026-11-04 는 수요일, 서울은 UTC+9, LA 는 2026-11-01 서머타임 종료 뒤 UTC-8.
 * 2026-11-04T00:00Z = 1793750400000 (1970-01-01 부터 20,761일 × 86,400,000).
 */
class DateTimePickerLabTest {

    private val seoul = ZoneId.of("Asia/Seoul")
    private val la = ZoneId.of("America/Los_Angeles")
    private val nov4 = LocalDate.of(2026, 11, 4)
    private val nov4UtcMidnight = 1_793_750_400_000L

    @Test
    fun `11월 4일의 UTC 자정 millis 는 달력 계산과 같다`() {
        assertEquals(20_761L * 86_400_000L, nov4UtcMidnight)
        assertEquals(nov4UtcMidnight, utcMidnightMillis(nov4))
    }

    @Test
    fun `읽기 함정 - UTC 자정을 LA 시간대로 읽으면 하루 전, 서울은 같은 날`() {
        assertEquals(nov4, utcDateOf(nov4UtcMidnight))
        assertEquals(LocalDate.of(2026, 11, 3), zonedDateOf(nov4UtcMidnight, la))
        assertEquals(nov4, zonedDateOf(nov4UtcMidnight, seoul))
    }

    @Test
    fun `쓰기 함정 - 서울 자정은 전날 15시 UTC 라 M3 가 3일로 맞춘다, LA 자정은 같은 날`() {
        val seoulMidnight = localMidnightMillis(nov4, seoul)
        assertEquals(nov4UtcMidnight - 9 * 3_600_000L, seoulMidnight)
        assertEquals(LocalDate.of(2026, 11, 3), utcDateOf(canonicalUtcMidnight(seoulMidnight)))
        assertEquals(nov4, utcDateOf(canonicalUtcMidnight(localMidnightMillis(nov4, la))))
    }

    @Test
    fun `실제 M3 DatePickerState 도 서울 자정을 넘기면 하루 전을 선택한다`() {
        // 화면이 옮긴 규칙(canonicalUtcMidnight)이 라이브러리와 같은지 — JVM 에서는 LegacyCalendarModelImpl(Calendar UTC) 경로
        val wrong = DatePickerState(Locale.KOREA, initialSelectedDateMillis = localMidnightMillis(nov4, seoul))
        val right = DatePickerState(Locale.KOREA, initialSelectedDateMillis = utcMidnightMillis(nov4))
        assertEquals(LocalDate.of(2026, 11, 3), utcDateOf(wrong.selectedDateMillis!!))
        assertEquals(nov4UtcMidnight, right.selectedDateMillis)
        assertEquals(canonicalUtcMidnight(localMidnightMillis(nov4, seoul)), wrong.selectedDateMillis)
    }

    @Test
    fun `now 와 비교하는 규칙은 낮 시간에 오늘을 막는다`() {
        val afternoonSeoul = nov4UtcMidnight + 5 * 3_600_000L // 11/4 14:00 KST = 05:00 UTC
        assertFalse(isSelectableWrong(nov4UtcMidnight, afternoonSeoul, seoul))
        assertTrue(isSelectableRight(nov4UtcMidnight, nov4))
    }

    @Test
    fun `서울 새벽에는 now 비교도 오늘을 열어 둔다 - 시각에 따라 결과가 바뀐다`() {
        val dawnSeoul = nov4UtcMidnight - 6 * 3_600_000L // 11/4 03:00 KST = 11/3 18:00 UTC
        assertTrue(isSelectableWrong(nov4UtcMidnight, dawnSeoul, seoul))
    }

    @Test
    fun `LA 시간대로 요일을 읽으면 주말 막기가 금토로 밀린다`() {
        val now = nov4UtcMidnight + 20 * 3_600_000L // 11/4 12:00 PST
        val rules = weekRules(now, la).associateBy { it.dayOfWeek }
        // 바른 규칙: 토 · 일 막힘, 금 열림
        assertTrue(rules.getValue("금").right)
        assertFalse(rules.getValue("토").right)
        assertFalse(rules.getValue("일").right)
        // 잘못된 규칙: 칸의 UTC 자정은 LA 로 전날 16:00 → 토 칸이 금요일로 읽혀 열리고, 월 칸이 일요일로 읽혀 막힌다
        assertTrue("토 칸이 금요일로 읽혀 열림", rules.getValue("토").wrong)
        assertFalse("월 칸이 일요일로 읽혀 막힘", rules.getValue("월").wrong)
    }

    @Test
    fun `서울에서 날짜 millis 에 9시를 더하면 18시가 된다`() {
        val wrong = combineWrong(nov4UtcMidnight, 9, 0)
        val right = combineRight(nov4UtcMidnight, 9, 0, seoul)
        assertEquals("2026-11-04 18:00", wallClockOf(wrong, seoul))
        assertEquals("2026-11-04 09:00", wallClockOf(right, seoul))
        assertEquals(9 * 3_600_000L, wrong - right)
    }

    @Test
    fun `LA 에서는 반대로 8시간 이르다`() {
        val wrong = combineWrong(nov4UtcMidnight, 9, 0)
        assertEquals("2026-11-04 01:00", wallClockOf(wrong, la))
        assertEquals("2026-11-04 09:00", wallClockOf(combineRight(nov4UtcMidnight, 9, 0, la), la))
    }

    @Test
    fun `기간은 포함 일수와 박 수가 하루 차이`() {
        val summary = summarizeRange(utcMidnightMillis(nov4), utcMidnightMillis(LocalDate.of(2026, 11, 7)))!!
        assertEquals(4L, summary.days)
        assertEquals(3L, summary.nights)
        val sameDay = summarizeRange(utcMidnightMillis(nov4), utcMidnightMillis(nov4))!!
        assertEquals(1L, sameDay.days)
        assertEquals(0L, sameDay.nights)
    }

    @Test
    fun `끝 날짜 없이 확인하면 끝은 비고 시작이 없으면 결과가 없다`() {
        val open = summarizeRange(utcMidnightMillis(nov4), null)!!
        assertNull(open.end)
        assertNull(open.days)
        assertNull(summarizeRange(null, null))
    }

    @Test
    fun `12시간 표기는 표시만 바꾼다 - 0시는 오전 12시, 12시는 오후 12시`() {
        assertEquals("오전 12:05", twelveHourLabel(0, 5))
        assertEquals("오후 12:00", twelveHourLabel(12, 0))
        assertEquals("오후 11:59", twelveHourLabel(23, 59))
    }
}
