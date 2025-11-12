package com.example.composesample.presentation.example.component.architecture.development.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
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
    data class Success<T>(val data: T, val timestamp: Long = System.currentTimeMillis()) :
        ApiResult<T>()

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

enum class StatusEnum {
    SUCCESS, ERROR, LOADING
}

sealed class StatusSealed {
    data class Success(val data: String, val timestamp: Long) : StatusSealed()
    data class Error(val exception: Exception, val retryCount: Int) : StatusSealed()
    object Loading : StatusSealed()
}

data class User(val id: String, val name: String, val email: String)
data class Product(val id: String, val name: String, val price: Double)

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
            item { ApiResultDemoCard() }
            item { UiStateDemoCard() }
            item { SealedInterfaceDemoCard() }
            item { NavigationEventDemoCard() }
            item { NestedSealedDemoCard() }
            item { SealedVsEnumDemoCard() }
            item { GenericTypeDemoCard() }
        }
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
private fun androidx.compose.foundation.layout.RowScope.StateButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
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
                    onClick = {
                        events = events + NavigationEvent.ToExternal("https://example.com")
                    },
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
                        paymentStatus =
                            PaymentStatus.Completed.Success("TX-${System.currentTimeMillis()}")
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
private fun SealedVsEnumDemoCard() {
    var useEnum by remember { mutableStateOf(true) }
    var enumStatus by remember { mutableStateOf(StatusEnum.SUCCESS) }
    var sealedStatus by remember { mutableStateOf<StatusSealed>(StatusSealed.Loading) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ Sealed Class vs Enum",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enum과 Sealed Class의 차이를 실제로 비교",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { useEnum = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (useEnum) Color(0xFFF57F17) else Color(0xFFFFE082)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Enum 사용",
                        color = if (useEnum) Color.White else Color(0xFFF57F17),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { useEnum = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (!useEnum) Color(0xFFF57F17) else Color(0xFFFFE082)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Sealed 사용",
                        color = if (!useEnum) Color.White else Color(0xFFF57F17),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (useEnum) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { enumStatus = StatusEnum.SUCCESS },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Success", color = Color.White, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { enumStatus = StatusEnum.ERROR },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Error", color = Color.White, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { enumStatus = StatusEnum.LOADING },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Loading", color = Color.White, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "현재 상태: $enumStatus",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ 추가 정보를 저장할 수 없습니다",
                            fontSize = 11.sp,
                            color = Color(0xFFFF5722)
                        )
                        Text(
                            text = "• 에러 메시지를 포함할 수 없음",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• 데이터나 타임스탬프를 저장할 수 없음",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            sealedStatus = StatusSealed.Success(
                                data = "User data loaded",
                                timestamp = System.currentTimeMillis()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Success", color = Color.White, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            sealedStatus = StatusSealed.Error(
                                exception = Exception("Network timeout"),
                                retryCount = 3
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Error", color = Color.White, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { sealedStatus = StatusSealed.Loading },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Loading", color = Color.White, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        when (sealedStatus) {
                            is StatusSealed.Success -> {
                                Text(
                                    text = "✓ Success 상태",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "📦 데이터: ${(sealedStatus as StatusSealed.Success).data}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "⏰ 시간: ${
                                        java.text.SimpleDateFormat("HH:mm:ss")
                                            .format((sealedStatus as StatusSealed.Success).timestamp)
                                    }",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            is StatusSealed.Error -> {
                                Text(
                                    text = "✗ Error 상태",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5722)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "❌ 에러: ${(sealedStatus as StatusSealed.Error).exception.message}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "🔄 재시도 횟수: ${(sealedStatus as StatusSealed.Error).retryCount}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            StatusSealed.Loading -> {
                                Text(
                                    text = "⏳ Loading 상태",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "상태 없음 (object 사용)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF57F17).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 핵심 차이점",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enum: 단순한 상태만 표현\nSealed: 상태 + 데이터를 함께 관리",
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
private fun GenericTypeDemoCard() {
    var selectedType by remember { mutableStateOf("String") }
    var stringResult by remember { mutableStateOf<ApiResult<String>>(ApiResult.Empty) }
    var userResult by remember { mutableStateOf<ApiResult<User>>(ApiResult.Empty) }
    var productResult by remember { mutableStateOf<ApiResult<Product>>(ApiResult.Empty) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔤 제네릭 타입 파라미터",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ApiResult<T>를 다양한 타입에 재사용",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { selectedType = "String" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedType == "String") Color(0xFF388E3C) else Color(
                            0xFFC8E6C9
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "String",
                        color = if (selectedType == "String") Color.White else Color(0xFF388E3C),
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { selectedType = "User" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedType == "User") Color(0xFF388E3C) else Color(
                            0xFFC8E6C9
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "User",
                        color = if (selectedType == "User") Color.White else Color(0xFF388E3C),
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { selectedType = "Product" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedType == "Product") Color(0xFF388E3C) else Color(
                            0xFFC8E6C9
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Product",
                        color = if (selectedType == "Product") Color.White else Color(0xFF388E3C),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedType) {
                "String" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ApiResult<String>",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        stringResult = ApiResult.Loading
                                        delay(1500)
                                        stringResult = ApiResult.Success("Hello, Kotlin!")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(
                                        0xFF388E3C
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load String Data", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (stringResult) {
                                is ApiResult.Success -> {
                                    Text(
                                        text = "✓ 데이터: ${(stringResult as ApiResult.Success).data}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF388E3C)
                                    )
                                }

                                is ApiResult.Error -> {
                                    Text(
                                        text = "✗ 에러: ${(stringResult as ApiResult.Error).message}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF5722)
                                    )
                                }

                                ApiResult.Loading -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF388E3C)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading...", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }

                                ApiResult.Empty -> {
                                    Text("버튼을 눌러 데이터 로드", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                "User" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ApiResult<User>",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        userResult = ApiResult.Loading
                                        delay(1500)
                                        userResult = ApiResult.Success(
                                            User(
                                                id = "user_123",
                                                name = "김개발",
                                                email = "dev@example.com"
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(
                                        0xFF388E3C
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load User Data", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (userResult) {
                                is ApiResult.Success -> {
                                    val user = (userResult as ApiResult.Success).data
                                    Column {
                                        Text(
                                            text = "✓ User 정보:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF388E3C)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "• ID: ${user.id}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "• Name: ${user.name}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "• Email: ${user.email}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                is ApiResult.Error -> {
                                    Text(
                                        text = "✗ 에러: ${(userResult as ApiResult.Error).message}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF5722)
                                    )
                                }

                                ApiResult.Loading -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF388E3C)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading...", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }

                                ApiResult.Empty -> {
                                    Text("버튼을 눌러 데이터 로드", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                "Product" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ApiResult<Product>",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        productResult = ApiResult.Loading
                                        delay(1500)
                                        productResult = ApiResult.Success(
                                            Product(
                                                id = "prod_456",
                                                name = "Kotlin 프로그래밍",
                                                price = 35000.0
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(
                                        0xFF388E3C
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load Product Data", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when (productResult) {
                                is ApiResult.Success -> {
                                    val product = (productResult as ApiResult.Success).data
                                    Column {
                                        Text(
                                            text = "✓ Product 정보:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF388E3C)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "• ID: ${product.id}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "• Name: ${product.name}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "• Price: ${String.format("%,.0f", product.price)}원",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                is ApiResult.Error -> {
                                    Text(
                                        text = "✗ 에러: ${(productResult as ApiResult.Error).message}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFF5722)
                                    )
                                }

                                ApiResult.Loading -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF388E3C)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Loading...", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }

                                ApiResult.Empty -> {
                                    Text("버튼을 눌러 데이터 로드", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "✓ 제네릭 타입의 장점",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF388E3C)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 하나의 ApiResult로 모든 타입 처리\n• 타입 안전성 보장\n• 코드 재사용성 극대화",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}