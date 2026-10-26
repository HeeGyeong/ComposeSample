package com.example.composesample.presentation.example.component.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * GsonMissingFieldExample 의 파싱·판정 로직 — 화면(GsonMissingFieldExampleUI)과 단위 테스트(GsonMissingFieldTest)가 함께 쓴다.
 *
 * Gson 은 인자 없는 생성자가 있을 때만 생성자를 부른다. Kotlin 은 생성자 파라미터 **전부**에 기본값이 있을 때만
 * 인자 없는 생성자를 만든다. 그렇지 않으면 Gson 은 생성자를 거치지 않고(JDK Unsafe) 객체를 만들고 JSON 에 있는 키만 채운다 —
 * Kotlin 의 기본값도 non-null 검사도 실행되지 않는다.
 */

// ──────────────────────────────────────────────────────────────────────────
// 모델 — 같은 JSON 을 모양이 다른 세 클래스로 파싱해 비교한다
// ──────────────────────────────────────────────────────────────────────────

/** 서버의 플랜 값 — 앱이 아는 것은 둘뿐이다(서버가 FAMILY 를 추가한 날을 재현). */
enum class MemberPlan { FREE, PRO }

/**
 * ① 일부만 기본값 — 2026-10-12 까지 DataRules.md 가 권하던 모양.
 * memberId 에 기본값이 없어 인자 없는 생성자가 생기지 않는다 → Gson 이 생성자 없이 만든다.
 */
data class PartialDefaultMember(
    @SerializedName("member_id") val memberId: String,
    @SerializedName("member_name") val memberName: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("plan") val plan: MemberPlan = MemberPlan.FREE,
    @SerializedName("roles") val roles: List<String> = emptyList()
)

/** ② 전부 기본값 — 인자 없는 생성자가 생겨 누락 키에는 기본값이 들어간다. 명시적 null 은 그대로 들어온다. */
data class AllDefaultMember(
    @SerializedName("member_id") val memberId: String = "",
    @SerializedName("member_name") val memberName: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("plan") val plan: MemberPlan = MemberPlan.FREE,
    @SerializedName("roles") val roles: List<String> = emptyList()
)

/**
 * ③ 응답 모델 — 서버가 주는 필드는 전부 nullable(CLAUDE.md · DataRules.md 규칙).
 * plan 은 문자열로 받는다 — enum 으로 받으면 모르는 값이 조용히 null 이 되어 원문이 사라진다.
 */
data class MemberResponse(
    @SerializedName("member_id") val memberId: String?,
    @SerializedName("member_name") val memberName: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("plan") val plan: String?,
    @SerializedName("roles") val roles: List<String?>?
)

/** 앱이 쓰는 모델 — 매퍼가 한 번 만든다. 선언된 null(plan)만 남기 때문에 컴파일러가 처리를 강제한다. */
data class MemberData(
    val memberId: String,
    val memberName: String?,
    val isActive: Boolean,
    val plan: MemberPlan?, // null = 앱이 모르는 플랜
    val roles: List<String>
)

/** 매퍼 — 필수값은 여기서 필드 이름과 함께 실패시키고, 기본값은 여기서 명시적으로 정한다. */
fun MemberResponse.toMemberData(): MemberData = MemberData(
    memberId = requireNotNull(memberId) { "member_id 누락" },
    memberName = memberName,
    isActive = isActive ?: false, // 안 보냈으면 비활성으로 본다 — 매퍼에서 내린 결정
    plan = MemberPlan.entries.firstOrNull { it.name == plan },
    roles = roles.orEmpty().filterNotNull()
)

// ──────────────────────────────────────────────────────────────────────────
// 입력 — 같은 응답의 다섯 가지 모양
// ──────────────────────────────────────────────────────────────────────────

enum class MemberPayload(val label: String, val note: String, val json: String) {
    NORMAL(
        "정상", "모든 키가 있다",
        """{"member_id":"m1","member_name":"kim","is_active":true,"plan":"PRO","roles":["admin"]}"""
    ),
    MISSING(
        "키 누락", "member_name 만 보냄",
        """{"member_name":"kim"}"""
    ),
    EXPLICIT_NULL(
        "명시적 null", "키는 있고 값이 null",
        """{"member_id":null,"member_name":"kim","is_active":null,"plan":null,"roles":null}"""
    ),
    RENAMED(
        "키 이름 변경", "서버가 camelCase 로 바꿈 — 오타와 같은 결과",
        """{"memberId":"m1","member_name":"kim","isActive":true,"plan":"PRO","roles":["admin"]}"""
    ),
    NEW_VALUES(
        "낯선 값", "Y/N 플래그 · 새 플랜 · null 원소",
        """{"member_id":"m1","member_name":"kim","is_active":"Y","plan":"FAMILY","roles":["admin",null]}"""
    )
}

