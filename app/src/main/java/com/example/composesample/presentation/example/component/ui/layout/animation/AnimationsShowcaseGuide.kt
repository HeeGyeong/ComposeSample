package com.example.composesample.presentation.example.component.ui.layout.animation

/**
 * Compose Animations Showcase 참고 자료
 *
 * - Compose Animation Quick Guide:
 *     https://developer.android.com/develop/ui/compose/animation/quick-guide
 * - Animation Specs(tween/spring/keyframes) 공식 문서:
 *     https://developer.android.com/develop/ui/compose/animation/customize
 * - AnimatedContent SizeTransform 공식 가이드:
 *     https://developer.android.com/develop/ui/compose/animation/composables-modifiers#animatedcontent
 * - updateTransition (다중 속성 동기 애니메이션):
 *     https://developer.android.com/develop/ui/compose/animation/value-based#updatetransition
 * - 라이브러리/카탈로그 참고:
 *     https://github.com/skydoves/compose-animations
 *     https://doveletter.dev/docs/compose-animations
 *
 * 핵심 개념
 * --------
 * - animateXxxAsState: 타깃 값이 바뀌면 자동 보간. UI 속성 1개에 적합.
 * - AnimatedVisibility: 컴포저블 등장/사라짐을 enter/exit transition 으로 묶음.
 * - Crossfade: 동일 슬롯에서 두 컴포저블을 페이드로 교체.
 * - AnimatedContent: 콘텐츠 자체가 바뀔 때 size+transition 까지 처리.
 *     SizeTransform 으로 크기 변화 톤 제어.
 * - updateTransition: 하나의 상태로 여러 속성 애니메이션을 동시에 발생.
 * - rememberInfiniteTransition: 반복 재생 (RepeatMode.Reverse/Restart).
 * - Animatable + animateTo / snapTo: 직접 제어가 필요한 제스처 기반 모션.
 */
