package com.example.composesample.presentation.example.component.ui.accessibility

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Large Content Viewer Example UI
 *
 * iOS의 Large Content Viewer를 Compose로 구현하고,
 * 키보드 내비게이션, 스크린 리더(TalkBack), Voice Access 지원을 추가합니다.
 */
private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val description: String
)

private val navItems = listOf(
    NavItem(Icons.Default.Home, "Home", "홈 화면으로 이동"),
    NavItem(Icons.Default.Search, "Search", "검색 화면으로 이동"),
    NavItem(Icons.Default.Favorite, "Favorites", "즐겨찾기 목록"),
    NavItem(Icons.Default.Notifications, "Alerts", "알림 목록"),
    NavItem(Icons.Default.AccountCircle, "Profile", "프로필 설정"),
)

private val toolbarItems = listOf(
    NavItem(Icons.Default.Star, "Star", "즐겨찾기 추가"),
    NavItem(Icons.Default.Search, "Search", "검색"),
    NavItem(Icons.Default.Settings, "Settings", "설정"),
    NavItem(Icons.Default.Notifications, "Notify", "알림"),
)

@Composable
fun LargeContentViewerExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabItem("Long-Press", selectedExample == 0, { selectedExample = 0 }, Modifier.weight(1f))
            TabItem("Keyboard", selectedExample == 1, { selectedExample = 1 }, Modifier.weight(1f))
            TabItem("TalkBack", selectedExample == 2, { selectedExample = 2 }, Modifier.weight(1f))
            TabItem("통합", selectedExample == 3, { selectedExample = 3 }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> LongPressPreviewDemo()
            1 -> KeyboardNavigationDemo()
            2 -> ScreenReaderDemo()
            3 -> IntegratedDemo()
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Large Content Viewer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Navigation Support for Accessibility",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@Composable
private fun LongPressPreviewDemo() {
    var previewedItem by remember { mutableStateOf<NavItem?>(null) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Long-Press 프리뷰",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "iOS Large Content Viewer처럼, 아이콘을 길게 누르면 " +
                                "확대된 프리뷰가 표시됩니다.\n" +
                                "detectTapGestures의 onLongPress로 구현합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "아이콘을 길게 눌러보세요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = previewedItem != null,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        previewedItem?.let { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            navItems.forEachIndexed { index, item ->
                                Column(
                                    modifier = Modifier
                                        .pointerInput(item.label) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    previewedItem = item
                                                },
                                                onPress = {
                                                    awaitRelease()
                                                    previewedItem = null
                                                },
                                                onTap = {
                                                    selectedIndex = index
                                                }
                                            )
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (index == selectedIndex) Color(0xFF1976D2) else Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        color = if (index == selectedIndex) Color(0xFF1976D2) else Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardNavigationDemo() {
    var previewedItem by remember { mutableStateOf<NavItem?>(null) }
    var focusedItem by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val viewConfiguration = LocalViewConfiguration.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "키보드 내비게이션 지원",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "키보드 사용자는 long-press가 불가능하므로 onFocusChanged로 대체합니다.\n" +
                                "포커스가 longPressTimeoutMillis 이상 유지되면 프리뷰를 표시합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Tab 키로 포커스를 이동해보세요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "포커스가 ${viewConfiguration.longPressTimeoutMillis}ms 이상 유지되면 프리뷰가 표시됩니다.",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = previewedItem != null,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        previewedItem?.let { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "⌨️ Keyboard Focus",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            navItems.forEach { item ->
                                var focusJob by remember { mutableStateOf<Job?>(null) }
                                val isFocused = focusedItem == item.label

                                Column(
                                    modifier = Modifier
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                focusedItem = item.label
                                                focusJob = scope.launch {
                                                    delay(viewConfiguration.longPressTimeoutMillis)
                                                    previewedItem = item
                                                }
                                            } else {
                                                if (focusedItem == item.label) {
                                                    focusedItem = null
                                                }
                                                focusJob?.cancel()
                                                focusJob = null
                                                previewedItem = null
                                            }
                                        }
                                        .focusable()
                                        .then(
                                            if (isFocused) {
                                                Modifier.border(
                                                    2.dp,
                                                    Color(0xFF4CAF50),
                                                    RoundedCornerShape(8.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (isFocused) Color(0xFF4CAF50) else Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        color = if (isFocused) Color(0xFF4CAF50) else Color(0xFF757575)
                                    )
                                }

                                DisposableEffect(item.label) {
                                    onDispose {
                                        focusJob?.cancel()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (focusedItem != null) Color(0xFF4CAF50)
                                        else Color(0xFFBDBDBD),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (focusedItem != null) {
                                    "포커스: $focusedItem  |  프리뷰: ${previewedItem?.label ?: "대기 중…"}"
                                } else {
                                    "포커스된 아이템 없음"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFF616161)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 핵심 포인트",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val points = listOf(
                        "viewConfiguration.longPressTimeoutMillis로 시스템 long-press 시간 사용",
                        "포커스 해제 시 Job.cancel()로 코루틴 누수 방지",
                        "isFocused 상태에 따라 포커스 링(border) 표시",
                    )
                    points.forEach { point ->
                        Text(
                            text = "• $point",
                            fontSize = 12.sp,
                            color = Color(0xFF795548),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenReaderDemo() {
    var previewedItem by remember { mutableStateOf<NavItem?>(null) }
    var actionLog by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "스크린 리더 (TalkBack) 지원",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "semantics { customActions }를 사용하여 " +
                                "TalkBack의 Actions 메뉴에 'Preview item'을 추가합니다.\n" +
                                "스크린 리더 사용자가 의도적으로 트리거하므로 딜레이 없이 바로 표시합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Custom Accessibility Action 데모",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )
                    Text(
                        text = "아이콘을 탭하면 Custom Action이 트리거됩니다. (TalkBack 시뮬레이션)",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = previewedItem != null,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        previewedItem?.let { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0)),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "🔊 TalkBack Action",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            navItems.forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .semantics {
                                            customActions = listOf(
                                                CustomAccessibilityAction(
                                                    label = "Preview ${item.label}",
                                                    action = {
                                                        scope.launch {
                                                            previewedItem = item
                                                        }
                                                        actionLog = actionLog + "Preview ${item.label}"
                                                        true
                                                    }
                                                )
                                            )
                                        }
                                        .clickable {
                                            previewedItem = if (previewedItem == item) null else item
                                            actionLog = actionLog + "Custom Action: Preview ${item.label}"
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.description,
                                        modifier = Modifier.size(24.dp),
                                        tint = if (previewedItem == item) Color(0xFF9C27B0) else Color(0xFF757575)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        color = if (previewedItem == item) Color(0xFF9C27B0) else Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }

                    if (actionLog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📋 Action Log",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B1FA2)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                actionLog.takeLast(5).forEach { log ->
                                    Text(
                                        text = "→ $log",
                                        fontSize = 11.sp,
                                        color = Color(0xFF616161)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegratedDemo() {
    var previewedItem by remember { mutableStateOf<NavItem?>(null) }
    var previewSource by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var focusedItem by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val viewConfiguration = LocalViewConfiguration.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "통합 데모: 모든 입력 방식 지원",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Long-Press(터치), onFocusChanged(키보드), semantics(스크린 리더), " +
                                "Voice Access 모두를 하나의 컴포넌트에 통합합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Bottom Navigation Bar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InputMethodBadge("👆 Touch", Color(0xFF1976D2))
                        InputMethodBadge("⌨️ Keyboard", Color(0xFF4CAF50))
                        InputMethodBadge("🔊 TalkBack", Color(0xFF9C27B0))
                        InputMethodBadge("🎤 Voice", Color(0xFFFF9800))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = previewedItem != null,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        previewedItem?.let { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val bgColor = when (previewSource) {
                                    "touch" -> Color(0xFF1976D2)
                                    "keyboard" -> Color(0xFF4CAF50)
                                    "talkback" -> Color(0xFF9C27B0)
                                    else -> Color(0xFF616161)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = bgColor),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = when (previewSource) {
                                                "touch" -> "👆 Long-Press"
                                                "keyboard" -> "⌨️ Keyboard Focus"
                                                "talkback" -> "🔊 TalkBack Action"
                                                else -> ""
                                            },
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            navItems.forEachIndexed { index, item ->
                                var focusJob by remember { mutableStateOf<Job?>(null) }
                                val isFocused = focusedItem == item.label

                                Column(
                                    modifier = Modifier
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                focusedItem = item.label
                                                focusJob = scope.launch {
                                                    delay(viewConfiguration.longPressTimeoutMillis)
                                                    previewedItem = item
                                                    previewSource = "keyboard"
                                                }
                                            } else {
                                                if (focusedItem == item.label) {
                                                    focusedItem = null
                                                }
                                                focusJob?.cancel()
                                                focusJob = null
                                                if (previewSource == "keyboard") {
                                                    previewedItem = null
                                                    previewSource = ""
                                                }
                                            }
                                        }
                                        .focusable()
                                        .semantics {
                                            customActions = listOf(
                                                CustomAccessibilityAction(
                                                    label = "Preview ${item.label}",
                                                    action = {
                                                        scope.launch {
                                                            previewedItem = item
                                                            previewSource = "talkback"
                                                        }
                                                        true
                                                    }
                                                )
                                            )
                                        }
                                        .pointerInput(item.label) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    previewedItem = item
                                                    previewSource = "touch"
                                                },
                                                onPress = {
                                                    awaitRelease()
                                                    if (previewSource == "touch") {
                                                        previewedItem = null
                                                        previewSource = ""
                                                    }
                                                },
                                                onTap = {
                                                    selectedIndex = index
                                                }
                                            )
                                        }
                                        .then(
                                            if (isFocused) {
                                                Modifier.border(
                                                    2.dp,
                                                    Color(0xFF4CAF50),
                                                    RoundedCornerShape(8.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.description,
                                        modifier = Modifier.size(24.dp),
                                        tint = when {
                                            index == selectedIndex -> Color(0xFF1976D2)
                                            isFocused -> Color(0xFF4CAF50)
                                            else -> Color(0xFF757575)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        color = when {
                                            index == selectedIndex -> Color(0xFF1976D2)
                                            isFocused -> Color(0xFF4CAF50)
                                            else -> Color(0xFF757575)
                                        }
                                    )
                                }

                                DisposableEffect(item.label) {
                                    onDispose {
                                        focusJob?.cancel()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            ToolbarDemo()
        }

        item {
            AccessibilityComparisonCard()
        }
    }
}

@Composable
private fun InputMethodBadge(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color
    )
}

@Composable
private fun ToolbarDemo() {
    var previewedItem by remember { mutableStateOf<NavItem?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Toolbar 아이콘 프리뷰",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "네비게이션 바뿐 아니라 Toolbar의 작은 아이콘에도 적용할 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = previewedItem != null,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                previewedItem?.let { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF455A64)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.label,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = item.description,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My App",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        toolbarItems.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .pointerInput(item.label) {
                                        detectTapGestures(
                                            onLongPress = {
                                                previewedItem = item
                                            },
                                            onPress = {
                                                awaitRelease()
                                                previewedItem = null
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp),
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessibilityComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "접근성 지원 방식 비교",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val methods = listOf(
                Triple("👆 Touch", "pointerInput + detectTapGestures(onLongPress)", "길게 누르는 동안 프리뷰, 놓으면 해제"),
                Triple("⌨️ Keyboard", "onFocusChanged + delay", "포커스가 longPressTimeout 이상 유지 시 프리뷰"),
                Triple("🔊 TalkBack", "semantics { customActions }", "Actions 메뉴에서 선택, 딜레이 없이 바로 표시"),
                Triple("🎤 Voice", "자동 지원 (추가 코드 불필요)", "\"Long press [item]\" 명령으로 자동 트리거"),
            )

            methods.forEach { (title, api, behavior) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = api,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = behavior,
                            fontSize = 11.sp,
                            color = Color(0xFF757575),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LargeContentViewerExampleUIPreview() {
    LargeContentViewerExampleUI(onBackEvent = {})
}
