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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.produceIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

/**
 * Flow onEachBatch — 크기 + 시간 윈도우 기준 배치 집계 연산자
 * - 단건 처리 vs 배치 처리(예: DB bulk insert)의 처리량을 타임라인으로 비교.
 * - buffer/conflate(FlowOperators 예제)는 "속도 분리/최신값"이 목적, 여기서는 "원소를 묶어 일괄 처리"가 목적.
 */
@Composable
fun FlowBatchingExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Flow: onEachBatch (배치 집계)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item {
                // 1. 단건 처리 — 원소마다 개별 처리 비용 발생 (예: 매 건 단일 INSERT)
                BatchDemoCard(
                    title = "1. 단건 처리 — onEach { }",
                    description = "원소 ${EMIT_COUNT}건을 한 건씩 처리. 매 건 ${SINGLE_PROCESS_MS}ms 비용 → 총 비용이 건수에 비례",
                    accent = Color(0xFF9E9E9E)
                ) { onLog ->
                    var processed = 0
                    fastEmitter()
                        .onEach { value ->
                            // 단건 처리: 매 원소마다 고정 비용 (단일 INSERT 시뮬레이션)
                            delay(SINGLE_PROCESS_MS)
                            processed++
                            onLog("처리 #$value (누적 $processed)")
                        }
                        .collect { }
                    onLog("총 $processed 건 단건 처리 완료")
                }
            }
            item {
                // 2. 배치 처리 — maxSize 또는 timeout 도달 시 묶어서 일괄 처리 (예: bulk INSERT)
                BatchDemoCard(
                    title = "2. 배치 처리 — onEachBatch(maxSize=$MAX_BATCH_SIZE, timeout=${BATCH_TIMEOUT_MS}ms)",
                    description = "최대 $MAX_BATCH_SIZE 건 또는 ${BATCH_TIMEOUT_MS}ms 윈도우로 묶어 한 번에 처리. 배치당 ${BATCH_PROCESS_MS}ms 고정 비용 → 호출 횟수가 크게 감소",
                    accent = Color(0xFF1E88E5)
                ) { onLog ->
                    var batchCount = 0
                    var processed = 0
                    fastEmitter()
                        .onEachBatch(maxSize = MAX_BATCH_SIZE, timeoutMillis = BATCH_TIMEOUT_MS) { batch ->
                            // 배치 처리: 묶음당 한 번의 고정 비용 (bulk INSERT 시뮬레이션)
                            delay(BATCH_PROCESS_MS)
                            batchCount++
                            processed += batch.size
                            onLog("배치 #$batchCount 처리 ${batch.size}건 $batch (누적 $processed)")
                        }
                        .collect { }
                    onLog("총 $batchCount 배치 / $processed 건 처리 완료")
                }
            }
            item {
                // 3. 미완성 배치 flush — 윈도우/업스트림 종료 시 남은 원소도 손실 없이 내보냄
                BatchDemoCard(
                    title = "3. 미완성 배치 flush",
                    description = "${SMALL_EMIT_COUNT}건을 maxSize=$MAX_BATCH_SIZE 로 묶으면 마지막 배치는 정원 미달. 업스트림 종료 시 남은 원소를 flush",
                    accent = Color(0xFF43A047)
                ) { onLog ->
                    var batchCount = 0
                    slowEmitter()
                        .onEachBatch(maxSize = MAX_BATCH_SIZE, timeoutMillis = BATCH_TIMEOUT_MS) { batch ->
                            batchCount++
                            val full = if (batch.size >= MAX_BATCH_SIZE) "정원" else "미완성(flush)"
                            onLog("배치 #$batchCount [$full] ${batch.size}건 $batch")
                        }
                        .collect { }
                    onLog("─ 총 $batchCount 배치 ─")
                }
            }
            item { GuideCard() }
        }
    }
}

/**
 * 크기(maxSize) 또는 시간 윈도우(timeoutMillis) 중 먼저 도달하는 조건으로
 * 업스트림 원소를 배치로 모아 일괄 처리(action)한 뒤, 원본 원소를 그대로 흘려보내는 커스텀 연산자.
 *
 * 동작 규칙:
 * - 첫 원소가 들어오면 timeout 타이머 시작
 * - maxSize 도달 → 즉시 배치 flush
 * - timeout 경과 → 그때까지 모인(미완성) 배치 flush
 * - 업스트림 종료 → 남은 원소를 마지막 배치로 flush (손실 없음)
 */
