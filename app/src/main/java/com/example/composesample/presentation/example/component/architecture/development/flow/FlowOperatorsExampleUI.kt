package com.example.composesample.presentation.example.component.architecture.development.flow

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Kotlin Flow 속도 제어 연산자 비교 — buffer / conflate / debounce / sample
 * - 빠른 emit + 느린 collect 환경에서 각 연산자가 어떻게 동작하는지 타임라인으로 시각화.
 */
@OptIn(FlowPreview::class)
@Composable
fun FlowOperatorsExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Flow: Buffer / Conflate / Debounce / Sample",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item {
                OperatorDemoCard(
                    title = "1. (no operator) — 직렬 처리",
                    description = "업스트림이 다운스트림 처리 완료를 기다린다. 총 시간이 emit 시간 + collect 시간",
                    accent = Color(0xFF9E9E9E)
                ) { onLog ->
                    fastEmitter()
                        .collect { value ->
                            onLog("emit#$value 도착")
                            delay(SLOW_COLLECT_MS)
                            onLog("collect#$value 완료")
                        }
                }
            }
            item {
                OperatorDemoCard(
                    title = "2. buffer() — 모든 값 보존, 병렬 처리",
                    description = "emit는 즉시 진행되고 collect는 자기 속도로 진행. 모든 값이 처리됨",
                    accent = Color(0xFF1E88E5)
                ) { onLog ->
                    fastEmitter()
                        .buffer()
                        .collect { value ->
                            onLog("collect#$value 시작")
                            delay(SLOW_COLLECT_MS)
                            onLog("collect#$value 완료")
                        }
                }
            }
            item {
                OperatorDemoCard(
                    title = "3. conflate() — 처리 중 새 값은 최신값으로 덮어쓰기",
                    description = "다운스트림이 바쁠 때 들어온 중간 값은 버려지고 마지막 값만 처리",
                    accent = Color(0xFF43A047)
                ) { onLog ->
                    fastEmitter()
                        .conflate()
                        .collect { value ->
                            onLog("collect#$value 시작")
                            delay(SLOW_COLLECT_MS)
                            onLog("collect#$value 완료 (중간 값 drop)")
                        }
                }
            }
            item {
                OperatorDemoCard(
                    title = "4. debounce(150ms) — 입력이 멈춘 후에만 통과",
                    description = "마지막 emit 후 timeout 동안 신규 emit이 없을 때만 다운스트림 전달",
                    accent = Color(0xFFEF6C00)
                ) { onLog ->
                    burstyEmitter()
                        .debounce(150L)
                        .collect { value ->
                            onLog("debounced→ $value")
                        }
                }
            }
            item {
                OperatorDemoCard(
                    title = "5. sample(200ms) — 주기적으로 최신값만 샘플링",
                    description = "200ms마다 가장 최근 emit된 값을 다운스트림으로 전달",
                    accent = Color(0xFF8E24AA)
                ) { onLog ->
                    fastEmitter()
                        .sample(200L)
                        .collect { value ->
                            onLog("sampled→ $value")
                        }
                }
            }
            item { GuideCard() }
        }
    }
}

/** 1.. EMIT_COUNT를 EMIT_INTERVAL_MS 간격으로 emit. */
private fun fastEmitter() = flow {
    repeat(EMIT_COUNT) { i ->
        emit(i + 1)
        delay(EMIT_INTERVAL_MS)
    }
}

/** debounce 시각화용 — 빠른 입력 burst + 사이의 짧은 정지(150ms 미만). */
private fun burstyEmitter() = flow {
    val pattern = listOf(
        "K" to 80L, "Ko" to 80L, "Kot" to 80L, "Kotl" to 80L, "Kotli" to 80L, "Kotlin" to 200L,
        "Kotlin " to 80L, "Kotlin F" to 80L, "Kotlin Fl" to 80L, "Kotlin Flo" to 80L, "Kotlin Flow" to 0L
    )
    pattern.forEach { (text, gap) ->
        emit(text)
        if (gap > 0) delay(gap)
    }
}

@Composable
private fun OperatorDemoCard(
    title: String,
    description: String,
    accent: Color,
    block: suspend (onLog: (String) -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val logs = remember { mutableStateListOf<String>() }
    var job by remember { mutableStateOf<Job?>(null) }
    val isRunning = job?.isActive == true
    var startTimeMs by remember { mutableStateOf(0L) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        if (isRunning) {
                            job?.cancel()
                        } else {
                            logs.clear()
                            startTimeMs = System.currentTimeMillis()
                            job = scope.launch {
                                block { msg ->
                                    val t = System.currentTimeMillis() - startTimeMs
                                    logs += String.format(Locale.US, "[%4dms] %s", t, msg)
                                }
                                logs += "─ 완료 ─"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text(text = if (isRunning) "중지" else "실행", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "이벤트 ${logs.size}건",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(logs.size) { idx ->
                        Text(
                            text = logs[idx],
                            color = Color(0xFFCFD8DC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Flow 속도 제어 연산자 비교",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "빠른 producer (${EMIT_INTERVAL_MS}ms 간격, ${EMIT_COUNT}건) + 느린 consumer (${SLOW_COLLECT_MS}ms 처리) 환경에서 각 연산자가 어떻게 동작하는지 타임라인 로그로 비교.",
                fontSize = 12.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun GuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "선택 기준", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "buffer — 모든 값을 처리해야 하지만 emit/collect 속도를 분리하고 싶을 때",
                "conflate — 최신 값만 중요. 중간 값 손실 OK (UI 상태 갱신)",
                "debounce — 사용자 입력이 멈춘 순간이 의미 있을 때 (검색창 자동완성)",
                "sample — 일정 주기로 가장 최근 상태가 필요할 때 (마우스 위치, 센서)"
            ).forEach {
                Text(
                    text = "• $it",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

private const val EMIT_COUNT = 8
private const val EMIT_INTERVAL_MS = 100L
private const val SLOW_COLLECT_MS = 300L
