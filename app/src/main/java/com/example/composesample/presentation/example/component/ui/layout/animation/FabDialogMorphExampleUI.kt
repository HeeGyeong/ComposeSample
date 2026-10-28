package com.example.composesample.presentation.example.component.ui.layout.animation

import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.composesample.presentation.MainHeader
import java.util.Locale
import kotlin.math.roundToInt

/**
 * FAB → 다이얼로그 모핑 예제.
 *
 * 플랫폼 AlertDialog 는 별도 창이라 FAB 자리에서 자라날 수 없다(가운데에서 페이드). 여기서는 FAB 과 다이얼로그를 Surface 하나로 두고
 * 진행값 p 하나로 위치·크기·모서리·색·내용 알파·스크림을 계산한다(계산은 FabDialogMorph.kt — 단위 테스트가 같은 함수를 부른다).
 * 접힌 상태는 트리 안 일반 FAB, 펼치는 동안만 창 원점에 고정한 Popup 오버레이에 그린다.
 */
@Composable
fun FabDialogMorphExampleUI(onBackEvent: () -> Unit) {
    val state = remember { MorphDemoState() }
    val progress = remember { Animatable(0f) }
    val overlayTally = remember { TextLayoutTally() }
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val windowSize = LocalWindowInfo.current.containerSize
    val view = LocalView.current
    val overlayVisible by remember { derivedStateOf { progress.value > 0f } }

    // expanded 가 바뀌면 진행값을 그쪽으로 — 도중에 바뀌면 이 효과가 취소되고 현재 값에서 반대로 이어간다
    LaunchedEffect(state.expanded) {
        val opening = state.expanded
        val target = if (opening) 1f else 0f
        if (progress.value == target) return@LaunchedEffect
        val spec: AnimationSpec<Float> =
            if (state.reduceMotion) snap() else tween(morphDurationMs(state.slow), easing = FastOutSlowInEasing)
        val specLabel = if (state.reduceMotion) "snap()" else "tween ${morphDurationMs(state.slow)}ms"
        // Compose 애니메이션이 실제로 쓰는 시간 배율 — 시스템 '애니메이션 길이 배율'(개발자 옵션·접근성 '애니메이션 삭제')이 여기로 온다
        val durationScale = coroutineContext[MotionDurationScale]?.scaleFactor
        overlayTally.reset()
        var frames = 0
        val startNs = System.nanoTime()
        progress.animateTo(target, spec) { frames++ }
        val report = MorphRunReport(
            specLabel = specLabel,
            frames = frames,
            elapsedMs = (System.nanoTime() - startNs) / 1_000_000,
            durationScale = durationScale,
            bodyLayouts = overlayTally.layouts,
            lineCounts = overlayTally.lineCounts,
        )
        if (opening) state.lastOpen = report else state.lastClose = report
    }

    // 비포커스 Popup(원문)은 키 이벤트를 받지 않는다 — 뒤로가기는 액티비티 창으로 가므로 트리의 BackHandler 가 받는다.
    // 포커스 Popup 이면 팝업 창이 받아 onDismissRequest 로 오고 이 BackHandler 는 불리지 않는다(실측)
    BackHandler(enabled = state.expanded) { state.dismiss("ⓑ 뒤로가기 — BackHandler") }

    // 배경 View 숨김: 펼친 동안 이 화면을 그리는 ComposeView 전체를 접근성에서 뺀다(별도 창인 Popup 은 영향 없음)
    val hideBackgroundView = overlayVisible && state.a11yMode == MorphA11yMode.HIDE_VIEW
    DisposableEffect(view, hideBackgroundView) {
        if (!hideBackgroundView) return@DisposableEffect onDispose { }
        val previous = view.importantForAccessibility
        view.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        onDispose { view.importantForAccessibility = previous }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .onGloballyPositioned {
                state.boxInWindow = it.positionInWindow()
                state.boxSize = it.size
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 배경 의미 비우기: 펼친 동안 이 화면의 의미를 지운다(hideFromAccessibility 는 그 노드 하나에만 걸린다)
                .then(
                    if (state.a11yMode == MorphA11yMode.CLEAR_SEMANTICS && overlayVisible) {
                        Modifier.clearAndSetSemantics { }
                    } else {
                        Modifier
                    }
                )
        ) {
            MainHeader(title = "FAB → 다이얼로그 모핑", onBackIconClicked = onBackEvent)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                // 마지막 카드가 오른쪽 아래 FAB 에 가리지 않게
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { OpenModeCard(state) }
                item { ProgressPreviewCard(state) }
                item { HandoverCard(state, windowSize) }
                item { DismissCard(state) }
                item { AccessibilityCard(state) }
                item { SummaryCard() }
            }
        }

        if (state.boxSize != IntSize.Zero) {
            val fabInBox = morphFabRect(
                state.boxSize.width.toFloat(), state.boxSize.height.toFloat(), density.density, rtl
            )
            if (!overlayVisible) {
                // 접힌 상태 — 트리 안 일반 FAB(배경 화면을 계속 만질 수 있다)
                MorphSurface(
                    frame = { morphFrame(0f, fabInBox, fabInBox, state.splitFade) },
                    dialogSizePx = IntSize.Zero,
                    fixedContentSize = true,
                    interaction = MorphInteraction.TREE_FAB,
                    paneTitle = null,
                    onFabClick = state::open,
                    onButton = {},
                    onBodyLayout = {},
                    modifier = Modifier.onGloballyPositioned { state.treeFabOnScreen = it.positionOnScreen() }
                )
            } else if (windowSize != IntSize.Zero) {
                val fabInOverlay = morphFabRectInOverlay(fabInBox, state.boxInWindow, state.offsetCorrection)
                val dialogRect = morphDialogRect(windowSize.width.toFloat(), windowSize.height.toFloat(), density.density)
                MorphOverlay(
                    windowSize = windowSize,
                    excludeGesture = state.excludeGesture,
                    focusable = state.a11yMode == MorphA11yMode.FOCUSABLE,
                    onPopupDismissRequest = { state.dismiss("ⓑ 뒤로가기 — Popup onDismissRequest(포커스 Popup)") },
                    scrimAlpha = { MORPH_SCRIM_MAX_ALPHA * progress.value },
                    onScrimTap = { state.dismiss("ⓑ 스크림 탭") },
                    onRootPositioned = handover@{ rootOnScreen ->
                        val treeFab = state.treeFabOnScreen ?: return@handover
                        state.handover = MorphHandover(
                            treeFabOnScreen = treeFab,
                            overlayRootOnScreen = rootOnScreen,
                            fabInOverlay = fabInOverlay.topLeft.round().let { Offset(it.x.toFloat(), it.y.toFloat()) },
                            corrected = state.offsetCorrection,
                        )
                    }
                ) {
                    MorphSurface(
                        frame = { morphFrame(progress.value, fabInOverlay, dialogRect, state.splitFade) },
                        dialogSizePx = IntSize(dialogRect.width.roundToInt(), dialogRect.height.roundToInt()),
                        fixedContentSize = state.fixedContentSize,
                        interaction = MorphInteraction.OVERLAY,
                        paneTitle = if (state.a11yMode != MorphA11yMode.NONE) "알림 받기 대화상자" else null,
                        onFabClick = {},
                        onButton = { label -> state.dismiss("ⓑ 버튼 '$label'") },
                        onBodyLayout = overlayTally::record,
                    )
                }
            }
        }
    }

    if (state.platformDialogOpen) {
        AlertDialog(
            onDismissRequest = { state.closePlatformDialog("ⓐ 플랫폼 Dialog — onDismissRequest(뒤로가기·바깥 탭)") },
            title = { Text("알림 받기") },
            text = { Text(MORPH_DIALOG_BODY) },
            confirmButton = {
                TextButton(onClick = { state.closePlatformDialog("ⓐ 플랫폼 Dialog — 버튼 '확인'") }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { state.closePlatformDialog("ⓐ 플랫폼 Dialog — 버튼 '취소'") }) { Text("취소") }
            }
        )
    }
}

