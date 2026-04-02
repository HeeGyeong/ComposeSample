package com.example.composesample.presentation.example.component.architecture.development.type

/**
 * Kotlin 타입 활용 예제 참고 자료
 *
 * - 공식 문서: https://kotlinlang.org/docs/basic-types.html
 *
 * 핵심 개념:
 * - 타입 추론: val x = 42 → Int 자동 추론. 명시적 타입 선언은 가독성을 위할 때만
 * - 스마트 캐스트: is 검사 이후 컴파일러가 자동으로 캐스팅 (as 불필요)
 * - Nothing 타입: 정상 반환이 없는 함수의 반환 타입 (throw, 무한 루프)
 * - Unit vs Nothing: Unit은 값 없음을 의미, Nothing은 실행 완료 불가를 의미
 *
 * 코틀린스러운 타입 사용:
 * - String? 대신 nullable 최소화 → 명시적 기본값 제공
 * - Any 대신 sealed class/interface로 타입 안전성 확보
 * - typealias로 복잡한 함수 타입에 의미 있는 이름 부여
 *   예) typealias OnUserClick = (userId: String) -> Unit
 */
