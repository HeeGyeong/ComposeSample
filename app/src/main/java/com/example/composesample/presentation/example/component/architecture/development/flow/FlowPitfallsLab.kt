package com.example.composesample.presentation.example.component.architecture.development.flow

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicInteger

/**
 * Flow 함정 실측 재료 — 화면(FlowPitfallsExampleUI)과 JVM 단위 테스트(FlowPitfallsLabTest)가 같은 함수를 쓴다.
 * 시간 대신 '업스트림 실행 횟수 · 받은 값 · catch 호출 수'를 센다 → 디버그 빌드(인터프리터)에서도 결과가 흔들리지 않는다.
 */

// ==================== 공용 가짜 API ====================

/** 업스트림 1회 비용(네트워크 흉내). 두 수집기가 응답 전에 모두 붙도록 충분히 길게 둔다. */
internal const val FETCH_LATENCY_MS = 300L

internal data class UserSnapshot(val version: Int) {
    val label: String get() = "v$version"
}

/** 호출 횟수를 세는 가짜 API — 호출마다 버전이 올라가므로 "두 화면이 서로 다른 응답을 받았다"가 그대로 보인다. */
internal class CountingUserApi {

    private val calls = AtomicInteger(0)

    val fetchCount: Int get() = calls.get()

    suspend fun fetchUser(): UserSnapshot {
        val version = calls.incrementAndGet()
        delay(FETCH_LATENCY_MS)
        return UserSnapshot(version)
    }

    /** 기사의 observeUser() 그대로 — 콜드 Flow 라 collect 할 때마다 fetchUser() 가 다시 돈다. */
    fun observeUser(): Flow<UserSnapshot> = flow { emit(fetchUser()) }
}

// ==================== 1. 콜드 Flow 를 두 곳에서 수집 ====================

internal enum class CollectMode(val label: String, val code: String) {
    COLD(
        "콜드 그대로",
        "val user = repository.observeUser()\n" +
            "launch { user.collect(::renderHeader) }\n" +
            "launch { user.collect(::renderProfile) }"
    ),
    SHARED(
        "shareIn 으로 공유",
        "val user = repository.observeUser()\n" +
            "    .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)\n" +
            "launch { user.collect(::renderHeader) }\n" +
            "launch { user.collect(::renderProfile) }"
    )
}

internal data class TwoCollectorResult(
    val mode: CollectMode,
    val fetchCount: Int,
    val header: String,
    val profile: String
)

internal suspend fun runTwoCollectors(mode: CollectMode): TwoCollectorResult = coroutineScope {
    val api = CountingUserApi()
    // shareIn 의 공유 코루틴은 끝나지 않으므로 측정이 끝나면 이 Job 으로 거둔다(실제 앱의 applicationScope 자리).
    val sharingJob = Job(coroutineContext[Job])
    try {
        val user = when (mode) {
            CollectMode.COLD -> api.observeUser()
            CollectMode.SHARED -> api.observeUser()
                .shareIn(this + sharingJob, SharingStarted.WhileSubscribed(), replay = 1)
        }
        // 두 화면(헤더·프로필)이 동시에 수집을 시작한다.
        val header = async { user.first() }
        val profile = async { user.first() }
        val headerUser = header.await()
        val profileUser = profile.await()
        TwoCollectorResult(mode, api.fetchCount, headerUser.label, profileUser.label)
    } finally {
        sharingJob.cancel()
    }
}

// ==================== 2. shareIn 설정 — replay · 재구독 ====================

/** 두 번째 구독자가 값을 기다려 주는 시간. 업스트림 1회(FETCH_LATENCY_MS)가 충분히 들어갈 만큼 길다. */
internal const val OBSERVE_WINDOW_MS = 800L

/** 첫 구독자가 떠난 뒤 다음 구독자가 붙기까지의 간격. 구독자 0 상태가 공유 코루틴에 전달될 시간을 준다. */
internal const val LEAVE_GAP_MS = 100L

/** WhileSubscribed(stopTimeoutMillis) 값 — 흔히 쓰는 5초. LEAVE_GAP_MS 보다 훨씬 길다. */
internal const val STOP_TIMEOUT_MS = 5_000L

internal enum class ShareCase(val label: String, val code: String, val note: String) {
    LATE_NO_REPLAY(
        "늦은 구독 · replay = 0",
        "shareIn(scope, WhileSubscribed(), replay = 0)",
        "첫 구독자가 값을 받고 계속 붙어 있는 동안 두 번째 구독자가 붙는다"
    ),
    LATE_REPLAY(
        "늦은 구독 · replay = 1",
        "shareIn(scope, WhileSubscribed(), replay = 1)",
        "같은 순서, replay 만 1"
    ),
    RESUBSCRIBE(
        "모두 떠난 뒤 재구독",
        "shareIn(scope, WhileSubscribed(), replay = 1)",
        "첫 구독자가 값을 받고 떠나 구독자가 0 이 된 뒤 새 구독자가 붙는다"
    ),
    RESUBSCRIBE_WITHIN_TIMEOUT(
        "재구독 · stopTimeout 5초",
        "shareIn(scope, WhileSubscribed(5_000), replay = 1)",
        "같은 순서, 떠난 지 ${LEAVE_GAP_MS}ms 만에 다시 붙는다(5초 안)"
    ),
    RESUBSCRIBE_EXPIRED_REPLAY(
        "재구독 · replayExpiration 0",
        "shareIn(scope, WhileSubscribed(replayExpirationMillis = 0), replay = 1)",
        "재구독과 같은 순서, 멈출 때 replay 캐시를 바로 비운다"
    )
}

