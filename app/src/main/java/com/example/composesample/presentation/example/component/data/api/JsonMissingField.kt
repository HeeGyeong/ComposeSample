package com.example.composesample.presentation.example.component.data.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import kotlinx.serialization.json.JsonException
import java.util.Locale

/**
 * GsonMissingFieldExample 6~8번 카드 — 같은 응답을 kotlinx.serialization(컴파일러 플러그인이 만든 serializer) + Json 으로 파싱한다.
 * 화면(GsonMissingFieldExampleUI)과 단위 테스트(JsonMissingFieldTest)가 함께 쓴다. 측정 버전: kotlinx-serialization 1.12.0.
 *
 * 플러그인이 만든 serializer 는 생성자를 부른다 — 누락 키에는 Kotlin 기본값이 들어가고, 기본값 없는 키가 빠지거나
 * non-null 에 null 이 오면 '파싱하는 줄'에서 경로와 함께 실패한다. Gson 처럼 성공한 뒤 값을 쓰는 줄에서 NPE 가 나지 않는다.
 */

// ──────────────────────────────────────────────────────────────────────────
// 6번 카드 — 같은 응답을 기본 Json · 관대한 Json 으로
// ──────────────────────────────────────────────────────────────────────────

/** ① PartialDefaultMember 와 같은 모양 — member_id 만 기본값이 없다. enum 은 @Serializable 없이도 플러그인이 serializer 를 만든다 */
@Serializable
data class JsonMember(
    @SerialName("member_id") val memberId: String,
    @SerialName("member_name") val memberName: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("plan") val plan: MemberPlan = MemberPlan.FREE,
    @SerialName("roles") val roles: List<String> = emptyList()
)

/** 6번 카드의 입력 한 가지 */
class JsonCase(val label: String, val note: String, val json: String)

/** #141 의 다섯 응답 앞뒤에, kotlinx 쪽 차이가 드러나는 입력 둘을 더한다 */
val jsonCases: List<JsonCase> = buildList {
    add(JsonCase("필수만", "member_id 만 보냄 — 나머지 키가 없다", """{"member_id":"m1"}"""))
    MemberPayload.entries.forEach { add(JsonCase(it.label, it.note, it.json)) }
    add(
        JsonCase(
            "null·새 플랜", "필수값은 있고 선택 필드가 null · 모르는 enum",
            """{"member_id":"m1","is_active":null,"plan":"FAMILY","roles":null}"""
        )
    )
}

/** 기본 설정 — 모르는 키 · 타입 불일치 · non-null 자리의 null 을 전부 오류로 */
val strictJson: Json = Json

/** 관대한 설정 — 모르는 키는 건너뛰고, null · 모르는 enum 값은 '기본값이 있는' 프로퍼티에서만 기본값으로 바꾼다 */
val lenientJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/** 한 칸의 결과. 실패면 text 는 예외 타입 + 짧은 설명(경로 포함), hint 는 라이브러리가 붙인 설정 안내 */
data class DecodeOutcome(val ok: Boolean, val text: String, val hint: String? = null)

private const val PACKAGE_PREFIX = "com.example.composesample.presentation.example.component.data.api."

/** 화면 칸에 맞춘 한 줄 — JsonDecodingException 은 shortMessage + path, 나머지는 message(입력 JSON 줄이 없다) */
@OptIn(ExperimentalSerializationApi::class)
fun SerializationException.shortText(): String = when (this) {
    is JsonDecodingException -> shortMessage + (path?.let { " (path $it)" } ?: "")
    else -> message.orEmpty()
}.replace(PACKAGE_PREFIX, "")

@OptIn(ExperimentalSerializationApi::class)
fun decodeMember(json: Json, input: String): DecodeOutcome = try {
    DecodeOutcome(ok = true, text = json.decodeFromString(JsonMember.serializer(), input).toString())
} catch (e: SerializationException) {
    DecodeOutcome(ok = false, text = "${e::class.java.simpleName}: ${e.shortText()}", hint = (e as? JsonException)?.hint)
}

