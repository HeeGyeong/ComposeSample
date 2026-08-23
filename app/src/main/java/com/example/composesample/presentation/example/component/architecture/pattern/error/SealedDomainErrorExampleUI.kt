package com.example.composesample.presentation.example.component.architecture.pattern.error

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// 함수가 실제로 실패할 수 있는 경우를 전부 타입으로 나열한 도메인 에러
private sealed interface OrderError {
    data class InvalidQuantity(val quantity: Int) : OrderError
    data class InsufficientStock(val requested: Int, val available: Int) : OrderError
    data object OutOfServiceHours : OrderError
}

// 성공/실패를 한 타입에 담는 결과(Either 최소 구현) — Arrow 등 외부 라이브러리 없이 Kotlin stdlib만으로 충분
private sealed interface OrderOutcome {
    data class Success(val quantity: Int, val totalPrice: Int) : OrderOutcome
    data class Failure(val error: OrderError) : OrderOutcome
}

private const val UNIT_PRICE = 1_200
private const val STOCK = 5

// 신규 패턴: 실패 가능성이 반환 타입에 그대로 드러난다 — 호출부는 when으로 강제 분기
private fun placeOrderSealed(quantity: Int, isServiceOpen: Boolean): OrderOutcome {
    if (!isServiceOpen) return OrderOutcome.Failure(OrderError.OutOfServiceHours)
    if (quantity <= 0) return OrderOutcome.Failure(OrderError.InvalidQuantity(quantity))
    if (quantity > STOCK) return OrderOutcome.Failure(OrderError.InsufficientStock(quantity, STOCK))
    return OrderOutcome.Success(quantity, quantity * UNIT_PRICE)
}

// 기존 패턴: 반환 타입은 Int뿐 — 실패 가능성은 KDoc 주석에만 있고 컴파일러가 강제하지 않는다
// @throws IllegalStateException 영업시간이 아닐 때
// @throws IllegalArgumentException 수량이 0 이하이거나 재고를 초과할 때
private fun placeOrderThrowing(quantity: Int, isServiceOpen: Boolean): Int {
    if (!isServiceOpen) throw IllegalStateException("영업시간이 아닙니다")
    if (quantity <= 0) throw IllegalArgumentException("수량은 1개 이상이어야 합니다: $quantity")
    if (quantity > STOCK) throw IllegalArgumentException("재고 부족: 요청 $quantity / 재고 $STOCK")
    return quantity * UNIT_PRICE
}

@Composable
fun SealedDomainErrorExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Sealed 도메인 에러 처리",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { ThrowingStyleCard() }
            item { SealedResultCard() }
            item { ExhaustivenessCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "함수형 도메인 에러 처리 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Kotlin은 checked exception이 없어, 함수 시그니처만 봐서는 무엇이 잘못될 수 있는지 알 수 없습니다. " +
                        "예외를 던지는 대신 실패를 sealed interface로 반환하면, 가능한 모든 결과가 타입 시스템에 드러나고 " +
                        "호출부는 컴파일러가 강제하는 when으로 모든 케이스를 처리하게 됩니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val axes = listOf(
                Triple("에러 위치", "throw / try-catch", "런타임에야 드러남 — 시그니처는 거짓말을 한다"),
                Triple("에러 위치", "sealed interface 반환", "컴파일 타임에 시그니처가 실패 케이스를 전부 선언"),
                Triple("호출부 강제력", "when(exhaustive)", "새 에러 케이스 추가 시 처리 누락을 컴파일 에러로 검출")
            )
            axes.forEach { (label, api, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.28f))
                    Text(text = api, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.32f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.4f))
                }
            }
        }
    }
}

