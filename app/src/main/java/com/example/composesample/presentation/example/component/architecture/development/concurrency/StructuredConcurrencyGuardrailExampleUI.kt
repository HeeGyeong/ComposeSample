package com.example.composesample.presentation.example.component.architecture.development.concurrency

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * 구조적 동시성 가드레일 예제 (kotlinx.coroutines 1.11.0)
 * - 1.11.0 은 구조적 동시성을 끊는 호출(launch(Job()) · launch(NonCancellable) · runInterruptible(Job()) 등)에
 *   **컴파일 경고**를 붙였다. 동작을 바꾼 것이 아니라, 같은 호출을 가로채는 `@Deprecated` 오버로드를 추가한 것이다.
 * - 이 화면은 경고가 붙은 호출을 일부러 실행해 **런타임에 무엇이 끊기는지**(부모 취소가 안 닿는다 · 부모가 안 기다린다 ·
 *   인터럽트가 안 된다)를 실측하고, 경고가 **정적 타입이 Job 일 때만** 걸린다는 빈틈도 같은 방식으로 보여준다.
 * - 같은 릴리스의 신규 API 3종(StateFlow.onSubscription · SharedFlow.asFlow · CompletableDeferred.asDeferred)은
 *   모두 "무엇을 밖에 노출하느냐"의 문제라 뒤쪽 카드에서 캐스팅 시도로 대조한다.
 * - 경고가 붙은 호출은 `@Suppress("DEPRECATION")` 로 **범위를 좁혀** 의도적으로만 부른다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "Structured Concurrency Guardrails" 섹션 참고.
 */

// ==================== 실측 파라미터 ====================

/** 2번 카드: 각 자식이 하는 일의 길이. 100ms 마다 틱을 남긴다. */
private const val WORK_MS = 1_200L
private const val TICK_MS = 100L

/** 2번 카드: 부모 스코프를 취소하는 시점. */
private const val CANCEL_AT_MS = 400L

/** 2번 카드: 권장 경로(finally + withContext(NonCancellable))가 취소 뒤에 하는 정리 작업의 길이. */
private const val CLEANUP_MS = 300L

/** 2번 카드: 타임라인 가로축 전체 길이. 디버그 빌드(HotSwan 인터프리터)에서는 틱마다 지연이 붙어 1.2초 작업이 1.3초 이상 걸린다. */
private const val TIMELINE_TOTAL_MS = 1_600L

/** 3번 카드: 병렬로 부르는 작업 3개의 지연(200 / 400 / 600ms). */
private const val LOAD_STEP_MS = 200L

/** 4번 카드: 블로킹 작업 길이와 취소 시점. */
private const val BLOCKING_MS = 1_500L
private const val INTERRUPT_CANCEL_AT_MS = 300L

// ==================== 공용 측정 도구 ====================

/*
 * 시간을 재는 측정 함수(2·3·4번 카드)는 withContext(Dispatchers.Default) 안에서 돈다. 호출 지점(rememberCoroutineScope)은 메인 스레드라
 * 프레임 작업에 따라 delay 재개가 밀릴 수 있고, UI 테스트에서는 컴포지션 코루틴의 delay 가 가상 시간으로 흐르기 때문이다.
 */

/** 측정 시작 시점부터 흐른 시간(ms). 여러 스레드에서 읽으므로 불변 값만 담는다. */
private class Stopwatch {
    private val startNanos = System.nanoTime()
    fun elapsed(): Long = (System.nanoTime() - startNanos) / 1_000_000
}

// ==================== 2. 부모 취소 실측 ====================

private enum class Lane(
    val label: String,
    val callSite: String,
    val compileResult: String,
    val color: Color
) {
    NORMAL("정상 자식", "launch { }", "통과", Color(0xFF43A047)),
    JOB("Job 전달", "launch(Job()) { }", "WARNING", Color(0xFFE53935)),
    NON_CANCELLABLE("NonCancellable 전달", "launch(NonCancellable) { }", "WARNING", Color(0xFFFB8C00)),
    JOB_PLUS_DISPATCHER("Job + 디스패처", "launch(Job() + Dispatchers.Default) { }", "경고 없음", Color(0xFF8E24AA)),
    CLEANUP("권장: 정리만 보호", "launch { try {…} finally { withContext(NonCancellable) {…} } }", "통과", Color(0xFF1E88E5))
}

private data class LaneResult(
    val lane: Lane,
    val isChildOfParent: Boolean,
    val endMs: Long,
    val endReason: String
)

private data class CancellationRun(
    val results: List<LaneResult>,
    val parentJoinedMs: Long
)

/** 자식 하나가 하는 일. 틱을 남기다가 취소되면 그 시점을 기록하고 다시 던진다. */
private suspend fun tickingWork(lane: Lane, clock: Stopwatch, ends: MutableMap<Lane, Pair<Long, String>>) {
    try {
        repeat((WORK_MS / TICK_MS).toInt()) { delay(TICK_MS) }
        ends[lane] = clock.elapsed() to "끝까지 실행"
    } catch (e: CancellationException) {
        ends[lane] = clock.elapsed() to "취소됨"
        throw e
    }
}

