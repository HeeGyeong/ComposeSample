package com.example.composesample.presentation.example.component.architecture.navigation

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay

/**
 * Nav3 SceneStrategy 레이어드 바텀시트 예제
 * - 이 프로젝트에서 **처음으로 실제 `androidx.navigation3` 를 쓰는 예제**다. 기존 Nav3 예제 3종
 *   (Navigation3 / Nav3ViewModelScope / Nav3SavedStateHandle)은 의존성 없이 순수 Compose 로 동작 차이만 시뮬레이션한다.
 * - 주제는 "백스택의 엔트리를 **어떤 모양으로** 그릴지"를 정하는 `SceneStrategy` 다. 전략이 `OverlayScene` 을 돌려주면
 *   NavDisplay 는 그 씬의 `overlaidEntries` 로 전략을 **다시** 호출해 아래 씬을 만든다 — 아래가 또 시트면 또 하나의
 *   오버레이가 되어 시트가 층층이 쌓인다(iOS 스택 시트).
 * - 화면 아래 관찰 패널에서 백스택 → 씬 분해 → onRemove 타이밍을 실시간으로 본다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "Nav3 SceneStrategy" 섹션 참고.
 */

// ==================== 백스택 키 ====================

/**
 * 백스택 원소. NavDisplay 의 타입 파라미터는 `T : Any` 라 `NavKey` 를 구현하지 않아도 된다.
 * `NavKey` 는 `rememberNavBackStack`(프로세스 종료 후 복원)을 쓸 때만 필요한데, 그 경로는 `@Serializable` 과
 * kotlinx-serialization 컴파일러 플러그인을 요구하므로 이 예제는 `mutableStateListOf` 백스택을 쓴다.
 */
private sealed interface SceneDemoKey {
    val label: String

    data object Home : SceneDemoKey {
        override val label: String = "Home"
    }

    data class Detail(val seq: Int) : SceneDemoKey {
        override val label: String get() = "상세#$seq"
    }

    /** 같은 종류의 시트를 두 번 쌓아도 contentKey 가 겹치지 않도록 일련번호를 둔다. */
    data class Sheet(val seq: Int, val kind: SheetKind) : SceneDemoKey {
        override val label: String get() = "${kind.title}#$seq"
    }
}

private enum class SheetKind(val title: String, val color: Color) {
    FILTER("필터", Color(0xFF3949AB)),
    SORT("정렬", Color(0xFF00897B)),
    CONFIRM("확인", Color(0xFFD81B60));

    fun next(): SheetKind = entries[(ordinal + 1) % entries.size]
}

// ==================== SceneStrategy / OverlayScene ====================

/** 시트의 겉모습. 엔트리 메타데이터로 실려 전략에 전달된다. */
private data class SheetStyle(val heightFraction: Float = 0.62f)

/**
 * 백스택 **맨 끝** 엔트리의 메타데이터에 [BottomSheetKey] 가 있으면 [BottomSheetScene] 을, 없으면 null 을 돌려준다.
 * 목록의 모든 전략이 null 이면 NavDisplay 가 SinglePaneSceneStrategy 로 떨어진다.
 *
 * 라이브러리의 `DialogSceneStrategy` 와 같은 골격이다. 차이는 Dialog 창 대신 **같은 창 안의 레이어**로 그린다는 것 —
 * 그 때문에 back 처리를 씬이 직접 맡아야 한다([BottomSheetScene.content] 참고).
 *
 * `calculateScene` 은 NavDisplay 내부의 `remember` 안에서 호출된다. 여기서 상태를 쓰면 컴포지션 중 쓰기가 되므로
 * 이 예제의 로그는 전부 버튼 핸들러와 `onRemove`(LaunchedEffect 안)에서만 남긴다.
 */
