package com.example.composesample.presentation.example.component.architecture.development.performance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }
    
    override fun toString(): String = "UserId($value)"
}

@JvmInline
value class ProductId(val value: String) {
    init {
        require(value.isNotBlank()) { "ProductId cannot be blank" }
    }
    
    override fun toString(): String = "ProductId($value)"
}

@JvmInline
value class Money(val cents: Long) {
    init {
        require(cents >= 0) { "Money cannot be negative" }
    }
    
    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun minus(other: Money) = Money(cents - other.cents)
    operator fun times(multiplier: Int) = Money(cents * multiplier)
    
    fun toDisplayString(): String = "$${cents / 100}.${String.format("%02d", cents % 100)}"
    override fun toString(): String = "Money(${toDisplayString()})"
}

@JvmInline
value class Email(val value: String) {
    init {
        require("@" in value) { "Invalid email format" }
    }
    
    override fun toString(): String = "Email($value)"
}

inline fun <T> measure(label: String, block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = System.currentTimeMillis() - start
    return result to elapsed
}

inline fun <reified T> castOrNull(any: Any?): T? {
    return any as? T
}

inline fun <reified T> isInstance(any: Any?): Boolean {
    return any is T
}

fun <T> measureNonInline(label: String, block: () -> T): Pair<T, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    val elapsed = System.currentTimeMillis() - start
    return result to elapsed
}

data class CartItem(
    val productId: ProductId,
    val name: String,
    val price: Money,
    val quantity: Int
)

@Composable
fun InlineValueClassExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Inline & Value Classes",
            onBackIconClicked = onBackEvent
        )

        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color(0xFF1976D2),
            contentColor = Color.White
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Value Classes", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Reified", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("성능 비교", fontSize = 12.sp) }
            )
        }

        when (selectedTab) {
            0 -> ValueClassesTab()
            1 -> ReifiedGenericsTab()
            2 -> PerformanceComparisonTab()
        }
    }
}

@Composable
private fun ValueClassesTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TypeSafetyDemoCard() }
        item { MoneyCalculatorCard() }
        item { ValidationDemoCard() }
    }
}

