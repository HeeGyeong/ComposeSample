package com.example.composesample.presentation.example.component.architecture.pattern.retain

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Retain API Example UI
 *
 * Compose 1.10의 retain API 개념을 시뮬레이션하여
 * ViewModel 대비 간소화된 Presenter 패턴을 보여줍니다.
 *
 * 주의: 실제 retain API는 Compose 1.10이 필요합니다.
 * 이 예제는 개념 이해를 위한 시뮬레이션입니다.
 */

interface Presenter {
    fun close()
}

class CounterPresenter : Presenter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun increment() {
        _count.value++
    }

    fun decrement() {
        if (_count.value > 0) _count.value--
    }

    fun reset() {
        _count.value = 0
    }

    fun asyncIncrement() {
        scope.launch {
            _isLoading.value = true
            delay(500)
            _count.value++
            _isLoading.value = false
        }
    }

    override fun close() {
        scope.cancel()
    }
}

class TodoPresenter : Presenter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _todos = MutableStateFlow<List<String>>(emptyList())
    val todos: StateFlow<List<String>> = _todos.asStateFlow()

    fun addTodo(text: String) {
        _todos.value = _todos.value + text
    }

    fun removeTodo(index: Int) {
        _todos.value = _todos.value.toMutableList().apply { removeAt(index) }
    }

    fun clearAll() {
        _todos.value = emptyList()
    }

    override fun close() {
        scope.cancel()
    }
}

