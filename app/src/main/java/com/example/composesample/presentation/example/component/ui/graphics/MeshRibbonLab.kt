package com.example.composesample.presentation.example.component.ui.graphics

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.sin

/**
 * drawMesh 리본의 계산 — 원문(Arda Kazancı 'Jetpack Compose 1.13 and Android Graphics')의 정점 셰이더와 **같은 식**을 CPU 로.
 * API 34 미만 대체 화면 · 삼각형 보기 · drawLine 반복 대조가 이 식을 쓰고, JVM 단위 테스트(MeshRibbonLabTest)로 고정한다.
 * android.graphics.Mesh 타입은 여기 두지 않는다 — API 34 타입은 MeshRibbonApi34.kt 의 @RequiresApi 객체 안에만.
 */

/** 리본의 놓임 — 픽셀 단위. 셰이더 uniform uStart · uEnd · uHalfWidth 에 그대로 들어간다 */
internal data class RibbonGeometry(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val halfWidth: Float
) {
    val length: Float get() = hypot(endX - startX, endY - startY)
}

/** drawMesh 의 blendMode — 메시 색이 dst, Paint 색이 src (Canvas.drawMesh 문서, null 이면 MODULATE) */
internal enum class RibbonBlend(val label: String) {
    Dst("DST(원문)"),
    Modulate("null = MODULATE(기본)"),
    Src("SRC")
}

/**
 * 리본을 그리는 쪽. API 34 의 Mesh 를 컴포저블 · 상태에 드러내지 않으려고 앱 인터페이스로 감싼다
 * (상태가 API 34 클래스 타입이면 API 34 미만 release 에서 check-cast 가 해석 실패로 크래시 — RELEASE-API34-CAST-01).
 */
internal interface RibbonRenderer {
    /** 프레임마다 uTime 하나만 넣고 drawMesh 한 번 */
    fun draw(canvas: android.graphics.Canvas, time: Float, blend: RibbonBlend = RibbonBlend.Dst, paintColor: Int = 0xFF000000.toInt())

    /** 메시를 만든 뒤 정점 버퍼만 바꾼다(모든 정점 corner.y = 0 → 굵기 0) — 메시에는 반영되지 않는다(생성 때 복사) */
    fun flattenBufferAfterBuild()

    /** 지금 버퍼로 Mesh 를 다시 만든다 */
    fun rebuild()
}

internal object RibbonMath {
    /** 원문 값 */
    const val Segments = 80
    const val FloatsPerVertex = 2
    const val VertexStrideBytes = FloatsPerVertex * 4

    /** 3π · 2π — 리본 위 파도 1.5 주기가 1초에 한 바퀴 */
    private const val WaveSpatial = 9.424778f
    private const val WaveTemporal = 6.283185f

    fun vertexCount(segments: Int = Segments): Int = (segments + 1) * 2

    /** TRIANGLE_STRIP 정점 버퍼 — 정점마다 corner(x = 0~1 진행, y = −1 · +1 좌우). 위치는 셰이더가 만든다 */
    fun corners(segments: Int = Segments): FloatArray {
        val out = FloatArray(vertexCount(segments) * FloatsPerVertex)
        var k = 0
        for (i in 0..segments) {
            val t = i / segments.toFloat()
            out[k++] = t
            out[k++] = -1f
            out[k++] = t
            out[k++] = 1f
        }
        return out
    }

    /** Mesh 생성자에 넘길 direct 버퍼(네이티브 바이트 순서, position 0) */
    fun cornerBuffer(segments: Int = Segments): ByteBuffer {
        val values = corners(segments)
        val buffer = ByteBuffer.allocateDirect(values.size * 4).order(ByteOrder.nativeOrder())
        buffer.asFloatBuffer().put(values)
        buffer.position(0)
        return buffer
    }

    fun fract(x: Float): Float = x - floor(x)

