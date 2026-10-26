package com.example.composesample.example

import com.example.composesample.presentation.example.component.data.api.DEEP_DEPTH
import com.example.composesample.presentation.example.component.data.api.MemberPayload
import com.example.composesample.presentation.example.component.data.api.decodeMember
import com.example.composesample.presentation.example.component.data.api.failureRows
import com.example.composesample.presentation.example.component.data.api.gsonOutcome
import com.example.composesample.presentation.example.component.data.api.jsonCases
import com.example.composesample.presentation.example.component.data.api.leakRows
import com.example.composesample.presentation.example.component.data.api.lenientJson
import com.example.composesample.presentation.example.component.data.api.depthLimit
import com.example.composesample.presentation.example.component.data.api.strictJson
import com.example.composesample.presentation.example.component.data.api.valueClassRows
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GsonMissingFieldExample 6~8번 카드(kotlinx.serialization 1.12.0) 검증 — 같은 응답을 플러그인 serializer + Json 으로 파싱한다.
 * given : #141 의 다섯 응답 + '필수만' · 'null·새 플랜'
 * when : 기본 Json · 관대한 Json(ignoreUnknownKeys + coerceInputValues) · Gson ① 로 파싱한다
 * then : kotlinx 는 기본값을 넣거나 파싱하는 줄에서 경로와 함께 실패하고, Gson 은 성공한 채 계약을 어긴다
 */
class JsonMissingFieldTest {

    private fun case(label: String) = jsonCases.single { it.label == label }

    @Test
    fun `누락 키에는 생성자 기본값이 들어가고 Gson 은 같은 입력에서 계약을 어긴다`() {
        // Given: member_id 만 있는 응답
        val input = case("필수만").json

        // When
        val json = decodeMember(strictJson, input)
        val gson = gsonOutcome(input)

        // Then
        assertTrue(json.ok)
        assertEquals("JsonMember(memberId=m1, memberName=null, isActive=true, plan=FREE, roles=[])", json.text)
        assertFalse(gson.ok)
        assertTrue(gson.text, gson.text.startsWith("파싱 성공 · 계약 위반 2 · 조용히 틀림 1"))
    }

    @Test
    fun `계약을 어기는 응답은 파싱 단계에서 경로와 함께 실패한다`() {
        // Given / When
        val missing = decodeMember(strictJson, MemberPayload.MISSING.json)
        val explicitNull = decodeMember(strictJson, MemberPayload.EXPLICIT_NULL.json)
        val renamed = decodeMember(strictJson, MemberPayload.RENAMED.json)
        val newValues = decodeMember(strictJson, MemberPayload.NEW_VALUES.json)

        // Then
        assertTrue(missing.text, missing.text.startsWith("MissingFieldException") && missing.text.endsWith("missing at path: $"))
        assertTrue(explicitNull.text, explicitNull.text.startsWith("JsonDecodingException") && explicitNull.text.endsWith("(path $.member_id)"))
        assertTrue(renamed.text, renamed.text.contains("unknown key 'memberId'"))
        assertTrue(renamed.hint.orEmpty().contains("ignoreUnknownKeys"))
        assertTrue(newValues.text, newValues.text.endsWith("(path $.is_active)"))
    }

    @Test
    fun `관대한 설정은 null 과 모르는 enum 을 기본값으로 바꾸지만 필수값과 타입 불일치는 그대로 실패한다`() {
        // Given / When
        val coerced = decodeMember(lenientJson, case("null·새 플랜").json)
        val renamed = decodeMember(lenientJson, MemberPayload.RENAMED.json)
        val explicitNull = decodeMember(lenientJson, MemberPayload.EXPLICIT_NULL.json)
        val yesFlag = decodeMember(lenientJson, MemberPayload.NEW_VALUES.json)

        // Then
        assertEquals("JsonMember(memberId=m1, memberName=null, isActive=true, plan=FREE, roles=[])", coerced.text)
        assertTrue(renamed.text, renamed.text.startsWith("MissingFieldException"))
        assertTrue(explicitNull.text, explicitNull.text.endsWith("(path $.member_id)"))
        assertFalse(yesFlag.ok)
        // 기본 설정은 같은 입력을 is_active 의 null 에서 거부한다
        assertFalse(decodeMember(strictJson, case("null·새 플랜").json).ok)
    }

    @Test
    fun `1_12 는 init 검증과 깊은 중첩을 JsonDecodingException 으로 바꾸지만 필수 필드 누락은 JsonException 이 아니다`() {
        // Given / When
        val rows = failureRows().associateBy { it.label.substringBefore('(').trim() }
        val missing = rows.getValue("필수 필드 누락")
        val init = rows.getValue("init { require")
        val deep = failureRows().single { it.label.endsWith("겹 중첩(기본 한도)") }
        val unlimited = failureRows().single { it.label.contains("maxNestingDepth") }

        // Then
        assertTrue(missing.bySerialization)
        assertFalse("catch (e: JsonException) 는 MissingFieldException 을 놓친다", missing.byJson)
        assertTrue(init.thrown, init.thrown.startsWith("JsonDecodingException: Deserialization failed because of 'age < 0'"))
        assertEquals("IllegalArgumentException: age < 0", init.cause)
        assertTrue(deep.thrown, deep.thrown.contains("too nested"))
        assertTrue(deep.byJson)
        // 한도를 풀면 1.11 처럼 Error — catch (e: Exception) 로도 못 잡는다
        assertEquals("StackOverflowError", unlimited.thrown)
        assertFalse(unlimited.byException)
    }

    @Test
    fun `JVM 에서는 기본 한도 400 겹에서 too nested 로 멈춘다`() {
        // When
        val limit = depthLimit()

        // Then: 기기 debug 의 백그라운드 스레드는 그 전에 StackOverflowError — 화면 7번 카드가 그 기기의 값을 잰다
        assertEquals(400, limit.maxOk)
        assertEquals(401, limit.failedAt)
        assertTrue(limit.stoppedBy, limit.stoppedBy.contains("too nested"))
        assertTrue(DEEP_DEPTH > 400)
    }

    @Test
    fun `value class 의 private 생성자는 팩토리를 건너뛰고 init 검증과 붙인 serializer 는 필드에도 걸린다`() {
        // Given / When
        val rows = valueClassRows()

        // Then: 팩토리는 거부, Json 은 같은 값으로 객체를 만든다
        assertEquals("null", rows[0].result)
        assertEquals("FactoryPanCard(pan=FactoryPan(digits=41111111111111ab))", rows[1].result)
        assertTrue(rows[2].result, rows[2].result.contains("'PAN 형식 아님'") && rows[2].result.contains("(path $.pan)"))
        assertEquals("""{"pan":"PAN[****1111]","amount_minor":1200}""", rows[3].result)
        assertTrue(rows[4].result, rows[4].result.startsWith("SerializationException"))
    }

    @Test
    fun `예외 메시지는 다른 필드가 실패해도 입력 전체를 담고 exceptionsWithDebugInfo 를 끄면 빠진다`() {
        // Given / When
        val rows = leakRows()

        // Then: message·input 은 PAN 을 담고, shortMessage+path 와 debugInfo 끈 message 는 담지 않는다
        assertTrue(rows[0].text, rows[0].leaks && rows[0].text.contains("$.amount_minor"))
        assertTrue(rows[1].leaks)
        assertFalse(rows[2].leaks)
        assertFalse(rows[3].text, rows[3].leaks)
        assertTrue(rows[3].text.contains("$.amount_minor"))
    }
}
