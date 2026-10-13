package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * RibbonModifierExample 의 경로 계산과 리본 Modifier.
 * 화면(RibbonModifierExampleUI)과 단위 테스트(RibbonPathTest)가 위쪽 순수 계산 함수를 함께 쓴다.
 */

// ──────────────────────────────────────────────────────────────────────────
// 순수 계산 — Android 클래스 없이 단위 테스트한다
// ──────────────────────────────────────────────────────────────────────────

/** 극좌표 → 화면 좌표. 각도는 반시계(수학) 방향인데 화면 y 는 아래로 커지므로 각도에 - 를 붙인다(원문과 같다) */
fun polarToCart(degrees: Float, distance: Float, origin: Offset = Offset.Zero): Offset {
    val radians = -degrees * (PI / 180.0)
    return Offset(
        x = (distance * cos(radians)).toFloat(),
        y = (distance * sin(radians)).toFloat()
    ) + origin
}

/**
 * 원을 start → end 로 끌면서 둘레의 점을 찍는다 — 원의 중심은 직선 위를 등속으로, 각도는 startAngle 부터 turns 바퀴.
 * 원문 createRibbon 은 각도 범위를 [startAngle, 360·(loops − 0.5) − startAngle] 로 잡는다 — 기본값(loops 7, −90°)이면
 * −90° ~ 2430° = 7바퀴로 여기 turns 7 과 같다. 원문 식은 시작 각을 바꾸면 바퀴 수도 함께 바뀌어, 시작 각만 따로 옮기도록 나눴다.
 */
fun ribbonPoints(
    start: Offset,
    end: Offset,
    radius: Float,
    turns: Float,
    startAngle: Float,
    resolution: Int = 1000
): List<Offset> = (0..resolution).map { i ->
    val t = i / resolution.toFloat()
    polarToCart(
        degrees = startAngle + 360f * turns * t,
        distance = radius,
        origin = lerp(start, end, t)
    )
}

/**
 * 경로를 resolution 등분해 접선 y 의 부호가 바뀔 때마다 구간 길이를 모은다(원문 방식).
 * 아래(+)로 향하는 반 바퀴와 위(−)로 향하는 반 바퀴가 번갈아 나온다 — 옆에서 본 나선의 뒤쪽과 앞쪽.
 * 경계는 표본 간격(length / resolution) 단위로 잘리므로 resolution 이 작으면 앞뒤가 바뀌는 지점이 밀린다.
 */
fun slopeSegments(length: Float, resolution: Int, tangentY: (distance: Float) -> Float): FloatArray {
    val segments = ArrayList<Float>()
    var isPositive = tangentY(0f) > 0f
    var segmentStart = 0f
    for (i in 0..resolution) {
        val distance = i / resolution.toFloat() * length
        val positive = tangentY(distance) > 0f
        if (positive != isPositive) {
            segments += distance - segmentStart
            segmentStart = distance
            isPositive = positive
        } else if (i == resolution) {
            segments += distance - segmentStart
        }
    }
    return segments.toFloatArray()
}

/**
 * DashPathEffect(intervals, phase) 가 경로 위 distance 를 그리는지(ON) — 짝수 칸이 ON, 홀수 칸이 OFF, phase 만큼 패턴을 당겨 시작.
 * Android 는 개수가 홀수면 마지막 칸을 버리고(네이티브 count & ~1), 패턴 합이 경로보다 짧으면 처음부터 되풀이한다.
 */
fun dashCovers(intervals: FloatArray, phase: Float, distance: Float): Boolean {
    val count = intervals.size and 1.inv()
    require(count >= 2) { "intervals 는 2개 이상이어야 한다" }
    var period = 0f
    for (i in 0 until count) period += intervals[i]
    var position = (distance + phase) % period
    for (i in 0 until count) {
        if (position < intervals[i]) return i % 2 == 0
        position -= intervals[i]
    }
    return false
}

/** distance 가 몇 번째 구간에 있는지 — 짝수 구간은 내용 뒤, 홀수 구간은 내용 앞에 그린다 */
fun segmentIndexAt(segments: FloatArray, distance: Float): Int {
    var end = 0f
    segments.forEachIndexed { index, length ->
        end += length
        if (distance < end) return index
    }
    return segments.lastIndex
}

/** 원문 두 번 그리기가 실제 구간과 어긋나는 길이의 비율(전체 길이 대비) */
data class SplitMismatch(val back: Float, val front: Float)

/**
 * 원문 방식 — 뒤는 dash(segments, phase 0), 앞은 dash(segments, phase = 첫 구간) — 을 구간 배열과 대조한다.
 * phase 를 첫 구간만큼 당기면 패턴 전체가 한 칸 밀리는데, 이것이 "홀수 구간만 그리기"가 되려면 모든 구간 길이가 같아야 한다.
 */
