package com.example.composesample.presentation.example.component.ui.layout.header

/**
 * UI/Layout/Header 예제 참고 자료
 *
 * ## StickyHeaderExampleUI (Sticky Header + 스크롤 상태 연동)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/lists#sticky-headers
 * 핵심 개념:
 * - LazyColumn의 stickyHeader { } DSL로 스크롤 중 헤더 고정
 * - LazyListState.firstVisibleItemIndex / firstVisibleItemScrollOffset 로 스크롤 위치 관찰
 * - derivedStateOf 로 스크롤 임계값 통과 시에만 헤더 스타일 변경(불필요한 리컴포지션 방지)
 * - 헤더 그룹핑: 데이터를 키별로 묶어 각 그룹 상단에 sticky header 배치
 */
