package com.example.composesample.presentation.example.component.ui.accessibility

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Accessible Focus Indicator Example
 *
 * 키보드/D-pad 사용자를 위한 포커스 시각화 4가지 패턴 비교:
 * - 기본 indication
 * - Outline (collectIsFocusedAsState + Modifier.border)
 * - Scale + Pulse (애니메이션)
 * - Indication API (커스텀 IndicationNodeFactory + DrawModifierNode)
 */
@Composable
fun AccessibleFocusIndicatorExampleUI(onBackEvent: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }

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
            TabChip("기본", tab == 0, { tab = 0 }, Modifier.weight(1f))
            TabChip("Outline", tab == 1, { tab = 1 }, Modifier.weight(1f))
            TabChip("Pulse", tab == 2, { tab = 2 }, Modifier.weight(1f))
            TabChip("Indication", tab == 3, { tab = 3 }, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        when (tab) {
            0 -> DefaultFocusDemo()
            1 -> OutlineFocusDemo()
            2 -> PulseFocusDemo()
            3 -> CustomIndicationDemo()
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(Modifier.size(8.dp))
            Column {
                Text(
                    text = "Accessible Focus Indicator",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "키보드/D-pad 포커스 시각화 + Indication API",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1976D2) else Color.White,
            contentColor = if (selected) Color.White else Color(0xFF424242)
        )
    ) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

/* ----------------------------------------------------------------------- */
/* Demo 0: 기본 focusable — 키보드 포커스 시 시각 표시가 거의 없음           */
/* ----------------------------------------------------------------------- */
@Composable
private fun DefaultFocusDemo() {
    DemoScaffold(
        title = "기본 Modifier.focusable()",
        description = "기본 indication만 적용. 키보드 사용자에게 시각 단서가 거의 없어 접근성이 떨어짐."
    ) { requesters ->
        repeat(4) { idx ->
            val source = remember { MutableInteractionSource() }
            FocusBox(
                label = "${idx + 1}",
                modifier = Modifier
                    .focusRequester(requesters[idx])
                    .focusable(interactionSource = source)
            )
        }
    }
}

/* ----------------------------------------------------------------------- */
/* Demo 1: Outline — collectIsFocusedAsState + Modifier.border              */
/* ----------------------------------------------------------------------- */
@Composable
private fun OutlineFocusDemo() {
    DemoScaffold(
        title = "Outline (border + collectIsFocusedAsState)",
        description = "포커스 상태를 State로 수집한 뒤 Modifier.border 두께/색상을 전환. 가장 단순한 접근성 강화 방법."
    ) { requesters ->
        repeat(4) { idx ->
            val source = remember { MutableInteractionSource() }
            val isFocused by source.collectIsFocusedAsState()
            FocusBox(
                label = "${idx + 1}",
                isFocused = isFocused,
                modifier = Modifier
                    .border(
                        width = if (isFocused) 3.dp else 0.dp,
                        color = if (isFocused) Color(0xFFD32F2F) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .focusRequester(requesters[idx])
                    .focusable(interactionSource = source)
            )
        }
    }
}

/* ----------------------------------------------------------------------- */
/* Demo 2: Pulse — 무한 반복 애니메이션으로 시선 유도                        */
/* ----------------------------------------------------------------------- */
@Composable
private fun PulseFocusDemo() {
    var pulseToggle by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(700)
            pulseToggle = !pulseToggle
        }
    }
    DemoScaffold(
        title = "Scale + Pulse 애니메이션",
        description = "포커스된 요소만 700ms 주기 scale 펄스. 모션 멀미 사용자 고려 시 사용 자제 권장."
    ) { requesters ->
        repeat(4) { idx ->
            val source = remember { MutableInteractionSource() }
            val isFocused by source.collectIsFocusedAsState()
            val targetScale = when {
                !isFocused -> 1f
                pulseToggle -> 1.08f
                else -> 1f
            }
            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = tween(700),
                label = "boxScale"
            )
            FocusBox(
                label = "${idx + 1}",
                isFocused = isFocused,
                modifier = Modifier
                    .scale(animatedScale)
                    .border(
                        width = if (isFocused) 2.dp else 0.dp,
                        color = if (isFocused) Color(0xFF388E3C) else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .focusRequester(requesters[idx])
                    .focusable(interactionSource = source)
            )
        }
    }
}

