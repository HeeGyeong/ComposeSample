package com.example.composesample.presentation.example.component.architecture.development.test

/**
 * Test Examples 참고 자료
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
 *   마이그레이션 가이드: https://developer.android.com/develop/ui/compose/testing/migrate-to-v2
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
 *   (coil 3.1.0 의 coil3.compose.internal.UtilsKt.previewHandler 바이트코드로 확인) → 일반 앱 실행에는 영향이 없다
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
