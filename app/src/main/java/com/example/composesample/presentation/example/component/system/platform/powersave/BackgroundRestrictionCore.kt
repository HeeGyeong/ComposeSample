package com.example.composesample.presentation.example.component.system.platform.powersave

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 절전 모드 예제 '백그라운드 제한 진단' 탭의 순수 로직 — 화면·ViewModel·단위 테스트가 함께 쓴다.
 *
 * 앱이 백그라운드에서 막히는 층(배터리 세이버 · Doze · 앱 대기 버킷 · 사용자의 '제한' · 강제 중지)을 한 시점의 값
 * [RestrictionSnapshot] 으로 모으고, [diagnose] 가 공식 문서(Power management resource limits · Doze and App Standby)와
 * 각 API 의 javadoc 에 적힌 제한으로 '지금 무엇이 막는가'를 판정한다.
 *
 * 프레임워크 상수(STANDBY_BUCKET_* API 28 · ApplicationExitInfo.REASON_* API 30 · ApplicationStartInfo API 35)는 값으로 옮겨 둔다.
 * minSdk 24 에서 상수를 그대로 쓰면 lint InlinedApi 경고가 늘고, 두 값이 같은지는 단위 테스트가 확인한다.
 */

/** 앱 대기 버킷 — UsageStatsManager.STANDBY_BUCKET_* 값. 숫자가 클수록 제한이 세다 */
enum class StandbyBucket(val code: Int, val limits: String) {
    // 숨은 값(@hide STANDBY_BUCKET_EXEMPTED) — 시스템이 예외로 둔 앱은 ACTIVE 보다 작은 값을 받는다
    EXEMPTED(5, "버킷 제한 없음(시스템이 예외로 둔 앱 — 공개 상수에 없는 값)"),
    ACTIVE(10, "작업 제한 없음(Android 16 부터 60분당 20분) · 알람 제한 없음 · 네트워크 제한 없음"),
    WORKING_SET(20, "작업 4시간당 10분 · 알람 시간당 10개 · 네트워크 제한 없음"),
    FREQUENT(30, "작업 12시간당 10분 · 알람 시간당 2개 · 네트워크 제한 없음"),
    RARE(40, "작업 24시간당 10분 · 알람 시간당 1개 · 백그라운드 네트워크 차단"),
    RESTRICTED(45, "작업 하루 1번 10분 묶음 · 알람 하루 1개 · 백그라운드 네트워크 차단");

    companion object {
        /** 표에 없는 값(숨은 NEVER 50 등)은 null — 화면은 숫자를 그대로 보인다 */
        fun fromCode(code: Int): StandbyBucket? = entries.firstOrNull { it.code == code }

        fun label(code: Int): String = fromCode(code)?.let { "${it.name}($code)" } ?: "알 수 없는 값($code)"
    }
}

/** ApplicationExitInfo.REASON_* 값(API 30) */
enum class ExitReason(val code: Int, val label: String) {
    UNKNOWN(0, "알 수 없음"),
    EXIT_SELF(1, "앱이 스스로 종료"),
    SIGNALED(2, "시그널로 종료"),
    LOW_MEMORY(3, "메모리 부족"),
    CRASH(4, "크래시"),
    CRASH_NATIVE(5, "네이티브 크래시"),
    ANR(6, "ANR"),
    INITIALIZATION_FAILURE(7, "초기화 실패"),
    PERMISSION_CHANGE(8, "권한 변경"),
    EXCESSIVE_RESOURCE_USAGE(9, "자원 과다 사용"),
    USER_REQUESTED(10, "사용자 요청"),
    USER_STOPPED(11, "기기 사용자(멀티 유저) 중지"),
    DEPENDENCY_DIED(12, "의존 프로세스 종료"),
    OTHER(13, "기타(시스템)"),
    FREEZER(14, "프리저"),
    PACKAGE_STATE_CHANGE(15, "패키지 상태 변경"),
    PACKAGE_UPDATED(16, "앱 업데이트");

