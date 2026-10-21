package com.example.composesample.presentation.example.component.architecture.development.language

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier

/**
 * SensitiveTypesExample 의 타입·실험 로직 — 화면(SensitiveTypesExampleUI)과 단위 테스트(SensitiveTypesTest)가 함께 쓴다.
 *
 * Kotlin 블로그(Verifone 결제 시스템 KotlinConf 발표 정리)의 패턴 중 "민감 값 가리기 · 검증 팩토리 · copy 가시성 ·
 * 타임아웃을 결과로"를 실제 코드로 옮기고, 그 타입 장치를 **Gson·리플렉션이 어떻게 우회하는지**를 함께 잰다.
 * 카드번호는 결제 테스트용 번호(4111…)만 쓴다.
 */

// ──────────────────────────────────────────────────────────────────────────
// 1. 가림 value class — toString 이 늘 가리고, 원문은 이름 있는 호출로만 꺼낸다
// ──────────────────────────────────────────────────────────────────────────

/** 검증을 통과한 카드번호. 생성자는 private 이라 [Pan.parse] 만이 입구다. */
@JvmInline
value class Pan private constructor(private val value: String) {
    /** 문자열 템플릿·data class toString·List 출력·예외 메시지가 전부 이 함수를 거친다 */
    override fun toString(): String = "PAN[${maskDigits(value)}]"

    /** 원문이 필요한 단 한 곳(네트워크 전송) — 일부러 눈에 띄고 검색되는 이름으로 둔다 */
    fun toNetwork(): String = value

    companion object {
        fun parse(raw: String): ParsedPan {
            val input = RawPan(raw)
            return when {
                raw.length !in PAN_LENGTHS -> ParsedPan.Invalid.WrongLength(input)
                !raw.all(Char::isDigit) -> ParsedPan.Invalid.NotNumeric(input)
                !passesLuhn(raw) -> ParsedPan.Invalid.BadChecksum(input)
                else -> ParsedPan.Valid(Pan(raw))
            }
        }
    }
}

/** 아직 검증하지 않은 입력. 거부 결과에 원문을 그대로 실으면 거부 로그에서 카드번호가 샌다 — 원문도 가린다. */
@JvmInline
value class RawPan(private val value: String) {
    override fun toString(): String = "RAW[${maskDigits(value)}]"
}

/** 바깥 입력은 경계에서 곧바로 이 결과로 바꾼다 — 잘못된 입력은 예외가 아니라 값이다 */
sealed interface ParsedPan {
    data class Valid(val pan: Pan) : ParsedPan

    sealed interface Invalid : ParsedPan {
        val raw: RawPan

        data class WrongLength(override val raw: RawPan) : Invalid
        data class NotNumeric(override val raw: RawPan) : Invalid
        data class BadChecksum(override val raw: RawPan) : Invalid
    }
}

/** 끝 4자리만 남기고 가린다(4자리 이하면 전부 가린다) */
fun maskDigits(value: String): String =
    if (value.length <= 4) "*".repeat(value.length) else "*".repeat(4) + value.takeLast(4)

/** 카드번호 체크 디지트(Luhn) — 오른쪽에서 두 번째 자리부터 하나 걸러 두 배 */
fun passesLuhn(digits: String): Boolean {
    var sum = 0
    digits.reversed().forEachIndexed { index, c ->
        val d = c - '0'
        sum += if (index % 2 == 1) (d * 2).let { if (it > 9) it - 9 else it } else d
    }
    return sum % 10 == 0
}

private val PAN_LENGTHS = 13..19

data class CardAuthRequest(val pan: Pan, val amountMinor: Long)

/** 화면·테스트가 쓰는 결제 테스트 카드 */
fun samplePan(): Pan = (Pan.parse(TEST_PAN) as ParsedPan.Valid).pan

const val TEST_PAN = "4111111111111111"

// ──────────────────────────────────────────────────────────────────────────
// 2. 가림이 실제로 걸리는 곳 / 안 걸리는 곳
// ──────────────────────────────────────────────────────────────────────────

