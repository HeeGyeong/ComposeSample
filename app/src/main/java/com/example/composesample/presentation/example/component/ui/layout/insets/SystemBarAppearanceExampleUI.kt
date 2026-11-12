package com.example.composesample.presentation.example.component.ui.layout.insets

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.pow

/**
 * 시스템 바 아이콘 색 선언 예제
 * - 화면마다 `WindowInsetsControllerCompat.isAppearanceLightStatusBars` 를 선언하고, 화면을 떠나면 되돌리는 패턴을
 *   두 가지로 만든다 — ⓐ 들어올 때 지금 값을 저장하고 나갈 때 그 값으로 되돌리는 순진한 방식,
 *   ⓑ 창마다 요청을 쌓아 가장 최근 요청이 이기게 하는 요청 스택(원문: composablehorizons/systembars 1.0.0).
 * - 같은 무대를 즉시 교체 · AnimatedContent · NavDisplay 로 바꿔 가며, 들어오는 화면의 효과와 나가는 화면의
 *   onDispose 가 어느 순서로 도는지와 그 결과 바 값을 기기에서 기록한다.
 * - 다이얼로그는 자기 창을 가진다 — 다이얼로그 안 선언을 어느 창에 거느냐에 따라 뒤 화면 값이 바뀌는지 본다.
 * - 아이콘 색은 다크 모드가 아니라 바 뒤에 실제로 깔린 색으로 골라야 한다 — 그 색을 PixelCopy 로 읽는다.
 * - 라이브러리는 쓰지 않는다. 판정 규칙은 [SystemBarAppearanceLab] 에 있고 단위 테스트가 같은 규칙을 검증한다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 참고.
 */

// ==================== 플랫폼 창 ====================

/** 기기 창을 [BarWindow] 로 — 읽기·쓰기 모두 WindowInsetsControllerCompat */
private class PlatformBarWindow(window: Window) : BarWindow {
    private val controller = WindowCompat.getInsetsController(window, window.decorView)

    override var flags: BarFlags
        get() = BarFlags(
            lightStatus = controller.isAppearanceLightStatusBars,
            lightNavigation = controller.isAppearanceLightNavigationBars
        )
        set(value) {
            controller.isAppearanceLightStatusBars = value.lightStatus
            controller.isAppearanceLightNavigationBars = value.lightNavigation
        }
}

/** 요청의 주인 — 같은 이름이라도 인스턴스마다 다른 요청이다(동일성 비교) */
private class RequestOwner(val label: String)

/** 프로세스 전체에서 창마다 스택 하나 — 이펙트는 메인 스레드에서만 돈다 */
private val barRegistry = BarRequestRegistry()

private fun BarFlags.text(): String =
    "상태바 ${lightStatus.iconLabel()} · 내비 바 ${lightNavigation.iconLabel()}"

private fun stackLabels(window: Window?): List<String> =
    window?.let { barRegistry.stackOf(it) }?.entries?.map { (owner, request) ->
        "${(owner as? RequestOwner)?.label ?: "?"}(${request.statusBar.label})"
    }.orEmpty()

// ==================== 화면 상태 ====================

private enum class StageEventKind { Enter, Leave }

private class StageEvent(val seq: Int, val page: StagePage, val kind: StageEventKind, val detail: String) {
    val text: String
        get() = "#$seq ${page.label} ${if (kind == StageEventKind.Enter) "효과" else "onDispose"} — $detail"
}

/** 한 단계의 결과 */
private class StepResult(
    val page: StagePage,
    val expected: BarFlags,
    val actual: BarFlags?,
    val order: SwapOrder?
) {
    val ok: Boolean get() = actual == expected
}

/** (전환, 선언, 시나리오) 한 번 실행한 결과 */
private class ScenarioRun(
    val transition: TransitionMode,
    val declaration: DeclarationMode,
    val scenario: StageScenario,
    val steps: List<StepResult>
)

private class BarLabState(val window: Window?) {
    /** 화면을 열 때 읽은 값 — 자동 대조는 매 실행 전에 이 값으로 되돌리고, 화면을 떠날 때도 여기로 되돌린다 */
    val original: BarFlags = window?.let { PlatformBarWindow(it).flags } ?: BarFlags(false, false)

    var current by mutableStateOf(original)
        private set
    var stack by mutableStateOf(emptyList<String>())
        private set

    var declaration by mutableStateOf(DeclarationMode.SaveRestore)
    var transition by mutableStateOf(TransitionMode.Animated)
    var page by mutableStateOf(StagePage.Home)
        private set

    /** NavDisplay 무대의 백스택 — 홈이 바닥 */
    val navStack = mutableStateListOf(StagePage.Home)

