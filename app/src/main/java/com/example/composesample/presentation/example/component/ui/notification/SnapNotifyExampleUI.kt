package com.example.composesample.presentation.example.component.ui.notification

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SnapNotifyExampleUI(
    onBackEvent: () -> Unit
) {
    var formData by remember { mutableStateOf(FormData()) }
    var cartItems by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            MainHeader(
                title = "SnapNotify Example",
                onBackIconClicked = onBackEvent
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { BasicUsageCard(snackbarHostState) }
                item { InteractiveSnackbarsCard(snackbarHostState) }
                item {
                    RealWorldUseCasesCard(
                        formData = formData,
                        onFormDataChange = { formData = it },
                        cartItems = cartItems,
                        onCartChange = { cartItems = it },
                        isLoading = isLoading,
                        onLoadingChange = { isLoading = it },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

data class FormData(
    val email: String = "",
    val password: String = "",
    val isValid: Boolean = false
)

@Composable
private fun BasicUsageCard(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
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
                text = "🎯 기본 사용법",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "가장 간단한 SnapNotify 사용 예시들:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicUsageButton(
                    text = "기본 메시지",
                    description = "기본 Snackbar 표시",
                    color = Color(0xFF1976D2)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("기본 메시지입니다")
                    }
                }

                BasicUsageButton(
                    text = "액션 포함 메시지",
                    description = "actionLabel과 콜백 포함",
                    color = Color(0xFF7B1FA2)
                ) {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "액션이 포함된 메시지입니다",
                            actionLabel = "실행"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            snackbarHostState.showSnackbar("액션이 실행되었습니다!")
                        }
                    }
                }

                BasicUsageButton(
                    text = "긴 메시지 테스트",
                    description = "여러 줄에 걸친 긴 메시지",
                    color = Color(0xFFD32F2F)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("이것은 매우 긴 메시지입니다. Snackbar는 자동으로 메시지를 적절히 처리하여 사용자에게 보여줍니다.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 모든 메시지는 자동으로 큐에 추가되어 순차적으로 표시됩니다!",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun BasicUsageButton(
    text: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun InteractiveSnackbarsCard(snackbarHostState: SnackbarHostState) {
    var undoCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

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
                text = "🎮 인터랙티브 Snackbar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "액션 버튼이 포함된 상호작용 가능한 Snackbar:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "🗑️ 파일이 삭제되었습니다",
                                actionLabel = "실행취소"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                undoCount++
                                snackbarHostState.showSnackbar("✅ 파일 삭제가 취소되었습니다")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("파일 삭제 (실행취소 가능)", color = Color.White, fontSize = 14.sp)
                }

                Button(
                    onClick = {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "🌐 네트워크 연결에 실패했습니다",
                                actionLabel = "재시도"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                snackbarHostState.showSnackbar("🔄 재시도 중...")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Network",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("네트워크 오류 (재시도 가능)", color = Color.White, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (undoCount > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "✅ 실행취소 버튼이 ${undoCount}번 클릭되었습니다!",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 액션 버튼을 통해 사용자가 즉시 대응할 수 있는 인터페이스를 제공합니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFD32F2F),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun RealWorldUseCasesCard(
    formData: FormData,
    onFormDataChange: (FormData) -> Unit,
    cartItems: Int,
    onCartChange: (Int) -> Unit,
    isLoading: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
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
                text = "🌟 실제 사용 사례",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "실제 앱에서 자주 사용되는 SnapNotify 활용 예시들:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            UseCaseSection(
                title = "📝 폼 검증 및 제출",
                description = "이메일 검증과 성공 피드백"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (formData.email.contains("@")) {
                                    snackbarHostState.showSnackbar("✅ 회원가입이 완료되었습니다!")
                                    onFormDataChange(formData.copy(isValid = true))
                                } else {
                                    snackbarHostState.showSnackbar("❌ 올바른 이메일 주소를 입력해주세요")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("유효한 이메일", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("❌ 이메일 형식이 올바르지 않습니다")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("잘못된 이메일", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            UseCaseSection(
                title = "🛒 쇼핑카트 작업",
                description = "상품 추가/제거 피드백 (현재: ${cartItems}개)"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onCartChange(cartItems + 1)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "🛒 상품이 장바구니에 추가되었습니다",
                                    actionLabel = "보기"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    snackbarHostState.showSnackbar("👀 장바구니를 확인하세요!")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("추가", color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (cartItems > 0) {
                                val currentItems = cartItems
                                onCartChange(cartItems - 1)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "🗑️ 상품이 제거되었습니다",
                                        actionLabel = "실행취소"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onCartChange(currentItems)
                                        snackbarHostState.showSnackbar("✅ 상품이 복원되었습니다")
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("ℹ️ 장바구니가 비어있습니다")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("제거", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            UseCaseSection(
                title = "🌐 네트워크 작업",
                description = "로딩 상태와 결과 피드백"
            ) {
                Button(
                    onClick = {
                        if (!isLoading) {
                            onLoadingChange(true)
                            scope.launch {
                                snackbarHostState.showSnackbar("⏳ 데이터를 불러오는 중...")

                                delay(3000)
                                onLoadingChange(false)
                                snackbarHostState.showSnackbar("✅ 데이터 로드가 완료되었습니다")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLoading) Color.Gray else Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("로딩 중...", color = Color.White, fontSize = 14.sp)
                    } else {
                        Text("네트워크 요청 시뮬레이션", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 실제 프로덕션 환경에서는 이러한 패턴들이 사용자 경험을 크게 향상시킵니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF7B1FA2),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun UseCaseSection(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7B1FA2)
        )
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}