package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.layoutBounds
import androidx.compose.ui.layout.onFirstVisible
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * LazyList 캐시 윈도우 + 노출(Impression) 추적 예제 (Compose foundation 1.12)
 * - "컴포즈됨 ≠ 화면에 보임": LazyLayoutCacheWindow 가 화면 밖 아이템을 미리 컴포즈(ahead)하고
 *   지나간 아이템을 버리지 않고 유지(behind)하는 것을 아이템 수로 실측한다.
 * - 같은 리스트에서 LaunchedEffect 기반 노출 로그가 화면 밖 아이템까지 찍히는 함정과,
 *   onVisibilityChanged / onFirstVisible 이 실제로 보일 때만 오는 것을 대조한다.
 * - prefetch 를 여러 프레임에 나눠 컴포즈하는 Pausable composition 을 무거운 아이템으로 실측한다.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 상수 ====================

private const val CACHE_ITEM_COUNT = 100
private val CACHE_ITEM_HEIGHT = 56.dp

/** 아이템 5개 + 20dp — 여섯 번째 아이템이 일부만 보여 viewport 기준에 따른 차이가 드러난다 */
private val CACHE_LIST_HEIGHT = 300.dp

private const val HEAVY_ITEM_COUNT = 30

/** 무거운 아이템 하나를 이루는 자식 컴포저블 수 — 각 자식 호출 시작점이 Pausable composition 의 중단 지점이다 */
private const val HEAVY_PART_COUNT = 12

/** 자식 하나의 컴포지션 비용(바쁜 대기). 12개면 아이템당 약 18ms — 90Hz 한 프레임(11ms)을 넘는다 */
private const val HEAVY_PART_COST_NANOS = 1_500_000L

private val HEAVY_LIST_HEIGHT = 200.dp

// ==================== 모델 ====================

/** 이 예제의 유일한 변수 — 리스트 상태를 어떤 prefetch 전략으로 만드는가 */
private enum class CacheWindowMode(
    val label: String,
    val code: String,
    val description: String,
) {
    DEFAULT(
        label = "기본 prefetch",
        code = "rememberLazyListState()",
        description = "스크롤 방향으로 다음 아이템 1개만 미리 준비한다. 화면을 벗어난 아이템은 바로 내려놓는다(재사용 풀로)."
    ),
    DP(
        label = "Dp 150 / 100",
        code = "rememberLazyListState(cacheWindow = LazyLayoutCacheWindow(ahead = 150.dp, behind = 100.dp))",
        description = "스크롤 방향 앞쪽 150dp 를 미리 컴포즈하고, 지나간 쪽 100dp 안의 아이템은 버리지 않고 유지한다."
    ),
    FRACTION(
        label = "뷰포트 1.0 / 1.0",
        code = "rememberLazyListState(cacheWindow = LazyLayoutCacheWindow(aheadFraction = 1f, behindFraction = 1f))",
        description = "뷰포트 높이에 비례 — 앞쪽 한 화면을 미리 컴포즈하고 지나간 한 화면을 유지한다."
    ),
}

@OptIn(ExperimentalFoundationApi::class)
private fun CacheWindowMode.cacheWindow(): LazyLayoutCacheWindow? = when (this) {
    CacheWindowMode.DEFAULT -> null
    CacheWindowMode.DP -> LazyLayoutCacheWindow(ahead = 150.dp, behind = 100.dp)
    CacheWindowMode.FRACTION -> LazyLayoutCacheWindow(aheadFraction = 1f, behindFraction = 1f)
}

/**
 * 같은 리스트에서 세 가지 "노출" 신호를 나란히 모은다.
 *
 * 이펙트·가시성 콜백은 컴포지션 밖(apply 단계, 레이아웃 이후)에서 불리므로 스냅샷 상태에 바로 써도 된다.
 */
@Stable
private class ImpressionTracker {
    /** 지금 컴포지션에 살아 있는 아이템 — DisposableEffect 진입부터 해제까지 */
    val composed = mutableStateSetOf<Int>()

    /** ❌ LaunchedEffect 로 찍은 노출 로그 */
    var effectLogCount by mutableIntStateOf(0)
        private set
    val effectLogged = mutableStateSetOf<Int>()

