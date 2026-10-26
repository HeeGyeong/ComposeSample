package com.example.composesample.example

import android.app.ApplicationExitInfo
import android.app.ApplicationStartInfo
import android.app.usage.UsageStatsManager
import com.example.composesample.presentation.example.component.system.platform.powersave.ExitReason
import com.example.composesample.presentation.example.component.system.platform.powersave.ExitRecord
import com.example.composesample.presentation.example.component.system.platform.powersave.RestrictionSnapshot
import com.example.composesample.presentation.example.component.system.platform.powersave.Severity
import com.example.composesample.presentation.example.component.system.platform.powersave.StandbyBucket
import com.example.composesample.presentation.example.component.system.platform.powersave.StartReason
import com.example.composesample.presentation.example.component.system.platform.powersave.START_MATCH_TOLERANCE_MILLIS
import com.example.composesample.presentation.example.component.system.platform.powersave.StartRecord
import com.example.composesample.presentation.example.component.system.platform.powersave.currentStart
import com.example.composesample.presentation.example.component.system.platform.powersave.diagnose
import com.example.composesample.presentation.example.component.system.platform.powersave.headline
import com.example.composesample.presentation.example.component.system.platform.powersave.startTypeLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * '백그라운드 제한 진단' 탭의 판정 검증.
 * given : 한 시점에 읽은 값(버킷·Doze·사용자 제한·예외 목록·종료/시작 기록)
 * when : diagnose / headline
 * then : 무엇이 막는지와 그 순서, 값으로 옮겨 둔 프레임워크 상수가 원본과 같은지
 */
class BackgroundRestrictionCoreTest {

    private companion object {
        const val PROCESS_START = 552_810L
    }

    private fun snapshot(
        sdkInt: Int = 35,
        powerSave: Boolean = false,
        ignoring: Boolean = false,
        deviceIdle: Boolean = false,
        lightIdle: Boolean? = false,
        bucket: Int? = StandbyBucket.ACTIVE.code,
        restricted: Boolean? = false,
        lowPowerStandby: Boolean? = false,
        lowPowerStandbyExempt: Boolean? = true,
        exits: List<ExitRecord>? = emptyList(),
        starts: List<StartRecord>? = emptyList()
    ) = RestrictionSnapshot(
        sdkInt = sdkInt,
        readAt = 0L,
        readUptimeMillis = 600_000L,
        processStartUptimeMillis = PROCESS_START,
        powerSave = powerSave,
        ignoringBatteryOptimizations = ignoring,
        deviceIdle = deviceIdle,
        lightIdle = lightIdle,
        standbyBucket = bucket,
        backgroundRestricted = restricted,
        lowPowerStandby = lowPowerStandby,
        lowPowerStandbyExempt = lowPowerStandbyExempt,
        exits = exits,
        starts = starts,
        bucketHistory = emptyList()
    )

    /** 이번 프로세스의 시작 기록 — fork 가 프로세스 시작 시각과 같다 */
    private fun thisStart(forceStopped: Boolean) =
        StartRecord(StartReason.LAUNCHER.code, startType = 1, wasForceStopped = forceStopped, launchUptimeMillis = PROCESS_START - 80, forkUptimeMillis = PROCESS_START - 20)

    private fun exit(reason: Int) = ExitRecord(reason, timestamp = 0L, processName = "p", importance = 400, description = null)

    @Test
    fun `값으로 옮긴 상수는 프레임워크 상수와 같다`() {
        assertEquals(UsageStatsManager.STANDBY_BUCKET_ACTIVE, StandbyBucket.ACTIVE.code)
        assertEquals(UsageStatsManager.STANDBY_BUCKET_WORKING_SET, StandbyBucket.WORKING_SET.code)
        assertEquals(UsageStatsManager.STANDBY_BUCKET_FREQUENT, StandbyBucket.FREQUENT.code)
        assertEquals(UsageStatsManager.STANDBY_BUCKET_RARE, StandbyBucket.RARE.code)
        assertEquals(UsageStatsManager.STANDBY_BUCKET_RESTRICTED, StandbyBucket.RESTRICTED.code)
        assertEquals(ApplicationExitInfo.REASON_USER_REQUESTED, ExitReason.USER_REQUESTED.code)
        assertEquals(ApplicationExitInfo.REASON_USER_STOPPED, ExitReason.USER_STOPPED.code)
        assertEquals(ApplicationExitInfo.REASON_ANR, ExitReason.ANR.code)
        assertEquals(ApplicationExitInfo.REASON_PACKAGE_UPDATED, ExitReason.PACKAGE_UPDATED.code)
        assertEquals(ApplicationStartInfo.START_REASON_LAUNCHER, StartReason.LAUNCHER.code)
        assertEquals(ApplicationStartInfo.START_REASON_START_ACTIVITY, StartReason.START_ACTIVITY.code)
        assertEquals("COLD", startTypeLabel(ApplicationStartInfo.START_TYPE_COLD))
        assertEquals("HOT", startTypeLabel(ApplicationStartInfo.START_TYPE_HOT))
    }

