package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// ============================================================================
// 모델 — 하나의 스트로크는 "점(Offset) 리스트 + 색상 + 굵기"의 불변 데이터
// ============================================================================
private data class DrawStroke(
    val points: List<Offset>,
    val color: Color,
    val width: Float,
)

// MVI 상태 — 완료된 스트로크/redo 스택/그리는 중인 스트로크/현재 색상·굵기
private data class DrawBoxState(
    val strokes: List<DrawStroke> = emptyList(),
    val redoStack: List<DrawStroke> = emptyList(),
    val liveStroke: DrawStroke? = null,
    val color: Color = Color.Black,
    val width: Float = 8f,
)

// MVI 인텐트 — 사용자/제스처가 만들어내는 단방향 이벤트
private sealed interface DrawIntent {
    data class StartStroke(val at: Offset) : DrawIntent
    data class Drag(val to: Offset) : DrawIntent
    data object EndStroke : DrawIntent
    data object Undo : DrawIntent
    data object Redo : DrawIntent
    data object Clear : DrawIntent
    data class SetColor(val color: Color) : DrawIntent
    data class SetWidth(val width: Float) : DrawIntent
}

// 순수 리듀서 — (현재 상태, 인텐트) → 다음 상태. 부수효과 없음
private fun reduce(state: DrawBoxState, intent: DrawIntent): DrawBoxState = when (intent) {
    is DrawIntent.StartStroke ->
        state.copy(liveStroke = DrawStroke(listOf(intent.at), state.color, state.width))

    is DrawIntent.Drag ->
        state.copy(liveStroke = state.liveStroke?.let { it.copy(points = it.points + intent.to) })

    DrawIntent.EndStroke -> {
        val live = state.liveStroke
        if (live == null || live.points.isEmpty()) {
            state.copy(liveStroke = null)
        } else {
            // 새 스트로크를 확정하면 redo 히스토리는 무효화
            state.copy(
                strokes = state.strokes + live,
                liveStroke = null,
                redoStack = emptyList()
            )
        }
    }

    DrawIntent.Undo ->
        if (state.strokes.isEmpty()) state
        else state.copy(
            strokes = state.strokes.dropLast(1),
            redoStack = state.redoStack + state.strokes.last()
        )

    DrawIntent.Redo ->
        if (state.redoStack.isEmpty()) state
        else state.copy(
            strokes = state.strokes + state.redoStack.last(),
            redoStack = state.redoStack.dropLast(1)
        )

    DrawIntent.Clear ->
        state.copy(strokes = emptyList(), redoStack = emptyList(), liveStroke = null)

    is DrawIntent.SetColor -> state.copy(color = intent.color)
    is DrawIntent.SetWidth -> state.copy(width = intent.width)
}

private val palette = listOf(
    Color.Black,
    Color(0xFFE53935), // Red
    Color(0xFF1E88E5), // Blue
    Color(0xFF43A047), // Green
    Color(0xFFFB8C00), // Orange
    Color(0xFF8E24AA), // Purple
)

