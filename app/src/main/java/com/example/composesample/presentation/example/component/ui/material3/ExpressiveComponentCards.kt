package com.example.composesample.presentation.example.component.ui.material3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarState
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberFloatingToolbarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * M3 1.5.0 Expressive 정식 컴포넌트 카드(#188) — Material3ExpressiveExampleUI 의 목록이 쓴다.
 * 측정 · 판정 · 식은 ExpressiveComponentLab.kt.
 */

private val Purple = Color(0xFF6750A4)
private val SoftPurple = Color(0xFFF3EDF7)
private val Body = Color(0xFF424242)
private val Muted = Color(0xFF757575)
private val Ok = Color(0xFF2E7D32)
private val Bad = Color(0xFFC62828)

// ==================== ① Wavy 진행 표시 ====================

/** 진행값 칩 — 기본 진폭 함수의 경계(10% · 95%) 양쪽을 고른다 */
private val WAVY_STEPS = listOf(0.05f, 0.3f, 0.6f, 0.97f, 1f)

@Composable
internal fun WavyProgressCard(lab: WavyLabState, onMeasure: () -> Unit) {
    var target by remember { mutableFloatStateOf(0.6f) }
    var smooth by remember { mutableStateOf(true) }
    var changes by remember { mutableIntStateOf(0) }
    val animated = remember { Animatable(0.6f) }
    // 진행값은 람다로 넘긴다 — 지시기가 그리기 캐시 단계에서 읽으므로 값이 바뀌어도 미리보기는 리컴포즈되지 않는다
    val progress = remember { { animated.value } }
    val previewRecompositions = remember { PlainCounter() }
    LaunchedEffect(target, smooth) {
        if (smooth) animated.animateTo(target, WavyProgressIndicatorDefaults.ProgressAnimationSpec) else animated.snapTo(target)
    }

    ExpCard(
        title = "① Wavy 진행 표시 (1.5.0 정식)",
        subtitle = "Linear/CircularWavyProgressIndicator — 파도 높이는 진행값의 함수(기본: 10% 이하 · 95% 이상이면 0), " +
            "값 사이 애니메이션은 기본으로 없다(ProgressAnimationSpec 을 직접 건다)."
    ) {
        ChipRow(WAVY_STEPS.map { "${(it * 100).toInt()}%" }, WAVY_STEPS.indexOf(target)) {
            target = WAVY_STEPS[it]
            changes++
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ProgressAnimationSpec 으로 옮기기", fontSize = 12.sp, color = Body, modifier = Modifier.weight(1f))
            Switch(checked = smooth, onCheckedChange = { smooth = it })
        }
        Spacer(Modifier.height(8.dp))
        WavyPreview(progress, previewRecompositions)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "진행값 바꾼 횟수 $changes · 미리보기 리컴포지션 ${previewRecompositions.count}회",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Body
        )
        ExpNote("오른쪽 두 개는 진행값 없는(indeterminate) 판 — 이 카드가 컴포지션에 있는 동안 계속 프레임을 부른다.")

        Spacer(Modifier.height(12.dp))
        Text("그리기와 시계 — 값이 그대로일 때", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple)
        ExpNote(
            "줄마다 목록을 컴포지션에서 빼고 지시기 하나만 둔 무대에서 ${WAVY_WINDOW_MILLIS}ms 동안 센다. " +
                "그린 프레임 = 창이 그려 보낸 프레임(FrameMetrics), 프레임 콜백 = 메인 스레드가 VSync 에 깨어 Choreographer 프레임을 돈 횟수 " +
                "(Recomposer 시계가 돌면 그리지 않아도 깬다). 화면을 건드리지 말 것."
        )
        OutlinedButton(onClick = onMeasure, enabled = !lab.running, modifier = Modifier.testTag("wavyMeasure")) {
            Text(
                if (lab.running) "재는 중…"
                else "7줄 재기 (약 ${(WavyCase.entries.size * (WAVY_WINDOW_MILLIS + WAVY_WARMUP_MILLIS + WAVY_LATE_REPORT_MILLIS)) / 1000}초)",
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        TableRow(listOf("줄", "그린 프레임", "프레임 콜백", "기대", "판정"), WAVY_WEIGHTS, header = true)
        WavyCase.entries.forEach { case ->
            val run = lab.results[case]
            TableRow(
                cells = listOf(
                    case.label,
                    run?.let { "${it.rendered} (${it.drawRate?.label ?: "?"})" } ?: "-",
                    run?.let { "${it.callbacks} (${it.clockRate?.label ?: "?"})" } ?: "-",
                    "${expectedDraws(case).label} / ${expectedClock(case).label}",
                    run?.let { if (it.matches(case)) "맞음" else "다름" } ?: "-"
                ),
                weights = WAVY_WEIGHTS,
                lastColor = run?.let { if (it.matches(case)) Ok else Bad }
            )
        }
        ExpNote(
            "선형은 0% · 100% 근처에서 그리지 않는 동안에도 시계가 돈다 — 파도 오프셋 무한 애니메이션을 파도 높이와 상관없이 띄우기 때문. " +
                "원형은 높이가 0 이 되면 오프셋 애니메이션을 멈춘다. 다 끝난 진행 표시를 화면에 남겨 둘 거라면 선형은 waveSpeed = 0.dp 를 주거나 치운다."
        )
    }
}

private val WAVY_WEIGHTS = listOf(0.34f, 0.16f, 0.18f, 0.18f, 0.14f)

@Composable
private fun WavyPreview(progress: () -> Float, recompositions: PlainCounter) {
    val first = remember { booleanArrayOf(true) }
    SideEffect { if (first[0]) first[0] = false else recompositions.count++ }
    Column {
        LinearWavyProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularWavyProgressIndicator(progress = progress)
            LinearWavyProgressIndicator(modifier = Modifier.width(110.dp))
            CircularWavyProgressIndicator()
        }
    }
}

/** 측정 무대 — 목록 대신 화면을 차지한다(MainHeader 아래) */
@Composable
internal fun WavyMeasureStage(case: WavyCase, onStop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "재는 중 — ${case.label} (${case.ordinal + 1}/${WavyCase.entries.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Purple
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "목록을 컴포지션에서 빼고 이 지시기 하나만 둔다. 끝나면 목록으로 돌아간다.",
            fontSize = 12.sp,
            color = Muted
        )
        Spacer(Modifier.height(28.dp))
        key(case) {
            Box(Modifier.testTag("wavyStage").padding(8.dp)) {
                WavySubject(case)
            }
        }
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = onStop) { Text("중단", fontSize = 12.sp) }
    }
}

