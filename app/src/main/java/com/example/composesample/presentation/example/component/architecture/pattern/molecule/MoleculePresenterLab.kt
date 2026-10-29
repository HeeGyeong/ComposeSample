package com.example.composesample.presentation.example.component.architecture.pattern.molecule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.molecule.moleculeFlow
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext

// ==================== 모델 · 이벤트 · 가짜 저장소 ====================

data class CatalogProduct(val name: String, val inStock: Boolean)

sealed interface CatalogEvent {
    data class QueryChanged(val query: String) : CatalogEvent
    data object ToggleInStock : CatalogEvent
    data object Refresh : CatalogEvent
}

/** 화면이 그리는 모델 — Flow 프리젠터와 Molecule 프리젠터가 같은 타입을 만든다 */
data class CatalogModel(
    val query: String,
    val inStockOnly: Boolean,
    val loading: Boolean,
    val items: List<CatalogProduct>,
) {
    companion object {
        /** stateIn 이 요구하는 초기값 — 프리젠터가 아직 아무것도 계산하지 않았을 때의 자리표시자 */
        val Placeholder = CatalogModel(query = "", inStockOnly = false, loading = false, items = emptyList())
    }
}

/** 두 프리젠터가 같은 규칙으로 모델을 만들도록 계산을 한곳에 둔다(results == null 이면 검색 중) */
fun catalogModelOf(query: String, inStockOnly: Boolean, results: List<CatalogProduct>?): CatalogModel =
    CatalogModel(
        query = query,
        inStockOnly = inStockOnly,
        loading = results == null,
        items = results.orEmpty().filter { !inStockOnly || it.inStock },
    )

/** 전이 로그 한 줄 — 화면과 단위 테스트가 같은 문장을 쓴다 */
fun describeCatalogModel(model: CatalogModel): String {
    val q = if (model.query.isEmpty()) "(전체)" else "'${model.query}'"
    val stock = if (model.inStockOnly) " · 재고만" else ""
    val body = if (model.loading) "검색 중" else "${model.items.size}개"
    return "$q$stock · $body"
}

/** 검색 호출 수를 세는 메모리 카탈로그 — 프리젠터마다 따로 둬서 호출 수를 나눠 센다 */
class FakeCatalog(private val latencyMs: Long = 400L) {
    private val products = listOf(
        CatalogProduct("Pixel 10 Pro", inStock = true),
        CatalogProduct("Pixel 10", inStock = false),
        CatalogProduct("Pixel Buds Pro", inStock = true),
        CatalogProduct("Pixel Watch", inStock = false),
        CatalogProduct("Pro Case", inStock = false),
        CatalogProduct("Clear Case", inStock = true),
        CatalogProduct("Charger Pro", inStock = true),
        CatalogProduct("USB-C Cable", inStock = true),
    )
    private val _searches = MutableStateFlow(0)
    val searches: StateFlow<Int> = _searches.asStateFlow()

    suspend fun search(query: String): List<CatalogProduct> {
        _searches.update { it + 1 }
        delay(latencyMs)
        return products.filter { it.name.contains(query, ignoreCase = true) }
    }
}

// ==================== 프리젠터 두 가지 ====================

/** Flow 연산자로 만든 프리젠터 — 입력마다 MutableStateFlow 를 두고 combine/flatMapLatest 로 엮는다 */
class CatalogFlowPresenter(private val catalog: FakeCatalog) {
    private val _computations = MutableStateFlow(0)
    /** 모델 계산(combine 람다) 횟수 */
    val computations: StateFlow<Int> = _computations.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun present(scope: CoroutineScope, events: Flow<CatalogEvent>): StateFlow<CatalogModel> {
        val query = MutableStateFlow("")
        val inStockOnly = MutableStateFlow(false)
        val refresh = MutableStateFlow(0)
        scope.launch {
            events.collect { event ->
                when (event) {
                    is CatalogEvent.QueryChanged -> query.value = event.query
                    CatalogEvent.ToggleInStock -> inStockOnly.update { !it }
                    CatalogEvent.Refresh -> refresh.update { it + 1 }
                }
            }
        }
        // 검색은 검색어·새로고침에만 반응하고, 재고 필터는 결과를 다시 거르기만 한다
        val results: Flow<List<CatalogProduct>?> = combine(query, refresh) { q, _ -> q }
            .flatMapLatest { q -> flow { emit(null); emit(catalog.search(q)) } }
        return combine(query, inStockOnly, results) { q, stock, r ->
            _computations.update { it + 1 }
            catalogModelOf(q, stock, r)
        }.stateIn(scope, SharingStarted.Eagerly, CatalogModel.Placeholder)
    }
}

