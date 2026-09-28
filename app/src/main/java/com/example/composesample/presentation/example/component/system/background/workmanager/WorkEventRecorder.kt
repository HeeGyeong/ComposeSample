package com.example.composesample.presentation.example.component.system.background.workmanager

import androidx.work.ExecutionEventListener
import androidx.work.ExperimentalEventsApi
import androidx.work.ListenableWorker
import androidx.work.ScheduleEventListener
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 이 예제가 큐에 넣는 작업에만 붙이는 태그.
 *
 * 리스너는 **앱 전역**이라 다른 예제(위치 스냅샷, 예외 핸들러 데모 등)의 작업 이벤트까지 전부 들어온다.
 * 화면에는 이 태그가 달린 것만 남긴다.
 */
const val WorkEventDemoTag = "work-event-listener-example"

/** 이벤트가 어느 리스너에서 왔는지. 두 리스너는 관찰하는 축이 다르다. */
enum class WorkEventSource(val label: String) {
    /** ScheduleEventListener — 큐/스케줄러 관점 (언제 대기열에 들어가고 풀리고 취소됐는가) */
    SCHEDULE("Schedule"),

    /** ExecutionEventListener — 실행 관점 (언제 돌기 시작하고 끝났는가) */
    EXECUTION("Execution"),

    /** 2.11 부터 있던 Consumer 예외 핸들러 — 같은 실패를 다른 모델로 받는다 */
    LEGACY_HANDLER("Consumer(2.11)")
}

/**
 * 타임라인 한 줄.
 *
 * @property elapsedMillis 화면이 "시작" 을 누른 시점 기준 경과 시간. 절대 시각보다 순서·간격이 중요해서 상대값으로 둔다.
 * @property threadName 콜백이 어느 스레드에서 오는지 보여주려고 함께 남긴다.
 */
data class WorkEventEntry(
    val source: WorkEventSource,
    val callback: String,
    val workName: String,
    val state: String,
    val detail: String,
    val elapsedMillis: Long,
    /** 기준점 계산용 절대 시각. 화면에는 [elapsedMillis] 만 보여준다. */
    val recordedAt: Long,
    val threadName: String
)

/**
 * work 2.12 의 두 리스너가 보내오는 이벤트를 모으는 전역 싱크.
 *
 * 리스너는 `Configuration` 에만 등록할 수 있고 앱 전역에 한 번뿐이라, 화면이 직접 등록할 수 없다.
 * 그래서 같은 폴더 [WorkerExceptionReporter] 와 같은 방식으로 전역 object 가 받아 두고 화면이 구독한다.
 */
object WorkEventRecorder {

    /** 화면에 남길 최근 이벤트 수. 오래된 것부터 버린다. */
    private const val MAX_ENTRIES = 60

    private val _entries = MutableStateFlow<List<WorkEventEntry>>(emptyList())
    val entries: StateFlow<List<WorkEventEntry>> = _entries.asStateFlow()

    /**
     * 시나리오를 새로 시작한다. 목록을 비우는 것이 곧 시계를 0으로 돌리는 것이다.
     *
     * 경과 시간의 기준점을 따로 들고 있지 않는 이유: 기준점을 필드로 두면 화면(메인 스레드)이 쓰고
     * 리스너(WorkManager 실행기 스레드)가 읽는 공유 가변 상태가 되어 가시성 문제가 생긴다.
     * **목록의 첫 항목 시각을 기준으로 삼으면** 그 문제가 아예 없어진다 — 비우면 다음 이벤트가 새 기준점이 된다.
     */
    fun startSession() {
        _entries.value = emptyList()
    }

    fun clear() {
        _entries.value = emptyList()
    }

    /**
     * 이 예제의 작업이 아니면 버린다.
     *
     * 태그 필터가 없으면 다른 예제나 시스템이 돌리는 작업 이벤트가 섞여 타임라인을 읽을 수 없다.
     */
    fun record(
        source: WorkEventSource,
        callback: String,
        workInfo: WorkInfo,
        detail: String = ""
    ) {
        if (WorkEventDemoTag !in workInfo.tags) return

        append(
            source = source,
            callback = callback,
            workName = workInfo.demoName(),
            state = workInfo.state.name,
            detail = detail
        )
    }

    /** Consumer 예외 핸들러 쪽은 WorkInfo 가 없어 이름만 받아 기록한다. */
    fun recordLegacy(workName: String, throwableName: String) {
        append(
            source = WorkEventSource.LEGACY_HANDLER,
            callback = "workerExecutionExceptionHandler",
            workName = workName,
            state = "-",
            detail = throwableName
        )
    }

    /**
     * 목록의 첫 항목을 기준점으로 삼아 경과 시간을 매긴다.
     *
     * `update` 블록 안에서 계산하므로 여러 스레드가 동시에 기록해도 기준점이 흔들리지 않는다
     * (MutableStateFlow.update 는 CAS 루프라 블록이 재실행될 수 있고, 그때 최신 목록으로 다시 계산된다).
     */
    private fun append(
        source: WorkEventSource,
        callback: String,
        workName: String,
        state: String,
        detail: String
    ) {
        val now = System.currentTimeMillis()
        val threadName = Thread.currentThread().name

        _entries.update { previous ->
            val base = previous.firstOrNull()?.recordedAt ?: now
            val entry = WorkEventEntry(
                source = source,
                callback = callback,
                workName = workName,
                state = state,
                detail = detail,
                elapsedMillis = now - base,
                recordedAt = now,
                threadName = threadName
            )
            (previous + entry).takeLast(MAX_ENTRIES)
        }
    }
}

