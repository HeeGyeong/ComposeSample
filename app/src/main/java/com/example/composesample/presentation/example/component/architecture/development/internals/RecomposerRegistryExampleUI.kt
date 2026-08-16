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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.tooling.CompositionRegistrationObserver
import androidx.compose.runtime.tooling.ObservableComposition
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// ============================================================================
// 관찰 인프라 — CompositionObserverExampleUI 와 동일 관례(RegistryEventRing).
//
// CompositionRegistrationObserver 콜백도 CompositionObserver 와 마찬가지로 컴포지션 기계장치가
// 동작하는 도중 호출될 수 있다. 콜백 안에서 스냅샷 상태에 바로 쓰면 컴포지션 중 쓰기가 되어
// 무한 리컴포지션으로 번질 수 있으므로, 사전 할당 링버퍼에 O(1) 적재만 하고 화면 반영은
// 클릭 핸들러 등 안전한 시점에 따로 한다.
// ============================================================================

private class RegistryEventRing(private val capacity: Int = 64) {
    private val buffer = arrayOfNulls<String>(capacity)
    private var writeIndex = 0
    private var totalCount = 0

    val count: Int get() = totalCount

    fun add(line: String) {
        buffer[writeIndex] = line
        writeIndex = (writeIndex + 1) % capacity
        totalCount++
    }

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
 * "컴포지션이 등록/해제되는 순간"을 기록하는 CompositionRegistrationObserver 구현체.
 *
 * 부착 즉시 이미 등록되어 있던 컴포지션들에 대해서도 onCompositionRegistered 가 재생된다
 * (미래 이벤트만 오는 게 아니다 — 실기기 계측으로 확인된 사실, 아래 RegistrationLogCard 참고).
 */
@OptIn(ExperimentalComposeRuntimeApi::class)
private class RegistryEventObserver(
    private val ring: RegistryEventRing,
    private val registeredCount: () -> Unit,
    private val unregisteredCount: () -> Unit
) : CompositionRegistrationObserver {

    override fun onCompositionRegistered(composition: ObservableComposition) {
        ring.add("🟢 onCompositionRegistered — @${Integer.toHexString(System.identityHashCode(composition))}")
        registeredCount()
    }

    override fun onCompositionUnregistered(composition: ObservableComposition) {
        ring.add("🔴 onCompositionUnregistered — @${Integer.toHexString(System.identityHashCode(composition))}")
        unregisteredCount()
    }
}

// ============================================================================
// UI
// ============================================================================

@Composable
fun RecomposerRegistryExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Recomposer 레지스트리 관찰",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { LiveRegistryCard() }
            item { RegistrationLogCard() }
            item { ChangeCountPitfallCard() }
            item { ComparisonCard() }
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
                text = "Recomposer 레지스트리란",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "지금까지의 컴포지션 관찰 축은 전부 \"컴포지션 하나 안\"을 들여다봤습니다. " +
                        "CompositionObserver 는 그 안의 스코프가 왜 무효화됐는지, SlotTreeInspector 는 그 안의 " +
                        "슬롯 구조가 어떻게 생겼는지를 답합니다. Recomposer 레지스트리는 한 단계 위 레벨입니다 — " +
                        "\"지금 이 프로세스에 컴포지션이 몇 개나 살아있고, 언제 새로 생겼다가 언제 사라지는가\"를 " +
                        "컴포지션 자체가 아니라 그것들을 구동하는 Recomposer 쪽에서 관찰합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// ① 지금 실행 중인 Recomposer 전체 조회 (opt-in 불필요)\n" +
                        "val recomposers: StateFlow<Set<RecomposerInfo>> = Recomposer.runningRecomposers\n\n" +
                        "// ② 특정 RecomposerInfo 에 등록/해제 관찰자 부착\n" +
                        "@OptIn(ExperimentalComposeRuntimeApi::class)\n" +
                        "val handle = recomposerInfo.observe(object : CompositionRegistrationObserver {\n" +
                        "    override fun onCompositionRegistered(composition: ObservableComposition) { /* ... */ }\n" +
                        "    override fun onCompositionUnregistered(composition: ObservableComposition) { /* ... */ }\n" +
                        "})\n" +
                        "// ⚠️ handle 은 nullable — 이미 관찰자가 붙어 있으면 null",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(10.dp))

            val rows = listOf(
                Triple("Recomposer.runningRecomposers", "StateFlow<Set<RecomposerInfo>>", "프로세스 전체의 실행 중 Recomposer 집합"),
                Triple("RecomposerInfo.observe(observer)", "CompositionObserverHandle?", "그 Recomposer 아래 컴포지션 등록/해제 관찰"),
                Triple("RecomposerInfo.hasPendingWork", "Boolean", "처리할 리컴포지션 작업이 남아있는가"),
                Triple("RecomposerInfo.state", "Flow<Recomposer.State>", "Idle/PendingWork 등 생명주기 상태")
            )
            rows.forEach { (api, type, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = api,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(0.42f)
                    )
                    Text(text = type, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF757575), modifier = Modifier.weight(0.3f))
                    Text(text = note, fontSize = 9.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.28f))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeRuntimeApi::class)
