package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * 다시 기록 범위 카드(#182)의 이웃이 그리는 타일 — 같은 레이어에 있으면 글로우가 무효화될 때마다 이 루프가 다시 돈다.
 * 화면 파일과 나눠 둔 이유: 프레임마다 도는 루프라 HotSwan(debug) 재작성 아래 수백 배 느려져 그리기 시간이 틀린다
 * → 이 파일(RecordScopeDrawingKt)만 app/build.gradle 에서 뺀다(MeshRibbonDrawingKt 와 같은 이유).
 */
internal fun DrawScope.drawNeighborTiles(tiles: Int, color: Color) {
    val columns = ceil(sqrt(tiles.toDouble())).toInt().coerceAtLeast(1)
    val rows = ceil(tiles / columns.toDouble()).toInt().coerceAtLeast(1)
    val cell = Size(size.width / columns, size.height / rows)
    val tile = Size(cell.width * 0.8f, cell.height * 0.8f)
    for (i in 0 until tiles) {
        val column = i % columns
        val row = i / columns
        drawRect(
            color = if ((column + row) % 2 == 0) color else color.copy(alpha = 0.55f),
            topLeft = Offset(column * cell.width + cell.width * 0.1f, row * cell.height + cell.height * 0.1f),
            size = tile
        )
    }
}
