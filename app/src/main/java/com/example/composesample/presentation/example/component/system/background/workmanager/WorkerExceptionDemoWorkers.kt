package com.example.composesample.presentation.example.component.system.background.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerExceptionInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 워커 예외 핸들러 예제의 런타임 자산 — 데모 워커 4종과, 핸들러가 받은 정보를 모아 두는 수집기.
 *
 * 핸들러 자체는 화면이 아니라 **앱 전역 Configuration** 에 등록된다([com.example.composesample.application.BaseApplication]).
 * WorkManager 는 워커를 별도 스레드에서 돌리므로 핸들러도 그 스레드에서 호출되고, 화면은 여기 쌓인
 * 리포트를 StateFlow 로 구독하기만 한다.
 *
 * 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 참고.
 */

/** 데모 워커가 공통으로 다는 태그. 화면은 이 태그로 WorkInfo 를 구독한다. */
internal const val WorkerExceptionDemoTag = "worker_exception_demo"

/** 어떤 시나리오로 넣은 작업인지 inputData 에 실어 보내 리포트에 함께 남긴다. */
internal const val WorkerExceptionScenarioKey = "scenario"

/**
 * 워커 관련 예외 핸들러 2종.
 *
 * 둘 다 [WorkerExceptionInfo] 를 받지만 **호출되는 지점이 다르다** — work-runtime 2.11.2 의
 * `WorkerWrapper` 바이트코드 기준으로 생성 실패는 "Could not create Worker" 로그 직후,
 * 실행 실패는 "failed because it threw an exception/error" 로그 직후에 불린다.
 */
enum class WorkerExceptionHandlerKind(
    val handlerName: String,
    val firedWhen: String
) {
    /** 워커 인스턴스를 만들지 못했을 때(팩토리/리플렉션 단계). */
    INITIALIZATION(
        handlerName = "setWorkerInitializationExceptionHandler",
        firedWhen = "워커 인스턴스 생성 실패"
    ),

    /** 워커는 만들어졌지만 실행 중 Throwable 이 밖으로 나갔을 때. */
    EXECUTION(
        handlerName = "setWorkerExecutionExceptionHandler",
        firedWhen = "실행 중 Throwable 전파"
    )
}

/** 핸들러가 받은 [WorkerExceptionInfo] 를 화면에서 읽을 수 있는 형태로 옮겨 담은 기록 1건. */
data class WorkerExceptionReport(
    val kind: WorkerExceptionHandlerKind,
    val workerClassName: String,
    val scenario: String,
    val throwableName: String,
    val throwableMessage: String?,
    val causeName: String?,
    val causeMessage: String?,
    val runAttemptCount: Int,
    val tags: List<String>,
    val threadName: String,
    val recordedAt: Long
)

/**
 * 핸들러가 남긴 리포트 보관소.
 *
 * 핸들러는 앱 초기화 시점에 딱 한 번 등록되므로 수집기도 앱 전역에 하나만 둔다.
 * 호출 스레드가 워커 실행 스레드라 [MutableStateFlow.update] 로만 갱신한다.
 */
object WorkerExceptionReporter {

    /** 화면에 남길 최근 기록 수. 오래된 것부터 버린다. */
    private const val MAX_REPORTS = 20

    private val _reports = MutableStateFlow<List<WorkerExceptionReport>>(emptyList())
    val reports: StateFlow<List<WorkerExceptionReport>> = _reports.asStateFlow()

    /**
     * 핸들러 본문. **여기서 다시 예외를 던져도 앱은 죽지 않는다** — WorkManager 가
     * `safeAccept` 로 감싸 "Exception handler threw an exception" 로그만 남기고 삼킨다.
     * 그래서 핸들러에서 조용히 실패하지 않도록 기록만 남기고 즉시 빠져나온다.
     */
    fun record(kind: WorkerExceptionHandlerKind, info: WorkerExceptionInfo) {
        val params = info.workerParameters
        val report = WorkerExceptionReport(
            kind = kind,
            workerClassName = info.workerClassName.substringAfterLast('.'),
            scenario = params.inputData.getString(WorkerExceptionScenarioKey).orEmpty(),
            throwableName = info.throwable.readableName(),
            throwableMessage = info.throwable.message,
            causeName = info.throwable.cause?.readableName(),
            causeMessage = info.throwable.cause?.message,
            runAttemptCount = params.runAttemptCount,
            tags = params.tags.toList().sorted(),
            threadName = Thread.currentThread().name,
            recordedAt = System.currentTimeMillis()
        )
        _reports.update { previous -> (listOf(report) + previous).take(MAX_REPORTS) }
    }

    fun clear() {
        _reports.value = emptyList()
    }
}

/** 익명 클래스면 simpleName 이 빈 문자열이라 그때는 전체 이름을 쓴다. */
private fun Throwable.readableName(): String =
    javaClass.simpleName.ifEmpty { javaClass.name }

// ==================== 데모 워커 4종 ====================

/**
 * ① 실행 중에 예외를 던지는 워커.
 *
 * `doWork()` 밖으로 나간 Throwable 이 `workerExecutionExceptionHandler` 를 깨운다.
 * 결과는 재시도가 아니라 **FAILED** 다(WorkerWrapper 가 Resolution.Failed 를 돌려준다).
 */
class ThrowingDemoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        delay(300L)
        throw IllegalStateException("doWork() 안에서 의도적으로 던진 예외")
    }
}

/**
 * ② 같은 실패를 예외 없이 끝내는 워커(대조군).
 *
 * `Result.failure()` 는 정상적인 종료 경로라서 **핸들러가 호출되지 않는다**.
 * WorkInfo 상태는 ①과 똑같이 FAILED 라, 상태만 봐서는 둘을 구분할 수 없다.
 */
class FailureResultDemoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        delay(300L)
        return Result.failure(
            workDataOf("reason" to "Result.failure() 로 끝냈다 — 예외는 전파되지 않았다")
        )
    }
}

/**
 * ③ 생성 단계에서 실패하는 워커.
 *
 * 기본 팩토리는 `Constructor.newInstance` 로 워커를 만들기 때문에, 생성자가 던진 예외는
 * **InvocationTargetException 으로 감싸져** `workerInitializationExceptionHandler` 에 전달된다.
 * 진짜 원인을 보려면 `throwable.cause` 를 봐야 한다.
 */
class BrokenInitDemoWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    init {
        throw IllegalArgumentException("생성자에서 의도적으로 던진 예외 — 의존성 주입 실패를 흉내낸다")
    }

    override suspend fun doWork(): Result = Result.success()
}

/**
 * ④ `(Context, WorkerParameters)` 생성자가 없는 워커.
 *
 * 기본 팩토리는 그 시그니처를 `getDeclaredConstructor` 로 찾는다. 여기서 실패하면 예외가
 * 감싸지지 않고 **NoSuchMethodException 그대로** 핸들러에 전달된다 — ③과 대조되는 지점이다.
 * 커스텀 WorkerFactory 없이 생성자에 의존성을 추가했을 때 실제로 나는 오류이기도 하다.
 */
class MissingConstructorDemoWorker(
    context: Context,
    params: WorkerParameters,
    private val injectedDependency: String
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        Result.success(workDataOf("dependency" to injectedDependency))
}
