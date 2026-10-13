package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Ribbon Modifier — 컴포저블을 감아 도는 리본을 경로 하나로 만들고, 접선 부호로 앞뒤 구간을 나눠 내용 앞뒤에 번갈아 그린다.
 * - 경로 계산·Modifier 는 RibbonPath.kt(순수 계산은 단위 테스트 RibbonPathTest)
 * - 참고 자료(URL/핵심 개념)는 exampleGuide.kt 의 "RibbonModifierExampleUI" 섹션 참고.
 */
@Composable
fun RibbonModifierExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Ribbon Modifier",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { RibbonIntroCard() }
            item { SpiralPathCard() }
            item { SlopeSegmentsCard() }
            item { DepthSplitCard() }
            item { ChainOrderCard() }
            item { PhaseTrickCard() }
            item { CacheInvalidationCard() }
            item { RibbonGuideCard() }
        }
    }
}

private val RibbonBrush = Brush.horizontalGradient(
    listOf(Color(0xFFFF8F00), Color(0xFFE91E63), Color(0xFF7C4DFF))
)
private val BackColor = Color(0xFF90A4AE)
private val FrontColor = Color(0xFFE91E63)
private val PillColor = Color(0xFF263238)
private val SlotColor = Color(0xFFECEFF1)

/** 소수 한 자리 백분율 — String.format 의 기본 Locale 경고를 피한다 */
private fun percent(fraction: Float): String = "${(fraction * 1000).roundToInt() / 10f}%"

@Composable
private fun RibbonCard(
    title: String,
    description: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun RibbonStat(text: String, color: Color = Color(0xFF37474F)) {
    Text(text = text, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color)
}

@Composable
private fun <T> RibbonChoiceRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF666666))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { (value, text) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(text = text, fontSize = 12.sp) }
            )
        }
    }
}

/**
 * 리본이 감을 대상. ribbon 을 background 보다 앞(바깥)에 둬야 ribbon 의 drawContent() 에 배경까지 들어가
 * 뒤 구간이 배경 뒤로 숨는다 — backgroundFirst 로 순서를 뒤집어 비교한다.
 */
