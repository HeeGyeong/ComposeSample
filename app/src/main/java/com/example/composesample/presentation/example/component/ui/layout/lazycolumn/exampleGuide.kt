package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

/**
 * UI/Layout/LazyColumn 예제 참고 자료
 *
 * ## LazyColumn 성능/이슈 (LazyColumnFlingBehaviorExampleUI, LazyColumnIssueExampleUI)
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
 *
 * ## LazyListReusePoolExampleUI (contentType 재사용 풀 함정)
 * - 출처(아티클): https://touchlab.co/the-one-liner-that-was-eating-our-memory
 * - contentType 공식 문서: https://developer.android.com/develop/ui/compose/lists#content-type
 * - 아이템 key: https://developer.android.com/develop/ui/compose/lists#item-keys
 * 핵심 개념:
 * - 재사용 풀 정리 규칙은 contentType 별로 7개까지 유지이고, 전체 슬롯 수에는 상한이 없다
 *   (androidx.compose.foundation.lazy.layout.LazyLayoutItemReusePolicy.getSlotsToRetain, 1.11.1 기준 내부 구현)
 * - contentType 에 아이템 고유값을 넘기면 버킷이 아이템 수만큼 생겨 정리 조건에 도달하지 못한다
 * - 남은 슬롯은 remember 값과 modifier 람다가 캡처한 객체까지 도달 가능한 상태로 붙들고 있다
 * - key(식별자, 아이템마다 달라야 함) vs contentType(분류, 레이아웃 종류만큼만) 의 역할 구분
 */
