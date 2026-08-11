package com.example.composesample.presentation.example.component.architecture.development.internals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.tooling.SnapshotInstanceObservers
import androidx.compose.runtime.snapshots.tooling.SnapshotObserver
import androidx.compose.runtime.snapshots.tooling.observeSnapshots
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionObserver
import androidx.compose.runtime.tooling.IdentifiableRecomposeScope
import androidx.compose.runtime.tooling.ObservableComposition
import androidx.compose.runtime.tooling.setObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// ============================================================================
// 관찰 인프라 — 전부 plain 객체(스냅샷 상태 아님)
//
// 관찰자 콜백은 ① 컴포지션이 진행되는 도중 ② 전역 스냅샷 락 아래에서 실행된다.
// 그래서 콜백 안에서 스냅샷 상태에 쓰면 "컴포지션 도중 쓰기"가 되어 무한 리컴포지션으로 번지고,
// 무거운 작업을 하면 스냅샷 락을 잡은 채 전체 앱을 멈춘다.
// → 콜백에서는 사전 할당된 링버퍼에 O(1) 로 적재만 하고, 화면 반영은 안전한 시점(클릭 핸들러)에 따로 한다.
// ============================================================================

/**
 * 이름 없는 상태 객체에 이름을 붙이는 최소 레지스트리.
 *
 * 관찰 API 가 콜백으로 넘겨주는 것은 상태 "객체"일 뿐 이름이 없어서, 그대로 찍으면
 * `MutableIntState(value=3)@1387209841` 처럼 보인다. 어떤 변수였는지 알아보려면 이렇게
 * 등록 시점에 이름을 직접 붙여 두는 수밖에 없다(예제 하단 "관찰 API의 한계" 카드 참조).
 *
 * 조회는 identity(===) 비교이며 등록 개수가 3개뿐이라 사실상 O(1) 이다.
 */
private class StateNameRegistry(capacity: Int = 8) {
    private val objects = arrayOfNulls<Any>(capacity)
    private val names = arrayOfNulls<String>(capacity)
    private var size = 0

    fun register(target: Any, name: String) {
        if (size >= objects.size) return
        objects[size] = target
        names[size] = name
        size++
    }

    /** 추적 대상이면 이름을, 아니면 null 을 반환한다. null 이 곧 "이 이벤트는 무시" 필터가 된다. */
    fun nameOf(target: Any?): String? {
        if (target == null) return null
        for (i in 0 until size) {
            if (objects[i] === target) return names[i]
        }
        return null
    }
}

/** 사전 할당된 고정 크기 링버퍼. 콜백에서의 적재는 할당 없이 O(1). */
private class EventRing(private val capacity: Int = 64) {
    private val buffer = arrayOfNulls<String>(capacity)
    private var writeIndex = 0
    private var totalCount = 0

    val count: Int get() = totalCount

    fun add(line: String) {
        buffer[writeIndex] = line
        writeIndex = (writeIndex + 1) % capacity
        totalCount++
    }

    /** 최신순으로 복사해 반환한다. 콜백이 아니라 화면 갱신 시점에만 호출할 것. */
    fun latestFirst(): List<String> {
        val result = ArrayList<String>(minOf(totalCount, capacity))
        val available = minOf(totalCount, capacity)
        for (i in 1..available) {
            val index = ((writeIndex - i) % capacity + capacity) % capacity
            buffer[index]?.let { result.add(it) }
        }
        return result
    }

    fun clear() {
        buffer.fill(null)
        writeIndex = 0
        totalCount = 0
    }
}

/**
 * "왜 리컴포즈됐는가"를 인과 로그로 남기는 CompositionObserver 구현체.
 *
 * setObserver 는 컴포지션 하나 전체에 붙기 때문에 화면의 모든 스코프 이벤트가 흘러 들어온다.
 * 그대로 기록하면 초당 수백 줄이 되므로, 레지스트리에 등록된 상태 객체가 관여한 이벤트만 남긴다.
 */
