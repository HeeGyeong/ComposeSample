package com.example.composesample.presentation.example.component.interaction.drag

/**
 * Interaction/Drag 예제 참고 자료
 *
 * ## DragAndDropExampleUI (LazyColumn 드래그 앤 드롭 + 위치 변경)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling
 * - reorderable 라이브러리: https://github.com/Calvin-LL/Reorderable
 * 핵심 개념:
 * - detectDragGestures(onDragStart/onDrag/onDragEnd)로 드래그 오프셋 추적
 * - 드래그 중인 아이템 인덱스와 hover 위치를 계산해 리스트 순서 swap
 * - graphicsLayer translationY 로 드래그 아이템을 시각적으로 따라오게 처리
 * - 리스트 상태(MutableList/StateFlow)를 갱신해 드롭 시 최종 순서 확정
 */
