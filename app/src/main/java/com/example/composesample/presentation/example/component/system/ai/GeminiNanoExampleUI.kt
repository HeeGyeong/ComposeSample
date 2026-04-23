package com.example.composesample.presentation.example.component.system.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ML Kit GenAI FeatureStatus를 모방한 로컬 시뮬레이션 상태
private enum class FeatureStatus(val label: String, val color: Long) {
    AVAILABLE("AVAILABLE (모델 준비 완료)", 0xFF2E7D32),
    DOWNLOADABLE("DOWNLOADABLE (다운로드 필요)", 0xFFEF6C00),
    DOWNLOADING("DOWNLOADING (다운로드 진행 중)", 0xFF1976D2),
    UNAVAILABLE("UNAVAILABLE (기기 미지원)", 0xFFC62828)
}

@Composable
fun GeminiNanoExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Gemini Nano (ML Kit GenAI)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { FeatureAvailabilityCard() }
            item { SummarizationDemoCard() }
            item { HybridRoutingCard() }
            item { ApiCodeCard() }
            item { ConstraintsCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("ML Kit GenAI 개요")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Gemini Nano는 AICore 시스템 서비스를 통해 기기 내부에서 실행되는 소형 LLM입니다. " +
                        "ML Kit GenAI API는 AICore 위에 공식 Android API 레이어를 제공하여 " +
                        "Summarization / Rewriting / Proofreading / Image Description 기능을 표준화합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            val items = listOf(
                "온디바이스" to "네트워크 없이 추론 → 프라이버시·레이턴시·비용 이점",
                "기기 제한" to "AICore 탑재 기기 (Pixel 8+, Galaxy S24+ 등)",
                "API 단계" to "Preview/Alpha — 버전 변경 가능성 있음",
                "기능 분류" to "Summarization · Rewriting · Proofreading · ImageDescription"
            )
            items.forEach { (k, v) -> LabeledRow(k, v) }
        }
    }
}

@Composable
private fun FeatureAvailabilityCard() {
    var status by remember { mutableStateOf(FeatureStatus.DOWNLOADABLE) }
    var progress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // DOWNLOADING 상태일 때 Mock 진행률 증가
    LaunchedEffect(status) {
        if (status == FeatureStatus.DOWNLOADING) {
            progress = 0f
            while (progress < 1f) {
                delay(120)
                progress = (progress + 0.07f).coerceAtMost(1f)
            }
            status = FeatureStatus.AVAILABLE
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("Feature Availability 플로우 (시뮬레이션)")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ML Kit GenAI 호출 전에 반드시 가용성을 확인합니다. " +
                        "상태별 UX 분기를 시뮬레이션으로 재현합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(status.color).copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "현재 상태: ${status.label}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(status.color)
                )
            }

            if (status == FeatureStatus.DOWNLOADING) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButton("AVAILABLE") { status = FeatureStatus.AVAILABLE }
                SmallButton("DOWNLOADABLE") { status = FeatureStatus.DOWNLOADABLE }
                SmallButton("UNAVAILABLE") { status = FeatureStatus.UNAVAILABLE }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButton(
                    label = "downloadFeature() 트리거",
                    enabled = status == FeatureStatus.DOWNLOADABLE
                ) {
                    scope.launch { status = FeatureStatus.DOWNLOADING }
                }
            }
        }
    }
}

