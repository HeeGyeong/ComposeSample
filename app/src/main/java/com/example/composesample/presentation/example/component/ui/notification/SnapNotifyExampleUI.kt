package com.example.composesample.presentation.example.component.ui.notification

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
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
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
    // 실제 사용 사례를 위한 상태들
    var formData by remember { mutableStateOf(FormData()) }
    var cartItems by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // Snackbar 상태 관리
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
                item { OverviewCard() }
                item { ComparisonCard() }
                item { BasicUsageCard(snackbarHostState) }
                item { ThemedMessagesCard(snackbarHostState) }
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
                item { IntegrationGuideCard() }
            }
        }
    }
}

// 데이터 클래스들
data class FormData(
    val email: String = "",
    val password: String = "",
    val isValid: Boolean = false
)

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🚀 SnapNotify: 혁신적인 Snackbar 라이브러리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "15줄 이상의 복잡한 Snackbar 코드를 단 1줄로 간소화하는 혁신적인 라이브러리입니다.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("1줄 코드", Color(0xFF4CAF50))
                FeatureChip("자동 상태 관리", Color(0xFF2196F3))
                FeatureChip("스레드 안전", Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "💡 GitHub: github.com/ivamsi/snapnotify | Maven: io.github.ivamsi:snapnotify:1.0.2",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 11.sp,
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
private fun ComparisonCard() {
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
                text = "📊 기존 방식 vs SnapNotify 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 기존 방식
            ComparisonSection(
                title = "😰 기존 Compose 방식 (15+ 줄)",
                color = Color(0xFFD32F2F),
                code = """
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { paddingValues ->
    Button(
        onClick = {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "저장되었습니다!",
                    actionLabel = "확인"
                )
            }
        }
    ) { Text("저장") }
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SnapNotify 방식
            ComparisonSection(
                title = "🎉 SnapNotify 방식 (1줄)",
                color = Color(0xFF388E3C),
                code = """
Button(
    onClick = {
        SnapNotify.success("저장되었습니다!")
    }
) { Text("저장") }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "✨ 93% 코드 감소! 상태 관리, 스레드 처리, 큐잉이 모두 자동화됩니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ComparisonSection(title: String, color: Color, code: String) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )

        Spacer(modifier = Modifier.height(8.dp))

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
}

@Composable
private fun BasicUsageCard(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
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
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
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
private fun ThemedMessagesCard(snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
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
                text = "🎨 테마별 메시지",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "상황에 맞는 4가지 테마 메시지를 제공합니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemedMessageButton(
                    text = "성공",
                    icon = Icons.Filled.Favorite,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("✅ 작업이 성공적으로 완료되었습니다!")
                    }
                }

                ThemedMessageButton(
                    text = "오류",
                    icon = Icons.Filled.Warning,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("❌ 오류가 발생했습니다. 다시 시도해주세요.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemedMessageButton(
                    text = "경고",
                    icon = Icons.Filled.Warning,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("⚠️ 주의: 이 작업은 되돌릴 수 없습니다.")
                    }
                }

                ThemedMessageButton(
                    text = "정보",
                    icon = Icons.Filled.Info,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("ℹ️ 새로운 업데이트가 사용 가능합니다.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎯 각 테마는 Material Design 가이드라인에 따른 색상과 아이콘을 사용합니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFE65100),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun ThemedMessageButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
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
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)),
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2196F3)),
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
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
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

            // 폼 검증 사례
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
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
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
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F))
                    ) {
                        Text("잘못된 이메일", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 쇼핑카트 사례
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
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
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
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800))
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

            // 네트워크 작업 사례
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

                                // 3초 후 완료 시뮬레이션
                                delay(3000)
                                onLoadingChange(false)
                                snackbarHostState.showSnackbar("✅ 데이터 로드가 완료되었습니다")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isLoading) Color.Gray else Color(0xFF2196F3)
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

@Composable
private fun IntegrationGuideCard() {
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
                text = "🔧 통합 가이드",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "프로젝트에 SnapNotify를 통합하는 단계별 가이드:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            IntegrationStep(
                step = "1",
                title = "의존성 추가",
                description = "build.gradle.kts에 라이브러리 추가",
                code = "implementation(\"io.github.ivamsi:snapnotify:1.0.2\")"
            )

            Spacer(modifier = Modifier.height(12.dp))

            IntegrationStep(
                step = "2",
                title = "Provider 설정",
                description = "앱 전체 또는 특정 화면에 Provider 래핑",
                code = """
SnapNotifyProvider {
    MyApp() // 또는 특정 화면
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            IntegrationStep(
                step = "3",
                title = "Hilt 설정 (선택사항)",
                description = "더 나은 의존성 관리를 위한 Hilt 통합",
                code = """
@HiltAndroidApp
class MyApplication : Application()
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            IntegrationStep(
                step = "4",
                title = "사용하기",
                description = "어디서든 간단하게 호출",
                code = """
// 기본 사용
SnapNotify.show("메시지")

// 테마별 사용
SnapNotify.success("성공!")
SnapNotify.error("오류 발생")

// 액션 포함
SnapNotify.show("메시지", "액션") { /* 처리 */ }
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "✅ 설정 완료! 이제 프로젝트 어디서든 SnapNotify를 사용할 수 있습니다.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF388E3C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun IntegrationStep(
    step: String,
    title: String,
    description: String,
    code: String
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF388E3C)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF388E3C)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF263238)
        ) {
            Text(
                text = code,
                modifier = Modifier.padding(8.dp),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF80CBC4),
                lineHeight = 12.sp
            )
        }
    }
}

