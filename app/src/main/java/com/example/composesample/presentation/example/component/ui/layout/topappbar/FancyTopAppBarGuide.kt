package com.example.composesample.presentation.example.component.ui.layout.topappbar

/**
 * How to Implement a Fancy TopAppBar on Compose Multiplatform
 *
 * 출처: https://efebu.medium.com/how-to-implement-a-fancy-topappbar-on-compose-multiplatform-5615ad02492b
 *
 * === 개요 ===
 *
 * Fancy TopAppBar는 Google Photos, Gmail 등의 앱에서 볼 수 있는
 * 스크롤에 반응하는 고급 TopAppBar입니다.
 *
 * Material 3에서는 다양한 TopAppBarScrollBehavior를 제공하여
 * Collapsing Toolbar와 같은 효과를 쉽게 구현할 수 있습니다.
 *
 * === TopAppBarScrollBehavior 종류 ===
 *
 * 1. **pinnedScrollBehavior()**
 *    - TopAppBar가 항상 고정됨
 *    - 스크롤해도 위치 변하지 않음
 *    - 가장 기본적인 동작
 *
 * 2. **enterAlwaysScrollBehavior()**
 *    - 아래로 스크롤 시 TopAppBar 즉시 나타남
 *    - 위로 스크롤 시 TopAppBar 숨겨짐
 *    - Quick return 패턴
 *
 * 3. **exitUntilCollapsedScrollBehavior()**
 *    - 위로 스크롤 시 TopAppBar 축소됨
 *    - Medium/Large TopAppBar에서만 사용 가능
 *    - Collapsing Toolbar 효과
 *
 * === TopAppBar 크기 종류 ===
 *
 * 1. **TopAppBar (Small)**
 *    - 높이: 64dp
 *    - 제목이 왼쪽에 위치
 *    - 가장 일반적인 형태
 *
 * 2. **MediumTopAppBar**
 *    - 높이: 112dp
 *    - 제목이 하단에 위치
 *    - exitUntilCollapsed 사용 가능
 *
 * 3. **LargeTopAppBar**
 *    - 높이: 152dp
 *    - 제목이 하단에 크게 표시
 *    - exitUntilCollapsed 사용 가능
 *
 * === 구현 방법 ===
 *
 * ```kotlin
 * @OptIn(ExperimentalMaterial3Api::class)
 * @Composable
 * fun FancyTopAppBarScreen() {
 *     val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
 *
 *     Scaffold(
 *         modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
 *         topBar = {
 *             TopAppBar(
 *                 title = { Text("Title") },
 *                 scrollBehavior = scrollBehavior,
 *                 actions = {
 *                     IconButton(onClick = { }) {
 *                         Icon(Icons.Default.Search, contentDescription = null)
 *                     }
 *                 }
 *             )
 *         }
 *     ) { paddingValues ->
 *         LazyColumn(contentPadding = paddingValues) {
 *             // Content
 *         }
 *     }
 * }
 * ```
 *
 * === nestedScroll 중요성 ===
 *
 * `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`은 필수입니다.
 * - Scaffold에 적용해야 함
 * - 스크롤 이벤트를 TopAppBar와 연결
 * - 없으면 scrollBehavior가 작동하지 않음
 *
 * === TopAppBar 색상 커스터마이징 ===
 *
 * ```kotlin
 * TopAppBar(
 *     title = { Text("Title") },
 *     scrollBehavior = scrollBehavior,
 *     colors = TopAppBarDefaults.topAppBarColors(
 *         containerColor = MaterialTheme.colorScheme.primaryContainer,
 *         scrolledContainerColor = MaterialTheme.colorScheme.primary,
 *         titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
 *     )
 * )
 * ```
 *
 * === Collapsing Toolbar 구현 ===
 *
 * LargeTopAppBar + exitUntilCollapsedScrollBehavior:
 *
 * ```kotlin
 * val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
 *
 * LargeTopAppBar(
 *     title = {
 *         Text(
 *             "Large Title",
 *             maxLines = 1,
 *             overflow = TextOverflow.Ellipsis
 *         )
 *     },
 *     scrollBehavior = scrollBehavior
 * )
 * ```
 *
 * === 스크롤 상태 접근 ===
 *
 * scrollBehavior를 통해 현재 스크롤 상태를 알 수 있습니다:
 *
 * ```kotlin
 * val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
 *
 * // 현재 오프셋
 * val currentOffset = scrollBehavior.state.heightOffset
 *
 * // 최대 오프셋 (TopAppBar 높이)
 * val maxOffset = scrollBehavior.state.heightOffsetLimit
 *
 * // 스크롤 진행률 (0.0 ~ 1.0)
 * val fraction = if (maxOffset != 0f) {
 *     (currentOffset / maxOffset).coerceIn(0f, 1f)
 * } else 0f
 * ```
 *
 * === 애니메이션 효과 추가 ===
 *
 * 스크롤 진행률을 이용한 커스텀 애니메이션:
 *
 * ```kotlin
 * val scrollFraction = // calculate fraction
 *
 * TopAppBar(
 *     title = {
 *         Text(
 *             "Title",
 *             modifier = Modifier.alpha(1f - scrollFraction)
 *         )
 *     },
 *     actions = {
 *         IconButton(
 *             onClick = { },
 *             modifier = Modifier.scale(1f - scrollFraction * 0.3f)
 *         ) {
 *             Icon(Icons.Default.Search, contentDescription = null)
 *         }
 *     },
 *     scrollBehavior = scrollBehavior
 * )
 * ```
 *
 * === 실전 활용 패턴 ===
 *
 * 1. **Profile Screen**
 *    - LargeTopAppBar + exitUntilCollapsed
 *    - 프로필 이미지가 스크롤 시 축소
 *
 * 2. **Search Screen**
 *    - TopAppBar + enterAlways
 *    - 검색창이 스크롤 시 빠르게 나타남
 *
 * 3. **List Screen**
 *    - MediumTopAppBar + exitUntilCollapsed
 *    - 제목이 크게 표시되다가 축소
 *
 * 4. **Detail Screen**
 *    - TopAppBar + pinned
 *    - 항상 고정된 TopAppBar
 *
 * === 주의사항 ===
 *
 * 1. nestedScroll Modifier는 Scaffold에 적용
 * 2. scrollBehavior는 TopAppBar에 전달
 * 3. LazyColumn의 contentPadding에 paddingValues 적용
 * 4. exitUntilCollapsed는 Medium/Large TopAppBar에서만 사용
 * 5. Material3 ExperimentalMaterial3Api 어노테이션 필요
 *
 * === 요약 ===
 *
 * Fancy TopAppBar는 Material 3의 TopAppBarScrollBehavior를 활용하여
 * 다양한 스크롤 효과를 쉽게 구현할 수 있습니다.
 *
 * 핵심 포인트:
 * - TopAppBarScrollBehavior 선택 (pinned, enterAlways, exitUntilCollapsed)
 * - nestedScroll Modifier 적용
 * - TopAppBar 크기 선택 (Small, Medium, Large)
 * - 스크롤 진행률을 이용한 커스텀 애니메이션
 */

object FancyTopAppBarGuide {
    const val GUIDE_INFO = """
        Fancy TopAppBar - Collapsing Toolbar
        
        TopAppBarScrollBehavior:
        - pinnedScrollBehavior: 항상 고정
        - enterAlwaysScrollBehavior: 즉시 나타남
        - exitUntilCollapsedScrollBehavior: 축소됨
        
        TopAppBar 크기:
        - TopAppBar: 64dp (Small)
        - MediumTopAppBar: 112dp
        - LargeTopAppBar: 152dp
        
        필수 설정:
        - nestedScroll Modifier 적용
        - scrollBehavior 전달
        - Material3 ExperimentalMaterial3Api
        
        출처: https://efebu.medium.com/how-to-implement-a-fancy-topappbar-on-compose-multiplatform-5615ad02492b
    """
}