@Composable
private fun LiveRegistryCard() {
    val recomposers by Recomposer.runningRecomposers.collectAsState()
    val info = recomposers.firstOrNull()
    val hasPendingWork = info?.hasPendingWork ?: false
    val state by (info?.state?.collectAsState(initial = Recomposer.State.Inactive)
        ?: remember { mutableStateOf(Recomposer.State.Inactive) })

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실동작 — 지금 이 화면의 Recomposer",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "runningRecomposers 를 그대로 구독한 값입니다. 이 예제 화면 하나가 앱 전체와 " +
                        "같은 Recomposer 아래에서 돌기 때문에 보통 1개로 채워집니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "실행 중 Recomposer 수", fontSize = 12.sp, color = Color(0xFF2E7D32))
                Text(
                    text = "${recomposers.size}개",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "hasPendingWork", fontSize = 12.sp, color = Color(0xFF1565C0))
                Text(
                    text = if (hasPendingWork) "true" else "false",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "state", fontSize = 12.sp, color = Color(0xFF6A1B9A))
                Text(
                    text = state.name,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeRuntimeApi::class)
@Composable
private fun RegistrationLogCard() {
    val ring = remember { RegistryEventRing() }
    var registeredTotal by remember { mutableIntStateOf(0) }
    var unregisteredTotal by remember { mutableIntStateOf(0) }
    var observing by remember { mutableStateOf(false) }
    val logLines = remember { mutableStateListOf<String>() }
    val itemIds = remember { mutableStateListOf<Int>().apply { addAll(0 until 8) } }
    var nextId by remember { mutableIntStateOf(8) }

    val recomposers by Recomposer.runningRecomposers.collectAsState()

    DisposableEffect(observing) {
        if (!observing) return@DisposableEffect onDispose { }
        val info = recomposers.firstOrNull()
        val handle = info?.observe(
            RegistryEventObserver(
                ring = ring,
                registeredCount = { registeredTotal++ },
                unregisteredCount = { unregisteredTotal++ }
            )
        )
        onDispose { handle?.dispose() }
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
                text = "실동작 — 컴포지션 등록/해제 로그",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "관찰을 시작하면 이미 화면에 떠 있는 LazyColumn item 들의 등록 이벤트가 먼저 한꺼번에 " +
                        "재생됩니다(미래 이벤트만 오는 게 아닙니다). 그 다음 아래 목록에 항목을 추가/제거하면 " +
                        "각 item 이 별도 서브컴포지션이라 등록/해제가 실시간으로 기록됩니다.",
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
                        registeredTotal = 0
                        unregisteredTotal = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF757575))
                ) {
                    Text(text = "지우기", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        itemIds.add(nextId)
                        nextId++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                ) {
                    Text(text = "항목 추가", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { if (itemIds.isNotEmpty()) itemIds.removeAt(itemIds.lastIndex) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text(text = "항목 제거", color = Color.White, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
                    .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(itemIds, key = { it }) { id ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "item #$id — 별도 서브컴포지션",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00695C)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

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
                    text = "등록 ${registeredTotal} / 해제 ${unregisteredTotal}",
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
                        text = "(로그 없음 — 관찰 시작 후 항목을 추가/제거하거나 \"로그 새로고침\"을 눌러보세요)",
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
        }
    }
}

@OptIn(ExperimentalComposeRuntimeApi::class)
@Composable
private fun ChangeCountPitfallCard() {
    val recomposers by Recomposer.runningRecomposers.collectAsState()
    var manualClicks by remember { mutableIntStateOf(0) }
    var snapshot by remember { mutableIntStateOf(0) }
    var snapshotChangeCount by remember { mutableStateOf<Long?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "함정 — changeCount 는 전역 리컴포지션 카운터가 아니다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RecomposerInfo.changeCount 라는 이름만 보면 \"리컴포지션이 몇 번 일어났는가\"를 셀 " +
                        "것 같지만, 실기기 계측 결과 이 화면에서 상태를 바꾸고 스크롤해도 값이 계속 0 이었습니다. " +
                        "직접 눌러서 대조해 보세요 — 왼쪽(수동 카운터)은 오르지만 오른쪽(changeCount)은 그대로입니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    manualClicks++
                    snapshot++
                    snapshotChangeCount = recomposers.firstOrNull()?.changeCount
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Text(text = "상태 변경 + changeCount 읽기", color = Color.White, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = "수동 카운터 (SideEffect 계열)", fontSize = 10.sp, color = Color(0xFF2E7D32))
                    Text(
                        text = "$manualClicks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF2E7D32)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = "RecomposerInfo.changeCount", fontSize = 10.sp, color = Color(0xFFC62828))
                    Text(
                        text = snapshotChangeCount?.toString() ?: "-",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC62828)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "이 프로젝트가 Compose 1.11.1 로 실측한 결과입니다 — 클릭·스크롤 후에도 계속 0 으로 " +
                        "관측됐습니다. \"몇 번 리컴포즈됐는가\"가 필요하면 여전히 SideEffect 수동 계측이나 " +
                        "CompositionObserver 를 써야 하고, 이 필드를 전역 리컴포지션 카운터로 서술하면 틀립니다.",
                fontSize = 11.sp,
                color = Color(0xFF6D4C41),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "세 예제가 보는 층위가 다르다",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "같은 컴포지션 런타임 내부를 보는 세 예제지만 관찰 대상의 층위가 다릅니다 — " +
                        "아래로 갈수록 보는 범위가 넓어집니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val rows = listOf(
                Triple("CompositionObserver", "한 컴포지션 안의 스코프", "이 스코프가 왜 무효화됐는가"),
                Triple("SlotTreeInspector", "한 컴포지션 안의 슬롯 구조", "지금 어떤 그룹 트리 모양인가"),
                Triple("Recomposer 레지스트리 (이 예제)", "프로세스의 컴포지션 전체", "몇 개가 살아있고 언제 생겼다 사라지는가")
            )
            rows.forEach { (name, scope, question) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "범위: $scope", fontSize = 10.sp, color = Color(0xFF757575))
                    Text(text = "질문: $question", fontSize = 10.sp, color = Color(0xFF757575))
                }
            }
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
                text = "주의할 점",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))

            val notes = listOf(
                Triple(
                    "부착 즉시 과거 컴포지션도 재생된다",
                    "동작 확인됨",
                    "observe() 는 미래 이벤트만 주지 않는다 — 부착 시점에 이미 등록돼 있던 컴포지션 전부에 대해 onCompositionRegistered 가 즉시 호출된다. 위 로그 카드에서 \"관찰 시작\" 직후 이미 이벤트가 여러 줄 쌓이는 것이 그 증거."
                ),
                Triple(
                    "observe() 의 반환은 nullable",
                    "CompositionObserver 와 동일 함정",
                    "이미 다른 관찰자가 붙어 있으면 null 을 반환한다. dispose() 호출 전에 반드시 null 체크(?.dispose())가 필요하다."
                ),
                Triple(
                    "changeCount 는 전역 리컴포지션 카운터가 아니다",
                    "실측으로 확정",
                    "이름만 보고 \"몇 번 리컴포즈됐는가\"로 쓰면 틀린다. 위 함정 카드에서 클릭·스크롤 후에도 0 으로 유지되는 것을 직접 확인할 수 있다."
                ),
                Triple(
                    "관찰 대상은 컴포지션이 아니라 RecomposerInfo",
                    "부착 지점 주의",
                    "observe() 는 Composition.setObserver() 처럼 특정 화면 하나가 아니라, 그 Recomposer 가 구동하는 모든 컴포지션(서브컴포지션 포함)의 등록/해제를 한꺼번에 받는다."
                )
            )
            notes.forEach { (title, verdict, note) ->
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
                "Recomposer.runningRecomposers 는 opt-in 없이 프로세스 전체의 실행 중 Recomposer 를 StateFlow<Set<RecomposerInfo>> 로 조회한다",
                "RecomposerInfo.observe(CompositionRegistrationObserver) 는 @OptIn(ExperimentalComposeRuntimeApi::class) 가 필요하며, 그 Recomposer 아래 컴포지션의 등록/해제 이벤트를 받는다",
                "부착 즉시 이미 등록된 컴포지션들의 등록 이벤트가 재생된다 — 미래 이벤트만 오는 스트림이 아니다",
                "LazyColumn 의 item 은 각각 별도 서브컴포지션이라 목록에 항목을 추가/제거하거나 스크롤하면 등록/해제가 실시간으로 발생한다",
                "changeCount 는 이름과 달리 \"전역 리컴포지션 횟수\"가 아니다 — 실측 결과 클릭·스크롤 후에도 0 으로 유지됐다",
                "CompositionObserver(한 컴포지션의 스코프 무효화) · SlotTreeInspector(한 컴포지션의 슬롯 구조) · 이 예제(컴포지션 자체의 생성/소멸)는 서로 다른 층위를 본다"
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
