package com.example.composesample.presentation.example.component.architecture.development.performance

/**
 * 성능 최적화 예제 참고 자료
 *
 * ## Inline Value Class (인라인 값 클래스)
 * - 공식 문서: https://kotlinlang.org/docs/inline-classes.html
 * 핵심 개념:
 * - @JvmInline value class: 래퍼 클래스의 타입 안전성 + 원시 타입의 런타임 성능
 * - 컴파일 시 래퍼 제거 → 힙 할당 없음. 박싱 비용 0
 * - 제한: 단일 val 프로퍼티만 허용, 상속 불가, init 블록 가능
 *
 * 활용 패턴:
 * - 도메인 구분: value class UserId(val id: String) vs value class OrderId(val id: String)
 *   → 동일한 String이지만 타입 시스템에서 혼용 방지
 * - 단위 표현: value class Meters(val value: Double), value class Kilograms(val value: Double)
 *
 * 주의사항:
 * - nullable 또는 제네릭 타입으로 사용 시 박싱 발생 (성능 이점 사라짐)
 * - 인터페이스 구현 시 박싱 발생
 *
 * ---
 *
 * ## Stability Annotations (@Stable / @Immutable)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/performance/stability
 * - Compose Compiler Metrics로 안정성 보고서 확인 가능
 *
 * 핵심 개념:
 * - @Immutable: 생성 후 모든 public 프로퍼티가 절대 변경되지 않음을 보장 (가장 강함)
 * - @Stable: equals()가 안정적이고 State 변화 시 Compose에 알림 보장 (var 허용)
 * - List<T>는 MutableList 구현 가능 → 컴파일러가 불안정으로 판단
 * - kotlinx.collections.immutable의 ImmutableList 사용 시 어노테이션 없이도 안정 판단
 *
 * Strong Skipping Mode (Compose 1.7+):
 * - 불안정한 람다를 포함해도 스킵 가능하도록 컴파일러 동작 개선
 * - gradle.properties: composeCompiler.enableStrongSkippingMode=true
 */