private const val MORPH_DIALOG_BODY = "새 댓글과 답장이 오면 바로 알려 드릴까요? 알림은 설정에서 언제든 끌 수 있습니다."

private fun morphDurationMs(slow: Boolean): Int = if (slow) 1750 else 350

private enum class MorphOpenMode(val label: String) {
    PLATFORM("ⓐ 플랫폼 AlertDialog"),
    MORPH("ⓑ 모핑 오버레이"),
}

private enum class MorphInteraction { TREE_FAB, OVERLAY, PREVIEW }

/** 펼친 동안의 접근성 처리 — 넷 다 표면에 paneTitle 을 붙인다(NONE 만 빼고) */
private enum class MorphA11yMode(val label: String) {
    NONE("없음(원문)"),
    CLEAR_SEMANTICS("배경 의미 비우기"),
    HIDE_VIEW("배경 View 숨김"),
    FOCUSABLE("포커스 Popup"),
}

private data class MorphRunReport(
    val specLabel: String,
    val frames: Int,
    val elapsedMs: Long,
    val durationScale: Float?,
    val bodyLayouts: Int,
    val lineCounts: List<Int>,
)

private data class MorphHandover(
    val treeFabOnScreen: Offset,
    val overlayRootOnScreen: Offset,
    val fabInOverlay: Offset,
    val corrected: Boolean,
) {
    val error: Offset get() = morphHandoverError(treeFabOnScreen, overlayRootOnScreen, fabInOverlay)
}

