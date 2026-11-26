package com.example.composesample.presentation.example.component.architecture.development.test

/**
 * Test Examples 참고 자료
 *
 * --- 실패 순간 자동 저장 — TestFailurePolicy (ui-test 1.13, 2026-11-26 보강 #191 — TestFailurePolicyCard.kt · UITestFailurePolicyTest) ---
 * - 후보 출처: Compose November '26 블로그(2026-11-18) https://developer.android.com/blog/posts/jetpack-compose-november-2026-release
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-ui
 * - 근거(ui-test-android 1.13.0 sources): ComposeUiTestConfig(failurePolicy) · TestFailurePolicy(screenshotCaptureMode,
 *   uiHierarchyCaptureMode, failureHandlers) · CaptureMode(Unspecified/Enabled/Disabled) · FailureArtifact(type, fileName) ·
 *   FailureContext(error, artifacts) · fun interface TestFailureHandler — opt-in 없음. androidx.compose.ui.test.v2.runComposeUiTest(config) 도
 *   실험 표시 없음(옛 패키지 androidx.compose.ui.test 의 runComposeUiTest(effectContext, …) 는 @ExperimentalTestApi + deprecated).
 *   createComposeRule(config) · runComposeUiTest(config) 모두 실패 시
 *   FailurePipelineRunner.runPipeline 을 탄다: Unspecified 면 러너 인자 androidx.compose.ui.test.failure.isScreenshotCaptureEnabled /
 *   isUiHierarchyCaptureEnabled(없으면 false) → 스크린샷 `${nanoTime}_screenshot.png`(UiAutomation.takeScreenshot, PNG) ·
 *   트리 `${nanoTime}_ui.txt`(View 계층 HumanReadables + 시맨틱 printToString) 를 PlatformTestStorageRegistry 에 쓰고,
 *   핸들러를 차례로 부른 뒤 원래 실패를 다시 던진다. 저장 · 핸들러에서 난 예외는 로그 + 원래 실패에 addSuppressed
 * - 저장소(androidx.test monitor 1.8.0): 기본은 FileTestStorage(러너 useTestStorageService 를 켜야 TestStorage). 폴더는
 *   TestDirCalculator — 러너 인자 additionalTestOutputDir, 없으면 API 29+ externalMediaDirs[0]/additionalTestOutputDir,
 *   그 아래는 externalCacheDir/additionalTestOutputDir. androidTest 클래스패스의 androidx.test.services:storage 1.6.0 은
 *   espresso · runner 를 따라 들어오지만 러너 인자 없이 등록되지 않는다
 * - 측정(Pixel Tablet API 35 · Pixel 4 API 30 에뮬, am instrument): 실패 실행은 정상 테스트 안에서 runCatching 으로 잡았다(스위트에 실패 테스트 없음)
 *   · 기본값 · 인자 없음 → 산출물 0, 핸들러 1회, 받은 error 는 던져진 그 AssertionError
 *   · 기본값 + 러너 인자 두 개 true → 2개 · Enabled/Enabled → 2개(같은 nanoTime 앞머리) · Disabled/Disabled + 러너 인자 → 0
 *   · 저장 위치 /storage/emulated/0/Android/media/com.example.composesample/additionalTestOutputDir(두 기기),
 *     -e additionalTestOutputDir 로 준 폴더도 그대로 따름(API 35)
 *   · 스크린샷 = 기기 화면 전체(2560×1600 · 1080×2280, 69~150KB) — 막 부팅한 에뮬의 'System UI isn't responding' 창까지 찍혔다
 *   · ui.txt = 머리글 '--- View and Compose Hierarchy ---' 아래 DecorView 부터 View 줄, AndroidComposeView 밑에 시맨틱 노드 —
 *     실패한 노드가 "Tag: 'resultText'" · "Text = '[결과: 30]'" 로 남는다(카드를 넣은 뒤 약 21KB · 210줄)
 *   · 예외를 던지는 핸들러 → 원래 AssertionError 그대로, 핸들러 예외는 suppressed, 뒤 핸들러도 불림
 * - Pixel_3_API_26 AVD(실제 API 28)는 이번에 '보안 시작 — 패턴' 화면에서 멈춰(adb offline) 쓰지 않았다 → API 29 미만 경로는 소스 기준
 * - AGP 9.5.3 의 DeviceProviderInstrumentTestTask · 관리 기기 테스트 태스크에 additionalTestOutputDir 처리 문자열이 있다 — 실기기가
 *   연결된 환경이라 connected 태스크는 돌리지 않았다
 *
 * --- Compose UI Testing ---
 * - 공식 문서: https://developer.android.com/develop/ui/compose/testing
 * - createComposeRule: 단일 Activity 없이 Composable을 직접 테스트
 * - createAndroidComposeRule: Activity 컨텍스트가 필요한 경우
 * - ⚠️ 패키지 주의: 기존 `androidx.compose.ui.test.junit4.createComposeRule` /
 *   `...junit4.createAndroidComposeRule` 은 deprecated 되었고
 *   `androidx.compose.ui.test.junit4.v2.*` 가 대체 API다(반환 타입은 v1과 동일해 import 만 바뀜).
 *   v2 는 UnconfinedTestDispatcher 대신 StandardTestDispatcher 를 사용해 작업을 즉시 실행하지 않고
 *   큐잉하므로, 즉시 실행에 의존하던 테스트는 waitUntil / awaitIdle 같은 명시적 동기화가 필요할 수 있다.
 *   마이그레이션 가이드: https://developer.android.com/develop/ui/compose/testing/migrate-v2
 * - onNodeWithTag / onNodeWithText / onNodeWithContentDescription: 시맨틱 트리 탐색
 * - performClick / performTextInput / performScrollTo: 사용자 인터랙션 시뮬레이션
 * - assertIsDisplayed / assertIsEnabled / assertTextEquals: 단언문
 *
 * 핵심 개념:
 * - 테스트 시맨틱 트리는 프로덕션 UI 트리와 별도로 유지됨
 * - testTag는 테스트 전용이므로 릴리즈 빌드에서 오버헤드 없음
 * - ComposeTestRule.mainClock: 애니메이션 시간을 수동으로 제어 가능
 * - waitUntil { condition }: 비동기 상태 변경을 기다리는 유틸리티
 *
 * --- Screenshot Testing (Paparazzi / Roborazzi) ---
 * - Paparazzi: https://cashapp.github.io/paparazzi/ (GitHub: https://github.com/cashapp/paparazzi)
 * - Roborazzi: https://github.com/takahirom/roborazzi
 * - 골든 이미지를 저장해두고 변경 시 자동으로 회귀를 검출
 * - Paparazzi: 에뮬레이터/실기기 없이 JVM에서 Android View/Compose 렌더링
 * - Roborazzi: Robolectric 위에서 실행, 더 넓은 Android API 커버
 * - 골든 이미지 갱신: ./gradlew recordPaparazziDebug 또는 recordRoborazzi
 * - 참고 블로그: https://medium.com/androiddevelopers/screenshot-testing-jetpack-compose-with-paparazzi-11d38feecef6
 *
 * --- Preview-Driven Screenshot Testing ---
 * - PreviewDrivenScreenshotExampleUI.kt 참조 (Preview를 source of truth로 매트릭스 파생)
 * - @Preview 를 단일 진실 공급원으로 삼아 locale × fontScale × theme 변형 매트릭스를 자동 파생
 * - 멀티프리뷰 애노테이션(여러 @Preview 묶음) + @PreviewParameter 로 차원을 코드로 표현
 * - 매트릭스 셀 1개 = 골든 이미지 1개. 축을 늘리면 커버리지가 곱(N×M×K)으로 증가
 * - AGP 8.5+ Compose Preview Screenshot Testing: @Preview 를 직접 입력으로 받아 공식 지원
 * - 공식 문서: https://developer.android.com/develop/ui/compose/tooling/previews
 * - Multipreview annotations: https://developer.android.com/develop/ui/compose/tooling/previews#multipreview
 * - @PreviewParameter: https://developer.android.com/develop/ui/compose/tooling/previews#preview-data
 *
 * --- Recomposition Test ---
 * - 출처: https://proandroiddev.com/catching-excessive-recompositions-in-jetpack-compose-with-tests-8d0b952e2853
 * - Compose 컴파일러가 생성하는 $changed 비트마스크 기반 최적화 검증
 * - remember { derivedStateOf { } } 패턴으로 불필요한 리컴포지션 제거
 * - RecompositionTestExample: 과도한 리컴포지션 감지 패턴 시연
 * - RecompositionCounter(SideEffect로 카운트 증가) + composeTestRule로 초기 컴포지션·상태 변경 후 재구성 횟수를 assertEquals로 단언
 * - @Stable/@Immutable 어노테이션으로 안정성을 보장해 스마트 리컴포지션 유도, key 파라미터로 LazyColumn 아이템 재사용 최적화
 *
 * --- Coroutine Flow Testing (Turbine) ---
 * - 원문: https://programminghard.dev/dont-learn-coroutine-testing-with-turbine/
 * - Turbine 이전에 코루틴 테스트 기초(runTest, 가상 시간)를 먼저 이해해야 함
 * - awaitItem() 체이닝은 과명세화(over-specification)를 유발함
 * - StateFlow 테스트: 상태별 독립 테스트 + runCurrent() / advanceUntilIdle()
 * - SharedFlow/단방향 이벤트 스트림에서는 Turbine이 적합
 * - 테스트 디스패처: StandardTestDispatcher(명시적 진행 제어) vs UnconfinedTestDispatcher(즉시 실행, 초기 상태 검증에 편리)
 *
 * --- Deterministic Images in Screenshot Tests ---
 * - 원문: https://alexzh.com/handling-asynchronous-images-in-android-screenshot-tests/
 * - Coil 3 Compose(프리뷰 핸들러): https://coil-kt.github.io/coil/compose/
 * - coil-test(FakeImageLoaderEngine): https://coil-kt.github.io/coil/testing/
 * - AsyncImage 는 LocalInspectionMode.current 가 true 일 때만 LocalAsyncImagePreviewHandler 를 조회한다
 *   (coil 3.1.0 의 coil3.compose.internal.UtilsKt.previewHandler 바이트코드로 확인 — 3.5.0 에도 같은 함수가 있다) → 일반 앱 실행에는 영향이 없다
 * - inspection 모드만 켜고 핸들러를 주지 않으면 기본값 AsyncImagePreviewHandler.Default 가 실제 ImageLoader.execute() 를
 *   그대로 수행한다 → 두 CompositionLocal 을 함께 제공해야 결정론이 생긴다
 * - 팩토리 AsyncImagePreviewHandler { image } 가 만드는 상태는 State.Success 가 아니라 painter 를 실은 State.Loading 이다
 *   → 픽셀은 고정되지만 onState 로 Success 를 기다리는 대기 로직은 끝나지 않는다
 *   (바이트코드 판독 후 실기기 SM-A725F/API 33 임시 계측 테스트로 재확인 — 방출된 상태는 [Loading] 하나뿐, Success 없음)
 * - LocalAsyncImagePreviewHandler / AsyncImagePreviewHandler 는 @ExperimentalCoilApi 이므로 @OptIn 필요
 * - 테스트 소스셋 전체를 덮으려면 coil-test 의 FakeImageLoaderEngine + SingletonImageLoader.setUnsafe(loader) 조합을 쓴다
 *   (이 프로젝트는 coil-test·스크린샷 러너를 의존성으로 두지 않아 코드 스니펫으로만 시연)
 * - 골든 이미지에 실제 사진 대신 단색이 찍히므로 레이아웃·크기 회귀 검출에는 오히려 유리하다
 */

