package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.graphics.PixelateDefaultBlockRatio
import com.example.composesample.presentation.example.component.ui.graphics.pixelGrid
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * PixelateExample 의 격자 계산 검증 — 밀도 1(dp = px) 기준, min 4 · max 14.
 * given : 내용 크기와 blockRatio
 * when : pixelGrid 로 셀 한 변과 가로·세로 셀 개수를 구한다
 * then : 원문(Joe Birch) 예시 40×200 은 셀 8.8 · 23×5, 셀은 높이로만 정해지고 min/max 로 잘린다
 */
class PixelateGridTest {

    private val min = 4f
    private val max = 14f

    @Test
    fun `원문 예시 40x200 은 셀 8_8 에 23x5 격자`() {
        val grid = pixelGrid(widthPx = 200f, heightPx = 40f, blockRatio = PixelateDefaultBlockRatio, minCellPx = min, maxCellPx = max)

        assertEquals(8.8f, grid.cellPx, 0.0001f)
        assertEquals(23, grid.columns) // 200 / 8.8 = 22.7 → 23
        assertEquals(5, grid.rows) // 40 / 8.8 = 4.5 → 5 (반올림)
        assertEquals(46, grid.fineColumns)
        assertEquals(10, grid.rows * 2)
    }

    @Test
    fun `높이가 같으면 글자 수와 상관없이 셀 크기가 같다`() {
        val short = pixelGrid(widthPx = 60f, heightPx = 40f, blockRatio = PixelateDefaultBlockRatio, minCellPx = min, maxCellPx = max)
        val long = pixelGrid(widthPx = 260f, heightPx = 40f, blockRatio = PixelateDefaultBlockRatio, minCellPx = min, maxCellPx = max)

        assertEquals(short.cellPx, long.cellPx, 0f)
        assertEquals(7, short.columns) // 60 / 8.8 = 6.8
        assertEquals(30, long.columns) // 260 / 8.8 = 29.5
    }

    @Test
    fun `작은 내용은 min 으로, 큰 내용은 max 로 잘린다`() {
        val small = pixelGrid(widthPx = 50f, heightPx = 12f, blockRatio = PixelateDefaultBlockRatio, minCellPx = min, maxCellPx = max)
        val tall = pixelGrid(widthPx = 300f, heightPx = 120f, blockRatio = PixelateDefaultBlockRatio, minCellPx = min, maxCellPx = max)

        assertEquals(4f, small.cellPx, 0f) // 12 × 0.22 = 2.64 → 4
        assertEquals(3, small.rows)
        assertEquals(14f, tall.cellPx, 0f) // 120 × 0.22 = 26.4 → 14
        assertEquals(9, tall.rows) // 120 / 14 = 8.6
    }

    @Test
    fun `아주 작은 내용도 최소 한 셀`() {
        val grid = pixelGrid(widthPx = 1f, heightPx = 1f, blockRatio = PixelateDefaultBlockRatio, minCellPx = min, maxCellPx = max)

        assertEquals(1, grid.columns)
        assertEquals(1, grid.rows)
    }

    @Test
    fun `blockRatio 가 작을수록 셀이 작아 격자가 촘촘하다`() {
        // 28sp 가격 한 줄(밀도 1 가정, 줄 높이 37px)
        val ratios = listOf(0.08f, 0.14f, 0.18f, 0.22f)
        val cells = ratios.map { pixelGrid(220f, 37f, it, min, max).cellPx }

        listOf(4f, 5.18f, 6.66f, 8.14f).zip(cells).forEach { (expected, actual) ->
            assertEquals(expected, actual, 0.001f) // 0.08 은 2.96 → min 4 로 잘림
        }
    }
}