    @Test
    fun `막는 것이 없으면 정상만 남는다`() {
        val findings = diagnose(snapshot())
        assertTrue(findings.all { it.severity == Severity.CLEAR })
        assertEquals("지금 이 기기에서 백그라운드를 막는 것은 없다", headline(findings))
    }

    @Test
    fun `사용자 제한이 Doze 와 RARE 버킷보다 먼저 온다`() {
        val findings = diagnose(snapshot(restricted = true, deviceIdle = true, bucket = StandbyBucket.RARE.code))
        assertEquals(
            listOf("사용자가 이 앱의 백그라운드 사용을 제한함", "기기가 Doze(깊은 유휴)", "앱 대기 버킷 RARE"),
            findings.take(3).map { it.title }
        )
        assertEquals("지금 백그라운드를 가장 세게 막는 것: 사용자가 이 앱의 백그라운드 사용을 제한함", headline(findings))
    }

    @Test
    fun `RESTRICTED 버킷은 막힘, FREQUENT 는 참고`() {
        assertEquals(Severity.BLOCKED, diagnose(snapshot(bucket = StandbyBucket.RESTRICTED.code)).first().severity)
        val frequent = diagnose(snapshot(bucket = StandbyBucket.FREQUENT.code))
        assertEquals(Severity.NOTE, frequent.first { it.title == "앱 대기 버킷 FREQUENT" }.severity)
    }

    @Test
    fun `깊은 Doze 중이면 가벼운 Doze 는 따로 적지 않고, 예외 앱이면 네트워크 허용을 덧붙인다`() {
        val findings = diagnose(snapshot(deviceIdle = true, lightIdle = true, ignoring = true))
        assertFalse(findings.any { it.title == "기기가 가벼운 Doze" })
        assertTrue(findings.first { it.title == "기기가 Doze(깊은 유휴)" }.detail.contains("배터리 최적화 예외"))
        assertTrue(findings.any { it.title == "배터리 최적화 예외" && it.severity == Severity.NOTE })
    }

    @Test
    fun `가벼운 Doze 만이면 미뤄짐`() {
        val findings = diagnose(snapshot(lightIdle = true))
        assertEquals("기기가 가벼운 Doze", findings.first().title)
        assertEquals(Severity.DEFERRED, findings.first().severity)
    }

    @Test
    fun `시작 기록이 없는 API 에서 직전 종료가 사용자 요청이면 이력, 멀티 유저 중지(USER_STOPPED)는 아니다`() {
        val requested = diagnose(snapshot(sdkInt = 33, starts = null, exits = listOf(exit(ExitReason.USER_REQUESTED.code))))
        assertEquals(Severity.HISTORY, requested.first().severity)
        assertEquals("지금 막는 것은 없다 — 단, 직전 프로세스 종료 = 사용자 요청", headline(requested))

        val stopped = diagnose(snapshot(sdkInt = 33, starts = null, exits = listOf(exit(ExitReason.USER_STOPPED.code))))
        assertFalse(stopped.any { it.severity == Severity.HISTORY })
    }

    @Test
    fun `시스템 설명은 판정 문구에 그대로 붙는다`() {
        val record = ExitRecord(ExitReason.USER_REQUESTED.code, 0L, "p", 400, "remove task")
        val finding = diagnose(snapshot(sdkInt = 33, starts = null, exits = listOf(record))).first()
        assertTrue(finding.detail.endsWith("시스템 설명: \"remove task\"."))
    }

