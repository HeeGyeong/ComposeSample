package com.example.composesample.presentation.example.component.ui.tab

/**
 * Tab 예제 참고 자료
 *
 * ## ResponsiveTabRowExampleUI (SubcomposeLayout 기반 반응형 TabRow)
 * - 출처: https://joebirch.co/android/building-a-responsive-tab-row-in-jetpack-compose/
 * 핵심 개념:
 * - TabRow(고정 너비, 균등 분할)와 ScrollableTabRow(콘텐츠 너비 + 스크롤) 중 어느 쪽을 써야 할지는 런타임 콘텐츠 길이에 따라 달라짐
 * - SubcomposeLayout으로 탭 콘텐츠를 무제한 너비(Constraints.Infinity)로 먼저 측정해 선호 너비(preferred width)를 계산하고, 고정 분할 너비(availableWidth / tab 수)와 비교해 초과하면 ScrollableTabRow, 아니면 TabRow를 렌더링
 * - 다국어 레이블, 서버에서 받은 동적 카테고리, 사용자 폰트 크기 설정처럼 런타임 전까지 길이를 알 수 없는 콘텐츠에 자동 대응
 * - 탭이 매우 많으면 추가 측정 단계 비용이 있지만, 일반적인 3-7개 탭 수준에서는 성능 영향 없음
 */
