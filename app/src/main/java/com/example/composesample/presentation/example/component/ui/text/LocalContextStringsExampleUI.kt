package com.example.composesample.presentation.example.component.ui.text

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R

/**
 * LocalContext for Strings (Anti-Pattern) - Example UI
 *
 * Compose에서 LocalContext.current.getString()은 쓰지 말아야 합니다.
 * stringResource()와 UiText 패턴으로 올바르게 처리하는 방법을 보여줍니다.
 */

sealed class UiText {
    data class DynamicString(val value: String) : UiText()

    class StringResource(
        @param:StringRes val id: Int,
        vararg val args: Any
    ) : UiText()
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(id, *args)
    }
}

private sealed class FakeNetworkState {
    object Idle : FakeNetworkState()
    object Loading : FakeNetworkState()
    data class Success(val message: UiText) : FakeNetworkState()
    data class Error(val message: UiText) : FakeNetworkState()
}

@Composable
fun LocalContextStringsExampleUI(
    onBackEvent: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
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
                        text = "LocalContext for Strings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Anti-Pattern vs stringResource vs UiText",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf("❌ 안티패턴", "✅ stringResource", "✅ UiText", "✅ 비교")
            tabs.forEachIndexed { i, label ->
                StringTab(
                    text = label,
                    isSelected = selectedTab == i,
                    onClick = { selectedTab = i }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> AntiPatternDemo()
            1 -> StringResourceDemo()
            2 -> UiTextDemo()
            3 -> ComparisonDemo()
        }
    }
}

