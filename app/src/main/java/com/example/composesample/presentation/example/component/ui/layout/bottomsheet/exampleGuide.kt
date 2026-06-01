package com.example.composesample.presentation.example.component.ui.layout.bottomsheet

/**
 * UI/Layout/BottomSheet 예제 참고 자료
 *
 * ## ModalBottomSheetUI / BottomSheetUI / CustomBottomSheetUI
 * - 공식 문서: https://developer.android.com/develop/ui/compose/components/bottom-sheets
 * 핵심 개념:
 * - M3 ModalBottomSheet + rememberModalBottomSheetState: 모달 형태 바텀시트(스크림 포함)
 * - sheetState.show()/hide()는 suspend → rememberCoroutineScope().launch 에서 호출
 * - SheetValue(Hidden/PartiallyExpanded/Expanded) 상태 전이 + skipPartiallyExpanded 옵션
 * - 일부 예제는 M2 BottomSheetScaffold 시연을 의도적으로 보존(@Suppress("DEPRECATION"), ARCHITECTURE.md 교육용 예외)
 * - currentFraction 계산으로 펼침 진행률에 따른 UI 변화 시각화
 */
