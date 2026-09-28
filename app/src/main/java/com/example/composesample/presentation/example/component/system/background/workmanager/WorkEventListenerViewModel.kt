package com.example.composesample.presentation.example.component.system.background.workmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * work 2.12 이벤트 리스너 예제의 시나리오 실행부.
 *
 * 리스너 자체는 [BaseApplication][com.example.composesample.application.BaseApplication] 이 앱 전역에 등록하고,
 * 수신은 [WorkEventRecorder] 가 맡는다. 여기서는 **관찰할 거리를 만들기 위해 작업을 큐에 넣기만** 한다.
 *
 * 시나리오가 네 개인 이유는 콜백마다 나오는 조건이 다르기 때문이다.
 *  - [Scenario.SUCCESS_CHAIN] : 정상 경로 — onEnqueued → onUnblocked → onStarted → onFinished 의 기본 골격
 *  - [Scenario.FAILED_CHAIN]  : 앞 작업이 실패 → 뒤 작업에 **onPrerequisiteFailed**. 실행 축에는 아무것도 안 온다
 *  - [Scenario.CANCELLED]     : 실행 중 취소 → **onStopped(stopReason)** + onCancelled
 *  - [Scenario.THROWING]      : 예외 → **onException**(suspend)과 2.11 Consumer 핸들러가 같은 실패를 각각 받는다
 */
class WorkEventListenerViewModel(application: Application) : AndroidViewModel(application) {

    enum class Scenario(val label: String, val description: String) {
        SUCCESS_CHAIN(
            "① 정상 체인",
            "A → B 순차 실행. 두 작업의 enqueue/unblock/start/finish 가 어떤 순서로 오는지"
        ),
        FAILED_CHAIN(
            "② 체인 실패",
            "A 가 실패하면 B 는 실행되지 않는다. 실행 축은 조용하고 스케줄 축만 onPrerequisiteFailed 를 준다"
        ),
        CANCELLED(
            "③ 실행 중 취소",
            "돌고 있는 작업을 취소해 onStopped(stopReason) 와 onCancelled 를 받는다"
        ),
        THROWING(
            "④ 예외 발생",
            "같은 실패가 2.12 onException(suspend) 과 2.11 Consumer 핸들러 양쪽으로 들어온다"
        )
    }

    data class UiState(
        val running: Scenario? = null,
        val blockedQueued: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    val events = WorkEventRecorder.entries

    private val workManager: WorkManager
        get() = WorkManager.getInstance(getApplication())

    fun run(scenario: Scenario) {
        if (_uiState.value.running != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(running = scenario) }
            WorkEventRecorder.startSession()

            when (scenario) {
                Scenario.SUCCESS_CHAIN -> runSuccessChain()
                Scenario.FAILED_CHAIN -> runFailedChain()
                Scenario.CANCELLED -> runCancelled()
                Scenario.THROWING -> runThrowing()
            }

            _uiState.update { it.copy(running = null) }
        }
    }

    /** A → B 순차 체인. `beginWith().then()` 이 B 를 BLOCKED 로 만들고, A 가 끝나면 onUnblocked 가 온다. */
    private suspend fun runSuccessChain() {
        workManager
            .beginUniqueWork(
                UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                workEventRequest<WorkEventSuccessWorker>("A")
            )
            .then(workEventRequest<WorkEventSuccessWorker>("B"))
            .enqueue()

        // 두 작업(각 1.5초)이 끝날 때까지 기다렸다가 화면의 "실행 중" 표시를 내린다
        delay(4_000)
    }

    /** A 가 Result.failure() → B 는 영영 실행되지 않고 onPrerequisiteFailed 만 온다. */
    private suspend fun runFailedChain() {
        workManager
            .beginUniqueWork(
                UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                workEventRequest<WorkEventFailingWorker>("A(실패)")
            )
            .then(workEventRequest<WorkEventSuccessWorker>("B(막힘)"))
            .enqueue()

        delay(2_500)
    }

    /** 돌고 있는 작업을 취소한다. CoroutineWorker 의 delay 가 즉시 풀려 onStopped 가 빨리 온다. */
    private suspend fun runCancelled() {
        val request = workEventRequest<WorkEventSuccessWorker>("취소 대상")
        workManager.enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.REPLACE, request)

        // 시작된 뒤에 취소해야 onStopped 를 볼 수 있다(큐에만 있을 때 취소하면 onCancelled 만 온다)
        delay(700)
        workManager.cancelWorkById(request.id)
        delay(1_500)
    }

    /** 예외를 던지는 워커. onException 과 Consumer 핸들러가 같은 실패를 각각 받는다. */
    private suspend fun runThrowing() {
        workManager.enqueueUniqueWork(
            UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            workEventRequest<WorkEventThrowingWorker>("예외")
        )
        delay(2_000)
    }

    /**
     * 충족되지 않는 제약을 단 작업을 큐에 넣는다.
     *
     * 큐에 들어가도 ENQUEUED 에서 멈춰 있어 실행 축 콜백이 오지 않는다 —
     * "스케줄 축에는 기록이 있는데 실행 축은 조용한" 상태를 보여주기 위한 것이다.
     */
    fun enqueueBlocked() {
        viewModelScope.launch {
            workManager.enqueueUniqueWork(
                BLOCKED_UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                workEventRequest<WorkEventBlockedWorker>("제약 대기", blockedConstraints())
            )
            _uiState.update { it.copy(blockedQueued = true) }
        }
    }

    fun cancelBlocked() {
        viewModelScope.launch {
            workManager.cancelUniqueWork(BLOCKED_UNIQUE_NAME)
            _uiState.update { it.copy(blockedQueued = false) }
        }
    }

    fun clearEvents() = WorkEventRecorder.clear()

    override fun onCleared() {
        super.onCleared()
        // 화면을 벗어나면 대기 중인 데모 작업이 남지 않도록 정리한다
        workManager.cancelUniqueWork(UNIQUE_NAME)
        workManager.cancelUniqueWork(BLOCKED_UNIQUE_NAME)
    }

    private companion object {
        const val UNIQUE_NAME = "work-event-demo"
        const val BLOCKED_UNIQUE_NAME = "work-event-demo-blocked"
    }
}
