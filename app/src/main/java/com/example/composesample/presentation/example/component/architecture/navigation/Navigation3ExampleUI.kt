package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun Navigation3ExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Navigation 3",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { BasicNavigationDemoCard() }
            item { BackStackVisualizationCard() }
            item { Nav2VsNav3ComparisonCard() }
            item { MigrationGuideCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧭 Navigation 3 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Compose State 기반의 새로운 네비게이션 라이브러리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow("📦 NavKey", "화면을 나타내는 식별자 (data class/object)")
            InfoRow("🖼️ NavDisplay", "백스택을 관찰하여 화면 표시")
            InfoRow("📚 Back Stack", "직접 관리하는 화면 스택 (List<NavKey>)")
            InfoRow("🎯 Single Source", "네비게이션 상태를 직접 소유")

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 핵심 철학",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Building Blocks Approach\n작고 분리된 API들을 조합하여\n복잡한 기능을 구현합니다",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = description,
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
    }
}

// Simple NavKey implementation for demo
sealed class DemoScreen {
    object Home : DemoScreen()
    data class Profile(val userId: String) : DemoScreen()
    object Settings : DemoScreen()
    data class Detail(val itemId: String) : DemoScreen()
}

@Composable
private fun BasicNavigationDemoCard() {
    var backStack by remember { mutableStateOf(listOf<DemoScreen>(DemoScreen.Home)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎮 기본 네비게이션",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "NavKey와 NavDisplay 개념 시뮬레이션",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current Screen Display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                elevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    AnimatedContent(
                        targetState = backStack.last(),
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(300)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(300)
                            )
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is DemoScreen.Home -> ScreenContent(
                                title = "🏠 Home Screen",
                                icon = Icons.Default.Home,
                                color = Color(0xFF1976D2)
                            )

                            is DemoScreen.Profile -> ScreenContent(
                                title = "👤 Profile Screen",
                                subtitle = "User: ${screen.userId}",
                                icon = Icons.Default.Person,
                                color = Color(0xFF7B1FA2)
                            )

                            is DemoScreen.Settings -> ScreenContent(
                                title = "⚙️ Settings Screen",
                                icon = Icons.Default.Settings,
                                color = Color(0xFFE65100)
                            )

                            is DemoScreen.Detail -> ScreenContent(
                                title = "📄 Detail Screen",
                                subtitle = "Item: ${screen.itemId}",
                                icon = Icons.Default.ArrowForward,
                                color = Color(0xFF00897B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Text(
                "Navigate to:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationButton(
                    text = "Profile",
                    icon = Icons.Default.Person,
                    color = Color(0xFF7B1FA2),
                    onClick = {
                        backStack = backStack + DemoScreen.Profile("user_${System.currentTimeMillis() % 100}")
                    },
                    modifier = Modifier.weight(1f)
                )

                NavigationButton(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    color = Color(0xFFE65100),
                    onClick = {
                        backStack = backStack + DemoScreen.Settings
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigationButton(
                    text = "Detail",
                    icon = Icons.Default.ArrowForward,
                    color = Color(0xFF00897B),
                    onClick = {
                        backStack = backStack + DemoScreen.Detail("item_${backStack.size}")
                    },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (backStack.size > 1) {
                            backStack = backStack.dropLast(1)
                        }
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back", fontSize = 13.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E7D32).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📝 Back Stack: ${backStack.size} screens",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = backStack.joinToString(" → ") { screen ->
                            when (screen) {
                                is DemoScreen.Home -> "Home"
                                is DemoScreen.Profile -> "Profile(${screen.userId})"
                                is DemoScreen.Settings -> "Settings"
                                is DemoScreen.Detail -> "Detail(${screen.itemId})"
                            }
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun ScreenContent(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun BackStackVisualizationCard() {
    var backStack by remember {
        mutableStateOf(
            listOf(
                DemoScreen.Home,
                DemoScreen.Profile("user_1"),
                DemoScreen.Settings
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📚 Back Stack 시각화",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "List<NavKey> 형태로 직접 관리하는 백스택",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stack visualization
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                backStack.reversed().forEachIndexed { reverseIndex, screen ->
                    val index = backStack.size - 1 - reverseIndex
                    val isTop = index == backStack.size - 1

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isTop) {
                                // Pop to this screen
                                backStack = backStack.take(index + 1)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTop) Color(0xFFE65100).copy(alpha = 0.2f) else Color.White,
                        elevation = if (isTop) 4.dp else 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isTop) Color(0xFFE65100)
                                            else Color(0xFFBDBDBD)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = when (screen) {
                                            is DemoScreen.Home -> "Home Screen"
                                            is DemoScreen.Profile -> "Profile Screen"
                                            is DemoScreen.Settings -> "Settings Screen"
                                            is DemoScreen.Detail -> "Detail Screen"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isTop) Color(0xFFE65100) else Color(0xFF666666)
                                    )

                                    when (screen) {
                                        is DemoScreen.Profile -> Text(
                                            "userId: ${screen.userId}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF999999)
                                        )

                                        is DemoScreen.Detail -> Text(
                                            "itemId: ${screen.itemId}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF999999)
                                        )

                                        else -> {}
                                    }
                                }
                            }

                            if (isTop) {
                                Text(
                                    "TOP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFE65100).copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            } else {
                                IconButton(
                                    onClick = { backStack = backStack.filterIndexed { i, _ -> i != index } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color(0xFFBDBDBD),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newScreen = listOf(
                            DemoScreen.Profile("user_${System.currentTimeMillis() % 100}"),
                            DemoScreen.Detail("item_${backStack.size}"),
                            DemoScreen.Settings
                        ).random()
                        backStack = backStack + newScreen
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Push", fontSize = 13.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        if (backStack.size > 1) {
                            backStack = backStack.dropLast(1)
                        }
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pop", fontSize = 13.sp, color = Color.White)
                }

                Button(
                    onClick = { backStack = listOf(DemoScreen.Home) },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF757575)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear", fontSize = 13.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 완전한 제어",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• backStack을 List로 직접 관리\n• 원하는 대로 조작 가능 (add, remove, replace)\n• Single Source of Truth 유지",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun Nav2VsNav3ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ Nav2 vs Nav3",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ComparisonRow(
                category = "상태 관리",
                nav2 = "NavController (내부 상태)",
                nav3 = "직접 관리 (Compose State)",
                nav3Better = true
            )

            ComparisonRow(
                category = "백스택 접근",
                nav2 = "읽기 전용 (제한적)",
                nav3 = "완전한 제어 (List<NavKey>)",
                nav3Better = true
            )

            ComparisonRow(
                category = "화면 정의",
                nav2 = "NavHost + composable()",
                nav3 = "NavDisplay + entryProvider",
                nav3Better = false
            )

            ComparisonRow(
                category = "파라미터 전달",
                nav2 = "route 문자열 파싱",
                nav3 = "NavKey 프로퍼티",
                nav3Better = true
            )

            ComparisonRow(
                category = "테스트",
                nav2 = "Mocking 필요",
                nav3 = "순수 함수 테스트",
                nav3Better = true
            )

            ComparisonRow(
                category = "커스터마이징",
                nav2 = "제한적",
                nav3 = "Building blocks 조합",
                nav3Better = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 언제 Nav3를 선택할까?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ 새 프로젝트 시작\n✓ Compose 중심 앱\n✓ 복잡한 네비게이션 필요\n✓ 완전한 제어 원함",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    category: String,
    nav2: String,
    nav3: String,
    nav3Better: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = category,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7B1FA2)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ComparisonBox(
                label = "Nav2",
                content = nav2,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.weight(1f)
            )

            ComparisonBox(
                label = "Nav3",
                content = nav3,
                color = if (nav3Better) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFE0E0E0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ComparisonBox(
    label: String,
    content: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 10.sp,
                color = Color(0xFF666666),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun MigrationGuideCard() {
    var currentStep by remember { mutableStateOf(0) }

    val steps = listOf(
        MigrationStep(
            "1️⃣ 의존성 추가",
            "implementation(\"androidx.navigation:navigation-compose:3.0.0\")"
        ),
        MigrationStep(
            "2️⃣ NavKey 구현",
            "sealed interface AppScreen : NavKey\nobject HomeScreen : AppScreen\ndata class DetailScreen(val id: String) : AppScreen"
        ),
        MigrationStep(
            "3️⃣ NavigationState 생성",
            "class NavigationState {\n  var backStack by mutableStateOf(listOf<NavKey>(HomeScreen()))\n  fun navigateTo(key: NavKey) { backStack = backStack + key }\n}"
        ),
        MigrationStep(
            "4️⃣ NavController 교체",
            "// Before: navController.navigate(route)\n// After: navigationState.navigateTo(screen)"
        ),
        MigrationStep(
            "5️⃣ NavHost → NavDisplay",
            "NavDisplay(\n  backStack = navigationState.backStack,\n  entryProvider = { key -> when(key) {...} }\n)"
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8EAF6),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 Migration Guide",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nav2에서 Nav3로 마이그레이션하는 5단계",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= currentStep) Color(0xFF3F51B5)
                                else Color(0xFFE0E0E0)
                            )
                            .border(
                                2.dp,
                                if (index == currentStep) Color(0xFF3F51B5)
                                else Color.Transparent,
                                CircleShape
                            )
                            .clickable { currentStep = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (index <= currentStep) Color.White else Color(0xFF999999)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current step content
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = steps[currentStep].title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF263238)
                    ) {
                        Text(
                            text = steps[currentStep].code,
                            fontSize = 10.sp,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { if (currentStep > 0) currentStep-- },
                    enabled = currentStep > 0,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF757575)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("이전", fontSize = 13.sp, color = Color.White)
                }

                Button(
                    onClick = { if (currentStep < steps.size - 1) currentStep++ },
                    enabled = currentStep < steps.size - 1,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3F51B5)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("다음", fontSize = 13.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3F51B5).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 AI Agent 활용",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Android Studio의 Agent Mode를 사용하여\nMigration Guide를 컨텍스트로 제공하고\n자동 마이그레이션을 시도할 수 있습니다",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

data class MigrationStep(
    val title: String,
    val code: String
)

