package com.example.composesample.presentation.example.component.architecture.development.performance

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
value class Percentage private constructor(val value: Int) {
    companion object {
        fun of(raw: Int): Percentage = Percentage(raw.coerceIn(0, 100))
    }
    
    fun toDisplayString(): String = "$value%"
    override fun toString(): String = "Percentage($value%)"
}

@JvmInline
value class Email(val value: String) {
    init {
        require("@" in value) { "Invalid email format" }
    }
    
    override fun toString(): String = "Email($value)"
}

// ===== Inline Functions 예제 =====

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

@Composable
fun InlineValueClassExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Inline & Value Classes",
            onBackIconClicked = onBackEvent
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ValueClassDemoCard()
            }
            
            item {
                ValueClassTypeSafetyCard()
            }
            
            item {
                ValueClassOperatorsCard()
            }
            
            item {
                InlineFunctionDemoCard()
            }
            
            item {
                ReifiedGenericsCard()
            }
            
            item {
                InlineModifiersCard()
            }
            
            item {
                BoxingExamplesCard()
            }
            
            item {
                PerformanceComparisonCard()
            }
        }
    }
}

@Composable
private fun ValueClassDemoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💎 Value Classes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "단일 값을 래핑하여 타입 안전성을 제공하며, 런타임 오버헤드를 최소화합니다.",
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ValueClassExample(
                title = "UserId",
                code = "@JvmInline\nvalue class UserId(val value: String)",
                example = UserId("user-123").toString(),
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ValueClassExample(
                title = "Money",
                code = "@JvmInline\nvalue class Money(val cents: Long)",
                example = Money(1299).toDisplayString(),
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ValueClassExample(
                title = "Email",
                code = "@JvmInline\nvalue class Email(val value: String)",
                example = Email("user@example.com").value,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "컴파일 타임 타입 체크 + 런타임 성능 최적화",
                        fontSize = 12.sp,
                        color = Color(0xFF1976D2),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ValueClassExample(
    title: String,
    code: String,
    example: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = code,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "예시: $example",
                fontSize = 12.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ValueClassTypeSafetyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🛡️ 타입 안전성",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val userId = UserId("user-456")
            val productId = ProductId("prod-789")
            
            TypeSafetyExample(
                description = "같은 String이지만 다른 타입",
                code = """
                    val userId = UserId("user-456")
                    val productId = ProductId("prod-789")
                """.trimIndent(),
                result = """
                    userId: ${userId}
                    productId: ${productId}
                    타입 혼동 방지됨 ✓
                """.trimIndent()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
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
                    Text(
                        text = "컴파일 타임에 타입 오류 감지 가능",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeSafetyExample(
    description: String,
    code: String,
    result: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = result,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ValueClassOperatorsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFFFF3E0)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "➕ 연산자 오버로딩",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val price = Money(1299)
            val quantity = 3
            val total = price * quantity
            val discount = Money(500)
            val final = total - discount
            
            OperatorExample(
                title = "Money 계산",
                operations = listOf(
                    "가격: ${price.toDisplayString()}",
                    "수량: $quantity",
                    "소계: ${total.toDisplayString()} (${price.toDisplayString()} × $quantity)",
                    "할인: ${discount.toDisplayString()}",
                    "최종: ${final.toDisplayString()}"
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val percent = Percentage.of(75)
            
            OperatorExample(
                title = "Percentage",
                operations = listOf(
                    "입력: 75",
                    "결과: ${percent.toDisplayString()}",
                    "범위 제한: 0-100%"
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFF9800).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 Value class에 연산자를 정의하여 도메인 로직을 캡슐화",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFFF9800),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OperatorExample(title: String, operations: List<String>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            operations.forEach { op ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFFF9800), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = op,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineFunctionDemoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE3F2FD)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Inline Functions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "함수 본문을 호출 지점에 복사하여 오버헤드를 제거합니다.",
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InlineFunctionExample(
                title = "measure (inline)",
                code = """
                    inline fun <T> measure(block: () -> T): Pair<T, Long> {
                        val start = currentTimeMillis()
                        val result = block()
                        return result to (currentTimeMillis() - start)
                    }
                """.trimIndent(),
                benefit = "람다 객체 할당 제거, 호출 오버헤드 제거",
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 작은 고차 함수에 inline을 사용하여 성능 최적화",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun InlineFunctionExample(
    title: String,
    code: String,
    benefit: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color.White
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray,
                    lineHeight = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = benefit,
                    fontSize = 11.sp,
                    color = color.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ReifiedGenericsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF3E5F5)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 Reified Generics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ReifiedExample(
                title = "isInstance",
                code = "inline fun <reified T> isInstance(any: Any?): Boolean = any is T",
                testCases = listOf(
                    "isInstance<String>(\"hello\")" to isInstance<String>("hello").toString(),
                    "isInstance<Int>(\"hello\")" to isInstance<Int>("hello").toString(),
                    "isInstance<UserId>(UserId(\"123\"))" to isInstance<UserId>(UserId("123")).toString()
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ReifiedExample(
                title = "castOrNull",
                code = "inline fun <reified T> castOrNull(any: Any?): T? = any as? T",
                testCases = listOf(
                    "castOrNull<String>(\"test\")" to (castOrNull<String>("test") ?: "null"),
                    "castOrNull<Int>(\"test\")" to (castOrNull<Int>("test")?.toString() ?: "null")
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF9C27B0).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 inline + reified로 런타임에 타입 정보 접근 가능 (타입 소거 극복)",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFF9C27B0),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ReifiedExample(
    title: String,
    code: String,
    testCases: List<Pair<String, String>>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9C27B0)
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            testCases.forEach { (test, result) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = test,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "→ $result",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineModifiersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔧 Inline 수정자",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF607D8B)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ModifierExample(
                keyword = "noinline",
                description = "특정 람다 파라미터를 인라인에서 제외",
                code = """
                    inline fun process(
                        action: () -> Unit,
                        noinline onError: (Throwable) -> Unit
                    ) { }
                """.trimIndent(),
                useCase = "람다를 변수에 저장하거나 전달할 때",
                color = Color(0xFFFF5722)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ModifierExample(
                keyword = "crossinline",
                description = "non-local return을 금지",
                code = """
                    inline fun process(
                        crossinline action: () -> Unit
                    ) { }
                """.trimIndent(),
                useCase = "람다를 다른 실행 컨텍스트에서 재호출할 때",
                color = Color(0xFF009688)
            )
        }
    }
}

@Composable
private fun ModifierExample(
    keyword: String,
    description: String,
    code: String,
    useCase: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = keyword,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = Color.White
            ) {
                Text(
                    text = code,
                    modifier = Modifier.padding(8.dp),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray,
                    lineHeight = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "사용 시기: $useCase",
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun BoxingExamplesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFFFF9C4)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠️ Value Class Boxing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            BoxingScenario(
                title = "No Boxing (최적화됨)",
                examples = listOf(
                    "val userId = UserId(\"123\")" to "✓",
                    "fun process(userId: UserId)" to "✓",
                    "userId.value" to "✓"
                ),
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BoxingScenario(
                title = "Boxing 발생 (주의)",
                examples = listOf(
                    "List<UserId>" to "제네릭",
                    "UserId?" to "Nullable",
                    "val any: Any = userId" to "업캐스트",
                    "UserId implements Comparable" to "인터페이스"
                ),
                color = Color(0xFFFF5722)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF57F17).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "💡 Boxing이 발생하면 래퍼 객체가 할당되어 성능 이점이 사라집니다",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp,
                    color = Color(0xFFF57F17),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun BoxingScenario(
    title: String,
    examples: List<Pair<String, String>>,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            examples.forEach { (example, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = example,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = note,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceComparisonCard() {
    var inlineTime by remember { mutableStateOf(0L) }
    var nonInlineTime by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isRunning) 1.05f else 1f,
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
                text = "📊 성능 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    isRunning = true
                    val (_, inline) = measure("inline") {
                        repeat(10000) { it * 2 }
                    }
                    inlineTime = inline
                    
                    val (_, nonInline) = measureNonInline("non-inline") {
                        repeat(10000) { it * 2 }
                    }
                    nonInlineTime = nonInline
                    isRunning = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isRunning
            ) {
                Text(
                    text = if (isRunning) "측정 중..." else "성능 측정 실행",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (inlineTime > 0 || nonInlineTime > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                PerformanceResult(
                    label = "Inline Function",
                    time = inlineTime,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PerformanceResult(
                    label = "Non-Inline Function",
                    time = nonInlineTime,
                    color = Color(0xFFFF5722)
                )
                
                if (nonInlineTime > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val improvement = if (nonInlineTime > inlineTime) {
                        ((nonInlineTime - inlineTime).toFloat() / nonInlineTime * 100).toInt()
                    } else 0
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "⚡ Inline이 약 ${improvement}% 더 빠릅니다",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
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

@Composable
private fun PerformanceResult(label: String, time: Long, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = "${time}ms",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }
}