/**
 * 화면 스코프(viewModelScope 와 같은 역할)에서 부모 코루틴 하나가 자식 5개를 띄운 뒤, [CANCEL_AT_MS] 에 스코프를 취소한다.
 *
 * 가드레일 오버로드를 **일부러** 호출해 런타임 결과를 재는 함수라 DEPRECATION 경고를 이 함수로만 좁혀 끈다.
 * 가드레일 오버로드는 바이트코드상 `launch(context as CoroutineContext, …)` 로 그대로 위임하므로, 동작은 1.10 과 같다.
 */
@Suppress("DEPRECATION")
private suspend fun measureParentCancellation(): CancellationRun = withContext(Dispatchers.Default) {
    val clock = Stopwatch()
    val ends = ConcurrentHashMap<Lane, Pair<Long, String>>()
    val childJobs = ConcurrentHashMap<Lane, Job>()
    val childOfParent = ConcurrentHashMap<Lane, Boolean>()

    val screenScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val parent = screenScope.launch {
        childJobs[Lane.NORMAL] = launch { tickingWork(Lane.NORMAL, clock, ends) }
        childJobs[Lane.JOB] = launch(Job()) { tickingWork(Lane.JOB, clock, ends) }
        childJobs[Lane.NON_CANCELLABLE] = launch(NonCancellable) { tickingWork(Lane.NON_CANCELLABLE, clock, ends) }
        // 정적 타입이 CoroutineContext 라 가드레일 오버로드가 선택되지 않는다 — 런타임 결과는 Job() 과 같다.
        childJobs[Lane.JOB_PLUS_DISPATCHER] = launch(Job() + Dispatchers.Default) {
            tickingWork(Lane.JOB_PLUS_DISPATCHER, clock, ends)
        }
        childJobs[Lane.CLEANUP] = launch {
            try {
                tickingWork(Lane.CLEANUP, clock, ends)
            } finally {
                // 취소된 코루틴에서도 정리 작업만은 끝까지 — 부모와의 연결은 그대로 유지된다.
                withContext(NonCancellable) {
                    delay(CLEANUP_MS)
                    ends[Lane.CLEANUP] = clock.elapsed() to "취소 → 정리 완료"
                }
            }
        }

        // "끊겼는가"를 부모의 children 목록으로 직접 확인한다.
        val children = coroutineContext.job.children.toSet()
        childJobs.forEach { (lane, job) -> childOfParent[lane] = job in children }
    }

    delay(CANCEL_AT_MS)
    screenScope.cancel()
    parent.join()
    val parentJoinedMs = clock.elapsed()

    // 부모가 기다려 주지 않은 고아들도 끝날 때까지 기다린 뒤 결과를 모은다(최대 WORK_MS).
    childJobs.values.joinAll()

    val results = Lane.entries.map { lane ->
        val (endMs, reason) = ends[lane] ?: (clock.elapsed() to "기록 없음")
        LaneResult(
            lane = lane,
            isChildOfParent = childOfParent[lane] ?: false,
            endMs = endMs,
            endReason = reason
        )
    }
    CancellationRun(results = results, parentJoinedMs = parentJoinedMs)
}

// ==================== 3. 부모는 누구를 기다리나 ====================

private data class LoadAllResult(
    val callSite: String,
    val returnedMs: Long,
    val sizeAtReturn: Int,
    val sizeLater: Int
)

/**
 * 흔한 "병렬로 3개 불러와 합치기" 코드. coroutineScope 는 **자기 자식**이 끝날 때까지만 기다린다.
 * Job 을 넘긴 launch 는 자식이 아니므로, 함수가 빈 목록을 들고 먼저 돌아온다.
 */
@Suppress("DEPRECATION")
private suspend fun loadAll(passJob: Boolean): LoadAllResult = withContext(Dispatchers.Default) {
    val clock = Stopwatch()
    val items = Collections.synchronizedList(mutableListOf<String>())
    val launched = mutableListOf<Job>()

    coroutineScope {
        repeat(3) { index ->
            val work: suspend CoroutineScope.() -> Unit = {
                delay(LOAD_STEP_MS * (index + 1))
                items += "item$index"
            }
            launched += if (passJob) launch(Job(), block = work) else launch(block = work)
        }
    }
    val returnedMs = clock.elapsed()
    val sizeAtReturn = items.size

    // 호출자는 이미 다음 단계로 넘어갔다 — 그 뒤에야 목록이 채워진다.
    launched.joinAll()
    LoadAllResult(
        callSite = if (passJob) "launch(Job()) { }" else "launch { }",
        returnedMs = returnedMs,
        sizeAtReturn = sizeAtReturn,
        sizeLater = items.size
    )
}

// ==================== 4. runInterruptible 실측 ====================

private data class InterruptResult(
    val callSite: String,
    val releasedMs: Long,
    val interrupted: Boolean,
    val blockEndMs: Long
)