fun dashSplitMismatch(segments: FloatArray, samples: Int = 2000): SplitMismatch {
    if (segments.size < 2) return SplitMismatch(0f, 0f)
    val length = segments.sum()
    var backWrong = 0
    var frontWrong = 0
    for (k in 0 until samples) {
        val distance = (k + 0.5f) / samples * length
        val isFront = segmentIndexAt(segments, distance) % 2 == 1
        if (dashCovers(segments, 0f, distance) == isFront) backWrong++
        if (dashCovers(segments, segments.first(), distance) != isFront) frontWrong++
    }
    return SplitMismatch(backWrong / samples.toFloat(), frontWrong / samples.toFloat())
}

// ──────────────────────────────────────────────────────────────────────────
// 경로 만들기 · 그리기 — Compose Path / PathMeasure 사용
// ──────────────────────────────────────────────────────────────────────────

/** 한 번 만들어 drawWithCache 에 보관하는 리본 — 경로·길이 측정·앞뒤 구간 */
class RibbonGeometry(
    val path: Path,
    val measure: PathMeasure,
    val segments: FloatArray
) {
    val length: Float get() = measure.length
}

/** 점 목록 → Path. Compose Path 에는 moveTo(Offset) 가 없어(원문 전체 코드에 빠진 확장) x, y 로 넘긴다 */
fun pathOf(points: List<Offset>): Path = Path().apply {
    moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
}

/**
 * 내용 크기에 맞춘 리본 — 왼쪽 끝 가운데에서 오른쪽 끝 가운데까지 원을 끈다.
 * 반지름 기본값은 원문처럼 높이/2 + 선 굵기 — 앞뒤가 바뀌는 원의 꼭대기·바닥이 내용 밖에 있어야 이음매가 안 보인다.
 */
fun buildRibbon(
    size: Size,
    strokePx: Float,
    turns: Float,
    startAngle: Float = -90f,
    radius: Float = size.height / 2f + strokePx,
    resolution: Int = 500
): RibbonGeometry {
    val path = pathOf(
        ribbonPoints(
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            radius = radius,
            turns = turns,
            startAngle = startAngle
        )
    )
    val measure = PathMeasure().apply { setPath(path, false) }
    val segments = slopeSegments(measure.length, resolution) { measure.getTangent(it).y }
    return RibbonGeometry(path, measure, segments)
}

/** 깊이를 나누는 방법 */
enum class RibbonSplit {
    /** 나누지 않고 내용 위에 통째로 */
    None,

    /** 원문 — 같은 구간 배열을 phase 0 / phase 첫 구간 대시로 두 번 */
    DashPhase,

    /** getSegment 로 짝수·홀수 구간을 각각 다른 Path 에 담아 두 번 — 구간 길이가 달라도 정확 */
    Segments
}

/** chainPathEffect(outer, inner) 는 inner 를 먼저 적용한다 — 어느 대시를 안쪽에 둘지 */
enum class ChainOrder {
    /** 원문 — 진행률로 먼저 자른 뒤(inner) 깊이로 나눈다(outer) */
    ProgressInner,

    /** 뒤집음 — 깊이로 먼저 나누면 구간마다 따로 된 윤곽선이 생기고, 진행률 대시가 윤곽선마다 처음부터 다시 적용된다 */
    DepthInner
}

/** 진행률 대시 — ON 이 길이 × p, OFF 가 전체 길이라 앞쪽 길이 × p 만 그린다 */
private fun progressDash(length: Float, progress: Float): PathEffect =
    PathEffect.dashPathEffect(floatArrayOf(length * progress, length))

/** 깊이 대시 — phase 0 이면 짝수 구간(뒤), phase = 첫 구간이면 한 칸 밀려 홀수 구간(앞) */
private fun depthDash(segments: FloatArray, front: Boolean): PathEffect =
    PathEffect.dashPathEffect(segments, phase = if (front) segments.first() else 0f)

private fun ribbonStroke(strokePx: Float, pathEffect: PathEffect?) = Stroke(
    width = strokePx,
    cap = StrokeCap.Round,
    join = StrokeJoin.Round,
    pathEffect = pathEffect
)

/**
 * 리본의 앞(front = true) 또는 뒤 한 번을 그린다. 내용 앞뒤로 두 번 불러 깊이를 만든다.
 * 구간이 1개뿐이면(반 바퀴 미만) DashPathEffect 에 intervals 를 1개만 넘기게 되어 예외가 나므로 나누지 않고 뒤에만 그린다.
 */