    val events = mutableStateListOf<StageEvent>()
    private var seq = 0

    /** 지금 컴포지션에 올라 있는 화면 수 — 전환 중에는 둘이다 */
    private val live = mutableStateMapOf<StagePage, Int>()

    var running by mutableStateOf(false)
    var progress by mutableStateOf("")
    val runs = mutableStateListOf<ScenarioRun>()

    /** 홈 하나만 올라 있을 때만 모드를 바꾼다(늦게 도는 onDispose 가 바꾼 뒤의 값을 덮지 않게) */
    val settled: Boolean
        get() = page == StagePage.Home && live.filterValues { it > 0 }.keys == setOf(StagePage.Home)

    fun refresh() {
        current = window?.let { PlatformBarWindow(it).flags } ?: original
        stack = stackLabels(window)
    }

    fun record(page: StagePage, kind: StageEventKind, detail: String) {
        seq += 1
        events.add(StageEvent(seq, page, kind, detail))
        while (events.size > 200) events.removeAt(0)
        live[page] = (live[page] ?: 0) + if (kind == StageEventKind.Enter) 1 else -1
        refresh()
    }

    fun clearLog() = events.clear()

    fun goTo(target: StagePage) {
        page = target
        // NavDisplay: 홈이면 바닥까지 pop, 백스택에 있으면 거기까지 pop, 없으면 push
        val index = navStack.indexOf(target)
        if (index >= 0) {
            while (navStack.size > index + 1) navStack.removeAt(navStack.lastIndex)
        } else {
            navStack.add(target)
        }
    }

    /** NavDisplay 의 뒤로가기 — 무대 백스택만 하나 줄인다 */
    fun navBack() {
        if (navStack.size > 1) {
            navStack.removeAt(navStack.lastIndex)
            page = navStack.last()
        }
    }

    /** 저장·복원이 남긴 값을 지운다 — 스택에 요청이 없을 때만 */
    fun restoreOriginal() {
        val w = window ?: return
        if (barRegistry.stackOf(w) == null) PlatformBarWindow(w).flags = original
        refresh()
    }
}

// ==================== 화면 ====================

@Composable
fun SystemBarAppearanceExampleUI(onBackEvent: () -> Unit) {
    val activity = LocalActivity.current
    val state = remember(activity) { BarLabState(activity?.window) }
    // 가장 먼저 등록한 이펙트라 가장 나중에 정리된다 — 무대 화면들의 onDispose 가 다 돈 뒤 원래 값으로 돌린다
    DisposableEffect(state) {
        onDispose { state.restoreOriginal() }
    }

    // LazyColumn 항목 안의 DisposableEffect 는 스크롤로 사라질 때 onDispose 가 돈다(DisplayCutout 예제 실측) —
    // 선언이 스크롤로 철회되지 않게 모든 카드를 늘 컴포지션에 두는 Column 을 쓴다
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "시스템 바 아이콘 색 선언",
            onBackIconClicked = onBackEvent
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WindowStateCard(state)
            StageCard(state)
            AutoCompareCard(state)
            DialogWindowCard(state)
            ContrastCard(state)
            PitfallCard()
        }
    }
}

// ==================== 1. 지금 이 창 ====================

@Composable
private fun WindowStateCard(state: BarLabState) {
    val configuration = LocalConfiguration.current
    val night = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val targetSdk = LocalView.current.context.applicationInfo.targetSdkVersion
    SectionCard("1. 지금 이 창의 시스템 바") {
        BodyText(
            "아이콘 색은 창의 플래그 하나다 — `isAppearanceLightStatusBars = true` 면 어두운 아이콘(밝은 배경용), " +
                "false 면 밝은 아이콘. 화면이 선언하고 떠날 때 되돌리는 것이 이 예제의 주제다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow("기기", "API ${Build.VERSION.SDK_INT} · targetSdk $targetSdk")
        ResultRow(
            "edge-to-edge",
            if (Build.VERSION.SDK_INT >= 35 && targetSdk >= 35) "강제(API 35+ · targetSdk 35+)" else "강제 아님"
        )
        ResultRow("다크 모드", if (night) "켜짐" else "꺼짐")
        ResultRow("원래 값", state.original.text())
        ResultRow("지금 값", state.current.text())
        ResultRow("요청 스택", state.stack.ifEmpty { listOf("(비어 있음)") }.joinToString(" › "))
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("다시 읽기", Color(0xFF546E7A)) { state.refresh() }
            DemoButton("원래 값으로", Color(0xFF8D6E63), enabled = state.settled && !state.running) {
                state.restoreOriginal()
            }
        }
        CaptionText("'원래 값'은 화면을 열 때 읽었다. 저장·복원이 남긴 값은 '원래 값으로'로 지운다(무대가 홈일 때만).")
    }
}

