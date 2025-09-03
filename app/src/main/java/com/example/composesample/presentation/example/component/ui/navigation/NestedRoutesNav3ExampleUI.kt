package com.example.composesample.presentation.example.component.ui.navigation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            item { BottomNavigationDemoCard(currentTab) { currentTab = it } }
            item { RouteComponentExampleCard(currentSubRoute) { currentSubRoute = it } }
            item { IndependentBackstackDemoCard() }
            item { StateManagementDemoCard() }
            item { MemoryManagementDemoCard() }
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
    val backgroundColor =
        if (isSelected) Color(0xFF388E3C).copy(alpha = 0.2f) else Color.Transparent
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

// 독립적인 백스택 관리 데모
@Composable
private fun IndependentBackstackDemoCard() {
    var homeStack by remember { mutableStateOf(listOf("Home")) }
    var prayersStack by remember { mutableStateOf(listOf("Prayers")) }
    var currentTab by remember { mutableStateOf("Home") }

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
                text = "🎯 독립적인 백스택 관리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "각 RouteComponent가 독립적인 네비게이션 스택을 유지합니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 탭 선택기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Home", "Prayers").forEach { tab ->
                    Button(
                        onClick = { currentTab = tab },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (currentTab == tab) Color(0xFF1976D2) else Color(0xFF1976D2).copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "$tab Tab",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 현재 선택된 탭의 스택 표시
            val currentStack = if (currentTab == "Home") homeStack else prayersStack
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "${currentTab} RouteComponent Stack:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    currentStack.forEachIndexed { index, route ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[$index] $route",
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (index == currentStack.lastIndex) {
                                Text(
                                    text = "← 현재",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF5722),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 네비게이션 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val routes = listOf("Detail", "Settings", "Profile", "About")
                        val newRoute = routes.random()
                        if (currentTab == "Home") {
                            homeStack = homeStack + newRoute
                        } else {
                            prayersStack = prayersStack + newRoute
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Push",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Push", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        if (currentTab == "Home" && homeStack.size > 1) {
                            homeStack = homeStack.dropLast(1)
                        } else if (currentTab == "Prayers" && prayersStack.size > 1) {
                            prayersStack = prayersStack.dropLast(1)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Pop",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pop", color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 각 탭을 전환해도 이전 스택이 그대로 유지됩니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// 상태 관리 및 복원 데모
@Composable
private fun StateManagementDemoCard() {
    var inputText by remember { mutableStateOf("") }
    var savedStates by remember { mutableStateOf(mapOf<String, String>()) }
    var currentRoute by remember { mutableStateOf("Route1") }
    var simulatedRotation by remember { mutableStateOf(false) }

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
                text = "🔄 상태 관리 및 복원",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SavedStateNavEntryDecorator로 상태가 자동 저장/복원됩니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 라우트 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Route1", "Route2", "Route3").forEach { route ->
                    Button(
                        onClick = { 
                            // 현재 상태 저장
                            savedStates = savedStates + (currentRoute to inputText)
                            currentRoute = route
                            // 저장된 상태 복원
                            inputText = savedStates[route] ?: ""
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (currentRoute == route) Color(0xFF7B1FA2) else Color(0xFF7B1FA2).copy(alpha = 0.3f)
                        )
                    ) {
                        Text(route, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 현재 라우트의 상태
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "현재 라우트: $currentRoute",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7B1FA2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("사용자 입력", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF7B1FA2),
                            focusedLabelColor = Color(0xFF7B1FA2)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 저장된 상태들 표시
            if (savedStates.isNotEmpty()) {
                Text(
                    text = "💾 저장된 상태들:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7B1FA2)
                )
                
                savedStates.forEach { (route, state) ->
                    Text(
                        text = "• $route: \"${state.take(20)}${if (state.length > 20) "..." else ""}\"",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 시뮬레이션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        simulatedRotation = !simulatedRotation
                        // 화면 회전 시뮬레이션 (상태는 유지됨)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Rotate",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("화면회전", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        // 프로세스 재시작 시뮬레이션
                        savedStates = savedStates + (currentRoute to inputText)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("상태저장", color = Color.White, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 라우트를 전환해도 각각의 입력 상태가 유지됩니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// 메모리 관리 데모
@Composable
private fun MemoryManagementDemoCard() {
    var activeRoutes by remember { mutableStateOf(setOf("Home")) }
    var memoryUsage by remember { mutableStateOf(mapOf("Home" to 12)) }
    var totalMemory by remember { mutableStateOf(12) }

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
                text = "🗑️ 메모리 관리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "RouteComponent 제거 시 자동으로 리소스가 정리됩니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 메모리 사용량 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📊 현재 메모리 사용량: ${totalMemory}MB",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    activeRoutes.forEach { route ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• $route Route",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "${memoryUsage[route] ?: 0}MB",
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 라우트 관리 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newRoutes = listOf("Detail", "Settings", "Profile", "About", "Help")
                        val newRoute = newRoutes.random()
                        if (!activeRoutes.contains(newRoute)) {
                            val newMemory = (8..15).random()
                            activeRoutes = activeRoutes + newRoute
                            memoryUsage = memoryUsage + (newRoute to newMemory)
                            totalMemory += newMemory
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Route",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("라우트 추가", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        if (activeRoutes.size > 1) {
                            val routeToRemove = activeRoutes.filter { it != "Home" }.randomOrNull()
                            routeToRemove?.let { route ->
                                activeRoutes = activeRoutes - route
                                val freedMemory = memoryUsage[route] ?: 0
                                memoryUsage = memoryUsage - route
                                totalMemory -= freedMemory
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove Route",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("라우트 제거", color = Color.White, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 라우트 제거 시 해당 ViewModel, 상태, 리소스가 모두 자동으로 정리됩니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}