/** Gson ① 결과 요약 — 파싱은 성공하고 어긋난 필드 수만 남는다(키가 문자열이 아닌 입력은 없다) */
fun gsonOutcome(input: String): DecodeOutcome = try {
    val member = parsePartial(input)
    val rows = member.rows(input)
    val broken = rows.count { it.problem == FieldProblem.BROKEN }
    val silent = rows.count { it.problem == FieldProblem.SILENT }
    DecodeOutcome(
        ok = broken + silent == 0,
        text = if (broken + silent == 0) member.toString() else "파싱 성공 · 계약 위반 $broken · 조용히 틀림 $silent — $member"
    )
} catch (e: RuntimeException) {
    DecodeOutcome(ok = false, text = "${e::class.java.simpleName}: ${e.message}")
}

// ──────────────────────────────────────────────────────────────────────────
// 7번 카드 — 실패의 모양: 어떤 예외가 나고, 무엇으로 잡히나
// ──────────────────────────────────────────────────────────────────────────

/** init 검증 — 1.12 는 생성자 안에서 난 예외를 JsonDecodingException 으로 감싼다(1.11 은 IllegalArgumentException 그대로) */
@Serializable
data class JsonAgedMember(val name: String, val age: Int) {
    init {
        require(age >= 0) { "age < 0" }
    }
}

/** 재귀 타입 — 중첩 한 겹마다 serializer 가 한 번 더 재귀한다 */
@Serializable
data class JsonNode(val child: JsonNode? = null)

/** depth 겹의 JsonNode JSON — {"child":{"child":…null…}} */
fun nestedNodeJson(depth: Int): String = "{\"child\":".repeat(depth) + "null" + "}".repeat(depth)

/** maxNestingDepth 기본값(400)을 풀어 1.11 처럼 한도가 없는 상태를 재현한다 */
@OptIn(ExperimentalSerializationApi::class)
val unlimitedJson: Json = Json { maxNestingDepth = Int.MAX_VALUE }

const val DEEP_DEPTH = 20_000

/**
 * 표의 한 줄. caughtBy 는 이 실패를 잡는 catch 절 — SerializationException · JsonException · Exception 순.
 * before111 은 2026-10-20 JVM 단독 실측(json 1.11.0, 같은 바이트코드)의 결과다.
 */
data class FailureRow(
    val label: String,
    val thrown: String,
    val cause: String?,
    val bySerialization: Boolean,
    val byJson: Boolean,
    val byException: Boolean,
    val before111: String
)

private class FailureCase(val label: String, val before111: String, val block: () -> Any)

@OptIn(ExperimentalSerializationApi::class)
fun failureRows(depth: Int = DEEP_DEPTH): List<FailureRow> = listOf(
    FailureCase("필수 필드 누락('키 누락' 응답)", "MissingFieldException — 같음") {
        strictJson.decodeFromString(JsonMember.serializer(), MemberPayload.MISSING.json)
    },
    FailureCase("타입 불일치('낯선 값' 응답)", "JsonDecodingException — 같음") {
        strictJson.decodeFromString(JsonMember.serializer(), MemberPayload.NEW_VALUES.json)
    },
    FailureCase("init { require(age >= 0) } 실패", "IllegalArgumentException 그대로 — catch(SerializationException) 를 빠져나간다") {
        strictJson.decodeFromString(JsonAgedMember.serializer(), """{"name":"kim","age":-1}""")
    },
    FailureCase("${"%,d".format(Locale.ROOT, depth)} 겹 중첩(기본 한도)", "StackOverflowError — Error 라 catch(Exception) 도 못 잡는다") {
        strictJson.decodeFromString(JsonNode.serializer(), nestedNodeJson(depth))
    },
    FailureCase("같은 입력, maxNestingDepth 해제", "(1.11 의 기본 동작과 같다)") {
        unlimitedJson.decodeFromString(JsonNode.serializer(), nestedNodeJson(depth))
    }
).map { case ->
    // StackOverflowError 까지 결과로 보이려고 Throwable 을 잡는다 — 앱 코드에서는 Error 를 잡지 않는다
    val thrown: Throwable? = try {
        case.block()
        null
    } catch (t: Throwable) {
        t
    }
    FailureRow(
        label = case.label,
        thrown = when (thrown) {
            null -> "성공"
            is SerializationException -> "${thrown::class.java.simpleName}: ${thrown.shortText()}"
            else -> thrown::class.java.simpleName
        },
        // MissingFieldException 은 경로를 붙여 같은 타입으로 다시 던진다 — 다른 타입일 때만 원인으로 보인다
        cause = thrown?.cause?.takeIf { it::class != thrown::class }?.let { "${it::class.java.simpleName}: ${it.message}" },
        bySerialization = thrown is SerializationException,
        byJson = thrown is JsonException,
        byException = thrown is Exception,
        before111 = case.before111
    )
}

