package com.example.composesample.presentation.example.component.architecture.development.time

/**
 * kotlin.time 시간 API 예제 참고 자료
 *
 * ## kotlin.time — Clock / Instant / TimeSource / TestTimeSource
 * - What's New: https://kotlinlang.org/docs/whatsnew24.html
 * - kotlin.time 공식 문서: https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/
 * - Clock/Instant KEEP(원래 kotlinx-datetime 소속이던 것이 stdlib로 편입): https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/instant-clock.md
 * 핵심 개념:
 * - Clock.System.now() → kotlin.time.Instant: "지금 몇 시인가"를 나타내는 벽시계. NTP 동기화·시간대 변경으로 값이 앞뒤로 튈 수 있음
 * - TimeSource.Monotonic.markNow() → ComparableTimeMark, .elapsedNow() → Duration: "얼마나 흘렀는가"만 재는 단조시계. 벽시계 변경에 영향받지 않음
 * - measureTimedValue { ... } → TimedValue<T>(value, duration): 실행 결과와 소요 시간을 함께 반환. Duration은 나노초 단위까지 표현 가능
 * - TestTimeSource: 테스트에서 실제 delay() 없이 시간을 임의로 전진(+=)시켜 타임아웃/재시도 로직을 결정론적으로 검증
 *
 * 프로젝트 코드 계약 확인(2026-08-20, 등록 시점 프로브는 2026-08-10):
 * - javap로 kotlin-stdlib-2.4.0.jar의 kotlin/time/Clock.class, Instant.class, TestTimeSource.class를 직접 확인 —
 *   셋 다 `@SinceKotlin("2.3")` + `WasExperimental(markerClass=[ExperimentalTime])` 메타데이터를 가지고 있어
 *   `@OptIn(kotlin.time.ExperimentalTime::class)`가 필요하다
 * - TimeSource.Monotonic / TimeMark.elapsedNow() / measureTimedValue는 클래스 레벨에 그런 마커가 없어 opt-in 불필요 —
 *   본 예제는 opt-in이 필요한 카드(①, ④)에만 `@OptIn`을 붙여 그 경계를 코드 구조로 드러낸다
 * - 이 프로젝트는 시간 측정에 System.currentTimeMillis()/kotlin.system.measureTimeMillis만 써왔고
 *   kotlin.time 계열 사용은 이 예제가 처음 — ③ 카드에서 같은 연산을 두 방식으로 동시에 실측해 비교한다
 */
