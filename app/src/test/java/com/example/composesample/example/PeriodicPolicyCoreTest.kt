package com.example.composesample.example

import androidx.work.WorkInfo
import com.example.composesample.presentation.example.component.system.background.workmanager.LaneSnapshot
import com.example.composesample.presentation.example.component.system.background.workmanager.PeriodicSpec
import com.example.composesample.presentation.example.component.system.background.workmanager.PolicyLane
import com.example.composesample.presentation.example.component.system.background.workmanager.RunOutcome
import com.example.composesample.presentation.example.component.system.background.workmanager.RunRecord
import com.example.composesample.presentation.example.component.system.background.workmanager.formatRemaining
import com.example.composesample.presentation.example.component.system.background.workmanager.isSettled
import com.example.composesample.presentation.example.component.system.background.workmanager.periodBaseRun
import com.example.composesample.presentation.example.component.system.background.workmanager.summary
import com.example.composesample.presentation.example.component.system.background.workmanager.verdictOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * WorkManager 예제 '주기 작업 재등록' 탭의 판정부 검증 — 화면이 쓰는 요약·정착 조건·주기 기준 매칭을 그대로 실행한다.
 * given : 정책별 스냅샷(WorkInfo 에서 뽑은 값)과 워커 실행 기록
 * when : 첫 스냅샷 ↔ 마지막 스냅샷을 비교
 * then : id·주기·세대·실행 횟수·주기 기준이 정책의 동작 설명과 맞게 요약되는가
 */
class PeriodicPolicyCoreTest {

    private val minute = 60_000L
    private val v1Id = UUID.fromString("11111111-0000-0000-0000-000000000000")
    private val v2Id = UUID.fromString("22222222-0000-0000-0000-000000000000")

    private fun snapshot(
        lane: PolicyLane,
        label: String,
        takenAt: Long,
        id: UUID,
        generation: Int,
        spec: PeriodicSpec,
        next: Long?,
        state: WorkInfo.State = WorkInfo.State.ENQUEUED
    ) = LaneSnapshot(
        lane = lane,
        label = label,
        takenAt = takenAt,
        infoCount = 1,
        workId = id,
        state = state,
        generation = generation,
        intervalMillis = spec.intervalMinutes * minute,
        nextScheduleAt = next,
        spec = spec
    )

    private fun run(lane: PolicyLane, id: UUID, spec: PeriodicSpec, start: Long, end: Long, outcome: RunOutcome = RunOutcome.FINISHED) =
        RunRecord(lane, id, generation = 0, spec = spec, startedAt = start, endedAt = end, outcome = outcome)

    @Test
    fun `KEEP 은 v2 를 버린다 - id·주기·세대가 그대로고 기준은 v1 실행 끝`() {
        val lane = PolicyLane.KEEP
        val v1End = 1_400L
        val runs = listOf(run(lane, v1Id, PeriodicSpec.V1, 1_000L, v1End))
        val snapshots = listOf(
            snapshot(lane, "v1 첫 실행 뒤", 2_000L, v1Id, 0, PeriodicSpec.V1, next = v1End + 15 * minute),
            snapshot(lane, "v2 재등록 뒤", 4_000L, v1Id, 0, PeriodicSpec.V1, next = v1End + 15 * minute)
        )
        val verdict = verdictOf(lane, snapshots, runs)!!

        assertFalse(verdict.idChanged)
        assertEquals(15L, verdict.lastIntervalMinutes)
        assertEquals("id 같음 · 주기 15분 · gen 0 · 실행 v1 1회 · 다음 실행 기준 = v1 실행 끝", verdict.summary())
    }

    @Test
    fun `UPDATE 는 같은 id 에 주기만 바뀌고 세대가 오르며 기준은 v1 실행 끝 그대로`() {
        val lane = PolicyLane.UPDATE
        val v1End = 1_400L
        val runs = listOf(run(lane, v1Id, PeriodicSpec.V1, 1_000L, v1End))
        val snapshots = listOf(
            snapshot(lane, "v1 첫 실행 뒤", 2_000L, v1Id, 0, PeriodicSpec.V1, next = v1End + 15 * minute),
            snapshot(lane, "v2 재등록 뒤", 4_000L, v1Id, 1, PeriodicSpec.V2, next = v1End + 30 * minute)
        )

        assertEquals(
            "id 같음 · 주기 15→30분 · gen 0→1 · 실행 v1 1회 · 다음 실행 기준 = v1 실행 끝",
            verdictOf(lane, snapshots, runs)!!.summary()
        )
    }

