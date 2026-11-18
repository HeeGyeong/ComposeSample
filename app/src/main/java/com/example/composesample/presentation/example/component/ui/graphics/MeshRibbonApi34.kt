package com.example.composesample.presentation.example.component.ui.graphics

import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Mesh
import android.graphics.MeshSpecification
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap

/**
 * Canvas.drawMesh 리본 — android.graphics.Mesh · MeshSpecification(API 34)을 이 파일의 @RequiresApi 객체 안에만 둔다.
 * 셰이더는 원문(Arda Kazancı)의 식 그대로: 정점마다 corner(진행 0~1, 좌우 ±1)를 받아 위치 · 굵기를 정하고,
 * 가변 stroke(진행 거리, 가운데서 떨어진 거리, 반지름)를 프래그먼트로 넘겨 가장자리를 부드럽게 · 색을 섞는다.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
internal object MeshRibbonApi34 {

    // uniform 이름은 상수 하나를 셰이더 문자열과 setFloatUniform 이 함께 쓴다 — 이름이 틀리면 플랫폼이 예외 메시지를 만들다
    // 잘못된 문자열을 JNI 에 넘겨, 디버깅 가능 앱(CheckJNI)은 catch 로 못 막는 네이티브 abort 로 죽는다(API 35 에뮬 실측)
    private const val U_START = "uStart"
    private const val U_END = "uEnd"
    private const val U_HALF_WIDTH = "uHalfWidth"
    private const val U_TIME = "uTime"
    private const val U_LENGTH = "uLength"
    private const val U_START_COLOR = "uStartColor"
    private const val U_END_COLOR = "uEndColor"

    private val VERTEX = """
        uniform float2 $U_START;
        uniform float2 $U_END;
        uniform float $U_HALF_WIDTH;
        uniform float $U_TIME;

        Varyings main(const Attributes a) {
            Varyings v;
            float2 dir = $U_END - $U_START;
            float2 normal = normalize(float2(-dir.y, dir.x));
            float along = a.corner.x * length(dir);
            float wave = sin(a.corner.x * 9.424778 - $U_TIME * 6.283185) * $U_HALF_WIDTH * 0.9;
            float pulseDist = a.corner.x - fract($U_TIME);
            float radius = $U_HALF_WIDTH * (0.65 + 1.15 * exp(-(pulseDist * pulseDist) / 0.025));
            float across = a.corner.y * (radius + 1.5);
            v.position = $U_START + dir * a.corner.x + normal * (wave + across);
            v.stroke = float3(along, across, radius);
            return v;
        }
    """

    private val FRAGMENT = """
        uniform float $U_LENGTH;
        layout(color) uniform float4 $U_START_COLOR;
        layout(color) uniform float4 $U_END_COLOR;

        float2 main(const Varyings v, out float4 color) {
            float dist = abs(v.stroke.y);
            float coverage = 1.0 - smoothstep(v.stroke.z - 1.2, v.stroke.z + 1.2, dist);
            float side = dist / max(v.stroke.z, 0.001);
            float t = clamp(v.stroke.x / max($U_LENGTH, 1.0) + 0.35 * side * side, 0.0, 1.0);
            float4 ink = mix($U_START_COLOR, $U_END_COLOR, t);
            color = ink * coverage;
            return v.position;
        }
    """

    /** 셰이더 문자열은 한 번만 파싱한다(원문 규칙 2 — 지연 싱글턴) */
    val specification: MeshSpecification by lazy(LazyThreadSafetyMode.NONE) {
        MeshSpecification.make(
            arrayOf(MeshSpecification.Attribute(MeshSpecification.TYPE_FLOAT2, 0, "corner")),
            RibbonMath.VertexStrideBytes,
            arrayOf(MeshSpecification.Varying(MeshSpecification.TYPE_FLOAT3, "stroke")),
            VERTEX,
            FRAGMENT
        )
    }

    /**
     * @param allUniforms false 면 원문 스니펫처럼 uTime 만 넣는다 — 나머지 uniform 은 0 으로 남는다
     */
    fun create(
        width: Float,
        height: Float,
        geometry: RibbonGeometry,
        startColor: Int,
        endColor: Int,
        allUniforms: Boolean = true
    ): RibbonRenderer = MeshRibbonRenderer(width, height, geometry, startColor, endColor, allUniforms)

    /** 하드웨어 가속이 아닌 캔버스(비트맵) — BaseCanvas.drawMesh: "software rendering doesn't support meshes" */
    fun probeSoftwareCanvas(width: Float, height: Float, geometry: RibbonGeometry): String {
        val bitmap = createBitmap(64, 32)
        val renderer = create(width, height, geometry, 0xFF1E88E5.toInt(), 0xFFE53935.toInt())
        return try {
            renderer.draw(Canvas(bitmap), 0.25f)
            "예외 없음"
        } catch (e: RuntimeException) {
            "${e.javaClass.simpleName}: ${e.message}"
        } finally {
            bitmap.recycle()
        }
    }

    private fun buildMesh(width: Float, height: Float, buffer: java.nio.ByteBuffer): Mesh {
        buffer.position(0)
        return Mesh(
            specification,
            Mesh.TRIANGLE_STRIP,
            buffer,
            RibbonMath.vertexCount(),
            RectF(0f, 0f, width, height)
        )
    }

    private class MeshRibbonRenderer(
        private val width: Float,
        private val height: Float,
        private val geometry: RibbonGeometry,
        private val startColor: Int,
        private val endColor: Int,
        private val allUniforms: Boolean
    ) : RibbonRenderer {
        private val buffer = RibbonMath.cornerBuffer()
        private val paint = Paint()
        private var mesh: Mesh = build()

        private fun build(): Mesh = buildMesh(width, height, buffer).apply {
            if (allUniforms) {
                setFloatUniform(U_START, geometry.startX, geometry.startY)
                setFloatUniform(U_END, geometry.endX, geometry.endY)
                setFloatUniform(U_HALF_WIDTH, geometry.halfWidth)
                setFloatUniform(U_LENGTH, geometry.length)
                setColorUniform(U_START_COLOR, startColor)
                setColorUniform(U_END_COLOR, endColor)
            }
        }

        override fun draw(canvas: Canvas, time: Float, blend: RibbonBlend, paintColor: Int) {
            mesh.setFloatUniform(U_TIME, time)
            paint.color = paintColor
            val mode = when (blend) {
                RibbonBlend.Dst -> BlendMode.DST
                RibbonBlend.Modulate -> null
                RibbonBlend.Src -> BlendMode.SRC
            }
            canvas.drawMesh(mesh, mode, paint)
        }

        override fun flattenBufferAfterBuild() {
            val floats = buffer.asFloatBuffer()
            for (v in 0 until RibbonMath.vertexCount()) floats.put(v * RibbonMath.FloatsPerVertex + 1, 0f)
        }

        override fun rebuild() {
            mesh = build()
        }
    }
}