/** 화면 상태 홀더 — 설정 스위치·측정값·닫기 기록 */
@Stable
private class MorphDemoState {
    var mode by mutableStateOf(MorphOpenMode.MORPH)
    var expanded by mutableStateOf(false)
    var platformDialogOpen by mutableStateOf(false)
    var slow by mutableStateOf(false)
    var reduceMotion by mutableStateOf(false)
    var splitFade by mutableStateOf(true)
    var fixedContentSize by mutableStateOf(true)
    var offsetCorrection by mutableStateOf(true)
    var a11yMode by mutableStateOf(MorphA11yMode.FOCUSABLE)
    var excludeGesture by mutableStateOf(false)
    var lastOpen by mutableStateOf<MorphRunReport?>(null)
    var lastClose by mutableStateOf<MorphRunReport?>(null)
    var boxInWindow by mutableStateOf(Offset.Zero)
    var boxSize by mutableStateOf(IntSize.Zero)
    var treeFabOnScreen by mutableStateOf<Offset?>(null)
    var handover by mutableStateOf<MorphHandover?>(null)
    val dismissLog = mutableStateListOf<String>()
    private var dismissSeq = 0

    fun open() {
        if (mode == MorphOpenMode.PLATFORM) platformDialogOpen = true else expanded = true
    }

    /** 오버레이 닫기 — 닫는 경로는 이 함수 하나로 모으고 이유를 남긴다 */
    fun dismiss(reason: String) {
        if (!expanded) return
        expanded = false
        log(reason)
    }

    fun closePlatformDialog(reason: String) {
        platformDialogOpen = false
        log(reason)
    }

    private fun log(reason: String) {
        dismissSeq++
        dismissLog.add(0, "#$dismissSeq $reason")
        while (dismissLog.size > 8) dismissLog.removeAt(dismissLog.lastIndex)
    }
}

/** Popup 을 앵커와 상관없이 창 왼쪽 위(0, 0)에 고정한다 — clippingEnabled = false 와 함께 써야 시스템 바 영역까지 덮는다 */
private object MorphWindowOriginPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset = IntOffset.Zero
}

