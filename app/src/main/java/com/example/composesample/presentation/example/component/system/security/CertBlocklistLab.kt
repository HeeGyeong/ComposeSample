package com.example.composesample.presentation.example.component.system.security

import okhttp3.Call
import okhttp3.CertificatePinner.Companion.sha256Hash
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

/**
 * App Security 예제 ①-2(인증서 차단 목록과 검사 시점)의 실험 로직 — ViewModel·단위 테스트가 함께 쓴다.
 *
 * 같은 비밀 요청(가짜 Authorization 헤더 + 본문)을 차단 목록 검사 자리만 바꿔 보내고,
 * EventListener 로 찍은 타임라인에서 '차단 전에 무엇이 서버로 나갔는가'를 비교한다.
 */

/**
 * CVE-2026-111943(CA 개인 키 유출) 대상 인증서의 SPKI SHA-256.
 * OkHttp 4.12.1·5.5.1 이 내장한 차단 목록(okhttp3.internal.tls.CertificateBlocklist)과 같은 값이다(단위 테스트가 대조).
 * 그보다 낮은 OkHttp 에서는 이 목록을 앱이 직접 HostnameVerifier 로 건다.
 */
val CVE_2026_111943_SPKI_SHA256: Set<String> = setOf(
    "43bffa19d6ef651451f6841e019931e96f6f2eaeff28ab4b6f1ce4576de2717b",
    "49dd353dc2b919a3770645117d0e60c16115e7d1bc259eefe734b00ea434fe5f",
    "5c0660a2b164910d8ff589c18e14552e9e0ec237dcefe2cb736d41a4811f81a0",
    "63ecefee431d49523bc1136c4e72d0206a477086caf606cd5cf025686c109cf8",
    "92c063d67e8f14db71d288e11266ffdaf69cbf115f7bab14f3bc2f68d25e0e30",
    "e0254c7fcdc265e2f0f8a428dabb75ab4ae039b4872d62f5ee57f0877d5a8a41",
    "ebed6475c2c2d80fcb199f3fe8dd6e6b3ce60b77eb15d4e42c3dec0c6f5b5105",
    "ec4ffa1ad0ac88081314b7aaffef6a91f97cd57291bb5ba686c5415f5eb4f6b6",
)

/** 차단 목록을 검사하는 자리 */
enum class CheckPlacement(val mark: String, val label: String) {
    NONE("대조", "검사 자리 없음 — OkHttp 내장 목록만"),
    APP_INTERCEPTOR("A", "애플리케이션 Interceptor — 받은 응답의 handshake"),
    NETWORK_INTERCEPTOR("B", "네트워크 Interceptor — proceed 전 chain.connection()"),
    HOSTNAME_VERIFIER("C", "HostnameVerifier — TLS 핸드셰이크 직후"),
}

/** 차단되기 전까지 서버로 나간 것 */
enum class SentBeforeBlock(val label: String) {
    NOTHING("요청 바이트 0 — 핸드셰이크 직후 연결을 버린다(secureConnectEnd 없음)"),
    CONNECTION_ONLY("연결만 — 요청 헤더·본문 0"),
    HEADERS("요청 헤더(Authorization 포함)까지 전송"),
    HEADERS_AND_BODY("요청 헤더(Authorization 포함)와 본문까지 전송"),
}

/** EventListener 이벤트 이름 목록으로 '차단 전에 나간 것'을 판정한다 */
fun sentBeforeBlock(eventNames: List<String>): SentBeforeBlock = when {
    "requestBodyEnd" in eventNames -> SentBeforeBlock.HEADERS_AND_BODY
    "requestHeadersEnd" in eventNames -> SentBeforeBlock.HEADERS
    "secureConnectEnd" in eventNames -> SentBeforeBlock.CONNECTION_ONLY
    else -> SentBeforeBlock.NOTHING
}

/** 체인에서 차단 목록에 걸린 첫 인증서 위치 — 없으면 null */
fun firstBlocked(chainSpkiSha256: List<String>, blocklist: Set<String>): Int? =
    chainSpkiSha256.indexOfFirst { it in blocklist }.takeIf { it >= 0 }

/** 실험에서 '유출됐다'고 가정할 인증서 — CVE 처럼 중간 CA(1번), 체인이 한 장이면 leaf */
fun blockTargetIndex(chainSize: Int): Int? = when {
    chainSize >= 2 -> 1
    chainSize == 1 -> 0
    else -> null
}

/**
 * 예외에서 인증서 거절(SSLPeerUnverifiedException)을 찾는다 — 원인 사슬과 suppressed 까지 훑는다.
 * OkHttp 는 경로(IP)마다 연결을 시도하고 실패를 첫 예외의 suppressed 로 모으므로,
 * 첫 경로가 다른 이유(IPv6 연결 실패 등)로 실패했으면 거절은 suppressed 안에만 있다.
 */
