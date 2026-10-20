package com.example.composesample.presentation.example.component.system.background.workmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * WorkManager 예제 '주기 작업 재등록' 탭의 시나리오 실행부.
 *
 * 세 정책([PolicyLane])을 서로 다른 고유 이름에서 **같은 순서로 동시에** 실행하고, 시점마다 WorkInfo 스냅샷을 남긴다.
 * 판정·요약은 [PeriodicPolicyCore][verdictOf] 에 있고, 실행 기록은 워커가 [PeriodicPolicyRunLog] 에 남긴다.
 *
 * 데모 주기 작업은 15·30분마다 실제로 다시 도는 진짜 작업이라, 화면을 벗어나면([onScreenLeft]) 바로 취소한다.
 */
class PeriodicPolicyViewModel(application: Application) : AndroidViewModel(application) {

    enum class Scenario(val label: String, val description: String) {
        APP_UPDATE(
            "① 앱 업데이트",
            "v1(15분)이 한 번 돈 뒤, 업데이트한 앱이 같은 이름을 v2(30분)로 다시 등록 — 기존 사용자의 경로"
        ),
        FRESH_INSTALL(
            "② 신규 설치",
            "등록된 작업이 없는 상태에서 v2(30분)를 등록 — 새로 설치한 사용자의 경로"
        ),
        EVERY_LAUNCH(
            "③ 앱 시작마다 등록 ×3",
            "Application.onCreate 에서 등록하는 코드처럼, 사양이 같은 v2 를 세 번 연달아 등록"
        ),
        WHILE_RUNNING(
            "④ 실행 중 재등록",
            "v1 이 ${LONG_WORK_MILLIS / 1_000}초짜리 실행을 하는 도중에 v2 로 다시 등록"
        )
    }

