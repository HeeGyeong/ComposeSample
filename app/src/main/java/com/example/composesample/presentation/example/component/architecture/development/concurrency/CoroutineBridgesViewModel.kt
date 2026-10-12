package com.example.composesample.presentation.example.component.architecture.development.concurrency

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale

/**
 * Coroutine Bridges 예제 7번 섹션 — 콜백이 아니라 **사람의 응답**을 suspend 로 기다리는 브리지.
 *
 * 작업 코루틴이 [ask] 에서 질문을 [pendingQuestion] 에 올리고 `CompletableDeferred.await()` 에서 멈춘다.
 * 화면은 그 질문을 다이얼로그로 띄우고, 사용자가 고르면 [onQuestionAnswered] 가 `complete(답)` 으로 작업을 깨운다.
 * 원문(On-Device AI Series Part 6 "Parking the ReAct Loop")의 ask 와, 원문이 다루지 않는
 * "기다리던 코루틴이 취소되면 다이얼로그를 거두는" 판([AskMode.CLEANUP])을 같은 작업으로 대조한다.
 *
 * 상태는 이 ViewModel 이 들고 있으므로 액티비티가 재생성돼도(다크 모드 전환) 질문과 기다리는 작업이 그대로 남는다.
 */
class CoroutineBridgesViewModel : ViewModel() {

    enum class AskMode(val label: String) {
        ORIGINAL("원문 그대로"),
        CLEANUP("finally 로 정리")
    }

    data class PendingQuestion(
        val taskName: String,
        val question: String,
        val options: List<String>
    )

    private val _pendingQuestion = MutableStateFlow<PendingQuestion?>(null)
    val pendingQuestion: StateFlow<PendingQuestion?> = _pendingQuestion.asStateFlow()

    private val _mode = MutableStateFlow(AskMode.ORIGINAL)
    val mode: StateFlow<AskMode> = _mode.asStateFlow()

    private val _events = MutableStateFlow<List<String>>(emptyList())
    val events: StateFlow<List<String>> = _events.asStateFlow()

    private val _runningTasks = MutableStateFlow<List<String>>(emptyList())
    val runningTasks: StateFlow<List<String>> = _runningTasks.asStateFlow()

    /** 지금 다이얼로그에 걸려 있는 질문의 deferred. 모든 접근은 메인 스레드(viewModelScope)에서 일어난다. */
    private var questionDeferred: CompletableDeferred<String?>? = null
    private val tasks = linkedMapOf<String, Job>()
    private var taskCounter = 0
    private var startedAt = 0L

    fun setMode(mode: AskMode) {
        _mode.value = mode
    }

    /**
     * 정리할 파일을 찾은 뒤 삭제 여부를 사용자에게 묻는 작업 하나를 시작한다.
     * [timeoutMs] 가 있으면 그 시간 안에 답이 없을 때 기다리던 쪽이 취소된다(TimeoutCancellationException).
     */
    fun startTask(timeoutMs: Long? = null) {
        val name = "작업 ${++taskCounter}"
        if (tasks.isEmpty()) {
            startedAt = SystemClock.elapsedRealtime()
            _events.value = emptyList()
        }
        val job = viewModelScope.launch {
            log("$name 시작 — 정리할 파일을 찾는 중")
            try {
                delay(SCAN_MS)
                log("$name 질문을 올리고 await() 에서 멈춤")
                val answer = if (timeoutMs == null) {
                    ask(name)
                } else {
                    try {
                        withTimeout(timeoutMs) { ask(name) }
                    } catch (e: TimeoutCancellationException) {
                        val left = _pendingQuestion.value?.taskName == name
                        log("$name ${timeoutMs / 1000}초 안에 답이 없어 기다리기를 그만둠 — 다이얼로그 ${if (left) "남아 있음 ⚠️" else "닫힘"}")
                        null
                    }
                }
                log("$name 재개 ← 답 = ${answer ?: "null"}")
                if (answer == "삭제") {
                    delay(DELETE_MS)
                    log("$name 파일 3개 삭제 완료")
                } else {
                    log("$name 삭제하지 않고 끝냄")
                }
            } catch (e: CancellationException) {
                log("$name 취소됨")
                throw e
            } finally {
                tasks.remove(name)
                _runningTasks.value = tasks.keys.toList()
            }
        }
        tasks[name] = job
        _runningTasks.value = tasks.keys.toList()
    }

    /** 앞 작업이 질문을 띄운 직후 다음 작업이 질문을 올리게 두 개를 연달아 시작한다. */
    fun startTwoTasks() {
        startTask()
        viewModelScope.launch {
            delay(SECOND_TASK_AFTER_MS)
            startTask()
        }
    }

    private suspend fun ask(taskName: String): String? = when (_mode.value) {
        AskMode.ORIGINAL -> askOriginal(taskName)
        AskMode.CLEANUP -> askWithCleanup(taskName)
    }

    /** 원문 그대로 — 이전 질문은 null 로 정리하고, 새 질문을 노출한 뒤 기다린다. */
    private suspend fun askOriginal(taskName: String): String? {
        questionDeferred?.complete(null)
        val deferred = CompletableDeferred<String?>()
        questionDeferred = deferred
        _pendingQuestion.value = PendingQuestion(taskName, QUESTION, OPTIONS)
        return deferred.await()
    }

    /**
     * 보강판 — 기다리던 코루틴이 취소되면(시간 초과·화면 종료) 다이얼로그와 deferred 를 거둔다.
     * 단, 그사이 다른 질문이 자리를 차지했다면 건드리지 않는다(identity 확인). 이 확인 없이 지우면
     * 밀려난 작업의 finally 가 새 작업의 다이얼로그까지 닫아, 새 작업은 영영 깨어나지 못한다(JVM 실측).
     */
    private suspend fun askWithCleanup(taskName: String): String? {
        questionDeferred?.complete(null)
        val deferred = CompletableDeferred<String?>()
        questionDeferred = deferred
        _pendingQuestion.value = PendingQuestion(taskName, QUESTION, OPTIONS)
        try {
            return deferred.await()
        } finally {
            if (questionDeferred === deferred) {
                questionDeferred = null
                _pendingQuestion.value = null
            }
        }
    }

    /** 다이얼로그에서 고르거나(답) 닫으면(null) 호출된다. 기다리는 작업이 없으면 답이 버려진다는 것을 기록한다. */
    fun onQuestionAnswered(answer: String?) {
        val asker = _pendingQuestion.value?.taskName
        if (asker != null && tasks[asker]?.isActive != true) {
            log("답 '${answer ?: "닫기"}' — $asker 는 이미 기다리지 않는다 → 답이 버려졌다(유령 다이얼로그)")
        }
        questionDeferred?.complete(answer)
        questionDeferred = null
        _pendingQuestion.value = null
    }

    private fun log(message: String) {
        val elapsed = (SystemClock.elapsedRealtime() - startedAt) / 1000.0
        _events.update { it + String.format(Locale.ROOT, "%5.1f초  %s", elapsed, message) }
    }

    private companion object {
        const val SCAN_MS = 600L
        const val DELETE_MS = 400L
        const val SECOND_TASK_AFTER_MS = 900L
        const val QUESTION = "찾은 파일 3개를 삭제할까요?"
        val OPTIONS = listOf("삭제", "건너뛰기")
    }
}