fun peerRejection(throwable: Throwable): SSLPeerUnverifiedException? =
    throwable.causeChain()
        .flatMap { sequenceOf(it) + it.suppressed.asSequence().flatMap { suppressed -> suppressed.causeChain() } }
        .filterIsInstance<SSLPeerUnverifiedException>()
        .firstOrNull()

/** 예외를 화면용 몇 줄로 — 맨 바깥 예외, 그와 다르면 그 안에서 찾은 인증서 거절(메시지 둘째 줄부터 leaf 인증서가 찍힌다) */
fun describeFailure(throwable: Throwable, maxRejectionLines: Int = 3): String {
    fun Throwable.messageLines() = message.orEmpty().lines().map { it.trim().trimStart('|').trim() }.filter { it.isNotBlank() }
    fun Throwable.headline() = "${this::class.java.simpleName}: ${messageLines().firstOrNull().orEmpty()}"

    val rejection = peerRejection(throwable)
    if (rejection == null || rejection === throwable) {
        val shown = rejection ?: throwable
        return (listOf(shown.headline()) + shown.messageLines().drop(1).take(maxRejectionLines - 1)).joinToString("\n")
    }
    val where = if (throwable.causeChain().any { it === rejection }) "cause" else "suppressed"
    return (listOf(throwable.headline(), "└ $where: ${rejection.headline()}") +
        rejection.messageLines().drop(1).take(maxRejectionLines - 1).map { "  $it" }).joinToString("\n")
}

/** 예외 원인 사슬(자기 자신부터). 순환 사슬에 대비해 깊이를 제한한다 */
internal fun Throwable.causeChain(): Sequence<Throwable> =
    generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH)

/** X500 DN("CN=a, O=b, C=c")에서 CN 만 뽑는다. CN 이 없으면 DN 을 그대로 쓴다 */
internal fun commonName(distinguishedName: String): String =
    distinguishedName.split(",")
        .map { it.trim() }
        .firstOrNull { it.startsWith("CN=") }
        ?.removePrefix("CN=")
        ?: distinguishedName

/** 체인 인증서 한 장 — SPKI SHA-256 은 CertificatePinner 핀과 같은 해시(핀은 이것의 Base64) */
data class ChainCert(
    val index: Int,
    val subject: String,
    val spkiSha256: String,
    val inBuiltInList: Boolean
)

/** 호출 시작 기준 경과 시간(ms)과 이벤트 */
data class CallEvent(val atMs: Long, val name: String, val detail: String = "")

/** 검사 자리 하나의 결과 */
data class BlocklistTrial(
    val placement: CheckPlacement,
    val blocked: Boolean,
    val events: List<CallEvent>,
    /** 검사 코드가 불린 횟수 */
    val checkCalls: Int,
    /** A 에서 차단 직전에 이미 받아 둔 서버 응답(코드 + 본문 앞부분) */
    val serverReply: String? = null,
    val httpCode: Int? = null,
    val failure: String? = null
) {
    val sent: SentBeforeBlock get() = sentBeforeBlock(events.map { it.name })
}

/** 같은 클라이언트로 여러 번 요청했을 때 검사 횟수 — verify 는 커넥션마다, 네트워크 Interceptor 는 요청마다 */
data class ReuseCheck(
    val requests: Int,
    val verifyCalls: Int,
    val networkInterceptorCalls: Int,
    val connectionsOpened: Int,
    val protocol: String
)

/**
 * 실제 요청을 보내는 실험기. 검사마다 새 클라이언트(새 커넥션 풀)를 만들어 앞 실험의 커넥션을 재사용하지 않는다.
 * 검사 코드는 람다가 아니라 클래스로 둔다 — 디버그 빌드의 HotSwan 인터프리터가 람다에서 던진 checked 예외를 감싸기 때문.
 */
class CertBlocklistLab(private val readUrl: String, private val postUrl: String) {

    /** gist 와 같은 위임 대상 — OkHttp 기본 HostnameVerifier */
    private val defaultVerifier: HostnameVerifier = OkHttpClient().hostnameVerifier

