package com.example.composesample.presentation.example.component.architecture.development.concurrency

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

@Composable
fun CoroutinesInternalsExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Coroutines Internals",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { StateMachineDemoCard() }
            item { ContinuationDemoCard() }
            item { StructuredConcurrencyDemoCard() }
            item { CancellationDemoCard() }
            item { DispatchersDemoCard() }
            item { ParallelExecutionDemoCard() }
        }
    }
}

@Composable
private fun StateMachineDemoCard() {
    var currentState by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(listOf<String>()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 State Machine 시각화",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "suspend 함수가 State Machine으로 변환되는 과정",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // State 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StateIndicator(0, currentState, "State 0\nfunc1()")
                StateIndicator(1, currentState, "State 1\nfunc2()")
                StateIndicator(2, currentState, "State 2\nfunc3()")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        results = emptyList()
                        scope.launch {
                            currentState = 0
                            results = results + "▶ State 0: func1() 호출"
                            delay(1000)
                            results = results + "✓ func1() 완료 → a = 10"

                            currentState = 1
                            results = results + "▶ State 1: func2() 호출"
                            delay(1000)
                            results = results + "✓ func2() 완료 → b = 20"

                            currentState = 2
                            results = results + "▶ State 2: func3() 호출"
                            delay(1000)
                            results = results + "✓ func3() 완료 → c = 30"

                            results = results + "🎯 결과: a + b + c = 60"
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "State Machine 실행",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        results.forEach { result ->
                            Text(
                                text = result,
                                fontSize = 11.sp,
                                color = when {
                                    result.startsWith("▶") -> Color(0xFFE65100)
                                    result.startsWith("✓") -> Color(0xFF4CAF50)
                                    result.startsWith("🎯") -> Color(0xFF1976D2)
                                    else -> Color.Gray
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
                        text = "💡 핵심 포인트",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• label 변수로 현재 상태 추적\n• 각 suspend point가 하나의 state\n• 지역 변수는 Continuation 필드로 저장\n• 재개 시 저장된 state부터 계속 실행",
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
private fun StateIndicator(stateNum: Int, currentState: Int, label: String) {
    val alpha by animateFloatAsState(
        targetValue = if (stateNum == currentState) 1f else 0.3f,
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = RoundedCornerShape(8.dp),
            color = if (stateNum == currentState) Color(0xFFE65100) else Color(0xFFFFCC80)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$stateNum",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stateNum == currentState) Color.White else Color(0xFFE65100)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContinuationDemoCard() {
    var log by remember { mutableStateOf(listOf<String>()) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📦 Continuation: 중단과 재개",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Continuation은 '나머지 계산'을 표현합니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        scope.launch {
                            log = log + "1️⃣ 작업 시작"
                            log = log + "📍 suspend point 1 도달"
                            log = log + "💾 Continuation 저장 (상태, 지역변수)"
                            log = log + "⏸️ 코루틴 중단..."
                            delay(1500)

                            log = log + "▶️ 재개: Continuation.resumeWith() 호출"
                            log = log + "📂 저장된 상태 복원"
                            delay(1000)

                            log = log + "2️⃣ 중단 지점 이후부터 계속 실행"
                            log = log + "📍 suspend point 2 도달"
                            log = log + "💾 Continuation 다시 저장"
                            log = log + "⏸️ 다시 중단..."
                            delay(1500)

                            log = log + "▶️ 다시 재개"
                            log = log + "3️⃣ 작업 완료"
                            log = log + "✅ 최종 결과 반환"
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "Continuation 동작 보기",
                    color = Color.White,
                    fontSize = 13.sp
                )
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
                                    entry.contains("중단") -> Color(0xFFFF6F00)
                                    entry.contains("재개") -> Color(0xFF4CAF50)
                                    entry.contains("저장") -> Color(0xFF2196F3)
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
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 Continuation의 역할",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7B1FA2)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 중단 시: 현재 상태와 변수 저장\n• 재개 시: 저장된 지점부터 실행\n• 콜백의 일반화된 형태\n• 컴파일러가 자동으로 CPS 변환",
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
private fun StructuredConcurrencyDemoCard() {
    var parentState by remember { mutableStateOf("대기") }
    var child1State by remember { mutableStateOf("대기") }
    var child2State by remember { mutableStateOf("대기") }
    var child3State by remember { mutableStateOf("대기") }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🏗️ Structured Concurrency",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "계층적 코루틴 관리: 부모-자식 관계",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 부모 코루틴
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E7D32),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "부모 코루틴",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "상태: $parentState",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 자식 코루틴들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChildCoroutineBox("자식 1", child1State, Modifier.weight(1f))
                ChildCoroutineBox("자식 2", child2State, Modifier.weight(1f))
                ChildCoroutineBox("자식 3", child3State, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            parentState = "실행 중"
                            scope.launch {
                                // 자식 1
                                child1State = "실행 중"
                                delay(1000)
                                child1State = "완료 ✓"

                                // 자식 2
                                child2State = "실행 중"
                                delay(1500)
                                child2State = "완료 ✓"

                                // 자식 3
                                child3State = "실행 중"
                                delay(800)
                                child3State = "완료 ✓"

                                delay(500)
                                parentState = "완료 ✓"
                                isRunning = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("정상 실행", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            parentState = "실행 중"
                            val job = scope.launch {
                                child1State = "실행 중"
                                child2State = "실행 중"
                                child3State = "실행 중"
                                delay(1000)
                            }

                            scope.launch {
                                delay(500)
                                job.cancel()
                                parentState = "취소됨 ❌"
                                child1State = "취소됨 ❌"
                                child2State = "취소됨 ❌"
                                child3State = "취소됨 ❌"
                                isRunning = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("부모 취소", color = Color.White, fontSize = 11.sp)
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
                        text = "💡 원칙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 부모는 자식이 모두 완료될 때까지 대기\n• 부모 취소 → 모든 자식 취소\n• 자식 실패 → 부모 취소 (기본)\n• 리소스 누수 방지",
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
private fun ChildCoroutineBox(label: String, state: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = when {
            state.contains("완료") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            state.contains("취소") -> Color(0xFFFF5722).copy(alpha = 0.2f)
            state.contains("실행") -> Color(0xFF2196F3).copy(alpha = 0.2f)
            else -> Color(0xFFE0E0E0)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                state.contains("완료") -> Color(0xFF4CAF50)
                state.contains("취소") -> Color(0xFFFF5722)
                state.contains("실행") -> Color(0xFF2196F3)
                else -> Color(0xFFBDBDBD)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state,
                fontSize = 10.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CancellationDemoCard() {
    var isRunning by remember { mutableStateOf(false) }
    var iteration by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("준비") }
    var job: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "❌ Cooperative Cancellation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "협력적 취소: isActive 체크",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "현재 상태: $status",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (status) {
                            "실행 중" -> Color(0xFF2196F3)
                            "취소됨" -> Color(0xFFFF5722)
                            else -> Color(0xFF666666)
                        }
                    )

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "반복 횟수: $iteration",
                            fontSize = 13.sp,
                            color = Color(0xFF666666)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFFC2185B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (!isRunning) {
                            isRunning = true
                            status = "실행 중"
                            iteration = 0
                            job = scope.launch {
                                try {
                                    while (isActive) {  // 취소 체크!
                                        iteration++
                                        delay(300)
                                    }
                                } catch (e: CancellationException) {
                                    status = "취소됨"
                                    isRunning = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("시작", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        job?.cancel()
                        status = "취소됨"
                        isRunning = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("취소", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFC2185B).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "⚠️ 주의사항",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2185B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "❌ 취소 불가: while(true) { work() }\n✓ 취소 가능: while(isActive) { work() }\n\n취소는 협력적입니다. isActive 체크나 delay() 등의 취소 지점이 필요합니다.",
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
private fun DispatchersDemoCard() {
    var currentThread by remember { mutableStateOf("없음") }
    var log by remember { mutableStateOf(listOf<String>()) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 Dispatchers: 스레드 전환",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "withContext로 스레드를 전환합니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        log = emptyList()
                        scope.launch {
                            log = log + "▶ Main: ${Thread.currentThread().name}"

                            withContext(Dispatchers.Default) {
                                log = log + "🔧 Default: ${Thread.currentThread().name}"
                                delay(500)
                            }

                            withContext(Dispatchers.IO) {
                                log = log + "💾 IO: ${Thread.currentThread().name}"
                                delay(500)
                            }

                            log = log + "▶ 다시 Main: ${Thread.currentThread().name}"
                            log = log + "✅ 완료"
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "Dispatcher 전환 보기",
                    color = Color.White,
                    fontSize = 13.sp
                )
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
                                fontSize = 10.sp,
                                color = when {
                                    entry.contains("Main") -> Color(0xFF4CAF50)
                                    entry.contains("Default") -> Color(0xFF2196F3)
                                    entry.contains("IO") -> Color(0xFFFF9800)
                                    else -> Color(0xFF666666)
                                },
                                fontFamily = FontFamily.Monospace,
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
                color = Color(0xFF0277BD).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 Dispatchers 종류",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0277BD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Main: UI 스레드\n• IO: I/O 작업 (네트워크, 파일)\n• Default: CPU 집약적 작업\n• Unconfined: 호출자 스레드",
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
private fun ParallelExecutionDemoCard() {
    var task1State by remember { mutableStateOf("대기") }
    var task2State by remember { mutableStateOf("대기") }
    var task3State by remember { mutableStateOf("대기") }
    var totalTime by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ async/await: 병렬 실행",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "여러 작업을 병렬로 실행합니다",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskBox("Task 1\n1초", task1State, Modifier.weight(1f))
                TaskBox("Task 2\n1.5초", task2State, Modifier.weight(1f))
                TaskBox("Task 3\n0.8초", task3State, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (totalTime > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⏱️ 총 소요 시간: ${totalTime}ms",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "순차 실행: ~3300ms → 병렬 실행: ~${totalTime}ms",
                            fontSize = 11.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRunning) {
                        isRunning = true
                        task1State = "준비"
                        task2State = "준비"
                        task3State = "준비"
                        totalTime = 0

                        scope.launch {
                            val time = measureTimeMillis {
                                coroutineScope {
                                    val t1 = async {
                                        task1State = "실행 중"
                                        delay(1000)
                                        task1State = "완료 ✓"
                                        "User"
                                    }

                                    val t2 = async {
                                        task2State = "실행 중"
                                        delay(1500)
                                        task2State = "완료 ✓"
                                        "Posts"
                                    }

                                    val t3 = async {
                                        task3State = "실행 중"
                                        delay(800)
                                        task3State = "완료 ✓"
                                        "Stats"
                                    }

                                    // 모두 완료 대기
                                    t1.await()
                                    t2.await()
                                    t3.await()
                                }
                            }

                            totalTime = time
                            isRunning = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57F17)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (isRunning) "실행 중..." else "병렬 실행 시작",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF57F17).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 병렬 실행의 장점",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 여러 작업을 동시에 실행\n• 가장 긴 작업만큼만 소요\n• await()로 결과 수집\n• 하나라도 실패하면 모두 취소",
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
private fun TaskBox(label: String, state: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = when {
            state.contains("완료") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            state.contains("실행") -> Color(0xFF2196F3).copy(alpha = 0.2f)
            else -> Color(0xFFE0E0E0)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                state.contains("완료") -> Color(0xFF4CAF50)
                state.contains("실행") -> Color(0xFF2196F3)
                else -> Color(0xFFBDBDBD)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = state,
                fontSize = 10.sp,
                color = when {
                    state.contains("완료") -> Color(0xFF4CAF50)
                    state.contains("실행") -> Color(0xFF2196F3)
                    else -> Color(0xFF666666)
                },
                textAlign = TextAlign.Center
            )
        }
    }
}