package com.example.composesample.presentation.example.component.architecture.development.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val timestamp: Long = System.currentTimeMillis()) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
    object Empty : ApiResult<Nothing>()
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Content(val items: List<String>, val count: Int) : UiState()
    data class Error(val message: String, val canRetry: Boolean = true) : UiState()
}

sealed interface NetworkState
sealed interface CacheState

data class OnlineState(val connectionType: String) : NetworkState, CacheState
data class OfflineState(val reason: String) : NetworkState
data class CachedData(val items: List<String>) : CacheState

sealed class NavigationEvent {
    object Back : NavigationEvent()
    data class ToDetail(val id: String) : NavigationEvent()
    data class ToExternal(val url: String) : NavigationEvent()
    data class ShowDialog(val title: String, val message: String) : NavigationEvent()
}

sealed class PaymentStatus {
    object Pending : PaymentStatus()
    
    sealed class InProgress : PaymentStatus() {
        data class Processing(val progress: Int) : InProgress()
        data class Verifying(val attempts: Int) : InProgress()
    }
    
    sealed class Completed : PaymentStatus() {
        data class Success(val transactionId: String) : Completed()
        data class Failed(val reason: String) : Completed()
    }
}

@Composable
fun SealedClassInterfaceExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Sealed Classes",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroductionCard() }
            item { ApiResultDemoCard() }
            item { UiStateDemoCard() }
            item { SealedInterfaceDemoCard() }
            item { NavigationEventDemoCard() }
            item { NestedSealedDemoCard() }
            item { WhenExpressionCard() }
        }
    }
}

