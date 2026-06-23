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
 * - Compose 컴파일러가 생성하는 $changed 비트마스크 기반 최적화 검증
 * - remember { derivedStateOf { } } 패턴으로 불필요한 리컴포지션 제거
 * - RecompositionTestExample: 과도한 리컴포지션 감지 패턴 시연
 *
 * --- Coroutine Flow Testing (Turbine) ---
 * - TurbineFlowTestExampleGuide.kt 참조 (별도 파일로 관리)
 */
