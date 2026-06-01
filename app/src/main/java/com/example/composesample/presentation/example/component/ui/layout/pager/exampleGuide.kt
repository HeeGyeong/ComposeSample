package com.example.composesample.presentation.example.component.ui.layout.pager

/**
 * UI/Layout/Pager 예제 참고 자료
 *
 * ## PullScreenPagerUI (HorizontalPager / VerticalPager)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/pager
 * 핵심 개념:
 * - HorizontalPager(state = rememberPagerState { pageCount }) 로 스와이프 페이지 전환
 * - PagerState.currentPage / currentPageOffsetFraction 으로 페이지 인디케이터·시차 효과 구현
 * - scope.launch { pagerState.animateScrollToPage(n) } 로 프로그래매틱 이동
 * - beyondViewportPageCount 로 인접 페이지 프리로드 수 조절
 */