private class BottomSheetSceneStrategy<T : Any>(
    private val backStack: List<T>,
    private val log: (String) -> Unit,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val last = entries.lastOrNull() ?: return null
        val style = last.metadata[BottomSheetKey] ?: return null
        // overlaidEntries 가 비면 NavDisplay 가 require 로 크래시한다 — 첫 엔트리는 시트로 그리지 않는다.
        if (entries.size < 2) return null
        return BottomSheetScene(
            key = last.contentKey,
            entry = last,
            previousEntries = entries.dropLast(1),
            overlaidEntries = entries.dropLast(1),
            indexInBackStack = entries.lastIndex,
            style = style,
            backStack = backStack,
            log = log,
            onBack = onBack,
        )
    }

    companion object {
        object BottomSheetKey : NavMetadataKey<SheetStyle>

        fun bottomSheet(style: SheetStyle = SheetStyle()): Map<String, Any> =
            metadata { put(BottomSheetKey, style) }
    }
}

/**
 * 시트 한 장 = OverlayScene 한 개.
 *
 * NavDisplay 는 오버레이를 **key 로 추적**해서, 같은 key 로 다시 계산된 인스턴스는 버리고 처음 인스턴스를 계속 그린다.
 * 그래서 입장/퇴장 애니메이션 상태([progress])를 인스턴스에 둬도 유지된다. 반대로 생성자 인자로 구운 값은 갱신되지 않으므로
 * "위에 몇 장이 쌓였나"처럼 바뀌는 값은 스냅샷 상태인 [backStack] 에서 매번 읽는다.
 */
private class BottomSheetScene<T : Any>(
    override val key: Any,
    private val entry: NavEntry<T>,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val indexInBackStack: Int,
    private val style: SheetStyle,
    private val backStack: List<T>,
    private val log: (String) -> Unit,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    /** 0 = 화면 아래로 숨음, 1 = 완전히 올라옴. */
    private val progress = Animatable(0f)

    override val content: @Composable () -> Unit = {
        LaunchedEffect(Unit) { progress.animateTo(1f, tween(durationMillis = 280)) }

        // 이 시트 위에 몇 장이 더 쌓였는지 — backStack 이 스냅샷 상태라 위에서 push/pop 되면 여기서 다시 그린다.
        val depthFromTop = (backStack.lastIndex - indexInBackStack).coerceAtLeast(0)
        val scale by animateFloatAsState(1f - 0.06f * depthFromTop, label = "sheetScale")
        val lift by animateDpAsState((-20).dp * depthFromTop, label = "sheetLift")

        // NavDisplay 의 back 핸들러는 오버레이가 아니라 **기본 씬**의 previousEntries 로 활성 여부를 정하고,
        // 완료 시 (엔트리 수 - 기본 씬 previousEntries 수) 만큼 onBack 을 반복한다.
        // → Home 위 시트: 비활성이라 back 이 액티비티로 새어 나간다. 상세 위 시트 2장: 한 번에 3개가 pop 된다.
        // Dialog 창을 쓰는 DialogScene 은 창이 back 을 받아 문제가 없지만, 같은 창 레이어인 이 시트는 직접 받아야 한다.
        // 나중에 등록된 핸들러가 우선하므로 맨 위 시트만 켜 두면 한 장씩 닫힌다.
        val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
        NavigationBackHandler(
            state = backState,
            isBackEnabled = depthFromTop == 0,
            onBackCompleted = onBack
        )

        // z-order 도 직접 정한다. NavDisplay 는 오버레이를 **처음 등장한 순서로 목록에 append 하고 그 역순으로** 그린다.
        // 한 번에 여러 장이 생기면 [위, 아래] 순이라 맞게 그려지지만, 한 장씩 push 하면 [필터, 정렬] 이 되어
        // 새 시트(정렬)를 먼저 그리고 필터를 그 위에 그린다 — 실기기에서 새 시트가 아래에 깔리는 것으로 확인했다.
        // Dialog 는 창 단위 z-order 라 무관하지만, 같은 창 레이어는 백스택 인덱스를 zIndex 로 줘야 한다.
        // (NavDisplay 는 자체 Box 없이 기본 씬과 오버레이를 호출한 쪽 레이아웃에 형제로 내보내므로 zIndex 가 먹는다.)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(indexInBackStack.toFloat())
        ) {
            // 스크림 — 시트마다 하나씩 깔리므로 쌓일수록 아래가 점점 어두워진다.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress.value }
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(style.heightFraction)
                    .graphicsLayer {
                        // 위쪽 가장자리를 기준으로 줄이고 들어 올려, 아래 시트의 윗단이 위 시트 뒤로 살짝 보이게 한다.
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        scaleX = scale
                        scaleY = scale
                        translationY = (1f - progress.value) * size.height + lift.toPx()
                    },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                entry.Content()
            }
        }
    }

    /** pop 된 뒤 컴포지션을 떠나기 **전에** 호출된다. 이 함수가 반환될 때까지 시트는 화면에 남는다. */
    override suspend fun onRemove() {
        val startedAt = SystemClock.uptimeMillis()
        log("$key · onRemove() 시작 — 백스택에선 빠졌지만 아직 컴포지션에 있다")
        progress.animateTo(0f, tween(durationMillis = 220))
        log("$key · onRemove() 반환(${SystemClock.uptimeMillis() - startedAt}ms) → 컴포지션 이탈")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BottomSheetScene<*>) return false
        return key == other.key &&
            entry == other.entry &&
            previousEntries == other.previousEntries &&
            overlaidEntries == other.overlaidEntries &&
            style == other.style
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + entry.hashCode()
        result = 31 * result + previousEntries.hashCode()
        result = 31 * result + overlaidEntries.hashCode()
        result = 31 * result + style.hashCode()
        return result
    }

    override fun toString(): String = "BottomSheetScene(key=$key)"
}