internal data class ShareResult(
    val case: ShareCase,
    val fetchCount: Int,
    val first: String,
    val second: List<String>
)

internal suspend fun runShareCase(case: ShareCase): ShareResult = coroutineScope {
    val api = CountingUserApi()
    val sharingJob = Job(coroutineContext[Job])
    try {
        val started = when (case) {
            ShareCase.RESUBSCRIBE_WITHIN_TIMEOUT -> SharingStarted.WhileSubscribed(stopTimeoutMillis = STOP_TIMEOUT_MS)
            ShareCase.RESUBSCRIBE_EXPIRED_REPLAY -> SharingStarted.WhileSubscribed(replayExpirationMillis = 0)
            else -> SharingStarted.WhileSubscribed()
        }
        val replay = if (case == ShareCase.LATE_NO_REPLAY) 0 else 1
        val user = api.observeUser().shareIn(this + sharingJob, started, replay)

        // 첫 구독자 — 늦은 구독 두 경우는 계속 붙어 있고, 재구독 두 경우는 값을 받자마자 떠난다.
        var stayingCollector: Job? = null
        val first = when (case) {
            ShareCase.LATE_NO_REPLAY, ShareCase.LATE_REPLAY -> {
                val received = CompletableDeferred<UserSnapshot>()
                stayingCollector = launch { user.collect { received.complete(it) } }
                received.await()
            }
            ShareCase.RESUBSCRIBE, ShareCase.RESUBSCRIBE_WITHIN_TIMEOUT, ShareCase.RESUBSCRIBE_EXPIRED_REPLAY -> {
                val value = user.first()
                delay(LEAVE_GAP_MS)
                value
            }
        }

        // 두 번째 구독자 — 정해진 시간 동안 받은 값을 모두 적는다(아무것도 안 오면 빈 목록).
        val second = mutableListOf<String>()
        withTimeoutOrNull(OBSERVE_WINDOW_MS) { user.collect { second += it.label } }
        stayingCollector?.cancel()

        ShareResult(case, api.fetchCount, first.label, second.toList())
    } finally {
        sharingJob.cancel()
    }
}

// ==================== 3. catch 는 자기보다 위(상류)만 잡는다 ====================

/** 실패를 일으키는 값. 기사 원문은 flowOf(1000) 에 `it == 1` 이라 어느 순서에서도 예외가 나지 않는다. */
internal const val FAILING_VALUE = 1

/** 기사 원문의 입력값 — 순서를 바꿔도 결과가 같다는 것을 보여 주는 대조군. */
internal const val ARTICLE_VALUE = 1000

internal enum class CatchCase(val label: String, val code: String) {
    CATCH_THEN_ON_EACH(
        "catch 가 onEach 위",
        "flowOf(x)\n" +
            "    .catch { emit(-1) }\n" +
            "    .onEach { if (it == 1) error(\"Boom!\") }\n" +
            "    .collect { received += it }"
    ),
    ON_EACH_THEN_CATCH(
        "onEach 가 catch 위",
        "flowOf(x)\n" +
            "    .onEach { if (it == 1) error(\"Boom!\") }\n" +
            "    .catch { emit(-1) }\n" +
            "    .collect { received += it }"
    ),
    THROW_IN_COLLECT(
        "collect 람다에서 실패",
        "flowOf(x)\n" +
            "    .catch { emit(-1) }\n" +
            "    .collect { if (it == 1) error(\"Boom!\"); received += it }"
    )
}

internal data class CatchResult(
    val case: CatchCase,
    val input: Int,
    val received: List<Int>,
    val handlerCalls: Int,
    // collect 밖까지 빠져나온 예외("타입: 메시지"). 정상 완료면 null.
    val escaped: String?
)

private fun failOn(value: Int) {
    if (value == FAILING_VALUE) error("Boom!")
}

internal suspend fun runCatchCase(case: CatchCase, input: Int): CatchResult {
    val received = mutableListOf<Int>()
    var handlerCalls = 0
    val escaped = try {
        when (case) {
            CatchCase.CATCH_THEN_ON_EACH -> flowOf(input)
                .catch { handlerCalls++; emit(-1) }
                .onEach { failOn(it) }
                .collect { received += it }
            CatchCase.ON_EACH_THEN_CATCH -> flowOf(input)
                .onEach { failOn(it) }
                .catch { handlerCalls++; emit(-1) }
                .collect { received += it }
            CatchCase.THROW_IN_COLLECT -> flowOf(input)
                .catch { handlerCalls++; emit(-1) }
                .collect { failOn(it); received += it }
        }
        null
    } catch (e: IllegalStateException) {
        // failOn 이 던지는 예외만 결과로 기록한다. 그 밖의 예외는 실험이 잘못된 것이므로 그대로 올려 보낸다.
        "${e::class.simpleName}: ${e.message}"
    }
    return CatchResult(case, input, received.toList(), handlerCalls, escaped)
}

