package com.example.composesample.presentation.example.component.architecture.development.tracing

/**
 * Perfetto 커스텀 트레이스 예제 참고 자료
 *
 * ## androidx.tracing (Trace)
 * - 공식 문서: https://developer.android.com/topic/performance/tracing/custom-events
 * - Perfetto: https://developer.android.com/topic/performance/tracing
 *
 * 핵심 개념:
 * - Trace.beginSection(label) / Trace.endSection() — 동기 구간을 표시. 반드시 "같은 스레드"에서
 *   페어링돼야 한다(스레드별 스택으로 관리됨). 코루틴이 멀티스레드 디스패처(Default/IO)에서
 *   suspend 후 재개될 때 다른 워커 스레드로 옮겨갈 수 있어, suspend 경계를 넘어 begin/end 를
 *   나눠 호출하면 페어링이 깨질 수 있다.
 * - Trace.beginAsyncSection(label, cookie) / Trace.endAsyncSection(label, cookie) — 스레드가 아니라
 *   (label, cookie) 조합으로 상관관계를 맺는다. 스레드가 바뀌어도 안전해 코루틴처럼 스레드를
 *   넘나드는 비동기 작업의 구간 표시에 적합하다.
 * - Trace.setCounter(name, value) — 시간에 따라 변하는 값(활성 코루틴 수 등)을 카운터 트랙으로 기록.
 * - Trace.isEnabled() — 현재 시스템 트레이싱이 켜져 있는지 확인. 트레이스 라벨 계산 비용이 크다면
 *   이 값으로 가드해 오버헤드를 줄일 수 있다.
 *
 * 편의 API:
 * - androidx.tracing:tracing-ktx 의 trace(label) { ... } 확장 함수는 try/finally 로 beginSection/
 *   endSection 을 자동 페어링해준다. 이 프로젝트는 스레드 페어링 함정을 직접 보여주는 것이 목적이라
 *   별도 의존성으로 추가하지 않았다 — 필요하면 tracing-ktx 를 추가해 동기 구간에 한정해 쓸 수 있다.
 *
 * 실측 결과 (2026-08-17, 이 프로젝트 debugRuntimeClasspath/debugCompileClasspath 대조):
 * - androidx.tracing:tracing:1.2.0 은 이미 다른 라이브러리(profileinstaller 등)를 통해
 *   런타임 클래스패스에는 전이 해석돼 있었지만, 컴파일 클래스패스에는 없어 Trace 클래스를 코드에서
 *   직접 참조하려면 build.gradle 에 명시적 의존성 선언이 필요했다.
 *
 * 결과를 실제로 보려면:
 * - Android Studio Profiler > System Trace 로 캡처하거나 `adb shell perfetto` 로 캡처한 뒤
 *   ui.perfetto.dev 에서 열면, beginSection/endSection 은 스레드 트랙 안의 중첩 슬라이스로,
 *   beginAsyncSection/endAsyncSection 은 스레드와 무관한 비동기 트랙으로, setCounter 는 카운터
 *   트랙으로 각각 표시된다. 이 예제는 캡처 없이도 "무엇이 기록되는지"를 화면에서 먼저
 *   확인할 수 있도록, 각 Trace 호출 시점의 스레드명/카운터 값을 그대로 로그로 보여준다.
 */
