package com.example.composesample.presentation.example.component.architecture.pattern.retain

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.LocalRetainedValuesStoreProvider
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.retain.retainManagedRetainedValuesStore
import androidx.compose.runtime.retain.retainRetainedValuesStoreRegistry
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeLayoutState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

/**
 * Retain API Example UI
 *
 * runtime-retain 1.12.1(Compose BOM 2026.09.00 의 전이 의존성)의 실제 retain API 를 쓴다. 탭 4개:
 * - 재생성: 같은 카운터를 remember / rememberSaveable / retain / ViewModel 로 보관하고 Activity.recreate()(다크 모드와 같은
 *   구성 변경 경로)·예제 목록 왕복·프로세스 종료에서 무엇이 남는지, 진행 중인 2초 작업이 재생성을 넘는지 본다
 * - 콜백: RetainObserver 다섯 콜백의 실제 순서 — 기본 저장소에서 숨기기 / ManagedRetainedValuesStore / 키 변경 /
 *   적용되지 않은 컴포지션(onUnused) / RetainedEffect 와 DisposableEffect
 * - 탭 보존: RetainedValuesStoreRegistry 로 탭마다 보존 저장소를 두기, key() 를 빠뜨리면 다른 탭의 값이 돌아오는 함정
 * - 비교: ViewModel 과 retain 의 범위 차이 요약
 *
 * 모든 콜백은 화면 아래 기록과 logcat(태그 RetainApiExample)에 같은 번호로 남는다.
 */

// ==================== 이벤트 기록 ====================

private data class RetainEvent(val seq: Int, val subject: String, val event: String)

/**
 * 이 예제의 이벤트 기록 — 프로세스 단위로 둔다.
 * 예제를 닫을 때 불리는 onRetired·onCleared 도 다시 들어왔을 때 보이게 하려는 것이다(프로세스가 죽으면 함께 사라진다).
 * 콜백은 컴포지션 안(생성자)이나 적용 단계(onRemembered 등)에서 불리므로 화면 상태 쓰기는 메인 루퍼의 다음 메시지로 미루고,
 * 번호만 즉시 매겨 실제 호출 순서를 남긴다.
 */
private object RetainEventLog {
    const val TAG = "RetainApiExample"
    private const val MAX_EVENTS = 300
    private val mainHandler = Handler(Looper.getMainLooper())
    private val nextSeq = AtomicInteger(1)
    private val issued = HashMap<String, Int>()

    val events = mutableStateListOf<RetainEvent>()

    /**
     * 종류별 인스턴스 번호 — "retain#2" 는 이 프로세스에서 두 번째로 만들어진 retain 카운터.
     * @Synchronized 대신 synchronized 블록 — debug(HotSwan) 재작성 아래에서는 @Synchronized 의 상호 배제가 사라진다(ComposeHotReloadGuide).
     */
    fun nextNumber(kind: String): Int = synchronized(issued) {
        val next = (issued[kind] ?: 0) + 1
        issued[kind] = next
        next
    }

    fun add(subject: String, event: String) {
        val item = RetainEvent(nextSeq.getAndIncrement(), subject, event)
        Log.d(TAG, "#${item.seq} $subject $event [${Thread.currentThread().name}]")
        mainHandler.post {
            events.add(item)
            while (events.size > MAX_EVENTS) events.removeAt(0)
        }
    }

    fun clear() {
        mainHandler.post { events.clear() }
    }
}

// ==================== 보관 대상 ====================

private enum class PendingWork(val label: String) {
    NONE("—"), RUNNING("진행 중"), DONE("완료"), CANCELLED("취소됨")
}

/**
 * 재생성 탭의 카운터 — 같은 클래스를 remember 와 retain 에 각각 넣는다.
 * RememberObserver·RetainObserver 를 모두 구현했지만 retain 으로 보관하면 RememberObserver 콜백은 오지 않는다(KDoc).
 * 진행 중 작업은 보관 방식의 '마지막 콜백'에서 취소한다 — remember 는 onForgotten, retain 은 onRetired/onUnused.
 */
private class DemoCounter(kind: String) : RememberObserver, RetainObserver {
    val name = "$kind#${RetainEventLog.nextNumber(kind)}"
    var count by mutableIntStateOf(0)
        private set
    var work by mutableStateOf(PendingWork.NONE)
        private set
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun increment() {
        count++
    }

