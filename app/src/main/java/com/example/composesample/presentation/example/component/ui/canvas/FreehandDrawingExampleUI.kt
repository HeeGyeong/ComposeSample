package com.example.composesample.presentation.example.component.ui.canvas

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch
import kotlin.math.min

// ============================================================================
// 모델 — 하나의 스트로크는 "입력 기록(점 리스트) + 색상 + 굵기"의 불변 데이터
// ============================================================================
private data class DrawStroke(
    val input: StrokeInput,
    val color: Color,
    val width: Float,
)

// MVI 상태 — 완료된 스트로크/redo 스택/그리는 중인 스트로크/현재 색상·굵기/비교선 표시
private data class DrawBoxState(
    val strokes: List<DrawStroke> = emptyList(),
    val redoStack: List<DrawStroke> = emptyList(),
    val liveStroke: DrawStroke? = null,
    val color: Color = Color.Black,
    val width: Float = 8f,
    val showFrameLine: Boolean = true,
)

// MVI 인텐트 — 사용자/제스처가 만들어내는 단방향 이벤트
private sealed interface DrawIntent {
    data class StartStroke(val at: Offset) : DrawIntent
    data class Drag(val historical: List<Offset>, val position: Offset) : DrawIntent
    data object EndStroke : DrawIntent
    data object Undo : DrawIntent
    data object Redo : DrawIntent
    data object Clear : DrawIntent
    data class SetColor(val color: Color) : DrawIntent
    data class SetWidth(val width: Float) : DrawIntent
    data class SetFrameLine(val show: Boolean) : DrawIntent
}

// 순수 리듀서 — (현재 상태, 인텐트) → 다음 상태. 부수효과 없음
private fun reduce(state: DrawBoxState, intent: DrawIntent): DrawBoxState = when (intent) {
    is DrawIntent.StartStroke ->
        state.copy(liveStroke = DrawStroke(startStroke(intent.at), state.color, state.width))

    is DrawIntent.Drag ->
        state.copy(liveStroke = state.liveStroke?.let {
            it.copy(input = it.input.append(intent.historical, intent.position))
        })

    DrawIntent.EndStroke -> {
        val live = state.liveStroke
        if (live == null || live.input.points.isEmpty()) {
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
    is DrawIntent.SetFrameLine -> state.copy(showFrameLine = intent.show)
}

private val palette = listOf(
    Color.Black,
    Color(0xFFE53935), // Red
    Color(0xFF1E88E5), // Blue
    Color(0xFF43A047), // Green
    Color(0xFFFB8C00), // Orange
    Color(0xFF8E24AA), // Purple
)

/** 비교선(위치만 쌓은 선) 색 */
private val FrameLineColor = Color(0xFFD50000)

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
            item { HistoricalCard() }
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
                text = "외부 라이브러리 없이 Canvas + pointerInput(awaitEachGesture)만으로 " +
                        "필기/서명 같은 자유 곡선을 그립니다. 손가락이 닿은 곳부터, 한 프레임에 묶여 온 " +
                        "중간 샘플(historical)까지 점으로 쌓습니다. DrawBox의 MVI 아키텍처를 차용해 " +
                        "상태(스트로크 목록·색상·굵기)를 단방향 흐름으로 관리하고, " +
                        "불변 리스트 기반으로 Undo/Redo를 구현합니다." +
                        "\n\n• 이벤트마다 historical + position 을 점으로 누적 → 하나의 스트로크(Path)로 렌더" +
                        "\n• DrawIntent + reduce()로 모든 상태 변경을 일원화" +
                        "\n• Undo: strokes → redoStack / Redo: 그 반대로 이동",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
        }
    }
}

/** 재생할 때 읽는 캔버스 위치 — 스크롤마다 바뀌므로 State 로 두지 않는다(스크롤 중 리컴포지션 없음). */
private class CanvasGeometry {
    var onScreen = Offset.Zero
    var size = IntSize.Zero
}

/** 원 재생 결과 — 캔버스가 실제로 받은 점으로 잰다. */
private data class ReplayResult(
    val radius: Float,
    val input: StrokeInput,
    val frameDeviation: Float,
    val allDeviation: Float,
)