// ==================== 백스택 조작 ====================

/** 백스택 변경과 이벤트 로그를 한곳에서 처리한다. 전부 이벤트 핸들러에서만 호출된다. */
private class SceneDemoActions(
    private val backStack: SnapshotStateList<SceneDemoKey>,
    private val eventLog: SnapshotStateList<String>,
) {
    private var seq = 1

    fun openSheet(kind: SheetKind) = push(SceneDemoKey.Sheet(seq++, kind))

    fun pushDetail() = push(SceneDemoKey.Detail(seq++))

    fun pop() {
        // Home 까지 빼면 백스택이 비어 NavDisplay 가 그릴 것이 없어진다.
        if (backStack.size <= 1) return
        val removed = backStack.removeAt(backStack.lastIndex)
        log("pop ${removed.label}")
    }

    /** 맨 끝의 시트들을 한 번에 뺀다 — 오버레이 여러 개의 onRemove 가 동시에 돈다. */
    fun closeAllSheets() {
        val removed = mutableListOf<String>()
        while (backStack.size > 1 && backStack.last() is SceneDemoKey.Sheet) {
            removed += backStack.removeAt(backStack.lastIndex).label
        }
        if (removed.isNotEmpty()) log("시트 전부 닫기 → ${removed.joinToString()} 동시 제거")
    }

    /** [key] 위에 쌓인 엔트리 수. 컴포지션에서 읽으면 백스택 변화에 따라 다시 그려진다. */
    fun countAbove(key: SceneDemoKey): Int = (backStack.lastIndex - backStack.indexOf(key)).coerceAtLeast(0)

    fun log(message: String) {
        eventLog.add(0, message)
        if (eventLog.size > 8) eventLog.removeAt(eventLog.lastIndex)
    }

    private fun push(key: SceneDemoKey) {
        backStack.add(key)
        log("push ${key.label}")
    }
}

// ==================== 화면 ====================