    /** onVisibilityChanged — 리스트 경계 기준 / viewport 미지정(앱 윈도우 기준) */
    val visibleInList = mutableStateSetOf<Int>()
    val visibleInWindow = mutableStateSetOf<Int>()

    /** 한 번이라도 리스트 안에서 전부 보인 아이템 */
    val everVisible = mutableStateSetOf<Int>()

    /** ✅ 권장 대안 — onVisibilityChanged(true) 를 앱 쪽 "이미 본 key" 집합으로 거른 1회 노출 */
    val impressedOnce = mutableStateSetOf<Int>()

    /** deprecated onFirstVisible 호출(비교용) */
    var firstVisibleCount by mutableIntStateOf(0)
        private set
    val firstVisibleSeen = mutableStateSetOf<Int>()

    fun onComposed(index: Int) {
        composed += index
    }

    fun onDisposed(index: Int) {
        composed -= index
    }

    fun onEffectLog(index: Int) {
        effectLogCount++
        effectLogged += index
    }

    fun onListVisibility(index: Int, visible: Boolean) {
        if (visible) {
            visibleInList += index
            everVisible += index
            // 아이템당 한 번 — 노드가 다시 부착돼도 집합이 걸러 준다(실제 앱에서는 key 를 쓴다)
            impressedOnce += index
        } else {
            visibleInList -= index
        }
    }

    fun onWindowVisibility(index: Int, visible: Boolean) {
        if (visible) visibleInWindow += index else visibleInWindow -= index
    }

    fun onFirstVisible(index: Int) {
        firstVisibleCount++
        firstVisibleSeen += index
    }

    /** 누적 로그만 지운다 — 지금 컴포즈·노출 상태는 그대로 둔다 */
    fun resetLogs() {
        effectLogCount = 0
        effectLogged.clear()
        everVisible.clear()
        everVisible += visibleInList
        impressedOnce.clear()
        impressedOnce += visibleInList
        firstVisibleCount = 0
        firstVisibleSeen.clear()
    }
}

/**
 * 메인 스레드가 지나간 프레임 수를 센다.
 *
 * 컴포지션은 메인 스레드를 붙잡고 있으므로, 두 자식 사이에 이 값이 바뀌었다면 그 사이에 프레임이 지나갔다 —
 * 즉 컴포지션이 멈췄다가 다음 프레임에 이어진 것이다. 시간 간격으로 판정하면 디버그 빌드의 잡음에 흔들린다.
 */
private class FrameCounter : Choreographer.FrameCallback {
    var frame = 0L
        private set
    private var running = false

    fun start() {
        if (running) return
        running = true
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        frame++
        if (running) Choreographer.getInstance().postFrameCallback(this)
    }
}

/**
 * 무거운 아이템의 자식별로 "몇 번째 프레임 사이에, 언제" 컴포즈됐는지 모은다.
 *
 * 아이템 컴포지션 도중에 기록하므로 스냅샷 상태가 아니다 — 아이템 하나가 다 컴포즈되면
 * 메인 스레드에 [publish] 를 걸어 컴포지션 밖에서 화면 값으로 옮긴다(주기적으로 깨어나는 루프를 두지 않는다).
 */
@Stable
private class SliceRecorder(private val frames: FrameCounter) {
    private val parts = HashMap<Int, MutableList<Pair<Long, Long>>>()
    private val startedWhileScrolling = HashMap<Int, Boolean>()
    private val composedByPrefetch = HashMap<Int, Boolean>()
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 천천히 스크롤하는 동안 true — 컴포지션 중에 읽기만 하므로 스냅샷 상태가 아니다 */
    var scrolling = false

    var results by mutableStateOf<List<SliceResult>>(emptyList())
        private set

    fun record(index: Int) {
        val list = parts.getOrPut(index) { mutableListOf() }
        if (list.isEmpty()) {
            startedWhileScrolling[index] = scrolling
            // 측정 전용 진단 — prefetch 실행기에서 불렸는지, 측정 패스에서 바로 컴포즈됐는지를 호출 스택으로 가른다
            composedByPrefetch[index] = Thread.currentThread().stackTrace.any { frame ->
                frame.className.contains("PrefetchScheduler")
            }
        }
        list += frames.frame to System.nanoTime()
        if (list.size == HEAVY_PART_COUNT) mainHandler.post { publish() }
    }

