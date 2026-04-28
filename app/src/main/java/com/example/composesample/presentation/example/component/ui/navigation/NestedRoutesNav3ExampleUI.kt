package com.example.composesample.presentation.example.component.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay


@Composable
fun NestedRoutesNav3ExampleUI(
    onBackEvent: () -> Unit
) {
    var currentTab by remember { mutableStateOf<AppRoute>(AppRoute.Home) }

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
            item { RealNavBackStackDemoCard() }
            item { RealStateManagementDemoCard() }
            item { RealRouteComponentDemoCard() }
        }
    }
}


sealed class AppRoute(val title: String, val icon: ImageVector) {
    object Home : AppRoute("Home", Icons.Filled.Home)
    object Prayers : AppRoute("Prayers", Icons.Filled.Menu)
    object Plans : AppRoute("Plans", Icons.Filled.Settings)
    object User : AppRoute("User", Icons.AutoMirrored.Filled.ArrowBack)
}

@Stable
class NavBackStackSimulator(initialRoute: String) {
    private val _entries = mutableStateListOf<NavEntrySimulator>()
    val entries: List<NavEntrySimulator> = _entries

    init {
        _entries.add(NavEntrySimulator("${initialRoute}Route", ""))
    }

    fun push(route: String) {
        _entries.add(NavEntrySimulator(route, ""))
    }

    fun pop() {
        if (_entries.size > 1) {
            _entries.removeLastOrNull()
        }
    }

    fun saveState(state: String) {
        if (_entries.isNotEmpty()) {
            _entries[_entries.lastIndex] = _entries.last().copy(savedState = state)
        }
    }

    fun getCurrentEntry(): NavEntrySimulator? = _entries.lastOrNull()
}

@Stable
data class NavEntrySimulator(
    val route: String,
    val savedState: String
)

@Stable
class EntryDecoratorSimulator {
    private val _decoratedEntries = mutableStateMapOf<String, DecoratedEntryState>()
    val decoratedEntries: Map<String, DecoratedEntryState> = _decoratedEntries

    fun decorateEntry(
        route: String,
        viewModelActive: Boolean = true,
        stateRestored: Boolean = false
    ) {
        _decoratedEntries[route] = DecoratedEntryState(
            viewModelActive = viewModelActive,
            stateRestored = stateRestored,
            memoryUsage = (8..20).random()
        )
    }

    fun removeEntry(route: String) {
        _decoratedEntries.remove(route)
    }

    fun restoreState(route: String) {
        _decoratedEntries[route] = _decoratedEntries[route]?.copy(stateRestored = true)
            ?: DecoratedEntryState(true, true, (8..20).random())
    }
}

@Stable
data class DecoratedEntryState(
    val viewModelActive: Boolean,
    val stateRestored: Boolean,
    val memoryUsage: Int
)

@Stable
class RouteComponentManagerSimulator {
    private val _activeComponents = mutableStateMapOf<String, RouteComponentState>()
    val activeComponents: Map<String, RouteComponentState> = _activeComponents

    fun createComponent(name: String) {
        _activeComponents[name] = RouteComponentState(
            isActive = true,
            backStackSize = 1,
            memoryUsage = (12..25).random()
        )
    }

    fun removeComponent(name: String) {
        _activeComponents.remove(name)
    }

    fun activateComponent(name: String) {
        _activeComponents[name] = _activeComponents[name]?.copy(isActive = true)
            ?: RouteComponentState(true, 1, (12..25).random())
    }

    fun deactivateComponent(name: String) {
        _activeComponents[name] = _activeComponents[name]?.copy(isActive = false)
            ?: return
    }
}

@Stable
data class RouteComponentState(
    val isActive: Boolean,
    val backStackSize: Int,
    val memoryUsage: Int
)