@Composable
fun Nav3SceneStrategyExampleUI(onBackEvent: () -> Unit) {
    val backStack = remember { mutableStateListOf<SceneDemoKey>(SceneDemoKey.Home) }
    val eventLog = remember { mutableStateListOf<String>() }
    val actions = remember { SceneDemoActions(backStack, eventLog) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        MainHeader(
            title = "Nav3 SceneStrategy 바텀시트",
            onBackIconClicked = onBackEvent
        )

        // NavDisplay 가 그리는 영역. 시트는 같은 창 안의 레이어라 이 테두리 밖으로 나가지 않는다.
        // NavDisplay 는 자체 Box 가 없어 기본 씬과 오버레이를 이 Box 의 형제 자식으로 내보낸다 — 반드시 Box 여야 겹친다.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            SceneStrategyNavHost(backStack = backStack, actions = actions)
        }

        ObservationPanel(backStack = backStack, eventLog = eventLog)
    }
}

@Composable
private fun SceneStrategyNavHost(
    backStack: SnapshotStateList<SceneDemoKey>,
    actions: SceneDemoActions
) {
    // 전략과 entryProvider 는 remember 로 고정한다 — NavDisplay 가 둘을 키로 씬을 다시 계산하기 때문.
    val sceneStrategies = remember(backStack, actions) {
        listOf(BottomSheetSceneStrategy<SceneDemoKey>(backStack, actions::log))
    }
    val provider = remember(actions) {
        entryProvider<SceneDemoKey> {
            // contentKey 를 라벨로 지정해 로그와 씬 key 를 사람이 읽을 수 있게 한다(기본값은 key 의 toString + 클래스명).
            entry<SceneDemoKey.Home>(clazzContentKey = { it.label }) {
                HomePane(actions = actions)
            }
            entry<SceneDemoKey.Detail>(clazzContentKey = { it.label }) { key ->
                DetailPane(key = key, actions = actions)
            }
            // 메타데이터가 전략의 판단 근거다 — 이 엔트리만 시트로 그려진다.
            entry<SceneDemoKey.Sheet>(
                clazzContentKey = { it.label },
                metadata = BottomSheetSceneStrategy.bottomSheet()
            ) { key ->
                SheetPane(key = key, actions = actions)
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { actions.pop() },
        sceneStrategies = sceneStrategies,
        entryProvider = provider
    )
}

// ==================== 엔트리 화면 ====================

@Composable
private fun HomePane(actions: SceneDemoActions) {
    // remember(saveable 아님) 이라, 컴포지션을 떠났다 돌아오면 0 부터 다시 센다.
    var ticks by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            ticks++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Home", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        CaptionText("SinglePaneScene — 전략이 null 을 돌려 기본 전략으로 그려졌다")
        Spacer(modifier = Modifier.height(10.dp))
        LifecycleRow()
        StatusRow(label = "틱 (0.5초마다 +1)", value = "$ticks")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "시트를 열어도 틱은 계속 오른다 — OverlayScene 은 아래 화면을 컴포지션에 남긴 채 위에 겹친다. " +
                "대신 Lifecycle 은 STARTED 로 내려간다. 반대로 상세로 push 했다 돌아오면 틱이 0 부터 다시 시작한다 " +
                "(SinglePane 전환은 이전 화면을 컴포지션에서 뺀다)."
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "시트 열기", color = SheetKind.FILTER.color) { actions.openSheet(SheetKind.FILTER) }
            DemoButton(text = "상세 push", color = Color(0xFF546E7A)) { actions.pushDetail() }
        }
        Spacer(modifier = Modifier.height(20.dp))
        PitfallSection()
    }
}

@Composable
private fun DetailPane(key: SceneDemoKey.Detail, actions: SceneDemoActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = key.label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        CaptionText("SinglePaneScene — Home 은 지금 컴포지션에 없다(틱이 멈췄다)")
        Spacer(modifier = Modifier.height(10.dp))
        LifecycleRow()
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "여기서 시트를 2장 쌓은 뒤 시스템 back 을 눌러 보자. 시트가 직접 back 을 받지 않으면 NavDisplay 가 " +
                "\"엔트리 수 − 기본 씬의 previousEntries 수\" 만큼 pop 해서 시트 2장과 이 화면이 한 번에 사라진다."
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "시트 열기", color = SheetKind.SORT.color) { actions.openSheet(SheetKind.SORT) }
            DemoButton(text = "뒤로 (pop)", color = Color(0xFF546E7A)) { actions.pop() }
        }
    }
}