    companion object {
        fun label(code: Int): String = entries.firstOrNull { it.code == code }?.let { "${it.name} — ${it.label}" } ?: "알 수 없는 값($code)"
    }
}

/** ApplicationStartInfo.START_REASON_* 값(API 35) */
enum class StartReason(val code: Int, val label: String) {
    ALARM(0, "알람"),
    BACKUP(1, "백업"),
    BOOT_COMPLETE(2, "부팅 완료"),
    BROADCAST(3, "브로드캐스트"),
    CONTENT_PROVIDER(4, "콘텐츠 프로바이더"),
    JOB(5, "작업(JobScheduler)"),
    LAUNCHER(6, "런처"),
    LAUNCHER_RECENTS(7, "최근 앱"),
    OTHER(8, "기타"),
    PUSH(9, "푸시"),
    SERVICE(10, "서비스"),
    START_ACTIVITY(11, "액티비티 시작");

    companion object {
        fun label(code: Int): String = entries.firstOrNull { it.code == code }?.let { "${it.name} — ${it.label}" } ?: "알 수 없는 값($code)"
    }
}

/** ApplicationStartInfo.START_TYPE_* 값(API 35) */
fun startTypeLabel(code: Int): String = when (code) {
    0 -> "UNSET"
    1 -> "COLD"
    2 -> "WARM"
    3 -> "HOT"
    else -> "알 수 없는 값($code)"
}

/** 프로세스 종료 기록 한 건(ApplicationExitInfo 에서 옮긴 값) */
data class ExitRecord(
    val reason: Int,
    val timestamp: Long,
    val processName: String,
    val importance: Int,
    val description: String?
)

/** 프로세스 시작 기록 한 건(ApplicationStartInfo 에서 옮긴 값) — 시각은 부팅 기준 단조 시계(ms) */
data class StartRecord(
    val reason: Int,
    val startType: Int,
    val wasForceStopped: Boolean,
    val launchUptimeMillis: Long? = null,
    val forkUptimeMillis: Long? = null
)

/** 버킷이 바뀐 시점(UsageEvents.Event.STANDBY_BUCKET_CHANGED) */
data class BucketChange(val timestamp: Long, val bucket: Int)

/** 한 시점에 읽은 값 — null 은 '이 API 레벨에서는 읽을 수 없음' */
data class RestrictionSnapshot(
    val sdkInt: Int,
    val readAt: Long,
    /** 읽은 순간의 SystemClock.uptimeMillis — 시작 기록 시각과 같은 시계 */
    val readUptimeMillis: Long = 0L,
    /** Process.getStartUptimeMillis — 이번 프로세스가 시작된 시각 */
    val processStartUptimeMillis: Long = 0L,
    val powerSave: Boolean,
    val ignoringBatteryOptimizations: Boolean,
    val deviceIdle: Boolean,
    /** API 33+ */
    val lightIdle: Boolean?,
    /** API 28+ */
    val standbyBucket: Int?,
    /** API 28+ */
    val backgroundRestricted: Boolean?,
    /** API 33+ */
    val lowPowerStandby: Boolean?,
    /** API 34+ — 저전력 대기가 꺼져 있어도 true */
    val lowPowerStandbyExempt: Boolean?,
    /** API 30+ · 최신 순 */
    val exits: List<ExitRecord>?,
    /** API 35+ · 최신 순 — 첫 기록이 이번 실행이라는 보장은 없다([currentStart]) */
    val starts: List<StartRecord>?,
    /** API 28+ · 최신 순 · 기기 잠금 해제 전이면 null */
    val bucketHistory: List<BucketChange>?
)

/** 판정 등급 — 위에 있을수록 백그라운드에 미치는 영향이 크다 */
enum class Severity(val label: String) {
    BLOCKED("막힘"),
    DEFERRED("미뤄짐"),
    HISTORY("이력"),
    NOTE("참고"),
    CLEAR("정상")
}

data class Finding(val severity: Severity, val title: String, val detail: String)

/** 시작 기록의 프로세스 생성(fork) 시각이 이번 프로세스 시작 시각과 이만큼 안이면 이번 실행의 기록으로 본다 */
const val START_MATCH_TOLERANCE_MILLIS = 5_000L