@Composable
private fun ThrowingStyleCard() {
    var resultText by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "① 기존 방식 — 예외 던지기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "함수 반환 타입은 Int뿐입니다. 실패 가능성은 KDoc @throws 주석에만 적혀 있고, " +
                        "컴파일러는 호출부가 try-catch를 했는지 전혀 확인하지 않습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// @throws IllegalStateException 영업시간이 아닐 때\n" +
                        "// @throws IllegalArgumentException 수량이 0 이하이거나 재고 초과 시\n" +
                        "fun placeOrderThrowing(quantity: Int, isServiceOpen: Boolean): Int",
                borderColor = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    resultText = try {
                        val total = placeOrderThrowing(quantity = 999, isServiceOpen = true)
                        "성공: ${total}원"
                    } catch (e: IllegalArgumentException) {
                        "예외 발생(런타임에야 확인됨): ${e.message}"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
            ) {
                Text(text = "재고 초과 주문 시도(999개)", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            resultText?.let {
                ResultRow(label = "결과", value = it, color = Color(0xFFE65100))
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚠ try-catch를 빠뜨려도 컴파일은 통과합니다. 어떤 예외 타입을 잡아야 하는지도 문서를 " +
                            "직접 읽기 전엔 알 수 없습니다.",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SealedResultCard() {
    var outcome by remember { mutableStateOf<OrderOutcome?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "② 신규 방식 — sealed interface 반환",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "반환 타입 OrderOutcome 자체가 Success 아니면 Failure(OrderError)임을 선언합니다. " +
                        "호출부는 when으로 두 갈래를 전부 다뤄야 하고, OrderError 세 종류도 마찬가지로 강제됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "sealed interface OrderOutcome {\n" +
                        "    data class Success(val quantity: Int, val totalPrice: Int) : OrderOutcome\n" +
                        "    data class Failure(val error: OrderError) : OrderOutcome\n" +
                        "}\n" +
                        "fun placeOrderSealed(quantity: Int, isServiceOpen: Boolean): OrderOutcome",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val scenarios = listOf(
                Triple("정상 주문(3개)", 3, true),
                Triple("수량 0개", 0, true),
                Triple("재고 초과(999개)", 999, true),
                Triple("영업시간 외", 3, false)
            )
            scenarios.forEach { (label, quantity, isOpen) ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Button(
                        onClick = { outcome = placeOrderSealed(quantity, isOpen) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) {
                        Text(text = label, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // exhaustive when — else 분기 없이 모든 케이스를 컴파일러가 강제
            val resultLine = when (val current = outcome) {
                null -> null
                is OrderOutcome.Success -> "성공: ${current.quantity}개, 총 ${current.totalPrice}원"
                is OrderOutcome.Failure -> when (val error = current.error) {
                    is OrderError.InvalidQuantity -> "실패(InvalidQuantity): 수량 ${error.quantity}은(는) 유효하지 않음"
                    is OrderError.InsufficientStock -> "실패(InsufficientStock): 요청 ${error.requested} / 재고 ${error.available}"
                    OrderError.OutOfServiceHours -> "실패(OutOfServiceHours): 영업시간이 아님"
                }
            }
            resultLine?.let {
                ResultRow(
                    label = "결과",
                    value = it,
                    color = if (outcome is OrderOutcome.Success) Color(0xFF388E3C) else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun ExhaustivenessCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "③ exhaustive when — 처리 누락을 컴파일 에러로",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "sealed interface를 when의 subject로 쓰면 else 없이도 모든 하위 타입을 분기해야 컴파일됩니다. " +
                        "위 SealedResultCard의 when이 바로 그 방식이고, 여기서 OrderError에 새 케이스를 " +
                        "하나 추가하면(예: RateLimited) 이 when들이 즉시 컴파일 에러로 변합니다 — " +
                        "테스트를 돌리거나 배포한 뒤가 아니라 코드를 작성하는 시점에 누락을 알 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "when (error) {\n" +
                        "    is OrderError.InvalidQuantity -> ...\n" +
                        "    is OrderError.InsufficientStock -> ...\n" +
                        "    OrderError.OutOfServiceHours -> ...\n" +
                        "    // else 없이 컴파일됨 — 세 케이스를 전부 나열했기 때문\n" +
                        "    // 네 번째 OrderError 하위 타입을 추가하면 이 when이 컴파일 에러가 된다\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "Kotlin은 checked exception이 없어 throw 기반 함수는 시그니처만으로 실패 가능성을 알 수 없음",
                "sealed interface를 반환 타입으로 쓰면 성공/실패 케이스가 타입 시스템에 드러나고, IDE 자동완성으로도 확인 가능",
                "when이 sealed interface를 대상으로 하면 else 없이도 컴파일되지만, 그러려면 모든 하위 타입을 나열해야 함(exhaustive)",
                "새 에러 케이스를 sealed interface에 추가하면 그 타입을 분기하는 모든 when이 컴파일 에러로 변함 — 처리 누락이 배포 전에 걸러짐",
                "Arrow의 Either 같은 외부 라이브러리 없이도 Kotlin stdlib(sealed interface + data class)만으로 이 패턴을 구현 가능"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color)
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
