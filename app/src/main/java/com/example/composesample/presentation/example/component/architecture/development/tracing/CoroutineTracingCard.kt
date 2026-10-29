package com.example.composesample.presentation.example.component.architecture.development.tracing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

private val PropagationGreen = Color(0xFF00796B)

/** tracing 2.0(tracing-wire) — 앱이 직접 쓰는 Perfetto 트레이스로 코루틴 구간·흐름 연결을 실측하는 카드 */
@Composable
internal fun CoroutineTracingCard() {
    val viewModel: CoroutineTracingViewModel = koinViewModel()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val running by viewModel.running.collectAsStateWithLifecycle()
    val global by viewModel.globalState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "실동작 — tracing 2.0: traceCoroutine 으로 스레드가 바뀌어도 구간 유지",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PropagationGreen
            )
            TraceNote(
                "tracing-wire 의 TraceDriver + TraceSink 로 앱 프로세스가 Perfetto 파일을 직접 쓴다(시스템 트레이싱·캡처 도구 없이). " +
                    "실행할 때마다 새 파일 하나를 만들고, 닫은 뒤 그 파일을 다시 읽어 구간 조각·스레드 트랙·flow id 를 센다."
            )
            TraceCode(
                "tracer.traceCoroutine(\"cat\", \"load\") {   // 코루틴 구간\n" +
                    "    delay(…)   // 멈출 때 이 스레드에서 닫고, 재개된 스레드에서 다시 연다\n" +
                    "}\n" +
                    "val token = tracer.tokenFromCoroutineContext()   // @DelicateTracingApi\n" +
                    "other.launch { tracer.traceCoroutine(\"cat\", \"upload\", token) { … } }"
            )
            TraceScenario.entries.forEach { scenario ->
                Button(
                    onClick = { viewModel.run(scenario) },
                    enabled = !running,
                    colors = ButtonDefaults.buttonColors(containerColor = PropagationGreen)
                ) {
                    Text(text = if (running) "실행 중..." else scenario.label, color = Color.White, fontSize = 12.sp)
                }
            }
            result?.let { r ->
                TraceMono(
                    "${r.scenario.label}\n" + r.lines.joinToString("\n") +
                        "\n\n파일 files/$TRACE_DIR/${r.fileName}\n${r.fileBytes}바이트 · 패킷 ${r.packets}"
                )
                TraceNote(
                    "꺼내 보기: adb exec-out run-as com.example.composesample cat files/$TRACE_DIR/${r.fileName} > t.perfetto-trace " +
                        "→ ui.perfetto.dev 에서 열면 같은 이름 조각이 스레드 트랙마다 나뉘어 있고 flow 화살표로 이어진다."
                )
            }
            TraceNote(
                "전역 Tracer: " + (if (global.isStub) "스텁" else "설정됨") +
                    " · 라이브러리 기본 초기화가 남긴 파일 ${global.profilerTraceFiles}개 — tracing-wire 를 넣으면 App Startup 의 " +
                    "ConnectedProfilerTracingInitializer 가 프로세스 시작마다 no_backup/perfetto_traces 에 빈 파일을 하나씩 만든다" +
                    "(실측: 세 번 실행 → 0바이트 3개). 이 앱은 매니페스트에서 그 초기화를 뺐다."
            )
        }
    }
}

@Composable
private fun TraceNote(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF616161), lineHeight = 16.sp)
}

@Composable
private fun TraceCode(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF263238),
        lineHeight = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .border(1.dp, PropagationGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}

@Composable
private fun TraceMono(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFB2DFDB),
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}