// ==================== 2. 무대 ====================

private const val STAGE_ANIMATION_MS = 300

@Composable
private fun StageCard(state: BarLabState) {
    SectionCard("2. 화면이 바뀔 때 — 저장·복원 vs 요청 스택") {
        BodyText(
            "무대의 두 화면이 각자 상태바 아이콘 색을 선언한다. 어두운 헤더 화면은 밝은 아이콘, 밝은 사진 화면은 " +
                "어두운 아이콘. 홈은 선언하지 않는다. 화면을 바꾸며 아래 기록에서 들어오는 화면의 효과와 나가는 화면의 " +
                "onDispose 순서, 그리고 실제 상태바 아이콘을 본다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        val modeEnabled = state.settled && !state.running
        ChipRow(
            label = "선언",
            names = DeclarationMode.entries.map { it.label },
            selectedIndex = state.declaration.ordinal,
            enabled = modeEnabled
        ) {
            state.declaration = DeclarationMode.entries[it]
            state.restoreOriginal()
        }
        ChipRow(
            label = "전환",
            names = TransitionMode.entries.map { it.label },
            selectedIndex = state.transition.ordinal,
            enabled = modeEnabled
        ) {
            state.transition = TransitionMode.entries[it]
            state.restoreOriginal()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Stage(state)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StagePage.entries.forEach { target ->
                OutlinedButton(
                    onClick = { state.goTo(target) },
                    enabled = !state.running && state.page != target
                ) { Text(target.label, fontSize = 12.sp) }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val expected = expectedFlags(state.original, state.page)
        ResultRow("보이는 화면", state.page.label)
        ResultRow("기대 상태바", expected.lightStatus.iconLabel())
        ResultRow(
            "실제 상태바",
            state.current.lightStatus.iconLabel() + if (state.current == expected) "  ✓" else "  ✗ 어긋남"
        )
        Spacer(modifier = Modifier.height(6.dp))
        EventLog(state.events.takeLast(8).map { it.text })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton("기록 지우기", Color(0xFF546E7A), enabled = !state.running) { state.clearLog() }
        }
        if (state.transition == TransitionMode.NavDisplay) {
            CaptionText("NavDisplay 무대가 홈이 아니면 시스템 뒤로가기가 무대 백스택을 먼저 하나 줄인다.")
        }
    }
}

@Composable
private fun Stage(state: BarLabState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        when (state.transition) {
            // 한 컴포지션 안에서 그룹이 통째로 바뀐다 — 나가는 화면이 먼저 정리된다
            TransitionMode.Immediate -> key(state.page) { StagePane(state.page, state) }
            // 전환 동안 두 화면이 함께 컴포지션에 있다 — 나가는 화면은 애니메이션이 끝난 뒤 정리된다
            TransitionMode.Animated -> AnimatedContent(
                targetState = state.page,
                transitionSpec = {
                    fadeIn(tween(STAGE_ANIMATION_MS)) togetherWith fadeOut(tween(STAGE_ANIMATION_MS))
                },
                label = "systemBarStage"
            ) { page -> StagePane(page, state) }
            // 실무 내비게이션 컨테이너 — 기본 전환은 페이드(navigation3-ui 1.2.1 소스)
            TransitionMode.NavDisplay -> NavDisplay(
                backStack = state.navStack,
                onBack = { state.navBack() },
                entryProvider = { key -> NavEntry(key) { page -> StagePane(page, state) } }
            )
        }
    }
}

/** 무대 화면 하나 — 선언과 기록을 한 이펙트에서 한다 */
@Composable
private fun StagePane(page: StagePage, state: BarLabState) {
    val owner = remember { RequestOwner(page.label) }
    val window = state.window
    val mode = state.declaration
    DisposableEffect(page, mode, window) {
        if (window == null || !page.declares) {
            state.record(page, StageEventKind.Enter, "선언 없음")
            return@DisposableEffect onDispose { state.record(page, StageEventKind.Leave, "선언 없음") }
        }
        val bar = PlatformBarWindow(window)
        when (mode) {
            DeclarationMode.SaveRestore -> {
                val declaration = SaveRestoreDeclaration(bar, page.request)
                val (saved, applied) = declaration.enter()
                state.record(
                    page, StageEventKind.Enter,
                    "저장 ${saved.lightStatus.iconLabel()} · 적용 ${applied.lightStatus.iconLabel()}"
                )
                onDispose {
                    val restored = declaration.leave()
                    state.record(page, StageEventKind.Leave, "되돌림 ${restored?.lightStatus?.iconLabel() ?: "-"}")
                }
            }

            DeclarationMode.Stack -> {
                val resolved = barRegistry.put(window, bar, owner, page.request)
                state.record(page, StageEventKind.Enter, "쌓음 → ${resolved.lightStatus.iconLabel()}")
                onDispose {
                    val after = barRegistry.remove(window, owner)
                    state.record(page, StageEventKind.Leave, "뺌 → ${after?.lightStatus?.iconLabel() ?: "-"}")
                }
            }
        }
    }

    val (background, foreground) = when (page) {
        StagePage.Home -> Color(0xFFECEFF1) to Color(0xFF37474F)
        StagePage.DarkHeader -> Color(0xFF263238) to Color.White
        StagePage.LightPhoto -> Color(0xFFFFF8E1) to Color(0xFF3E2723)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(page.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = foreground)
            Text(
                if (page.declares) "상태바 ${page.request.statusBar.label} 요청" else "선언 없음",
                fontSize = 12.sp,
                color = foreground
            )
        }
    }
}

