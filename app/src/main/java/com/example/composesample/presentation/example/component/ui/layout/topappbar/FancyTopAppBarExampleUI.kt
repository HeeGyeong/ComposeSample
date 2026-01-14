package com.example.composesample.presentation.example.component.ui.layout.topappbar

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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Fancy TopAppBar Example UI
 *
 * 다양한 TopAppBarScrollBehavior를 시연하는 예제입니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FancyTopAppBarExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Example Selector
        ExampleSelectorCard(
            selectedExample = selectedExample,
            onExampleSelected = { selectedExample = it },
            onBackEvent = onBackEvent
        )

        // Show selected example
        when (selectedExample) {
            0 -> PinnedScrollBehaviorExample()
            1 -> EnterAlwaysScrollBehaviorExample()
            2 -> ExitUntilCollapsedMediumExample()
            3 -> ExitUntilCollapsedLargeExample()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExampleSelectorCard(
    selectedExample: Int,
    onExampleSelected: (Int) -> Unit,
    onBackEvent: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackEvent) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1976D2)
                    )
                }

                Text(
                    text = "📱 Fancy TopAppBar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExampleButton(
                text = "Pinned (항상 고정)",
                isSelected = selectedExample == 0,
                onClick = { onExampleSelected(0) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExampleButton(
                text = "Enter Always (즉시 나타남)",
                isSelected = selectedExample == 1,
                onClick = { onExampleSelected(1) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExampleButton(
                text = "Exit Until Collapsed (Medium)",
                isSelected = selectedExample == 2,
                onClick = { onExampleSelected(2) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExampleButton(
                text = "Exit Until Collapsed (Large)",
                isSelected = selectedExample == 3,
                onClick = { onExampleSelected(3) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExampleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = if (isSelected) Color(0xFF1976D2) else Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp),
        elevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 1. Pinned Scroll Behavior
 * - TopAppBar가 항상 고정됨
 * - 스크롤해도 위치 변하지 않음
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PinnedScrollBehaviorExample() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pinned TopAppBar",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    scrolledContainerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        DemoListContent(
            paddingValues = paddingValues,
            title = "Pinned Scroll Behavior",
            description = "TopAppBar가 항상 고정되어 있습니다.\n스크롤해도 위치가 변하지 않습니다."
        )
    }
}

/**
 * 2. Enter Always Scroll Behavior
 * - 아래로 스크롤 시 TopAppBar 즉시 나타남
 * - 위로 스크롤 시 TopAppBar 숨겨짐
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterAlwaysScrollBehaviorExample() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Enter Always TopAppBar",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    scrolledContainerColor = Color(0xFF2E7D32)
                )
            )
        }
    ) { paddingValues ->
        DemoListContent(
            paddingValues = paddingValues,
            title = "Enter Always Scroll Behavior",
            description = "위로 스크롤하면 TopAppBar가 숨겨지고,\n아래로 스크롤하면 즉시 나타납니다.\n(Quick Return 패턴)"
        )
    }
}

/**
 * 3. Exit Until Collapsed (Medium TopAppBar)
 * - 위로 스크롤 시 TopAppBar 축소됨
 * - Medium 크기 (112dp)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitUntilCollapsedMediumExample() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 스크롤 진행률 계산
    val scrollFraction by remember {
        derivedStateOf {
            val offset = scrollBehavior.state.heightOffset
            val limit = scrollBehavior.state.heightOffsetLimit
            if (limit != 0f) {
                (offset / limit).coerceIn(0f, 1f)
            } else 0f
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Medium Collapsing Toolbar",
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(1f - scrollFraction * 0.3f)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.scale(1f - scrollFraction * 0.2f)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.scale(1f - scrollFraction * 0.2f)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFFF6F00),
                    scrolledContainerColor = Color(0xFFE65100)
                )
            )
        }
    ) { paddingValues ->
        DemoListContent(
            paddingValues = paddingValues,
            title = "Exit Until Collapsed (Medium)",
            description = "위로 스크롤하면 TopAppBar가 축소되어\nSmall 크기로 변합니다.\n(Collapsing Toolbar 효과)"
        )
    }
}

/**
 * 4. Exit Until Collapsed (Large TopAppBar)
 * - 위로 스크롤 시 TopAppBar 크게 축소됨
 * - Large 크기 (152dp)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitUntilCollapsedLargeExample() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 스크롤 진행률 계산
    val scrollFraction by remember {
        derivedStateOf {
            val offset = scrollBehavior.state.heightOffset
            val limit = scrollBehavior.state.heightOffsetLimit
            if (limit != 0f) {
                (offset / limit).coerceIn(0f, 1f)
            } else 0f
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Large Collapsing Toolbar",
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(1f - scrollFraction * 0.5f)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier.scale(1f - scrollFraction * 0.3f)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Star",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.scale(1f - scrollFraction * 0.3f)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color(0xFF7B1FA2),
                    scrolledContainerColor = Color(0xFF6A1B9A)
                )
            )
        }
    ) { paddingValues ->
        DemoListContent(
            paddingValues = paddingValues,
            title = "Exit Until Collapsed (Large)",
            description = "Large TopAppBar가 위로 스크롤하면\nSmall 크기로 크게 축소됩니다.\n제목과 아이콘에 애니메이션 효과가 적용됩니다."
        )
    }
}

@Composable
private fun DemoListContent(
    paddingValues: PaddingValues,
    title: String,
    description: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Description Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp,
                backgroundColor = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📱 $title",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Demo Items
        items(30) { index ->
            DemoListItem(index = index + 1)
        }
    }
}

@Composable
private fun DemoListItem(index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when (index % 5) {
                            0 -> Color(0xFF1976D2)
                            1 -> Color(0xFF2E7D32)
                            2 -> Color(0xFFFF6F00)
                            3 -> Color(0xFF7B1FA2)
                            else -> Color(0xFFC62828)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "List Item $index",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "위아래로 스크롤하여 TopAppBar의 동작을 확인하세요",
                    fontSize = 13.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

