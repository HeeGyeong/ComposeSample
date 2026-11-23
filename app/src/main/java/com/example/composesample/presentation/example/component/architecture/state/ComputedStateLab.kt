package com.example.composesample.presentation.example.component.architecture.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ==================== computedStateOf — 캐시 없는 대안(#185, Compose 1.13) ====================
// 같은 쓰기를 두 API 로 돌려 계산 · 리컴포지션을 센다. 화면(6번 카드)은 기기의 실제 컴포지션에서,
// 단위 테스트(DerivedStateCostLabTest)는 같은 리더 컴포저블을 JVM 컴포지션에서 돌린다.

/** 한 번 잴 때의 모양 — 입력은 매번 1 씩 바뀐다 */
enum class ComputeScenario(val label: String) {
    SharedSame("한 인스턴스 · 스코프 5 · 결과 같음"),
    OwnSame("스코프마다 인스턴스 5 · 결과 같음"),
    LayeredSame("기반 1 + 위층 4 · 스코프 1 · 결과 같음"),
    ChangedTwoReads("스코프 1 이 두 번 읽음 · 결과 바뀜")
}

/** 쓰기들이 부른 일 — calcs 는 스코프가 읽는 상태의 계산, baseCalcs 는 층 모양의 기반 계산 */
data class ComputeCost(val calcs: Int, val baseCalcs: Int, val recompositions: Int) {
    operator fun minus(other: ComputeCost) =
        ComputeCost(calcs - other.calcs, baseCalcs - other.baseCalcs, recompositions - other.recompositions)

    operator fun times(writes: Int) = ComputeCost(calcs * writes, baseCalcs * writes, recompositions * writes)
}

/**
 * 쓰기 한 번의 예상 — runtime 1.13.0(ComputedState.kt · RecomposeScopeImpl.kt · GapComposer.kt) 규칙.
 * - derivedStateOf: 의존성이 바뀌면 인스턴스마다 한 번 다시 계산해 레코드에 캐시 — 검사 · 다시 읽기 · 다른 스코프는 캐시를 쓴다.
 *   정책 없는 기반은 같은 값이어도 새 레코드라 위층이 따라 다시 계산된다(3번 카드)
 * - computedStateOf: 캐시가 없다. 스코프는 읽은 값을 따로 기억하고, 입력이 바뀌면 isInvalidFor 로 다시 계산해 비교한다.
 *   같으면 건너뛰는데, 건너뛴 스코프는 구독을 다시 걸려고 읽은 상태를 다시 읽는다(rereadTrackedInstances) → 검사 1 + 재읽기 1.
 *   다르면 다시 그리며 읽는 횟수만큼 또 계산한다. 위층이 기반을 읽으면 위층 계산마다 기반도 계산한다(스코프에는 바깥 것만 기록)
 */
fun expectedPerWrite(scenario: ComputeScenario, api: StateApi): ComputeCost = when (api) {
    StateApi.Derived -> when (scenario) {
        ComputeScenario.SharedSame -> ComputeCost(calcs = 1, baseCalcs = 0, recompositions = 0)
        ComputeScenario.OwnSame -> ComputeCost(calcs = SCOPES, baseCalcs = 0, recompositions = 0)
        ComputeScenario.LayeredSame -> ComputeCost(calcs = UPPERS, baseCalcs = 1, recompositions = 0)
        ComputeScenario.ChangedTwoReads -> ComputeCost(calcs = 1, baseCalcs = 0, recompositions = 1)
    }

    StateApi.Computed -> when (scenario) {
        ComputeScenario.SharedSame, ComputeScenario.OwnSame -> ComputeCost(calcs = SCOPES * 2, baseCalcs = 0, recompositions = 0)
        ComputeScenario.LayeredSame -> ComputeCost(calcs = UPPERS * 2, baseCalcs = UPPERS * 2, recompositions = 0)
        ComputeScenario.ChangedTwoReads -> ComputeCost(calcs = 1 + 2, baseCalcs = 0, recompositions = 1)
    }
}

const val SCOPES = 5
const val UPPERS = 4

/** 한 번 재기의 쓰기 수 */
const val COMPUTE_WRITES = 20

/** 결과가 바뀌지 않게 하는 문턱 — 입력은 0 부터 1 씩 오른다 */
private const val THRESHOLD = 1_000_000_000

/** 평범한 필드 카운터 — 세는 일이 상태 쓰기가 되면 안 된다 */
class ComputeCounters {
    var calcs = 0
    var baseCalcs = 0
    var recompositions = 0

    /** 읽은 값을 담아 둘 곳(읽기 식이 버려지지 않게) */
    var lastRead: Any? = null

    fun snapshot() = ComputeCost(calcs, baseCalcs, recompositions)
}

/** 한 번 재기에 쓰는 상태 묶음 — 한 API 로 만든다 */
class ComputeSetup(val scenario: ComputeScenario, val api: StateApi) {
    val counters = ComputeCounters()
    val input = mutableIntStateOf(0)

    private fun <T> make(calculation: () -> T): State<T> =
        if (api == StateApi.Computed) computedStateOf(calculation = calculation) else derivedStateOf(calculation)

    /** 스코프가 읽는 상태 — 공유면 같은 인스턴스가 다섯 번 들어 있다 */
    val readers: List<State<Any>> = when (scenario) {
        ComputeScenario.SharedSame -> {
            val shared = make { counters.calcs++; input.intValue > THRESHOLD }
            List(SCOPES) { shared }
        }

        ComputeScenario.OwnSame -> List(SCOPES) { make { counters.calcs++; input.intValue > THRESHOLD } }
        ComputeScenario.LayeredSame -> {
            val base = make { counters.baseCalcs++; input.intValue / THRESHOLD }
            List(UPPERS) { index -> make { counters.calcs++; base.value + index > THRESHOLD } }
        }

        ComputeScenario.ChangedTwoReads -> listOf(make { counters.calcs++; input.intValue })
    }

    fun write() {
        input.intValue++
    }
}

/** 스코프 모양 — 화면과 단위 테스트가 같은 것을 쓴다. 레이아웃 노드를 만들지 않는다 */
@Composable
fun ComputeReaders(setup: ComputeSetup) {
    val counters = setup.counters
    when (setup.scenario) {
        ComputeScenario.SharedSame, ComputeScenario.OwnSame ->
            setup.readers.forEachIndexed { index, state -> key(index) { ReadOnce(state, counters) } }

        ComputeScenario.LayeredSame -> ReadAll(setup.readers, counters)
        ComputeScenario.ChangedTwoReads -> ReadTwice(setup.readers.first(), counters)
    }
}

@Composable
private fun ReadOnce(state: State<Any>, counters: ComputeCounters) {
    SideEffect { counters.recompositions++ }
    counters.lastRead = state.value
}

@Composable
private fun ReadAll(states: List<State<Any>>, counters: ComputeCounters) {
    SideEffect { counters.recompositions++ }
    states.forEach { counters.lastRead = it.value }
}

@Composable
private fun ReadTwice(state: State<Any>, counters: ComputeCounters) {
    SideEffect { counters.recompositions++ }
    counters.lastRead = state.value
    counters.lastRead = state.value
}

/** 한 API 의 결과 */
data class ComputeResult(val scenario: ComputeScenario, val api: StateApi, val writes: Int, val measured: ComputeCost) {
    val expected: ComputeCost get() = expectedPerWrite(scenario, api) * writes
    val matches: Boolean get() = measured == expected
}

/**
 * 계산 안에서 상태를 쓰면 — computedStateOf 는 계산을 읽기 전용 스냅숏(Snapshot.readOnly)에서 돌려 IllegalStateException,
 * derivedStateOf 는 막지 않는다(쓰기가 그대로 반영된다)
 */
fun writeInsideCalculation(api: StateApi): String {
    val target = mutableIntStateOf(0)
    val calculation = { target.intValue = 1; 0 }
    val state = if (api == StateApi.Computed) computedStateOf(calculation = calculation) else derivedStateOf(calculation)
    return try {
        state.value
        "예외 없음 — 계산 안의 쓰기가 반영됐다(target = ${target.intValue})"
    } catch (e: IllegalStateException) {
        "IllegalStateException: ${e.message}"
    }
}

/** 6번 카드 상태 */
class ComputeLabState {
    var scenario by mutableStateOf(ComputeScenario.SharedSame)
    var setup by mutableStateOf<ComputeSetup?>(null)
        private set
    var running by mutableStateOf(false)
        private set
    var runToken by mutableIntStateOf(0)
        private set
    var results by mutableStateOf<List<ComputeResult>>(emptyList())
        private set
    var writeResults by mutableStateOf<List<Pair<StateApi, String>>>(emptyList())
        private set

    fun request() {
        if (!running) runToken++
    }

    fun tryWrites() {
        writeResults = StateApi.entries.map { it to writeInsideCalculation(it) }
    }

    /**
     * 두 API 를 차례로 — 리더를 새 상태로 컴포즈하고 두 프레임 기다린 뒤(첫 읽기 제외) 쓰기마다 두 프레임을 기다린다.
     * 쓰기 한 번의 리컴포지션이 다음 쓰기와 섞이지 않게 하려는 것
     */
    suspend fun execute(awaitFrame: suspend () -> Unit) {
        running = true
        try {
            val out = mutableListOf<ComputeResult>()
            for (api in StateApi.entries) {
                val next = ComputeSetup(scenario, api)
                setup = next
                repeat(2) { awaitFrame() }
                val before = next.counters.snapshot()
                repeat(COMPUTE_WRITES) {
                    next.write()
                    repeat(2) { awaitFrame() }
                }
                out += ComputeResult(scenario, api, COMPUTE_WRITES, next.counters.snapshot() - before)
            }
            results = out
        } finally {
            setup = null
            running = false
        }
    }
}
