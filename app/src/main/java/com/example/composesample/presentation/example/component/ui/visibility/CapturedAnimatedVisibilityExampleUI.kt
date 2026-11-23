package com.example.composesample.presentation.example.component.ui.visibility

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.CapturedAnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

private val Indigo = Color(0xFF283593)
private val Teal = Color(0xFF00695C)
private val Plum = Color(0xFF6A1B9A)
private val Slate = Color(0xFF37474F)
private val Muted = Color(0xFF757575)
private val Body = Color(0xFF424242)
private val Matched = Color(0xFF2E7D32)
private val Mismatch = Color(0xFFC62828)

@Composable
fun CapturedAnimatedVisibilityExampleUI(onBackEvent: () -> Unit) {
    // 카드 상태 — 카드가 스크롤로 빠졌다 돌아와도 결과가 남도록 화면 루트에 둔다
    val closeLab = remember { CloseLabState() }
    val liveLab = remember { LiveLabState() }
    val benchLab = remember { ExitBenchState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4FA))
    ) {
        MainHeader(title = "CapturedAnimatedVisibility", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // 3번 카드가 재는 동안의 터치(스크롤 · 물결)는 그 자체가 프레임이라 결과를 무효로 표시한다 — 이벤트는 소비하지 않는다
                .pointerInput(benchLab) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial)
                            benchLab.onUserTouch()
                        }
                    }
                },
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { OverviewCard() }
            item { CloseWithNullCard(closeLab) }
            item { LivenessCard(liveLab) }
            item { ExitFrameTimeCard(benchLab) }
            item { MigrationCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Indigo)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("상태를 바로 지워도 나가는 애니메이션", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "AnimatedVisibility 는 나가는 애니메이션이 끝날 때까지 내용을 계속 컴포즈한다. 닫으면서 선택을 null 로 지우면 " +
                    "내용에 보여 줄 것이 없어 빈 칸이 나가거나, !! 로 받았다면 앱이 죽는다. 그래서 마지막 값을 직접 붙들어 왔다. " +
                    "Compose 1.13 의 CapturedAnimatedVisibility 는 visible 이 false 가 되는 순간 내용을 컴포지션에서 빼고, " +
                    "마지막으로 그린 그림(레이어)을 나가게 한다.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            CodeBlock(
                "@Composable\n" +
                    "fun TrackDetails(track: Track?) {\n" +
                    "  // 닫으면 track 이 바로 null\n" +
                    "  CapturedAnimatedVisibility(\n" +
                    "    visible = track != null,\n" +
                    "    enter = fadeIn() + expandVertically(),\n" +
                    "    exit = fadeOut(tween(800)) +\n" +
                    "      shrinkVertically(tween(800)),\n" +
                    "  ) {\n" +
                    "    if (track != null) TrackCard(track)\n" +
                    "  }\n" +
                    "}"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "원문(Compose November '26 블로그)의 예시. 내용 람다 안의 null 검사는 들어올 때를 위한 것이다 — 나갈 때는 내용이 불리지 않는다.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }
    }
}

// ==================== 1. 선택을 바로 null 로 ====================