    fun incrementLater() {
        if (work == PendingWork.RUNNING) return
        work = PendingWork.RUNNING
        RetainEventLog.add(name, "2초 작업 시작")
        scope.launch {
            delay(WORK_MILLIS)
            count++
            work = PendingWork.DONE
            RetainEventLog.add(name, "2초 작업 완료 → +1")
        }
    }

    private fun release(reason: String) {
        if (work == PendingWork.RUNNING) {
            work = PendingWork.CANCELLED
            RetainEventLog.add(name, "$reason → 2초 작업 취소")
        }
        scope.cancel()
    }

    // remember 로 보관했을 때만 불린다
    override fun onRemembered() = RetainEventLog.add(name, "onRemembered")
    override fun onForgotten() {
        RetainEventLog.add(name, "onForgotten")
        release("onForgotten")
    }
    override fun onAbandoned() = release("onAbandoned")

    // retain 으로 보관했을 때만 불린다
    override fun onRetained() = RetainEventLog.add(name, "onRetained")
    override fun onEnteredComposition() = RetainEventLog.add(name, "onEnteredComposition")
    override fun onExitedComposition() = RetainEventLog.add(name, "onExitedComposition")
    override fun onRetired() {
        RetainEventLog.add(name, "onRetired")
        release("onRetired")
    }
    override fun onUnused() {
        RetainEventLog.add(name, "onUnused")
        release("onUnused")
    }
}

/**
 * 비교용 ViewModel — 이 앱에서 예제 화면의 ViewModelStoreOwner 는 BlogExampleActivity 다.
 * 그래서 예제를 닫아도 남아 있다가 BlogExampleActivity 가 끝날 때 onCleared 가 불린다.
 */
private class RetainCompareViewModel : ViewModel() {
    val name = "ViewModel#${RetainEventLog.nextNumber("ViewModel")}"

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    private val _work = MutableStateFlow(PendingWork.NONE)
    val work: StateFlow<PendingWork> = _work.asStateFlow()

    init {
        RetainEventLog.add(name, "생성")
    }

    fun increment() {
        _count.update { it + 1 }
    }

    fun incrementLater() {
        if (_work.value == PendingWork.RUNNING) return
        _work.value = PendingWork.RUNNING
        RetainEventLog.add(name, "2초 작업 시작")
        viewModelScope.launch {
            delay(WORK_MILLIS)
            _count.update { it + 1 }
            _work.value = PendingWork.DONE
            RetainEventLog.add(name, "2초 작업 완료 → +1")
        }
    }

    override fun onCleared() {
        RetainEventLog.add(name, "onCleared")
    }
}

/** 콜백 탭의 관찰 대상 — RetainObserver 콜백을 기록만 하는 객체 */
private class TracedValue(prefix: String, detail: String = "") : RetainObserver {
    val name = "$prefix#${RetainEventLog.nextNumber(prefix)}$detail"

    override fun onRetained() = RetainEventLog.add(name, "onRetained")
    override fun onEnteredComposition() = RetainEventLog.add(name, "onEnteredComposition")
    override fun onExitedComposition() = RetainEventLog.add(name, "onExitedComposition")
    override fun onRetired() = RetainEventLog.add(name, "onRetired")
    override fun onUnused() = RetainEventLog.add(name, "onUnused")
}

/** 탭 보존 탭의 탭별 카운터 */
private class TabCounter(tab: String) : RetainObserver {
    val name = "$tab#${RetainEventLog.nextNumber("탭:$tab")}"
    var count by mutableIntStateOf(0)
        private set

    fun increment() {
        count++
    }

    override fun onRetained() = RetainEventLog.add(name, "onRetained")
    override fun onEnteredComposition() = RetainEventLog.add(name, "onEnteredComposition")
    override fun onExitedComposition() = RetainEventLog.add(name, "onExitedComposition")
    override fun onRetired() = RetainEventLog.add(name, "onRetired")
    override fun onUnused() = RetainEventLog.add(name, "onUnused")
}

/**
 * ② 카드의 '보존 켜짐' 표시. 저장소와 수명을 맞추려고 저장소처럼 retain 한다 —
 * rememberSaveable 로 두면 프로세스 종료 뒤 새로 만든 저장소(켜짐)와 표시(꺼짐)가 어긋난다.
 */
private class RetentionSwitch {
    var enabled by mutableStateOf(true)
}

private const val WORK_MILLIS = 2_000L

