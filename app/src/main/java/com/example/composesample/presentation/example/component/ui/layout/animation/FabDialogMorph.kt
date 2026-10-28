package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import kotlin.math.min

/**
 * FAB → 다이얼로그 모핑 예제의 계산 — 화면과 단위 테스트가 함께 쓴다.
 *
 * 진행값 p(0 = FAB, 1 = 다이얼로그) 하나에서 위치·크기·모서리·색 비율·내용 알파·스크림 알파를 모두 계산한다.
 * 기본값은 원문 라이브러리(MorphingFab)의 MorphingFabDefaults 와 같다.
 */

internal const val MORPH_FAB_SIZE_DP = 56f
internal const val MORPH_FAB_EDGE_PADDING_DP = 16f
internal const val MORPH_FAB_CORNER_DP = 16f
internal const val MORPH_DIALOG_CORNER_DP = 28f
internal const val MORPH_DIALOG_WIDTH_DP = 320f
internal const val MORPH_DIALOG_HEIGHT_DP = 280f
internal const val MORPH_DIALOG_MIN_MARGIN_DP = 24f

// 아이콘은 진행의 1/3 까지 사라지고, 다이얼로그 내용은 0.55 부터 나타난다 — 둘 사이(0.33~0.55)는 일부러 비워 둔 구간
internal const val MORPH_ICON_FADE_OUT_BY = 1f / 3f
internal const val MORPH_CONTENT_FADE_IN_FROM = 0.55f
internal const val MORPH_SCRIM_MAX_ALPHA = 0.32f

/** 진행값 하나에서 계산한 한 프레임 — bounds 는 호출자가 넘긴 사각형과 같은 좌표계(px) */
internal data class MorphFrame(
    val bounds: Rect,
    val cornerDp: Float,
    val colorFraction: Float,
    val iconAlpha: Float,
    val contentAlpha: Float,
    val scrimAlpha: Float,
)

/** FAB 아이콘 알파 — 창을 나누면 1/3 까지 사라지고, 나누지 않으면 진행 전체에 걸쳐 1 → 0 */
internal fun morphIconAlpha(p: Float, splitFade: Boolean): Float =
    if (splitFade) (1f - p / MORPH_ICON_FADE_OUT_BY).coerceIn(0f, 1f) else (1f - p).coerceIn(0f, 1f)

/** 다이얼로그 내용 알파 — 창을 나누면 0.55 부터 1 까지, 나누지 않으면 진행 전체에 걸쳐 0 → 1 */
internal fun morphContentAlpha(p: Float, splitFade: Boolean): Float =
    if (splitFade) {
        ((p - MORPH_CONTENT_FADE_IN_FROM) / (1f - MORPH_CONTENT_FADE_IN_FROM)).coerceIn(0f, 1f)
    } else {
        p.coerceIn(0f, 1f)
    }

internal fun morphFrame(p: Float, fab: Rect, dialog: Rect, splitFade: Boolean): MorphFrame {
    val t = p.coerceIn(0f, 1f)
    return MorphFrame(
        bounds = lerp(fab, dialog, t),
        cornerDp = MORPH_FAB_CORNER_DP + (MORPH_DIALOG_CORNER_DP - MORPH_FAB_CORNER_DP) * t,
        colorFraction = t,
        iconAlpha = morphIconAlpha(t, splitFade),
        contentAlpha = morphContentAlpha(t, splitFade),
        scrimAlpha = MORPH_SCRIM_MAX_ALPHA * t,
    )
}

/** 아이콘·내용이 동시에 보이는지 — 화면 표의 꼬리표 */
internal enum class MorphFadePhase(val label: String) {
    ICON_ONLY("아이콘만"),
    BOTH("겹침 — 아이콘과 내용이 함께 보인다"),
    NEITHER("빈 구간 — 표면 색만 자란다"),
    CONTENT_ONLY("내용만"),
}

