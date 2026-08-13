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
 * - @OptIn 필요: ArcAnimationSpec → ExperimentalAnimationSpecApi / DeferredTargetAnimation → ExperimentalAnimatableApi
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
 */
