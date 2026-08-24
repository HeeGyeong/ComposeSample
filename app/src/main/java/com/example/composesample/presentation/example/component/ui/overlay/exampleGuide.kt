package com.example.composesample.presentation.example.component.ui.overlay

/**
 * 좌표 기반 스포트라이트 오버레이(코치마크) Example 참고 자료
 *
 * - onGloballyPositioned 공식 문서: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/package-summary#(androidx.compose.ui.Modifier).onGloballyPositioned(kotlin.Function1)
 * - Popup 공식 문서: https://developer.android.com/reference/kotlin/androidx/compose/ui/window/package-summary#Popup(androidx.compose.ui.window.PopupPositionProvider,kotlin.Function0,androidx.compose.ui.window.PopupProperties,kotlin.Function2)
 * - Path 공식 문서(op/PathOperation 포함): https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/Path
 * - 참고 라이브러리(기법 출처, 이 예제는 라이브러리 도입이 아니라 기법 자체를 stdlib+Compose API로 직접 구현): https://github.com/ShowcaseLayoutCompose (AW #739 소개)
 *
 * 핵심 개념:
 * - onGloballyPositioned로 타깃 컴포저블이 배치를 마친 뒤의 boundsInWindow()를 얻어 window 좌표계 기준 좌표를 수집한다
 * - Popup은 별도 창(android.view.Window)처럼 렌더되어 부모 Composable(Card/LazyColumn 등)의 clip이나 패딩 경계에 갇히지 않는다
 *   — 그냥 Box로 겹쳐 그리면 스크림이 화면 전체를 덮지 못하는 문제가 이 예제가 Popup을 쓰는 이유다
 * - "구멍 뚫기"는 전체 영역 Path에서 타깃 영역 Path를 PathOperation.Difference로 빼서 "전체 - 타깃" 모양의 Path를 만들고,
 *   그 Path로 clipPath를 건 뒤 스크림 색을 채우는 방식으로 구현한다(타깃 영역은 클립에서 제외돼 원본이 그대로 비쳐 보임)
 * - 스텝 전환은 구멍의 left/top/right/bottom 네 좌표를 각각 animateFloatAsState로 애니메이션하고, Canvas의 draw 단계에서
 *   State.value를 직접 읽어(리컴포지션이 아니라 재드로우만 유발) 매끄럽게 이동시킨다
 * - PopupPositionProvider를 직접 구현하면 앵커 컴포저블과 무관하게 Popup을 원하는 window 좌표에 고정 배치할 수 있다
 *
 * 프로젝트 내 관련 예제와의 구분:
 * - BottomSheetContent(BottomSheet 예제): onGloballyPositioned로 시트 높이를 측정하는 용도로만 쓰고, Popup/구멍 뚫기는 다루지 않음
 * - 이 예제: onGloballyPositioned(좌표 수집) + Popup(전체화면 오버레이) + Path.op/clipPath(구멍 뚫기) + 애니메이션을
 *   하나의 파이프라인으로 엮어 코치마크라는 완결된 UI 패턴을 구현
 *
 * 주의사항:
 * - 데모는 실제 디바이스 전체 화면이 아니라 화면 안의 "미니 화면" 영역(고정 높이 Box)에 한정해 Popup을 겹쳐 놓는다.
 *   진짜 전체 화면에 적용하려면 상태 바/제스처 인셋을 함께 고려해 좌표를 보정해야 하는데, 그 보정은 기기/edge-to-edge
 *   설정에 따라 달라져 이 예제의 핵심(좌표 기반 구멍 뚫기 기법)과는 별개의 관심사라 범위에서 제외했다
 * - clipPath의 Path.op(Difference) 연산은 매 프레임 Path 객체를 새로 만든다 — 실제 프로덕션에서 대상이 아주 많거나
 *   고빈도로 애니메이션된다면 Path 재사용/캐싱을 고려할 것(이 예제는 타깃 3개 규모의 데모라 별도 캐싱을 하지 않음)
 */
