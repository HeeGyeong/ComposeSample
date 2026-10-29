package com.example.composesample.presentation.example.component.architecture.pattern.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun MoleculePresenterExampleUI(onBackEvent: () -> Unit) {
    val viewModel: MoleculePresenterViewModel = koinViewModel()
    val running by viewModel.running.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        MainHeader(title = "Molecule 프리젠터", onBackIconClicked = onBackEvent)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { TwoPresentersCard(viewModel) }
            item { RecompositionModeCard(viewModel, running) }
            item { SharingCard(viewModel, running) }
            item { MissingClockCard(viewModel, running) }
            item { SummaryCard() }
        }
    }
}

// ==================== 1. 같은 이벤트, 두 프리젠터 ====================

private val QueryPresets = listOf("" to "전체", "pixel" to "pixel", "pro" to "pro", "case" to "case")

@Composable
private fun TwoPresentersCard(viewModel: MoleculePresenterViewModel) {
    val flowModel by viewModel.flowModel.collectAsStateWithLifecycle()
    val moleculeModel by viewModel.moleculeModel.collectAsStateWithLifecycle()
    val flowLog by viewModel.flowLog.collectAsStateWithLifecycle()
    val moleculeLog by viewModel.moleculeLog.collectAsStateWithLifecycle()
    val flowSearches by viewModel.flowCatalog.searches.collectAsStateWithLifecycle()
    val moleculeSearches by viewModel.moleculeCatalog.searches.collectAsStateWithLifecycle()
    val flowComputations by viewModel.flowComputations.collectAsStateWithLifecycle()
    val moleculeCompositions by viewModel.moleculeCompositions.collectAsStateWithLifecycle()

    MoleculeSectionCard(title = "1. 같은 이벤트, 두 프리젠터") {
        MoleculeBody(
            "버튼 하나가 같은 이벤트를 두 프리젠터에 보낸다. 왼쪽은 입력마다 MutableStateFlow 를 두고 combine·flatMapLatest 로 " +
                "엮은 Flow 프리젠터, 오른쪽은 같은 일을 remember·LaunchedEffect·평범한 대입으로 쓴 @Composable 프리젠터를 " +
                "launchMolecule 로 StateFlow 로 바꾼 것이다(검색은 400ms 걸리는 가짜 카탈로그)."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            QueryPresets.forEach { (query, label) ->
                FilterChip(
                    selected = moleculeModel.query == query,
                    onClick = { viewModel.onEvent(CatalogEvent.QueryChanged(query)) },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = { viewModel.onEvent(CatalogEvent.ToggleInStock) }) {
                Text(if (moleculeModel.inStockOnly) "재고만 끄기" else "재고만", fontSize = 12.sp)
            }
            OutlinedButton(onClick = { viewModel.onEvent(CatalogEvent.Refresh) }) { Text("새로고침", fontSize = 12.sp) }
            OutlinedButton(onClick = { viewModel.clearLogs() }) { Text("로그 지우기", fontSize = 12.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PresenterPanel(
                title = "Flow(combine)",
                model = flowModel,
                counts = "검색 ${flowSearches}회 · 모델 계산 ${flowComputations}회",
                first = viewModel.flowFirst,
                log = flowLog,
                modifier = Modifier.weight(1f)
            )
            PresenterPanel(
                title = "Molecule",
                model = moleculeModel,
                counts = "검색 ${moleculeSearches}회 · 컴포지션 ${moleculeCompositions}회",
                first = viewModel.moleculeFirst,
                log = moleculeLog,
                modifier = Modifier.weight(1f)
            )
        }
        val same = flowModel == moleculeModel
        MoleculeNote(
            (if (same) "✓ 지금 두 모델이 같다" else "… 지금 두 모델이 다르다(한쪽이 아직 검색 중일 수 있다)") +
                " · 실측: 두 쪽 전이 순서가 같고, 검색어를 바꾼 첫 모델은 둘 다 '새 검색어 + 이전 결과'다 " +
                "(Molecule 이라고 중간 모델이 사라지지 않는다 — 결과에 검색어를 함께 담아 비교해야 없어진다)."
        )
        MoleculeNote(
            "'생성 직후' 는 ViewModel 이 만들어진 순간의 .value. Molecule 은 첫 컴포지션을 launchMolecule 호출 안에서 끝내 " +
                "디스패처와 상관없이 첫 결과가 바로 있다. Flow 쪽은 stateIn 에 자리표시자(검색 중 아님 · 0개)를 줘야 하고, " +
                "그게 보이는지는 디스패처에 달렸다 — viewModelScope(Main.immediate)에서는 메인 스레드에서 바로 덮여 안 보였고, " +
                "단위 테스트(StandardTestDispatcher)에서는 자리표시자가 첫 값이었다."
        )
        MoleculeMono(
            "// Flow\n" +
                "combine(query, refresh) { q, _ -> q }\n" +
                "  .flatMapLatest { q -> flow { emit(null); emit(search(q)) } }\n" +
                "combine(query, inStock, results, ::modelOf)\n" +
                "  .stateIn(scope, Eagerly, Placeholder)\n\n" +
                "// Molecule\n" +
                "var results by remember { mutableStateOf<List?>(null) }\n" +
                "LaunchedEffect(query, refresh) {\n" +
                "  results = null; results = search(query)\n" +
                "}\n" +
                "return modelOf(query, inStock, results)"
        )
    }
}

@Composable
private fun PresenterPanel(
    title: String,
    model: CatalogModel,
    counts: String,
    first: String,
    log: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = describeCatalogModel(model), fontSize = 12.sp, color = Color(0xFF263238))
        Text(
            text = if (model.loading) "…" else model.items.joinToString("\n") { it.name }.ifEmpty { "(결과 없음)" },
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = Color(0xFF455A64)
        )
        MoleculeNote(counts)
        MoleculeNote("생성 직후: $first")
        Text(
            text = log.joinToString("\n").ifEmpty { "(로그 없음)" },
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF37474F)
        )
    }
}