private val gson = Gson()

fun parsePartial(payload: MemberPayload): PartialDefaultMember = parsePartial(payload.json)

/** 임의 JSON — 6번 카드가 kotlinx 쪽 입력을 같은 모델로 Gson 에도 넣는다 */
fun parsePartial(json: String): PartialDefaultMember = gson.fromJson(json, PartialDefaultMember::class.java)

fun parseAllDefault(payload: MemberPayload): AllDefaultMember =
    gson.fromJson(payload.json, AllDefaultMember::class.java)

fun parseResponse(payload: MemberPayload): MemberResponse =
    gson.fromJson(payload.json, MemberResponse::class.java)

// ──────────────────────────────────────────────────────────────────────────
// 1번 카드 — 필드별로 선언과 실제 값을 나란히
// ──────────────────────────────────────────────────────────────────────────

enum class FieldProblem { NONE, SILENT, BROKEN }

/** 표의 한 줄. problem 이 BROKEN 이면 타입 계약이 깨진 값(나중에 NPE), SILENT 면 예외 없이 틀린 값. */
data class FieldRow(val name: String, val declared: String, val value: String, val problem: FieldProblem, val reason: String?)

/** 기본값이 없음을 나타내는 표식(null 기본값과 구분). */
private object NoDefault

private fun show(value: Any?): String = when (value) {
    null -> "null"
    is String -> "\"$value\""
    else -> value.toString()
}

private fun row(
    raw: JsonObject,
    key: String,
    name: String,
    declared: String,
    value: Any?,
    nonNull: Boolean,
    default: Any? = NoDefault,
    required: Boolean = false,
    nonNullElements: Boolean = false
): FieldRow {
    // sent: null = 키 없음, JsonNull = 명시적 null
    val sent: JsonElement? = raw.get(key)
    val absent = sent == null || sent.isJsonNull
    val declaredText = if (default === NoDefault) declared else "$declared = ${show(default)}"
    val (problem, reason) = when {
        nonNull && value == null -> FieldProblem.BROKEN to when {
            sent == null -> "키가 없는데 non-null 선언에 null"
            sent.isJsonNull -> "명시적 null 이 non-null 선언에 그대로"
            else -> "모르는 값 ${sent} → null"
        }
        nonNullElements && value is List<*> && value.contains(null) ->
            FieldProblem.BROKEN to "non-null 원소 리스트 안에 null"
        sent != null && sent.isJsonPrimitive && sent.asJsonPrimitive.isString && value is Boolean ->
            FieldProblem.SILENT to "문자열 $sent 을 $value 로 읽음"
        default !== NoDefault && absent && value != default ->
            FieldProblem.SILENT to "기본값 ${show(default)} 무시"
        required && sent == null && value == default ->
            FieldProblem.SILENT to "필수값이 기본값으로 — 빈 ID 가 정상 값처럼 흐른다"
        else -> FieldProblem.NONE to null
    }
    return FieldRow(name, declaredText, show(value), problem, reason)
}

fun PartialDefaultMember.rows(payload: MemberPayload): List<FieldRow> = rows(payload.json)

fun PartialDefaultMember.rows(json: String): List<FieldRow> {
    val raw = JsonParser.parseString(json).asJsonObject
    // 읽기만으로는 예외가 나지 않는다 — Kotlin 은 프로퍼티를 읽을 때 null 검사를 하지 않는다
    return listOf(
        row(raw, "member_id", "memberId", "String", memberId, nonNull = true),
        row(raw, "member_name", "memberName", "String?", memberName, nonNull = false, default = null),
        row(raw, "is_active", "isActive", "Boolean", isActive, nonNull = true, default = true),
        row(raw, "plan", "plan", "MemberPlan", plan, nonNull = true, default = MemberPlan.FREE),
        row(raw, "roles", "roles", "List<String>", roles, nonNull = true, default = emptyList<String>(), nonNullElements = true)
    )
}

fun AllDefaultMember.rows(payload: MemberPayload): List<FieldRow> {
    val raw = JsonParser.parseString(payload.json).asJsonObject
    return listOf(
        row(raw, "member_id", "memberId", "String", memberId, nonNull = true, default = "", required = true),
        row(raw, "member_name", "memberName", "String?", memberName, nonNull = false, default = null),
        row(raw, "is_active", "isActive", "Boolean", isActive, nonNull = true, default = true),
        row(raw, "plan", "plan", "MemberPlan", plan, nonNull = true, default = MemberPlan.FREE),
        row(raw, "roles", "roles", "List<String>", roles, nonNull = true, default = emptyList<String>(), nonNullElements = true)
    )
}