/**
 * 시작 기록 중 이번 프로세스의 것. 목록의 첫 기록을 그대로 쓰면 안 된다 — Pixel Tablet API 35 에뮬레이터에서
 * 부팅 직후에는 이번 실행의 기록이 없이 지난 부팅의 기록만 돌아왔고, 기록이 들어온 뒤에도 지난 부팅의 기록(부팅 기준 1449초)이
 * 이번 실행 기록(1399초)보다 앞에 있었다. fork(없으면 launch) 시각이 [RestrictionSnapshot.processStartUptimeMillis] 와 가까운 기록만 고른다.
 */
fun currentStart(s: RestrictionSnapshot): StartRecord? = s.starts?.firstOrNull { record ->
    val at = record.forkUptimeMillis ?: record.launchUptimeMillis
    at != null && abs(at - s.processStartUptimeMillis) <= START_MATCH_TOLERANCE_MILLIS
}

/**
 * 한 시점의 값으로 '지금 무엇이 백그라운드를 막는가'를 판정한다. 결과는 [Severity] 순(같은 등급은 아래 규칙 순서).
 *
 * 제한 내용의 출처: ActivityManager.isBackgroundRestricted · PowerManager.isLowPowerStandbyEnabled ·
 * isDeviceLightIdleMode · ApplicationExitInfo.REASON_USER_REQUESTED · ApplicationStartInfo.wasForceStopped 의 javadoc,
 * 버킷·Doze 는 developer.android.com 'Power management resource limits'·'Optimize for Doze and App Standby' 표.
 */
