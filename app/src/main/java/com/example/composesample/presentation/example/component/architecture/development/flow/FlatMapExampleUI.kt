package com.example.composesample.presentation.example.component.architecture.development.flow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@Composable
fun FlatMapExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "flatMap vs flatMapLatest",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FlatMapConcatDemoCard() }
            item { FlatMapMergeDemoCard() }
            item { FlatMapLatestDemoCard() }
            item { ConcurrencyControlCard() }
            item { ErrorHandlingCard() }
            item { SearchDemoCard() }
            item { PerformanceComparisonCard() }
        }
    }
}

// Request State for visualization
enum class RequestState { WAITING, RUNNING, COMPLETED, CANCELLED }

data class RequestStatus(
    val name: String,
    val state: RequestState = RequestState.WAITING,
    val progress: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun FlatMapConcatDemoCard() {
    var isRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(listOf<String>()) }
    var totalTime by remember { mutableStateOf(0L) }
    var requestStatuses by remember { mutableStateOf(listOf<RequestStatus>()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 flatMapConcat: 순차 처리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "각 내부 Flow를 순차적으로 수집합니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        requestStatuses = listOf(
                            RequestStatus("Request-1"),
                            RequestStatus("Request-2"),
                            RequestStatus("Request-3")
                        )
                        scope.launch {
                            val time = measureTimeMillis {
                                flowOf("Request-1", "Request-2", "Request-3")
                                    .flatMapConcat { request ->
                                        flow {
                                            val index = request.substringAfter("-").toInt() - 1
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    if (i == index) status.copy(state = RequestState.RUNNING)
                                                    else status
                                                }
                                            log = log + "▶ $request 시작"

                                            // Progress animation
                                            repeat(10) {
                                                delay(100)
                                                requestStatuses =
                                                    requestStatuses.mapIndexed { i, status ->
                                                        if (i == index) status.copy(progress = (it + 1) / 10f)
                                                        else status
                                                    }
                                            }

                                            emit("$request 완료")
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    if (i == index) status.copy(
                                                        state = RequestState.COMPLETED,
                                                        progress = 1f
                                                    )
                                                    else status
                                                }
                                            log = log + "✓ $request 완료"
                                        }
                                    }
                                    .collect { }
                            }
                            totalTime = time
                            log = log + "⏱️ 총 소요 시간: ${time}ms"
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "flatMapConcat 실행",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            // Visual Timeline
            if (requestStatuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🎬 실시간 진행 상태",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        requestStatuses.forEach { status ->
                            RequestVisualizationRow(status, Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (log.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        log.forEach { entry ->
                            Text(
                                text = entry,
                                fontSize = 11.sp,
                                color = when {
                                    entry.contains("시작") -> Color(0xFF2196F3)
                                    entry.contains("완료") && !entry.contains("시간") -> Color(
                                        0xFF4CAF50
                                    )

                                    entry.contains("시간") -> Color(0xFFFF6F00)
                                    else -> Color(0xFF666666)
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E7D32).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 특징",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ 순서 보장\n✓ 이전 Flow 완료 후 다음 시작\n✗ 느린 처리 속도 (${totalTime}ms)\n\n사용: 순서가 중요한 작업",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun FlatMapMergeDemoCard() {
    var isRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(listOf<String>()) }
    var totalTime by remember { mutableStateOf(0L) }
    var requestStatuses by remember { mutableStateOf(listOf<RequestStatus>()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ flatMapMerge: 병렬 처리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "여러 내부 Flow를 동시에 수집합니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        requestStatuses = listOf(
                            RequestStatus("Request-1"),
                            RequestStatus("Request-2"),
                            RequestStatus("Request-3")
                        )
                        scope.launch {
                            val time = measureTimeMillis {
                                flowOf("Request-1", "Request-2", "Request-3")
                                    .flatMapMerge { request ->
                                        flow {
                                            val index = request.substringAfter("-").toInt() - 1
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    if (i == index) status.copy(state = RequestState.RUNNING)
                                                    else status
                                                }
                                            log = log + "▶ $request 시작"

                                            // Progress animation (all run simultaneously)
                                            repeat(10) {
                                                delay(100)
                                                requestStatuses =
                                                    requestStatuses.mapIndexed { i, status ->
                                                        if (i == index) status.copy(progress = (it + 1) / 10f)
                                                        else status
                                                    }
                                            }

                                            emit("$request 완료")
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    if (i == index) status.copy(
                                                        state = RequestState.COMPLETED,
                                                        progress = 1f
                                                    )
                                                    else status
                                                }
                                            log = log + "✓ $request 완료"
                                        }
                                    }
                                    .collect { }
                            }
                            totalTime = time
                            log = log + "⏱️ 총 소요 시간: ${time}ms"
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "flatMapMerge 실행",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            // Visual Timeline
            if (requestStatuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🎬 실시간 진행 상태 (병렬 실행)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        requestStatuses.forEach { status ->
                            RequestVisualizationRow(status, Color(0xFF1976D2))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (log.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        log.forEach { entry ->
                            Text(
                                text = entry,
                                fontSize = 11.sp,
                                color = when {
                                    entry.contains("시작") -> Color(0xFF2196F3)
                                    entry.contains("완료") && !entry.contains("시간") -> Color(
                                        0xFF4CAF50
                                    )

                                    entry.contains("시간") -> Color(0xFFFF6F00)
                                    else -> Color(0xFF666666)
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 특징",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ 병렬 실행\n✓ 빠른 처리 속도 (~${totalTime}ms)\n✗ 순서 미보장\n\n사용: 독립적인 작업들",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun FlatMapLatestDemoCard() {
    var isRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(listOf<String>()) }
    var totalTime by remember { mutableStateOf(0L) }
    var requestStatuses by remember { mutableStateOf(listOf<RequestStatus>()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 flatMapLatest: 최신만 처리",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "새로운 값이 오면 이전 Flow를 취소합니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        requestStatuses = listOf(
                            RequestStatus("Request-1"),
                            RequestStatus("Request-2"),
                            RequestStatus("Request-3")
                        )

                        scope.launch {
                            val time = measureTimeMillis {
                                flow {
                                    emit("Request-1")
                                    delay(300)  // 빠르게 다음 값 방출
                                    emit("Request-2")
                                    delay(300)
                                    emit("Request-3")
                                }
                                    .flatMapLatest { request ->
                                        flow {
                                            val index = request.substringAfter("-").toInt() - 1

                                            // Start new request
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    when {
                                                        i == index -> status.copy(state = RequestState.RUNNING)
                                                        status.state == RequestState.RUNNING -> status.copy(
                                                            state = RequestState.CANCELLED
                                                        )

                                                        else -> status
                                                    }
                                                }
                                            log = log + "▶ $request 시작"

                                            // Progress animation
                                            repeat(10) {
                                                delay(100)
                                                val currentStatus = requestStatuses.getOrNull(index)
                                                if (currentStatus?.state == RequestState.RUNNING) {
                                                    requestStatuses =
                                                        requestStatuses.mapIndexed { i, status ->
                                                            if (i == index) status.copy(progress = (it + 1) / 10f)
                                                            else status
                                                        }
                                                }
                                            }

                                            emit("$request 완료")
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    if (i == index && status.state == RequestState.RUNNING) {
                                                        status.copy(
                                                            state = RequestState.COMPLETED,
                                                            progress = 1f
                                                        )
                                                    } else status
                                                }
                                            log = log + "✓ $request 완료"
                                        }
                                    }
                                    .collect { }
                            }
                            totalTime = time
                            log = log + "⏱️ 총 소요 시간: ${time}ms"
                            log = log + "⚠️ Request-1, 2는 취소됨 (완료 전에 새 요청)"
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "flatMapLatest 실행",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            // Visual Timeline
            if (requestStatuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🎬 실시간 진행 상태 (취소 동작)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        requestStatuses.forEach { status ->
                            RequestVisualizationRow(status, Color(0xFFE65100))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (log.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        log.forEach { entry ->
                            Text(
                                text = entry,
                                fontSize = 11.sp,
                                color = when {
                                    entry.contains("시작") -> Color(0xFF2196F3)
                                    entry.contains("✓") -> Color(0xFF4CAF50)
                                    entry.contains("⏱️") -> Color(0xFFFF6F00)
                                    entry.contains("⚠️") -> Color(0xFFFF5722)
                                    else -> Color(0xFF666666)
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 특징",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✓ 최신 결과만 유지\n✓ 이전 작업 자동 취소\n✓ 리소스 절약 (~${totalTime}ms)\n\n사용: 검색, 필터링, 탭 전환",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun ConcurrencyControlCard() {
    var isRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(listOf<String>()) }
    var concurrency by remember { mutableStateOf(2) }
    var requestStatuses by remember { mutableStateOf(listOf<RequestStatus>()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8EAF6),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎚️ 동시성 제어 (Concurrency)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "flatMapMerge는 동시 실행 개수를 제한할 수 있습니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Concurrency selector
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3F51B5).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "동시 실행 수:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3F51B5)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3).forEach { value ->
                            Button(
                                onClick = { concurrency = value },
                                modifier = Modifier.size(40.dp),
                                enabled = !isRunning,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (concurrency == value)
                                        Color(0xFF3F51B5)
                                    else
                                        Color(0xFFE0E0E0)
                                ),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "$value",
                                    color = if (concurrency == value) Color.White else Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        requestStatuses = (1..5).map { RequestStatus("Request-$it") }

                        scope.launch {
                            flowOf(1, 2, 3, 4, 5)
                                .flatMapMerge(concurrency = concurrency) { num ->
                                    flow {
                                        val index = num - 1
                                        requestStatuses = requestStatuses.mapIndexed { i, status ->
                                            if (i == index) status.copy(state = RequestState.RUNNING)
                                            else status
                                        }
                                        log = log + "▶ Request-$num 시작 (동시 실행: $concurrency)"

                                        repeat(10) {
                                            delay(100)
                                            requestStatuses =
                                                requestStatuses.mapIndexed { i, status ->
                                                    if (i == index) status.copy(progress = (it + 1) / 10f)
                                                    else status
                                                }
                                        }

                                        emit(num)
                                        requestStatuses = requestStatuses.mapIndexed { i, status ->
                                            if (i == index) status.copy(
                                                state = RequestState.COMPLETED,
                                                progress = 1f
                                            )
                                            else status
                                        }
                                        log = log + "✓ Request-$num 완료"
                                    }
                                }
                                .collect { }
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "실행 (5개 요청, 동시성: $concurrency)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            // Visual Timeline
            if (requestStatuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🎬 실시간 진행 (최대 $concurrency 개 동시 실행)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        requestStatuses.forEach { status ->
                            RequestVisualizationRow(status, Color(0xFF3F51B5))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (log.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        log.takeLast(8).forEach { entry ->
                            Text(
                                text = entry,
                                fontSize = 11.sp,
                                color = when {
                                    entry.contains("시작") -> Color(0xFF2196F3)
                                    entry.contains("완료") -> Color(0xFF4CAF50)
                                    else -> Color(0xFF666666)
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3F51B5).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 특징",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• concurrency=1: 순차 실행 (flatMapConcat과 유사)\n• concurrency=2: 최대 2개 동시 실행\n• concurrency=3+: 리소스 제어하며 병렬 처리\n\n사용: API rate limiting, 서버 부하 제어",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun ErrorHandlingCard() {
    var selectedOperator by remember { mutableStateOf("flatMapConcat") }
    var isRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf(listOf<String>()) }
    var requestStatuses by remember { mutableStateOf(listOf<RequestStatus>()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ 에러 핸들링",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "각 연산자의 에러 전파 방식을 확인하세요",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Operator selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("flatMapConcat", "flatMapMerge", "flatMapLatest").forEach { op ->
                    Button(
                        onClick = { selectedOperator = op },
                        modifier = Modifier.weight(1f),
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedOperator == op)
                                Color(0xFFC62828)
                            else
                                Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            op.replace("flatMap", ""),
                            color = if (selectedOperator == op) Color.White else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        requestStatuses = listOf(
                            RequestStatus("Request-1"),
                            RequestStatus("Request-2 💥"),
                            RequestStatus("Request-3")
                        )

                        scope.launch {
                            try {
                                flowOf(1, 2, 3)
                                    .let { source ->
                                        when (selectedOperator) {
                                            "flatMapConcat" -> source.flatMapConcat { num ->
                                                createErrorFlow(num, requestStatuses) {
                                                    requestStatuses = it; log =
                                                    (log + it) as List<String>
                                                }
                                            }

                                            "flatMapMerge" -> source.flatMapMerge { num ->
                                                createErrorFlow(num, requestStatuses) {
                                                    requestStatuses = it; log =
                                                    (log + it) as List<String>
                                                }
                                            }

                                            else -> source.flatMapLatest { num ->
                                                createErrorFlow(num, requestStatuses) {
                                                    requestStatuses = it; log =
                                                    (log + it) as List<String>
                                                }
                                            }
                                        }
                                    }
                                    .collect { }
                            } catch (e: Exception) {
                                log = log + "❌ 에러 발생: ${e.message}"
                                log = log + "🛑 Flow 종료 (이후 요청 취소)"
                            }
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "실행 ($selectedOperator)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            // Visual Timeline
            if (requestStatuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "🎬 에러 전파 확인",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        requestStatuses.forEach { status ->
                            RequestVisualizationRow(status, Color(0xFFC62828))
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (log.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        log.forEach { entry ->
                            Text(
                                text = entry,
                                fontSize = 11.sp,
                                color = when {
                                    entry.contains("시작") -> Color(0xFF2196F3)
                                    entry.contains("✓") -> Color(0xFF4CAF50)
                                    entry.contains("❌") || entry.contains("💥") -> Color(0xFFD32F2F)
                                    entry.contains("🛑") -> Color(0xFFFF5722)
                                    else -> Color(0xFF666666)
                                },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFC62828).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 에러 처리 특징",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 모든 flatMap은 내부 Flow 에러 시 즉시 종료\n• 이후 요청은 실행되지 않음\n• try-catch 또는 catch 연산자로 핸들링 필요\n\n사용: retry, onErrorReturn 등과 조합",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

private fun createErrorFlow(
    num: Int,
    currentStatuses: List<RequestStatus>,
    onUpdate: (List<RequestStatus>) -> Unit
): kotlinx.coroutines.flow.Flow<Int> = flow {
    val index = num - 1
    onUpdate(currentStatuses.mapIndexed { i, status ->
        if (i == index) status.copy(state = RequestState.RUNNING) else status
    })

    delay(500)

    if (num == 2) {
        onUpdate(currentStatuses.mapIndexed { i, status ->
            if (i == index) status.copy(state = RequestState.CANCELLED, progress = 0.5f) else status
        })
        throw IllegalStateException("Request-2에서 에러 발생 💥")
    }

    onUpdate(currentStatuses.mapIndexed { i, status ->
        if (i == index) status.copy(state = RequestState.COMPLETED, progress = 1f) else status
    })

    emit(num)
}

@Composable
private fun SearchDemoCard() {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<String>()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchLog by remember { mutableStateOf(listOf<String>()) }

    // 검색어가 변경될 때마다 flatMapLatest로 검색
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            isSearching = true
            searchLog = searchLog + "🔍 검색: '$searchQuery'"

            // flatMapLatest 시뮬레이션
            delay(800)  // API 호출 시뮬레이션

            searchResults = listOf(
                "${searchQuery} - 결과 1",
                "${searchQuery} - 결과 2",
                "${searchQuery} - 결과 3"
            )
            searchLog = searchLog + "✓ '$searchQuery' 검색 완료"
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔍 실전 예제: 검색 기능",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "flatMapLatest를 사용한 검색 (이전 검색 자동 취소)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("검색어 입력 (2글자 이상)", fontSize = 12.sp) },
                placeholder = { Text("예: Android", fontSize = 12.sp) },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF7B1FA2),
                    focusedLabelColor = Color(0xFF7B1FA2)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (isSearching) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "검색 중...",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "검색 결과:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        searchResults.forEach { result ->
                            Text(
                                "• $result",
                                fontSize = 11.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (searchLog.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "검색 로그:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        searchLog.takeLast(5).forEach { entry ->
                            Text(
                                entry,
                                fontSize = 10.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 왜 flatMapLatest?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 빠르게 입력 시 이전 검색 취소\n• 최신 검색어 결과만 표시\n• 네트워크 요청 최소화\n• 리소스 절약",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun PerformanceComparisonCard() {
    var concatTime by remember { mutableStateOf(0L) }
    var mergeTime by remember { mutableStateOf(0L) }
    var latestTime by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFCE4EC),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚖️ 성능 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "3개의 요청(각 1초)을 다른 방식으로 처리",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        concatTime = 0
                        mergeTime = 0
                        latestTime = 0

                        scope.launch {
                            // flatMapConcat
                            concatTime = measureTimeMillis {
                                flowOf(1, 2, 3)
                                    .flatMapConcat { flow { delay(1000); emit(it) } }
                                    .collect { }
                            }

                            // flatMapMerge
                            mergeTime = measureTimeMillis {
                                flowOf(1, 2, 3)
                                    .flatMapMerge { flow { delay(1000); emit(it) } }
                                    .collect { }
                            }

                            // flatMapLatest
                            latestTime = measureTimeMillis {
                                flow {
                                    emit(1)
                                    delay(300)
                                    emit(2)
                                    delay(300)
                                    emit(3)
                                }
                                    .flatMapLatest { flow { delay(1000); emit(it) } }
                                    .collect { }
                            }

                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC2185B)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "측정 중..." else "성능 측정 시작",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (concatTime > 0 || mergeTime > 0 || latestTime > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                // flatMapConcat 결과
                PerformanceResultBox(
                    title = "flatMapConcat",
                    time = concatTime,
                    description = "순차 처리",
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // flatMapMerge 결과
                PerformanceResultBox(
                    title = "flatMapMerge",
                    time = mergeTime,
                    description = "병렬 처리",
                    color = Color(0xFF1976D2)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // flatMapLatest 결과
                PerformanceResultBox(
                    title = "flatMapLatest",
                    time = latestTime,
                    description = "최신만 처리",
                    color = Color(0xFFE65100)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFC2185B).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📊 결론",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2185B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• flatMapConcat: ~3초 (1+1+1)\n• flatMapMerge: ~1초 (병렬)\n• flatMapLatest: ~1초 (마지막만)\n\n상황에 맞는 연산자를 선택하세요!",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceResultBox(
    title: String,
    time: Long,
    description: String,
    color: Color
) {
    val progress by animateFloatAsState(
        targetValue = if (time > 0) 1f else 0f,
        animationSpec = tween(500)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }

            if (time > 0) {
                Box(
                    modifier = Modifier
                        .alpha(progress),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${time}ms",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = color
                )
            }
        }
    }
}

@Composable
private fun RequestVisualizationRow(
    status: RequestStatus,
    themeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    when (status.state) {
                        RequestState.WAITING -> Color(0xFFBDBDBD)
                        RequestState.RUNNING -> themeColor
                        RequestState.COMPLETED -> Color(0xFF4CAF50)
                        RequestState.CANCELLED -> Color(0xFFFF5722)
                    }
                )
                .border(
                    width = 2.dp,
                    color = when (status.state) {
                        RequestState.WAITING -> Color(0xFF9E9E9E)
                        RequestState.RUNNING -> themeColor.copy(alpha = 0.5f)
                        RequestState.COMPLETED -> Color(0xFF388E3C)
                        RequestState.CANCELLED -> Color(0xFFE64A19)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (status.state == RequestState.RUNNING) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Request name and progress
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (status.state) {
                        RequestState.WAITING -> Color(0xFF9E9E9E)
                        RequestState.RUNNING -> themeColor
                        RequestState.COMPLETED -> Color(0xFF4CAF50)
                        RequestState.CANCELLED -> Color(0xFFFF5722)
                    }
                )

                Text(
                    text = when (status.state) {
                        RequestState.WAITING -> "대기 중"
                        RequestState.RUNNING -> "${(status.progress * 100).toInt()}%"
                        RequestState.COMPLETED -> "✓ 완료"
                        RequestState.CANCELLED -> "✗ 취소됨"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = status.progress,
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearEasing
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when (status.state) {
                                RequestState.WAITING -> Color(0xFFBDBDBD)
                                RequestState.RUNNING -> themeColor
                                RequestState.COMPLETED -> Color(0xFF4CAF50)
                                RequestState.CANCELLED -> Color(0xFFFF5722)
                            }
                        )
                )
            }
        }
    }
}

