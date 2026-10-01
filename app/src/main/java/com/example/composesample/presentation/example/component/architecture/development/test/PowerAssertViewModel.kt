@file:OptIn(ExperimentalPowerAssert::class)

package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.powerassert.CallExplanation
import kotlin.powerassert.EqualityExpression
import kotlin.powerassert.ExperimentalPowerAssert
import kotlin.powerassert.LiteralExpression
import kotlin.powerassert.ValueExpression

private const val MAX_CAUSE_DEPTH = 8

/**
 * Power-Assert 예제 로직. 같은 조건식을 [plainCheck]·[powerCheck] 에 각각 넣어 실패 메시지를 비교하고,
 * [CallExplanation] 의 구조와 변환 규칙(한 번만 평가·지연 생성·설명이 null 이 되는 경로)을 결정적인 값으로 잰다.
 *
 * 조건식은 두 함수에 **소스 그대로 두 번** 적는다 — Power-Assert 는 호출부의 소스 텍스트를 도식에 쓰기 때문이다.
 *
 * ⚠️ 이 클래스는 debug 핫 리로드(HotSwan)에서 제외했다(app/build.gradle) — HotSwan 이 실행하는 변환된 호출부에서는
 * 부수효과 있는 식이 여러 번 평가됐다(incrementAndGet 3회, 제외하면 1회 — ComposeHotReloadGuide.md).
 */
class PowerAssertViewModel : ViewModel() {

    enum class Scenario(val label: String, val code: String) {
        SUM("합계 비교", "cart.items.sumOf { it.price } == cart.total"),
        COUNT("컬렉션 조건", "users.count { it.age >= 20 } == 3"),
        NULL_CHAIN("null 안전 호출", "member.address?.city == \"서울\""),
        SHORT_CIRCUIT("단락 평가(&&)", "name.isNotBlank() && name.first().isUpperCase()"),
        WITH_MESSAGE("메시지 함께", "order.quantity in 1..99  (message = \"수량 범위 초과\")")
    }

    data class Comparison(val plain: String, val power: String)

    /** 규칙 실측 한 줄 — 무엇을 했고 무엇을 셌는지 */
    data class RuleResult(val title: String, val lines: List<String>)

    private val _comparisons = MutableStateFlow<Map<Scenario, Comparison>>(emptyMap())
    val comparisons = _comparisons.asStateFlow()

    private val _explanationLines = MutableStateFlow<List<String>>(emptyList())
    val explanationLines = _explanationLines.asStateFlow()

    private val _rules = MutableStateFlow<List<RuleResult>>(emptyList())
    val rules = _rules.asStateFlow()

    // ==================== ① plain vs power ====================

    fun runComparisons() {
        _comparisons.value = Scenario.entries.associateWith { run(it) }
    }

    private fun run(scenario: Scenario): Comparison = when (scenario) {
        Scenario.SUM -> {
            val cart = Cart(listOf(Item("커피", 4500), Item("케이크", 6800), Item("쿠키", 2300)), total = 13000)
            Comparison(
                plain = failureOf { plainCheck(cart.items.sumOf { it.price } == cart.total) },
                power = failureOf { powerCheck(cart.items.sumOf { it.price } == cart.total) }
            )
        }
        Scenario.COUNT -> {
            val users = listOf(User("민수", 17), User("지현", 25), User("현우", 31), User("서연", 19))
            Comparison(
                plain = failureOf { plainCheck(users.count { it.age >= 20 } == 3) },
                power = failureOf { powerCheck(users.count { it.age >= 20 } == 3) }
            )
        }
        Scenario.NULL_CHAIN -> {
            val member = Member("민수", address = null)
            Comparison(
                plain = failureOf { plainCheck(member.address?.city == "서울") },
                power = failureOf { powerCheck(member.address?.city == "서울") }
            )
        }
        Scenario.SHORT_CIRCUIT -> {
            val name = "minsu"
            Comparison(
                plain = failureOf { plainCheck(name.isNotBlank() && name.first().isUpperCase()) },
                power = failureOf { powerCheck(name.isNotBlank() && name.first().isUpperCase()) }
            )
        }
        Scenario.WITH_MESSAGE -> {
            val order = Order(quantity = 120)
            Comparison(
                plain = failureOf { plainCheck(order.quantity in 1..99, "수량 범위 초과") },
                power = failureOf { powerCheck(order.quantity in 1..99, "수량 범위 초과") }
            )
        }
    }

