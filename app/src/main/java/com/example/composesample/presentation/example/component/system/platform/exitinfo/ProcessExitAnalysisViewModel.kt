package com.example.composesample.presentation.example.component.system.platform.exitinfo

import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationExitInfo
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.system.exitProcess

/**
 * '프로세스 종료 사후 분석' 예제의 실행부.
 *
 * 실험 버튼은 프로세스를 끝내기 전에 [Experiment](종류·pid·시각)를 SharedPreferences 에 commit 하고
 * setProcessStateSummary 로 같은 내용을 시스템에도 남긴다. 다음 실행에서 getHistoricalProcessExitReasons 를 읽어
 * pid 로 맞춰 본다. 화면에 있는 동안만 앱의 기본 처리기([CrashRecorder])를 끼워 크래시를 앱도 직접 기록한다.
 */
class ProcessExitAnalysisViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val pid: Int = Process.myPid(),
        /** ApplicationExitInfo 는 API 30 부터 */
        val supported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
        val lowMemoryKillReportSupported: Boolean? = null,
        val exits: List<ExitSnapshot> = emptyList(),
        val experiments: List<Experiment> = emptyList(),
        val crashes: List<CrashRecord> = emptyList(),
        val summaryProbe: String? = null,
        val loading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val prefs: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val crashRecorder = CrashRecorder(prefs)

    init {
        refresh()
    }

    /**
     * 화면에 들어올 때 기본 처리기 앞에 끼운다 — ViewModel 은 BlogExampleActivity 범위라
     * onCleared 에서만 떼면 다른 예제로 옮겨 가도 처리기가 남는다.
     */
    fun attachCrashRecorder() {
        val current = Thread.getDefaultUncaughtExceptionHandler()
        if (current === crashRecorder) return
        crashRecorder.previous = current
        Thread.setDefaultUncaughtExceptionHandler(crashRecorder)
    }

    fun detachCrashRecorder() {
        // 다른 곳이 그 사이 처리기를 바꿨다면 건드리지 않는다
        if (Thread.getDefaultUncaughtExceptionHandler() === crashRecorder) {
            Thread.setDefaultUncaughtExceptionHandler(crashRecorder.previous)
        }
    }

    override fun onCleared() {
        detachCrashRecorder()
    }

    fun refresh() {
        _uiState.update { it.copy(loading = true) }
        viewModelScope.launch {
            val exits = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) readExits() else emptyList()
            }
            _uiState.update {
                it.copy(
                    exits = exits,
                    experiments = readLines(KEY_EXPERIMENTS).mapNotNull(::decodeExperiment),
                    crashes = readLines(KEY_CRASHES).mapNotNull(::decodeCrash),
                    lowMemoryKillReportSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ActivityManager.isLowMemoryKillReportSupported()
                    } else null,
                    loading = false
                )
            }
        }
    }

    // ==================== 실험 — 모두 이 프로세스를 끝낸다 ====================

    fun triggerAnr() {
        begin(ExperimentKind.ANR)
        blockMainThreadForAnr()
    }

    /** 트레이스의 main 스레드 스택에서 찾을 이름 — [ANR_MARKER] 와 같아야 한다 */
    private fun blockMainThreadForAnr() {
        // 이 버튼을 누른 터치(UP) 처리가 끝나지 않은 채 입력 대기 한도를 넘기면 ANR. '앱 닫기'를 고르면 REASON_ANR 로 끝난다
        Thread.sleep(ANR_BLOCK_MILLIS)
    }

    fun triggerCrash() {
        begin(ExperimentKind.CRASH)
        throw IllegalStateException("CS159 의도적 크래시")
    }

    fun exitSelf() {
        begin(ExperimentKind.EXIT_SELF)
        exitProcess(0)
    }

    fun killSelf() {
        begin(ExperimentKind.KILL_SELF)
        Process.killProcess(Process.myPid())
    }

    /** 끝내기 직전 — 프로세스가 곧 사라지므로 commit 으로 바로 쓴다 */
    private fun begin(kind: ExperimentKind) {
        val pid = Process.myPid()
        val at = System.currentTimeMillis()
        val summary = summaryOf(kind, pid, at)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) setSummary(summary.toByteArray(Charsets.UTF_8))
        appendLine(KEY_EXPERIMENTS, encodeExperiment(Experiment(kind, pid, at, summary)))
    }

    // ==================== 상태 요약 한도 ====================

    /** [bytes] 바이트짜리 요약을 써 본다 — 이 프로세스의 요약은 마지막 호출 값으로 바뀐다 */
    fun probeSummary(bytes: Int, korean: Boolean) {
        val text = probeText(bytes, korean)
        val size = utf8Size(text)
        val result = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            "API 30 미만 — setProcessStateSummary 없음"
        } else {
            try {
                setSummary(text.toByteArray(Charsets.UTF_8))
                "${text.length}자 · ${size}바이트 → 성공"
            } catch (e: RuntimeException) {
                "${text.length}자 · ${size}바이트 → ${e.javaClass.simpleName}: ${e.message}"
            }
        }
        _uiState.update { it.copy(summaryProbe = result) }
    }

    fun clearRecords() {
        prefs.edit(commit = true) { clear() }
        refresh()
    }

    // ==================== 읽기 ====================

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setSummary(bytes: ByteArray) {
        getApplication<Application>().getSystemService(ActivityManager::class.java).setProcessStateSummary(bytes)
    }

    /** 자기 패키지의 모든 프로세스(:remote 등) 종료 기록, 최신 순 */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun readExits(): List<ExitSnapshot> {
        val app = getApplication<Application>()
        return app.getSystemService(ActivityManager::class.java)
            .getHistoricalProcessExitReasons(app.packageName, 0, MAX_EXITS)
            .map { it.toSnapshot() }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun ApplicationExitInfo.toSnapshot(): ExitSnapshot {
        val trace = readTrace()
        // 네이티브 크래시(API 31)는 protobuf 라 텍스트 스택으로 읽지 않는다
        val textTrace = trace?.takeIf { reason == ApplicationExitInfo.REASON_ANR }?.toString(Charsets.UTF_8)
        return ExitSnapshot(
            pid = pid,
            reason = reason,
            status = status,
            timestamp = timestamp,
            processName = processName,
            importance = importance,
            pssKb = pss,
            rssKb = rss,
            // System.exit·killProcess 는 null 이 아니라 빈 문자열을 준다(API 35 실측)
            description = description?.takeIf { it.isNotBlank() },
            summary = processStateSummary?.toString(Charsets.UTF_8),
            traceBytes = trace?.size,
            mainStack = textTrace?.let { mainThreadStack(it, MAX_STACK_LINES) }.orEmpty()
        )
    }

    /** 트레이스는 시스템 전역 순환 버퍼라 없을 수 있다 — 크기 상한까지만 읽는다 */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun ApplicationExitInfo.readTrace(): ByteArray? = try {
        traceInputStream?.use { input ->
            val buffer = ByteArray(MAX_TRACE_BYTES)
            var total = 0
            while (total < buffer.size) {
                val read = input.read(buffer, total, buffer.size - total)
                if (read < 0) break
                total += read
            }
            buffer.copyOf(total)
        }
    } catch (e: IOException) {
        null
    }

    private fun readLines(key: String): List<String> =
        prefs.getString(key, null)?.split('\n')?.filter { it.isNotBlank() }?.asReversed().orEmpty()

    private fun appendLine(key: String, line: String) {
        val lines = (prefs.getString(key, null)?.split('\n')?.filter { it.isNotBlank() }.orEmpty() + line).takeLast(MAX_RECORDS)
        prefs.edit(commit = true) { putString(key, lines.joinToString("\n")) }
    }

    /**
     * 기본 처리기 앞에 끼워 크래시를 앱이 직접 남기고 원래 처리기로 넘긴다(프로세스는 원래 처리기가 끝낸다).
     * 프레임워크 클래스 상속이 아니라 인터페이스 구현이라 debug(HotSwan)에서도 콜백이 그대로 불린다.
     */
    private class CrashRecorder(private val prefs: SharedPreferences) : Thread.UncaughtExceptionHandler {
        /** 끼우기 직전의 처리기 — 기록 뒤 여기로 넘긴다 */
        var previous: Thread.UncaughtExceptionHandler? = null

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            try {
                val record = CrashRecord(Process.myPid(), System.currentTimeMillis(), thread.name, throwable.javaClass.name, throwable.message.orEmpty())
                val lines = (prefs.getString(KEY_CRASHES, null)?.split('\n')?.filter { it.isNotBlank() }.orEmpty() + encodeCrash(record)).takeLast(MAX_RECORDS)
                prefs.edit(commit = true) { putString(KEY_CRASHES, lines.joinToString("\n")) }
            } finally {
                val next = previous
                if (next != null) {
                    next.uncaughtException(thread, throwable)
                } else {
                    Process.killProcess(Process.myPid())
                    exitProcess(10)
                }
            }
        }
    }

    companion object {
        /** 트레이스에서 찾을 이 예제의 함수 이름 */
        const val ANR_MARKER = "blockMainThreadForAnr"
        // 입력 대기 한도가 AOSP 5초·삼성 10초라, 대화상자에서 '앱 닫기'를 누를 시간이 남도록 30초(실측 — 20초면 삼성에서 대화상자가 뜨자마자 풀렸다)
        private const val ANR_BLOCK_MILLIS = 30_000L
        private const val PREFS_NAME = "process_exit_analysis"
        private const val KEY_EXPERIMENTS = "experiments"
        private const val KEY_CRASHES = "crashes"
        private const val MAX_RECORDS = 10
        private const val MAX_EXITS = 10
        private const val MAX_TRACE_BYTES = 512 * 1024
        private const val MAX_STACK_LINES = 400
    }
}
