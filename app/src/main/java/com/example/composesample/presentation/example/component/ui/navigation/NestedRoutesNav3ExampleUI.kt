package com.example.composesample.presentation.example.component.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader


@Composable
fun NestedRoutesNav3ExampleUI(
    onBackEvent: () -> Unit
) {
    var currentTab by remember { mutableStateOf<AppRoute>(AppRoute.Home) }
    var currentSubRoute by remember { mutableStateOf<SubRoute?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Nested Routes Nav3 Example",
            onBackIconClicked = onBackEvent
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { NavigationOverviewCard() }
            item { RoutesStructureCard() }
            item { BottomNavigationDemoCard(currentTab) { currentTab = it } }
            item { RouteComponentExampleCard(currentSubRoute) { currentSubRoute = it } }
            item { ArchitectureDiagramCard() }
            item { CodeExamplesCard() }
            item { BenefitsCard() }
        }
    }
}


sealed class AppRoute(val title: String, val icon: ImageVector) {
    object Home : AppRoute("Home", Icons.Filled.Home)
    object Prayers : AppRoute("Prayers", Icons.Filled.Menu)
    object Plans : AppRoute("Plans", Icons.Filled.Settings)
    object User : AppRoute("User", Icons.Filled.ArrowBack)
}

sealed class SubRoute(val title: String) {
    object PhraseOfTheDay : SubRoute("Phrase of the Day")
    object SaintOfTheDay : SubRoute("Saint of the Day")
    object PrayersDetail : SubRoute("Prayers Detail")
    object NovenaFlow : SubRoute("Novena Flow")
    object ChapletFlow : SubRoute("Chaplet Flow")
    object PlansFlow : SubRoute("Plans Flow")
}