/** 창 전체를 덮는 오버레이 — 펼쳐지는 동안(p > 0)에만 둔다. 떠 있는 동안 화면의 모든 터치를 가져간다 */
@Composable
private fun MorphOverlay(
    windowSize: IntSize,
    excludeGesture: Boolean,
    focusable: Boolean,
    onPopupDismissRequest: () -> Unit,
    scrimAlpha: () -> Float,
    onScrimTap: () -> Unit,
    onRootPositioned: (Offset) -> Unit,
    surface: @Composable () -> Unit,
) {
    val properties = remember(excludeGesture, focusable) {
        PopupProperties(
            // 포커스 Popup 이면 키 이벤트(뒤로가기)가 팝업 창으로 가서 dismissOnBackPress → onDismissRequest 로 닫힌다
            focusable = focusable,
            dismissOnBackPress = focusable,
            dismissOnClickOutside = false,
            excludeFromSystemGesture = excludeGesture,
            clippingEnabled = false,
        )
    }
    Popup(
        popupPositionProvider = MorphWindowOriginPositionProvider,
        onDismissRequest = onPopupDismissRequest,
        properties = properties
    ) {
        // fillMaxSize 는 안 된다 — 팝업 창은 WRAP_CONTENT 라 내려오는 제약이 창 크기가 아니다. px 그대로 고정한다
        Box(
            modifier = Modifier
                .layout { measurable, _ ->
                    val placeable = measurable.measure(Constraints.fixed(windowSize.width, windowSize.height))
                    layout(windowSize.width, windowSize.height) { placeable.place(0, 0) }
                }
                .onGloballyPositioned { onRootPositioned(it.positionOnScreen()) }
        ) {
            // 스크림 — 창 전체라 헤더·시스템 바 영역까지 함께 어두워진다
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind { drawRect(Color.Black, alpha = scrimAlpha()) }
                    .pointerInput(Unit) { detectTapGestures { onScrimTap() } }
            )
            surface()
        }
    }
}

/**
 * FAB 이자 다이얼로그인 표면 하나. 값은 전부 frame() 람다로 받아 배치·그리기 단계에서만 읽는다
 * (진행값이 바뀌어도 리컴포즈 없이 다시 배치·그리기만 한다).
 */
@Composable
private fun MorphSurface(
    frame: () -> MorphFrame,
    dialogSizePx: IntSize,
    fixedContentSize: Boolean,
    interaction: MorphInteraction,
    paneTitle: String?,
    onFabClick: () -> Unit,
    onButton: (String) -> Unit,
    onBodyLayout: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fabColor = MaterialTheme.colorScheme.primaryContainer
    val dialogColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val iconTint = MaterialTheme.colorScheme.onPrimaryContainer
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .offset { frame().bounds.topLeft.round() }
            .layout { measurable, _ ->
                val bounds = frame().bounds
                val width = bounds.width.roundToInt().coerceAtLeast(0)
                val height = bounds.height.roundToInt().coerceAtLeast(0)
                val placeable = measurable.measure(Constraints.fixed(width, height))
                layout(width, height) { placeable.place(0, 0) }
            }
            .then(modifier)
            .graphicsLayer {
                shadowElevation = 6.dp.toPx()
                shape = RoundedCornerShape(frame().cornerDp.dp)
                clip = true
            }
            .drawBehind { drawRect(lerp(fabColor, dialogColor, frame().colorFraction)) }
            .then(
                when (interaction) {
                    MorphInteraction.TREE_FAB -> Modifier.clickable(role = Role.Button) { onFabClick() }
                    // 펼친 표면의 탭은 삼킨다 — 아니면 아래 스크림으로 새어 닫힌다
                    MorphInteraction.OVERLAY -> Modifier.pointerInput(Unit) { detectTapGestures { } }
                    MorphInteraction.PREVIEW -> Modifier
                }
            )
            .then(if (paneTitle != null) Modifier.semantics { this.paneTitle = paneTitle } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = "알림 설정 열기",
            tint = iconTint,
            modifier = Modifier.graphicsLayer { alpha = frame().iconAlpha }
        )
        if (interaction != MorphInteraction.TREE_FAB) {
            val contentModifier = if (fixedContentSize) {
                // 최종 크기에서 한 번만 배치 — 가운데 정렬 + 표면 클립이 나머지를 맡는다
                Modifier.requiredSize(with(density) { DpSize(dialogSizePx.width.toDp(), dialogSizePx.height.toDp()) })
            } else {
                // 비교군: 자라는 표면 크기를 그대로 받아 프레임마다 다시 줄바꿈
                Modifier.fillMaxSize()
            }
            MorphDialogBody(
                modifier = contentModifier.graphicsLayer { alpha = frame().contentAlpha },
                onButton = if (interaction == MorphInteraction.OVERLAY) onButton else { _ -> },
                onBodyLayout = onBodyLayout,
            )
        }
    }
}

