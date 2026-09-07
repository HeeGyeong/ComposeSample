package com.example.composesample.example

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.Worker
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * WorkManager 테스트 하네스 예제의 실제 검증 코드.
 *
 * 화면(`WorkManagerTestExampleUI`)이 설명하는 내용이 여기서 실제로 통과한다.
 * 규칙 하나로 "테스트 모드 WorkManager 초기화 + TestDriver 확보"를 끝내는 것이 핵심이다.
 */

/**
 * 매 테스트마다 반복되던 초기화 보일러플레이트를 걷어내는 JUnit 규칙.
 *
 * - [SynchronousExecutor] 를 쓰면 작업이 호출 스레드에서 즉시 실행돼 대기 코드가 필요 없다.
 * - [WorkManagerTestInitHelper.getTestDriver] 는 초기화 이후에만 유효하므로 규칙이 함께 들고 있는다.
 */
class WorkManagerTestRule(
    private val context: Context = ApplicationProvider.getApplicationContext()
) : TestRule {

    lateinit var workManager: WorkManager
        private set

    lateinit var testDriver: TestDriver
        private set

    override fun apply(base: Statement, description: Description): Statement =
        object : Statement() {
            override fun evaluate() {
                val config = Configuration.Builder()
                    .setMinimumLoggingLevel(Log.DEBUG)
                    // 작업을 즉시·동기 실행 → 테스트에서 sleep/idling 이 필요 없어진다
                    .setExecutor(SynchronousExecutor())
                    .build()
                WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
                workManager = WorkManager.getInstance(context)
                testDriver = requireNotNull(WorkManagerTestInitHelper.getTestDriver(context))
                try {
                    base.evaluate()
                } finally {
                    workManager.cancelAllWork()
                }
            }
        }
}

/** 입력을 그대로 출력으로 돌려주는 워커 — 격리 실행 검증용 */
class EchoWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): ListenableWorker.Result {
        val echo = inputData.getString(KEY) ?: return ListenableWorker.Result.failure()
        return ListenableWorker.Result.success(workDataOf(KEY to echo.uppercase()))
    }

    companion object {
        const val KEY = "echo"
    }
}

/** runAttemptCount 가 2 미만이면 재시도를 요구하는 워커 — 재시도 분기 검증용 */
class RetryUntilThirdWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): ListenableWorker.Result =
        if (runAttemptCount < 2) ListenableWorker.Result.retry()
        else ListenableWorker.Result.success(workDataOf("attempt" to runAttemptCount))
}

@RunWith(AndroidJUnit4::class)
class WorkManagerTestExampleTest {

    @get:Rule
    val workRule = WorkManagerTestRule()

    private fun log(msg: String) = Log.d("WMTestProbe", msg)

    /** 제약이 걸린 작업은 TestDriver 가 만족시켜 주기 전까지 실행되지 않는다 */
    @Test
    fun constraintsBlockUntilTestDriverMeetsThem() {
        val request = OneTimeWorkRequestBuilder<EchoWorker>()
            .setInputData(workDataOf(EchoWorker.KEY to "hello"))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresCharging(true)
                    .build()
            )
            .build()

        workRule.workManager.enqueue(request).result.get()

        val before = workRule.workManager.getWorkInfoById(request.id).get().state
        log("제약 충족 전 상태 = $before")

        workRule.testDriver.setAllConstraintsMet(request.id)

        val after = workRule.workManager.getWorkInfoById(request.id).get()
        log("제약 충족 후 상태 = ${after.state} / output = ${after.outputData.getString(EchoWorker.KEY)}")

        assertEquals(WorkInfo.State.ENQUEUED, before)
        assertEquals(WorkInfo.State.SUCCEEDED, after.state)
    }

    /** 초기 지연도 실제로 기다리지 않고 TestDriver 로 앞당긴다 */
    @Test
    fun initialDelayIsSkippedByTestDriver() {
        val request = OneTimeWorkRequestBuilder<EchoWorker>()
            .setInputData(workDataOf(EchoWorker.KEY to "delayed"))
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        workRule.workManager.enqueue(request).result.get()
        val before = workRule.workManager.getWorkInfoById(request.id).get().state
        log("24시간 지연 설정 후 상태 = $before")

        workRule.testDriver.setInitialDelayMet(request.id)
        val after = workRule.workManager.getWorkInfoById(request.id).get().state
        log("setInitialDelayMet 후 상태 = $after")

        assertEquals(WorkInfo.State.ENQUEUED, before)
        assertEquals(WorkInfo.State.SUCCEEDED, after)
    }

    /** WorkManager 없이 워커 하나만 떼어내 실행한다 */
    @Test
    fun workerRunsInIsolation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val worker = TestListenableWorkerBuilder<EchoWorker>(context)
            .setInputData(workDataOf(EchoWorker.KEY to "isolated"))
            .build()

        val result = worker.startWork().get()
        log("격리 실행 결과 = $result")
        assertEquals(ListenableWorker.Result.success(workDataOf(EchoWorker.KEY to "ISOLATED")), result)
    }

    /** runAttemptCount 를 주입해 재시도 분기를 대기 없이 검증한다 */
    @Test
    fun runAttemptCountCanBeInjected() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val firstAttempt = TestListenableWorkerBuilder<RetryUntilThirdWorker>(context)
            .setRunAttemptCount(0)
            .build()
            .startWork().get()

        val thirdAttempt = TestListenableWorkerBuilder<RetryUntilThirdWorker>(context)
            .setRunAttemptCount(2)
            .build()
            .startWork().get()

        log("runAttemptCount=0 → $firstAttempt / runAttemptCount=2 → $thirdAttempt")
        assertEquals(ListenableWorker.Result.retry(), firstAttempt)
        assertEquals(ListenableWorker.Result.success(workDataOf("attempt" to 2)), thirdAttempt)
    }

    /** 입력이 없으면 실패하는 분기까지 격리 실행으로 확인 */
    @Test
    fun missingInputMakesWorkerFail() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = TestListenableWorkerBuilder<EchoWorker>(context)
            .setInputData(Data.EMPTY)
            .build()
            .startWork().get()

        log("입력 없음 → $result")
        assertEquals(ListenableWorker.Result.failure(), result)
    }
}
