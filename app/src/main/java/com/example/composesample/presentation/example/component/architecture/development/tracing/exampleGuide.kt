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

/**
 * ProfilingManager 예제 참고 자료 (#172, 2026-11-12)
 *
 * - 개요: https://developer.android.com/topic/performance/tracing/profiling-manager/overview
 * - 요청 방법: https://developer.android.com/topic/performance/tracing/profiling-manager/how-to-capture
 * - 트리거(API 36): https://developer.android.com/topic/performance/tracing/profiling-manager/trigger-based-capture
 * - 한도와 수집 동작: https://developer.android.com/topic/performance/tracing/profiling-manager/will-my-profile-always-be-collected
 * - 로컬 디버그 명령: https://developer.android.com/topic/performance/tracing/profiling-manager/debug-mode
 * - API: https://developer.android.com/reference/android/os/ProfilingManager
 * - androidx.core 빌더: https://developer.android.com/reference/androidx/core/os/package-summary
 * - Perfetto TracePacket 필드: https://perfetto.dev/docs/reference/trace-packet-proto
 * - 출처(Android Weekly #752): Sentry, "Android profiling now uses Perfetto via ProfilingManager"
 *   https://blog.sentry.io/android-profilingmanager-perfetto/
 *
 * 구성(ProfilingLab.kt · ProfilingManagerViewModel.kt · ProfilingManagerExampleUI.kt):
 * - 부하는 큰 JSON 을 Gson 으로 파싱하는 것. ProfilingManager 카드는 원문 사례처럼 일부러 메인 스레드에서(사용자 20,000명 × 8번),
 *   레거시 카드(Debug.startMethodTracingSampling / Debug.startMethodTracing)는 줄인 부하(2,000명 × 4번)를 백그라운드 스레드에서 —
 *   추적은 프로세스 전체에 걸리고, 전수 추적은 오래된 기기에서 메인 스레드를 몇 분씩 막을 수 있어서다(아래 API 30 실측).
 * - 요청은 androidx.core 1.15+ 의 StackSamplingRequestBuilder + requestProfiling(context, request, executor, listener)
 *   (이 앱은 core 1.19.1 — 새 의존성 없음). Kotlin 에서는 androidx.core.os.Profiling 이 @JvmName 파사드라
 *   최상위 함수 requestProfiling · registerForAllProfilingResults 로 부른다.
 * - API 35 타입(ProfilingResult · 빌더)은 @RequiresApi(35) 객체(ProfilingApi35) 안에서만 다루고, 밖으로는 앱 데이터 클래스
 *   (ProfilingOutcome)만 내보낸다 — 버전 분기 밖에서 API 35 타입 상태를 읽으면 그 미만 기기 release 에서 클래스 해석 오류가 날 수 있다.
 * - 결과 파일(.perfetto-stack-sample)은 앱 안에서 다시 읽는다(ProfilingLab.parseStackSampleFile — CoroutineTraceLab 의 ProtoReader 재사용):
 *   TracePacket.trusted_packet_sequence_id=10 · interned_data=12 · sequence_flags=13 · perf_sample=66,
 *   InternedData.function_names=5 · frames=6 · callstacks=7, PerfSample.pid=2 · tid=3 · callstack_iid=4. 콜스택은 바깥 → 안쪽.
 *
 * 실측 (2026-11-12, Pixel Tablet API 35 에뮬 debug · release(디버그 키 서명, AOT speed) / Pixel 4 API 30 에뮬 debug · release):
 * - 결과는 실제로 온다: 시스템의 perfetto_cmd 가 4초 세션을 열고 traced_perf(별도 프로세스)가 linux.perf 로 기록 →
 *   /data/misc/perfetto-traces/profiling/ 에 쓴 뒤 앱 files/profiling/profile_<태그>_<시각>.perfetto-stack-sample 로 넘겨준다.
 *   요청 → 콜백 6.0초(4초 + 후처리 약 2초), 2초 요청은 4.0초.
 * - 앱 프로세스 스레드(/proc/self/task/<tid>/comm): API 35 는 startMethodTracingSampling 에 'Sampling Profil'·'Trace writer po' 2개,
 *   startMethodTracing 에 'Trace writer po' 1개가 늘고, ProfilingManager 기록 중에는 0개(debug · release 매번 같음).
 *   API 30 은 샘플링에 'Sampling Profil' 1개 · 전수 0개(Trace writer 풀이 없다). 시작 직후 바로 읽으면 이름이 붙기 전이라
 *   프로세스 이름(e.composesample)으로 보였다 → 100ms 기다린 뒤 읽는다. 콜백 실행기는 메인 실행기로 — 새 실행기는 콜백 때
 *   스레드를 만들어 비교가 흐려진다.
 * - 파일: 레거시 샘플링 13~47KB · 전수 2.6~4.4MB(API 35, 앱이 씀), API 30 전수는 16MB 버퍼를 채운다(화면에 '버퍼 가득' 표시) /
 *   ProfilingManager 4초 34~75KB.
 * - Java 프레임 이름이 debug · release(비 debuggable, AOT speed) 모두 들어 있다: 메인 스레드 샘플 중 Gson 프레임 포함
 *   94 중 90 · 81 중 80(debug), 58 중 56 · 56 중 55 · 33 중 31 · 60 중 57(release). 맨 안쪽 Java 프레임 1위는 java.lang.StringFactory.newStringFromChars(JsonReader 문자열),
 *   그다음 JsonReader.nextQuotedValue · nextNonWhitespace 등. 다른 스레드 샘플은 HeapTaskDaemon(GC) · RenderThread.
 *   기기 안 파서와 꺼낸 파일을 읽은 독립 파이썬 파서가 패킷 328 · 샘플 314 · 메인 94 · Gson 90 · 순위까지 같았다.
 * - 부하 시간은 흔들린다 — 화면은 참고로만 두고 결정적인 값(스레드 · 파일 · 프레임 수)을 보인다. 레거시 카드(2,000명 × 4번):
 *   API 35 release 추적 없이 67~110ms · 샘플링 87~209 · 전수 1,058~1,280(12~18배), API 35 debug 는 전수도 44~118(차이 거의 없음),
 *   API 30 debug 추적 없이 40~334 · 전수 1,815~2,169, API 30 release(JIT) 전수 1,343~3,985.
 *   처음 설계(20,000명 × 8번 · 메인 스레드)로는 API 30 debug 전수 추적이 143초(추적 없이 2.2초)에 64MB 버퍼를 채웠다 → 지금 설계로 바꿨다.
 *   ProfilingManager 카드(메인 스레드, 20,000명 × 8번) 부하는 API 35 release 373~1,026ms.
 * - 거절: 기록 중 같은 앱의 두 번째 요청 → 3 ERROR_FAILED_PROFILING_IN_PROGRESS(15~28ms, 파일 없음).
 *   한도를 켠 상태에서 이 세션의 반복 요청 뒤 → 2 ERROR_FAILED_RATE_LIMIT_PROCESS(3~28ms). errorMessage 는 둘 다 null.
 * - 전역 수신기(registerForAllProfilingResults)는 requestProfiling 콜백과 별개로 같은 결과(성공 · 실패 모두)를 다시 받는다.
 * - ⚠️ 로컬 디버그: 공식 원복 명령 `device_config delete profiling_testing rate_limiter.disabled` 만으로는 한도가 다시 켜지지 않았다
 *   (put true → delete 뒤에도 계속 성공, put false → delete 하자 바로 2). 원복은 false 를 한 번 넣고 지운다.
 * - ProfilingResult.getTriggerType() 은 API 36 — API 35 에서 부르면 NoSuchMethodError(임시 프로브 실측).
 * - API 30: ② · ③ 버튼은 'API 35 이상에서만'으로 꺼지고 전역 수신기는 등록하지 않는다. release 로 진입 · 레거시 실행 · 나가기 · 재진입 크래시 0
 *   (이 API 30 에뮬 이미지는 dex2oat32 가 없어 AOT 컴파일이 안 된다 — release 는 JIT 상태로 확인).
 * - API 36 ProfilingTrigger 는 TRIGGER_TYPE_APP_FULLY_DRAWN · ANR 두 가지, android-37 SDK 에는 COLD_START · OOM · KILL_* 등이 더 있다(javap).
 *   이 예제의 기기에는 API 36 이 없어 화면에 코드 형태만 둔다.
 *
 * ## Java 힙 덤프 — 떠난 화면이 남긴 객체 세기 (#174, 2026-11-13 · ⑤ 카드, HeapDumpLab.kt)
 * - 요청 방법(JavaHeapDumpRequestBuilder · 결과 .perfetto-java-heap-dump):
 *   https://developer.android.com/topic/performance/tracing/profiling-manager/how-to-capture
 * - hprof 와의 차이(복제 프로세스에서 덤프 · 값 없는 참조 그래프):
 *   https://developer.android.com/topic/performance/tracing/profiling-manager/retrieve-and-analyze
 * - 공식 OOM 사례(정적 레지스트리에 남은 리스너): https://developer.android.com/topic/performance/tracing/profiling-manager/debugging-oom-example
 * - 한도: https://developer.android.com/topic/performance/tracing/profiling-manager/will-my-profile-always-be-collected
 * - 필드 정의(Perfetto): https://android.googlesource.com/platform/external/perfetto/+/refs/heads/main/protos/perfetto/trace/profiling/heap_graph.proto
 *
 * 구성: 작은 화면이 들어올 때 앱 전체 레지스트리(ScreenListenerRegistry, 정적 목록)에 LeakedScreenListener 를 등록하고,
 * '떠날 때 해제 안 함'이면 onDispose 에서 빼지 않는다. 화면을 5번 열고 닫은 뒤 힙 덤프를 받아 클래스 이름으로 인스턴스를 센다.
 * 앱 안 Debug.dumpHprofData 는 대조용 — cacheDir 에 쓰고 크기만 재고 바로 지운다(남은 공간 2GB 미만이면 실행하지 않음).
 *
 * 파일 읽기(parseJavaHeapDump — 기기 파일을 독립 파이썬 파서로 읽어 같은 수인지 대조, 3개 파일 모두 일치):
 * - TracePacket.heap_graph=56 → HeapGraph.pid=1 · objects=2 · field_names=4 · continued=5 · index=6 · roots=7 · location_names=8 · types=9.
 *   HeapGraphType.id=1 · class_name=3 · kind=7 · classloader_id=8 · HeapGraphObject.id=1 | id_delta=7 · type_id=2 ·
 *   reference_object_id=5(packed). 덤프 하나가 힙 그래프 패킷 43~292개로 나뉘어 온다.
 * - ⚠️ id_delta 의 기준은 패킷을 넘어 이어진다(첫 객체도 거의 id_delta) · 참조 id 에는 6번 필드(이름은 reference_field_id_base)를
 *   더해야 한다(proto 주석 "should be named reference_object_id_base"). 이 둘을 빼먹은 첫 파서는 참조가 대부분 허공을 가리켰다.
 * - ⚠️ 같은 클래스 이름이 타입 id 둘로 나오기도 한다(덤프에 따라 0~3,290개 이름) — 한쪽은 인스턴스 0. 그래서 이름으로 모아 센다.
 * - 필드마다 바이트를 복사하는 ProtoReader 대신 위치만 옮긴다(17~117MB · 객체 30만~557만 개).
 *   HotSwan 재작성 아래에서 이 파서는 debug 단위 테스트 10.3~38.6초(재작성 없이 47~72ms)라 파서 클래스 3개를 HotSwan 에서 뺐다
 *   (app/build.gradle · ComposeHotReloadGuide.md). 뺀 뒤 기기 debug 325~445ms · release AOT 58~80ms.
 *
 * 실측(Pixel Tablet API 35 에뮬 debug · release AOT — 같은 결과 / 앱 안 hprof 는 Pixel 4 API 30 에뮬 debug · release 도):
 * - 기록 방식: logcat 에 perfetto_hprof 'forking for <앱 pid>' 뒤 다른 pid 가 'dumping heap … finished'(0.3~2.4초) —
 *   공식 문서의 '복제(copy-on-write) 프로세스에서 덤프'와 같다. 요청 → 콜백 3.0~3.2초.
 * - 누수 5 → 덤프 5 · 해제 0 → 0 · **해제했어도 GC 요청 없이 찍으면 5** — 덤프에는 아직 수거되지 않은 객체도 들어간다
 *   (계측 프로브: 버린 표식 50개가 GC 없이 50 · Runtime.gc() 뒤 0). 그래서 카드는 덤프 전에 GC 를 요청한다(보장은 아님).
 * - ⚠️ 루트에서 참조만 따라가는 도달성으로는 정적 필드가 붙잡은 누수가 '닿지 않음'으로 나왔다 — 앱 클래스의 Class 객체 170개 중
 *   루트 0(부트 · zygote 클래스 27,435개는 STICKY_CLASS 루트), 그 Class 객체를 가리키는 참조도 0(클래스 로더의 클래스 표는 네이티브).
 *   그래서 이 예제는 도달성을 계산하지 않고 GC 뒤 인스턴스 수로 센다.
 * - 덤프 중 메인 스레드의 가장 긴 멈춤 29~48ms 는 요청 전 평소 값(16~60ms)과 구분되지 않았다. 앱 안 hprof 는 백그라운드
 *   스레드에서 불러도 덤프 내내 메인 스레드가 멈췄다 — API 30 release 650ms · 50MB, API 30 debug 1.8초 · 305~330MB(2회),
 *   API 35 debug(계측 프로브) 2.0~2.9초 · 238~557MB.
 * - 파일 크기: release 20~27MB(객체 36만~75만) · debug 105~117MB(객체 490만~557만 — 상위가 HotSwan 의 org.objectweb.asm.tree.* 객체,
 *   VarInsnNode 718,500 등). 원시 값 · 문자열 내용 · 배열 내용이 빠지는 것은 공식 문서 설명이고 이 예제에서 따로 재지 않았다.
 * - 한도: 힙 덤프 한 번 뒤 10분쯤 지난 요청이 3ms 만에 2(RATE_LIMIT_PROCESS)로 거절됐다. 측정은 ③ 카드의 device_config 로 한도를 끄고
 *   했고, 끝난 뒤 false → delete 로 되돌려 같은 요청이 37ms 만에 2 로 거절되는 것으로 원복을 확인했다. 한도 이력은 시스템이 보관하고
 *   (dumpsys profiling 서비스 없음 · device_config 에 비용 값 없음) Play 이미지라 초기화할 수 없어, '힙 덤프가 한도에 더 빨리 걸리는가'는
 *   재현 가능한 측정이 안 돼 다루지 않는다.
 */
