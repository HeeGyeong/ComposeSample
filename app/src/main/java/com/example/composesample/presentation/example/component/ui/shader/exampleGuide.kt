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

/**
 * 스크래치 아트 예제 참고 자료 (ScratchFoilExampleUI · ScratchFoilLab.kt)
 *
 * ## 개요
 * 매 프레임 바뀌는 셰이더 배경 위에서 '긁기'를 하려면 획이 색을 들 수 없다 → 배경을 통째로 그리고,
 * 그 위 검은 판을 오프스크린 레이어에 그려 획마다 `BlendMode.Clear` 로 구멍을 낸다.
 * 배경은 `ShaderBrush` 하위 클래스가 AGSL `RuntimeShader` 를 돌려주고, 기울기는 uniform 하나로 넘긴다.
 *
 * - 출처: Manuel Mato, "How a holographic foil effect works in Jetpack Compose"(ProAndroidDev, Android Weekly #751)
 *   https://proandroiddev.com/how-a-holographic-foil-effect-works-in-jetpack-compose-9535d621c055
 *   (셰이더 전문은 gist 임베드라 확보하지 못했다 — 본문 코드 조각과 설명으로 대조)
 * - ShaderBrush: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/ShaderBrush
 * - BlendMode: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/BlendMode
 * - CompositingStrategy: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/CompositingStrategy
 * - 움직임 센서(TYPE_GAME_ROTATION_VECTOR): https://developer.android.com/develop/sensors-and-location/sensors/sensors_motion
 * - Compose 단계(그리기 단계에서 상태 읽기): https://developer.android.com/develop/ui/compose/phases
 *
 * ## 원문과의 대조
 * - 원문 셰이더(약 100줄)는 네 겹(fbm 도메인 워핑 굴곡 · 박막 간섭 색 · 빛 띠 · 글리터)이다. 이 예제는 박막 간섭 색
 *   (두께 0.45~1.95, RGB = 650·530·450nm 비율의 cos)과 빛 띠(screen 합성)만 짧게 옮겼다 — 셰이더 수학은 범위 밖.
 * - 원문의 "캔버스 크기가 같은 동안 같은 셰이더를 쓰는 ShaderBrush" 는 **ShaderBrush 의 기본 동작**이다.
 *   ui-graphics 1.12.1 `ShaderBrush.applyTo` 가 `createdSize != size` 일 때만 `createShader(size)` 를 부른다.
 *   하위 클래스가 할 일은 RuntimeShader 를 필드로 들고 createShader 에서 해상도만 넣어 같은 객체를 돌려주는 것.
 * - 원문 필터 두 줄(`smoothed += (target - smoothed) * alpha` · 보이지 않는 변화면 null)에 더해, 기울기를 위쪽 벡터의
 *   x · y 성분이 아니라 **중립 자세에 수직인 두 축**으로 잰다. 세워 든 기기(위쪽 = 화면 y)를 앞뒤로 θ 기울이면
 *   y 성분은 1 - cosθ(10° 에 0.015)만 바뀌어 거의 반응하지 않는다(단위 테스트로 고정).
 * - API 33 미만: 원문은 움직이는 그라데이션 흉내를 '파스텔이고 기울기에 거의 반응 안 함'으로 뺐다 → 정적 그라데이션.
 *
 * ## 측정 (Pixel Tablet API 35 에뮬레이터 debug 3회 · release 1회(디버그 키 서명) — 결과 PROBE 고유 12줄 동일)
 * - 보드: 긁기 · 기울기 모두 리컴포지션 1(첫 컴포지션)에서 늘지 않음, createShader 1. 누운 자세에서 센서 이벤트
 *   918 개를 받는 동안 내보냄 0 → 그리기도 그대로. 가속도 (4.9, 0, 8.5) 로 30° 기울이면 기울기 (1.0, 0.0)(= 2 × sin30°)
 * - 레이어 비교(같은 배경 · 판 · 획, 세로선 위 픽셀): saveLayer (230,180,246) · Offscreen (229,177,244) = 배경 색,
 *   **레이어 없음 (0,0,0) 검정** — 상자에 clip(graphicsLayer)이 있어도 막지 못한다(graphicsLayer 는 그리기 목록이지
 *   오프스크린 버퍼가 아니다). ui/graphics 의 Pixelate 함정 ②(레이어 없는 DstIn → 검은 띠)와 같은 원리
 * - ShaderBrush 수명: remember 한 브러시는 리컴포즈 10회에 createShader 0 · 컴파일 0 · 그리기 0, 크기 바꾸기에
 *   createShader 1 · 컴파일 0. 매번 새 브러시는 리컴포즈 10회에 createShader 10 · **셰이더 컴파일 10** · 그리기 10
 * - 그리기는 레이어 단위: '리컴포즈 n' 글자를 미리보기 상자(clip) **안**에 두었을 때는 remember 한 브러시도
 *   그리기 10 이었다(createShader 0). 같은 레이어 안의 글자가 바뀌면 drawBehind 도 다시 돈다 → 글자를 밖으로 뺐다
 * - 기울기 읽는 위치(8자 궤적 120 프레임): drawBehind 안 → 리컴포지션 0 · 그리기 120, 컴포지션 → 120 · 120
 * - 필터 고정 입력(시드 167, 200 개 · 잡음 ±0.003): 탁자 — 필터 없음 200 · 저역 통과만 200 · 저역 통과 + 문턱 1
 *   (첫 값=중립에도 잡음이 있어 한 번 넘는다). 20° 기울인 뒤 멈춤 — 200 · 200 · 101, 멈춘 동안 100 · 100 · 7
 * - 수명: 화면이 보이는 동안 sensorservice 활성 연결 1(TiltSensorHub), 홈으로 나가면 0(LifecycleResumeEffect 의
 *   onPauseOrDispose). 시스템 애니메이션 꺼짐(animator_duration_scale 0)이면 연결 0 · 받음 0 · 8자 재생 안 함
 * - 측정 중 발견한 화면 버그: 8자 재생 중 모드 칩을 누르면 두 방식의 횟수가 섞였다(에뮬레이터 소프트웨어 렌더링은
 *   120 프레임에 4초 이상) → 측정 중 칩 비활성 + 시작할 때의 모드를 결과에 기록
 * - Pixel 4 API 30 에뮬레이터(정적 그라데이션 폴백): 카드 3 · 4 · 5 의 횟수가 위와 같다(셰이더 컴파일만 0 → '—').
 *   레이어 비교도 같다 — saveLayer · Offscreen (140,84,185) 그라데이션, 레이어 없음 (0,0,0)
 * - 화면 회전: 자동 회전이 켜진 API 30 에서 기울이다 가로로 돌면 보드 크기가 바뀌어 createShader 1 → 3(가로 · 다시 세로)
 *   — ShaderBrush 의 크기별 캐시대로다. BlogExampleActivity 는 회전을 직접 처리해(configChanges) 다시 만들어지지 않고
 *   onResume 도 없다 → 처음엔 화면 회전 값을 onResume 에서만 읽어 가로에서 축이 틀렸다. 지금은 구성(orientation)이
 *   바뀌면 다시 읽고 그 자세를 새 중립으로 잡는다
 * - 축 대응: 처음 작성한 ROTATION_90/270 대응이 서로 뒤바뀌어 있었다(단위 테스트도 같은 틀린 값을 고정하고 있었다).
 *   Display.getRotation 문서(기기를 반시계로 90° → ROTATION_90)대로 '세워 든 기기의 위쪽 = 화면 위쪽' 으로 테스트를
 *   다시 썼고, 기기에서도 가속도 (+g, 0, 0)(오른쪽 변이 위)에 ROTATION_90 이 되는 것을 확인했다. 가로 상태의 기울기
 *   방향까지는 에뮬레이터에서 확인하지 못했다(가속도만 바꾸면 회전 벡터 융합이 몇 초 늦게 따라오고, 가로에서는 보드 카드가
 *   화면 밖이라 LazyColumn 이 해제한다)
 *
 * ## 관련 예제
 * - 자유 곡선 그리기(획 · undo): ui/canvas FreehandDrawingExampleUI
 * - 센서 융합 · 화면 회전에 따른 축 재배치: system/platform/sensor SensorFusionCompassExampleUI
 * - 레이어 없는 BlendMode 의 검은 띠: ui/graphics PixelateExampleUI
 */