@OptIn(ExperimentalComposeRuntimeApi::class, ComposeToolingApi::class)
private class CausalCompositionObserver(
    private val registry: StateNameRegistry,
    private val ring: EventRing
) : CompositionObserver {

    // 스코프도 이름이 없어서 S1, S2... 별칭을 순서대로 붙인다.
    private val knownScopes = arrayOfNulls<Any>(16)
    private val scopeAliases = arrayOfNulls<String>(16)
    private var scopeCount = 0

    // 추적 대상 상태를 읽거나 그것 때문에 무효화된 적 있는 "관심 스코프"만 진입/이탈을 기록한다.
    private val interestingScopes = arrayOfNulls<Any>(16)
    private var interestingCount = 0

    private fun aliasOf(scope: RecomposeScope): String {
        for (i in 0 until scopeCount) {
            if (knownScopes[i] === scope) return scopeAliases[i]!!
        }
        if (scopeCount >= knownScopes.size) return "S?"
        val alias = "S${scopeCount + 1}"
        knownScopes[scopeCount] = scope
        scopeAliases[scopeCount] = alias
        scopeCount++
        return alias
    }

    private fun markInteresting(scope: RecomposeScope) {
        for (i in 0 until interestingCount) {
            if (interestingScopes[i] === scope) return
        }
        if (interestingCount >= interestingScopes.size) return
        interestingScopes[interestingCount] = scope
        interestingCount++
    }

    private fun isInteresting(scope: RecomposeScope): Boolean {
        for (i in 0 until interestingCount) {
            if (interestingScopes[i] === scope) return true
        }
        return false
    }

    /**
     * 스코프의 정체성. `identity` 는 슬롯 테이블 상의 앵커라 리컴포지션을 넘어 같은 스코프면 같은 값이지만,
     * 여기서도 사람이 읽을 수 있는 함수 이름은 나오지 않는다(짧은 해시로만 표기).
     */
    private fun identityHint(scope: RecomposeScope): String {
        val identity = (scope as? IdentifiableRecomposeScope)?.identity ?: return "id=?"
        return "id=@${Integer.toHexString(System.identityHashCode(identity))}"
    }

    override fun onBeginComposition(composition: ObservableComposition) {
        ring.add("▶ onBeginComposition — 리컴포지션 패스 시작")
    }

    override fun onEndComposition(composition: ObservableComposition) {
        ring.add("⏹ onEndComposition — 패스 종료")
    }

    override fun onScopeEnter(scope: RecomposeScope) {
        if (isInteresting(scope)) ring.add("  ┌ onScopeEnter ${aliasOf(scope)} — 이 스코프 재실행 시작")
    }

    override fun onScopeExit(scope: RecomposeScope) {
        if (isInteresting(scope)) ring.add("  └ onScopeExit ${aliasOf(scope)}")
    }

    /** 이 스코프가 그 상태를 "구독"하는 순간. 이 구독이 있어야 나중에 onScopeInvalidated 가 발생한다. */
    override fun onReadInScope(scope: RecomposeScope, value: Any) {
        val name = registry.nameOf(value) ?: return
        markInteresting(scope)
        ring.add("  ├ onReadInScope ${aliasOf(scope)} ← $name 읽음(구독 형성)")
    }

    /** 핵심 콜백 — "어떤 스코프가 어떤 상태 때문에 무효화됐는가", 즉 리컴포지션의 원인. */
    override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {
        val name = registry.nameOf(value) ?: return
        markInteresting(scope)
        ring.add("🔴 onScopeInvalidated ${aliasOf(scope)} ⟸ $name 변경 [${identityHint(scope)}]")
    }

    override fun onScopeDisposed(scope: RecomposeScope) {
        if (isInteresting(scope)) ring.add("  ✖ onScopeDisposed ${aliasOf(scope)} — 컴포지션에서 제거")
    }
}

/**
 * Snapshot 계층 관찰자. 컴포지션이 아니라 "스냅샷 시스템"을 관찰하므로
 * 어떤 컴포지션에도 속하지 않는 쓰기(ViewModel, 백그라운드 코루틴 등)까지 잡힌다.
 */