/** 이 스레드에서 끝까지 읽힌 최대 깊이(maxOk)와, 실패한 가장 얕은 깊이(failedAt)에서 멈춘 이유 */
data class DepthLimit(val maxOk: Int, val failedAt: Int?, val stoppedBy: String)

/**
 * 기본 한도(400)는 '스택이 넘치기 전에 멈춘다'는 가정이다. 실제 경계는 스레드 스택 크기와 프레임 크기에 달려서,
 * JVM 단위 테스트와 기기 release(Galaxy A72, Dispatchers.Default 1MB 스택 — JIT·AOT 같음)는 400 겹까지 읽고 401 겹에서 too nested 로 멈추지만,
 * 기기 debug(디버깅 가능 빌드 — HotSwan 재작성을 꺼도 같음)는 230~240 겹에서 StackOverflowError 가 먼저 난다(2026-10-26 실측).
 * 디버깅 가능 빌드의 프레임 크기는 실행 상태에 따라 바뀌어 경계가 실행마다 조금씩 다르다 — 탐색 중 실제로 실패한 깊이의 예외를 그대로 보고한다.
 * StackOverflowError 도 '실패'로 세려고 Throwable 을 잡는다 — 예제의 측정용이고, 앱 코드에서는 Error 를 잡지 않는다.
 */
fun depthLimit(json: Json = strictJson, upTo: Int = 1_000): DepthLimit {
    var low = 0
    var high = upTo
    var failure: Pair<Int, Throwable>? = null
    while (low < high) {
        val mid = (low + high + 1) / 2
        val thrown = try {
            json.decodeFromString(JsonNode.serializer(), nestedNodeJson(mid))
            null
        } catch (t: Throwable) {
            t
        }
        if (thrown == null) {
            low = mid
        } else {
            high = mid - 1
            if (failure == null || mid < failure.first) failure = mid to thrown
        }
    }
    val reason = when (val stopped = failure?.second) {
        null -> "멈추지 않음"
        is SerializationException -> "${stopped::class.java.simpleName}: ${stopped.shortText()}"
        else -> stopped::class.java.simpleName
    }
    return DepthLimit(maxOk = low, failedAt = failure?.first, stoppedBy = reason)
}

// ──────────────────────────────────────────────────────────────────────────
// 8번 카드 — value class 검증과 예외 메시지에 남는 입력
// ──────────────────────────────────────────────────────────────────────────

fun isPanFormat(raw: String): Boolean = raw.length == 16 && raw.all { it in '0'..'9' }

/** 검증 팩토리만 있는 value class — 플러그인 serializer 는 private 생성자를 직접 불러 팩토리를 건너뛴다(Gson 과 같은 함정) */
@Serializable
@JvmInline
value class FactoryPan private constructor(val digits: String) {
    companion object {
        fun of(raw: String): FactoryPan? = if (isPanFormat(raw)) FactoryPan(raw) else null
    }
}

/** init 검증 value class — 역직렬화도 생성자를 거치므로 검사가 실행된다 */
@Serializable
@JvmInline
value class CheckedPan(val digits: String) {
    init {
        require(isPanFormat(digits)) { "PAN 형식 아님" }
    }
}

@Serializable
data class FactoryPanCard(val pan: FactoryPan)

@Serializable
data class CheckedPanCard(val pan: CheckedPan, @SerialName("amount_minor") val amountMinor: Long)

/** 가려 쓰는 serializer — value class 에 붙이면 data class 필드에도 걸린다(#144 Gson TypeAdapter 는 필드에서 안 걸림) */
@Serializable(with = MaskedPanSerializer::class)
@JvmInline
value class MaskedPan(val digits: String) {
    override fun toString(): String = "PAN[****${digits.takeLast(4)}]"
}