@Composable
private fun MorphDialogBody(
    modifier: Modifier,
    onButton: (String) -> Unit,
    onBodyLayout: (Int) -> Unit,
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = "알림 받기",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = MORPH_DIALOG_BODY,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            onTextLayout = { onBodyLayout(it.lineCount) }
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onButton("취소") }) { Text("취소") }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { onButton("확인") }) { Text("확인") }
        }
    }
}

// ==================== 카드 ====================

@Composable
private fun OpenModeCard(state: MorphDemoState) {
    MorphCard(title = "1. 열기 방식 — 플랫폼 Dialog 대 모핑 오버레이") {
        MorphBody(
            "오른쪽 아래 + 버튼이 이 예제의 FAB 입니다. ⓐ 는 같은 버튼이 Material3 AlertDialog 를 띄우고(별도 창 — 가운데에서 " +
                "페이드), ⓑ 는 FAB 표면 자체가 다이얼로그로 자랍니다."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MorphOpenMode.entries.forEach { mode ->
                FilterChip(
                    selected = state.mode == mode,
                    onClick = { state.mode = mode },
                    label = { Text(mode.label, fontSize = 12.sp) }
                )
            }
        }
        MorphToggle("느리게 (350ms → 1750ms)", state.slow) { state.slow = it }
        MorphToggle("모션 줄이기 — snap()", state.reduceMotion) { state.reduceMotion = it }
        MorphMono(
            "열기  ${describeRun(state.lastOpen)}\n" +
                "닫기  ${describeRun(state.lastClose)}"
        )
        MorphNote(
            "배율은 애니메이션 코루틴의 MotionDurationScale — 시스템 '애니메이션 삭제'(animator_duration_scale 0)에서는 0 이 되어 " +
                "tween 350ms 도 1프레임에 끝난다(실측). 그래서 Android 에서는 snap() 이 없어도 시스템 설정을 따른다 — snap() 은 " +
                "앱 안 설정이나 원문처럼 다른 플랫폼(Compose Multiplatform)을 위한 것."
        )
        MorphNote(
            "ⓒ 같은 창 안에서 컨테이너를 바꾸는 방법(SharedTransitionLayout + sharedBounds)은 Shared Element Transition " +
                "예제에 있다. 창 안 레이아웃 경계를 넘지 못하는 것은 2번 카드 미리보기가 같은 계산으로 보인다."
        )
    }
}

private fun describeRun(report: MorphRunReport?): String {
    if (report == null) return "-"
    val scale = report.durationScale?.let { String.format(Locale.US, "%.2f", it) } ?: "없음"
    return "${report.specLabel} · 프레임 ${report.frames} · ${report.elapsedMs}ms · 배율 $scale\n" +
        "      본문 배치 ${report.bodyLayouts}회 · 줄 수 ${describeLineCounts(report.lineCounts)}"
}

