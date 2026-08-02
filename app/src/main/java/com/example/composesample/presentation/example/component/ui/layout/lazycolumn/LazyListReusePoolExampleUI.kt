package com.example.composesample.presentation.example.component.ui.layout.lazycolumn

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

// ==================== 상수 ====================

/** 데모 리스트 아이템 수. 전부 훑어야 재사용 풀에 슬롯이 쌓인다 */
private const val REUSE_ITEM_COUNT = 200

/** 아이템 하나가 붙들고 있는 데이터 크기(64KB). 실제 앱에서는 비트맵·파싱 결과·리스트 스냅샷이 이 자리에 온다 */
private const val REUSE_PAYLOAD_BYTES = 64 * 1024

/**
 * contentType 하나당 재사용 풀이 유지하는 슬롯 수.
 *
 * foundation 1.11.1 의 `LazyLayoutItemReusePolicy.getSlotsToRetain` 을 디스어셈블해 확인한 값으로,
 * 공개 API 가 아닌 내부 구현 상수다(버전에 따라 바뀔 수 있음). 중요한 것은 숫자 자체가 아니라
 * **"타입별로만 상한이 있고 전체 개수에는 상한이 없다"** 는 구조다.
 */
private const val REUSE_SLOTS_PER_TYPE = 7

/** 자동 스크롤 1회 이동량(px) */
private const val REUSE_SCROLL_STEP_PX = 1500f

/** 자동 스크롤 최대 반복 횟수(리스트 끝에 닿지 못하는 상황에서의 안전장치) */
private const val REUSE_SCROLL_MAX_STEPS = 120

// ==================== 모델 ====================

/**
 * 데모용 아이템.
 *
 * 실제 "종류"는 Node / Leaf 둘뿐이지만 id·label 은 아이템마다 전부 다르다.
 * 이 차이가 contentType 을 무엇으로 잡느냐에 따라 버킷 수를 1~2개로도, 200개로도 만든다.
 */
private sealed interface ReuseItem {
    val id: Int
    val label: String

    data class Node(override val id: Int, override val label: String) : ReuseItem
    data class Leaf(override val id: Int, override val label: String) : ReuseItem
}

/** contentType 을 무엇으로 잡을지 — 이 예제의 유일한 변수 */
private enum class ReuseContentTypeMode(
    val label: String,
    val code: String,
    val bucketCount: Int,
    val description: String,
) {
    NONE(
        label = "지정 안 함",
        code = "contentType 미지정 (= null)",
        bucketCount = 1,
        description = "모든 아이템이 같은 null 타입 → 버킷 1개. 이종 아이템끼리도 슬롯을 돌려 쓰다 보니 " +
                "재사용 효율은 떨어지지만, 보유량은 7개로 묶여 있어 메모리는 안전하다.",
    ),
    BY_CLASS(
        label = "클래스 단위",
        code = "contentType = { item::class }",
        bucketCount = 2,
        description = "실제 레이아웃이 다른 만큼만 타입을 나눈다(Node/Leaf 2종) → 버킷 2개. " +
                "같은 종류끼리만 슬롯을 재사용하므로 효율도 좋고 보유량도 2×7 로 묶인다. 권장 형태.",
    ),
    BY_ITEM(
        label = "아이템 고유값",
        code = "contentType = { item.label }",
        bucketCount = REUSE_ITEM_COUNT,
        description = "아이템마다 값이 달라 버킷이 아이템 수만큼 생긴다 → 각 버킷에 1개씩만 들어 있어 " +
                "정리 대상이 되지 않고, 스크롤한 아이템의 슬롯이 전부 남는다. 이 예제가 재현하는 함정.",
    ),
    ;

    /** 이 모드가 아이템에 부여하는 contentType 값 */
    fun contentTypeOf(item: ReuseItem): Any? = when (this) {
        NONE -> null
        BY_CLASS -> item::class
        BY_ITEM -> item.label
    }

    /** 이론상 재사용 풀이 붙들 수 있는 슬롯 수 상한 */
    val retainLimit: Int get() = bucketCount * REUSE_SLOTS_PER_TYPE
}

