package com.example.composesample.presentation.example.component.system.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** 클라이언트와 서버가 나눠 갖는 비밀 키. 실제 앱이라면 소스에 두지 않는다(그 한계는 화면에서 다룬다). */
private const val SHARED_SECRET = "composesample-demo-shared-secret-v1"

private const val SIGNED_URL = "https://api.example.com/v1/orders"
private const val REQUEST_BODY = """{"orderId":"A-1024","amount":12000}"""

/** 서명이 유효한 것으로 인정되는 시간 창. 기사 권고대로 초·시간이 아니라 '분' 단위로 잡는다. */
private const val FRESHNESS_WINDOW_MILLIS = 60_000L

private const val HEADER_TIMESTAMP = "X-Timestamp"
private const val HEADER_NONCE = "X-Nonce"
private const val HEADER_SIGNATURE = "X-Signature"

/**
 * API 요청 서명(HMAC-SHA256)과 재전송 방지를 실제 OkHttp 인터셉터 체인으로 보여준다.
 *
 * **네트워크를 쓰지 않는다.** 클라이언트 서명 인터셉터 뒤에 "서버" 역할의 검증 인터셉터를 두고,
 * 그 인터셉터가 `chain.proceed()` 를 호출하는 대신 직접 [Response] 를 만들어 돌려준다.
 * 덕분에 실제 인터셉터 파이프라인(요청 변형 → 검증 → 응답)을 그대로 쓰면서 외부 서버가 필요 없다.
 *
 * 네 가지 시나리오가 "무엇이 요청을 거절시키는가"를 각각 하나씩 보여준다.
 *  - [Scenario.NORMAL]            : 정상 서명 → 200
 *  - [Scenario.TAMPERED_BODY]     : 서명 후 바디를 바꿔치기 → 바디 해시가 달라져 서명 불일치
 *  - [Scenario.EXPIRED_TIMESTAMP] : 서명 자체는 유효하지만 타임스탬프가 창 밖 → 거절
 *  - [Scenario.REPLAY]            : 직전에 통과한 요청을 그대로 재전송 → 같은 nonce 라 거절
 *
 * 마지막 시나리오가 중요한 이유: 서명만으로는 재전송을 막지 못한다. 가로챈 요청은 서명이 멀쩡하므로,
 * 서버가 nonce 를 기억하고 타임스탬프로 창을 좁혀야 비로소 한 번만 유효해진다.
 */
class RequestSigningViewModel : ViewModel() {

    enum class Scenario(val label: String) {
        NORMAL("① 정상 요청"),
        TAMPERED_BODY("② 바디 변조"),
        EXPIRED_TIMESTAMP("③ 타임스탬프 만료"),
        REPLAY("④ 재전송")
    }

    /** 서명 헤더 3종. 재전송 시나리오는 이 값을 그대로 다시 보낸다. */
    data class SignedHeaders(
        val timestamp: Long,
        val nonce: String,
        val signature: String
    )

    data class StepLog(
        val scenario: String,
        val accepted: Boolean,
        val statusLine: String,
        val reason: String,
        /** 서명 대상이 된 canonical string (클라이언트가 만든 것) */
        val canonical: String,
        val signature: String
    )

    /**
     * 비교 방식별로 **몇 바이트를 읽고 판정했는지** 센 결과.
     *
     * 시간을 재지 않고 읽은 바이트 수를 세는 이유: 디버그 빌드는 인터프리터로 돌아 ns 단위 측정이
     * 실행마다 요동친다(프로젝트에서 Room 벤치마크가 같은 이유로 배율이 튀었다). 읽은 바이트 수는
     * 결정적이고, "순진한 비교는 첫 불일치에서 멈춘다"는 성질 자체를 그대로 드러낸다.
     */
    data class CompareProbe(
        val label: String,
        val naiveBytesRead: Int,
        val constantTimeBytesRead: Int,
        val totalBytes: Int
    )