    @Test
    fun `wasForceStopped 가 true 면 강제 중지 한 건으로 묶는다`() {
        val findings = diagnose(
            snapshot(
                starts = listOf(thisStart(forceStopped = true)),
                exits = listOf(exit(ExitReason.USER_REQUESTED.code))
            )
        )
        assertEquals(listOf("이번 프로세스는 강제 중지 뒤 첫 실행"), findings.filter { it.severity == Severity.HISTORY }.map { it.title })
        assertEquals("지금 막는 것은 없다 — 단, 이번 프로세스는 강제 중지 뒤 첫 실행", headline(findings))
    }

    @Test
    fun `wasForceStopped 가 false 면 같은 사용자 요청 종료도 참고로 내린다`() {
        val findings = diagnose(
            snapshot(
                starts = listOf(thisStart(forceStopped = false)),
                exits = listOf(exit(ExitReason.USER_REQUESTED.code))
            )
        )
        assertFalse(findings.any { it.severity == Severity.HISTORY })
        assertEquals(Severity.NOTE, findings.first { it.title == "직전 종료 = 사용자 요청, 강제 중지는 아님" }.severity)
        assertEquals("지금 이 기기에서 백그라운드를 막는 것은 없다", headline(findings))
    }

    @Test
    fun `목록 첫 기록이 이번 실행이 아니면 wasForceStopped 를 쓰지 않는다`() {
        // 에뮬레이터 API 35 실측 모양 — 이번 부팅 기록 없이 지난 부팅의 기록(가동 1449초)만 돌아온다
        val stale = StartRecord(StartReason.START_ACTIVITY.code, startType = 1, wasForceStopped = true, launchUptimeMillis = 1_449_910L, forkUptimeMillis = 1_449_917L)
        val s = snapshot(starts = listOf(stale), exits = listOf(exit(ExitReason.USER_REQUESTED.code)))
        assertNull(currentStart(s))
        val findings = diagnose(s)
        assertEquals(listOf("직전 프로세스 종료 = 사용자 요청"), findings.filter { it.severity == Severity.HISTORY }.map { it.title })
    }

    @Test
    fun `fork 시각이 허용 범위 안인 기록만 이번 실행으로 고른다`() {
        val older = StartRecord(StartReason.LAUNCHER.code, 1, wasForceStopped = true, forkUptimeMillis = PROCESS_START - START_MATCH_TOLERANCE_MILLIS - 1)
        val mine = StartRecord(StartReason.START_ACTIVITY.code, 1, wasForceStopped = false, forkUptimeMillis = PROCESS_START + 30)
        assertEquals(mine, currentStart(snapshot(starts = listOf(older, mine))))
        // fork 가 없으면 launch 로 본다
        val launchOnly = StartRecord(StartReason.BROADCAST.code, 1, wasForceStopped = false, launchUptimeMillis = PROCESS_START - 400)
        assertEquals(launchOnly, currentStart(snapshot(starts = listOf(launchOnly))))
    }

    @Test
    fun `저전력 대기는 이 앱이 예외가 아닐 때만 미뤄짐`() {
        assertFalse(diagnose(snapshot(lowPowerStandby = true, lowPowerStandbyExempt = true)).any { it.title.startsWith("저전력 대기") })
        assertTrue(diagnose(snapshot(lowPowerStandby = true, lowPowerStandbyExempt = false)).any { it.title.startsWith("저전력 대기") })
        // API 33 은 예외 여부(API 34)를 읽을 수 없다 — 예외가 아닌 쪽으로 본다
        assertTrue(diagnose(snapshot(sdkInt = 33, lowPowerStandby = true, lowPowerStandbyExempt = null)).any { it.title.startsWith("저전력 대기") })
    }

    @Test
    fun `낮은 API 에서 못 읽은 값은 판정에서 빠진다`() {
        val findings = diagnose(
            snapshot(
                sdkInt = 26, lightIdle = null, bucket = null, restricted = null,
                lowPowerStandby = null, lowPowerStandbyExempt = null, exits = null, starts = null
            )
        )
        assertTrue(findings.isEmpty())
        assertEquals("지금 이 기기에서 백그라운드를 막는 것은 없다", headline(findings))
    }

    @Test
    fun `공개 상수에 없는 버킷 값은 숫자 그대로, 숨은 EXEMPTED 는 정상`() {
        assertEquals("알 수 없는 값(50)", StandbyBucket.label(50))
        assertEquals("EXEMPTED(5)", StandbyBucket.label(5))
        assertEquals(Severity.CLEAR, diagnose(snapshot(bucket = 5)).first { it.title == "앱 대기 버킷 EXEMPTED" }.severity)
    }
}
