package com.example.composesample.presentation.example.component.ui.layout.topappbar

/**
 * Layout/TopAppBar 예제 참고 자료
 *
 * ## FancyTopAppBarExampleUI (스크롤 반응형 Collapsing TopAppBar)
 * - 출처: https://efebu.medium.com/how-to-implement-a-fancy-topappbar-on-compose-multiplatform-5615ad02492b
 * 핵심 개념:
 * - TopAppBarScrollBehavior 3종: pinnedScrollBehavior(고정) · enterAlwaysScrollBehavior(즉시 등장/숨김) · exitUntilCollapsedScrollBehavior(Medium/Large 전용 축소)
 * - Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)을 Scaffold에 반드시 적용해야 스크롤과 TopAppBar가 연동됨
 * - scrollBehavior.state.heightOffset/heightOffsetLimit로 스크롤 진행률을 계산해 alpha/scale 등 커스텀 애니메이션에 활용
 */
