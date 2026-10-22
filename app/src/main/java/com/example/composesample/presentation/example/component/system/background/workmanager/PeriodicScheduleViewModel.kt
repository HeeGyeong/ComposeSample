package com.example.composesample.presentation.example.component.system.background.workmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * WorkManager 예제 '주기 작업 실행 시각' 탭의 시나리오 실행부.
 *
 * 시나리오마다 고유 이름 두 개([ScheduleLane])에 주기 작업을 등록하고, 시점마다 WorkInfo.nextScheduleTimeMillis 를
 * [expectedNextRun] 규칙으로 계산한 기대값과 함께 남긴다. 데모 작업은 진짜로 다시 도는 주기 작업이라
 * 시나리오가 끝나면 바로 취소하고, 탭을 벗어나도([onScreenLeft]) 취소한다.
 */
class PeriodicScheduleViewModel(application: Application) : AndroidViewModel(application) {

    enum class Scenario(val label: String, val description: String) {
        DRIFT(
            "① 주기 기준 vs 슬롯 지정",
            "A 는 30분 주기 그대로, B 는 워커가 실행 안에서 다음 :00/:30 슬롯을 setNextScheduleTimeOverride 로 지정"
        ),
        MIN_GAP(
            "② 최소 15분",
            "A 는 첫 실행 전에 +1분 지정, B 는 한 번 돈 뒤에 +1분 지정 — 두 번째부터는 실행 끝 + 15분보다 당길 수 없다"
        ),
        ONE_SHOT(
            "③ 오버라이드는 한 번만",
            "+20초 지정 → 실행되면 지워지는지, 다시 지정 → 태그만 바꾼 updateWork 에도 남는지, clear 로 지워지는지"
        ),
        RETRY(
            "④ 재시도 시각 지정",
            "둘 다 Result.retry() — A 는 기본 백오프, B 는 워커가 +3분을 오버라이드로 지정(커스텀 백오프)"
        ),
        FLEX(
            "⑤ flex 첫 실행",
            "A 는 30분 주기·flex 10분, B 는 flex 없음 — 첫 실행이 바로인지 (주기 − flex) 뒤인지"
        ),
        POLICY(
            "⑥ 정책과 오버라이드",
            "오버라이드를 실은 요청을 CANCEL_AND_REENQUEUE · KEEP · UPDATE 로 등록"
        )
    }

