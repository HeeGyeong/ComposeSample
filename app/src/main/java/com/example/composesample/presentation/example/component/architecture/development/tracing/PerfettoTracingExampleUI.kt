package com.example.composesample.presentation.example.component.architecture.development.tracing

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.tracing.Trace
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

// ============================================================================
// 관찰 인프라 — 여러 Dispatchers.Default 워커 스레드가 동시에 기록하므로, RecomposerRegistryExampleUI
// 의 RegistryEventRing 과 같은 이유로 Compose State 가 아닌 plain 동기화 버퍼에 먼저 적재하고,
// joinAll() 이후(메인 컨텍스트로 복귀한 시점) 한 번에 Compose State 로 반영한다.
// ============================================================================

private data class TraceLogEntry(
    val label: String,
    val beginThread: String,
    val endThread: String,
    val mismatch: Boolean
)

private class TraceEventBuffer {
    private val lock = Any()
    private val entries = mutableListOf<TraceLogEntry>()

    fun add(entry: TraceLogEntry) {
        synchronized(lock) { entries.add(0, entry) }
    }

    fun snapshot(): List<TraceLogEntry> = synchronized(lock) { entries.toList() }

    fun clear() = synchronized(lock) { entries.clear() }
}

// ============================================================================
// UI
// ============================================================================

