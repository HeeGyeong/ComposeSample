package com.example.composesample.presentation.example.component.architecture.development.di

/**
 * Koin Compiler Plugin 예제 참고 자료 (KoinCompilerPluginExampleUI · KoinCompilerPluginLab.kt)
 *
 * ## 개요
 * Koin Compiler Plugin(`io.insert-koin.compiler.plugin` 1.2.2)은 KSP 가 아니라 **Kotlin 컴파일러 플러그인**이다.
 * startKoin · koinApplication 진입점마다 DI 그래프를 맞춰 보고, 빠진 정의를 빌드 오류로 낸다. 2026-11-05(#106) 이 앱에 실제로 적용했다 —
 * 그전의 이 예제는 KSP 판(koin-annotations + koin-ksp-compiler) 애노테이션을 의존성 없이 글로만 설명하는 화면이었다.
 *
 * - 저장소 · README: https://github.com/InsertKoinIO/koin-compiler-plugin
 * - 1.2.2 릴리스(2026-11-04): https://github.com/InsertKoinIO/koin-compiler-plugin/releases/tag/1.2.2
 * - 1.2.1 릴리스(Kotlin 2.4.20 지원 · 버전 검사 엄격화): https://github.com/InsertKoinIO/koin-compiler-plugin/releases/tag/1.2.1
 * - 컴파일 안전성 규칙: https://github.com/InsertKoinIO/koin-compiler-plugin/blob/1.2.2/docs/COMPILE_TIME_SAFETY.md
 * - 지원 Kotlin 목록: https://github.com/InsertKoinIO/koin-compiler-plugin/blob/1.2.2/supported-kotlin-versions.txt
 * - KSP 판에서 옮기기: https://github.com/InsertKoinIO/koin-compiler-plugin/blob/1.2.2/docs/MIGRATION_FROM_KSP.md
 *
 * ## 적용 (gradle/libs.versions.toml · build.gradle · app/build.gradle)
 * - `koin-compiler = { id = "io.insert-koin.compiler.plugin", version.ref = "koinCompilerPlugin" }` → app 에만 적용.
 *   마커는 Maven Central 에 있다(settings 의 mavenCentral 로 충분). 런타임 의존성 추가 없음 — single<T>() 등은 koin-core 4.2.2 의
 *   `org.koin.plugin.module.dsl` 에 이미 있다(플러그인 없이 쓰면 NotImplementedError — 아래 측정).
 * - 애노테이션 문법(@Singleton · @Factory · @KoinViewModel · @Named)은 KSP 없이 플러그인이 읽지만 `koin-annotations` 의존성이 필요해
 *   이 앱은 DSL 만 쓴다.
 * - 진입점 정리: BaseApplication 의 `startKoin { loadKoinModules(A); loadKoinModules(B) }` → `startKoin { modules(A + B) }`.
 *   koin-core 4.2.2 소스상 둘 다 `koin.loadModules(allowOverride = true, createEagerInstances = false)` 뒤 startKoin 이
 *   createEagerInstances() — 동작은 같다(로거가 INFO 일 때 modules() 만 "Started N definitions" 를 찍는데 이 앱은 로거를 두지 않는다).
 *   문서(COMPILE_TIME_SAFETY.md 'Runtime Module Loading')는 loadKoinModules 를 컴파일 시점에 증명할 수 없는 런타임 로딩으로 본다 —
 *   진입점이 startKoin 하나일 때는 통과했지만, 이 예제의 koinApplication 이 더해지자 앱의 get() · koinViewModel() 63곳이 D002 로 막혔다
 *   (단독 프로젝트에서도 'loadKoinModules + 다른 koinApplication' 조합에서만 재현, modules() 형태는 통과).
 *
 * ## 진단 코드 (이 앱 · 단독 프로젝트에서 낸 것)
 * - KOIN-D001 Missing dependency — 플러그인 DSL 생성자 매개변수의 정의가 진입점 그래프에 없다. "required by · in module" 을 함께 찍는다
 * - KOIN-D002 Missing definition — get() · koinViewModel() 호출 위치의 타입이 그래프에 없다. 손으로 쓴 람다 안의 get() 도 여기서 걸린다
 * - KOIN-D004 Circular dependency — `dsl:A → dsl:B → dsl:A`
 * - KOIN-W001 진입점이 올리지 않는 모듈 · KOIN-W003 모듈 목록을 실행 중에 고른다(검증 불가)
 * - 오류 줄마다 "→ Fix with AI: set up Kotzilla MCP …" 광고 줄이 하나 더 붙는다(e: 로 찍힌다)
 *
 * ## 측정 — 앱 (:app, Kotlin 2.4.21 · AGP 9.4.1 내장 Kotlin · HotSwan 2.1.0)
 * - 기존 정의 58개를 수집(userLogs): 손으로 쓴 람다 57개는 provider-only — 생성자를 추측하지 않고 람다 안의 get() 을 호출 위치에서 검사,
 *   singleOf(::Navigation) 1개는 constructor-shorthand 로 생성자 검사. 예제 모듈의 플러그인 DSL 6개 · 람다 3개를 더해 67개
 * - 변이(:app:compileDebugKotlin): checkoutModule 에서 single<CartClock>() 삭제 → D001(required by PriceCalculator 'clock') + D002
 *   (Lab 의 get<CartClock>()) · apiModule 에서 GsonConverterFactory 정의 삭제 → D002 ApiModule.kt:24:34 · viewModelModule 에서
 *   SSEViewModel 삭제 → D002 3곳(SSEExampleUI 51:38 · ReverseLazyColumnExampleUI 51:38 · 175:38, koinViewModel 호출 위치)
 * - 플러그인을 처음 걸었을 때 기존 클래스는 debug · release · 단위 테스트 모두 바이트 동일, 추가는 org.koin.plugin.hints 힌트 파일
 *   (모듈마다 1개 — 정의마다 dsl_single(T, Unit, Unit, Unit) 같은 빈 표식 함수)와 META-INF/app.kotlin_module 뿐.
 *   예제까지 넣은 최종 상태: 기존 DI 모듈 8 + 예제 모듈 3 = 힌트 11개, 바뀐 기존 클래스는 BaseApplication · 목록(Examples2026Kt) ·
 *   이 예제 화면뿐. release 는 minifyEnabled false 라 힌트 11개가 dex 에 그대로 들어간다(서명한 release APK 에서 확인)
 * - HotSwan SKIPPED 84 → 85(늘어난 1개는 아래 제외한 Lab 파일 facade — EXCLUDED_FILE_FACADE). debug 에서 Koin 예외(InstanceCreationException — 자바 기준 checked Exception)가
 *   UndeclaredThrowableException 으로 한 겹 감싸여 3번 카드 사슬이 release 와 달랐다 → KoinCompilerPluginLabKt 를 hotSwanCompiler.exclude
 *   (단위 테스트가 사슬 전체를 비교해 잡았다 — contains 비교였던 첫 판은 통과시켰다)
 * - 빌드 로그: 컴파일마다 "w: Koin compiler plugin: Kotlin 2.4.21 is newer than the newest verified version (2.4.20) — proceeding
 *   with the 2.4.20 adapter" 한 줄
 *
 * ## 측정 — 화면 (Pixel 4 API 30 에뮬레이터 · debug 3회 · release 1회(디버그 키 서명) — 결과 12줄 모두 같음)
 * - 1번 카드: repository · clock 은 그래프와 같은 인스턴스, factory 두 번은 다른 인스턴스, cache = null, maxRetries = 3
 * - 3번 카드: 아래 두 사슬과 바른 호출(성공 · LabCache · 성공 · orderId=A-1001)
 * - 바뀐 startKoin 진입점으로 다른 예제(MVI — MVIExampleViewModel(get()))가 뜨고 크래시 버퍼 0
 * - HotSwan 이 변환한 debug 코드에서도 플러그인이 바꾼 single<T>() 가 동작했다(제외 전 debug 단위 테스트의 GraphReport 정상 —
 *   달랐던 것은 예외 사슬의 겉껍질뿐)
 *
 * ## 측정 — 빌드는 통과하고 실행 중에 터지는 것
 * - 한정자: `single(named("local")) { LabCache() }` + `single { RemoteClient(get(named("remote"))) }` → 빌드 통과, 실행 시
 *   InstanceCreationException ← NoDefinitionFoundException("type 'LabCache' and qualifier 'remote'"). 문서상 한정자 검사는
 *   @Named 매개변수(애노테이션) 쪽 규칙이다 — 손으로 쓴 람다의 named() 는 대조되지 않았다
 * - parametersOf: `factory { (orderId: String) -> OrderLookup(orderId) }` 를 get<OrderLookup>() 로 → InstanceCreationException ←
 *   NoParameterFoundException. 플러그인 DSL factory<T>() 에 String 매개변수를 두면 반대로 D001(kotlin.String) — 실행 시 매개변수는
 *   애노테이션 @InjectedParam 으로 표시해야 한다
 * - nullable · 기본값 매개변수는 정의가 없어도 통과(cache = null · maxRetries = 3 — "Skipping injection for parameter 'maxRetries'")
 * - **실행 중에 고르는 모듈 목록**: `modules(if (flag) a else b)` 는 W003 경고만 낸다. 컴파일 단위에 이런 진입점이 하나라도 있으면
 *   다른 파일의 진입점까지 D001 · D002 검사가 꺼졌다 — 임시 파일에 하나를 두자 CartClock · Gson 변이가 둘 다 빌드를 통과했고, 경고는
 *   'the entry point loading apiModule' 을 가리켰다(1.2.2, 단독 프로젝트에서도 같음 — 문서에는 없는 동작). 그래서 예제에 넣지 않았다
 * - 문서의 알려진 한계: 판정하는 호출 위치는 그 컴파일의 그래프를 **합쳐서** 본다 — koinApplication 이 둘이면 a.koin.get<T>() 는 다른
 *   쪽만 T 를 줘도 통과한다
 * - 플러그인 없이 single<T>() 를 쓰면 빌드는 되고, 모듈을 올리는 순간 NotImplementedError("Error while using 'single<T>()'. The Koin
 *   Compiler plugin is missing ...") — 본문이 USE_KOIN_COMPILER_PLUGIN 자리 표시다
 *
 * ## 측정 — Kotlin 2.4.21 호환 (공식 검증 목록 2.3.20 · 2.4.0 · 2.4.10 · 2.4.20 밖)
 * - 어댑터 선택: 목록에서 2.4.21 이하 최고 = 2.4.20 → Kotlin240Adapter(2.4.0 · 2.4.10 · 2.4.20 이 같은 어댑터를 쓴다)
 * - 컴파일러 jar(kotlin-compiler · kotlin-compiler-embeddable) 2.4.20 → 2.4.21: 클래스 26,016개 중 8개만 내용이 다름 — 전부
 *   ir/backend/js/lower · backend/wasm/lower · cli/pipeline/web/js(JS · Wasm 백엔드), 추가 · 삭제 0. 플러그인이 참조하는 Kotlin 클래스
 *   278개와 겹치는 것 없음
 * - 저장소의 tools/abi-check(AbiCheck.java) 방식으로 1.2.2 jar 의 org.jetbrains.kotlin 멤버 참조 476개(본체 454 · k240 어댑터 21 ·
 *   어댑터 공통 1)를 대 보면 2.4.20 · 2.4.21 모두 누락 0. 음성 대조로 1.2.0 ↔ 2.4.20 은 릴리스 노트의 '컴파일러 API 4개'와 같은
 *   4건 누락(FirResolvedQualifier.getClassId · IrFactory.createSimpleFunction · IrAnnotationImpl.setArgumentMapping ·
 *   IrUtilsKt.getValueArgument)
 * - 실제로 Gradle 이 싣는 kotlin-compiler-embeddable 은 com.intellij 를 org.jetbrains.kotlin.com.intellij 로 옮겨 두어 두 버전 모두
 *   KtPsiSourceElement.getPsi 1건이 풀리지 않는다 — 플러그인의 psiOrNull 이 LinkageError 를 잡아 null 로 넘기는 의도된 경로(버전 무관)
 * - 단독 JVM 프로젝트(Kotlin 2.4.20 · 2.4.21 + koin-core 4.2.2): 같은 D001 문구 · 같은 실행 결과, 차이는 2.4.21 의 버전 경고뿐
 * - Kotlin 은 .20 패치 자리에 기능 릴리스를 낸다(2.4.20 이 1.2.0 의 API 4개를 깨뜨림) → Kotlin 을 올릴 때는 이 플러그인의 검증 목록부터
 */
