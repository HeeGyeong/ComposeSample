package com.example.composesample.presentation.example.component.ui.graphics

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Pixelate 모자이크 Modifier — hitherejoe/Pixelate 의 Pixelate.kt(Apache 2.0, Maven 미배포 · 파일 복사형)를 따라 직접 구현했다.
 * 기본값과 그리는 순서는 원본과 같고, 예제 비교용 스위치(smoothing · clearEachFrame · featherInLayer)와 집계(PixelateStats),
 * 원본과 같은 그리기를 Modifier.Node 로 옮긴 pixelateNode 를 더했다. 출처·실측은 exampleGuide.kt 의 Pixelate 절.
 */

/** 원본 DefaultBlockRatio — 셀 한 변 = 내용 높이 × 0.22 */
internal const val PixelateDefaultBlockRatio = 0.22f

/** 원본 MinimumSafeBlockRatio — 이보다 작으면 글자가 다시 읽히기 시작한다(README: 0.14 숫자 식별, 0.08 읽힘) */
internal const val PixelateMinimumSafeBlockRatio = 0.18f

/** 셀 한 변(px)과 가로·세로 셀 개수 — coarse 비트맵은 셀 1개 = 픽셀 1개, fine 은 그 2배 */
internal data class PixelGrid(val cellPx: Float, val columns: Int, val rows: Int) {
    val fineColumns: Int get() = columns * 2
    val fineRows: Int get() = rows * 2
}

/**
 * 셀 크기를 **높이**로 정한다 — 글자는 높이가 글꼴 크기로만 정해지므로 같은 크기의 "$12" 와 "$134,502.88" 이 같은 블록이 된다.
 * 폭으로 정하면 글자 수에 따라 블록 크기가 달라진다. min/max 는 작은 내용이 한 셀로 뭉개지거나 큰 내용이 몇 개의 큰 사각형이 되는 것을 막는다.
 */
internal fun pixelGrid(
    widthPx: Float,
    heightPx: Float,
    blockRatio: Float,
    minCellPx: Float,
    maxCellPx: Float
): PixelGrid {
    val cell = (heightPx * blockRatio).coerceIn(minCellPx, maxCellPx)
    return PixelGrid(
        cellPx = cell,
        columns = (widthPx / cell).roundToInt().coerceAtLeast(1),
        rows = (heightPx / cell).roundToInt().coerceAtLeast(1)
    )
}

/**
 * 그리기·할당 집계. 그리기 단계에서 스냅샷 상태를 쓰면 매 프레임 재컴포지션을 부르므로 일반 필드로 두고 화면이 주기적으로 읽는다.
 * 그리기는 메인 스레드에서만 일어난다(RenderThread 는 기록된 명령을 재생만 한다).
 */
internal class PixelateStats {
    var draws = 0L
        private set
    var allocations = 0
        private set
    var totalDrawNanos = 0L
        private set

    fun onAllocated() {
        allocations++
    }

    fun onDrawn(nanos: Long) {
        draws++
        totalDrawNanos += nanos
    }

    fun reset() {
        draws = 0
        allocations = 0
        totalDrawNanos = 0
    }
}

/** 한 크기에 묶인 격자와 비트맵 두 장 — 크기가 바뀌면 새로 만든다 */
private class PixelBuffers(val width: Float, val height: Float, val grid: PixelGrid) {
    val coarse = ImageBitmap(grid.columns, grid.rows)
    val fine = ImageBitmap(grid.fineColumns, grid.fineRows)
    val coarseCanvas = Canvas(coarse)
    val fineCanvas = Canvas(fine)
    val coarseSize = Size(grid.columns.toFloat(), grid.rows.toFloat())
    val fineSize = Size(grid.fineColumns.toFloat(), grid.fineRows.toFloat())
    val dstSize = IntSize(width.roundToInt(), height.roundToInt())

    fun matches(size: Size) = size.width == width && size.height == height
}

// CacheDrawScope(drawWithCache)와 DrawScope(노드) 둘 다 Density 라 크기를 따로 받는다
private fun Density.newBuffers(size: Size, minBlockSize: Dp, maxBlockSize: Dp, blockRatio: Float): PixelBuffers =
    PixelBuffers(size.width, size.height, pixelGrid(size.width, size.height, blockRatio, minBlockSize.toPx(), maxBlockSize.toPx()))

