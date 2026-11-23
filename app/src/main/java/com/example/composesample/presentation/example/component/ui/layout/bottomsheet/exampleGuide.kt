package com.example.composesample.presentation.example.component.ui.layout.bottomsheet

/**
 * UI/Layout/BottomSheet 예제 참고 자료
 *
 * ## ModalBottomSheetExampleUI / BottomSheetExampleUI / CustomBottomSheetExampleUI
 * - 공식 문서: https://developer.android.com/develop/ui/compose/components/bottom-sheets
 * 핵심 개념:
 * - M3 ModalBottomSheet + rememberModalBottomSheetState: 모달 형태 바텀시트(스크림 포함)
 *   — M3 1.5.0 에서 rememberModalBottomSheetState · rememberStandardBottomSheetState 는 deprecated →
 *   rememberBottomSheetState(initialValue, enabledValues). 옛 함수는 '반만 펼침' 위치를 예전 방식(화면 50%)으로 고정해 만들고,
 *   새 함수는 ComposeMaterial3Flags.isBottomSheetPartiallyExpandedDeterministicEnabled(기본 true — 작은 시트는 자기 높이까지만)를 따른다.
 *   enabledValues 에 PartiallyExpanded 가 없거나 BottomSheetScaffold(peek 높이)면 차이 없음(material3 1.5.0 소스)
 * - sheetState.show()/hide()는 suspend → rememberCoroutineScope().launch 에서 호출
 * - SheetValue(Hidden/PartiallyExpanded/Expanded) 상태 전이 + skipPartiallyExpanded 옵션
 * - 일부 예제는 M2 BottomSheetScaffold 시연을 의도적으로 보존(@Suppress("DEPRECATION"), ARCHITECTURE.md 교육용 예외)
 *   — Compose 1.13 에서 M2 라이브러리 유지보수 종료가 발표돼 화면 위에 M2LegacyNotice 한 줄을 둔다(DEPRECATION-113-01)
 * - currentFraction 계산으로 펼침 진행률에 따른 UI 변화 시각화
 */