@OptIn(ExperimentalComposeRuntimeApi::class)
private class SnapshotTraceObserver(
    private val registry: StateNameRegistry,
    private val ring: EventRing
) : SnapshotObserver {

    override fun onPreCreate(parent: Snapshot?, readonly: Boolean): SnapshotInstanceObservers {
        // 생성되는 스냅샷마다 read/write 옵저버를 주입한다. 읽기는 빈도가 지나치게 높아 기록하지 않는다.
        return SnapshotInstanceObservers(
            readObserver = { },
            writeObserver = { target ->
                registry.nameOf(target)?.let { ring.add("✏️ [observeSnapshots] write — $it") }
            }
        )
    }

    override fun onCreated(
        snapshot: Snapshot,
        parent: Snapshot?,
        observers: SnapshotInstanceObservers?
    ) = Unit

    override fun onPreDispose(snapshot: Snapshot) = Unit

    override fun onApplied(snapshot: Snapshot, changed: Set<Any>) {
        // changed 는 이 스냅샷이 실제로 바꾼 상태 객체 집합이다.
        // 집합 순회지만 추적 대상이 3개뿐이라 사실상 상수 시간이고, 여기는 컴포지션 도중이 아니다.
        for (target in changed) {
            registry.nameOf(target)?.let { ring.add("✅ [observeSnapshots] onApplied — $it") }
        }
    }
}

// ============================================================================
// UI
// ============================================================================

@Composable
fun CompositionObserverExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Composition Observer (왜 리컴포즈됐나)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { CausalLogCard() }
            item { CountVsCauseCard() }
            item { SnapshotObserverCard() }
            item { LimitationCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CompositionObserver 란",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "리컴포지션을 다루는 기존 도구들은 대부분 \"몇 번 일어났는가\"만 알려줍니다. " +
                        "SideEffect 로 카운터를 올리거나 Layout Inspector 의 recomposition count 를 보는 방식이 그렇습니다. " +
                        "CompositionObserver 는 여기에 없는 축을 답합니다 — 어떤 스코프가 어떤 상태 객체 때문에 " +
                        "무효화되었는지, 즉 리컴포지션의 원인을 런타임에게 직접 물어봅니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "@OptIn(ExperimentalComposeRuntimeApi::class)\n" +
                        "@Composable\n" +
                        "fun Attach() {\n" +
                        "    // currentComposer.composition = 지금 이 Composable 이 속한 컴포지션\n" +
                        "    val composition = currentComposer.composition\n" +
                        "    DisposableEffect(Unit) {\n" +
                        "        // ⚠️ 반환 타입이 nullable — 이미 관찰자가 붙어 있으면 null\n" +
                        "        val handle = composition.setObserver(myObserver)\n" +
                        "        onDispose { handle?.dispose() }\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val callbacks = listOf(
                Triple("onBeginComposition", "패스", "리컴포지션 패스가 시작될 때 1회"),
                Triple("onScopeEnter", "스코프", "각 리컴포즈 스코프의 본문 재실행 직전"),
                Triple("onReadInScope", "구독", "그 스코프가 상태를 읽어 구독을 형성할 때"),
                Triple("onScopeExit", "스코프", "스코프 본문 재실행이 끝날 때"),
                Triple("onScopeInvalidated", "원인", "★ 어떤 상태 변경이 어떤 스코프를 무효화했는지"),
                Triple("onScopeDisposed", "해제", "스코프가 컴포지션에서 제거될 때"),
                Triple("onEndComposition", "패스", "리컴포지션 패스가 끝날 때 1회")
            )
            callbacks.forEach { (name, kind, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(
                            if (name == "onScopeInvalidated") Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = if (name == "onScopeInvalidated") Color(0xFFD32F2F) else Color(0xFF1976D2),
                        modifier = Modifier.weight(0.34f)
                    )
                    Text(text = kind, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.14f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.52f))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeRuntimeApi::class)
