package com.example.composesample.presentation.example.component.system.background.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * '주기 작업 실행 시각' 탭의 실행 기록 — 워커(실행기 스레드)와 화면(메인)이 함께 만지므로 [MutableStateFlow.update] 로만 쓴다.
 * 메모리에만 둔다(시나리오는 몇십 초 안에 끝나는 관찰용).
 */
object PeriodicScheduleRunLog {
    private val _runs = MutableStateFlow<List<ScheduleRunRecord>>(emptyList())
    val runs: StateFlow<List<ScheduleRunRecord>> = _runs.asStateFlow()

    fun started(record: ScheduleRunRecord) = _runs.update { (it + record).takeLast(MAX_RECORDS) }

    fun ended(record: ScheduleRunRecord, override: Long?, updateResult: String?, retried: Boolean) = _runs.update { list ->
        list.map {
            if (it.workId == record.workId && it.startedAt == record.startedAt) {
                it.copy(endedAt = System.currentTimeMillis(), override = override, updateResult = updateResult, retried = retried)
            } else {
                it
            }
        }
    }

    fun clear() = _runs.update { emptyList() }

    private const val MAX_RECORDS = 40
}

/** 시나리오가 쓰는 주기 작업 요청 — 워커가 자기 요청을 updateWork 할 때도 같은 모양으로 만든다(사양이 어긋나지 않게) */
fun scheduleRequest(
    lane: ScheduleLane,
    mode: ScheduleWorkMode,
    intervalMinutes: Long = SCHEDULE_INTERVAL_MINUTES,
    flexMinutes: Long? = null,
    tag: String = SCHEDULE_TAG,
    id: UUID? = null,
    override: Long? = null,
    clearOverride: Boolean = false
): PeriodicWorkRequest {
    val builder = if (flexMinutes != null) {
        PeriodicWorkRequestBuilder<PeriodicScheduleWorker>(intervalMinutes, TimeUnit.MINUTES, flexMinutes, TimeUnit.MINUTES)
    } else {
        PeriodicWorkRequestBuilder<PeriodicScheduleWorker>(intervalMinutes, TimeUnit.MINUTES)
    }
    builder
        .setInputData(
            workDataOf(
                PeriodicScheduleWorker.KEY_LANE to lane.name,
                PeriodicScheduleWorker.KEY_MODE to mode.name,
                PeriodicScheduleWorker.KEY_INTERVAL to intervalMinutes
            )
        )
        .addTag(tag)
    if (id != null) builder.setId(id)
    if (override != null) builder.setNextScheduleTimeOverride(override)
    if (clearOverride) builder.clearNextScheduleTimeOverride()
    return builder.build()
}

/**
 * updateWork 가 돌려주는 ListenableFuture 를 기다린다. ListenableFuture.await(concurrent-futures-ktx)는 이 앱 클래스패스에 없어
 * 리스너로 직접 잇는다 — 제네릭 함수는 HotSwan 이 재작성하지 못하므로(GENERIC_DECLARATION) 타입을 고정했다.
 */
suspend fun ListenableFuture<WorkManager.UpdateResult>.awaitUpdate(): WorkManager.UpdateResult =
    suspendCancellableCoroutine { continuation ->
        addListener({
            try {
                continuation.resume(get())
            } catch (e: ExecutionException) {
                continuation.resumeWithException(e.cause ?: e)
            }
        }, Runnable::run)
        continuation.invokeOnCancellation { cancel(false) }
    }

const val SCHEDULE_INTERVAL_MINUTES = 30L
const val SCHEDULE_TAG = "periodic-schedule"

/** 워커가 지정하는 슬롯: 30분 정각(…:00 · …:30) 중 지금부터 16분 이후의 첫 번째 */
const val SLOT_MINUTES = 30
const val SLOT_MIN_GAP_MILLIS = 16 * 60_000L

/** 재시도 워커가 지정하는 다음 시각: 지금부터 3분 뒤(기본 백오프 30초 대신) */
const val RETRY_OVERRIDE_MILLIS = 3 * 60_000L

/**
 * 잠깐 기다렸다 끝나는 주기 워커 — 모드에 따라 실행 안에서 자기 다음 실행 시각을 지정하거나 재시도한다.
 *
 * 실행 안에서 자기 요청을 updateWork 하면 오버라이드 세대가 올라가, 실행이 끝난 뒤의 '오버라이드 지우기'
 * (WorkerWrapper → resetWorkSpecNextScheduleTimeOverride 는 세대가 같을 때만)가 그 값을 지우지 않는다.
 * KDoc 이 말하는 "UPDATE 와 함께 써야 실행 중인 워커를 끊지 않는다"의 updateWork 가 이 경로다.
 */
class PeriodicScheduleWorker(
    context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val lane = ScheduleLane.fromName(inputData.getString(KEY_LANE)) ?: return Result.success()
        val mode = ScheduleWorkMode.fromName(inputData.getString(KEY_MODE))
        val interval = inputData.getLong(KEY_INTERVAL, SCHEDULE_INTERVAL_MINUTES)
        val record = ScheduleRunRecord(lane, id, params.generation, mode, startedAt = System.currentTimeMillis())
        PeriodicScheduleRunLog.started(record)
        delay(WORK_MILLIS)

        val override = when (mode) {
            ScheduleWorkMode.SELF_SLOT -> nextSlot(System.currentTimeMillis(), SLOT_MINUTES, SLOT_MIN_GAP_MILLIS)
            ScheduleWorkMode.RETRY_OVERRIDE -> System.currentTimeMillis() + RETRY_OVERRIDE_MILLIS
            else -> null
        }
        val updateResult = override?.let {
            WorkManager.getInstance(applicationContext)
                .updateWork(scheduleRequest(lane, mode, interval, id = id, override = it))
                .awaitUpdate()
                .name
        }
        PeriodicScheduleRunLog.ended(record, override, updateResult, retried = mode.retries)
        return if (mode.retries) Result.retry() else Result.success()
    }

    companion object {
        const val KEY_LANE = "periodic_schedule_lane"
        const val KEY_MODE = "periodic_schedule_mode"
        const val KEY_INTERVAL = "periodic_schedule_interval"
        const val WORK_MILLIS = 300L
    }
}