@Composable
private fun NavigationOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🧭 Navigation 3 중첩 라우팅이란?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Navigation 3는 각 RouteComponent가 독립적인 백스택을 관리하여 모듈화된 네비게이션을 구현할 수 있게 해주는 라이브러리입니다.",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("독립적 백스택", Color(0xFF4CAF50))
                FeatureChip("모듈화", Color(0xFF2196F3))
                FeatureChip("확장성", Color(0xFFFF9800))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "💡 핵심: Activity → Compose 전환으로 단일 MainActivity 구현",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, color: Color) {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RoutesStructureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏗️ 라우트 구조",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            CodeBlock(
                """
                @Serializable
                sealed class AppRoute : NavKey {
                    @Serializable object Home : AppRoute()
                    @Serializable object Prayers : AppRoute()
                    @Serializable object Plans : AppRoute()
                    @Serializable object User : AppRoute()
                }
                
                val bottomNavItems = listOf(
                    AppRoute.Home, AppRoute.Prayers, 
                    AppRoute.Plans, AppRoute.User
                )
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "@Serializable의 장점:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7B1FA2)
            )
            
            Text(
                text = "• JSON 직렬화/역직렬화 지원\n• 네비게이션 상태 저장\n• 딥링킹 지원\n• 백그라운드 프로세스 간 데이터 전달",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun BottomNavigationDemoCard(
    currentTab: AppRoute,
    onTabSelected: (AppRoute) -> Unit
) {
    val tabs = listOf(AppRoute.Home, AppRoute.Prayers, AppRoute.Plans, AppRoute.User)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E8),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📱 Bottom Navigation 데모",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEach { tab ->
                        BottomNavItem(
                            route = tab,
                            isSelected = currentTab == tab,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "현재 선택된 탭: ${currentTab.title}",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    route: AppRoute,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF388E3C).copy(alpha = 0.2f) else Color.Transparent
    val iconColor = if (isSelected) Color(0xFF388E3C) else Color.Gray
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = route.icon,
            contentDescription = route.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = route.title,
            fontSize = 10.sp,
            color = iconColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun RouteComponentExampleCard(
    currentSubRoute: SubRoute?,
    onSubRouteSelected: (SubRoute?) -> Unit
) {
    val subRoutes = listOf(
        SubRoute.PhraseOfTheDay,
        SubRoute.SaintOfTheDay,
        SubRoute.PrayersDetail,
        SubRoute.NovenaFlow,
        SubRoute.ChapletFlow,
        SubRoute.PlansFlow
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔄 RouteComponent 예시",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Home Route에서 접근 가능한 서브 라우트들:",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subRoutes.chunked(2).forEach { rowRoutes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowRoutes.forEach { route ->
                            Button(
                                onClick = { 
                                    onSubRouteSelected(if (currentSubRoute == route) null else route)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (currentSubRoute == route) 
                                        Color(0xFFE65100) else Color(0xFFE65100).copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = route.title,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        if (rowRoutes.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            

            if (currentSubRoute != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Navigate",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "현재 화면: ${currentSubRoute.title}",
                            fontSize = 14.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitectureDiagramCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🏛️ 아키텍처 다이어그램",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {

                ArchitectureBox(
                    title = "MainActivity",
                    subtitle = "메인 네비게이션 백스택",
                    color = Color(0xFF1976D2)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Home", "Prayers", "Plans", "User").forEach { tab ->
                        ArchitectureBox(
                            title = tab,
                            subtitle = "RouteComponent",
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                

                ArchitectureBox(
                    title = "Sub Routes",
                    subtitle = "독립적인 백스택 관리",
                    color = Color(0xFFFF9800)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "🔑 핵심 개념:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Text(
                text = "• 각 RouteComponent는 독립적인 백스택 보유\n• 모듈화된 구조로 유지보수성 향상\n• 설정 변경 시에도 상태 유지\n• 메모리 누수 방지",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ArchitectureBox(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, color, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CodeExamplesCard() {
    var selectedExample by remember { mutableStateOf("RouteComponent") }
    val examples = listOf("RouteComponent", "BottomNav", "EntryDecorators")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "💻 코드 예시",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                examples.forEach { example ->
                    Button(
                        onClick = { selectedExample = example },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedExample == example) 
                                Color(0xFFD32F2F) else Color(0xFFD32F2F).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = example,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when (selectedExample) {
                "RouteComponent" -> RouteComponentCodeBlock()
                "BottomNav" -> BottomNavCodeBlock()
                "EntryDecorators" -> EntryDecoratorsCodeBlock()
            }
        }
    }
}

@Composable
private fun RouteComponentCodeBlock() {
    CodeBlock(
        """
        @Composable
        fun HomeRouteComponent() {
            val backstack = rememberNavBackStack<HomeSubRoute>()
            
            NavDisplay(
                backstack = backstack,
                entryProvider = { route ->
                    when (route) {
                        HomeSubRoute.Home -> HomeRoute(backstack)
                        HomeSubRoute.SaintRoute -> SaintRoute(backstack)
                        HomeSubRoute.PhraseRoute -> PhraseRoute(backstack)
                    }
                },
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                )
            )
        }
        """.trimIndent()
    )
}

@Composable
private fun BottomNavCodeBlock() {
    CodeBlock(
        """
        @Composable
        fun BottomNavigationBar(
            currentRoute: AppRoute,
            onNavigation: (AppRoute) -> Unit
        ) {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item,
                        onClick = { onNavigation(item) }
                    )
                }
            }
        }
        """.trimIndent()
    )
}

@Composable
private fun EntryDecoratorsCodeBlock() {
    CodeBlock(
        """
        val entryDecorators = listOf(
            // 화면 전환 최적화
            rememberSceneSetupNavEntryDecorator(),
            
            // 상태 저장/복원
            rememberSavedStateNavEntryDecorator(),
            
            // ViewModel 라이프사이클 관리
            rememberViewModelStoreNavEntryDecorator()
        )
        """.trimIndent()
    )
}

@Composable
private fun BenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF1F8E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "✨ Navigation 3 중첩 라우팅의 장점",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val benefits = listOf(
                "🎯 단일 책임 원칙" to "각 RouteComponent가 자신만의 네비게이션 스택 관리",
                "🔄 설정 변경 대응" to "화면 회전 등에서도 상태 유지",
                "🗑️ 메모리 관리" to "라우트 제거 시 자동으로 리소스 정리",
                "🧩 모듈화" to "독립적인 컴포넌트로 유지보수성 향상",
                "⚡ 성능 최적화" to "불필요한 recomposition 방지",
                "🔗 재사용성" to "다른 RouteComponent에서도 쉽게 사용 가능"
            )
            
            benefits.forEach { (title, description) ->
                BenefitItem(title = title, description = description)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 결론: Activity 기반에서 완전한 Compose 기반 네비게이션으로 전환하여 더 나은 사용자 경험과 개발자 경험을 제공합니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun BenefitItem(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF388E3C),
            modifier = Modifier.width(140.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF263238)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF80CBC4),
            lineHeight = 16.sp
        )
    }
}