fun <T> Flow<T>.onEachBatch(
    maxSize: Int,
    timeoutMillis: Long,
    action: suspend (List<T>) -> Unit
): Flow<T> = flow {
    chunkedBatch(maxSize, timeoutMillis).collect { batch ->
        action(batch)
        batch.forEach { emit(it) }
    }
}

/**
 * 업스트림을 크기/시간 기준 배치(List)로 묶어 emit 하는 핵심 연산자.
 * produceIn 으로 업스트림을 채널에 버퍼링하고, withTimeoutOrNull 로 시간 윈도우를 구현한다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<T>.chunkedBatch(
    maxSize: Int,
    timeoutMillis: Long
): Flow<List<T>> = flow {
    coroutineScope {
        // 업스트림을 별도 코루틴에서 수집해 채널로 흘려보낸다 (emit/처리 속도 분리).
        val upstream = produceIn(this)
        val batch = mutableListOf<T>()
        var windowOpen = false

        while (true) {
            // 배치가 비어 있으면 무한 대기, 채워져 있으면 윈도우 시간만큼만 대기.
            val result = if (windowOpen) {
                withTimeoutOrNull(timeoutMillis) { upstream.receiveCatching() }
            } else {
                upstream.receiveCatching()
            }

            if (result == null) {
                // 시간 윈도우 만료 → 모인 배치 flush
                if (batch.isNotEmpty()) {
                    emit(batch.toList())
                    batch.clear()
                    windowOpen = false
                }
                continue
            }

            if (result.isClosed) {
                // 업스트림 종료 → 마지막 미완성 배치 flush
                if (batch.isNotEmpty()) emit(batch.toList())
                break
            }

            val item = result.getOrThrow()
            if (batch.isEmpty()) {
                // 첫 원소 도착 → 시간 윈도우 시작
                windowOpen = true
            }
            batch.add(item)
            if (batch.size >= maxSize) {
                // 정원 도달 → 즉시 flush
                emit(batch.toList())
                batch.clear()
                windowOpen = false
            }
        }
    }
}

/** 1.. EMIT_COUNT 를 EMIT_INTERVAL_MS 간격으로 빠르게 emit (배치가 정원으로 채워지는 시나리오). */
private fun fastEmitter(): Flow<Int> = flow {
    repeat(EMIT_COUNT) { i ->
        emit(i + 1)
        delay(EMIT_INTERVAL_MS)
    }
}

/** 느리게 emit 해서 timeout flush + 마지막 미완성 배치를 유도하는 시나리오. */
private fun slowEmitter(): Flow<Int> = flow {
    repeat(SMALL_EMIT_COUNT) { i ->
        emit(i + 1)
        delay(SLOW_EMIT_INTERVAL_MS)
    }
}

@Composable
private fun BatchDemoCard(
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
                text = "Flow 배치 집계 (onEachBatch)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "스트림 원소를 크기(maxSize) 또는 시간 윈도우(timeout) 기준으로 묶어 일괄 처리한다. " +
                        "DB bulk insert·네트워크 배치 전송처럼 '호출당 고정 비용'이 큰 작업에서 처리량을 크게 높인다. " +
                        "1번(단건)과 2번(배치)의 총 소요 시간을 비교해 보라.",
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
            Text(text = "buffer / conflate 와의 차이", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "buffer — 원소를 1건씩 그대로 전달하되 emit/collect 속도만 분리 (개수 보존)",
                "conflate — 처리 중 들어온 중간 값은 버리고 최신값만 (개수 손실 OK)",
                "onEachBatch — 여러 원소를 List로 묶어 한 번에 처리 (개수 보존 + 호출 횟수 감소)",
                "선택 기준: 호출당 고정 오버헤드가 큰 작업(bulk insert, batch API)이면 배치 집계가 유리"
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

private const val EMIT_COUNT = 20
private const val EMIT_INTERVAL_MS = 30L
private const val SLOW_EMIT_INTERVAL_MS = 120L
private const val SMALL_EMIT_COUNT = 7
private const val MAX_BATCH_SIZE = 5
private const val BATCH_TIMEOUT_MS = 300L
private const val SINGLE_PROCESS_MS = 60L
private const val BATCH_PROCESS_MS = 80L