@Composable
fun PerfettoTracingExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Perfetto 코루틴/Flow 트레이싱",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { SyncPitfallCard() }
            item { AsyncSafeCard() }
            item { CounterTrackCard() }
            item { ComparisonCard() }
            item { LimitationCard() }
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
                text = "androidx.tracing 이란",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Perfetto/System Trace 에 커스텀 구간을 남기는 API 입니다. 지금까지의 리컴포지션 예제들이 " +
                        "\"컴포지션이 왜/언제\"를 봤다면, 이 예제는 \"코루틴이 어느 스레드에서 얼마나\" 걸렸는지를 " +
                        "시스템 트레이싱 도구로 넘기는 축입니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// ① 동기 구간 — 반드시 같은 스레드에서 begin/end 페어링\n" +
                        "Trace.beginSection(\"label\")\n" +
                        "Trace.endSection()\n\n" +
                        "// ② 비동기 구간 — (label, cookie) 로 상관관계, 스레드 무관하게 안전\n" +
                        "Trace.beginAsyncSection(\"label\", cookie)\n" +
                        "Trace.endAsyncSection(\"label\", cookie)\n\n" +
                        "// ③ 값 트랙 — 시간에 따라 변하는 값 기록\n" +
                        "Trace.setCounter(\"name\", value)",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "⚠️ 실측: androidx.tracing:tracing 1.2.0 은 이 프로젝트의 debugRuntimeClasspath 에는 " +
                        "이미 전이 해석돼 있었지만 debugCompileClasspath 에는 없어, Trace 클래스를 코드에서 " +
                        "직접 쓰려면 build.gradle 에 명시적 의존성 선언이 필요했습니다.",
                fontSize = 11.sp,
                color = Color(0xFF6D4C41),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SyncPitfallCard() {
    val scope = rememberCoroutineScope()
    val buffer = remember { TraceEventBuffer() }
    var running by remember { mutableStateOf(false) }
    var totalCount by remember { mutableIntStateOf(0) }
    var mismatchCount by remember { mutableIntStateOf(0) }
    val logLines = remember { mutableStateListOf<TraceLogEntry>() }

    fun publish() {
        val snap = buffer.snapshot()
        logLines.clear()
        logLines.addAll(snap)
        totalCount = snap.size
        mismatchCount = snap.count { it.mismatch }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — 함정: beginSection/endSection 은 같은 스레드에서만 안전",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dispatchers.Default(스레드풀)에서 구간 도중 delay 로 suspend 하면, 재개 시 다른 워커 " +
                        "스레드로 옮겨갈 수 있습니다. 8개를 동시에 실행해 begin/end 시점의 실제 스레드명을 " +
                        "직접 대조해보세요(스케줄링에 따라 매번 재현되지는 않을 수 있습니다 — 여러 번 눌러보세요).",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "launch(Dispatchers.Default) {\n" +
                        "    Trace.beginSection(\"span\")   // 스레드 A 에서 시작\n" +
                        "    delay(30)                      // suspend — 재개 스레드가 바뀔 수 있음\n" +
                        "    Trace.endSection()             // 스레드 A 또는 B?\n" +
                        "}",
                borderColor = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    running = true
                    scope.launch {
                        val jobs = (1..8).map { idx ->
                            launch(Dispatchers.Default) {
                                val label = "sync-span-$idx"
                                Trace.beginSection(label)
                                val beginThread = Thread.currentThread().name
                                delay((10L..60L).random())
                                val endThread = Thread.currentThread().name
                                Trace.endSection()
                                buffer.add(TraceLogEntry(label, beginThread, endThread, beginThread != endThread))
                            }
                        }
                        jobs.joinAll()
                        publish()
                        running = false
                    }
                },
                enabled = !running,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text(text = if (running) "실행 중..." else "8개 동시 실행", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "총 실행 / 스레드 불일치", fontSize = 12.sp, color = Color(0xFFC62828))
                Text(
                    text = "$totalCount / $mismatchCount",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (logLines.isEmpty()) {
                    Text(
                        text = "(로그 없음 — \"8개 동시 실행\"을 눌러보세요)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    logLines.forEach { entry ->
                        Text(
                            text = "${entry.label}: begin=${entry.beginThread} / end=${entry.endThread}" +
                                    if (entry.mismatch) " ⚠️ 다름" else " (동일)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (entry.mismatch) Color(0xFFFF8A80) else Color(0xFFB3E5FC),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AsyncSafeCard() {
    val scope = rememberCoroutineScope()
    val buffer = remember { TraceEventBuffer() }
    val cookieGen = remember { AtomicInteger(1000) }
    var running by remember { mutableStateOf(false) }
    val logLines = remember { mutableStateListOf<TraceLogEntry>() }
    var traceEnabled by remember { mutableStateOf(Trace.isEnabled()) }

    fun publish() {
        logLines.clear()
        logLines.addAll(buffer.snapshot())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — 안전한 방법: beginAsyncSection/endAsyncSection",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "스레드가 아니라 (label, cookie) 조합으로 짝을 맞추기 때문에, 위와 똑같이 스레드가 " +
                        "바뀌어도 안전합니다. 로그의 스레드명이 달라도 ⚠️ 표시가 없는 것을 위 카드와 비교해보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "val cookie = nextCookie()\n" +
                        "Trace.beginAsyncSection(\"span\", cookie)\n" +
                        "delay(30)                              // 스레드가 바뀌어도 무관\n" +
                        "Trace.endAsyncSection(\"span\", cookie)",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Trace.isEnabled()", fontSize = 12.sp, color = Color(0xFF1565C0))
                Row {
                    Text(
                        text = if (traceEnabled) "true" else "false",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "시스템 트레이싱을 캡처 중이 아니면 보통 false 입니다. 라벨 문자열 계산 비용이 크다면 " +
                        "이 값으로 가드해 오버헤드를 줄일 수 있습니다.",
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E),
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        running = true
                        scope.launch {
                            val jobs = (1..8).map {
                                launch(Dispatchers.Default) {
                                    val cookie = cookieGen.incrementAndGet()
                                    val label = "async-span"
                                    Trace.beginAsyncSection(label, cookie)
                                    val beginThread = Thread.currentThread().name
                                    delay((10L..60L).random())
                                    val endThread = Thread.currentThread().name
                                    Trace.endAsyncSection(label, cookie)
                                    buffer.add(TraceLogEntry("$label#$cookie", beginThread, endThread, mismatch = false))
                                }
                            }
                            jobs.joinAll()
                            publish()
                            running = false
                        }
                    },
                    enabled = !running,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text(text = if (running) "실행 중..." else "8개 동시 실행", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { traceEnabled = Trace.isEnabled() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "isEnabled 새로고침", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (logLines.isEmpty()) {
                    Text(
                        text = "(로그 없음 — \"8개 동시 실행\"을 눌러보세요)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    logLines.forEach { entry ->
                        Text(
                            text = "${entry.label}: begin=${entry.beginThread} / end=${entry.endThread} (cookie 로 상관관계 유지)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFB3E5FC),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterTrackCard() {
    val scope = rememberCoroutineScope()
    var activeCollectors by remember { mutableIntStateOf(0) }
    val counterLog = remember { mutableStateListOf<String>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — setCounter: Flow 활성 구독자 수를 카운터 트랙으로",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00897B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "구독이 시작/종료될 때마다 setCounter 를 호출하면 Perfetto 가 시간축 그래프로 그려줍니다. " +
                        "\"구독 추가\"를 여러 번 눌러 겹치게 만들어보세요(2.5초 후 자동으로 구독이 끝납니다).",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "activeFlowCollectors", fontSize = 12.sp, color = Color(0xFF00695C))
                Text(
                    text = "$activeCollectors",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00695C)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    scope.launch {
                        activeCollectors++
                        Trace.setCounter("activeFlowCollectors", activeCollectors)
                        counterLog.add(0, "setCounter(activeFlowCollectors, $activeCollectors) — 구독 시작")
                        delay(2500)
                        activeCollectors--
                        Trace.setCounter("activeFlowCollectors", activeCollectors)
                        counterLog.add(0, "setCounter(activeFlowCollectors, $activeCollectors) — 구독 종료")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(text = "구독 추가", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 150.dp)
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (counterLog.isEmpty()) {
                    Text(
                        text = "(로그 없음 — \"구독 추가\"를 눌러보세요)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    counterLog.take(14).forEach { line ->
                        Text(
                            text = line,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFB2DFDB),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "세 API 의 쓰임새 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val rows = listOf(
                Triple("beginSection/endSection", "동기 구간, 같은 스레드", "일반 함수 호출처럼 suspend 없이 끝나는 구간"),
                Triple("beginAsyncSection/endAsyncSection", "cookie 기반, 스레드 무관", "코루틴처럼 스레드를 넘나드는 구간"),
                Triple("setCounter", "값 트랙", "활성 개수·큐 길이 등 시간에 따라 변하는 값")
            )
            rows.forEach { (name, requirement, useCase) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "요구사항: $requirement", fontSize = 10.sp, color = Color(0xFF757575))
                    Text(text = "적합한 경우: $useCase", fontSize = 10.sp, color = Color(0xFF757575))
                }
            }
        }
    }
}

@Composable
private fun LimitationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "주의할 점",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))

            val notes = listOf(
                Triple(
                    "스레드 불일치는 매번 재현되지 않는다",
                    "확률적",
                    "Dispatchers.Default 는 스레드풀이라 재개 스레드가 바뀔 확률은 풀의 여유 스레드 수·타이밍에 따라 달라집니다. 위 카드에서 8개를 동시에 여러 번 실행해 대조해보세요."
                ),
                Triple(
                    "beginSection/endSection 은 예외를 던지지 않는다",
                    "조용히 깨짐",
                    "스레드가 어긋나도 즉시 크래시나 에러가 나지 않습니다 — Perfetto 캡처에서 구간이 열린 채 남거나 다른 구간과 뒤섞여 보이는 방식으로만 드러납니다."
                ),
                Triple(
                    "androidx.tracing 은 컴파일 클래스패스에 없었다",
                    "실측으로 확정",
                    "다른 라이브러리가 런타임 클래스패스에만 전이로 끌어오고 있어, 코드에서 직접 쓰려면 이 예제에서 명시적으로 의존성을 추가해야 했습니다."
                ),
                Triple(
                    "결과를 온전히 보려면 Perfetto 캡처가 필요하다",
                    "관찰 방법이 다름",
                    "이 화면의 로그는 \"무엇이 기록됐는지\"를 캡처 없이 먼저 보여주는 것일 뿐입니다. 실제 트랙 시각화는 Android Studio Profiler > System Trace 나 adb shell perfetto 캡처가 필요합니다."
                )
            )
            notes.forEach { (title, verdict, note) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC62828)
                        )
                        Text(text = verdict, fontSize = 9.sp, color = Color(0xFF757575))
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), lineHeight = 14.sp)
                }
            }
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
                "Trace.beginSection/endSection 은 스레드별 스택으로 관리되어, 같은 스레드에서 페어링돼야 안전하다",
                "Dispatchers.Default/IO 는 스레드풀이라 코루틴이 suspend 후 재개될 때 다른 워커 스레드로 옮겨갈 수 있다 — 위 카드에서 실측으로 확인",
                "Trace.beginAsyncSection/endAsyncSection 은 (label, cookie) 로 상관관계를 맺어 스레드가 달라도 안전하다",
                "Trace.setCounter 는 활성 개수처럼 시간에 따라 변하는 값을 카운터 트랙으로 남긴다",
                "Trace.isEnabled() 로 현재 시스템 트레이싱 여부를 확인해, 라벨 계산 비용이 큰 경우 오버헤드를 가드할 수 있다",
                "androidx.tracing 은 이 프로젝트의 런타임 클래스패스에는 이미 있었지만 컴파일 클래스패스에는 없어 명시적 의존성 선언이 필요했다",
                "실제 트랙 시각화는 Android Studio Profiler > System Trace 또는 adb shell perfetto 캡처로 확인해야 한다"
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
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 15.sp
        )
    }
}
