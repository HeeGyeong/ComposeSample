package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.shader.declaredAgslUniforms
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * AGSL 셰이더 튜닝 예제의 이름 확인 — 소스에 없는 이름을 setFloatUniform 에 넘기면 debug 빌드가 네이티브 abort 로 죽어서,
 * 넘기기 전에 선언을 읽는다.
 * given : 편집기에 들어올 수 있는 소스(주석 · 배열 · 여러 이름 · layout(color) · 자식 셰이더)
 * when : declaredAgslUniforms
 * then : 주석 안 선언은 빼고 실제 선언 이름만
 */
class AgslSourceNamesTest {

    @Test
    fun `기본 셰이더의 다섯 이름`() {
        val src = """
            uniform float2 resolution;
            uniform float time;
            uniform float noiseScale;
            uniform float colorShift;
            uniform shader contents;
            half4 main(float2 p) { return contents.eval(p); }
        """.trimIndent()
        assertEquals(setOf("resolution", "time", "noiseScale", "colorShift", "contents"), declaredAgslUniforms(src))
    }

    @Test
    fun `이름을 바꾸면 옛 이름은 없다 — 앱이 죽던 경우`() {
        val src = "uniform float2 resolution;uniform float time;uniform float scale;uniform float colorShift;" +
            "uniform shader contents;half4 main(float2 p){return contents.eval(p);}"
        assertEquals(setOf("resolution", "time", "scale", "colorShift", "contents"), declaredAgslUniforms(src))
    }

    @Test
    fun `주석 안의 선언은 선언이 아니다`() {
        val src = "// uniform float time;\n/* uniform float noiseScale;\n uniform float x; */\nuniform float colorShift;"
        assertEquals(setOf("colorShift"), declaredAgslUniforms(src))
    }

    @Test
    fun `여러 이름 · 배열 · layout(color)`() {
        val src = "uniform float a, b[2];\nlayout(color) uniform half4 tint;\nuniform   int   count ;"
        assertEquals(setOf("a", "b", "tint", "count"), declaredAgslUniforms(src))
    }
}