@Composable
private fun CloseWithNullCard(lab: CloseLabState) {
    val scope = rememberCoroutineScope()
    LabCard(
        title = "1. 상세 패널 닫기 — 선택을 바로 null 로",
        color = Indigo,
        description = "곡을 누르면 세 패널이 같은 선택을 연다. 닫기는 선택을 null 로 지우고 visible 을 내린다. 퇴장은 원문 예시처럼 " +
            "fadeOut + shrinkVertically. 결과는 패널 높이(onSizeChanged)와 내용 람다의 DisposableEffect 로 잰다."
    ) {
        ControlRow("곡") {
            sampleTracks.forEach { track ->
                LabChip(track.title, lab.visible && lab.selected?.id == track.id) { lab.open(track) }
            }
        }
        ControlRow("닫는 순서") {
            CloseOrder.entries.forEach { order ->
                LabChip(order.label, lab.order == order) { if (!lab.busy) lab.order = order }
            }
        }
        ControlRow("퇴장 시간") {
            exitMillisChoices.forEach { millis ->
                LabChip("${millis}ms", lab.exitMillis == millis) { if (!lab.busy) lab.exitMillis = millis }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(onClick = { scope.launch { lab.close() } }, enabled = lab.visible && !lab.busy) {
            Text("닫기 (선택 = null)", fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ExitVariant.entries.forEach { variant ->
            Text(variant.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Body)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                ExitPanel(
                    variant = variant,
                    visible = lab.visible,
                    track = lab.selected,
                    exitMillis = lab.exitMillis.toInt(),
                    trace = lab.traces.getValue(variant)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        CloseResults(lab)
    }
}

/**
 * 1번 카드의 패널 하나 — 계측 테스트(CapturedAnimatedVisibilityTest)가 같은 것을 쓴다.
 * 바깥 Box 는 자식이 없으면 높이 0 이라, 퇴장 끝에 내용이 컴포지션에서 빠지는 순간도 높이로 보인다.
 */
@Composable
internal fun ExitPanel(
    variant: ExitVariant,
    visible: Boolean,
    track: Track?,
    exitMillis: Int,
    trace: ExitTrace?,
    modifier: Modifier = Modifier
) {
    val enter = fadeIn(tween(ENTER_MILLIS)) + expandVertically(tween(ENTER_MILLIS))
    val exit = fadeOut(tween(exitMillis)) + shrinkVertically(tween(exitMillis))
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exitPanel-${variant.name}")
            .onSizeChanged { trace?.onHeight(it.height, System.nanoTime()) }
    ) {
        when (variant) {
            ExitVariant.AnimatedNull -> AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
                ContentLifeProbe(trace)
                // 나가는 동안에도 이 람다가 불린다 — track 은 이미 null
                if (track != null) TrackCard(track)
            }

            ExitVariant.AnimatedHold -> {
                // 흔히 쓰던 우회 — 선택이 null 이 되기 전 값을 붙들어 나가는 동안 그것을 그린다
                val last = remember { LastValue<Track>() }
                if (track != null) last.value = track
                AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
                    ContentLifeProbe(trace)
                    last.value?.let { TrackCard(it) }
                }
            }

            ExitVariant.Captured -> CapturedAnimatedVisibility(visible = visible, enter = enter, exit = exit) {
                ContentLifeProbe(trace)
                // 나갈 때는 이 람다 대신 빈 람다가 들어간다 — null 검사는 들어올 때를 위한 것
                if (track != null) TrackCard(track)
            }
        }
    }
}

/** 내용 람다가 컴포지션에서 빠지는 순간을 기록한다 — 레이아웃 노드를 만들지 않아 내용 모양에 영향이 없다 */
@Composable
private fun ContentLifeProbe(trace: ExitTrace?) {
    DisposableEffect(trace) {
        onDispose { trace?.onContentDisposed(System.nanoTime()) }
    }
}

@Composable
private fun TrackCard(track: Track) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEF0F8), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(track.coverArgb), RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Text(track.artist, fontSize = 12.sp, color = Muted)
            Text("길이 ${track.length} · 재생 대기", fontSize = 11.sp, color = Muted)
        }
    }
}