@Composable
private fun BottomNavigationDemoCard(
    currentTab: AppRoute,
    onTabSelected: (AppRoute) -> Unit
) {
    val tabs = listOf(AppRoute.Home, AppRoute.Prayers, AppRoute.Plans, AppRoute.User)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📱 Bottom Navigation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
private fun RealNavBackStackDemoCard() {

    val homeBackStack = remember { NavBackStackSimulator("Home") }
    val prayersBackStack = remember { NavBackStackSimulator("Prayers") }
    var currentRouteComponent by remember { mutableStateOf("Home") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 실제 NavBackStack 동작",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "각 RouteComponent가 독립적인 NavBackStack을 관리합니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // RouteComponent 선택기
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Home", "Prayers").forEach { component ->
                    Button(
                        onClick = { currentRouteComponent = component },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentRouteComponent == component)
                                Color(0xFF1976D2) else Color(0xFF1976D2).copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "${component}RouteComponent",
                            color = Color.White,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 현재 선택된 RouteComponent의 백스택 표시
            val currentBackStack =
                if (currentRouteComponent == "Home") homeBackStack else prayersBackStack

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "NavBackStack<${currentRouteComponent}Route>:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    currentBackStack.entries.forEachIndexed { index, entry ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entry[$index]: ${entry.route}",
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.weight(1f)
                            )

                            if (index == currentBackStack.entries.lastIndex) {
                                Text(
                                    text = "← Current",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF5722),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (entry.savedState.isNotEmpty()) {
                            Text(
                                text = "  SavedState: ${entry.savedState}",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // NavBackStack 조작 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val subRoutes = if (currentRouteComponent == "Home") {
                            listOf("PhraseOfDay", "SaintOfDay", "Calendar")
                        } else {
                            listOf("Novena", "Chaplet", "Rosary")
                        }
                        currentBackStack.push("${subRoutes.random()}Route")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
                        currentBackStack.pop()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    text = "💡 실제로는 rememberNavBackStack<RouteType>()으로 생성되며, 각 RouteComponent가 독립적인 백스택을 유지합니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun RealStateManagementDemoCard() {
    val entryDecorator = remember { EntryDecoratorSimulator() }
    var currentRoute by remember { mutableStateOf("HomeRoute") }
    var inputText by rememberSaveable(currentRoute) { mutableStateOf("") }
    var rotationCount by remember { mutableStateOf(0) }


    LaunchedEffect(currentRoute) {
        entryDecorator.decorateEntry(currentRoute)
        delay(100)
        entryDecorator.restoreState(currentRoute)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔄 EntryDecorator 상태 관리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "SavedStateNavEntryDecorator가 자동으로 상태를 저장/복원합니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("HomeRoute", "DetailRoute", "SettingsRoute").forEach { route ->
                    Button(
                        onClick = {
                            currentRoute = route
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (currentRoute == route)
                                Color(0xFF7B1FA2) else Color(0xFF7B1FA2).copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = route.replace("Route", ""),
                            color = Color.White,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "NavEntry: $currentRoute",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7B1FA2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val entryState = entryDecorator.decoratedEntries[currentRoute]
                    if (entryState != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ViewModel: ${if (entryState.viewModelActive) "Active" else "Cleared"}",
                                fontSize = 11.sp,
                                color = if (entryState.viewModelActive) Color(0xFF4CAF50) else Color.Gray
                            )
                            Text(
                                text = "State: ${if (entryState.stateRestored) "Restored" else "New"}",
                                fontSize = 11.sp,
                                color = if (entryState.stateRestored) Color(0xFF2196F3) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("rememberSaveable로 저장되는 상태", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7B1FA2),
                            focusedLabelColor = Color(0xFF7B1FA2)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        rotationCount++

                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Rotate",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("회전($rotationCount)", color = Color.White, fontSize = 9.sp)
                }

                Button(
                    onClick = {

                        entryDecorator.decoratedEntries.keys.forEach { route ->
                            if (route != currentRoute) {
                                entryDecorator.removeEntry(route)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Cleanup",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("정리", color = Color.White, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 실제로는 rememberSavedStateNavEntryDecorator()가 자동으로 상태 저장/복원을 처리합니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun RealRouteComponentDemoCard() {
    val routeComponentManager = remember { RouteComponentManagerSimulator() }
    var selectedComponent by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🧩 RouteComponent 라이프사이클",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "각 RouteComponent의 생성, 활성화, 제거 과정을 시뮬레이션합니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))


            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "📊 활성 RouteComponent 목록:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (routeComponentManager.activeComponents.isEmpty()) {
                        Text(
                            text = "활성 컴포넌트 없음",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        routeComponentManager.activeComponents.forEach { (name, component) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${name}RouteComponent",
                                        fontSize = 12.sp,
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "백스택: ${component.backStackSize}개 엔트리",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${component.memoryUsage}MB",
                                        fontSize = 12.sp,
                                        color = Color(0xFFD32F2F),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (component.isActive) "활성" else "비활성",
                                        fontSize = 10.sp,
                                        color = if (component.isActive) Color(0xFF4CAF50) else Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = "RouteComponent 관리:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val availableComponents = listOf("Home", "Prayers", "Plans", "Profile")
            availableComponents.chunked(2).forEach { rowComponents ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowComponents.forEach { component ->
                        val isActive = routeComponentManager.activeComponents.containsKey(component)
                        Button(
                            onClick = {
                                if (isActive) {
                                    routeComponentManager.removeComponent(component)
                                } else {
                                    routeComponentManager.createComponent(component)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isActive) Color(0xFFD32F2F) else Color(
                                    0xFF4CAF50
                                )
                            )
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Filled.Delete else Icons.Filled.Add,
                                contentDescription = if (isActive) "Remove" else "Create",
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isActive) "제거" else "생성",
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (rowComponents.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 실제로는 NavDisplay와 entryProvider를 통해 RouteComponent가 생성되고, 필요 없을 때 자동으로 정리됩니다!",
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF388E3C),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}