    data class UiState(
        val running: Scenario? = null,
        val step: String = "",
        val shown: Scenario? = null,
        val snapshots: List<ScheduleSnapshot> = emptyList(),
        /** 예외 메시지·updateWork 결과처럼 스냅샷이 아닌 관찰 */
        val notes: List<String> = emptyList(),
        val timedOut: Boolean = false,
        val interrupted: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val runs: StateFlow<List<ScheduleRunRecord>> = PeriodicScheduleRunLog.runs

    private val workManager: WorkManager
        get() = WorkManager.getInstance(getApplication())

    private var job: Job? = null

    init {
        // 프로세스가 죽어 onScreenLeft 를 못 탄 이전 실행의 데모 작업이 남아 있을 수 있다
        cancelLanes()
    }

    fun run(scenario: Scenario) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _uiState.value = UiState(running = scenario, shown = scenario, step = "이전 데모 작업 정리")
            try {
                reset()
                when (scenario) {
                    Scenario.DRIFT -> runDrift()
                    Scenario.MIN_GAP -> runMinGap()
                    Scenario.ONE_SHOT -> runOneShot()
                    Scenario.RETRY -> runRetry()
                    Scenario.FLEX -> runFlex()
                    Scenario.POLICY -> runPolicy()
                }
            } finally {
                // 관찰이 끝나면 데모 작업을 남기지 않는다(30분마다·재시도마다 실제로 돈다)
                cancelLanes()
                _uiState.update { it.copy(running = null, step = "") }
            }
        }
    }

    fun onScreenLeft() {
        if (job?.isActive == true) {
            job?.cancel()
            _uiState.update { it.copy(interrupted = true) }
        }
        cancelLanes()
    }

    // ViewModel.onCleared 는 빈 메서드라 super 를 부르지 않는다(lint EmptySuperCall)
    override fun onCleared() {
        cancelLanes()
    }

    // ==================== 시나리오 ====================

    /** 실행 끝 + 주기(밀린다) vs 워커가 정한 벽시계 슬롯(정각) */
    private suspend fun runDrift() {
        step("A 주기 그대로 · B 슬롯 지정 등록 → 첫 실행이 끝나기를 기다린다")
        enqueue(ScheduleLane.A, scheduleRequest(ScheduleLane.A, ScheduleWorkMode.PLAIN))
        enqueue(ScheduleLane.B, scheduleRequest(ScheduleLane.B, ScheduleWorkMode.SELF_SLOT))
        waitSettled(ScheduleLane.A, ScheduleLane.B)
        val a = lastRun(ScheduleLane.A)
        val b = lastRun(ScheduleLane.B)
        snapshot(ScheduleLane.A, "첫 실행 뒤", a?.endedAt?.plus(INTERVAL_MILLIS), "실행 끝 ${a?.endedAt?.let(::formatClock)} + 30분")
        snapshot(ScheduleLane.B, "첫 실행 뒤", b?.override, "워커가 지정한 슬롯 ${b?.override?.let(::formatClock)}")
        b?.updateResult?.let { note("B 워커 안 updateWork → $it") }
    }

    /** 오버라이드도 최소 15분 — 단 첫 실행 전에는 그대로 */
    private suspend fun runMinGap() {
        step("A: 첫 실행 전에 +1분 지정")
        val overrideA = System.currentTimeMillis() + ONE_MINUTE
        enqueue(ScheduleLane.A, scheduleRequest(ScheduleLane.A, ScheduleWorkMode.PLAIN, override = overrideA))
        snapshot(ScheduleLane.A, "등록 직후(첫 실행 전)", overrideA, "지정한 +1분 ${formatClock(overrideA)} 그대로")

        step("B: 바로 한 번 돌린 뒤 +1분 지정")
        enqueue(ScheduleLane.B, scheduleRequest(ScheduleLane.B, ScheduleWorkMode.PLAIN))
        waitSettled(ScheduleLane.B)
        val b = lastRun(ScheduleLane.B)
        val overrideB = System.currentTimeMillis() + ONE_MINUTE
        val result = update(ScheduleLane.B, override = overrideB)
        note("B updateWork(+1분) → $result")
        val end = b?.endedAt
        snapshot(
            ScheduleLane.B, "한 번 돈 뒤 +1분 지정",
            end?.let { maxOf(overrideB, it + MIN_PERIODIC_MILLIS) },
            "max(지정 ${formatClock(overrideB)}, 실행 끝 ${end?.let(::formatClock)} + 15분)"
        )
    }

    /** 오버라이드는 다음 한 번만 — 실행되면 지워지고, 관계없는 업데이트에는 남고, clear 로 지운다 */
    private suspend fun runOneShot() {
        val first = System.currentTimeMillis() + TWENTY_SECONDS
        enqueue(ScheduleLane.A, scheduleRequest(ScheduleLane.A, ScheduleWorkMode.PLAIN, override = first))
        snapshot(ScheduleLane.A, "등록 직후", first, "지정한 +20초 ${formatClock(first)}")

        step("지정 시각(+20초)까지 기다린다")
        val ran = waitSettled(ScheduleLane.A, timeoutMillis = ONE_SHOT_TIMEOUT_MILLIS)
        val run = lastRun(ScheduleLane.A)
        val end = run?.endedAt
        if (ran && run != null) note("실행: ${formatClock(run.startedAt)} 시작(지정보다 ${formatSigned(run.startedAt - first)})")
        snapshot(ScheduleLane.A, "실행 뒤", end?.plus(INTERVAL_MILLIS), "지워짐 → 실행 끝 ${end?.let(::formatClock)} + 30분")

        step("다시 +40분 지정 → 태그만 바꾼 updateWork → clear")
        val again = System.currentTimeMillis() + FORTY_MINUTES
        note("+40분 지정 updateWork → ${update(ScheduleLane.A, override = again)}")
        snapshot(ScheduleLane.A, "+40분 지정 뒤", again, "지정 ${formatClock(again)}")
        note("태그만 바꾼 updateWork → ${update(ScheduleLane.A, tag = SCHEDULE_TAG_ALT)}")
        snapshot(ScheduleLane.A, "관계없는 업데이트 뒤", again, "오버라이드가 남아 있으면 그대로 ${formatClock(again)}")
        note("clearNextScheduleTimeOverride updateWork → ${update(ScheduleLane.A, clear = true)}")
        snapshot(ScheduleLane.A, "clear 뒤", end?.plus(INTERVAL_MILLIS), "실행 끝 + 30분으로 복귀")
    }

    /** 재시도: 기본 백오프 vs 워커가 지정한 시각 */
    private suspend fun runRetry() {
        step("둘 다 재시도하는 워커 등록 → 첫 실행(재시도)이 끝나기를 기다린다")
        enqueue(ScheduleLane.A, scheduleRequest(ScheduleLane.A, ScheduleWorkMode.RETRY_PLAIN))
        enqueue(ScheduleLane.B, scheduleRequest(ScheduleLane.B, ScheduleWorkMode.RETRY_OVERRIDE))
        waitSettled(ScheduleLane.A, ScheduleLane.B)
        val a = lastRun(ScheduleLane.A)
        val b = lastRun(ScheduleLane.B)
        snapshot(ScheduleLane.A, "재시도 뒤", a?.endedAt?.plus(DEFAULT_BACKOFF_MILLIS), "백오프 — 실행 끝 ${a?.endedAt?.let(::formatClock)} + 30초")
        snapshot(ScheduleLane.B, "재시도 뒤", b?.override, "워커가 지정한 +3분 ${b?.override?.let(::formatClock)}")
        b?.updateResult?.let { note("B 워커 안 updateWork → $it") }
    }

    /** flex 가 있으면 첫 실행이 바로가 아니라 (주기 − flex) 뒤 */
    private suspend fun runFlex() {
        val enqueuedAt = System.currentTimeMillis()
        enqueue(ScheduleLane.A, scheduleRequest(ScheduleLane.A, ScheduleWorkMode.PLAIN, flexMinutes = FLEX_MINUTES))
        enqueue(ScheduleLane.B, scheduleRequest(ScheduleLane.B, ScheduleWorkMode.PLAIN))
        val flexShift = INTERVAL_MILLIS - FLEX_MINUTES * 60_000
        snapshot(ScheduleLane.A, "등록 직후", enqueuedAt + flexShift, "등록 ${formatClock(enqueuedAt)} + (30 − 10)분")
        step("flex 없는 B 의 첫 실행을 기다린다")
        waitSettled(ScheduleLane.B)
        val b = lastRun(ScheduleLane.B)
        snapshot(ScheduleLane.B, "첫 실행 뒤", b?.endedAt?.plus(INTERVAL_MILLIS), "바로 실행 → 실행 끝 + 30분")
        snapshot(ScheduleLane.A, "그 시점", enqueuedAt + flexShift, "아직 첫 실행 전 — 그대로")
        note("A 실행 기록 ${runs.value.count { it.lane == ScheduleLane.A }}회 · B ${runs.value.count { it.lane == ScheduleLane.B }}회")
    }

    /** 오버라이드는 UPDATE(또는 KEEP)와만 — CANCEL_AND_REENQUEUE 는 새 WorkSpec 이라 최소 간격을 지킬 수 없어 막는다 */
    private suspend fun runPolicy() {
        val override = System.currentTimeMillis() + FORTY_MINUTES
        listOf(
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE to ScheduleLane.A,
            ExistingPeriodicWorkPolicy.KEEP to ScheduleLane.B
        ).forEach { (policy, lane) ->
            step("$policy 로 등록")
            val outcome = try {
                val request = scheduleRequest(lane, ScheduleWorkMode.PLAIN, override = override)
                workManager.enqueueUniquePeriodicWork(lane.uniqueName, policy, request).await()
                "예외 없음"
            } catch (e: IllegalArgumentException) {
                "${e.javaClass.simpleName}: ${e.message}"
            }
            note("${lane.name} · $policy → $outcome")
            snapshot(lane, "$policy 로 등록", if (outcome == "예외 없음") override else null, "등록됐다면 지정 ${formatClock(override)}")
        }
        step("UPDATE 로 등록")
        cancelLanes()
        waitUntil(RESET_TIMEOUT_MILLIS) { ScheduleLane.entries.none { currentInfo(it) != null } }
        enqueue(ScheduleLane.A, scheduleRequest(ScheduleLane.A, ScheduleWorkMode.PLAIN, override = override))
        note("A · UPDATE → 예외 없음")
        snapshot(ScheduleLane.A, "UPDATE 로 등록", override, "지정 ${formatClock(override)}")
    }

    // ==================== WorkManager 조작 ====================

    private suspend fun enqueue(lane: ScheduleLane, request: PeriodicWorkRequest) {
        workManager.enqueueUniquePeriodicWork(lane.uniqueName, ExistingPeriodicWorkPolicy.UPDATE, request).await()
    }

    /** 지금 등록된 작업과 같은 id 로 사양만 바꾼다 — 모드·주기는 원래 요청을 그대로 다시 만든다 */
    private suspend fun update(
        lane: ScheduleLane,
        override: Long? = null,
        tag: String = SCHEDULE_TAG,
        clear: Boolean = false
    ): String {
        val info = currentInfo(lane) ?: return "작업 없음"
        val request = scheduleRequest(
            lane = lane,
            mode = ScheduleWorkMode.PLAIN,
            tag = tag,
            id = info.id,
            override = override,
            clearOverride = clear
        )
        return workManager.updateWork(request).awaitUpdate().name
    }

    private suspend fun reset() {
        ScheduleLane.entries.forEach { workManager.cancelUniqueWork(it.uniqueName).await() }
        waitUntil(RESET_TIMEOUT_MILLIS) { ScheduleLane.entries.none { currentInfo(it)?.state == WorkInfo.State.RUNNING } }
        PeriodicScheduleRunLog.clear()
    }

    /** 각 레인이 한 번 돌고(실행 끝 기록) 다음 실행이 그 뒤로 잡힐 때까지 기다린다 */
    private suspend fun waitSettled(vararg lanes: ScheduleLane, timeoutMillis: Long = SETTLE_TIMEOUT_MILLIS): Boolean {
        val settled = waitUntil(timeoutMillis) {
            lanes.all { lane ->
                val end = lastRun(lane)?.endedAt ?: return@all false
                val info = currentInfo(lane) ?: return@all false
                val next = info.nextScheduleTimeMillis
                info.state == WorkInfo.State.ENQUEUED && next != Long.MAX_VALUE && next > end + 1_000
            }
        }
        if (!settled) _uiState.update { it.copy(timedOut = true) }
        return settled
    }

    private suspend fun waitUntil(timeoutMillis: Long, condition: suspend () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            delay(POLL_MILLIS)
        }
        return condition()
    }

    /** 살아 있는(끝나지 않은) 작업만 — 이전 시나리오에서 취소된 기록이 같은 이름으로 남아 있어도 보지 않는다 */
    private suspend fun currentInfo(lane: ScheduleLane): WorkInfo? =
        workManager.getWorkInfosForUniqueWorkFlow(lane.uniqueName).first().firstOrNull { !it.state.isFinished }

    private fun lastRun(lane: ScheduleLane): ScheduleRunRecord? = runs.value.lastOrNull { it.lane == lane && it.endedAt != null }

    private suspend fun snapshot(lane: ScheduleLane, label: String, expected: Long?, reason: String) {
        val info = currentInfo(lane)
        val snapshot = ScheduleSnapshot(
            lane = lane,
            label = label,
            takenAt = System.currentTimeMillis(),
            state = info?.state?.name,
            generation = info?.generation,
            intervalMillis = info?.periodicityInfo?.repeatIntervalMillis,
            flexMillis = info?.periodicityInfo?.flexIntervalMillis,
            runAttemptCount = info?.runAttemptCount,
            nextScheduleAt = info?.nextScheduleTimeMillis?.takeIf { it != Long.MAX_VALUE },
            expected = expected,
            expectedReason = reason
        )
        _uiState.update { it.copy(snapshots = it.snapshots + snapshot) }
    }

    private fun note(text: String) = _uiState.update { it.copy(notes = it.notes + text) }

    private fun cancelLanes() {
        ScheduleLane.entries.forEach { workManager.cancelUniqueWork(it.uniqueName) }
    }

    private fun step(text: String) = _uiState.update { it.copy(step = text) }

    private companion object {
        const val INTERVAL_MILLIS = SCHEDULE_INTERVAL_MINUTES * 60_000
        const val ONE_MINUTE = 60_000L
        const val TWENTY_SECONDS = 20_000L
        const val FORTY_MINUTES = 40 * 60_000L
        const val FLEX_MINUTES = 10L
        const val POLL_MILLIS = 150L
        const val SETTLE_TIMEOUT_MILLIS = 20_000L
        const val ONE_SHOT_TIMEOUT_MILLIS = 90_000L
        const val RESET_TIMEOUT_MILLIS = 8_000L
    }
}

/** ③ 에서 '관계없는 업데이트'로 쓰는 다른 태그 */
const val SCHEDULE_TAG_ALT = "periodic-schedule-alt"