@Composable
private fun ProgressPreviewCard(state: MorphDemoState) {
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var p by remember { mutableFloatStateOf(0f) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    val tally = remember { TextLayoutTally() }
    var tallyVersion by remember { mutableIntStateOf(0) }

    // 설정이 바뀌면 센 값을 비운다
    LaunchedEffect(state.splitFade, state.fixedContentSize) {
        tally.reset()
        tallyVersion++
    }

    MorphCard(title = "2. 진행값 하나로 계산하는 값") {
        MorphBody(
            "Popup 없이 같은 계산을 이 상자(트리) 안에만 그린 미리보기입니다. 스크림과 다이얼로그가 상자 경계 밖으로 나가지 " +
                "못합니다 — 실제 FAB 이 창 전체를 덮으려면 오버레이가 필요한 이유입니다."
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFECEFF1))
                .onSizeChanged { boxSize = it }
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(0.9f, 0.7f, 0.8f, 0.5f).forEach { fraction ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(12.dp)
                            .background(Color(0xFFCFD8DC), RoundedCornerShape(6.dp))
                    )
                }
            }
            if (boxSize != IntSize.Zero) {
                val fab = morphFabRect(boxSize.width.toFloat(), boxSize.height.toFloat(), density.density, rtl)
                val dialog = morphDialogRect(boxSize.width.toFloat(), boxSize.height.toFloat(), density.density)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(Color.Black, alpha = morphFrame(p, fab, dialog, state.splitFade).scrimAlpha)
                        }
                )
                MorphSurface(
                    frame = { morphFrame(p, fab, dialog, state.splitFade) },
                    dialogSizePx = IntSize(dialog.width.roundToInt(), dialog.height.roundToInt()),
                    fixedContentSize = state.fixedContentSize,
                    interaction = MorphInteraction.PREVIEW,
                    paneTitle = null,
                    onFabClick = {},
                    onButton = {},
                    onBodyLayout = tally::record,
                )
            }
        }
        Slider(
            value = p,
            onValueChange = { p = it },
            // 놓을 때 센 값을 화면에 옮긴다(배치 중 상태 쓰기를 피한다)
            onValueChangeFinished = { tallyVersion++ }
        )
        MorphToggle("내용 창 나누기 (아이콘 ~1/3 · 내용 0.55~)", state.splitFade) { state.splitFade = it }
        MorphToggle("내용 크기 고정 — requiredSize", state.fixedContentSize) { state.fixedContentSize = it }
        MorphNote(
            "고정을 끄면 본문이 자라는 표면 크기를 그대로 받아 프레임마다 다시 줄바꿈한다 — 실제 FAB 을 열면 1번 카드의 '본문 배치'가 " +
                "1회 → 10~15회, 줄 수 46→…→2 로 바뀌었다(Pixel 4 API 30 에뮬 debug 15회 · release 10회 — 횟수는 프레임 수를 따른다)."
        )
        if (boxSize != IntSize.Zero) {
            val fab = morphFabRect(boxSize.width.toFloat(), boxSize.height.toFloat(), density.density, rtl)
            val dialog = morphDialogRect(boxSize.width.toFloat(), boxSize.height.toFloat(), density.density)
            val frame = morphFrame(p, fab, dialog, state.splitFade)
            val color = lerp(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.surfaceContainerHigh,
                frame.colorFraction
            )
            MorphMono(describeFrame(p, frame, density.density, color))
        }
        // tallyVersion 을 읽어 놓을 때마다 다시 그린다
        val tallyText = tallyVersion.let {
            "설정을 바꾼 뒤 본문 다시 배치 ${tally.layouts}회 · 줄 수 ${describeLineCounts(tally.lineCounts)}"
        }
        MorphMono(tallyText)
    }
}

private fun describeFrame(p: Float, frame: MorphFrame, density: Float, color: Color): String {
    fun dp(px: Float) = String.format(Locale.US, "%.1f", px / density)
    fun a(v: Float) = String.format(Locale.US, "%.2f", v)
    val b: Rect = frame.bounds
    return "p = ${a(p)} · ${morphFadePhase(frame.iconAlpha, frame.contentAlpha).label}\n" +
        "위치 (${dp(b.left)}, ${dp(b.top)})dp · 크기 ${dp(b.width)}×${dp(b.height)}dp\n" +
        "모서리 ${String.format(Locale.US, "%.1f", frame.cornerDp)}dp · 색 ${(frame.colorFraction * 100).roundToInt()}% " +
        String.format(Locale.US, "#%06X", color.toArgb() and 0xFFFFFF) + "\n" +
        "아이콘 α ${a(frame.iconAlpha)} · 내용 α ${a(frame.contentAlpha)} · 스크림 α ${a(frame.scrimAlpha)}"
}