/** 측정 대상 하나 — 계측 테스트도 그대로 띄운다 */
@Composable
internal fun WavySubject(case: WavyCase) {
    val value = case.progress
    when (case.shape) {
        WavyShape.Linear -> LinearWavyProgressIndicator(
            progress = { value },
            waveSpeed = if (case.stillWave) 0.dp else WavyProgressIndicatorDefaults.LinearDeterminateWavelength
        )
        WavyShape.Circular -> CircularWavyProgressIndicator(
            progress = { value },
            waveSpeed = if (case.stillWave) 0.dp else WavyProgressIndicatorDefaults.CircularWavelength
        )
        WavyShape.Classic -> LinearProgressIndicator(progress = { value }, modifier = Modifier.width(240.dp))
    }
}

// ==================== ①-2 LoadingIndicator (아직 실험) ====================

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LoadingIndicatorCard() {
    var step by remember { mutableIntStateOf(2) }
    val value by animateFloatAsState(WAVY_STEPS[step], label = "loadingProgress")
    ExpCard(
        title = "①-2 LoadingIndicator (1.5.0 에서도 실험)",
        subtitle = "LoadingIndicator · ContainedLoadingIndicator · MaterialShapes 는 아직 @ExperimentalMaterial3ExpressiveApi — " +
            "쓰려면 @OptIn 이 필요하다. 진행값 판은 MaterialShapes 다각형 사이를 진행값만큼 morph 한다."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            LoadingIndicator()
            ContainedLoadingIndicator()
            LoadingIndicator(progress = { value })
        }
        Spacer(Modifier.height(8.dp))
        ChipRow(WAVY_STEPS.map { "${(it * 100).toInt()}%" }, step) { step = it }
        ExpNote("정식이 된 Wavy 와 달리 API 가 바뀔 수 있다 — 같은 화면에서 opt-in 경계를 보여 주려고 넣었다.")
    }
}

