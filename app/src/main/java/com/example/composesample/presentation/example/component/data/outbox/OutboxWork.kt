package com.example.composesample.presentation.example.component.data.outbox

import android.content.Context
import android.os.Process
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/** 작업을 어떻게 등록하나 */
enum class EnqueueMode(val label: String) {
    CHAIN_APPEND_OR_REPLACE("대화별 체인 · APPEND_OR_REPLACE"),
    CHAIN_APPEND("대화별 체인 · APPEND"),
    INDEPENDENT("메시지마다 따로")
}

/** 영구 실패를 워커가 어떻게 끝내나 */
enum class FailureHandling(val label: String) {
    MARK_ROW("행만 FAILED · success()"),
    RESULT_FAILURE("Result.failure()")
}

data class SendOptions(
    val mode: EnqueueMode = EnqueueMode.CHAIN_APPEND_OR_REPLACE,
    val failure: FailureHandling = FailureHandling.MARK_ROW,
    /** 함정 — 워커가 시도마다 새 id 를 만든다(행의 clientId 를 쓰지 않음) */
    val newIdEveryAttempt: Boolean = false
)

fun newClientId(): String = UUID.randomUUID().toString()

fun shortId(clientId: String): String = clientId.take(8)

/**
 * 프로세스당 하나 — 화면(ViewModel)과 워커가 같은 DB·서버·클라이언트를 쓴다.
 * 앱을 죽인 뒤 WorkManager 가 새 프로세스에서 워커를 먼저 띄우면 여기서 처음 만들어진다.
 */
object OutboxRuntime {
    class Holder(context: Context) {
        val db: OutboxDatabase = OutboxDatabase.getInstance(context)
        val server = FakeChatServer(File(context.filesDir, "offline-outbox/server.json"))
        val client = OutboxClient(HttpClient(server.engine()), Process.myPid())
        val log = OutboxEventLog()
    }

    @Volatile
    private var holder: Holder? = null

    fun get(context: Context): Holder =
        holder ?: synchronized(this) {
            holder ?: Holder(context.applicationContext).also { holder = it }
        }
}

/** 워커·화면이 남기는 기록(프로세스 안에서만 — 서버 기록은 파일에 남는다) */
class OutboxEventLog {
    private val format = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun add(clientId: String, text: String) {
        val line = "${format.format(Date())} pid ${Process.myPid()} · ${shortId(clientId)} · $text"
        _lines.update { (listOf(line) + it).take(MAX_LINES) }
    }

    fun clear() = _lines.update { emptyList() }

    private companion object {
        const val MAX_LINES = 40
    }
}

/**
 * 아웃박스 행 하나를 보내는 워커. 입력은 clientId 뿐 — 본문·대화는 행에서 읽는다(행이 단일 진실).
 * - 성공: 행에 server_id·seq 를 채우고 SENT
 * - 재시도 사유(IOException·408·429·5xx): [OUTBOX_MAX_ATTEMPTS] 번째 전까지 Result.retry() — 백오프는 WorkManager 가 잰다
 * - 영구 사유·상한 도달: 행을 FAILED 로 내린 뒤 [FailureHandling] 대로 끝낸다
 */
class OutboxSendWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val runtime = OutboxRuntime.get(applicationContext)
        val dao = runtime.db.outboxDao()
        val clientId = inputData.getString(KEY_CLIENT_ID) ?: return Result.failure()
        val failure = runCatching { FailureHandling.valueOf(inputData.getString(KEY_FAILURE).orEmpty()) }
            .getOrDefault(FailureHandling.MARK_ROW)
        val newIdEveryAttempt = inputData.getBoolean(KEY_NEW_ID_EVERY_ATTEMPT, false)
        val attempt = runAttemptCount + 1

        val row = dao.get(clientId) ?: return Result.success()
        if (row.status != OutboxStatus.PENDING) {
            // 같은 행에 작업이 두 번 걸려도(재등록·다시 보내기) 이미 끝난 행은 다시 보내지 않는다
            runtime.log.add(clientId, "시도 $attempt — 이미 ${row.status}, 건너뜀")
            return Result.success()
        }

        // 올바른 방식: 행에 저장해 둔 id 를 그대로 보낸다. 함정: 워커 안에서 매번 새 id 를 만든다
        val sendId = if (newIdEveryAttempt) newClientId() else row.clientId
        return when (val result = runtime.client.send(row.conversationId, sendId, row.body)) {
            is SendResult.Sent -> {
                dao.markSent(clientId, result.serverId, result.seq, attempt)
                runtime.log.add(clientId, "시도 $attempt → seq ${result.seq} · ${result.note}" + idNote(sendId, clientId))
                Result.success()
            }
            is SendResult.Retry -> {
                if (attempt >= OUTBOX_MAX_ATTEMPTS) {
                    dao.markFailed(clientId, attempt, "${result.reason} — 시도 $attempt 회 상한")
                    runtime.log.add(clientId, "시도 $attempt → ${result.reason} · 상한이라 FAILED · ${failure.label}")
                    finish(failure)
                } else {
                    dao.recordAttempt(clientId, attempt, result.reason)
                    runtime.log.add(clientId, "시도 $attempt → ${result.reason} · retry()" + idNote(sendId, clientId))
                    Result.retry()
                }
            }
            is SendResult.Permanent -> {
                dao.markFailed(clientId, attempt, result.reason)
                runtime.log.add(clientId, "시도 $attempt → ${result.reason} · FAILED · ${failure.label}")
                finish(failure)
            }
        }
    }

    private fun idNote(sendId: String, clientId: String) = if (sendId != clientId) " · 보낸 id ${shortId(sendId)}(새로 만듦)" else ""

    private fun finish(failure: FailureHandling): Result =
        if (failure == FailureHandling.RESULT_FAILURE) Result.failure() else Result.success()

    companion object {
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_FAILURE = "failure"
        const val KEY_NEW_ID_EVERY_ATTEMPT = "new_id_every_attempt"
    }
}

object OutboxWork {
    const val TAG = "offline-outbox"

    fun messageTag(clientId: String) = "offline-outbox-msg-$clientId"

    fun chainName(conversationId: String) = "offline-outbox-chain-$conversationId"

    /**
     * 행 하나에 작업 하나를 건다. 행 저장(Room)과 작업 등록(WorkManager 의 DB)은 **다른 DB 라 한 트랜잭션이 아니다** —
     * 그 사이에 프로세스가 죽으면 작업 없는 PENDING 행이 남으므로 화면이 열릴 때 [OutboxViewModel] 이 다시 건다.
     */
    fun enqueue(context: Context, row: OutboxMessageEntity, options: SendOptions) {
        val request = OneTimeWorkRequestBuilder<OutboxSendWorker>()
            .setInputData(
                workDataOf(
                    OutboxSendWorker.KEY_CLIENT_ID to row.clientId,
                    OutboxSendWorker.KEY_FAILURE to options.failure.name,
                    OutboxSendWorker.KEY_NEW_ID_EVERY_ATTEMPT to options.newIdEveryAttempt
                )
            )
            // 최소값 10초(MIN_BACKOFF_MILLIS). 지정하지 않으면 30초 — 데모가 기다리지 않게 최소로
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            // 기기가 진짜로 오프라인(비행기 모드)이면 실행되지 않고 ENQUEUED 로 기다린다
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(TAG)
            .addTag(messageTag(row.clientId))
            .build()
        val workManager = WorkManager.getInstance(context)
        when (options.mode) {
            EnqueueMode.CHAIN_APPEND_OR_REPLACE ->
                workManager.enqueueUniqueWork(chainName(row.conversationId), ExistingWorkPolicy.APPEND_OR_REPLACE, request)
            EnqueueMode.CHAIN_APPEND ->
                workManager.enqueueUniqueWork(chainName(row.conversationId), ExistingWorkPolicy.APPEND, request)
            EnqueueMode.INDEPENDENT ->
                workManager.enqueueUniqueWork("offline-outbox-one-${row.clientId}", ExistingWorkPolicy.KEEP, request)
        }
    }
}
