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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
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

// 다운로드 진행 상태
sealed class DownloadState {
    object Idle : DownloadState()
    data class Progress(val step: Int, val total: Int) : DownloadState()
    data class Done(val result: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
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

/**
 * callbackFlow — 다중 값 콜백 → Flow 변환
 *
 * suspendCoroutine/suspendCancellableCoroutine은 단일 값 반환만 가능
 * 진행률처럼 여러 번 값을 방출하는 콜백은 callbackFlow로 변환
 *
 * trySend: Flow에 값 방출 (채널이 닫혀 있으면 무시)
 * close(): Flow 정상 종료
 * close(throwable): Flow 에러 종료
 * awaitClose: Flow 수집 종료 시 리소스 정리
 */
fun downloadWithCallbackFlow(): Flow<DownloadState> = callbackFlow {
    val task = LegacyApiSimulator.downloadWithProgress(object : ProgressCallback {
        override fun onProgress(step: Int, total: Int) {
            trySend(DownloadState.Progress(step, total))
        }

        override fun onComplete(result: String) {
            trySend(DownloadState.Done(result))
            close()
        }

        override fun onError(error: Exception) {
            close(error)
        }
    })

    // Flow 수집이 취소되거나 완료될 때 레거시 API 작업도 함께 정리
    awaitClose { task.cancel() }
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
            item { CallbackFlowSection() }
            item { PrebuiltBridgesSection() }
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

// ── callbackFlow 섹션 ──
@Composable
private fun CallbackFlowSection() {
    val scope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var isRunning by remember { mutableStateOf(false) }
    var currentJob by remember { mutableStateOf<Job?>(null) }

    SectionCard(title = "5. callbackFlow (다중 값 콜백 → Flow)") {
        InfoText("suspendCoroutine은 단일 값만 반환합니다.\n진행률처럼 여러 번 값을 방출하는 콜백은 callbackFlow로 Flow로 변환합니다.")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            """
fun downloadProgress(): Flow<DownloadState> = callbackFlow {
    val task = legacyApi.downloadWithProgress(
        object : ProgressCallback {
            override fun onProgress(step: Int, total: Int) {
                trySend(DownloadState.Progress(step, total))
            }
            override fun onComplete(result: String) {
                trySend(DownloadState.Done(result))
                close()       // Flow 정상 종료
            }
            override fun onError(e: Exception) {
                close(e)      // Flow 에러 종료
            }
        }
    )
    awaitClose { task.cancel() }  // 수집 취소 시 정리
}

// 수집 (UI)
scope.launch {
    downloadProgress().collect { state ->
        when (state) {
            is DownloadState.Progress ->
                updateProgress(state.step, state.total)
            is DownloadState.Done ->
                showResult(state.result)
            is DownloadState.Error ->
                showError(state.message)
        }
    }
}
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 진행 상태 표시
        when (val state = downloadState) {
            is DownloadState.Idle -> {
                InfoText("다운로드 시작 버튼을 눌러 진행률을 확인하세요.")
            }
            is DownloadState.Progress -> {
                val progress = state.step.toFloat() / state.total.toFloat()
                Text(
                    text = "진행 중: ${state.step} / ${state.total} (${(progress * 100).toInt()}%)",
                    color = Color(0xFF00D4FF),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF00D4FF),
                    trackColor = Color(0xFF333333)
                )
            }
            is DownloadState.Done -> {
                Text(
                    text = "✅ ${state.result}",
                    color = Color(0xFF00FF88),
                    fontSize = 13.sp
                )
            }
            is DownloadState.Error -> {
                Text(
                    text = "❌ ${state.message}",
                    color = Color(0xFFFF5722),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(
                text = "다운로드 시작",
                color = Color(0xFF009688),
                enabled = !isRunning,
                modifier = Modifier.weight(1f)
            ) {
                val job = scope.launch {
                    isRunning = true
                    downloadState = DownloadState.Idle
                    try {
                        downloadWithCallbackFlow().collect { state ->
                            downloadState = state
                        }
                    } catch (e: CancellationException) {
                        downloadState = DownloadState.Error("취소됨")
                    } catch (e: Exception) {
                        downloadState = DownloadState.Error(e.message ?: "알 수 없는 오류")
                    } finally {
                        isRunning = false
                    }
                }
                currentJob = job
            }
            ActionButton(
                text = "취소",
                color = Color(0xFFFF9800),
                enabled = isRunning,
                modifier = Modifier.weight(1f)
            ) {
                currentJob?.cancel()
            }
        }
    }
}

// ── 기존 라이브러리 브릿지 섹션 ──
@Composable
private fun PrebuiltBridgesSection() {
    // LiveData.asFlow() 데모용 LiveData
    val liveData = remember { MutableLiveData("초기값") }
    // remember로 Flow 인스턴스 캐싱 — 리컴포지션 시 재생성 방지
    val liveDataFlow = remember(liveData) { liveData.asFlow() }
    val liveDataAsFlow by liveDataFlow.collectAsStateWithLifecycle(initialValue = "...")

    // Flow.asLiveData() 데모용 StateFlow
    val stateFlow = remember { MutableStateFlow(0) }
    val flowAsLiveData = remember { stateFlow.asLiveData() }
    val flowAsLiveDataValue by flowAsLiveData.observeAsState(initial = 0)
    val scope = rememberCoroutineScope()

    SectionCard(title = "6. 기존 라이브러리 브릿지 활용") {
        InfoText("직접 브릿지를 만들기 전에, 라이브러리가 제공하는 기존 브릿지를 먼저 확인하세요.\n대부분의 경우 이미 완성된 브릿지가 존재합니다.")
        Spacer(modifier = Modifier.height(12.dp))

        // ── LiveData.asFlow() ──
        Text(
            text = "① LiveData.asFlow()",
            color = Color(0xFFFFAA00),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        InfoText("lifecycle-livedata-ktx 제공. LiveData를 cold Flow로 변환합니다.\nLifecycle이 STARTED 이상일 때만 값을 방출합니다.")
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            """
// LiveData → Flow 변환
val liveData: MutableLiveData<String> = MutableLiveData("초기값")
val flow: Flow<String> = liveData.asFlow()

// Compose에서 수집
val value by liveData.asFlow()
    .collectAsStateWithLifecycle(initialValue = "")
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow(label = "LiveData → Flow", value = liveDataAsFlow, isLoading = false)
        Spacer(modifier = Modifier.height(6.dp))
        ActionButton(
            text = "LiveData 값 변경",
            color = Color(0xFFFFAA00),
            enabled = true
        ) {
            liveData.value = "업데이트: ${System.currentTimeMillis() % 10000}"
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(16.dp))

        // ── Flow.asLiveData() ──
        Text(
            text = "② Flow.asLiveData()",
            color = Color(0xFF00D4FF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        InfoText("Flow를 LiveData로 변환합니다. ViewModel에서 Flow를 LiveData로\n노출할 때 또는 LiveData 기반 기존 코드와 통합할 때 사용합니다.")
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            """
// Flow → LiveData 변환
val stateFlow = MutableStateFlow(0)
val liveData: LiveData<Int> = stateFlow.asLiveData()

// XML / Compose 모두에서 observe 가능
val value by liveData.observeAsState(initial = 0)
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow(label = "Flow → LiveData", value = flowAsLiveDataValue.toString(), isLoading = false)
        Spacer(modifier = Modifier.height(6.dp))
        ActionButton(
            text = "StateFlow 값 증가",
            color = Color(0xFF00D4FF),
            enabled = true
        ) {
            scope.launch { stateFlow.emit(stateFlow.value + 1) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF333333))
        Spacer(modifier = Modifier.height(16.dp))

        // ── ListenableFuture.await() ──
        Text(
            text = "③ ListenableFuture.await()",
            color = Color(0xFF00FF88),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        InfoText("kotlinx-coroutines-guava 제공. CameraX, WorkManager 등\nGuava ListenableFuture를 suspend 함수로 변환합니다.")
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            """
// kotlinx-coroutines-guava 의존성 필요
// implementation "org.jetbrains.kotlinx:kotlinx-coroutines-guava"

// CameraX 예시 — ImageCapture
suspend fun takePhoto(imageCapture: ImageCapture): Uri =
    suspendCancellableCoroutine { cont ->
        // ❌ 직접 구현 — 불필요
    }

// ✅ 기존 브릿지 활용
suspend fun takePhoto(imageCapture: ImageCapture): Uri {
    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(file).build()
    // ListenableFuture.await() 가 suspendCancellableCoroutine을
    // 내부적으로 구현하여 취소 전파까지 처리
    val result = imageCapture
        .takePicture(outputOptions, executor)
        .await()          // kotlinx-coroutines-guava 확장 함수
    return result.savedUri ?: Uri.EMPTY
}
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(8.dp))
        InfoText("💡 기타 기존 브릿지:\n• Room: suspend fun, Flow 직접 지원\n• Retrofit: suspend fun 직접 지원\n• WorkManager: workInfo.asFlow()\n• DataStore: preferences.data (이미 Flow)")
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
        HorizontalDivider(color = Color(0xFF444444), modifier = Modifier.padding(vertical = 8.dp))
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
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFF444444))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "callbackFlow — 다중 값 콜백",
            color = Color(0xFF00FF88),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        InfoText("위 두 함수는 모두 단일 값을 반환합니다.\n여러 번 값을 방출하는 콜백(진행률, 센서, 위치 스트림 등)은\ncallbackFlow { } + awaitClose { } 패턴으로 Flow로 변환합니다.")
    }
}

// ──────────────────────────────────────────────
// 공통 UI 컴포넌트
// ──────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16213E)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
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
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
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
