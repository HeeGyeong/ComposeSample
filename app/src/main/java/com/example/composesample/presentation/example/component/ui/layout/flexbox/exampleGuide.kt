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
 * ## FlexBoxExampleUI (반응형 디자인)
 * 핵심 개념:
 * - 화면 크기에 따라 아이템 배치가 줄바꿈되는 CSS Flexbox 유사 패턴
 *
 * ## FlowOverflowExampleUI (Flow 레이아웃 오버플로 제어)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/flow
 * 핵심 개념:
 * - maxLines: FlowRow/FlowColumn 이 표시할 최대 줄 수 제한(넘는 줄은 컴포즈만 되고 배치되지 않음)
 * - FlowRowOverflow.expandIndicator { }: 넘치는 자리에 "+N개" 같은 커스텀 인디케이터 배치
 * - FlowRowOverflow.expandOrCollapseIndicator(expandIndicator, collapseIndicator, minRowsToShowCollapse, minHeightToShowCollapse):
 *   펼침/접힘 인디케이터를 한 쌍으로 등록. minRowsToShowCollapse 는 펼친 뒤 실제 줄 수가 이 값 이상일 때만 "접기"를 노출
 * - ContextualFlowRow(itemCount, maxLines) { index -> }: 인덱스 기반 지연 생성 — 내부적으로
 *   SubcomposeMeasureScope 를 사용해 실제 배치되는 아이템만 컴포즈함(일반 FlowRow 는 content 람다가
 *   forEach 로 전체를 미리 호출하므로 화면에 안 보여도 전부 컴포즈됨)
 * - FlowRowOverflowScope.totalItemCount / shownItemCount: 오버플로 인디케이터 안에서 전체/표시 개수 조회
 * - ⚠️ 이 프로젝트가 해석하는 foundation-layout 1.11.1 기준, overflow 파라미터를 받는 FlowRow/FlowColumn/
 *   ContextualFlowRow/ContextualFlowColumn 오버로드는 전부 `@Deprecated("The overflow parameter has been
 *   deprecated")` 로 표시돼 있다(javap 로 확인, replaceWith 없음). 그럼에도 이 버전에서 FlowRowOverflow 를
 *   전달할 수 있는 유일한 진입점이라 대체 API가 없어, 해당 호출부에만 `@Suppress("DEPRECATION")` 을 최소
 *   범위로 적용했다. Compose 1.12+ 로 올라가면(로컬 SDK android-37 미설치로 현재는 마이그레이션 보류)
 *   비-deprecated 대체 오버로드가 생겼는지 재확인할 것.
 */
