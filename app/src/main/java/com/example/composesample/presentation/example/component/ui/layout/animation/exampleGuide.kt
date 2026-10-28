package com.example.composesample.presentation.example.component.ui.layout.animation

/**
 * Layout/Animation 예제 참고 자료
 *
 * ## SharedElementTransitionExampleUI (Shared Element Transitions)
 * - 출처: https://medium.com/@kmpbits/master-compose-shared-element-transitions-a-smooth-ui-journey-fc483172531a
 * 핵심 개념:
 * - SharedTransitionLayout 내부에서 Modifier.sharedElement()/sharedBounds()로 화면(또는 상태) 간 공통 요소를 부드럽게 전환
 * - 고유 key로 요소를 식별(양쪽 상태에서 동일 key 필요), AnimatedContent/AnimatedVisibility의 animatedVisibilityScope 전달 필수
 * - skipToLookaheadSize()로 깜빡임 없는 크기 전환, boundsTransform으로 tween/spring 등 커스텀 애니메이션 적용
 *
 * ## SharedElementDebugToolingExampleUI (Compose 1.11 Shared Element 디버그 오버레이)
 * - 출처:
 *   - 공식 공지: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
 *   - Compose 1.11 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-animation
 * 핵심 개념:
 * - LookaheadAnimationVisualDebugging(Experimental)으로 SharedTransitionLayout의 key 매칭 상태를 화면 위에 오버레이 시각화
 * - 색상 3종: overlayColor(정상 매칭)·multipleMatchesColor(같은 key 중복 충돌)·unmatchedElementColor(짝 없는 요소)
 * - isShowKeyLabelEnabled로 매칭 key 라벨 표시, runComposeUiTest의 mainClock.advanceTimeBy/awaitFrame으로 결정론적 프레임 검증 가능
 * - ⚠️ 같은 key 의 sharedElement 가 동시에 둘 이상 보이면 매칭이 확정되지 않아 SharedTransitionLayout 이 '전환 중'
 *   (foundMatch && isAnimating)에서 벗어나지 못한다 → 화면이 멈춰 있어도 매 프레임 다시 그린다(실측 2초 124프레임,
 *   key 를 다르게 하면 0). 디버그 오버레이와 무관한 동작이다. 참고로 1.11.4 의 디버그 헬퍼는 전환이 진행 중일 때
 *   infiniteRepeatable 애니메이션 2개를 붙이며, 그 판단 플래그(isLookaheadAnimationVisualDebuggingEnabled)는
 *   isEnabled 인자와 무관한 상수 true 다(바이트코드 확인) — 예제의 다중 매칭 데모는 스위치로 켰을 때만 띄운다
 *
 * ## AnimationsShowcaseExampleUI (Compose 애니메이션 API 쇼케이스)
 * - 공식 문서:
 *   - Quick Guide: https://developer.android.com/develop/ui/compose/animation/quick-guide
 *   - Animation Specs(tween/spring/keyframes): https://developer.android.com/develop/ui/compose/animation/customize
 *   - AnimatedContent SizeTransform: https://developer.android.com/develop/ui/compose/animation/composables-modifiers#animatedcontent
 *   - updateTransition: https://developer.android.com/develop/ui/compose/animation/value-based#updatetransition
 * - 카탈로그 참고: https://github.com/skydoves/compose-animations , https://doveletter.dev/docs/compose-animations
 * 핵심 개념:
 * - animateXxxAsState(단일 값 보간) / AnimatedVisibility(등장·사라짐) / Crossfade(슬롯 교체) / AnimatedContent(SizeTransform 포함 콘텐츠 전환)
 * - updateTransition으로 하나의 상태에서 여러 속성 동시 애니메이션, rememberInfiniteTransition으로 무한 반복
 * - Animatable + animateTo/snapTo로 제스처 기반 직접 제어 모션 구현
 *
 * ## ArcPathAnimationExampleUI (2D 경로 애니메이션 — Arc / Spline)
 * - 공식 문서:
 *   - Customize animations(AnimationSpec 일반): https://developer.android.com/develop/ui/compose/animation/customize
 *   - Lookahead / approachLayout: https://developer.android.com/develop/ui/compose/animation/composables-modifiers#lookahead-scope
 * - API 레퍼런스:
 *   - ArcAnimationSpec: https://developer.android.com/reference/kotlin/androidx/compose/animation/core/ArcAnimationSpec
 *   - DeferredTargetAnimation: https://developer.android.com/reference/kotlin/androidx/compose/animation/core/DeferredTargetAnimation
 * 핵심 개념:
 * - 축이 기존 애니메이션 예제와 다르다 — SpringTweenSnap/AnimationsShowcase가 "시간에 따른 값 변화(1D 이징)"라면
 *   이 예제는 값이 2D(Offset/IntSize)일 때 "두 점 사이를 어떤 모양의 궤적으로 지나는가"를 다룬다.
 * - 경로를 눈대중으로 비교하지 않기 위해, AnimationSpec을 TargetBasedAnimation으로 감싸
 *   getValueFromNanos()로 좌표를 직접 샘플링해 Canvas에 그린다(화면의 곡선 = 스펙의 실제 출력).
 *
 * ### 실측 기록 (SM-A725F / API 33, Compose 1.11.1)
 * 예제 문구의 기하학적 주장은 추측이 아니라 아래 측정에서 나왔다.
 * 시작 (0,0) → 목표 (200,200), durationMillis=1000 기준 t=0.25 좌표:
 * - tween(기본 easing)        : (47.3, 47.3)  ← x==y 이므로 직선
 * - ArcAnimationSpec(ArcAbove): (72.6, 13.7)  ← x>y, 가로로 먼저 벌어짐
 * - ArcAnimationSpec(ArcBelow): (13.7, 72.6)  ← ArcAbove의 거울상
 * - ArcAnimationSpec(ArcLinear): (47.3, 47.3) ← tween과 완전히 동일 = "호를 쓰지 않는" 모드
 * 경유점 (100,-80) at 500ms 를 준 경우 t=0.25 좌표:
 * - keyframes         : (50, -40)   ← 키프레임 사이 직선 보간(경유점에서 각이 짐)
 * - keyframesWithSpline: (50, -62.5) ← 코너를 없애려 구간 안쪽을 미리 굽힘
 *   (두 스펙 모두 t=0.5에서 경유점 좌표를 정확히 통과한다 — 차이는 통과 전후의 꺾임뿐)
 * - keyframes 안 `값 at 시각 using ArcMode.X`는 그 키프레임에서 **출발하는** 구간에 적용된다
 *   (0ms에 ArcAbove → [0,500] 구간이 위로, 500ms에 ArcBelow → [500,1000] 구간이 아래로 휘는 것을 좌표로 확인).
 *   따라서 마지막 키프레임에 붙이면 뒤에 구간이 없어 효과가 없다.
 *
 * ### opt-in 경계 (프로브 컴파일로 확정)
 * - @OptIn 필요: ArcAnimationSpec → ExperimentalAnimationSpecApi. DeferredTargetAnimation 은 Compose 1.12 에서 정식 API 가 되어
 *   ExperimentalAnimatableApi 옵트인이 없어졌다(1.11 까지는 필요)
 * - opt-in 불필요(안정 API): keyframesWithSpline { }, keyframes { } 안의 `using ArcMode.X`
 *   → 게이팅이 ArcMode 자체가 아니라 ArcAnimationSpec 클래스에 붙어 있기 때문이다.
 *
 * ### DeferredTargetAnimation 계약 (실측)
 * - 생성 직후: pendingTarget=null, isIdle=true
 * - 첫 updateTarget(A): 반환 A(애니메이션 없이 즉시 확정), isIdle=true 유지
 * - 이후 updateTarget(B): 반환은 아직 A(현재값), pendingTarget=B, isIdle=false
 * - ⚠️ updateTarget에 넘기는 CoroutineScope는 MonotonicFrameClock을 가져야 한다.
 *   없으면 내부 Animatable이 withFrameNanos에서 IllegalStateException으로 죽는다
 *   ("A MonotonicFrameClock is not available in this CoroutineContext" — 스택트레이스로 확인).
 *   컴포지션의 rememberCoroutineScope()는 이 조건을 만족한다.
 * - 기존 LookaheadScopeExampleUI의 animateBounds가 이 계층 위에 올라간 편의 API에 해당한다.
 *
 * ### 자기 코드 계약 검증 (스모크 테스트, 삭제됨)
 * approachMeasure 블록이 스냅샷 상태에 쓰고 그 값을 같은 컴포지션이 읽으므로 리컴포지션 루프 위험이 있다.
 * 임시 androidTest로 화면 진입 → 경로 재생 → 확대/축소 왕복을 돌려 waitForIdle()이 무한 대기 없이
 * 통과하는 것을 확인했다(SM-A725F/API 33). 값이 문자열로 안정되면 동일 값 재대입이라 무효화가 멈춘다.
 *
 * ## FabDialogMorphExampleUI (FAB → 다이얼로그 모핑 — 하나의 Surface 를 진행값 하나로)
 * - 출처:
 *   - 원문: https://joebirch.co/uncategorized/morphing-a-floating-action-button-into-a-dialog-in-compose-multiplatform/
 *     (Joe Birch, Android Weekly #750)
 *   - 라이브러리(채택하지 않고 원리만 구현 — Compose Multiplatform 라이브러리): https://github.com/hitherejoe/MorphingFab
 *   - PopupProperties: https://developer.android.com/reference/kotlin/androidx/compose/ui/window/PopupProperties
 *   - 시스템 제스처 제외 200dp 한도: https://developer.android.com/reference/android/view/View#setSystemGestureExclusionRects(java.util.List%3Candroid.graphics.Rect%3E)
 *   - paneTitle 등 의미 속성: https://developer.android.com/develop/ui/compose/accessibility/semantics
 * 핵심 개념:
 * - 플랫폼 AlertDialog 는 별도 창이라 FAB 자리에서 자라날 수 없다. FAB 과 다이얼로그를 Surface 하나로 두고 Animatable 진행값
 *   p(0~1) 하나에서 위치·크기(Rect lerp)·모서리(16→28dp)·색(Color lerp)·스크림(0.32p)을 계산한다(FabDialogMorph.kt — 단위 테스트가
 *   같은 함수를 부른다). 값은 offset { } / layout { } / graphicsLayer { } / drawBehind { } 람다에서만 읽어 리컴포즈 없이 그린다
 *   (원문은 컴포지션에서 p 를 읽는다).
 * - 내용은 창을 나눠 페이드: 아이콘은 1/3 까지 사라지고 다이얼로그 내용은 0.55 부터 — 0.33~0.55 는 표면 색만 자라는 빈 구간이고
 *   어디에서도 둘이 겹치지 않는다. 다이얼로그 내용은 requiredSize 로 최종 크기에서 한 번만 배치(가운데 정렬 + 표면 clip).
 * - 펼치는 동안(p > 0)만 창 원점(PopupPositionProvider 가 IntOffset.Zero)에 고정한 Popup, clippingEnabled = false
 *   (FLAG_LAYOUT_NO_LIMITS)로 시스템 바 영역까지 덮는다. 팝업 창은 WRAP_CONTENT 라 fillMaxSize 대신 containerSize 를 px 로 고정.
 *   접힌 상태(p == 0)는 트리 안 일반 FAB — 박스 좌표에 박스의 positionInWindow 를 더해야 오버레이 좌표와 같은 픽셀이다.
 * - 이 앱은 BlogExampleActivity 루트가 systemBars 패딩을 주므로 예제 Box 가 이미 내비 바 위에 있다. 원문은 FAB 위치에
 *   navigationBars 인셋을 더하는데 여기서 더하면 두 번 띄워져 더하지 않는다. 다이얼로그 높이도 컨테이너에 맞춰 줄인다
 *   (원문은 너비만 — 카드 미리보기 높이 300dp 때문에 더함).
 *
 * ### 실측 기록 (Pixel 4 API 30 에뮬 debug·release, Pixel Tablet API 35 에뮬 debug, Compose 1.12.1)
 * - 넘김 오차: 보정 켬 (0, 0)px — 폰(예제 Box 창 위치 (0, 83))·태블릿((0, 48)) 모두. 보정 끔 (0, -83)px = 상태 바 높이만큼 튄다.
 *   스크림은 상태 바·내비 영역까지 덮는다(그 영역 픽셀이 x0.68 — API 30 상태 바, API 35 루트의 노란 배경 모두).
 * - 본문 배치(onTextLayout 횟수): requiredSize 1회(줄 수 2) · 끄면 열 때 debug 15회 · release 10회, 줄 수 46→…→2.
 * - 모션: 애니메이션 코루틴의 MotionDurationScale 이 시스템 animator_duration_scale 을 따른다 — 0 으로 두면 tween 350ms 가
 *   1프레임에 끝났다(배율 0.00, debug·release). Android 에서는 snap() 없이도 시스템 '애니메이션 삭제'를 따르고, snap() 은 앱 안
 *   설정이나 다른 플랫폼용이다. 측정 뒤 animator_duration_scale 은 원래 값(설정 없음)으로 되돌렸다.
 * - 뒤로가기: 비포커스 Popup(원문)은 키가 액티비티 창으로 가서 트리의 BackHandler(enabled = expanded)가 받고, 포커스 Popup 은
 *   팝업 창이 받아 dismissOnBackPress → onDismissRequest 로 온다(BackHandler 는 불리지 않음). 3버튼·제스처 내비 모두 같음.
 * - 시스템 제스처 제외: 원문의 PopupProperties(focusable = false, clippingEnabled = false) 는 excludeFromSystemGesture 가 기본값
 *   true 로 남아 API 29+ 에서 팝업 전체를 제외 영역으로 요청한다(AndroidPopup.android.kt PopupLayoutHelperImpl29). 문서는 세로
 *   200dp 한도만 적고, 제스처 내비 Pixel 4 API 30 에뮬(밀도 2.75, 높이 2280px)에서는 아래쪽 200dp = 550px 이 걸렸다 — 왼쪽 가장자리
 *   스와이프가 y 1720 까지는 뒤로가기, y 1740 부터는 앱 터치. Pixel Tablet API 35 에뮬(밀도 2.0, 높이 1600px)도 아래쪽 400px —
 *   y 1150 뒤로가기, y 1250 앱 터치. 그 스와이프는 스크림 detectTapGestures 가 탭으로 받아(움직임으로
 *   취소하지 않는다) 닫히긴 했지만 경로는 '스크림 탭'이었다. 포커스 Popup 도 같다 → 창 전체 오버레이는 false.
 * - 접근성 창(uiautomator dump --compressed · --windows): 플랫폼 Dialog 는 Dialog 창만 목록에 남고(액티비티 창 빠짐), 비포커스
 *   Popup 은 액티비티 창이 활성·포커스로 남아 스크림 뒤 배경이 읽힌다. 배경에 clearAndSetSemantics { } 를 걸면 오히려 아래 깔린 예제
 *   목록이 드러났다 — 가지치기(SemanticsOwner.getAllUncoveredSemanticsNodesToIntObjectMap)는 isImportantForAccessibility 인 노드의
 *   영역만 아래에서 빼는데, 비운 노드는 중요하지 않아 아무것도 가리지 않는다. ComposeView 에
 *   IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS 를 펼친 동안만 걸면 배경 0개(기본 dump 는 중요하지 않은 노드까지 보여 16개로
 *   보이므로 --compressed 로 판정). 포커스 Popup 은 Dialog 처럼 Popup 창만 남는다. --windows 덤프는 API 30 에서 같은 팝업 창(같은
 *   id)을 두 번 적고 액티비티 창의 노드를 팝업 내용으로 적는다 — 창 목록·플래그만 쓰고 노드는 활성 창 덤프로 본다.
 * - TalkBack(켠 채 FAB 탭 — adb 주입 탭·스와이프는 터치 탐색을 거치지 않고 앱에 바로 간다): 원문은 접근성 포커스가 배경 헤더에
 *   남고(테두리가 스크림 아래에 그려진다 — 포커스 표시는 노드가 있는 창에 그려진다), 배경 View 숨김은 포커스가 사라지며, 포커스
 *   Popup 과 플랫폼 AlertDialog 는 다이얼로그 제목으로 옮겨 간다. 항목 이동(스와이프·Alt+→)은 adb 로 몰 수 없어 재지 않았다.
 *   측정 동안 TalkBack 설정(enabled_accessibility_services 등)은 원래 값으로 되돌렸다.
 * - semantics { dialog() } 는 Android 접근성 노드로 옮겨지지 않는다(ui 1.12.1 AndroidComposeViewAccessibilityDelegateCompat 에
 *   IsDialog 사용 0 — 테스트의 isDialog() 매처용). paneTitle 은 AccessibilityNodeInfo.paneTitle 과 창 상태 변경 이벤트로 간다.
 */
