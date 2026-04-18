package com.example.composesample.presentation.example.component.ui.canvas

/**
 * Compose Canvas - Understanding Shapes and Animations for Beginners
 *
 * 출처: https://proandroiddev.com/compose-canvas-understanding-shapes-and-animations-for-beginners-255653149393
 *
 * === 개요 ===
 *
 * Jetpack Compose의 Canvas는 커스텀 그래픽과 애니메이션을 그리는
 * 강력한 도구입니다. Android View의 Canvas API를 Compose에서
 * 선언적으로 사용할 수 있습니다.
 *
 * Canvas를 사용하면 Circle, Rectangle, Path, Arc 등 다양한
 * 도형을 자유롭게 그릴 수 있으며, 애니메이션과 결합하여
 * 동적인 UI를 만들 수 있습니다.
 *
 * === Canvas 기본 사용법 ===
 *
 * ```kotlin
 * Canvas(modifier = Modifier.size(100.dp)) {
 *     drawCircle(
 *         color = Color.Red,
 *         radius = 50.dp.toPx()
 *     )
 * }
 * ```
 *
 * === DrawScope 컨텍스트 ===
 *
 * Canvas 블록 내부에서는 DrawScope가 리시버로 제공됩니다.
 * DrawScope는 다양한 그리기 함수를 제공합니다:
 *
 * 1. **drawCircle()** - 원 그리기
 * 2. **drawRect()** - 사각형 그리기
 * 3. **drawRoundRect()** - 둥근 사각형 그리기
 * 4. **drawLine()** - 선 그리기
 * 5. **drawPath()** - 자유로운 경로 그리기
 * 6. **drawArc()** - 호/부채꼴 그리기
 * 7. **drawOval()** - 타원 그리기
 * 8. **drawPoints()** - 점들 그리기
 *
 * === 좌표 시스템 ===
 *
 * - 원점 (0, 0)은 왼쪽 상단
 * - X축은 오른쪽으로 증가
 * - Y축은 아래쪽으로 증가
 * - size.width, size.height로 캔버스 크기 접근
 * - center로 중앙 좌표 접근
 *
 * === DP to PX 변환 ===
 *
 * Compose에서는 DP 단위를 사용하지만, Canvas는 픽셀 단위를 사용합니다.
 *
 * ```kotlin
 * val radiusPx = 50.dp.toPx()  // DP -> PX 변환
 * ```
 *
 * === 기본 도형 그리기 ===
 *
 * **1. Circle (원)**
 *
 * ```kotlin
 * drawCircle(
 *     color = Color.Red,
 *     radius = 50.dp.toPx(),
 *     center = Offset(100f, 100f)
 * )
 * ```
 *
 * **2. Rectangle (사각형)**
 *
 * ```kotlin
 * drawRect(
 *     color = Color.Blue,
 *     topLeft = Offset(50f, 50f),
 *     size = Size(100f, 100f)
 * )
 * ```
 *
 * **3. RoundRect (둥근 사각형)**
 *
 * ```kotlin
 * drawRoundRect(
 *     color = Color.Green,
 *     topLeft = Offset(50f, 50f),
 *     size = Size(100f, 100f),
 *     cornerRadius = CornerRadius(16.dp.toPx())
 * )
 * ```
 *
 * **4. Line (선)**
 *
 * ```kotlin
 * drawLine(
 *     color = Color.Black,
 *     start = Offset(0f, 0f),
 *     end = Offset(100f, 100f),
 *     strokeWidth = 4.dp.toPx()
 * )
 * ```
 *
 * **5. Arc (호/부채꼴)**
 *
 * ```kotlin
 * drawArc(
 *     color = Color.Yellow,
 *     startAngle = 0f,
 *     sweepAngle = 90f,
 *     useCenter = true,  // true: 부채꼴, false: 호
 *     topLeft = Offset(50f, 50f),
 *     size = Size(100f, 100f)
 * )
 * ```
 *
 * === Path 그리기 ===
 *
 * Path는 복잡한 형태를 그릴 때 사용합니다.
 *
 * ```kotlin
 * val path = Path().apply {
 *     moveTo(100f, 100f)        // 시작점
 *     lineTo(200f, 100f)        // 직선
 *     lineTo(150f, 200f)        // 직선
 *     close()                   // 경로 닫기 (시작점으로)
 * }
 *
 * drawPath(
 *     path = path,
 *     color = Color.Magenta
 * )
 * ```
 *
 * === DrawStyle (그리기 스타일) ===
 *
 * **Fill (채우기)**
 *
 * ```kotlin
 * drawCircle(
 *     color = Color.Red,
 *     radius = 50.dp.toPx(),
 *     style = Fill  // 기본값
 * )
 * ```
 *
 * **Stroke (테두리)**
 *
 * ```kotlin
 * drawCircle(
 *     color = Color.Red,
 *     radius = 50.dp.toPx(),
 *     style = Stroke(width = 4.dp.toPx())
 * )
 * ```
 *
 * === Brush (그라디언트) ===
 *
 * 단색 대신 그라디언트를 사용할 수 있습니다.
 *
 * **1. Linear Gradient (선형 그라디언트)**
 *
 * ```kotlin
 * drawRect(
 *     brush = Brush.linearGradient(
 *         colors = listOf(Color.Red, Color.Blue),
 *         start = Offset(0f, 0f),
 *         end = Offset(100f, 100f)
 *     )
 * )
 * ```
 *
 * **2. Radial Gradient (방사형 그라디언트)**
 *
 * ```kotlin
 * drawCircle(
 *     brush = Brush.radialGradient(
 *         colors = listOf(Color.Yellow, Color.Red),
 *         center = center,
 *         radius = 100.dp.toPx()
 *     )
 * )
 * ```
 *
 * **3. Sweep Gradient (회전 그라디언트)**
 *
 * ```kotlin
 * drawCircle(
 *     brush = Brush.sweepGradient(
 *         colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Red),
 *         center = center
 *     )
 * )
 * ```
 *
 * === 애니메이션과 결합 ===
 *
 * Canvas는 remember된 상태와 애니메이션을 사용하여
 * 동적인 그래픽을 만들 수 있습니다.
 *
 * **1. Infinite Animation (무한 애니메이션)**
 *
 * ```kotlin
 * val infiniteTransition = rememberInfiniteTransition()
 * val rotation by infiniteTransition.animateFloat(
 *     initialValue = 0f,
 *     targetValue = 360f,
 *     animationSpec = infiniteRepeatable(
 *         animation = tween(2000, easing = LinearEasing),
 *         repeatMode = RepeatMode.Restart
 *     )
 * )
 *
 * Canvas(modifier = Modifier.size(200.dp)) {
 *     rotate(rotation) {
 *         drawRect(color = Color.Red, size = Size(100f, 100f))
 *     }
 * }
 * ```
 *
 * **2. Value Animation (값 애니메이션)**
 *
 * ```kotlin
 * var isAnimating by remember { mutableStateOf(false) }
 * val radius by animateFloatAsState(
 *     targetValue = if (isAnimating) 100f else 50f,
 *     animationSpec = spring(
 *         dampingRatio = Spring.DampingRatioMediumBouncy,
 *         stiffness = Spring.StiffnessLow
 *     )
 * )
 *
 * Canvas(modifier = Modifier.clickable { isAnimating = !isAnimating }) {
 *     drawCircle(color = Color.Red, radius = radius)
 * }
 * ```
 *
 * === Transform (변환) ===
 *
 * DrawScope는 다양한 변환 함수를 제공합니다.
 *
 * **1. rotate() - 회전**
 *
 * ```kotlin
 * rotate(45f) {
 *     drawRect(color = Color.Red, size = Size(100f, 100f))
 * }
 * ```
 *
 * **2. scale() - 크기 조정**
 *
 * ```kotlin
 * scale(2f) {
 *     drawCircle(color = Color.Blue, radius = 50f)
 * }
 * ```
 *
 * **3. translate() - 이동**
 *
 * ```kotlin
 * translate(left = 100f, top = 50f) {
 *     drawCircle(color = Color.Green, radius = 30f)
 * }
 * ```
 *
 * === BlendMode (블렌드 모드) ===
 *
 * 여러 도형을 겹칠 때 블렌드 모드를 사용할 수 있습니다.
 *
 * ```kotlin
 * drawCircle(
 *     color = Color.Red,
 *     radius = 100.dp.toPx(),
 *     center = Offset(100f, 100f),
 *     blendMode = BlendMode.Screen
 * )
 * ```
 *
 * 주요 BlendMode:
 * - Clear: 투명하게
 * - SrcOver: 기본값 (위에 그리기)
 * - DstOver: 아래에 그리기
 * - Multiply: 곱하기
 * - Screen: 스크린
 * - Overlay: 오버레이
 *
 * === Alpha (투명도) ===
 *
 * ```kotlin
 * drawCircle(
 *     color = Color.Red,
 *     radius = 50.dp.toPx(),
 *     alpha = 0.5f  // 50% 투명
 * )
 * ```
 *
 * === 실전 활용 예제 ===
 *
 * **1. Progress Indicator (진행 표시기)**
 *
 * ```kotlin
 * var progress by remember { mutableStateOf(0f) }
 *
 * Canvas(modifier = Modifier.size(100.dp)) {
 *     drawArc(
 *         color = Color.Gray,
 *         startAngle = -90f,
 *         sweepAngle = 360f,
 *         useCenter = false,
 *         style = Stroke(width = 8.dp.toPx())
 *     )
 *
 *     drawArc(
 *         color = Color.Blue,
 *         startAngle = -90f,
 *         sweepAngle = progress * 360f,
 *         useCenter = false,
 *         style = Stroke(width = 8.dp.toPx())
 *     )
 * }
 * ```
 *
 * **2. Wave Animation (파도 애니메이션)**
 *
 * ```kotlin
 * val phase by infiniteTransition.animateFloat(
 *     initialValue = 0f,
 *     targetValue = 360f,
 *     animationSpec = infiniteRepeatable(tween(2000))
 * )
 *
 * Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
 *     val path = Path().apply {
 *         moveTo(0f, size.height / 2)
 *         for (x in 0..size.width.toInt() step 10) {
 *             val y = size.height / 2 + sin((x + phase) * PI / 180) * 30
 *             lineTo(x.toFloat(), y.toFloat())
 *         }
 *     }
 *     drawPath(path = path, color = Color.Blue, style = Stroke(width = 4f))
 * }
 * ```
 *
 * === 주의사항 ===
 *
 * 1. Canvas는 Recomposition될 때마다 다시 그려짐
 * 2. 복잡한 계산은 remember로 캐싱
 * 3. DP -> PX 변환 필수
 * 4. Path 객체는 재사용 가능
 * 5. 성능이 중요한 경우 drawWithCache 사용
 *
 * === drawWithCache 최적화 ===
 *
 * 복잡한 Path나 계산을 캐싱할 수 있습니다.
 *
 * ```kotlin
 * Canvas(modifier = Modifier.size(200.dp)) {
 *     val path = drawContext.canvas.nativeCanvas.let {
 *         Path().apply {
 *             // 복잡한 경로 생성
 *         }
 *     }
 *
 *     drawPath(path = path, color = Color.Red)
 * }
 * ```
 *
 * === 요약 ===
 *
 * Canvas는 Jetpack Compose에서 커스텀 그래픽을 그리는 핵심 도구입니다.
 *
 * 핵심 포인트:
 * - DrawScope 컨텍스트에서 다양한 도형 그리기
 * - DP -> PX 변환 필수
 * - Path로 복잡한 형태 그리기
 * - Brush로 그라디언트 적용
 * - 애니메이션과 결합하여 동적 UI 구현
 * - Transform, BlendMode, Alpha로 다양한 효과
 * - drawWithCache로 성능 최적화
 */

object CanvasShapesGuide {
    const val GUIDE_INFO = """
        Canvas Shapes & Animations
        
        기본 도형:
        - drawCircle: 원 그리기
        - drawRect: 사각형 그리기
        - drawRoundRect: 둥근 사각형
        - drawLine: 선 그리기
        - drawArc: 호/부채꼴
        - drawPath: 자유로운 경로
        
        그리기 스타일:
        - Fill: 채우기 (기본)
        - Stroke: 테두리
        
        그라디언트:
        - linearGradient: 선형
        - radialGradient: 방사형
        - sweepGradient: 회전
        
        변환:
        - rotate, scale, translate
        
        애니메이션:
        - animateFloatAsState
        - rememberInfiniteTransition
        
        출처: https://proandroiddev.com/compose-canvas-understanding-shapes-and-animations-for-beginners-255653149393
    """
}