    @Test
    fun `CANCEL_AND_REENQUEUE 는 새 id 가 곧바로 한 번 돌아 기준이 v2 실행 끝으로 바뀐다`() {
        val lane = PolicyLane.CANCEL_AND_REENQUEUE
        val v2End = 3_500L
        val runs = listOf(
            run(lane, v1Id, PeriodicSpec.V1, 1_000L, 1_400L),
            run(lane, v2Id, PeriodicSpec.V2, 3_100L, v2End)
        )
        val snapshots = listOf(
            snapshot(lane, "v1 첫 실행 뒤", 2_000L, v1Id, 0, PeriodicSpec.V1, next = 1_400L + 15 * minute),
            snapshot(lane, "v2 재등록 뒤", 5_000L, v2Id, 0, PeriodicSpec.V2, next = v2End + 30 * minute)
        )
        val verdict = verdictOf(lane, snapshots, runs)!!

        assertTrue(verdict.idChanged)
        assertEquals(
            "id 바뀜(2개) · 주기 15→30분 · gen 0 · 실행 v1 1회, v2 1회 · 다음 실행 기준 = v2 실행 끝",
            verdict.summary()
        )
    }

    @Test
    fun `실행 중에 끊긴 실행은 중단 횟수로 따로 센다`() {
        val lane = PolicyLane.CANCEL_AND_REENQUEUE
        val runs = listOf(
            run(lane, v1Id, PeriodicSpec.V1, 1_000L, 1_700L, RunOutcome.STOPPED),
            run(lane, v2Id, PeriodicSpec.V2, 1_800L, 2_200L)
        )
        val snapshots = listOf(
            snapshot(lane, "v1 실행 중", 1_200L, v1Id, 0, PeriodicSpec.V1, next = null, state = WorkInfo.State.RUNNING),
            snapshot(lane, "정착 뒤", 4_000L, v2Id, 0, PeriodicSpec.V2, next = 2_200L + 30 * minute)
        )

        assertEquals(
            "id 바뀜(2개) · 주기 15→30분 · gen 0 · 실행 v1 1회, v2 1회 (중단 1회) · 다음 실행 기준 = v2 실행 끝",
            verdictOf(lane, snapshots, runs)!!.summary()
        )
    }

    @Test
    fun `정착 조건 - ENQUEUED 이고 다음 실행이 1분보다 멀고 실행 중인 기록이 없어야 한다`() {
        val lane = PolicyLane.UPDATE
        val now = 10_000L
        val settled = snapshot(lane, "", now, v1Id, 0, PeriodicSpec.V2, next = now + 30 * minute)
        val immediate = settled.copy(nextScheduleAt = now - 5)
        val running = settled.copy(state = WorkInfo.State.RUNNING, nextScheduleAt = null)
        val runningRecord = RunRecord(lane, v1Id, 0, PeriodicSpec.V2, startedAt = now - 100)

        assertTrue(settled.isSettled(now, emptyList()))
        // 첫 실행 몫이 아직 남았다(주기 작업의 첫 실행 시각 = 등록 시각)
        assertFalse(immediate.isSettled(now, emptyList()))
        assertFalse(running.isSettled(now, emptyList()))
        // WorkInfo 는 이미 ENQUEUED 로 보여도 워커 기록이 아직 실행 중이면 기다린다
        assertFalse(settled.isSettled(now, listOf(runningRecord)))
        // 다른 정책의 실행 기록은 상관없다
        assertTrue(settled.isSettled(now, listOf(runningRecord.copy(lane = PolicyLane.KEEP))))
    }

    @Test
    fun `주기 기준은 허용치 3초 안의 실행 끝과만 맞춘다`() {
        val lane = PolicyLane.UPDATE
        val end = 100_000L
        val runs = listOf(run(lane, v1Id, PeriodicSpec.V1, end - 400, end))

        val near = snapshot(lane, "", end + 1_000, v1Id, 1, PeriodicSpec.V2, next = end + 2_500 + 30 * minute)
        assertEquals(runs.first(), near.periodBaseRun(runs))

        val far = near.copy(nextScheduleAt = end + 3_001 + 30 * minute)
        assertNull(far.periodBaseRun(runs))
        // 한 번도 안 돈 첫 주기(다음 실행 = 등록 시각)는 기준 실행이 없다
        assertNull(near.copy(nextScheduleAt = end + 5_000).periodBaseRun(runs))
    }

    @Test
    fun `남은 시간 형식`() {
        assertEquals("14분 59초 뒤", formatRemaining(15 * minute - 600))
        assertEquals("42초 뒤", formatRemaining(42_300))
        assertEquals("지금 실행 가능", formatRemaining(-3))
    }
}