    data class UiState(
        val isRunning: Boolean = false,
        val logs: List<StepLog> = emptyList(),
        val probes: List<CompareProbe> = emptyList(),
        /** 서버가 이미 본 nonce. 실제 서버라면 TTL 붙은 저장소에 둔다. */
        val seenNonces: Set<String> = emptySet(),
        val lastAccepted: SignedHeaders? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    val requestUrl: String = SIGNED_URL
    val requestBody: String = REQUEST_BODY
    val freshnessWindowSeconds: Long = FRESHNESS_WINDOW_MILLIS / 1000

    fun run(scenario: Scenario) {
        if (_uiState.value.isRunning) return

        // 재전송은 "통과했던 요청"이 있어야 의미가 있다
        val replayTarget = _uiState.value.lastAccepted
        if (scenario == Scenario.REPLAY && replayTarget == null) {
            appendLog(
                StepLog(
                    scenario = scenario.label,
                    accepted = false,
                    statusLine = "실행 안 함",
                    reason = "먼저 ① 정상 요청을 실행해 통과시킨 뒤에 재전송할 수 있습니다.",
                    canonical = "",
                    signature = ""
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true) }

            val result = withContext(Dispatchers.IO) {
                runCatching { execute(scenario, replayTarget) }
                    .getOrElse { throwable ->
                        ExecutionResult(
                            log = StepLog(
                                scenario = scenario.label,
                                accepted = false,
                                statusLine = "오류",
                                reason = "${throwable::class.java.simpleName}: ${throwable.message.orEmpty()}",
                                canonical = "",
                                signature = ""
                            )
                        )
                    }
            }

            _uiState.update { state ->
                state.copy(
                    isRunning = false,
                    logs = state.logs + result.log,
                    seenNonces = result.seenNonces ?: state.seenNonces,
                    lastAccepted = result.acceptedHeaders ?: state.lastAccepted
                )
            }
        }
    }

    /** 순진한 비교 vs 상수 시간 비교가 각각 몇 바이트를 읽는지 센다. */
    fun measureComparison() {
        val correct = hmacSha256(SHARED_SECRET, "canonical-request-sample")
        val probes = listOf(
            "첫 바이트부터 다름" to forge(correct, 0),
            "9번째 바이트부터 다름" to forge(correct, 8),
            "마지막 바이트만 다름" to forge(correct, correct.lastIndex),
            "완전히 일치" to correct.copyOf()
        ).map { (label, candidate) ->
            CompareProbe(
                label = label,
                naiveBytesRead = naiveEquals(correct, candidate).second,
                constantTimeBytesRead = constantTimeEquals(correct, candidate).second,
                totalBytes = correct.size
            )
        }

        _uiState.update { it.copy(probes = probes) }
    }

    fun clear() {
        _uiState.update { it.copy(logs = emptyList(), seenNonces = emptySet(), lastAccepted = null) }
    }

    // ============================================================
    // 실행부 — 실제 OkHttp 체인
    // ============================================================

    private fun execute(scenario: Scenario, replayTarget: SignedHeaders?): ExecutionResult {
        // 서버가 기억하고 있는 nonce 를 이 호출 동안 쓰고, 통과하면 여기에 추가한다
        val seen = _uiState.value.seenNonces.toMutableSet()
        var captured: SignedHeaders? = null
        var verdict = Verdict(accepted = false, code = 500, reason = "검증이 실행되지 않았습니다")

        val client = OkHttpClient.Builder()
            // ① 클라이언트: 서명 헤더를 붙인다
            .addInterceptor(
                SigningInterceptor(
                    // 만료 시나리오는 창 밖의 과거 시각으로 '정상 서명'한다 — 서명은 유효한데 신선하지 않은 상태
                    timestampOverride = if (scenario == Scenario.EXPIRED_TIMESTAMP) {
                        System.currentTimeMillis() - FRESHNESS_WINDOW_MILLIS * 10
                    } else {
                        null
                    },
                    // 재전송 시나리오는 지난번 헤더를 그대로 다시 쓴다
                    replay = replayTarget.takeIf { scenario == Scenario.REPLAY },
                    onSigned = { captured = it }
                )
            )
            // ② 중간자: 서명이 끝난 뒤 바디만 바꿔치기한다
            .apply {
                if (scenario == Scenario.TAMPERED_BODY) {
                    addInterceptor(TamperInterceptor())
                }
            }
            // ③ 서버: 검증하고 여기서 응답을 만들어 반환한다(네트워크로 나가지 않는다)
            .addInterceptor(
                VerifyingInterceptor(seenNonces = seen, onVerdict = { verdict = it })
            )
            .build()

        val request = Request.Builder()
            .url(SIGNED_URL)
            .post(REQUEST_BODY.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val (code, message) = client.newCall(request).execute().use { it.code to it.message }
        val signed = captured

        return ExecutionResult(
            log = StepLog(
                scenario = scenario.label,
                accepted = verdict.accepted,
                statusLine = "HTTP $code $message",
                reason = verdict.reason,
                canonical = verdict.canonicalSeenByServer,
                signature = signed?.signature.orEmpty()
            ),
            seenNonces = seen.toSet(),
            acceptedHeaders = signed.takeIf { verdict.accepted }
        )
    }

    private data class ExecutionResult(
        val log: StepLog,
        val seenNonces: Set<String>? = null,
        val acceptedHeaders: SignedHeaders? = null
    )

    private data class Verdict(
        val accepted: Boolean,
        val code: Int,
        val reason: String,
        val canonicalSeenByServer: String = ""
    )

    /**
     * 클라이언트 쪽 서명 인터셉터.
     *
     * canonical string 에 메서드·경로·타임스탬프·nonce·**바디 해시**를 모두 넣는 이유는,
     * 그중 하나라도 바뀌면 서명이 깨지도록 요청 전체를 서명 대상에 묶기 위해서다.
     */
    private class SigningInterceptor(
        private val timestampOverride: Long?,
        private val replay: SignedHeaders?,
        private val onSigned: (SignedHeaders) -> Unit
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()

            val headers = replay ?: run {
                val timestamp = timestampOverride ?: System.currentTimeMillis()
                val nonce = UUID.randomUUID().toString()
                val canonical = canonicalString(
                    method = request.method,
                    path = request.url.encodedPath,
                    timestamp = timestamp,
                    nonce = nonce,
                    bodyBytes = request.bodyBytes()
                )
                SignedHeaders(
                    timestamp = timestamp,
                    nonce = nonce,
                    signature = hmacSha256(SHARED_SECRET, canonical).toHex()
                )
            }

            onSigned(headers)

            return chain.proceed(
                request.newBuilder()
                    .header(HEADER_TIMESTAMP, headers.timestamp.toString())
                    .header(HEADER_NONCE, headers.nonce)
                    .header(HEADER_SIGNATURE, headers.signature)
                    .build()
            )
        }
    }

    /** 서명이 끝난 요청의 바디만 바꾸는 중간자. 헤더(서명)는 그대로 두는 것이 핵심이다. */
    private class TamperInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val tampered = """{"orderId":"A-1024","amount":99999999}"""
            return chain.proceed(
                chain.request().newBuilder()
                    .post(tampered.toRequestBody(JSON_MEDIA_TYPE))
                    .build()
            )
        }
    }

    /**
     * "서버" 역할. `chain.proceed()` 를 부르지 않고 직접 응답을 만들어 반환하므로 네트워크로 나가지 않는다.
     *
     * 검사 순서가 곧 설계 결정이다: **신선도 → nonce → 서명**.
     * 서명 계산이 가장 비싸므로 값싼 검사로 먼저 걸러낸다.
     */
    private class VerifyingInterceptor(
        private val seenNonces: MutableSet<String>,
        private val onVerdict: (Verdict) -> Unit
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val timestamp = request.header(HEADER_TIMESTAMP)?.toLongOrNull()
            val nonce = request.header(HEADER_NONCE)
            val signature = request.header(HEADER_SIGNATURE)

            val verdict = when {
                timestamp == null || nonce == null || signature == null ->
                    Verdict(false, 400, "서명 헤더가 없습니다.")

                kotlin.math.abs(System.currentTimeMillis() - timestamp) > FRESHNESS_WINDOW_MILLIS ->
                    Verdict(
                        accepted = false,
                        code = 401,
                        reason = "타임스탬프가 허용 창(${FRESHNESS_WINDOW_MILLIS / 1000}초) 밖입니다. " +
                            "서명 자체는 유효하지만 오래된 요청이라 받지 않습니다."
                    )

                nonce in seenNonces ->
                    Verdict(
                        accepted = false,
                        code = 401,
                        reason = "이미 사용된 nonce 입니다. 서명이 유효해도 두 번째부터는 거절합니다(재전송 차단)."
                    )

                else -> {
                    // 서버는 '받은 요청'으로 canonical string 을 다시 만든다 — 바디가 바뀌었다면 여기서 갈린다
                    val canonical = canonicalString(
                        method = request.method,
                        path = request.url.encodedPath,
                        timestamp = timestamp,
                        nonce = nonce,
                        bodyBytes = request.bodyBytes()
                    )
                    val expected = hmacSha256(SHARED_SECRET, canonical)
                    val actual = signature.fromHex()

                    if (actual != null && MessageDigest.isEqual(expected, actual)) {
                        seenNonces += nonce
                        Verdict(true, 200, "서명 일치 · 신선도 통과 · 처음 보는 nonce.", canonical)
                    } else {
                        Verdict(
                            accepted = false,
                            code = 401,
                            reason = "서명이 일치하지 않습니다. 서버가 받은 바디로 다시 계산한 값과 다릅니다.",
                            canonicalSeenByServer = canonical
                        )
                    }
                }
            }

            onVerdict(verdict)

            val payload = if (verdict.accepted) {
                """{"status":"accepted"}"""
            } else {
                """{"status":"rejected"}"""
            }

            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(verdict.code)
                .message(if (verdict.accepted) "OK" else "Unauthorized")
                .body(payload.toResponseBody(JSON_MEDIA_TYPE))
                .build()
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json".toMediaType()

        /**
         * 서명 대상 문자열. 줄바꿈으로 필드를 구분해 "구분자 없는 이어붙이기" 때 생기는 모호함을 피한다
         * (예: a="xy",b="z" 와 a="x",b="yz" 가 같은 문자열이 되는 문제).
         */
        fun canonicalString(
            method: String,
            path: String,
            timestamp: Long,
            nonce: String,
            bodyBytes: ByteArray
        ): String = buildString {
            append(method).append('\n')
            append(path).append('\n')
            append(timestamp).append('\n')
            append(nonce).append('\n')
            append(MessageDigest.getInstance("SHA-256").digest(bodyBytes).toHex())
        }

        fun hmacSha256(secret: String, message: String): ByteArray {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
            return mac.doFinal(message.toByteArray())
        }

        /** 요청 바디를 바이트로 읽는다. OkHttp 의 RequestBody 는 스트림이라 Buffer 에 한 번 써서 꺼낸다. */
        fun Request.bodyBytes(): ByteArray {
            val body = body ?: return ByteArray(0)
            val buffer = Buffer()
            body.writeTo(buffer)
            return buffer.readByteArray()
        }

        fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

        fun String.fromHex(): ByteArray? {
            if (length % 2 != 0) return null
            return runCatching {
                ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
            }.getOrNull()
        }

        /** 지정한 위치의 바이트 하나만 뒤집은 위조 서명. */
        fun forge(source: ByteArray, flipAt: Int): ByteArray =
            source.copyOf().also { it[flipAt] = (it[flipAt].toInt() xor 0xFF).toByte() }

        /** 순진한 비교 — 첫 불일치에서 즉시 돌아간다. 몇 바이트를 읽었는지 함께 돌려준다. */
        fun naiveEquals(a: ByteArray, b: ByteArray): Pair<Boolean, Int> {
            if (a.size != b.size) return false to 0
            var read = 0
            for (i in a.indices) {
                read++
                if (a[i] != b[i]) return false to read
            }
            return true to read
        }

        /** 상수 시간 비교 — 결과와 무관하게 전부 읽고 차이를 OR 로 누적한다(MessageDigest.isEqual 과 같은 방식). */
        fun constantTimeEquals(a: ByteArray, b: ByteArray): Pair<Boolean, Int> {
            if (a.size != b.size) return false to 0
            var diff = 0
            var read = 0
            for (i in a.indices) {
                read++
                diff = diff or (a[i].toInt() xor b[i].toInt())
            }
            return (diff == 0) to read
        }
    }

    private fun appendLog(log: StepLog) {
        _uiState.update { it.copy(logs = it.logs + log) }
    }
}