@Composable
private fun DrawingCanvasCard() {
    // MVI 상태 보관 — 모든 변경은 dispatch(intent) → reduce() 단방향으로만
    val stateHolder = remember { mutableStateOf(DrawBoxState()) }
    val state = stateHolder.value
    val dispatch: (DrawIntent) -> Unit = { intent ->
        stateHolder.value = reduce(stateHolder.value, intent)
    }

    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val geometry = remember { CanvasGeometry() }
    var canvasFullyVisible by remember { mutableStateOf(false) }
    var replaying by remember { mutableStateOf(false) }
    var replayResult by remember { mutableStateOf<ReplayResult?>(null) }
    var replayError by remember { mutableStateOf<String?>(null) }

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
                        "색상·굵기를 바꾸고 Undo/Redo/Clear로 히스토리를 조작해 보세요. " +
                        "빨간 선은 같은 획을 change.position 만으로 쌓았을 때의 선입니다(2번 카드).",
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
                        .onGloballyPositioned { coordinates ->
                            geometry.onScreen = coordinates.positionOnScreen()
                            geometry.size = coordinates.size
                            // 부모(LazyColumn)에 잘리면 그 부분은 터치를 받지 못한다 → 재생은 전체가 보일 때만
                            canvasFullyVisible = coordinates.boundsInRoot().size == coordinates.size.toSize()
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                // 소비해야 바깥 LazyColumn 이 스크롤로 가져가지 않는다
                                down.consume()
                                // 터치 슬롭을 기다리지 않고 손가락이 닿은 곳에서 시작(탭도 점 하나로 남는다)
                                dispatch(DrawIntent.StartStroke(down.position))
                                try {
                                    while (true) {
                                        val change = awaitPointerEvent().changes
                                            .firstOrNull { it.id == down.id } ?: break
                                        if (change.positionChanged() || change.historical.isNotEmpty()) {
                                            dispatch(
                                                DrawIntent.Drag(
                                                    // 이 이벤트에 묶여 온 중간 샘플(오래된 순) → 이 프레임의 마지막 위치
                                                    historical = change.historical.map { it.position },
                                                    position = change.position
                                                )
                                            )
                                        }
                                        change.consume()
                                        // 손 뗀 이벤트의 위치까지 쌓고 끝낸다
                                        if (!change.pressed) break
                                    }
                                } finally {
                                    dispatch(DrawIntent.EndStroke)
                                }
                            }
                        }
                ) {
                    // 완료된 스트로크 + 그리는 중인 스트로크를 함께 렌더
                    state.strokes.forEach { drawStroke(it, state.showFrameLine) }
                    state.liveStroke?.let { drawStroke(it, state.showFrameLine) }
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

            Spacer(modifier = Modifier.height(8.dp))
            StrokeStatsText(stroke = state.liveStroke ?: state.strokes.lastOrNull())

            Spacer(modifier = Modifier.height(8.dp))

            // ---- 비교선 · 원 재생 ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.showFrameLine,
                    onCheckedChange = { dispatch(DrawIntent.SetFrameLine(it)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "비교선: position 만 쌓은 선(빨강) 겹쳐 보기",
                    fontSize = 12.sp,
                    color = Color(0xFF616161)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ActionButton(
                    label = if (replaying) "재생 중…" else "원 재생",
                    enabled = !replaying && canvasFullyVisible && state.liveStroke == null,
                    container = Color(0xFF6A1B9A)
                ) {
                    replaying = true
                    replayError = null
                    scope.launch {
                        try {
                            val circle = replayCircle(view, geometry)
                            // 손 뗀 이벤트까지 처리된 뒤의 상태를 읽는다
                            withFrameNanos { }
                            val stroke = stateHolder.value.strokes.lastOrNull()
                            val start = stroke?.input?.points?.firstOrNull()
                            // 마지막 획이 재생한 원인지 시작점으로 확인 — 아니면 결과를 내지 않는다
                            if (stroke == null || start == null || (start - circle.start).getDistance() > 1f) {
                                replayResult = null
                                replayError = "재생한 획을 찾지 못했습니다(시작점 불일치)"
                            } else {
                                replayResult = ReplayResult(
                                    radius = circle.radius,
                                    input = stroke.input,
                                    frameDeviation = maxCircleDeviation(stroke.input.framePoints, circle.center, circle.radius),
                                    allDeviation = maxCircleDeviation(stroke.input.points, circle.center, circle.radius)
                                )
                            }
                        } finally {
                            replaying = false
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (canvasFullyVisible) {
                        "${REPLAY_SAMPLE_HZ}Hz 로 샘플링한 원 1바퀴(${REPLAY_DURATION_MS}ms)를 " +
                                "${REPLAY_FRAME_HZ}Hz 프레임으로 묶어 캔버스에 보냅니다"
                    } else {
                        "캔버스 전체가 화면에 보일 때 재생할 수 있습니다"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    lineHeight = 15.sp
                )
            }
            replayError?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = it, fontSize = 12.sp, color = Color(0xFFC62828))
            }
            replayResult?.let { ReplayResultText(it) }

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
                    enabled = !replaying && state.strokes.isNotEmpty(),
                    container = Color(0xFF1976D2)
                ) { dispatch(DrawIntent.Undo) }
                ActionButton(
                    label = "Redo",
                    enabled = !replaying && state.redoStack.isNotEmpty(),
                    container = Color(0xFF388E3C)
                ) { dispatch(DrawIntent.Redo) }
                ActionButton(
                    label = "Clear",
                    enabled = !replaying && (state.strokes.isNotEmpty() || state.liveStroke != null),
                    container = Color(0xFF757575)
                ) { dispatch(DrawIntent.Clear) }
            }
        }
    }
}

