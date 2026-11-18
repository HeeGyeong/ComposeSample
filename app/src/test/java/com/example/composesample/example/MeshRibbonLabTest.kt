package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.graphics.RibbonGeometry
import com.example.composesample.presentation.example.component.ui.graphics.RibbonMath
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

/**
 * MeshRibbon 예제의 CPU 거울 식 검증 — 원문(Arda Kazancı) 정점 셰이더와 같은 식.
 * given : 80조각 스트립, 반폭 10, (0,50) → (200,50) 수평 리본
 * when : 정점 버퍼 · 반지름 · 파도 · 위치를 계산한다
 * then : 정점 162 · stride 8 · 펄스 자리 1.8배 · 먼 곳 0.65배 · 좌우 정점은 가운데 선에 대칭
 */
class MeshRibbonLabTest {

    private val g = RibbonGeometry(0f, 50f, 200f, 50f, 10f)

    @Test
    fun `원문 값 — 정점 162 · 정점마다 float 두 개 · stride 8바이트`() {
        assertEquals(162, RibbonMath.vertexCount())
        assertEquals(8, RibbonMath.VertexStrideBytes)
        val c = RibbonMath.corners()
        assertEquals(324, c.size)
        assertArrayEquals(floatArrayOf(0f, -1f, 0f, 1f, 1f / 80f, -1f), c.copyOfRange(0, 6), 1e-6f)
        assertArrayEquals(floatArrayOf(1f, -1f, 1f, 1f), c.copyOfRange(320, 324), 0f)
    }

    @Test
    fun `direct 버퍼는 네이티브 순서 · position 0 · 같은 값`() {
        val b = RibbonMath.cornerBuffer()
        assertTrue(b.isDirect)
        assertEquals(0, b.position())
        assertEquals(162 * 8, b.capacity())
        val f = b.asFloatBuffer()
        assertEquals(1f, f.get(3), 0f) // 두 번째 정점의 corner.y
        assertEquals(1f, f.get(320), 0f) // 마지막 쌍의 진행
    }

    @Test
    fun `펄스가 지나는 자리는 1_8배 · 먼 곳은 0_65배`() {
        // 시간 2.3 → fract 0.3 이 펄스 자리
        assertEquals(18f, RibbonMath.radius(0.3f, 2.3f, 10f), 1e-4f)
        assertEquals(6.5f, RibbonMath.radius(0.9f, 2.3f, 10f), 0.01f)
        assertTrue(RibbonMath.radius(0.35f, 2.3f, 10f) < 18f)
    }

    @Test
    fun `fract 는 음수에서도 0 이상`() {
        assertEquals(0.75f, RibbonMath.fract(-0.25f), 1e-6f)
        assertEquals(0.5f, RibbonMath.fract(3.5f), 1e-6f)
    }

    @Test
    fun `좌우 정점은 가운데 선에서 (반지름 + 1_5) 만큼 떨어진다`() {
        val center = FloatArray(2)
        val left = FloatArray(2)
        val right = FloatArray(2)
        val t = 0.3f
        RibbonMath.position(0.5f, 0f, t, g, center)
        RibbonMath.position(0.5f, -1f, t, g, left)
        RibbonMath.position(0.5f, 1f, t, g, right)
        val r = RibbonMath.radius(0.5f, t, g.halfWidth) + 1.5f
        assertEquals(r, hypot(left[0] - center[0], left[1] - center[1]), 1e-3f)
        assertEquals(r, hypot(right[0] - center[0], right[1] - center[1]), 1e-3f)
        // 수평 리본이라 x 는 진행만 따른다
        assertEquals(100f, center[0], 1e-3f)
    }

    @Test
    fun `가운데 선은 파도만큼 움직인다 — t 0 · 진행 0 이면 sin 0 = 0`() {
        val p = FloatArray(2)
        RibbonMath.position(0f, 0f, 0f, g, p)
        assertEquals(0f, p[0], 1e-4f)
        assertEquals(50f, p[1], 1e-4f)
        assertEquals(0f, RibbonMath.wave(0f, 0f, 10f), 1e-6f)
        // 진행 1/6 · t 0 → sin(π/2) = 1 → 반폭 × 0.9
        assertEquals(9f, RibbonMath.wave(1f / 6f, 0f, 10f), 1e-3f)
    }

    @Test
    fun `방향이 0 이면 위치가 NaN — uniform 을 안 넣은 원문 스니펫이 아무것도 안 그리는 이유`() {
        val zero = RibbonGeometry(0f, 0f, 0f, 0f, 0f)
        val p = FloatArray(2)
        RibbonMath.position(0.5f, 1f, 0.3f, zero, p)
        assertTrue(p[0].isNaN() || p[1].isNaN())
    }

    @Test
    fun `스트립 위치 · 가운데 선 · 조각 굵기 개수`() {
        assertEquals(324, RibbonMath.stripPositions(0.3f, g).size)
        assertEquals(162, RibbonMath.centerline(0.3f, g).size)
        assertEquals(2f * RibbonMath.radius(0.5f / 80f, 0.3f, 10f), RibbonMath.segmentWidth(0, 0.3f, 10f), 1e-5f)
    }

    @Test
    fun `그려진 픽셀은 바탕과 한 채널이라도 40 넘게 다른 것`() {
        val white = 0xFFFFFFFF.toInt()
        val pixels = intArrayOf(white, 0xFFF0F0F0.toInt(), 0xFF1E88E5.toInt(), 0xFFFFFF00.toInt())
        // F0 은 15 차이라 아님, 파랑 · 노랑(파랑 채널 0)은 그림
        assertEquals(2, RibbonMath.paintedPixels(pixels, white))
        // 검정 · 흰색 평균 = 255 / 2 → 127(0x7F, 정수 나눗셈)
        assertEquals(0xFF7F7F7F.toInt(), RibbonMath.averageColor(intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())))
    }
}
