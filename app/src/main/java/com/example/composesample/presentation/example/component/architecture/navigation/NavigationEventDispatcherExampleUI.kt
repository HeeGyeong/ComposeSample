package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.ExperimentalNavigationEventApi
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.NavigationEventState
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.boldMarkup

/**
 * NavigationEvent 디스패처 예제
 * - 기존 `PredictiveBackExample` 은 **플랫폼 back 제스처의 진행률**이 축이다. 이 예제는 그 위 계층인
 *   `androidx.navigationevent` 의 모델을 다룬다 — **back/forward 양방향** + **이전·다음 목적지 목록(info)** +
 *   **전환 상태(transitionState)** 를 하나로 묶어 다루는 방식.
 * - `DirectNavigationEventInput` 으로 제스처 없이 이벤트를 주입할 수 있어, 실기기 스와이프 없이도
 *   콜백 순서와 상태 전이를 **결정론적으로** 재현한다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "NavigationEvent Dispatcher" 섹션 참고.
 */

// ==================== 목적지 정보 ====================

/**
 * `NavigationEventInfo` 는 인터페이스가 아니라 **추상 클래스**라 직접 상속해야 한다.
 * 라이브러리는 이 타입을 그대로 실어 나르기만 하고 내용은 앱이 정한다.
 */
// 1.2.0 부터 NavigationEventInfo 에 open val title(실험)이 생겨 같은 이름은 override 해야 컴파일된다.
// Android 에서는 이 title 을 읽는 호스트가 없어(웹 탭 제목용) 동작은 1.1.2 와 같다.
@OptIn(ExperimentalNavigationEventApi::class)
private data class DestinationInfo(override val title: String) : NavigationEventInfo()

/** 데모용 목적지 스택. 인덱스를 옮기면 back/forward 목록이 갈라진다. */
private val DESTINATIONS = listOf("홈", "목록", "상세", "설정")

// ==================== 화면 ====================

@Composable
fun NavigationEventDispatcherExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "NavigationEvent 디스패처",
            onBackIconClicked = onBackEvent
        )

        // parent 를 생략하면 LocalNavigationEventDispatcherOwner 를 읽고, 비어 있으면 예외가 난다.
        // 이 화면은 스스로가 루트이므로 parent = null 을 명시해 루트 디스패처를 만든다.
        val owner = rememberNavigationEventDispatcherOwner(parent = null)

        CompositionLocalProvider(
            LocalNavigationEventDispatcherOwner.provides(owner)
        ) {
            DispatcherContent(dispatcher = owner.navigationEventDispatcher)
        }
    }
}