object MaskedPanSerializer : KSerializer<MaskedPan> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("data.api.MaskedPan", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: MaskedPan) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): MaskedPan = throw SerializationException("가려 쓴 PAN 은 다시 읽지 않는다")
}

@Serializable
data class MaskedPanRequest(val pan: MaskedPan, @SerialName("amount_minor") val amountMinor: Long)

const val TEST_CARD_PAN = "4111111111111111"

/** 8번 카드의 한 줄. problem 은 1번 카드와 같은 색 규칙(SILENT = 검증 우회 · NONE = 의도대로) */
data class PanRow(val code: String, val result: String, val problem: FieldProblem)

private fun attempt(block: () -> Any?): Pair<String, Throwable?> = try {
    block().toString() to null
} catch (e: SerializationException) {
    "${e::class.java.simpleName}: ${e.shortText()}" to e
}

fun valueClassRows(): List<PanRow> {
    val bad = "41111111111111ab"
    val (factoryDecoded, _) = attempt { strictJson.decodeFromString(FactoryPanCard.serializer(), """{"pan":"$bad"}""") }
    val (checkedDecoded, checkedError) = attempt {
        strictJson.decodeFromString(CheckedPanCard.serializer(), """{"pan":"$bad","amount_minor":1200}""")
    }
    val (maskedRead, _) = attempt { strictJson.decodeFromString(MaskedPanRequest.serializer(), """{"pan":"PAN[****1111]","amount_minor":1200}""") }
    return listOf(
        PanRow("FactoryPan.of(\"$bad\")", FactoryPan.of(bad).toString(), FieldProblem.NONE),
        PanRow("Json → FactoryPanCard {\"pan\":\"$bad\"}", factoryDecoded, FieldProblem.SILENT),
        PanRow(
            "Json → CheckedPanCard(init 검증)", checkedDecoded + (checkedError?.cause?.let { " · cause ${it::class.java.simpleName}" } ?: ""),
            FieldProblem.NONE
        ),
        PanRow(
            "Json.encodeToString(MaskedPanRequest)",
            strictJson.encodeToString(MaskedPanRequest.serializer(), MaskedPanRequest(MaskedPan(TEST_CARD_PAN), 1_200)),
            FieldProblem.NONE
        ),
        PanRow("Json → MaskedPanRequest(가린 값 읽기)", maskedRead, FieldProblem.NONE)
    )
}

/** 다른 필드(amount_minor 가 null)가 실패해도 메시지에는 입력 전체 — 유효한 PAN 까지 — 가 들어간다 */
const val LEAK_INPUT = """{"pan":"$TEST_CARD_PAN","amount_minor":null}"""

/** 예외 메시지에서 입력 JSON 줄을 뺀다(exceptionsWithDebugInfo 기본값은 true) */
@OptIn(ExperimentalSerializationApi::class)
val quietJson: Json = Json { exceptionsWithDebugInfo = false }

data class LeakRow(val code: String, val text: String) {
    val leaks: Boolean get() = TEST_CARD_PAN in text
}

@OptIn(ExperimentalSerializationApi::class)
fun leakRows(): List<LeakRow> {
    fun fail(json: Json): SerializationException = try {
        json.decodeFromString(CheckedPanCard.serializer(), LEAK_INPUT)
        error("실패해야 하는 입력이 통과했다")
    } catch (e: SerializationException) {
        e
    }
    val loud = fail(strictJson)
    val quiet = fail(quietJson)
    val loudDecoding = loud as? JsonDecodingException
    return listOf(
        LeakRow("Log.w(TAG, \"파싱 실패\", e) → e.message", loud.message.orEmpty().replace(PACKAGE_PREFIX, "")),
        LeakRow("e.input (JsonDecodingException)", loudDecoding?.input.toString()),
        LeakRow("e.shortMessage + e.path", "${loudDecoding?.shortMessage} · ${loudDecoding?.path}"),
        LeakRow("Json { exceptionsWithDebugInfo = false } → e.message", quiet.message.orEmpty().replace(PACKAGE_PREFIX, ""))
    )
}
