package com.example.composesample.presentation.example.component.architecture.development.language

/**
 * Kotlin 언어 기능 예제 참고 자료
 *
 * ## Sealed Class vs Sealed Interface
 * - 공식 문서: https://kotlinlang.org/docs/sealed-classes.html
 * 핵심 개념:
 * - sealed class: 단일 파일·패키지 내 하위 클래스 제한. 생성자 인자 가능
 * - sealed interface: 다중 상속 가능. data class/object가 여러 sealed interface 구현 가능
 * - when 표현식과 함께 사용 시 else 분기 불필요 → 컴파일러가 완전성 보장
 *
 * 활용 패턴:
 * - UI 상태 모델링: sealed class UiState { Loading, Success(data), Error(msg) }
 * - 이벤트 처리: sealed interface UiEvent { data class Click(id: Int), object Refresh }
 * - 다중 상태 조합: sealed interface 다중 구현으로 복합 상태 표현
 *
 * Kotlin 1.9+ 변경:
 * - sealed class 하위 클래스가 같은 패키지 전체로 확장 (파일 제한 완화)
 */