/**
 * 아이템 컴포지션이 붙들고 있는 데이터.
 *
 * 살아있는 개수를 세기 위해 [ReuseTracker] 가 약한 참조로만 추적한다 —
 * 추적기 자신이 강한 참조를 들면 측정 자체가 누수가 된다.
 */
private class ReusePayload(val label: String, val tint: Color) {
    private val blob = ByteArray(REUSE_PAYLOAD_BYTES)

    /** 이 페이로드가 차지하는 크기(KB) */
    val weightKb: Int = blob.size / 1024
}

/** GC 유도 후 측정한 결과 */
private data class ReuseMeasurement(
    val createdCount: Int,
    val aliveCount: Int,
    val heapMb: Float,
) {
    /** 살아남은 페이로드가 붙들고 있는 대략적인 크기(MB) */
    val retainedMb: Float get() = aliveCount * REUSE_PAYLOAD_BYTES / (1024f * 1024f)
}

/**
 * 아이템 페이로드의 생존 여부를 추적한다.
 *
 * 생성 카운터와 참조 목록은 **스냅샷 상태가 아니다** — 아이템 컴포지션 도중에 갱신되기 때문에
 * 상태로 두면 컴포지션 중 상태 쓰기가 되어 불필요한 재실행을 부른다.
 * 화면에 보여줄 값은 [measure] 시점에만 [measurement] 로 옮긴다.
 */
@Stable
private class ReuseTracker {
    private val refs = mutableListOf<WeakReference<ReusePayload>>()
    private var createdCount = 0

    var measurement by mutableStateOf<ReuseMeasurement?>(null)
        private set

    fun register(payload: ReusePayload) {
        refs += WeakReference(payload)
        createdCount++
    }

    /**
     * GC 를 유도한 뒤 아직 회수되지 않은 페이로드 수를 센다.
     *
     * System.gc() 는 "수집해 달라"는 힌트일 뿐 즉시 수행을 보장하지 않으므로
     * 짧은 지연을 두고 여러 번 호출한다. 그래도 수치는 매번 조금씩 흔들릴 수 있다.
     */
    suspend fun measure() {
        repeat(3) {
            System.gc()
            System.runFinalization()
            delay(120)
        }
        refs.removeAll { it.get() == null }

        val runtime = Runtime.getRuntime()
        measurement = ReuseMeasurement(
            createdCount = createdCount,
            aliveCount = refs.size,
            heapMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f),
        )
    }

    fun reset() {
        refs.clear()
        createdCount = 0
        measurement = null
    }
}

// ==================== 화면 ====================