/**
 * 블로킹 호출(Thread.sleep)을 runInterruptible 로 감싸고 [INTERRUPT_CANCEL_AT_MS] 에 취소한다.
 * 올바른 경로는 취소가 스레드 인터럽트로 번역되어 즉시 풀리고, Job 을 넘긴 경로는 블로킹이 끝날 때까지 붙잡힌다.
 */
@Suppress("DEPRECATION")
private suspend fun measureInterruptible(passJob: Boolean): InterruptResult = withContext(Dispatchers.Default) {
    val clock = Stopwatch()
    val interrupted = AtomicBoolean(false)
    val blockEnd = AtomicLong(-1L)
    val block: () -> Unit = {
        try {
            Thread.sleep(BLOCKING_MS)
        } catch (e: InterruptedException) {
            interrupted.set(true)
            // InterruptedException 은 checked 예외다. 디버그 빌드(HotSwan 2.0 인터프리터)는 이 람다를 Proxy 로 실행하므로
            // 그대로 던지면 UndeclaredThrowableException 으로 감싸져 runInterruptible 이 취소로 번역하지 못하고 앱이 죽는다.
            // runInterruptible 이 할 번역을 블록 안에서 먼저 한다 — unchecked 라 Proxy 를 그대로 통과한다.
            throw CancellationException("Blocking call was interrupted").apply { initCause(e) }
        }
        blockEnd.set(clock.elapsed())
    }

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val job = scope.launch {
        if (passJob) runInterruptible(Job(), block) else runInterruptible(Dispatchers.IO, block)
    }
    delay(INTERRUPT_CANCEL_AT_MS)
    job.cancel()
    job.join()
    val releasedMs = clock.elapsed()
    scope.cancel()

    InterruptResult(
        callSite = if (passJob) "runInterruptible(Job()) { }" else "runInterruptible(Dispatchers.IO) { }",
        releasedMs = releasedMs,
        interrupted = interrupted.get(),
        blockEndMs = blockEnd.get()
    )
}

// ==================== 7. SharedFlow 노출 3단계 ====================

private data class ExposureResult(
    val callSite: String,
    val runtimeClass: String,
    val isSharedFlow: Boolean,
    val replayCache: String,
    val forgedAccepted: Boolean,
    val consumerReceived: String
)

/**
 * 소비자 쪽의 "나쁜 캐스팅"을 재현한다. 노출된 타입을 MutableSharedFlow 로 되돌려 이벤트를 위조해 본다.
 */
private fun tryForge(exposed: Flow<String>): Boolean =
    (exposed as? MutableSharedFlow<String>)?.tryEmit("위조 이벤트") ?: false

/** asFlow() 는 1.11.0 에서 @ExperimentalCoroutinesApi 다. */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun measureExposure(): List<ExposureResult> {
    val ways: List<Pair<String, (MutableSharedFlow<String>) -> Flow<String>>> = listOf(
        "val events: SharedFlow = _events" to { source -> source },
        "_events.asSharedFlow()" to { source -> source.asSharedFlow() },
        "_events.asFlow()  // 1.11 신규 · Experimental" to { source -> source.asFlow() }
    )
    return ways.map { (callSite, expose) ->
        val source = MutableSharedFlow<String>(replay = 1)
        source.tryEmit("정상 이벤트")
        val exposed = expose(source)

        val forged = tryForge(exposed)
        val shared = exposed as? SharedFlow<String>
        ExposureResult(
            callSite = callSite,
            runtimeClass = exposed::class.java.simpleName,
            isSharedFlow = shared != null,
            replayCache = shared?.replayCache?.toString() ?: "타입에 없음(접근 불가)",
            forgedAccepted = forged,
            // replay 로 받는 첫 값 — 위조가 성공했다면 소비자는 위조 이벤트를 받는다.
            consumerReceived = exposed.first()
        )
    }
}

// ==================== 8. CompletableDeferred 노출 ====================

private data class DeferredScenario(
    val title: String,
    val consumerAction: String,
    val consumerActionResult: String,
    val producerCompleteAccepted: Boolean,
    val otherConsumerSees: String
)

private fun tryHijack(exposed: Deferred<String>): Boolean =
    (exposed as? CompletableDeferred<String>)?.complete("가짜 결과") ?: false

/** await 결과를 문자열로. 이 코루틴 자신이 취소된 경우에는 삼키지 않고 다시 던진다. */
private suspend fun awaitAsText(deferred: Deferred<String>): String = try {
    deferred.await()
} catch (e: CancellationException) {
    currentCoroutineContext().ensureActive()
    "${e::class.java.simpleName}: ${e.message}"
}