// ==================== ② SplitButtonLayout ====================

/** 칸 자리(root 좌표 px) */
private data class Placed(val left: Float, val width: Int, val height: Int) {
    val centerX: Float get() = left + width / 2f
}

/** 칸마다 onPlaced 로 자리를 받는다 — 같은 값이면 쓰지 않아 다시 배치 고리를 만들지 않는다 */
private class SplitRecorder {
    val boxes = mutableStateMapOf<String, Placed>()

    fun record(key: String, placed: Placed) {
        if (boxes[key] != placed) boxes[key] = placed
    }
}

/** 자리를 적고 같은 이름으로 테스트 태그도 단다(계측 테스트가 같은 노드를 찾는다) */
private fun Modifier.recordAs(recorder: SplitRecorder, key: String): Modifier = this
    .onPlaced { c -> recorder.record(key, Placed(c.positionInRoot().x, c.size.width, c.size.height)) }
    .testTag(key)

@Composable
internal fun SplitButtonCard() {
    val recorder = remember { SplitRecorder() }
    val density = LocalDensity.current
    fun dp(px: Number): Float = px.toFloat() / density.density

    ExpCard(
        title = "② SplitButtonLayout (1.5.0 정식)",
        subtitle = "주 동작(leading) + 관련 동작(trailing). trailing 을 먼저 재서 폭을 주고 남은 폭을 leading 에 준다, " +
            "두 버튼 높이는 큰 쪽으로 맞춘다. trailing 은 checked 오버로드가 있어 메뉴 열림을 모양(모서리 full)으로 보여 준다."
    ) {
        SplitVariants()

        Spacer(Modifier.height(14.dp))
        Text("무대 폭을 줄이면 — 누가 줄어드나", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple)
        SPLIT_STAGE_WIDTHS_DP.forEach { w ->
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .width(w.dp)
                    .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(4.dp))
                    .padding(4.dp)
            ) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(onClick = {}, modifier = Modifier.recordAs(recorder, "lead-$w")) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("PDF로 내보내기")
                        }
                    },
                    trailingButton = {
                        SplitButtonDefaults.TrailingButton(
                            checked = false,
                            onCheckedChange = {},
                            modifier = Modifier.recordAs(recorder, "trail-$w")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "형식 고르기", modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize))
                        }
                    },
                    modifier = Modifier.testTag("split-$w")
                )
            }
        }
        val rows = SPLIT_STAGE_WIDTHS_DP.mapNotNull { w ->
            val lead = recorder.boxes["lead-$w"] ?: return@mapNotNull null
            val trail = recorder.boxes["trail-$w"] ?: return@mapNotNull null
            SplitRow(w, dp(lead.width), dp(trail.width), dp(lead.height), dp(trail.height))
        }
        Spacer(Modifier.height(6.dp))
        TableRow(listOf("무대", "leading 폭", "trailing 폭", "높이 L/T"), SPLIT_WEIGHTS, header = true)
        rows.forEach { r ->
            TableRow(
                listOf(
                    "${r.stageDp}dp",
                    "${oneDecimal(r.leadingWidthDp)}dp",
                    "${oneDecimal(r.trailingWidthDp)}dp",
                    "${oneDecimal(r.leadingHeightDp)} / ${oneDecimal(r.trailingHeightDp)}"
                ),
                SPLIT_WEIGHTS
            )
        }
        if (rows.size == SPLIT_STAGE_WIDTHS_DP.size) {
            Verdict("trailing 폭 그대로", trailingKeptWidth(rows))
            Verdict("leading 이 줄어든다", leadingAbsorbed(rows))
            Verdict("두 버튼 높이가 같다(좁으면 leading 글자가 여러 줄 → 둘 다 커진다)", sameHeights(rows))
        }

        Spacer(Modifier.height(14.dp))
        Text("trailing 아이콘의 광학 중앙", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Purple)
        ExpNote(
            "trailing 은 시작 쪽 모서리가 좁고(${SPLIT_INNER_CORNER_DP.toInt()}dp) 끝 쪽이 둥글다. 내용을 0.11 × (시작 − 끝 모서리) 만큼 옮긴다 " +
                "(M3 내부 horizontalCenterOptically). checked 면 양쪽이 full 이라 0."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            listOf(false, true).forEach { checked ->
                val tag = if (checked) "checked" else "unchecked"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SplitButtonLayout(
                        leadingButton = {
                            SplitButtonDefaults.LeadingButton(onClick = {}) { Text("보내기") }
                        },
                        trailingButton = {
                            SplitButtonDefaults.TrailingButton(
                                checked = checked,
                                onCheckedChange = {},
                                modifier = Modifier.recordAs(recorder, "surface-$tag")
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(SplitButtonDefaults.TrailingIconSize)
                                        .recordAs(recorder, "icon-$tag")
                                )
                            }
                        }
                    )
                    Text(if (checked) "checked" else "해제", fontSize = 11.sp, color = Muted)
                }
            }
        }
        val full = SPLIT_HEIGHT_DP / 2
        listOf(false, true).forEach { checked ->
            val tag = if (checked) "checked" else "unchecked"
            val surface = recorder.boxes["surface-$tag"]
            val icon = recorder.boxes["icon-$tag"]
            val expected = if (checked) opticalOffsetDp(full, full, 12f, 12f) else opticalOffsetDp(SPLIT_INNER_CORNER_DP, full, 12f, 12f)
            if (surface != null && icon != null) {
                Text(
                    text = "${if (checked) "checked" else "해제"}: 아이콘 중심 − 버튼 중심 = ${oneDecimal(dp(icon.centerX - surface.centerX))}dp " +
                        "(식 ${oneDecimal(expected)}dp, 배치는 px 반올림)",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Body
                )
            }
        }
    }
}