@Composable
fun LazyListReusePoolExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "LazyList contentType 재사용 풀",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ReusePoolOverviewCard() }
            item { ReusePoolDemoCard() }
            item { ReusePoolMechanismCard() }
            item { ReusePoolCodeCard() }
            item { ReusePoolSummaryCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun ReusePoolOverviewCard() {
    ReusePoolCard {
        Text(
            text = "한 줄짜리 contentType 이 메모리를 먹는 이유",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "LazyColumn 은 화면 밖으로 나간 아이템의 컴포지션(슬롯)을 바로 버리지 않고 " +
                    "재사용 풀에 넣어 뒀다가 새 아이템에 다시 씁니다. 이때 어떤 슬롯을 어떤 아이템에 " +
                    "돌려 쓸지 가르는 기준이 contentType 입니다.\n\n" +
                    "문제는 이 풀의 정리 규칙이 **타입별**이라는 점입니다. Compose 는 contentType 하나당 " +
                    "$REUSE_SLOTS_PER_TYPE 개까지만 남기고 초과분을 버리는데, 전체 개수에 대한 상한은 없습니다. " +
                    "그래서 contentType 에 아이템마다 다른 값을 넘기면 버킷이 아이템 수만큼 생기고, " +
                    "각 버킷에는 1개씩만 들어 있어 **정리 조건에 영원히 걸리지 않습니다**.\n\n" +
                    "남은 슬롯은 자기 컴포지션이 들고 있던 데이터와 modifier 람다가 캡처한 객체까지 " +
                    "함께 붙들고 있으므로, 스크롤할수록 회수되지 않는 메모리가 쌓입니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
    }
}

// ==================== 2. 핵심 실동작 ====================

@Composable
private fun ReusePoolDemoCard() {
    val items = remember {
        List(REUSE_ITEM_COUNT) { index ->
            val label = "item-$index"
            if (index % 3 == 0) ReuseItem.Node(index, label) else ReuseItem.Leaf(index, label)
        }
    }
    val tracker = remember { ReuseTracker() }
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf(ReuseContentTypeMode.BY_CLASS) }
    var busy by remember { mutableStateOf(false) }

    // 모드를 바꿨는데 이전 풀이 남아 있으면 비교가 성립하지 않는다 → 추적기를 비운다.
    // (리스트 자체는 아래 key(mode) 로 통째로 새로 만든다)
    LaunchedEffect(mode) { tracker.reset() }

    ReusePoolCard {
        Text(
            text = "1. contentType 을 바꿔가며 실측",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "아이템 ${REUSE_ITEM_COUNT}개, 각 아이템은 ${REUSE_PAYLOAD_BYTES / 1024}KB 데이터를 붙들고 있습니다. " +
                    "모드를 고르고 ① 끝까지 훑기 → ② GC 후 측정 순서로 눌러 보세요. " +
                    "같은 리스트를 같은 만큼 스크롤해도 살아남는 페이로드 수가 달라집니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ReuseContentTypeMode.entries.forEach { entry ->
                ReusePoolChip(
                    label = entry.label,
                    selected = mode == entry,
                    warning = entry == ReuseContentTypeMode.BY_ITEM,
                    onClick = { if (!busy) mode = entry }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mode.code,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mode.description,
            fontSize = 11.sp,
            color = Color(0xFF757575),
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // key(mode): 모드가 바뀌면 리스트 서브컴포지션을 통째로 새로 만들어 이전 재사용 풀을 버린다
        key(mode) {
            val listState = rememberLazyListState()

            ReuseDemoList(
                items = items,
                mode = mode,
                tracker = tracker,
                listState = listState
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ReusePoolChip(
                    label = "① 끝까지 훑기",
                    selected = false,
                    warning = false,
                    onClick = {
                        if (!busy) {
                            busy = true
                            scope.launch {
                                scrollThroughAll(listState)
                                busy = false
                            }
                        }
                    }
                )
                ReusePoolChip(
                    label = "② GC 후 측정",
                    selected = false,
                    warning = false,
                    onClick = {
                        if (!busy) {
                            busy = true
                            scope.launch {
                                tracker.measure()
                                busy = false
                            }
                        }
                    }
                )
                ReusePoolChip(
                    label = "초기화",
                    selected = false,
                    warning = false,
                    onClick = { if (!busy) tracker.reset() }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        ReuseMeasurementBox(mode = mode, busy = busy, measurement = tracker.measurement)
    }
}

/** 실제 관측 대상 리스트 */
@Composable
private fun ReuseDemoList(
    items: List<ReuseItem>,
    mode: ReuseContentTypeMode,
    tracker: ReuseTracker,
    listState: LazyListState,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            items = items,
            key = { item -> item.id },
            // 이 한 줄이 이 예제의 전부다 — 여기에 무엇을 넘기느냐로 재사용 풀의 크기가 결정된다
            contentType = { item -> mode.contentTypeOf(item) }
        ) { item ->
            ReuseItemRow(item = item, tracker = tracker)
        }
    }
}

@Composable
private fun ReuseItemRow(item: ReuseItem, tracker: ReuseTracker) {
    val isNode = item is ReuseItem.Node

    // 이 컴포지션이 붙들고 있는 데이터.
    // 슬롯이 재사용 풀에 남아 있는 동안에는 이 객체도 함께 살아남는다.
    // (관측이 목적이라 컴포지션 1회당 1번만 실행되는 remember 계산 안에서 등록한다)
    val payload = remember(item.id) {
        ReusePayload(
            label = item.label,
            tint = if (isNode) Color(0xFF1976D2) else Color(0xFF43A047)
        ).also(tracker::register)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            // 드로우 람다가 payload 를 캡처한다 — 아티클에서 LayoutNode 가 붙들고 있던
            // "modifier 가 캡처한 람다"와 같은 모양을 의도적으로 재현한 부분
            .drawBehind {
                drawRect(color = payload.tint, size = Size(6.dp.toPx(), size.height))
            }
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payload.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            Text(
                text = "${if (isNode) "Node" else "Leaf"} · ${payload.weightKb}KB 보유",
                fontSize = 10.sp,
                color = Color(0xFF9E9E9E)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(payload.tint.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (isNode) "Node" else "Leaf",
                fontSize = 10.sp,
                color = payload.tint,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** 측정 결과 표시 */
@Composable
private fun ReuseMeasurementBox(
    mode: ReuseContentTypeMode,
    busy: Boolean,
    measurement: ReuseMeasurement?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp)
    ) {
        ReuseStatRow("contentType 버킷 수", "${mode.bucketCount}개")
        ReuseStatRow(
            label = "이론상 보유 상한",
            value = "${mode.bucketCount} × $REUSE_SLOTS_PER_TYPE = ${mode.retainLimit}개 슬롯"
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            busy -> Text(
                text = "측정 중…",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFFD740)
            )

            measurement == null -> Text(
                text = "① 끝까지 훑기 → ② GC 후 측정 을 눌러 주세요.",
                fontSize = 11.sp,
                color = Color(0xFF90A4AE),
                lineHeight = 16.sp
            )

            else -> {
                ReuseStatRow("생성된 페이로드", "${measurement.createdCount}개")
                ReuseStatRow(
                    label = "GC 후 살아있는 수",
                    value = "${measurement.aliveCount}개",
                    highlight = true
                )
                ReuseStatRow(
                    label = "살아있는 페이로드 크기",
                    value = String.format("%.1f MB", measurement.retainedMb),
                    highlight = true
                )
                ReuseStatRow(
                    label = "앱 힙 사용량",
                    value = String.format("%.1f MB", measurement.heapMb)
                )
            }
        }
    }
}

@Composable
private fun ReuseStatRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF90A4AE),
            modifier = Modifier.width(150.dp)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) Color(0xFFFF8A80) else Color(0xFFE0E0E0)
        )
    }
}

