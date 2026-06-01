package com.example.composesample.presentation.example.component.ui.layout.flexbox

/**
 * UI/Layout/Flexbox 예제 참고 자료
 *
 * ## FlowRowLayoutExampleUI (FlowRow / FlowColumn 공식 Flexbox)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/flow
 * - 출처: https://navczydev.medium.com/meet-flexbox-the-powerful-new-layout-system-for-compose-446b1f65cc62
 * 핵심 개념:
 * - FlowRow/FlowColumn: 공간이 부족하면 자동으로 다음 줄(열)로 줄바꿈하는 레이아웃
 * - maxItemsInEachRow: 한 줄 최대 아이템 수 제한
 * - horizontalArrangement/verticalArrangement + itemSpacing 으로 간격 제어
 * - Modifier.weight(): FlowRowScope 내에서 남은 공간 비율 분배
 *
 * ## FlexBoxUI (반응형 디자인)
 * 핵심 개념:
 * - 화면 크기에 따라 아이템 배치가 줄바꿈되는 CSS Flexbox 유사 패턴
 */