@Composable
private fun CloseResults(lab: CloseLabState) {
    val results = lab.results
    if (results.isEmpty()) {
        Text("닫기를 누르면 퇴장이 끝난 뒤 세 패널의 결과를 적는다.", fontSize = 12.sp, color = Muted)
        return
    }
    val first = results.first()
    Text(
        text = "${first.order.label} · 퇴장 ${first.exitMillis}ms",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Indigo
    )
    Spacer(modifier = Modifier.height(4.dp))
    TableRow(listOf("방법", "닫은 뒤 보임", "줄어든 프레임", "내용 해제"), header = true)
    results.forEach { result ->
        TableRow(
            cells = listOf(
                result.variant.label.take(1),
                spanCell(result.shownMillis, result.shownSpan),
                "${result.shrinkFrames}",
                spanCell(result.releasedMillis, result.releasedSpan)
            ),
            color = if (result.matches) Body else Mismatch
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    val expected = ExitVariant.entries.joinToString(" / ") { variant ->
        "${variant.label.take(1)} ${expectedShown(variant, first.order).label} · ${expectedReleased(variant).label}"
    }
    Text("예상(보임 · 해제): $expected", fontSize = 11.sp, color = Muted, lineHeight = 16.sp)
    val matched = results.count { it.matches }
    Text(
        text = "${results.size}줄 가운데 예상과 같음 $matched",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (matched == results.size) Matched else Mismatch
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = if (first.order == CloseOrder.Together) {
            "① 은 닫는 그 프레임에 빈 내용이 측정돼 높이가 0 — 애니메이션할 것이 없는데도 내용 람다는 퇴장 시간 내내 컴포지션에 남는다. " +
                "② 는 붙든 값으로 끝까지 나간다. ③ 은 선택을 지웠는데도 끝까지 나가고, 내용은 첫 프레임에 이미 컴포지션에서 빠졌다."
        } else {
            "③ 도 빈 칸이 된다. 그림은 visible 이 false 가 되기 직전 프레임에 그려진 것 — 그 사이 프레임에 선택이 비어 내용이 높이 0 으로 " +
                "측정 · 배치됐고, 그 크기를 들고 나간다. 마지막 값을 붙드는 ② 만 끝까지 나간다. 선택과 visible 은 같은 프레임에 바꾼다."
        },
        fontSize = 12.sp,
        color = Body,
        lineHeight = 18.sp
    )
}

private fun spanCell(millis: Long?, span: Span?): String =
    if (millis == null) "기록 없음" else "${millis}ms ${span?.label ?: "(애매)"}"

// ==================== 2. 나가는 동안 · 다시 열 때 ====================

@Composable
private fun LivenessCard(lab: LiveLabState) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val refreshHz = remember(view) { view.display?.refreshRate ?: 60f }
    LabCard(
        title = "2. 나가는 동안 · 다시 열 때 — 살아 있는 내용과 그림",
        color = Teal,
        description = "두 패널의 내용에 무한 스피너 · '눌러 보기' 버튼 · remember 한 내용 번호가 있다. 퇴장은 ${LIVE_EXIT_MILLIS}ms 선형 " +
            "페이드로 길게 잡았다 — 나가는 동안 버튼을 눌러 보고, 스피너가 도는지 본다. 내용 번호는 카드가 스크롤로 빠졌다 들어와도 오른다(새 컴포지션)."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveVariant.entries.forEach { variant ->
                Column(modifier = Modifier.weight(1f)) {
                    Text(variant.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Body)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = LIVE_PANEL_MIN_HEIGHT)
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        LivePanel(
                            variant = variant,
                            visible = lab.visible,
                            trace = lab.traces.getValue(variant),
                            exitMillis = LIVE_EXIT_MILLIS.toInt()
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedButton(onClick = { lab.open() }, enabled = !lab.visible && !lab.busy) { Text("열기", fontSize = 12.sp) }
            LiveAction.entries.forEach { action ->
                Button(onClick = { scope.launch { lab.run(action, refreshHz) } }, enabled = lab.visible && !lab.busy) {
                    Text(action.label, fontSize = 12.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LiveResults(lab)
    }
}

private val LIVE_PANEL_MIN_HEIGHT = 132.dp

/**
 * 2번 카드의 패널 하나 — 계측 테스트는 showSpinner = false 로 쓴다(무한 애니메이션은 테스트가 쉬지 못하게 한다).
 * 퇴장은 선형 페이드만 — 기본 이징(FastOutSlowIn)은 1초 만에 80% 넘게 진행해 나가는 동안의 스피너 · 버튼을 볼 틈이 없고,
 * 접히면 버튼이 움직여 누르기 어렵다
 */
@Composable
internal fun LivePanel(
    variant: LiveVariant,
    visible: Boolean,
    trace: LiveTrace,
    exitMillis: Int,
    modifier: Modifier = Modifier,
    showSpinner: Boolean = true
) {
    val enter = fadeIn(tween(ENTER_MILLIS)) + expandVertically(tween(ENTER_MILLIS))
    val exit = fadeOut(tween(exitMillis, easing = LinearEasing))
    Box(modifier = modifier.fillMaxWidth().testTag("livePanel-${variant.name}")) {
        when (variant) {
            LiveVariant.Animated -> AnimatedVisibility(visible = visible, enter = enter, exit = exit) {
                LiveContent(trace, showSpinner)
            }

            LiveVariant.Captured -> CapturedAnimatedVisibility(visible = visible, enter = enter, exit = exit) {
                LiveContent(trace, showSpinner)
            }
        }
    }
}

@Composable
private fun LiveContent(trace: LiveTrace, showSpinner: Boolean) {
    // 내용이 새로 컴포즈될 때만 번호가 바뀐다 — 다시 열었을 때 같은 번호면 그동안 컴포지션에 남아 있었다는 뜻
    val serial = remember { trace.newSerial() }
    var taps by remember { mutableIntStateOf(0) }
    SideEffect { trace.currentSerial = serial }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0F2F1), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSpinner) {
            Spinner(onDraw = trace::onDraw)
        } else {
            Spacer(modifier = Modifier.size(SPINNER_SIZE))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("내용 #$serial", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Teal)
        OutlinedButton(onClick = { taps++ }) { Text("눌러 보기 $taps", fontSize = 11.sp) }
    }
}

private val SPINNER_SIZE = 32.dp

/** 각도를 그리기 단계에서 읽는 스피너 — 그려지는 동안은 매 프레임 그리기 람다가 불린다 */
@Composable
private fun Spinner(onDraw: () -> Unit = {}, color: Color = Teal) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle = transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900, easing = LinearEasing)),
        label = "angle"
    )
    Canvas(modifier = Modifier.size(SPINNER_SIZE)) {
        onDraw()
        val stroke = 4.dp.toPx()
        rotate(angle.value) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = Size(size.width - stroke, size.height - stroke),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun LiveResults(lab: LiveLabState) {
    val results = lab.results
    if (results.isEmpty()) {
        Text("닫기 · 닫고 다시 열기를 누르면 나가는 동안의 스피너 그리기와 다시 연 뒤의 내용 번호를 적는다.", fontSize = 12.sp, color = Muted)
        return
    }
    val first = results.first()
    Text("${first.action.label} · 창 ${first.windowMillis}ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Teal)
    Spacer(modifier = Modifier.height(4.dp))
    TableRow(listOf("방법", "나가는 동안 스피너 그리기", "다시 연 뒤 내용"), header = true)
    results.forEach { result ->
        val reopened = when (result.kept) {
            null -> "-"
            true -> "#${result.serialBefore} → #${result.serialAfter} 이어짐"
            false -> "#${result.serialBefore} → #${result.serialAfter} 처음부터"
        }
        TableRow(
            cells = listOf(result.variant.label, "${result.exitDraws}회 (${result.drawRate?.label ?: "애매"})", reopened),
            color = if (result.matches) Body else Mismatch
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    val matched = results.count { it.matches }
    Text(
        text = "예상: AnimatedVisibility 계속 · 이어짐 / Captured 0 · 처음부터 — ${results.size}줄 가운데 같음 $matched",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (matched == results.size) Matched else Mismatch,
        lineHeight = 17.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    FactRow(
        "그림은 멈춰 있다",
        "Captured 는 나가는 동안 내용 컴포지션이 없다 — 무한 애니메이션은 시계째 사라지고(그리기 0), 버튼은 눌리지 않는다(터치는 아래로 간다). " +
            "로딩 표시처럼 나가는 동안에도 움직여야 하는 내용이면 AnimatedVisibility 를 쓴다."
    )
    FactRow(
        "다시 열면 처음부터",
        "나가는 도중 visible 을 다시 true 로 하면 AnimatedVisibility 는 같은 내용을 되돌려 remember · 입력 · 스크롤이 이어진다. " +
            "Captured 는 이미 버린 내용을 새로 컴포즈한다 — 내용 안의 remember 는 처음 값이다."
    )
}

// ==================== 3. 원문 주장: 나가는 동안 프레임 시간 ====================

@Composable
private fun ExitFrameTimeCard(lab: ExitBenchState) {
    val view = LocalView.current
    val window = remember(view) { view.context.findActivityWindow() }
    val recorder = remember(window) { if (frameMetricsSupported && window != null) ExitFrameRecorder(window) else null }
    DisposableEffect(recorder) { onDispose { recorder?.close() } }
    LaunchedEffect(lab.runToken) {
        if (lab.runToken > 0 && recorder != null) lab.execute(recorder)
    }
    LabCard(
        title = "3. 원문 주장 — 나가는 동안 프레임 시간 7~8% 빠름",
        color = Plum,
        description = "원문: '상태를 바로 놓아 주니 퇴장도 싸다 — 벤치마크에서 나가는 동안의 프레임 시간이 AnimatedVisibility 보다 7~8% 빠르다.' " +
            "같은 카드를 두 방법으로 ${BENCH_ROUNDS}번씩 번갈아 닫고(퇴장 ${BENCH_EXIT_MILLIS}ms), 그동안 앱 창의 FrameMetrics 를 모은다."
    ) {
        ControlRow("내용") {
            BenchContent.entries.forEach { content ->
                LabChip(content.label, lab.content == content) { if (!lab.running) lab.content = content }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(onClick = { lab.request() }, enabled = recorder != null && !lab.running) {
            Text("두 방법 번갈아 재기", fontSize = 13.sp)
        }
        Text(
            text = if (recorder == null) "API 26 미만 — 겨냥한 VSync 시각이 없어 재지 않는다" else lab.status,
            fontSize = 12.sp,
            color = if (lab.running) Plum else Muted,
            lineHeight = 17.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        BenchPanel(lab)
        Spacer(modifier = Modifier.height(8.dp))
        BenchContent.entries.forEach { content -> lab.reports[content]?.let { BenchResult(it) } }
        FactRow(
            "무엇을 재나",
            "프레임마다 FrameMetrics 의 전체(TOTAL_DURATION)와 메인 스레드(입력 + 애니메이션 + 측정 · 배치 + 그리기 — Compose 의 리컴포지션은 " +
                "애니메이션 단계에서 돈다)를 받는다. 닫은 순간부터 ${BENCH_EXIT_MILLIS}ms 안을 겨냥한 프레임만 쓰고, 첫 프레임은 따로 센다 — " +
                "Captured 는 그 프레임에 내용 컴포지션을 버린다."
        )
        FactRow(
            "이 화면의 실측",
            "Pixel 4 API 30 에뮬레이터(소프트웨어 GPU) release 2회: 정적 카드는 전체 40~41 → 19~21ms(49~54% 빠름)인데 메인 스레드는 " +
                "0.66 → 0.72~0.75ms(10~13% 느림), 스피너 카드는 전체 29~47% · 메인 11~14% 빠름. 전체의 차이는 거의 모두 메인 스레드 밖" +
                "(렌더 스레드 · GPU)에서 났다 — 원문과 방향은 같고 크기는 다르다(원문은 기기 · 내용을 밝히지 않았다)."
        )
        FactRow(
            "숫자 읽기",
            "시간은 기기 · 빌드마다 다르다. 같은 에뮬레이터의 debug 는 정적 카드 전체가 +34% · −25% 로 실행마다 뒤집혔다 — 두세 번 재서 " +
                "방향이 같은지 보고, 값은 release 로 읽는다."
        )
    }
}

private val BENCH_PANEL_HEIGHT = 196.dp

/** 측정 패널 — 바깥 높이를 고정해 퇴장 동안 목록의 다른 항목이 움직이지 않게 한다(두 방법이 같은 바깥 레이아웃 비용) */
@Composable
private fun BenchPanel(lab: ExitBenchState) {
    val enter: EnterTransition = fadeIn(tween(ENTER_MILLIS)) + expandVertically(tween(ENTER_MILLIS))
    val exit: ExitTransition = fadeOut(tween(BENCH_EXIT_MILLIS.toInt())) + shrinkVertically(tween(BENCH_EXIT_MILLIS.toInt()))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BENCH_PANEL_HEIGHT)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        when (lab.variant) {
            BenchVariant.Animated -> AnimatedVisibility(visible = lab.visible, enter = enter, exit = exit) {
                BenchCard(lab.content)
            }

            BenchVariant.Captured -> CapturedAnimatedVisibility(visible = lab.visible, enter = enter, exit = exit) {
                BenchCard(lab.content)
            }
        }
    }
}

private val benchWords = ("새벽 세 시 신호 바다 쪽 창문 느린 계단 낮은 궤도 종이 달 항구 불빛 첫차 골목 우산 " +
    "라디오 잡음 겨울 버스 정류장 젖은 운동화 편의점 형광등 마지막 노래 되감기 테이프 먼지 창틀 고양이").split(" ")

@Composable
private fun BenchCard(content: BenchContent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3E5F5), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Plum, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("가사 미리 보기", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Text("단어 ${benchWords.size}개 · 칸마다 Text 하나", fontSize = 11.sp, color = Muted)
            }
            if (content == BenchContent.Spinner) Spinner(color = Plum)
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            benchWords.forEach { word ->
                Text(
                    text = word,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = Body
                )
            }
        }
    }
}

@Composable
private fun BenchResult(report: BenchReport) {
    Text(
        text = report.content.label + if (report.touched) " — 재는 중 화면을 건드려 무효" else "",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (report.touched) Mismatch else Plum
    )
    Spacer(modifier = Modifier.height(4.dp))
    TableRow(listOf("방법", "프레임", "전체", "메인", "첫 프레임"), header = true)
    BenchVariant.entries.forEach { variant ->
        val frames = report.frames[variant] ?: return@forEach
        TableRow(
            listOf(
                if (variant == BenchVariant.Animated) "Animated…" else "Captured…",
                "${frames.frames}",
                formatMillis(frames.medianTotalNanos),
                formatMillis(frames.medianUiNanos),
                formatMillis(frames.firstFrameMedianTotalNanos)
            )
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "ms 는 프레임 중앙값(첫 프레임은 따로). Captured 가 전체 ${formatPercent(report.totalFaster)}% · 메인 스레드 " +
            "${formatPercent(report.uiFaster)}% 빠름(음수면 느림) — 원문 7~8%",
        fontSize = 12.sp,
        color = Body,
        lineHeight = 17.sp
    )
    Spacer(modifier = Modifier.height(10.dp))
}

// ==================== 4 · 정리 ====================

@Composable
private fun MigrationCard() {
    LabCard(
        title = "4. AnimatedVisibility 를 바꿔 쓸 때",
        color = Slate,
        description = "animation 1.13.0 소스(CapturedAnimatedVisibility.kt)로 확인한 차이. opt-in 은 필요 없다."
    ) {
        FactRow(
            "Column · Row 안의 기본 퇴장이 바뀐다",
            "Column 안의 AnimatedVisibility 는 ColumnScope 판이라 기본값이 fadeIn + expandVertically / fadeOut + shrinkVertically 다. " +
                "CapturedAnimatedVisibility 에는 스코프 판이 없어 어디서나 fadeIn + expandIn / shrinkOut + fadeOut — 오른쪽 아래(BottomEnd) " +
                "쪽으로 두 방향 다 줄어든다. 이름만 바꾸면 접히는 모양이 달라지니 enter · exit 를 적는다."
        )
        FactRow(
            "내용 람다에 스코프가 없다",
            "내용은 @Composable () -> Unit 이다 — AnimatedVisibilityScope 가 없어 자식의 Modifier.animateEnterExit 나 transition 을 쓸 수 없다" +
                "(컴파일 오류). 자식마다 따로 들어오고 나가야 하면 AnimatedVisibility 를 쓴다."
        )
        FactRow(
            "그림은 '바로 전 프레임'",
            "나갈 때 그리는 것은 자식을 배치하며 기록해 둔 GraphicsLayer 다. visible 이 false 가 되는 컴포지션에서 내용을 빈 람다로 바꾸므로 " +
                "그 프레임의 내용은 그려지지 않는다 — 선택과 visible 을 같은 프레임에 바꾸면 지우기 전 그림이 남는다(1번 카드의 늦은 visible 대조)."
        )
        FactRow(
            "나가는 동안 크기는 마지막 측정값",
            "퇴장 중에는 자식이 없어 마지막으로 잰 크기를 그대로 보고한다. LookaheadScope(SharedTransitionLayout 등) 안에서는 " +
                "lookahead 크기를 0 으로 보고해 이웃이 곧바로 최종 자리를 향한다."
        )
        FactRow(
            "상태 관찰",
            "MutableTransitionState 를 받는 오버로드가 있다 — currentState · isIdle 로 퇴장이 끝났는지 본다. 이때도 내용은 퇴장 시작에 이미 빠져 있다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "Column {\n" +
                "  // 이름만 바꾸면 기본 퇴장이 shrinkOut(BottomEnd)\n" +
                "  CapturedAnimatedVisibility(\n" +
                "    visible = open,\n" +
                "    enter = fadeIn() + expandVertically(),\n" +
                "    exit = fadeOut() + shrinkVertically(),\n" +
                "  ) { Details(item) }\n" +
                "}"
        )
    }
}

@Composable
private fun SummaryCard() {
    LabCard(title = "정리", color = Indigo, description = "닫으면서 데이터를 놓아야 하면 Captured, 나가는 동안에도 살아 있어야 하면 AnimatedVisibility.") {
        listOf(
            "AnimatedVisibility 는 나가는 동안 내용을 컴포즈한다 — 선택을 바로 지우면 빈 칸 · !! 크래시, 그래서 마지막 값을 붙들어 왔다",
            "CapturedAnimatedVisibility 는 visible = false 순간 내용을 컴포지션에서 빼고 기록해 둔 그림을 나가게 한다",
            "그림은 바로 전 프레임 — 선택과 visible 은 같은 프레임에 바꾼다",
            "나가는 동안 그림은 멈춰 있고 터치를 받지 않는다. 다시 열면 내용은 처음부터",
            "스코프 판(ColumnScope · RowScope)과 AnimatedVisibilityScope 가 없다 — enter · exit 를 적고, 자식별 animateEnterExit 가 필요하면 바꾸지 않는다",
            "원문의 '7~8% 빠름'은 3번 카드로 이 기기에서 다시 잰다"
        ).forEach { line ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("•", fontSize = 12.sp, color = Indigo)
                Spacer(modifier = Modifier.width(6.dp))
                Text(line, fontSize = 12.sp, color = Body, lineHeight = 18.sp)
            }
        }
    }
}

// ==================== 공통 ====================

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun Context.findActivityWindow(): Window? = findActivity()?.window

@Composable
private fun LabCard(
    title: String,
    color: Color,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Muted, lineHeight = 17.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ControlRow(label: String, chips: @Composable () -> Unit) {
    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Body)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { chips() }
}

@Composable
private fun LabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFC5CAE9))
    )
}

@Composable
private fun TableRow(cells: List<String>, header: Boolean = false, color: Color = Color(0xFF212121)) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                modifier = Modifier.weight(if (index == 0) 1.6f else 1f),
                fontSize = 11.sp,
                fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
                color = if (header) Indigo else color,
                lineHeight = 15.sp
            )
        }
    }
    HorizontalDivider(color = Color(0xFFE0E0E0))
}

@Composable
private fun FactRow(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Text(text = body, fontSize = 12.sp, color = Body, lineHeight = 18.sp)
    }
}

@Composable
private fun CodeBlock(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263238), RoundedCornerShape(8.dp))
            .padding(10.dp),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFECEFF1),
        lineHeight = 16.sp
    )
}
