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
import androidx.compose.ui.platform.LocalContext
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

// ==================== UiText sealed class ====================
// ViewModel에서 문자열을 타입 안전하게 표현하기 위한 sealed class

sealed class UiText {
    /** 이미 해결된 동적 문자열 (API 응답, 사용자 입력 등) */
    data class DynamicString(val value: String) : UiText()

    /** 아직 해결되지 않은 리소스 ID 기반 문자열 */
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText()
}

/** UI 레이어에서만 호출: stringResource를 통해 문자열을 해결 */
@Composable
fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> stringResource(id, *args)
    }
}

// ==================== Fake ViewModel State (데모용) ====================

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
        // Header
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

        // Tab row
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

// ==================== 1. Anti-Pattern Demo ====================

@Composable
private fun AntiPatternDemo() {
    val context = LocalContext.current

    // ❌ 안티패턴: LocalContext를 통해 getString()으로 문자열 가져오기
    val antiPatternTitle = context.getString(R.string.app_name)

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

        item {
            ProblemCard(
                number = "1",
                title = "Preview 미지원",
                description = "@Preview는 실제 Android 환경이 아니므로 Context가 제한됩니다.\n" +
                        "LocalContext.current.getString()은 Preview를 깨뜨릴 수 있습니다.",
                badCode = """// ❌ Preview에서 런타임 에러 가능
@Composable
fun TitleText() {
    val context = LocalContext.current
    Text(text = context.getString(R.string.title))
}

@Preview
@Composable
fun TitleTextPreview() {
    TitleText() // Context가 없어 오류 발생 가능
}"""
            )
        }

        item {
            ProblemCard(
                number = "2",
                title = "Locale 변경에 무반응",
                description = "런타임에 앱 언어를 변경해도 LocalContext를 통한 문자열은\n" +
                        "자동으로 리컴포지션되지 않습니다.",
                badCode = """// ❌ Locale 변경 시 UI가 업데이트되지 않음
val context = LocalContext.current
// locale 변경 후에도 이 값은 그대로임
val text = context.getString(R.string.hello)
Text(text = text)"""
            )
        }

        item {
            ProblemCard(
                number = "3",
                title = "ViewModel 오염",
                description = "ViewModel에서 Context를 들고 있으면\n테스트 불가능하고 Context 누수 위험이 있습니다.",
                badCode = """// ❌ ViewModel에서 Context 직접 사용
class MyViewModel(
    private val context: Context  // 절대 금지!
) : ViewModel() {

    val errorMsg = context.getString(
        R.string.error_network  // Context 오염
    )
}"""
            )
        }

        item {
            // Live demo of the anti-pattern
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "실제 동작 (안티패턴)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "context.getString(R.string.app_name)",
                                fontSize = 11.sp,
                                color = Color(0xFF80CBC4),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "→ \"$antiPatternTitle\"",
                                fontSize = 13.sp,
                                color = Color(0xFFFFEB3B),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "⚠️ 실제로 작동하더라도, Preview와 테스트에서 문제가 생깁니다.",
                        fontSize = 12.sp,
                        color = Color(0xFFE64A19),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ==================== 2. stringResource Demo ====================

@Composable
private fun StringResourceDemo() {
    // ✅ 올바른 Compose 방식
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
            GoodCard(
                title = "기본 사용",
                goodCode = """@Composable
fun TitleText() {
    // ✅ Context 없이 바로 사용
    val title = stringResource(R.string.screen_title)
    Text(text = title)
}

// ✅ Preview에서도 완벽 동작
@Preview
@Composable
fun TitleTextPreview() {
    TitleText()
}"""
            )
        }

        item {
            GoodCard(
                title = "포맷 인자 (Format Args)",
                goodCode = """// strings.xml
// <string name="welcome">안녕하세요, %s님!</string>

@Composable
fun WelcomeText(username: String) {
    val message = stringResource(
        R.string.welcome, username  // 인자 전달
    )
    Text(text = message)
}"""
            )
        }

        item {
            // Live demo
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

                    // Simulated format args
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

// ==================== 3. UiText Demo ====================

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
            GoodCard(
                title = "UiText 정의",
                goodCode = """sealed class UiText {
    // API 응답 등 이미 만들어진 문자열
    data class DynamicString(val value: String) : UiText()

    // 리소스 ID (아직 미해결)
    class StringResource(
        @StringRes val id: Int,
        vararg val args: Any
    ) : UiText()
}

// UI 레이어 확장 함수
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.DynamicString -> value
    is UiText.StringResource -> stringResource(id, *args)
}"""
            )
        }

        item {
            GoodCard(
                title = "ViewModel에서 사용",
                goodCode = """class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiText?>(null)
    val state = _state.asStateFlow()

    // ✅ Context 없이! 리소스 ID만 저장
    fun onNetworkError() {
        _state.value = UiText.StringResource(
            R.string.error_network
        )
    }

    // API가 돌려준 동적 메시지
    fun onApiError(serverMessage: String) {
        _state.value = UiText.DynamicString(serverMessage)
    }
}

// UI: 여기서만 stringResource 호출
@Composable
fun MyScreen() {
    val msg by viewModel.state.collectAsState()
    msg?.let { Text(text = it.asString()) }
}"""
            )
        }

        item {
            // Live UiText demo
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

                    // State display
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
                                state.message.asString(),  // ✅ UI에서만 해결
                                "✅"
                            )
                            is FakeNetworkState.Error -> Triple(
                                Color(0xFFFFEBEE),
                                state.message.asString(),  // ✅ UI에서만 해결
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

                    // UiText type indicator
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

                    // Action buttons
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                networkState = FakeNetworkState.Success(
                                    // StringResource: 리소스 ID 저장 (ViewModel 역할)
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
                                    // DynamicString: API가 준 오류 메시지 (ViewModel 역할)
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

// ==================== 4. Comparison Demo ====================

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

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✅ LocalContext 적절한 사용 예",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF80CBC4)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """@Composable
fun ShareButton(text: String) {
    val context = LocalContext.current // ✅ Context 필요
    Button(onClick = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, null)
        )
    }) {
        // ✅ 문자열은 stringResource로
        Text(stringResource(R.string.share))
    }
}""",
                        fontSize = 11.sp,
                        color = Color(0xFFE0E0E0),
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
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

            // Header
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

// ==================== Common Components ====================

@Composable
private fun ProblemCard(
    number: String,
    title: String,
    description: String,
    badCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = " $number ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = badCode,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFEF9A9A),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun GoodCard(
    title: String,
    goodCode: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = goodCode,
                    modifier = Modifier.padding(10.dp),
                    fontSize = 11.sp,
                    color = Color(0xFFA5D6A7),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
            }
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