    // ==================== ② CallExplanation 구조 ====================

    /** 합계 비교 시나리오의 설명 객체를 풀어서 보여준다 — 문자열이 아니라 값이 온다는 것이 핵심 */
    fun inspectExplanation() {
        val cart = Cart(listOf(Item("커피", 4500), Item("케이크", 6800), Item("쿠키", 2300)), total = 13000)
        val explanation = failureOrNull { powerCheck(cart.items.sumOf { it.price } == cart.total) }?.explanation
        if (explanation == null) {
            _explanationLines.value = listOf("설명 없음(변환되지 않은 호출)")
            return
        }
        val lines = mutableListOf<String>()
        // source 는 호출이 있는 줄의 들여쓰기부터 담고, 인자 offset 도 그 기준이다
        val indent = explanation.source.length - explanation.source.trimStart().length
        lines += "source = ${explanation.source.trimStart()}"
        lines += "  (파일 offset ${explanation.offset}, 앞 들여쓰기 ${indent}칸 포함 기준)"
        explanation.arguments.forEachIndexed { index, argument ->
            if (argument == null) {
                lines += "arguments[$index] = null (@PowerAssert.Ignore 또는 기본값)"
                return@forEachIndexed
            }
            val text = explanation.source.substring(argument.startOffset, argument.endOffset)
            lines += "arguments[$index] kind=${argument.kind} [${argument.startOffset}, ${argument.endOffset}) \"$text\""
            argument.expressions.forEach { expression ->
                lines += "  ${expression.javaClass.simpleName} @${expression.displayOffset} = ${expression.describe()}"
            }
        }
        // 구조화된 값으로 직접 메시지를 만들 수 있다 — 등식의 좌·우변을 바로 꺼낸다
        val equality = explanation.expressions.filterIsInstance<EqualityExpression>().firstOrNull()
        if (equality != null) {
            val lhs = equality.lhs as? Int
            val rhs = equality.rhs as? Int
            lines += ""
            lines += "직접 만든 메시지: 합계 ${equality.lhs} ≠ 총액 ${equality.rhs}" +
                if (lhs != null && rhs != null) " (차이 ${lhs - rhs})" else ""
        }
        _explanationLines.value = lines
    }

    private fun kotlin.powerassert.Expression.describe(): String = when (this) {
        is EqualityExpression -> "$value (lhs=$lhs, rhs=$rhs)"
        is LiteralExpression -> "$value (리터럴)"
        is ValueExpression -> "$value"
        else -> "$value"
    }

    // ==================== ③ 변환 규칙 실측 ====================

    fun runRules() {
        _rules.value = listOf(evaluatedOnce(), lazyMessage(), reflectionCall(), kotlinAssertOnArt())
    }

    /** 조건식은 한 번만 평가된다 — 메시지를 만들려고 식을 다시 적으면 부수효과가 두 번 난다 */
    private fun evaluatedOnce(): RuleResult {
        val powerCounter = AtomicInteger(0)
        val powerMessage = failureOf { powerCheck(powerCounter.incrementAndGet() == 5) }
        val plainCounter = AtomicInteger(0)
        failureOf { plainCheck(plainCounter.incrementAndGet() == 5, "값=${plainCounter.incrementAndGet()}") }
        return RuleResult(
            title = "부수효과 있는 식은 몇 번 평가되나",
            lines = listOf(
                "powerCheck(counter.incrementAndGet() == 5) → incrementAndGet ${powerCounter.get()}회",
                "plainCheck(식, \"값=\${counter.incrementAndGet()}\") → incrementAndGet ${plainCounter.get()}회",
                "powerCheck 의 도식 — 한 번 계산한 값을 그대로 보여준다:"
            ) + powerMessage.lines().filter { it.isNotBlank() }.map { "  $it" }
        )
    }

