package com.example.composesample.presentation.example.component.ui.canvas

/**
 * 📚 Motion Blur for a Spinning Wheel 학습 가이드
 *
 * 출처: https://proandroiddev.com/motion-blur-for-a-spinning-wheel-in-jetpack-compose-368c1647224d
 *
 * 이 문서는 Jetpack Compose에서 스피닝 휠(회전하는 바퀴)에
 * 모션 블러 효과를 적용하는 다양한 기법을 다룹니다.
 *
 * =================================================================================================
 * 🎯 이 예제로 학습할 수 있는 내용
 * =================================================================================================
 *
 * 1. Canvas로 스피닝 휠(룰렛) 그리기
 * 2. InfiniteTransition으로 연속 회전 애니메이션 구현
 * 3. Ghost Frames(유령 프레임) 기법으로 모션 블러 효과
 * 4. BlurMaskFilter를 활용한 Paint 레벨 블러
 * 5. RenderEffect(API 31+)로 하드웨어 가속 블러
 * 6. 각 기법의 장단점 비교
 *
 * =================================================================================================
 * 🔍 핵심 개념 1: 스피닝 휠 기본 구조
 * =================================================================================================
 *
 * ```kotlin
 * Canvas(modifier = Modifier.size(240.dp)) {
 *     val radius = size.minDimension / 2f
 *     val cx = size.width / 2f
 *     val cy = size.height / 2f
 *     val sweepAngle = 360f / sectors.size
 *
 *     sectors.forEachIndexed { i, (color, label) ->
 *         val startAngle = i * sweepAngle
 *         drawArc(
 *             color = color,
 *             startAngle = startAngle,
 *             sweepAngle = sweepAngle,
 *             useCenter = true,
 *             topLeft = Offset(cx - radius, cy - radius),
 *             size = Size(radius * 2, radius * 2)
 *         )
 *     }
 * }
 * ```
 *
 * =================================================================================================
 * 🔍 핵심 개념 2: 회전 애니메이션
 * =================================================================================================
 *
 * ```kotlin
 * val infiniteTransition = rememberInfiniteTransition()
 * val rotation by infiniteTransition.animateFloat(
 *     initialValue = 0f,
 *     targetValue = 360f,
 *     animationSpec = infiniteRepeatable(
 *         animation = tween(
 *             durationMillis = 2000,
 *             easing = LinearEasing
 *         )
 *     )
 * )
 *
 * // graphicsLayer로 회전 적용
 * Modifier.graphicsLayer { rotationZ = rotation }
 * ```
 *
 * - LinearEasing: 일정한 속도로 회전 (모션 블러 효과를 잘 보여줌)
 * - 빠른 속도일수록 모션 블러 효과가 두드러짐
 *
 * =================================================================================================
 * 🔍 핵심 개념 3: Ghost Frames (유령 프레임) 기법
 * =================================================================================================
 *
 * 모션 블러의 핵심 원리: 동일한 물체를 여러 각도에서 반투명하게 겹쳐 그립니다.
 *
 * ```kotlin
 * val ghostCount = 8
 * val ghostDelta = speed / 60f  // 프레임당 이동 각도
 * val baseAlpha = 0.7f
 *
 * repeat(ghostCount) { i ->
 *     val ghostRotation = rotation - i * ghostDelta
 *     val alpha = baseAlpha * (1f - i.toFloat() / ghostCount)
 *
 *     withTransform({
 *         rotate(ghostRotation, pivot = Offset(cx, cy))
 *     }) {
 *         // 동일한 휠을 alpha 적용해 그리기
 *         drawWheel(alpha = alpha)
 *     }
 * }
 * ```
 *
 * 장점:
 * - API 레벨 제한 없음 (모든 Android 버전에서 동작)
 * - 블러 방향이 정확히 회전 방향과 일치
 * - ghostCount와 ghostDelta 조정으로 블러 강도 제어
 *
 * 단점:
 * - ghostCount가 많을수록 그리기 호출 증가 (성능 비용)
 *
 * =================================================================================================
 * 🔍 핵심 개념 4: BlurMaskFilter
 * =================================================================================================
 *
 * Canvas의 Paint에 BlurMaskFilter를 적용하여 블러를 만듭니다.
 *
 * ```kotlin
 * val blurPaint = Paint().apply {
 *     asFrameworkPaint().apply {
 *         isAntiAlias = true
 *         maskFilter = BlurMaskFilter(
 *             radius,           // 블러 반지름 (px)
 *             BlurMaskFilter.Blur.NORMAL  // 블러 스타일
 *         )
 *     }
 * }
 *
 * drawIntoCanvas { canvas ->
 *     canvas.drawCircle(
 *         center = Offset(cx, cy),
 *         radius = radius,
 *         paint = blurPaint
 *     )
 * }
 * ```
 *
 * BlurMaskFilter.Blur 종류:
 * - NORMAL: 내부+외부 모두 블러
 * - SOLID: 내부는 선명, 외부만 블러 (그림자 효과)
 * - INNER: 내부만 블러, 외부는 선명
 * - OUTER: 외부에만 블러 그림 (내부는 투명)
 *
 * 주의: 하드웨어 가속 활성화 시 BlurMaskFilter는 소프트웨어 레이어로 폴백될 수 있습니다.
 *
 * =================================================================================================
 * 🔍 핵심 개념 5: RenderEffect (API 31+)
 * =================================================================================================
 *
 * Android 12(API 31)부터 사용 가능한 하드웨어 가속 블러입니다.
 *
 * ```kotlin
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
 *     Modifier.graphicsLayer {
 *         // 회전 적용
 *         rotationZ = rotation
 *         // 반경 방향 블러 효과를 위해 x축 블러만 적용
 *         renderEffect = BlurEffect(
 *             radiusX = blurRadius,
 *             radiusY = 0f,          // y축 블러는 0 (반경 방향만)
 *             edgeTreatment = TileMode.Decal
 *         )
 *     }
 * }
 * ```
 *
 * 장점:
 * - GPU 가속으로 성능이 우수
 * - 네이티브 블러 품질
 *
 * 단점:
 * - API 31+ 필요
 * - 회전 방향 블러가 아닌 축 방향 블러 (정확도 다소 낮음)
 *
 * =================================================================================================
 * 🔍 핵심 개념 6: 속도 연동 동적 블러 강도
 * =================================================================================================
 *
 * 실제 물리적 운동처럼 속도에 비례하여 블러 강도를 동적으로 조절합니다.
 *
 * ```kotlin
 * val currentAngle by remember { mutableFloatStateOf(0f) }
 * val angularVelocity = // 이전 프레임과 현재 프레임의 각도 차이
 *
 * val blurRadius = (abs(angularVelocity) / maxVelocity) * maxBlurRadius
 * val ghostCount = (abs(angularVelocity) / maxVelocity * maxGhosts).toInt()
 * ```
 *
 * =================================================================================================
 * 🚀 기법 비교 요약
 * =================================================================================================
 *
 * | 기법            | API 레벨 | 성능   | 방향 정확도 | 구현 난이도 |
 * |----------------|---------|--------|-----------|-----------|
 * | Ghost Frames   | 모든 버전  | 보통   | 높음       | 보통       |
 * | BlurMaskFilter | 모든 버전  | 낮음   | 낮음       | 쉬움       |
 * | RenderEffect   | 31+     | 높음   | 보통       | 쉬움       |
 *
 * ✅ 권장: 빠른 회전 + 정확한 블러 → Ghost Frames
 *          간단한 흐림 효과 → BlurMaskFilter
 *          최신 기기 성능 우선 → RenderEffect
 */
object MotionBlurGuide