    private fun publish() {
        results = parts.entries
            .filter { it.value.size == HEAVY_PART_COUNT }
            .sortedBy { it.key }
            .map { (index, list) ->
                // 같은 프레임 사이에 연달아 컴포즈된 자식들이 한 조각이다
                val chunks = list.groupBy({ it.first }, { it.second }).values
                val longestMs = chunks.maxOf { times ->
                    (times.max() - times.min() + HEAVY_PART_COST_NANOS) / 1_000_000f
                }
                SliceResult(
                    index = index,
                    slices = chunks.size,
                    longestChunkMs = longestMs,
                    duringScroll = startedWhileScrolling[index] == true,
                    viaPrefetch = composedByPrefetch[index] == true
                )
            }
    }
}

private data class SliceResult(
    val index: Int,
    val slices: Int,
    val longestChunkMs: Float,
    val duringScroll: Boolean,
    val viaPrefetch: Boolean,
)

/** 정렬된 인덱스 집합을 "#0–#4, #9" 처럼 구간으로 줄인다 */
private fun Set<Int>.toRanges(): String {
    if (isEmpty()) return "없음"
    val sorted = sorted()
    val parts = mutableListOf<String>()
    var start = sorted.first()
    var prev = start
    for (value in sorted.drop(1)) {
        if (value != prev + 1) {
            parts += if (start == prev) "#$start" else "#$start–#$prev"
            start = value
        }
        prev = value
    }
    parts += if (start == prev) "#$start" else "#$start–#$prev"
    return parts.joinToString(", ")
}

// ==================== 화면 ====================

@Composable
fun LazyListCacheWindowExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "LazyList 캐시 윈도우와 노출 추적",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CacheWindowOverviewCard() }
            item { CacheWindowDemoCard() }
            item { ImpressionGuideCard() }
            item { PausableCompositionCard() }
            item { CacheWindowPitfallCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun CacheWindowOverviewCard() {
    CacheCard {
        CardTitle("컴포즈됨 ≠ 화면에 보임")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "LazyColumn 의 아이템은 네 가지 상태 중 하나에 있다. 화면에 배치된 아이템, 아직 보이지 않지만 " +
                "미리 컴포즈해 둔 아이템(ahead), 이미 지나갔지만 버리지 않고 남겨 둔 아이템(behind), " +
                "그리고 내려놓아 재사용 풀로 간 아이템이다. 앞의 셋은 모두 컴포지션이 살아 있어서 " +
                "remember·LaunchedEffect·DisposableEffect 가 실행 중이다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        StatTable(
            listOf(
                "LazyLayoutCacheWindow" to "ahead(앞쪽 미리 컴포즈) · behind(지나간 쪽 유지) 크기 — Dp 또는 뷰포트 비율",
                "rememberLazyListState(cacheWindow)" to "1.12 에서 캐시 윈도우를 거는 곳(@ExperimentalFoundationApi)",
                "Modifier.onVisibilityChanged" to "배치된 노드가 viewport 안에 들어오고 나갈 때(안정 API)",
                "Modifier.onFirstVisible" to "1.11.0 부터 deprecated — 다시 부착될 때마다 또 온다(아래에서 실측)",
                "Modifier.layoutBounds(holder)" to "viewport 로 쓸 경계를 잡아 두는 곳(기본은 앱 윈도우)",
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "LazyColumn(cacheWindow = …) 파라미터는 foundation 1.13.0-alpha03 에서 생겼다. 이 프로젝트의 1.12.1 에는 " +
                "없어서 리스트 상태를 만들 때 넘긴다. 같은 윈도우를 받는 rememberLazyGridState 도 있고, " +
                "Pager 는 공개 파라미터 없이 기본으로 켜져 있다."
        )
    }
}

// ==================== 2. 실측 ====================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CacheWindowDemoCard() {
    var mode by remember { mutableStateOf(CacheWindowMode.DEFAULT) }

    CacheCard {
        CardTitle("1. 같은 리스트, 다른 캐시 윈도우")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "아이템 ${CACHE_ITEM_COUNT}개 · 높이 ${CACHE_ITEM_HEIGHT.value.toInt()}dp · 리스트 ${CACHE_LIST_HEIGHT.value.toInt()}dp " +
                "(5개 + 여섯 번째가 20dp 걸친다). 모드를 바꾸면 리스트와 기록을 새로 만든다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CacheWindowMode.entries.forEach { entry ->
                CacheChip(label = entry.label, selected = mode == entry) { mode = entry }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        CodeText(mode.code)
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(mode.description)
        Spacer(modifier = Modifier.height(10.dp))

        // key(mode): 모드가 바뀌면 리스트 상태·기록을 통째로 새로 만든다(rememberSaveable 이 윈도우를 키로 쓰기도 한다)
        key(mode) {
            val tracker = remember { ImpressionTracker() }
            val cacheWindow = remember { mode.cacheWindow() }
            val listState = if (cacheWindow == null) {
                rememberLazyListState()
            } else {
                rememberLazyListState(cacheWindow = cacheWindow)
            }
            val listBounds = remember { LayoutBoundsHolder() }

            CacheDemoList(listState = listState, tracker = tracker, listBounds = listBounds)
            Spacer(modifier = Modifier.height(10.dp))
            CacheScrollButtons(listState = listState, tracker = tracker)
            Spacer(modifier = Modifier.height(10.dp))
            CacheReadout(listState = listState, tracker = tracker)
        }
    }
}

