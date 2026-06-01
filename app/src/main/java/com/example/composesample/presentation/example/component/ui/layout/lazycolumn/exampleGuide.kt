package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

/**
 * UI/Layout/LazyColumn 예제 참고 자료
 *
 * ## LazyColumn 성능/이슈 (LazyColumnFlingBehaviorExampleUI, LazyColumnIssueUI)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/lists
 * - 성능: https://developer.android.com/develop/ui/compose/lists#item-keys
 * 핵심 개념:
 * - items(list, key = { it.id }): 안정적 key로 아이템 이동/삭제 시 재사용·애니메이션 보장
 * - contentType 지정으로 이종 아이템 재사용 효율 향상
 * - FlingBehavior 커스터마이징으로 스크롤 감속/스냅 동작 변경
 * - targetSDK 35 edge-to-edge 대응 시 contentPadding/WindowInsets 처리
 *
 * ## LazyStaggeredGridExampleUI (폭포수 그리드)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/lists#lazy-staggered-grid
 * 핵심 개념:
 * - LazyVerticalStaggeredGrid + StaggeredGridCells.Fixed/Adaptive 로 동적 높이 폭포수 배치
 * - 필터링 시 key 기반 itemPlacement 애니메이션
 *
 * ## ReverseLazyColumnExampleUI (역방향 리스트)
 * 핵심 개념:
 * - reverseLayout = true 로 채팅처럼 하단부터 쌓이는 리스트 구현
 */
