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
 * - 창 블러 문서(setBackgroundBlurRadius·FLAG_BLUR_BEHIND, RenderEffect 와의 차이): https://source.android.com/docs/core/display/window-blurs#app-developers
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
 * - ⚠️ **(2026-11-18 수정) 폴백의 '캡처 + 블러' 는 API 28 이상에서 앱을 끝냈다**(API 30 에뮬 debug · release 재현, 언제부터인지는 확인 안 함):
 *   ① API 28+ 의 `toImageBitmap()` 은 ui-graphics 1.12.1 `LayerSnapshotV28` = `Bitmap.createBitmap(Picture)` 라 **HARDWARE 비트맵**이고,
 *   `scale()` 사본도 HARDWARE 로 남아(플랫폼 createBitmap 이 끝에 `copy(HARDWARE)`) `getPixels` 가 IllegalStateException
 *   ② 축소 배율 1 이면 `scale()` 이 불변 원본을 그대로 돌려줘(같은 크기 · 불변이면 source 반환) `setPixels` 도 IllegalStateException.
 *   → ARGB_8888 사본을 만들고 결과는 새 가변 비트맵에 쓴다. 9/14 추가 때 계측 테스트는 '그려지지 않은 레이어' 실패 경로만 확인했다(위 항목)
 * - 비용(API 30 에뮬, 904×303 캡처, 반지름 6, 총 시간): release(JIT) 축소 8 7~21ms · 축소 1 16~36ms. 픽셀 루프는
 *   `Dispatchers.Default` 에서 돌리고, 계산 object(SoftwareBlur)는 HotSwan 에서 뺐다 — 재작성 아래 debug 는 축소 8 약 170ms ·
 *   축소 1 약 9초였고 제외 뒤 16~82ms · 20~29ms. 이 크기에선 캡처 · 사본 비용이 커서 축소 배율의 효과가 총 시간에 작게 보인다
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
 *
 * ## Pixelate 모자이크 Modifier (PixelateExampleUI · PixelateModifier.kt)
 * - 원문(Joe Birch, Android Weekly #748): https://joebirch.co/android/pixelating-composables-in-compose-multiplatform/
 * - 라이브러리(Apache 2.0 · Maven 미배포 — Pixelate.kt 파일 복사형, 채택이 아니라 같은 그리기를 직접 구현): https://github.com/hitherejoe/Pixelate
 * - drawWithCache 요소·캐시 무효화 소스(DrawModifier.kt): https://github.com/androidx/androidx/blob/androidx-main/compose/ui/ui/src/commonMain/kotlin/androidx/compose/ui/draw/DrawModifier.kt
 * - Modifier.Node 로 사용자 정의 모디파이어 만들기: https://developer.android.com/develop/ui/compose/custom-modifiers
 * - Modifier.blur(API 31+): https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/package-summary
 *
 * ### 핵심 개념 (원본과 같은 순서)
 * - 셀 한 변 = 내용 **높이** × blockRatio(기본 0.22), 4dp~14dp 로 자름 → 같은 글꼴 크기의 "$12" 와 "$134,502.88" 이 같은 블록.
 *   원문 예시 40×200 → 셀 8.8 · 23×5(단위 테스트 PixelateGridTest 로 대조)
 * - drawWithCache 에서 셀 1개 = 픽셀 1개인 coarse 비트맵과 2배 fine 비트맵을 만들고, 매 프레임
 *   ① 투명색 BlendMode.Src 로 coarse 지우기 → scale(pivot = Offset.Zero) 로 줄여 drawContent ② fine 에 FilterQuality.Low 보간
 *   ③ 원래 크기로 FilterQuality.None 확대 ④ saveLayer + 네 변 그라데이션 BlendMode.DstIn 으로 셀 한 변 폭 페더
 * - 그리기만 바꾸므로 레이아웃은 그대로 — 가리기/공개를 3번 오가도 onSizeChanged 는 처음 1회뿐(크기 변화 없음)
 *
 * ### 실측 (SM-A725F · Android 13 · debug·release 동일, 2026-10-15)
 * - blockRatio(28sp 가격, 464×112px): 0.08 → 셀 4dp(min), 그대로 읽힘 · 0.14 → 5.6dp, 숫자 식별 가능 · 0.18 → 7.2dp · 0.22 → 8.8dp 판독 불가
 *   (README 의 "0.14 숫자 식별 · 0.08 읽힘"과 같음 → MinimumSafeBlockRatio 0.18)
 * - Text 마다 걸면 줄마다 셀이 다르다(28sp 줄 7.2dp · 12sp 줄 min 4dp), 부모 Column 하나에 걸면 묶음 높이로 10.2dp 한 격자
 * - **함정 ① 지우기 생략**: 값을 3번 바꾼 뒤 평균 어둡기 지움 26.7~27.4 vs 생략 66.1~67.7 — 옛 값이 누적돼 비친다.
 *   단, 비트맵이 재사용될 때만 생긴다 → 예제는 remember 로 요소를 고정해 재현(아래 함정 ③ 때문에 그냥 부르면 매번 새 비트맵이라 안 보인다)
 * - **함정 ② saveLayer 없는 DstIn**: 노란 배경·카드까지 알파가 깎여 가장자리가 **투명이 아니라 검은 띠**로 보인다 — 별도 레이어가 없는
 *   노드는 창 버퍼에 바로 그려지기 때문(A72·API 30 에뮬레이터 동일)
 * - **함정 ③ 리컴포즈마다 비트맵 재할당**(원본에도 있음): ui 1.12.1 의 DrawWithCacheElement.equals 는 람다 동일성(!==),
 *   update 는 block 을 바꾸며 invalidateDrawCache. Modifier.pixelate() 는 부를 때마다 새 람다라 호출부가 리컴포즈될 때마다
 *   캐시 블록(비트맵 두 장)이 다시 돈다. onMeasureResultChanged 경로는 크기가 바뀔 때만이라 같은 크기 값 변경은 무관.
 *   매 프레임 값 바꾸기(release AOT, 60회/초): 그냥 부름 할당 매 프레임(멈춰도 0.5초 집계 리컴포즈마다 +1) · remember 와
 *   Modifier.Node(data class 요소) 는 글꼴 크기를 바꿀 때만 +1
 * - 그리기 시간(release AOT · 매 프레임): drawContent 만 0.12ms · pixelate 그냥 부름 0.55ms · remember 0.43ms · Node 0.40ms.
 *   debug 는 HotSwan 재작성 아래 pixelate 5.5ms·그대로 0.33ms → PixelateModifierKt·PixelateNode·PixelBuffers 제외 후 0.8~1.0ms·0.17ms
 *   (app/build.gradle, ComposeHotReloadGuide.md)
 * - 시맨틱: pixelate 만 걸면 uiautomator 덤프에 text="₩7,031,962" 노드가 그대로, clearAndSetSemantics { contentDescription } 이면
 *   텍스트 노드 없이 content-desc 만. 어느 쪽이든 보안 경계는 아니다(README Caveats — 컴포지션·메모리·디버거)
 * - Modifier.blur: API 30 에뮬레이터에서 4dp·12dp 모두 무시돼 값이 또렷이 보이고, A72(API 33)에서도 4dp 는 읽힌다(12dp 는 가려짐).
 *   pixelate 는 API 30 에뮬레이터에서도 같은 모자이크
 * - 비용 경고(README): 내용이 다시 그려질 때마다 오프스크린 두 장 + 확대 — 긴 목록의 모든 행에 걸지 않는다
 *
 * ## Gooey 텍스트 모핑 (GooeyTextMorphExampleUI · GooeyMorphLab.kt)
 * - 원문(Juan Silva, Android Weekly #753): https://dev.to/lightscout/how-morph-text-dissolves-one-word-into-the-next-47cl
 * - 원작(Victor Welander 'Gooey Text Morphing' — React · SVG 필터)을 Compose 로 옮긴 글. 라이브러리는 쓰지 않고 같은 그리기를 직접 구현
 * - ColorMatrix 계산식 · 0~255 범위(Android 문서): https://developer.android.com/reference/android/graphics/ColorMatrix
 * - 원문이 든 Compose Multiplatform 오프셋 척도 이슈: https://github.com/JetBrains/compose-multiplatform/issues/3461
 * - RenderEffect(API 31+): https://developer.android.com/reference/android/graphics/RenderEffect
 *
 * ### 핵심 개념 (원문 코드와 같은 순서 · ui 1.12.1 javap)
 * - 두 단어를 한 Box 에 겹치고 Text 마다 `graphicsLayer { alpha; renderEffect = BlurEffect(r, r, TileMode.Decal); clip = false }`,
 *   그 둘을 감싼 부모 하나에 `graphicsLayer { colorFilter = ColorFilter.colorMatrix(알파 행 0,0,0,gain,bias) }`.
 *   `GraphicsLayerScope.colorFilter` 는 인터페이스 기본 메서드지만 ReusableGraphicsLayerScope 가 덮어쓰고, 레이어 세 구현
 *   (GraphicsLayerOwnerLayer · RenderNodeLayer · ViewLayer) 모두 GraphicsLayer 로 넘긴다 — 색 필터가 있는 레이어는 오프스크린에 합성된다
 * - 진행 f 를 떠남 t1 = (1 − f) / (1 − p) · 옴 t2 = (f − p) / (1 − p) 로 나눈다(p = morphSequence / 2, 0.499 상한 — 앞 p 동안 옛 단어 그대로).
 *   알파 t^0.4, 블러 min(blurBase / t − blurBase, blurMax) — t 0.5 → 8dp · 0.25 → 24dp · 1/6 에서 상한 40dp(GooeyMorphLabTest)
 * - 프레임 루프는 `withFrameMillis` 의 dt 로 진행 → 쉼 → 다음 단어. 예제는 진행을 graphicsLayer 블록 안에서만 읽어 무대가 단어가 바뀔 때만
 *   리컴포즈된다(원문은 컴포지션에서 읽음) — API 35 에뮬 12초 +5(데모 페이지 값 주기 2.15초) · 멈춘 채 슬라이더 5번 +0
 * - 조절값: 코드 기본 1s · 0.25s · 8/40dp · 18 · −7 · 0.5 / 데모 페이지 1.36s · 0.79s · 12.19/20.76dp · 3.9 · −38.25 · 7.24(p 0.499)
 *
 * ### 원문 주장 실측 (PixelCopy 로 읽은 화면 픽셀 — Pixel Tablet API 35 에뮬 debug · release 같은 값, Pixel 4 API 30 에뮬 debug)
 * - **② 오프셋 열은 0~255 척도 — 맞다.** 검정 견본(입력 α · gain · bias) 128·1·0 / 128·1·−64 / 128·1·−0.25 / 10·18·−7 / 20·18·−7 의 출력이
 *   소프트웨어 캔버스와 화면 모두 128 · 64 · 128 · 173 · 255(API 30 · 35) = 0~255 예상, −1~1 로 읽었다면 128 · 0 · 64 · 0 · 0.
 *   그래서 18 · −7 은 입력 α ≤ 0.39 → 0 · ≥ 14.6(약 6%) → 255, 3.9 · −38.25 는 9.8 ~ 75.2(약 4~29%) 사이를 편다.
 *   −1~1 척도로 계산한 bias(−38.25 / 255 = −0.15)를 그대로 옮기면 문턱이 사라져 옅은 띠가 남는다 — f 0.40 옅은 픽셀 13,766 → 46,725(3.4배, debug)
 * - **③ 블러는 임계 레이어 경계에서 잘린다 — 맞다.** 안쪽 패딩 48dp(상한 40 + 8)는 f 0~1 21장 모두 레이어 경계 1px 테두리의 잉크 0,
 *   패딩 0 은 f 0.30~0.95 에서 255~450px(위아래가 평평하게 잘림). API 30 은 블러가 없어 둘 다 0
 * - **① '단어마다 따로 임계하면 끝내 붙지 않는다' — 부풀린 주장.** 따로 걸어도 굳은 모양이 맞닿으면 픽셀은 이어진다. 지름 40dp · 블러 12dp
 *   두 점을 0 → 80dp 로 훑으면 한 덩어리 최대 간격이 부모 28 vs 따로 26dp(18 · −7), 16 vs 14dp(3.9 · −38.25) — 부모가 더하는 것은 닿기 전
 *   2dp 의 다리와 6~14px 굵은 목뿐이고, 다리가 끊길 때 가운데 방울이 따로 남는다(18 · −7, 30dp 덩어리 3). 두 단어가 같은 자리에 겹치는
 *   이 모핑에서는 f 0.55 잉크 차 0.2%(18 · −7) · 8%(3.9 · −38.25). API 30 은 블러가 없어 맞닿은 0dp 에서만 이어지고 부모 = 따로
 *
 * ### 함께 본 것
 * - 코드 기본값 18 · −7 은 문턱이 낮아 떠나는 단어의 블러가 거의 끝까지 덩어리로 남아 오는 단어를 덮고, 덩어리 바깥에 블러의 8비트 알파
 *   1~14 단계가 그대로 계단 띠로 보인다(gain 18 이면 한 단계가 출력 18). 데모 페이지 값은 글자가 일찍 드러나고 띠가 얇다 — 원문 페이지가
 *   기본값과 다른 값을 쓰는 이유로 보인다
 * - API 31 미만은 BlurEffect 를 레이어가 무시한다 — 두 단어가 임계를 지나며 툭 바뀌는 크로스페이드(세 겹 카드 ②=①, ③=④)
 * - 측정 비용: 캡처 한 장(약 48만 픽셀)을 훑는 InkAnalysis 는 HotSwan 재작성 아래 debug 단위 테스트 829ms · 재작성 끔 2.1ms(2400×200) →
 *   app/build.gradle 에서 이 객체만 제외(ComposeHotReloadGuide.md). 제외 뒤 간격 41단계 훑기가 약 6초
 * - 측정은 에뮬레이터(swiftshader)뿐 — 실기기 GPU 블러의 띠 모양 · 수치는 확인하지 않았다
 *
 * ## Canvas.drawMesh 리본 (MeshRibbonExampleUI · MeshRibbonLab.kt · MeshRibbonApi34.kt · MeshRibbonDrawing.kt)
 * - 원문(Arda Kazancı, Android Weekly #753 — ADS 'Elevate your apps with the latest graphics features' 정리):
 *   https://ardakazanci.com/posts/new-graphics-features-in-jetpack-compose-and-android/
 * - Canvas.drawMesh(blendMode 의미 · 안티에일리어싱 없음): https://developer.android.com/reference/android/graphics/Canvas#drawMesh(android.graphics.Mesh,%20android.graphics.BlendMode,%20android.graphics.Paint)
 * - Mesh · MeshSpecification(API 34): https://developer.android.com/reference/android/graphics/Mesh
 *   https://developer.android.com/reference/android/graphics/MeshSpecification
 *
 * ### 핵심 개념 (원문 코드와 같은 구조 · android-35 sources)
 * - `MeshSpecification.make(attributes = [corner: float2 @0], stride 8, varyings = [stroke: float3], VERTEX, FRAGMENT)` —
 *   기본 sRGB · premultiplied, 속성 최대 8 · varying 최대 6(position 은 따로). 셰이더 문자열은 한 번만 파싱(지연 싱글턴)
 * - 정점 162개 = 진행 0~1 × 좌우 ±1 의 TRIANGLE_STRIP. 버퍼에는 corner 두 숫자만, 위치 · 굵기 · 파도 · 펄스는 정점 셰이더가
 *   uniform(uStart · uEnd · uHalfWidth · uTime)으로 계산. 프래그먼트는 가운데서 떨어진 거리로 가장자리를 smoothstep(안티에일리어싱이
 *   지원되지 않으므로 직접) · 진행 거리로 색을 섞는다(`layout(color)` uniform 은 setColorUniform)
 * - Mesh 는 drawWithCache(크기가 바뀔 때만)에서 만들고 프레임마다 `setFloatUniform("uTime")` + `nativeCanvas.drawMesh` 한 번.
 *   Compose Canvas 는 drawMesh 를 감싸지 않는다(1.12.1). API 34 타입은 @RequiresApi 객체 안에만, 컴포저블엔 앱 인터페이스(RibbonRenderer)
 * - `drawMesh(mesh, blendMode, paint)`: 메시 색 = dst, Paint 색 = src, null = MODULATE. 하드웨어 가속이 아닌 캔버스는 RuntimeException
 * - Mesh 생성자는 정점 버퍼를 CPU 쪽으로 복사한다(문서 'CPU backed buffer') → 모양을 바꾸려면 다시 만든다, 프레임마다는 uniform 만
 *
 * ### 실측 (Pixel Tablet API 35 에뮬 debug · release 같은 값, Pixel 4 API 30 에뮬은 대체 화면 — 실기기 A72 는 API 33 이라 쓰지 않음)
 * - API 35 swiftshader 에서 drawMesh 가 그려진다. 정점 셰이더와 같은 식의 CPU 거울(노란 점 · 모서리)이 메시 가장자리와 겹친다
 * - 그리기 호출: drawLine 반복 80회 vs drawMesh 1회. CPU 기록 시간(release, 1초 창 6개씩 세 번의 중앙값): 굵기 하나 35~57µs ·
 *   조각마다 굵기 36~63µs · drawMesh 6~8µs(약 5~10배). 창 하나는 GC · 에뮬 스케줄링으로 수백 µs~1.2ms 튀는 표본이 섞인다
 *   (API 30 release drawLine 35 · 94µs). GPU 래스터화 시간은 재지 않았다
 * - 원문 '굵기는 하나뿐'은 drawPath 기준 — drawLine 조각마다 strokeWidth 를 바꾸면 굵기는 달라지지만 Butt 끝이라 꺾이는 곳마다 틈 ·
 *   겹침이 생긴다(폭이 좁은 폰 화면에서는 빗살처럼 갈라진다)
 * - BlendMode(가운데 4px 평균): DST 검정 · 흰 Paint = #81608C(메시 색) · MODULATE 검정 #000000 · 흰 #81608C · SRC 검정 #000000 · 흰 #FFFFFF
 *   → 기본 Paint()(검정)에 null 을 넘기면 리본이 검다(#107 MeshGradient 의 drawVertices 와 같은 축). 원문의 DST 는 Paint 를 버린다
 * - 함정 ① 원문 스니펫은 uTime 만 넣는다 — 그려진 픽셀 0(나머지 uniform 까지 131,074): 방향 0 → normalize NaN, 색 0 = 투명
 * - 함정 ② uniform 이름 오타(`setFloatUniform("uTme")`): release 는 IllegalArgumentException 이지만 메시지의 이름이 깨져 나오고
 *   ('unable to find uniform named P瘱'), debug(디버깅 가능 → CheckJNI)는 `JNI DETECTED ERROR IN APPLICATION: input is not valid
 *   Modified UTF-8: illegal start byte 0x90 … string: 'unable to find uniform named…'` 로 **네이티브 abort — catch 로 못 막고 앱이 죽는다**.
 *   예외 메시지를 만드는 플랫폼 JNI 가 잘못된 문자열을 넘기는 것으로 보인다(API 35 에뮬만 확인). 예제는 그 버튼을 두지 않고 이름을 상수 하나로
 * - 함정 ③ 비트맵 캔버스: `RuntimeException: software rendering doesn't support meshes`(자바 쪽에서 던져 catch 가능)
 * - 함정 ④ 만든 뒤 버퍼를 고쳐도(굵기 0) 그려진 픽셀 131,074 그대로 → 다시 만들면 0
 * - 리본 칸 리컴포지션: 재생 6초 1 → 1(debug · release) — 시간은 그리기 단계에서만 읽는다
 * - HotSwan: 프레임마다 도는 CPU 계산(RibbonMath)과 drawLine 루프(MeshRibbonDrawingKt)가 debug 에서 16~19ms/프레임(화면 27fps) →
 *   둘만 제외(debug 단위 테스트 한 프레임 분량 2,080µs vs 재작성 끔 57µs). 제외 뒤 debug 272 · 418 · 29µs, 34fps
 *
 * ## ProgressiveBlurExampleUI (#149, Compose 1.13 Modifier.blur { } — 점진 블러)
 * - Compose November '26 릴리스 블로그(첫 항목): https://developer.android.com/blog/posts/jetpack-compose-november-2026-release
 * - ui 릴리스 노트(1.13.0-alpha03 도입 · beta01 매개변수 이름 radius → blurRadiusSpec): https://developer.android.com/jetpack/androidx/releases/compose-ui
 * - TJ Dahunsi "Material is the metaphor"(Android Weekly #749 — satin glass 헤더 · 공유 요소 함정):
 *   https://www.tunjid.com/articles/material-is-the-metaphor-pushing-the-limits-of-building-with-material-design-cross-platform-with-jetpack-compose-6acf75b3c23999c5f99f8d5b
 * ### 핵심 개념(ui 1.13.0 · ui-graphics 1.13.0 sources, opt-in 없음)
 * - `Modifier.blur { }` 의 BlurScope: size(DpSize) · blurRadiusSpec · edgeTreatment · alpha. 블록은 실행마다 reset(반경 0 · Rectangle · 1) 뒤 돈다.
 * - BlurRadiusSpec: uniform · vertical/horizontal/linear/radialGradient(두 반경 또는 BlurStop 2~16개) · shader(maxRadius){ 셰이더 알파 = 세기 }.
 *   shader 스펙은 같은지 비교할 수 없어(identity) 블록이 돌 때마다 새 효과 — 셰이더는 한 번 만들어 재사용(KDoc).
 * - API: uniform 은 RenderEffect.createBlurEffect(API 31+). 나머지는 RuntimeShader 패스(API 33+) — API 31~32 는 identity 효과(선명),
 *   31 미만은 Modifier 무시. 뒤를 흐리는 배경 블러가 아니다 — 자기 그림만 흐린다(헤더 밑 유리는 목록에 단다).
 * - BlurNode 는 placeWithLayer 의 레이어 블록으로 돈다: 블록 안 상태 읽기가 바뀌면 renderEffect 만 바꾸고 내용은 다시 기록하지 않는다.
 * - edgeTreatment: Rectangle(기본) = clip + Clamp, Unbounded = 자르지 않음 + Decal(칸 밖으로 번지고 가장자리가 비침), Shape = 그 모양으로 clip + Clamp.
 * - 공유 요소(sharedElement)는 전환 동안 SharedTransitionLayout 오버레이에 그려져 목록의 blur · 헤더 스크림 밖이다.
 *   renderInOverlayDuringTransition = false 면 제자리(목록 안)에 그려 blur 를 받지만 들어오는 목록의 fadeIn 도 함께 받아 초반엔 보이지 않는다.
 * ### 실측(Pixel Tablet API 35 에뮬 debug · release 같은 값, Pixel 4 API 30 에뮬)
 * - 일곱 스펙 r=12dp 블러 전 대비 선명도(PixelCopy): 균일 0.03 · 세로 0.32 · stop 3개 0.33 · 가로 0.31 · 대각선 0.31 · 방사형 0.08 · 셰이더 0.51 → API 30 은 모두 1.00.
 * - 가장자리(16dp): 칸 밖 24dp 띠 번짐 Rectangle 0 · Unbounded 29,872px · Shape 0, 안쪽 3px 띠 밝아짐 Unbounded +97.2 → API 30 은 모두 0.
 * - satin glass(420dp 스크롤 900ms): ① 컴포지션에서 읽기 리컴포지션 11/12 프레임(API 30 54/55) · ② 블록에서 읽기 0, 블러 블록 11(54).
 *   API 35 에뮬은 블러 때문에 13fps(swiftshader).
 * - 헤더 밑으로 돌아오는 5번 칸(블러 끔 · 스크림만 = 1): ① 오버레이 전환 85% 1.14 → 끝난 첫 프레임 0.04(한 번에 1.10), ② 오버레이 안 씀 0.04 내내,
 *   ③ 원문 해법 1.36 → 1.00 → +150ms 0.19 → +800ms 0.04. API 30 은 블러가 없어 ① · ③ 스크림 차(0.47~0.50)만 남는다.
 * - HotSwan: 픽셀 계산(BlurPixelMath)을 메인에서 돌리던 첫 판이 debug 에서 ANR 28건 → Dispatchers.Default + 제외(ComposeHotReloadGuide.md).
 */