/** Molecule 프리젠터 — 같은 일을 remember·LaunchedEffect 와 평범한 대입으로 쓴다 */
class CatalogMoleculePresenter(private val catalog: FakeCatalog) {
    private val _compositions = MutableStateFlow(0)
    /** 프리젠터 본문이 끝까지 실행돼 반영된 횟수(SideEffect) — 값을 돌려주는 컴포저블이라 매번 본문 전체가 다시 돈다 */
    val compositions: StateFlow<Int> = _compositions.asStateFlow()

    @Composable
    fun present(events: Flow<CatalogEvent>): CatalogModel {
        var query by remember { mutableStateOf("") }
        var inStockOnly by remember { mutableStateOf(false) }
        var refresh by remember { mutableIntStateOf(0) }
        LaunchedEffect(events) {
            events.collect { event ->
                when (event) {
                    is CatalogEvent.QueryChanged -> query = event.query
                    CatalogEvent.ToggleInStock -> inStockOnly = !inStockOnly
                    CatalogEvent.Refresh -> refresh++
                }
            }
        }
        var results by remember { mutableStateOf<List<CatalogProduct>?>(null) }
        LaunchedEffect(query, refresh) {
            results = null
            results = catalog.search(query)
        }
        SideEffect { _compositions.update { it + 1 } }
        return catalogModelOf(query, inStockOnly, results)
    }
}

// ==================== 실험 2: RecompositionMode 와 쓰기 패턴 ====================

enum class MoleculeClockMode(val label: String) {
    Immediate("Immediate"),
    ContextClock("ContextClock"),
}

enum class SnapshotWritePattern(val label: String) {
    Burst("한 번에 10번"),
    Yield("쓸 때마다 yield()"),
    PerFrame("프레임마다 1번"),
    Background("Default 스레드에서 10번"),
}

/** values 는 초기 컴포지션 값(0) 부터 방출된 순서 그대로 */
data class BurstResult(val mode: MoleculeClockMode, val pattern: SnapshotWritePattern, val values: List<Int>)

fun describeBurst(result: BurstResult): String {
    val emitted = result.values.drop(1)
    val seq = if (emitted.isEmpty()) "-" else emitted.joinToString("→")
    return "방출 ${emitted.size} · $seq"
}

/**
 * 스냅샷 상태 하나를 10번 바꾸고, 그 상태를 읽기만 하는 프리젠터가 몇 번 방출하는지 센다(메인 스레드에서 호출).
 * 실험마다 새 스코프 · 새 상태로 만들고 끝나면 취소한다.
 */
suspend fun runBurstExperiment(mode: MoleculeClockMode, pattern: SnapshotWritePattern): BurstResult {
    val counter = mutableIntStateOf(0)
    val values = mutableListOf<Int>()
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    try {
        when (mode) {
            MoleculeClockMode.Immediate ->
                scope.launchMolecule(RecompositionMode.Immediate, emitter = { values += it }) { counter.intValue }
            MoleculeClockMode.ContextClock ->
                scope.launchMolecule(
                    RecompositionMode.ContextClock,
                    emitter = { values += it },
                    context = AndroidUiDispatcher.Main,
                ) { counter.intValue }
        }
        when (pattern) {
            SnapshotWritePattern.Burst -> repeat(10) { counter.intValue++ }
            SnapshotWritePattern.Yield -> repeat(10) { counter.intValue++; yield() }
            SnapshotWritePattern.PerFrame -> repeat(10) { counter.intValue++; awaitFrame() }
            SnapshotWritePattern.Background -> withContext(Dispatchers.Default) { repeat(10) { counter.intValue++ } }
        }
        // 남은 재구성이 끝나도록 몇 프레임 기다린다
        repeat(6) { awaitFrame() }
    } finally {
        scope.cancel()
    }
    return BurstResult(mode, pattern, values.toList())
}

// ==================== 실험 3: 공유(launchMolecule) vs 수집마다(moleculeFlow) ====================