private val SPLIT_WEIGHTS = listOf(0.18f, 0.26f, 0.26f, 0.3f)

@Composable
private fun SplitVariants() {
    var menuOpen by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf("PDF") }
    val rotation by animateFloatAsState(if (menuOpen) 180f else 0f, label = "splitArrow")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box {
            SplitButtonLayout(
                leadingButton = {
                    SplitButtonDefaults.LeadingButton(onClick = {}) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.LeadingIconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("${picked}로 내보내기")
                    }
                },
                trailingButton = {
                    SplitButtonDefaults.TrailingButton(checked = menuOpen, onCheckedChange = { menuOpen = it }) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "형식 고르기",
                            modifier = Modifier
                                .size(SplitButtonDefaults.TrailingIconSize)
                                .graphicsLayer { rotationZ = rotation }
                        )
                    }
                }
            )
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                listOf("PDF", "PNG", "CSV").forEach { format ->
                    DropdownMenuItem(text = { Text(format) }, onClick = {
                        picked = format
                        menuOpen = false
                    })
                }
            }
        }
        SplitButtonLayout(
            leadingButton = { SplitButtonDefaults.TonalLeadingButton(onClick = {}) { Text("Tonal") } },
            trailingButton = {
                SplitButtonDefaults.TonalTrailingButton(checked = false, onCheckedChange = {}) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize))
                }
            }
        )
        SplitButtonLayout(
            leadingButton = { SplitButtonDefaults.OutlinedLeadingButton(onClick = {}) { Text("Outlined") } },
            trailingButton = {
                SplitButtonDefaults.OutlinedTrailingButton(checked = false, onCheckedChange = {}) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize))
                }
            }
        )
        SplitButtonLayout(
            leadingButton = { SplitButtonDefaults.ElevatedLeadingButton(onClick = {}) { Text("Elevated") } },
            trailingButton = {
                SplitButtonDefaults.ElevatedTrailingButton(checked = false, onCheckedChange = {}) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize))
                }
            }
        )
    }
}