@Composable
private fun SheetPane(key: SceneDemoKey.Sheet, actions: SceneDemoActions) {
    val above = actions.countAbove(key)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFBDBDBD))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "${key.kind.title} 시트", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = key.kind.color)
        CaptionText("BottomSheetScene(OverlayScene) · contentKey = \"${key.label}\"")
        Spacer(modifier = Modifier.height(10.dp))
        LifecycleRow()
        StatusRow(label = "위에 쌓인 시트", value = "${above}장")
        Spacer(modifier = Modifier.height(6.dp))
        BodyText(
            if (above == 0) "맨 위 오버레이라 RESUMED 다. back·스크림 탭은 이 시트만 닫는다."
            else "위에 시트가 있어 STARTED 로 캡됐다. 컴포지션은 그대로고, 스크림이 덮어 입력만 막힌다."
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val nextKind = key.kind.next()
            DemoButton(text = "${nextKind.title} 쌓기", color = nextKind.color) { actions.openSheet(nextKind) }
            DemoButton(text = "닫기 (pop)", color = Color(0xFF546E7A)) { actions.pop() }
        }
        Spacer(modifier = Modifier.height(8.dp))
        DemoButton(text = "시트 전부 닫기", color = Color(0xFF78909C)) { actions.closeAllSheets() }
    }
}

// ==================== 관찰 패널 ====================

@Composable
private fun ObservationPanel(backStack: List<SceneDemoKey>, eventLog: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        PanelLabel("백스택 — NavDisplay 가 관찰하는 유일한 상태")
        MonoText(backStack.joinToString("  ›  ") { it.label })
        Spacer(modifier = Modifier.height(8.dp))
        PanelLabel("씬 분해 — SceneState 의 재귀 규칙을 같은 백스택으로 재현")
        describeSceneResolution(backStack).forEach { MonoText(it) }
        Spacer(modifier = Modifier.height(8.dp))
        PanelLabel("이벤트 로그 (최신이 위)")
        if (eventLog.isEmpty()) {
            CaptionText("아직 없음 — 시트를 열고 닫아 보세요")
        } else {
            eventLog.forEach { MonoText(it) }
        }
    }
}

/**
 * NavDisplay 가 씬을 고르는 과정을 백스택만으로 다시 적는다(SceneState 소스의 규칙 그대로):
 * 전체 엔트리로 전략 호출 → 결과가 OverlayScene 이면 그 overlaidEntries 로 다시 호출 → 오버레이가 아닌 씬이 나오면 멈춤.
 * 마지막 씬이 NavDisplay 의 currentScene(기본 씬)이고, 그 위의 것들이 overlayScenes 다.
 */
private fun describeSceneResolution(stack: List<SceneDemoKey>): List<String> {
    val lines = mutableListOf<String>()
    var entries = stack
    var step = 1
    while (entries.isNotEmpty()) {
        val top = entries.last()
        val names = entries.joinToString(prefix = "[", postfix = "]") { it.label }
        if (top is SceneDemoKey.Sheet && entries.size >= 2) {
            val below = entries.dropLast(1).joinToString(prefix = "[", postfix = "]") { it.label }
            lines += "$step) $names → BottomSheetScene(${top.label}), overlaid=$below"
            entries = entries.dropLast(1)
            step++
        } else {
            lines += "$step) $names → null → SinglePaneScene(${top.label}) = 기본 씬"
            break
        }
    }
    return lines
}

// ==================== 걸리는 것들 ====================