internal fun morphFadePhase(iconAlpha: Float, contentAlpha: Float): MorphFadePhase = when {
    iconAlpha > 0f && contentAlpha > 0f -> MorphFadePhase.BOTH
    iconAlpha > 0f -> MorphFadePhase.ICON_ONLY
    contentAlpha > 0f -> MorphFadePhase.CONTENT_ONLY
    else -> MorphFadePhase.NEITHER
}

/**
 * 컨테이너 가운데 다이얼로그 사각형(px) — 320×280dp, 가장자리 여백이 24dp 보다 좁아지면 줄인다.
 * 원문은 너비만 줄인다. 높이도 줄이는 것은 이 예제의 카드 미리보기(높이 300dp)를 위해 더했다.
 */
internal fun morphDialogRect(containerWidthPx: Float, containerHeightPx: Float, density: Float): Rect {
    val margin = MORPH_DIALOG_MIN_MARGIN_DP * density
    val width = min(MORPH_DIALOG_WIDTH_DP * density, containerWidthPx - margin * 2f).coerceAtLeast(0f)
    val height = min(MORPH_DIALOG_HEIGHT_DP * density, containerHeightPx - margin * 2f).coerceAtLeast(0f)
    val left = (containerWidthPx - width) / 2f
    val top = (containerHeightPx - height) / 2f
    return Rect(left, top, left + width, top + height)
}

/** 박스 안 FAB 사각형(px, 박스 좌표) — 아래·끝 모서리에서 16dp 안쪽, 56dp */
internal fun morphFabRect(boxWidthPx: Float, boxHeightPx: Float, density: Float, rtl: Boolean): Rect {
    val size = MORPH_FAB_SIZE_DP * density
    val padding = MORPH_FAB_EDGE_PADDING_DP * density
    val left = if (rtl) padding else boxWidthPx - padding - size
    val top = boxHeightPx - padding - size
    return Rect(left, top, left + size, top + size)
}

/**
 * 오버레이(창 원점에 고정한 Popup) 좌표로 옮긴 FAB 사각형.
 * 박스의 창 위치를 더해야 트리 FAB 과 같은 픽셀이다 — correct = false 는 그 보정을 빼 넘김 순간 튀는 것을 보이는 비교군.
 */
internal fun morphFabRectInOverlay(fabInBox: Rect, boxInWindow: Offset, correct: Boolean): Rect =
    if (correct) fabInBox.translate(boxInWindow) else fabInBox

/** 트리 FAB 과 오버레이 첫 프레임(p = 0) FAB 의 화면 좌표 차 — (0, 0) 이어야 넘기는 순간 튀지 않는다 */
internal fun morphHandoverError(treeFabOnScreen: Offset, overlayRootOnScreen: Offset, fabInOverlay: Offset): Offset =
    overlayRootOnScreen + fabInOverlay - treeFabOnScreen

/**
 * 다이얼로그 본문 Text 의 배치 기록 — 배치 횟수와 지나간 줄 수(연속 중복 제거).
 * 내용을 최종 크기로 고정(requiredSize)하면 한 번, 자라는 표면 크기를 그대로 받으면 프레임마다 다시 줄바꿈한다.
 */
internal class TextLayoutTally {
    var layouts = 0
        private set
    private val lines = ArrayList<Int>()

    val lineCounts: List<Int> get() = lines.toList()

    fun record(lineCount: Int) {
        layouts++
        if (lines.lastOrNull() != lineCount) lines.add(lineCount)
    }

    fun reset() {
        layouts = 0
        lines.clear()
    }
}

/** 줄 수 변화를 짧게 — 6개를 넘으면 앞 3개 … 뒤 2개 */
internal fun describeLineCounts(lineCounts: List<Int>): String = when {
    lineCounts.isEmpty() -> "-"
    lineCounts.size <= 6 -> lineCounts.joinToString("→")
    else -> lineCounts.take(3).joinToString("→") + "→…→" + lineCounts.takeLast(2).joinToString("→") +
        " (${lineCounts.size}가지)"
}
