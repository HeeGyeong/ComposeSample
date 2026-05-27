package com.example.composesample.presentation.example.component.ui.shader

/**
 * AGSL Shader Live Tuning 예제 참고 자료
 *
 * ## 개요
 * AGSL(Android Graphics Shading Language, SkSL 기반)로 작성한 `RuntimeShader` 를
 * Compose 의 `graphicsLayer { renderEffect = ... }` 에 연결해 실시간으로 튜닝한다.
 *
 * - 출처: https://hotswan.dev/blog/compose-agsl-shader-tuning
 * - 공식 문서: https://developer.android.com/develop/ui/views/graphics/agsl
 * - RuntimeShader: https://developer.android.com/reference/android/graphics/RuntimeShader
 *
 * ## 핵심 API (모두 API 33+ / Android 13 TIRAMISU)
 * - `android.graphics.RuntimeShader(source)` — AGSL 소스를 컴파일. 잘못된 소스는 생성자에서 예외
 * - `RuntimeShader.setFloatUniform(name, ...)` — uniform float/float2 값 주입.
 *   소스에 없는 uniform 이름을 set 하면 IllegalArgumentException
 * - `RenderEffect.createRuntimeShaderEffect(shader, "contents")` — 셰이더를 RenderEffect 로 래핑.
 *   두 번째 인자는 하위 콘텐츠를 받을 `uniform shader` 의 이름
 * - `android.graphics.RenderEffect.asComposeRenderEffect()` — Compose 의 renderEffect 로 변환
 *
 * ## 패턴 메모
 * - 셰이더 소스 라이브 편집: `remember(shaderSource) { runCatching { RuntimeShader(src) } }`
 *   → 소스 텍스트가 바뀔 때만 재컴파일, 컴파일 에러는 exceptionOrNull().message 로 표시
 * - uniform 실시간 반영: `graphicsLayer { }` 람다에서 상태(time/슬라이더)를 읽고 매 프레임 set →
 *   상태 변화 시 draw 람다가 다시 실행됨. size(graphicsLayer scope) 로 resolution 전달
 * - 애니메이션 시간: `withFrameNanos` 누적 dt(초) × speed 배속
 * - 방어 코드: uniform 이름 불일치로 setFloatUniform 이 던질 수 있으므로 graphicsLayer 내부를
 *   runCatching 으로 감싸고 실패 시 renderEffect = null (원본 콘텐츠 표시)
 *
 * ## 제약 / 분기
 * - minSdk 24 환경이므로 `Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU` 분기 후
 *   미지원 단말은 placeholder UI 표시. 실제 셰이더 코드는 `@RequiresApi(33)` 로 격리
 *
 * ## 기존 예제와의 차별점
 * - GraphicsLayerExample: BlendMode / ColorFilter 위주
 * - 본 예제: AGSL(SkSL) 셰이더 코드 작성 + uniform 전달 + 라이브 재컴파일이 핵심
 */
