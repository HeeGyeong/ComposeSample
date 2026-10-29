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
 * - trace(label) { ... } 확장 함수는 try/finally 로 beginSection/endSection 을 자동 페어링해준다.
 *   1.x 에서는 tracing-ktx 에 있었지만 2.0 부터는 tracing-android 본체의 같은 TraceKt 로 옮겨져
 *   별도 의존성 없이 쓸 수 있다(tracing-ktx 2.0.3 은 빈 껍데기). 이 예제는 스레드 페어링 함정을 직접
 *   보여주는 것이 목적이라 쓰지 않았다 — 쓰더라도 동기 구간에 한정해야 한다.
 *
 * 2.0.3 상향 (2026-09-29):
 * - KMP 분리로 tracing → tracing-android 로 해석된다. Trace 정적 메서드 시그니처는 1.2.0 과 동일(바이너리 호환)
 * - 신규 setCounter(String, Long) 오버로드는 SDK 가드 없이 API 29+ 경로만 탄다 — minSdk 24 에서는 Int 버전을 쓸 것
 * - 상향 전후 atrace 실측 동일: 동기 B 8건, 비동기 S/F 8/8, 카운터 트랙 기록
 *
 * 실측 결과 (2026-08-17, 이 프로젝트 debugRuntimeClasspath/debugCompileClasspath 대조):
 * - androidx.tracing:tracing:1.2.0 은 이미 다른 라이브러리(profileinstaller 등)를 통해
 *   런타임 클래스패스에는 전이 해석돼 있었지만, 컴파일 클래스패스에는 없어 Trace 클래스를 코드에서
 *   직접 참조하려면 build.gradle 에 명시적 의존성 선언이 필요했다.
 *
 * ## tracing 2.0 — tracing-wire 로 앱이 직접 쓰는 트레이스 (#124, 2026-10-30)
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/tracing
 * - Perfetto TrackEvent·flow: https://perfetto.dev/docs/instrumentation/track-events
 * - TracePacket 필드: https://perfetto.dev/docs/reference/trace-packet-proto
 * - 파일 열기: https://ui.perfetto.dev/
 *
 * 구성(CoroutineTraceLab.kt · CoroutineTracingViewModel.kt · CoroutineTracingCard.kt):
 * - TraceDriver(context, TraceSink(context, fileProvider = { file }), isCategoryEnabled = { true }) — 시스템 atrace 가 아니라
 *   앱 프로세스가 Perfetto 프로토 파일을 직접 쓴다. 파일 이름은 File.createPerfettoFile(prefix)(같은 초면 -1, -2 …).
 *   close() 가 큐를 비우고 파일을 닫는다. PerfettoTracer·TraceContext 생성자는 @RestrictTo 라 TraceDriver.tracer 로만 얻는다.
 * - tracer.traceCoroutine(category, name) { } — 내부 CoroutinePropagationElement(ThreadContextElement)가 코루틴이 멈출 때
 *   그 스레드에서 구간을 닫고, 재개된 스레드에서 같은 이름·flow id 로 다시 연다 → 트랙마다 BEGIN/END 가 맞고 조각들이 flow 로 이어진다.
 * - 토큰: 구조가 끊긴 작업(다른 스코프·콜백 스레드)은 tokenFromCoroutineContext()(@DelicateTracingApi) 나
 *   tokenForManualPropagation()(@ExperimentalContextPropagation) 을 양쪽 구간에 넘겨야 같은 flow id 를 쓴다. 토큰이 없으면 새 flow id.
 * - 같은 Tracer 의 동기 beginSection(…).close() 를 다른 스레드에서 부르면 ThreadTrack.assertThreadIdWhenNotOptimized 가
 *   IllegalArgumentException("Invariant violation…")을 던진다. tracing-android consumer 규칙(-assumenosideeffects)이 R8 축소
 *   빌드에서 이 검사를 지운다(이 프로젝트 release 는 축소를 꺼서 debug·release 모두 던졌다).
 * - 의존성만 넣어도 생기는 것: tracing-wire 매니페스트의 App Startup ConnectedProfilerTracingInitializer 가 프로세스 시작마다
 *   TraceSink(context) 로 no_backup/perfetto_traces 에 빈 파일을 만들고(지우지 않음 — 쌓인다) Tracer.setGlobalTracer 를 부른다.
 *   이 앱은 AndroidManifest 의 InitializationProvider 에서 그 meta-data 를 tools:node="remove" 로 뺐다(WorkManager 초기화와 같은 자리).
 *   exported 리시버 ConnectedProfilerTracingReceiver(android.permission.DUMP — 셸 전용)는 남는다.
 *
 * 실측 (2026-10-30, Pixel 4 API 30 에뮬, debug 3회·release 2회 같음):
 * - ① traceCoroutine 8개(Default, delay 2번): 구간마다 3조각이 스레드 2~3개 트랙에, flow id 1개 · 트랙마다 BEGIN=END · 스레드가 바뀐 코루틴 5~8/8
 * - ② 동기 beginSection 을 delay 너머로: close() 예외 수 = 스레드가 바뀐 수(5~8/8) · 파일에 BEGIN 8, END 가 모자란 트랙 3~4
 * - ③ 자식 코루틴·토큰 없음: 끊김(자식이 도는 동안 부모 이름 조각이 4개로 다시 열림) · 다른 스코프·토큰 없음: 끊김 ·
 *   tokenFromCoroutineContext(): 연결 · tokenForManualPropagation()(콜백 스레드): 연결
 * - 초기화 제거 전 새로 설치 후 세 번 실행 → 0바이트 파일 3개, 제거 후 → 0개 · Tracer.global 은 스텁
 * - 기기 안 파서(CoroutineTraceLab.parseTraceFile)와 꺼낸 파일을 읽은 독립 파이썬 파서의 패킷·조각·연결 수가 같았다(패킷 38).
 *   카테고리를 모두 켜면 TraceContext.flush 가 메타 구간 'flush' 하나를 남긴다.
 *
 * 결과를 실제로 보려면:
 * - Android Studio Profiler > System Trace 로 캡처하거나 `adb shell perfetto` 로 캡처한 뒤
 *   ui.perfetto.dev 에서 열면, beginSection/endSection 은 스레드 트랙 안의 중첩 슬라이스로,
 *   beginAsyncSection/endAsyncSection 은 스레드와 무관한 비동기 트랙으로, setCounter 는 카운터
 *   트랙으로 각각 표시된다. 이 예제는 캡처 없이도 "무엇이 기록되는지"를 화면에서 먼저
 *   확인할 수 있도록, 각 Trace 호출 시점의 스레드명/카운터 값을 그대로 로그로 보여준다.
 */
