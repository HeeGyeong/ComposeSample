package com.example.composesample.presentation.example.component.ui.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp

/**
 * drawMesh 리본 예제에서 프레임마다 도는 CPU 그리기 — drawLine 반복 · API 34 미만 대체 · 정점 표시.
 * 화면 파일과 나눠 둔 이유: 매 프레임 루프라 HotSwan(debug) 재작성 아래 수백 배 느려져 비교 카드의 CPU 시간이 틀린다
 * → 이 파일(MeshRibbonDrawingKt)과 RibbonMath 만 app/build.gradle 에서 뺀다(PixelateModifierKt 와 같은 이유).
 */

/** 가운데 선을 Segments 조각으로 나눠 조각마다 drawLine 하나 — @return 그리기 호출 수 */
internal fun DrawScope.drawLineRibbon(
    time: Float,
    geometry: RibbonGeometry,
    variableWidth: Boolean,
    startColor: Color,
    endColor: Color
): Int {
    val c = RibbonMath.centerline(time, geometry)
    val segments = RibbonMath.Segments
    for (i in 0 until segments) {
        val width = if (variableWidth) RibbonMath.segmentWidth(i, time, geometry.halfWidth) else geometry.halfWidth * 2f
        drawLine(
            color = lerp(startColor, endColor, i / segments.toFloat()),
            start = Offset(c[i * 2], c[i * 2 + 1]),
            end = Offset(c[i * 2 + 2], c[i * 2 + 3]),
            strokeWidth = width,
            cap = StrokeCap.Butt
        )
    }
    return segments
}

/** API 34 미만 대체 — 정점 셰이더와 같은 위치를 CPU 에서 계산해 다각형으로 채운다(색은 진행 방향 그라데이션 근사) */
internal fun DrawScope.drawCpuRibbon(time: Float, geometry: RibbonGeometry, startColor: Color, endColor: Color) {
    val v = RibbonMath.stripPositions(time, geometry)
    val pairs = v.size / 4
    val path = Path()
    path.moveTo(v[0], v[1])
    for (i in 1 until pairs) path.lineTo(v[i * 4], v[i * 4 + 1])
    for (i in pairs - 1 downTo 0) path.lineTo(v[i * 4 + 2], v[i * 4 + 3])
    path.close()
    drawPath(path, Brush.horizontalGradient(listOf(startColor, endColor), startX = geometry.startX, endX = geometry.endX))
}

/** CPU 거울로 계산한 정점 162개와 스트립 삼각형의 모서리 */
internal fun DrawScope.drawRibbonWireframe(time: Float, geometry: RibbonGeometry) {
    val v = RibbonMath.stripPositions(time, geometry)
    val n = v.size / 2
    val edge = Color(0xCC212121)
    for (i in 0 until n - 1) {
        drawLine(edge, Offset(v[i * 2], v[i * 2 + 1]), Offset(v[(i + 1) * 2], v[(i + 1) * 2 + 1]), strokeWidth = 1f)
        if (i + 2 < n) {
            drawLine(edge, Offset(v[i * 2], v[i * 2 + 1]), Offset(v[(i + 2) * 2], v[(i + 2) * 2 + 1]), strokeWidth = 1f)
        }
    }
    for (i in 0 until n) drawCircle(Color(0xFFFFEB3B), radius = 2.5f, center = Offset(v[i * 2], v[i * 2 + 1]))
}