@Composable
private fun SummarizationDemoCard() {
    var result by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sampleInput = "온디바이스 AI는 네트워크 없이 추론하므로 프라이버시와 레이턴시 면에서 유리합니다. " +
            "하지만 모델 크기와 기기 리소스 제약으로 인해 복잡한 추론은 클라우드와 조합하는 하이브리드 전략이 일반적입니다."

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("Summarization Mock 데모")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "입력",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(text = sampleInput, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (isRunning) return@Button
                    isRunning = true
                    result = null
                    scope.launch {
                        // 실제로는 summarizer.runInference(request) 호출
                        delay(800)
                        result = "• 온디바이스 AI: 네트워크 독립성 · 프라이버시 · 빠른 응답\n" +
                                "• 리소스 제약으로 인해 복잡한 추론은 클라우드와 하이브리드 설계가 일반적"
                        isRunning = false
                    }
                },
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(text = if (isRunning) "추론 중…" else "요약 실행 (Mock)", color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "결과",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = result ?: "요약 실행 버튼을 눌러보세요 (실기기가 아니어도 Mock 응답이 반환됩니다)",
                    fontSize = 12.sp,
                    color = Color(0xFF2E7D32),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun HybridRoutingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("Nano ↔ Cloud 하이브리드 라우팅")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "가용성 확인 결과에 따라 온디바이스(Nano) 또는 클라우드(Gemini API)로 분기합니다. " +
                        "저사양 기기에서도 기능을 유지하면서 고사양 기기에서는 네트워크·비용을 절감합니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "suspend fun summarize(input: String): String {\n" +
                        "    val status = Summarizer.getFeatureStatus()\n" +
                        "    return when (status) {\n" +
                        "        FeatureStatus.AVAILABLE ->\n" +
                        "            runNanoSummarizer(input)\n\n" +
                        "        FeatureStatus.DOWNLOADABLE -> {\n" +
                        "            Summarizer.downloadFeature()  // 비동기 트리거\n" +
                        "            runCloudSummarizer(input)      // 대기 중에는 Cloud\n" +
                        "        }\n\n" +
                        "        FeatureStatus.UNAVAILABLE ->\n" +
                        "            runCloudSummarizer(input)      // 미지원 기기\n\n" +
                        "        FeatureStatus.DOWNLOADING ->\n" +
                        "            runCloudSummarizer(input)\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun ApiCodeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("실제 ML Kit GenAI 호출 구조 (참고)")
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "실기기(AICore 지원)에서 동작하는 실제 호출 흐름입니다. " +
                        "본 예제는 의존성을 추가하지 않고 구조만 보여줍니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            CodeBlock(
                code = "// build.gradle.kts\n" +
                        "implementation(\"com.google.mlkit:genai-summarization:<ver>\")\n\n" +
                        "// 1) 가용성 확인\n" +
                        "val client = Summarization.getClient(\n" +
                        "    SummarizerOptions.builder(context)\n" +
                        "        .setInputType(INPUT_TYPE_ARTICLE)\n" +
                        "        .setOutputType(OUTPUT_TYPE_THREE_BULLETS)\n" +
                        "        .setLanguage(LANGUAGE_KO)\n" +
                        "        .build()\n" +
                        ")\n" +
                        "val status = client.checkFeatureStatus().await()\n\n" +
                        "// 2) 다운로드가 필요하면 트리거\n" +
                        "if (status == FeatureStatus.DOWNLOADABLE) {\n" +
                        "    client.downloadFeature(progressListener).await()\n" +
                        "}\n\n" +
                        "// 3) 추론 실행\n" +
                        "val request = SummarizationRequest.builder(input).build()\n" +
                        "val result = client.runInference(request) { chunk ->\n" +
                        "    // 스트리밍 토큰 수신\n" +
                        "}.await()\n" +
                        "val summary = result.summary",
                borderColor = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun ConstraintsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle("제약 사항 및 주의점")
            Spacer(modifier = Modifier.height(8.dp))
            val items = listOf(
                "지원 기기" to "AICore 탑재 기기만 해당 (Pixel 8+, Galaxy S24+ 등)",
                "API 단계" to "Preview/Alpha — 릴리즈 주기마다 시그니처 변경 가능",
                "입력 한도" to "기능별 토큰 제한 존재 (Summarization은 수천 토큰 수준)",
                "안전 필터" to "민감 콘텐츠는 빈 응답 또는 예외로 거부될 수 있음",
                "에뮬레이터" to "대부분 UNAVAILABLE 반환 → Cloud fallback 필수"
            )
            items.forEach { (k, v) -> LabeledRow(k, v) }
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
            SectionTitle("핵심 정리")
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "ML Kit GenAI는 AICore 기반 Gemini Nano에 대한 표준 Android API 레이어",
                "호출 전 `checkFeatureStatus()`로 AVAILABLE/DOWNLOADABLE/UNAVAILABLE 확인이 필수",
                "DOWNLOADABLE은 `downloadFeature()`를 비동기로 트리거 후 Cloud fallback 권장",
                "하이브리드 라우팅(Nano ↔ Cloud)으로 기기별 품질·비용 균형을 유지",
                "에뮬레이터·저사양 기기에서는 실 추론 불가 → 개발 중에는 Mock/Cloud 이중 경로 설계"
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2)
    )
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242),
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(text = value, fontSize = 11.sp, color = Color(0xFF616161), lineHeight = 16.sp)
    }
}

@Composable
private fun SmallButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1976D2),
            disabledContainerColor = Color(0xFFBDBDBD)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = Color.White, fontSize = 11.sp)
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