@Composable
private fun IntroductionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Sealed Classes란?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "제한된 클래스 계층 구조:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    BulletPoint("모든 하위 타입이 컴파일 타임에 알려짐")
                    BulletPoint("when 표현식에서 else 분기 불필요")
                    BulletPoint("타입 안전한 상태 관리 가능")
                    BulletPoint("리팩토링 시 컴파일 에러로 누락 방지")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFF1976D2).copy(alpha = 0.2f))

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "주요 사용처",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UseCaseChip("API 응답", Color(0xFF4CAF50))
                UseCaseChip("UI 상태", Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UseCaseChip("이벤트", Color(0xFFFF9800))
                UseCaseChip("네비게이션", Color(0xFF9C27B0))
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color(0xFF1976D2), CircleShape)
                .padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF666666),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun UseCaseChip(text: String, color: Color) {
    Surface(
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
private fun ApiResultDemoCard() {
    var apiState by remember { mutableStateOf<ApiResult<List<String>>>(ApiResult.Empty) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📡 API 응답 상태",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "sealed class로 API 응답 상태를 타입 안전하게 관리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeBox(
                """
                sealed class ApiResult<out T> {
                    data class Success<T>(val data: T) : ApiResult<T>()
                    data class Error(val message: String) : ApiResult<Nothing>()
                    object Loading : ApiResult<Nothing>()
                    object Empty : ApiResult<Nothing>()
                }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            apiState = ApiResult.Loading
                            delay(2000)
                            apiState = ApiResult.Success(
                                listOf("Item 1", "Item 2", "Item 3")
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Success", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        scope.launch {
                            apiState = ApiResult.Loading
                            delay(1500)
                            apiState = ApiResult.Error("Network timeout")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Error", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ApiResultView(apiState)
        }
    }
}

@Composable
private fun ApiResultView(state: ApiResult<List<String>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is ApiResult.Success -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Success",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        state.data.forEach { item ->
                            Text(
                                text = "• $item",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                        Text(
                            text = state.message,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
                ApiResult.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                ApiResult.Empty -> {
                    Text(
                        text = "버튼을 눌러 API 호출 시뮬레이션",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun UiStateDemoCard() {
    var uiState by remember { mutableStateOf<UiState>(UiState.Idle) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🖥️ UI 상태 관리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "화면 상태를 명확하게 정의하고 관리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeBox(
                """
                sealed class UiState {
                    object Idle : UiState()
                    object Loading : UiState()
                    data class Content(val items: List<String>) : UiState()
                    data class Error(val message: String) : UiState()
                }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StateButton(
                    text = "Load",
                    color = Color(0xFF2196F3),
                    onClick = {
                        scope.launch {
                            uiState = UiState.Loading
                            delay(2000)
                            uiState = UiState.Content(
                                items = List(5) { "Item ${it + 1}" },
                                count = 5
                            )
                        }
                    }
                )

                StateButton(
                    text = "Error",
                    color = Color(0xFFFF5722),
                    onClick = {
                        uiState = UiState.Error("Failed to load data", canRetry = true)
                    }
                )

                StateButton(
                    text = "Reset",
                    color = Color(0xFF9E9E9E),
                    onClick = {
                        uiState = UiState.Idle
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            UiStateView(uiState) {
                scope.launch {
                    uiState = UiState.Loading
                    delay(1500)
                    uiState = UiState.Content(
                        items = List(5) { "Item ${it + 1}" },
                        count = 5
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.StateButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun UiStateView(state: UiState, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                UiState.Idle -> {
                    Text(
                        text = "Ready to load data",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                UiState.Loading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                is UiState.Content -> {
                    Column {
                        Text(
                            text = "Content (${state.count} items)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.items.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            fontSize = 13.sp,
                            color = Color(0xFFFF5722),
                            fontWeight = FontWeight.Medium
                        )
                        if (state.canRetry) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF2196F3)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retry", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SealedInterfaceDemoCard() {
    var networkState by remember { mutableStateOf<NetworkState>(OnlineState("WiFi")) }
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔗 Sealed Interface",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "다중 상속 가능한 sealed interface (Kotlin 1.5+)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeBox(
                """
                sealed interface NetworkState
                sealed interface CacheState

                data class OnlineState(val type: String) : NetworkState, CacheState
                data class OfflineState(val reason: String) : NetworkState
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { networkState = OnlineState("WiFi") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Online", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { networkState = OfflineState("No connection") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Offline", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            NetworkStateView(networkState)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showExplanation = !showExplanation },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (showExplanation) "설명 닫기" else "sealed interface 장점 보기",
                    color = Color(0xFF9C27B0),
                    fontSize = 12.sp
                )
            }

            AnimatedVisibility(
                visible = showExplanation,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF9C27B0).copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✓ 다중 상속 가능",
                                fontSize = 12.sp,
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "OnlineState가 NetworkState와 CacheState 모두 구현",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ 더 유연한 타입 계층",
                                fontSize = 12.sp,
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "여러 sealed 타입을 조합하여 복잡한 상태 표현",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkStateView(state: NetworkState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (state) {
                is OnlineState -> {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Online",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Connected via ${state.connectionType}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Implements: NetworkState, CacheState",
                            fontSize = 10.sp,
                            color = Color(0xFF9C27B0),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                is OfflineState -> {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Offline",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                        Text(
                            text = state.reason,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Implements: NetworkState",
                            fontSize = 10.sp,
                            color = Color(0xFF9C27B0),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationEventDemoCard() {
    var events by remember { mutableStateOf<List<NavigationEvent>>(emptyList()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🧭 Navigation Event",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57C00)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "sealed class로 네비게이션 이벤트 타입 안전하게 처리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeBox(
                """
                sealed class NavigationEvent {
                    object Back : NavigationEvent()
                    data class ToDetail(val id: String) : NavigationEvent()
                    data class ToExternal(val url: String) : NavigationEvent()
                }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { events = events + NavigationEvent.Back },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Back", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = { events = events + NavigationEvent.ToDetail("123") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Detail", color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { events = events + NavigationEvent.ToExternal("https://example.com") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("External", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = { events = emptyList() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear", color = Color.White, fontSize = 11.sp)
                }
            }

            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Event Log (${events.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        events.takeLast(5).forEach { event ->
                            NavigationEventItem(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationEventItem(event: NavigationEvent) {
    val (icon, text, color) = when (event) {
        NavigationEvent.Back -> Triple("⬅️", "Back", Color(0xFF9E9E9E))
        is NavigationEvent.ToDetail -> Triple("📄", "ToDetail(id=${event.id})", Color(0xFF2196F3))
        is NavigationEvent.ToExternal -> Triple("🌐", "ToExternal(${event.url})", Color(0xFF4CAF50))
        is NavigationEvent.ShowDialog -> Triple("💬", "ShowDialog", Color(0xFFFF9800))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

@Composable
private fun NestedSealedDemoCard() {
    var paymentStatus by remember { mutableStateOf<PaymentStatus>(PaymentStatus.Pending) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8EAF6),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💳 Nested Sealed Classes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "계층적 상태를 중첩된 sealed class로 표현",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeBox(
                """
                sealed class PaymentStatus {
                    object Pending : PaymentStatus()
                    sealed class InProgress : PaymentStatus() {
                        data class Processing(val progress: Int) : InProgress()
                        data class Verifying(val attempts: Int) : InProgress()
                    }
                    sealed class Completed : PaymentStatus() {
                        data class Success(val txId: String) : Completed()
                        data class Failed(val reason: String) : Completed()
                    }
                }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        paymentStatus = PaymentStatus.Pending
                        delay(500)
                        paymentStatus = PaymentStatus.InProgress.Processing(30)
                        delay(800)
                        paymentStatus = PaymentStatus.InProgress.Processing(70)
                        delay(800)
                        paymentStatus = PaymentStatus.InProgress.Verifying(1)
                        delay(1000)
                        paymentStatus = PaymentStatus.Completed.Success("TX-${System.currentTimeMillis()}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("결제 프로세스 시작", color = Color.White, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            PaymentStatusView(paymentStatus)
        }
    }
}

@Composable
private fun PaymentStatusView(status: PaymentStatus) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (status) {
                PaymentStatus.Pending -> {
                    StatusIndicator("대기 중", Color(0xFF9E9E9E), "⏳")
                }
                is PaymentStatus.InProgress.Processing -> {
                    StatusIndicator("처리 중", Color(0xFF2196F3), "⚙️")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "진행률: ${status.progress}%",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                is PaymentStatus.InProgress.Verifying -> {
                    StatusIndicator("검증 중", Color(0xFFFF9800), "🔍")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "시도: ${status.attempts}회",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                is PaymentStatus.Completed.Success -> {
                    StatusIndicator("완료", Color(0xFF4CAF50), "✅")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "거래 ID: ${status.transactionId}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
                is PaymentStatus.Completed.Failed -> {
                    StatusIndicator("실패", Color(0xFFFF5722), "❌")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "사유: ${status.reason}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF5722)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(label: String, color: Color, emoji: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun WhenExpressionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 Exhaustive when",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF607D8B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "sealed class는 when 표현식에서 else 불필요",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeBox(
                """
                fun handle(result: ApiResult<String>) = when (result) {
                    is ApiResult.Success -> result.data
                    is ApiResult.Error -> result.message
                    ApiResult.Loading -> "Loading..."
                    ApiResult.Empty -> "No data"
                    // else 불필요! 모든 케이스가 처리됨
                }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "✓ 컴파일 타임 안전성",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 새로운 하위 타입 추가 시 컴파일 에러\n• 모든 케이스 처리 강제\n• 리팩토링 안전성 향상",
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
private fun CodeBox(code: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF263238)
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF80CBC4),
            lineHeight = 14.sp
        )
    }
}

