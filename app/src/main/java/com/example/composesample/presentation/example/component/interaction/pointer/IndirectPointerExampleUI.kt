package com.example.composesample.presentation.example.component.interaction.pointer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.indirect.IndirectPointerEvent
import androidx.compose.ui.input.indirect.IndirectPointerEventPrimaryDirectionalMotionAxis
import androidx.compose.ui.input.indirect.IndirectPointerEventType
import androidx.compose.ui.input.indirect.IndirectPointerInputModifierNode
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlin.math.roundToInt

// ============================================================================
// 이벤트 로그 링버퍼 — RecomposerRegistryExampleUI 의 RegistryEventRing 과 동일 관례.
// 사전 할당 배열에 O(1) 적재만 하고, 화면에는 latestFirst() 스냅샷만 노출한다.
// ============================================================================
private class PointerLogRing(private val capacity: Int = 40) {
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
        val available = minOf(totalCount, capacity)
        return (1..available).map { i ->
            val index = ((writeIndex - i) % capacity + capacity) % capacity
            buffer[index] ?: ""
        }
    }
}

// ============================================================================
// IndirectPointerInputModifierNode 부착 — pointerInput{} 같은 상위 헬퍼가 없어
// ModifierNodeElement 로 Modifier.Node 를 직접 델리게이트해야 한다(DelegatableNode 는
// Modifier.Node 가 이미 구현하므로 별도 상속 불필요).
// ============================================================================
private class IndirectPointerLogNode(
    var onEvent: (IndirectPointerEvent) -> Unit
) : Modifier.Node(), IndirectPointerInputModifierNode {
    override fun onIndirectPointerEvent(event: IndirectPointerEvent, pass: PointerEventPass) {
        if (pass == PointerEventPass.Main) onEvent(event)
    }

    override fun onCancelIndirectPointerInput() = Unit
}

private data class IndirectPointerLogElement(
    val onEvent: (IndirectPointerEvent) -> Unit
) : ModifierNodeElement<IndirectPointerLogNode>() {
    override fun create() = IndirectPointerLogNode(onEvent)
    override fun update(node: IndirectPointerLogNode) {
        node.onEvent = onEvent
    }
}

private fun Modifier.logIndirectPointerEvents(onEvent: (IndirectPointerEvent) -> Unit): Modifier =
    this then IndirectPointerLogElement(onEvent)

@Composable
fun IndirectPointerExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Indirect Pointer Example",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(12.dp)); ConceptCard() }
            item { Spacer(Modifier.height(12.dp)); IndirectPointerCaptureCard() }
            item { Spacer(Modifier.height(12.dp)); PointerPipelineObservatoryCard() }
            item { Spacer(Modifier.height(12.dp)); ComparisonSummaryCard() }
            item { Spacer(Modifier.height(12.dp)); CaveatsCard() }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "간접 포인터(Indirect Pointer)란",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "터치스크린을 직접 누르는 입력이 아니라, 트랙패드처럼 화면과 분리된 표면을 움직여 " +
                    "화면 위 커서 위치를 원격 조작하는 장치의 입력이다. Compose는 이 입력을 " +
                    "IndirectPointerInputModifierNode(DelegatableNode 확장)로 노출한다.",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "onIndirectPointerEvent(event, pass) / onCancelIndirectPointerInput()",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6200EE)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "event.changes: List<IndirectPointerInputChange>(id/position/pressed/pressure) · " +
                    "event.type: IndirectPointerEventType(Unknown/Press/Release/Move) · " +
                    "event.primaryDirectionalMotionAxis: IndirectPointerEventPrimaryDirectionalMotionAxis(None/X/Y)",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "바이트코드 확인: 위 인터페이스/타입들은 @OptIn 없이 바로 사용 가능하다 — 단 이벤트를 " +
                    "합성 주입하는 테스트 전용 훅(RootForTest.sendIndirectPointerEvent 등)은 " +
                    "@ExperimentalIndirectPointerApi로 별도 게이팅돼 있다(아래 주의사항 참고)",
                fontSize = 10.sp,
                color = Color(0xFF388E3C)
            )
        }
    }
}