// ==================== 4. catch 와 취소 ====================

/** 시간 초과 기준. 상류 작업(LONG_WORK_MS)보다 짧아 반드시 시간 초과가 난다. */
internal const val SHORT_TIMEOUT_MS = 100L

internal const val LONG_WORK_MS = 1_000L

internal enum class CancelCase(val label: String, val code: String) {
    COLLECTOR_CANCELLED(
        "수집 코루틴이 취소됨",
        "val job = launch {\n" +
            "    flow { emit(1); awaitCancellation() }\n" +
            "        .catch { emit(-1) }\n" +
            "        .collect { received += it }\n" +
            "}\n" +
            "job.cancel()"
    ),
    TIMEOUT_AROUND_COLLECT(
        "withTimeout 이 수집을 감쌈",
        "withTimeout(100) {\n" +
            "    flow { delay(1_000); emit(1) }\n" +
            "        .catch { emit(-1) }\n" +
            "        .collect { received += it }\n" +
            "}"
    ),
    TIMEOUT_INSIDE_UPSTREAM(
        "withTimeout 이 상류 안",
        "flow { withTimeout(100) { delay(1_000) }; emit(1) }\n" +
            "    .catch { emit(-1) }\n" +
            "    .collect { received += it }"
    ),
    RETHROW_IN_HANDLER(
        "catch 에서 CancellationException 을 다시 던짐",
        "launch {\n" +
            "    flow { withTimeout(100) { delay(1_000) }; emit(1) }\n" +
            "        .catch { e ->\n" +
            "            if (e is CancellationException) throw e\n" +
            "            emit(-1)\n" +
            "        }\n" +
            "        .collect { received += it }\n" +
            "}"
    )
}

internal data class CancelResult(
    val case: CancelCase,
    val handlerCalls: Int,
    // catch 핸들러가 받은 예외 타입. 핸들러가 불리지 않았으면 null.
    val handlerSaw: String?,
    val received: List<Int>,
    val outcome: String
)

internal suspend fun runCancelCase(case: CancelCase): CancelResult = coroutineScope {
    val received = mutableListOf<Int>()
    var handlerCalls = 0
    var handlerSaw: String? = null

    val outcome = when (case) {
        CancelCase.COLLECTOR_CANCELLED -> {
            val job = launch {
                flow { emit(1); awaitCancellation() }
                    .catch { e -> handlerCalls++; handlerSaw = e::class.simpleName; emit(-1) }
                    .collect { received += it }
            }
            // 첫 값을 받고 상류가 멈춰 있는 동안 취소한다.
            delay(SHORT_TIMEOUT_MS)
            job.cancelAndJoin()
            if (job.isCancelled) "수집 코루틴이 취소로 끝남" else "정상 완료"
        }
        CancelCase.TIMEOUT_AROUND_COLLECT -> try {
            withTimeout(SHORT_TIMEOUT_MS) {
                flow { delay(LONG_WORK_MS); emit(1) }
                    .catch { e -> handlerCalls++; handlerSaw = e::class.simpleName; emit(-1) }
                    .collect { received += it }
            }
            "정상 완료"
        } catch (e: TimeoutCancellationException) {
            // 바깥 코루틴은 취소되지 않았으므로 여기서 잡아도 안전하다(withTimeout 이 자기 블록만 취소했다).
            "${e::class.simpleName} 이 withTimeout 밖으로 나옴"
        }
        CancelCase.TIMEOUT_INSIDE_UPSTREAM -> {
            flow { withTimeout(SHORT_TIMEOUT_MS) { delay(LONG_WORK_MS) }; emit(1) }
                .catch { e -> handlerCalls++; handlerSaw = e::class.simpleName; emit(-1) }
                .collect { received += it }
            "정상 완료"
        }
        CancelCase.RETHROW_IN_HANDLER -> {
            val job = launch {
                flow { withTimeout(SHORT_TIMEOUT_MS) { delay(LONG_WORK_MS) }; emit(1) }
                    .catch { e ->
                        handlerCalls++
                        handlerSaw = e::class.simpleName
                        if (e is CancellationException) throw e
                        emit(-1)
                    }
                    .collect { received += it }
            }
            job.join()
            // 몸체가 CancellationException 으로 끝난 코루틴은 '취소됨'이 되고, 부모에게 실패를 전하지 않는다.
            if (job.isCancelled) "launch 가 취소로 끝남(부모에 실패 전파 없음)" else "정상 완료"
        }
    }
    CancelResult(case, handlerCalls, handlerSaw, received.toList(), outcome)
}
