package com.example.composesample.presentation.example.component.architecture.pattern.error

/**
 * Sealed 도메인 에러 처리 Example 참고 자료
 *
 * - 개념 원문: Signatures, be true: domain errors and functional handling in Kotlin
 *   https://blog.jetbrains.com/kotlin/2026/08/signatures-be-true-domain-errors-and-functional-handling-in-kotlin/
 * - sealed 클래스/인터페이스 공식 문서: https://kotlinlang.org/docs/sealed-classes.html
 * - Kotlin의 예외 처리 철학(checked exception 없음): https://kotlinlang.org/docs/exceptions.html
 *
 * 핵심 개념:
 * - Kotlin에는 Java의 checked exception이 없어, throw 하는 함수는 시그니처만 봐서는 실패 가능성을 알 수 없음
 * - sealed interface를 반환 타입으로 쓰면 성공/실패를 포함한 모든 결과가 타입 시스템에 드러남
 * - when이 sealed 타입을 대상으로 하면 else 없이도 컴파일되지만, 그러려면 모든 하위 타입을 나열해야 함(exhaustive)
 * - 새 에러 케이스를 sealed interface에 추가하면 그 타입을 분기하는 모든 when이 컴파일 에러로 변함
 *
 * 프로젝트 내 관련 예제와의 구분:
 * - SealedClassInterfaceExample(언어 기능): sealed class 자체의 문법을 소개하는 예제, UI 상태 표시(ApiResult<T>)가 주 용도
 * - 이 예제: sealed interface를 "도메인 함수의 반환 타입"으로 써서 예외 대신 에러를 값으로 다루는 아키텍처 패턴에 초점
 *
 * 주의사항:
 * - Arrow 라이브러리의 Either 없이도 Kotlin stdlib(sealed interface + data class)만으로 동일한 효과를 낼 수 있음
 * - 이 패턴은 "예상 가능한 실패"(유효성 검증, 재고 부족 등)에 적합하고, 프로그래밍 오류(NPE 등)까지 전부 sealed로
 *   감싸는 것은 과도할 수 있음 — 두 종류를 구분해서 적용할 것
 */