@Composable
private fun DispatcherContent(dispatcher: NavigationEventDispatcher) {
    var index by remember { mutableIntStateOf(2) }
    var progress by remember { mutableFloatStateOf(0f) }
    var injectedCount by remember { mutableIntStateOf(0) }
    var callbackCount by remember { mutableIntStateOf(0) }
    val callbackLog = remember { mutableStateListOf<String>() }

    // mergedHistory 가 backInfo → currentInfo → forwardInfo 순으로 이어 붙이므로
    // backInfo 는 "가까운 것부터"가 아니라 **시간순(오래된 것부터)** 으로 담는다.
    val currentInfo = remember(index) { DestinationInfo(DESTINATIONS[index]) }
    val backInfo = remember(index) { DESTINATIONS.take(index).map(::DestinationInfo) }
    val forwardInfo = remember(index) { DESTINATIONS.drop(index + 1).map(::DestinationInfo) }

    val state = rememberNavigationEventState(
        currentInfo = currentInfo,
        backInfo = backInfo,
        forwardInfo = forwardInfo
    )

    val isBackEnabled = index > 0
    val isForwardEnabled = index < DESTINATIONS.lastIndex

    fun record(message: String) {
        callbackCount++
        callbackLog.add(0, message)
        if (callbackLog.size > 6) callbackLog.removeAt(callbackLog.lastIndex)
    }

    // 진행률은 콜백으로 오지 않는다 — state.transitionState 로만 관찰된다.
    // 콜백은 완료/취소라는 **종료 사건**만 알려준다.
    NavigationEventHandler(
        state = state,
        isBackEnabled = isBackEnabled,
        onBackCompleted = {
            if (index > 0) index--
            progress = 0f
            record("onBackCompleted → ${DESTINATIONS[index]}")
        },
        onBackCancelled = {
            progress = 0f
            record("onBackCancelled (위치 유지)")
        },
        isForwardEnabled = isForwardEnabled,
        onForwardCompleted = {
            if (index < DESTINATIONS.lastIndex) index++
            progress = 0f
            record("onForwardCompleted → ${DESTINATIONS[index]}")
        },
        onForwardCancelled = {
            progress = 0f
            record("onForwardCancelled (위치 유지)")
        }
    )

    // 제스처 없이 이벤트를 넣는 입력원. 디스패처에 붙이고 화면을 떠날 때 뗀다.
    val input = remember { DirectNavigationEventInput() }
    DisposableEffect(dispatcher, input) {
        dispatcher.addInput(input)
        onDispose { dispatcher.removeInput(input) }
    }

    val transition = state.transitionState
    val inProgress = transition as? NavigationEventTransitionState.InProgress

    // 6번 카드(1.2.0) — 디스패처·입력원은 스크롤로 항목이 사라져도 유지되도록 LazyColumn 밖에 둔다
    val gateDemo = remember { InputGateDemo() }
    DisposableEffect(gateDemo) {
        gateDemo.attach()
        onDispose { gateDemo.dispose() }
    }
    var gateBackOn by remember { mutableStateOf(true) }
    var gateForwardOn by remember { mutableStateOf(false) }
    val gateState = rememberNavigationEventState(currentInfo = remember { DestinationInfo("데모") })
    // 1.2.0 팩토리로 감싼 Owner 를 Local 로 넘겨, 이 핸들러가 손으로 만든 디스패처에 붙게 한다
    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides gateDemo.owner) {
        NavigationEventHandler(
            state = gateState,
            isBackEnabled = gateBackOn,
            onBackCompleted = { gateDemo.backHandled++ },
            isForwardEnabled = gateForwardOn,
            onForwardCompleted = { gateDemo.forwardHandled++ }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ConceptCard() }
        item { OwnerCard() }
        item {
            InjectionCard(
                transitionLabel = transition.describe(),
                inProgressDirection = inProgress?.direction,
                progress = progress,
                isBackEnabled = isBackEnabled,
                isForwardEnabled = isForwardEnabled,
                injectedCount = injectedCount,
                callbackCount = callbackCount,
                callbackLog = callbackLog,
                onStart = { back ->
                    injectedCount++
                    progress = 0f
                    val event = NavigationEvent(
                        swipeEdge = if (back) NavigationEvent.EDGE_LEFT else NavigationEvent.EDGE_RIGHT,
                        progress = 0f
                    )
                    if (back) input.backStarted(event) else input.forwardStarted(event)
                },
                onProgress = { back ->
                    injectedCount++
                    progress = (progress + 0.25f).coerceAtMost(1f)
                    val event = NavigationEvent(
                        swipeEdge = if (back) NavigationEvent.EDGE_LEFT else NavigationEvent.EDGE_RIGHT,
                        progress = progress
                    )
                    if (back) input.backProgressed(event) else input.forwardProgressed(event)
                },
                onComplete = { back ->
                    injectedCount++
                    if (back) input.backCompleted() else input.forwardCompleted()
                },
                onCancel = { back ->
                    injectedCount++
                    if (back) input.backCancelled() else input.forwardCancelled()
                }
            )
        }
        item {
            HistoryCard(
                state = state,
                index = index,
                onJump = { target ->
                    index = target
                    progress = 0f
                }
            )
        }
        item {
            GatingCard(
                isBackEnabled = isBackEnabled,
                isForwardEnabled = isForwardEnabled,
                injectedCount = injectedCount,
                callbackCount = callbackCount
            )
        }
        item {
            InputGateCard(
                demo = gateDemo,
                backOn = gateBackOn,
                forwardOn = gateForwardOn,
                onBackOnChange = { gateBackOn = it },
                onForwardOnChange = { gateForwardOn = it }
            )
        }
        item { PitfallCard() }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun ConceptCard() {
    SectionCard(title = "1. 제스처가 아니라 '내비게이션 이벤트' 모델") {
        BodyText(
            "플랫폼 predictive back 은 back 한 방향의 진행률만 준다. `androidx.navigationevent` 는 그 위에 " +
                "**양방향(back/forward)** · **이전·다음 목적지 목록** · **전환 상태**를 한 모델로 얹고, " +
                "제스처·버튼·테스트 입력 같은 서로 다른 입력원을 같은 디스패처로 모은다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("구분", "PredictiveBackHandler / 이 디스패처", isHeader = true)
        TableRow("방향", "back 만 / back + forward")
        TableRow("맥락", "없음 / currentInfo·backInfo·forwardInfo")
        TableRow("상태", "콜백 흐름 / transitionState(Idle·InProgress)")
        TableRow("입력원", "플랫폼 제스처 고정 / 제스처·직접 주입 등 교체 가능")
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "구성 요소는 넷이다 — **입력원**(NavigationEventInput)이 이벤트를 넣고, **디스패처**가 우선순위대로 " +
                "**핸들러**에 전달하며, 핸들러는 자기 **info** 를 들고 있다가 완료/취소를 콜백으로 돌려준다."
        )
    }
}

// ==================== 2. 디스패처 소유자 ====================

@Composable
private fun OwnerCard() {
    SectionCard(title = "2. 디스패처는 누가 들고 있나") {
        BodyText(
            "컴포저블 `NavigationEventHandler` 는 `LocalNavigationEventDispatcherOwner` 에서 디스패처를 찾는다. " +
                "비어 있으면 예외로 끝나므로, 화면이 스스로 루트를 만들어 제공해야 한다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "// parent 를 생략하면 Local 을 읽고, 비어 있으면 예외가 난다\n" +
                "val owner = rememberNavigationEventDispatcherOwner(parent = null)\n\n" +
                "CompositionLocalProvider(\n" +
                "    LocalNavigationEventDispatcherOwner.provides(owner)\n" +
                ") {\n" +
                "    // 이 안에서만 NavigationEventHandler 를 쓸 수 있다\n" +
                "}"
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText("빠뜨렸을 때 실제로 나오는 메시지 2종:")
        CodeBlock(
            "IllegalStateException: No NavigationEventDispatcher was provided\n" +
                "  via LocalNavigationEventDispatcherOwner\n" +
                "IllegalStateException: No NavigationEventDispatcherOwner provided in\n" +
                "  LocalNavigationEventDispatcherOwner. If you intended to create a root\n" +
                "  dispatcher, explicitly pass null as the parent."
        )
        CaptionText(
            "앞의 것은 핸들러가, 뒤의 것은 rememberNavigationEventDispatcherOwner 가 던진다. " +
                "부모를 물려받는 중첩 디스패처를 만들 때는 parent 를 넘기면 된다. " +
                "1.2.0 부터는 Local 과 View 트리에 Owner 가 없을 때 LocalContext 의 ContextWrapper 사슬을 거슬러 " +
                "액티비티 같은 Owner 를 한 번 더 찾으므로, 이 예외는 그것마저 없을 때만 난다."
        )
    }
}

// ==================== 3. 결정론적 입력 ====================

@Composable
private fun InjectionCard(
    transitionLabel: String,
    inProgressDirection: Int?,
    progress: Float,
    isBackEnabled: Boolean,
    isForwardEnabled: Boolean,
    injectedCount: Int,
    callbackCount: Int,
    callbackLog: List<String>,
    onStart: (Boolean) -> Unit,
    onProgress: (Boolean) -> Unit,
    onComplete: (Boolean) -> Unit,
    onCancel: (Boolean) -> Unit
) {
    val gestureRunning = inProgressDirection != null

    SectionCard(title = "3. 제스처 없이 이벤트 주입하기") {
        BodyText(
            "`DirectNavigationEventInput` 은 started → progressed → completed(또는 cancelled) 를 직접 부를 수 있는 입력원이다. " +
                "손가락 없이도 같은 경로를 그대로 재현할 수 있어 데모와 테스트가 결정론적이 된다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("transitionState", transitionLabel)
        ResultRow("주입/콜백", "${injectedCount}회 주입 → ${callbackCount}회 콜백")

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "back 방향" + if (!isBackEnabled) " (비활성)" else "",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemoButton("시작", Color(0xFF3949AB)) { onStart(true) }
            DemoButton("진행 +25%", Color(0xFF5C6BC0), enabled = gestureRunning) { onProgress(true) }
            DemoButton("완료", Color(0xFF2E7D32), enabled = gestureRunning) { onComplete(true) }
            DemoButton("취소", Color(0xFF757575), enabled = gestureRunning) { onCancel(true) }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "forward 방향" + if (!isForwardEnabled) " (비활성)" else "",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemoButton("시작", Color(0xFF00838F)) { onStart(false) }
            DemoButton("진행 +25%", Color(0xFF00ACC1), enabled = gestureRunning) { onProgress(false) }
            DemoButton("완료", Color(0xFF2E7D32), enabled = gestureRunning) { onComplete(false) }
            DemoButton("취소", Color(0xFF757575), enabled = gestureRunning) { onCancel(false) }
        }

        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("주입 진행률", "%.2f".format(progress))
        Spacer(modifier = Modifier.height(6.dp))
        if (callbackLog.isEmpty()) {
            CaptionText("아직 콜백이 없다. 시작 → 진행 → 완료 순으로 눌러 보라.")
        } else {
            CodeBlock(callbackLog.joinToString("\n"))
        }
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "여기서 드러나는 설계가 하나 있다 — **진행률은 콜백으로 오지 않는다.** 컴포저블 핸들러가 받는 것은 " +
                "완료·취소라는 종료 사건뿐이고, 중간 진행은 `transitionState` 를 읽어서만 알 수 있다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        TableRow("실측", "코어 API 를 실기기에서 직접 두드려 본 결과", isHeader = true)
        TableRow("정상 순서", "Idle → InProgress(direction=-1) → 완료 후 다시 Idle")
        TableRow("취소", "onBackCancelled 뒤 상태는 Idle 로 복귀")
        TableRow("시작 생략", "progressed 만 부르면 무시 · completed 는 **그대로 콜백이 온다**")
        TableRow("기본값", "NavigationEvent() 의 swipeEdge 는 EDGE_NONE(2)")
        CaptionText(
            "즉 라이브러리는 started → progressed → completed 순서를 강제하지 않는다. " +
                "위 버튼이 진행 중에만 활성화되는 것은 이 화면의 UI 선택이지 API 제약이 아니다."
        )
    }
}

// ==================== 4. 목적지 목록 ====================

@Composable
private fun HistoryCard(
    state: NavigationEventState<DestinationInfo>,
    index: Int,
    onJump: (Int) -> Unit
) {
    SectionCard(title = "4. current / back / forward 목록") {
        BodyText(
            "핸들러는 현재 위치뿐 아니라 **뒤에 무엇이 있고 앞에 무엇이 있는지**를 함께 들고 있다. " +
                "덕분에 제스처 중에 \"돌아갈 화면\" 미리보기를 그릴 수 있다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DESTINATIONS.forEachIndexed { i, title ->
                DemoButton(
                    text = title,
                    color = if (i == index) Color(0xFF3949AB) else Color(0xFFB0BEC5)
                ) { onJump(i) }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("currentInfo", state.currentInfo.title)
        ResultRow("backInfo", state.backInfo.joinToString(" → ") { it.title }.ifEmpty { "(없음)" })
        ResultRow("forwardInfo", state.forwardInfo.joinToString(" → ") { it.title }.ifEmpty { "(없음)" })
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "순서 규칙은 라이브러리가 정해 둔 것이 있다 — `NavigationEventHistory.mergedHistory` 가 " +
                "**backInfo → currentInfo → forwardInfo** 를 그대로 이어 붙이므로, backInfo 는 가까운 것부터가 아니라 " +
                "**오래된 것부터** 담아야 전체 이력이 시간순으로 맞는다."
        )
    }
}

// ==================== 5. 게이팅 ====================

@Composable
private fun GatingCard(
    isBackEnabled: Boolean,
    isForwardEnabled: Boolean,
    injectedCount: Int,
    callbackCount: Int
) {
    SectionCard(title = "5. 끝에 도달하면 이벤트는 어디로 가나") {
        BodyText(
            "핸들러의 isBackEnabled/isForwardEnabled 가 false 이면 그 방향 이벤트는 이 핸들러로 오지 않는다. " +
                "받을 핸들러가 하나도 없으면 디스패처의 fallback 이 처리하는데, 이 화면은 fallback 을 주지 않았으므로 " +
                "그대로 사라진다 — 위의 주입/콜백 숫자 차이가 그 결과다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("isBackEnabled", if (isBackEnabled) "true" else "false (첫 화면)")
        ResultRow("isForwardEnabled", if (isForwardEnabled) "true" else "false (마지막 화면)")
        ResultRow("차이", "${injectedCount - callbackCount}회 (주입됐지만 콜백 없음)")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "실기기에서 코어 API 로 확인한 결과, isBackEnabled 를 false 로 두고 시작 → 완료를 주입하면 " +
                "**콜백은 0회**다. 예외도 로그도 없이 조용히 사라진다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "진행 이벤트는 원래 콜백을 만들지 않으므로 이 차이가 전부 '무시된 이벤트'는 아니다. " +
                "끝 화면에서 시작→완료만 눌러 보면 차이가 어떻게 벌어지는지 분명해진다."
        )
    }
}

// ==================== 6. 함정 ====================

// ==================== 6. 1.2.0 — 입력원이 핸들러 상태를 안다 ====================

@Composable
private fun InputGateCard(
    demo: InputGateDemo,
    backOn: Boolean,
    forwardOn: Boolean,
    onBackOnChange: (Boolean) -> Unit,
    onForwardOnChange: (Boolean) -> Unit
) {
    SectionCard(title = "6. 1.2.0 — 입력원이 \"받을 핸들러가 있는지\"를 안다") {
        BodyText(
            "1.2.0 부터 NavigationEventInput 이 연결된 디스패처에 켜진 핸들러가 있는지를 직접 알려 준다. " +
                "실제 입력원(시스템 제스처 등)은 이 값이 true 일 때만 제스처 감지를 켜면 된다. " +
                "아래 스위치는 손으로 만든 디스패처에 붙은 핸들러를 켜고 끈다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "class GateAwareInput : NavigationEventInput() {\n" +
                "    override fun onHasEnabledHandlersChanged(has: Boolean) { … }\n" +
                "    override fun onHasEnabledBackHandlersChanged(has: Boolean) { … }\n" +
                "    override fun onHasEnabledForwardHandlersChanged(has: Boolean) { … }\n" +
                "}\n" +
                "input.hasEnabledHandlers   // 프로퍼티로도 읽힌다(스냅샷 상태는 아니다)\n\n" +
                "// 이미 있는 디스패처를 감싼 Owner — Local 로 넘기면 핸들러가 이 디스패처에 붙는다\n" +
                "val owner = NavigationEventDispatcherOwner(dispatcher)"
        )
        Spacer(modifier = Modifier.height(8.dp))
        ToggleRow("back 핸들러 켜기", backOn, onBackOnChange)
        ToggleRow("forward 핸들러 켜기", forwardOn, onForwardOnChange)
        Spacer(modifier = Modifier.height(6.dp))
        val input = demo.gateInput
        ResultRow("hasEnabledHandlers", input.anyEnabled.toString())
        ResultRow("…BackHandlers", input.backEnabled.toString())
        ResultRow("…ForwardHandlers", input.forwardEnabled.toString())
        ResultRow("콜백", "${input.changeCount}회 (onHasEnabled*Changed)")

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemoButton("back 보내기", Color(0xFF3949AB)) { demo.sendBack() }
            DemoButton("forward 보내기", Color(0xFF00838F)) { demo.sendForward() }
        }
        Spacer(modifier = Modifier.height(6.dp))
        ResultRow("핸들러가 받음", "back ${demo.backHandled} · forward ${demo.forwardHandled}")
        ResultRow("fallback 이 받음", "back ${demo.backFallbacks} · forward ${demo.forwardFallbacks}")
        CaptionText(
            "핸들러를 끄고 보내면 디스패처 생성자에 넘긴 fallback 이 받는다(5번 카드의 루트 디스패처는 fallback 이 없어서 사라졌다)."
        )

        Spacer(modifier = Modifier.height(10.dp))
        DemoButton("입력원을 뗀 뒤 back 보내기", Color(0xFF6D4C41)) { demo.sendBackWhileDetached() }
        ResultRow("결과", demo.detachedSendResult)
        CaptionText(
            "1.1.x 는 디스패처에서 뗀 입력원으로 보내면 IllegalStateException 이었고, 1.2.0 은 조용히 무시한다. " +
                "KDoc 의 @throws 문구는 아직 예전 그대로다."
        )

        Spacer(modifier = Modifier.height(10.dp))
        TableRow("1.2.0 신규", "용도", isHeader = true)
        TableRow("hasEnabled*Handlers", "입력원이 받을 핸들러 유무를 질의 — 제스처 감지 켜기/끄기")
        TableRow("Owner 팩토리", "NavigationEventDispatcherOwner(dispatcher) — 손으로 만든 디스패처를 Local 로")
        TableRow("Info.title/url", "@ExperimentalNavigationEventApi · 웹 호스트(탭 제목·주소창)용, Android 소비자 없음")
        TableRow("테스트 owner", "navigationEventInput · onForwardCompletedFallback(navigationevent-testing)")
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF37474F), modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * 6번 카드 상태 — 손으로 만든 디스패처(back/forward fallback 포함), 1.2.0 Owner 팩토리, 입력원 2개, 계수.
 * 라이브러리 콜백(fallback·onHasEnabled*Changed)이 상태를 쓰므로 컴포저블 밖 클래스에 둔다(#108 HotSwan 함정 회피).
 */
private class InputGateDemo {
    var backHandled by mutableIntStateOf(0)
    var forwardHandled by mutableIntStateOf(0)
    var backFallbacks by mutableIntStateOf(0)
        private set
    var forwardFallbacks by mutableIntStateOf(0)
        private set
    var detachedSendResult by mutableStateOf("아직 보내지 않음")
        private set

    val dispatcher = NavigationEventDispatcher(
        onBackCompletedFallback = { backFallbacks++ },
        onForwardCompletedFallback = { forwardFallbacks++ }
    )

    // 1.2.0 팩토리 — 예전에는 object : NavigationEventDispatcherOwner { … } 를 직접 써야 했다
    val owner: NavigationEventDispatcherOwner = NavigationEventDispatcherOwner(dispatcher)

    val gateInput = GateAwareInput()
    private val probeInput = DirectNavigationEventInput()

    fun attach() {
        dispatcher.addInput(gateInput)
        dispatcher.addInput(probeInput)
    }

    fun dispose() {
        dispatcher.removeInput(gateInput)
        dispatcher.removeInput(probeInput)
        dispatcher.dispose()
    }

    fun sendBack() = probeInput.backCompleted()

    fun sendForward() = probeInput.forwardCompleted()

    /** 입력원을 뗀 뒤 보낸다 — 받은 쪽이 있는지 계수로 확인하고 다시 붙인다 */
    fun sendBackWhileDetached() {
        dispatcher.removeInput(probeInput)
        val before = backHandled + backFallbacks
        detachedSendResult = try {
            probeInput.backCompleted()
            if (backHandled + backFallbacks == before) "예외 없음 · 아무도 받지 않음(무시됨)" else "누군가 받음"
        } catch (e: IllegalStateException) {
            "IllegalStateException: ${e.message}"
        } finally {
            dispatcher.addInput(probeInput)
        }
    }
}

/** 디스패처의 핸들러 상태 변화를 받는 입력원 — 실제 입력원이라면 여기서 제스처 감지를 켜고 끈다 */
private class GateAwareInput : NavigationEventInput() {
    var anyEnabled by mutableStateOf(false)
        private set
    var backEnabled by mutableStateOf(false)
        private set
    var forwardEnabled by mutableStateOf(false)
        private set
    var changeCount by mutableIntStateOf(0)
        private set

    override fun onHasEnabledHandlersChanged(hasEnabledHandlers: Boolean) {
        anyEnabled = hasEnabledHandlers
        changeCount++
    }

    override fun onHasEnabledBackHandlersChanged(hasEnabledBackHandlers: Boolean) {
        backEnabled = hasEnabledBackHandlers
        changeCount++
    }

    override fun onHasEnabledForwardHandlersChanged(hasEnabledForwardHandlers: Boolean) {
        forwardEnabled = hasEnabledForwardHandlers
        changeCount++
    }
}

@Composable
private fun PitfallCard() {
    SectionCard(title = "6. 걸리는 것들") {
        PitfallRow(
            "Local 을 채우지 않으면 바로 예외",
            "컴포저블 핸들러는 디스패처를 CompositionLocal 에서 찾는다. 루트를 만들 때는 " +
                "rememberNavigationEventDispatcherOwner(parent = null) 처럼 parent 를 명시해야 한다."
        )
        PitfallRow(
            "NavigationEventInfo 는 추상 클래스다",
            "인터페이스가 아니라서 `data class X(...) : NavigationEventInfo()` 로 상속해야 한다. " +
                "내용은 라이브러리가 해석하지 않고 그대로 실어 나른다."
        )
        PitfallRow(
            "진행률은 콜백이 아니라 상태로 온다",
            "컴포저블 핸들러의 파라미터는 onBackCompleted/onBackCancelled 뿐이다. " +
                "중간 진행은 transitionState 가 InProgress 일 때의 latestEvent.progress 로 읽는다."
        )
        PitfallRow(
            "backInfo 의 순서는 취향이 아니다",
            "mergedHistory 가 backInfo → current → forwardInfo 로 이어 붙이므로 backInfo 는 오래된 것부터다."
        )
        PitfallRow(
            "라이브러리는 이벤트 순서를 강제하지 않는다",
            "backStarted 없이 backCompleted 만 불러도 콜백은 그대로 온다(실측). 반대로 backProgressed 만 " +
                "부르면 조용히 무시된다. 순서 보장은 입력원을 만드는 쪽 책임이다."
        )
        PitfallRow(
            "direction 상수는 0 이 back 이 아니다",
            "TRANSITIONING_BACK = -1, TRANSITIONING_FORWARD = 1, TRANSITIONING_UNKNOWN = 0 이다. " +
                "0 을 기본값처럼 비교하면 UNKNOWN 과 헷갈린다."
        )
        PitfallRow(
            "입력원은 붙였으면 떼어야 한다",
            "addInput 한 입력원은 화면을 떠날 때 removeInput 으로 정리한다(이 예제는 DisposableEffect 사용). " +
                "직접 만든 디스패처라면 dispose() 도 함께 고려한다."
        )
        PitfallRow(
            "이 라이브러리는 Compose 버전의 하한을 건다",
            "navigationevent-compose 1.1.2·1.2.0 의 pom 은 compose ui/runtime 1.11.2 를 요구한다. BOM 이 1.11.1 을 " +
                "가리키던 시절(2026.05.00)에는 높은 쪽이 이겨 그 둘만 1.11.2 로 올라가는 패치 스큐가 생겼고, " +
                "BOM 2026.06.01(1.11.4)로 올린 지금은 BOM 쪽이 더 높아 스큐가 사라졌다. 실제로 쓰는 API(HostDefaultKey)는 " +
                "1.11.1 에도 있어 호환 요구일 뿐이지만, BOM 을 1.11.2 미만으로 내리면 다시 끌어올려진다."
        )
    }
}

// ==================== 표시 헬퍼 ====================

/** transitionState 를 화면에 적을 문자열로 바꾼다. Idle/InProgress 둘뿐이라 when 이 exhaustive 하다(else 불필요). */
private fun NavigationEventTransitionState.describe(): String = when (this) {
    is NavigationEventTransitionState.Idle -> "Idle (전환 없음)"
    is NavigationEventTransitionState.InProgress -> {
        val direction = when (direction) {
            NavigationEventTransitionState.TRANSITIONING_BACK -> "BACK"
            NavigationEventTransitionState.TRANSITIONING_FORWARD -> "FORWARD"
            else -> "UNKNOWN"
        }
        "InProgress(dir=$direction, progress=%.2f)".format(latestEvent.progress)
    }
}

// ==================== 공통 요소 ====================

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
    Text(text = text.boldMarkup(), fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text.boldMarkup(), fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
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
            text = first.boldMarkup(),
            modifier = Modifier.width(72.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second.boldMarkup(),
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
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
            modifier = Modifier.width(92.dp),
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
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = Color.White)
    }
}
