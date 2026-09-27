package com.example.composesample.presentation.example.component.system.security

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

/**
 * API 요청 서명(HMAC-SHA256) + 재전송 방지 예제.
 *
 * 네트워크를 쓰지 않는다 — 서명 인터셉터 뒤에 "서버" 역할의 검증 인터셉터를 두고 그 인터셉터가
 * 직접 응답을 만든다. 자세한 구조와 검사 순서는 [RequestSigningViewModel] KDoc 참고.
 */
@Composable
fun RequestSigningExampleUI(onBackEvent: () -> Unit) {
    val viewModel: RequestSigningViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "API 요청 서명",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard(viewModel) }
            item { ScenarioCard(viewModel, uiState) }
            item { ComparisonCard(viewModel, uiState) }
            item { LimitCard() }
        }
    }
}

// ==================== 1. 무엇에 서명하는가 ====================

@Composable
private fun OverviewCard(viewModel: RequestSigningViewModel) {
    SigningCard(title = "1. 무엇에 서명하는가") {
        BodyText(
            "TLS 는 전송 구간을 보호하지만, 요청이 \"정말 이 앱이 보냈고 도중에 바뀌지 않았는가\"까지 " +
                "증명하지는 않는다. 그래서 요청의 주요 부분을 한 줄로 모아(canonical string) 공유 비밀 키로 " +
                "HMAC-SHA256 서명하고, 서버가 같은 값을 다시 계산해 맞춰 본다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(
            "// 서명 대상 — 줄바꿈으로 필드를 구분한다\n" +
                "POST\\n\n" +
                "/v1/orders\\n\n" +
                "1774689600000\\n            // X-Timestamp\n" +
                "3f2b…-…-…\\n                // X-Nonce (요청마다 새로)\n" +
                "9c1f…(바디의 SHA-256)\n\n" +
                "signature = HMAC-SHA256(secret, canonical)  →  X-Signature"
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "바디 해시까지 서명 대상에 넣는 것이 핵심이다. 그래야 헤더만 베껴 바디를 바꾸는 공격이 서명 검사에서 걸린다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeyValueRow("요청", "POST ${viewModel.requestUrl}")
        KeyValueRow("바디", viewModel.requestBody)
        KeyValueRow("허용 창", "${viewModel.freshnessWindowSeconds}초")
        CaptionText(
            "서버는 값싼 검사부터 한다 — 신선도 → nonce → 서명. HMAC 계산이 가장 비싸므로 마지막에 둔다."
        )
    }
}

// ==================== 2. 네 가지 시나리오 ====================

@Composable
private fun ScenarioCard(
    viewModel: RequestSigningViewModel,
    uiState: RequestSigningViewModel.UiState
) {
    SigningCard(title = "2. 무엇이 요청을 거절시키는가") {
        BodyText(
            "아래 버튼은 실제 OkHttp 인터셉터 체인을 통과한다. 다만 마지막 인터셉터가 chain.proceed() 를 " +
                "호출하지 않고 검증 후 응답을 직접 만들기 때문에, 네트워크로 나가지 않으면서도 실동작을 볼 수 있다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RequestSigningViewModel.Scenario.entries.forEach { scenario ->
                ActionButton(
                    label = scenario.label,
                    enabled = !uiState.isRunning
                ) { viewModel.run(scenario) }
            }
            ActionButton(
                label = "초기화",
                enabled = uiState.logs.isNotEmpty(),
                tone = ButtonTone.NEUTRAL
            ) { viewModel.clear() }
        }

        if (uiState.seenNonces.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CaptionText("서버가 기억 중인 nonce: ${uiState.seenNonces.size}개 (실제 서버라면 TTL 붙은 저장소에 둔다)")
        }

        uiState.logs.forEach { log ->
            Spacer(modifier = Modifier.height(12.dp))
            LogRow(log)
        }

        if (uiState.logs.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            CaptionText("④ 재전송은 ① 정상 요청이 한 번 통과한 뒤에야 의미가 있다 — 그 요청의 서명을 그대로 다시 보낸다.")
        }
    }
}

