// NewApi 억제: 이 파일만 java.time(API 26+)을 쓴다. 프로젝트는 desugaring 을 켜지 않았고(minSdk 24),
// 화면은 진입부(DateTimePickerExampleUI) 한 곳의 SDK_INT 분기로 API 26 미만에서 이 파일을 부르지 않는다.
// 화면 쪽에는 java.time 타입을 넘기지 않는다 — 문자열 · millis · SampleZone 만 오간다.
@file:Suppress("NewApi")

package com.example.composesample.presentation.example.component.ui.material3

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * M3 날짜·시간 선택기 예제의 순수 로직 — 화면(DateTimePickerExampleUI)과 JVM 단위 테스트(DateTimePickerLabTest)가 함께 쓴다.
 * M3 의 날짜 값(DatePickerState.selectedDateMillis · SelectableDates 의 utcTimeMillis)은 모두 **그날 UTC 자정의 epoch millis** 다.
 * java.time 은 API 26+ (이 프로젝트는 desugaring 을 켜지 않았다) → 화면은 API 26 미만에서 안내만 보인다.
 * 아래 '화면용' 함수들은 java.time 타입 대신 문자열 · millis 를 돌려준다.
 */

/** 비교에 쓰는 시간대 — 기기 시간대와 상관없이 같은 결과를 보이려고 이름으로 고정한다. */
enum class SampleZone(val label: String, val id: String) {
    SEOUL("서울 UTC+9", "Asia/Seoul"),
    LOS_ANGELES("LA UTC-8", "America/Los_Angeles"),
    UTC("UTC", "UTC")
}

fun SampleZone.zoneId(): ZoneId = ZoneId.of(id)


/** 바른 읽기 — UTC 로 날짜를 꺼낸다(M3 가 내부에서 쓰는 것과 같은 변환). */
fun utcDateOf(utcMillis: Long): LocalDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

/** 흔한 잘못 — 그 millis 를 어떤 시간대(대개 기기 시간대)의 날짜로 읽는다. UTC 보다 늦은(음수 오프셋) 곳에서 하루 전. */
fun zonedDateOf(utcMillis: Long, zone: ZoneId): LocalDate = Instant.ofEpochMilli(utcMillis).atZone(zone).toLocalDate()

/** 바른 쓰기 — 피커에 넘길 값은 그 날짜의 UTC 자정. */
fun utcMidnightMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** 흔한 잘못 — 로컬 자정(예: 서울 자정 = 전날 15:00 UTC)을 넘긴다. UTC 보다 빠른(양수 오프셋) 곳에서 하루 전이 선택된다. */
fun localMidnightMillis(date: LocalDate, zone: ZoneId): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

/**
 * M3 가 initialSelectedDateMillis 를 '그날의 시작'으로 맞추는 규칙(material3 1.4.0 CalendarModelImpl.getCanonicalDate:
 * `Instant.ofEpochMilli(t).atZone(ZoneId.of("UTC")).toLocalDate()`)을 그대로 옮긴 것 — 화면은 실제 DatePickerState 결과와 나란히 보인다.
 */
fun canonicalUtcMidnight(millis: Long): Long = utcMidnightMillis(utcDateOf(millis))

// ==================== SelectableDates ====================

private fun DayOfWeek.isWeekend() = this == DayOfWeek.SATURDAY || this == DayOfWeek.SUNDAY

/**
 * 흔한 잘못 두 가지를 한 번에 — ① 칸의 UTC 자정 millis 를 '지금'과 비교한다(오늘 칸의 UTC 자정은 대개 지금보다 이르다)
 * ② 요일을 지정 시간대(대개 기기 시간대)로 읽는다(음수 오프셋에서 토요일 칸이 금요일로 읽힌다).
 */
fun isSelectableWrong(cellUtcMillis: Long, nowMillis: Long, zone: ZoneId): Boolean =
    cellUtcMillis >= nowMillis && !zonedDateOf(cellUtcMillis, zone).dayOfWeek.isWeekend()

/** 바름 — 칸은 UTC 로 날짜로 바꾸고, '오늘'(사용자 시간대의 날짜)과 날짜끼리 비교한다. */
fun isSelectableRight(cellUtcMillis: Long, today: LocalDate): Boolean {
    val date = utcDateOf(cellUtcMillis)
    return !date.isBefore(today) && !date.dayOfWeek.isWeekend()
}

data class DayRule(val date: String, val dayOfWeek: String, val wrong: Boolean, val right: Boolean)

/** 어제부터 7일 — 어제 · 오늘 · 주말이 함께 들어온다. zone 은 '오늘'을 정하고 잘못된 규칙이 요일을 읽는 시간대. */
fun weekRules(nowMillis: Long, zone: ZoneId): List<DayRule> {
    val today = zonedDateOf(nowMillis, zone)
    return (-1L..5L).map { offset ->
        val date = today.plusDays(offset)
        val cell = utcMidnightMillis(date)
        DayRule(
            date = date.toString(),
            dayOfWeek = KOREAN_DAYS[date.dayOfWeek.value - 1],
            wrong = isSelectableWrong(cell, nowMillis, zone),
            right = isSelectableRight(cell, today)
        )
    }
}

private val KOREAN_DAYS = listOf("월", "화", "수", "목", "금", "토", "일")

// ==================== 날짜 + 시간 ====================