@Composable
private fun IndirectPointerCaptureCard() {
    val ring = remember { PointerLogRing() }
    var eventCount by remember { mutableIntStateOf(0) }
    var lastAxis by remember { mutableStateOf("-") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "실동작 1 — 원시(raw) 간접 포인터 캡처",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "아래 영역에 logIndirectPointerEvents() 가 부착되어 있다 (수신 이벤트: $eventCount 건, " +
                    "최근 축=$lastAxis)",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFCE93D8), RoundedCornerShape(8.dp))
                    .logIndirectPointerEvents { event ->
                        eventCount++
                        lastAxis = when (event.primaryDirectionalMotionAxis) {
                            IndirectPointerEventPrimaryDirectionalMotionAxis.X -> "X"
                            IndirectPointerEventPrimaryDirectionalMotionAxis.Y -> "Y"
                            else -> "None"
                        }
                        val typeLabel = when (event.type) {
                            IndirectPointerEventType.Press -> "Press"
                            IndirectPointerEventType.Release -> "Release"
                            IndirectPointerEventType.Move -> "Move"
                            else -> "Unknown"
                        }
                        event.changes.forEach { change ->
                            ring.add(
                                "#$eventCount type=$typeLabel axis=$lastAxis " +
                                    "id=${change.id.value} pos=(${change.position.x.roundToInt()}," +
                                    "${change.position.y.roundToInt()}) pressed=${change.pressed} " +
                                    "pressure=${"%.2f".format(change.pressure)}"
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (eventCount == 0) {
                    Text(
                        text = "이벤트 대기 중 — 트랙패드 등 간접 포인터 장치가 연결된 기기에서만 발생",
                        fontSize = 10.sp,
                        color = Color(0xFF7B1FA2)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val lines = ring.latestFirst()
            if (lines.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .background(Color(0xFF212121), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    lines.take(6).forEach { line ->
                        Text(text = line, fontSize = 9.sp, color = Color(0xFF9CCC65))
                    }
                }
            }
        }
    }
}

@Composable
private fun PointerPipelineObservatoryCard() {
    val seenTypes = remember { mutableStateListOf<String>() }
    var lastLine by remember { mutableStateOf("아직 터치 없음") }

    val trackpadOnlyTypes = listOf(
        "PanStart", "PanMove", "PanEnd", "ScaleStart", "ScaleChange", "ScaleEnd"
    )
    val reachableTypes = listOf("Press", "Move", "Release")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "실동작 2 — 표준 PointerEvent 파이프라인의 세분화된 타입 관찰",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "간접 포인터 전용 인터페이스와 별개로, 기존 Modifier.pointerInput() 이 받는 " +
                    "PointerEventType 에도 트랙패드용 값이 이미 추가돼 있다 — 아래 상자를 손가락 1~2개로 " +
                    "눌러/움직여/떼며 어떤 타입이 켜지는지 직접 확인해보라.",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val typeLabel = when (event.type) {
                                    PointerEventType.Press -> "Press"
                                    PointerEventType.Release -> "Release"
                                    PointerEventType.Move -> "Move"
                                    PointerEventType.Enter -> "Enter"
                                    PointerEventType.Exit -> "Exit"
                                    PointerEventType.Scroll -> "Scroll"
                                    PointerEventType.PanStart -> "PanStart"
                                    PointerEventType.PanMove -> "PanMove"
                                    PointerEventType.PanEnd -> "PanEnd"
                                    PointerEventType.ScaleStart -> "ScaleStart"
                                    PointerEventType.ScaleChange -> "ScaleChange"
                                    PointerEventType.ScaleEnd -> "ScaleEnd"
                                    else -> "Unknown"
                                }
                                if (typeLabel !in seenTypes) seenTypes.add(typeLabel)

                                val pointerTypeLabel = event.changes.firstOrNull()?.let { change ->
                                    when (change.type) {
                                        PointerType.Touch -> "Touch"
                                        PointerType.Mouse -> "Mouse"
                                        PointerType.Stylus -> "Stylus"
                                        PointerType.Eraser -> "Eraser"
                                        else -> "Unknown"
                                    }
                                } ?: "-"

                                lastLine = "type=$typeLabel pointerType=$pointerTypeLabel " +
                                    "pointers=${event.changes.size}"
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = lastLine, fontSize = 10.sp, color = Color(0xFF1565C0))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "지금까지 관측된 타입 (터치스크린으로 도달 가능)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            TypeChipRow(labels = reachableTypes, litLabels = seenTypes, litColor = Color(0xFF388E3C))
            Spacer(Modifier.height(6.dp))
            Text(
                text = "트랙패드 2손가락 제스처 전용 (MotionEvent.classification 필요 — 터치스크린 " +
                    "멀티터치로는 절대 켜지지 않는다)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            TypeChipRow(labels = trackpadOnlyTypes, litLabels = seenTypes, litColor = Color(0xFFEF6C00))
        }
    }
}