private val TabNames = listOf("홈", "검색", "설정")

private enum class StoreMode(val label: String) {
    DEFAULT_KEYED("기본 저장소 + key()"),
    REGISTRY_KEYED("Registry + key()"),
    REGISTRY_NO_KEY("Registry, key() 없음"),
}

// ==================== 화면 ====================

@Composable
fun RetainApiExampleUI(
    onBackEvent: () -> Unit
) {
    // 재생성 뒤에도 같은 탭이 컴포즈돼야 그 탭의 retain 값이 다시 소비된다 — remember 였다면 재생성마다 첫 탭으로 돌아가
    // 보고 있던 탭의 값이 전부 onRetired 된다(소비되지 않은 값은 onResume 뒤 프레임 끝에 정리)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val activity = LocalActivity.current
    val recreate = {
        RetainEventLog.add("Activity", "recreate() 호출")
        activity?.recreate()
        Unit
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabItem("재생성", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
            TabItem("콜백", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
            TabItem("탭 보존", selectedTab == 2, { selectedTab = 2 }, Modifier.weight(1f))
            TabItem("비교", selectedTab == 3, { selectedTab = 3 }, Modifier.weight(1f))
        }

        RecreateBar(onRecreate = recreate)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> RecreationTab(onRecreate = recreate)
                1 -> CallbackTab()
                2 -> RegistryTab()
                3 -> ComparisonTab()
            }
        }

        EventLogPanel()
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Retain API",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "retain { } 실물 — runtime-retain 1.12.1",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1976D2) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF616161)
        )
    }
}

@Composable
private fun RecreateBar(onRecreate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onRecreate,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Activity.recreate()", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "다크 모드 전환과 같은 구성 변경 재생성 경로",
            fontSize = 11.sp,
            color = Color(0xFF757575),
            lineHeight = 15.sp
        )
    }
}

// ==================== 1. 재생성 ====================

@Composable
private fun RecreationTab(onRecreate: () -> Unit) {
    val remembered = remember { DemoCounter("remember") }
    var savedCount by rememberSaveable { mutableIntStateOf(0) }
    val retained = retain { DemoCounter("retain") }
    // private 클래스라 기본 팩토리(리플렉션)가 아니라 initializer 로 만든다 — PerItemViewModel 예제와 같은 방식
    val viewModel: RetainCompareViewModel = viewModel { RetainCompareViewModel() }
    val viewModelCount by viewModel.count.collectAsStateWithLifecycle()
    val viewModelWork by viewModel.work.collectAsStateWithLifecycle()

    val incrementAll = {
        remembered.increment()
        savedCount++
        retained.increment()
        viewModel.increment()
    }
    val incrementAllLater = {
        remembered.incrementLater()
        retained.incrementLater()
        viewModel.incrementLater()
    }

    TabColumn {
        InfoCard(
            title = "재생성에서 무엇이 남나",
            text = "같은 카운터를 네 가지 방식으로 보관합니다. +1 을 몇 번 누른 뒤 위의 recreate() 를 누르거나 " +
                    "다크 모드를 바꿔 보세요. 인스턴스 번호는 이 프로세스에서 몇 번째로 만들어진 객체인지입니다."
        )

        WhiteCard {
            SectionTitle("보관 방식별 상태")
            Spacer(modifier = Modifier.height(8.dp))
            HolderRow("보관", "인스턴스", "값", "2초 작업", header = true)
            HolderRow("remember", remembered.name, remembered.count.toString(), remembered.work.label)
            HolderRow("rememberSaveable", "(값만 저장)", savedCount.toString(), "—")
            HolderRow("retain", retained.name, retained.count.toString(), retained.work.label, highlight = true)
            HolderRow("ViewModel", viewModel.name, viewModelCount.toString(), viewModelWork.label)

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoButton("+1 (네 곳 모두)", Color(0xFF1976D2), onClick = incrementAll)
                DemoButton("2초 뒤 +1", Color(0xFFFF9800), onClick = incrementAllLater)
            }
            Spacer(modifier = Modifier.height(8.dp))
            DemoButton(
                text = "2초 작업 시작 → 바로 recreate()",
                color = Color(0xFF6A1B9A),
                onClick = {
                    incrementAllLater()
                    onRecreate()
                }
            )
        }

        ResultMatrixCard()

        NoteCard(
            title = "프로세스 종료 확인",
            text = "HOME 으로 나간 뒤 adb shell am kill com.example.composesample 로 프로세스를 끝내고 앱을 다시 여세요. " +
                    "rememberSaveable 의 값만 돌아오고 retain·ViewModel 은 새 객체(#1)입니다 — retain 은 직렬화하지 않고 " +
                    "메모리(ViewModel 안의 저장소)에만 둡니다."
        )
    }
}

