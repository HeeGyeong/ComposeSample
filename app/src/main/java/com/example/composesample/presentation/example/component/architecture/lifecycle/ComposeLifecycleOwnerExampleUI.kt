package com.example.composesample.presentation.example.component.architecture.lifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.rememberLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

/**
 * lifecycle 2.10 의 `rememberLifecycleOwner(maxLifecycle, parent)` 예제.
 *
 * 컴포저블 하위 트리에 "부모를 따라가되 [maxLifecycle] 을 넘지 않는" LifecycleOwner 를 만들어 준다.
 * 실제 상태는 min(부모 상태, 상한)이고, 컴포지션에서 빠지면 DESTROYED 로 끝난다.
 */
@Composable
fun ComposeLifecycleOwnerExampleUI(onBackEvent: () -> Unit) {
    val viewModel: ComposeLifecycleOwnerViewModel = koinViewModel()
    val ticks = viewModel.ticks.collectAsStateWithLifecycle().value
    val logs = viewModel.logs.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Composable 범위 LifecycleOwner",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { PagerCard(viewModel, ticks) }
            item { PlaygroundCard(viewModel) }
            item { LogCard(viewModel, logs) }
            item { ComparisonCard() }
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun OverviewCard() {
    OwnerCard(title = "1. 하위 트리에 상한이 걸린 수명주기") {
        BodyText(
            "LocalLifecycleOwner 는 보통 Activity(또는 Nav 엔트리) 하나다. 그래서 화면 안의 일부만 " +
                "\"보이지 않으니 멈춰라\"라고 말할 방법이 없었다. rememberLifecycleOwner 는 그 하위 트리 전용 owner 를 만든다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "val owner = rememberLifecycleOwner(\n" +
                "    maxLifecycle = if (isCurrent) RESUMED else STARTED, // 기본값 RESUMED\n" +
                "    parent = LocalLifecycleOwner.current,               // 기본값\n" +
                ")\n" +
                "CompositionLocalProvider(LocalLifecycleOwner provides owner) {\n" +
                "    Page()  // 안쪽의 LifecycleResumeEffect·repeatOnLifecycle 이 owner 를 따른다\n" +
                "}"
        )
        TableRow("상태", "min(부모 상태, maxLifecycle)", isHeader = true)
        TableRow("부모 이벤트", "그대로 따라가되 상한에서 멈춘다")
        TableRow("상한 변경", "LaunchedEffect 로 반영 — 한 프레임 뒤에 적용")
        TableRow("컴포지션 이탈", "DESTROYED 로 내려가고 부모 관찰을 해제")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "공개 API 는 이 함수 하나다. 실제 owner 클래스(ComposeLifecycleOwner)는 internal 이라 직접 만들 수 없다(javap 확인)."
        )
    }
}

// ==================== 2. Pager ====================

@Composable
private fun PagerCard(viewModel: ComposeLifecycleOwnerViewModel, ticks: List<Int>) {
    var capEnabled by remember { mutableStateOf(true) }
    val pagerState = rememberPagerState { ComposeLifecycleOwnerViewModel.PAGE_COUNT }

    OwnerCard(title = "2. Pager — 화면 밖 페이지를 STARTED 로 캡") {
        BodyText(
            "각 페이지는 \"영상 재생\"을 흉내 내는 틱 작업을 repeatOnLifecycle(RESUMED) 안에서 돌린다. " +
                "옆 페이지를 미리 컴포즈하도록(beyondViewportPageCount = 1) 두고, 양옆이 살짝 보이게 했다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(label = "상한 적용", selected = capEnabled) { capEnabled = true }
            ActionButton(label = "상한 없음(부모 그대로)", selected = !capEnabled) { capEnabled = false }
            ActionButton(label = "틱 초기화", selected = false) { viewModel.resetTicks() }
        }
        Spacer(modifier = Modifier.height(10.dp))

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            contentPadding = PaddingValues(horizontal = 36.dp),
            pageSpacing = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { page ->
            val isCurrent = page == pagerState.settledPage
            if (capEnabled) {
                // 현재 페이지만 RESUMED, 나머지(보이거나 미리 컴포즈된 페이지)는 STARTED 에서 멈춘다
                val owner = rememberLifecycleOwner(
                    maxLifecycle = if (isCurrent) Lifecycle.State.RESUMED else Lifecycle.State.STARTED
                )
                CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                    VideoPage(page, isCurrent, ticks[page], viewModel)
                }
            } else {
                // 대조군: 모든 페이지가 화면(부모)의 owner 를 공유 → 컴포즈된 페이지는 전부 RESUMED
                VideoPage(page, isCurrent, ticks[page], viewModel)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        TickSummary(ticks = ticks, currentPage = pagerState.settledPage)
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "상한 없음으로 두면 현재 페이지와 미리 컴포즈된 이웃 페이지의 틱이 함께 오른다. " +
                "상한을 걸면 현재 페이지만 오르고, 옆 페이지는 STARTED 라 LifecycleStartEffect 는 살아 있지만 " +
                "LifecycleResumeEffect 와 RESUMED 작업은 멈춘다. 틱은 시간 대신 세는 값이라 기기 속도와 무관하게 비교된다."
        )
    }
}

@Composable
private fun VideoPage(
    page: Int,
    isCurrent: Boolean,
    tickCount: Int,
    viewModel: ComposeLifecycleOwnerViewModel
) {
    val owner = LocalLifecycleOwner.current
    val state by owner.lifecycle.currentStateAsState()
    val source = "P$page"

    // 보일 때만 돌아야 하는 작업 — RESUMED 구간에서만 틱을 올린다
    LaunchedEffect(owner) {
        owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(TICK_INTERVAL_MS)
                viewModel.tick(page)
            }
        }
    }
    // STARTED 경계(예: 센서 등록/해제)
    LifecycleStartEffect(page) {
        viewModel.record(source, "StartEffect ▶")
        onStopOrDispose { viewModel.record(source, "StartEffect ■") }
    }
    // RESUMED 경계(예: 영상 재생/일시정지)
    LifecycleResumeEffect(page) {
        viewModel.record(source, "ResumeEffect ▶")
        onPauseOrDispose { viewModel.record(source, "ResumeEffect ■") }
    }

    val playing = state == Lifecycle.State.RESUMED
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (playing) Color(0xFF1B5E20) else Color(0xFF37474F),
                RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isCurrent) 2.dp else 0.dp,
                color = if (isCurrent) Color(0xFFFFD54F) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Text(text = "페이지 $page", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Lifecycle: ${state.name}",
                color = stateColorOnDark(state),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (playing) "▶ 재생 중" else "❚❚ 정지",
                color = Color.White,
                fontSize = 13.sp
            )
        }
        Text(
            text = "틱 $tickCount",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun TickSummary(ticks: List<Int>, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ticks.forEachIndexed { index, value ->
            val isCurrent = index == currentPage
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isCurrent) Color(0xFFFFF8E1) else Color(0xFFF5F5F5),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "P$index", fontSize = 11.sp, color = Color(0xFF757575))
                Text(
                    text = "$value",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF212121)
                )
            }
        }
    }
}

