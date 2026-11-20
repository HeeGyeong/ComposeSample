package com.example.composesample.presentation.example.component.ui.dynamic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
 * A2UI 컴포넌트 트리 렌더링(#120) — 에이전트가 JSON 으로 보낸 UI 트리를 androidx.a2ui 1.0.0-alpha02 가 Material 3 Compose 로 그린다.
 * 에이전트는 미리 써 둔 대본(A2uiDemoScript)으로 흉내 내므로 네트워크 · Gemini 호출이 없다.
 */
@Composable
fun A2uiRenderingExampleUI(onBackEvent: () -> Unit) {
    val viewModel: A2uiDemoViewModel = koinViewModel()
    val surfaces by viewModel.surfaces.collectAsStateWithLifecycle()
    val log by viewModel.log.collectAsStateWithLifecycle()
    val nextStep by viewModel.nextStep.collectAsStateWithLifecycle()
    val loopStopped by viewModel.loopStopped.collectAsStateWithLifecycle()
    val lastJson by viewModel.lastJson.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "A2UI 컴포넌트 트리 렌더링", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item {
                ScriptCard(
                    nextStep = nextStep,
                    lastJson = lastJson,
                    loopStopped = loopStopped,
                    onNext = viewModel::sendNextStep,
                    onAll = viewModel::sendRemainingSteps,
                    onReset = viewModel::reset
                )
            }
            item {
                A2uiCard(title = "2. 그려진 surface", color = A2uiTeal) {
                    if (surfaces.isEmpty()) {
                        Caption("열린 surface 없음 — 1 단계(createSurface)를 보내면 생긴다. 5 단계(deleteSurface)를 보내면 닫힌다.")
                    }
                    surfaces.forEach { surface ->
                        Text(
                            text = "surfaceId = ${surface.id}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF607D8B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        // 앱의 MaterialTheme 아래에서 그린다 — 대본의 Text · Card · TextField · Slider · CheckBox · Button 이 M3 컴포넌트가 된다
                        A2uiSurface(
                            surfaceModel = surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, A2uiTeal.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            item { LogCard(log) }
            item { BadMessageCard(onSend = viewModel::sendBadCase) }
            item { NotesCard() }
        }
    }
}

private val A2uiTeal = Color(0xFF00796B)
private val A2uiIndigo = Color(0xFF3949AB)
private val A2uiRed = Color(0xFFC62828)

@Composable
private fun OverviewCard() {
    A2uiCard(title = "에이전트가 보낸 UI 를 네이티브로", color = A2uiIndigo) {
        Body(
            "A2UI 는 에이전트(LLM)가 '어떤 UI 를 그릴지' 를 JSON 메시지로 보내고, 앱이 그 트리를 자기 컴포넌트로 그리는 프로토콜이다(v0.9). " +
                "앱은 카탈로그(쓸 수 있는 컴포넌트 목록)를 정하고, 에이전트는 그 안에서만 고른다 — 코드를 내려받아 실행하지 않는다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Code(
            """
            val catalog = materialA2uiBasicCatalogV1(image, video, audioPlayer,
                functions = A2uiBasicCatalogV1.Functions(context))
            val processor = A2uiMessageProcessor(listOf(catalog))   // ViewModel 에
            launch(Dispatchers.Default) { processor.collectMessages() }
            processor.processMessage(A2uiMessageParser().parse(json))
            // UI: processor.activeSurfaces 를 모아 각각 A2uiSurface(surface)
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Caption(
            "이 예제의 에이전트는 미리 써 둔 메시지 대본이다. 버튼을 누르면 사용자 동작이 outboundEvents 로 나가고, " +
                "대본이 0.4초 뒤 답(updateDataModel)을 보낸다. 네트워크 호출은 없다."
        )
    }
}

@Composable
private fun ScriptCard(
    nextStep: Int,
    lastJson: String,
    loopStopped: String?,
    onNext: () -> Unit,
    onAll: () -> Unit,
    onReset: () -> Unit
) {
    val steps = A2uiDemoScript.steps
    A2uiCard(title = "1. 메시지를 한 단계씩 보내기", color = A2uiIndigo) {
        steps.forEachIndexed { index, step ->
            Text(
                text = (if (index < nextStep) "✓ " else "  ") + step.label,
                fontSize = 12.sp,
                color = if (index < nextStep) A2uiTeal else Color(0xFF9E9E9E),
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onNext,
                enabled = nextStep < steps.size && loopStopped == null,
                colors = ButtonDefaults.buttonColors(containerColor = A2uiIndigo),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text(text = "다음 메시지", fontSize = 12.sp) }
            OutlinedButton(
                onClick = onAll,
                enabled = nextStep < steps.size && loopStopped == null,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text(text = "남은 것 모두", fontSize = 12.sp) }
            OutlinedButton(
                onClick = onReset,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) { Text(text = "처음부터", fontSize = 12.sp) }
        }
        if (loopStopped != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "처리 루프가 멈췄다: $loopStopped", fontSize = 11.sp, color = A2uiRed)
        }
        if (lastJson.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Caption("마지막으로 보낸 JSON")
            Code(lastJson)
        }
    }
}

@Composable
private fun LogCard(log: List<A2uiLogLine>) {
    A2uiCard(title = "3. 기록 — 보낸 것 · 에이전트로 가는 이벤트 · 답", color = A2uiTeal) {
        if (log.isEmpty()) {
            Caption("(아직 없음)")
        }
        log.forEach { line ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = line.kind.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (line.kind) {
                        A2uiLogLine.Kind.Rejected, A2uiLogLine.Kind.Error -> A2uiRed
                        A2uiLogLine.Kind.Event, A2uiLogLine.Kind.Reply -> A2uiIndigo
                        A2uiLogLine.Kind.Sent -> A2uiTeal
                    },
                    modifier = Modifier.width(56.dp)
                )
                Text(text = line.text, fontSize = 11.sp, color = Color(0xFF424242), lineHeight = 15.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Caption(
            "3 단계까지 보낸 뒤 이름을 넣고 '주문하기' 를 누르면 → 이벤트 문맥에 데이터 모델에서 풀린 이름 · 잔 수 · 포장이 담겨 나가고, " +
                "대본이 /status 를 바꾸는 답을 보낸다. 입력값은 TextField · Slider · CheckBox 의 바인딩으로 데이터 모델에 바로 쓰인다."
        )
    }
}

@Composable
private fun BadMessageCard(onSend: (A2uiDemoScript.BadCase) -> Unit) {
    A2uiCard(title = "4. 틀린 메시지 — 앱은 죽지 않는다", color = A2uiRed) {
        Body("LLM 이 만든 JSON 은 틀릴 수 있다. 아래를 보내 보면 어디서 거절되는지(파서 · 처리 루프 · 렌더러) 기록에 남는다.")
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            A2uiDemoScript.badCases.forEach { case ->
                OutlinedButton(
                    onClick = { onSend(case) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) { Text(text = case.label, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun NotesCard() {
    A2uiCard(title = "정리", color = A2uiIndigo) {
        listOf(
            "1.0.0-alpha02 — 알파 사이에도 API 가 바뀐다(alpha01 → 02 에서 카탈로그 함수 인자가 functions 로 바뀜).",
            "compose-ui · material3-a2ui 는 Compose 1.13(compileSdk 37.1)을 요구한다. 이 앱은 BOM 2026.11.01 로 먼저 올린 뒤 넣었다 " +
                "— 그대로 넣으면 앱 전체 Compose 가 1.13 알파로 끌려 올라간다.",
            "파서는 모든 예외를 A2uiValidationException(VALIDATION_FAILED)으로 감싼다. processInput(파서, json) 은 그것을 " +
                "A2uiClientErrorMessage 로 바꿔 에이전트 쪽(outboundEvents)으로 보내고, 이 예제는 parse → processMessage 를 직접 불러 거절을 화면 기록에 남긴다.",
            "처리 루프(collectMessages)는 A2uiException 만 잡는다. 나머지 예외는 루프를 끝내므로 ViewModel 에서 잡아 '처음부터' 로 새 처리기를 만든다.",
            "Image · Video · AudioPlayer 는 렌더러를 앱이 꼭 준다 — 이 예제는 URL 을 내려받지 않고 자리만 그린다."
        ).forEach { Body("• $it") }
    }
}

@Composable
private fun A2uiCard(title: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun Body(text: String) {
    Text(text = text, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
}

@Composable
private fun Caption(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 15.sp)
}

@Composable
private fun Code(code: String) {
    Text(
        text = code,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFECEFF1),
        lineHeight = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .padding(10.dp)
    )
}
