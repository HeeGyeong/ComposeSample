package com.example.composesample.presentation.example.component.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * API 오리진 페일오버 — "다른 호스트로 넘어가도 되는 요청인가"를 정하는 정책과 상태 기계.
 * 출처: Erfan Moghaddam, "Designing Safer API Failover in an Android App"(Android Weekly #747).
 * 원문은 OkHttp 애플리케이션 인터셉터로 설명하지만, 이 예제는 같은 정책을 Ktor 클라이언트 플러그인(on(Send))으로 옮겨
 * 기존 MockEngine 시나리오 위에서 원문의 "검증할 상황" 표를 그대로 실행한다.
 */

/** 오리진 = scheme + host + port. 경로·쿼리는 오리진이 아니다. */
internal data class ApiOrigin(val protocol: URLProtocol, val host: String, val port: Int = protocol.defaultPort) {
    override fun toString(): String = host
}

/** 쿨다운 안이라 시도하지 않고 바로 실패시킨 요청. 네트워크 호출은 일어나지 않는다. */
internal class OriginUnavailableException(message: String) : IOException(message)

internal object FailoverRules {
    /** 자동으로 다시 보내도 되는 메서드 — 원문의 좁은 예시(GET·HEAD·OPTIONS). POST 는 호출자에게 돌려준다(RFC 9110 §9.2.2). */
    fun mayReplayAutomatically(method: HttpMethod): Boolean =
        method == HttpMethod.Get || method == HttpMethod.Head || method == HttpMethod.Options

    /** 게이트웨이 실패만 전환 후보 — 400·401·404 는 다른 오리진에 보내도 결과가 같다. */
    fun isCandidateGatewayFailure(code: Int): Boolean = code == 502 || code == 503 || code == 504
}

/**
 * 세션 단위로 선택한 오리진 하나를 들고 있는 상태 기계.
 * Primary → (자격 있는 실패) → Secondary → (실패) → Unavailable → (쿨다운 경과 + 새 요청) → Primary.
 * 상태 전이는 잠금 안에서만 일어나고, 실패는 "그 요청이 실제로 쓴 오리진"으로만 판정한다.
 */
internal class OriginFailoverManager(
    val primary: ApiOrigin,
    val secondary: ApiOrigin?,
    private val cooldownMs: Long,
    /** Android 에서는 SystemClock::elapsedRealtime — 단조 증가하고 기기 절전 중에도 흐른다. 테스트는 가짜 시계를 넣는다. */
    private val clock: () -> Long,
    /** false 면 "실제로 쓴 오리진" 확인을 빼고 실패가 오면 무조건 상태를 넘긴다(동시 실패 함정 대조용). */
    private val checkUsedOrigin: Boolean = true
) {
    enum class State { PRIMARY, SECONDARY, UNAVAILABLE }

    private val lock = Any()
    private var state = State.PRIMARY
    private var unavailableSince = 0L

    val currentState: State get() = synchronized(lock) { state }

    fun manages(origin: ApiOrigin): Boolean = origin == primary || origin == secondary

    /** 이 요청에 쓸 오리진. Unavailable 이고 쿨다운 안이면 null, 쿨다운이 지났으면 Primary 로 되돌린다. */
    fun select(): ApiOrigin? = synchronized(lock) {
        when (state) {
            State.PRIMARY -> primary
            State.SECONDARY -> secondary
            State.UNAVAILABLE -> if (clock() - unavailableSince >= cooldownMs) {
                state = State.PRIMARY
                primary
            } else {
                null
            }
        }
    }

    /**
     * [used] 오리진에서 자격 있는 실패가 났다. 돌려주는 값은 이 요청이 한 번 더 보내 볼 오리진(없으면 null).
     * 다른 요청이 이미 상태를 넘겼다면 이 실패는 "옛 오리진의 실패"일 뿐이라 새 오리진을 떨어뜨리지 않는다.
     */
    fun reportFailure(used: ApiOrigin): ApiOrigin? = synchronized(lock) {
        val selectedNow = when (state) {
            State.PRIMARY -> primary
            State.SECONDARY -> secondary
            State.UNAVAILABLE -> null
        }
        if (checkUsedOrigin && used != selectedNow) {
            return selectedNow?.takeIf { it != used }
        }
        when (state) {
            State.PRIMARY -> if (secondary != null) {
                state = State.SECONDARY
                secondary
            } else {
                toUnavailable()
                null
            }
            State.SECONDARY -> {
                toUnavailable()
                null
            }
            State.UNAVAILABLE -> null
        }
    }

    private fun toUnavailable() {
        state = State.UNAVAILABLE
        unavailableSince = clock()
    }
}

/** URL 에 포트를 적지 않으면 URLBuilder.port 는 0(DEFAULT_PORT)이다 — 실효 포트로 바꿔 비교해야 443 오리진과 같아진다. */
private fun HttpRequestBuilder.origin(): ApiOrigin =
    ApiOrigin(url.protocol, url.host, url.port.takeIf { it != 0 } ?: url.protocol.defaultPort)

private fun HttpRequestBuilder.copyTo(origin: ApiOrigin): HttpRequestBuilder =
    HttpRequestBuilder().takeFrom(this).apply {
        // scheme·host·port 만 바꾸고 경로·쿼리·헤더·본문은 그대로 둔다
        url.protocol = origin.protocol
        url.host = origin.host
        url.port = origin.port
    }

/**
 * 관리 대상 오리진으로 가는 요청만 고쳐 쓰고, 자격 있는 실패면 다른 오리진으로 한 번만 다시 보낸다.
 * 취소(CancellationException)는 잡지 않는다 — 폴백을 띄우지 않고 그대로 올라간다.
 * Ktor 의 HttpRequestRetry 와 같이 매번 원본 요청의 복사본으로 proceed 한다(3.6.0 소스).
 */
internal fun originFailoverPlugin(manager: OriginFailoverManager, onLog: (String) -> Unit) =
    createClientPlugin("OriginFailover") {
        on(Send) { request ->
            if (!manager.manages(request.origin())) {
                onLog("관리 대상 아님(${request.url.host}) → 그대로 보냄")
                return@on proceed(request)
            }
            val selected = manager.select()
                ?: throw OriginUnavailableException("쿨다운 중 — 네트워크를 타지 않고 실패")
            val replayable = FailoverRules.mayReplayAutomatically(request.method)
            val method = request.method.value
            val first: HttpClientCall = try {
                proceed(request.copyTo(selected))
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (!replayable) {
                    onLog("$method → $selected 전송 실패(${e.message}) → 재생하지 않고 호출자에게")
                    throw e
                }
                val next = manager.reportFailure(selected) ?: throw e
                onLog("$method → $selected 전송 실패 → ↻ $next 로 한 번 더")
                return@on retryOnce(request, next, manager, onLog)
            }
            val code = first.response.status.value
            if (!FailoverRules.isCandidateGatewayFailure(code)) return@on first
            if (!replayable) {
                onLog("$method → $selected $code → 쓰기라 재생하지 않음")
                return@on first
            }
            val next = manager.reportFailure(selected)
            if (next == null) {
                onLog("$method → $selected $code → 넘어갈 곳 없음(${manager.currentState})")
                return@on first
            }
            onLog("$method → $selected $code → ↻ $next 로 한 번 더")
            retryOnce(request, next, manager, onLog)
        }
    }

/**
 * 다른 오리진으로 딱 한 번 다시 보낸다. 다시 보낸 쪽의 실패도 상태 기계에 보고하지만(보조 실패 → Unavailable),
 * 세 번째 시도는 하지 않는다 — 한 요청이 오리진 사이를 오가며 계속 재생되지 않게.
 */
private suspend fun Send.Sender.retryOnce(
    request: HttpRequestBuilder,
    origin: ApiOrigin,
    manager: OriginFailoverManager,
    onLog: (String) -> Unit
): HttpClientCall {
    val call = try {
        proceed(request.copyTo(origin))
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        manager.reportFailure(origin)
        onLog("${request.method.value} → $origin 전송 실패 → 그만 ⇒ ${manager.currentState}")
        throw e
    }
    val code = call.response.status.value
    if (FailoverRules.isCandidateGatewayFailure(code)) {
        manager.reportFailure(origin)
        onLog("${request.method.value} → $origin $code → 그만 ⇒ ${manager.currentState}")
    } else {
        onLog("${request.method.value} → $origin $code")
    }
    return call
}

// ──────────────────────────────────────────────────────────────────────────
// 원문의 "검증할 상황" 표 — MockEngine 으로 두 오리진을 흉내 내 실행한다
// ──────────────────────────────────────────────────────────────────────────

internal val PRIMARY_ORIGIN = ApiOrigin(URLProtocol.HTTPS, "primary.api.local")
internal val SECONDARY_ORIGIN = ApiOrigin(URLProtocol.HTTPS, "secondary.api.local")
private const val COOLDOWN_MS = 5_000L

internal enum class FailoverSituation(val situation: String, val expected: String) {
    PRIMARY_OK("주 오리진이 읽기에 성공", "주 오리진을 계속 쓴다"),
    PRIMARY_GATEWAY("읽기 중 주 오리진 503", "보조 오리진으로 한 번 다시"),
    CLIENT_ERROR("주 오리진이 401(인증 오류)", "전환 없이 그대로 돌려준다"),
    SECONDARY_FAILS("보조로 넘어간 뒤 보조도 503", "Unavailable 로"),
    COOLDOWN("쿨다운 중 새 요청", "바로 다시 시도하지 않는다"),
    COOLDOWN_OVER("쿨다운이 지난 뒤 새 요청", "주 오리진부터 다시"),
    CONCURRENT("주 오리진 요청 3개가 동시에 503", "옛 실패가 새 보조를 떨어뜨리지 않는다"),
    CANCELLED("요청이 취소됨", "폴백을 띄우지 않는다"),
    AMBIGUOUS_WRITE("POST 전송 중 연결 끊김(모호한 실패)", "재생하지 않고 호출자에게"),
    UNRELATED_HOST("관리하지 않는 호스트로 가는 요청", "손대지 않는다")
}

internal data class FailoverRun(
    val situation: FailoverSituation,
    val passed: Boolean,
    val actual: String,
    val log: List<String>
)

/** 오리진별 동작을 코드로 정하는 가짜 서버 두 대. 호출 수를 센다. */
private class TwoOrigins {
    val hits = ConcurrentHashMap<String, AtomicInteger>()
    var primary: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("primary ok") }
    var secondary: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("secondary ok") }
    var other: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { respond("other ok") }

    fun hit(host: String): Int = hits[host]?.get() ?: 0

    val engine = MockEngine { request ->
        hits.getOrPut(request.url.host) { AtomicInteger() }.incrementAndGet()
        when (request.url.host) {
            PRIMARY_ORIGIN.host -> primary(request)
            SECONDARY_ORIGIN.host -> secondary(request)
            else -> other(request)
        }
    }
}