@Composable
private fun CacheDemoList(
    listState: LazyListState,
    tracker: ImpressionTracker,
    listBounds: LayoutBoundsHolder,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(CACHE_LIST_HEIGHT)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            // 이 리스트의 경계를 노출 판정 viewport 로 쓴다
            .layoutBounds(listBounds)
    ) {
        items(count = CACHE_ITEM_COUNT, key = { it }) { index ->
            CacheItemRow(index = index, tracker = tracker, listBounds = listBounds)
        }
    }
}

// DEPRECATION 억제: onFirstVisible 은 1.11.0 부터 deprecated 다. 그 이유(다시 부착될 때마다 다시 호출)를
// 이 화면에서 실측해 보이려고 일부러 부른다 — 노출 집계는 권장 대안(impressedOnce)으로 한다.
@Suppress("DEPRECATION")
@Composable
private fun CacheItemRow(index: Int, tracker: ImpressionTracker, listBounds: LayoutBoundsHolder) {
    // 컴포지션 수명 — 미리 컴포즈(ahead)·유지(behind)된 아이템에서도 살아 있다
    DisposableEffect(index) {
        tracker.onComposed(index)
        onDispose { tracker.onDisposed(index) }
    }
    // ❌ "컴포지션에 들어오면 노출" 로 보는 방식 — 화면 밖에서 미리 컴포즈될 때도 실행된다
    LaunchedEffect(index) { tracker.onEffectLog(index) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(CACHE_ITEM_HEIGHT)
            // ✅ 리스트 경계 안에 전부(기본 minFractionVisible = 1f) 들어왔을 때만 true
            .onVisibilityChanged(viewportBounds = listBounds) { visible ->
                tracker.onListVisibility(index, visible)
            }
            // 비교용 — viewport 를 넘기지 않으면 앱 윈도우가 기준이라 리스트에 잘린 부분을 모른다
            .onVisibilityChanged { visible -> tracker.onWindowVisibility(index, visible) }
            // ⚠️ deprecated(1.11.0) — 이름과 달리 노드가 다시 부착되면(내려놓았다가 다시 컴포즈) 또 온다
            .onFirstVisible(viewportBounds = listBounds) { tracker.onFirstVisible(index) }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$index",
            modifier = Modifier.width(52.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1976D2)
        )
        Text(
            text = "피드 아이템 $index",
            fontSize = 12.sp,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun CacheScrollButtons(listState: LazyListState, tracker: ImpressionTracker) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    fun run(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            block()
            busy = false
        }
    }

    suspend fun page(direction: Int) {
        val viewport = listState.layoutInfo.viewportSize.height
        listState.animateScrollBy(direction * viewport.toFloat())
        // 스크롤이 멈춘 뒤 채워지는 캐시 윈도우·가시성 콜백을 기다린다
        delay(400)
    }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CacheChip(label = "▼ 한 화면", selected = false) { run { page(1) } }
        CacheChip(label = "▲ 한 화면", selected = false) { run { page(-1) } }
        CacheChip(label = "왕복 ▼▼▲▲", selected = false) {
            run {
                repeat(2) { page(1) }
                repeat(2) { page(-1) }
            }
        }
        CacheChip(label = "기록 초기화", selected = false) { tracker.resetLogs() }
    }
}