@Composable
private fun HolderRow(
    kind: String,
    instance: String,
    value: String,
    work: String,
    header: Boolean = false,
    highlight: Boolean = false
) {
    val background = when {
        header -> Color(0xFF1976D2)
        highlight -> Color(0xFFE8F5E9)
        else -> Color(0xFFFAFAFA)
    }
    val textColor = if (header) Color.White else Color(0xFF424242)
    val weight = if (header || highlight) FontWeight.Bold else FontWeight.Normal
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(kind, Modifier.weight(1.35f), fontSize = 11.sp, color = textColor, fontWeight = weight)
        Text(instance, Modifier.weight(1.15f), fontSize = 11.sp, color = textColor, fontFamily = FontFamily.Monospace)
        Text(value, Modifier.weight(0.45f), fontSize = 12.sp, color = textColor, fontWeight = weight, textAlign = TextAlign.Center)
        Text(work, Modifier.weight(0.8f), fontSize = 11.sp, color = textColor, textAlign = TextAlign.Center)
    }
}

/** 실측 결과 — 기기·빌드는 exampleGuide.kt 에 */
@Composable
private fun ResultMatrixCard() {
    WhiteCard {
        SectionTitle("실측 결과")
        Spacer(modifier = Modifier.height(8.dp))
        val rows = listOf(
            listOf("보관", "recreate()\n다크 모드", "헤더 ← 로 닫고\n다시 열기", "프로세스\n종료"),
            listOf("remember", "새 객체 · 0\n작업 취소", "새 객체 · 0", "새 객체 · 0"),
            listOf("rememberSaveable", "값 복원", "0", "값 복원"),
            listOf("retain", "같은 객체 · 값\n작업 이어짐", "새 객체 · 0\n(닫을 때 onRetired)", "새 객체 · 0"),
            listOf("ViewModel", "같은 객체 · 값\n작업 이어짐", "같은 객체 · 값\n(액티비티 범위)", "새 객체 · 0"),
        )
        rows.forEachIndexed { index, cells ->
            val header = index == 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            header -> Color(0xFF455A64)
                            cells[0] == "retain" -> Color(0xFFE8F5E9)
                            else -> Color(0xFFFAFAFA)
                        }
                    )
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                cells.forEachIndexed { column, cell ->
                    Text(
                        text = cell,
                        modifier = Modifier.weight(if (column == 0) 1.1f else 1f),
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        color = if (header) Color.White else Color(0xFF424242),
                        fontWeight = if (header || column == 0) FontWeight.Bold else FontWeight.Normal,
                        textAlign = if (column == 0) TextAlign.Start else TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "헤더 ← 는 같은 액티비티 안에서 컴포지션만 빠지는 것이라, RESUMED 상태의 기본 저장소가 나간 retain 값을 " +
                    "곧바로 onRetired 하고 ViewModel 은 BlogExampleActivity 의 ViewModelStore 에 남습니다. 시스템 뒤로가기는 " +
                    "BlogExampleActivity 자체를 끝내므로 retain 은 onExitedComposition → onRetired, ViewModel 은 onCleared 까지 " +
                    "한 번에 옵니다.",
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 18.sp
        )
    }
}

// ==================== 2. 콜백 ====================

