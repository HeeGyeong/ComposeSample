package com.example.composesample.presentation.example.component.architecture.development.concurrency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// ──────────────────────────────────────────────
// 시뮬레이션용 콜백 인터페이스 정의
// ──────────────────────────────────────────────

// 단순 성공/실패 콜백
interface SimpleCallback {
    fun onSuccess(result: String)
    fun onFailure(error: Exception)
}

// 취소 가능한 작업 핸들
interface CancellableTask {
    fun cancel()
}

// 진행률 콜백
interface ProgressCallback {
    fun onProgress(step: Int, total: Int)
    fun onComplete(result: String)
    fun onError(error: Exception)
}

// ──────────────────────────────────────────────
// 콜백 기반 API 시뮬레이터 (레거시 SDK 모의)
// ──────────────────────────────────────────────

object LegacyApiSimulator {

    /**
     * 콜백 기반 네트워크 요청 시뮬레이션
     * 실제 예: 구버전 Retrofit enqueue, OkHttp Callback 등
     */
    fun fetchData(query: String, callback: SimpleCallback): CancellableTask {
        var isCancelled = false
        // 비동기 작업 시뮬레이션 (실제로는 Thread나 Handler 사용)
        val thread = Thread {
            try {
                Thread.sleep(1500)
                if (!isCancelled) {
                    callback.onSuccess("결과: '$query' 조회 완료 (${System.currentTimeMillis() % 10000}ms)")
                }
            } catch (e: InterruptedException) {
                // 취소됨
            }
        }
        thread.start()

        return object : CancellableTask {
            override fun cancel() {
                isCancelled = true
                thread.interrupt()
            }
        }
    }

    /**
     * 실패 시나리오 시뮬레이션
     */
    fun fetchWithError(callback: SimpleCallback): CancellableTask {
        var isCancelled = false
        val thread = Thread {
            try {
                Thread.sleep(1000)
                if (!isCancelled) {
                    callback.onFailure(Exception("서버 오류: 500 Internal Server Error"))
                }
            } catch (e: InterruptedException) {
                // 취소됨
            }
        }
        thread.start()

        return object : CancellableTask {
            override fun cancel() {
                isCancelled = true
                thread.interrupt()
            }
        }
    }

    /**
     * 진행률 콜백 시뮬레이션
     * 실제 예: 파일 다운로드, 업로드 진행률 등
     */
    fun downloadWithProgress(callback: ProgressCallback): CancellableTask {
        var isCancelled = false
        val thread = Thread {
            try {
                val total = 5
                for (step in 1..total) {
                    if (isCancelled) return@Thread
                    Thread.sleep(600)
                    callback.onProgress(step, total)
                }
                if (!isCancelled) {
                    callback.onComplete("다운로드 완료 (${total}개 청크)")
                }
            } catch (e: InterruptedException) {
                // 취소됨
            } catch (e: Exception) {
                if (!isCancelled) callback.onError(e)
            }
        }
        thread.start()

        return object : CancellableTask {
            override fun cancel() {
                isCancelled = true
                thread.interrupt()
            }
        }
    }
}

// ──────────────────────────────────────────────
// suspendCoroutine 변환 함수들
// ──────────────────────────────────────────────

/**
 * suspendCoroutine — 취소를 지원하지 않는 기본 변환
 *
 * 단점: 코루틴이 취소되어도 내부 콜백은 계속 실행됨
 * 사용: 취소가 필요 없는 단순 일회성 작업
 */
suspend fun fetchDataWithSuspendCoroutine(query: String): String =
    suspendCoroutine { continuation ->
        LegacyApiSimulator.fetchData(query, object : SimpleCallback {
            override fun onSuccess(result: String) {
                continuation.resume(result)
            }

            override fun onFailure(error: Exception) {
                continuation.resumeWithException(error)
            }
        })
    }

/**
 * suspendCancellableCoroutine — 취소를 지원하는 권장 변환
 *
 * invokeOnCancellation: 코루틴 취소 시 리소스 정리 실행
 * 사용: 대부분의 실제 Android API 변환에 권장
 */
