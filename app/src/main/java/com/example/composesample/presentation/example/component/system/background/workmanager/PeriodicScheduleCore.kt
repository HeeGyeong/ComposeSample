package com.example.composesample.presentation.example.component.system.background.workmanager

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.abs

/**
 * '주기 작업 실행 시각' 탭의 순수 로직 — 화면·ViewModel·워커·단위 테스트가 함께 쓴다.
 *
 * 주기 작업의 다음 실행 시각은 WorkManager 가 WorkSpec 값으로 계산한다(work-runtime 2.12.0 `WorkSpec.calculateNextRunTime`).
 * [expectedNextRun] 은 그 규칙을 그대로 옮긴 것이고, 시나리오는 WorkInfo.nextScheduleTimeMillis 실측값을 이것과 맞춰 본다.
 */

/** 시나리오가 쓰는 고유 이름 두 개 — 시나리오마다 두 줄을 나란히 비교한다 */
enum class ScheduleLane(val uniqueName: String) {
    A("periodic-schedule-a"),
    B("periodic-schedule-b");

    companion object {
        fun fromName(name: String?): ScheduleLane? = entries.firstOrNull { it.name == name }
    }
}

/** 워커가 실행마다 할 일 */
enum class ScheduleWorkMode {
    /** 그냥 끝낸다 */
    PLAIN,

    /** 다음 :00/:30 슬롯을 계산해 자기 요청에 setNextScheduleTimeOverride 로 지정하고 성공 */
    SELF_SLOT,

    /** 재시도 — 백오프 그대로 */
    RETRY_PLAIN,

    /** 재시도 — 다음 시각을 오버라이드로 직접 지정(커스텀 백오프) */
    RETRY_OVERRIDE;

    val retries: Boolean get() = this == RETRY_PLAIN || this == RETRY_OVERRIDE

    companion object {
        fun fromName(name: String?): ScheduleWorkMode = entries.firstOrNull { it.name == name } ?: PLAIN
    }
}

/** 워커가 doWork 안에서 남기는 실행 기록 */
data class ScheduleRunRecord(
    val lane: ScheduleLane,
    val workId: UUID,
    val generation: Int,
    val mode: ScheduleWorkMode,
    val startedAt: Long,
    val endedAt: Long? = null,
    /** 워커가 직접 지정한 다음 시각(없으면 null) */
    val override: Long? = null,
    /** 워커 안 updateWork 의 결과(UpdateResult 이름) */
    val updateResult: String? = null,
    val retried: Boolean = false
)

/** 한 시점의 WorkInfo 값 + 그 시점에 기대한 다음 실행 시각 */
data class ScheduleSnapshot(
    val lane: ScheduleLane,
    val label: String,
    val takenAt: Long,
    val state: String?,
    val generation: Int?,
    val intervalMillis: Long?,
    val flexMillis: Long?,
    val runAttemptCount: Int?,
    /** ENQUEUED 일 때만 값이 있다(그 밖의 상태는 WorkInfo 가 Long.MAX_VALUE 를 준다) */
    val nextScheduleAt: Long?,
    /** 이 시점에 규칙으로 계산한 기대값과 그 근거 — 근거를 알 수 없으면 null */
    val expected: Long?,
    val expectedReason: String
) {
    /** 실측 − 기대(ms). 둘 중 하나라도 없으면 null */
    val deltaMillis: Long? get() = if (nextScheduleAt == null || expected == null) null else nextScheduleAt - expected

    /** lastEnqueueTime 은 워커가 끝난 직후 WorkManager 가 기록한다 — 워커가 남긴 끝 시각과 조금 다를 수 있다 */
    val matches: Boolean get() = deltaMillis?.let { abs(it) <= SCHEDULE_MATCH_TOLERANCE_MILLIS } == true
}

/** 주기 작업 최소 주기(PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS) — 오버라이드에도 적용된다 */
const val MIN_PERIODIC_MILLIS = 15 * 60_000L

/** WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS — 기본 백오프는 지수(EXPONENTIAL) 30초 */
const val DEFAULT_BACKOFF_MILLIS = 30_000L

/** 워커 끝 시각과 lastEnqueueTime 의 차이 허용치 */
const val SCHEDULE_MATCH_TOLERANCE_MILLIS = 3_000L

/**
 * WorkSpec.calculateNextRunTime 을 주기 작업 경우만 옮긴 것.
 *
 * - 오버라이드가 있으면: 첫 실행 전(periodCount 0)은 그 값 그대로, 그 뒤로는 max(오버라이드, 마지막 등록·실행 끝 + 15분)
 * - 재시도 중이면: 마지막 실행 끝 + 백오프(지수: 30초 × 2^(시도−1))
 * - 그 밖: 첫 실행 전은 등록 시각 + 초기 지연(flex 가 있으면 + (주기 − flex)), 그 뒤는 마지막 실행 끝 + 주기
 */
fun expectedNextRun(
    override: Long?,
    periodCount: Int,
    lastEnqueueTime: Long,
    intervalMillis: Long,
    flexMillis: Long = intervalMillis,
    initialDelayMillis: Long = 0,
    backedOff: Boolean = false,
    runAttemptCount: Int = 0,
    backoffMillis: Long = DEFAULT_BACKOFF_MILLIS
): Long = when {
    override != null ->
        if (periodCount == 0) override else maxOf(override, lastEnqueueTime + MIN_PERIODIC_MILLIS)
    backedOff -> lastEnqueueTime + backoffMillis * (1L shl (runAttemptCount - 1).coerceAtLeast(0))
    periodCount == 0 -> {
        val flexShift = if (flexMillis != intervalMillis) intervalMillis - flexMillis else 0
        lastEnqueueTime + initialDelayMillis + flexShift
    }
    else -> lastEnqueueTime + intervalMillis
}

/**
 * [now] + [minGapMillis] 이후의 첫 '벽시계 슬롯'(분이 [slotMinutes] 의 배수인 정각, 초·밀리초 0).
 * "매일 아침 7시 전에"를 줄인 데모 — 슬롯이 실행 끝 + 15분보다 뒤여야 오버라이드가 그대로 쓰인다(그래서 간격 16분).
 */
fun nextSlot(now: Long, slotMinutes: Int, minGapMillis: Long, zone: TimeZone = TimeZone.getDefault()): Long {
    val earliest = now + minGapMillis
    val calendar = Calendar.getInstance(zone).apply {
        timeInMillis = earliest
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MINUTE, -(get(Calendar.MINUTE) % slotMinutes))
    }
    if (calendar.timeInMillis < earliest) calendar.add(Calendar.MINUTE, slotMinutes)
    return calendar.timeInMillis
}

private val clockFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT)

/** 시:분:초.밀리초 — 슬롯 정각(…:00.000)과 실행 끝 기준(…:07.523)의 차이가 보이게 */
fun formatClock(millis: Long): String = clockFormat.format(Date(millis))

/** "+18분 41초" / "−2초" */
fun formatSigned(millis: Long): String {
    val sign = if (millis < 0) "−" else "+"
    val total = abs(millis) / 1_000
    val minutes = total / 60
    val seconds = total % 60
    return when {
        minutes > 0 -> "$sign${minutes}분 ${seconds}초"
        total > 0 -> "$sign${seconds}초"
        else -> "$sign${abs(millis)}ms"
    }
}
