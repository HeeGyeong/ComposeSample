package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val MONTHS = listOf(
    "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
    "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
)

@Composable
fun MonthPickerDialExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Month Picker Dial",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { InteractiveDialCard() }
            item { AngleCalculationCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Airbnb ChromaDial 개요",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Airbnb의 월 선택 다이얼은 Canvas로 12개 세그먼트를 원형으로 배치하고, " +
                        "atan2로 터치 각도를 계산해 가장 가까운 월로 스냅합니다. " +
                        "각 세그먼트는 폴라 좌표(cos/sin)로 위치가 결정되며, 드래그 중에는 " +
                        "실시간 각도를 유지하다가 터치 종료 시 spring 애니메이션으로 스냅됩니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun InteractiveDialCard() {
    val scope = rememberCoroutineScope()
    // 현재 다이얼 회전 각도 (degrees, 0=12시 방향)
    val rotation = remember { Animatable(0f) }
    var selectedMonth by remember { mutableIntStateOf(0) }

    // 회전 각도 변화 → 선택된 월 인덱스 동기화 (360 / 12 = 30도 단위)
    // 라벨은 `index * 30° + rotation - 90°` 위치에 그려지므로,
    // 12시 방향(-90°)에 오는 라벨의 index를 역산하면 (12 - rotation/30) % 12
    LaunchedEffect(rotation) {
        snapshotFlow { rotation.value }.collectLatest { angle ->
            val normalized = ((angle % 360f) + 360f) % 360f
            selectedMonth = (12 - (normalized / 30f).roundToInt()) % 12
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "드래그하여 월 선택",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val textMeasurer = rememberTextMeasurer()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // 터치 각도 변화를 rotation에 누적
                            var previousAngle = 0f
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    previousAngle = angleOf(center, startOffset)
                                },
                                onDrag = { change, _ ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val currentAngle = angleOf(center, change.position)
                                    var delta = currentAngle - previousAngle
                                    // 180도 경계 처리
                                    if (delta > 180f) delta -= 360f
                                    if (delta < -180f) delta += 360f
                                    scope.launch {
                                        rotation.snapTo(rotation.value + delta)
                                    }
                                    previousAngle = currentAngle
                                    change.consume()
                                },
                                onDragEnd = {
                                    // 30도 단위로 스냅 (spring 애니메이션)
                                    val current = rotation.value
                                    val target = (current / 30f).roundToInt() * 30f
                                    scope.launch {
                                        rotation.animateTo(
                                            targetValue = target,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                    }
                                }
                            )
                        }
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val labelRadius = radius * 0.78f

                    // 원형 트랙
                    drawCircle(
                        color = Color(0xFFE0E0E0),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // 12개월 라벨 — rotation만큼 회전시켜 배치
                    MONTHS.forEachIndexed { index, label ->
                        // 12시 방향부터 시계 방향. index*30도 + 현재 rotation
                        val angleDeg = index * 30f + rotation.value - 90f
                        val angleRad = angleDeg * PI.toFloat() / 180f
                        val x = center.x + labelRadius * cos(angleRad)
                        val y = center.y + labelRadius * sin(angleRad)

                        // 12시 방향(상단 중앙)에 위치한 월이 선택됨 — selectedMonth 상태 재사용
                        val selected = index == selectedMonth

                        val labelColor = if (selected) Color(0xFF1976D2) else Color(0xFF9E9E9E)
                        val result = textMeasurer.measure(
                            text = label,
                            style = TextStyle(
                                fontSize = if (selected) 16.sp else 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = labelColor
                            )
                        )
                        drawText(
                            textLayoutResult = result,
                            topLeft = Offset(
                                x - result.size.width / 2f,
                                y - result.size.height / 2f
                            )
                        )
                    }

                    // 상단 인디케이터(선택된 월을 가리키는 삼각형 위치의 도트)
                    drawCircle(
                        color = Color(0xFF1976D2),
                        radius = 6.dp.toPx(),
                        center = Offset(center.x, center.y - radius - 4.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "선택된 월: ${MONTHS[selectedMonth]}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AngleCalculationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "각도 계산 — atan2로 터치 방향 구하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "터치 지점과 다이얼 중심 사이의 벡터로 현재 각도를 계산합니다. " +
                        "이전 각도와의 차이(delta)를 회전에 누적하면 손가락이 원을 따라가는 효과를 얻습니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// 중심→터치 벡터의 각도(degrees)\n" +
                        "fun angleOf(center: Offset, touch: Offset): Float {\n" +
                        "    val dx = touch.x - center.x\n" +
                        "    val dy = touch.y - center.y\n" +
                        "    return (atan2(dy, dx) * 180f / PI.toFloat())\n" +
                        "}\n\n" +
                        "// 드래그 중 각도 변화를 누적\n" +
                        "val currentAngle = angleOf(center, change.position)\n" +
                        "var delta = currentAngle - previousAngle\n" +
                        "// ±180 경계 처리\n" +
                        "if (delta > 180f) delta -= 360f\n" +
                        "if (delta < -180f) delta += 360f\n" +
                        "rotation.snapTo(rotation.value + delta)\n\n" +
                        "// 드래그 종료 시 30도(1개월) 단위로 스냅\n" +
                        "val target = (rotation.value / 30f).roundToInt() * 30f\n" +
                        "rotation.animateTo(\n" +
                        "    targetValue = target,\n" +
                        "    animationSpec = spring(\n" +
                        "        dampingRatio = MediumBouncy,\n" +
                        "        stiffness = Low\n" +
                        "    )\n" +
                        ")",
                borderColor = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "폴라 좌표(cos/sin)로 12개 월 라벨을 원주에 등간격 배치 (30도 단위)",
                "atan2(dy, dx)로 중심→터치 벡터의 각도를 계산하여 드래그 회전 추적",
                "각도 delta 계산 시 ±180도 경계를 넘는 경우 보정 필수 (12시 방향 wrap)",
                "드래그 종료 시 spring(MediumBouncy) 애니메이션으로 가장 가까운 30도로 스냅",
                "snapshotFlow + collectLatest로 rotation 변화를 구독해 선택된 월 인덱스 계산"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}

// 중심→터치 벡터의 각도(degrees, -180~180)
private fun angleOf(center: Offset, touch: Offset): Float {
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    return (atan2(dy, dx) * 180f / PI.toFloat())
}