// ==================== 2. RecompositionMode ====================

@Composable
private fun RecompositionModeCard(viewModel: MoleculePresenterViewModel, running: Boolean) {
    val results by viewModel.burstResults.collectAsStateWithLifecycle()
    MoleculeSectionCard(title = "2. RecompositionMode — 언제 다시 컴포즈하나") {
        MoleculeBody(
            "스냅샷 상태 하나를 10번 바꾸고, 그 값을 돌려주기만 하는 프리젠터가 몇 번 방출하는지 센다(emitter 판 — 같은 값도 센다). " +
                "Immediate 는 바뀔 때마다 바로, ContextClock 은 컨텍스트의 프레임 시계(여기서는 Choreographer)가 " +
                "프레임을 줄 때 재구성한다. 첫 값(0)은 launchMolecule 안에서 동기로 나오므로 세지 않는다."
        )
        MoleculeNote(
            "실측(에뮬레이터 debug·release 각 2회 이상): 중단 없이 10번 쓰면 두 모드 다 1번 — 메인 스레드가 비어야 apply 알림이 간다. " +
                "yield 사이로 쓰면 Immediate 10 · ContextClock 은 대개 1(한 프레임 안 — 프레임 경계에 걸려 4번으로 나뉜 적이 한 번 있다). " +
                "프레임마다 쓰면 둘 다 10. 다른 스레드에서 쓰면 ContextClock 1, Immediate 는 1~3번에 중간값도 매번 달랐다 " +
                "(같은 10 이 두 번 나온 적도 있다 — StateFlow 판은 같은 값을 거른다)."
        )
        Button(onClick = { viewModel.runBurstExperiments() }, enabled = !running) {
            Text("4가지 쓰기 × 2가지 모드 측정", fontSize = 12.sp)
        }
        if (results.isNotEmpty()) {
            MoleculeMono(
                results.joinToString("\n") { r ->
                    "${r.pattern.label} · ${r.mode.label}\n  ${describeBurst(r)}"
                }
            )
        }
    }
}

// ==================== 3. 공유 vs 수집마다 ====================

