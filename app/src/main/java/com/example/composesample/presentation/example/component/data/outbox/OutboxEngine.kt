package com.example.composesample.presentation.example.component.data.outbox

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * 오프라인 아웃박스 전송의 엔진 — 안드로이드 클래스 없이 JVM 단위 테스트(OutboxEngineTest)로 검증한다.
 *
 * 출처: Manuel Lorenzo "Design a chat app (Android and iOS): outbox, idempotent sends and sync"(Android Weekly #749).
 * 서버는 [FakeChatServer] 를 MockEngine 에 물려 흉내 낸다 — 같은 clientId 는 한 번만 저장하고(멱등), 대화마다 seq 를 매기며,
 * 응답 유실·일시 장애(503)·영구 거부(400)·토큰 만료(401)·연결 실패를 스위치나 본문 표식으로 재현한다.
 * 서버 상태는 파일에 남겨 프로세스가 죽어도 "서버는 이미 저장했다"가 유지된다(응답 유실 뒤 앱 종료 실험의 전제).
 */

const val OUTBOX_ORIGIN = "https://chat.example"

/** 본문 표식 — 실험 카드가 전역 스위치 없이 메시지 하나에만 장애를 건다. 표식 1회성 판정도 서버 파일에 남는다 */
const val MARK_REJECT = "[거부]"
const val MARK_TRANSIENT = "[503]"
const val MARK_LOSE_ACK = "[유실]"

/** 서버가 보관한 메시지 — 상대방 화면에 보이는 것 */
data class StoredMessage(
    val serverId: Long,
    val conversationId: String,
    val clientId: String,
    val body: String,
    val seq: Long
)

/** 서버가 받은 요청 하나. pid 는 요청을 보낸 앱 프로세스 — 앱을 죽인 뒤 재시도가 새 프로세스에서 왔는지 보인다 */
data class ChatServerHit(
    val no: Int,
    val conversationId: String,
    val clientId: String,
    val status: Int,
    val note: String,
    val pid: Int
)

/** 응답을 잃었다 — 서버는 이미 저장했다. 연결이 응답 도중 끊긴 것과 같은 IOException */
class LostResponseException(message: String) : IOException(message)

/** 서버에 닿지 못했다(오프라인) — 서버는 아무것도 저장하지 않았다 */
class UnreachableException(message: String) : IOException(message)

// ==================== 저장 파일·요청·응답 모델 (nullable + 매퍼 — DataRules) ====================

private data class ServerFileJson(
    @SerializedName("next_server_id") val nextServerId: Long?,
    @SerializedName("messages") val messages: List<StoredMessageJson>?,
    @SerializedName("used_marks") val usedMarks: List<String>?,
    @SerializedName("hits") val hits: List<HitJson>?,
    @SerializedName("next_hit_no") val nextHitNo: Int?
)

private data class StoredMessageJson(
    @SerializedName("server_id") val serverId: Long?,
    @SerializedName("conversation_id") val conversationId: String?,
    @SerializedName("client_id") val clientId: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("seq") val seq: Long?
)

private data class HitJson(
    @SerializedName("no") val no: Int?,
    @SerializedName("conversation_id") val conversationId: String?,
    @SerializedName("client_id") val clientId: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("note") val note: String?,
    @SerializedName("pid") val pid: Int?
)

private data class SendRequestJson(
    @SerializedName("client_id") val clientId: String?,
    @SerializedName("body") val body: String?,
    @SerializedName("pid") val pid: Int?
)

private data class SendResponseJson(
    @SerializedName("server_id") val serverId: Long?,
    @SerializedName("seq") val seq: Long?,
    @SerializedName("replayed") val replayed: Boolean?
)

private fun StoredMessageJson.toStored(): StoredMessage? {
    return StoredMessage(
        serverId = serverId ?: return null,
        conversationId = conversationId ?: return null,
        clientId = clientId ?: return null,
        body = body.orEmpty(),
        seq = seq ?: return null
    )
}

private fun HitJson.toHit(): ChatServerHit? {
    return ChatServerHit(
        no = no ?: return null,
        conversationId = conversationId.orEmpty(),
        clientId = clientId.orEmpty(),
        status = status ?: 0,
        note = note.orEmpty(),
        pid = pid ?: 0
    )
}

// ==================== 가짜 서버 ====================

/**
 * 채팅 서버. `POST /conversations/{id}/messages` 에 `{client_id, body}` 를 받는다.
 * - 같은 대화에 같은 client_id 가 이미 있으면 **저장하지 않고 원래 server_id·seq 를 200 으로** 돌려준다(멱등)
 * - 새 메시지면 그 대화의 다음 seq 로 저장하고 201
 * - 순서는 서버가 매긴 seq 가 정한다 — 기기 시계는 쓰지 않는다
 *
 * @param file null 이면 메모리에만 둔다(단위 테스트). 앱은 filesDir 의 파일을 준다.
 */
class FakeChatServer(private val file: File?) {
    private val gson = Gson()

    // @Synchronized 대신 synchronized 블록 — debug(HotSwan) 재작성 아래에서는 @Synchronized 의 상호 배제가 사라진다
    private val lock = Any()
    private val messages = mutableListOf<StoredMessage>()
    private val usedMarks = HashSet<String>()
    private val hits = ArrayDeque<ChatServerHit>()
    private var nextServerId = 1L
    private var nextHitNo = 1

    /** 서버가 받아 준 토큰. 401 스위치를 켜면 바뀌어 클라이언트가 가진 토큰이 만료된다 */
    @Volatile var validToken = "token-1"
        private set

    // 채팅 카드에서 손으로 거는 1회성 스위치 — 남은 횟수만큼 다음 요청에 적용된다
    val loseNextAck = AtomicInteger(0)
    val failNext503 = AtomicInteger(0)
    val rejectNext400 = AtomicInteger(0)
    val expireTokenNext = AtomicInteger(0)

    @Volatile var offline = false
    @Volatile var latencyMs = 300L

    /** 저장·요청 기록이 바뀔 때 — ViewModel 이 화면 상태를 다시 읽는다 */
    @Volatile var onChange: () -> Unit = {}

    init {
        load()
    }

    fun messages(): List<StoredMessage> = synchronized(lock) { messages.sortedWith(compareBy({ it.conversationId }, { it.seq })) }

    fun messages(conversationId: String): List<StoredMessage> =
        synchronized(lock) { messages.filter { it.conversationId == conversationId }.sortedBy { it.seq } }

    /** 최신이 앞 */
    fun hits(): List<ChatServerHit> = synchronized(lock) { hits.toList().asReversed() }

    /** 대화 id 가 prefix 로 시작하는 메시지·기록을 지운다(카드별 초기화) */
    fun clear(prefix: String) {
        synchronized(lock) {
            messages.removeAll { it.conversationId.startsWith(prefix) }
            hits.removeAll { it.conversationId.startsWith(prefix) }
            usedMarks.removeAll { it.substringAfter('|').startsWith(prefix) }
            persistLocked()
        }
        onChange()
    }

    /** 토큰 발급(가짜 갱신 엔드포인트) */
    fun issueToken(): String = validToken

    sealed interface Reply {
        data class Http(val status: Int, val body: String) : Reply
        /** 저장까지 끝낸 뒤 응답을 잃는다 */
        data object Lost : Reply
        /** 서버에 닿지 않았다 */
        data object Unreachable : Reply
    }

    /** 메시지 요청 하나를 처리한다. 화면·테스트가 같은 판정을 보도록 MockEngine 밖에서도 부를 수 있다 */
    fun handleSend(conversationId: String, authorization: String?, requestBody: String): Reply {
        val reply = synchronized(lock) { handleSendLocked(conversationId, authorization, requestBody) }
        onChange()
        return reply
    }

    private fun handleSendLocked(conversationId: String, authorization: String?, requestBody: String): Reply {
        val request = runCatching { gson.fromJson(requestBody, SendRequestJson::class.java) }.getOrNull()
        val clientId = request?.clientId
        val body = request?.body
        val pid = request?.pid ?: 0
        if (clientId == null || body == null) return http(conversationId, "?", pid, 400, "client_id·body 누락")

        if (offline) {
            record(conversationId, clientId, pid, 0, "연결 실패(오프라인) — 저장 안 함")
            persistLocked()
            return Reply.Unreachable
        }
        if (expireTokenNext.decrementIfPositive()) {
            validToken = "token-${validToken.substringAfter('-').toInt() + 1}"
        }
        if (authorization != "Bearer $validToken") return http(conversationId, clientId, pid, 401, "토큰 만료 — 저장 안 함")

        val markKey = "$conversationId|$body"
        if (body.startsWith(MARK_REJECT) || rejectNext400.decrementIfPositive()) {
            return http(conversationId, clientId, pid, 400, "영구 거부 — 저장 안 함")
        }
        if ((body.startsWith(MARK_TRANSIENT) && usedMarks.add("503|$markKey")) || failNext503.decrementIfPositive()) {
            return http(conversationId, clientId, pid, 503, "일시 장애 — 저장 안 함")
        }

        val existing = messages.firstOrNull { it.conversationId == conversationId && it.clientId == clientId }
        if (existing != null) {
            return http(
                conversationId, clientId, pid, 200, "같은 client_id — 저장 안 함, 원래 seq ${existing.seq} 반환",
                SendResponseJson(existing.serverId, existing.seq, true)
            )
        }

        val seq = (messages.filter { it.conversationId == conversationId }.maxOfOrNull { it.seq } ?: 0L) + 1
        val stored = StoredMessage(nextServerId++, conversationId, clientId, body, seq)
        messages += stored
        val lose = (body.startsWith(MARK_LOSE_ACK) && usedMarks.add("lost|$markKey")) || loseNextAck.decrementIfPositive()
        if (lose) {
            record(conversationId, clientId, pid, 0, "저장(seq $seq) 뒤 응답 유실")
            persistLocked()
            return Reply.Lost
        }
        return http(conversationId, clientId, pid, 201, "저장 seq $seq", SendResponseJson(stored.serverId, seq, false))
    }

    private fun http(conversationId: String, clientId: String, pid: Int, status: Int, note: String, response: SendResponseJson? = null): Reply {
        record(conversationId, clientId, pid, status, note)
        persistLocked()
        return Reply.Http(status, response?.let { gson.toJson(it) } ?: "{}")
    }

    private fun record(conversationId: String, clientId: String, pid: Int, status: Int, note: String) {
        hits.addLast(ChatServerHit(nextHitNo++, conversationId, clientId, status, note, pid))
        while (hits.size > MAX_HITS) hits.removeFirst()
    }

    /** MockEngine 에 물리는 엔진 — 메시지 요청은 [handleSend], `/token` 은 토큰 갱신 */
    fun engine(): MockEngine = MockEngine { request ->
        delay(latencyMs)
        val path = request.url.encodedPath
        if (path == "/token") {
            return@MockEngine respond(issueToken(), HttpStatusCode.OK)
        }
        val conversationId = path.removePrefix("/conversations/").substringBefore('/')
        val text = (request.body as? TextContent)?.text.orEmpty()
        when (val reply = handleSend(conversationId, request.headers[HttpHeaders.Authorization], text)) {
            is Reply.Http -> respond(
                content = reply.body,
                status = HttpStatusCode.fromValue(reply.status),
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
            // checked 예외를 람다에서 던진다 — debug(HotSwan)에서는 UndeclaredThrowableException 으로 감싸여 나온다.
            // 그래서 클라이언트는 예외 타입이 아니라 원인 사슬로 판정한다([classifyFailure]).
            Reply.Lost -> throw LostResponseException("응답 유실 — 서버는 저장했다")
            Reply.Unreachable -> throw UnreachableException("서버에 닿지 못함")
        }
    }

    private fun load() {
        val f = file ?: return
        if (!f.exists()) return
        val json = runCatching { gson.fromJson(f.readText(), ServerFileJson::class.java) }.getOrNull() ?: return
        synchronized(lock) {
            messages.clear()
            json.messages.orEmpty().mapNotNullTo(messages) { it.toStored() }
            usedMarks.clear()
            usedMarks.addAll(json.usedMarks.orEmpty())
            hits.clear()
            json.hits.orEmpty().mapNotNullTo(hits) { it.toHit() }
            nextServerId = json.nextServerId ?: ((messages.maxOfOrNull { it.serverId } ?: 0L) + 1)
            nextHitNo = json.nextHitNo ?: ((hits.maxOfOrNull { it.no } ?: 0) + 1)
        }
    }

    private fun persistLocked() {
        val f = file ?: return
        val json = ServerFileJson(
            nextServerId = nextServerId,
            messages = messages.map { StoredMessageJson(it.serverId, it.conversationId, it.clientId, it.body, it.seq) },
            usedMarks = usedMarks.toList(),
            hits = hits.map { HitJson(it.no, it.conversationId, it.clientId, it.status, it.note, it.pid) },
            nextHitNo = nextHitNo
        )
        // 임시 파일에 쓰고 이름을 바꾼다 — 쓰는 도중 프로세스가 죽어도 이전 파일이 남는다
        f.parentFile?.mkdirs()
        val tmp = File(f.parentFile, f.name + ".tmp")
        tmp.writeText(gson.toJson(json))
        if (!tmp.renameTo(f)) {
            f.delete()
            tmp.renameTo(f)
        }
    }

    private fun AtomicInteger.decrementIfPositive(): Boolean {
        while (true) {
            val current = get()
            if (current <= 0) return false
            if (compareAndSet(current, current - 1)) return true
        }
    }

    private companion object {
        const val MAX_HITS = 60
    }
}

// ==================== 클라이언트와 재시도 분류 ====================

/** 응답 하나를 어떻게 처리할지 — 워커와 화면의 분류표가 같은 함수를 쓴다 */
enum class SendDecision(val label: String) {
    SUCCESS("성공"),
    RETRY("재시도 (백오프)"),
    REFRESH_THEN_RETRY("토큰 갱신 후 1회 재시도"),
    PERMANENT("영구 실패 → FAILED")
}

/** 원문의 규칙: IOException·408·429·5xx 는 재시도, 그 밖의 4xx 는 영구, 401 은 토큰을 한 번 갱신하고 다시 */
fun decideStatus(code: Int): SendDecision = when {
    code in 200..299 -> SendDecision.SUCCESS
    code == 401 -> SendDecision.REFRESH_THEN_RETRY
    code == 408 || code == 429 -> SendDecision.RETRY
    code in 500..599 -> SendDecision.RETRY
    else -> SendDecision.PERMANENT
}

/** 예외는 타입이 아니라 원인 사슬로 본다 — 사슬 어디에든 IOException 이 있으면 네트워크 문제라 재시도 */
fun classifyFailure(t: Throwable): SendDecision {
    var cause: Throwable? = t
    var depth = 0
    while (cause != null && depth < 10) {
        if (cause is IOException) return SendDecision.RETRY
        cause = cause.cause
        depth++
    }
    return SendDecision.PERMANENT
}

/** 사슬에서 가장 안쪽의 의미 있는 예외 이름 — 화면 기록용 */
fun rootName(t: Throwable): String {
    var cause: Throwable = t
    var depth = 0
    while (depth < 10) {
        val next = cause.cause ?: break
        cause = next
        depth++
    }
    return cause.javaClass.simpleName
}

sealed interface SendResult {
    /** replayed = 서버가 이미 가진 client_id 라 저장하지 않고 원래 seq 를 돌려줌 */
    data class Sent(val serverId: Long, val seq: Long, val replayed: Boolean, val note: String) : SendResult
    data class Retry(val reason: String) : SendResult
    data class Permanent(val reason: String) : SendResult
}

/**
 * 아웃박스 행 하나를 서버로 보낸다. 같은 행을 다시 보낼 때는 **행에 저장된 clientId 를 그대로** 써야 한다 —
 * 그 규칙을 지키는 것은 호출자(워커)의 몫이고, 이 클래스는 받은 id 를 보낼 뿐이다.
 */
class OutboxClient(private val http: HttpClient, private val pid: Int = 0) {
    private val gson = Gson()

    @Volatile var token = "token-1"
        private set

    suspend fun send(conversationId: String, clientId: String, body: String): SendResult {
        var refreshed = false
        while (true) {
            val response = try {
                http.post("$OUTBOX_ORIGIN/conversations/$conversationId/messages") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(TextContent(gson.toJson(SendRequestJson(clientId, body, pid)), ContentType.Application.Json))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val decision = classifyFailure(e)
                val name = rootName(e)
                return if (decision == SendDecision.RETRY) SendResult.Retry(name) else SendResult.Permanent("예상 못 한 예외 $name")
            }
            val code = response.status.value
            when (decideStatus(code)) {
                SendDecision.SUCCESS -> {
                    val parsed = runCatching { gson.fromJson(response.bodyAsText(), SendResponseJson::class.java) }.getOrNull()
                    val serverId = parsed?.serverId
                    val seq = parsed?.seq
                    if (serverId == null || seq == null) return SendResult.Permanent("$code 인데 server_id·seq 없음")
                    val replayed = parsed.replayed ?: false
                    val note = buildString {
                        append(code)
                        if (replayed) append(" 같은 seq 반환(재전송)")
                        if (refreshed) append(" · 토큰 갱신 후")
                    }
                    return SendResult.Sent(serverId, seq, replayed, note)
                }
                SendDecision.REFRESH_THEN_RETRY -> {
                    if (refreshed) return SendResult.Permanent("401 — 갱신한 토큰도 거부")
                    refreshed = true
                    refreshToken()
                }
                SendDecision.RETRY -> return SendResult.Retry("$code")
                SendDecision.PERMANENT -> return SendResult.Permanent("$code")
            }
        }
    }

    private suspend fun refreshToken() {
        val response = http.post("$OUTBOX_ORIGIN/token")
        token = response.bodyAsText()
    }
}

// ==================== 백오프 ====================

/** 시도 횟수 상한 — 이 횟수째에도 재시도 사유면 행을 FAILED 로 내린다(체인을 막지 않도록) */
const val OUTBOX_MAX_ATTEMPTS = 4

/** WorkManager EXPONENTIAL 백오프: n 번째 재시도 전 대기 = 초기값 × 2^(n-1). 초기값 최소 10초(MIN_BACKOFF_MILLIS) */
fun exponentialBackoffMillis(maxAttempts: Int, initialMillis: Long): List<Long> =
    (1 until maxAttempts).map { n -> initialMillis shl (n - 1) }
