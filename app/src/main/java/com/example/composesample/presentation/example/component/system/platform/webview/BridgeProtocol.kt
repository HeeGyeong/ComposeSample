package com.example.composesample.presentation.example.component.system.platform.webview

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.net.URI

/**
 * WebView 브리지 예제의 순수 Kotlin 부분 — 안드로이드 클래스 없이 단위 테스트(WebViewBridgeProtocolTest)로 검증한다.
 *
 * - 요청↔응답 추적: dari(easyhooon/dari)의 MessageEntry·MessageStatus 와 같은 모양. requestId 로 짝짓고,
 *   requestId 가 없으면 응답을 기다리지 않는 fire-and-forget 이다.
 * - 앱이 응답을 "보냈다"(SUCCESS)와 웹이 응답을 "받았다"(WebDelivery)를 따로 센다 — 응답 스크립트가 SyntaxError 면
 *   앱 로그는 SUCCESS 인데 웹 Promise 는 영영 기다린다.
 * - 응답 데이터를 JS 코드에 끼워 넣는 두 방식(손 이스케이프 vs JSON 문자열 표기)과 evaluateJavascript 결과 해석.
 */

/** 브리지 메시지 하나의 상태 — dari 의 세 값(IN_PROGRESS·SUCCESS·ERROR) + 응답을 기다리지 않는 메시지 */
enum class BridgeStatus { IN_PROGRESS, SUCCESS, ERROR, FIRE_AND_FORGET }

/** 웹이 응답을 실제로 받았는지 — evaluateJavascript 결과로 판정한다(응답 함수가 true 를 돌려주면 DELIVERED) */
enum class WebDelivery { NOT_SENT, DELIVERED, FAILED }

data class BridgeEntry(
    val seq: Int,
    val handler: String,
    val requestId: String?,
    val requestData: String?,
    val status: BridgeStatus,
    val responseData: String? = null,
    val delivery: WebDelivery = WebDelivery.NOT_SENT,
    val requestAtMs: Long,
    val responseAtMs: Long? = null
) {
    val durationMs: Long? get() = responseAtMs?.minus(requestAtMs)
}

/**
 * 웹→앱 요청과 앱의 응답을 한 줄로 묶는 기록기. 호출은 한 스레드(메인)에서만 한다 — 브리지 콜백은 JavaBridge 스레드라
 * 호출하는 쪽이 메인으로 넘긴 뒤 부른다.
 */
class BridgeMessageTracker(private val clock: () -> Long) {
    private val list = mutableListOf<BridgeEntry>()
    private var seq = 0

    val entries: List<BridgeEntry> get() = list.toList()

    fun onRequest(handler: String, requestId: String?, data: String?): BridgeEntry {
        val entry = BridgeEntry(
            seq = ++seq,
            handler = handler,
            requestId = requestId,
            requestData = data,
            status = if (requestId == null) BridgeStatus.FIRE_AND_FORGET else BridgeStatus.IN_PROGRESS,
            requestAtMs = clock()
        )
        list += entry
        return entry
    }

    /**
     * requestId 로 기다리는 요청을 찾아 응답을 붙인다. 모르는 id·이미 끝난 요청이면 null — 응답이 두 번 와도 첫 응답만 남는다.
     * 응답이 도착한 순서와 상관없이 id 로 찾으므로 늦게 보낸 요청이 먼저 끝나도 엇갈리지 않는다.
     */
    fun onResponse(requestId: String, success: Boolean, data: String?): BridgeEntry? {
        val index = list.indexOfFirst { it.requestId == requestId && it.status == BridgeStatus.IN_PROGRESS }
        if (index < 0) return null
        val updated = list[index].copy(
            status = if (success) BridgeStatus.SUCCESS else BridgeStatus.ERROR,
            responseData = data,
            responseAtMs = clock()
        )
        list[index] = updated
        return updated
    }

    /** 응답 스크립트를 웹이 실행했는지 — 앱 쪽 상태(SUCCESS/ERROR)는 그대로 두고 전달 여부만 적는다 */
    fun onDelivery(requestId: String, delivered: Boolean) {
        val index = list.indexOfLast { it.requestId == requestId && it.responseAtMs != null }
        if (index < 0) return
        list[index] = list[index].copy(delivery = if (delivered) WebDelivery.DELIVERED else WebDelivery.FAILED)
    }

    fun clear() {
        list.clear()
        seq = 0
    }
}

/** URL 의 오리진(scheme://host[:port]) — about:blank·data: 처럼 호스트가 없으면 URL 앞부분 그대로 */
fun originOf(url: String): String {
    val uri = runCatching { URI(url) }.getOrNull() ?: return url.take(24)
    val host = uri.host ?: return url.take(24)
    val port = if (uri.port == -1) "" else ":${uri.port}"
    return "${uri.scheme}://$host$port"
}

// ==================== 응답 데이터를 JS 코드에 끼워 넣기 ====================

private val gson = Gson()

/**
 * dari 샘플의 callJs 와 같은 손 이스케이프 — 역슬래시·작은따옴표·\n 만 바꿔 작은따옴표 리터럴로 감싼다.
 * JSONObject.toString() 결과처럼 이미 \r 을 이스케이프한 JSON 텍스트를 넘길 때는 안전하지만,
 * 일반 텍스트에 \r 이 섞이면 리터럴 안에 줄 끝 문자가 그대로 남아 스크립트 전체가 SyntaxError 가 된다.
 */
fun manualJsLiteral(text: String): String =
    "'" + text.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n") + "'"

/**
 * JSON 문자열 표기 — JSON 문자열은 그대로 JS 문자열 리터럴이다(ES2019 부터 U+2028·U+2029 포함).
 * Gson 은 기본으로 < > & = ' 까지 \u 로 바꾸므로 따옴표로 리터럴을 끊고 코드를 끼워 넣는 것도 막힌다.
 */
fun jsonJsLiteral(text: String): String = gson.toJson(text)

/** JS 문자열 리터럴 안에 그대로 있으면 SyntaxError 가 되는 줄 끝 문자(\r·\n) 수 */
fun rawNewlineCount(literal: String): Int = literal.count { it == '\r' || it == '\n' }

/**
 * evaluateJavascript 결과(JSON 텍스트)를 읽을 수 있는 값으로 — 문자열이면 따옴표·이스케이프를 벗기고, 나머지는 그대로.
 * null(콜백 인자 자체가 null)은 "(null 인자)" 로 구분한다.
 */
fun decodeEvaluateResult(raw: String?): String {
    if (raw == null) return "(null 인자)"
    return runCatching {
        val element = JsonParser.parseString(raw)
        if (element.isJsonPrimitive && element.asJsonPrimitive.isString) element.asString else raw
    }.getOrDefault(raw)
}
