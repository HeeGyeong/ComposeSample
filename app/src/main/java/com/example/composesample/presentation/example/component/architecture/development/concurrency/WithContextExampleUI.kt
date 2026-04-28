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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.system.measureTimeMillis

@Composable
fun WithContextExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "withContext vs launch",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ComparisonCard() }
            item { WithContextDemoCard() }
            item { LaunchDemoCard() }
            item { AsyncAwaitDemoCard() }
            item { ExceptionHandlingCard() }
            item { PerformanceComparisonCard() }
        }
    }
}

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔀 핵심 차이점",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // withContext
            ComparisonItem(
                title = "withContext(Dispatchers.IO)",
                icon = "⏸️",
                color = Color(0xFF1976D2),
                points = listOf(
                    "suspend 함수 (일시 중단)",
                    "작업 완료를 기다림",
                    "결과값 반환 가능",
                    "순차적 실행",
                    "예외 전파 (try-catch 가능)"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // launch
            ComparisonItem(
                title = "launch(Dispatchers.IO)",
                icon = "🚀",
                color = Color(0xFFE65100),
                points = listOf(
                    "새로운 코루틴 시작",
                    "즉시 다음 코드 실행",
                    "Job 반환 (결과값 X)",
                    "병렬 실행 가능",
                    "CoroutineExceptionHandler 필요"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E7D32).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 선택 기준",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 순차 + 결과 필요? → withContext\n• Fire-and-Forget? → launch\n• 병렬 + 결과 필요? → async/await\n• 예외를 호출한 곳에서 처리? → withContext/async",
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
private fun ComparisonItem(
    title: String,
    icon: String,
    color: Color,
    points: List<String>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            points.forEach { point ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(color)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = point,
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun WithContextDemoCard() {
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    var executionTime by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⏸️ withContext 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "순차 실행 + 결과 반환",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Code snippet
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF263238)
            ) {
                Text(
                    text = """
                        suspend fun fetchData(): String {
                            return withContext(Dispatchers.IO) {
                                delay(1000)  // 네트워크 시뮬레이션
                                "Data from API"  // 결과 반환
                            }
                        }
                        
                        // 사용
                        val data = fetchData()  // 완료 대기
                        updateUI(data)  // 결과 사용
                    """.trimIndent(),
                    fontSize = 9.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        result = null
                        executionTime = null

                        val time = measureTimeMillis {
                            // withContext로 순차 실행
                            val data1 = fetchDataWithContext("Task 1")
                            val data2 = fetchDataWithContext("Task 2")
                            result = "$data1\n$data2"
                        }

                        executionTime = time
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isLoading) "실행 중..." else "순차 실행 (withContext)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (result != null || executionTime != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1976D2).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✅ 실행 결과",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        result?.let {
                            Text(
                                text = it,
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        executionTime?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⏱️ 실행 시간: ${it}ms (순차 실행)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaunchDemoCard() {
    var isRunning by remember { mutableStateOf(false) }
    var log by remember { mutableStateOf<List<String>>(emptyList()) }
    var executionTime by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🚀 launch 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "병렬 실행 + Fire-and-Forget",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Code snippet
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF263238)
            ) {
                Text(
                    text = """
                        fun sendAnalytics() {
                            viewModelScope.launch(Dispatchers.IO) {
                                analytics.send()  // 결과 무시
                            }
                            // 즉시 다음 코드 실행
                        }
                        
                        // 병렬 실행
                        val job1 = launch(Dispatchers.IO) { task1() }
                        val job2 = launch(Dispatchers.IO) { task2() }
                        job1.join()  // 완료 대기
                        job2.join()
                    """.trimIndent(),
                    fontSize = 9.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isRunning = true
                        log = emptyList()
                        executionTime = null

                        val time = measureTimeMillis {
                            log = log + "시작: 두 작업 병렬 실행"

                            // launch로 병렬 실행
                            val job1 = launch(Dispatchers.IO) {
                                delay(1000)
                                log = log + "Task 1 완료"
                            }

                            val job2 = launch(Dispatchers.IO) {
                                delay(1000)
                                log = log + "Task 2 완료"
                            }

                            log = log + "즉시 실행: 작업 완료를 기다리지 않음"

                            job1.join()
                            job2.join()

                            log = log + "모든 작업 완료"
                        }

                        executionTime = time
                        isRunning = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isRunning
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isRunning) "실행 중..." else "병렬 실행 (launch)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (log.isNotEmpty() || executionTime != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📝 실행 로그",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        log.forEach { logItem ->
                            Text(
                                text = "• $logItem",
                                fontSize = 11.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        executionTime?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⏱️ 실행 시간: ${it}ms (병렬 실행)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceComparisonCard() {
    var isComparing by remember { mutableStateOf(false) }
    var withContextTime by remember { mutableStateOf<Long?>(null) }
    var launchTime by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ 성능 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "동일한 3개 작업 실행 (각 1초)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isComparing = true
                        withContextTime = null
                        launchTime = null

                        // withContext로 순차 실행
                        val time1 = measureTimeMillis {
                            val result1 = fetchDataWithContext("1")
                            val result2 = fetchDataWithContext("2")
                            val result3 = fetchDataWithContext("3")
                        }
                        withContextTime = time1

                        delay(500) // 잠깐 대기

                        // launch로 병렬 실행
                        val time2 = measureTimeMillis {
                            val job1 = launch(Dispatchers.IO) {
                                delay(1000)
                            }
                            val job2 = launch(Dispatchers.IO) {
                                delay(1000)
                            }
                            val job3 = launch(Dispatchers.IO) {
                                delay(1000)
                            }
                            job1.join()
                            job2.join()
                            job3.join()
                        }
                        launchTime = time2

                        isComparing = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isComparing
            ) {
                if (isComparing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isComparing) "비교 중..." else "성능 비교 시작",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (withContextTime != null && launchTime != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // withContext 결과
                PerformanceResultItem(
                    title = "withContext (순차)",
                    time = withContextTime!!,
                    color = Color(0xFF1976D2),
                    description = "3개 작업을 순차적으로 실행"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // launch 결과
                PerformanceResultItem(
                    title = "launch (병렬)",
                    time = launchTime!!,
                    color = Color(0xFFE65100),
                    description = "3개 작업을 병렬로 실행"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 분석
                val speedup = withContextTime!!.toFloat() / launchTime!!.toFloat()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📊 분석",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B1FA2)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• 병렬 실행이 약 ${String.format(Locale.US, "%.1f", speedup)}배 빠름\n• 독립적인 작업은 병렬 실행 권장\n• 의존적인 작업은 순차 실행 필요",
                            fontSize = 11.sp,
                            color = Color(0xFF666666),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceResultItem(
    title: String,
    time: Long,
    color: Color,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = Color(0xFF999999)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${time}ms",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AsyncAwaitDemoCard() {
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<String?>(null) }
    var executionTime by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ async/await 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3F51B5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "병렬 실행 + 결과 수집 (launch와 withContext의 장점 결합)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 설명
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF3F51B5).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📚 추가 이유",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• launch: 병렬 O, 결과 X\n• withContext: 병렬 X, 결과 O\n• async/await: 병렬 O, 결과 O ✨",
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Code snippet
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF263238)
            ) {
                Text(
                    text = """
                        // 실제 사용 예: 여러 API 동시 호출
                        suspend fun loadProfile() {
                            // 3개 작업을 동시에 시작
                            val user = async { fetchUser() }      // 1초
                            val posts = async { fetchPosts() }    // 1초
                            val friends = async { fetchFriends() } // 1초
                            
                            // 모든 결과를 기다림 (총 1초만 소요)
                            val profile = Profile(
                                user = user.await(),     // 결과 수집
                                posts = posts.await(),   // 결과 수집
                                friends = friends.await() // 결과 수집
                            )
                            // vs 순차 실행: 3초 소요
                        }
                    """.trimIndent(),
                    fontSize = 9.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        results = null
                        executionTime = null

                        val time = measureTimeMillis {
                            // async로 병렬 실행 + 결과 반환
                            val deferred1 = async(Dispatchers.IO) {
                                delay(1000)
                                "User Data"
                            }

                            val deferred2 = async(Dispatchers.IO) {
                                delay(1000)
                                "Posts Data"
                            }

                            val deferred3 = async(Dispatchers.IO) {
                                delay(1000)
                                "Friends Data"
                            }

                            // await()로 결과 수집
                            val result1 = deferred1.await()
                            val result2 = deferred2.await()
                            val result3 = deferred3.await()

                            results = "$result1\n$result2\n$result3"
                        }

                        executionTime = time
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isLoading) "실행 중..." else "병렬 + 결과 수집 (async/await)",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (results != null || executionTime != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF3F51B5).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✅ 실행 결과",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3F51B5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        results?.let {
                            Text(
                                text = it,
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                        executionTime?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⏱️ 실행 시간: ${it}ms (병렬 실행)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3F51B5)
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
                        text = "🎯 확인 포인트",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. async는 Deferred<T> 반환 (결과 컨테이너)\n2. await()로 실제 결과값 추출\n3. 여러 async 동시 시작 = 병렬 실행\n4. 실행시간: 가장 긴 작업만큼만 소요\n5. 모든 결과를 모아서 처리 가능",
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
private fun ExceptionHandlingCard() {
    var launchResult by remember { mutableStateOf<String?>(null) }
    var withContextResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ 예외 처리 차이",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "launch vs withContext의 예외 전파 방식 (실무에서 매우 중요)",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 추가 이유 설명
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📚 추가 이유",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "실무에서 네트워크 에러, 파싱 에러 등을 처리할 때\nlaunch와 withContext의 예외 처리 방식이 다름을\n모르면 에러를 놓치거나 앱이 크래시될 수 있음",
                        fontSize = 10.sp,
                        color = Color(0xFF666666),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // withContext 예외 처리
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF263238)
            ) {
                Text(
                    text = """
                        // withContext: 예외가 호출자에게 전파
                        suspend fun loadData() {
                            try {
                                val data = withContext(Dispatchers.IO) {
                                    if (error) throw Exception("Error!")
                                    "Data"
                                }
                                // 성공 처리
                            } catch (e: Exception) {
                                // ✅ 외부에서 예외 처리 가능!
                                showError(e)
                            }
                        }
                    """.trimIndent(),
                    fontSize = 9.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        withContextResult = try {
                            withContext(Dispatchers.IO) {
                                delay(500)
                                throw Exception("withContext 예외 발생!")
                            }
                            "성공"
                        } catch (e: Exception) {
                            "✅ 예외 포착됨: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "withContext 예외 테스트",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (withContextResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1976D2).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = withContextResult!!,
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // launch 예외 처리
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF263238)
            ) {
                Text(
                    text = """
                        // launch: 새로운 코루틴이므로 예외 내부 처리
                        try {
                            scope.launch {
                                throw Exception("Error!")
                            }
                        } catch (e: Exception) {
                            // ❌ 여기서 못 잡음! (이미 다른 코루틴)
                        }
                        
                        // ✅ 올바른 방법 1: 내부에서 처리
                        scope.launch {
                            try {
                                throw Exception("Error!")
                            } catch (e: Exception) {
                                showError(e)
                            }
                        }
                        
                        // ✅ 올바른 방법 2: CoroutineExceptionHandler
                    """.trimIndent(),
                    fontSize = 9.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        val job = launch {
                            try {
                                delay(500)
                                throw Exception("launch 예외 발생!")
                            } catch (e: Exception) {
                                launchResult = "✅ launch 내부에서 예외 포착: ${e.message}"
                            }
                        }
                        job.join()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "launch 예외 테스트",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }

            if (launchResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = launchResult!!,
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🎯 확인 포인트",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. withContext: suspend 함수 → 예외 전파 O\n   → 호출한 곳에서 try-catch 가능\n\n2. launch: 새 코루틴 → 예외 전파 X\n   → 내부에서 try-catch 필요\n   → 또는 CoroutineExceptionHandler\n\n3. async: await() 시점에 예외 발생\n   → await()를 try-catch로 감싸기\n\n4. 실무 팁: 에러 UI 표시가 필요하면\n   withContext/async가 더 편리",
                        fontSize = 11.sp,
                        color = Color(0xFF666666),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// Helper functions
private suspend fun fetchDataWithContext(name: String): String {
    return withContext(Dispatchers.IO) {
        delay(1000) // 네트워크 시뮬레이션
        "Result: $name"
    }
}