// ==================== 3. 상한 직접 조절 ====================

@Composable
private fun PlaygroundCard(viewModel: ComposeLifecycleOwnerViewModel) {
    var cap by remember { mutableStateOf(Lifecycle.State.RESUMED) }
    var childVisible by remember { mutableStateOf(true) }
    val parentOwner = LocalLifecycleOwner.current
    val parentState by parentOwner.lifecycle.currentStateAsState()

    OwnerCard(title = "3. 상한을 직접 바꿔 보기") {
        BodyText(
            "자식 owner 의 상한을 바꾸면 자식만 내려가고 올라온다. 홈으로 나갔다가 돌아오면 부모가 CREATED 로 " +
                "내려가므로 자식도 따라 내려가고, 복귀 시에는 상한까지만 올라온다. 자식을 숨기면 DESTROYED 로 끝난다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "maxLifecycle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CAP_OPTIONS.forEach { option ->
                ActionButton(label = option.name, selected = cap == option) { cap = option }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(label = if (childVisible) "자식 숨기기" else "자식 표시", selected = false) {
                childVisible = !childVisible
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        StateRow(label = "부모(화면)", state = parentState)
        StateRow(label = "상한", state = cap)
        if (childVisible) {
            CappedChild(cap = cap, viewModel = viewModel)
        } else {
            StatusText(label = "자식", value = "컴포지션에 없음")
        }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "이 카드도 LazyColumn 아이템이라, 스크롤로 완전히 화면 밖에 나가면 자식이 컴포지션을 떠나 DESTROYED 가 되고 " +
                "돌아오면 ON_CREATE 부터 새 owner 로 다시 시작한다(실기기 로그로 확인). 상한은 remember 로 살아 있어 그대로 적용된다."
        )
    }
}

@Composable
private fun CappedChild(cap: Lifecycle.State, viewModel: ComposeLifecycleOwnerViewModel) {
    val owner = rememberLifecycleOwner(maxLifecycle = cap)
    val state by owner.lifecycle.currentStateAsState()

    ObserveEvents(owner = owner, source = "자식", viewModel = viewModel)
    StateRow(label = "자식 = min(부모, 상한)", state = state)
}

/**
 * owner 가 받는 Lifecycle 이벤트를 로그로 남긴다.
 *
 * onDispose 에서 옵저버를 떼지 않는다 — 컴포지션을 떠날 때 rememberLifecycleOwner 가 보내는
 * ON_PAUSE/ON_STOP/ON_DESTROY 까지 받아야 하기 때문이다. owner 는 이 컴포지션과 함께 버려지므로 누수는 없다.
 */
@Composable
private fun ObserveEvents(owner: LifecycleOwner, source: String, viewModel: ComposeLifecycleOwnerViewModel) {
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> viewModel.record(source, event) }
        owner.lifecycle.addObserver(observer)
        onDispose { }
    }
}