@Composable
private fun CacheReadout(listState: LazyListState, tracker: ImpressionTracker) {
    // layoutInfo 는 스크롤 프레임마다 바뀌므로 필요한 값만 파생 상태로 읽는다
    val visibleRange by remember {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) null else visible.first().index..visible.last().index
        }
    }
    val range = visibleRange
    val composed = tracker.composed.toSet()
    val ahead = if (range == null) 0 else composed.count { it > range.last }
    val behind = if (range == null) 0 else composed.count { it < range.first }
    val fakeImpressions = tracker.effectLogged.count { it !in tracker.everVisible }
    val duplicates = tracker.firstVisibleCount - tracker.firstVisibleSeen.size

    ReadoutBox {
        ReadoutRow(
            "화면에 걸친 아이템",
            if (range == null) "없음" else "#${range.first}–#${range.last} (${range.last - range.first + 1}개)"
        )
        ReadoutRow("살아 있는 컴포지션", "${composed.size}개 · ${composed.toRanges()}")
        ReadoutRow("  그중 화면 밖", "앞쪽(ahead) ${ahead}개 · 지나간 쪽(behind) ${behind}개", highlight = true)
        Spacer(modifier = Modifier.height(6.dp))
        ReadoutRow(
            "❌ LaunchedEffect",
            "${tracker.effectLogCount}회 · 전부 보인 적 없는 아이템 ${fakeImpressions}개",
            highlight = true
        )
        ReadoutRow(
            "✅ 보임(리스트 기준)",
            "지금 ${tracker.visibleInList.size}개 · ${tracker.visibleInList.toSet().toRanges()}"
        )
        ReadoutRow(
            "   보임(윈도우 기준)",
            "지금 ${tracker.visibleInWindow.size}개 · ${tracker.visibleInWindow.toSet().toRanges()}"
        )
        ReadoutRow(
            "✅ 보임 + 본 key",
            "고유 ${tracker.impressedOnce.size}개 · 중복 0회 · ${tracker.impressedOnce.toSet().toRanges()}"
        )
        ReadoutRow(
            "⚠️ onFirstVisible",
            "${tracker.firstVisibleCount}회 · 고유 ${tracker.firstVisibleSeen.size}개 · 중복 ${duplicates}회 · " +
                tracker.firstVisibleSeen.toSet().toRanges(),
            highlight = duplicates > 0
        )
    }
}

// ==================== 3. 노출 추적 해설 ====================