/**
 * WorkManagerTestExampleUI (WorkManager 테스트 하네스)
 * - 공식 문서: https://developer.android.com/develop/background-work/background-tasks/testing/persistent/integration-testing
 * - work-testing API: https://developer.android.com/reference/androidx/work/testing/package-summary
 * - 출처(Android Weekly #743): https://segunfamisa.com/posts/androids-missing-work-manager-test-rule
 *
 * 핵심 개념:
 * - 의존성은 `androidTestImplementation("androidx.work:work-testing")` 한 줄이며 런타임 work 와 같은 버전을 쓴다.
 *   aar-metadata 는 minCompileSdk=34 / minAGP=1.0.0 이라 이 프로젝트는 상향 없이 2.9.1 을 그대로 채택했다.
 * - 세 도구의 역할이 다르다:
 *   ① `WorkManagerTestInitHelper.initializeTestWorkManager(context, config)` — 실제 스케줄러를 테스트 구현으로 교체
 *   ② `TestDriver` — `setAllConstraintsMet` / `setInitialDelayMet` / `setPeriodDelayMet` (전부 UUID 를 받는다)
 *   ③ `TestListenableWorkerBuilder<W>` — WorkManager 없이 워커 하나만 실행
 * - `SynchronousExecutor` 를 Configuration 에 넣으면 작업이 호출 스레드에서 즉시 실행돼 대기/idling 코드가 없어진다.
 * - 실측(SM-A725F/Android 13, 프로젝트 androidTest 5개 전부 통과):
 *   · 네트워크+충전 제약 작업 → enqueue 직후 `ENQUEUED`, `setAllConstraintsMet` 후 `SUCCEEDED`(출력 echo="HELLO").
 *     기기의 충전·네트워크 상태는 전혀 건드리지 않았다.
 *   · 초기 지연 24시간 → `ENQUEUED`, `setInitialDelayMet` 후 `SUCCEEDED`.
 *   · 격리 실행 → `Success {mOutputData=Data {echo : ISOLATED, }}`, 입력 없음 → `Failure {mOutputData=Data {}}`.
 *   · `setRunAttemptCount(0)` → `Retry`, `setRunAttemptCount(2)` → `Success {mOutputData=Data {attempt : 2, }}`.
 * - ⚠️ `initializeTestWorkManager` 는 프로세스의 WorkManager 싱글턴을 갈아끼운다. 앱 코드(예제 화면)에서 부르면
 *   기존 `WorkManagerExampleUI` 가 쓰는 실제 인스턴스까지 바뀌므로, 실행 코드는 androidTest 에만 두고
 *   화면은 설명 + 측정 결과만 싣는다(같은 폴더의 ScreenshotTesting/ComposeTesting 예제와 동일한 구성).
 * - ⚠️ `ExecutorsMode` 오버로드 3종: `LEGACY_OVERRIDE_WITH_SYNCHRONOUS_EXECUTORS`(기본) /
 *   `PRESERVE_EXECUTORS`(설정한 executor 유지) / `USE_TIME_BASED_SCHEDULING`(시간 기반 스케줄링).
 * - ⚠️ `enqueue(...).result.get()` 과 `getWorkInfoById(id).get()` 은 ListenableFuture 블로킹 대기다(메인 스레드 금지).
 * - ⚠️ work 2.11.2 에서 `getWorkInfoById(id).get()` 의 타입은 `WorkInfo?` 다(2.9.1 에서는 null 처리 없이 컴파일됐다) →
 *   테스트에서는 `checkNotNull(...)` 로 감싸 작업이 없으면 그 자리에서 실패하게 한다(2.11.2 상향 때 androidTest 컴파일이 여기서 깨졌다).
 * - ⚠️ WorkManager 는 프로세스 싱글턴이라 테스트 간 상태가 샌다 → 규칙의 finally 에서 `cancelAllWork()` 로 정리한다.
 * - ⚠️ TestDriver 는 "조건이 만족됐다"고 알릴 뿐 조건 판정 자체를 검증하지 않는다. 제약이 제대로 걸렸는지는
 *   WorkRequest 의 Constraints 를 단언하는 편이 맞다.
 * - 실제 검증 코드: `app/src/androidTest/java/com/example/composesample/example/WorkManagerTestExampleTest.kt`
 *
 * ## PowerAssertExampleUI (Kotlin Power-Assert 컴파일러 플러그인 — 2.4.20)
 * - 공식 문서: https://kotlinlang.org/docs/power-assert.html
 *
 * ### 설정 (2.4.20 Gradle 플러그인 바이트코드 + 실측)
 * - 플러그인 `org.jetbrains.kotlin.plugin.power-assert` 는 Kotlin 과 같은 버전으로 배포된다(카탈로그 kotlinVersion 참조)
 * - `functions` 기본값은 `kotlin.assert` 하나. 변환 대상은 마지막 파라미터가 String / () -> String 인 함수
 * - `compilationFilter` 기본값 TESTS 는 **컴파일 이름이 정확히 "test"** 인 것만 고른다. AGP 9 내장 Kotlin 의 컴파일은
 *   debug / release / debugUnitTest / debugAndroidTest 라서 기본값으로는 아무 데도 적용되지 않는다 → 이름 목록으로 직접 지정
 * - `includedSourceSets` 는 2.4.20 에서 deprecated("compilationFilter 를 쓰라")
 * - `addRuntimeDependency` 기본 true — 적용된 컴파일에 `org.jetbrains.kotlin:kotlin-power-assert-runtime`(jvm 27KB, stdlib 만 의존)이 붙는다
 *
 * ### @PowerAssert (2.4.20, @ExperimentalPowerAssert)
 * - 함수에 @PowerAssert 를 붙이면 functions 에 적지 않아도 호출부가 변환되고, 함수 안에서 `PowerAssert.explanation` 으로
 *   `CallExplanation`(source · offset · arguments[kind, startOffset, endOffset, expressions] · expressions)을 받는다
 * - 부분식은 ValueExpression / LiteralExpression / EqualityExpression(lhs·rhs) — 값으로 직접 메시지를 만들 수 있다.
 *   `source` 는 호출이 있는 줄의 들여쓰기부터 담고 offset 도 그 기준. `@PowerAssert.Ignore` 파라미터는 arguments 에서 null
 * - 컴파일러는 `powerCheck$powerassert(…, Function0<CallExplanation>)` 오버로드를 만들고 변환된 호출부는 이쪽을 부른다.
 *   원래 함수 안의 `PowerAssert.explanation` 은 null 상수로 컴파일된다(release javap) → 리플렉션 호출이면 설명 null
 * - 플러그인 없이 컴파일된 본문에서 `PowerAssert.explanation` 을 읽으면 런타임 스텁이 NotImplementedError 를 던진다
 *
 * ### 실측 (Galaxy A72 · Android 13, debug·release / 로컬 JVM 단위 테스트)
 * - plain 은 "조건이 false 입니다" 뿐, power 는 각 부분식의 값(합계 13600·총액 13000, count 2, null 체인, 'm', 범위 1..99)을 도식으로
 * - && 오른쪽이 평가되면 값이 찍히고, 평가되지 않은 부분에는 값이 없다. message 는 도식 위에 붙는다
 * - 부수효과: `powerCheck(counter.incrementAndGet() == 5)` → 1회. 메시지를 위해 식을 다시 적은 plain 은 2회
 * - 성공 경로: 값의 toString 0회(설명은 람다라 실패할 때만 만든다) / 실패 1회 / 메시지를 미리 만든 plain 은 성공해도 1회
 * - `kotlin.assert` 는 `kotlin._Assertions.ENABLED` 일 때만 검사(변환 뒤에도 같음). 기기에서 debug 는 던지고 release 는 건너뛴다
 *   (desiredAssertionStatus() 는 둘 다 false). 로컬 JVM 단위 테스트는 단언이 켜진 채 돈다
 * - 단위 테스트 before/after: `expected:<13000> but was:<13600>` → 식 도식 + 같은 문장 / `Assertion failed` → 식 도식
 * - 계측 테스트(debugAndroidTest)의 assert 도 도식과 함께 던진다(필터에 포함)
 *
 * ### ⚠️ debug 핫 리로드(HotSwan 2.0.2)와 함께 쓸 때
 * - @PowerAssert 함수 본문을 HotSwan 이 실행하면 `PowerAssert.explanation` 이 치환되지 않아 NotImplementedError(기기·JVM 테스트)
 * - 변환된 호출부를 HotSwan 이 실행하면 부수효과 있는 식이 3회 평가(`-Photswan.dispatchRewriteEnabled=false` 면 1회)
 * - → `hotSwanCompiler { exclude("…PowerAssertChecksKt"); exclude("…PowerAssertViewModel") }` (정확한 클래스명) 로 해결.
 *   상세는 docs/devtools/ComposeHotReloadGuide.md
 * - 실제 검증 코드: `app/src/test/java/com/example/composesample/example/PowerAssertExampleTest.kt`
 */
