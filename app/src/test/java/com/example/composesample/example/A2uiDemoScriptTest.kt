package com.example.composesample.example

import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import com.example.composesample.presentation.example.component.ui.dynamic.A2uiDemoScript
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * A2UI 예제(#120) 대본의 모양 검증 — 파서(android.util.JsonReader)는 기기에서만 돌아서, 여기서는 JSON 구조 규칙을 본다.
 * given : 대본의 메시지 5개 · 틀린 메시지 4개 · 주문 답 생성기
 * when : kotlinx.serialization 으로 JSON 을 풀어 봉투 · 컴포넌트 · 바인딩을 따라간다
 * then : 대본은 v0.9 규칙(봉투 하나 · 루트 'root' · 자식 id 가 모두 있음 · 바인딩 경로에 값이 있음)을 지키고, 틀린 메시지는 일부러 하나씩 어긴다
 */
class A2uiDemoScriptTest {

    private val envelopes = setOf("createSurface", "updateComponents", "updateDataModel", "deleteSurface")
    private val basicTypes = setOf("Text", "Column", "Row", "Card", "TextField", "Slider", "CheckBox", "Button", "Divider")

    private fun parse(json: String): JsonObject = Json.parseToJsonElement(json).jsonObject

    private fun envelopeOf(message: JsonObject): Pair<String, JsonObject> {
        val keys = message.keys - "version"
        assertEquals("봉투는 하나: $keys", 1, keys.size)
        val name = keys.single()
        return name to message.getValue(name).jsonObject
    }

    private fun components(step: Int): List<JsonObject> =
        envelopeOf(parse(A2uiDemoScript.steps[step].json)).second.getValue("components").jsonArray.map { it.jsonObject }

    @Test
    fun `대본은 전부 v0_9 봉투 하나 · 같은 surface · 순서는 create - components - data - components - delete`() {
        val names = A2uiDemoScript.steps.map { step ->
            val message = parse(step.json)
            assertEquals("v0.9", message.getValue("version").jsonPrimitive.content)
            val (name, body) = envelopeOf(message)
            assertTrue(name in envelopes)
            assertEquals(A2uiDemoScript.SURFACE_ID, body.getValue("surfaceId").jsonPrimitive.content)
            name
        }
        assertEquals(
            listOf("createSurface", "updateComponents", "updateDataModel", "updateComponents", "deleteSurface"),
            names
        )
    }

    @Test
    fun `카탈로그 id 는 라이브러리의 기본 카탈로그 id 와 같다`() {
        assertEquals(A2uiBasicCatalogV1.CatalogId, A2uiDemoScript.BASIC_CATALOG_ID)
        val create = envelopeOf(parse(A2uiDemoScript.steps[0].json)).second
        assertEquals(A2uiBasicCatalogV1.CatalogId, create.getValue("catalogId").jsonPrimitive.content)
    }

    @Test
    fun `컴포넌트 트리 — id 는 겹치지 않고 루트가 있으며 자식 id 가 모두 정의돼 있다(4 단계는 앞 트리에 더한다)`() {
        val first = components(1)
        val second = components(3)
        val ids = first.map { it.getValue("id").jsonPrimitive.content }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue("root" in ids)
        val known = (ids + second.map { it.getValue("id").jsonPrimitive.content }).toSet()
        (first + second).forEach { c ->
            assertTrue(c.getValue("component").jsonPrimitive.content in basicTypes)
            c["child"]?.let { assertTrue(it.jsonPrimitive.content in known) }
            c["children"]?.jsonArray?.forEach { assertTrue(it.jsonPrimitive.content in known) }
        }
    }

    @Test
    fun `바인딩한 경로는 3 단계 데이터 모델에 모두 값이 있다`() {
        val model = envelopeOf(parse(A2uiDemoScript.steps[2].json)).second.getValue("value")
        val paths = mutableListOf<String>()
        fun collect(e: JsonElement) {
            when (e) {
                is JsonObject -> {
                    (e["path"] as? JsonPrimitive)?.let { paths += it.content }
                    e.values.forEach(::collect)
                }
                is JsonArray -> e.forEach(::collect)
                else -> Unit
            }
        }
        components(1).forEach(::collect)
        assertEquals(setOf("/form/name", "/form/cups", "/form/takeout", "/status"), paths.toSet())
        paths.forEach { path ->
            var node: JsonElement = model
            path.trim('/').split('/').forEach { key -> node = node.jsonObject[key] ?: throw AssertionError("$path 없음") }
        }
    }

    @Test
    fun `주문 답 — 이벤트 문맥을 상태 줄로, 따옴표는 이스케이프 · 모르는 값은 기본값`() {
        val reply = parse(A2uiDemoScript.orderReply(mapOf("name" to "Kim \"Ace\"", "cups" to 3.0, "takeout" to true)))
        val (name, body) = envelopeOf(reply)
        assertEquals("updateDataModel", name)
        assertEquals("/status", body.getValue("path").jsonPrimitive.content)
        assertEquals("주문 접수: Kim \"Ace\" · 3잔 · 포장", body.getValue("value").jsonPrimitive.content)

        val empty = parse(A2uiDemoScript.orderReply(mapOf("name" to "  ", "cups" to 99, "takeout" to "yes")))
        assertEquals("주문 접수: 이름 없음 · 5잔 · 매장", envelopeOf(empty).second.getValue("value").jsonPrimitive.content)
        val missing = parse(A2uiDemoScript.orderReply(emptyMap()))
        assertEquals("주문 접수: 이름 없음 · 1잔 · 매장", envelopeOf(missing).second.getValue("value").jsonPrimitive.content)
        assertEquals("a\\nb\\u0001", A2uiDemoScript.jsonEscape("a\nb\u0001"))
    }

    @Test
    fun `틀린 메시지는 하나씩 규칙을 어긴다 — 깨진 JSON · v0_8 · id 없음 · 카탈로그 밖 타입`() {
        val (broken, version, noId, unknown) = A2uiDemoScript.badCases
        try {
            Json.parseToJsonElement(broken.json)
            fail("깨진 JSON 이 풀리면 안 된다")
        } catch (_: Exception) {
            // 기대한 실패
        }
        assertEquals("v0.8", parse(version.json).getValue("version").jsonPrimitive.content)
        val noIdComponent = envelopeOf(parse(noId.json)).second.getValue("components").jsonArray.single().jsonObject
        assertTrue("id" !in noIdComponent)
        val unknownComponent = envelopeOf(parse(unknown.json)).second.getValue("components").jsonArray.single().jsonObject
        assertTrue(unknownComponent.getValue("component").jsonPrimitive.content !in basicTypes)
    }
}
