package com.example.composesample.presentation.example.component.ui.accessibility

/**
 * Accessible Focus Indicator (Indication API) 학습 가이드
 *
 * 출처: https://eevis.codes/blog/2026-04-30/more-accessible-focus-indicators-with-compose/
 *
 * Jetpack Compose에서 키보드/D-pad/TV 리모컨 사용자가 현재 어떤 요소에
 * 포커스가 있는지 시각적으로 인지할 수 있도록 포커스 인디케이터를 강화하는 방법을 다룹니다.
 *
 * =================================================================================================
 * 핵심 1: Modifier.focusable() 의 기본 동작
 * =================================================================================================
 * - clickable 이 아닌 일반 컴포저블은 기본적으로 키보드 포커스를 받지 못함
 * - Modifier.focusable(interactionSource = ..., indication = ...) 으로 키보드 탐색 활성화
 * - 기본 indication 은 Material3 Ripple 이며 키보드 포커스 시 시각적 표시가 약함
 *
 * =================================================================================================
 * 핵심 2: collectIsFocusedAsState 로 간단히 외곽선 그리기
 * =================================================================================================
 * ```kotlin
 * val source = remember { MutableInteractionSource() }
 * val isFocused by source.collectIsFocusedAsState()
 * Box(
 *     Modifier
 *         .border(2.dp, if (isFocused) Color.Blue else Color.Transparent)
 *         .focusable(interactionSource = source)
 * )
 * ```
 *
 * =================================================================================================
 * 핵심 3: IndicationNodeFactory 로 재사용 가능한 커스텀 인디케이션
 * =================================================================================================
 * - Compose 1.7+ 에서 도입된 IndicationNodeFactory 는 Modifier.indication() 으로 적용
 * - DrawModifierNode 를 사용하면 콘텐츠 위에 외곽선/펄스/오버레이를 그릴 수 있음
 * - InteractionSource.interactions Flow 를 collect 해 FocusInteraction.Focus / Unfocus 추적
 *
 * =================================================================================================
 * 적용 가이드
 * =================================================================================================
 * - 외곽선 두께는 2dp 이상 권장 (WCAG 2.4.7 Focus Visible)
 * - 명도 대비 3:1 이상 색상 사용
 * - 동작은 빠르게(150~250ms) — 모션 멀미 사용자 고려 시 prefers-reduced-motion 대응 필요
 * - clickable 컴포저블에는 자동으로 indication 이 적용되므로 Box/Card 등 비클릭 요소에 주로 적용
 */
object AccessibleFocusIndicatorGuide
