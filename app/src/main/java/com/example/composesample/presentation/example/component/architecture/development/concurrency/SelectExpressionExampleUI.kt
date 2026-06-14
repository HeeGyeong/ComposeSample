package com.example.composesample.presentation.example.component.architecture.development.concurrency

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlin.random.Random

/**
 * Kotlin Select Expressions — 여러 suspending 작업을 경쟁시켜 먼저 끝난 결과만 채택
 * - select { } 는 여러 절(clause) 중 가장 먼저 준비되는 하나를 선택해 그 블록만 실행한다.
 * - onAwait(Deferred) / onTimeout / onReceiveCatching(Channel) 세 가지 절을 시나리오별로 비교한다.
 * - 참고 자료(URL/핵심 개념)는 exampleGuide.kt 의 "Select Expressions" 섹션 참고.
 */
@Composable
fun SelectExpressionExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Kotlin Select Expressions",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item {
                // 1. onAwait — 여러 미러 서버를 동시 요청하고 가장 먼저 끝난 응답만 채택
                ScenarioCard(
                    title = "1. 최속 미러 서버 (onAwait)",
                    description = "3개 미러 서버에 동시 요청(async)한 뒤 select 로 가장 먼저 응답한 Deferred 의 결과만 채택하고 나머지는 취소한다. 지연이 매번 랜덤이라 실행마다 승자가 바뀐다.",
                    accent = Color(0xFF1E88E5),
                    run = ::runFastestMirror
                )
            }
            item {
                // 2. onTimeout — 주 작업이 한도를 넘기면 폴백으로 전환
                ScenarioCard(
                    title = "2. 타임아웃 폴백 (onTimeout)",
                    description = "주 작업(async)과 onTimeout 절을 함께 select 한다. 작업이 한도(300ms) 안에 끝나면 그 결과를, 넘기면 캐시 폴백 값을 반환한다. 작업 지연이 랜덤이라 두 결과를 모두 볼 수 있다.",
                    accent = Color(0xFFF9A825),
                    run = ::runTimeoutFallback
                )
            }
            item {
                // 3. onReceiveCatching — 여러 채널을 도착 순서대로 멀티플렉싱
                ScenarioCard(
                    title = "3. 다중 채널 멀티플렉싱 (onReceiveCatching)",
                    description = "고속(50ms)·저속(120ms) 두 채널을 select 로 동시에 수신해, 먼저 도착한 값부터 순서대로 소비한다. 채널이 닫히면 onReceiveCatching 이 닫힘 결과를 돌려주어 종료를 감지한다.",
                    accent = Color(0xFF8E24AA),
                    run = ::runChannelMultiplex
                )
            }
            item { GuideCard() }
        }
    }
}

/** 실행 결과: 요약(승자/종류), 상세(값/소요), 타임라인 로그. */
private data class SelectResult(
    val summary: String,
    val detail: String,
    val logs: List<String>
)

@Composable
private fun ScenarioCard(
    title: String,
    description: String,
    accent: Color,
    run: suspend () -> SelectResult
) {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<SelectResult?>(null) }
    var running by remember { mutableStateOf(false) }

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
            Button(
                onClick = {
                    running = true
                    result = null
                    scope.launch {
                        result = run()
                        running = false
                    }
                },
                enabled = !running,
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(text = if (running) "실행 중..." else "실행", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            ResultBox(result = result, running = running)
        }
    }
}