@Composable
private fun LogRow(log: RequestSigningViewModel.StepLog) {
    val accent = if (log.accepted) Color(0xFF2E7D32) else Color(0xFFC62828)

    Column {
        Row {
            Text(
                text = log.scenario,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = log.statusLine,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accent
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        BodyText(log.reason)

        if (log.signature.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            CodeBlock(
                buildString {
                    append("X-Signature: ").append(log.signature)
                    if (log.canonical.isNotEmpty()) {
                        append("\n\n// 서버가 받은 요청으로 다시 만든 canonical string\n")
                        append(log.canonical)
                    }
                }
            )
        }
    }
}

// ==================== 3. 상수 시간 비교 ====================

@Composable
private fun ComparisonCard(
    viewModel: RequestSigningViewModel,
    uiState: RequestSigningViewModel.UiState
) {
    SigningCard(title = "3. 서명 비교는 왜 상수 시간이어야 하는가") {
        BodyText(
            "서명을 == 로 비교하면 첫 불일치에서 즉시 반환한다. 맞는 바이트가 많을수록 응답이 아주 조금 늦어지고, " +
                "공격자는 그 차이를 반복 측정해 서명을 앞에서부터 한 바이트씩 맞춰 나갈 수 있다. " +
                "그래서 결과와 무관하게 항상 전부 읽는 비교를 쓴다 — JDK 의 MessageDigest.isEqual 이 그 구현이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        ActionButton(label = "읽은 바이트 수 세기", enabled = true) { viewModel.measureComparison() }

        if (uiState.probes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            TableRow("경우", "== 비교", "상수 시간", isHeader = true)
            uiState.probes.forEach { probe ->
                TableRow(
                    probe.label,
                    "${probe.naiveBytesRead} B",
                    "${probe.constantTimeBytesRead} B"
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "시간이 아니라 '읽은 바이트 수'를 센다. 디버그 빌드는 인터프리터로 돌아 ns 측정이 실행마다 " +
                    "요동치지만, 읽은 바이트 수는 결정적이라 성질 자체가 그대로 드러난다 " +
                    "(상수 시간 쪽은 어떤 경우에도 ${uiState.probes.first().totalBytes}B 전부를 읽는다)."
            )
        }
    }
}

// ==================== 4. 서명으로 막지 못하는 것 ====================

@Composable
private fun LimitCard() {
    SigningCard(title = "4. 서명으로 막지 못하는 것") {
        PitfallRow(
            "키가 앱 안에 있으면 서명은 위조된다",
            "APK 를 뜯거나 Frida 로 메모리를 들여다봐 키를 얻으면 공격자도 유효한 서명을 만들 수 있다. " +
                "문자열 난독화(XOR 등)는 정적 분석만 늦출 뿐 동적 계측은 막지 못한다. " +
                "요청 서명은 '자동화된 대량 남용의 단가를 올리는' 층이지 인증 수단이 아니다."
        )
        PitfallRow(
            "기기 무결성은 별개 축이다",
            "요청이 변조되지 않았다는 것과 그 기기가 정상이라는 것은 다른 질문이다. 후자는 Play Integrity 로 " +
                "검증하며, 같은 폴더의 App Security 예제가 그 verdict 구조를 다룬다."
        )
        PitfallRow(
            "nonce 저장소에는 TTL 이 필요하다",
            "이 예제는 nonce 를 메모리 Set 에 담지만, 실제 서버는 허용 창보다 조금 긴 TTL 을 붙여 보관한다. " +
                "TTL 이 없으면 저장소가 무한히 커지고, 창보다 짧으면 만료된 nonce 가 되살아나 재전송이 다시 통한다."
        )
        CaptionText("참고 자료와 개념 정리는 같은 폴더 exampleGuide.kt 참고.")
    }
}

// ==================== 공통 UI ====================

private enum class ButtonTone { PRIMARY, NEUTRAL }

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    tone: ButtonTone = ButtonTone.PRIMARY,
    onClick: () -> Unit
) {
    val container = when (tone) {
        ButtonTone.PRIMARY -> Color(0xFF37474F)
        ButtonTone.NEUTRAL -> Color(0xFF90A4AE)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            disabledContainerColor = Color(0xFFCFD8DC)
        )
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun SigningCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun KeyValueRow(key: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = key,
            fontSize = 12.sp,
            color = Color(0xFF757575),
            modifier = Modifier.width(56.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF37474F)
        )
    }
}

/** 코드·서명 문자열은 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(
    first: String,
    second: String,
    third: String,
    isHeader: Boolean = false
) {
    val weight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    val color = if (isHeader) Color(0xFF212121) else Color(0xFF424242)

    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = first, fontSize = 12.sp, fontWeight = weight, color = color, modifier = Modifier.weight(1f))
        Text(
            text = second,
            fontSize = 12.sp,
            fontWeight = weight,
            fontFamily = FontFamily.Monospace,
            color = color,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = third,
            fontSize = 12.sp,
            fontWeight = weight,
            fontFamily = FontFamily.Monospace,
            color = color,
            modifier = Modifier.width(72.dp)
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "· $title",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = description, fontSize = 12.sp, color = Color(0xFF616161), lineHeight = 18.sp)
    }
}