/** 흔한 잘못 — 날짜 millis(UTC 자정)에 시 · 분을 더한다 → 그 시각은 UTC 벽시계 시각이 된다. */
fun combineWrong(dateUtcMillis: Long, hour: Int, minute: Int): Long =
    dateUtcMillis + hour * MILLIS_PER_HOUR + minute * MILLIS_PER_MINUTE

/** 바름 — UTC 로 날짜를 꺼내 사용자 시간대의 그 날짜 · 그 시각으로. */
fun combineRight(dateUtcMillis: Long, hour: Int, minute: Int, zone: ZoneId): Long =
    utcDateOf(dateUtcMillis).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

private const val MILLIS_PER_HOUR = 3_600_000L
private const val MILLIS_PER_MINUTE = 60_000L

private val WALL_CLOCK = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/** 그 순간이 zone 의 벽시계로 몇 시인가. */
fun wallClockOf(millis: Long, zone: ZoneId): String = Instant.ofEpochMilli(millis).atZone(zone).format(WALL_CLOCK)

/** TimePickerState.hour 는 is24Hour 와 상관없이 0..23 — 12시간 표기는 표시에서만 바꾼다. */
fun twelveHourLabel(hour: Int, minute: Int): String {
    val period = if (hour < 12) "오전" else "오후"
    val h = (hour % 12).let { if (it == 0) 12 else it }
    return "$period $h:${minute.toString().padStart(2, '0')}"
}

// ==================== 기간 ====================

data class RangeSummary(val start: String, val end: String?, val days: Long?, val nights: Long?)

/** DateRangePicker 의 두 값(UTC 자정) → 포함 일수 · 박 수. 끝 날짜 없이 확인하면 end 가 null. */
fun summarizeRange(startUtcMillis: Long?, endUtcMillis: Long?): RangeSummary? {
    val start = startUtcMillis?.let { utcDateOf(it) } ?: return null
    val end = endUtcMillis?.let { utcDateOf(it) } ?: return RangeSummary(start.toString(), null, null, null)
    val nights = ChronoUnit.DAYS.between(start, end)
    return RangeSummary(start.toString(), end.toString(), nights + 1, nights)
}

// ==================== 화면용 — java.time 타입을 밖으로 내보내지 않는다 ====================

fun deviceZoneName(): String = ZoneId.systemDefault().id

fun utcDateText(utcMillis: Long): String = utcDateOf(utcMillis).toString()

private fun shiftLabel(date: LocalDate, expected: LocalDate): String = when {
    date.isBefore(expected) -> "  ✗ 하루 전"
    date.isAfter(expected) -> "  ✗ 하루 뒤"
    else -> ""
}

data class ReadReport(val utc: String, val device: String, val deviceShift: String, val la: String, val laShift: String)

/** 1번 카드 — 같은 millis 를 UTC · 기기 시간대 · LA 로 읽는다. */
fun readReport(utcMillis: Long): ReadReport {
    val utc = utcDateOf(utcMillis)
    val device = zonedDateOf(utcMillis, ZoneId.systemDefault())
    val la = zonedDateOf(utcMillis, SampleZone.LOS_ANGELES.zoneId())
    return ReadReport(utc.toString(), device.toString(), shiftLabel(device, utc), la.toString(), shiftLabel(la, utc))
}

data class InitialValues(val today: String, val localMidnight: Long, val utcMidnight: Long)

/** 2번 카드 — 그 시간대의 '오늘'과, 피커에 넘길 두 후보(로컬 자정 · UTC 자정). */
fun initialValues(zone: SampleZone, nowMillis: Long): InitialValues {
    val today = zonedDateOf(nowMillis, zone.zoneId())
    return InitialValues(today.toString(), localMidnightMillis(today, zone.zoneId()), utcMidnightMillis(today))
}

/** 피커가 고른 날(UTC 자정 millis)이 기대한 날짜(yyyy-MM-dd)와 다르면 표시. */
fun shiftFrom(selectedUtcMillis: Long, expectedDate: String): String =
    shiftLabel(utcDateOf(selectedUtcMillis), LocalDate.parse(expectedDate))

fun weekRules(nowMillis: Long, zone: SampleZone): List<DayRule> = weekRules(nowMillis, zone.zoneId())

fun wallClockOf(millis: Long, zone: SampleZone): String = wallClockOf(millis, zone.zoneId())

fun isSelectableWrong(cellUtcMillis: Long, nowMillis: Long, zone: SampleZone): Boolean =
    isSelectableWrong(cellUtcMillis, nowMillis, zone.zoneId())

/** 바른 규칙 — '오늘'은 그 시간대에서 지금의 날짜. */
fun isSelectableRight(cellUtcMillis: Long, nowMillis: Long, zone: SampleZone): Boolean =
    isSelectableRight(cellUtcMillis, zonedDateOf(nowMillis, zone.zoneId()))

data class TimeReport(val date: String, val right: String, val wrong: String, val diffHours: Double, val zone: String)

/** 5번 카드 — 날짜(없으면 기기 시간대의 오늘)에 시각을 두 방식으로 합쳐 기기 시간대 벽시계로. */
fun timeReport(pickedDateMillis: Long?, hour: Int, minute: Int, nowMillis: Long): TimeReport {
    val zone = ZoneId.systemDefault()
    val date = pickedDateMillis ?: utcMidnightMillis(zonedDateOf(nowMillis, zone))
    val right = combineRight(date, hour, minute, zone)
    val wrong = combineWrong(date, hour, minute)
    return TimeReport(utcDateText(date), wallClockOf(right, zone), wallClockOf(wrong, zone), (wrong - right) / 3_600_000.0, zone.id)
}