// ==================== ③ HorizontalFloatingToolbar ====================

@Composable
internal fun FloatingToolbarCard() {
    var expanded by remember { mutableStateOf(true) }
    var clip by remember { mutableStateOf(false) }
    val toolbarState = rememberFloatingToolbarState()

    ExpCard(
        title = "③ HorizontalFloatingToolbar (1.5.0 정식)",
        subtitle = "exitAlwaysScrollBehavior 를 목록의 조상에 nestedScroll 로 붙이면 내용을 올릴 때 숨고 내릴 때 나온다. " +
            "손을 떼면 다 숨기거나 다 보이게 맞춘다(collapsedFraction 0.5 기준) — 숨는 거리의 반을 못 넘게 내리면 도로 숨는다. " +
            "FAB 를 누르면 expanded 를 뒤집는다(접히면 FAB 만 남는다)."
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("상자 clipToBounds", fontSize = 12.sp, color = Body, modifier = Modifier.weight(1f))
            Switch(checked = clip, onCheckedChange = { clip = it }, modifier = Modifier.testTag("toolbarClip"))
        }
        ToolbarStage(expanded = expanded, onToggle = { expanded = !expanded }, clip = clip, state = toolbarState)
        ToolbarReadout(toolbarState)
        ExpNote(
            "숨는 거리(offsetLimit)는 툴바 위쪽부터 부모 아래 끝까지 — 툴바는 부모 밖으로 완전히 나간다. " +
                "화면 바닥이 아닌 카드 안에 두면 clipToBounds 를 끈 채로는 나간 툴바가 아래 글자를 덮는다(위 스위치). " +
                "옮기는 양은 목록이 실제로 소비한 스크롤뿐이다(소스: onPostScroll 이 consumed.y 만 더한다)."
        )
        ExpNote(
            "소스(1.5.0): 터치 탐색(TalkBack)이 켜져 있으면 expanded 를 무시하고 항상 펼친다 · 화면 밖으로 나간 툴바는 키보드 포커스를 받지 않는다."
        )
    }
}