@Composable
fun RetainApiExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedExample by remember { mutableStateOf(0) }

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
            TabItem("비교", selectedExample == 0, { selectedExample = 0 }, Modifier.weight(1f))
            TabItem("라이프사이클", selectedExample == 1, { selectedExample = 1 }, Modifier.weight(1f))
            TabItem("Presenter", selectedExample == 2, { selectedExample = 2 }, Modifier.weight(1f))
            TabItem("Nav3", selectedExample == 3, { selectedExample = 3 }, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedExample) {
            0 -> ViewModelVsRetainDemo()
            1 -> RetainLifecycleDemo()
            2 -> PresenterPatternDemo()
            3 -> NavigationSupportDemo()
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
                    text = "Retain API",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Goodbye ViewModel. Hello retain!",
                    fontSize = 14.sp,
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
private fun ViewModelVsRetainDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ViewModel vs retain API",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Compose 1.10의 retain API는 ViewModel의 역할을 " +
                                "더 간단한 방식으로 대체합니다. " +
                                "Configuration Change 생존이 프레임워크 관심사(ViewModel)에서 " +
                                "UI 관심사(retain)로 이동합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            CodeComparisonCard(
                title = "기존: ViewModel 방식",
                titleColor = Color(0xFFD32F2F),
                bgColor = Color(0xFFFFF0F0),
                icon = "❌",
                points = listOf(
                    "ViewModel() 상속 필요",
                    "@HiltViewModel 어노테이션",
                    "별도 DI 아티팩트 (hilt-viewmodel)",
                    "hiltViewModel() 팩토리 필요"
                ),
                code = """@HiltViewModel
@Inject
class AuthViewModel(
    ...
): ViewModel() {
    val state: StateFlow<UiState>
    fun login(creds: Credentials) { .. }
    fun logout() { .. }
}

// 사용
entry<Route.Auth> {
    AuthScreen(
        viewModel = hiltViewModel()
    )
}"""
            )
        }

        item {
            CodeComparisonCard(
                title = "새로운: retain 방식",
                titleColor = Color(0xFF2E7D32),
                bgColor = Color(0xFFF0FFF0),
                icon = "✅",
                points = listOf(
                    "일반 Kotlin 클래스",
                    "@Inject만으로 충분",
                    "추가 DI 설정 불필요",
                    "retain { } 으로 보존"
                ),
                code = """@Inject
class AuthPresenter(...) {
    val state: StateFlow<UiState>
    fun login(creds: Credentials) { .. }
    fun logout() { .. }
}

// 사용
entry<Route.Auth> {
    AuthScreen(
        presenter = retain { presenter() }
    )
}"""
            )
        }

        item {
            ComparisonTableCard()
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 핵심",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "retain은 remember와 다른 Retention 라이프사이클을 가집니다.\n" +
                                "• remember: Composition 떠나면 소멸\n" +
                                "• rememberSaveable: 직렬화 필요, 프로세스 사망 생존\n" +
                                "• retain: 직렬화 불필요, Config Change 생존 (= ViewModel)",
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CodeComparisonCard(
    title: String,
    titleColor: Color,
    bgColor: Color,
    icon: String,
    points: List<String>,
    code: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$icon $title",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            points.forEach { point ->
                Text(
                    text = "• $point",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = code,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(rememberScrollState()),
                    fontSize = 11.sp,
                    color = Color(0xFFE0E0E0),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ComparisonTableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "비교 테이블",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val rows = listOf(
                Triple("클래스 타입", "ViewModel() 상속", "일반 Kotlin 클래스"),
                Triple("DI 설정", "@HiltViewModel + 별도 아티팩트", "@Inject (일반)"),
                Triple("인스턴스 생성", "hiltViewModel()", "retain { ... }"),
                Triple("정리 콜백", "onCleared()", "onRetired()"),
                Triple("Config Change", "✅ 생존", "✅ 생존"),
                Triple("프로세스 사망", "SavedStateHandle", "✗ (별도 필요)"),
                Triple("직렬화", "불필요", "불필요"),
            )

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color(0xFF1976D2))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "항목",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "ViewModel",
                    modifier = Modifier.weight(1.2f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "retain",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            rows.forEachIndexed { index, (label, vm, retain) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) Color(0xFFFAFAFA) else Color.White)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = vm,
                        modifier = Modifier.weight(1.2f),
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                    Text(
                        text = retain,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun RetainLifecycleDemo() {
    val lifecycleEvents = remember { mutableStateListOf<Pair<String, Color>>() }
    var isRetained by remember { mutableStateOf(false) }
    var isInComposition by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RetainObserver Lifecycle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "retain된 객체의 라이프사이클 이벤트를 시뮬레이션합니다.\n" +
                                "각 버튼은 RetainObserver의 콜백에 대응합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RetainObserver 콜백",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LifecycleButton(
                        text = "onRetained()",
                        description = "값이 retain 됨",
                        color = Color(0xFF1976D2),
                        enabled = !isRetained,
                        onClick = {
                            isRetained = true
                            lifecycleEvents.add("onRetained()" to Color(0xFF1976D2))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LifecycleButton(
                        text = "onEnteredComposition()",
                        description = "Composition에 진입",
                        color = Color(0xFF4CAF50),
                        enabled = isRetained && !isInComposition,
                        onClick = {
                            isInComposition = true
                            lifecycleEvents.add("onEnteredComposition()" to Color(0xFF4CAF50))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LifecycleButton(
                        text = "onExitedComposition()",
                        description = "Composition에서 퇴장 (Config Change 등)",
                        color = Color(0xFFFF9800),
                        enabled = isInComposition,
                        onClick = {
                            isInComposition = false
                            lifecycleEvents.add("onExitedComposition()" to Color(0xFFFF9800))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LifecycleButton(
                        text = "onUnused()",
                        description = "더 이상 사용되지 않음",
                        color = Color(0xFF9E9E9E),
                        enabled = isRetained && !isInComposition,
                        onClick = {
                            lifecycleEvents.add("onUnused()" to Color(0xFF9E9E9E))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LifecycleButton(
                        text = "onRetired()",
                        description = "완전히 폐기 → 리소스 정리!",
                        color = Color(0xFFF44336),
                        enabled = isRetained && !isInComposition,
                        onClick = {
                            isRetained = false
                            lifecycleEvents.add("onRetired() → close()" to Color(0xFFF44336))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            lifecycleEvents.clear()
                            isRetained = false
                            isInComposition = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("초기화", fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "이벤트 로그",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80CBC4)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (lifecycleEvents.isEmpty()) {
                        Text(
                            text = "> 버튼을 눌러 라이프사이클을 시뮬레이션하세요.",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E),
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        lifecycleEvents.forEachIndexed { index, (event, color) ->
                            Text(
                                text = "${index + 1}. $event",
                                fontSize = 12.sp,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔄 ViewModel과의 대응",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ViewModel.onCleared() → RetainObserver.onRetired()\n\n" +
                                "ViewModel은 onCleared() 하나로 정리하지만,\n" +
                                "RetainObserver는 더 세분화된 라이프사이클을 제공합니다:\n\n" +
                                "• onRetained → 값 보존 시작\n" +
                                "• onEnteredComposition → 화면에 표시\n" +
                                "• onExitedComposition → 화면에서 사라짐 (Config Change)\n" +
                                "• onUnused → 더 이상 참조 없음\n" +
                                "• onRetired → 최종 정리 (= onCleared)",
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun LifecycleButton(
    text: String,
    description: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (enabled) color.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
        label = "lifecycleBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (enabled) color else Color(0xFFE0E0E0),
        label = "lifecycleBorder"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (enabled) color else Color(0xFFE0E0E0))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (enabled) color else Color(0xFFBDBDBD)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = if (enabled) Color(0xFF757575) else Color(0xFFBDBDBD)
            )
        }
    }
}

@Composable
private fun PresenterPatternDemo() {
    val presenter = remember { CounterPresenter() }
    val todoPresenter = remember { TodoPresenter() }

    DisposableEffect(Unit) {
        onDispose {
            presenter.close()
            todoPresenter.close()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Presenter Pattern (retain 시뮬레이션)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "ViewModel 대신 일반 Kotlin 클래스를 Presenter로 사용합니다.\n" +
                                "실제로는 retain { Presenter() }로 Config Change를 생존합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            CounterPresenterCard(presenter)
        }

        item {
            TodoPresenterCard(todoPresenter)
        }
    }
}

@Composable
private fun CounterPresenterCard(presenter: CounterPresenter) {
    val count by presenter.count.collectAsStateWithLifecycle()
    val isLoading by presenter.isLoading.collectAsStateWithLifecycle()

    val scale by animateFloatAsState(
        targetValue = if (count > 0) 1f else 0.95f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "counterScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CounterPresenter",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "일반 Kotlin 클래스 (ViewModel 아님!)",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$count",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            if (isLoading) {
                Text(
                    text = "Loading...",
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { presenter.decrement() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("−", fontSize = 18.sp)
                }
                Button(
                    onClick = { presenter.increment() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Button(
                    onClick = { presenter.asyncIncrement() },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Async +1", fontSize = 12.sp)
                }
                Button(
                    onClick = { presenter.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TodoPresenterCard(presenter: TodoPresenter) {
    val todos by presenter.todos.collectAsStateWithLifecycle()
    var todoCounter by remember { mutableIntStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TodoPresenter",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "일반 Kotlin 클래스 (ViewModel 아님!)",
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        presenter.addTodo("할 일 #$todoCounter")
                        todoCounter++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("추가", fontSize = 13.sp)
                }
                Button(
                    onClick = { presenter.clearAll() },
                    enabled = todos.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("전체 삭제", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (todos.isEmpty()) {
                Text(
                    text = "할 일이 없습니다. 추가 버튼을 눌러주세요.",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                todos.forEachIndexed { index, todo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = todo,
                                fontSize = 14.sp,
                                color = Color(0xFF424242)
                            )
                        }
                        IconButton(
                            onClick = { presenter.removeTodo(index) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "삭제",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationSupportDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Navigation 3 + retain",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "ViewModel이 ViewModelStoreNavEntryDecorator가 필요하듯,\n" +
                                "retain은 RetainedValuesStoreNavEntryDecorator가 필요합니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            BackstackSimulation()
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nav3 Decorator 비교",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DecoratorRow(
                        label = "ViewModel",
                        decorator = "ViewModelStoreNavEntryDecorator",
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DecoratorRow(
                        label = "retain",
                        decorator = "RetainedValuesStoreNavEntryDecorator",
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💡 가이드",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = RetainApiGuide.GUIDE_INFO.trimIndent(),
                        fontSize = 13.sp,
                        color = Color(0xFF424242),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DecoratorRow(label: String, decorator: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = decorator,
            fontSize = 11.sp,
            color = Color(0xFF424242),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun BackstackSimulation() {
    data class ScreenEntry(val name: String, val color: Color, val hasRetainedValue: Boolean)

    val backstack = remember {
        mutableStateListOf(
            ScreenEntry("Home", Color(0xFF1976D2), false)
        )
    }
    val retiredScreens = remember { mutableStateListOf<String>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "백스택 시뮬레이션",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "엔트리가 백스택에 있으면 retain 값 보존,\n제거되면 onRetired() 호출됨",
                fontSize = 13.sp,
                color = Color(0xFF757575),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val screens = listOf(
                            "Auth" to Color(0xFF4CAF50),
                            "Profile" to Color(0xFFFF9800),
                            "Settings" to Color(0xFF9C27B0),
                            "Detail" to Color(0xFF00BCD4)
                        )
                        val nextIdx = (backstack.size - 1).coerceIn(0, screens.size - 1)
                        if (nextIdx < screens.size) {
                            val (name, color) = screens[nextIdx]
                            backstack.add(ScreenEntry(name, color, true))
                        }
                    },
                    enabled = backstack.size < 5,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Push", fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        if (backstack.size > 1) {
                            val removed = backstack.removeAt(backstack.size - 1)
                            if (removed.hasRetainedValue) {
                                retiredScreens.add(removed.name)
                            }
                        }
                    },
                    enabled = backstack.size > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pop", fontSize = 13.sp)
                }
                Button(
                    onClick = {
                        while (backstack.size > 1) {
                            val removed = backstack.removeAt(backstack.size - 1)
                            if (removed.hasRetainedValue) {
                                retiredScreens.add(removed.name)
                            }
                        }
                    },
                    enabled = backstack.size > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pop All", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "백스택 (${backstack.size}개)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))

            backstack.reversed().forEachIndexed { idx, entry ->
                val isTop = idx == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isTop) entry.color.copy(alpha = 0.15f)
                            else Color(0xFFF5F5F5)
                        )
                        .border(
                            if (isTop) 2.dp else 1.dp,
                            if (isTop) entry.color else Color(0xFFE0E0E0),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(entry.color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.name,
                            fontSize = 14.sp,
                            fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                            color = Color(0xFF424242)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (entry.hasRetainedValue) {
                            Text(
                                text = "retain ✓",
                                fontSize = 11.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isTop) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "← TOP",
                                fontSize = 11.sp,
                                color = entry.color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (retiredScreens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "onRetired() 호출됨:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
                retiredScreens.forEach { name ->
                    Text(
                        text = "  ✗ $name → close() 호출",
                        fontSize = 12.sp,
                        color = Color(0xFFF44336),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RetainApiExampleUIPreview() {
    RetainApiExampleUI(onBackEvent = {})
}