@Composable
private fun TypeChipRow(labels: List<String>, litLabels: List<String>, litColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEach { label ->
            val isLit = label in litLabels
            Box(
                modifier = Modifier
                    .background(
                        color = if (isLit) litColor else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = if (isLit) Color.White else Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
private fun ComparisonSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "두 파이프라인의 역할 차이",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF3700B3)
            )
            Spacer(Modifier.height(8.dp))
            ComparisonRow(
                label = "IndirectPointerInputModifierNode",
                desc = "트랙패드 커서 자체의 원시 위치/눌림 상태 + 주 이동축(X/Y) 힌트를 그대로 전달. " +
                    "멀티터치 의미론과 무관하게 이 인터페이스에만 별도로 도달한다."
            )
            Spacer(Modifier.height(6.dp))
            ComparisonRow(
                label = "PointerEventType.Pan*/Scale*",
                desc = "트랙패드의 2손가락 제스처를 MotionEvent.classification(TWO_FINGER_SWIPE/PINCH)으로 " +
                    "인식해, 기존 터치용 PointerEvent 파이프라인 위에 합성해 넣는다. Modifier.transformable/" +
                    "scrollable 같은 기존 위젯이 트랙패드도 특별 취급 없이 그대로 동작하게 하기 위함 — " +
                    "ComposeUiFlags.isTrackpadGestureHandlingEnabled(기본 true) + API 29+/34+ 필요."
            )
        }
    }
}

@Composable
private fun ComparisonRow(label: String, desc: String) {
    Column {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6200EE))
        Text(text = desc, fontSize = 11.sp, color = Color.DarkGray)
    }
}

@Composable
private fun CaveatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "⚠️ 확인된 제약",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE65100)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "• 실동작 1(원시 캡처)과 실동작 2 하단 주황 칩(Pan*/Scale*)은 실제 트랙패드 같은 " +
                    "간접 포인터 장치가 이 기기에 연결돼 있어야 켜진다 — 터치스크린만 있는 기기에서는 " +
                    "0건으로 남는 것이 정상 동작이다.\n" +
                    "• RootForTest.sendIndirectPointerEvent(IndirectPointerEvent)가 트랙패드 없이 합성 " +
                    "이벤트를 주입할 수 있는 default 테스트 훅으로 존재하지만 @ExperimentalIndirectPointerApi로 " +
                    "게이팅돼 있고(IndirectPointerInputModifierNode 자체와는 별도 opt-in), 프로덕션 예제 " +
                    "코드에서는 사용하지 않는다.\n" +
                    "• 이 실기기(설치된 앱이 별도 사용자 프로필 소속이라 서명 충돌로 재설치 불가)에서는 " +
                    "합성 이벤트 주입 → onIndirectPointerEvent() 도달 여부를 계측하지 못했다 — 원시 캡처 " +
                    "카드는 프로브 컴파일까지만 검증됐고 실제 디스패치는 미검증 상태다.\n" +
                    "• 실동작 2 상단(Press/Move/Release, Touch/Mouse/Stylus)는 이 기기 터치스크린으로도 " +
                    "바로 재현 가능 — 위 상자를 눌러보면 즉시 초록 칩이 켜진다.",
                fontSize = 11.sp,
                color = Color(0xFFE65100)
            )
        }
    }
}
