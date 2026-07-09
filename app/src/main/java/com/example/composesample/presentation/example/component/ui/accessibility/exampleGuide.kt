package com.example.composesample.presentation.example.component.ui.accessibility

/**
 * Accessibility 예제 참고 자료
 *
 * ## LargeContentViewerExampleUI (Large Content Viewer with Navigation Support)
 * - 출처: https://eevis.codes/blog/2026-02-28/adding-navigation-support-to-large-content-viewer-with-compose/
 * - 이전 글: https://eevis.codes/blog/beyond-font-scaling-large-content-viewer-with-compose/
 * 핵심 개념:
 * - iOS의 Large Content Viewer(작은 아이콘 long-press 시 확대 프리뷰)를 Compose로 구현
 * - pointerInput(detectTapGestures.onLongPress)로 터치 프리뷰, onFocusChanged + delay(longPressTimeoutMillis)로 키보드 내비게이션 대응
 * - semantics { customActions }로 TalkBack 스크린 리더용 커스텀 액션 등록, Voice Access는 기존 long-press 재사용으로 별도 대응 불필요
 *
 * ## AccessibleFocusIndicatorExampleUI (Indication API 기반 포커스 인디케이터)
 * - 출처: https://eevis.codes/blog/2026-04-30/more-accessible-focus-indicators-with-compose/
 * 핵심 개념:
 * - Modifier.focusable(interactionSource, indication)로 키보드/D-pad/TV 리모컨 포커스 활성화 (기본 Ripple만으로는 시각 표시 약함)
 * - collectIsFocusedAsState로 간단히 외곽선 그리기 가능, Compose 1.7+ IndicationNodeFactory + DrawModifierNode로 재사용 가능한 커스텀 인디케이션 구현
 * - WCAG 2.4.7 기준 외곽선 2dp 이상·명도 대비 3:1 이상 권장, clickable 요소는 자동 적용되므로 Box/Card 등 비클릭 요소에 주로 적용
 */
