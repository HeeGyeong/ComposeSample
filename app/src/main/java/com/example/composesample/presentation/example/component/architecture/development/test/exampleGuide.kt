package com.example.composesample.presentation.example.component.architecture.development.test

/**
 * Test Examples 참고 자료
 *
 * --- Compose UI Testing ---
 * - 공식 문서: https://developer.android.com/develop/ui/compose/testing
 * - createComposeRule: 단일 Activity 없이 Composable을 직접 테스트
 * - createAndroidComposeRule: Activity 컨텍스트가 필요한 경우
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
 * - Paparazzi: https://cashapp.github.io/paparazzi/
 * - Roborazzi: https://github.com/takahirom/roborazzi
 * - 골든 이미지를 저장해두고 변경 시 자동으로 회귀를 검출
 * - Paparazzi: 에뮬레이터/실기기 없이 JVM에서 Android View/Compose 렌더링
 * - Roborazzi: Robolectric 위에서 실행, 더 넓은 Android API 커버
 * - 골든 이미지 갱신: ./gradlew recordPaparazziDebug 또는 recordRoborazzi
 *
 * --- Preview-Driven Screenshot Testing ---
 * - PreviewDrivenScreenshotExampleUI.kt 참조 (Preview를 source of truth로 매트릭스 파생)
 * - @Preview 를 단일 진실 공급원으로 삼아 locale × fontScale × theme 변형 매트릭스를 자동 파생
 * - 멀티프리뷰 애노테이션(여러 @Preview 묶음) + @PreviewParameter 로 차원을 코드로 표현
 * - 매트릭스 셀 1개 = 골든 이미지 1개. 축을 늘리면 커버리지가 곱(N×M×K)으로 증가
 * - AGP 8.5+ Compose Preview Screenshot Testing: @Preview 를 직접 입력으로 받아 공식 지원
 * - 공식 문서: https://developer.android.com/develop/ui/compose/tooling/previews
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
 * --- Fakes vs Mocks in Testing ---
 * - 원문: https://medium.com/@dhananjaydy/fakes-vs-mocks-in-testing-08ec776742d9 (Android Weekly #736)
 * - Mock: 상호작용을 검증하는 테스트 더블. verify()로 "몇 번, 어떤 인자로 호출됐는지"를 확인하며, 반환값은 사전에 프로그래밍(stub)된 값만 나옴 — 실제 상태를 저장하지 않음
 * - Fake: 실제로 동작하는 경량 구현체(예: 인메모리 Map 기반 DAO). production 코드와 동일한 계약을 만족해 insert→get 라운드트립 같은 실제 로직을 그대로 수행
 * - 핵심 함정: Mock에 insert 후 같은 값을 get 하면, stub에 없는 값이라 null이 나옴(실제 상태에 반영 안 됨) — Fake는 항상 일관됨
 * - 브리틀니스: 저장 로직을 리팩터링(insertUser 2회 호출로 변경, 최종 상태는 동일)해도 Mock의 verify(exactly=1)은 깨지지만(구현 세부사항 결합), Fake 기반 상태 검증(assertEquals)은 안 깨짐
 * - 적합한 대상: Mock은 외부 시스템과의 상호작용(결제 API 호출 여부, 분석 이벤트 발행) 검증에, Fake는 데이터 계층(Repository/DAO)처럼 상태를 갖는 컴포넌트 검증에 적합
 * - MockK(io.mockk, 프로젝트 테스트 의존성에 이미 존재): every { } 로 stub, verify(exactly = n) { } 로 상호작용 검증
 */
