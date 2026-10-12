package com.example.composesample.example

import com.example.composesample.presentation.example.component.data.api.FieldProblem
import com.example.composesample.presentation.example.component.data.api.FlagInput
import com.example.composesample.presentation.example.component.data.api.MemberPayload
import com.example.composesample.presentation.example.component.data.api.MemberPlan
import com.example.composesample.presentation.example.component.data.api.converterRows
import com.example.composesample.presentation.example.component.data.api.failFastRows
import com.example.composesample.presentation.example.component.data.api.flagRows
import com.example.composesample.presentation.example.component.data.api.mapResult
import com.example.composesample.presentation.example.component.data.api.parseAllDefault
import com.example.composesample.presentation.example.component.data.api.parsePartial
import com.example.composesample.presentation.example.component.data.api.rows
import com.example.composesample.presentation.example.component.data.api.runUseProbes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * GsonMissingFieldExample 의 판정 검증 — 비정상 응답(누락·명시적 null·이름 변경·낯선 값)을 파서와 매퍼에 넣는다.
 * given : MemberPayload 다섯 가지 JSON
 * when : Gson(직접·Retrofit·Ktor)으로 모양이 다른 모델에 파싱하거나 응답 모델 + 매퍼를 거친다
 * then : 일부만 기본값인 모델은 non-null 계약이 깨지고, 매퍼는 NPE 없이 값이나 경계 오류를 낸다
 */
class GsonMissingFieldTest {

    @Test
    fun `일부만 기본값이면 누락 키에 Kotlin 기본값이 들어가지 않는다`() {
        // Given: member_name 만 있는 응답
        // When
        val member = parsePartial(MemberPayload.MISSING)
        val values: List<Any?> = listOf(member.memberId, member.plan, member.roles)

        // Then: non-null 선언 필드가 null, Boolean 기본값 true 는 false
        assertTrue(values.all { it == null })
        assertFalse(member.isActive)
        assertEquals(3, member.rows(MemberPayload.MISSING).count { it.problem == FieldProblem.BROKEN })
    }

    @Test
    fun `전부 기본값이면 누락 키는 기본값이지만 명시적 null 은 그대로 들어온다`() {
        // Given / When
        val missing = parseAllDefault(MemberPayload.MISSING)
        val explicitNull = parseAllDefault(MemberPayload.EXPLICIT_NULL)
        val nullValues: List<Any?> = listOf(explicitNull.memberId, explicitNull.plan, explicitNull.roles)

        // Then
        assertEquals("", missing.memberId)
        assertTrue(missing.isActive)
        assertEquals(emptyList<String>(), missing.roles)
        assertTrue(nullValues.all { it == null })
        // 원시 타입 Boolean 은 null 을 건너뛰어 기본값 true 유지
        assertTrue(explicitNull.isActive)
    }

    @Test
    fun `모르는 enum 값과 리스트 안 null 원소도 non-null 선언을 뚫는다`() {
        // Given / When
        val member = parseAllDefault(MemberPayload.NEW_VALUES)
        val plan: MemberPlan? = member.plan
        val roles: List<String?> = member.roles

        // Then
        assertNull(plan)
        assertTrue(roles.contains(null))
        // 문자열 "Y" 는 parseBoolean → false
        assertFalse(member.isActive)
    }

    @Test
    fun `파싱은 성공하고 NPE 는 값을 쓰는 줄에서 난다`() {
        // Given / When
        val results = runUseProbes().associateBy { it.probe.label }

        // Then: 로그·equals 는 통과, hashCode·copy·멤버 호출·when 은 NPE, 엘비스는 막는다
        assertTrue(results.getValue("로그·toString").ok)
        assertTrue(results.getValue("equals").ok)
        listOf("Set·Map 키 (hashCode)", "copy()", "String 사용", "List 사용", "for 순회", "enum when", "non-null 파라미터에 전달").forEach { label ->
            val result = results.getValue(label)
            assertFalse(label, result.ok)
            assertTrue("$label → ${result.text}", result.text.startsWith("NullPointerException"))
        }
        assertEquals("\"unknown\"", results.getValue("엘비스 방어").text)
    }

    @Test
    fun `Boolean 은 누락을 false 로, Boolean 널 타입도 Y 는 false 로 읽는다`() {
        // Given / When
        val rows = flagRows().associateBy { it.input }

        // Then
        assertEquals("false", rows.getValue(FlagInput.MISSING).asBoolean)
        assertEquals("null", rows.getValue(FlagInput.MISSING).asNullable)
        assertEquals("false", rows.getValue(FlagInput.YES).asNullable)
        assertEquals("\"Y\" → true", rows.getValue(FlagInput.YES).asString)
        assertTrue(rows.getValue(FlagInput.ONE).asBoolean.startsWith("✗ JsonSyntax"))
        assertEquals("null → 모름", rows.getValue(FlagInput.NULL).asString)
    }

    @Test
    fun `disableJdkUnsafe 는 생성자 없는 생성만 파싱 시점 예외로 바꾼다`() {
        // Given / When
        val rows = failFastRows()

        // Then: ① 은 두 입력 모두 JsonIOException, ② 의 명시적 null 은 통과(계약 위반 남음)
        rows.filter { it.model.startsWith("①") }.forEach { assertTrue(it.result, it.result.startsWith("JsonIOException")) }
        val allDefaultNull = rows.single { it.model.startsWith("②") && it.payload == MemberPayload.EXPLICIT_NULL }
        assertTrue(allDefaultNull.result.startsWith("통과"))
        assertEquals(FieldProblem.BROKEN, allDefaultNull.problem)
    }

    @Test
    fun `응답 모델과 매퍼는 모든 비정상 응답에서 NPE 없이 값이나 경계 오류를 낸다`() {
        MemberPayload.entries.forEach { payload ->
            // When
            val result = mapResult(payload)

            // Then: 실패는 필드 이름을 가진 IllegalArgumentException 뿐
            result.exceptionOrNull()?.let { error ->
                assertTrue("$payload → $error", error is IllegalArgumentException)
                assertEquals("member_id 누락", error.message)
            }
        }
        val newValues = mapResult(MemberPayload.NEW_VALUES).getOrThrow()
        assertNull(newValues.plan)
        assertEquals(listOf("admin"), newValues.roles)
        assertTrue(mapResult(MemberPayload.RENAMED).isFailure)
    }

    @Test
    fun `Retrofit 과 Ktor 의 Gson 컨버터도 직접 파싱과 같은 결과다`() {
        runBlocking {
            // When
            val rows = converterRows()

            // Then
            rows.forEach { row -> assertTrue("${row.payload}: ${row.direct} / ${row.retrofit} / ${row.ktor}", row.same) }
            assertEquals(MemberPayload.entries.size, rows.size)
        }
    }
}