@Composable
private fun SharingCard(viewModel: MoleculePresenterViewModel, running: Boolean) {
    val results by viewModel.sharingResults.collectAsStateWithLifecycle()
    MoleculeSectionCard(title = "3. launchMolecule(핫) vs moleculeFlow(콜드)") {
        MoleculeBody(
            "같은 프리젠터를 두 곳에서 수집하고 검색어를 한 번 바꾼다. launchMolecule 은 컴포지션 하나를 스코프에 띄워 " +
                "StateFlow 로 나눠 주고, moleculeFlow 는 수집할 때마다 컴포지션을 새로 만든다 — 프리젠터 안의 " +
                "LaunchedEffect(검색)도 수집기 수만큼 돈다. 컴포지션 5회 = 첫 값 · 첫 결과 · 새 검색어 · 검색 중 · 새 결과."
        )
        Button(onClick = { viewModel.runSharingExperiments() }, enabled = !running) {
            Text("두 수집기로 측정", fontSize = 12.sp)
        }
        if (results.isNotEmpty()) {
            MoleculeMono(results.joinToString("\n") { describeSharing(it) })
        }
    }
}

// ==================== 4. 시계 없는 ContextClock ====================

@Composable
private fun MissingClockCard(viewModel: MoleculePresenterViewModel, running: Boolean) {
    val results by viewModel.clockResults.collectAsStateWithLifecycle()
    MoleculeSectionCard(title = "4. 시계 없는 ContextClock") {
        MoleculeBody(
            "viewModelScope 는 Dispatchers.Main.immediate 뿐이라 MonotonicFrameClock 이 없다. 그 구성의 스코프에 " +
                "ContextClock 을 띄우면 무엇이 어디로 가는지 본다. 실험 스코프에는 CoroutineExceptionHandler 를 달았다 — " +
                "viewModelScope 에는 처리기가 없어 같은 실패가 앱 크래시가 된다."
        )
        MoleculeNote(
            "실측: 재구성 루프가 시작하자마자 시계를 찾다 실패하고(처리기가 받는 진짜 원인), 그 정리 과정에서 컴포지션이 " +
                "닫혀 launchMolecule 의 setContent 가 'The composition is disposed' 를 던진다 — 호출자가 보는 예외는 원인이 아니다."
        )
        Button(onClick = { viewModel.runClockExperiments() }, enabled = !running) {
            Text("시계 없이 / 있게 띄워 보기", fontSize = 12.sp)
        }
        if (results.isNotEmpty()) {
            MoleculeMono(
                results.joinToString("\n\n") { r ->
                    (if (r.withClock) "+ AndroidUiDispatcher.Main" else "시계 없음") + "\n" +
                        "  처리기: ${r.handlerMessage ?: "-"}\n" +
                        "  호출자: ${r.thrownMessage ?: "-"}\n" +
                        "  첫 값: ${r.value ?: "-"}"
                }
            )
        }
    }
}

// ==================== 5. 정리 ====================

@Composable
private fun SummaryCard() {
    MoleculeSectionCard(title = "5. 정리") {
        MoleculeBody(
            "• 프리젠터를 @Composable 로 쓰면 연산자 사슬 대신 remember·LaunchedEffect·if 로 상태를 만든다 — 값을 돌려주는 " +
                "컴포저블이라 바뀔 때마다 본문 전체가 다시 돈다\n" +
                "• launchMolecule 은 첫 컴포지션을 호출 안에서 동기로 끝내 StateFlow 의 첫 값을 만든다(stateIn 처럼 자리표시자를 정하지 않는다)\n" +
                "• 화면용은 ContextClock + 프레임 시계(AndroidUiDispatcher.Main), 테스트·백그라운드는 Immediate\n" +
                "• 여러 곳에서 볼 상태는 launchMolecule(핫) 하나를 나눠 쓴다 — moleculeFlow 는 수집기마다 따로 돈다\n" +
                "• 테스트는 moleculeFlow(Immediate) + Turbine 이 정석이지만, Android 모듈의 JVM 단위 테스트에서는 Compose runtime 이 " +
                "android.os.Trace 를 불러 'not mocked' 로 실패했다(실측) — 계측 테스트·순수 JVM 모듈·returnDefaultValues 중 하나가 필요하다"
        )
    }
}

// ==================== 공통 조각 ====================

@Composable
private fun MoleculeSectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MoleculeBody(text: String) {
    Text(text = text, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF37474F))
}

@Composable
private fun MoleculeNote(text: String) {
    Text(text = text, fontSize = 11.sp, lineHeight = 16.sp, color = Color(0xFF607D8B))
}

@Composable
private fun MoleculeMono(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}
