package com.example.composesample.presentation.example.component.ui.scroll

/**
 * Scroll 예제 참고 자료
 *
 * ## NestedScrollingExampleUI (NestedScrollConnection + Collapsing Toolbar)
 * 핵심 개념:
 * - 스크롤 처리 순서: onPreScroll(자식 처리 전) → Child Scroll(LazyColumn 자체 처리) → onPostScroll(자식 처리 후) → onPreFling/onPostFling(플링 속도 제어)
 * - Collapsing Toolbar: toolbarOffset을 0~-toolbarHeightPx 범위로 제어하고 alpha = 1f - (abs(offset) / totalHeight)로 계산
 * - 위로 스크롤 시 onPreScroll에서 일부(예: 30%) 우선 소비해 헤더를 숨기고, 나머지는 LazyColumn에 위임. 아래로 스크롤 시 LazyColumn이 더 이상 소비 못 하는 양(available.y > 0)을 onPostScroll에서 소비해 헤더를 다시 표시
 * - remember()로 NestedScrollConnection 객체를 재사용해 불필요한 recomposition 방지
 *
 * ## CustomScrollBehaviorExampleUI (커스텀 TopAppBarScrollBehavior)
 * - 출처: https://le0nidas.gr/2026/02/08/handle-recyclerviews-scroll-events-in-custom-topappbarscrollbehavior/
 * 핵심 개념:
 * - TopAppBarScrollBehavior는 내부적으로 NestedScrollConnection을 통해 state.heightOffset(0 또는 heightOffsetLimit)을 조정해 앱바를 완전히 보이거나 숨김
 * - MyEnterAlwaysScrollBehavior: 스크롤 버퍼(scrollAccumulation 100px 누적)로 미세한 제스처를 무시하고, animate()+tween(150)으로 부드러운 전환, animationInProgress 플래그로 애니메이션 중 재진입 방지
 * - MyExitUntilCollapsedScrollBehavior: onPreScroll에서 위로 스크롤(available.y < 0)일 때만 숨기고, onPostScroll에서 스크롤이 완전히 끝(available == Offset.Zero)났을 때만 다시 표시
 * - RecyclerView 등 View 기반 스크롤은 NestedScrollDispatcher로 이벤트를 Compose 쪽 NestedScrollConnection에 전달해 연동 가능
 */