/** 데모 작업 이름 태그(`name:<이름>`)를 뽑아낸다. 없으면 id 앞 6자리. */
private fun WorkInfo.demoName(): String =
    tags.firstOrNull { it.startsWith(WorkEventNameTagPrefix) }
        ?.removePrefix(WorkEventNameTagPrefix)
        ?: id.toString().take(6)

const val WorkEventNameTagPrefix = "name:"

/**
 * 실행 축 리스너. 네 콜백이 **전부 abstract 라 모두 구현해야 한다**(Schedule 쪽은 기본 구현이 있어 선택적).
 *
 * 모든 콜백이 `suspend` 라서 기존 `Consumer` 핸들러와 호출 모델이 다르다 —
 * 리스너 안에서 곧바로 중단 함수를 호출할 수 있고, WorkManager 는 이 호출이 끝날 때까지 기다린다.
 */
@OptIn(ExperimentalEventsApi::class)
class DemoExecutionEventListener : ExecutionEventListener {

    override suspend fun onStarted(workInfo: WorkInfo) {
        WorkEventRecorder.record(WorkEventSource.EXECUTION, "onStarted", workInfo)
    }

    override suspend fun onStopped(stopReason: Int, workInfo: WorkInfo) {
        WorkEventRecorder.record(
            source = WorkEventSource.EXECUTION,
            callback = "onStopped",
            workInfo = workInfo,
            detail = "stopReason=$stopReason (${stopReason.stopReasonName()})"
        )
    }

    override suspend fun onFinished(result: ListenableWorker.Result, workInfo: WorkInfo) {
        WorkEventRecorder.record(
            source = WorkEventSource.EXECUTION,
            callback = "onFinished",
            workInfo = workInfo,
            detail = result.javaClass.simpleName
        )
    }

    override suspend fun onException(throwable: Throwable, workInfo: WorkInfo) {
        WorkEventRecorder.record(
            source = WorkEventSource.EXECUTION,
            callback = "onException",
            workInfo = workInfo,
            detail = throwable.javaClass.simpleName + ": " + throwable.message.orEmpty()
        )
    }
}

/**
 * 스케줄 축 리스너. 다섯 콜백 모두 **기본 구현이 있어** 필요한 것만 재정의해도 된다(여기서는 전부 관찰한다).
 *
 * `onPrerequisiteFailed` 가 체인의 앞 작업이 실패해 뒤 작업이 영영 돌지 않게 된 순간을 알려준다 —
 * 실행 축 리스너만 보면 "아무 일도 일어나지 않은" 것처럼 보이는 구간이다.
 */
@OptIn(ExperimentalEventsApi::class)
class DemoScheduleEventListener : ScheduleEventListener {

    override suspend fun onEnqueued(workInfo: WorkInfo) {
        WorkEventRecorder.record(WorkEventSource.SCHEDULE, "onEnqueued", workInfo)
    }

    override suspend fun onUpdated(oldWorkInfo: WorkInfo, updatedWorkInfo: WorkInfo) {
        WorkEventRecorder.record(
            source = WorkEventSource.SCHEDULE,
            callback = "onUpdated",
            workInfo = updatedWorkInfo,
            detail = "${oldWorkInfo.state.name} → ${updatedWorkInfo.state.name}"
        )
    }

    override suspend fun onUnblocked(workInfo: WorkInfo) {
        WorkEventRecorder.record(
            source = WorkEventSource.SCHEDULE,
            callback = "onUnblocked",
            workInfo = workInfo,
            detail = "선행 조건이 풀려 실행 대기로"
        )
    }

    override suspend fun onCancelled(workInfo: WorkInfo) {
        WorkEventRecorder.record(WorkEventSource.SCHEDULE, "onCancelled", workInfo)
    }

    override suspend fun onPrerequisiteFailed(workInfo: WorkInfo) {
        WorkEventRecorder.record(
            source = WorkEventSource.SCHEDULE,
            callback = "onPrerequisiteFailed",
            workInfo = workInfo,
            detail = "앞 작업이 실패해 이 작업은 실행되지 않는다"
        )
    }
}

/** `WorkInfo.STOP_REASON_*` 중 데모에서 나올 만한 것만 이름을 붙인다. */
private fun Int.stopReasonName(): String = when (this) {
    WorkInfo.STOP_REASON_CANCELLED_BY_APP -> "CANCELLED_BY_APP"
    WorkInfo.STOP_REASON_CONSTRAINT_CONNECTIVITY -> "CONSTRAINT_CONNECTIVITY"
    WorkInfo.STOP_REASON_CONSTRAINT_BATTERY_NOT_LOW -> "CONSTRAINT_BATTERY_NOT_LOW"
    WorkInfo.STOP_REASON_CONSTRAINT_CHARGING -> "CONSTRAINT_CHARGING"
    WorkInfo.STOP_REASON_PREEMPT -> "PREEMPT"
    WorkInfo.STOP_REASON_TIMEOUT -> "TIMEOUT"
    WorkInfo.STOP_REASON_NOT_STOPPED -> "NOT_STOPPED"
    else -> "기타"
}
