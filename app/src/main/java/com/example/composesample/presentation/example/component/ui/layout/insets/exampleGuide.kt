package com.example.composesample.presentation.example.component.ui.layout.insets

/**
 * UI/Layout/Insets 예제 참고 자료
 *
 * ## DisplayCutoutInsetsExampleUI (디스플레이 컷아웃 & 인셋)
 * - 인셋 공식 가이드: https://developer.android.com/develop/ui/compose/layouts/insets
 * - Edge-to-edge: https://developer.android.com/develop/ui/views/layout/edge-to-edge
 * - DisplayCutout: https://developer.android.com/reference/android/view/DisplayCutout
 * - WindowInsetsCompat: https://developer.android.com/reference/androidx/core/view/WindowInsetsCompat
 * - DisplayShapeCompat(core 1.18.0): https://developer.android.com/reference/androidx/core/view/DisplayShapeCompat
 *
 * 핵심 개념:
 * - **safe* 는 측정값이 아니라 합집합이다**(foundation-layout 1.11.4 WindowInsetsHolder 생성자
 *   바이트코드 확인):
 *   `safeDrawing = systemBars ∪ ime ∪ displayCutout` /
 *   `safeGestures = tappableElement ∪ mandatorySystemGestures ∪ systemGestures ∪ waterfall` /
 *   `safeContent = safeDrawing ∪ safeGestures`.
 *   → **waterfall 은 safeDrawing 에 들어가지 않는다.** 곡면 엣지 대응은 safeGestures/safeContent 쪽이다.
 * - 컷아웃 인셋은 기기의 컷아웃 유무가 아니라 **내 윈도우가 컷아웃 영역까지 확장했는지**로 결정된다.
 *   기기 자체의 컷아웃은 `Display.getCutout()`(API 29+)이 윈도우와 무관하게 알려주고,
 *   창의 확장 여부는 `layoutInDisplayCutoutMode`(API 28+, ALWAYS 는 API 30+)와
 *   `WindowCompat.setDecorFitsSystemWindows(window, false)` 두 축이 정한다.
 *
 * 실측(SM-A725F / Android 13 / API 33, 1080x2400 @450dpi, density 2.8125):
 * - 기기에는 펀치홀 컷아웃이 있다 — `dumpsys window displays` 기준
 *   `DisplayCutout{insets=Rect(0, 86 - 0, 0) ... boundingRect=Rect(514, 0 - 566, 86)}`,
 *   waterfall 0. 액티비티는 `mAppBounds=Rect(0, 86 - 1080, 2265)` 로 컷아웃 **아래**에서 시작한다.
 * - 그 상태(`decorFitsSystemWindows=true`)에서 컴포즈가 읽는 인셋은 **13종 전부 0**이다.
 *   statusBars 도 0 — 데코가 이미 잘라낸 뒤라 컴포즈까지 내려오는 값이 없다.
 * - `setDecorFitsSystemWindows(false)` + `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` 를 걸면
 *   같은 기기에서 statusBars 0/86/0/0 · displayCutout 0/86/0/0 · **safeDrawing 0/86/0/135** ·
 *   **safeGestures 0/120/0/135** · safeContent 0/120/0/135 로 바뀐다(px).
 *   safeGestures 의 top 120 이 safeDrawing 의 86 보다 커서, 합집합인 safeContent 가 120 을 따른다.
 * - `WindowInsets.cutoutPath` 는 같은 조건에서 `Rect.fromLTRB(514, 0, 566, 86)` 로,
 *   플랫폼 dumpsys 의 boundingRect 와 정확히 일치한다. 확장 전에는 null.
 * - ⚠️ **`displayShape` 는 API 34 전용이 아니다.** API 33 에서도 non-null 이고 path 도 비어 있지 않다
 *   (`isEmpty=false`, bounds 0,0~1080,2400). core 1.18.0 의 `WindowInsetsCompat.Impl20.createDisplayShape`
 *   가 `Display.getRealSize()` 와 `DisplayCompat.getRoundedCorner()` 네 개(이 기기는 전부 radius=90px,
 *   center (90,90)/(990,90)/(990,2310)/(90,2310))로 **둥근 사각형을 직접 합성**하기 때문이다
 *   (`isRound()` 가 true 인 워치라면 원으로 만든다). 즉 플랫폼이 준 모양이 아니라 반경으로 재구성한
 *   근사치이며, 플랫폼 `WindowInsets.getDisplayShape()` 를 읽는 것은 `Impl34` 뿐이다.
 * - ⚠️ 그래서 지원 여부를 `displayShape != null` 로 판정하면 안 된다. 합성조차 못 하면
 *   `DisplayShapeCompat.EMPTY`(spec "")가 돌아오고, 그 `getPath()` 는 **새 빈 Path** 다
 *   (ImplBase: spec 이 비면 `new Path()`). `path.isEmpty` 로 봐야 한다.
 * - ⚠️ `DisplayCutoutCompat.getCutoutPath()` 는 **API 31 미만에서 무조건 null**(바이트코드:
 *   `SDK_INT >= 31` 이면 `Api31Impl.getCutoutPath`, 아니면 `aconst_null`). 그 아래에서 모양이
 *   필요하면 `DisplayCutout.getBoundingRects()`(API 29+)의 사각형 근사가 한계다.
 * - `WindowInsets.cutoutPath` 는 **Compose 1.11 신규**(1.8.2 에 없음, 1.11.1 부터). 실험 API 마커 없음.
 *   `Modifier.recalculateWindowInsets()` 는 1.8.2 에도 이미 있으므로 신규가 아니다.
 * - `Modifier.padding(insets.asPaddingValues())` 는 여백만 주고 **소비하지 않는다.** 적용+소비는
 *   `windowInsetsPadding()`, 소비만은 `consumeWindowInsets()`, 위치 기준 재계산은
 *   `recalculateWindowInsets()`(InsetsConsumingModifierNode + LayoutModifierNode).
 *
 * 이 화면을 만들며 밟은 함정 2가지(둘 다 실기기 계측으로 확인):
 * - ⚠️ **LazyColumn 항목의 `DisposableEffect` 는 화면 이탈이 아니라 스크롤 아웃에서 돈다.**
 *   계측: 40행 리스트에서 첫 항목이 화면 밖으로 밀리자 곧바로 onDispose 가 실행됐다.
 *   윈도우 설정 원복을 카드 안에 두면 스크롤만으로 사용자의 설정이 되돌아간다 →
 *   `rememberWindowModeController()` 로 화면 루트에 올렸다.
 * - ⚠️ **`when (this) { is Activity -> ... }` 같은 타입 분기는 디버그 빌드에서 화면을 통째로 날린다.**
 *   Kotlin 2.4 는 이 관용구(대상 없는 `when { this is A -> }` 포함)를 JDK 21 의
 *   `SwitchBootstraps.typeSwitch` invokedynamic 으로 컴파일하는데, 디버그 빌드에 들어가는 HotSwan
 *   2.0.0 인터프리터가 그 BSM 을 지원하지 않는다 —
 *   `InterpreterInternalError: INVOKEDYNAMIC not yet implemented (unknown BSM ... typeSwitch)`.
 *   증상은 크래시가 아니라 **컴포지션이 조용히 실패해 화면이 0x0** 으로 뜨는 것이었다
 *   (semantics 루트 `(l=0, t=86, r=0, b=86)px`). 그래서 이 예제는 Context 재귀 대신
 *   activity-compose 1.13.0 의 `LocalActivity.current` 를 쓴다.
 *   참고: 같은 바이트코드를 가진 기존 클래스가 26개 있으나(1687개 중), 계측한 화면
 *   (SealedDomainError·MVI)은 정상 렌더됐다 — 해당 메서드가 컴포지션 경로에서 실행될 때만 터진다.
 */