@Composable
private fun PitfallSection() {
    Text(text = "걸리는 것들", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
    Spacer(modifier = Modifier.height(8.dp))
    PitfallRow(
        "같은 창 오버레이는 back 을 직접 받아야 한다",
        "NavDisplay 의 back 핸들러는 기본 씬의 previousEntries 로 켜지고, 완료 시 (엔트리 수 − 그 수) 만큼 pop 한다. " +
            "Home 위 시트면 핸들러가 꺼져 back 이 액티비티로 새고, 상세 위 시트 2장이면 3개가 한 번에 빠진다. " +
            "DialogScene 은 Dialog 창이 back 을 받아서 이 문제가 없다."
    )
    PitfallRow(
        "같은 창 오버레이는 z-order 도 직접 정해야 한다",
        "NavDisplay 는 오버레이를 처음 등장한 순서로 append 하고 역순으로 그린다. 한 장씩 push 하면 새 시트가 " +
            "아래에 깔린다(실기기 확인). Dialog 는 창 단위라 무관하고, 같은 창 레이어는 Modifier.zIndex(백스택 인덱스)로 바로잡는다. " +
            "NavDisplay 는 자체 Box 가 없어 기본 씬과 오버레이를 부모 레이아웃에 형제로 내보내므로 Box 안에 둬야 한다."
    )
    PitfallRow(
        "overlaidEntries 는 비어 있으면 안 된다",
        "NavDisplay 가 require 로 크래시한다. 첫 엔트리가 시트가 될 수 있다면 전략에서 null 을 돌려 SinglePane 으로 넘긴다."
    )
    PitfallRow(
        "calculateScene 은 순수하게",
        "NavDisplay 내부의 remember 안에서 호출된다. 여기서 상태를 쓰면 컴포지션 중 쓰기가 된다."
    )
    PitfallRow(
        "퇴장 애니메이션은 onRemove 에서",
        "pop 즉시 사라지지 않고 onRemove 가 반환될 때까지 컴포지션에 남는다. 이벤트 로그의 ms 가 그 시간이다."
    )
    PitfallRow(
        "가려져도 dispose 되지 않는다 — RESUMED 만 빠진다",
        "오버레이가 하나라도 있으면 기본 씬은 STARTED, 오버레이 중에서도 맨 위만 RESUMED 다. " +
            "영상·카메라처럼 보일 때만 돌아야 하는 작업은 LifecycleResumeEffect 쪽에 건다."
    )
    PitfallRow(
        "같은 key 의 오버레이는 처음 인스턴스가 계속 쓰인다",
        "재계산된 인스턴스는 버려진다. 그래서 Animatable 을 씬에 둬도 되지만, 생성자로 구운 값은 갱신되지 않는다."
    )
    PitfallRow(
        "then 과 단일 sceneStrategy 오버로드는 deprecated",
        "1.1.x 는 sceneStrategies 리스트를 받는 NavDisplay 로 옮겨 갔다. 앞에서부터 시도하고 전부 null 이면 SinglePane 이다."
    )
    PitfallRow(
        "NavKey 가 필수는 아니다",
        "NavDisplay 는 T : Any 다. NavKey 는 rememberNavBackStack(@Serializable + 직렬화 플러그인)용이라, " +
            "이 예제는 mutableStateListOf 를 써서 프로세스 종료 후 복원을 포기했다."
    )
}

// ==================== 공통 요소 ====================

@Composable
private fun LifecycleRow() {
    val state by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val color = when (state) {
        Lifecycle.State.RESUMED -> Color(0xFF2E7D32)
        Lifecycle.State.STARTED -> Color(0xFFEF6C00)
        else -> Color(0xFF757575)
    }
    StatusRow(label = "Lifecycle", value = state.name, valueColor = color)
}

@Composable
private fun StatusRow(label: String, value: String, valueColor: Color = Color(0xFF212121)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF616161))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun DemoButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun PitfallRow(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE65100))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = body, fontSize = 12.sp, color = Color(0xFF616161), lineHeight = 17.sp)
    }
}

@Composable
private fun PanelLabel(text: String) {
    Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF455A64))
    Spacer(modifier = Modifier.height(2.dp))
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 15.sp)
}

@Composable
private fun MonoText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        lineHeight = 15.sp
    )
}
