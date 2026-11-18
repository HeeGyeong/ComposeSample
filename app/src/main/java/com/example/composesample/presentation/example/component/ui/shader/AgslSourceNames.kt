package com.example.composesample.presentation.example.component.ui.shader

/**
 * AGSL 소스가 선언한 uniform 이름(주석은 걷어 낸 뒤) — `uniform float a;` · `uniform float a, b;` · `uniform float a[2];` ·
 * `layout(color) uniform half4 c;` · `uniform shader contents;`.
 *
 * 왜 미리 읽는가: RuntimeShader.setFloatUniform · RenderEffect.createRuntimeShaderEffect 에 소스에 없는 이름을 넘기면
 * 플랫폼이 'unable to find uniform named …' 예외 메시지를 만들다 깨진 문자열을 JNI(NewStringUTF)에 넘긴다 —
 * release 는 이름이 깨진 IllegalArgumentException 이지만, 디버깅 가능 앱은 CheckJNI 가 이를 잡아 **네이티브 abort**
 * (catch · runCatching 으로 못 막는다, Pixel Tablet API 35 에뮬 실측). 그래서 이름이 있는지 먼저 확인하고 없으면 부르지 않는다.
 * 타입이 다른 경우(선언 float2 에 값 하나)는 abort 없이 IllegalArgumentException 이라 기존 runCatching 이 받는다.
 */
internal fun declaredAgslUniforms(source: String): Set<String> {
    val noBlockComments = source.replace(Regex("/\\*[\\s\\S]*?\\*/"), " ")
    val code = noBlockComments.replace(Regex("//[^\\n]*"), " ")
    val names = mutableSetOf<String>()
    Regex("\\buniform\\s+[A-Za-z_]\\w*\\s+([^;]+);").findAll(code).forEach { match ->
        match.groupValues[1].split(',').forEach { part ->
            val name = part.substringBefore('[').trim()
            if (name.matches(Regex("[A-Za-z_]\\w*"))) names += name
        }
    }
    return names
}