@Composable
private fun CausalLogCard() {
    val ring = remember { EventRing() }
    val registry = remember { StateNameRegistry() }

    // 상태 객체를 만들면서 곧바로 이름을 등록한다 — 관찰자는 이 이름이 붙은 상태만 기록한다.
    val counterA = remember { mutableIntStateOf(0).also { registry.register(it, "counterA") } }
    val counterB = remember { mutableIntStateOf(0).also { registry.register(it, "counterB") } }

    var observing by remember { mutableStateOf(false) }
    val logLines = remember { mutableStateListOf<String>() }

    // 이 Composable 이 속한 컴포지션. 관찰자는 이 컴포지션 전체(=화면 전체)에 붙는다.
    val composition = currentComposer.composition

    DisposableEffect(observing) {
        if (!observing) return@DisposableEffect onDispose { }
        val handle = composition.setObserver(CausalCompositionObserver(registry, ring))
        onDispose { handle?.dispose() }
    }

    // 링버퍼 → 화면 상태 복사. 반드시 콜백 밖(클릭 핸들러)에서만 호출한다.
    fun publish() {
        logLines.clear()
        logLines.addAll(ring.latestFirst())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — 리컴포지션 인과 로그",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "관찰을 시작한 뒤 counterA / counterB 를 눌러보세요. 두 값은 각각 다른 자식 Composable 안에서 " +
                        "읽히므로 서로 다른 리컴포즈 스코프에 구독됩니다. 로그를 보면 A 를 눌렀을 때 A 를 읽은 스코프만 " +
                        "무효화되고 B 쪽 스코프는 조용하다는 것이 원인 단위로 드러납니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        observing = !observing
                        if (!observing) publish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (observing) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                ) {
                    Text(
                        text = if (observing) "관찰 중지" else "관찰 시작",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = { publish() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(text = "로그 새로고침", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        ring.clear()
                        logLines.clear()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "지우기", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { counterA.intValue++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text(text = "counterA 증가", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { counterB.intValue++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text(text = "counterB 증가", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 각각 별도의 Composable = 별도의 리컴포즈 스코프. 상태를 파라미터가 아니라 객체로 넘겨
            // "읽기"가 자식 스코프 안에서 일어나게 한다(값으로 넘기면 부모 스코프가 읽은 것이 된다).
            TrackedCounterRow(label = "counterA 를 읽는 스코프", state = counterA, accent = Color(0xFF00897B))
            Spacer(modifier = Modifier.height(6.dp))
            TrackedCounterRow(label = "counterB 를 읽는 스코프", state = counterB, accent = Color(0xFF6A1B9A))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (observing) "🟢 관찰 중" else "⚪ 관찰 중지됨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (observing) Color(0xFF388E3C) else Color(0xFF757575)
                )
                Text(
                    text = "버퍼에 적재된 이벤트 ${ring.count}건",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF757575)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (logLines.isEmpty()) {
                    Text(
                        text = "(로그 없음 — 관찰 시작 후 카운터를 누르고 \"로그 새로고침\"을 눌러보세요)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    logLines.take(14).forEach { line ->
                        Text(
                            text = line,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (line.startsWith("🔴")) Color(0xFFFF8A80) else Color(0xFFB3E5FC),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "로그가 최신순이라 실제 진행 순서는 아래에서 위입니다. 상태를 바꾸면 먼저 " +
                        "onScopeInvalidated 로 원인이 기록되고, 그 다음 패스에서 해당 스코프만 다시 실행되며 " +
                        "onReadInScope 로 구독이 다시 형성되는 흐름이 보입니다.",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun TrackedCounterRow(label: String, state: MutableIntState, accent: Color) {
    // state.intValue 읽기가 이 함수 안에서 일어나므로, 구독도 무효화도 이 스코프에 귀속된다.
    val value = state.intValue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = accent)
        Text(
            text = "value = $value",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = accent
        )
    }
}

@Composable
private fun CountVsCauseCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\"몇 번\" 계측과 무엇이 다른가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SideEffect 로 카운터를 올리는 방식은 이 프로젝트의 리컴포지션 예제들이 공통으로 쓰는 계측입니다. " +
                        "그 방식은 횟수를 정확히 세지만, 횟수가 늘어난 이유는 개발자가 코드를 읽어 추론해야 합니다. " +
                        "CompositionObserver 는 그 추론 단계를 런타임의 사실로 대체합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// 계측 A — 횟수만 답한다(기존 예제들의 방식)\n" +
                        "@Composable\n" +
                        "fun Child() {\n" +
                        "    val counter = remember { RecompositionCounter() }\n" +
                        "    SideEffect { counter.value++ }   // \"3번 리컴포즈됨\"\n" +
                        "}\n\n" +
                        "// 계측 B — 원인을 답한다(CompositionObserver)\n" +
                        "override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {\n" +
                        "    // \"이 스코프가 counterA 때문에 무효화됨\"\n" +
                        "}",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val rows = listOf(
                Triple("답하는 질문", "몇 번 리컴포즈됐는가", "왜 리컴포즈됐는가"),
                Triple("계측 위치", "관찰하려는 Composable 마다 삽입", "컴포지션 하나에 관찰자 1개 부착"),
                Triple("원인 규명", "코드를 읽어 개발자가 추론", "원인 상태 객체를 런타임이 직접 전달"),
                Triple("API 안정성", "SideEffect — stable", "ExperimentalComposeRuntimeApi — opt-in 필요")
            )
            rows.forEach { (aspect, sideEffect, observer) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFEDE7F6), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = aspect,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5E35B1),
                        modifier = Modifier.weight(0.24f)
                    )
                    Text(text = sideEffect, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.38f))
                    Text(text = observer, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.38f))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeRuntimeApi::class)
@Composable
private fun SnapshotObserverCard() {
    val ring = remember { EventRing(32) }
    val registry = remember { StateNameRegistry() }
    val counterC = remember { mutableIntStateOf(0).also { registry.register(it, "counterC") } }

    var observing by remember { mutableStateOf(false) }
    val logLines = remember { mutableStateListOf<String>() }

    DisposableEffect(observing) {
        if (!observing) return@DisposableEffect onDispose { }

        val handles = ArrayList<ObserverHandle>(3)
        // ① 실험 API — 스냅샷 생성 자체에 개입해 read/write 옵저버를 주입한다.
        handles.add(Snapshot.observeSnapshots(SnapshotTraceObserver(registry, ring)))
        // ② stable API — 전역 스냅샷의 쓰기만 본다.
        handles.add(
            Snapshot.registerGlobalWriteObserver { target ->
                registry.nameOf(target)?.let { ring.add("🌐 registerGlobalWriteObserver — $it") }
            }
        )
        // ③ stable API — 변경이 전역에 적용(apply)되는 시점만 본다.
        handles.add(
            Snapshot.registerApplyObserver { changed, _ ->
                for (target in changed) {
                    registry.nameOf(target)?.let { ring.add("📬 registerApplyObserver — $it") }
                }
            }
        )
        onDispose { handles.forEach { it.dispose() } }
    }

    fun publish() {
        logLines.clear()
        logLines.addAll(ring.latestFirst())
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "짝이 되는 축 — Snapshot 관찰 API",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CompositionObserver 가 컴포지션을 보는 반면, Snapshot 관찰 API 는 상태 계층 자체를 봅니다. " +
                        "중요한 것은 이 API 들의 커버리지가 서로 겹치지 않고 상보적이라는 점입니다 — 아래 세 API 를 " +
                        "동시에 붙여 두고, 쓰기 경로를 바꿔가며 어느 것이 발화하는지 직접 대조해 보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        observing = !observing
                        if (!observing) publish()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (observing) Color(0xFFD32F2F) else Color(0xFF388E3C)
                    )
                ) {
                    Text(
                        text = if (observing) "관찰 중지" else "관찰 시작",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = { publish() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(text = "로그 새로고침", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 명시적 스냅샷 없이 전역 스냅샷에 직접 쓴다(일반적인 onClick 경로).
                        counterC.intValue++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text(text = "전역에 직접 쓰기", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        // 새 mutable 스냅샷을 만들어 그 안에서 쓴다.
                        // 컴포지션도 이렇게 자기 스냅샷 안에서 진행되므로, 이 버튼이 "컴포지션 내부 쓰기"의 관측 가능한 대역이다.
                        Snapshot.withMutableSnapshot { counterC.intValue++ }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3949AB))
                ) {
                    Text(text = "스냅샷 안에서 쓰기", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // 같은 값을 다시 대입 — 값이 동등하면 setter 가 실제 쓰기를 건너뛰므로
                    // 어떤 관찰 API 에도 잡히지 않는다(벽 ①).
                    counterC.intValue = counterC.intValue
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9A825))
            ) {
                Text(text = "같은 값 쓰기 (아무 로그도 남지 않음)", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "counterC", fontSize = 12.sp, color = Color(0xFF00897B))
                Text(
                    text = "value = ${counterC.intValue}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF00897B)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "세 버튼을 차례로 눌러 로그를 비교해 보세요. 실기기 실측 결과는 다음과 같습니다 — " +
                        "전역에 직접 쓰면 stable 2종(globalWrite·apply)만 발화하고 observeSnapshots 는 침묵합니다. " +
                        "반대로 스냅샷 안에서 쓰면 globalWrite 가 침묵하고 observeSnapshots 가 발화합니다. " +
                        "즉 어느 하나로 모든 쓰기를 볼 수 없고, 둘을 합쳐야 전체가 됩니다. " +
                        "그리고 \"같은 값 쓰기\"는 어느 쪽에도 잡히지 않습니다(벽 ①).",
                fontSize = 11.sp,
                color = Color(0xFF6D4C41),
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "이 차이가 중요한 이유: 컴포지션도 자기 스냅샷 안에서 진행됩니다. 그래서 컴포지션 도중 " +
                        "일어난 상태 쓰기는 전역 write 옵저버에 원리상 잡히지 않고, observeSnapshots 만이 도달합니다" +
                        "(벽 ③). \"스냅샷 안에서 쓰기\" 버튼이 그 상황의 관측 가능한 대역입니다.",
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF212121), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (logLines.isEmpty()) {
                    Text(
                        text = "(로그 없음 — 관찰 시작 후 버튼을 눌러보세요)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF9E9E9E)
                    )
                } else {
                    logLines.take(10).forEach { line ->
                        Text(
                            text = line,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFC5E1A5),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "발화 매트릭스 (실기기 계측 결과)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // 헤더 + 실측 행. 값은 계측 테스트로 확인한 것이며 추정이 아니다.
            MatrixRow("관찰 API", "전역 직접", "스냅샷 안", isHeader = true)
            MatrixRow("registerGlobalWriteObserver", "발화", "침묵")
            MatrixRow("registerApplyObserver", "발화", "발화")
            MatrixRow("observeSnapshots (write)", "침묵", "발화")
            Spacer(modifier = Modifier.height(8.dp))

            val rows = listOf(
                Triple("observeSnapshots", "실험", "스냅샷 생성에 개입해 read/write 옵저버 주입 — 새 스냅샷이 만들어질 때만 관여하므로 전역 직접 쓰기는 못 본다"),
                Triple("registerGlobalWriteObserver", "stable", "전역 스냅샷의 쓰기만 — 컴포지션 등 별도 스냅샷 안의 쓰기는 원리상 안 보인다"),
                Triple("registerApplyObserver", "stable", "변경이 전역에 적용되는 커밋 시점 — 개별 쓰기 단위가 아니라 양쪽 경로 모두에서 발화한다")
            )
            rows.forEach { (api, stability, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = api,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(0.32f)
                    )
                    Text(text = stability, fontSize = 9.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.12f))
                    Text(text = note, fontSize = 9.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.56f))
                }
            }
        }
    }
}

@Composable
private fun MatrixRow(api: String, direct: String, inSnapshot: String, isHeader: Boolean = false) {
    val background = if (isHeader) Color(0xFFBBDEFB) else Color(0xFFF5F5F5)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = api,
            fontSize = 9.sp,
            fontFamily = if (isHeader) FontFamily.Default else FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF1565C0),
            modifier = Modifier.weight(0.54f)
        )
        listOf(direct, inSnapshot).forEach { cell ->
            Text(
                text = cell,
                fontSize = 9.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isHeader -> Color(0xFF1565C0)
                    cell == "발화" -> Color(0xFF2E7D32)
                    else -> Color(0xFFC62828)
                },
                modifier = Modifier.weight(0.23f)
            )
        }
    }
}