/** asDeferred() 는 1.11.0 에서 @ExperimentalCoroutinesApi 다. */
@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun measureDeferredExposure(): List<DeferredScenario> {
    // A. 타입만 올려서 노출 — 캐스팅 한 번이면 생산자 대신 결과를 정해 버릴 수 있다.
    val producerA = CompletableDeferred<String>()
    val exposedA: Deferred<String> = producerA
    val hijackedA = tryHijack(exposedA)
    val completeA = producerA.complete("진짜 결과")

    // B. asDeferred() 로 노출 — 캐스팅이 실패한다.
    val producerB = CompletableDeferred<String>()
    val exposedB = producerB.asDeferred()
    val hijackedB = tryHijack(exposedB)
    val completeB = producerB.complete("진짜 결과")

    // C. asDeferred() 로 노출했지만 소비자가 cancel() — Job 인터페이스라 막을 수 없고, 원본까지 취소된다.
    val producerC = CompletableDeferred<String>()
    val exposedC = producerC.asDeferred()
    exposedC.cancel()
    val completeC = producerC.complete("진짜 결과")

    return listOf(
        DeferredScenario(
            title = "A. val result: Deferred = _result",
            consumerAction = "(result as CompletableDeferred).complete(\"가짜 결과\")",
            consumerActionResult = if (hijackedA) "성공 — 소비자가 결과를 정했다" else "실패",
            producerCompleteAccepted = completeA,
            otherConsumerSees = awaitAsText(exposedA)
        ),
        DeferredScenario(
            title = "B. _result.asDeferred()  // 1.11 신규 · Experimental",
            consumerAction = "(result as CompletableDeferred).complete(\"가짜 결과\")",
            consumerActionResult = if (hijackedB) "성공" else "캐스팅 실패(${exposedB::class.java.simpleName})",
            producerCompleteAccepted = completeB,
            otherConsumerSees = awaitAsText(exposedB)
        ),
        DeferredScenario(
            title = "C. asDeferred() 뷰에서 cancel()",
            consumerAction = "result.cancel()",
            consumerActionResult = "원본 isCancelled = ${producerC.isCancelled}",
            producerCompleteAccepted = completeC,
            otherConsumerSees = awaitAsText(producerC)
        )
    )
}

// ==================== 화면 ====================

@Composable
fun StructuredConcurrencyGuardrailExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "구조적 동시성 가드레일",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GuardrailOverviewCard() }
            item { ParentCancellationCard() }
            item { ScopeWaitCard() }
            item { InterruptibleCard() }
            item { GapAndAlternativeCard() }
            item { OnSubscriptionCard() }
            item { SharedFlowExposureCard() }
            item { DeferredExposureCard() }
            item { PitfallCard() }
        }
    }
}

// ==================== 1. 무엇이 걸리나 ====================

@Composable
private fun GuardrailOverviewCard() {
    SectionCard(title = "1. 컴파일러가 잡아 주는 호출") {
        BodyText(
            "1.11.0 은 구조적 동시성을 끊는 호출에 경고를 붙였다. 라이브러리 동작을 바꾼 것이 아니라 " +
                "같은 이름의 오버로드를 하나 더 추가해서, 잘못된 호출만 그쪽으로 끌어당긴 것이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("수준", "호출 (도입 버전)", isHeader = true)
        TableRow("ERROR", "suspend 함수 안에서 스코프 없이 launch { } · async { }  (1.10.2)")
        TableRow("WARNING", "launch(Job()) · async(Job())  (1.11.0)")
        TableRow("WARNING", "launch(NonCancellable) · async(NonCancellable)  (1.11.0)")
        TableRow("WARNING", "runInterruptible(Job())  (1.11.0)")
        TableRow("WARNING", "produce(Job()) · future(Job())  (1.11.0)")
        CaptionText("coroutines 1.11.0 / 1.10.2 / 1.10.1 jar 를 javap 로 대조해 확인. 1.10.1 에는 이 오버로드가 하나도 없다.")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText("구조는 두 가지다(바이트코드로 확인한 모양).")
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "// ① 더 구체적인 파라미터 타입으로 가로채기 — 동작은 그대로 위임\n" +
                "@Deprecated(\"Passing a Job to coroutine builders breaks structured concurrency…\",\n" +
                "            level = WARNING)\n" +
                "fun CoroutineScope.launch(context: Job, start: CoroutineStart = DEFAULT,\n" +
                "                          block: suspend CoroutineScope.() -> Unit): Job =\n" +
                "    launch(context as CoroutineContext, start, block)\n\n" +
                "// ② 원래는 '없는' 함수를 만들어 친절한 에러로 바꾸기\n" +
                "@Deprecated(\"'launch' can not be called without the corresponding coroutine scope…\",\n" +
                "            level = ERROR)\n" +
                "@LowPriorityInOverloadResolution\n" +
                "fun launch(context: CoroutineContext = EmptyCoroutineContext, …): Job =\n" +
                "    throw UnsupportedOperationException(\"Should never be called…\")"
        )
        Spacer(modifier = Modifier.height(8.dp))
        PitfallRow(
            "① 은 오버로드 해석 규칙을 이용한다",
            "인자의 정적 타입이 Job 이면 CoroutineContext 보다 구체적인 Job 오버로드가 선택된다. " +
                "경고만 붙을 뿐 호출은 원래 launch 로 넘어가므로 1.10 과 동작이 같다 — 그래서 아래 카드들에서 실제로 실행해 볼 수 있다."
        )
        PitfallRow(
            "② 는 우선순위를 낮춰 '빈자리'에만 들어간다",
            "CoroutineScope 리시버가 있으면 진짜 CoroutineScope.launch 가 이기고, 리시버가 없을 때만 이 함수가 선택된다. " +
                "덕분에 \"Unresolved reference: launch\" 대신 coroutineScope { } 로 감싸라는 안내가 뜬다. " +
                "(@LowPriorityInOverloadResolution 은 kotlin.internal 이라 앱 코드에서는 쓸 수 없다.)"
        )
        CodeBlock(
            "e: 'fun launch(context: CoroutineContext = ..., …): Job' is deprecated.\n" +
                "   'launch' can not be called without the corresponding coroutine scope.\n" +
                "   Consider wrapping 'launch' in 'coroutineScope { }', using 'runBlocking { }',\n" +
                "   or using some other 'CoroutineScope'."
        )
        CaptionText("실제 컴파일 출력(Kotlin 2.4.20). WARNING 쪽 메시지는 모두 \"will be deprecated with an error in the future\" 로 끝난다.")
    }
}