@Composable
private fun TypeSafetyDemoCard() {
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var productIdInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("1") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛒 장바구니 시뮬레이터",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Value Classes로 타입 안전성 보장",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = productIdInput,
                onValueChange = { 
                    productIdInput = it
                    errorMessage = null
                },
                label = { Text("Product ID") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF388E3C),
                    focusedLabelColor = Color(0xFF388E3C)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { 
                    nameInput = it
                    errorMessage = null
                },
                label = { Text("상품명") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF388E3C),
                    focusedLabelColor = Color(0xFF388E3C)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { 
                        priceInput = it
                        errorMessage = null
                    },
                    label = { Text("가격 (센트)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF388E3C),
                        focusedLabelColor = Color(0xFF388E3C)
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { 
                        quantityInput = it
                        errorMessage = null
                    },
                    label = { Text("수량") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF388E3C),
                        focusedLabelColor = Color(0xFF388E3C)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            fontSize = 12.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    try {
                        val productId = ProductId(productIdInput.trim())
                        val price = Money(priceInput.toLong())
                        val quantity = quantityInput.toInt()

                        if (nameInput.isBlank()) {
                            errorMessage = "상품명을 입력하세요"
                            return@Button
                        }

                        cartItems.add(
                            CartItem(
                                productId = productId,
                                name = nameInput,
                                price = price,
                                quantity = quantity
                            )
                        )

                        productIdInput = ""
                        nameInput = ""
                        priceInput = ""
                        quantityInput = "1"
                    } catch (e: Exception) {
                        errorMessage = when (e) {
                            is IllegalArgumentException -> e.message ?: "검증 실패"
                            is NumberFormatException -> "숫자를 입력하세요"
                            else -> "알 수 없는 오류"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("장바구니 추가", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (cartItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                val totalAmount = cartItems.fold(Money(0)) { acc, item ->
                    acc + (item.price * item.quantity)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "장바구니",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF388E3C)
                            )
                            Text(
                                text = "총액: ${totalAmount.toDisplayString()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        cartItems.forEachIndexed { index, item ->
                            CartItemRow(
                                item = item,
                                onDelete = { cartItems.removeAt(index) }
                            )
                            if (index < cartItems.size - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "${item.productId} • ${item.price.toDisplayString()} × ${item.quantity}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = (item.price * item.quantity).toDisplayString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
private fun MoneyCalculatorCard() {
    var amount1Input by remember { mutableStateOf("") }
    var amount2Input by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("+") }
    var result by remember { mutableStateOf<Money?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💰 Money 계산기",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "연산자 오버로딩으로 안전한 금액 계산",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount1Input,
                    onValueChange = { 
                        amount1Input = it
                        error = null
                    },
                    label = { Text("금액 1 (센트)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFFF9800),
                        focusedLabelColor = Color(0xFFFF9800)
                    ),
                    singleLine = true
                )

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            operation = when (operation) {
                                "+" -> "-"
                                "-" -> "+"
                                else -> "+"
                            }
                        },
                    shape = CircleShape,
                    color = Color(0xFFFF9800)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = operation,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                OutlinedTextField(
                    value = amount2Input,
                    onValueChange = { 
                        amount2Input = it
                        error = null
                    },
                    label = { Text("금액 2 (센트)", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFFF9800),
                        focusedLabelColor = Color(0xFFFF9800)
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            error?.let { err ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = err, fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    try {
                        val money1 = Money(amount1Input.toLong())
                        val money2 = Money(amount2Input.toLong())

                        result = when (operation) {
                            "+" -> money1 + money2
                            "-" -> money1 - money2
                            else -> money1 + money2
                        }
                        error = null
                    } catch (e: Exception) {
                        error = when (e) {
                            is IllegalArgumentException -> e.message ?: "검증 실패"
                            is NumberFormatException -> "숫자를 입력하세요"
                            else -> "계산 오류"
                        }
                        result = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("계산하기", color = Color.White, fontWeight = FontWeight.Bold)
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "결과",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.toDisplayString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${res.cents} cents",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationDemoCard() {
    var emailInput by remember { mutableStateOf("") }
    var emailResult by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✉️ Email 검증 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Value Class의 init 블록으로 자동 검증",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = emailInput,
                onValueChange = { 
                    emailInput = it
                    emailError = null
                    emailResult = null
                },
                label = { Text("이메일 주소") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1976D2),
                    focusedLabelColor = Color(0xFF1976D2)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    try {
                        val email = Email(emailInput.trim())
                        emailResult = "✓ 유효한 이메일: ${email.value}"
                        emailError = null
                    } catch (e: IllegalArgumentException) {
                        emailError = e.message ?: "이메일 형식이 올바르지 않습니다"
                        emailResult = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("검증하기", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            emailError?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = error, fontSize = 12.sp, color = Color(0xFFD32F2F))
                    }
                }
            }

            emailResult?.let { success ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = success, fontSize = 12.sp, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReifiedGenericsTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TypeCheckingDemoCard() }
        item { CastingDemoCard() }
    }
}

@Composable
private fun TypeCheckingDemoCard() {
    var inputValue by remember { mutableStateOf("") }
    val testResults = remember { mutableStateListOf<Pair<String, Boolean>>() }
    var selectedType by remember { mutableStateOf("String") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 Reified 타입 체크",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "런타임에 타입 정보 확인 (타입 소거 극복)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("테스트할 값") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF9C27B0),
                    focusedLabelColor = Color(0xFF9C27B0)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("String", "Int", "UserId").forEach { type ->
                    Button(
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedType == type) 
                                Color(0xFF9C27B0) else Color(0xFFE1BEE7)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type,
                            fontSize = 11.sp,
                            color = if (selectedType == type) Color.White else Color(0xFF9C27B0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val testValue: Any = when (selectedType) {
                        "Int" -> inputValue.toIntOrNull() ?: inputValue
                        "UserId" -> try { UserId(inputValue) } catch (e: Exception) { inputValue }
                        else -> inputValue
                    }

                    testResults.clear()
                    testResults.add("String" to isInstance<String>(testValue))
                    testResults.add("Int" to isInstance<Int>(testValue))
                    testResults.add("UserId" to isInstance<UserId>(testValue))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9C27B0)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("타입 체크 실행", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (testResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "타입 체크 결과",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        testResults.forEach { (type, isMatch) ->
                            TypeCheckResult(type, isMatch)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeCheckResult(typeName: String, isMatch: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = if (isMatch) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "isInstance<$typeName>",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF212121)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isMatch) Icons.Filled.CheckCircle else Icons.Filled.Close,
                    contentDescription = null,
                    tint = if (isMatch) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = isMatch.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMatch) Color(0xFF4CAF50) else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun CastingDemoCard() {
    var inputValue by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf("String") }
    var castResult by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8EAF6),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 Safe Casting",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reified로 안전한 타입 캐스팅",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("캐스팅할 값") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF3F51B5),
                    focusedLabelColor = Color(0xFF3F51B5)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("String", "Int", "UserId").forEach { type ->
                    Button(
                        onClick = { targetType = type },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (targetType == type) 
                                Color(0xFF3F51B5) else Color(0xFFC5CAE9)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type,
                            fontSize = 11.sp,
                            color = if (targetType == type) Color.White else Color(0xFF3F51B5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val testValue: Any = inputValue

                    castResult = when (targetType) {
                        "String" -> castOrNull<String>(testValue)?.let { "✓ String: $it" }
                            ?: "✗ 캐스팅 실패 (null)"
                        "Int" -> castOrNull<Int>(testValue)?.let { "✓ Int: $it" }
                            ?: "✗ 캐스팅 실패 (null)"
                        "UserId" -> castOrNull<UserId>(testValue)?.let { "✓ UserId: $it" }
                            ?: "✗ 캐스팅 실패 (null)"
                        else -> "알 수 없는 타입"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("캐스팅 실행", color = Color.White, fontWeight = FontWeight.Bold)
            }

            castResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (result.startsWith("✓")) 
                        Color(0xFF4CAF50).copy(alpha = 0.1f) 
                    else 
                        Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (result.startsWith("✓")) 
                                Icons.Filled.CheckCircle 
                            else 
                                Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (result.startsWith("✓")) 
                                Color(0xFF4CAF50) 
                            else 
                                Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result,
                            fontSize = 13.sp,
                            color = if (result.startsWith("✓")) 
                                Color(0xFF4CAF50) 
                            else 
                                Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceComparisonTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { PerformanceComparisonCard() }
    }
}

@Composable
private fun PerformanceComparisonCard() {
    var inlineTime by remember { mutableStateOf(0L) }
    var nonInlineTime by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var iterations by remember { mutableStateOf(10000) }

    val scale by animateFloatAsState(
        targetValue = if (isRunning) 1.02f else 1f,
        animationSpec = spring()
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE8EAF6)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Inline vs Non-Inline 성능 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "실시간 성능 측정 벤치마크",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1000, 10000, 100000).forEach { count ->
                    Button(
                        onClick = { iterations = count },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (iterations == count) 
                                Color(0xFF3F51B5) else Color(0xFFC5CAE9)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isRunning
                    ) {
                        Text(
                            text = "${count / 1000}K",
                            fontSize = 11.sp,
                            color = if (iterations == count) Color.White else Color(0xFF3F51B5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    isRunning = true
                    
                    val (_, inline) = measure("inline") {
                        repeat(iterations) { it * 2 }
                    }
                    inlineTime = inline

                    val (_, nonInline) = measureNonInline("non-inline") {
                        repeat(iterations) { it * 2 }
                    }
                    nonInlineTime = nonInline
                    
                    isRunning = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isRunning
            ) {
                if (isRunning) {
                    Text("측정 중...", color = Color.White, fontSize = 14.sp)
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "성능 측정 시작 ($iterations iterations)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            AnimatedVisibility(
                visible = inlineTime > 0 || nonInlineTime > 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    PerformanceResult(
                        label = "Inline Function",
                        time = inlineTime,
                        color = Color(0xFF4CAF50),
                        isFaster = inlineTime < nonInlineTime && nonInlineTime > 0
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PerformanceResult(
                        label = "Non-Inline Function",
                        time = nonInlineTime,
                        color = Color(0xFFFF5722),
                        isFaster = nonInlineTime < inlineTime && inlineTime > 0
                    )

                    if (nonInlineTime > 0 && inlineTime > 0) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val improvement = if (nonInlineTime > inlineTime) {
                            ((nonInlineTime - inlineTime).toFloat() / nonInlineTime * 100).toInt()
                        } else {
                            -((inlineTime - nonInlineTime).toFloat() / inlineTime * 100).toInt()
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (improvement > 0) 
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color(0xFFFF5722).copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (improvement > 0) 
                                        "⚡ Inline이 ${improvement}% 더 빠릅니다"
                                    else
                                        "⚠️ Non-inline이 ${-improvement}% 더 빠릅니다",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (improvement > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "차이: ${kotlin.math.abs(nonInlineTime - inlineTime)}ms",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3F51B5).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "💡 실제 성능 향상은 사용 사례와 JVM 최적화에 따라 다를 수 있습니다",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF3F51B5),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceResult(
    label: String,
    time: Long,
    color: Color,
    isFaster: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = if (isFaster) 
            androidx.compose.foundation.BorderStroke(2.dp, color) 
        else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isFaster) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isFaster) FontWeight.Bold else FontWeight.Medium,
                    color = color
                )
            }
            Text(
                text = "${time}ms",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}