/**
 * 리스트를 끝까지 훑는다.
 *
 * animateScrollToItem 으로 먼 인덱스를 지정하면 중간 아이템을 건너뛰므로
 * 슬롯이 풀에 쌓이지 않는다 → 화면 높이 단위로 나눠 스크롤해야 관측이 성립한다.
 */
private suspend fun scrollThroughAll(listState: LazyListState) {
    listState.scrollToItem(0)

    var steps = 0
    while (listState.canScrollForward && steps < REUSE_SCROLL_MAX_STEPS) {
        listState.animateScrollBy(
            value = REUSE_SCROLL_STEP_PX,
            animationSpec = tween(durationMillis = 90, easing = LinearEasing)
        )
        steps++
    }
}

// ==================== 3. 정리 규칙 ====================

@Composable
private fun ReusePoolMechanismCard() {
    ReusePoolCard {
        Text(
            text = "2. 풀 정리 규칙은 '타입별'이다",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "LazyLayout 은 재사용 풀을 정리할 때 슬롯을 순회하면서 " +
                    "'이 타입은 이미 $REUSE_SLOTS_PER_TYPE 개 확보했는가'만 확인하고 초과분을 버립니다. " +
                    "전체 슬롯 수는 세지 않습니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReusePoolCodeBlock(
            code = """
                // Compose 내부 정리 로직(개념 재현)
                fun getSlotsToRetain(slotIds: SlotIdsSet) {
                    countPerType.clear()
                    slotIds.forEach { slotId ->
                        val type = contentTypeOf(slotId)
                        val current = countPerType.getOrDefault(type, 0)
                        if (current == 7) {
                            slotIds.remove(slotId)   // 타입당 7개 초과분만 버린다
                        } else {
                            countPerType[type] = current + 1
                        }
                    }
                    // 전체 슬롯 수에 대한 상한은 없다
                }
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "버킷이 1~2개면 보유량은 7~14개로 묶입니다. 그런데 버킷이 아이템 수만큼 생기면 " +
                    "각 버킷의 개수는 항상 1이라 `current == 7` 조건에 도달하지 못하고, " +
                    "결국 스크롤로 지나친 아이템의 슬롯이 전부 남습니다.\n\n" +
                    "남은 슬롯은 단순한 빈 껍데기가 아닙니다. 그 컴포지션이 remember 로 들고 있던 값과 " +
                    "modifier 람다가 캡처한 객체까지 도달 가능한 상태로 유지되므로, " +
                    "아이템 하나가 큰 데이터를 참조하고 있었다면 그만큼이 회수되지 않습니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
    }
}

// ==================== 4. 코드 대조 ====================

@Composable
private fun ReusePoolCodeCard() {
    ReusePoolCard {
        Text(
            text = "3. key 와 contentType 은 다른 축이다",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "둘 다 items() 에 넘기는 람다라 헷갈리기 쉽지만 역할이 정반대입니다. " +
                    "key 는 '이 아이템이 그 아이템인가'를 가리는 **식별자**라 아이템마다 달라야 하고, " +
                    "contentType 은 '이 슬롯을 저 아이템에 돌려 써도 되는가'를 가리는 **분류**라 " +
                    "종류 수만큼만 있어야 합니다. key 를 쓰던 감각으로 contentType 에 " +
                    "아이템 고유값을 넣는 순간 함정에 빠집니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReusePoolCodeBlock(
            code = """
                // ❌ 아이템마다 고유한 값 → 버킷이 아이템 수만큼
                items(
                    items = rows,
                    key = { it.id },
                    contentType = { it.data }
                ) { ... }

                // ✅ 실제 레이아웃 종류만큼만
                items(
                    items = rows,
                    key = { it.id },
                    contentType = { it.data?.let { data -> data::class } }
                ) { ... }

                // ✅ enum·상수 문자열도 좋다 (개수가 고정이라면)
                contentType = { if (it.isHeader) "header" else "row" }
            """.trimIndent()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "판별 기준은 간단합니다 — contentType 으로 넘길 값의 **가짓수가 데이터 양에 따라 " +
                    "늘어난다면 잘못된 것**입니다. 화면에 그려지는 레이아웃 형태가 몇 가지인지 세어 보고, " +
                    "그 수를 넘지 않는 값을 쓰면 됩니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
    }
}

// ==================== 5. 정리 ====================

@Composable
private fun ReusePoolSummaryCard() {
    ReusePoolCard {
        Text(
            text = "정리",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• contentType 은 식별자가 아니라 분류다 — 가짓수가 데이터 양에 비례하면 잘못 쓴 것이다.\n" +
                    "• 정리 규칙에 전역 상한이 없다 — 타입 수를 늘리는 만큼 보유 가능한 슬롯도 늘어난다.\n" +
                    "• 잘못 지정하는 것이 지정하지 않는 것보다 나쁘다 — null 은 버킷 1개지만 " +
                    "고유값은 버킷 N개다.\n" +
                    "• 남은 슬롯은 자기 컴포지션이 참조하던 객체까지 붙들고 있다 — 아이템이 무거울수록 손해가 크다.\n" +
                    "• 이 유형은 크래시로 드러나지 않는다 — 스크롤을 많이 하는 화면에서 " +
                    "천천히 힙이 오르는 형태라 힙 덤프로 봐야 보인다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "※ System.gc() 는 수집을 보장하지 않으므로 측정값은 실행할 때마다 조금씩 다를 수 있습니다. " +
                    "절대 수치보다 모드 간 차이를 보세요.",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E),
            lineHeight = 16.sp
        )
    }
}

// ==================== 공용 UI 조각 ====================

@Composable
private fun ReusePoolCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun ReusePoolChip(
    label: String,
    selected: Boolean,
    warning: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = if (warning) Color(0xFFD32F2F) else Color(0xFF1976D2)
    val idleColor = if (warning) Color(0xFFFFEBEE) else Color(0xFFE3F2FD)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeColor else idleColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else activeColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReusePoolCodeBlock(code: String) {
    Text(
        text = code,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFE0E0E0),
        lineHeight = 17.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp)
    )
}