@Composable
private fun StrokeStatsText(stroke: DrawStroke?) {
    val text = if (stroke == null) {
        "마지막 획: 없음"
    } else {
        val input = stroke.input
        "마지막 획: 이벤트 ${input.events} · historical ${input.historicalTotal}" +
                "(이벤트당 최대 ${input.maxHistoricalPerEvent})\n" +
                "점: position 만 ${input.framePoints.size} · historical 포함 ${input.points.size}"
    }
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF424242),
        lineHeight = 17.sp
    )
}

@Composable
private fun ReplayResultText(result: ReplayResult) {
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = "재생 결과 (반지름 ${"%.0f".format(result.radius)}px)\n" +
                "position 만: ${result.input.framePoints.size}점 · 원에서 최대 ${"%.1f".format(result.frameDeviation)}px\n" +
                "historical 포함: ${result.input.points.size}점 · 원에서 최대 ${"%.1f".format(result.allDeviation)}px",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF4A148C),
        lineHeight = 17.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF3E5F5))
            .padding(10.dp)
    )
}

/** 재생한 원 — 캔버스 좌표 기준. */
private class ReplayCircle(val center: Offset, val radius: Float, val start: Offset)

/**
 * 원 1바퀴를 프레임마다 MotionEvent 하나로 묶어 창의 루트 뷰에 보낸다.
 * 시스템이 한 프레임 동안 쌓인 MOVE 를 하나로 묶어(batching) 보내는 것과 같은 모양 —
 * 앞의 샘플은 addBatch 로 history 에, 마지막 샘플이 현재 위치가 된다.
 */
private suspend fun replayCircle(view: View, geometry: CanvasGeometry): ReplayCircle {
    val size = geometry.size
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = min(size.width, size.height) * 0.35f
    val frames = replayFrames(center, radius)
    val root = view.rootView
    val rootOnScreen = IntArray(2).also { root.getLocationOnScreen(it) }
    val origin = geometry.onScreen
    val downTime = SystemClock.uptimeMillis()

    frames.forEachIndexed { index, batch ->
        // 프레임마다 이벤트 하나 — 묶음이 프레임 박자로 도착하는 것을 흉내 낸다
        withFrameNanos { }
        val action = if (index == 0) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_MOVE
        sendBatch(root, rootOnScreen, origin, downTime, action, batch)
    }
    withFrameNanos { }
    val last = frames.last().last()
    sendBatch(root, rootOnScreen, origin, downTime, MotionEvent.ACTION_UP, listOf(last))
    return ReplayCircle(center, radius, frames.first().first().position)
}

