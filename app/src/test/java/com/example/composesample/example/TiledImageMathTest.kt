package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.media.image.SrcRect
import com.example.composesample.presentation.example.component.ui.media.image.TILE_PX
import com.example.composesample.presentation.example.component.ui.media.image.argbBytes
import com.example.composesample.presentation.example.component.ui.media.image.sampleSizeFor
import com.example.composesample.presentation.example.component.ui.media.image.viewportFor
import com.example.composesample.presentation.example.component.ui.media.image.visibleTiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TiledImageDeepZoomExample 의 타일 계산 검증 — 8000×6000 원본, 1080×945 뷰어 기준.
 * given : 화면 배율·이미지 원점 위치
 * when : inSampleSize·보이는 영역·보이는 타일을 계산한다
 * then : 화면보다 성기게 디코딩하지 않고, 보이는 영역을 빈틈없이 덮고, 가장자리는 이미지 경계에서 자른다
 */
class TiledImageMathTest {

    private val w = 8000
    private val h = 6000

    @Test
    fun `inSampleSize 는 1 나누기 배율을 넘지 않는 가장 큰 2의 거듭제곱이다`() {
        assertEquals(1, sampleSizeFor(2f))
        assertEquals(1, sampleSizeFor(1f))
        assertEquals(1, sampleSizeFor(0.6f))
        assertEquals(2, sampleSizeFor(0.5f))
        assertEquals(2, sampleSizeFor(0.3f))
        assertEquals(4, sampleSizeFor(0.25f))
        assertEquals(4, sampleSizeFor(1080f / 8000f)) // 1/0.135 = 7.4 → 8 은 화면보다 성기다
    }

    @Test
    fun `1대1 가운데에서는 화면에 걸친 타일만 원본 해상도로 덮는다`() {
        // Given: 원본 (4000, 3000) 을 1080×945 화면 가운데에
        val offsetX = 540f - 4000f
        val offsetY = 472.5f - 3000f

        // When
        val viewport = viewportFor(offsetX, offsetY, 1f, 1080, 945, w, h)
        val tiles = visibleTiles(viewport, w, h, sample = 1)

        // Then: x 3460~4540 → 열 6~8, y 2527~3473 → 행 4~6 = 9장, 전부 512px
        assertEquals(SrcRect(3460, 2527, 4540, 3473), viewport)
        assertEquals(9, tiles.size)
        assertEquals(listOf(6, 7, 8), tiles.map { it.key.col }.distinct())
        assertEquals(listOf(4, 5, 6), tiles.map { it.key.row }.distinct())
        assertTrue(tiles.all { it.decodedWidth == TILE_PX && it.decodedHeight == TILE_PX })
    }

    @Test
    fun `가장자리 타일은 이미지 경계에서 자르고 영역 밖은 요청하지 않는다`() {
        // Given: 오른쪽 아래 끝에 맞춘 1:1
        val viewport = viewportFor(1080f - w, 945f - h, 1f, 1080, 945, w, h)

        // When
        val tiles = visibleTiles(viewport, w, h, sample = 1)

        // Then: 마지막 열 15(7680~8000, 320px)·마지막 행 11(5632~6000, 368px)
        assertEquals(SrcRect(6920, 5055, 8000, 6000), viewport)
        val last = tiles.last()
        assertEquals(15, last.key.col)
        assertEquals(11, last.key.row)
        assertEquals(SrcRect(7680, 5632, 8000, 6000), last.rect)
        assertEquals(320, last.decodedWidth)
        assertEquals(368, last.decodedHeight)
    }

    @Test
    fun `전체 보기는 inSampleSize 4 의 타일 12장이면 된다`() {
        // Given: 폭에 맞춘 전체 보기(배율 0.135)
        val scale = 1080f / w
        val viewport = viewportFor(0f, (945f - h * scale) / 2f, scale, 1080, 945, w, h)

        // When
        val tiles = visibleTiles(viewport, w, h, sampleSizeFor(scale))

        // Then: 타일 한 칸이 원본 2048px → 4×3, 디코딩 크기 합은 2000×1500
        assertEquals(SrcRect(0, 0, w, h), viewport)
        assertEquals(12, tiles.size)
        assertEquals(2000, tiles.filter { it.key.row == 0 }.sumOf { it.decodedWidth })
        assertEquals(1500, tiles.filter { it.key.col == 0 }.sumOf { it.decodedHeight })
    }

    @Test
    fun `한 번에 디코딩하면 ARGB 로 183MB, inSampleSize 8 이면 64분의 1`() {
        assertEquals(192_000_000L, argbBytes(w, h))
        assertEquals(3_000_000L, argbBytes(w, h, 8))
    }
}