@Composable
fun FreehandDrawingExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Freehand Drawing",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { DrawingCanvasCard() }
            item { MviPatternCard() }
            item { ImplementationCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "자유 곡선 드로잉 캔버스 (순수 Compose)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "외부 라이브러리 없이 Canvas + pointerInput(detectDragGestures)만으로 " +
                        "필기/서명 같은 자유 곡선을 그립니다. DrawBox의 MVI 아키텍처를 차용해 " +
                        "상태(스트로크 목록·색상·굵기)를 단방향 흐름으로 관리하고, " +
                        "불변 리스트 기반으로 Undo/Redo를 구현합니다." +
                        "\n\n• 드래그로 점을 누적 → 하나의 스트로크(Path)로 렌더" +
                        "\n• DrawIntent + reduce()로 모든 상태 변경을 일원화" +
                        "\n• Undo: strokes → redoStack / Redo: 그 반대로 이동",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun DrawingCanvasCard() {
    // MVI 상태 보관 — 모든 변경은 dispatch(intent) → reduce() 단방향으로만
    val stateHolder = remember { mutableStateOf(DrawBoxState()) }
    val state = stateHolder.value
    val dispatch: (DrawIntent) -> Unit = { intent ->
        stateHolder.value = reduce(stateHolder.value, intent)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. 그리기 캔버스 (드래그로 그리기)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "캔버스 위를 드래그하면 손가락 경로가 스트로크로 그려집니다. " +
                        "색상·굵기를 바꾸고 Undo/Redo/Clear로 히스토리를 조작해 보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ---- 그리기 영역 ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> dispatch(DrawIntent.StartStroke(offset)) },
                                onDrag = { change, _ -> dispatch(DrawIntent.Drag(change.position)) },
                                onDragEnd = { dispatch(DrawIntent.EndStroke) },
                                onDragCancel = { dispatch(DrawIntent.EndStroke) }
                            )
                        }
                ) {
                    // 완료된 스트로크 + 그리는 중인 스트로크를 함께 렌더
                    state.strokes.forEach { drawStroke(it) }
                    state.liveStroke?.let { drawStroke(it) }
                }

                if (state.strokes.isEmpty() && state.liveStroke == null) {
                    Text(
                        text = "여기를 드래그해서 그려보세요",
                        color = Color(0xFFBDBDBD),
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Text(
                    text = "strokes=${state.strokes.size}  redo=${state.redoStack.size}",
                    color = Color(0xFF9E9E9E),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- 색상 팔레트 ----
            Text(text = "색상", fontSize = 12.sp, color = Color(0xFF616161))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                palette.forEach { color ->
                    ColorSwatch(
                        color = color,
                        selected = state.color == color,
                        onClick = { dispatch(DrawIntent.SetColor(color)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---- 굵기 슬라이더 ----
            Text(
                text = "굵기: ${state.width.toInt()}px",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
            Slider(
                value = state.width,
                onValueChange = { dispatch(DrawIntent.SetWidth(it)) },
                valueRange = 2f..40f
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ---- 히스토리 버튼 ----
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    label = "Undo",
                    enabled = state.strokes.isNotEmpty(),
                    container = Color(0xFF1976D2)
                ) { dispatch(DrawIntent.Undo) }
                ActionButton(
                    label = "Redo",
                    enabled = state.redoStack.isNotEmpty(),
                    container = Color(0xFF388E3C)
                ) { dispatch(DrawIntent.Redo) }
                ActionButton(
                    label = "Clear",
                    enabled = state.strokes.isNotEmpty() || state.liveStroke != null,
                    container = Color(0xFF757575)
                ) { dispatch(DrawIntent.Clear) }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color(0xFF1976D2) else Color(0xFFBDBDBD),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}

@Composable
private fun ActionButton(
    label: String,
    enabled: Boolean,
    container: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            disabledContainerColor = Color(0xFFE0E0E0)
        )
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color(0xFF9E9E9E),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MviPatternCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. MVI 단방향 상태 흐름",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "제스처와 버튼은 직접 상태를 바꾸지 않고 DrawIntent를 발행만 합니다. " +
                        "순수 reduce() 함수가 (상태, 인텐트) → 다음 상태를 계산해 단방향 흐름을 만듭니다. " +
                        "상태가 불변(immutable)이라 Undo/Redo가 리스트 이동만으로 안전하게 동작합니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                code = """
                sealed interface DrawIntent {
                    data class StartStroke(val at: Offset) : DrawIntent
                    data class Drag(val to: Offset) : DrawIntent
                    data object EndStroke : DrawIntent
                    data object Undo : DrawIntent
                    data object Redo : DrawIntent
                    data object Clear : DrawIntent
                    ...
                }

                fun reduce(state: DrawBoxState, intent: DrawIntent) = when (intent) {
                    is StartStroke -> state.copy(
                        liveStroke = DrawStroke(listOf(intent.at), state.color, state.width)
                    )
                    is Drag -> state.copy(
                        liveStroke = state.liveStroke?.let {
                            it.copy(points = it.points + intent.to)
                        }
                    )
                    EndStroke -> state.copy(
                        strokes = state.strokes + state.liveStroke!!,
                        liveStroke = null,
                        redoStack = emptyList() // 새 스트로크 → redo 무효화
                    )
                    Undo -> state.copy(
                        strokes = state.strokes.dropLast(1),
                        redoStack = state.redoStack + state.strokes.last()
                    )
                    Redo -> state.copy(
                        strokes = state.strokes + state.redoStack.last(),
                        redoStack = state.redoStack.dropLast(1)
                    )
                    ...
                }
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun ImplementationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. 제스처 → Path 렌더링 & 내보내기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• detectDragGestures: onDragStart에서 스트로크를 시작하고 onDrag의 " +
                        "change.position을 점으로 누적, onDragEnd/onDragCancel에서 확정합니다.\n" +
                        "• drawPath + Stroke(cap=Round, join=Round): 점들을 lineTo로 이어 부드러운 " +
                        "선으로 그립니다. 더 매끄럽게 하려면 인접 점의 중점으로 quadraticBezierTo를 쓸 수 있습니다.\n" +
                        "• 점이 1개뿐이면(드래그 슬롭 미달) 작은 원으로 점을 찍어 표현합니다.\n" +
                        "• PNG 내보내기는 graphicsLayer로 화면을 캡처해 ImageBitmap → Bitmap으로 " +
                        "변환 후 compress합니다(아래 코드 참고).",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                code = """
                // 스트로크 렌더링
                fun DrawScope.drawStroke(s: DrawStroke) {
                    val path = Path().apply {
                        moveTo(s.points.first().x, s.points.first().y)
                        for (i in 1 until s.points.size)
                            lineTo(s.points[i].x, s.points[i].y)
                    }
                    drawPath(
                        path = path,
                        color = s.color,
                        style = Stroke(
                            width = s.width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // PNG 내보내기 (개념)
                val layer = rememberGraphicsLayer()
                Canvas(Modifier.drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                    drawLayer(layer)
                }) { /* strokes 그리기 */ }
                // 저장 시: val bitmap = layer.toImageBitmap().asAndroidBitmap()
                //          bitmap.compress(PNG, 100, outputStream)
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFE0E0E0),
        lineHeight = 18.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp)
    )
}

// ============== 렌더링 헬퍼 ==============
private fun DrawScope.drawStroke(stroke: DrawStroke) {
    val points = stroke.points
    if (points.isEmpty()) return

    // 점이 하나뿐이면(드래그 슬롭 미달) 점을 원으로 표현
    if (points.size == 1) {
        drawCircle(
            color = stroke.color,
            radius = stroke.width / 2f,
            center = points.first()
        )
        return
    }

    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
    drawPath(
        path = path,
        color = stroke.color,
        style = Stroke(
            width = stroke.width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
