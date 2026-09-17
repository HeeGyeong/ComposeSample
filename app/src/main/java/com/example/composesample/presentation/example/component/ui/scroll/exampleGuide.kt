package com.example.composesample.presentation.example.component.ui.scroll

/**
 * Scroll 예제 참고 자료
 *
 * ## NestedScrollingExampleUI (NestedScrollConnection + Collapsing Toolbar)
 * 핵심 개념:
 * - 스크롤 처리 순서: onPreScroll(자식 처리 전) → Child Scroll(LazyColumn 자체 처리) → onPostScroll(자식 처리 후) → onPreFling/onPostFling(플링 속도 제어)
 * - Collapsing Toolbar: toolbarOffset을 0~-toolbarHeightPx 범위로 제어하고 alpha = 1f - (abs(offset) / totalHeight)로 계산
 * - 위로 스크롤 시 onPreScroll에서 일부(예: 30%) 우선 소비해 헤더를 숨기고, 나머지는 LazyColumn에 위임. 아래로 스크롤 시 LazyColumn이 더 이상 소비 못 하는 양(available.y > 0)을 onPostScroll에서 소비해 헤더를 다시 표시
 * - remember()로 NestedScrollConnection 객체를 재사용해 불필요한 recomposition 방지
 *
 * ## CustomScrollBehaviorExampleUI (커스텀 TopAppBarScrollBehavior)
 * - 출처: https://le0nidas.gr/2026/02/08/handle-recyclerviews-scroll-events-in-custom-topappbarscrollbehavior/
 * 핵심 개념:
 * - TopAppBarScrollBehavior는 내부적으로 NestedScrollConnection을 통해 state.heightOffset(0 또는 heightOffsetLimit)을 조정해 앱바를 완전히 보이거나 숨김
 * - MyEnterAlwaysScrollBehavior: 스크롤 버퍼(scrollAccumulation 100px 누적)로 미세한 제스처를 무시하고, animate()+tween(150)으로 부드러운 전환, animationInProgress 플래그로 애니메이션 중 재진입 방지
 * - MyExitUntilCollapsedScrollBehavior: onPreScroll에서 위로 스크롤(available.y < 0)일 때만 숨기고, onPostScroll에서 스크롤이 완전히 끝(available == Offset.Zero)났을 때만 다시 표시
 * - RecyclerView 등 View 기반 스크롤은 NestedScrollDispatcher로 이벤트를 Compose 쪽 NestedScrollConnection에 전달해 연동 가능
 *
 * ## ImeNestedScrollExampleUI (IME 인터랙티브 제어)
 * - 공식 문서(WindowInsets 처리): https://developer.android.com/develop/ui/compose/layouts/insets
 * - KDoc(foundation-layout 소스, 1.7.6 기준 — API 자체는 프로젝트 해석 버전과 동일하게 @ExperimentalLayoutApi):
 *   `Modifier.imeNestedScroll()` = "Controls the soft keyboard as a nested scrolling on Android R and later.
 *   This allows the user to drag the soft keyboard up and down. After scrolling, the IME will animate
 *   either to the fully shown or fully hidden position, depending on the position and fling."
 * 핵심 개념:
 * - `imeNestedScroll()`은 Build.VERSION.SDK_INT < R(30)이면 자기 자신(this)을 그대로 반환하는 no-op — 리스트가
 *   맨 위/아래에 도달해 더 이상 스크롤을 소비하지 못할 때 남는 delta를 IME 표시/숨김 애니메이션으로 이어받는다
 * - `WindowInsets.Companion.imeAnimationSource` = 애니메이션이 시작되기 직전의 인셋 값,
 *   `imeAnimationTarget` = 애니메이션이 끝났을 때 도달할 인셋 값. 애니메이션이 없을 때는 둘이 같다
 * - `WindowInsets.Companion.ime`(현재값)는 source와 target 사이를 프레임마다 보간하며 움직이므로,
 *   `(current - source) / (target - source)`로 실제 시스템 키보드 애니메이션의 진행률(0~1)을 그대로 계산할 수 있다 —
 *   직접 animateFloatAsState로 흉내내는 것보다 실제 IME 곡선과 어긋나지 않는다
 * - 세 값 모두 `@OptIn(ExperimentalLayoutApi::class)` 필요(기존 `ImeStateUtil.rememberImeState`의 `isImeVisible`과 동일 게이팅)
 *
 * ## CustomOverscrollExampleUI (커스텀 오버스크롤)
 * - 공식 문서(오버스크롤 커스터마이즈): https://developer.android.com/develop/ui/compose/touch-input/pointer-input/scroll#custom-overscroll
 * - API 레퍼런스(OverscrollEffect): https://developer.android.com/reference/kotlin/androidx/compose/foundation/OverscrollEffect
 * 핵심 개념:
 * - `OverscrollEffect` 는 세 가지만 요구한다 — `applyToScroll(delta, source, performScroll)`,
 *   `applyToFling(velocity, performFling)`, `isInProgress`. 시각 효과는 `node: DelegatableNode` 로 따로 낸다.
 * - `applyToScroll` 안에서 `performScroll(leftForScroll)` 을 호출해야 리스트가 실제로 움직인다.
 *   반환값(리스트가 소비한 양)을 빼고 남은 것이 "오버스크롤로 넘어가는 delta" 다 — 즉 리스트가 끝에 닿기 전에는
 *   남는 양이 0 이라 이펙트가 걸리지 않는다.
 * - 되감기 순서가 중요하다: 이미 오버스크롤이 걸린 상태에서 반대 방향 입력이 오면 리스트보다 오버스크롤을 먼저
 *   0 쪽으로 되돌려야 손가락과 화면이 어긋나지 않는다(공식 샘플의 `consumedByPreScroll` 단계).
 * - `source == NestedScrollSource.UserInput` 게이팅 — `SideEffect`(animateScrollTo 등 프로그래매틱 스크롤)까지
 *   오버스크롤로 넘기면 코드로 스크롤했을 때도 고무줄이 늘어난다.
 * - `LocalOverscrollFactory` 는 `ProvidableCompositionLocal<OverscrollFactory?>` 다. 팩토리를 제공하면 하위 트리의
 *   모든 스크롤 컨테이너가 그 이펙트를 쓰고, **null 을 제공하면 오버스크롤이 생성되지 않는다**(비활성화 경로).
 *   같은 이유로 `rememberOverscrollEffect()` 의 반환 타입도 nullable 이다.
 * - `OverscrollFactory` 는 `equals`/`hashCode` 를 추상 멤버로 **강제**한다 — CompositionLocal 값 비교로 불필요한
 *   이펙트 재생성을 막기 위한 것이므로, 람다를 필드로 들고 있으면 매 리컴포지션마다 달라지지 않도록 remember 로 고정해야 한다.
 * - `withoutVisualEffect()` / `withoutEventHandling()` 는 같은 이펙트를 두 조각으로 나눠 준다. 스크롤 컨테이너는
 *   전자로 이벤트만 처리하고, 클리핑 밖의 다른 컴포저블에 후자를 `Modifier.overscroll()` 로 붙이면 그림만 그 자리에 나온다.
 *   (컨테이너가 자기 경계에서 잘라 버리는 효과를 헤더나 배경으로 옮기는 용도)
 * - `AndroidEdgeEffectOverscrollFactory` 는 public 이 아니라 직접 인스턴스화할 수 없다. "기본 이펙트"가 필요하면
 *   `rememberOverscrollEffect()` 로 LocalOverscrollFactory 가 만들어 준 것을 받아 쓴다.
 * - 계측 팁: 임의의 이펙트를 감싸 `applyToScroll`/`applyToFling` 인자만 기록하는 데코레이터를 만들면 기본 이펙트도
 *   그대로 실측할 수 있다. 단 `node` 는 위임 대상 것을 그대로 돌려주므로 원본과 래퍼를 동시에 modifier 로 붙이면 안 된다.
 */