    /** 펄스가 지나는 자리에서 부풀고(최대 1.8배) 먼 곳은 0.65배 */
    fun radius(cornerX: Float, time: Float, halfWidth: Float): Float {
        val pulse = cornerX - fract(time)
        return halfWidth * (0.65f + 1.15f * exp(-(pulse * pulse) / 0.025f))
    }

    fun wave(cornerX: Float, time: Float, halfWidth: Float): Float =
        sin(cornerX * WaveSpatial - time * WaveTemporal) * halfWidth * 0.9f

    /** 정점 셰이더와 같은 위치 — out[0] = x, out[1] = y */
    fun position(cornerX: Float, cornerY: Float, time: Float, g: RibbonGeometry, out: FloatArray) {
        val dx = g.endX - g.startX
        val dy = g.endY - g.startY
        val len = hypot(dx, dy)
        val nx = if (len == 0f) Float.NaN else -dy / len
        val ny = if (len == 0f) Float.NaN else dx / len
        val across = cornerY * (radius(cornerX, time, g.halfWidth) + 1.5f)
        val offset = wave(cornerX, time, g.halfWidth) + across
        out[0] = g.startX + dx * cornerX + nx * offset
        out[1] = g.startY + dy * cornerX + ny * offset
    }

    /** 스트립 정점 전부의 위치(x, y 교대) — 삼각형 보기 · 대체 화면 */
    fun stripPositions(time: Float, g: RibbonGeometry, segments: Int = Segments): FloatArray {
        val out = FloatArray(vertexCount(segments) * 2)
        val p = FloatArray(2)
        for (i in 0..segments) {
            val t = i / segments.toFloat()
            position(t, -1f, time, g, p)
            out[i * 4] = p[0]
            out[i * 4 + 1] = p[1]
            position(t, 1f, time, g, p)
            out[i * 4 + 2] = p[0]
            out[i * 4 + 3] = p[1]
        }
        return out
    }

    /** 가운데 선(corner.y = 0)의 점 segments + 1 개 — drawLine 반복용 */
    fun centerline(time: Float, g: RibbonGeometry, segments: Int = Segments): FloatArray {
        val out = FloatArray((segments + 1) * 2)
        val p = FloatArray(2)
        for (i in 0..segments) {
            position(i / segments.toFloat(), 0f, time, g, p)
            out[i * 2] = p[0]
            out[i * 2 + 1] = p[1]
        }
        return out
    }

    /** 바탕색과 어느 채널이든 tolerance 넘게 다른 픽셀 수 — 여러 색이 섞인 리본이 '그려졌는가'를 센다 */
    fun paintedPixels(pixels: IntArray, backgroundArgb: Int, tolerance: Int = 40): Int {
        var n = 0
        for (p in pixels) {
            var differs = false
            for (shift in intArrayOf(0, 8, 16)) {
                val a = (p shr shift) and 0xFF
                val b = (backgroundArgb shr shift) and 0xFF
                if (kotlin.math.abs(a - b) > tolerance) {
                    differs = true
                    break
                }
            }
            if (differs) n++
        }
        return n
    }

    /** 사각형 안 픽셀 평균색(불투명 ARGB) */
    fun averageColor(pixels: IntArray): Int {
        if (pixels.isEmpty()) return 0
        var r = 0L
        var g = 0L
        var b = 0L
        for (p in pixels) {
            r += (p shr 16) and 0xFF
            g += (p shr 8) and 0xFF
            b += p and 0xFF
        }
        val n = pixels.size
        return (0xFF shl 24) or ((r / n).toInt() shl 16) or ((g / n).toInt() shl 8) or (b / n).toInt()
    }

    /** 세그먼트 i(점 i → i+1)의 굵기 — 가운데 진행값의 2 × 반지름 */
    fun segmentWidth(i: Int, time: Float, halfWidth: Float, segments: Int = Segments): Float =
        2f * radius((i + 0.5f) / segments, time, halfWidth)
}
