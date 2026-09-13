package com.example.composesample.presentation.example.component.ui.graphics

/**
 * Graphics 예제 참고 자료
 *
 * ## NewShadowApiExampleUI (향상된 Shadow 효과)
 * - 별도 외부 출처 없음(Kotlin 2.4.0 + Compose 1.11.1(ComposeBom 2026.05.00) 환경 자체 정리 노트)
 *
 * ### 핵심 개념
 * - `Modifier.shadow()`의 ambientColor/spotColor로 elevation 기반 그림자에 색상 제어 추가, clip=false로 경계 밖 확장 허용
 * - `drawBehind` + `inset()`으로 인너 섀도우·뉴모피즘(밝은/어두운 이중 그림자) 등 커스텀 그림자 직접 구현
 * - 실시간 속성 제어: radius(elevation 0~30dp)/spread(추가 레이어)/offset/alpha를 슬라이더로 조절
 * - animateFloatAsState + spring()으로 터치 시 elevation·alpha·scale 동시 애니메이션
 * - 주의: 모디파이어 순서(shadow→background), 그림자 잘림 방지 패딩, 과도한 그림자로 인한 성능 저하 회피
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
*/