private fun sendBatch(
    root: View,
    rootOnScreen: IntArray,
    canvasOnScreen: Offset,
    downTime: Long,
    action: Int,
    batch: List<TimedSample>,
) {
    val properties = arrayOf(
        MotionEvent.PointerProperties().apply {
            id = 0
            toolType = MotionEvent.TOOL_TYPE_FINGER
        }
    )

    fun coordsOf(sample: TimedSample) = arrayOf(
        MotionEvent.PointerCoords().apply {
            // 화면 좌표로 만든다 — Compose 는 현재 위치를 rawX/rawY 에서, historical 은 getHistoricalX/Y 에서 읽는다
            x = canvasOnScreen.x + sample.position.x
            y = canvasOnScreen.y + sample.position.y
            pressure = 1f
            size = 1f
        }
    )

    val first = batch.first()
    val event = MotionEvent.obtain(
        downTime, downTime + first.timeMillis, action, 1, properties, coordsOf(first),
        0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
    )
    try {
        batch.drop(1).forEach { sample -> event.addBatch(downTime + sample.timeMillis, coordsOf(sample), 0) }
        // 창 좌표로 옮긴다 — getX 는 창 기준, getRawX 는 화면 기준으로 남는다(실제 입력과 같은 모양)
        event.offsetLocation(-rootOnScreen[0].toFloat(), -rootOnScreen[1].toFloat())
        root.dispatchTouchEvent(event)
    } finally {
        event.recycle()
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
private fun HistoricalCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. 프레임 사이 중간점 — historical",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "터치 센서는 화면 주사율보다 자주 위치를 보고합니다. 시스템은 한 프레임 동안 쌓인 " +
                        "MOVE 샘플을 MotionEvent 하나로 묶어(batching) 보내고, Compose 는 마지막 샘플을 " +
                        "change.position 에, 앞의 샘플들을 change.historical 에 담습니다. " +
                        "position 만 쌓으면 프레임마다 점 하나 — 빠르게 그린 곡선이 다각형으로 꺾입니다.\n\n" +
                        "• 원 재생: 240Hz 샘플 37개를 60Hz 프레임 9개로 묶어 보내면 position 만은 9각형, " +
                        "historical 까지는 36각형이 됩니다. 정 n각형이 원에서 벗어나는 최대 거리는 r(1 − cos(π/n))\n" +
                        "• historical 은 오래된 순서이고 모두 position 보다 앞선 시각 → historical 다음에 position\n" +
                        "• historical 좌표도 position 과 같은 요소 기준(로컬)으로 변환돼 옵니다",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "시스템 입력 경로 실측 (API 30 에뮬레이터)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "반지름 250px 원을 240Hz 로 화면에 주입해(UiAutomation, 비동기 · debug 빌드) 이 캔버스가 받은 점을 셌습니다.\n" +
                        "• 150ms 원: 이벤트 9개에 historical 35개 — 주입한 샘플 36개가 하나도 빠지지 않았고, " +
                        "원에서 벗어난 거리는 position 만 33~34px · historical 포함 0.97~0.98px\n" +
                        "• 프레임이 늦은 첫 실행: 한 이벤트에 샘플 18개가 묶여 왔고, position 만 쌓은 선은 원을 가로질렀습니다(242px)\n" +
                        "• 600ms 원: 이벤트 36개 · historical 143개, position 만 1.5~2.1px · 포함 0.1px\n" +
                        "• position 은 실제 샘플이 아닐 수 있습니다 — 150ms 원은 9개 중 6~8개, 600ms 원은 36개 중 23~27개의 " +
                        "position 이 주입한 샘플 위치에 없는 점이었습니다(입력 리샘플링: 프레임 시각에 맞춰 샘플 사이를 보간). " +
                        "실제 샘플은 historical 쪽에 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                code = """
                awaitEachGesture {
                  val down = awaitFirstDown()
                  // 바깥 스크롤이 가져가지 않게
                  down.consume()
                  // 슬롭을 기다리지 않고 닿은 곳에서 시작
                  dispatch(StartStroke(down.position))
                  while (true) {
                    val change = awaitPointerEvent()
                      .changes
                      .firstOrNull { it.id == down.id }
                      ?: break
                    val samples = change.historical
                    if (change.positionChanged() ||
                      samples.isNotEmpty()
                    ) {
                      dispatch(
                        Drag(
                          // 묶여 온 샘플(오래된 순)
                          samples.map { it.position },
                          // 이 프레임의 마지막 샘플
                          change.position
                        )
                      )
                    }
                    change.consume()
                    // 손 뗀 위치까지 쌓고 끝
                    if (!change.pressed) break
                  }
                  dispatch(EndStroke)
                }
                """.trimIndent()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "detectDragGestures(onDragStart, onDrag, onDragEnd)로 그릴 때 빠지는 점",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "이 예제의 이전 구현을 API 30 에뮬레이터에서 adb input tap/swipe 로 잰 결과입니다.\n" +
                        "• 획 시작: onDragStart 의 위치는 손가락이 닿은 곳이 아니라 터치 슬롭(기본 8dp)을 넘은 이벤트의 위치 " +
                        "— 4번 모두 닿은 곳에서 35~53px 떨어진 곳부터 그려졌습니다\n" +
                        "• 탭: 슬롭을 넘지 않으면 onDragStart 가 불리지 않아 점이 남지 않았습니다(3번 모두 strokes 그대로)\n" +
                        "• 획 끝: onDragEnd 에는 위치가 없어 손 뗀 위치가 쌓이지 않았습니다 — 14~120px " +
                        "(adb swipe 는 이동 이벤트가 300ms 에 5~14개로 듬성해 크게 나온 값)\n" +
                        "→ 그리기 캔버스처럼 닿는 순간부터 입력을 가져야 하는 곳은 awaitEachGesture 로 직접 받습니다. " +
                        "같은 swipe 에서 지금 구현은 시작 · 끝이 닿은 곳 · 뗀 곳과 같았고, 탭은 점 하나로 남았습니다.",
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp
            )
        }
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
                text = "3. MVI 단방향 상태 흐름",
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
                    data class Drag(
                        val historical: List<Offset>,
                        val position: Offset
                    ) : DrawIntent
                    data object EndStroke : DrawIntent
                    data object Undo : DrawIntent
                    data object Redo : DrawIntent
                    data object Clear : DrawIntent
                    ...
                }

                fun reduce(state: DrawBoxState, intent: DrawIntent) = when (intent) {
                    is StartStroke -> state.copy(
                        liveStroke = DrawStroke(startStroke(intent.at), state.color, state.width)
                    )
                    is Drag -> state.copy(
                        liveStroke = state.liveStroke?.let {
                            it.copy(input = it.input.append(
                                intent.historical, intent.position
                            ))
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
                text = "4. 제스처 → Path 렌더링 & 내보내기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• awaitEachGesture: awaitFirstDown 에서 스트로크를 시작하고, 이벤트마다 " +
                        "historical + position 을 점으로 누적, 손 뗀 이벤트에서 확정합니다(2번 카드).\n" +
                        "• drawPath + Stroke(cap=Round, join=Round): 점들을 lineTo로 이어 부드러운 " +
                        "선으로 그립니다. 더 매끄럽게 하려면 인접 점의 중점으로 quadraticBezierTo를 쓸 수 있습니다.\n" +
                        "• 점이 1개뿐이면(탭) 작은 원으로 점을 찍어 표현합니다.\n" +
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
                    val points = s.input.points
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size)
                            lineTo(points[i].x, points[i].y)
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
private fun DrawScope.drawStroke(stroke: DrawStroke, showFrameLine: Boolean) {
    val points = stroke.input.points
    if (points.isEmpty()) return

    // 점이 하나뿐이면(탭) 점을 원으로 표현
    if (points.size == 1) {
        drawCircle(
            color = stroke.color,
            radius = stroke.width / 2f,
            center = points.first()
        )
        return
    }

    drawPath(
        path = polylinePath(points),
        color = stroke.color,
        style = Stroke(
            width = stroke.width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 비교선 — 같은 획을 이벤트마다 position 만 쌓았을 때의 선과 그 점
    val framePoints = stroke.input.framePoints
    if (showFrameLine && framePoints.size > 1) {
        drawPath(
            path = polylinePath(framePoints),
            color = FrameLineColor,
            style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round)
        )
        val dotRadius = 2.5.dp.toPx()
        framePoints.forEach { drawCircle(color = FrameLineColor, radius = dotRadius, center = it) }
    }
}

private fun polylinePath(points: List<Offset>) = Path().apply {
    moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        lineTo(points[i].x, points[i].y)
    }
}