/**
 * @param smoothing false 면 coarse 를 곧바로 원래 크기로 키운다(원본에 없는 비교용 — 2배 보간 단계를 빼면 어떻게 보이나)
 * @param clearEachFrame false 면 재사용 비트맵을 지우지 않는다(함정 재현 — 비트맵이 재사용될 때 이전 값이 비친다)
 * @param featherInLayer false 면 saveLayer 없이 DstIn 을 쓴다(함정 재현 — 먼저 그려진 배경까지 알파가 깎인다)
 */
private fun ContentDrawScope.drawPixelated(
    buffers: PixelBuffers,
    featherEdges: Boolean,
    smoothing: Boolean,
    clearEachFrame: Boolean,
    featherInLayer: Boolean
) {
    val grid = buffers.grid
    val contentScope = this

    // 1) 내용을 셀 1개 = 픽셀 1개 크기로 줄여 그린다 — 디테일은 여기서 버려지고 이후 어떤 단계도 되살리지 못한다
    draw(this, layoutDirection, buffers.coarseCanvas, buffers.coarseSize) {
        if (clearEachFrame) {
            drawRect(color = Color.Transparent, blendMode = BlendMode.Src)
        }
        scale(
            scaleX = grid.columns / buffers.width,
            scaleY = grid.rows / buffers.height,
            pivot = Offset.Zero
        ) {
            contentScope.drawContent()
        }
    }

    // 2) 2배 비트맵에 Low 보간으로 옮겨 이웃 블록 사이의 계단을 누그러뜨린다(Src 라 지우기가 필요 없다)
    val upscaled = if (smoothing) {
        draw(this, layoutDirection, buffers.fineCanvas, buffers.fineSize) {
            drawImage(
                image = buffers.coarse,
                srcSize = IntSize(grid.columns, grid.rows),
                dstSize = IntSize(grid.fineColumns, grid.fineRows),
                filterQuality = FilterQuality.Low,
                blendMode = BlendMode.Src
            )
        }
        buffers.fine
    } else {
        buffers.coarse
    }

    // 3) 원래 크기로 None 확대 — 픽셀 하나가 단단한 블록 하나가 된다
    if (!featherEdges) {
        drawImage(
            image = upscaled,
            srcSize = IntSize(upscaled.width, upscaled.height),
            dstSize = buffers.dstSize,
            filterQuality = FilterQuality.None
        )
        return
    }

    // 4) 가장자리 페더 — DstIn 은 '이미 그려진 것'의 알파를 깎는다. 레이어 안이어야 깎이는 대상이 모자이크뿐이다
    if (featherInLayer) {
        drawContext.canvas.saveLayer(Rect(0f, 0f, buffers.width, buffers.height), Paint())
    }
    drawImage(
        image = upscaled,
        srcSize = IntSize(upscaled.width, upscaled.height),
        dstSize = buffers.dstSize,
        filterQuality = FilterQuality.None
    )
    featherEdgesDstIn(buffers.width, buffers.height, feather = grid.cellPx)
    if (featherInLayer) {
        drawContext.canvas.restore()
    }
}

/**
 * 원본과 같은 drawWithCache 구현. ⚠️ 이 함수를 부를 때마다 새 람다 → 새 DrawWithCacheElement 가 만들어지고, 요소 비교가 람다
 * 동일성(!==)이라 호출부가 리컴포즈될 때마다 update → invalidateDrawCache 로 캐시 블록(비트맵 할당)이 다시 돈다(ui 1.12.1 소스).
 * 호출부에서 remember 로 고정하거나 pixelateNode 를 쓴다.
 */
internal fun Modifier.pixelate(
    blockRatio: Float = PixelateDefaultBlockRatio,
    minBlockSize: Dp = 4.dp,
    maxBlockSize: Dp = 14.dp,
    featherEdges: Boolean = true,
    smoothing: Boolean = true,
    clearEachFrame: Boolean = true,
    featherInLayer: Boolean = true,
    stats: PixelateStats? = null
): Modifier = drawWithCache {
    if (size.width < 1f || size.height < 1f) {
        return@drawWithCache onDrawWithContent { drawContent() }
    }

    // 이 블록은 크기가 바뀔 때, 여기서 읽은 상태가 바뀔 때, 그리고 요소가 바뀔 때(위 ⚠️) 다시 돈다
    val buffers = newBuffers(size, minBlockSize, maxBlockSize, blockRatio)
    stats?.onAllocated()

    onDrawWithContent {
        val start = System.nanoTime()
        drawPixelated(buffers, featherEdges, smoothing, clearEachFrame, featherInLayer)
        stats?.onDrawn(System.nanoTime() - start)
    }
}