@Composable
private fun HandoverCard(state: MorphDemoState, windowSize: IntSize) {
    MorphCard(title = "3. 트리 ↔ 오버레이 넘김 — 같은 픽셀") {
        MorphBody(
            "접힌 FAB 은 예제 Box 좌표, 펼치는 동안은 창 원점에 고정한 Popup 좌표에 그립니다. 두 좌표계의 차이가 예제 Box 의 " +
                "창 위치(positionInWindow)라 이를 더하지 않으면 누르는 순간 표면이 그만큼 튑니다."
        )
        MorphToggle("창 위치 보정 (positionInWindow 더하기)", state.offsetCorrection) { state.offsetCorrection = it }
        val h = state.handover
        MorphMono(
            "창 크기(containerSize) ${windowSize.width}×${windowSize.height}px\n" +
                "예제 Box 창 위치 ${fmt(state.boxInWindow)} · 크기 ${state.boxSize.width}×${state.boxSize.height}px\n" +
                if (h == null) {
                    "넘김 오차 — FAB 을 한 번 펼치면 잰다"
                } else {
                    "트리 FAB 화면 위치 ${fmt(h.treeFabOnScreen)}\n" +
                        "오버레이 원점 ${fmt(h.overlayRootOnScreen)} + p=0 FAB ${fmt(h.fabInOverlay)}\n" +
                        "넘김 오차 ${fmt(h.error)}px " + (if (h.error == Offset.Zero) "— 같은 픽셀" else "— 튄다") +
                        (if (h.corrected) " (보정 켬)" else " (보정 끔)")
                }
        )
        MorphNote(
            "이 앱은 BlogExampleActivity 루트가 systemBars 패딩을 줘서 예제 Box 가 이미 내비 바 위에 있습니다. 원문은 FAB 위치에 " +
                "내비 바 인셋을 더하는데, 여기서 그대로 더하면 두 번 띄워집니다."
        )
    }
}

private fun fmt(offset: Offset): String = "(${offset.x.roundToInt()}, ${offset.y.roundToInt()})"

@Composable
private fun DismissCard(state: MorphDemoState) {
    MorphCard(title = "4. 닫기 경로 — 직접 연결한다") {
        MorphBody(
            "플랫폼 Dialog 는 뒤로가기·바깥 탭을 창이 받아 onDismissRequest 로 줍니다. 오버레이는 스크림 탭 · 버튼 · 뒤로가기를 " +
                "직접 잇습니다. 뒤로가기 경로는 Popup 의 포커스로 갈립니다 — 비포커스(원문)는 키가 액티비티 창으로 가서 트리의 " +
                "BackHandler(펼친 동안만 켬), 포커스 Popup 은 팝업 창이 받아 dismissOnBackPress → onDismissRequest(5번 카드에서 선택)."
        )
        MorphToggle("시스템 제스처 제외 요청 — excludeFromSystemGesture", state.excludeGesture) {
            state.excludeGesture = it
        }
        MorphNote(
            "원문처럼 PopupProperties(focusable = false, clippingEnabled = false) 로 만들면 이 값이 기본값 true 로 남는다. 창 전체 " +
                "팝업이 제외를 요청하면 시스템은 가장자리마다 아래쪽 200dp 만 들어준다 — 제스처 내비 에뮬(Pixel 4 API 30)에서 " +
                "화면 아래 550px(y 1730~), 태블릿(Pixel Tablet API 35)에서 400px(y 1200~)의 왼쪽 가장자리 스와이프가 뒤로가기가 되지 못하고 " +
                "앱 터치가 됐다. 그 스와이프는 스크림의 " +
                "detectTapGestures 가 탭으로 받아(움직임으로 취소하지 않는다) 닫히긴 하지만 기록은 '스크림 탭'이다. 두 포커스 모드 같음."
        )
        MorphMono(if (state.dismissLog.isEmpty()) "닫은 기록 없음" else state.dismissLog.joinToString("\n"))
        OutlinedButton(onClick = { state.dismissLog.clear() }) { Text("기록 지우기", fontSize = 12.sp) }
    }
}

