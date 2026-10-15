package com.example.composesample.example

import com.example.composesample.presentation.example.component.system.platform.webview.BridgeMessageTracker
import com.example.composesample.presentation.example.component.system.platform.webview.BridgeStatus
import com.example.composesample.presentation.example.component.system.platform.webview.WebDelivery
import com.example.composesample.presentation.example.component.system.platform.webview.decodeEvaluateResult
import com.example.composesample.presentation.example.component.system.platform.webview.jsonJsLiteral
import com.example.composesample.presentation.example.component.system.platform.webview.manualJsLiteral
import com.example.composesample.presentation.example.component.system.platform.webview.originOf
import com.example.composesample.presentation.example.component.system.platform.webview.rawNewlineCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * WebViewBridgeExample 의 순수 로직 검증 — 6번 카드(requestId 짝짓기)와 응답 리터럴, 4번 카드의 결과 해석.
 * given : 가짜 시계를 쓰는 BridgeMessageTracker · 응답 데이터 문자열
 * when : 요청·응답을 화면과 같은 순서(B·C·A 도착)로 넣거나 리터럴로 바꾼다
 * then : 상태·소요·전달 여부와 리터럴의 날 줄바꿈 수가 기기 실측과 같다
 */
class WebViewBridgeProtocolTest {

    private var now = 0L
    private val tracker = BridgeMessageTracker { now }

    @Test
    fun `응답이 늦게 보낸 순서로 와도 requestId 로 각자 자기 요청에 붙는다`() {
        // Given: A(600)·B(200)·C(400) 를 동시에 보냄
        tracker.onRequest("slowEcho", "req_1", """{"delay":600}""")
        tracker.onRequest("slowEcho", "req_2", """{"delay":200}""")
        tracker.onRequest("slowEcho", "req_3", """{"delay":400}""")

        // When: 도착 순서 B → C → A
        now = 200; tracker.onResponse("req_2", true, """{"for":"req_2"}""")
        now = 400; tracker.onResponse("req_3", true, """{"for":"req_3"}""")
        now = 600; tracker.onResponse("req_1", true, """{"for":"req_1"}""")

        // Then: 각 요청이 자기 응답과 자기 소요 시간을 갖는다
        val byId = tracker.entries.associateBy { it.requestId }
        listOf("req_1" to 600L, "req_2" to 200L, "req_3" to 400L).forEach { (id, duration) ->
            val entry = byId.getValue(id)
            assertEquals(BridgeStatus.SUCCESS, entry.status)
            assertEquals("""{"for":"$id"}""", entry.responseData)
            assertEquals(duration, entry.durationMs)
        }
    }

    @Test
    fun `모르는 id 와 두 번째 응답은 무시하고 끝나지 않은 요청은 IN_PROGRESS 로 남는다`() {
        // Given
        tracker.onRequest("fail", "req_1", null)
        tracker.onRequest("noResponse", "req_2", null)

        // When
        val first = tracker.onResponse("req_1", false, """{"error":"PERMISSION_DENIED"}""")
        val second = tracker.onResponse("req_1", true, "늦게 온 두 번째 응답")
        val unknown = tracker.onResponse("req_99", true, "누구의 응답도 아님")

        // Then
        assertEquals(BridgeStatus.ERROR, first?.status)
        assertNull(second)
        assertNull(unknown)
        assertEquals("""{"error":"PERMISSION_DENIED"}""", tracker.entries[0].responseData)
        assertEquals(BridgeStatus.IN_PROGRESS, tracker.entries[1].status)
        assertNull(tracker.entries[1].durationMs)
    }

    @Test
    fun `requestId 가 없으면 fire-and-forget 이고 응답이 붙지 않는다`() {
        // Given / When
        val entry = tracker.onRequest("logEvent", null, """{"screen":"home"}""")

        // Then
        assertEquals(BridgeStatus.FIRE_AND_FORGET, entry.status)
        assertEquals(WebDelivery.NOT_SENT, entry.delivery)
    }

    @Test
    fun `앱이 보낸 응답(SUCCESS)과 웹 수신 실패(FAILED)는 따로 남는다`() {
        // Given: 응답은 보냈다
        tracker.onRequest("plainText", "req_1", null)
        tracker.onResponse("req_1", true, "첫 줄\r\n둘째 줄")

        // When: 응답 스크립트가 SyntaxError 라 evaluateJavascript 결과가 "null"
        tracker.onDelivery("req_1", delivered = false)

        // Then
        val entry = tracker.entries.single()
        assertEquals(BridgeStatus.SUCCESS, entry.status)
        assertEquals(WebDelivery.FAILED, entry.delivery)
    }

    @Test
    fun `손 이스케이프는 CR 을 그대로 남기고 JSON 문자열 표기는 날 줄바꿈이 없다`() {
        // Given: 화면 6번 카드와 같은 일반 텍스트
        val text = "첫 줄\r\n둘째 줄"

        // When
        val manual = manualJsLiteral(text)
        val json = jsonJsLiteral(text)

        // Then: 화면 표시 "JSON 0 · 손 1" 과 같다
        assertEquals(1, rawNewlineCount(manual))
        assertEquals(0, rawNewlineCount(json))
        assertEquals("'첫 줄\r\\n둘째 줄'", manual)
        assertEquals("\"첫 줄\\r\\n둘째 줄\"", json)
    }

    @Test
    fun `JSON 문자열 표기는 작은따옴표와 태그 문자도 유니코드 이스케이프로 바꾼다`() {
        // Given: 리터럴을 끊고 코드를 끼워 넣으려는 값
        val text = "'); alert(1); ('</script>"

        // When
        val json = jsonJsLiteral(text)

        // Then: 따옴표 문자·꺾쇠가 리터럴 안에 그대로 남지 않는다
        assertFalse(json.contains('\''))
        assertFalse(json.contains('<'))
        assertTrue(json.contains("\\u0027"))
    }

    @Test
    fun `evaluateJavascript 결과의 문자열은 따옴표를 벗기고 나머지는 그대로 둔다`() {
        assertEquals("따옴표 \"안\" 문자열", decodeEvaluateResult("\"따옴표 \\\"안\\\" 문자열\""))
        assertEquals("2", decodeEvaluateResult("2"))
        assertEquals("{\"a\":1,\"b\":[2,3]}", decodeEvaluateResult("{\"a\":1,\"b\":[2,3]}"))
        assertEquals("null", decodeEvaluateResult("null"))
        assertEquals("(null 인자)", decodeEvaluateResult(null))
    }

    @Test
    fun `오리진은 scheme·host·port 이고 호스트가 없는 URL 은 앞부분 그대로다`() {
        assertEquals("https://bridge.example", originOf("https://bridge.example/"))
        assertEquals("https://other.example:8443", originOf("https://other.example:8443/a?b=c"))
        assertEquals("about:blank", originOf("about:blank"))
    }
}
