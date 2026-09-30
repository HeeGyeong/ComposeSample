package com.example.composesample.presentation.example.component.ui.graphics

/**
 * Graphics 예제 참고 자료
 *
 * ## NewShadowApiExampleUI (dropShadow / innerShadow — Compose 1.9+)
 * - 공식 문서(Add shadows in Compose): https://developer.android.com/develop/ui/compose/graphics/draw/shadows
 * - API 레퍼런스(androidx.compose.ui.draw): https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/package-summary
 *
 * ### 핵심 개념
 * - `Modifier.dropShadow(shape, Shadow)` / `Modifier.innerShadow(shape, Shadow)` — 프로젝트가 해석하는 ui 1.11.4 바이트코드에서 확인, opt-in 불필요
 * - `Shadow(radius, color 또는 brush, spread, offset: DpOffset, alpha, blendMode)` 하나로 모든 속성을 지정한다
 *   (값을 람다로 주는 블록 오버로드 `dropShadow(shape) { … }` / `innerShadow(shape) { … }` 도 있다 — DropShadowScope/InnerShadowScope)
 * - 순서가 곧 그리는 순서: dropShadow → background(도형 뒤에 깔림) / background → innerShadow(배경 위, 안쪽 가장자리)
 * - `Modifier.shadow(elevation)` 은 시스템 광원 기준 플랫폼 그림자라 spread·offset·blendMode 를 줄 수 없다 → 기본 비교 카드에서 대조
 * - 여러 번 체이닝 가능(먼저 쓴 그림자가 아래에 깔림) → 레이어드 그림자 / brush → 그라디언트 그림자 / 밝은 색 + 큰 radius·spread → 글로우
 * - 뉴모피즘: 배경색 기준 밝은(좌상)·어두운(우하) 그림자 한 쌍 — 바깥(dropShadow ×2)은 볼록, 안쪽(innerShadow ×2)은 오목
 * - 2026-09-15 재작성: 이전 버전은 제목과 달리 11개 카드 모두 `Modifier.shadow()`/`drawBehind` 로 구현돼 새 API 호출이 0건이었다
 *   → 새 API 로 바꾸고 중복 카드(글로우·컬러·방향·키보드 버튼)를 흡수해 7개 카드로 정리
  *
 * ## Dialog Background Blur (다이얼로그 배경 블러)
 * - 공식 문서: https://developer.android.com/develop/ui/views/graphics/blur
 * - API: https://developer.android.com/reference/android/view/Window#setBackgroundBlurRadius(int)
 * 핵심 개념:
 * - "블러"는 서로 다른 세 가지를 가리킨다 — ① `Modifier.blur`(그 컴포저블이 그린 내용, API 31+ 이며 하위에서는 조용히 무시)
 *   ② `Window.setBackgroundBlurRadius`(내 윈도우의 배경 영역, API 31+) ③ `LayoutParams.blurBehindRadius` +
 *   `FLAG_BLUR_BEHIND`(내 윈도우 **뒤의 다른 윈도우**, API 31+). 다이얼로그 뒤를 흐리게 하는 것은 ③이다
 * - Compose `Dialog` 의 윈도우는 `(LocalView.current.parent as? DialogWindowProvider)?.window` 로 얻는다
 *   (`DialogWindowProvider` 는 compose-ui 의 공개 인터페이스). null 체크 필수 — Compose 구현 구조에 기대는 캐스팅이다
 * - **지원 여부는 두 겹이다**: `SDK_INT >= 31` 만으로 부족하고 `WindowManager.isCrossWindowBlurEnabled()` 가 true 여야 한다.
 *   이 값은 런타임에 바뀌므로 `addCrossWindowBlurEnabledListener(Consumer<Boolean>)` 로 구독한다
 *   (배터리 세이버 · 개발자 옵션 · 저사양 기기에서 실제로 꺼진다)
 * - `blurBehindRadius` 만 넣고 `FLAG_BLUR_BEHIND` 를 켜지 않으면 적용되지 않는다. 0 으로 되돌릴 때는 플래그도 함께 내린다
 * - `setBackgroundBlurRadius` 는 윈도우 배경이 **반투명**이어야 보인다. Compose Dialog 의 기본 배경은 투명이라
 *   `setBackgroundDrawable(ColorDrawable(반투명))` 을 깔아 줘야 한다
 * - **실기기 실측(SM-A725F / Android 13, API 33)**: `crossWindowBlurEnabled = false` — API 31+ 기기인데도 시스템이 꺼 뒀다.
 *   `blurBehindRadius=40` · `dimAmount=0.4` · `FLAG_BLUR_BEHIND`/`FLAG_DIM_BEHIND` 가 모두 정상 set 되고 되읽기까지 성공하지만
 *   화면에는 블러가 나오지 않는다 → **"코드는 성공하고 그림만 없다"** 의 실례
 * - 하위(minSdk 24) 폴백: `rememberGraphicsLayer()` 로 배경을 기록(`layer.record { drawContent() }`)한 뒤
 *   `toImageBitmap()` 으로 꺼내 다운스케일 + 박스 블러 2패스를 돌린다. 비용은 픽셀 수에 비례하므로 **반지름보다 축소 배율이 지배적**이다
 * - ⚠️ **그려지지 않은 레이어는 캡처할 수 없다**: `GraphicsLayer` 의 크기는 draw 패스의 `record` 에서 정해지므로,
 *   한 번도 그려지지 않았으면 `size = 0x0` 이고 `toImageBitmap()` 이 `IllegalArgumentException("width & height must be > 0")`
 *   으로 터진다. 계측 테스트(잠금 화면 뒤라 draw 패스가 없는 상태)에서 이 예외를 그대로 확인했다 → 캡처 전에 size 확인 필요
 * - 캡처 폴백의 본질적 한계: **정지 화면**이다(뒤 콘텐츠가 움직여도 갱신되지 않고, 다른 앱의 창은 애초에 캡처 불가).
 *   크로스 윈도우 블러가 플랫폼 API 로 제공되는 이유가 여기에 있다
 *
 * ## Mesh Gradient (MeshGradientPainter — Compose 1.12 신규)
 * - 공식 문서(Mesh gradients): https://developer.android.com/develop/ui/compose/graphics/draw/mesh-gradient
 * - 릴리스 노트(compose-ui 1.12.0-alpha02 Modifier 도입 → alpha03 Painter 로 교체): https://developer.android.com/jetpack/androidx/releases/compose-ui
 * - 하드웨어 가속 지원표(drawVertices = API 29): https://developer.android.com/develop/ui/views/graphics/hardware-accel
 *
 * ### 핵심 개념 (1.12.1 aar javap + 실기기·에뮬레이터 실측)
 * - **공개 진입점은 `MeshGradientPainter(rows, columns, hasBicubicColor = false) { setVertex(...) }` 하나**다.
 *   `MeshGradientConfig`·`MeshGradientRenderer` 는 바이트코드에서는 public 으로 보이지만 Kotlin `internal` 이라
 *   앱에서 쓰면 "Cannot access … it is internal in file" 컴파일 오류가 난다. DrawScope 에서는 `with(painter) { draw(size) }` 로 그린다
 * - rows·columns 는 **패치 수**, 꼭짓점은 (rows+1)×(columns+1) 개. 위치는 0~1 정규화 좌표.
 *   `setVertex(row, column, position, color, leftControlPoint, topControlPoint, rightControlPoint, bottomControlPoint)` 의
 *   제어점 네 개는 꼭짓점 기준 **상대 오프셋**이고 기본값 `Offset.Unspecified`
 * - 제어점 추론 규칙(바이트코드): 가로 방향 = normalize(오른쪽 이웃 − 왼쪽 이웃) × 0.33,
 *   left = −방향 × |왼쪽 이웃 − P|, right = 방향 × |오른쪽 이웃 − P| (세로도 같음, 이웃이 없으면 자기 자신).
 *   같은 규칙으로 계산한 값을 직접 넣은 그림과 생략한 그림이 240×200 전 픽셀 **차이 0**(기본 배치·꼭짓점 이동·직접 지정 혼합 3조건)
 * - Painter 는 **onDraw 마다 블록을 다시 실행**한다(onDraw → configure(block) → renderer.draw). 블록에서 State 를 읽으면
 *   draw 단계에서만 다시 그려진다 — 애니메이션 중 캔버스 리컴포지션 누적 1회 유지(격자 변경 때만 +1) 실측
 * - configure 는 매번 위치 0·색 Transparent 로 초기화한다 → **꼭짓점을 빼먹으면 (0,0) 투명 점**이 되어 예외 없이 모양이 무너진다
 * - `rows/columns ≤ 0` 은 생성자에서, `setVertex` 의 범위 초과는 **그리는 시점**에 IllegalArgumentException
 *   ("row (2) must be in range 0..1") → 화면에 붙인 Painter 라면 draw 단계 크래시
 * - `intrinsicSize = Size.Unspecified` → 크기 modifier 없이 Image 에 넣으면 0×0px(실측)
 * - 색은 **OkLab** 에서 보간한다: 빨강→파랑 1×1 메시 가운데 픽셀 #8C53A2 = `lerp(Red, Blue, 0.5f)` 와 차이 0,
 *   `Brush.horizontalGradient`(sRGB) 는 #7F0080. hasBicubicColor 는 색 기저만 바꾸고(bilinear ↔ Catmull-Rom) 모양은 같다
 * - 렌더러는 패치를 화면에서 약 8px 간격으로 4~64등분(TargetPxPerSegment 8, Min 4, Max 64) → 격자를 늘려도 비용이 비례하지 않는다
 *   (release 호환 모드 3×3 약 25ms vs 5×5 약 26ms)
 *
 * ### ⚠️ 기기에 따라 메시 전체가 검게 나온다 (1.12.1)
 * - 렌더러는 **색을 지정하지 않은 `android.graphics.Paint()`(검정)** 로 `Canvas.drawVertices(TRIANGLES, …, colors, …)` 를 부른다.
 *   1.12.0 릴리스 노트는 "hardware-accelerated Canvas.drawMesh" 를 쓴다고 적었지만, 1.12.1 ui aar 에는 drawMesh 호출이 0건이고
 *   drawVertices(MeshGradientRendererImpl) 만 있다
 * - 플랫폼 drawVertices 를 직접 불러 대조: **Galaxy A72(SM-A725F, Android 13/API 33)** 는 검정 Paint → #000000,
 *   흰 Paint → #7E0081 (꼭짓점 색에 Paint 색을 곱함). **Pixel 에뮬레이터 API 30·34·35** 는 Paint 색과 무관하게 #7E0081
 *   → 영향 받는 기기에서는 MeshGradientPainter 가 화면·소프트웨어 비트맵·GraphicsLayer 캡처 모두 #FF000000
 * - API 33 에뮬레이터 이미지가 없어 "API 33 공통"인지 "제조사 프레임워크"인지는 미확정. API 26 에뮬레이터는 부팅되지 않아 API 29 미만도 미확인
 * - 우회(예제의 호환 모드): `android.graphics.Canvas(bitmap)` 을 상속해 `drawVertices` 만 가로채 Paint 색을 흰색으로 바꾸고,
 *   그 소프트웨어 캔버스에 그린 뒤 `drawImage` 로 옮긴다. 흰색(1.0)을 곱하므로 영향 받는 기기에서도 색이 나오고, 그렇지 않은 기기에서는 결과가 같다
 * - 비용(A72, 3×3 bicubic, 폭 전체·높이 180dp): release 기본 약 7.2ms(60회/초) → 호환 약 25ms(36회/초),
 *   debug 기본 약 29ms → 호환 약 52ms. 정적 그림에는 충분하지만 애니메이션에는 무겁다
 * - 판정은 화면이 뜰 때 소프트웨어 캔버스에 1×1 메시를 그려 가운데 픽셀이 검정인지로 한다(실측상 화면 렌더·레이어 캡처와 결과가 같았다)
 * - 픽셀 비교 검증은 **빈 그림 거짓 통과**에 주의 — 두 그림이 모두 검정이면 차이 0 이 나온다. 예제는 흰 Paint 캔버스로 그리고 색 가짓수를 함께 확인한다
*/