@Composable
private fun StringTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
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
                .padding(vertical = 10.dp, horizontal = 6.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@Composable
private fun AntiPatternDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "❌ 안티패턴: LocalContext.getString()",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB71C1C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "많은 개발자들이 습관적으로 View 방식처럼 Context를 통해 문자열을 가져옵니다.\n" +
                                "이것은 Compose에서 안티패턴입니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StringResourceDemo() {
    val appName = stringResource(R.string.app_name)
    var counter by remember { mutableIntStateOf(1) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✅ 올바른 방법: stringResource()",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compose가 제공하는 전용 API입니다.\n" +
                                "Preview, 테스트, Locale 변경 모두 완벽히 지원합니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "실제 동작 (stringResource)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ResultDisplay(
                        label = "stringResource(R.string.app_name)",
                        value = appName,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val countText = "아이템 $counter 개"
                    ResultDisplay(
                        label = "동적 카운터 (Compose 리컴포지션 연동)",
                        value = countText,
                        color = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { if (counter > 0) counter-- },
                            modifier = Modifier.weight(1f)
                        ) { Text("-") }
                        OutlinedButton(
                            onClick = { counter++ },
                            modifier = Modifier.weight(1f)
                        ) { Text("+") }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "stringResource 내부 동작",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "내부적으로 LocalConfiguration을 구독합니다.\n" +
                                "Locale이 변경되면 LocalConfiguration이 바뀌고,\n" +
                                "이를 읽는 Composable이 자동으로 리컴포지션됩니다.\n\n" +
                                "LocalContext.current.getString()은 이 구독 메커니즘 없이\n" +
                                "단순히 Context의 resources를 직접 호출하기 때문에\n" +
                                "Compose 리컴포지션 시스템을 우회합니다.",
                        fontSize = 12.sp,
                        color = Color(0xFF37474F),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun UiTextDemo() {
    var networkState by remember {
        mutableStateOf<FakeNetworkState>(FakeNetworkState.Idle)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✅ UiText sealed class 패턴",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ViewModel에서 Context 없이 문자열을 표현하는 패턴입니다.\n" +
                                "UI 레이어에서만 실제 문자열로 해결됩니다.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "UiText 인터랙티브 데모",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = networkState,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "networkState"
                    ) { state ->
                        val (bgColor, text, icon) = when (state) {
                            FakeNetworkState.Idle -> Triple(
                                Color(0xFFF5F5F5),
                                "버튼을 눌러 ViewModel 이벤트를 시뮬레이션하세요",
                                "💤"
                            )
                            FakeNetworkState.Loading -> Triple(
                                Color(0xFFE3F2FD),
                                "로딩 중...",
                                "⏳"
                            )
                            is FakeNetworkState.Success -> Triple(
                                Color(0xFFE8F5E9),
                                state.message.asString(),
                                "✅"
                            )
                            is FakeNetworkState.Error -> Triple(
                                Color(0xFFFFEBEE),
                                state.message.asString(),
                                "❌"
                            )
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = icon, fontSize = 20.sp)
                                Text(
                                    text = text,
                                    fontSize = 13.sp,
                                    color = Color(0xFF37474F),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (networkState is FakeNetworkState.Success || networkState is FakeNetworkState.Error) {
                        val uiTextType = when (networkState) {
                            is FakeNetworkState.Success ->
                                (networkState as FakeNetworkState.Success).message::class.simpleName
                            is FakeNetworkState.Error ->
                                (networkState as FakeNetworkState.Error).message::class.simpleName
                            else -> null
                        }
                        uiTextType?.let { type ->
                            Text(
                                text = "UiText 타입: ${type}",
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                networkState = FakeNetworkState.Success(
                                    UiText.StringResource(R.string.app_name)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Text("성공 → UiText.StringResource (리소스 ID)")
                        }

                        Button(
                            onClick = {
                                networkState = FakeNetworkState.Error(
                                    UiText.DynamicString("서버 오류: 네트워크 연결을 확인하세요 (500)")
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828)
                            )
                        ) {
                            Text("오류 → UiText.DynamicString (API 메시지)")
                        }

                        OutlinedButton(
                            onClick = { networkState = FakeNetworkState.Idle },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("초기화")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonDemo() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "패턴 비교 요약",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }
            }
        }

        item { ComparisonTableCard() }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✅ LocalContext를 써도 괜찮은 경우",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6A1B9A)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val validCases = listOf(
                        "startActivity() — 실제 Context가 필요",
                        "Toast.makeText() — Context 필수",
                        "getSystemService() — 시스템 서비스 접근",
                        "외부 라이브러리가 Context를 요구하는 경우",
                    )
                    validCases.forEach { case ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✓ ", fontSize = 12.sp, color = Color(0xFF7B1FA2))
                            Text(
                                text = case,
                                fontSize = 12.sp,
                                color = Color(0xFF424242),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun ComparisonTableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val headers = listOf("패턴", "Preview", "Locale", "ViewModel", "테스트")
            val rows = listOf(
                listOf("LocalContext\n.getString", "❌", "❌", "❌", "어려움"),
                listOf("stringResource\n()", "✅", "✅", "❌*", "쉬움"),
                listOf("UiText\nsealed", "✅", "✅", "✅", "쉬움"),
                listOf("StringResolver\n인터페이스", "✅", "✅", "✅", "최고"),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                    .padding(vertical = 5.dp, horizontal = 8.dp)
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        text = h,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(if (i == 0) 2f else 1f),
                        textAlign = if (i == 0) TextAlign.Start else TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            rows.forEachIndexed { idx, cols ->
                val isAnti = idx == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isAnti) Color(0xFFFFEBEE)
                            else if (idx % 2 == 0) Color(0xFFF1F8E9) else Color.White,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    cols.forEachIndexed { colIdx, cell ->
                        Text(
                            text = cell,
                            fontSize = 11.sp,
                            color = if (isAnti) Color(0xFFB71C1C) else Color(0xFF424242),
                            modifier = Modifier.weight(if (colIdx == 0) 2f else 1f),
                            textAlign = if (colIdx == 0) TextAlign.Start else TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
                if (idx < rows.size - 1) Spacer(modifier = Modifier.height(2.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "* stringResource()는 @Composable 함수이므로 ViewModel에서 직접 호출 불가\n  → UiText.StringResource로 ID를 저장하고 UI에서 해결할 것",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ResultDisplay(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "→ \"$value\"",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocalContextStringsExampleUIPreview() {
    LocalContextStringsExampleUI(onBackEvent = {})
}