/** 한 줄 실험 결과 — [leaks] 는 결과에 원문 카드번호가 그대로 들어 있는지, [intended] 는 원문이 나가는 것이 목적인 출구인지 */
data class ProbeRow(val code: String, val result: String, val intended: Boolean = false) {
    val leaks: Boolean get() = TEST_PAN in result
}

/** 코드에서 흔히 문자열이 되는 다섯 경로 — 전부 Pan.toString 을 거친다 */
fun maskingRows(): List<ProbeRow> {
    val pan = samplePan()
    val request = CardAuthRequest(pan, amountMinor = 1_200)
    val nullable: Pan? = pan
    val rejected = Pan.parse("4111111111111112")
    return listOf(
        ProbeRow("\"카드 \$pan 처리\"", "카드 $pan 처리"),
        ProbeRow("\"\$request\" (data class)", "$request"),
        ProbeRow("\"\$nullable\" (Pan?)", "$nullable"),
        ProbeRow("listOf(pan).toString()", listOf(pan).toString()),
        ProbeRow("IllegalStateException(\"실패 \$request\").message", IllegalStateException("실패 $request").message.orEmpty()),
        ProbeRow("거부 결과 \"\$parsed\" (RawPan)", "$rejected"),
        ProbeRow("pan.toNetwork()", pan.toNetwork(), intended = true)
    )
}

/**
 * Pan 전용 TypeAdapter — 단독 Pan 에는 걸리지만, data class 필드에서는 **걸리지 않는다**:
 * value class 필드는 JVM 에서 String 필드로 지워져 Gson 이 String 어댑터를 고른다.
 */
class PanTypeAdapter : TypeAdapter<Pan>() {
    override fun write(out: JsonWriter, value: Pan?) {
        out.value(value?.toString())
    }

    override fun read(reader: JsonReader): Pan? = when (val parsed = Pan.parse(reader.nextString())) {
        is ParsedPan.Valid -> parsed.pan
        is ParsedPan.Invalid -> null
    }
}

private val plainGson: Gson = Gson()
private val adapterGson: Gson = GsonBuilder().registerTypeAdapter(Pan::class.java, PanTypeAdapter()).create()

/** 함정 ① — Gson 직렬화는 toString 을 거치지 않는다 */
fun serializationRows(): List<ProbeRow> {
    val pan = samplePan()
    val request = CardAuthRequest(pan, amountMinor = 1_200)
    return listOf(
        ProbeRow("gson.toJson(request)", plainGson.toJson(request)),
        ProbeRow("gson.toJson(pan)", plainGson.toJson(pan)),
        ProbeRow("어댑터 등록 · toJson(pan)", adapterGson.toJson(pan)),
        ProbeRow("어댑터 등록 · toJson(request)", adapterGson.toJson(request)),
        ProbeRow("경계 DTO · toJson(request.toDto())", plainGson.toJson(request.toDto()), intended = true)
    )
}

/** 네트워크로 나가는 모양 — 원문은 toNetwork() 한 곳에서만 꺼낸다(로그에는 이 DTO 가 아니라 request 를 남긴다) */
data class CardAuthDto(
    @SerializedName("pan") val pan: String,
    @SerializedName("amount_minor") val amountMinor: Long
)

fun CardAuthRequest.toDto() = CardAuthDto(pan = pan.toNetwork(), amountMinor = amountMinor)

/** 들어오는 모양 — 서버 필드는 nullable(CLAUDE.md 규칙), 도메인 타입은 매퍼가 팩토리로 만든다 */
data class CardAuthResponse(
    @SerializedName("pan") val pan: String?,
    @SerializedName("amount_minor") val amountMinor: Long?
)

fun CardAuthResponse.parsePan(): ParsedPan = Pan.parse(pan.orEmpty())

/** 함정 ② — Gson 역직렬화는 private 생성자도 검증 팩토리도 거치지 않는다 */
data class DecodeRow(val raw: String, val direct: String, val withAdapter: String, val viaFactory: String)

