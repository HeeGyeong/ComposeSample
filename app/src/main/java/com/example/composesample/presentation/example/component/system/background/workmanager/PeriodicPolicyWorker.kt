package com.example.composesample.presentation.example.component.system.background.workmanager

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 주기 작업 재등록 정책 탭의 실행 기록.
 *
 * 워커는 WorkManager 실행기 스레드에서, 화면은 메인 스레드에서 이 목록을 만지므로
 * 쓰기는 전부 [MutableStateFlow.update](원자적 비교-교체)로 한다.
 * 메모리에만 두므로 프로세스가 죽으면 사라진다 — 시나리오는 몇 초 안에 끝나는 관찰용이다.
 */
object PeriodicPolicyRunLog {
    private val _runs = MutableStateFlow<List<RunRecord>>(emptyList())
    val runs: StateFlow<List<RunRecord>> = _runs.asStateFlow()

    fun started(record: RunRecord) = _runs.update { (it + record).takeLast(MAX_RECORDS) }

    /** 시작 기록이 지워진 뒤(시나리오 사이 정리) 도착한 끝 기록은 버린다 — 이전 시나리오가 섞이지 않게 */
    fun ended(record: RunRecord, outcome: RunOutcome, stopReason: Int? = null) = _runs.update { list ->
        list.map {
            if (it.workId == record.workId && it.startedAt == record.startedAt) {
                it.copy(endedAt = System.currentTimeMillis(), outcome = outcome, stopReason = stopReason)
            } else {
                it
            }
        }
    }

    fun clear(lanes: Collection<PolicyLane>) = _runs.update { list -> list.filterNot { it.lane in lanes } }

    private const val MAX_RECORDS = 60
}

/**
 * 하는 일 없이 잠깐 기다렸다 끝나는 주기 워커 — 실행될 때마다 [PeriodicPolicyRunLog] 에 남긴다.
 *
 * 기록에 넣는 사양·세대는 **이번 실행이 받은 값**(inputData · WorkerParameters.generation)이다.
 * WorkInfo 는 최신 요청을 보여주므로, 둘을 나란히 두면 UPDATE 가 "실행 중인 워커는 그대로 두고
 * 다음 실행부터" 적용된다는 것이 보인다.
 */
class PeriodicPolicyWorker(
    context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 데모 화면이 아닌 곳에서 넣은 요청이면 아무것도 하지 않는다
        val lane = PolicyLane.fromUniqueName(inputData.getString(KEY_LANE)) ?: return Result.success()
        val record = RunRecord(
            lane = lane,
            workId = id,
            generation = params.generation,
            spec = PeriodicSpec.fromLabel(inputData.getString(KEY_SPEC)) ?: PeriodicSpec.V1,
            startedAt = System.currentTimeMillis()
        )
        PeriodicPolicyRunLog.started(record)
        try {
            delay(inputData.getLong(KEY_WORK_MILLIS, DEFAULT_WORK_MILLIS))
        } catch (e: CancellationException) {
            // CANCEL_AND_REENQUEUE 가 실행 중인 작업을 취소하면 여기로 온다 — 기록만 남기고 취소는 그대로 전파한다
            val reason = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) stopReason else null
            PeriodicPolicyRunLog.ended(record, RunOutcome.STOPPED, reason)
            throw e
        }
        PeriodicPolicyRunLog.ended(record, RunOutcome.FINISHED)
        return Result.success()
    }

    companion object {
        const val KEY_LANE = "periodic_policy_lane"
        const val KEY_SPEC = "periodic_policy_spec"
        const val KEY_WORK_MILLIS = "periodic_policy_work_millis"
        const val DEFAULT_WORK_MILLIS = 400L
    }
}