suspend fun fetchDataWithSuspendCancellableCoroutine(query: String): String =
    suspendCancellableCoroutine { continuation ->
        val task = LegacyApiSimulator.fetchData(query, object : SimpleCallback {
            override fun onSuccess(result: String) {
                // isActive 확인: 이미 취소된 경우 resume 호출 방지
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }

            override fun onFailure(error: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
        })

        // 코루틴 취소 시 레거시 API 작업도 함께 취소
        continuation.invokeOnCancellation {
            task.cancel()
        }
    }

/**
 * 실패 케이스 — suspendCancellableCoroutine + 에러 처리
 */
suspend fun fetchDataWithError(): String =
    suspendCancellableCoroutine { continuation ->
        val task = LegacyApiSimulator.fetchWithError(object : SimpleCallback {
            override fun onSuccess(result: String) {
                if (continuation.isActive) continuation.resume(result)
            }

            override fun onFailure(error: Exception) {
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        })

        continuation.invokeOnCancellation { task.cancel() }
    }

// ──────────────────────────────────────────────
// UI
// ──────────────────────────────────────────────

@Composable
fun CoroutineBridgesExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E))
    ) {
        MainHeader(
            title = "Coroutine Bridges",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewSection() }
            item { SuspendCoroutineSection() }
            item { SuspendCancellableCoroutineSection() }
            item { ErrorHandlingSection() }
            item { CancellationDemoSection() }
            item { ComparisonSection() }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ── 개요 섹션 ──
@Composable
private fun OverviewSection() {
    SectionCard(title = "콜백 → suspend 변환 필요성") {
        InfoText("레거시 Android API(카메라, BLE, 위치, 구버전 네트워크 라이브러리 등)는\n콜백 방식으로 설계되어 코루틴과 자연스럽게 통합되지 않습니다.")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
// ❌ 콜백 방식 — 중첩, 에러 처리 복잡
api.fetch("query") { result ->
    api.process(result) { processed ->
        api.save(processed) { saved ->
            // 콜백 지옥
        }
    }
}

// ✅ suspend 변환 후 — 순차적, 명확한 에러 처리
val result = api.fetchSuspend("query")
val processed = api.processSuspend(result)
val saved = api.saveSuspend(processed)
            """.trimIndent()
        )
    }
}

// ── suspendCoroutine 섹션 ──
@Composable
private fun SuspendCoroutineSection() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("버튼을 눌러 실행") }
    var isLoading by remember { mutableStateOf(false) }

    SectionCard(title = "1. suspendCoroutine (기본)") {
        InfoText("코루틴 취소를 지원하지 않는 기본 변환 함수.\n단순 일회성 작업에 사용합니다.")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
suspend fun fetchData(query: String): String =
    suspendCoroutine { continuation ->
        legacyApi.fetch(query, object : Callback {
            override fun onSuccess(result: String) {
                continuation.resume(result)
            }
            override fun onFailure(e: Exception) {
                continuation.resumeWithException(e)
            }
        })
    }
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ResultRow(
            label = "결과",
            value = result,
            isLoading = isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton(
            text = "suspendCoroutine 실행",
            color = Color(0xFF4CAF50),
            enabled = !isLoading
        ) {
            scope.launch {
                isLoading = true
                result = "요청 중..."
                try {
                    result = fetchDataWithSuspendCoroutine("테스트 쿼리")
                } catch (e: Exception) {
                    result = "오류: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}

// ── suspendCancellableCoroutine 섹션 ──
@Composable
private fun SuspendCancellableCoroutineSection() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("버튼을 눌러 실행") }
    var isLoading by remember { mutableStateOf(false) }

    SectionCard(title = "2. suspendCancellableCoroutine (권장)") {
        InfoText("코루틴 취소 시 invokeOnCancellation으로 리소스를 정리합니다.\n대부분의 Android API 변환에 권장되는 방식입니다.")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
suspend fun fetchData(query: String): String =
    suspendCancellableCoroutine { continuation ->
        val task = legacyApi.fetch(query, object : Callback {
            override fun onSuccess(result: String) {
                if (continuation.isActive)
                    continuation.resume(result)
            }
            override fun onFailure(e: Exception) {
                if (continuation.isActive)
                    continuation.resumeWithException(e)
            }
        })
        // 코루틴 취소 시 레거시 작업도 함께 취소
        continuation.invokeOnCancellation {
            task.cancel()
        }
    }
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ResultRow(label = "결과", value = result, isLoading = isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton(
            text = "suspendCancellableCoroutine 실행",
            color = Color(0xFF2196F3),
            enabled = !isLoading
        ) {
            scope.launch {
                isLoading = true
                result = "요청 중..."
                try {
                    result = fetchDataWithSuspendCancellableCoroutine("Compose 예제")
                } catch (e: CancellationException) {
                    result = "취소됨"
                } catch (e: Exception) {
                    result = "오류: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}

// ── 에러 처리 섹션 ──
@Composable
private fun ErrorHandlingSection() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("버튼을 눌러 실패 시나리오 실행") }
    var isLoading by remember { mutableStateOf(false) }

    SectionCard(title = "3. 에러 처리") {
        InfoText("콜백의 onFailure를 resumeWithException으로 전달합니다.\ntry-catch로 일반 suspend 함수처럼 처리할 수 있습니다.")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
// 호출부: 일반 예외처럼 처리
scope.launch {
    try {
        val result = fetchDataWithError()
    } catch (e: Exception) {
        // onFailure가 resumeWithException으로 전달됨
        Log.e("TAG", e.message)
    }
}
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ResultRow(label = "결과", value = result, isLoading = isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton(
            text = "실패 시나리오 실행",
            color = Color(0xFFFF5722),
            enabled = !isLoading
        ) {
            scope.launch {
                isLoading = true
                result = "요청 중..."
                try {
                    result = fetchDataWithError()
                } catch (e: Exception) {
                    result = "✅ 예외 캐치 성공: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}

// ── 취소 데모 섹션 ──
@Composable
private fun CancellationDemoSection() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("실행 후 취소 버튼을 눌러보세요") }
    var isLoading by remember { mutableStateOf(false) }
    var currentJob by remember { mutableStateOf<Job?>(null) }

    SectionCard(title = "4. 취소 전파 데모") {
        InfoText("코루틴 취소 시 invokeOnCancellation이 호출되어\n레거시 API 작업도 함께 정리됩니다.")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
// withTimeout으로도 자동 취소 가능
val result = withTimeout(1000L) {
    fetchDataWithSuspendCancellableCoroutine("query")
    // 1초 초과 시 TimeoutCancellationException 발생
    // → invokeOnCancellation 자동 호출 → task.cancel()
}
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ResultRow(label = "상태", value = result, isLoading = isLoading)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(
                text = "작업 시작",
                color = Color(0xFF9C27B0),
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                val job = scope.launch {
                    isLoading = true
                    result = "작업 진행 중... (취소 가능)"
                    try {
                        val fetched = fetchDataWithSuspendCancellableCoroutine("취소 테스트")
                        result = fetched
                    } catch (e: CancellationException) {
                        result = "✅ 취소됨 — invokeOnCancellation 호출됨"
                    } finally {
                        isLoading = false
                    }
                }
                currentJob = job
            }
            ActionButton(
                text = "취소",
                color = Color(0xFFFF9800),
                enabled = isLoading,
                modifier = Modifier.weight(1f)
            ) {
                currentJob?.cancel()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        // withTimeout 데모
        ActionButton(
            text = "500ms 타임아웃 테스트 (자동 취소)",
            color = Color(0xFF607D8B),
            enabled = !isLoading
        ) {
            scope.launch {
                isLoading = true
                result = "500ms 후 자동 취소 예정..."
                try {
                    val fetched = withTimeout(500L) {
                        fetchDataWithSuspendCancellableCoroutine("타임아웃 테스트")
                    }
                    result = fetched
                } catch (e: Exception) {
                    result = "✅ 타임아웃으로 자동 취소: ${e.javaClass.simpleName}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
}

// ── 비교 섹션 ──
@Composable
private fun ComparisonSection() {
    SectionCard(title = "suspendCoroutine vs suspendCancellableCoroutine") {
        ComparisonRow(
            left = "suspendCoroutine",
            right = "suspendCancellableCoroutine"
        )
        Divider(color = Color(0xFF444444), modifier = Modifier.padding(vertical = 8.dp))
        ComparisonItem(
            aspect = "취소 지원",
            left = "❌ 미지원",
            right = "✅ 지원"
        )
        ComparisonItem(
            aspect = "invokeOnCancellation",
            left = "❌ 없음",
            right = "✅ 있음"
        )
        ComparisonItem(
            aspect = "리소스 정리",
            left = "수동 처리 필요",
            right = "자동 정리 가능"
        )
        ComparisonItem(
            aspect = "isActive 체크",
            left = "불가",
            right = "✅ 가능"
        )
        ComparisonItem(
            aspect = "권장 용도",
            left = "단순 일회성 변환",
            right = "대부분의 Android API"
        )
        Spacer(modifier = Modifier.height(8.dp))
        InfoText("💡 실제 Android API 예시:\n• FusedLocationProviderClient.lastLocation\n• BiometricPrompt 인증 콜백\n• BluetoothGattCallback\n• CameraX ImageCapture\n• WorkManager ListenableFuture")
    }
}

// ──────────────────────────────────────────────
// 공통 UI 컴포넌트
// ──────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        backgroundColor = Color(0xFF16213E),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color(0xFF00D4FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Card(
        backgroundColor = Color(0xFF0D1117),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = code,
            color = Color(0xFFE8E8E8),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(12.dp),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        color = Color(0xFFAAAAAA),
        fontSize = 13.sp,
        lineHeight = 20.sp
    )
}

@Composable
private fun ResultRow(label: String, value: String, isLoading: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "$label: ",
            color = Color(0xFF888888),
            fontSize = 12.sp
        )
        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF00D4FF),
                strokeWidth = 2.dp,
                modifier = Modifier
                    .height(16.dp)
                    .width(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = value,
            color = Color(0xFF00FF88),
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = color,
            disabledBackgroundColor = color.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ComparisonRow(left: String, right: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = left,
            color = Color(0xFFFFAA00),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = right,
            color = Color(0xFF00D4FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ComparisonItem(aspect: String, left: String, right: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = aspect, color = Color(0xFF888888), fontSize = 11.sp)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = left,
                color = Color(0xFFFFAA00),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = right,
                color = Color(0xFF00D4FF),
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
