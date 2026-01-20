package com.example.composesample.presentation.example.component.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResponsiveTabRowExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ShortLabelsCard() }
            item { LongLabelsCard() }
            item { MixedLabelsCard() }
            item { WithBadgesCard() }
            item { ComparisonCard() }
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }

            Column {
                Text(
                    text = "📱 Responsive TabRow",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SubcomposeLayout으로 자동 탭 선택",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun ShortLabelsCard() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Following", "Followers")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "짧은 레이블 (TabRow 사용)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "각 탭이 균등한 너비를 가지며 화면을 채웁니다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResponsiveTabRow(
                selectedTabIndex = selectedTab,
                tabTitles = tabs,
                onTabClick = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tabs[selectedTab]} 콘텐츠",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun LongLabelsCard() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("일반 설정", "알림 설정", "개인정보 보호 및 보안", "계정 관리")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "긴 레이블 (ScrollableTabRow 사용)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "각 탭이 콘텐츠 크기만큼 너비를 가지며 스크롤 가능합니다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResponsiveTabRow(
                selectedTabIndex = selectedTab,
                tabTitles = tabs,
                onTabClick = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tabs[selectedTab]} 화면",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun MixedLabelsCard() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("홈", "탐색", "알림", "메시지", "프로필 설정 및 관리")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "혼합 레이블 (자동 선택)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "마지막 탭이 길어서 자동으로 ScrollableTabRow로 전환됩니다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResponsiveTabRow(
                selectedTabIndex = selectedTab,
                tabTitles = tabs,
                containerColor = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1976D2),
                onTabClick = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tabs[selectedTab]} 탭",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
private fun WithBadgesCard() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("받은편지함", "보낸편지함", "임시보관함")
    val counts = listOf(12, null, 3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "배지와 함께 사용",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "탭에 읽지 않은 개수 배지를 표시할 수 있습니다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResponsiveTabRow(
                selectedTabIndex = selectedTab,
                tabTitles = tabs,
                tabCounts = counts,
                onTabClick = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tabs[selectedTab],
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    counts[selectedTab]?.let { count ->
                        Text(
                            text = "${count}개의 메시지",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard() {
    var selectedTab1 by remember { mutableIntStateOf(0) }
    var selectedTab2 by remember { mutableIntStateOf(0) }
    var selectedTab3 by remember { mutableIntStateOf(0) }

    val longTabs = listOf("개인정보 보호 설정", "알림 및 권한 관리", "보안 및 계정 설정")
    val shortTabs = listOf("홈", "탐색", "알림")
    val mixedTabs = listOf("프로필", "설정", "알림 관리")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "1. 일반 TabRow (긴 텍스트 → 잘림)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TabRow(
                selectedTabIndex = selectedTab1,
                backgroundColor = Color(0xFFFFF3E0),
                contentColor = Color(0xFFFF6F00)
            ) {
                longTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab1 == index,
                        onClick = { selectedTab1 = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. ScrollableTabRow (짧은 텍스트 → 불필요한 스크롤)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ScrollableTabRow(
                selectedTabIndex = selectedTab2,
                backgroundColor = Color(0xFFF3E5F5),
                contentColor = Color(0xFF7B1FA2),
                edgePadding = 0.dp
            ) {
                shortTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab2 == index,
                        onClick = { selectedTab2 = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "3. ResponsiveTabRow (중간 길이 → 자동 최적화)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ResponsiveTabRow(
                selectedTabIndex = selectedTab3,
                tabTitles = mixedTabs,
                containerColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32),
                onTabClick = { selectedTab3 = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ResponsiveTabRow는 콘텐츠를 측정하여 자동으로 최적의 TabRow를 선택합니다",
                fontSize = 11.sp,
                color = Color(0xFF666666),
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * ResponsiveTabRow - SubcomposeLayout을 활용한 반응형 탭
 */
@Composable
fun ResponsiveTabRow(
    selectedTabIndex: Int,
    tabTitles: List<String>,
    modifier: Modifier = Modifier,
    onTabClick: (Int) -> Unit,
    containerColor: Color = Color.White,
    contentColor: Color = Color(0xFF1976D2),
    indicator: @Composable (tabPositions: List<androidx.compose.material.TabPosition>) -> Unit = { tabPositions ->
        if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = contentColor
            )
        }
    },
    divider: @Composable () -> Unit = {},
    tabTextStyle: TextStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    tabContentHorizontalPadding: Dp = 16.dp,
    tabCounts: List<Int?>? = null
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val availableWidthPx = constraints.maxWidth
        val numberOfTabs = tabTitles.size

        val tabPreferredWidths = mutableListOf<Int>()
        subcompose("MEASURE_INDIVIDUAL_TABS") {
            tabTitles.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier.padding(horizontal = tabContentHorizontalPadding),
                    contentAlignment = Alignment.Center
                ) {
                    TabContent(
                        title = title,
                        count = tabCounts?.getOrNull(index),
                        style = tabTextStyle
                    )
                }
            }
        }.forEach { measurable ->
            tabPreferredWidths.add(
                measurable.measure(
                    Constraints(minWidth = 0, maxWidth = Constraints.Infinity)
                ).width
            )
        }

        val widthPerTabIfFixed = availableWidthPx / numberOfTabs
        val useScrollable = tabPreferredWidths.any { preferredWidth ->
            preferredWidth > widthPerTabIfFixed
        }

        val layoutContent = @Composable {
            if (useScrollable) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = containerColor,
                    contentColor = contentColor,
                    edgePadding = 0.dp,
                    indicator = indicator,
                    divider = divider,
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onTabClick(index) },
                            text = {
                                TabContent(
                                    title = title,
                                    count = tabCounts?.getOrNull(index),
                                    style = tabTextStyle
                                )
                            }
                        )
                    }
                }
            } else {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = containerColor,
                    contentColor = contentColor,
                    indicator = indicator,
                    divider = divider
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onTabClick(index) },
                            text = {
                                TabContent(
                                    title = title,
                                    count = tabCounts?.getOrNull(index),
                                    style = tabTextStyle
                                )
                            }
                        )
                    }
                }
            }
        }

        val placeables = subcompose("LAYOUT_ACTUAL_ROW", layoutContent)
            .map { it.measure(constraints) }

        val mainPlaceable = placeables.firstOrNull()
        if (mainPlaceable != null) {
            layout(mainPlaceable.width, mainPlaceable.height) {
                mainPlaceable.placeRelative(0, 0)
            }
        } else {
            layout(0, 0) {}
        }
    }
}

@Composable
private fun TabContent(
    title: String,
    count: Int?,
    style: TextStyle
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = style,
            maxLines = 1
        )
        if (count != null && count > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(
                        color = Color(0xFFE91E63),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}
