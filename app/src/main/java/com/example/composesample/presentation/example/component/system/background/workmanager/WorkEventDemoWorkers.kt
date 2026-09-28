package com.example.composesample.presentation.example.component.system.background.workmanager

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

/** 데모 작업이 얼마나 일하는 척할지. 타임라인에서 onStarted~onFinished 간격으로 보인다. */
private const val WORK_DURATION_MILLIS = 1_500L

/**
 * 성공하는 워커. 잠깐 일하고 끝난다.
 *
 * `delay` 를 쓰므로 취소되면 `CancellationException` 으로 즉시 빠져나온다 —
 * 취소 시나리오에서 onStopped 가 빨리 오는 이유다.
 */
class WorkEventSuccessWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        delay(WORK_DURATION_MILLIS)
        return Result.success()
    }
}

/** 실패를 Result 로 돌려주는 워커. 체인의 뒤 작업이 prerequisite 실패로 막히게 만든다. */
class WorkEventFailingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        delay(300)
        return Result.failure()
    }
}

/**
 * 예외를 밖으로 던지는 워커.
 *
 * 같은 실패가 **두 경로로** 들어온다 — 2.12 의 `ExecutionEventListener.onException`(suspend)과
 * 2.11 부터 있던 `setWorkerExecutionExceptionHandler`(Consumer). 화면은 그 둘을 나란히 보여준다.
 */
class WorkEventThrowingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        delay(200)
        error("이벤트 리스너 데모용 의도적 예외")
    }
}

/** 절대 충족되지 않는 제약을 달아 계속 ENQUEUED 로 대기시키는 워커. */
class WorkEventBlockedWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        delay(WORK_DURATION_MILLIS)
        return Result.success()
    }
}

/**
 * 데모 작업 요청을 만든다.
 *
 * 태그를 두 개 붙인다 — 화면 필터용 [WorkEventDemoTag] 와, 타임라인에 사람이 읽을 이름을 주는 `name:<이름>`.
 * WorkInfo 에는 워커 클래스명이 없어서(구현 세부를 감춘다) 이름을 태그로 실어 보내는 방법을 쓴다.
 */
inline fun <reified W : CoroutineWorker> workEventRequest(
    name: String,
    constraints: Constraints? = null
): OneTimeWorkRequest = OneTimeWorkRequestBuilder<W>()
    .addTag(WorkEventDemoTag)
    .addTag("$WorkEventNameTagPrefix$name")
    .apply { constraints?.let { setConstraints(it) } }
    .build()

/** 충족되지 않는 제약(충전 중 + 네트워크 없음은 동시 성립이 어렵다)으로 대기 상태를 만든다. */
fun blockedConstraints(): Constraints = Constraints.Builder()
    .setRequiresCharging(true)
    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
    .build()