// ==================== 4. 로그 ====================

@Composable
private fun LogCard(viewModel: ComposeLifecycleOwnerViewModel, logs: List<ComposeLifecycleOwnerViewModel.LogEntry>) {
    OwnerCard(title = "4. 이벤트 로그 (최신이 위)") {
        FlowRow {
            ActionButton(label = "로그 지우기", selected = false) { viewModel.clearLogs() }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (logs.isEmpty()) {
            CaptionText("아직 이벤트가 없다. 페이지를 넘기거나 상한을 바꿔 보자.")
        }
        logs.forEach { entry ->
            Row(modifier = Modifier.padding(vertical = 1.dp)) {
                Text(
                    text = "#${entry.seq}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF9E9E9E),
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = entry.source,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.width(40.dp)
                )
                Text(text = entry.event, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF424242))
            }
        }
    }
}

// ==================== 5. 비교 ====================

@Composable
private fun ComparisonCard() {
    OwnerCard(title = "5. 언제 무엇을 쓰나") {
        TableRow("방법", "하위 트리 수명주기", isHeader = true)
        TableRow("LocalLifecycleOwner", "화면 전체와 같다 — 안 보이는 페이지도 RESUMED")
        TableRow("수동 if (isCurrent)", "직접 분기 — 백그라운드 전환은 따로 처리해야 한다")
        TableRow("rememberLifecycleOwner", "부모 + 상한을 한 번에 — 기존 Lifecycle*Effect 가 그대로 동작")
        Spacer(modifier = Modifier.height(8.dp))
        BulletText("상한은 STARTED·CREATED 처럼 '덜 활성'으로만 건다. 부모보다 높게 줘도 부모를 넘지 않는다.")
        BulletText("Nav3 는 엔트리마다 같은 일을 이미 한다 — 오버레이 아래 화면이 STARTED 인 이유(SceneStrategy 예제).")
        BulletText("collectAsStateWithLifecycle 도 LocalLifecycleOwner 를 쓰므로, 상한을 건 페이지 안에서는 자동으로 따라간다.")
    }
}

// ==================== 공통 요소 ====================

private const val TICK_INTERVAL_MS = 200L
private val CAP_OPTIONS = listOf(Lifecycle.State.CREATED, Lifecycle.State.STARTED, Lifecycle.State.RESUMED)

private fun stateColor(state: Lifecycle.State): Color = when (state) {
    Lifecycle.State.RESUMED -> Color(0xFF2E7D32)
    Lifecycle.State.STARTED -> Color(0xFFEF6C00)
    Lifecycle.State.DESTROYED -> Color(0xFFC62828)
    else -> Color(0xFF757575)
}

private fun stateColorOnDark(state: Lifecycle.State): Color = when (state) {
    Lifecycle.State.RESUMED -> Color(0xFFA5D6A7)
    Lifecycle.State.STARTED -> Color(0xFFFFCC80)
    else -> Color(0xFFCFD8DC)
}

@Composable
private fun StateRow(label: String, state: Lifecycle.State) {
    StatusText(label = label, value = state.name, valueColor = stateColor(state))
}

@Composable
private fun StatusText(label: String, value: String, valueColor: Color = Color(0xFF757575)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF616161))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

@Composable
private fun ActionButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1565C0) else Color(0xFF90A4AE)
        )
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun OwnerCard(title: String, content: @Composable () -> Unit) {
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
private fun BulletText(text: String) {
    Text(
        text = "· $text",
        fontSize = 12.sp,
        color = Color(0xFF616161),
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(first: String, second: String, isHeader: Boolean = false) {
    val weight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    val color = if (isHeader) Color(0xFF212121) else Color(0xFF424242)

    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = first,
            fontSize = 11.sp,
            fontWeight = weight,
            fontFamily = FontFamily.Monospace,
            color = color,
            modifier = Modifier.width(150.dp)
        )
        Text(text = second, fontSize = 11.sp, fontWeight = weight, color = color, lineHeight = 16.sp)
    }
}