@Composable
private fun EventLog(lines: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        if (lines.isEmpty()) {
            Text("(기록 없음)", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFB0BEC5))
        }
        lines.forEach {
            Text(it, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFECEFF1), lineHeight = 15.sp)
        }
    }
}

// ==================== 3. 자동 대조 ====================

/** 한 단계 — 화면을 바꾸고 들어오는 효과와 나가는 onDispose 가 둘 다 돌 때까지 기다린 뒤 바를 읽는다 */
private suspend fun runStep(state: BarLabState, target: StagePage): StepResult {
    val from = state.page
    val startSeq = state.events.lastOrNull()?.seq ?: 0
    state.goTo(target)
    val done = withTimeoutOrNull(5_000) {
        snapshotFlow {
            val since = state.events.filter { it.seq > startSeq }
            val enter = since.firstOrNull { it.page == target && it.kind == StageEventKind.Enter }
            val leave = since.firstOrNull { it.page == from && it.kind == StageEventKind.Leave }
            if (enter != null && leave != null) enter.seq to leave.seq else null
        }.first { it != null }
    }
    // 이펙트가 쓴 값이 창에 반영된 다음 프레임에 읽는다
    withFrameNanos { }
    withFrameNanos { }
    state.refresh()
    val order = done?.let { (enter, leave) -> if (enter < leave) SwapOrder.EnterFirst else SwapOrder.LeaveFirst }
    return StepResult(
        page = target,
        expected = expectedFlags(state.original, target),
        actual = if (done != null) state.current else null,
        order = order
    )
}

/** 모드를 바꾸고 무대가 홈 하나로 가라앉을 때까지 기다린 뒤 원래 값으로 되돌린다 */
private suspend fun resetStage(state: BarLabState, transition: TransitionMode, declaration: DeclarationMode) {
    if (state.page != StagePage.Home) state.goTo(StagePage.Home)
    withTimeoutOrNull(5_000) { snapshotFlow { state.settled }.first { it } }
    state.transition = transition
    state.declaration = declaration
    withFrameNanos { }
    withTimeoutOrNull(5_000) { snapshotFlow { state.settled }.first { it } }
    state.restoreOriginal()
}

private suspend fun runAll(state: BarLabState) {
    state.running = true
    state.runs.clear()
    val total = TransitionMode.entries.size * DeclarationMode.entries.size * StageScenario.entries.size
    var index = 0
    for (transition in TransitionMode.entries) {
        for (declaration in DeclarationMode.entries) {
            for (scenario in StageScenario.entries) {
                index += 1
                state.progress = "$index/$total — ${transition.label} · ${declaration.label} · ${scenario.label.take(1)}"
                resetStage(state, transition, declaration)
                val steps = scenario.steps.map { runStep(state, it) }
                state.runs.add(ScenarioRun(transition, declaration, scenario, steps))
            }
        }
    }
    resetStage(state, TransitionMode.Animated, DeclarationMode.SaveRestore)
    state.progress = "끝 — ${state.runs.sumOf { run -> run.steps.count { !it.ok } }}단계 어긋남"
    state.running = false
}

