package com.example.composesample.presentation.example.component.architecture.development.concurrency

import kotlinx.coroutines.CopyableThrowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.reflect.Method
import kotlin.coroutines.ExperimentalStdlibCoroutineSupportApi
import kotlin.coroutines.debug.StackTraceRecoverable

/**
 * stdlib StackTraceRecoverable 실측 재료 — 화면(CoroutineStackTraceExampleUI 의 3·6번 카드)과
 * JVM 단위 테스트(StackTraceRecoverableLabTest)가 같은 예외·함수를 쓴다.
 *
 * kotlinx.coroutines 1.11.0 이 예외를 복사하는 순서(바이트코드 확인 — internal.ExceptionsConstructorKt):
 * 1. CopyableThrowable 이면 createCopy()
 * 2. 아니면 Class.getMethod("copyForStackTraceRecovery") — 인터페이스 타입이 아니라 '공개 무인자 메서드 이름'으로 찾는다.
 *    찾으면 그 결과가 곧 답이다(null 이면 복사하지 않는다 — 생성자 경로로 넘어가지 않는다)
 * 3. 그것도 없을 때만 필드 수 검사 → 반사 생성자
 */

// ==================== 실측용 예외 5종 ====================

/** 필드는 있는데 복사 방법이 없는 예외 — 3번 경로의 필드 수 검사에서 복사를 포기한다. */
internal class FieldOnlyFailure(message: String, val requestId: String) : RuntimeException(message)

/**
 * stdlib 인터페이스로 복사 방법을 주는 예외. 선언에 kotlinx.coroutines 가 전혀 없다
 * → 코루틴 의존성이 없는 계층(순수 Kotlin 도메인 모듈 등)에도 둘 수 있다.
 */
@OptIn(ExperimentalStdlibCoroutineSupportApi::class)
internal class RecoverableFailure(
    message: String,
    val requestId: String
) : RuntimeException(message), StackTraceRecoverable<RecoverableFailure> {

    // CopyableThrowable 판과 같은 규칙 — 원본을 cause 로 달아 "실제로 터진 자리"를 보존한다.
    override fun copyForStackTraceRecovery(): RecoverableFailure =
        RecoverableFailure(message ?: "", requestId).also { it.initCause(this) }
}

/** 두 인터페이스를 모두 구현한 예외 — 어느 쪽 복사가 쓰였는지 copiedBy 에 남긴다. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibCoroutineSupportApi::class)
internal class BothWaysFailure(
    message: String,
    val copiedBy: String = "원본"
) : RuntimeException(message), CopyableThrowable<BothWaysFailure>, StackTraceRecoverable<BothWaysFailure> {

    override fun createCopy(): BothWaysFailure =
        BothWaysFailure(message ?: "", "createCopy").also { it.initCause(this) }

    override fun copyForStackTraceRecovery(): BothWaysFailure =
        BothWaysFailure(message ?: "", "copyForStackTraceRecovery").also { it.initCause(this) }
}

/**
 * null 로 복사를 끈 예외. 필드가 없고 (String) 생성자가 있어 원래라면 반사 복사 대상이지만,
 * 메서드가 발견된 순간 그 결과(null)가 최종 답이 된다.
 */
@OptIn(ExperimentalStdlibCoroutineSupportApi::class)
internal class OptOutFailure(message: String) :
    RuntimeException(message), StackTraceRecoverable<OptOutFailure> {

    override fun copyForStackTraceRecovery(): OptOutFailure? = null
}

/**
 * 인터페이스 없이 같은 이름의 공개 메서드만 둔 예외 — 라이브러리가 이름으로 찾는다는 증거일 뿐,
 * 따라 할 패턴이 아니다(라이브러리 구현 세부에 기대는 코드). 메서드가 private 이면 getMethod 가 못 찾는다.
 */
internal class NameOnlyFailure(message: String, val requestId: String) : RuntimeException(message) {

    fun copyForStackTraceRecovery(): NameOnlyFailure =
        NameOnlyFailure(message ?: "", requestId).also { it.initCause(this) }
}

internal enum class RecoverableCase(val label: String, val note: String) {
    FIELD_ONLY("필드만 있는 예외", "복사 방법 없음 → 필드 수 검사에서 포기"),
    RECOVERABLE("StackTraceRecoverable", "stdlib 인터페이스로 복사 방법 제공"),
    BOTH_WAYS("둘 다 구현", "CopyableThrowable · StackTraceRecoverable 동시 구현"),
    OPT_OUT("null 반환", "필드 없음 + (String) 생성자 — 원래라면 반사 복사 대상"),
    NAME_ONLY("이름만 같은 메서드", "인터페이스 없이 public fun copyForStackTraceRecovery()")
}

internal fun RecoverableCase.newFailure(): Throwable = when (this) {
    RecoverableCase.FIELD_ONLY -> FieldOnlyFailure("프로필 파싱 실패", "req-42")
    RecoverableCase.RECOVERABLE -> RecoverableFailure("프로필 파싱 실패", "req-42")
    RecoverableCase.BOTH_WAYS -> BothWaysFailure("프로필 파싱 실패")
    RecoverableCase.OPT_OUT -> OptOutFailure("프로필 파싱 실패")
    RecoverableCase.NAME_ONLY -> NameOnlyFailure("프로필 파싱 실패", "req-42")
}

// ==================== 라이브러리 복사 함수 직접 호출 ====================

/**
 * kotlinx.coroutines 가 복구 때 부르는 바로 그 복사 함수(tryCopyException)를 직접 부른다.
 * - internal 이라 이름으로 찾는다(못 찾으면 null — 코루틴 버전이 바뀌어 위치나 이름이 달라진 것이다).
 * - 이 함수 자체는 복구 스위치(DEBUG)를 보지 않으므로, 복구가 꺼진 안드로이드에서도
 *   "복구가 켜졌다면 catch 로 무엇이 들어오는지"를 확인할 수 있다.
 * - Method.invoke 는 caller-sensitive 라 debug 의 HotSwan 인터프리터가 실행하지 못한다(IllegalAccessException) →
 *   리플렉션만 이 object 로 모아 이것만 HotSwan 에서 뺀다(app/build.gradle, CopyVisibilityProbe 와 같은 처리).
 */
internal object LibraryCopyProbe {

    private val tryCopy: Method? = runCatching {
        Class.forName("kotlinx.coroutines.internal.ExceptionsConstructorKt")
            .getMethod("tryCopyException", Throwable::class.java)
    }.getOrNull()

    val isAvailable: Boolean get() = tryCopy != null

    fun copyOf(original: Throwable): Throwable? = tryCopy?.invoke(null, original) as Throwable?
}

internal data class LibraryCopyResult(
    val case: RecoverableCase,
    val copied: Boolean,
    val causeIsOriginal: Boolean,
    val detail: String
)

internal fun measureLibraryCopy(case: RecoverableCase): LibraryCopyResult {
    val original = case.newFailure()
    val copy = LibraryCopyProbe.copyOf(original)
    return LibraryCopyResult(
        case = case,
        copied = copy != null && copy !== original,
        causeIsOriginal = copy?.cause === original,
        detail = copy?.keptDetail() ?: "-"
    )
}

/** 사본에 무엇이 남았는지 — 도메인 필드(requestId) 또는 어느 복사 메서드가 쓰였는지. */
internal fun Throwable.keptDetail(): String = when (this) {
    is RecoverableFailure -> "requestId=$requestId"
    is NameOnlyFailure -> "requestId=$requestId"
    is FieldOnlyFailure -> "requestId=$requestId"
    is BothWaysFailure -> "copiedBy=$copiedBy"
    else -> "-"
}