private fun MockRequestHandleScope.gateway(): HttpResponseData =
    respond("unavailable", HttpStatusCode.ServiceUnavailable)

/** [situation] 하나를 새 서버·새 관리자·새 클라이언트로 실행하고 기대 결과와 비교한다. */
internal suspend fun runFailoverSituation(
    situation: FailoverSituation,
    checkUsedOrigin: Boolean = true
): FailoverRun = coroutineScope {
    val log = mutableListOf<String>()
    val onLog: (String) -> Unit = { line -> synchronized(log) { log += line } }
    val fakeNow = AtomicLong(0L)
    val servers = TwoOrigins()
    val manager = OriginFailoverManager(PRIMARY_ORIGIN, SECONDARY_ORIGIN, COOLDOWN_MS, fakeNow::get, checkUsedOrigin)
    val client = HttpClient(servers.engine) { install(originFailoverPlugin(manager, onLog)) }
    val url = "https://${PRIMARY_ORIGIN.host}/v1/items?page=2"

    suspend fun getStatus(): String = try {
        client.get(url).status.value.toString()
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        e::class.java.simpleName
    }

    val (passed, actual) = when (situation) {
        FailoverSituation.PRIMARY_OK -> {
            val s = getStatus()
            (s == "200" && servers.hit(SECONDARY_ORIGIN.host) == 0 && manager.currentState == OriginFailoverManager.State.PRIMARY) to
                "$s · 보조 호출 ${servers.hit(SECONDARY_ORIGIN.host)} · ${manager.currentState}"
        }

        FailoverSituation.PRIMARY_GATEWAY -> {
            servers.primary = { gateway() }
            val s = getStatus()
            (s == "200" && servers.hit(SECONDARY_ORIGIN.host) == 1 && manager.currentState == OriginFailoverManager.State.SECONDARY) to
                "$s · 보조 호출 ${servers.hit(SECONDARY_ORIGIN.host)} · ${manager.currentState}"
        }

        FailoverSituation.CLIENT_ERROR -> {
            servers.primary = { respond("unauthorized", HttpStatusCode.Unauthorized) }
            val s = getStatus()
            (s == "401" && servers.hit(SECONDARY_ORIGIN.host) == 0 && manager.currentState == OriginFailoverManager.State.PRIMARY) to
                "$s · 보조 호출 ${servers.hit(SECONDARY_ORIGIN.host)} · ${manager.currentState}"
        }

        FailoverSituation.SECONDARY_FAILS -> {
            servers.primary = { gateway() }
            servers.secondary = { gateway() }
            val s = getStatus()
            (s == "503" && manager.currentState == OriginFailoverManager.State.UNAVAILABLE) to
                "$s · ${manager.currentState}"
        }

        FailoverSituation.COOLDOWN -> {
            servers.primary = { gateway() }
            servers.secondary = { gateway() }
            getStatus()
            val before = servers.hits.values.sumOf { it.get() }
            fakeNow.set(COOLDOWN_MS / 2)
            val s = getStatus()
            val calls = servers.hits.values.sumOf { it.get() } - before
            (s == "OriginUnavailableException" && calls == 0) to "$s · 새 네트워크 호출 $calls"
        }

        FailoverSituation.COOLDOWN_OVER -> {
            servers.primary = { gateway() }
            servers.secondary = { gateway() }
            getStatus()
            servers.primary = { respond("primary back") }
            fakeNow.set(COOLDOWN_MS + 1)
            val before = servers.hit(PRIMARY_ORIGIN.host)
            val s = getStatus()
            (s == "200" && servers.hit(PRIMARY_ORIGIN.host) == before + 1 && manager.currentState == OriginFailoverManager.State.PRIMARY) to
                "$s · 주 오리진 호출 +${servers.hit(PRIMARY_ORIGIN.host) - before} · ${manager.currentState}"
        }

        FailoverSituation.CONCURRENT -> {
            // 세 요청이 모두 주 오리진을 쓴 뒤에 503 을 받게 한다
            servers.primary = { delay(100); gateway() }
            val results = (1..3).map { async { getStatus() } }.awaitAll()
            (results.all { it == "200" } && manager.currentState == OriginFailoverManager.State.SECONDARY) to
                "${results.joinToString("/")} · ${manager.currentState}"
        }

        FailoverSituation.CANCELLED -> {
            servers.primary = { delay(1_000); gateway() }
            val job = launch { getStatus() }
            delay(100)
            job.cancel()
            job.join()
            (servers.hit(SECONDARY_ORIGIN.host) == 0 && manager.currentState == OriginFailoverManager.State.PRIMARY) to
                "취소됨 · 보조 호출 ${servers.hit(SECONDARY_ORIGIN.host)} · ${manager.currentState}"
        }

        FailoverSituation.AMBIGUOUS_WRITE -> {
            servers.primary = { throw IOException("connection reset") }
            val s = try {
                client.post("https://${PRIMARY_ORIGIN.host}/v1/payments") { setBody("{\"amount\":1000}") }.status.value.toString()
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                e::class.java.simpleName
            }
            (s == "IOException" && servers.hit(SECONDARY_ORIGIN.host) == 0) to
                "$s · 보조 호출 ${servers.hit(SECONDARY_ORIGIN.host)}"
        }

        FailoverSituation.UNRELATED_HOST -> {
            val s = try {
                client.get("https://cdn.other.local/logo.png").status.value.toString()
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                e::class.java.simpleName
            }
            (s == "200" && servers.hit("cdn.other.local") == 1 && servers.hit(PRIMARY_ORIGIN.host) == 0) to
                "$s · cdn 호출 ${servers.hit("cdn.other.local")} · 주 오리진 호출 ${servers.hit(PRIMARY_ORIGIN.host)}"
        }
    }
    client.close()
    FailoverRun(situation, passed, actual, synchronized(log) { log.toList() })
}
