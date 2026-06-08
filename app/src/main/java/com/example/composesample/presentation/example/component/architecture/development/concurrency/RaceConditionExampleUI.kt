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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicLong

/**
 * 코루틴 Race Condition 방지 — 공유 가변 상태 보호 4전략 비교
 * - 여러 코루틴이 같은 변수를 동시에 증가시키면 read-modify-write 가 겹쳐 업데이트가 유실된다.
 * - 비보호 / Atomic / Mutex / 단일 스레드 confinement 를 같은 부하로 실행해 정확성·소요시간을 비교한다.
 * - 참고 자료(URL/핵심 개념)는 exampleGuide.kt 의 "Race Condition" 섹션 참고.
 */
@Composable
fun RaceConditionExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "코루틴 Race Condition 방지",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item {
                // 1. 비보호 — counter++ 의 read-modify-write 가 멀티 스레드에서 겹쳐 업데이트 유실
                StrategyCard(
                    title = "1. 비보호 (var counter++)",
                    description = "동기화 없이 공유 var 를 증가. Dispatchers.Default(멀티 스레드)에서 read-modify-write 가 겹쳐 최종값이 기대값보다 작아진다(유실).",
                    accent = Color(0xFFE53935),
                    run = ::runUnprotected
                )
            }
            item {
                // 2. AtomicLong — 하드웨어 CAS 로 증가 연산 자체를 원자화
                StrategyCard(
                    title = "2. AtomicLong (CAS)",
                    description = "incrementAndGet() 은 compare-and-set 기반 원자 연산. 락 없이도 증가 1건이 분할되지 않아 유실이 없다. 단일 변수 카운터에 가장 가볍고 빠르다.",
                    accent = Color(0xFF43A047),
                    run = ::runAtomic
                )
            }
            item {
                // 3. Mutex — 코루틴용 상호 배제 락으로 임계 구역 직렬화
                StrategyCard(
                    title = "3. Mutex.withLock",
                    description = "코루틴 친화적 상호 배제 락. withLock { } 임계 구역을 한 번에 하나의 코루틴만 진입시켜 정확하다. 여러 상태를 함께 갱신할 때 적합하나 경합 시 직렬화 비용이 크다.",
                    accent = Color(0xFF1E88E5),
                    run = ::runMutex
                )
            }
            item {
                // 4. 단일 스레드 confinement — 모든 갱신을 한 스레드에 가둬 경쟁 자체를 제거
                StrategyCard(
                    title = "4. 단일 스레드 confinement",
                    description = "limitedParallelism(1) 디스패처에서만 상태를 갱신(스레드 한정). 동시 접근 자체가 발생하지 않아 락 없이 정확하다. 갱신마다 컨텍스트 전환 비용이 든다.",
                    accent = Color(0xFF8E24AA),
                    run = ::runConfined
                )
            }
            item { GuideCard() }
        }
    }
}

/** 실행 결과: 최종 카운터 값과 소요 시간(ms). */
private data class RunResult(val finalValue: Long, val elapsedMs: Long)

@Composable
private fun StrategyCard(
    title: String,
    description: String,
    accent: Color,
    run: suspend () -> Long
) {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<RunResult?>(null) }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        running = true
                        result = null
                        scope.launch {
                            val start = System.currentTimeMillis()
                            val value = run()
                            result = RunResult(value, System.currentTimeMillis() - start)
                            running = false
                        }
                    },
                    enabled = !running,
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text(text = if (running) "실행 중..." else "실행", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "코루틴 ${COROUTINE_COUNT}개 × ${INCREMENTS_PER_COROUTINE}회 = 기대 $EXPECTED",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ResultBox(result = result, running = running)
        }
    }
}