fun diagnose(s: RestrictionSnapshot): List<Finding> {
    val findings = mutableListOf<Finding>()

    if (s.backgroundRestricted == true) {
        findings += Finding(
            Severity.BLOCKED,
            "사용자가 이 앱의 백그라운드 사용을 제한함",
            "앱 화면이 앞에 없으면 작업·알람이 실행되지 않고 포그라운드 서비스도 시작할 수 없다. 충전 중에도 그대로다. " +
                "앱이 풀 수 없고 사용자만 앱 정보 → 배터리에서 바꿀 수 있다."
        )
    }

    val bucket = s.standbyBucket?.let { StandbyBucket.fromCode(it) }
    if (bucket == StandbyBucket.RESTRICTED) {
        findings += Finding(Severity.BLOCKED, "앱 대기 버킷 RESTRICTED", bucket.limits + ". 충전 중에도 유지된다.")
    }

    if (s.lowPowerStandby == true && s.lowPowerStandbyExempt != true) {
        findings += Finding(
            Severity.DEFERRED,
            "저전력 대기(Low Power Standby) 켜짐",
            "화면이 꺼진 동안 Doze 정비 창 밖에서는 네트워크가 끊기고 wake lock 이 무시된다(포그라운드 서비스 포함)."
        )
    }

    if (s.deviceIdle) {
        val exempt = if (s.ignoringBatteryOptimizations) {
            " 이 앱은 배터리 최적화 예외라 네트워크와 부분 wake lock 은 쓸 수 있다 — 일반 알람·작업이 미뤄지는 것은 같다."
        } else ""
        findings += Finding(
            Severity.DEFERRED,
            "기기가 Doze(깊은 유휴)",
            "네트워크 접근이 끊기고 wake lock 이 무시된다. 일반 작업·부정확 알람은 정비 창까지 미뤄지고, " +
                "FCM 은 높은 우선순위만 바로 온다.$exempt"
        )
    } else if (s.lightIdle == true) {
        findings += Finding(
            Severity.DEFERRED,
            "기기가 가벼운 Doze",
            "화면이 잠시 꺼진 단계 — 작업·동기화·네트워크를 정해진 주기에 몰아서 실행한다. 정비 창 동안에는 false 로 읽힌다."
        )
    }

    if (bucket == StandbyBucket.RARE) {
        findings += Finding(Severity.DEFERRED, "앱 대기 버킷 RARE", bucket.limits + ".")
    }

    if (s.powerSave) {
        findings += Finding(
            Severity.DEFERRED,
            "배터리 세이버 켜짐",
            "기기 전체 절전 — 백그라운드 작업·위치·네트워크가 평소보다 세게 제한된다(범위는 버전·제조사마다 다르다)."
        )
    }

    // REASON_USER_REQUESTED 하나가 강제 중지 · 최근 앱에서 제거 · (Android 14 전) 앱 업데이트에 함께 쓰인다.
    // 강제 중지(stopped 상태)였는지는 API 35 의 wasForceStopped 로만 확정되고, 그 아래에서는 시스템 설명 문자열로 짐작할 뿐이다
    val lastStart = currentStart(s)
    val lastExit = s.exits?.firstOrNull()
    val userRequested = lastExit?.reason == ExitReason.USER_REQUESTED.code
    val said = lastExit?.description?.let { " 시스템 설명: \"$it\"." } ?: ""
    when {
        lastStart?.wasForceStopped == true -> findings += Finding(
            Severity.HISTORY,
            "이번 프로세스는 강제 중지 뒤 첫 실행",
            "강제 중지된 동안 알람·작업·브로드캐스트가 오지 않았고, 지워진 알람·작업·콜백은 다시 등록해야 한다(wasForceStopped javadoc)." +
                if (userRequested) said else ""
        )

        userRequested && lastStart != null -> findings += Finding(
            Severity.NOTE,
            "직전 종료 = 사용자 요청, 강제 중지는 아님",
            "wasForceStopped 가 false 다 — 최근 앱에서 제거 같은 종료라 앱이 강제 중지(stopped) 상태에 들어가지 않았다.$said"
        )

        userRequested -> findings += Finding(
            Severity.HISTORY,
            "직전 프로세스 종료 = 사용자 요청",
            "같은 값이 설정의 '강제 중지'와 최근 앱에서 제거(javadoc), Android 14 전에는 앱 업데이트에도 쓰인다. " +
                "강제 중지였다면 앱을 다시 열 때까지 알람·작업·브로드캐스트가 오지 않았다 — 어느 쪽인지는 이번 실행의 시작 기록(API 35 wasForceStopped)으로만 확정된다.$said"
        )
    }

    if (bucket == StandbyBucket.FREQUENT || bucket == StandbyBucket.WORKING_SET) {
        findings += Finding(Severity.NOTE, "앱 대기 버킷 ${bucket.name}", bucket.limits + ".")
    }

    if (s.ignoringBatteryOptimizations) {
        findings += Finding(
            Severity.NOTE,
            "배터리 최적화 예외",
            "Doze·앱 대기 중에도 네트워크와 부분 wake lock 을 쓸 수 있다. 일반 알람·작업이 미뤄지는 것은 다른 앱과 같다."
        )
    }

    if (bucket == StandbyBucket.ACTIVE || bucket == StandbyBucket.EXEMPTED) {
        findings += Finding(Severity.CLEAR, "앱 대기 버킷 ${bucket.name}", bucket.limits + ".")
    }
    if (s.backgroundRestricted == false) {
        findings += Finding(Severity.CLEAR, "백그라운드 사용 제한 없음", "사용자가 이 앱을 '제한'으로 두지 않았다.")
    }

    return findings.sortedBy { it.severity.ordinal }
}

/** 판정 결과를 한 줄로 — 화면 맨 위 문장 */
fun headline(findings: List<Finding>): String {
    val active = findings.firstOrNull { it.severity == Severity.BLOCKED || it.severity == Severity.DEFERRED }
    if (active != null) return "지금 백그라운드를 가장 세게 막는 것: ${active.title}"
    val history = findings.firstOrNull { it.severity == Severity.HISTORY }
    if (history != null) return "지금 막는 것은 없다 — 단, ${history.title}"
    return "지금 이 기기에서 백그라운드를 막는 것은 없다"
}

// SimpleDateFormat 은 스레드 안전하지 않다 — 브로드캐스트·화면 갱신이 섞여 부르므로 호출마다 만든다
fun formatTime(millis: Long): String = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(Date(millis))
