package com.example.composesample.presentation.example.component.data.outbox

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.reflect.UndeclaredThrowableException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 오프라인 아웃박스 예제의 ViewModel — 화면은 Room 의 아웃박스 행만 그리고, 보내기는 행 저장 + 작업 등록이다.
 * DB·가짜 서버·클라이언트는 [OutboxRuntime] 이 프로세스당 하나로 들고 있어 워커와 같은 것을 본다.
 * 실험 카드는 대화 id 를 따로 써서 채팅 카드의 행·서버 기록을 건드리지 않는다.
 */
class OutboxViewModel(application: Application) : AndroidViewModel(application) {

    data class WorkUi(val state: WorkInfo.State, val runAttemptCount: Int, val nextRunAt: Long?)

    data class ServerUi(
        val loseNextAck: Int,
        val failNext503: Int,
        val rejectNext400: Int,
        val expireTokenNext: Int,
        val offline: Boolean,
        val validToken: String
    )

    data class AckRun(val label: String, val result: String, val serverCopies: Int, val serverLines: List<String>)

    data class OrderRun(val label: String, val serverOrder: String, val secondSentAfter: String, val note: String)

    data class ChainRun(val label: String, val lines: List<String>)

    data class ArrivalTable(val title: String, val rows: List<String>, val verdict: String)

    data class ClassifyRow(val input: String, val decision: String)