fun DrawScope.drawRibbonPass(
    ribbon: RibbonGeometry,
    brush: Brush,
    strokePx: Float,
    front: Boolean,
    progress: Float,
    split: RibbonSplit = RibbonSplit.DashPhase,
    chainOrder: ChainOrder = ChainOrder.ProgressInner
) {
    // 원문처럼 0 이면 아예 그리지 않는다 — 길이 0 짜리 ON 대시([0, 길이])를 만들지 않는다
    if (progress <= 0f) return
    val length = ribbon.length
    val canSplit = ribbon.segments.size >= 2
    when {
        split == RibbonSplit.None || !canSplit -> {
            // 나누지 않는 경우: None 은 내용 위에, 구간 1개는 내용 뒤에 통째로
            val drawNow = if (split == RibbonSplit.None) front else !front
            if (drawNow) {
                drawPath(ribbon.path, brush, style = ribbonStroke(strokePx, progressDash(length, progress)))
            }
        }

        split == RibbonSplit.DashPhase -> {
            val depth = depthDash(ribbon.segments, front)
            val progressEffect = progressDash(length, progress)
            val effect = when (chainOrder) {
                ChainOrder.ProgressInner -> PathEffect.chainPathEffect(outer = depth, inner = progressEffect)
                ChainOrder.DepthInner -> PathEffect.chainPathEffect(outer = progressEffect, inner = depth)
            }
            drawPath(ribbon.path, brush, style = ribbonStroke(strokePx, effect))
        }

        else -> {
            // getSegment: 이번 쪽(짝수 = 뒤, 홀수 = 앞) 구간만 진행률 지점까지 잘라 한 Path 에 모은다
            val limit = length * progress
            val target = Path()
            var start = 0f
            ribbon.segments.forEachIndexed { index, segment ->
                val end = start + segment
                if (index % 2 == (if (front) 1 else 0) && start < limit) {
                    ribbon.measure.getSegment(start, minOf(end, limit), target, startWithMoveTo = true)
                }
                start = end
            }
            drawPath(target, brush, style = ribbonStroke(strokePx, null))
        }
    }
}

/**
 * 컴포저블을 감아 도는 리본 — 원문(sinasamaki)과 같은 방식이되 progress 를 그리기 단계에서 읽는 람다로 받는다.
 * 경로·PathMeasure·구간 배열은 크기가 바뀔 때만 다시 만들고, 진행률이 바뀌면 다시 그리기만 한다.
 */
fun Modifier.ribbon(
    brush: Brush,
    stroke: Dp = 4.dp,
    turns: Float = 7f,
    progress: () -> Float = { 1f },
    onCacheBuilt: (() -> Unit)? = null,
    onDrawn: (() -> Unit)? = null
): Modifier = drawWithCache {
    val strokePx = stroke.toPx()
    val ribbon = buildRibbon(size, strokePx, turns)
    onCacheBuilt?.invoke()
    onDrawWithContent {
        val p = progress().coerceIn(0f, 1f)
        drawRibbonPass(ribbon, brush, strokePx, front = false, progress = p)
        drawContent()
        drawRibbonPass(ribbon, brush, strokePx, front = true, progress = p)
        onDrawn?.invoke()
    }
}

/**
 * 원문 시그니처 그대로 progress 를 값으로 받는 판(대조용).
 * 진행률이 바뀔 때마다 호출한 컴포저블이 리컴포즈되고 Modifier 가 새 람다로 다시 만들어진다 —
 * drawWithCache 는 람다를 참조로 비교하므로(DrawWithCacheElement.equals) 그때마다 캐시를 버리고 경로를 처음부터 만든다.
 */
fun Modifier.ribbonByValue(
    brush: Brush,
    progress: Float,
    stroke: Dp = 4.dp,
    turns: Float = 7f,
    onCacheBuilt: (() -> Unit)? = null,
    onDrawn: (() -> Unit)? = null
): Modifier = drawWithCache {
    val strokePx = stroke.toPx()
    val ribbon = buildRibbon(size, strokePx, turns)
    onCacheBuilt?.invoke()
    onDrawWithContent {
        drawRibbonPass(ribbon, brush, strokePx, front = false, progress = progress)
        drawContent()
        drawRibbonPass(ribbon, brush, strokePx, front = true, progress = progress)
        onDrawn?.invoke()
    }
}

/** 화면 카드용 — 깊이 분할 방식·chain 순서·시작 각·반지름을 바꿔 가며 비교한다 */
fun Modifier.ribbonVariant(
    brush: Brush,
    stroke: Dp,
    turns: Float,
    startAngle: Float = -90f,
    radiusScale: Float? = null,
    split: RibbonSplit = RibbonSplit.DashPhase,
    chainOrder: ChainOrder = ChainOrder.ProgressInner,
    progress: () -> Float = { 1f }
): Modifier = drawWithCache {
    val strokePx = stroke.toPx()
    val radius = radiusScale?.let { size.height * it } ?: (size.height / 2f + strokePx)
    val ribbon = buildRibbon(size, strokePx, turns, startAngle, radius)
    onDrawWithContent {
        val p = progress().coerceIn(0f, 1f)
        drawRibbonPass(ribbon, brush, strokePx, front = false, progress = p, split = split, chainOrder = chainOrder)
        drawContent()
        drawRibbonPass(ribbon, brush, strokePx, front = true, progress = p, split = split, chainOrder = chainOrder)
    }
}