// ==================== 2. 부모 취소 실측 ====================

@Composable
private fun ParentCancellationCard() {
    val scope = rememberCoroutineScope()
    var lastRun by remember { mutableStateOf<CancellationRun?>(null) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "2. 부모를 취소하면 누가 멈추나") {
        BodyText(
            "화면 스코프 안의 부모 코루틴이 자식 5개를 띄운다. 모두 ${WORK_MS}ms 짜리 일을 하고, " +
                "${CANCEL_AT_MS}ms 에 화면 스코프를 취소한다(화면을 나간 것과 같다)."
        )
        Spacer(modifier = Modifier.height(8.dp))
        Lane.entries.forEach { lane ->
            ResultRow(lane.label, "${lane.callSite}  → ${lane.compileResult}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = if (running) "실행 중… (최대 ${WORK_MS / 1000.0}초)" else "실행", color = Color(0xFF3949AB)) {
            if (!running) {
                running = true
                scope.launch {
                    lastRun = measureParentCancellation()
                    running = false
                }
            }
        }

        lastRun?.let { measured ->
            Spacer(modifier = Modifier.height(12.dp))
            measured.results.forEach { result ->
                Text(
                    text = result.lane.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = result.lane.color
                )
                TimelineBar(
                    endMs = result.endMs,
                    cancelMs = CANCEL_AT_MS,
                    totalMs = TIMELINE_TOTAL_MS,
                    color = result.lane.color
                )
                ResultRow("부모의 자식", if (result.isChildOfParent) "예" else "아니오 — 연결이 끊겼다")
                ResultRow("종료", "${result.endMs}ms · ${result.endReason}")
                Spacer(modifier = Modifier.height(8.dp))
            }
            CaptionText("세로 빨간 선 = 취소 시점(${CANCEL_AT_MS}ms). 막대 끝 = 그 자식이 실제로 끝난 시점.")
            Spacer(modifier = Modifier.height(8.dp))
            ResultRow("부모 join", "${measured.parentJoinedMs}ms 에 반환")
            BodyText(
                "부모는 '자기 자식'(정상 · 권장)만 기다렸다. Job 이나 NonCancellable 을 넘긴 자식은 부모의 children 에 " +
                    "들어가지 않으므로, 취소도 닿지 않고 join 도 기다려 주지 않는다 — 화면을 나간 뒤에도 끝까지 돈다."
            )
        }
    }
}

// ==================== 3. 부모는 누구를 기다리나 ====================

@Composable
private fun ScopeWaitCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<LoadAllResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "3. coroutineScope 는 누구를 기다리나") {
        BodyText(
            "\"3개를 병렬로 불러와 합친다\"는 흔한 코드. coroutineScope 는 자기 자식이 모두 끝나야 반환된다. " +
                "각 작업은 ${LOAD_STEP_MS}·${LOAD_STEP_MS * 2}·${LOAD_STEP_MS * 3}ms 걸린다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "suspend fun loadAll(): List<String> {\n" +
                "    val items = synchronizedList(mutableListOf<String>())\n" +
                "    coroutineScope {\n" +
                "        repeat(3) { i -> launch(Job()) { items += load(i) } }  // ⚠️\n" +
                "    }\n" +
                "    return items\n" +
                "}"
        )
        DemoButton(text = if (running) "실행 중…" else "두 방식 실행", color = Color(0xFF00897B)) {
            if (!running) {
                running = true
                scope.launch {
                    results = listOf(loadAll(passJob = false), loadAll(passJob = true))
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            TableRow("호출", "반환 시점 · 반환 때 개수 · 나중 개수", isHeader = true)
            results.forEach { result ->
                TableRow(result.callSite, "${result.returnedMs}ms · ${result.sizeAtReturn}개 · ${result.sizeLater}개")
            }
            Spacer(modifier = Modifier.height(8.dp))
            BodyText(
                "Job 을 넘기면 coroutineScope 가 기다릴 자식이 0개라 곧바로 반환된다. 호출자는 빈 목록을 받고, " +
                    "목록은 그 뒤에야 채워진다. 크래시도 경고 로그도 없이 \"가끔 데이터가 비어 있다\"는 버그가 된다."
            )
        }
    }
}

// ==================== 4. runInterruptible(Job) ====================

@Composable
private fun InterruptibleCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<InterruptResult>>(emptyList()) }
    var running by remember { mutableStateOf(false) }

    SectionCard(title = "4. runInterruptible 에 Job 을 넘기면") {
        BodyText(
            "runInterruptible 은 코루틴 취소를 스레드 인터럽트로 번역해 블로킹 호출을 깨운다. " +
                "${BLOCKING_MS}ms 짜리 Thread.sleep 을 감싸고 ${INTERRUPT_CANCEL_AT_MS}ms 에 취소한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = if (running) "실행 중… (최대 ${BLOCKING_MS / 1000.0}초)" else "두 방식 실행", color = Color(0xFF6D4C41)) {
            if (!running) {
                running = true
                scope.launch {
                    results = listOf(measureInterruptible(passJob = false), measureInterruptible(passJob = true))
                    running = false
                }
            }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { result ->
                Text(text = result.callSite, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("인터럽트", if (result.interrupted) "됨" else "안 됨")
                ResultRow("호출자 풀림", "${result.releasedMs}ms (취소 요청은 ${INTERRUPT_CANCEL_AT_MS}ms)")
                ResultRow("블로킹 종료", if (result.blockEndMs < 0) "중단됨" else "${result.blockEndMs}ms 에 끝까지 실행")
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "Job 을 넘기면 블록이 그 Job 의 자식이 되어 호출자의 취소가 전달되지 않는다. 인터럽트가 일어나지 않으니 " +
                    "스레드는 끝까지 붙잡혀 있고, 취소한 쪽도 그동안 풀려나지 못한다 — 가드레일 메시지가 말하는 " +
                    "\"prevents it from being cancelled when the caller gets cancelled\" 가 이것이다."
            )
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "이 화면의 블록은 InterruptedException 을 잡아 CancellationException 으로 바꿔 던진다. 디버그 빌드(HotSwan 2.0 인터프리터)는 " +
                    "일반 람다를 Proxy 로 실행해서, checked 예외가 람다 밖으로 나가면 UndeclaredThrowableException 으로 감싼다 — " +
                    "그러면 runInterruptible 이 취소로 번역하지 못해 앱이 죽는다(실기기에서 재현). inline·suspend 람다는 영향이 없다."
            )
        }
    }
}