@Composable
private fun ResultBox(result: RunResult?, running: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
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
                        text = "동시 증가 실행 중...",
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            result == null -> {
                Text(
                    text = "실행을 눌러 결과를 확인하세요.",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            else -> {
                val lost = EXPECTED - result.finalValue
                val correct = lost == 0L
                Column {
                    Text(
                        text = "최종값 = ${result.finalValue} / 기대값 = $EXPECTED",
                        color = Color(0xFFCFD8DC),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (correct) {
                            "정확 ✓  (소요 ${result.elapsedMs}ms)"
                        } else {
                            "유실 발생 ✗  −$lost 건 손실  (소요 ${result.elapsedMs}ms)"
                        },
                        color = if (correct) Color(0xFF81C784) else Color(0xFFEF9A9A),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 전략별 실행 로직 — 모두 동일 부하(COROUTINE_COUNT개 코루틴 × INCREMENTS_PER_COROUTINE회)
// ──────────────────────────────────────────────────────────────────────────

/** 1. 비보호: 공유 var 를 멀티 스레드에서 동기화 없이 증가 → read-modify-write 경쟁으로 유실. */
private suspend fun runUnprotected(): Long = coroutineScope {
    var counter = 0L
    (1..COROUTINE_COUNT).map {
        launch(Dispatchers.Default) {
            repeat(INCREMENTS_PER_COROUTINE) {
                // counter++ = (읽기 → +1 → 쓰기) 3단계가 원자적이지 않아 다른 스레드 증가분을 덮어쓴다.
                counter++
            }
        }
    }.joinAll()
    counter
}

/** 2. AtomicLong: CAS 기반 원자 증가로 락 없이 정확. */
private suspend fun runAtomic(): Long = coroutineScope {
    val counter = AtomicLong(0)
    (1..COROUTINE_COUNT).map {
        launch(Dispatchers.Default) {
            repeat(INCREMENTS_PER_COROUTINE) {
                counter.incrementAndGet()
            }
        }
    }.joinAll()
    counter.get()
}

/** 3. Mutex: withLock 임계 구역을 직렬화해 정확. 경합 시 락 대기 비용 발생. */
private suspend fun runMutex(): Long = coroutineScope {
    var counter = 0L
    val mutex = Mutex()
    (1..COROUTINE_COUNT).map {
        launch(Dispatchers.Default) {
            repeat(INCREMENTS_PER_COROUTINE) {
                mutex.withLock { counter++ }
            }
        }
    }.joinAll()
    counter
}

/** 4. 단일 스레드 confinement: limitedParallelism(1) 에 갱신을 한정해 경쟁 자체를 제거. */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun runConfined(): Long = coroutineScope {
    var counter = 0L
    // 병렬도 1 디스패처 — 이 디스패처에서 실행되는 블록은 한 번에 하나만 수행된다.
    val confined = Dispatchers.Default.limitedParallelism(1)
    (1..COROUTINE_COUNT).map {
        launch(Dispatchers.Default) {
            repeat(INCREMENTS_PER_COROUTINE) {
                // 상태 갱신만 단일 스레드로 위임 → 동시 접근 없음
                withContext(confined) { counter++ }
            }
        }
    }.joinAll()
    counter
}

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Race Condition (경쟁 상태)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "여러 코루틴이 공유 가변 상태를 동시에 읽고 쓰면 연산이 겹쳐 결과가 비결정적으로 깨진다. " +
                        "아래 4가지 전략을 같은 부하(${COROUTINE_COUNT}개 코루틴 × ${INCREMENTS_PER_COROUTINE}회 증가, 기대값 $EXPECTED)로 실행해 보라. " +
                        "1번(비보호)만 최종값이 기대값보다 작아지고(유실), 나머지는 정확하다. 소요 시간으로 각 보호 기법의 비용도 함께 비교할 수 있다.",
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
            Text(text = "전략 선택 기준", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "단일 값 카운터/플래그 → Atomic (가장 가볍고 빠름, 락 없음)",
                "여러 상태를 함께 일관되게 갱신 → Mutex.withLock (임계 구역 보호)",
                "특정 자원에 대한 모든 접근을 한 곳으로 모음 → 단일 스레드 confinement",
                "Compose 상태 노출 → MutableStateFlow.update { } (CAS 루프로 안전한 갱신)",
                "주의: @Synchronized/Java lock 은 스레드를 블로킹해 코루틴에 부적합 → Mutex 사용"
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

private const val COROUTINE_COUNT = 100
private const val INCREMENTS_PER_COROUTINE = 1000
private const val EXPECTED = COROUTINE_COUNT * INCREMENTS_PER_COROUTINE
