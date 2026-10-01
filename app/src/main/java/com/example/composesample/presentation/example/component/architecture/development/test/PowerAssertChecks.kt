// Power-Assert 런타임 API(@PowerAssert·CallExplanation)는 2.4.20 에서 실험 API(경고 수준 opt-in)다
@file:OptIn(ExperimentalPowerAssert::class)

package com.example.composesample.presentation.example.component.architecture.development.test

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.powerassert.CallExplanation
import kotlin.powerassert.ExperimentalPowerAssert
import kotlin.powerassert.PowerAssert
import kotlin.powerassert.toDefaultMessage

/**
 * Power-Assert 예제의 단언 함수 두 개 — 본문은 같고 @PowerAssert 유무만 다르다.
 *
 * - [plainCheck]: 평범한 함수. 실패하면 넘겨받은 message 만 알 수 있다.
 * - [powerCheck]: @PowerAssert 함수(Kotlin 2.4.20, 실험 API). 플러그인이 켜진 컴파일에서 이 함수를 부르면
 *   호출부가 변환되어, 함수 안에서 [PowerAssert.explanation] 으로 호출 식의 소스와 중간값을 구조화된 형태로 받는다.
 *   Gradle 의 `functions` 목록에 적지 않아도 된다.
 *
 * ⚠️ 이 파일(PowerAssertChecksKt)은 debug 핫 리로드(HotSwan)에서 제외했다(app/build.gradle) — HotSwan 이 실행하는 본문에서는
 * PowerAssert.explanation 이 치환되지 않아 NotImplementedError 가 난다(ComposeHotReloadGuide.md).
 */
class CheckFailure(message: String, val explanation: CallExplanation?) : IllegalStateException(message)

@OptIn(ExperimentalContracts::class)
fun plainCheck(condition: Boolean, message: String? = null) {
    contract { returns() implies condition }
    if (!condition) throw CheckFailure(message ?: "조건이 false 입니다", explanation = null)
}

@OptIn(ExperimentalContracts::class)
@PowerAssert
fun powerCheck(condition: Boolean, @PowerAssert.Ignore message: String? = null) {
    contract { returns() implies condition }
    if (!condition) {
        // 변환되지 않은 호출(리플렉션·Java·플러그인 없는 모듈)이면 null 이다
        val explanation = PowerAssert.explanation
        val text = buildString {
            if (!message.isNullOrBlank()) appendLine(message)
            append(explanation?.toDefaultMessage() ?: "조건이 false 입니다(설명 없음)")
        }
        throw CheckFailure(text, explanation)
    }
}
