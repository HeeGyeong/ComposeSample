package com.example.composesample.presentation.example.component.architecture.development.performance

/**
 * 성능 최적화 예제 참고 자료
 *
 * ## Inline Value Class (인라인 값 클래스)
 * - 공식 문서: https://kotlinlang.org/docs/inline-classes.html
 * - 출처: https://carrion.dev/en/posts/kotlin-inline-functions-value-classes/
 * 핵심 개념:
 * - @JvmInline value class: 래퍼 클래스의 타입 안전성 + 원시 타입의 런타임 성능
 * - 컴파일 시 래퍼 제거 → 힙 할당 없음. 박싱 비용 0
 * - 제한: 단일 val 프로퍼티만 허용, 상속 불가, init 블록 가능
 * - inline 함수와 조합: 함수 호출 오버헤드 제거 + reified 타입 파라미터로 타입 소거 문제 해결 (Zero-Cost Abstractions)
 * - noinline/crossinline: 람다를 인라인 대상에서 제외하거나 non-local return을 금지할 때 사용
 *
 * 활용 패턴:
 * - 도메인 구분: value class UserId(val id: String) vs value class OrderId(val id: String)
 *   → 동일한 String이지만 타입 시스템에서 혼용 방지
 * - 단위 표현: value class Meters(val value: Double), value class Kilograms(val value: Double)
 *
 * 주의사항:
 * - Any/인터페이스/제네릭(List<T> 등) 타입으로 다룰 시 박싱 발생 (성능 이점 사라짐)
 * - 단, String처럼 참조 타입을 감싼 value class는 nullable(?)만으로는 박싱되지 않음
 *   (언더라잉 타입의 null 표현을 그대로 재사용 — InlineValueClassExampleUI.kt 리플렉션 실측으로 확인)
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
 * Strong Skipping Mode (Kotlin 2.0.20 컴파일러부터 기본 활성화 — 이 프로젝트는 Kotlin 2.4.0):
 * - 불안정한 파라미터를 가진 컴포저블도 skippable 로 컴파일된다 ("불안정 = 항상 리컴포지션"이 아니다)
 * - 단, 비교 전략이 다르다 — 안정 파라미터는 equals(구조 비교), 불안정 파라미터는 ===(인스턴스 동일성)
 *   → 내용이 같아도 .copy()/.toList() 등으로 새 인스턴스를 만들면 리컴포지션된다
 * - 람다는 자동으로 remember 로 감싸진다 (자동 람다 메모이제이션)
 * - 별도 설정이 필요 없다. 끄려면 build.gradle 에
 *   composeCompiler { featureFlags.add(ComposeFeatureFlag.StrongSkipping.disabled()) }
 *   (구 gradle.properties 키 composeCompiler.enableStrongSkippingMode 는 폐기됨)
 *
 * 안정성 판정을 눈으로 확인하는 방법 (Compose Compiler Metrics):
 * - build.gradle 에 composeCompiler { reportsDestination = ...; metricsDestination = ... } 추가 후 빌드
 * - composables.txt: 함수별 restartable/skippable 여부와 파라미터별 stable 표시
 * - classes.txt: 클래스별 stable/runtime 판정과 <runtime stability> 근거
 */