@Composable
private fun AutoCompareCard(state: BarLabState) {
    val scope = rememberCoroutineScope()
    SectionCard("3. 자동 대조 — 전환 3종 × 선언 2종") {
        BodyText(
            "위 무대를 그대로 몰아 두 시나리오를 돌린다. 실행마다 바를 원래 값으로 되돌린 뒤 시작하고, 단계마다 " +
                "효과와 onDispose 가 둘 다 돈 다음 프레임에 바를 읽어 '보이는 화면의 요청'과 비교한다."
        )
        CaptionText(StageScenario.entries.joinToString("\n") { it.label })
        Spacer(modifier = Modifier.height(6.dp))
        DemoButton(
            if (state.running) "실행 중…" else "12개 실행",
            Color(0xFF1565C0),
            enabled = !state.running && state.window != null
        ) {
            scope.launch { runAll(state) }
        }
        if (state.progress.isNotEmpty()) CaptionText(state.progress)
        CaptionText(
            "실측(API 30·35, debug·release 같음): 즉시 교체는 나가는 onDispose 가 먼저, AnimatedContent·NavDisplay 는 " +
                "들어오는 효과가 먼저 돌았다. 그 순서에서 저장·복원은 ① 밝은 사진 단계에서 요청을 잃고(밝은 아이콘), " +
                "② 홈으로 나온 뒤 어두운 아이콘을 남긴다. 요청 스택은 12개 모두 맞다."
        )
        if (state.runs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            TableRow("전환", "저장·복원 / 요청 스택 (①·② 단계별)", isHeader = true)
            TransitionMode.entries.forEach { transition ->
                val cells = DeclarationMode.entries.joinToString("  /  ") { declaration ->
                    state.runs.filter { it.transition == transition && it.declaration == declaration }
                        .joinToString(" ") { run ->
                            run.scenario.label.take(1) + run.steps.joinToString("") { if (it.ok) "✓" else "✗" }
                        }
                }
                val orders = state.runs.filter { it.transition == transition }
                    .flatMap { run -> run.steps.mapNotNull { it.order } }
                    .groupingBy { it }.eachCount()
                val orderText = orders.entries.joinToString(" · ") { (order, count) ->
                    (if (order == SwapOrder.EnterFirst) "들어오는 효과 먼저" else "나가는 onDispose 먼저") + " $count"
                }
                TableRow(transition.label, "$cells\n순서: $orderText")
            }
            val failures = state.runs.flatMap { run -> run.steps.filter { !it.ok }.map { run to it } }
            if (failures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                failures.forEach { (run, step) ->
                    CaptionText(
                        "✗ ${run.transition.label} · ${run.declaration.label} ${run.scenario.label.take(1)} → ${step.page.label}: " +
                            "기대 ${step.expected.lightStatus.iconLabel()} · 실제 ${step.actual?.lightStatus?.iconLabel() ?: "시간 초과"}"
                    )
                }
            }
        }
    }
}

// ==================== 4. 다이얼로그는 자기 창 ====================

private enum class DialogTarget(val label: String) {
    OwnWindow("다이얼로그 창에 선언"),
    ActivityWindow("액티비티 창에 선언")
}

/** 흰 다이얼로그가 원하는 것 — 어두운 상태바 아이콘 */
private val dialogRequest = BarRequest(statusBar = BarContent.Dark)

@Composable
private fun DialogWindowCard(state: BarLabState) {
    var target by remember { mutableStateOf<DialogTarget?>(null) }
    var dim by remember { mutableStateOf(true) }
    SectionCard("4. 다이얼로그는 자기 창이다") {
        BodyText(
            "Compose `Dialog` 는 별도 창이다. 그 안의 `LocalView` 는 다이얼로그 창의 뷰이고, 그 창은 " +
                "`DialogWindowProvider` 로 얻는다. 흰 다이얼로그가 '어두운 상태바 아이콘'을 선언할 때 어느 창에 거는지에 " +
                "따라 액티비티 창의 값이 바뀌는지 본다(요청 스택 방식)."
        )
        Spacer(modifier = Modifier.height(6.dp))
        ChipRow(
            label = "배경",
            names = listOf("dim 켬(기본)", "dim 끔"),
            selectedIndex = if (dim) 0 else 1,
            enabled = target == null
        ) { dim = it == 0 }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DialogTarget.entries.forEach { t ->
                OutlinedButton(onClick = { target = t }, enabled = !state.running) { Text(t.label, fontSize = 12.sp) }
            }
        }
        CaptionText("다이얼로그를 닫으면 요청이 빠지고, 각 창은 첫 요청 직전 값으로 돌아간다.")
        Spacer(modifier = Modifier.height(6.dp))
        TableRow("실측(API 30·35)", "상태바 아이콘 — SystemUI 판정(dumpsys)", isHeader = true)
        TableRow("다이얼로그 창", "dim 켬·끔 모두 그대로 — 다이얼로그 창 값만 바뀐다")
        TableRow("액티비티 창 · dim 끔", "바로 어두운 아이콘 — 뒤 화면의 바를 바꾼다")
        TableRow("액티비티 창 · dim 켬", "떠 있는 동안 밝은 아이콘 그대로 — 액티비티 창 값은 이미 바뀌어 있다")
        CaptionText(
            "dim 창이 맨 위에 있으면 상태바는 밝은 아이콘으로 판정됐다(두 API 모두). 그래서 dim 을 켠 채 확인하면 " +
                "'액티비티 창에 걸어도 괜찮다'로 잘못 읽힌다 — dim 을 끄거나 창 값을 직접 읽어야 차이가 보인다."
        )
    }
    target?.let { t ->
        BarDialog(
            target = t,
            dim = dim,
            activityWindow = state.window,
            // 요청은 다이얼로그가 컴포지션에서 빠질 때(onDispose) 빠진다 — 닫기 콜백 시점에 읽으면 빠지기 전 값이다
            onRequestChanged = { state.refresh() },
            onDismiss = { target = null }
        )
    }
}