@Composable
private fun ResultBox(result: SelectResult?, running: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            running -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFCFD8DC),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "select 경쟁 실행 중...",
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            result == null -> {
                Box(modifier = Modifier.height(44.dp), contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = "실행을 눌러 결과를 확인하세요.",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            else -> {
                Column {
                    Text(
                        text = "▶ ${result.summary}",
                        color = Color(0xFF81C784),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.detail,
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    result.logs.forEach { line ->
                        Text(
                            text = line,
                            color = Color(0xFF9E9E9E),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 시나리오별 실행 로직 — 모두 select { } 로 가장 먼저 준비되는 절을 선택
// ──────────────────────────────────────────────────────────────────────────

/**
 * 1. onAwait: 여러 Deferred 중 가장 먼저 끝난 것을 select 로 채택.
 * - 미러 서버 3개를 서로 다른 랜덤 지연으로 동시 요청한 뒤, 최속 응답만 사용하고 나머지는 취소한다.
 */
private suspend fun runFastestMirror(): SelectResult = coroutineScope {
    val mirrors = listOf("서버 A", "서버 B", "서버 C")
    val delays = mirrors.map { Random.nextLong(80, 400) }
    val logs = mirrors.mapIndexed { i, name ->
        "$name 요청 시작 (예상 ${delays[i]}ms)"
    }.toMutableList()

    val start = System.currentTimeMillis()
    val deferreds = mirrors.mapIndexed { i, name ->
        async {
            delay(delays[i])
            "$name 응답 데이터"
        }
    }

    // select: 가장 먼저 완료되는 Deferred 의 onAwait 절만 실행된다.
    val (winnerIndex, response) = select<Pair<Int, String>> {
        deferreds.forEachIndexed { i, d ->
            d.onAwait { result -> i to result }
        }
    }

    // 승자 외 나머지 요청은 더 이상 필요 없으므로 취소한다.
    deferreds.forEachIndexed { i, d -> if (i != winnerIndex) d.cancel() }
    val elapsed = System.currentTimeMillis() - start
    logs.add("→ ${mirrors[winnerIndex]} 가 가장 먼저 응답 → 나머지 취소")

    SelectResult(
        summary = "${mirrors[winnerIndex]} 승리",
        detail = "응답 = \"$response\" / 소요 ${elapsed}ms",
        logs = logs
    )
}

/**
 * 2. onTimeout: 주 작업과 시간 제한 절을 함께 경쟁.
 * - 작업이 한도(timeoutMs) 안에 끝나면 그 결과를, 넘기면 onTimeout 절의 폴백 값을 반환한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun runTimeoutFallback(): SelectResult = coroutineScope {
    val timeoutMs = 300L
    val workDelay = Random.nextLong(150, 600)
    val logs = mutableListOf(
        "주 작업 시작 (예상 ${workDelay}ms)",
        "타임아웃 한도 = ${timeoutMs}ms"
    )

    val start = System.currentTimeMillis()
    val work = async {
        delay(workDelay)
        "실시간 데이터"
    }

    // select: work 가 먼저 끝나면 onAwait, 아니면 onTimeout 절이 선택된다.
    val fallback = "캐시 폴백 데이터"
    val result = select {
        work.onAwait { it }
        onTimeout(timeoutMs) { fallback }
    }

    val elapsed = System.currentTimeMillis() - start
    val timedOut = result == fallback
    if (timedOut) {
        // 타임아웃이 선택되었으므로 아직 진행 중인 주 작업은 취소한다.
        work.cancel()
        logs.add("→ 한도 내 미응답 → onTimeout 발동 → 폴백 반환")
    } else {
        logs.add("→ 한도 내 응답 → 주 작업 결과 채택")
    }

    SelectResult(
        summary = if (timedOut) "타임아웃 → 폴백" else "정상 응답",
        detail = "결과 = \"$result\" / 소요 ${elapsed}ms",
        logs = logs
    )
}

/**
 * 3. onReceiveCatching: 여러 채널을 동시에 수신해 먼저 도착한 값부터 소비.
 * - 고속/저속 두 producer 가 같은 select 루프에서 도착 순서대로 멀티플렉싱된다.
 * - onReceiveCatching 은 채널이 닫혀도 예외 대신 닫힘 결과(ChannelResult)를 돌려주어 종료를 안전하게 감지한다.
 */
private suspend fun runChannelMultiplex(): SelectResult = coroutineScope {
    val logs = mutableListOf<String>()
    val fast = Channel<String>()
    val slow = Channel<String>()

    // 고속 producer: 50ms 간격 3건
    launch {
        repeat(3) { delay(50); fast.send("fast-$it") }
        fast.close()
    }
    // 저속 producer: 120ms 간격 3건
    launch {
        repeat(3) { delay(120); slow.send("slow-$it") }
        slow.close()
    }

    val start = System.currentTimeMillis()
    var fastOpen = true
    var slowOpen = true
    val received = mutableListOf<String>()

    // 두 채널이 모두 닫힐 때까지, 먼저 값이 준비되는 채널에서 수신한다.
    while (fastOpen || slowOpen) {
        val item = select<String?> {
            if (fastOpen) fast.onReceiveCatching { res ->
                res.getOrNull().also { if (it == null) fastOpen = false }
            }
            if (slowOpen) slow.onReceiveCatching { res ->
                res.getOrNull().also { if (it == null) slowOpen = false }
            }
        }
        if (item != null) {
            received.add(item)
            logs.add("수신: $item (${System.currentTimeMillis() - start}ms)")
        }
    }

    SelectResult(
        summary = "${received.size}건 수신 (도착 순)",
        detail = "순서 = ${received.joinToString(", ")}",
        logs = logs
    )
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
                text = "select expression (선택 표현식)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "select { } 는 여러 suspending 작업(절)을 동시에 대기하다가, 가장 먼저 준비되는 하나를 골라 그 블록만 실행하고 나머지는 무시한다. " +
                        "Deferred.onAwait / onTimeout / Channel.onReceiveCatching 등 절을 조합해 '가장 빠른 응답 채택', '타임아웃 폴백', '여러 소스 멀티플렉싱' 같은 패턴을 구현할 수 있다. " +
                        "아래 3가지 시나리오를 실행해 어떤 절이 선택되는지 타임라인 로그로 확인해 보라.",
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
            Text(text = "사용 기준 & 주의점", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "여러 데이터 소스 중 먼저 끝난 것만 채택 → Deferred.onAwait (최속 응답/헤지 요청)",
                "작업이 한도를 넘기면 대체값으로 전환 → onTimeout (withTimeoutOrNull 보다 유연)",
                "여러 채널/Flow 를 도착 순서대로 합치기 → onReceiveCatching (멀티플렉싱)",
                "select 는 선택된 절만 실행 → 나머지 Deferred/작업은 직접 cancel() 로 정리해야 누수가 없다",
                "한 번의 select 는 절 하나만 선택 → 반복 수신은 while 루프로 감싼다",
                "onTimeout/onReceiveCatching 일부 API 는 @ExperimentalCoroutinesApi 옵트인이 필요하다"
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