/** completed = 두 수집기가 모두 'pro' 검색 결과를 받았는가(3초 안에) */
data class SharingResult(val cold: Boolean, val collectors: Int, val compositions: Int, val searches: Int, val completed: Boolean)

fun describeSharing(result: SharingResult): String {
    val kind = if (result.cold) "moleculeFlow(콜드)" else "launchMolecule(핫)"
    val tail = if (result.completed) "" else " · ⚠ 3초 안에 끝나지 않음"
    return "$kind · 수집기 ${result.collectors} → 컴포지션 ${result.compositions}회 · 검색 ${result.searches}회$tail"
}

/** 같은 프리젠터를 두 곳에서 수집하고 검색어를 한 번 바꾼다(메인 스레드에서 호출) */
suspend fun runSharingExperiment(cold: Boolean): SharingResult {
    val catalog = FakeCatalog(latencyMs = 60L)
    val presenter = CatalogMoleculePresenter(catalog)
    val events = MutableSharedFlow<CatalogEvent>(extraBufferCapacity = 8)
    val seen = List(2) { MutableStateFlow<CatalogModel?>(null) }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val completed: Boolean = try {
        if (cold) {
            // 콜드 Flow — 수집기마다 컴포지션이 따로 생긴다. ContextClock 이라 수집하는 쪽 컨텍스트에 시계가 필요하다
            val models = moleculeFlow(RecompositionMode.ContextClock) { presenter.present(events) }
            seen.forEach { sink -> scope.launch(AndroidUiDispatcher.Main) { models.collect { sink.value = it } } }
        } else {
            // 핫 StateFlow — 컴포지션 하나를 여러 수집기가 나눠 본다
            val models = scope.launchMolecule(RecompositionMode.ContextClock, AndroidUiDispatcher.Main) {
                presenter.present(events)
            }
            seen.forEach { sink -> scope.launch { models.collect { sink.value = it } } }
        }
        val done = withTimeoutOrNull(3_000L) {
            seen.forEach { sink -> sink.first { it != null && it.query == "" && !it.loading } }
            events.emit(CatalogEvent.QueryChanged("pro"))
            // 검색어가 바뀐 첫 모델은 '새 검색어 + 이전 결과'(검색 중 아님)라 결과 내용까지 확인해야 새 검색이 끝난 것이다
            seen.forEach { sink ->
                sink.first { m -> m != null && m.query == "pro" && !m.loading && m.items.all { it.name.contains("pro", ignoreCase = true) } }
            }
        } != null
        // 마지막 컴포지션의 SideEffect 가 반영되도록 몇 프레임 기다린 뒤 닫는다(방출은 SideEffect 보다 먼저 일어난다)
        repeat(3) { awaitFrame() }
        done
    } finally {
        scope.cancel()
    }
    return SharingResult(cold, seen.size, presenter.compositions.value, catalog.searches.value, completed)
}

// ==================== 실험 4: 시계 없는 ContextClock ====================

/** handlerMessage = 스코프의 CoroutineExceptionHandler 가 받은 것, thrownMessage = launchMolecule 호출자가 받은 것 */
data class MissingClockResult(val withClock: Boolean, val handlerMessage: String?, val thrownMessage: String?, val value: Int?)

fun describeThrowable(t: Throwable): String = "${t::class.java.simpleName}: ${t.message}"

/**
 * ContextClock 을 시계 없는 스코프(viewModelScope 와 같은 구성)에서 띄워 본다.
 * 처리기가 없으면 실패가 그대로 앱 크래시가 되므로 실험 스코프에만 처리기를 단다.
 */
fun tryContextClock(withClock: Boolean, dispatcher: CoroutineContext = Dispatchers.Main.immediate): MissingClockResult {
    var handled: Throwable? = null
    val scope = CoroutineScope(SupervisorJob() + dispatcher + CoroutineExceptionHandler { _, t -> handled = t })
    var value: Int? = null
    val thrown: Throwable? = try {
        val models = if (withClock) {
            scope.launchMolecule(RecompositionMode.ContextClock, AndroidUiDispatcher.Main) { 42 }
        } else {
            scope.launchMolecule(RecompositionMode.ContextClock) { 42 }
        }
        value = models.value
        null
    } catch (e: IllegalStateException) {
        e
    } finally {
        scope.cancel()
    }
    return MissingClockResult(withClock, handled?.let(::describeThrowable), thrown?.let(::describeThrowable), value)
}