@Composable
private fun BarDialog(
    target: DialogTarget,
    dim: Boolean,
    activityWindow: Window?,
    onRequestChanged: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        DisposableEffect(dialogWindow, dim) {
            if (!dim) dialogWindow?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            onDispose { }
        }
        val declareOn = if (target == DialogTarget.OwnWindow) dialogWindow else activityWindow
        val owner = remember { RequestOwner("다이얼로그") }
        DisposableEffect(declareOn) {
            if (declareOn == null) return@DisposableEffect onDispose { }
            barRegistry.put(declareOn, PlatformBarWindow(declareOn), owner, dialogRequest)
            onRequestChanged()
            onDispose {
                barRegistry.remove(declareOn, owner)
                onRequestChanged()
            }
        }

        // 이펙트가 적용된 다음 프레임에 두 창을 다시 읽는다
        var tick by remember { mutableIntStateOf(0) }
        LaunchedEffect(declareOn, dim) {
            withFrameNanos { }
            tick += 1
        }
        val dialogFlags = remember(tick) { dialogWindow?.let { PlatformBarWindow(it).flags } }
        val activityFlags = remember(tick) { activityWindow?.let { PlatformBarWindow(it).flags } }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(target.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Spacer(modifier = Modifier.height(6.dp))
                BodyText("이 다이얼로그는 상태바 어두운 아이콘을 요청했다. ${if (dim) "뒤 화면 어둡게(dim) 켬" else "dim 끔"}.")
                Spacer(modifier = Modifier.height(8.dp))
                ResultRow("다이얼로그 창", dialogFlags?.text() ?: "-")
                ResultRow("액티비티 창", activityFlags?.text() ?: "-")
                ResultRow("다이얼로그 스택", stackLabels(dialogWindow).ifEmpty { listOf("(없음)") }.joinToString(" › "))
                ResultRow("액티비티 스택", stackLabels(activityWindow).ifEmpty { listOf("(없음)") }.joinToString(" › "))
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DemoButton("다시 읽기", Color(0xFF546E7A)) { tick += 1 }
                    DemoButton("닫기", Color(0xFF37474F), onClick = onDismiss)
                }
            }
        }
    }
}

// ==================== 5. 대비 — 바 뒤에 깔린 색 ====================

/** 바 뒤 띠의 평균 색 */
private class BarBackdrop(val statusArgb: Int?, val navigationArgb: Int?)

/**
 * PixelCopy(API 26+)로 내 창의 상태바·내비 바 자리를 읽는다. 아이콘은 SystemUI 창이 그리므로 여기엔 없다 —
 * edge-to-edge 가 아니면 데코가 그린 바 배경색이, 강제면 그 자리에 앱이 그린 내용이 나온다.
 */
@SuppressLint("ObsoleteSdkInt") // debug 는 HotSwan 때문에 minSdk 26 — release(minSdk 24)에는 이 표시가 필요하다
@RequiresApi(Build.VERSION_CODES.O)
private object BarPixelSampler {
    suspend fun sample(window: Window): BarBackdrop {
        val decor = window.decorView
        val insets = ViewCompat.getRootWindowInsets(decor)
        val top = insets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
        val bottom = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        val width = decor.width
        val height = decor.height
        val status = if (top > 0 && width > 0) averageColor(window, Rect(0, 0, width, top)) else null
        val navigation = if (bottom > 0 && width > 0) averageColor(window, Rect(0, height - bottom, width, height)) else null
        return BarBackdrop(status, navigation)
    }

