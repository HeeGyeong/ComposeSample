package com.example.composesample.presentation.example.component.ui.button

/**
 * Button 예제 참고 자료
 *
 * ## ButtonGroupExampleUI (Material 3 Expressive ButtonGroup)
 * - 출처: https://joebirch.co/android/exploring-jetpack-compose-buttongroup/
 *
 * ### 핵심 개념
 * - ButtonGroup(Experimental, `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`): 버튼 수평 배치 + 자동 확장(expandedRatio) + 오버플로우 메뉴 기본 지원
 * - ButtonGroupScope: clickableItem()(단순 클릭) / toggleableItem()(Single·Multi 선택) / customItem()(커스텀)
 * - Connected 스타일: connectedLeadingButtonShapes()/Middle/Trailing로 세그먼트 컨트롤 구현 (deprecated된 SegmentedButton 대체)
 * - 본 예제는 실제 라이브러리 API 대신 Simulated*Button 계열 컴포넌트로 동작을 시각적으로 재현(시뮬레이션)
 */