/** 정상 · 숫자 아님 · 길이 · 체크 디지트 — 팩토리가 거부하는 세 가지를 하나씩 */
val DECODE_SAMPLES = listOf(TEST_PAN, "41111111111111ab", "4111 1111", "4111111111111112")

fun decodeRows(): List<DecodeRow> = DECODE_SAMPLES.map { raw ->
    // 도메인 모델(필드 이름 그대로)과 응답 DTO(@SerializedName)에 같은 값을 각자의 키로 준다
    val domainJson = """{"pan":"$raw","amountMinor":1200}"""
    val responseJson = """{"pan":"$raw","amount_minor":1200}"""
    DecodeRow(
        raw = raw,
        direct = describeDecoded { plainGson.fromJson(domainJson, CardAuthRequest::class.java) },
        withAdapter = describeDecoded { adapterGson.fromJson(domainJson, CardAuthRequest::class.java) },
        viaFactory = describeParsed(plainGson.fromJson(responseJson, CardAuthResponse::class.java).parsePan())
    )
}

private fun describeDecoded(decode: () -> CardAuthRequest): String {
    // Gson 이 만든 객체의 pan 을 팩토리로 다시 검사해 "검증 없이 들어왔는가"를 드러낸다
    val decoded = decode()
    val recheck = Pan.parse(decoded.pan.toNetwork())
    return "${decoded.pan} 생성 · 재검사 ${describeParsed(recheck)}"
}

fun describeParsed(parsed: ParsedPan): String = when (parsed) {
    is ParsedPan.Valid -> "통과"
    is ParsedPan.Invalid.WrongLength -> "거부(길이)"
    is ParsedPan.Invalid.NotNumeric -> "거부(숫자 아님)"
    is ParsedPan.Invalid.BadChecksum -> "거부(체크 디지트)"
}

// ──────────────────────────────────────────────────────────────────────────
// 3. data class 의 private 생성자와 copy()
// ──────────────────────────────────────────────────────────────────────────

/**
 * Kotlin 2.4 까지의 기본 동작(copy 는 생성자가 private 이어도 public)을 표시로 고정한 쪽.
 * 표시 없이 두면 선언에 경고가 나고(2.5 부터 오류), 바깥에서 copy() 를 부르는 줄에도 경고가 난다.
 */
@ExposedCopyVisibility
data class OpenCopyAuthorization private constructor(val stan: String, val amountMinor: Long) {
    companion object {
        fun approve(stan: String, amountMinor: Long): OpenCopyAuthorization? =
            if (isValidStan(stan) && amountMinor > 0) OpenCopyAuthorization(stan, amountMinor) else null
    }
}

/** copy() 도 생성자만큼 private — 바깥에서 copy() 를 부르면 컴파일 오류 */
@ConsistentCopyVisibility
data class ClosedCopyAuthorization private constructor(val stan: String, val amountMinor: Long) {
    companion object {
        fun approve(stan: String, amountMinor: Long): ClosedCopyAuthorization? =
            if (isValidStan(stan) && amountMinor > 0) ClosedCopyAuthorization(stan, amountMinor) else null
    }
}

private fun isValidStan(stan: String) = stan.length == 6 && stan.all(Char::isDigit)

/**
 * copy() 를 리플렉션으로 들여다보는 곳 — HotSwan(debug 핫 리로드) 인터프리터는 caller-sensitive 리플렉션
 * (getDeclaredMethods)을 실행하지 못해 IllegalAccessException 이 난다(debug 단위 테스트에서 재현,
 * dispatchRewriteEnabled=false 면 통과). 그래서 이 object 만 app/build.gradle 의 hotSwanCompiler 에서 뺀다.
 */
object CopyVisibilityProbe {
    /** 바이트코드의 copy 메서드 접근 제한자 — 컴파일러가 막는지와 별개로 실제로 열려 있는가 */
    fun modifiers(type: Class<*>): String =
        type.declaredMethods.filter { it.name == "copy" }.joinToString { Modifier.toString(it.modifiers) }.ifEmpty { "copy 없음" }