// ==================== 5. 빈틈과 대안 ====================

@Composable
private fun GapAndAlternativeCard() {
    SectionCard(title = "5. 가드레일이 못 잡는 것, 그리고 대안") {
        BodyText(
            "가드레일은 오버로드 해석으로 동작하므로 인자의 정적 타입만 본다. 런타임에 Job 이 들어 있어도 " +
                "정적 타입이 CoroutineContext 면 그대로 통과한다(2번 카드의 보라색 레인이 그 결과다)."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("호출", "컴파일 결과 (실측)", isHeader = true)
        TableRow("launch(Job())", "WARNING")
        TableRow("launch(SupervisorJob())", "WARNING — CompletableJob 도 Job")
        TableRow("launch(Job() + Dispatchers.IO)", "통과 — + 결과가 CoroutineContext")
        TableRow("val ctx: CoroutineContext = Job()\nlaunch(ctx)", "통과 — 변수 타입이 CoroutineContext")
        TableRow("withContext(Job())", "통과 — withContext 에는 가드레일이 없다")
        TableRow("withContext(NonCancellable)", "통과 — 정상 관용구")
        CaptionText(
            "launch · runInterruptible · withContext(NonCancellable) 줄은 테스트 소스에 같은 호출을 넣고 컴파일해 확인했고, " +
                "withContext(Job()) 줄은 jar 에 Job 을 받는 withContext 오버로드가 없는 것으로 확인했다. 경고가 없어도 런타임 결과는 Job() 과 같다."
        )
        Spacer(modifier = Modifier.height(12.dp))
        TableRow("하고 싶었던 것", "구조를 지키는 대안", isHeader = true)
        TableRow("취소돼도 정리는 끝내기", "finally { withContext(NonCancellable) { … } }")
        TableRow("자식 실패가 형제를 안 죽이게", "supervisorScope { … }")
        TableRow("화면보다 오래 사는 작업", "앱 수명 스코프(DI 로 주입) · WorkManager")
        TableRow("정말로 떼어 놓는 fire-and-forget", "GlobalScope.launch — 고아임을 이름으로 드러낸다")
        CaptionText("대안 목록은 가드레일을 추가한 kotlinx.coroutines PR(#4435)의 안내를 따랐다.")
    }
}

// ==================== 6. StateFlow.onSubscription ====================

@Composable
private fun OnSubscriptionCard() {
    val counter = remember { MutableStateFlow(0) }
    val log = remember { mutableStateListOf<String>() }

    // 1.11 신규: StateFlow 에 붙이면 StateFlow 가 돌아온다.
    val subscribed: StateFlow<Int> = remember(counter) {
        counter.onSubscription {
            log.add(0, "onSubscription · 그 순간 subscriptionCount=${counter.subscriptionCount.value}, value=${counter.value}")
        }
    }
    // 비교: 정적 타입을 SharedFlow 로 두면 기존 오버로드가 선택된다(1.10 까지 StateFlow 에서도 이쪽이었다).
    val sharedOverload: SharedFlow<Int> = remember(counter) {
        val asShared: SharedFlow<Int> = counter
        asShared.onSubscription { }
    }

    val subscriptionCount by counter.subscriptionCount.collectAsState()
    var showA by remember { mutableStateOf(false) }
    var showB by remember { mutableStateOf(false) }

    SectionCard(title = "6. StateFlow.onSubscription 이 StateFlow 를 돌려준다") {
        BodyText(
            "onSubscription 은 구독이 등록된 직후 블록을 실행한다. 1.10 까지는 SharedFlow 용 하나뿐이라 " +
                "StateFlow 에 붙이면 SharedFlow 가 돌아왔고, .value 가 사라져 collectAsState() 에 초기값을 따로 넘겨야 했다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow("StateFlow 쪽", "${subscribed::class.java.simpleName} · StateFlow 인가 = ${StateFlow::class.java.isInstance(subscribed)}")
        ResultRow("SharedFlow 쪽", "${sharedOverload::class.java.simpleName} · StateFlow 인가 = ${StateFlow::class.java.isInstance(sharedOverload)}")
        ResultRow(".value", "${subscribed.value}  ← 구독 없이도 읽힌다")
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "val count: StateFlow<Int> = _count.onSubscription { onFirstSeen() }\n" +
                "val value by count.collectAsState()          // 1.11: 초기값 불필요\n" +
                "// 1.10: SharedFlow 가 돌아와 collectAsState(initial = _count.value) 필요"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = if (showA) "구독자 A 끄기" else "구독자 A 켜기", color = Color(0xFF00897B)) { showA = !showA }
            DemoButton(text = if (showB) "구독자 B 끄기" else "구독자 B 켜기", color = Color(0xFF00897B)) { showB = !showB }
            DemoButton(text = "+1", color = Color(0xFF3949AB)) { counter.value += 1 }
        }
        ResultRow("구독자 수", "$subscriptionCount")
        if (showA) SubscriberRow(name = "A", flow = subscribed)
        if (showB) SubscriberRow(name = "B", flow = subscribed)

        if (log.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            CodeBlock(log.take(6).joinToString("\n"))
            CaptionText(
                "구독자를 켤 때마다 블록이 한 번씩 돈다. 블록 안에서 읽은 subscriptionCount 에 이미 자기 자신이 포함돼 있다면 " +
                    "'등록 후 실행'이 보장된다는 뜻이다 — 이 블록에서 새로고침을 요청해도 결과를 놓치지 않는다."
            )
        }
    }
}

