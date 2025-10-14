package com.example.composesample.presentation.example.component.architecture.lifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun AutoCloseableExampleUI(
    onBackEvent: () -> Unit,
    viewModel: AutoCloseableExampleViewModel = koinViewModel()
) {
    val items by viewModel.items.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val serviceStatus by viewModel.serviceStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "AutoCloseable ViewModel",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { ServiceStatusCard(serviceStatus) }
            item { TraditionalVsAutoCloseableCard() }
            item { ItemsServiceCard(items, viewModel::addItem) }
            item { CustomersServiceCard(customers, viewModel::addCustomer) }
            item { NetworkServiceCard(networkStatus, viewModel::connectNetwork, viewModel::disconnectNetwork) }
            item { BenefitsCard() }
            item { ImplementationGuideCard() }
        }
    }
}

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
                text = "🔧 AutoCloseable ViewModel Pattern",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ViewModel이 소멸될 때 자동으로 리소스를 정리하는 패턴입니다. onCleared() 오버라이드 없이 깔끔하게 메모리 누수를 방지할 수 있습니다.",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureChip("자동 정리", Color(0xFF4CAF50))
                FeatureChip("메모리 안전", Color(0xFF2196F3))
                FeatureChip("깔끔한 코드", Color(0xFFFF9800))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = "💡 이 화면을 나가면 모든 서비스가 자동으로 정리됩니다 (로그 확인)",
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
private fun ServiceStatusCard(serviceStatus: Map<String, ServiceStatusInfo>) {
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
                text = "📊 서비스 상태 모니터링",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "AutoCloseable 서비스들의 실시간 상태를 확인할 수 있습니다:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            serviceStatus.values.forEach { status ->
                ServiceStatusItem(status)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF388E3C).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "✅ 모든 서비스가 활성화되어 있습니다. 화면을 나가면 자동으로 정리됩니다.",
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
private fun ServiceStatusItem(status: ServiceStatusInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (status.isActive) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = status.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF388E3C)
            )
            Text(
                text = status.description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            text = if (status.isActive) "Active" else "Closed",
            fontSize = 12.sp,
            color = if (status.isActive) Color(0xFF4CAF50) else Color(0xFFD32F2F),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TraditionalVsAutoCloseableCard() {
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
                text = "⚖️ 전통적 방식 vs AutoCloseable",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComparisonSection(
                title = "❌ 전통적 방식",
                code = """
override fun onCleared() {
    super.onCleared()
    itemsService.cleanup()
    customersService.cleanup()
    networkService.cleanup()
}
                """.trimIndent(),
                issues = listOf(
                    "onCleared() 오버라이드 필요",
                    "각 서비스마다 cleanup() 호출",
                    "새 서비스 추가 시 누락 가능"
                ),
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ComparisonSection(
                title = "✅ AutoCloseable 방식",
                code = """
class HomeViewModel(
    private val itemsService: ItemsService,
    private val customersService: CustomersService,
    private val networkService: NetworkService
) : ViewModel(
    itemsService,
    customersService,
    networkService
) {
    // onCleared() 불필요!
}
                """.trimIndent(),
                issues = listOf(
                    "자동으로 close() 호출",
                    "보일러플레이트 코드 제거",
                    "타입 안전한 리소스 정리"
                ),
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun ComparisonSection(
    title: String,
    code: String,
    issues: List<String>,
    color: Color
) {
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
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFF80CBC4),
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        issues.forEach { issue ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun ItemsServiceCard(items: List<String>, onAddItem: (String) -> Unit) {
    var newItem by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Items",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🛒 ItemsService",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CoroutineScope를 관리하는 서비스입니다. close() 시 scope.cancel() 호출",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("새 아이템 입력", fontSize = 12.sp) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFE65100)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newItem.isNotBlank()) {
                            onAddItem(newItem)
                            newItem = ""
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFFE65100), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "현재 아이템 (${items.size}개):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Item",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CustomersServiceCard(customers: List<String>, onAddCustomer: (String) -> Unit) {
    var newCustomer by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Customers",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "👥 CustomersService",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "데이터베이스 연결을 관리하는 서비스입니다. close() 시 연결 종료",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCustomer,
                    onValueChange = { newCustomer = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("새 고객 입력", fontSize = 12.sp) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFD32F2F)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newCustomer.isNotBlank()) {
                            onAddCustomer(newCustomer)
                            newCustomer = ""
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFFD32F2F), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "현재 고객 (${customers.size}명):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            customers.forEach { customer ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Customer",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = customer,
                        fontSize = 14.sp,
                        color = Color(0xFF333333)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun NetworkServiceCard(
    status: String,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Network",
                    tint = Color(0xFF7B1FA2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🌐 NetworkService",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7B1FA2)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "네트워크 연결을 관리하는 서비스입니다. close() 시 연결 종료",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "연결 상태",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (status.contains("Connected")) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("연결", color = Color.White)
                }

                Button(
                    onClick = onDisconnect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Disconnect",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("종료", color = Color.White)
                }
            }
        }
    }
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
                text = "✨ AutoCloseable 패턴의 이점",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                "자동 리소스 정리" to "ViewModel 소멸 시 자동으로 close() 호출",
                "메모리 누수 방지" to "코루틴, 리스너, 연결 등 확실한 정리",
                "깔끔한 코드" to "onCleared() 오버라이드 불필요",
                "타입 안전성" to "인터페이스 계약으로 close() 구현 강제",
                "테스트 용이성" to "Mock 객체 주입 및 검증 간편"
            ).forEach { (title, description) ->
                BenefitItem(title, description)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BenefitItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Benefit",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(20.dp)
        )
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
}

@Composable
private fun ImplementationGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📚 구현 가이드",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ImplementationStep(
                step = "1",
                title = "Service에 Closeable 구현",
                code = """
interface ItemsService : Closeable {
    val items: Flow<List<Item>>
}

class RealItemsService : ItemsService {
    private val scope = CoroutineScope(...)
    
    override fun close() {
        scope.cancel()
    }
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ImplementationStep(
                step = "2",
                title = "ViewModel 생성자에 전달",
                code = """
class HomeViewModel(
    private val itemsService: ItemsService,
    private val customersService: CustomersService
) : ViewModel(
    itemsService,
    customersService
) {
    // onCleared() 불필요!
}
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "🎉 완성! ViewModel 소멸 시 자동으로 모든 서비스의 close()가 호출됩니다.",
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
private fun ImplementationStep(step: String, title: String, code: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1976D2)
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

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1976D2)
            )
        }

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
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color(0xFF80CBC4),
                lineHeight = 12.sp
            )
        }
    }
}