    /**
     * 바깥 코드가 copy(amountMinor = -500) 을 부른 것과 같은 일을 리플렉션으로 한다.
     * 소스로 직접 부르면 2.4 에서는 경고, 2.5 부터는 오류라서 — 실행 결과만 보여주기 위해 리플렉션을 쓴다.
     */
    fun forgeAmount(target: Any): String = try {
        val copy = target.javaClass.getDeclaredMethod("copy", String::class.java, Long::class.javaPrimitiveType)
        "만들어짐: ${copy.invoke(target, "000001", -500L)}"
    } catch (e: IllegalAccessException) {
        "막힘: ${e.javaClass.simpleName}"
    } catch (e: InvocationTargetException) {
        "실패: ${e.targetException.javaClass.simpleName}"
    } catch (e: NoSuchMethodException) {
        "copy 없음"
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 4. 타임아웃을 예외가 아니라 결과로
// ──────────────────────────────────────────────────────────────────────────

sealed interface AuthorizationOutcome {
    data class Approved(val authCode: String) : AuthorizationOutcome
    data class Declined(val reason: String) : AuthorizationOutcome

    /** 응답을 못 받았다 ≠ 실패했다 — 저쪽에서는 승인됐을 수 있다 */
    data object TimedOut : AuthorizationOutcome
}

/** 승인은 하되 첫 응답 하나를 잃어버리는 가짜 매입사 */
class FakeAcquirer {
    private val approvals = linkedMapOf<String, String>()
    private var dropNextResponse = true

    val approvedCount: Int get() = approvals.size

    fun authorize(stan: String): AuthorizationOutcome {
        val code = "AUTH-${approvals.size + 1}"
        approvals[stan] = code
        if (dropNextResponse) {
            dropNextResponse = false
            return AuthorizationOutcome.TimedOut
        }
        return AuthorizationOutcome.Approved(code)
    }

    /** STAN 으로 처리 결과 조회 — 재시도가 아니라 '확인' */
    fun lookup(stan: String): AuthorizationOutcome =
        approvals[stan]?.let { AuthorizationOutcome.Approved(it) } ?: AuthorizationOutcome.Declined("기록 없음")
}

class GatewayTimeoutException : RuntimeException("응답 시간 초과")

data class TimeoutReport(val steps: List<String>, val approvedAtAcquirer: Int)

/** 흔한 처리 — 타임아웃을 예외로 바꾸고 잡아서 '실패'로 보고 새 요청으로 재시도 */
fun timeoutAsException(): TimeoutReport {
    val acquirer = FakeAcquirer()
    val steps = mutableListOf<String>()
    fun call(stan: String): String = when (val outcome = acquirer.authorize(stan)) {
        is AuthorizationOutcome.Approved -> outcome.authCode
        is AuthorizationOutcome.Declined -> error(outcome.reason)
        AuthorizationOutcome.TimedOut -> throw GatewayTimeoutException()
    }
    val first = runCatching { call("000101") }
    steps += "STAN 000101 → ${first.exceptionOrNull()?.javaClass?.simpleName ?: first.getOrNull()}"
    if (first.isFailure) {
        steps += "실패로 보고 새 요청(STAN 000102) 재시도 → ${call("000102")}"
    }
    return TimeoutReport(steps, acquirer.approvedCount)
}

/** 타임아웃을 결과로 — 재시도가 아니라 같은 STAN 으로 조회 */
fun timeoutAsOutcome(): TimeoutReport {
    val acquirer = FakeAcquirer()
    val steps = mutableListOf<String>()
    when (val outcome = acquirer.authorize("000201")) {
        is AuthorizationOutcome.Approved -> steps += "STAN 000201 → ${outcome.authCode}"
        is AuthorizationOutcome.Declined -> steps += "STAN 000201 → 거절 ${outcome.reason}"
        AuthorizationOutcome.TimedOut -> {
            steps += "STAN 000201 → TimedOut (결과)"
            steps += "재시도 대신 조회(000201) → ${acquirer.lookup("000201")}"
        }
    }
    return TimeoutReport(steps, acquirer.approvedCount)
}