@Composable
private fun CallbackTab() {
    // ⑤ 두 효과를 탭 맨 위에 둔다 — 재생성 때 DisposableEffect 만 정리되고 다시 시작한다
    DisposableEffect(Unit) {
        RetainEventLog.add("DisposableEffect", "시작")
        onDispose { RetainEventLog.add("DisposableEffect", "onDispose") }
    }
    RetainedEffect(Unit) {
        RetainEventLog.add("RetainedEffect", "시작")
        onRetire { RetainEventLog.add("RetainedEffect", "onRetire") }
    }

    var showA by rememberSaveable { mutableStateOf(true) }
    var showB by rememberSaveable { mutableStateOf(true) }
    var keyC by rememberSaveable { mutableIntStateOf(1) }
    // 저장소는 if 바깥에서 만든다 — 안에 두면 숨길 때 저장소째 사라진다(공식 샘플 retainingCollapsingContentSample)
    val storeB = retainManagedRetainedValuesStore()
    val switchB = retain { RetentionSwitch() }
    val precomposeState = remember { SubcomposeLayoutState() }
    var probeSlot by remember { mutableIntStateOf(0) }

    TabColumn {
        InfoCard(
            title = "RetainObserver 콜백 실측",
            text = "각 카드의 버튼을 누르고 아래 기록을 보세요. retain 으로 보관한 값이 받는 콜백은 " +
                    "onRetained · onEnteredComposition · onExitedComposition · onRetired · onUnused 다섯 가지입니다."
        )

        ScenarioCard(
            title = "① 기본 저장소에서 숨기기",
            code = "if (show) { retain { A() } }",
            description = "화면이 RESUMED 인 동안 안드로이드 기본 저장소는 나간 값을 보존하지 않습니다. " +
                    "숨기면 onExitedComposition 다음 바로 onRetired, 다시 보이면 새 객체 — 이때 retain 은 remember 와 같습니다."
        ) {
            SlotLine { if (showA) TracedSlot("A") else HiddenSlot() }
            Spacer(modifier = Modifier.height(8.dp))
            DemoButton(if (showA) "숨기기" else "보이기", Color(0xFF1976D2)) { showA = !showA }
        }

        ScenarioCard(
            title = "② ManagedRetainedValuesStore 안에서 숨기기",
            code = "val store = retainManagedRetainedValuesStore()\n" +
                    "if (show) {\n" +
                    "    LocalRetainedValuesStoreProvider(store) { retain { B() } }\n" +
                    "}",
            description = "프로바이더가 빠질 때 저장소가 보존을 시작해, 숨겨도 onExitedComposition 만 오고 다시 보이면 " +
                    "같은 객체가 onEnteredComposition 으로 돌아옵니다. 숨긴 채 recreate() 해도 저장소째 보존돼 같은 객체가 " +
                    "돌아오고, 숨긴 채로 보존을 끄면 그때 onRetired."
        ) {
            SlotLine {
                if (showB) {
                    LocalRetainedValuesStoreProvider(storeB) { TracedSlot("B") }
                } else {
                    HiddenSlot()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoButton(if (showB) "숨기기" else "보이기", Color(0xFF1976D2)) { showB = !showB }
                if (switchB.enabled) {
                    DemoButton("보존 끄기", Color(0xFFD32F2F)) {
                        storeB.disableRetainingExitedValues()
                        switchB.enabled = false
                    }
                } else {
                    DemoButton("보존 켜기", Color(0xFF2E7D32)) {
                        storeB.enableRetainingExitedValues()
                        switchB.enabled = true
                    }
                }
            }
            Text(
                text = if (switchB.enabled) "보존: 켜짐 (enableRetainingExitedValues)" else "보존: 꺼짐 (disableRetainingExitedValues)",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        ScenarioCard(
            title = "③ 키가 바뀌면",
            code = "retain(key) { C() }",
            description = "키가 바뀌면 같은 자리라도 새 값을 계산합니다. 옛 값은 onExitedComposition 으로 나가고, " +
                    "저장소가 보존 중이 아니므로(RESUMED) 곧바로 onRetired — 새 값은 onRetained 로 시작합니다."
        ) {
            SlotLine { KeyedTracedSlot(keyC) }
            Spacer(modifier = Modifier.height(8.dp))
            DemoButton("key = ${keyC + 1} 로 바꾸기", Color(0xFF1976D2)) { keyC++ }
        }

        ScenarioCard(
            title = "④ 적용되지 않은 컴포지션 → onUnused",
            code = "val paused = state.createPausedPrecomposition(id) { retain { D() } }\n" +
                    "while (!paused.resume { false }) {}\n" +
                    "paused.cancel()  // 또는 paused.apply().dispose()",
            description = "LazyList 프리페치처럼 미리 컴포즈만 하고 붙이지 않은 채 취소하면 retain 계산은 돌았지만 " +
                    "적용되지 않았으므로 onRetained 없이 onUnused 하나만 옵니다. 적용 후 해제하면 네 콜백이 순서대로 옵니다."
        ) {
            // 미리 컴포즈의 호스트 — 슬롯을 측정·배치하지 않는 빈 SubcomposeLayout
            SubcomposeLayout(state = precomposeState, modifier = Modifier.size(0.dp)) {
                layout(0, 0) {}
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DemoButton("resume → cancel()", Color(0xFF616161)) {
                    precomposeThenCancel(precomposeState, slotId = "probe-${probeSlot++}")
                }
                DemoButton("apply() → dispose()", Color(0xFF1976D2)) {
                    precomposeThenApplyAndDispose(precomposeState, slotId = "probe-${probeSlot++}")
                }
            }
        }

        ScenarioCard(
            title = "⑤ RetainedEffect 와 DisposableEffect",
            code = "DisposableEffect(Unit) { …; onDispose { … } }\n" +
                    "RetainedEffect(Unit) { …; onRetire { … } }",
            description = "이 탭에 들어오면 둘 다 '시작'. recreate() 를 누르면 DisposableEffect 만 onDispose → 시작을 " +
                    "반복하고 RetainedEffect 는 아무 기록도 남기지 않습니다. 다른 탭으로 가면 둘 다 끝납니다."
        ) {}
    }
}

/** 카드 안에서 retain 값을 하나 보관하고 이름을 보여 준다 */
@Composable
private fun TracedSlot(prefix: String) {
    val value = retain { TracedValue(prefix) }
    SlotChip(text = value.name, color = Color(0xFF2E7D32))
}

@Composable
private fun KeyedTracedSlot(key: Int) {
    val value = retain(key) { TracedValue("C", detail = "(key=$key)") }
    SlotChip(text = value.name, color = Color(0xFF2E7D32))
}

@Composable
private fun HiddenSlot() {
    SlotChip(text = "(숨김)", color = Color(0xFFBDBDBD))
}

/** 미리 컴포즈한 뒤 적용하지 않고 취소한다 — 버림받은 컴포지션의 retain 값은 onUnused */
private fun precomposeThenCancel(state: SubcomposeLayoutState, slotId: Any) {
    RetainEventLog.add("④", "precompose → cancel()")
    val paused = state.createPausedPrecomposition(slotId) { TracedSlot("D") }
    while (!paused.resume { false }) {
        // 멈추지 않도록 false 를 돌려주므로 한 번에 끝난다
    }
    paused.cancel()
}

/** 미리 컴포즈한 뒤 적용하고 곧바로 해제한다 — 적용 시점에 onRetained·onEnteredComposition, 해제에 나머지 둘 */
private fun precomposeThenApplyAndDispose(state: SubcomposeLayoutState, slotId: Any) {
    RetainEventLog.add("④", "precompose → apply() → dispose()")
    val paused = state.createPausedPrecomposition(slotId) { TracedSlot("D") }
    while (!paused.resume { false }) {
        // 위와 같다
    }
    paused.apply().dispose()
}

// ==================== 3. 탭 보존 ====================

@Composable
private fun RegistryTab() {
    var mode by rememberSaveable { mutableStateOf(StoreMode.REGISTRY_KEYED) }
    var currentTab by rememberSaveable { mutableStateOf(TabNames.first()) }
    // 모드별로 레지스트리를 따로 둔다 — 한 레지스트리를 두 방식이 같이 쓰면 서로의 보존 값이 섞인다
    val keyedRegistry = retainRetainedValuesStoreRegistry()
    val noKeyRegistry = retainRetainedValuesStoreRegistry()

    TabColumn {
        InfoCard(
            title = "탭마다 보존 저장소 두기",
            text = "탭을 바꾸면 이전 탭의 내용은 컴포지션에서 빠집니다. RetainedValuesStoreRegistry 는 키마다 " +
                    "ManagedRetainedValuesStore 를 하나씩 만들어, 빠진 탭의 retain 값을 다시 돌아올 때까지 붙잡아 둡니다."
        )

        WhiteCard {
            SectionTitle("보관 방식")
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StoreMode.entries.forEach { option ->
                    ChoiceChip(option.label, selected = mode == option) { mode = option }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TabNames.forEach { tab ->
                    TabItem(tab, currentTab == tab, { currentTab = tab }, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            when (mode) {
                StoreMode.DEFAULT_KEYED -> key(currentTab) {
                    InnerTabPanel(currentTab)
                }

                StoreMode.REGISTRY_KEYED -> key(currentTab) {
                    keyedRegistry.LocalRetainedValuesStoreProvider(currentTab) {
                        InnerTabPanel(currentTab)
                    }
                }

                // 함정: 같은 호출 위치에서 저장소만 바꾸면 retain 의 키(호출 위치 + 타입)가 같아 앞 탭의 값을 그대로 돌려준다
                StoreMode.REGISTRY_NO_KEY -> noKeyRegistry.LocalRetainedValuesStoreProvider(currentTab) {
                    InnerTabPanel(currentTab)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            DemoButton("검색 탭 보존 비우기 — clearChild(\"검색\")", Color(0xFFD32F2F)) {
                RetainEventLog.add("Registry", "clearChild(\"검색\")")
                keyedRegistry.clearChild("검색")
            }
        }

        NoteCard(
            title = "목적지별 보존을 직접 만들 때",
            text = "프로젝트의 navigation3 1.2.0 과 최신 1.3.0-alpha02 에는 retain 용 NavEntryDecorator 가 없습니다" +
                    "(aar 확인). 목적지 단위로 보존하려면 이 레지스트리를 목적지 키로 감싸고, 백스택에서 빠진 목적지는 " +
                    "clearChild 로 비워야 합니다 — 비우지 않으면 레지스트리가 폐기될 때까지 남습니다(KDoc)."
        )
    }
}

@Composable
private fun InnerTabPanel(tab: String) {
    val counter = retain { TabCounter(tab) }
    val mismatched = !counter.name.startsWith("$tab#")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (mismatched) Color(0xFFFFEBEE) else Color(0xFFE3F2FD))
            .padding(12.dp)
    ) {
        Text("보고 있는 탭: $tab", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Text(
            text = "retain 이 돌려준 객체: ${counter.name} · 값 ${counter.count}",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF424242)
        )
        if (mismatched) {
            Text(
                text = "다른 탭의 객체가 돌아왔습니다 (key() 없음)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        DemoButton("+1", Color(0xFF1976D2)) { counter.increment() }
    }
}

// ==================== 4. 비교 ====================

@Composable
private fun ComparisonTab() {
    TabColumn {
        InfoCard(
            title = "ViewModel vs retain",
            text = "retain 은 구성 변경을 넘는 보관을 UI(컴포지션) 쪽 관심사로 옮깁니다. 안드로이드 기본 저장소 자체가 " +
                    "ViewModel(LifecycleRetainedValuesStoreOwner)이라 재생성 생존 범위는 같지만, 값이 묶이는 곳은 " +
                    "ViewModelStoreOwner 가 아니라 retain 을 부른 컴포지션 위치입니다."
        )

        CodeComparisonCard(
            title = "ViewModel",
            titleColor = Color(0xFFD32F2F),
            bgColor = Color(0xFFFFF0F0),
            points = listOf(
                "ViewModel() 상속 · viewModelScope 제공",
                "ViewModelStoreOwner 범위 — 화면을 닫아도 owner 가 살아 있으면 남음",
                "Koin 이면 viewModel { } 등록 + koinViewModel()"
            ),
            code = """class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {
    val state: StateFlow<UiState>
    fun login(c: Credentials) {
        viewModelScope.launch { … }
    }
}

// 사용
val vm: AuthViewModel = koinViewModel()"""
        )

        CodeComparisonCard(
            title = "retain",
            titleColor = Color(0xFF2E7D32),
            bgColor = Color(0xFFF0FFF0),
            points = listOf(
                "일반 클래스 + RetainObserver",
                "스코프와 정리는 직접 — onRetired·onUnused 에서 cancel",
                "컴포지션 위치에 묶임 — 화면을 닫으면 바로 정리",
                "Context·View 를 붙잡으면 안 됨 (lint RetainLeaksContext)"
            ),
            code = """class AuthPresenter(
    private val repo: AuthRepository
) : RetainObserver {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val state: StateFlow<UiState>
    fun login(c: Credentials) { scope.launch { … } }
    override fun onRetired() = scope.cancel()
    override fun onUnused() = scope.cancel()
    override fun onRetained() {}
    override fun onEnteredComposition() {}
    override fun onExitedComposition() {}
}

// 사용 — 의존성은 컴포지션에서 꺼내 넘긴다
val repo = koinInject<AuthRepository>()
val presenter = retain { AuthPresenter(repo) }"""
        )

        ComparisonTableCard()

        NoteCard(
            title = "retain 과 rememberSaveable 함께 쓰기",
            text = "저장해야 할 값(검색어)은 rememberSaveable·rememberSerializable 로, 다시 만들기 비싼 값(검색 결과·연결)은 " +
                    "retain 으로 나누고 remember(saved, retained) 로 묶습니다. 프로세스 종료 뒤에는 저장된 검색어로 " +
                    "새 retain 객체를 다시 초기화합니다(공식 샘플 rememberAndRetainSample)."
        )
    }
}

@Composable
private fun CodeComparisonCard(
    title: String,
    titleColor: Color,
    bgColor: Color,
    points: List<String>,
    code: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            points.forEach { point ->
                Text(
                    text = "• $point",
                    fontSize = 13.sp,
                    color = Color(0xFF424242),
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(code)
        }
    }
}

@Composable
private fun ComparisonTableCard() {
    WhiteCard {
        SectionTitle("비교 표")
        Spacer(modifier = Modifier.height(12.dp))

        val rows = listOf(
            Triple("클래스", "ViewModel() 상속", "일반 클래스"),
            Triple("만들기", "viewModel { … }", "retain { … }"),
            Triple("묶이는 곳", "ViewModelStoreOwner\n(이 앱: BlogExampleActivity)", "컴포지션 위치 +\nRetainedValuesStore"),
            Triple("정리 콜백", "onCleared()", "onRetired()\nonUnused()"),
            Triple("재생성(다크 모드)", "같은 객체", "같은 객체"),
            Triple("헤더 ← 로 닫으면", "남음(액티비티 종료 때 onCleared)", "곧바로 onRetired"),
            Triple("프로세스 종료", "사라짐 → SavedStateHandle", "사라짐 → rememberSaveable"),
            Triple("직렬화", "불필요", "불필요"),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(Color(0xFF1976D2))
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text("항목", Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("ViewModel", Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("retain", Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        rows.forEachIndexed { index, (label, vm, retain) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) Color(0xFFFAFAFA) else Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(label, Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                Text(vm, Modifier.weight(1.2f), fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 15.sp)
                Text(
                    retain,
                    Modifier.weight(1.2f),
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// ==================== 이벤트 기록 패널 ====================

@Composable
private fun EventLogPanel() {
    val events = RetainEventLog.events
    val listState = rememberLazyListState()
    LaunchedEffect(events.size) {
        if (events.isNotEmpty()) listState.scrollToItem(events.lastIndex)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "이벤트 기록 · logcat ${RetainEventLog.TAG}",
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF80CBC4)
                )
                TextButton(onClick = { RetainEventLog.clear() }) {
                    Text("지우기", fontSize = 12.sp, color = Color(0xFFB0BEC5))
                }
            }
            if (events.isEmpty()) {
                Text(
                    text = "> 아직 기록이 없습니다",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E),
                    fontFamily = FontFamily.Monospace
                )
            } else {
                LazyColumn(state = listState) {
                    items(events, key = { it.seq }) { event ->
                        Text(
                            text = "${event.seq}. ${event.subject}  ${event.event}",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = eventColor(event.event),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun eventColor(event: String): Color = when {
    event.startsWith("onRetained") -> Color(0xFF64B5F6)
    event.startsWith("onEnteredComposition") -> Color(0xFF81C784)
    event.startsWith("onExitedComposition") -> Color(0xFFFFB74D)
    event.startsWith("onRetired") || event.startsWith("onCleared") -> Color(0xFFE57373)
    event.startsWith("onUnused") -> Color(0xFFBDBDBD)
    event.startsWith("onRemembered") || event.startsWith("onForgotten") -> Color(0xFFCE93D8)
    else -> Color(0xFFECEFF1)
}

// ==================== 공통 조각 ====================

@Composable
private fun TabColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun WhiteCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF212121)
    )
}

@Composable
private fun InfoCard(title: String, text: String) {
    WhiteCard {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun NoteCard(title: String, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57F17)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun ScenarioCard(
    title: String,
    code: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    WhiteCard {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(code)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = Color(0xFF616161),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp),
        fontSize = 11.sp,
        color = Color(0xFFE0E0E0),
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp
    )
}

@Composable
private fun SlotLine(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("보이는 값: ", fontSize = 12.sp, color = Color(0xFF757575))
        content()
    }
}

@Composable
private fun SlotChip(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = color
    )
}

@Composable
private fun ChoiceChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF1976D2) else Color(0xFFECEFF1))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) Color.White else Color(0xFF424242)
    )
}

@Composable
private fun DemoButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