/** StateFlow 라서 초기값 없이 collectAsState() 를 부를 수 있다. 컴포지션에 들어올 때 구독, 나갈 때 해제. */
@Composable
private fun SubscriberRow(name: String, flow: StateFlow<Int>) {
    val value by flow.collectAsState()
    ResultRow("구독자 $name", "value = $value")
}

// ==================== 7. SharedFlow.asFlow ====================

@Composable
private fun SharedFlowExposureCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<ExposureResult>>(emptyList()) }

    SectionCard(title = "7. SharedFlow 를 밖에 어떻게 내보내나") {
        BodyText(
            "ViewModel 이 내부 MutableSharedFlow 를 노출하는 세 가지 방법. 소비자가 노출된 값을 MutableSharedFlow 로 " +
                "캐스팅해 '위조 이벤트'를 넣어 보고, 핫 플로우 전용 정보(replayCache)가 보이는지도 확인한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "캐스팅 시도", color = Color(0xFF8E24AA)) {
            scope.launch { results = measureExposure() }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { result ->
                Text(text = result.callSite, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("런타임 클래스", result.runtimeClass)
                ResultRow("SharedFlow?", if (result.isSharedFlow) "예" else "아니오")
                ResultRow("replayCache", result.replayCache)
                ResultRow("위조", if (result.forgedAccepted) "성공 — 외부에서 emit 됐다" else "캐스팅 실패")
                ResultRow("소비자 수신", result.consumerReceived)
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "타입만 올린 노출은 캐스팅 한 번으로 뚫린다. asSharedFlow() 는 쓰기를 막지만 여전히 SharedFlow 라서 " +
                    "replayCache·subscriptionCount 같은 구현 세부가 계약에 섞인다. asFlow() 는 평범한 Flow 로 감싸 " +
                    "그것까지 숨긴다 — 나중에 내부를 Channel 이나 콜드 플로우로 바꿔도 소비자가 깨지지 않는다. " +
                    "(asFlow() 는 1.11.0 기준 @ExperimentalCoroutinesApi 다. StateFlow.onSubscription 은 opt-in 없이 쓴다.)"
            )
        }
    }
}

// ==================== 8. CompletableDeferred.asDeferred ====================

