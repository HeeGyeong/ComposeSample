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
 * 프로젝트 코드 계약 검증(2026-08-20) — opt-in 판정 정정:
 * - 후보 등록(2026-08-10) 시점에는 "@OptIn(ExperimentalTime::class)만 필요"로 적혀 있었고, javap로 kotlin-stdlib-2.4.0.jar의
 *   Clock.class/Instant.class/TestTimeSource.class를 훑어도 셋 다 `@SinceKotlin("2.3")` + `WasExperimental(markerClass=[ExperimentalTime])`
 *   메타데이터를 갖고 있어 opt-in이 필요해 보였다
 * - 하지만 실제로 `@OptIn(ExperimentalTime::class)`를 제거하고 `:app:compileDebugKotlin`을 돌려 확인한 결과 **빌드가 그대로 성공** —
 *   `WasExperimental`은 "예전엔 experimental이었지만 Kotlin 2.3부터 stable로 승격됐다"는 컴파일러용 이력 메타데이터일 뿐,
 *   opt-in 요구 사실을 남기는 마커가 아니었다. Clock/Instant/TestTimeSource/TimeSource.Monotonic/measureTimedValue 전부
 *   이 프로젝트의 Kotlin 2.4.0에서는 opt-in 없이 바로 쓸 수 있는 안정 API다
 * - 교훈: 바이트코드의 `WasExperimental` 어노테이션과 실제 `@RequiresOptIn` 게이팅을 혼동하지 말 것 — opt-in 요구 여부는
 *   해당 애노테이션을 실제로 빼고 컴파일해보는 것이 가장 확실하다(javap 정적 추론만으로는 오판 가능)
 * - 이 프로젝트는 시간 측정에 System.currentTimeMillis()/kotlin.system.measureTimeMillis만 써왔고
 *   kotlin.time 계열 사용은 이 예제가 처음 — ③ 카드에서 같은 연산을 두 방식으로 동시에 실측해 비교한다
 */
