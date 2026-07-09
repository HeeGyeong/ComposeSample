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
 */
