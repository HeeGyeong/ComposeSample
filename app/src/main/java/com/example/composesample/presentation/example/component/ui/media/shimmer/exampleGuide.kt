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
 *
 * ## AutoSkeletonModifierExampleUI (자동 스켈레톤 로딩 감지 Modifier)
 * - CompositionLocal 공식 문서: https://developer.android.com/develop/ui/compose/compositionlocal
 * - drawWithContent 공식 문서: https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/package-summary#(androidx.compose.ui.Modifier).drawWithContent(kotlin.Function1)
 * - graphicsLayer 공식 문서: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/package-summary#(androidx.compose.ui.Modifier).graphicsLayer(kotlin.Function1)
 * - 참고 아티클(기법 출처, 이 예제는 라이브러리 도입이 아니라 기법 자체를 stdlib+Compose API로 직접 구현): "Lights Out: Automatic Skeleton Loading in Compose Multiplatform" https://www.kmpbits.com/posts/skeletal-compose-multiplatform/ (AW #741 소개, 원문은 Compose **Multiplatform** 전용 skeletal 라이브러리를 다루므로 라이브러리를 그대로 가져오지 않고 CompositionLocal + drawWithContent + graphicsLayer 조합만 이 프로젝트의 표준 Jetpack Compose API로 재구현)
 *
 * 핵심 개념:
 * - SkeletonContainer가 로딩 상태와 rememberInfiniteTransition 애니메이션 진행도를 한 번만 소유하고, CompositionLocalProvider로 하위에 전파한다
 * - Modifier.autoSkeleton()은 각 요소가 LocalSkeletonScope.current에서 loading을 자동으로 읽어 그린다 — 요소마다 isLoading 파라미터를 넘기지 않는다
 * - shimmer 사각형은 별도 스켈레톤 트리를 만들지 않고, 그 요소 자신이 이미 측정된 크기(DrawScope.size)를 그대로 재사용해 그린다
 * - drawWithContent(바깥) 안에서 drawContent() 다음에 shimmer를 그리고, graphicsLayer(alpha)는 그 안쪽에 둬야 콘텐츠 알파와 shimmer 알파가 서로 독립적으로 크로스페이드된다 — 순서를 바꾸면 shimmer까지 콘텐츠와 함께 페이드된다
 * - SkeletonContainer 스코프 밖에서 autoSkeleton()을 쓰면 LocalSkeletonScope.current가 null이라 아무 것도 하지 않고 원본 Modifier를 그대로 반환한다(비용 0)
 *
 * 프로젝트 내 관련 예제와의 구분:
 * - ShimmerExampleUI/TextShimmerExampleUI: 요소마다 shimmerAnimation 값을 파라미터로 전달받고, 실제 콘텐츠와 별개로 크기를 맞춘 Spacer 스켈레톤 트리를 수동으로 구성(수동 상태 관리)
 * - 이 예제: CompositionLocal로 로딩 상태를 자동 전파하고, 실제 콘텐츠(Text/Box)에 Modifier 하나만 얹어 그 콘텐츠의 측정 크기를 그대로 재사용(자동 감지) — 데모 카드에서 두 방식을 같은 로딩 상태로 나란히 비교
 *
 * 주의사항:
 * - 접근성 semantics(스크린 리더에 로딩 상태를 알리는 SemanticsPropertyKey 등)는 원문 라이브러리가 갖춘 기능이지만, 이 예제는 CompositionLocal + drawWithContent + graphicsLayer 조합이라는 핵심 기법에 집중하기 위해 범위에서 제외했다
 * - autoSkeleton()의 shimmer 그라데이션은 매 프레임 새 Brush 객체를 만든다 — 이 예제는 데모 규모(요소 몇 개)라 별도 캐싱을 하지 않는다
 */