@Composable
private fun RibbonPill(
    text: String,
    modifier: Modifier = Modifier,
    ribbon: Modifier,
    height: Dp = 44.dp,
    fontSize: TextUnit = 14.sp,
    backgroundFirst: Boolean = false
) {
    val background = Modifier.background(PillColor, RoundedCornerShape(50))
    Box(
        modifier = modifier
            .height(height)
            .then(if (backgroundFirst) background.then(ribbon) else ribbon.then(background))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 소개
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun RibbonIntroCard() {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1800, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "경로 하나를 두 번 그린다", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "원을 버튼의 왼쪽 끝에서 오른쪽 끝으로 끌며 둘레에 점을 찍으면 나선 경로가 된다. 이 경로를 접선이 위로 향하는 반 바퀴와 " +
                    "아래로 향하는 반 바퀴로 나눠 한쪽은 drawContent() 앞에, 다른 쪽은 뒤에 그리면 리본이 버튼을 감은 것처럼 보인다. " +
                    "진행률은 대시 효과 하나를 더 엮어(chainPathEffect) 앞에서부터 잘라 낸다.",
                fontSize = 12.sp,
                color = Color(0xFF333333)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                RibbonPill(
                    text = "NEW · 리본 감기",
                    ribbon = Modifier.ribbon(brush = RibbonBrush, stroke = 5.dp, progress = { progress.value }),
                    modifier = Modifier.width(220.dp)
                )
            }
            TextButton(
                onClick = {
                    scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(1f, tween(durationMillis = 1800, easing = FastOutSlowInEasing))
                    }
                }
            ) {
                Text(text = "다시 감기", fontSize = 12.sp)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 1. 경로 만들기
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun SpiralPathCard() {
    var t by remember { mutableFloatStateOf(0.35f) }
    var turns by remember { mutableFloatStateOf(3f) }

    RibbonCard(
        title = "1. 원을 끌며 둘레에 점 찍기",
        description = "원의 중심을 내용의 왼쪽 끝 가운데에서 오른쪽 끝 가운데로 옮기면서 각도는 −90°(바닥)부터 바퀴 수만큼 돌린다. " +
            "극좌표 (각도 θ, 반지름 r) → (r·cos(−θ), r·sin(−θ)) 에 중심을 더한 점 1001개를 lineTo 로 잇는다. 반지름은 내용 높이/2 + 선 굵기.",
        accent = Color(0xFF1E88E5)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .drawWithCache {
                    val margin = 48.dp.toPx()
                    val slotHeight = 40.dp.toPx()
                    val strokePx = 4.dp.toPx()
                    val top = (size.height - slotHeight) / 2f
                    val start = Offset(margin, size.height / 2f)
                    val end = Offset(size.width - margin, size.height / 2f)
                    val radius = slotHeight / 2f + strokePx
                    val points = ribbonPoints(start, end, radius, turns, startAngle = -90f)
                    val fullPath = pathOf(points)
                    onDrawBehind {
                        drawRoundRect(
                            color = SlotColor,
                            topLeft = Offset(margin, top),
                            size = Size(end.x - margin, slotHeight),
                            cornerRadius = CornerRadius(slotHeight / 2f)
                        )
                        drawPath(fullPath, Color(0x33E91E63), style = Stroke(width = 1.5.dp.toPx()))
                        val upTo = (t * points.lastIndex).roundToInt()
                        if (upTo > 0) {
                            drawPath(
                                pathOf(points.subList(0, upTo + 1)),
                                FrontColor,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                        val center = lerp(start, end, t)
                        val accent = Color(0xFF1E88E5)
                        drawCircle(accent, radius = radius, center = center, style = Stroke(width = 1.dp.toPx()))
                        drawLine(accent, center, points[upTo], strokeWidth = 1.dp.toPx())
                        drawCircle(accent, radius = 3.dp.toPx(), center = center)
                        drawCircle(FrontColor, radius = 4.dp.toPx(), center = points[upTo])
                    }
                }
        )
        Slider(value = t, onValueChange = { t = it })
        RibbonChoiceRow(
            label = "바퀴 수",
            options = listOf(1f to "1바퀴", 3f to "3바퀴", 7f to "7바퀴 (원문)"),
            selected = turns,
            onSelect = { turns = it }
        )
        Spacer(modifier = Modifier.height(4.dp))
        RibbonStat("t = ${(t * 100).roundToInt() / 100f} · 각도 ${(-90f + 360f * turns * t).roundToInt()}° · 원 중심 = 내용 폭의 ${(t * 100).roundToInt()}% 지점")
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 2. 접선 부호로 구간 나누기
// ──────────────────────────────────────────────────────────────────────────

/** 구간 색칠용 — getSegment 로 짝수(뒤)·홀수(앞) 구간을 각각 한 Path 에 모으고 경계 점을 구한다 */
private class SegmentView(val origin: Offset, val slotSize: Size, val ribbon: RibbonGeometry) {
    val back = Path()
    val front = Path()
    val boundaries = ArrayList<Offset>()

    init {
        var start = 0f
        ribbon.segments.forEachIndexed { index, length ->
            val end = start + length
            ribbon.measure.getSegment(start, end, if (index % 2 == 0) back else front, startWithMoveTo = true)
            if (index < ribbon.segments.lastIndex) boundaries += ribbon.measure.getPosition(end)
            start = end
        }
    }
}

@Composable
private fun SlopeSegmentsCard() {
    var resolution by remember { mutableIntStateOf(500) }
    var turns by remember { mutableFloatStateOf(7f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val view = remember(canvasSize, turns, resolution, density) {
        if (canvasSize == IntSize.Zero) {
            null
        } else with(density) {
            val margin = 48.dp.toPx()
            val slotHeight = 40.dp.toPx()
            val slotSize = Size(canvasSize.width - margin * 2f, slotHeight)
            SegmentView(
                origin = Offset(margin, (canvasSize.height - slotHeight) / 2f),
                slotSize = slotSize,
                ribbon = buildRibbon(slotSize, 4.dp.toPx(), turns, resolution = resolution)
            )
        }
    }
    var effectResult by remember(turns, resolution) { mutableStateOf<String?>(null) }

    RibbonCard(
        title = "2. 접선 부호로 앞뒤 나누기",
        description = "PathMeasure 로 길이를 등분해 getTangent(거리).y 의 부호가 바뀌는 곳마다 구간 길이를 모은다(원문은 500 등분). " +
            "회색은 첫 구간처럼 접선이 위로 향하는 구간(내용 뒤), 분홍은 아래로 향하는 구간(내용 앞). 검은 점이 경계 — 원의 꼭대기·바닥에 와야 한다.",
        accent = Color(0xFF8E24AA)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .onSizeChanged { canvasSize = it }
                .drawWithCache {
                    val strokePx = 4.dp.toPx()
                    val dot = 3.dp.toPx()
                    onDrawBehind {
                        val current = view ?: return@onDrawBehind
                        translate(current.origin.x, current.origin.y) {
                            drawRoundRect(
                                color = SlotColor,
                                size = current.slotSize,
                                cornerRadius = CornerRadius(current.slotSize.height / 2f)
                            )
                            drawPath(current.back, BackColor, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                            drawPath(current.front, FrontColor, style = Stroke(width = strokePx, cap = StrokeCap.Round))
                            current.boundaries.forEach { drawCircle(Color.Black, radius = dot, center = it) }
                        }
                    }
                }
        )
        RibbonChoiceRow(
            label = "표본 수",
            options = listOf(40 to "40", 500 to "500 (원문)"),
            selected = resolution,
            onSelect = { resolution = it }
        )
        RibbonChoiceRow(
            label = "바퀴 수",
            options = listOf(0.4f to "0.4바퀴", 3f to "3바퀴", 7f to "7바퀴 (원문)"),
            selected = turns,
            onSelect = { turns = it }
        )
        Spacer(modifier = Modifier.height(4.dp))
        view?.ribbon?.let { ribbon ->
            val segments = ribbon.segments
            if (segments.size >= 2) {
                RibbonStat("구간 ${segments.size}개 · 평균 ${(ribbon.length / segments.size).roundToInt()}px · 최장 ${segments.max().roundToInt()} / 최단 ${segments.min().roundToInt()}px")
                RibbonStat("표본 간격 ${(ribbon.length / resolution).roundToInt()}px — 경계는 이 간격 단위로만 잡힌다")
            } else {
                RibbonStat("구간 1개 — 반 바퀴 미만이라 접선 부호가 한 번도 안 바뀐다", color = Color(0xFFC62828))
            }
            OutlinedButton(
                onClick = {
                    effectResult = try {
                        PathEffect.dashPathEffect(segments)
                        "만들어짐 — intervals ${segments.size}개"
                    } catch (e: RuntimeException) {
                        "${e.javaClass.simpleName} — intervals ${segments.size}개(2개 미만)"
                    }
                }
            ) {
                Text(text = "이 구간 배열로 dashPathEffect 만들기", fontSize = 12.sp)
            }
            effectResult?.let {
                RibbonStat(it, color = if (it.startsWith("만들어짐")) Color(0xFF2E7D32) else Color(0xFFC62828))
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 3. 내용 앞뒤로 나눠 그리기
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun DepthSplitCard() {
    var split by remember { mutableStateOf(true) }
    var clearContent by remember { mutableStateOf(true) }
    var backgroundFirst by remember { mutableStateOf(false) }
    val progress = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    RibbonCard(
        title = "3. 내용 앞뒤로 나눠 그리기",
        description = "drawWithCache { onDrawWithContent { 뒤 구간 · drawContent() · 앞 구간 } }. 뒤 구간은 dashPathEffect(구간 배열), " +
            "앞 구간은 같은 배열에 phase = 첫 구간 길이를 줘 패턴을 한 칸 민다.",
        accent = Color(0xFFE91E63)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            RibbonPill(
                text = "새 기능 써 보기",
                ribbon = Modifier.ribbonVariant(
                    brush = RibbonBrush,
                    stroke = 5.dp,
                    turns = 7f,
                    radiusScale = if (clearContent) null else 0.3f,
                    split = if (split) RibbonSplit.DashPhase else RibbonSplit.None,
                    progress = { progress.value }
                ),
                modifier = Modifier.width(220.dp),
                backgroundFirst = backgroundFirst
            )
        }
        RibbonChoiceRow(
            label = "깊이 분할",
            options = listOf(true to "켬 (원문)", false to "끔"),
            selected = split,
            onSelect = { split = it }
        )
        RibbonChoiceRow(
            label = "반지름",
            options = listOf(true to "높이/2 + 굵기 (원문)", false to "높이 × 0.3"),
            selected = clearContent,
            onSelect = { clearContent = it }
        )
        RibbonChoiceRow(
            label = "Modifier 순서",
            options = listOf(false to "ribbon → background (원문)", true to "background → ribbon"),
            selected = backgroundFirst,
            onSelect = { backgroundFirst = it }
        )
        OutlinedButton(
            onClick = {
                scope.launch {
                    progress.snapTo(0f)
                    progress.animateTo(1f, tween(durationMillis = 1600, easing = LinearEasing))
                }
            }
        ) {
            Text(text = "감기 재생", fontSize = 12.sp)
        }
        when {
            !split -> RibbonStat("끔 — 리본 전체가 drawContent() 뒤에 그려져 내용 위를 덮는다. 감은 느낌이 없다")
            !clearContent -> RibbonStat("높이 × 0.3 — 앞뒤가 바뀌는 원의 꼭대기·바닥이 버튼 면 안으로 들어와 리본이 버튼 위에서 끊긴다")
            backgroundFirst -> RibbonStat("background → ribbon — 배경은 ribbon 바깥에서 먼저 그려져 drawContent() 엔 글자만 남는다. 뒤 구간도 배경 위에 보인다")
            else -> RibbonStat("원문 구성 — 앞뒤가 바뀌는 지점이 버튼 위아래 밖이라 이음매가 안 보인다")
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 4. 진행률 — chainPathEffect 순서
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun ChainOrderCard() {
    val progress = remember { mutableFloatStateOf(0.06f) }
    val provider = remember(progress) { { progress.floatValue } }
    val scope = rememberCoroutineScope()

    RibbonCard(
        title = "4. 진행률 — chainPathEffect 는 inner 가 먼저",
        description = "chainPathEffect(outer, inner) 는 inner 를 먼저 적용한다. 원문은 inner = 진행률 대시 [길이×p, 길이] 로 앞부분만 남긴 뒤 " +
            "outer = 깊이 대시로 나눈다. 뒤집어 깊이 대시를 먼저 적용하면 구간마다 따로 된 윤곽선이 생기고, 진행률 대시는 윤곽선마다 처음부터 다시 잰다.",
        accent = Color(0xFFFF8F00)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChainOrderColumn("원문 (inner = 진행률)", ChainOrder.ProgressInner, provider, Modifier.weight(1f))
            ChainOrderColumn("뒤집음 (inner = 깊이)", ChainOrder.DepthInner, provider, Modifier.weight(1f))
        }
        ProgressSlider(progress)
        OutlinedButton(
            onClick = {
                scope.launch {
                    animate(0f, 1f, animationSpec = tween(durationMillis = 2400, easing = LinearEasing)) { value, _ ->
                        progress.floatValue = value
                    }
                }
            }
        ) {
            Text(text = "0 → 100% 재생", fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChainOrderColumn(label: String, order: ChainOrder, progress: () -> Float, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            RibbonPill(
                text = "리본",
                ribbon = Modifier.ribbonVariant(
                    brush = RibbonBrush,
                    stroke = 4.dp,
                    turns = 5f,
                    chainOrder = order,
                    progress = progress
                ),
                modifier = Modifier.fillMaxWidth(),
                height = 34.dp,
                fontSize = 12.sp
            )
        }
        Text(text = label, fontSize = 11.sp, color = Color(0xFF555555))
    }
}

/** 슬라이더만 진행률을 컴포지션에서 읽는다 — 카드가 리컴포즈되지 않아 리본 캐시가 유지된다 */
@Composable
private fun ProgressSlider(progress: MutableFloatState) {
    Slider(value = progress.floatValue, onValueChange = { progress.floatValue = it })
    RibbonStat("진행률 ${(progress.floatValue * 100).roundToInt()}% · 5바퀴 = 반 바퀴 구간 10개, 구간 하나가 전체 길이의 10%")
}

// ──────────────────────────────────────────────────────────────────────────
// 5. 함정 — phase 트릭
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun PhaseTrickCard() {
    var startAngle by remember { mutableFloatStateOf(-90f) }
    var pillSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val stats = remember(pillSize, startAngle, density) {
        if (pillSize == IntSize.Zero) {
            null
        } else {
            val segments = buildRibbon(pillSize.toSize(), with(density) { 4.dp.toPx() }, turns = 7f, startAngle = startAngle).segments
            segments to dashSplitMismatch(segments)
        }
    }

    RibbonCard(
        title = "5. 함정 — phase = 첫 구간은 구간 길이가 같을 때만 맞다",
        description = "phase 를 첫 구간 길이만큼 주면 패턴 전체가 그만큼 밀린다. 이것이 \"홀수 구간만\"이 되려면 모든 구간이 첫 구간과 길이가 같아야 한다. " +
            "원문 기본값(−90°, 바닥에서 시작)은 앞뒤 반 바퀴가 대칭이라 같지만, 시작 각을 옮기면 첫 구간이 짧아진다.",
        accent = Color(0xFFC62828)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PhaseColumn(
                label = "phase = 첫 구간 (원문)",
                split = RibbonSplit.DashPhase,
                startAngle = startAngle,
                modifier = Modifier.weight(1f),
                onPillSize = { pillSize = it }
            )
            PhaseColumn(
                label = "getSegment 로 나눔",
                split = RibbonSplit.Segments,
                startAngle = startAngle,
                modifier = Modifier.weight(1f)
            )
        }
        RibbonChoiceRow(
            label = "시작 각",
            options = listOf(-90f to "−90° (원문)", -45f to "−45°", 0f to "0°", 45f to "45°"),
            selected = startAngle,
            onSelect = { startAngle = it }
        )
        Spacer(modifier = Modifier.height(4.dp))
        stats?.let { (segments, mismatch) ->
            RibbonStat("구간 ${segments.size}개 · 첫 구간 = 둘째 구간의 ${percent(segments[0] / segments[1])}")
            RibbonStat(
                "원문 어긋남(전체 길이 대비) — 뒤 ${percent(mismatch.back)} · 앞 ${percent(mismatch.front)}",
                color = if (mismatch.front > 0.05f) Color(0xFFC62828) else Color(0xFF37474F)
            )
            RibbonStat("getSegment — 구간을 하나씩 잘라 앞·뒤 Path 에 담아 0%")
            if (segments.size % 2 == 1) {
                RibbonStat("구간이 홀수 개 — Android 는 마지막 칸을 버리고(count & ~1) 패턴을 처음부터 되풀이한다")
            }
        }
    }
}

@Composable
private fun PhaseColumn(
    label: String,
    split: RibbonSplit,
    startAngle: Float,
    modifier: Modifier,
    onPillSize: ((IntSize) -> Unit)? = null
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            RibbonPill(
                text = "리본",
                ribbon = Modifier.ribbonVariant(
                    brush = RibbonBrush,
                    stroke = 4.dp,
                    turns = 7f,
                    startAngle = startAngle,
                    split = split
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { onPillSize?.invoke(it) },
                height = 34.dp,
                fontSize = 12.sp
            )
        }
        Text(text = label, fontSize = 11.sp, color = Color(0xFF555555))
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 6. 함정 — 진행률을 값으로 넘기기
// ──────────────────────────────────────────────────────────────────────────

/** 화면 밖 카운터 — 상태가 아니어서 값을 바꿔도 리컴포지션을 일으키지 않는다(재생이 끝나면 한 번 읽어 표시) */
private class RibbonCounters {
    var recompositions = 0
    var cacheBuilds = 0
    var draws = 0

    fun reset() {
        recompositions = 0
        cacheBuilds = 0
        draws = 0
    }

    fun summary(): String = "리컴포지션 $recompositions · 캐시 생성 $cacheBuilds · 그리기 $draws"
}

@Composable
private fun CacheInvalidationCard() {
    val progress = remember { Animatable(1f) }
    val provider = remember(progress) { { progress.value } }
    val byValue = remember { RibbonCounters() }
    val byLambda = remember { RibbonCounters() }
    var result by remember { mutableStateOf<Pair<String, String>?>(null) }
    var playing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    RibbonCard(
        title = "6. 함정 — 진행률을 값으로 넘기면 캐시가 매 프레임 버려진다",
        description = "원문 시그니처는 ribbon(progress: Float). 애니메이션 값을 컴포지션에서 읽어 넘기면 프레임마다 리컴포즈 → Modifier 를 새로 만듦 → " +
            "drawWithCache 의 블록 람다가 바뀜 → 캐시를 버리고 점 1001개·접선 500번을 다시 계산한다. () -> Float 로 넘겨 onDrawWithContent 안에서 읽으면 다시 그리기만 한다.",
        accent = Color(0xFF6D4C41)
    ) {
        ValueRibbonRow(progress, byValue)
        LambdaRibbonRow(provider, byLambda)
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            enabled = !playing,
            onClick = {
                scope.launch {
                    playing = true
                    progress.snapTo(0f)
                    byValue.reset()
                    byLambda.reset()
                    progress.animateTo(1f, tween(durationMillis = 1600, easing = LinearEasing))
                    result = byValue.summary() to byLambda.summary()
                    playing = false
                }
            }
        ) {
            Text(text = if (playing) "재생 중..." else "재생 (1.6초)", fontSize = 12.sp)
        }
        val current = result
        if (current == null) {
            RibbonStat("재생하면 1.6초 동안 센 횟수를 보여 준다")
        } else {
            RibbonStat("값으로: ${current.first}", color = Color(0xFFC62828))
            RibbonStat("람다로: ${current.second}", color = Color(0xFF2E7D32))
        }
    }
}

/** 진행률을 컴포지션에서 읽는다 — 값이 바뀔 때마다 이 함수가 리컴포즈되고 ribbonByValue 가 새로 만들어진다 */
@Composable
private fun ValueRibbonRow(progress: Animatable<Float, AnimationVector1D>, counters: RibbonCounters) {
    val value = progress.value
    SideEffect { counters.recompositions++ }
    RibbonRowLayout(label = "값으로 — ribbonByValue(progress = anim.value)") {
        RibbonPill(
            text = "값",
            ribbon = Modifier.ribbonByValue(
                brush = RibbonBrush,
                progress = value,
                onCacheBuilt = { counters.cacheBuilds++ },
                onDrawn = { counters.draws++ }
            ),
            modifier = Modifier.width(180.dp),
            height = 36.dp,
            fontSize = 12.sp
        )
    }
}

/** 진행률을 람다로만 넘긴다 — 이 함수는 재생 중 리컴포즈되지 않는다 */
@Composable
private fun LambdaRibbonRow(progress: () -> Float, counters: RibbonCounters) {
    SideEffect { counters.recompositions++ }
    RibbonRowLayout(label = "람다로 — ribbon(progress = { anim.value })") {
        RibbonPill(
            text = "람다",
            ribbon = Modifier.ribbon(
                brush = RibbonBrush,
                progress = progress,
                onCacheBuilt = { counters.cacheBuilds++ },
                onDrawn = { counters.draws++ }
            ),
            modifier = Modifier.width(180.dp),
            height = 36.dp,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun RibbonRowLayout(label: String, pill: @Composable () -> Unit) {
    Text(text = label, fontSize = 11.sp, color = Color(0xFF555555))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        pill()
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 핵심 정리
// ──────────────────────────────────────────────────────────────────────────

@Composable
private fun RibbonGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "핵심 정리", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "경로 — 원의 중심을 내용의 왼쪽 끝에서 오른쪽 끝으로 끌며 둘레의 점을 lineTo. 반지름 = 높이/2 + 굵기라 앞뒤가 바뀌는 꼭대기·바닥이 내용 밖에 있다",
                "구간 — PathMeasure.getTangent(거리).y 의 부호가 바뀌는 곳마다 길이를 모은다. 표본이 적으면 경계가 밀린다",
                "깊이 — onDrawWithContent { 뒤 · drawContent() · 앞 }. ribbon 이 background 보다 바깥(앞)에 있어야 배경이 drawContent() 에 들어간다",
                "dashPathEffect(구간 배열) 은 짝수 구간, phase = 첫 구간이면 홀수 구간 — 구간 길이가 모두 같을 때만 맞다. 다르면 getSegment 로 앞·뒤 Path 를 따로 만든다",
                "intervals 가 2개 미만이면 DashPathEffect 생성자가 예외를 던진다 — 반 바퀴 미만이면 나누지 않는다",
                "chainPathEffect(outer, inner) 는 inner 먼저 — 진행률로 먼저 자르고 깊이로 나눈다",
                "진행률은 () -> Float 로 받아 그리기 단계에서 읽는다 — 값으로 받으면 프레임마다 캐시가 다시 만들어진다",
                "Compose Path 의 moveTo·lineTo 는 (x, y) 만 받는다 — 원문 전체 코드의 moveTo(Offset) 는 확장 함수를 따로 만들어야 한다"
            ).forEach {
                Text(
                    text = "• $it",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}