/* ----------------------------------------------------------------------- */
/* Demo 3: Custom IndicationNodeFactory                                     */
/* ----------------------------------------------------------------------- */
@Composable
private fun CustomIndicationDemo() {
    val customIndication = remember {
        FocusOutlineIndication(color = Color(0xFF7B1FA2), strokeWidth = 3.dp)
    }
    DemoScaffold(
        title = "IndicationNodeFactory (Compose 1.7+)",
        description = "DrawModifierNode 로 ContentDrawScope에서 직접 외곽선 렌더링. Modifier.indication() 으로 어디든 재사용 가능."
    ) { requesters ->
        repeat(4) { idx ->
            val source = remember { MutableInteractionSource() }
            FocusBox(
                label = "${idx + 1}",
                modifier = Modifier
                    .indication(source, customIndication)
                    .focusRequester(requesters[idx])
                    .focusable(interactionSource = source)
            )
        }
    }
}

/* ----------------------------------------------------------------------- */
/* 공통 — 4개 박스 + 이전/다음 포커스 버튼                                   */
/* ----------------------------------------------------------------------- */
@Composable
private fun DemoScaffold(
    title: String,
    description: String,
    content: @Composable (List<FocusRequester>) -> Unit
) {
    val requesters = remember { List(4) { FocusRequester() } }
    var current by remember { mutableIntStateOf(-1) }

    LaunchedEffect(current) {
        if (current in 0..3) {
            requesters[current].requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
        Spacer(Modifier.height(4.dp))
        Text(text = description, fontSize = 12.sp, color = Color(0xFF616161))
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content(requesters)
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { current = ((if (current < 0) 0 else current) - 1 + 4) % 4 },
                modifier = Modifier.weight(1f)
            ) { Text("◀ 이전 포커스") }
            Button(
                onClick = { current = ((if (current < 0) -1 else current) + 1) % 4 },
                modifier = Modifier.weight(1f)
            ) { Text("다음 포커스 ▶") }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "💡 키보드 Tab/방향키로도 포커스 이동 가능. 버튼은 프로그래매틱 포커스 요청 데모.",
            fontSize = 11.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun FocusBox(
    label: String,
    isFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .size(width = 70.dp, height = 70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isFocused) Color(0xFFE3F2FD) else Color.White)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )
    }
}

/* ----------------------------------------------------------------------- */
/* IndicationNodeFactory — 포커스 시 외곽선을 그리는 커스텀 인디케이션        */
/* ----------------------------------------------------------------------- */
private class FocusOutlineIndication(
    private val color: Color,
    private val strokeWidth: Dp
) : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return FocusOutlineNode(interactionSource, color, strokeWidth)
    }

    override fun equals(other: Any?): Boolean =
        other is FocusOutlineIndication &&
            color == other.color &&
            strokeWidth == other.strokeWidth

    override fun hashCode(): Int = 31 * color.hashCode() + strokeWidth.hashCode()
}

private class FocusOutlineNode(
    private val source: InteractionSource,
    private val color: Color,
    private val strokeWidth: Dp
) : Modifier.Node(), DrawModifierNode {

    private var isFocused: Boolean = false

    override fun onAttach() {
        coroutineScope.launch {
            source.interactions.collect { interaction ->
                val next = when (interaction) {
                    is FocusInteraction.Focus -> true
                    is FocusInteraction.Unfocus -> false
                    else -> isFocused
                }
                if (next != isFocused) {
                    isFocused = next
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isFocused) {
            drawRect(
                color = color,
                style = Stroke(width = strokeWidth.toPx())
            )
        }
    }
}