    /** 검사 없이 접속해 서버가 보낸 체인을 읽는다 */
    fun readChain(): List<ChainCert> {
        val client = baseBuilder(EventLog()).build()
        try {
            client.newCall(Request.Builder().url(readUrl).build()).execute().use { response ->
                val certificates = response.handshake?.peerCertificates.orEmpty()
                return certificates.filterIsInstance<X509Certificate>().mapIndexed { index, certificate ->
                    val spki = certificate.sha256Hash().hex()
                    ChainCert(
                        index = index,
                        subject = commonName(certificate.subjectX500Principal.name),
                        spkiSha256 = spki,
                        inBuiltInList = spki in CVE_2026_111943_SPKI_SHA256
                    )
                }
            }
        } finally {
            client.release()
        }
    }

    /** 비밀 요청 한 번 — placement 자리에서 blocklist(SPKI SHA-256 hex)를 검사한다 */
    fun trial(placement: CheckPlacement, blocklist: Set<String>): BlocklistTrial {
        val log = EventLog()
        val checks = AtomicInteger()
        val serverReply = AtomicReference<String?>()
        val blocked = blocklist.map { it.decodeHex() }.toSet()
        val builder = baseBuilder(log)
        when (placement) {
            CheckPlacement.NONE -> Unit
            CheckPlacement.APP_INTERCEPTOR -> builder.addInterceptor(AppLayerCheck(blocked, log, checks, serverReply))
            CheckPlacement.NETWORK_INTERCEPTOR -> builder.addNetworkInterceptor(NetworkLayerCheck(blocked, log, checks))
            CheckPlacement.HOSTNAME_VERIFIER ->
                builder.hostnameVerifier(BlocklistHostnameVerifier(blocked, defaultVerifier, log, checks))
        }
        val client = builder.build()
        return try {
            client.newCall(secretRequest()).execute().use { response ->
                response.body.string()
                BlocklistTrial(placement, blocked = false, events = log.snapshot(), checkCalls = checks.get(), httpCode = response.code)
            }
        } catch (e: IOException) {
            BlocklistTrial(
                placement = placement,
                blocked = peerRejection(e) != null,
                events = log.snapshot(),
                checkCalls = checks.get(),
                serverReply = serverReply.get(),
                failure = describeFailure(e)
            )
        } finally {
            client.release()
        }
    }

    /** 차단 목록을 비운 채 같은 클라이언트로 requests 번 GET — 검사 코드가 몇 번 불리는지 센다 */
    fun reuseCheck(requests: Int = REUSE_REQUESTS): ReuseCheck {
        val log = EventLog()
        val verifyCalls = AtomicInteger()
        val networkCalls = AtomicInteger()
        val client = baseBuilder(log)
            .hostnameVerifier(BlocklistHostnameVerifier(emptySet(), defaultVerifier, log, verifyCalls))
            .addNetworkInterceptor(NetworkLayerCheck(emptySet(), log, networkCalls))
            .build()
        try {
            var protocol = ""
            repeat(requests) {
                client.newCall(Request.Builder().url(readUrl).build()).execute().use { response ->
                    response.body.string()
                    protocol = response.protocol.toString()
                }
            }
            return ReuseCheck(
                requests = requests,
                verifyCalls = verifyCalls.get(),
                networkInterceptorCalls = networkCalls.get(),
                connectionsOpened = log.snapshot().count { it.name == "connectEnd" },
                protocol = protocol
            )
        } finally {
            client.release()
        }
    }

    private fun baseBuilder(log: EventLog): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .eventListener(log)

    /** 가짜 토큰 + 본문 — 실제 비밀이 아니고, 공개 데모 서버(jsonplaceholder)는 본문을 그대로 돌려준다 */
    private fun secretRequest(): Request =
        Request.Builder()
            .url(postUrl)
            .header("Authorization", "Bearer $DEMO_TOKEN")
            .post(DEMO_BODY.toRequestBody(JSON))
            .build()

    /** 실험마다 만든 클라이언트의 커넥션·스레드를 바로 정리한다 */
    private fun OkHttpClient.release() {
        connectionPool.evictAll()
        dispatcher.executorService.shutdown()
    }

    private companion object {
        const val TIMEOUT_SECONDS = 10L
        const val REUSE_REQUESTS = 2
        const val PEEK_BYTES = 160L
        const val DEMO_TOKEN = "demo-token-157"
        const val DEMO_BODY = """{"title":"cert-blocklist-demo","body":"secret-157","userId":157}"""
        val JSON = "application/json; charset=utf-8".toMediaType()
    }

