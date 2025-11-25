package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
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
            item { BasicNavigationDemoCard() }
            item { BackStackVisualizationCard() }
            item { TypeSafeNavigationCard() }
            item { NestedNavigationCard() }
        }
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
                        backStack =
                            backStack + DemoScreen.Profile("user_${System.currentTimeMillis() % 100}")
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
                                    onClick = {
                                        backStack = backStack.filterIndexed { i, _ -> i != index }
                                    },
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
private fun TypeSafeNavigationCard() {
    var selectedUserId by remember { mutableStateOf("") }
    var navigatedUserId by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔒 Type-Safe Navigation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "NavKey를 통한 타입 안전한 파라미터 전달",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nav2 vs Nav3 비교
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "❌ Nav2 (String-based, 런타임 오류 가능)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFF3E0)
                    ) {
                        Text(
                            text = "navController.navigate(\"profile/\$userId\")\n// 타입 체크 없음, 런타임 에러 위험",
                            fontSize = 9.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(8.dp),
                            lineHeight = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "✅ Nav3 (Type-safe, 컴파일 타임 체크)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "navigationState.navigateTo(\n  DemoScreen.Profile(userId = \"user_123\")\n)\n// 컴파일 타임에 타입 체크 보장",
                            fontSize = 9.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(8.dp),
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Demo
            Text(
                "🎯 시도해보기:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("user_1", "user_2", "user_3").forEach { userId ->
                    Button(
                        onClick = {
                            selectedUserId = userId
                            navigatedUserId = userId
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (navigatedUserId == userId) Color(0xFF0277BD) else Color(0xFF90CAF9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            userId,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            if (navigatedUserId != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0277BD).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "✅ 네비게이션 성공",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0277BD)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "DemoScreen.Profile(userId = \"$navigatedUserId\")",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "💡 IDE가 자동완성을 제공하고, 잘못된 타입은 컴파일 에러로 즉시 감지됩니다",
                            fontSize = 9.sp,
                            color = Color(0xFF666666),
                            lineHeight = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NestedNavigationCard() {
    var parentBackStack by remember {
        mutableStateOf(listOf<String>("Main", "Settings"))
    }
    var childBackStack by remember {
        mutableStateOf(listOf<String>("Account", "Privacy"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFCE4EC),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏗️ Nested Navigation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "중첩된 네비게이션 그래프 독립 관리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Navigation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC2185B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Parent Navigation",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2185B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFCE4EC)
                    ) {
                        Text(
                            text = parentBackStack.joinToString(" → "),
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { parentBackStack = parentBackStack + "Profile" },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC2185B)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("+ Screen", fontSize = 10.sp, color = Color.White)
                        }

                        Button(
                            onClick = { if (parentBackStack.size > 1) parentBackStack = parentBackStack.dropLast(1) },
                            enabled = parentBackStack.size > 1,
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE91E63)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Back", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Child Navigation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFAD1457)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("C", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Child Navigation (Settings 내부)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFAD1457)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF8BBD0)
                    ) {
                        Text(
                            text = childBackStack.joinToString(" → "),
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { childBackStack = childBackStack + "Notifications" },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFAD1457)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("+ Setting", fontSize = 10.sp, color = Color.White)
                        }

                        Button(
                            onClick = { if (childBackStack.size > 1) childBackStack = childBackStack.dropLast(1) },
                            enabled = childBackStack.size > 1,
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC2185B)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Text("Back", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFC2185B).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 독립적인 백스택 관리",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2185B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Parent와 Child가 각각 독립된 List<NavKey> 소유\n• Child 네비게이션이 Parent에 영향을 주지 않음\n• 모듈화된 네비게이션 구조 구현 가능",
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}