@Composable
private fun DeferredExposureCard() {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<DeferredScenario>>(emptyList()) }

    SectionCard(title = "8. CompletableDeferred 를 읽기 전용으로") {
        BodyText(
            "결과 하나를 기다리게 하려고 CompletableDeferred 를 Deferred 타입으로 반환하는 코드는 흔하다. " +
                "Deferred 는 상속에 opt-in 이 필요한 인터페이스(InternalForInheritanceCoroutinesApi)라 " +
                "읽기 전용 래퍼를 직접 만들 수도 없었다. 1.11 의 asDeferred() 가 그 래퍼다" +
                "(아직 @ExperimentalCoroutinesApi 라 opt-in 이 필요하다)."
        )
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "세 시나리오 실행", color = Color(0xFF6D4C41)) {
            scope.launch { results = measureDeferredExposure() }
        }

        if (results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            results.forEach { scenario ->
                Text(text = scenario.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
                ResultRow("소비자", scenario.consumerAction)
                ResultRow("결과", scenario.consumerActionResult)
                ResultRow("생산자 complete", if (scenario.producerCompleteAccepted) "받아들여짐" else "거절됨(이미 끝남)")
                ResultRow("다른 소비자", scenario.otherConsumerSees)
                Spacer(modifier = Modifier.height(8.dp))
            }
            BodyText(
                "asDeferred() 는 complete 를 막아 주지만 cancel 은 막지 못한다. Deferred 는 Job 이라 cancel() 이 " +
                    "인터페이스에 있고, 래퍼(ReadonlyDeferred)는 그 호출을 원본에 그대로 위임한다(바이트코드 확인). " +
                    "소비자 하나가 cancel() 하면 같은 결과를 기다리던 모든 소비자가 CancellationException 을 받는다."
            )
        }
    }
}

// ==================== 9. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "9. 걸리는 것들") {
        PitfallRow(
            "경고가 없다고 안전한 게 아니다",
            "가드레일은 정적 타입이 Job/NonCancellable 인 호출만 잡는다. Job() + Dispatchers.IO 나 " +
                "CoroutineContext 변수에 담아 넘긴 Job 은 경고 없이 같은 결과를 낸다(2·5번 카드)."
        )
        PitfallRow(
            "고아 코루틴의 예외는 부모에게 오지 않는다",
            "Job 을 넘긴 자식이 던진 예외는 부모의 try-catch 로 들어오지 않고 전역 예외 처리로 간다. " +
                "안드로이드에서는 CoroutineExceptionHandler 가 없으면 앱이 죽는다 — 그래서 이 화면은 고아에서 예외를 던지지 않는다."
        )
        PitfallRow(
            "NonCancellable 은 withContext 에만",
            "NonCancellable 은 '취소되지 않는 Job' 이라 launch/async 에 넘기면 부모와의 연결이 끊긴다. " +
                "취소된 뒤에도 끝내야 하는 정리 작업은 finally 안의 withContext(NonCancellable) 로 — 부모는 그 정리를 기다려 준다."
        )
        PitfallRow(
            "지금은 WARNING, 곧 ERROR",
            "WARNING 메시지는 전부 \"will be deprecated with an error in the future\" 로 끝난다. " +
                "경고를 @Suppress 로 덮어 두면 승격되는 날 빌드가 깨진다 — 이 예제처럼 의도적 호출은 함수 단위로 좁혀서만 끈다."
        )
        PitfallRow(
            "asDeferred() 는 취소까지 막지 않는다",
            "읽기 전용은 '완료' 기준이다. 결과를 여럿이 공유한다면 소비자에게 Deferred 대신 suspend 함수(await 을 감싼)만 " +
                "노출하는 편이 cancel 경로까지 닫는다(8번 카드 C)."
        )
        PitfallRow(
            "onSubscription 블록은 구독자마다 돈다",
            "StateFlow 한 개에 onSubscription 을 붙여도 블록은 구독이 생길 때마다 실행된다. " +
                "'첫 구독자일 때 한 번만'이 목적이면 stateIn(WhileSubscribed) 쪽이 맞다(6번 카드에서 A·B 를 번갈아 켜 보면 보인다)."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/** 0 ~ totalMs 가로축 위에 자식이 살아 있던 구간을 막대로, 취소 시점을 빨간 세로선으로 그린다. */
@Composable
private fun TimelineBar(
    endMs: Long,
    cancelMs: Long,
    totalMs: Long,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(12.dp)
    ) {
        drawRect(color = Color(0xFFECEFF1), size = size)
        val endX = size.width * (endMs.coerceIn(0L, totalMs).toFloat() / totalMs)
        drawRect(color = color, size = Size(endX, size.height))
        val cancelX = size.width * (cancelMs.toFloat() / totalMs)
        drawLine(
            color = Color(0xFFD32F2F),
            start = Offset(cancelX, 0f),
            end = Offset(cancelX, size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

/** 코드는 줄이 길어 접히면 읽기 어렵다 → 가로 스크롤로 원문 모양을 유지한다. */
@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(120.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (isHeader) FontFamily.Default else FontFamily.Monospace,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(96.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