    /**
     * A — gist 첫 판(ForbiddenCertificatesInterceptor): 응답을 받은 뒤 handshake 를 본다.
     * 이 시점엔 요청 헤더·본문이 이미 서버에 도착했고 서버 응답까지 와 있다.
     */
    private class AppLayerCheck(
        private val blocked: Set<ByteString>,
        private val log: EventLog,
        private val checks: AtomicInteger,
        private val serverReply: AtomicReference<String?>
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            checks.incrementAndGet()
            val handshake = response.handshake ?: return response
            if (handshake.peerCertificates.hasBlocked(blocked)) {
                serverReply.set("HTTP ${response.code} · ${response.peekBody(PEEK_BYTES).string().replace(Regex("\\s+"), " ")}")
                log.mark("CHECK blocked", "응답을 받은 뒤 — 검사")
                response.close()
                throw SSLPeerUnverifiedException("forbidden certificate!")
            }
            log.mark("CHECK passed", "응답을 받은 뒤 — 검사")
            return response
        }
    }

    /** B — 네트워크 Interceptor: 커넥션은 이미 맺었지만 요청을 쓰기 전(proceed 전)에 본다 */
    private class NetworkLayerCheck(
        private val blocked: Set<ByteString>,
        private val log: EventLog,
        private val checks: AtomicInteger
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            checks.incrementAndGet()
            val handshake = chain.connection()?.handshake()
            if (handshake != null && handshake.peerCertificates.hasBlocked(blocked)) {
                log.mark("CHECK blocked", "proceed 전 — 검사")
                throw SSLPeerUnverifiedException("forbidden certificate!")
            }
            log.mark("CHECK passed", if (handshake == null) "chain.connection() 의 handshake 없음" else "proceed 전 — 검사")
            return chain.proceed(chain.request())
        }
    }

    /** C — gist 개정판(ForbiddenCertificatesHostnameVerifier): 기본 검증에 위임하고 체인을 대조한다 */
    private class BlocklistHostnameVerifier(
        private val blocked: Set<ByteString>,
        private val delegate: HostnameVerifier,
        private val log: EventLog,
        private val checks: AtomicInteger
    ) : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            checks.incrementAndGet()
            val hostnameOk = delegate.verify(hostname, session)
            val result = hostnameOk && !session.peerCertificates.toList().hasBlocked(blocked)
            log.mark(if (result) "CHECK passed" else "CHECK blocked", "verify = $result — 검사")
            return result
        }
    }

    /** 호출 하나의 이벤트를 시각과 함께 모은다. 연결 시도는 다른 스레드에서도 불려 스레드 안전한 목록을 쓴다 */
    private class EventLog : EventListener() {
        private val startNanos = System.nanoTime()
        private val events = CopyOnWriteArrayList<CallEvent>()

        fun mark(name: String, detail: String = "") {
            events += CallEvent((System.nanoTime() - startNanos) / 1_000_000, name, detail)
        }

        fun snapshot(): List<CallEvent> = events.toList()

        override fun callStart(call: Call) = mark("callStart")
        override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) =
            mark("dnsEnd", "주소 ${inetAddressList.size}개")
        override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) =
            mark("connectStart", inetSocketAddress.address.hostAddress.orEmpty())
        override fun secureConnectStart(call: Call) = mark("secureConnectStart")
        override fun secureConnectEnd(call: Call, handshake: Handshake?) =
            mark("secureConnectEnd", handshake?.tlsVersion?.javaName.orEmpty())
        override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) =
            mark("connectEnd", protocol?.toString().orEmpty())
        override fun connectFailed(
            call: Call,
            inetSocketAddress: InetSocketAddress,
            proxy: Proxy,
            protocol: Protocol?,
            ioe: IOException
        ) = mark("connectFailed", "${inetSocketAddress.address.hostAddress} · ${ioe::class.java.simpleName}")
        override fun connectionAcquired(call: Call, connection: Connection) = mark("connectionAcquired")
        override fun requestHeadersEnd(call: Call, request: Request) =
            mark("requestHeadersEnd", "헤더 ${request.headers.size}개(Authorization 포함)")
        override fun requestBodyEnd(call: Call, byteCount: Long) = mark("requestBodyEnd", "본문 ${byteCount}B")
        override fun responseHeadersEnd(call: Call, response: Response) = mark("responseHeadersEnd", "HTTP ${response.code}")
        override fun callEnd(call: Call) = mark("callEnd")
        override fun callFailed(call: Call, ioe: IOException) = mark("callFailed", ioe::class.java.simpleName)
    }
}

/** 체인 중 하나라도 차단 목록에 있는가 — gist 와 같이 SPKI SHA-256(CertificatePinner.sha256Hash)로 비교 */
private fun List<Certificate>.hasBlocked(blocked: Set<ByteString>): Boolean =
    filterIsInstance<X509Certificate>().any { it.sha256Hash() in blocked }

/** 예외 원인 사슬을 훑을 최대 깊이 — 순환 사슬에 걸려 무한 루프에 빠지지 않도록 둔다 */
private const val MAX_CAUSE_DEPTH = 10
