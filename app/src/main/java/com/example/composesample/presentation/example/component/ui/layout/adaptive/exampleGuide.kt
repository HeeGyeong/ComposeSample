package com.example.composesample.presentation.example.component.ui.layout.adaptive

/**
 * Adaptive Layout (WindowSizeClass) 예제 참고 자료
 *
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/adaptive
 * - WindowSizeClass API (androidx.window): https://developer.android.com/reference/kotlin/androidx/window/core/layout/WindowSizeClass
 * - WindowSizeClass API (Material3, 이 예제가 사용하는 API): https://developer.android.com/reference/kotlin/androidx/compose/material3/windowsizeclass/WindowSizeClass
 *
 * 핵심 개념:
 * - WindowSizeClass: 화면 너비/높이를 Compact/Medium/Expanded 3단계로 분류
 *   - Compact: 600dp 미만 (일반 폰 세로 모드)
 *   - Medium: 600~840dp (폴더블 반 접힘, 태블릿 세로)
 *   - Expanded: 840dp 이상 (태블릿 가로, 데스크탑)
 * - calculateWindowSizeClass(): Activity 컨텍스트에서 현재 WindowSizeClass 계산
 *
 * 패턴:
 * - Compact → 단일 컬럼 (ListDetail 순차 표시)
 * - Medium → 2컬럼 그리드
 * - Expanded → 사이드바 + 메인 영역 (ListDetail 동시 표시)
 *
 * 의존성:
 * implementation(libs.material3WindowSizeClass)
 */

/**
 * Compose MediaQuery API 예제 참고 자료
 *
 * - Compose 2026년 4월 릴리스 소개: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
 * - Adaptive UI 공식 가이드: https://developer.android.com/develop/ui/compose/layouts/adaptive
 * - 폴더블 자세(FoldingFeature) 참고: https://developer.android.com/reference/kotlin/androidx/window/layout/FoldingFeature
 *
 * 핵심 개념:
 * - UiMediaScope: 환경 질의의 대상이 되는 인터페이스. 8개 속성으로 구성된다.
 *   - windowWidth / windowHeight : Dp
 *   - windowPosture : Flat / Tabletop / Book (androidx.window 힌지 상태 기반)
 *   - pointerPrecision : Fine(마우스·스타일러스) / Coarse(터치) / Blunt(리모컨) / None
 *   - keyboardKind : Physical / Virtual / None
 *   - viewingDistance : Near(폰·태블릿) / Medium(자동차) / Far(TV)
 *   - hasCamera / hasMicrophone : Boolean
 * - mediaQuery { }        : 질의를 즉시 평가해 Boolean 반환. 람다가 읽은 값이 바뀌면 호출한 쪽이 리컴포지션
 * - derivedMediaQuery { } : derivedStateOf 로 감싼 State<Boolean> 반환. 결과가 뒤집힐 때만 리컴포지션
 * - 두 함수 모두 LocalUiMediaScope 를 consume 하므로, 이 CompositionLocal 이 제공돼야만 동작한다.
 *
 * 활성화(이 API 를 쓸 때 가장 먼저 막히는 지점):
 * - LocalUiMediaScope 는 기본값이 없다. 값을 읽으면 "CompositionLocal LocalUiMediaScope not present"
 *   IllegalStateException 이 발생한다.
 * - 플랫폼(ComposeViewContext.ProvideCompositionLocals)이 이 값을 제공하는 것은
 *   ComposeUiFlags.isMediaQueryIntegrationEnabled 가 true 일 때뿐이며, 이 플래그의 기본값은 false 다.
 * - 플랫폼 구현을 만드는 obtainUiMediaScope() 는 Kotlin internal 이라 앱 모듈에서 호출할 수 없다.
 *   즉 "직접 만들어 provide 한다"는 우회로가 없고, 플래그를 켜는 것이 유일한 경로다.
 * - 플래그는 컴포지션 루트가 만들어질 때 한 번 읽히므로 반드시 setContent 이전에 설정해야 한다.
 *   이 프로젝트는 BlogExampleActivity.onCreate 에서 켠다.
 * - 컴파일은 언제나 통과하므로 빌드 성공만으로는 이 문제가 드러나지 않는다.
 *
 * 테스트/프리뷰:
 * - UiMediaScope 는 인터페이스라 직접 구현해 CompositionLocalProvider 로 갈아끼울 수 있다.
 *   예제의 mediaQuery/derivedMediaQuery 비교 카드가 이 방식으로 창 폭을 시뮬레이션한다.
 * - 이때 windowWidth 는 값이 아니라 getter 로 둬야 질의 시점에 스냅샷 상태를 읽는다.
 *
 * AdaptiveLayout(WindowSizeClass) 예제와의 차이:
 * - WindowSizeClass 는 material3 API 로 Activity 가 필요하고 폭·높이를 3단계로 분류하는 것이 전부다.
 * - MediaQuery 는 ui 레이어 API 로 크기 외 환경(자세·입력 방식·시청 거리)까지 하나의 스코프로 질의한다.
 *
 * 실험 API:
 * - @OptIn(ExperimentalMediaQueryApi::class) 필요 (어노테이션은 ui-util 에 있음)
 * - 플래그 접근에는 @OptIn(ExperimentalComposeUiApi::class) 필요
 */