fun MemberResponse.rows(payload: MemberPayload): List<FieldRow> {
    val raw = JsonParser.parseString(payload.json).asJsonObject
    // 전부 nullable 선언 — null 은 정직한 값이다. 남는 문제는 Gson 의 관대한 타입 변환("Y" → false)뿐
    return listOf(
        row(raw, "member_id", "memberId", "String?", memberId, nonNull = false),
        row(raw, "member_name", "memberName", "String?", memberName, nonNull = false),
        row(raw, "is_active", "isActive", "Boolean?", isActive, nonNull = false),
        row(raw, "plan", "plan", "String?", plan, nonNull = false),
        row(raw, "roles", "roles", "List<String?>?", roles, nonNull = false)
    )
}

/** 매퍼 결과 — 실패는 응답 경계에서 필드 이름과 함께. 매퍼는 suspend 가 아니라 runCatching 이 취소 예외를 삼킬 일이 없다. */
fun mapResult(payload: MemberPayload): Result<MemberData> = runCatching { parseResponse(payload).toMemberData() }

// ──────────────────────────────────────────────────────────────────────────
// 2번 카드 — 파싱은 성공했다. 어디서 터지나
// ──────────────────────────────────────────────────────────────────────────

/** non-null 파라미터를 받는 함수 — private 이 아니어야 진입부 null 검사(checkNotNullParameter)가 생성된다. */
fun displayMemberId(id: String): String = "#$id"

class UseProbe(val label: String, val code: String, val run: (PartialDefaultMember) -> Any?)

/** 키 누락 응답으로 만든 PartialDefaultMember 에 평범한 코드를 하나씩 실행한다. */
val useProbes: List<UseProbe> = listOf(
    UseProbe("로그·toString", "Log.d(TAG, \"\$member\")") { it.toString() },
    UseProbe("문자열 템플릿", "\"id=\${member.memberId}\"") { "id=${it.memberId}" },
    UseProbe("equals", "member == 다시 파싱한 것") { it == parsePartial(MemberPayload.MISSING) },
    UseProbe("Set·Map 키 (hashCode)", "setOf(member)") { hashSetOf(it).size },
    UseProbe("copy()", "member.copy(memberName = \"lee\")") { it.copy(memberName = "lee") },
    UseProbe("String 사용", "member.memberId.length") { it.memberId.length },
    UseProbe("List 사용", "member.roles.size") { it.roles.size },
    UseProbe("for 순회", "for (role in member.roles)") { member ->
        var total = 0
        for (role in member.roles) total += role.length
        total
    },
    UseProbe("enum when", "when (member.plan) { FREE -> …; PRO -> … }") { member ->
        when (member.plan) {
            MemberPlan.FREE -> "무료"
            MemberPlan.PRO -> "유료"
        }
    },
    UseProbe("non-null 파라미터에 전달", "displayMemberId(member.memberId)") { displayMemberId(it.memberId) },
    // 컴파일러는 '필요 없는 엘비스'라고 경고하지만 바이트코드의 null 검사는 남는다(실측) — 그래서 방어가 동작한다
    @Suppress("USELESS_ELVIS")
    UseProbe("엘비스 방어", "member.memberId ?: \"unknown\"") { it.memberId ?: "unknown" },
    UseProbe("Boolean 분기", "if (member.isActive) …") { if (it.isActive) "활성" else "비활성으로 처리" }
)

data class ProbeResult(val probe: UseProbe, val ok: Boolean, val text: String)