@Composable
private fun LimitationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "관찰 API 의 한계 — 완전한 로깅은 원리상 불가능하다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "관찰 API 로 디버깅 도구를 만들려 하면 네 가지 벽에 차례로 부딪힙니다. 세 개는 우회할 수 있고 " +
                        "하나는 설계상 막혀 있습니다. 이걸 모르고 시작하면 \"내 로거가 변경을 놓친다\"를 버그로 오해하게 됩니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val walls = listOf(
                Triple(
                    "① 동일 값 쓰기는 보이지 않는다",
                    "설계상 불가",
                    "값이 동등하면 setter 가 실제 쓰기를 건너뛰므로 write 옵저버 자체가 호출되지 않는다. 위 카드에서 실제로 확인 가능."
                ),
                Triple(
                    "② 이전 값을 복구할 수 없다",
                    "우회 불가",
                    "이전 값을 담은 StateRecord 필드가 internal 이고 레코드가 재사용되므로, \"무엇에서 무엇으로 바뀌었나\"는 직접 캐싱해야 한다."
                ),
                Triple(
                    "③ 컴포지션 중 쓰기가 안 보인다",
                    "observeSnapshots 로 우회",
                    "컴포지션은 격리된 스냅샷 안에서 진행되므로 전역 write 옵저버에 잡히지 않는다. 스냅샷 생성에 개입하는 실험 API 만이 경로."
                ),
                Triple(
                    "④ 상태·스코프에 이름이 없다",
                    "직접 등록으로 우회",
                    "콜백이 넘겨주는 것은 객체뿐이라 그대로는 @1387209841 로만 보인다. 이 예제가 StateNameRegistry 로 이름을 붙인 이유."
                )
            )
            walls.forEach { (title, verdict, note) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFC62828)
                        )
                        Text(text = verdict, fontSize = 9.sp, color = Color(0xFF757575))
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), lineHeight = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⚠️ 콜백에서 지켜야 할 계약",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "관찰자 콜백은 컴포지션이 진행되는 도중, 그리고 write 옵저버의 경우 전역 스냅샷 락을 잡은 채 " +
                        "실행됩니다. 여기서 스냅샷 상태에 쓰면 컴포지션 중 쓰기가 되어 무한 리컴포지션으로 번지고, " +
                        "무거운 작업을 하면 락을 잡은 채 앱 전체를 멈춥니다.",
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            CodeBlock(
                code = "// ❌ 콜백에서 스냅샷 상태에 직접 쓰기 — 컴포지션 중 쓰기 → 무한 리컴포지션\n" +
                        "override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {\n" +
                        "    logLines.add(\"...\")   // mutableStateListOf 에 쓰기\n" +
                        "}\n\n" +
                        "// ✅ 사전 할당 링버퍼에 O(1) 적재만 하고, 화면 반영은 안전한 시점에\n" +
                        "override fun onScopeInvalidated(scope: RecomposeScope, value: Any?) {\n" +
                        "    val name = registry.nameOf(value) ?: return   // 필터도 O(1)\n" +
                        "    ring.add(\"...\")                                // 할당 없는 고정 배열\n" +
                        "}\n" +
                        "// 클릭 핸들러 등 컴포지션 밖에서: logLines.addAll(ring.latestFirst())",
                borderColor = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "CompositionObserver 는 onScopeInvalidated(scope, value) 로 \"어떤 스코프가 어떤 상태 때문에 무효화됐는가\"를 알려준다 — 횟수만 세는 SideEffect 계측과는 답하는 질문이 다르다",
                "부착은 currentComposer.composition 에 setObserver 로 하며, 반환 handle 이 nullable 이라 이미 관찰자가 붙어 있으면 null 이 온다",
                "관찰자는 컴포지션 하나 전체에 붙으므로 화면의 모든 스코프 이벤트가 들어온다 — 실사용에는 관심 상태만 남기는 필터가 사실상 필수다",
                "Snapshot 관찰 API 3종은 커버리지가 상보적이다 — 전역 직접 쓰기는 registerGlobalWriteObserver 만, 스냅샷 안의 쓰기는 observeSnapshots 만 본다(registerApplyObserver 는 양쪽 커밋을 모두 본다)",
                "컴포지션은 자기 스냅샷 안에서 진행되므로 컴포지션 중 쓰기는 전역 write 옵저버에 원리상 안 잡히고, observeSnapshots 가 유일한 경로다",
                "동일한 값을 다시 쓰면 쓰기 자체가 일어나지 않아 어떤 관찰 API 에도 잡히지 않는다 — 완전한 변경 로깅은 설계상 불가능하다",
                "콜백은 컴포지션 도중·전역 스냅샷 락 아래에서 실행되므로, 사전 할당 링버퍼에 O(1) 적재만 하고 화면 반영은 컴포지션 밖에서 해야 한다"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 15.sp
        )
    }
}
