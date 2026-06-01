package com.example.composesample.presentation.example.component.ui.media.shimmer

/**
 * UI/Media/Shimmer 예제 참고 자료
 *
 * ## ShimmerExampleUI / TextShimmerExampleUI (로딩 Shimmer 효과)
 * - 공식 문서(brush): https://developer.android.com/develop/ui/compose/graphics/draw/brush
 * - 애니메이션: https://developer.android.com/develop/ui/compose/animation/value-based
 * 핵심 개념:
 * - rememberInfiniteTransition + animateFloat 로 이동하는 그라데이션 오프셋 생성
 * - Brush.linearGradient(반투명 색상 리스트) + Modifier.background(brush) 로 반짝임 표현
 * - 외부 라이브러리 없이 자체 구현: 애니메이션된 startX/endX 를 brush에 반영
 * - Text Shimmer: TextStyle.brush 에 애니메이션 그라데이션을 지정해 글자에 효과 적용
 */
