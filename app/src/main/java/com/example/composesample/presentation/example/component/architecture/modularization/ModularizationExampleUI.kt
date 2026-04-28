package com.example.composesample.presentation.example.component.architecture.modularization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@Composable
fun ModularizationExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Modularization",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ModuleStructureCard() }
            item { BeforeAfterComparisonCard() }
            item { DependencyFlowCard() }
        }
    }
}

@Composable
private fun ModuleStructureCard() {
    var selectedModule by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏗️ 모듈 구조",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Wiring Module 패턴의 모듈 구조",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Module
            ModuleBox(
                name = ":app",
                description = "앱 진입점, 모든 모듈 통합",
                color = Color(0xFF1976D2),
                isSelected = selectedModule == "app",
                onClick = { selectedModule = if (selectedModule == "app") null else "app" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Wiring Module
            ModuleBox(
                name = ":wiring",
                description = "모듈 간 연결, DI 설정",
                color = Color(0xFFE65100),
                isSelected = selectedModule == "wiring",
                onClick = { selectedModule = if (selectedModule == "wiring") null else "wiring" },
                indent = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feature Modules
            Column(
                modifier = Modifier.padding(start = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModuleBox(
                    name = ":feature:home",
                    description = "홈 화면 기능 구현",
                    color = Color(0xFF7B1FA2),
                    isSelected = selectedModule == "home",
                    onClick = { selectedModule = if (selectedModule == "home") null else "home" },
                    indent = 2
                )

                ModuleBox(
                    name = ":feature:home-api",
                    description = "홈 기능 공개 인터페이스",
                    color = Color(0xFFBA68C8),
                    isSelected = selectedModule == "home-api",
                    onClick = {
                        selectedModule = if (selectedModule == "home-api") null else "home-api"
                    },
                    indent = 3
                )

                ModuleBox(
                    name = ":feature:profile",
                    description = "프로필 기능 구현",
                    color = Color(0xFF00897B),
                    isSelected = selectedModule == "profile",
                    onClick = {
                        selectedModule = if (selectedModule == "profile") null else "profile"
                    },
                    indent = 2
                )

                ModuleBox(
                    name = ":feature:profile-api",
                    description = "프로필 공개 인터페이스",
                    color = Color(0xFF4DB6AC),
                    isSelected = selectedModule == "profile-api",
                    onClick = {
                        selectedModule =
                            if (selectedModule == "profile-api") null else "profile-api"
                    },
                    indent = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Core Module
            ModuleBox(
                name = ":core",
                description = "공통 유틸리티, 네트워크, DB",
                color = Color(0xFF616161),
                isSelected = selectedModule == "core",
                onClick = { selectedModule = if (selectedModule == "core") null else "core" },
                indent = 4
            )

            AnimatedVisibility(
                visible = selectedModule != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                selectedModule?.let { module ->
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "📝 역할",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (module) {
                                        "app" -> "• Application 클래스\n• MainActivity\n• 전체 DI 그래프 초기화\n• 모든 Feature 통합"
                                        "wiring" -> "• Feature 구현체와 API 연결\n• DI Module 정의\n• 의존성 해결\n• 모듈 간 통신 중재"
                                        "home" -> "• HomeScreen 구현\n• HomeViewModel\n• HomeApiImpl (인터페이스 구현)"
                                        "home-api" -> "• HomeApi interface\n• navigateToHome()\n• 구현체 없음, 선언만"
                                        "profile" -> "• ProfileScreen 구현\n• ProfileViewModel\n• ProfileApiImpl"
                                        "profile-api" -> "• ProfileApi interface\n• navigateToProfile()\n• getProfileName()"
                                        "core" -> "• 네트워크 클라이언트\n• Room Database\n• 공통 유틸리티\n• Base 클래스"
                                        else -> ""
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666),
                                    lineHeight = 16.sp
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
private fun ModuleBox(
    name: String,
    description: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    indent: Int = 0
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Color.White,
        shadowElevation = if (isSelected) 4.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = color
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color(0xFF999999)
                )
            }

            Icon(
                if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BeforeAfterComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ Before vs After",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Before
            ComparisonSection(
                title = "❌ Before: 직접 의존",
                color = Color(0xFFD32F2F),
                items = listOf(
                    ":feature:home → :feature:profile",
                    "강한 결합",
                    "순환 의존성 위험",
                    "Profile 변경 시 Home 영향",
                    "독립 테스트 어려움"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // After
            ComparisonSection(
                title = "✅ After: Wiring Module",
                color = Color(0xFF388E3C),
                items = listOf(
                    ":feature:home → :feature:profile-api",
                    ":wiring → :feature:profile",
                    "느슨한 결합",
                    "단방향 의존성",
                    "독립적 테스트 가능",
                    "구현 변경 영향 최소화"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0277BD).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 핵심 차이",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0277BD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Feature 간 직접 의존을 제거하고\nAPI를 통한 간접 통신으로 전환합니다",
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
private fun ComparisonSection(
    title: String,
    color: Color,
    items: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        if (title.startsWith("✅")) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item,
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun DependencyFlowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 의존성 흐름",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "단방향 의존성 구조",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dependency flow visualization
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DependencyFlowItem(":app", Color(0xFF1976D2), 0)
                DependencyArrow()
                DependencyFlowItem(":wiring", Color(0xFFE65100), 1)
                DependencyArrow()
                DependencyFlowItem(":feature:home", Color(0xFF7B1FA2), 2)
                DependencyArrow()
                DependencyFlowItem(":feature:profile-api", Color(0xFFBA68C8), 3)
                DependencyArrow()
                DependencyFlowItem(":core", Color(0xFF616161), 4)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "🎯 의존성 규칙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RuleItem("✓", "위에서 아래로만 의존", Color(0xFF388E3C))
                    RuleItem("✓", "Feature 간 직접 의존 금지", Color(0xFF388E3C))
                    RuleItem("✓", "API를 통한 간접 통신", Color(0xFF388E3C))
                    RuleItem("✗", "아래에서 위로 의존 금지", Color(0xFFD32F2F))
                    RuleItem("✗", "순환 의존성 금지", Color(0xFFD32F2F))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 장점",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 명확한 의존성 방향\n• 순환 의존성 방지\n• 빌드 그래프 최적화\n• 병렬 빌드 가능",
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
private fun DependencyFlowItem(name: String, color: Color, level: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.2f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun DependencyArrow() {
    Box(
        modifier = Modifier
            .padding(start = 16.dp)
            .width(2.dp)
            .height(12.dp)
            .background(Color(0xFFBDBDBD))
    )
}

@Composable
private fun RuleItem(icon: String, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Text(
            text = icon,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFF666666)
        )
    }
}

