package com.example.composesample.presentation.example.component.ui.tab

/**
 * Building a Responsive Tab Row in Jetpack Compose
 *
 * 출처: https://joebirch.co/android/building-a-responsive-tab-row-in-jetpack-compose/
 *
 * === 개요 ===
 *
 * Jetpack Compose에서 탭을 구현할 때 TabRow와 ScrollableTabRow를 사용할 수 있습니다.
 * 하지만 콘텐츠의 길이에 따라 어떤 것을 사용해야 할지 런타임에만 알 수 있는 경우가 있습니다.
 *
 * ResponsiveTabRow는 이 문제를 해결하기 위해 SubcomposeLayout을 활용하여
 * 콘텐츠를 먼저 측정한 후 적절한 TabRow를 자동으로 선택합니다.
 *
 * === TabRow vs ScrollableTabRow ===
 *
 * **TabRow (고정 너비)**
 * - 각 탭이 동일한 너비를 가짐
 * - 전체 너비를 탭 수로 균등 분할
 * - 탭 수가 적고 레이블이 짧을 때 적합
 * - 모든 탭이 화면에 표시됨
 *
 * **ScrollableTabRow (스크롤 가능)**
 * - 각 탭이 콘텐츠에 맞는 너비를 가짐
 * - 수평 스크롤 가능
 * - 탭이 많거나 레이블이 길 때 적합
 * - 기본적으로 시작 정렬 (start alignment)
 *
 * === 문제점 ===
 *
 * 1. **고정 너비 문제**: TabRow 사용 시 텍스트가 잘리거나 여러 줄로 표시될 수 있음
 * 2. **불필요한 스크롤**: ScrollableTabRow 사용 시 공간이 충분해도 스크롤이 가능해짐
 * 3. **동적 콘텐츠**: 다국어 지원이나 동적 레이블의 경우 미리 예측 불가능
 * 4. **텍스트 크기**: 사용자 설정에 따라 텍스트 크기가 달라질 수 있음
 *
 * === SubcomposeLayout ===
 *
 * SubcomposeLayout은 Compose에서 콘텐츠를 단계적으로 측정하고 배치할 수 있는
 * 강력한 도구입니다. 이를 통해 콘텐츠를 먼저 측정한 후 그 결과를 바탕으로
 * 최적의 레이아웃을 결정할 수 있습니다.
 *
 * **동작 방식:**
 *
 * ```kotlin
 * SubcomposeLayout(modifier = modifier) { constraints ->
 *     // 1단계: 측정 (MEASURE_PHASE)
 *     val measurements = subcompose("MEASURE_PHASE") {
 *         // 측정할 콘텐츠
 *     }.map { it.measure(...) }
 *     
 *     // 2단계: 레이아웃 결정 (LAYOUT_PHASE)
 *     val content = subcompose("LAYOUT_PHASE") {
 *         // 측정 결과를 바탕으로 실제 UI 구성
 *     }.map { it.measure(constraints) }
 *     
 *     // 3단계: 배치
 *     layout(width, height) {
 *         content.forEach { it.placeRelative(0, 0) }
 *     }
 * }
 * ```
 *
 * === ResponsiveTabRow 구현 전략 ===
 *
 * **1. 콘텐츠 측정**
 *
 * 각 탭의 콘텐츠를 무제한 너비 제약으로 측정하여
 * 선호하는 너비(preferred width)를 계산합니다.
 *
 * ```kotlin
 * val tabPreferredWidths = mutableListOf<Int>()
 * subcompose("MEASURE_INDIVIDUAL_TABS") {
 *     tabTitles.forEachIndexed { index, title ->
 *         Box(modifier = Modifier.padding(horizontal = padding)) {
 *             TabContent(title = title, ...)
 *         }
 *     }
 * }.forEach { measurable ->
 *     tabPreferredWidths.add(
 *         measurable.measure(
 *             Constraints(minWidth = 0, maxWidth = Constraints.Infinity)
 *         ).width
 *     )
 * }
 * ```
 *
 * **2. 필요한 TabRow 타입 결정**
 *
 * 고정 너비 탭에서 각 탭이 가질 수 있는 너비를 계산하고,
 * 측정된 선호 너비와 비교하여 결정합니다.
 *
 * ```kotlin
 * val widthPerTabIfFixed = availableWidth / numberOfTabs
 * val useScrollable = tabPreferredWidths.any { preferredWidth -> 
 *     preferredWidth > widthPerTabIfFixed 
 * }
 * ```
 *
 * **3. 적절한 컴포넌트 렌더링**
 *
 * 측정 결과를 바탕으로 TabRow 또는 ScrollableTabRow를 렌더링합니다.
 *
 * ```kotlin
 * if (useScrollable) {
 *     ScrollableTabRow(...)
 * } else {
 *     TabRow(...)
 * }
 * ```
 *
 * === ResponsiveTabRow 주요 파라미터 ===
 *
 * **selectedTabIndex**
 * - 현재 선택된 탭의 인덱스
 * - 0부터 시작
 *
 * **tabTitles**
 * - 각 탭에 표시될 제목 리스트
 * - 탭의 개수는 이 리스트의 크기로 결정됨
 *
 * **onTabClick**
 * - 탭 클릭 시 호출되는 콜백
 * - 클릭된 탭의 인덱스를 파라미터로 받음
 *
 * **tabContentHorizontalPadding**
 * - 각 탭 콘텐츠의 수평 패딩
 * - 측정 시 이 패딩이 포함됨
 *
 * **containerColor / contentColor**
 * - TabRow의 배경색과 콘텐츠 색상
 *
 * **indicator**
 * - 선택된 탭을 표시하는 인디케이터
 * - TabPosition 리스트를 받아 렌더링
 *
 * **divider**
 * - TabRow 하단에 표시되는 구분선
 *
 * === 사용 예제 ===
 *
 * **기본 사용:**
 *
 * ```kotlin
 * var selectedTab by remember { mutableIntStateOf(0) }
 *
 * ResponsiveTabRow(
 *     selectedTabIndex = selectedTab,
 *     tabTitles = listOf("Posts", "Following", "Followers"),
 *     onTabClick = { selectedTab = it }
 * )
 * ```
 *
 * **배지와 함께 사용:**
 *
 * ```kotlin
 * ResponsiveTabRow(
 *     selectedTabIndex = selectedTab,
 *     tabTitles = listOf("Inbox", "Sent", "Drafts"),
 *     tabCounts = listOf(12, null, 3),
 *     onTabClick = { selectedTab = it }
 * )
 * ```
 *
 * **커스텀 스타일:**
 *
 * ```kotlin
 * ResponsiveTabRow(
 *     selectedTabIndex = selectedTab,
 *     tabTitles = listOf("Home", "Explore", "Notifications"),
 *     containerColor = MaterialTheme.colorScheme.primaryContainer,
 *     contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
 *     tabTextStyle = MaterialTheme.typography.titleMedium,
 *     onTabClick = { selectedTab = it }
 * )
 * ```
 *
 * === SubcomposeLayout 측정 최적화 ===
 *
 * **무제한 너비 제약 사용:**
 *
 * ```kotlin
 * Constraints(minWidth = 0, maxWidth = Constraints.Infinity)
 * ```
 *
 * 이를 통해 콘텐츠가 원하는 최대 너비를 얻을 수 있습니다.
 *
 * **패딩 포함 측정:**
 *
 * 탭 콘텐츠를 측정할 때 실제로 적용될 패딩을 포함하여
 * 정확한 너비를 계산합니다.
 *
 * === 실전 활용 시나리오 ===
 *
 * **1. 프로필 탭**
 * - "게시물", "팔로잉", "팔로워"
 * - 짧은 레이블: TabRow 사용
 *
 * **2. 설정 탭**
 * - "일반 설정", "알림 설정", "개인정보 보호 및 보안"
 * - 긴 레이블: ScrollableTabRow 자동 전환
 *
 * **3. 다국어 지원**
 * - 영어: "Home", "Settings"
 * - 독일어: "Startseite", "Einstellungen"
 * - 언어에 따라 자동 조정
 *
 * **4. 동적 카테고리**
 * - 서버에서 받아온 카테고리 이름
 * - 런타임에 길이가 결정됨
 *
 * **5. 접근성 고려**
 * - 사용자가 큰 텍스트 크기 설정
 * - 자동으로 ScrollableTabRow로 전환
 *
 * === 성능 고려사항 ===
 *
 * 1. **측정 비용**
 *    - SubcomposeLayout은 추가 측정 단계를 수반
 *    - 탭이 매우 많은 경우 성능 영향 가능
 *    - 일반적인 사용 케이스(3-7개 탭)에서는 문제없음
 *
 * 2. **리컴포지션**
 *    - 탭 제목이 변경되면 재측정 발생
 *    - remember로 안정적인 리스트 사용 권장
 *
 * 3. **측정 캐싱**
 *    - 동일한 콘텐츠는 재측정하지 않음
 *    - Compose가 자동으로 최적화
 *
 * === 커스터마이징 ===
 *
 * **인디케이터 커스터마이징:**
 *
 * ```kotlin
 * ResponsiveTabRow(
 *     indicator = { tabPositions ->
 *         if (tabPositions.isNotEmpty()) {
 *             Box(
 *                 Modifier
 *                     .tabIndicatorOffset(tabPositions[selectedTabIndex])
 *                     .height(4.dp)
 *                     .background(
 *                         color = Color.Blue,
 *                         shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
 *                     )
 *             )
 *         }
 *     }
 * )
 * ```
 *
 * **구분선 커스터마이징:**
 *
 * ```kotlin
 * ResponsiveTabRow(
 *     divider = {
 *         HorizontalDivider(
 *             thickness = 2.dp,
 *             color = MaterialTheme.colorScheme.outline
 *         )
 *     }
 * )
 * ```
 *
 * === 주의사항 ===
 *
 * 1. SubcomposeLayout은 측정 단계가 추가되므로 매우 많은 탭에는 비효율적
 * 2. 탭 제목 리스트는 안정적이어야 함 (remember 사용)
 * 3. 동적으로 변경되는 카운트는 별도 상태로 관리
 * 4. 접근성을 위해 적절한 contentDescription 제공
 *
 * === 다른 활용 사례 ===
 *
 * SubcomposeLayout을 활용한 다른 반응형 컴포넌트:
 *
 * 1. **ResponsiveGrid** - 아이템 크기에 따라 열 수 자동 조정
 * 2. **ResponsiveBottomBar** - 아이템 수에 따라 레이아웃 변경
 * 3. **AdaptiveDialog** - 콘텐츠 크기에 따라 다이얼로그 크기 조정
 * 4. **FlexibleToolbar** - 콘텐츠에 따라 툴바 레이아웃 변경
 *
 * === 요약 ===
 *
 * ResponsiveTabRow는 SubcomposeLayout을 활용하여 런타임에
 * 최적의 TabRow 타입을 자동으로 선택합니다.
 *
 * 핵심 포인트:
 * - SubcomposeLayout으로 콘텐츠 먼저 측정
 * - 측정 결과로 TabRow vs ScrollableTabRow 결정
 * - 다국어, 동적 콘텐츠, 접근성 자동 대응
 * - 개발자는 콘텐츠만 제공하면 됨
 * - 추가 로직 없이 항상 최적의 UI 제공
 */

object ResponsiveTabRowGuide {
    const val GUIDE_INFO = """
        Responsive TabRow - SubcomposeLayout
        
        문제점:
        - TabRow: 텍스트 잘림 가능
        - ScrollableTabRow: 불필요한 스크롤
        - 동적 콘텐츠 대응 어려움
        
        해결책:
        - SubcomposeLayout으로 측정
        - 자동으로 적절한 TabRow 선택
        - 런타임 콘텐츠 길이 대응
        
        핵심 개념:
        - 1단계: 콘텐츠 측정
        - 2단계: TabRow 타입 결정
        - 3단계: 적절한 컴포넌트 렌더링
        
        장점:
        - 다국어 자동 대응
        - 접근성 지원
        - 동적 콘텐츠 처리
        - 개발자 편의성
        
        출처: https://joebirch.co/android/building-a-responsive-tab-row-in-jetpack-compose/
    """
}