    /** 설명은 실패할 때만 만들어진다 — 성공 경로에서는 값의 toString 이 한 번도 불리지 않는다 */
    private fun lazyMessage(): RuleResult {
        val passProbe = ToStringProbe(ready = true)
        failureOf { powerCheck(passProbe.ready) }
        val failProbe = ToStringProbe(ready = false)
        failureOf { powerCheck(failProbe.ready) }
        val eagerProbe = ToStringProbe(ready = true)
        failureOf { plainCheck(eagerProbe.ready, "probe=$eagerProbe") }
        return RuleResult(
            title = "성공 경로의 비용 — 값의 toString 호출 수",
            lines = listOf(
                "powerCheck(probe.ready) 성공 → toString ${passProbe.toStringCount}회",
                "powerCheck(probe.ready) 실패 → toString ${failProbe.toStringCount}회(도식을 그릴 때)",
                "plainCheck(probe.ready, \"probe=\$probe\") 성공 → toString ${eagerProbe.toStringCount}회(메시지를 미리 만듦)"
            )
        )
    }

    /** 변환되지 않은 호출(리플렉션)에서는 설명이 null — 컴파일러가 만든 오버로드도 함께 본다 */
    private fun reflectionCall(): RuleResult {
        // 클래스로더를 명시한다 — debug 의 HotSwan 인터프리터가 실행하는 코드에서 Class.forName(이름) 만 쓰면
        // 호출자 클래스로더를 찾지 못해 ClassNotFoundException 이 난다(실기기 실측)
        val owner = Class.forName(
            "com.example.composesample.presentation.example.component.architecture.development.test.PowerAssertChecksKt",
            false,
            CheckFailure::class.java.classLoader
        )
        val generated = owner.declaredMethods.map { it.name }.filter { it.startsWith("powerCheck") }.sorted()
        val reflected = try {
            owner.getMethod("powerCheck", Boolean::class.javaPrimitiveType, String::class.java).invoke(null, false, null)
            null
        } catch (e: InvocationTargetException) {
            e.targetException.findCheckFailure()
        }
        val direct = failureOrNull { powerCheck(false) }
        return RuleResult(
            title = "설명이 null 이 되는 경로",
            lines = listOf(
                "생성된 메서드: ${generated.joinToString()}",
                "직접 호출 → explanation ${if (direct?.explanation != null) "있음" else "null"}",
                "리플렉션 호출 → explanation ${if (reflected?.explanation != null) "있음" else "null"} · 메시지 \"${reflected?.message}\""
            )
        )
    }

    /**
     * kotlin.assert 는 kotlin._Assertions.ENABLED 가 true 일 때만 검사한다(변환 뒤에도 그 조건은 그대로).
     * 이 값은 빌드 종류에 따라 다르다 — 실측: debug(디버거블) 빌드는 던지고 release 는 검사하지 않는다.
     */
    private fun kotlinAssertOnArt(): RuleResult {
        val items = listOf(1, 2, 3)
        val thrown = try {
            assert(items.sum() == 7)
            null
        } catch (e: AssertionError) {
            e
        }
        return RuleResult(
            title = "앱 코드의 kotlin.assert",
            lines = listOf(
                "desiredAssertionStatus() = ${PowerAssertViewModel::class.java.desiredAssertionStatus()}",
                "assert(items.sum() == 7) → ${thrown?.let { "AssertionError" } ?: "아무 일도 없음(검사 자체를 건너뜀)"}"
            ) + (thrown?.message?.lines()?.filter { it.isNotBlank() }?.map { "  $it" } ?: emptyList())
        )
    }

    // ==================== 공통 ====================

    private inline fun failureOf(block: () -> Unit): String =
        failureOrNull(block)?.message ?: "통과"

    /**
     * 실패를 [CheckFailure] 로 받는다. 디버그 빌드의 HotSwan 이 예외를 감쌀 수 있어 원인 사슬에서 찾는다.
     */
    private inline fun failureOrNull(block: () -> Unit): CheckFailure? =
        try {
            block()
            null
        } catch (t: Throwable) {
            t.findCheckFailure() ?: throw t
        }

    private fun Throwable.findCheckFailure(): CheckFailure? =
        generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH).filterIsInstance<CheckFailure>().firstOrNull()

    data class Item(val name: String, val price: Int)
    data class Cart(val items: List<Item>, val total: Int)
    data class User(val name: String, val age: Int)
    data class Address(val city: String)
    data class Member(val name: String, val address: Address?)
    data class Order(val quantity: Int)

    /** toString 호출 수를 센다 */
    class ToStringProbe(val ready: Boolean) {
        var toStringCount = 0
            private set

        override fun toString(): String {
            toStringCount++
            return "Probe(ready=$ready)"
        }
    }
}