@Composable
private fun AccessibilityCard(state: MorphDemoState) {
    MorphCard(title = "5. 접근성 — 다이얼로그 의미는 직접") {
        MorphBody(
            "플랫폼 Dialog 를 띄우면 접근성 창 목록에 Dialog 창 하나만 남습니다(활성·포커스, 액티비티 창은 빠짐). 비포커스 " +
                "Popup(원문)은 액티비티 창이 활성·포커스로 남아 스크림 뒤 배경이 그대로 읽힙니다. 없음을 뺀 셋은 표면에 paneTitle 을 붙입니다."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MorphA11yMode.entries.take(2).forEach { mode -> A11yChip(state, mode) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MorphA11yMode.entries.drop(2).forEach { mode -> A11yChip(state, mode) }
        }
        MorphMono(
            "펼친 동안 활성 창에서 읽히는 것\n" +
                "• 없음(원문): 포커스 액티비티 · 스크림 뒤 헤더·카드 글자\n" +
                "• 배경 의미 비우기: 아래 깔린 예제 목록(Last update·Sample UI) — 역효과\n" +
                "• 배경 View 숨김: 0개 · Popup 은 비활성 창으로 남음\n" +
                "• 포커스 Popup: 다이얼로그 4개 · 액티비티 창이 목록에서 빠짐"
        )
        MorphMono(
            "TalkBack 을 켠 채 펼쳤을 때 접근성 포커스\n" +
                "• 없음(원문): 배경 헤더에 남는다(테두리가 스크림 아래)\n" +
                "• 배경 View 숨김: 어디에도 없다 — 직접 찾아야 한다\n" +
                "• 포커스 Popup: 다이얼로그 제목 '알림 받기'로 옮겨 간다\n" +
                "• ⓐ 플랫폼 AlertDialog: 다이얼로그 제목으로 옮겨 간다"
        )
        MorphNote(
            "비우기가 역효과인 이유: 접근성 가지치기는 '중요한' 노드의 영역만 아래를 가린다. clearAndSetSemantics { } 로 비운 노드는 " +
                "중요하지 않아, 이 앱처럼 같은 컴포지션에서 화면 아래에 다른 화면(예제 목록)이 깔려 있으면 그것이 드러난다. View 숨김은 " +
                "ComposeView 에 IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS 를 펼친 동안만 건다."
        )
        MorphNote(
            "측정: uiautomator dump --compressed(접근성 서비스처럼 중요 노드만) · --windows 창 목록은 Pixel 4 API 30 에뮬 debug·release 같음, TalkBack 은 Pixel Tablet API 35 에뮬 debug(초록 포커스 테두리 위치). " +
                "semantics { dialog() } 는 Android 접근성 노드로 옮겨지지 않는다(ui 1.12.1 — 테스트의 isDialog() 매처용), paneTitle 은 " +
                "AccessibilityNodeInfo.paneTitle 과 창 상태 변경 이벤트로 간다."
        )
    }
}

@Composable
private fun A11yChip(state: MorphDemoState, mode: MorphA11yMode) {
    FilterChip(
        selected = state.a11yMode == mode,
        onClick = { state.a11yMode = mode },
        label = { Text(mode.label, fontSize = 12.sp) }
    )
}

@Composable
private fun SummaryCard() {
    MorphCard(title = "6. 정리") {
        MorphBody(
            "• 표면 하나 · 진행값 하나 — 위치·크기·모서리·색·알파·스크림을 모두 p 에서 계산\n" +
                "• 내용은 창을 나눠 페이드(겹침 없음), 다이얼로그 내용은 최종 크기에서 한 번만 배치\n" +
                "• 펼치는 동안만 창 원점 Popup(clippingEnabled = false), 접힌 상태는 트리 안 FAB — 넘김은 창 위치를 더해 같은 픽셀로\n" +
                "• 창 전체 Popup 은 excludeFromSystemGesture = false — 아니면 아래쪽 200dp 에서 가장자리 뒤로가기가 막힌다\n" +
                "• Android 에서는 포커스 Popup 이 플랫폼 Dialog 와 같은 접근성 창 구성 — 뒤로가기는 onDismissRequest 로\n" +
                "• 닫기·뒤로가기·다이얼로그 의미는 플랫폼이 주지 않으니 직접 연결"
        )
    }
}

// ==================== 공통 조각 ====================

@Composable
private fun MorphCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FA))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun MorphBody(text: String) {
    Text(text = text, fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF37474F))
}

@Composable
private fun MorphNote(text: String) {
    Text(text = text, fontSize = 12.sp, lineHeight = 17.sp, color = Color(0xFF607D8B))
}

@Composable
private fun MorphMono(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(8.dp))
            .padding(10.dp)
    )
}

@Composable
private fun MorphToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