    private suspend fun averageColor(window: Window, rect: Rect): Int? =
        suspendCancellableCoroutine { continuation ->
            val bitmap = createBitmap(rect.width(), rect.height())
            PixelCopy.request(
                window, rect, bitmap,
                { result ->
                    if (result != PixelCopy.SUCCESS) {
                        bitmap.recycle()
                        continuation.resume(null)
                        return@request
                    }
                    var r = 0L
                    var g = 0L
                    var b = 0L
                    var n = 0L
                    // 띠 전체를 4px 간격으로 훑는다
                    for (y in 0 until bitmap.height step 4) {
                        for (x in 0 until bitmap.width step 4) {
                            val p = bitmap[x, y]
                            r += (p shr 16) and 0xFF
                            g += (p shr 8) and 0xFF
                            b += p and 0xFF
                            n += 1
                        }
                    }
                    bitmap.recycle()
                    continuation.resume(
                        if (n == 0L) null else (0xFF shl 24) or ((r / n).toInt() shl 16) or ((g / n).toInt() shl 8) or (b / n).toInt()
                    )
                },
                Handler(Looper.getMainLooper())
            )
        }
}

/** sRGB 상대 휘도(0~1) */
private fun relativeLuminance(argb: Int): Double {
    fun channel(c: Int): Double {
        val v = c / 255.0
        return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel((argb shr 16) and 0xFF) + 0.7152 * channel((argb shr 8) and 0xFF) + 0.0722 * channel(argb and 0xFF)
}

/** 흰 글자·검은 글자 중 대비가 큰 쪽 — true 면 어두운 아이콘 */
private fun prefersDarkIcons(argb: Int): Boolean {
    val l = relativeLuminance(argb)
    return (l + 0.05) / 0.05 > 1.05 / (l + 0.05)
}

/** 불투명이면 #RRGGBB, 아니면 알파까지 #AARRGGBB */
private fun Int.hex(): String =
    if (this ushr 24 == 0xFF) "#" + (this and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()
    else "#" + (this.toLong() and 0xFFFFFFFFL).toString(16).padStart(8, '0').uppercase()

@SuppressLint("ObsoleteSdkInt") // release minSdk 24 — PixelCopy(Window) 는 API 26 부터라 이 가드가 필요하다(debug 는 HotSwan 때문에 minSdk 26)
private suspend fun sampleBackdrop(window: Window): BarBackdrop? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) BarPixelSampler.sample(window) else null

@Composable
private fun ContrastCard(state: BarLabState) {
    val configuration = LocalConfiguration.current
    val night = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val scope = rememberCoroutineScope()
    var backdrop by remember { mutableStateOf<BarBackdrop?>(null) }
    var sampled by remember { mutableStateOf(false) }
    var sampledAt by remember { mutableStateOf("") }
    val window = state.window

    fun sampleNow() {
        val w = window ?: return
        scope.launch {
            // 버튼 눌림 효과가 사라진 뒤 읽는다
            delay(150)
            backdrop = sampleBackdrop(w)
            sampled = true
            state.refresh()
            sampledAt = "읽음 — ${state.current.text()}"
        }
    }
    LaunchedEffect(window) { sampleNow() }

    SectionCard("5. 아이콘 색은 바 뒤에 깔린 색으로 고른다") {
        BodyText(
            "아이콘 색은 테마나 다크 모드가 아니라 바 뒤에 실제로 깔린 색으로 골라야 한다. 이 창의 상태바·내비 바 자리를 " +
                "PixelCopy 로 읽어 평균 색과, 그 색에서 대비가 큰 아이콘을 보인다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        @Suppress("DEPRECATION") // 값 자체를 보이려고 읽는다 — API 35 + targetSdk 35 에서는 그려지지 않는다
        val declaredStatusColor = window?.statusBarColor
        ResultRow("statusBarColor", declaredStatusColor?.hex() ?: "-")
        val result = backdrop
        if (result == null) {
            ResultRow("바 뒤 색", if (sampled) "API 26 미만 — PixelCopy 없음" else "읽는 중")
        } else {
            BackdropRow("상태바 뒤", result.statusArgb, state.current.lightStatus)
            ResultRow("내비 바 뒤", result.navigationArgb?.hex() ?: "인셋 0 — 이 창이 그리는 바 자리가 없다")
        }
        // 다크 모드로 고르는 흔한 코드: 라이트 테마면 어두운 아이콘
        val byTheme = !night
        val themeVerdict = result?.statusArgb?.let { if (prefersDarkIcons(it) == byTheme) " ✓ 바 뒤 색과 같음" else " ✗ 바 뒤 색과 반대" }.orEmpty()
        ResultRow("다크 모드", "${if (night) "켜짐" else "꺼짐"} — 다크 모드로 고르면 ${byTheme.iconLabel()}$themeVerdict")
        if (sampledAt.isNotEmpty()) CaptionText(sampledAt)
        Spacer(modifier = Modifier.height(6.dp))
        DemoButton("다시 읽기", Color(0xFF546E7A), enabled = window != null) { sampleNow() }
        Spacer(modifier = Modifier.height(6.dp))
        TableRow("실측", "이 앱의 바 뒤 색과 원래 아이콘", isHeader = true)
        TableRow("API 30", "데코가 그린 statusBarColor #3700B3 · 밝은 아이콘 → 대비 맞음")
        TableRow("API 35 · target 35", "statusBarColor 는 투명(#00000000)으로 읽히고 바 뒤는 앱의 노란 여백 #FFFF00 · 밝은 아이콘 → 거의 안 보임")
        CaptionText(
            "다크 모드를 켜도 이 앱 테마(Material.Light)는 바 뒤 색이 그대로였다 — 다크 모드로 아이콘을 고르는 코드는 " +
                "노란 바 위에 밝은 아이콘을 둔다. 내비 바는 판정하지 않는다: API 35 태블릿 에뮬(제스처 내비)에서 앱이 읽은 " +
                "내비 바 플래그는 false 인데 창 속성에는 시스템이 붙인 FORCE_LIGHT_NAVIGATION_BARS 가 있어 핸들이 어둡게 보였다."
        )
    }
}

@Composable
private fun BackdropRow(label: String, argb: Int?, currentLight: Boolean) {
    if (argb == null) {
        ResultRow(label, "인셋 0 — 이 창이 그리는 바 자리가 없다")
        return
    }
    val wantDark = prefersDarkIcons(argb)
    val verdict = if (wantDark == currentLight) "✓ 지금 값과 같음" else "✗ 지금은 ${currentLight.iconLabel()}"
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
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(12.dp)
                .height(12.dp)
                .background(Color(argb), RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${argb.hex()} · 휘도 ${String.format(Locale.US, "%.2f", relativeLuminance(argb))} → ${wantDark.iconLabel()} $verdict",
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

// ==================== 6. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard("6. 함정") {
        PitfallRow(
            "원래 값은 첫 요청 직전에 한 번만 읽는다",
            "저장·복원이 틀리는 이유는 화면마다 '지금 값'을 원래 값으로 저장하기 때문이다. 전환 중엔 그 지금 값이 " +
                "다른 화면의 요청이다. 요청 스택은 창마다 첫 요청 직전 값을 한 번 읽고, 다 빠지면 거기로 돌아간다."
        )
        PitfallRow(
            "LazyColumn 항목 안에서 선언하지 않는다",
            "항목 안의 DisposableEffect 는 화면 이탈이 아니라 스크롤로 항목이 사라질 때 onDispose 가 돈다" +
                "(디스플레이 컷아웃 예제 실측). 선언은 화면 루트나 늘 컴포지션에 있는 곳에 둔다."
        )
        PitfallRow(
            "API 35 + targetSdk 35 에서는 바 배경색을 칠할 수 없다",
            "edge-to-edge 강제로 statusBarColor 는 투명으로 읽히고(실측 #00000000) 바 뒤에는 앱이 그린 내용이 깔린다. " +
                "같은 화면이 API 30 에선 보라 바 · API 35 에선 노란 바 — 아이콘 색은 API 마다 다시 골라야 한다."
        )
        PitfallRow(
            "dim 다이얼로그가 떠 있으면 상태바 아이콘은 밝은 색이다",
            "액티비티 창에 어두운 아이콘을 걸어도 dim 다이얼로그가 맨 위인 동안은 SystemUI 가 밝은 아이콘으로 판정했다" +
                "(API 30·35 dumpsys). 눈으로만 확인하면 잘못 건 선언을 놓친다."
        )
        PitfallRow(
            "다이얼로그 안의 창은 LocalView 로 찾는다",
            "다이얼로그 안에서 `LocalActivity.current.window` 를 쓰면 뒤 화면 창을 바꾼다. 다이얼로그 자기 창은 " +
                "`LocalView.current.parent as? DialogWindowProvider` 로 얻는다."
        )
    }
}

// ==================== 공통 컴포넌트 ====================

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

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
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
            modifier = Modifier.width(104.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = if (isHeader) FontFamily.Default else FontFamily.Monospace,
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

/** 제네릭으로 두면 debug(HotSwan)에서 GENERIC_DECLARATION 으로 건너뛴다 — 이름 목록과 위치로 받는다 */
@Composable
private fun ChipRow(
    label: String,
    names: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F), modifier = Modifier.width(60.dp))
        names.forEachIndexed { index, name ->
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                enabled = enabled,
                label = { Text(name, fontSize = 11.sp) }
            )
        }
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