    private val app = application
    private val runtime = OutboxRuntime.get(application)
    private val dao = runtime.db.outboxDao()
    private val arrivalDao = runtime.db.arrivalDao()
    private val server = runtime.server
    private val workManager = WorkManager.getInstance(application)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)

    val chat: StateFlow<List<OutboxMessageEntity>> =
        dao.observe(CHAT_ID).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 메시지 태그(clientId)별 작업 상태 — 같은 행에 작업이 여러 번 걸렸으면 끝나지 않은 것을 먼저 */
    val work: StateFlow<Map<String, WorkUi>> = workManager.getWorkInfosByTagFlow(OutboxWork.TAG)
        .map { infos -> infos.groupByClientId().mapValues { (_, list) -> list.pick().toUi() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val log: StateFlow<List<String>> = runtime.log.lines

    private val _options = MutableStateFlow(SendOptions())
    val options: StateFlow<SendOptions> = _options.asStateFlow()

    private val _serverUi = MutableStateFlow(serverUi())
    val serverUi: StateFlow<ServerUi> = _serverUi.asStateFlow()

    private val _serverMessages = MutableStateFlow(server.messages(CHAT_ID))
    val serverMessages: StateFlow<List<StoredMessage>> = _serverMessages.asStateFlow()

    private val _serverHits = MutableStateFlow(chatHits())
    val serverHits: StateFlow<List<ChatServerHit>> = _serverHits.asStateFlow()

    private val _busy = MutableStateFlow<String?>(null)
    val busy: StateFlow<String?> = _busy.asStateFlow()

    private val _ackRuns = MutableStateFlow<List<AckRun>>(emptyList())
    val ackRuns: StateFlow<List<AckRun>> = _ackRuns.asStateFlow()

    private val _orderRuns = MutableStateFlow<List<OrderRun>>(emptyList())
    val orderRuns: StateFlow<List<OrderRun>> = _orderRuns.asStateFlow()

    private val _chainRuns = MutableStateFlow<List<ChainRun>>(emptyList())
    val chainRuns: StateFlow<List<ChainRun>> = _chainRuns.asStateFlow()

    private val _arrivals = MutableStateFlow<List<ArrivalTable>>(emptyList())
    val arrivals: StateFlow<List<ArrivalTable>> = _arrivals.asStateFlow()

    /** 화면의 재시도 분류표 — 워커와 같은 [decideStatus]·[classifyFailure] 로 계산한다 */
    val classifyRows: List<ClassifyRow> = buildList {
        listOf(201, 200, 408, 429, 500, 503, 400, 404, 409, 401).forEach { add(ClassifyRow("HTTP $it", decideStatus(it).label)) }
        add(ClassifyRow("LostResponseException(IOException)", classifyFailure(LostResponseException("x")).label))
        add(ClassifyRow("UndeclaredThrowable(LostResponse) — debug 의 감싼 형태", classifyFailure(UndeclaredThrowableException(LostResponseException("x"))).label))
        add(ClassifyRow("IllegalStateException", classifyFailure(IllegalStateException("x")).label))
    }

    /** EXPONENTIAL · 최소 10초에서 상한까지의 대기 */
    val backoffMillis: List<Long> = exponentialBackoffMillis(OUTBOX_MAX_ATTEMPTS, WorkRequest.MIN_BACKOFF_MILLIS)

    init {
        server.onChange = ::refreshServer
        viewModelScope.launch { reconcile() }
    }

    override fun onCleared() {
        server.onChange = {}
    }

    // ==================== 채팅 카드 ====================

    fun send(text: String) {
        val body = text.trim()
        if (body.isEmpty()) return
        viewModelScope.launch { insertAndEnqueue(CHAT_ID, body, _options.value) }
    }

    /** 연달아 셋 — 체인이면 서버 seq 가 친 순서대로, 따로면 장애가 걸린 것만 뒤로 밀린다 */
    fun sendBurst() {
        viewModelScope.launch {
            (1..3).forEach { insertAndEnqueue(CHAT_ID, "연속 $it", _options.value) }
        }
    }

    /** FAILED 행 다시 보내기 — 새 행이 아니라 같은 행(같은 clientId)을 PENDING 으로 되돌린다 */
    fun resend(clientId: String) {
        viewModelScope.launch {
            dao.resetToPending(clientId)
            val row = dao.get(clientId) ?: return@launch
            OutboxWork.enqueue(app, row, _options.value)
            runtime.log.add(clientId, "다시 보내기 — 같은 행·같은 id 로 작업 등록")
        }
    }

    fun resetChat() {
        viewModelScope.launch {
            workManager.cancelUniqueWork(OutboxWork.chainName(CHAT_ID))
            chat.value.forEach { workManager.cancelAllWorkByTag(OutboxWork.messageTag(it.clientId)) }
            dao.deleteByPrefix(CHAT_ID)
            server.clear(CHAT_ID)
            runtime.log.clear()
        }
    }

    fun setMode(mode: EnqueueMode) = _options.update { it.copy(mode = mode) }

    fun setFailure(failure: FailureHandling) = _options.update { it.copy(failure = failure) }

    fun setNewIdEveryAttempt(on: Boolean) = _options.update { it.copy(newIdEveryAttempt = on) }

    // ==================== 서버 스위치 ====================

    fun loseNextAck() = bump { server.loseNextAck.set(1) }

    fun failNext503() = bump { server.failNext503.set(2) }

    fun rejectNext400() = bump { server.rejectNext400.set(1) }

    fun expireTokenNext() = bump { server.expireTokenNext.set(1) }

    fun setOffline(on: Boolean) = bump { server.offline = on }

    fun clearSwitches() = bump {
        server.loseNextAck.set(0)
        server.failNext503.set(0)
        server.rejectNext400.set(0)
        server.expireTokenNext.set(0)
        server.offline = false
    }

    private fun bump(change: () -> Unit) {
        change()
        _serverUi.value = serverUi()
    }

    private fun serverUi() = ServerUi(
        loseNextAck = server.loseNextAck.get(),
        failNext503 = server.failNext503.get(),
        rejectNext400 = server.rejectNext400.get(),
        expireTokenNext = server.expireTokenNext.get(),
        offline = server.offline,
        validToken = server.validToken
    )

    private fun refreshServer() {
        _serverMessages.value = server.messages(CHAT_ID)
        _serverHits.value = chatHits()
        _serverUi.value = serverUi()
    }

    private fun chatHits(): List<ChatServerHit> = server.hits().filter { it.conversationId == CHAT_ID }.take(12)

    // ==================== 실험 ① 응답 유실: 같은 id vs 시도마다 새 id ====================

    fun runAckExperiment() = experiment("응답 유실 실험") {
        val base = freshPrefix("exp-ack")
        val same = insertAndEnqueue("$base-same", "$MARK_LOSE_ACK 안녕", SendOptions(newIdEveryAttempt = false))
        val fresh = insertAndEnqueue("$base-new", "$MARK_LOSE_ACK 안녕", SendOptions(newIdEveryAttempt = true))
        val finals = awaitSettled(listOf(same.clientId, fresh.clientId), timeoutMs = 60_000)
        _ackRuns.value = listOf(
            ackRun("행의 clientId 를 그대로", finals.getValue(same.clientId)),
            ackRun("시도마다 새 id (함정)", finals.getValue(fresh.clientId))
        )
    }

    private fun ackRun(label: String, settled: Settled): AckRun {
        val stored = server.messages(settled.row.conversationId)
        return AckRun(
            label = label,
            result = "${settled.describe()} · ${"%.1f".format(Locale.ROOT, settled.atMs / 1000.0)}초",
            serverCopies = stored.size,
            serverLines = stored.map { "seq ${it.seq} · ${it.body} · id ${shortId(it.clientId)}" }
        )
    }

    // ==================== 실험 ② 일시 장애와 순서: 대화별 체인 vs 메시지마다 따로 ====================

    fun runOrderExperiment() = experiment("순서 실험") {
        val base = freshPrefix("exp-order")
        val runs = listOf(
            "대화별 체인 (APPEND_OR_REPLACE)" to SendOptions(mode = EnqueueMode.CHAIN_APPEND_OR_REPLACE),
            "메시지마다 따로" to SendOptions(mode = EnqueueMode.INDEPENDENT)
        ).mapIndexed { index, (label, options) ->
            val conversationId = "$base-$index"
            val first = insertAndEnqueue(conversationId, "$MARK_TRANSIENT 첫째", options)
            val second = insertAndEnqueue(conversationId, "둘째", options)
            Triple(label, first, second)
        }
        _orderRuns.value = runs.map { (label, first, second) ->
            viewModelScope.async {
                val finals = awaitSettled(listOf(first.clientId, second.clientId), timeoutMs = 60_000)
                val order = server.messages(first.conversationId).joinToString(" → ") { it.body.removePrefix(MARK_TRANSIENT).trim() }
                val secondSettled = finals.getValue(second.clientId)
                OrderRun(
                    label = label,
                    serverOrder = order.ifEmpty { "(없음)" },
                    secondSentAfter = "%.1f초".format(Locale.ROOT, secondSettled.atMs / 1000.0),
                    note = "첫째 ${finals.getValue(first.clientId).describe()} · 둘째 ${secondSettled.describe()}"
                )
            }
        }.awaitAll()
    }

    // ==================== 실험 ③ 영구 실패 뒤의 체인 ====================

    fun runChainExperiment() = experiment("체인 실험") {
        val base = freshPrefix("exp-chain")
        val configs = listOf(
            "Result.failure() + APPEND" to SendOptions(mode = EnqueueMode.CHAIN_APPEND, failure = FailureHandling.RESULT_FAILURE),
            "Result.failure() + APPEND_OR_REPLACE" to SendOptions(mode = EnqueueMode.CHAIN_APPEND_OR_REPLACE, failure = FailureHandling.RESULT_FAILURE),
            "행만 FAILED · success() + APPEND" to SendOptions(mode = EnqueueMode.CHAIN_APPEND, failure = FailureHandling.MARK_ROW)
        )
        _chainRuns.value = configs.mapIndexed { index, (label, options) ->
            viewModelScope.async {
                val conversationId = "$base-$index"
                // 첫째(영구 거부)와 둘째를 함께 건다 → 둘째는 첫째 뒤에 매달린다
                val first = insertAndEnqueue(conversationId, "$MARK_REJECT 첫째", options)
                val second = insertAndEnqueue(conversationId, "둘째", options)
                val early = awaitSettled(listOf(first.clientId, second.clientId), timeoutMs = 30_000)
                // 체인이 끝난 뒤에 셋째를 건다 → 정책이 '끝난(실패한) 체인'을 어떻게 다루는지
                val third = insertAndEnqueue(conversationId, "셋째", options)
                val late = awaitSettled(listOf(third.clientId), timeoutMs = 30_000)
                ChainRun(
                    label = label,
                    lines = listOf(
                        "첫째(거부) ${early.getValue(first.clientId).describe()}",
                        "둘째(함께 등록) ${early.getValue(second.clientId).describe()}",
                        "셋째(끝난 뒤 등록) ${late.getValue(third.clientId).describe()}"
                    )
                )
            }
        }.awaitAll()
    }

    // ==================== 중복 도착 → 한 행 ====================

    /** 같은 메시지가 내 보내기·소켓·동기화·보내기 응답으로 네 번 들어온다. 키 설계 셋에 같은 순서로 넣는다 */
    fun runArrivalExperiment() = experiment("중복 도착") {
        arrivalDao.clearKey()
        arrivalDao.clearIndex()
        arrivalDao.clearReplace()
        val clientId = newClientId()
        val arrivals = listOf<Pair<Long?, String>>(
            null to "내 보내기(PENDING)",
            7L to "소켓 이벤트",
            7L to "푸시 → 동기화",
            7L to "보내기 응답"
        )
        arrivals.forEach { (seq, source) ->
            arrivalDao.upsertByKey(ArrivalByKeyEntity(clientId, "안녕", seq, source))
            arrivalDao.upsertByIndex(ArrivalByIndexEntity(clientId = clientId, body = "안녕", seq = seq, source = source))
            arrivalDao.replaceByIndex(ArrivalReplaceEntity(clientId = clientId, body = "안녕", seq = seq, source = source))
        }
        val byKey = arrivalDao.byKey()
        val byIndex = arrivalDao.byIndex()
        val byReplace = arrivalDao.byReplace()
        _arrivals.value = listOf(
            ArrivalTable(
                title = "① clientId 기본 키 + @Upsert",
                rows = byKey.map { "id ${shortId(it.clientId)} · seq ${it.seq} · ${it.source}" },
                verdict = verdict(byKey.size, byKey.firstOrNull()?.seq)
            ),
            ArrivalTable(
                title = "② 자동 증가 키 + clientId 고유 인덱스 + @Upsert",
                rows = byIndex.map { "local_id ${it.localId} · seq ${it.seq} · ${it.source}" },
                verdict = verdict(byIndex.size, byIndex.firstOrNull()?.seq)
            ),
            ArrivalTable(
                title = "③ ②와 같은 테이블 모양 + @Insert(REPLACE)",
                rows = byReplace.map { "local_id ${it.localId} · seq ${it.seq} · ${it.source}" },
                verdict = verdict(byReplace.size, byReplace.firstOrNull()?.seq) +
                    (byReplace.firstOrNull()?.let { if (it.localId != 1L) " · local_id 가 1 → ${it.localId} 로 바뀜" else "" } ?: "")
            )
        )
    }

    private fun verdict(rows: Int, seq: Long?): String = when {
        rows != 1 -> "행 $rows 개"
        seq == null -> "1행이지만 seq 가 비었다 — 뒤의 세 번은 반영되지 않음"
        else -> "1행 · seq $seq 반영"
    }

    // ==================== 공통 ====================

    private suspend fun insertAndEnqueue(conversationId: String, body: String, options: SendOptions): OutboxMessageEntity {
        val row = OutboxMessageEntity(
            clientId = newClientId(),
            conversationId = conversationId,
            body = body,
            status = OutboxStatus.PENDING,
            serverId = null,
            seq = null,
            createdAt = System.currentTimeMillis(),
            attempts = 0,
            lastError = null
        )
        // 1) 행을 먼저 저장한다 — 말풍선은 이 순간 PENDING 으로 보인다. 2) 그다음 작업을 건다
        dao.insert(row)
        OutboxWork.enqueue(app, row, options)
        if (conversationId == CHAT_ID) runtime.log.add(row.clientId, "행 저장(PENDING) → 작업 등록 · ${options.mode.label}")
        return row
    }

    /** 작업 없는 PENDING 행 — 행 저장과 작업 등록 사이에 프로세스가 죽은 경우 — 을 화면이 열릴 때 다시 건다 */
    private suspend fun reconcile() {
        for (row in dao.pending(CHAT_ID)) {
            val infos = workManager.getWorkInfosByTagFlow(OutboxWork.messageTag(row.clientId)).first()
            if (infos.none { !it.state.isFinished }) {
                OutboxWork.enqueue(app, row, _options.value)
                runtime.log.add(row.clientId, "작업 없는 PENDING 행 → 화면을 열 때 다시 등록")
            }
        }
    }

    private data class Settled(val row: OutboxMessageEntity, val workState: String, val atMs: Long, val timedOut: Boolean) {
        /** 행은 PENDING 인데 작업이 전부 끝났다 — 아무도 다시 보내지 않는다 */
        val stuck: Boolean get() = row.status == OutboxStatus.PENDING && !timedOut

        fun describe(): String = when {
            timedOut -> "시간 초과 · 행 ${row.status} · 작업 $workState"
            stuck -> "⛔ 행 PENDING 인데 작업 $workState — 멈춤"
            row.status == OutboxStatus.SENT -> "SENT seq ${row.seq} · 시도 ${row.attempts} · 작업 $workState"
            else -> "${row.status}(${row.lastError}) · 작업 $workState"
        }
    }

    /** 행이 끝나고(SENT·FAILED) 그 행의 작업도 전부 끝날 때까지, 또는 작업이 끝났는데 행이 PENDING 으로 남을 때까지 기다린다 */
    private suspend fun awaitSettled(clientIds: List<String>, timeoutMs: Long): Map<String, Settled> {
        val start = SystemClock.elapsedRealtime()
        val settled = LinkedHashMap<String, Settled>()
        while (settled.size < clientIds.size && SystemClock.elapsedRealtime() - start < timeoutMs) {
            for (id in clientIds) {
                if (id in settled) continue
                // 작업 상태를 먼저, 행을 나중에 읽는다 — 워커는 행을 고친 뒤에 끝나므로 '작업 끝남'을 본 다음 읽은 행은 최종 값이다.
                // 반대 순서면 두 읽기 사이에 워커가 끝날 때 옛 PENDING 행 + 끝난 작업을 '멈춤'으로 잘못 판정한다(release 에서 실제로 났다)
                val infos = workManager.getWorkInfosByTagFlow(OutboxWork.messageTag(id)).first()
                if (infos.isNotEmpty() && infos.all { it.state.isFinished }) {
                    val row = dao.get(id) ?: continue
                    settled[id] = Settled(row, infos.pick().state.name, SystemClock.elapsedRealtime() - start, timedOut = false)
                }
            }
            if (settled.size < clientIds.size) delay(200)
        }
        for (id in clientIds) {
            if (id in settled) continue
            val row = dao.get(id) ?: continue
            val infos = workManager.getWorkInfosByTagFlow(OutboxWork.messageTag(id)).first()
            settled[id] = Settled(row, infos.pickOrNull()?.state?.name ?: "없음", SystemClock.elapsedRealtime() - start, timedOut = true)
        }
        return settled
    }

    private fun experiment(name: String, block: suspend () -> Unit) {
        if (_busy.value != null) return
        _busy.value = name
        viewModelScope.launch {
            try {
                block()
            } finally {
                _busy.value = null
            }
        }
    }

    /** 실험마다 대화 id 를 새로 쓰고, 지난 실험의 행·서버 기록은 지운다 */
    private suspend fun freshPrefix(kind: String): String {
        dao.deleteByPrefix(kind)
        server.clear(kind)
        return "$kind-${timeFormat.format(Date()).replace(":", "")}"
    }

    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

    private fun List<WorkInfo>.groupByClientId(): Map<String, List<WorkInfo>> {
        val prefix = OutboxWork.messageTag("")
        return groupBy { info -> info.tags.firstOrNull { it.startsWith(prefix) }?.removePrefix(prefix).orEmpty() }
            .filterKeys { it.isNotEmpty() }
    }

    private fun List<WorkInfo>.pick(): WorkInfo = firstOrNull { !it.state.isFinished } ?: last()

    private fun List<WorkInfo>.pickOrNull(): WorkInfo? = if (isEmpty()) null else pick()

    private fun WorkInfo.toUi(): WorkUi = WorkUi(
        state = state,
        runAttemptCount = runAttemptCount,
        nextRunAt = nextScheduleTimeMillis.takeIf { state == WorkInfo.State.ENQUEUED && it in 1 until Long.MAX_VALUE }
    )

    companion object {
        const val CHAT_ID = "chat"
    }
}
