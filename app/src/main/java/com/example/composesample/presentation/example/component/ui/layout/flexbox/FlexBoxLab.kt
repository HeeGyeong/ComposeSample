package com.example.composesample.presentation.example.component.ui.layout.flexbox

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/*
 * Compose FlexBox(foundation-layout 1.13 정식) 보강(#187) — FlowRow 와 같은 칸을 나란히 놓고 onPlaced 로 읽은 자리로 비교한다.
 * 같은 목록의 'FlexBox Layout Example'(FlexBoxExampleUI)은 util 의 직접 만든 FlexBoxLayout 이다 — 이름만 같다.
 */

/** 칸 하나가 놓인 자리(부모 좌표 px) */
data class PlacedBox(val left: Int, val top: Int, val width: Int, val height: Int) {
    val right: Int get() = left + width
}

/** 칸마다 onPlaced 로 받은 자리 — 같은 값이면 쓰지 않아 다시 그리기 고리를 만들지 않는다 */
@Stable
class PlacementRecorder {
    val boxes = mutableStateMapOf<Int, PlacedBox>()

    fun record(index: Int, box: PlacedBox) {
        if (boxes[index] != box) boxes[index] = box
    }

    /** count 칸이 모두 놓였으면 순서대로, 아니면 null */
    fun snapshot(count: Int): List<PlacedBox>? =
        if ((0 until count).all { boxes.containsKey(it) }) (0 until count).map { boxes.getValue(it) } else null
}

/** 이 칸이 놓인 자리(부모 좌표)를 recorder 의 index 칸에 적는다 */
fun Modifier.recordPlacement(recorder: PlacementRecorder, index: Int): Modifier = this.onPlaced { coordinates ->
    val p = coordinates.positionInParent()
    recorder.record(index, PlacedBox(p.x.roundToInt(), p.y.roundToInt(), coordinates.size.width, coordinates.size.height))
}

/** 줄 — 위쪽 좌표가 같은 칸끼리(±1px), 줄은 위에서 아래 · 줄 안은 왼쪽에서 오른쪽 순 */
fun linesOf(boxes: List<PlacedBox>): List<List<Int>> {
    val lines = ArrayList<MutableList<Int>>()
    val tops = ArrayList<Int>()
    boxes.indices.sortedBy { boxes[it].top }.forEach { i ->
        val at = tops.indexOfFirst { abs(it - boxes[i].top) <= 1 }
        if (at >= 0) lines[at] += i else {
            tops += boxes[i].top
            lines += mutableListOf(i)
        }
    }
    return lines.map { line -> line.sortedBy { boxes[it].left } }
}

/** 배치 요약 — 줄 수 · 자연 폭보다 좁아진 칸 · 컨테이너 오른쪽 밖으로 나간 칸 */
data class LayoutSummary(val lines: Int, val squeezed: List<Int>, val overflow: List<Int>)

fun summarize(boxes: List<PlacedBox>, naturalWidths: List<Int>, containerWidth: Int): LayoutSummary = LayoutSummary(
    lines = linesOf(boxes).size,
    squeezed = boxes.indices.filter { it < naturalWidths.size && boxes[it].width < naturalWidths[it] - 1 },
    overflow = boxes.indices.filter { boxes[it].right > containerWidth + 1 },
)

/** 두 배치가 모든 칸에서 같은 자리 · 크기인가(±tolerance px) */
fun samePlacement(a: List<PlacedBox>, b: List<PlacedBox>, tolerancePx: Int = 1): Boolean =
    a.size == b.size && a.indices.all { i ->
        abs(a[i].left - b[i].left) <= tolerancePx && abs(a[i].top - b[i].top) <= tolerancePx &&
            abs(a[i].width - b[i].width) <= tolerancePx && abs(a[i].height - b[i].height) <= tolerancePx
    }

/** FlexBox grow — basis 합과 gap 을 뺀 남는 공간을 grow 비율로 더한다 */
fun growWidths(basis: List<Float>, grow: List<Float>, container: Float, gap: Float): List<Float> {
    val free = container - basis.sum() - gap * (basis.size - 1)
    val total = grow.sum()
    return basis.indices.map { basis[it] + if (free > 0f && total > 0f) free * grow[it] / total else 0f }
}

/** FlexBox shrink — 넘치는 만큼을 shrink × basis 비율로 뺀다(CSS flex 와 같은 식) */
fun shrinkWidths(basis: List<Float>, shrink: List<Float>, container: Float, gap: Float): List<Float> {
    val overflow = basis.sum() + gap * (basis.size - 1) - container
    val scaled = basis.indices.map { basis[it] * shrink[it] }
    val total = scaled.sum()
    return basis.indices.map { basis[it] - if (overflow > 0f && total > 0f) overflow * scaled[it] / total else 0f }
}

/** FlowRow weight — 고정 칸(null 이 아닌 폭)과 간격을 뺀 나머지를 weight 비율로 나눈다(칸의 기본 크기는 보지 않는다) */
fun weightWidths(fixed: List<Float?>, weights: List<Float>, container: Float, gap: Float): List<Float> {
    val rest = container - fixed.filterNotNull().sum() - gap * (fixed.size - 1)
    val total = weights.sum()
    return fixed.indices.map { fixed[it] ?: (rest * weights[it] / total) }
}

/** 기대 폭과 실제 폭(px)이 모두 tolerance 안인가 */
fun widthsMatch(actualPx: List<Int>, expectedPx: List<Float>, tolerancePx: Float = 1.5f): Boolean =
    actualPx.size == expectedPx.size && actualPx.indices.all { abs(actualPx[it] - expectedPx[it]) <= tolerancePx }

/** 소수 첫째 자리(로캘 무관 · 반올림) */
fun oneDecimalText(value: Float): String {
    val tenths = (value * 10.0).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    val a = abs(tenths)
    return "$sign${a / 10}.${a % 10}"
}

/** 비교 무대 폭 — 360dp 폰 카드 안에도 들어가고 기기마다 같은 조건 */
const val FLEX_DEMO_WIDTH_DP = 280f

/** ② 카드 — FlowRow · FlexBox 호스트의 리컴포지션 · 측정 횟수(상태가 아닌 평범한 필드: 세는 행위가 다시 그리기를 부르지 않게) */
class PassCounter {
    var flowCompositions = 0
    var flowMeasures = 0
    var flexCompositions = 0
    var flexMeasures = 0

    data class Snapshot(val flowCompositions: Int, val flowMeasures: Int, val flexCompositions: Int, val flexMeasures: Int) {
        operator fun minus(other: Snapshot) = Snapshot(
            flowCompositions - other.flowCompositions,
            flowMeasures - other.flowMeasures,
            flexCompositions - other.flexCompositions,
            flexMeasures - other.flexMeasures,
        )
    }

    fun snapshot() = Snapshot(flowCompositions, flowMeasures, flexCompositions, flexMeasures)
}

/** 이 노드가 측정될 때마다 onMeasure — 크기 · 자리는 그대로 */
fun Modifier.countMeasures(onMeasure: () -> Unit): Modifier = this.layout { measurable, constraints ->
    onMeasure()
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}