/** 계측 테스트도 그대로 띄우는 무대 — 목록 30줄 + 바닥 툴바 */
@Composable
internal fun ToolbarStage(expanded: Boolean, onToggle: () -> Unit, clip: Boolean, state: FloatingToolbarState) {
    val behavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        exitDirection = FloatingToolbarExitDirection.Bottom,
        state = state
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(240.dp)
            .then(if (clip) Modifier.clipToBounds() else Modifier)
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .nestedScroll(behavior)
            .testTag("toolbarStage")
    ) {
        LazyColumn(Modifier.fillMaxSize().testTag("toolbarList")) {
            items(TOOLBAR_ROWS) { i ->
                Text(
                    "받은 메일 ${i + 1}",
                    fontSize = 13.sp,
                    color = Body,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
        HorizontalFloatingToolbar(
            expanded = expanded,
            floatingActionButton = {
                FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = onToggle) {
                    Icon(Icons.Filled.Add, contentDescription = "툴바 접기·펴기")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-12).dp),
            scrollBehavior = behavior
        ) {
            IconButton(onClick = {}) { Icon(Icons.Filled.Edit, contentDescription = "편집") }
            IconButton(onClick = {}) { Icon(Icons.Filled.Share, contentDescription = "공유") }
            IconButton(onClick = {}) { Icon(Icons.Filled.Delete, contentDescription = "삭제") }
        }
    }
}

internal const val TOOLBAR_ROWS = 30

/** 스크롤마다 바뀌는 값을 읽는 곳을 여기로 좁힌다 — 다시 그리는 범위가 이 줄뿐 */
@Composable
private fun ToolbarReadout(state: FloatingToolbarState) {
    val density = LocalDensity.current
    Text(
        text = "offset ${oneDecimal(state.offset / density.density)}dp / offsetLimit ${oneDecimal(state.offsetLimit / density.density)}dp",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Body,
        modifier = Modifier.padding(top = 6.dp)
    )
}

// ==================== ④ HorizontalCenteredHeroCarousel ====================

internal const val HERO_COUNT = 6
internal const val HERO_SCROLL_MILLIS = 1500
private val HeroColors = listOf(
    Color(0xFF6750A4), Color(0xFF7D5260), Color(0xFF386A20), Color(0xFF006A6A), Color(0xFF8C4A00), Color(0xFF3F5AA9)
)

@Composable
internal fun HeroCarouselCard() {
    var place by remember { mutableStateOf(HeroReadPlace.Draw) }
    val probe = remember { HeroProbe() }
    val state = rememberCarouselState { HERO_COUNT }
    val scope = rememberCoroutineScope()
    val results = remember { mutableStateMapOf<HeroReadPlace, Int>() }
    var items by remember { mutableStateOf<List<HeroItem>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    ExpCard(
        title = "④ HorizontalCenteredHeroCarousel (1.5.0 정식)",
        subtitle = "선택한 칸을 큰 칸으로 가운데 두고 양옆에 작은 칸 — 양 끝에서는 시작 · 끝에 붙는다. " +
            "칸은 모두 큰 칸 폭으로 배치하고 작은 칸은 잘라서(mask) 보인다."
    ) {
        HeroStage(state, place, probe)
        Spacer(Modifier.height(8.dp))
        ChipRow(HeroReadPlace.entries.map { it.label }, place.ordinal) { place = HeroReadPlace.entries[it] }
        ExpNote(
            "컴포지션에서 읽기: 칸 글자를 Text(carouselItemDrawInfo.size) 로 — 스크롤 프레임마다 칸이 리컴포즈되고, " +
                "글자는 큰 칸 폭 가운데라 작은 칸에서 잘린다. 그리기에서 읽기: drawText 로 마스크 안 가운데에, 마스크보다 넓으면 번호만."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = !busy, onClick = {
                scope.launch {
                    busy = true
                    try {
                        for (p in HeroReadPlace.entries) {
                            place = p
                            state.scrollToItem(0)
                            delay(400)
                            probe.compositions = 0
                            // 기본 spring 은 느린 기기에서 몇 프레임에 끝나 비교가 약하다 — 같은 길이로 고정
                            state.animateScrollToItem(1, tween(HERO_SCROLL_MILLIS, easing = LinearEasing))
                            delay(200)
                            results[p] = probe.compositions
                        }
                        items = probe.snapshot()
                    } finally {
                        busy = false
                    }
                }
            }) { Text("두 자리 차례로 재기", fontSize = 12.sp) }
            OutlinedButton(onClick = { items = probe.snapshot() }) { Text("지금 폭 읽기", fontSize = 12.sp) }
        }
        HeroReadPlace.entries.forEach { p ->
            Text(
                "${p.label}: 0 → 1 을 ${HERO_SCROLL_MILLIS}ms 에 넘기는 동안 칸 컴포지션 ${results[p]?.toString() ?: "-"}회",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Body
            )
        }
        val composition = results[HeroReadPlace.Composition]
        val draw = results[HeroReadPlace.Draw]
        if (composition != null && draw != null) Verdict("컴포지션에서 읽으면 칸 컴포지션이 훨씬 많다", compositionReadCostsMore(composition, draw))

        if (items.isNotEmpty()) {
            val selected = state.currentItem
            val visible = expectedHeroVisible(selected, HERO_COUNT)
            Spacer(Modifier.height(8.dp))
            Text("선택 ${selected + 1}번 — 보여야 할 칸 ${visible.map { it + 1 }}", fontSize = 12.sp, color = Body)
            TableRow(listOf("칸", "배치 폭", "마스크 폭", "drawInfo min~max"), HERO_WEIGHTS, header = true)
            items.filter { it.index in visible }.forEach { item ->
                TableRow(
                    listOf(
                        "${item.index + 1}${if (item.index == selected) " (선택)" else ""}",
                        "${oneDecimal(item.layoutWidth / density.density)}dp",
                        "${oneDecimal(item.maskWidth / density.density)}dp",
                        "${oneDecimal(item.minSize / density.density)}~${oneDecimal(item.maxSize / density.density)}"
                    ),
                    HERO_WEIGHTS
                )
            }
            Verdict("배치 폭은 모두 큰 칸 폭", layoutAllLarge(items.filter { it.index in visible }))
            Verdict("선택 칸만 큰 칸, 이웃은 작은 칸", heroMasksMatch(items, selected, HERO_COUNT))
        }
    }
}

private val HERO_WEIGHTS = listOf(0.22f, 0.24f, 0.24f, 0.3f)

/** 계측 테스트도 그대로 띄우는 캐러셀 */
@Composable
internal fun HeroStage(state: CarouselState, place: HeroReadPlace, probe: HeroProbe) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val shape = RoundedCornerShape(20.dp)
    HorizontalCenteredHeroCarousel(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .testTag("hero"),
        itemSpacing = 8.dp
    ) { i ->
        SideEffect { probe.compositions++ }
        val color = HeroColors[i % HeroColors.size]
        val tile = Modifier
            .fillMaxSize()
            .onPlaced { probe.placed(i, it.size.width) }
            .maskClip(shape)
            .background(color)
        when (place) {
            HeroReadPlace.Composition -> {
                // 컴포지션에서 읽는다 — 스크롤 프레임마다 이 칸이 리컴포즈된다
                val size = carouselItemDrawInfo.size
                Box(
                    tile.drawBehind {
                        probe.drawn(i, carouselItemDrawInfo.size, carouselItemDrawInfo.minSize, carouselItemDrawInfo.maxSize)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("${i + 1} · ${(size / density.density).toInt()}dp", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
            HeroReadPlace.Draw -> Box(
                tile.drawWithContent {
                    drawContent()
                    val info = carouselItemDrawInfo
                    probe.drawn(i, info.size, info.minSize, info.maxSize)
                    val style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    val mask = info.maskRect
                    // 마스크 폭에 맞춰 글자를 고른다 — 그리기 단계라 폭이 바뀌어도 리컴포지션 없이
                    val full = measurer.measure("${i + 1} · ${(info.size / density.density).toInt()}dp", style)
                    val layout = if (full.size.width <= mask.width - 8f) full else measurer.measure("${i + 1}", style)
                    drawText(
                        layout,
                        topLeft = Offset(
                            mask.left + (mask.width - layout.size.width) / 2f,
                            (size.height - layout.size.height) / 2f
                        )
                    )
                }
            )
        }
    }
}

// ==================== 공통 ====================

@Composable
private fun ExpCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Purple)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = Muted, lineHeight = 17.sp)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ExpNote(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Muted,
        lineHeight = 16.sp,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun Verdict(label: String, ok: Boolean) {
    Text(
        text = "${if (ok) "✓" else "✗"} $label",
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = if (ok) Ok else Bad,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun ChipRow(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        labels.forEachIndexed { idx, label ->
            Box(
                modifier = Modifier
                    .background(if (selected == idx) Purple else Color(0xFFE8DEF8), RoundedCornerShape(20.dp))
                    .clickable { onSelect(idx) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (selected == idx) Color.White else Purple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TableRow(cells: List<String>, weights: List<Float>, header: Boolean = false, lastColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (header) SoftPurple else Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        cells.forEachIndexed { i, cell ->
            Text(
                text = cell,
                modifier = Modifier.weight(weights[i]),
                fontSize = 10.sp,
                fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
                color = if (i == cells.lastIndex && lastColor != null) lastColor else Body,
                lineHeight = 14.sp
            )
        }
    }
}

/** 카드 묶음 사이 구분 제목 */
@Composable
internal fun SectionTitle(text: String, top: Dp = 8.dp) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Muted,
        modifier = Modifier.padding(top = top, start = 4.dp)
    )
}