@Composable
private fun ImpressionGuideCard() {
    CacheCard {
        CardTitle("2. 노출은 컴포지션이 아니라 배치·가시성으로 센다")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "캐시 윈도우가 미리 컴포즈하는 아이템은 prefetch 단계에서 compose 와 apply 까지 끝난다. 그래서 " +
                "LaunchedEffect·DisposableEffect 는 화면에 나오기 전에 이미 실행되고, 이것을 노출 로그로 쓰면 " +
                "사용자가 본 적 없는 아이템이 노출로 집계된다. 윈도우가 클수록 가짜 노출이 늘어난다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeText(
            """
            // ❌ 미리 컴포즈될 때도 실행된다
            LaunchedEffect(item.id) { logImpression(item.id) }

            // ✅ 리스트 경계 안에 절반 이상, 0.5초 이상 보였을 때
            val listBounds = remember { LayoutBoundsHolder() }
            LazyColumn(Modifier.layoutBounds(listBounds)) {
                items(feed, key = { it.id }) { item ->
                    Row(Modifier.onVisibilityChanged(
                        minDurationMs = 500,
                        minFractionVisible = 0.5f,
                        viewportBounds = listBounds,
                    ) { visible -> if (visible) logImpression(item.id) })
                }
            }
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(10.dp))
        StatTable(
            listOf(
                "미리 컴포즈된 아이템" to "배치되지 않아 가시성 콜백이 오지 않는다(위치 변경 감시가 배치 뒤에 시작)",
                "behind 로 유지된 아이템" to "배치에서 빠지면 onUnplaced 로 false 가 온다 — 컴포지션은 살아 있어도",
                "minFractionVisible" to "기본 1f = 전부 보여야 true. 0f 는 1px 이라도 보이면 true",
                "minDurationMs" to "보임은 이 시간만큼 기다렸다 알리고, 안 보임은 즉시 알린다",
                "viewport 미지정" to "앱 윈도우 기준 — 리스트 경계에 잘린 아이템도 윈도우 안이면 '전부 보임'",
                "viewport = 리스트 경계" to "fractionVisibleIn 은 윈도우와 겹치지 않는다 — 페이지가 스크롤돼 리스트가 화면 밖에 있어도 보임으로 남는다",
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "onFirstVisible 은 '아이템당 한 번'이 아니라 '노드가 부착돼 있는 동안 한 번'이라 아이템이 내려놓아졌다가 " +
                "돌아오면 또 온다. 이 때문에 foundation 이 아닌 ui 1.11.0 에서 deprecated 됐고(1.10.0 까지는 경고 없음), " +
                "공식 대안은 onVisibilityChanged 의 true 를 앱이 이미 본 key 로 거르는 것이다. 위 실측의 왕복 버튼으로 " +
                "두 방식의 중복 횟수를 모드별로 비교할 수 있다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "실측(이 기기): 이 페이지를 위아래로 스크롤하면, 리스트에 20dp 만 걸친 여섯 번째 아이템에도 onFirstVisible 이 " +
                "온다. 같은 viewport·같은 기준(1f)의 onVisibilityChanged 는 오지 않는다. 원인은 확정하지 못했다."
        )
    }
}

// ==================== 4. Pausable composition ====================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PausableCompositionCard() {
    // 화면에서는 바꾸지 않는다 — 읽기만 한다(아래 설명 참조)
    val pausableEnabled = remember { ComposeFoundationFlags.isPausableCompositionInPrefetchEnabled }
    var generation by remember { mutableIntStateOf(0) }

    CacheCard {
        CardTitle("3. 무거운 아이템과 prefetch 시간 예산")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "prefetch 는 다음 프레임까지 남은 시간 안에서만 일한다. Pausable composition 이 켜져 있으면 " +
                "아이템을 컴포즈하다 시간이 다 되면 멈췄다가 다음 프레임에 이어서 컴포즈한다(중단 지점은 새로 " +
                "삽입되는 restartable 컴포저블 호출의 시작). 아래 리스트의 아이템은 자식 ${HEAVY_PART_COUNT}개가 " +
                "각각 ${HEAVY_PART_COST_NANOS / 1_000_000f}ms 를 쓰고, 자식마다 어느 프레임 사이에·어느 경로" +
                "(prefetch / 측정 패스)로 컴포즈됐는지를 기록한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        StatTable(
            listOf(
                "정지 화면" to "View 가 최근 2프레임 동안 그리지 않았으면 예산이 Long.MAX_VALUE — 시간 제한 없이 한 번에 컴포즈한다",
                "시작 조건" to "남은 시간 > 같은 contentType 의 평균 소요 시간일 때만 시작한다. 정지 상태에서 통째로 " +
                    "컴포즈하며 평균을 크게 배우면, 스크롤 중에는 시작하지 못하고 화면에 들어올 때 측정 패스에서 " +
                    "한 번에 컴포즈된다(끊김)",
                "이 리스트" to "기본 prefetch — 정지 상태에서는 미리 컴포즈하지 않아 첫 prefetch 가 스크롤 중에 일어난다",
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        ReadoutBox {
            ReadoutRow(
                "isPausableCompositionInPrefetchEnabled",
                pausableEnabled.toString(),
                highlight = true
            )
            ReadoutRow(
                "무거운 아이템",
                "자식 ${HEAVY_PART_COUNT}개 × ${HEAVY_PART_COST_NANOS / 1_000_000f}ms ≈ " +
                    "${HEAVY_PART_COUNT * HEAVY_PART_COST_NANOS / 1_000_000f}ms"
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        CacheChip(
            label = if (generation == 0) "무거운 리스트 만들기" else "다시 만들기",
            selected = false
        ) { generation++ }

        if (generation > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            key(generation) { HeavyListSection() }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "실측(SM-A725F, 4초 스크롤): release 는 스크롤 중 컴포즈된 10개 중 prefetch 8개, 그중 6개가 2조각" +
                "(가장 긴 조각 15–18ms, 나뉘지 않은 아이템 약 20.5ms). debug 는 켜짐에서 prefetch 5개 중 3개가 2조각, " +
                "Application.onCreate 에서 끈 빌드에서는 prefetch 4개 전부 1조각이었다. 같은 리스트에 캐시 윈도우" +
                "(ahead 한 화면)를 걸면 정지 상태에서 미리 컴포즈하며 평균을 배워, 스크롤 중 아이템이 전부 측정 패스로 " +
                "컴포즈됐다. debug 빌드는 HotSwan 의 재작성 때문에 멈추지 않아 이 파일을 HotSwan 대상에서 뺐다(app/build.gradle)."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "이 플래그는 화면에서 바꾸지 않는다. 공식 문서는 라이브러리가 로드된 뒤의 변경을 정의되지 않은 동작으로 보고 " +
                "Application.onCreate 에서 설정하라고 한다. prefetch 가 실행될 때마다 값을 읽기 때문에, " +
                "멈춰 있는 컴포지션이 남은 채로 끄면 같은 아이템을 다른 경로로 다시 컴포즈하게 된다 — " +
                "이 화면을 담고 있는 바깥 LazyColumn 도 같은 플래그를 쓴다."
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeavyListSection() {
    val frames = remember { FrameCounter() }
    val recorder = remember { SliceRecorder(frames) }
    val scope = rememberCoroutineScope()
    var scrolled by remember { mutableStateOf(false) }
    // 리스트가 처음 측정되기 전(이펙트는 같은 프레임의 레이아웃보다 먼저 실행된다)에 세기 시작한다
    DisposableEffect(Unit) {
        frames.start()
        onDispose { frames.stop() }
    }
    // 기본 prefetch — 정지 상태에서는 아무것도 미리 컴포즈하지 않아, 첫 prefetch 가 스크롤 중에 일어난다
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(HEAVY_LIST_HEIGHT)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
    ) {
        items(count = HEAVY_ITEM_COUNT, key = { it }) { index ->
            HeavyItem(index = index, recorder = recorder)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    CacheChip(label = if (scrolled) "스크롤 완료" else "천천히 스크롤(4초)", selected = scrolled) {
        if (!scrolled) {
            scrolled = true
            scope.launch {
                recorder.scrolling = true
                val viewport = listState.layoutInfo.viewportSize.height
                listState.animateScrollBy(
                    value = viewport * 3f,
                    animationSpec = tween(durationMillis = 4000, easing = LinearEasing)
                )
                recorder.scrolling = false
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    ReadoutBox {
        val idle = recorder.results.filterNot { it.duringScroll }
        val moving = recorder.results.filter { it.duringScroll }
        ReadoutRow("정지 상태에서 컴포즈", sliceSummary(idle), highlight = false)
        ReadoutRow("스크롤 중 컴포즈", sliceSummary(moving), highlight = moving.any { it.slices > 1 })
        moving.take(8).forEach { result ->
            ReadoutRow(
                "  #${result.index} " + if (result.viaPrefetch) "prefetch" else "측정 패스",
                "${result.slices}조각 · 가장 긴 조각 ${"%.1f".format(Locale.ROOT, result.longestChunkMs)}ms",
                highlight = result.slices > 1
            )
        }
    }
}

private fun sliceSummary(results: List<SliceResult>): String {
    if (results.isEmpty()) return "아직 없음"
    val split = results.count { it.slices > 1 }
    val prefetched = results.count { it.viaPrefetch }
    val longest = results.maxOf { it.longestChunkMs }
    return "${results.size}개(prefetch ${prefetched}) · 나뉜 아이템 ${split}개 · " +
        "가장 긴 조각 ${"%.1f".format(Locale.ROOT, longest)}ms"
}

@Composable
private fun HeavyItem(index: Int, recorder: SliceRecorder) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "무거운 아이템 #$index",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5D4037)
        )
        Spacer(modifier = Modifier.height(4.dp))
        repeat(HEAVY_PART_COUNT) { part ->
            HeavyPart(index = index, part = part, recorder = recorder)
        }
    }
}

/** 자식 하나 — 이 호출의 시작이 Pausable composition 이 멈출 수 있는 지점이다 */
@Composable
private fun HeavyPart(index: Int, part: Int, recorder: SliceRecorder) {
    // 무거운 계산을 흉내 낸다 — 컴포지션 1회당 한 번(remember 계산 안)만 실행된다
    remember(index, part) {
        recorder.record(index)
        val start = System.nanoTime()
        while (System.nanoTime() - start < HEAVY_PART_COST_NANOS) {
            // 바쁜 대기
        }
        part
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .padding(vertical = 1.dp)
            .background(Color(0xFFBCAAA4))
    )
}

// ==================== 5. 함정 정리 ====================

@Composable
private fun CacheWindowPitfallCard() {
    CacheCard {
        CardTitle("정리")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "• 캐시 윈도우를 키우면 스크롤 중 끊김은 줄지만 살아 있는 컴포지션이 늘어난다 — remember 로 들고 있는 " +
                "데이터와 실행 중인 이펙트도 그만큼 늘어난다.\n" +
                "• 노출·자동재생·분석 이벤트를 LaunchedEffect 에 걸지 않는다. onVisibilityChanged 를 쓰고, " +
                "중첩 리스트라면 viewportBounds 로 리스트 경계를 넘긴다.\n" +
                "• viewport 를 리스트 경계로 잡으면 페이지 스크롤로 리스트가 화면 밖에 있는 경우를 모른다 — " +
                "둘 다 중요하면 두 조건을 함께 본다.\n" +
                "• onFirstVisible 은 deprecated 다 — 노드가 다시 부착될 때마다 다시 온다. 아이템당 한 번은 " +
                "onVisibilityChanged 와 본 key 집합으로 만든다.\n" +
                "• behind 로 유지된 아이템이 돌아오면 다시 컴포즈하지 않는다 — 재사용 풀에서 꺼내 다시 컴포즈하는 " +
                "것보다 빠르지만, 그 사이 이펙트가 계속 돈다.\n" +
                "• 무거운 아이템이 정지 상태에서 통째로 미리 컴포즈되면 prefetch 가 평균 시간을 크게 배워, 스크롤 중에는 " +
                "시작하지 못하고 측정 패스에서 끊긴다 — 아이템 자체를 가볍게 나누는 것이 먼저다.\n" +
                "• ComposeFoundationFlags 는 앱 시작 시점에만 바꾼다."
        )
    }
}

// ==================== 공용 UI 조각 ====================

@Composable
private fun CacheCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun CardTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF388E3C)
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color(0xFF424242),
        lineHeight = 18.sp
    )
}

@Composable
private fun CaptionText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF757575),
        lineHeight = 16.sp
    )
}

@Composable
private fun CodeText(code: String) {
    Text(
        text = code,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFE0E0E0),
        lineHeight = 15.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(10.dp)
    )
}

@Composable
private fun StatTable(rows: List<Pair<String, String>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEach { (key, value) ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(
                    text = key,
                    modifier = Modifier.width(130.dp),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF1976D2)
                )
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    color = Color(0xFF616161),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun ReadoutBox(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp),
        content = content
    )
}

@Composable
private fun ReadoutRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF90A4AE),
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) Color(0xFFFF8A80) else Color(0xFFE0E0E0)
        )
    }
}

@Composable
private fun CacheChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF1976D2) else Color(0xFFE3F2FD))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF1976D2),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
