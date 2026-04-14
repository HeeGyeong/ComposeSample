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
 *
 * ## Name-Based Destructuring (Kotlin 2.3.20)
 * - Kotlin KEEP: https://youtrack.jetbrains.com/issue/KT-19627
 * - What's New: https://kotlinlang.org/docs/whatsnew2320.html
 * 핵심 개념:
 * - 위치 기반 구조 분해(componentN)는 프로퍼티 순서 변경 시 조용한 버그 유발
 * - 이름 기반: val (local = propertyName) 형태로 프로퍼티 이름을 명시적으로 매칭
 * - 컴파일러 플래그: -Xname-based-destructuring={only-syntax|name-mismatch|complete}
 * - only-syntax: 명시적 형태만 허용 (기존 코드 영향 없음, 점진적 마이그레이션에 적합)
 * - name-mismatch: 변수명과 프로퍼티명 불일치 시 경고 발생
 * - complete: 이름 기반이 기본, 위치 기반은 [] 구문으로 전환
 * - 현재 Experimental 상태. 향후 이름 기반이 기본 동작이 될 예정
 */