/**
 * 같은 그리기를 Modifier.Node 로 — 요소가 data class 라 매개변수가 같으면 리컴포지션에서 같다고 판정돼 update 가 불리지 않고,
 * 노드가 비트맵을 들고 있다가 크기나 매개변수가 바뀔 때만 새로 만든다.
 */
internal fun Modifier.pixelateNode(
    blockRatio: Float = PixelateDefaultBlockRatio,
    minBlockSize: Dp = 4.dp,
    maxBlockSize: Dp = 14.dp,
    featherEdges: Boolean = true,
    stats: PixelateStats? = null
): Modifier = this then PixelateElement(blockRatio, minBlockSize, maxBlockSize, featherEdges, stats)

private data class PixelateElement(
    val blockRatio: Float,
    val minBlockSize: Dp,
    val maxBlockSize: Dp,
    val featherEdges: Boolean,
    val stats: PixelateStats?
) : ModifierNodeElement<PixelateNode>() {
    override fun create() = PixelateNode(this)

    override fun update(node: PixelateNode) {
        node.update(this)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "pixelateNode"
        properties["blockRatio"] = blockRatio
        properties["minBlockSize"] = minBlockSize
        properties["maxBlockSize"] = maxBlockSize
        properties["featherEdges"] = featherEdges
    }
}

private class PixelateNode(private var options: PixelateElement) : Modifier.Node(), DrawModifierNode {
    private var buffers: PixelBuffers? = null

    override fun onDetach() {
        buffers = null
    }

    fun update(newOptions: PixelateElement) {
        // data class 비교라 여기까지 왔으면 매개변수가 실제로 바뀐 것 — 격자가 달라질 수 있으니 버리고 다시 그린다
        options = newOptions
        buffers = null
        invalidateDraw()
    }

    override fun ContentDrawScope.draw() {
        if (size.width < 1f || size.height < 1f) {
            drawContent()
            return
        }
        val current = buffers?.takeIf { it.matches(size) }
            ?: newBuffers(size, options.minBlockSize, options.maxBlockSize, options.blockRatio).also {
                buffers = it
                options.stats?.onAllocated()
            }
        val start = System.nanoTime()
        drawPixelated(current, options.featherEdges, smoothing = true, clearEachFrame = true, featherInLayer = true)
        options.stats?.onDrawn(System.nanoTime() - start)
    }
}

/** 네 변에 투명 → 검정 그라데이션을 DstIn 으로 — 원본 Pixelate.kt 와 같은 순서·폭(셀 한 변) */
private fun DrawScope.featherEdgesDstIn(width: Float, height: Float, feather: Float) {
    drawRect(
        brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = 0f, endX = feather),
        size = Size(feather, height),
        blendMode = BlendMode.DstIn
    )
    drawRect(
        brush = Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = width - feather, endX = width),
        topLeft = Offset(width - feather, 0f),
        size = Size(feather, height),
        blendMode = BlendMode.DstIn
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black), startY = 0f, endY = feather),
        size = Size(width, feather),
        blendMode = BlendMode.DstIn
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(Color.Black, Color.Transparent), startY = height - feather, endY = height),
        topLeft = Offset(0f, height - feather),
        size = Size(width, feather),
        blendMode = BlendMode.DstIn
    )
}

/** 비교용 — 픽셀화 없이 그리기만 재서 같은 집계에 넣는다 */
internal fun Modifier.measuredPlainDraw(stats: PixelateStats): Modifier = drawWithContent {
    val start = System.nanoTime()
    drawContent()
    stats.onDrawn(System.nanoTime() - start)
}

/** 파이프라인 카드용 — 원본 위에 셀 격자 선을 그어 내용이 어떻게 나뉘는지 보여 준다 */
internal fun Modifier.pixelGridOverlay(
    blockRatio: Float = PixelateDefaultBlockRatio,
    minBlockSize: Dp = 4.dp,
    maxBlockSize: Dp = 14.dp,
    color: Color = Color(0x99E53935)
): Modifier = drawWithContent {
    drawContent()
    val grid = pixelGrid(size.width, size.height, blockRatio, minBlockSize.toPx(), maxBlockSize.toPx())
    val cellWidth = size.width / grid.columns
    val cellHeight = size.height / grid.rows
    for (c in 1 until grid.columns) {
        val x = c * cellWidth
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
    }
    for (r in 1 until grid.rows) {
        val y = r * cellHeight
        drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
    }
}