    data class UiState(
        val running: Scenario? = null,
        val step: String = "",
        val shown: Scenario? = null,
        val snapshots: List<LaneSnapshot> = emptyList(),
        /** 정착을 기다리다 시간 초과한 정책 — 결과를 그대로 믿으면 안 된다는 표시 */
        val timedOut: Set<PolicyLane> = emptySet(),
        val interrupted: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val runs: StateFlow<List<RunRecord>> = PeriodicPolicyRunLog.runs

    private val workManager: WorkManager
        get() = WorkManager.getInstance(getApplication())

    private var job: Job? = null

    init {
        // 프로세스가 죽어 onScreenLeft 를 못 탄 이전 실행의 데모 주기 작업이 남아 있을 수 있다
        cancelLanes()
    }

    fun run(scenario: Scenario) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            _uiState.value = UiState(running = scenario, shown = scenario, step = "이전 데모 작업 정리")
            try {
                reset()
                when (scenario) {
                    Scenario.APP_UPDATE -> runAppUpdate()
                    Scenario.FRESH_INSTALL -> runFreshInstall()
                    Scenario.EVERY_LAUNCH -> runEveryLaunch()
                    Scenario.WHILE_RUNNING -> runWhileRunning()
                }
            } finally {
                _uiState.update { it.copy(running = null, step = "") }
            }
        }
    }

    /** 탭을 벗어나면 진행 중인 시나리오와 데모 주기 작업을 모두 멈춘다(결과 화면 값은 남긴다) */
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

    /** v1 이 한 번 돈 뒤 v2 로 다시 등록 — "기존 사용자는 새 사양을 받는가" */
    private suspend fun runAppUpdate() {
        step("v1(15분) 등록 → 첫 실행이 끝나기를 기다린다")
        PolicyLane.entries.forEach { register(it, PeriodicSpec.V1) }
        waitSettled()
        snapshotAll("v1 첫 실행 뒤")

        step("앱 업데이트 — 같은 이름을 v2(30분)로 다시 등록")
        PolicyLane.entries.forEach { register(it, PeriodicSpec.V2) }
        waitSettled()
        snapshotAll("v2 재등록 뒤")
    }

    /** 등록된 것이 없을 때 v2 등록 — 세 정책 모두 같은 결과가 나와야 한다(데모에서 버그가 안 보이는 이유) */
    private suspend fun runFreshInstall() {
        step("작업이 없는 상태에서 v2(30분) 등록")
        PolicyLane.entries.forEach { register(it, PeriodicSpec.V2) }
        waitSettled()
        snapshotAll("v2 등록 뒤")
    }

    /** 사양이 같은 등록을 세 번 — 앱을 켤 때마다 등록하는 코드에서 정책이 무엇을 하는가 */
    private suspend fun runEveryLaunch() {
        repeat(LAUNCH_COUNT) { index ->
            step("앱 시작 ${index + 1}/$LAUNCH_COUNT — v2(30분) 등록")
            PolicyLane.entries.forEach { register(it, PeriodicSpec.V2) }
            waitSettled()
            snapshotAll("${index + 1}회째 등록 뒤")
        }
    }

    /** 실행 중에 다시 등록 — UPDATE 는 끊지 않고, CANCEL_AND_REENQUEUE 는 끊는다 */
    private suspend fun runWhileRunning() {
        step("v1(15분, ${LONG_WORK_MILLIS / 1_000}초짜리 실행) 등록 → 실행이 시작되기를 기다린다")
        PolicyLane.entries.forEach { register(it, PeriodicSpec.V1, workMillis = LONG_WORK_MILLIS) }
        waitUntil(STARTED_TIMEOUT_MILLIS) {
            PolicyLane.entries.all { lane -> runs.value.any { it.lane == lane && it.outcome == RunOutcome.RUNNING } }
        }
        snapshotAll("v1 실행 중")

        step("실행 중에 v2(30분)로 다시 등록")
        PolicyLane.entries.forEach { register(it, PeriodicSpec.V2) }
        snapshotAll("v2 재등록 직후")

        step("실행이 끝나고 다음 주기로 넘어가기를 기다린다")
        waitSettled()
        snapshotAll("정착 뒤")
    }

    // ==================== WorkManager 조작 ====================

    private suspend fun register(
        lane: PolicyLane,
        spec: PeriodicSpec,
        workMillis: Long = PeriodicPolicyWorker.DEFAULT_WORK_MILLIS
    ) {
        val request = PeriodicWorkRequestBuilder<PeriodicPolicyWorker>(spec.intervalMinutes, TimeUnit.MINUTES)
            .setInputData(
                workDataOf(
                    PeriodicPolicyWorker.KEY_LANE to lane.uniqueName,
                    PeriodicPolicyWorker.KEY_SPEC to spec.label,
                    PeriodicPolicyWorker.KEY_WORK_MILLIS to workMillis
                )
            )
            .addTag(spec.tag)
            .build()
        // 앱의 등록 코드는 버전이 바뀌어도 정책은 그대로라고 가정한다 — v1·v2 모두 그 정책으로 등록
        workManager.enqueueUniquePeriodicWork(lane.uniqueName, lane.policy, request).await()
    }

    /** 이전 시나리오의 작업을 취소하고, 실행 중이던 워커가 멈춘 뒤 기록을 비운다 */
    private suspend fun reset() {
        PolicyLane.entries.forEach { workManager.cancelUniqueWork(it.uniqueName).await() }
        waitUntil(RESET_TIMEOUT_MILLIS) {
            PolicyLane.entries.none { infosOf(it).any { info -> info.state == WorkInfo.State.RUNNING } }
        }
        PeriodicPolicyRunLog.clear(PolicyLane.entries)
    }

    /** 세 정책 모두 '즉시 실행 몫'이 끝나 다음 실행이 주기 뒤로 밀릴 때까지 기다린다 */
    private suspend fun waitSettled() {
        val settled = waitUntil(SETTLE_TIMEOUT_MILLIS) {
            val now = System.currentTimeMillis()
            PolicyLane.entries.all { snapshotOf(it, "").isSettled(now, runs.value) }
        }
        if (!settled) {
            val now = System.currentTimeMillis()
            val late = PolicyLane.entries.filterNot { snapshotOf(it, "").isSettled(now, runs.value) }
            _uiState.update { it.copy(timedOut = it.timedOut + late) }
        }
        // 정착한 뒤에도 늦게 도는 실행이 없는지 잠깐 더 본다
        delay(SETTLE_GRACE_MILLIS)
    }

    private suspend fun waitUntil(timeoutMillis: Long, condition: suspend () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            delay(POLL_MILLIS)
        }
        return condition()
    }

    private suspend fun snapshotAll(label: String) {
        val taken = PolicyLane.entries.map { snapshotOf(it, label) }
        _uiState.update { it.copy(snapshots = it.snapshots + taken) }
    }

    private suspend fun infosOf(lane: PolicyLane): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWorkFlow(lane.uniqueName).first()

    private suspend fun snapshotOf(lane: PolicyLane, label: String): LaneSnapshot {
        val infos = infosOf(lane)
        // 끝나지 않은 것이 지금 살아 있는 작업이다(취소된 이전 작업이 같은 이름으로 남아 있을 수 있다)
        val current = infos.firstOrNull { !it.state.isFinished } ?: infos.lastOrNull()
        return LaneSnapshot(
            lane = lane,
            label = label,
            takenAt = System.currentTimeMillis(),
            infoCount = infos.size,
            workId = current?.id,
            state = current?.state,
            generation = current?.generation,
            intervalMillis = current?.periodicityInfo?.repeatIntervalMillis,
            nextScheduleAt = current?.nextScheduleTimeMillis?.takeIf { it != Long.MAX_VALUE },
            spec = current?.let { PeriodicSpec.fromTags(it.tags) }
        )
    }

    private fun cancelLanes() {
        PolicyLane.entries.forEach { workManager.cancelUniqueWork(it.uniqueName) }
    }

    private fun step(text: String) = _uiState.update { it.copy(step = text) }

    private companion object {
        const val LAUNCH_COUNT = 3
        const val LONG_WORK_MILLIS = 4_000L
        const val POLL_MILLIS = 150L
        const val SETTLE_TIMEOUT_MILLIS = 20_000L
        const val SETTLE_GRACE_MILLIS = 1_000L
        const val STARTED_TIMEOUT_MILLIS = 10_000L
        const val RESET_TIMEOUT_MILLIS = 8_000L
    }
}

/** 화면 상단 표의 정책 설명 — work-runtime 2.12.0 소스(ExistingPeriodicWorkPolicy·WorkerUpdater·EnqueueRunnable) 기준 */
internal val ExistingPeriodicWorkPolicy.whenExisting: String
    get() = when (this) {
        ExistingPeriodicWorkPolicy.KEEP -> "아무것도 하지 않는다 — 새 요청(주기·태그·입력)은 버려진다"
        ExistingPeriodicWorkPolicy.UPDATE ->
            "같은 id 의 사양만 바꾼다 · generation +1 · 주기 기준 시각 유지 · 실행 중이면 끊지 않고 다음 실행부터"
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE ->
            "기존 작업을 취소·삭제하고 새 id 로 등록한다 · 실행 중이면 끊는다"
        else -> "CANCEL_AND_REENQUEUE 와 같다(deprecated)"
    }