fun runUseProbes(): List<ProbeResult> {
    val member = parsePartial(MemberPayload.MISSING)
    return useProbes.map { probe ->
        try {
            ProbeResult(probe, ok = true, text = show(probe.run(member)))
        } catch (e: RuntimeException) {
            ProbeResult(probe, ok = false, text = "${e::class.java.simpleName}: ${e.message}")
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 3번 카드 — Boolean: 꺼짐과 모름
// ──────────────────────────────────────────────────────────────────────────

data class FlagAsBoolean(@SerializedName("is_premium") val isPremium: Boolean)
data class FlagAsNullable(@SerializedName("is_premium") val isPremium: Boolean?)
data class FlagAsString(@SerializedName("is_premium") val isPremium: String?)

enum class FlagInput(val label: String, val json: String) {
    MISSING("누락", """{}"""),
    NULL("null", """{"is_premium":null}"""),
    YES("\"Y\"", """{"is_premium":"Y"}"""),
    ONE("1", """{"is_premium":1}"""),
    TRUE("true", """{"is_premium":true}""")
}

/** Y/N·1/0·true/false 를 해석하고 그 밖의 값과 누락은 '모름'(null)으로 남긴다. */
fun String?.toPremiumFlag(): Boolean? = when (this?.trim()?.uppercase()) {
    "Y", "TRUE", "1" -> true
    "N", "FALSE", "0" -> false
    else -> null
}

data class FlagRow(val input: FlagInput, val asBoolean: String, val asNullable: String, val asString: String)

private fun flagText(block: () -> String): String = try {
    block()
} catch (e: RuntimeException) {
    "✗ ${e::class.java.simpleName.removeSuffix("Exception")}" // 표 칸이 좁아 접미사는 뺀다
}

fun flagRows(): List<FlagRow> = FlagInput.entries.map { input ->
    FlagRow(
        input = input,
        asBoolean = flagText { show(gson.fromJson(input.json, FlagAsBoolean::class.java).isPremium) },
        asNullable = flagText { show(gson.fromJson(input.json, FlagAsNullable::class.java).isPremium) },
        asString = flagText {
            val raw = gson.fromJson(input.json, FlagAsString::class.java).isPremium
            when (val flag = raw.toPremiumFlag()) {
                null -> "${show(raw)} → 모름"
                else -> "${show(raw)} → $flag"
            }
        }
    )
}

// ──────────────────────────────────────────────────────────────────────────
// 4번 카드 — disableJdkUnsafe(): 생성자 없는 생성을 파싱 시점 예외로
// ──────────────────────────────────────────────────────────────────────────

data class FailFastRow(val model: String, val payload: MemberPayload, val result: String, val problem: FieldProblem)

fun failFastRows(): List<FailFastRow> {
    val strict = GsonBuilder().disableJdkUnsafe().create()
    val cases = listOf(
        "① 일부만 기본값" to PartialDefaultMember::class.java,
        "② 전부 기본값" to AllDefaultMember::class.java
    )
    return cases.flatMap { (model, type) ->
        listOf(MemberPayload.MISSING, MemberPayload.EXPLICIT_NULL).map { payload ->
            try {
                val parsed: Any = strict.fromJson(payload.json, type)
                // 통과한 객체에 non-null 선언 null 이 남았는지
                val broken = parsed is AllDefaultMember && parsed.rows(payload).any { it.problem == FieldProblem.BROKEN }
                FailFastRow(model, payload, "통과 $parsed", if (broken) FieldProblem.BROKEN else FieldProblem.NONE)
            } catch (e: RuntimeException) {
                val message = e.message?.substringBefore(';')?.replace(type.name, type.simpleName)
                FailFastRow(model, payload, "${e::class.java.simpleName}: $message", FieldProblem.NONE)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 5번 카드 — 프로젝트 DI 의 두 컨버터 경로도 끝은 같은 Gson.fromJson
// ──────────────────────────────────────────────────────────────────────────

private const val MOCK_BASE_URL = "https://mockapi.local/"

private interface MemberService {
    @GET("member")
    suspend fun member(): PartialDefaultMember
}

/** Retrofit + GsonConverterFactory.create() — ApiModule 과 같은 컨버터. 응답은 네트워크 대신 인터셉터가 만든다. */
suspend fun parseWithRetrofit(payload: MemberPayload): PartialDefaultMember {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(payload.json.toResponseBody("application/json".toMediaType()))
                .build()
        }
        .build()
    try {
        return Retrofit.Builder()
            .baseUrl(MOCK_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MemberService::class.java)
            .member()
    } finally {
        client.dispatcher.executorService.shutdown()
    }
}

/** Ktor ContentNegotiation { gson() } — KtorModule 과 같은 컨버터. 응답은 MockEngine 이 만든다. */
suspend fun parseWithKtor(payload: MemberPayload): PartialDefaultMember {
    val client = HttpClient(MockEngine {
        respond(payload.json, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
    }) {
        install(ContentNegotiation) { gson() }
    }
    try {
        return client.get("${MOCK_BASE_URL}member").body()
    } finally {
        client.close()
    }
}

data class ConverterRow(val payload: MemberPayload, val direct: String, val retrofit: String, val ktor: String) {
    val same: Boolean get() = direct == retrofit && direct == ktor
}

private suspend fun convertText(block: suspend () -> Any): String = try {
    block().toString()
} catch (e: CancellationException) {
    throw e // 화면을 떠나 취소된 것은 결과가 아니다
} catch (e: RuntimeException) {
    "${e::class.java.simpleName}: ${e.message}"
}

suspend fun converterRows(): List<ConverterRow> = MemberPayload.entries.map { payload ->
    ConverterRow(
        payload = payload,
        direct = convertText { parsePartial(payload) },
        retrofit = convertText { parseWithRetrofit(payload) },
        ktor = convertText { parseWithKtor(payload) }
    )
}
