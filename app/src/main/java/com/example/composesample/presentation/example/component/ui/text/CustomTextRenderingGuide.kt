package com.example.composesample.presentation.example.component.ui.text

/**
 * Exploring Custom Text Rendering with Jetpack Compose
 *
 * 출처: https://segunfamisa.com/posts/exploring-custom-text-rendering-in-compose
 *
 * === 개요 ===
 *
 * Jetpack Compose는 기본 Text 컴포넌트 외에도 저수준 텍스트 API를 제공하여
 * 커스텀 텍스트 렌더링을 가능하게 합니다.
 *
 * 이를 통해 페이드 효과, 웨이브 효과, 타이핑 애니메이션 등
 * 다양한 텍스트 효과를 구현할 수 있습니다.
 *
 * === 핵심 API ===
 *
 * **1. TextMeasurer**
 *
 * TextMeasurer는 텍스트를 실제로 그리기 전에 측정할 수 있게 해줍니다.
 * 스타일, 텍스트, 제약 조건 등을 고려하여 텍스트 크기를 계산합니다.
 *
 * ```kotlin
 * val textMeasurer = rememberTextMeasurer()
 * val textLayoutResult = remember(text, textStyle, constraints) {
 *     textMeasurer.measure(
 *         text = text,
 *         style = textStyle,
 *         constraints = constraints
 *     )
 * }
 * ```
 *
 * **2. TextLayoutResult**
 *
 * TextLayoutResult는 텍스트 레이아웃에 대한 상세한 정보를 담고 있습니다.
 * 이를 통해 매우 강력한 텍스트 조작이 가능합니다.
 *
 * 주요 속성:
 * - `lineCount`: 텍스트를 그리는 데 필요한 라인 수
 * - `size`: 텍스트의 전체 크기
 * - `getBoundingBox(index)`: 특정 문자의 바운딩 박스
 * - `getLineStart(lineIndex)`: 특정 라인의 시작 문자 인덱스
 * - `getLineEnd(lineIndex)`: 특정 라인의 끝 문자 인덱스
 * - `getLineLeft(lineIndex)`: 특정 라인의 왼쪽 X 좌표
 * - `getLineRight(lineIndex)`: 특정 라인의 오른쪽 X 좌표
 * - `getLineTop(lineIndex)`: 특정 라인의 상단 Y 좌표
 * - `getLineBottom(lineIndex)`: 특정 라인의 하단 Y 좌표
 *
 * **3. Canvas**
 *
 * Canvas API를 사용하여 실제로 텍스트를 그립니다.
 * 다양한 변환과 효과를 적용할 수 있습니다.
 *
 * ```kotlin
 * Canvas(modifier = Modifier.size(canvasSize)) {
 *     drawText(
 *         textMeasurer = textMeasurer,
 *         text = text,
 *         topLeft = Offset(x, y),
 *         style = textStyle
 *     )
 * }
 * ```
 *
 * === 구현 예제 ===
 *
 * **1. FadedText (페이드 텍스트)**
 *
 * 멀티라인 텍스트가 점진적으로 페이드되어 마지막 라인이 완전히 보이는 효과입니다.
 *
 * 구현 전략:
 * - TextMeasurer로 라인 수 측정
 * - 각 라인에 대해 알파 값을 점진적으로 증가
 * - 첫 라인은 거의 투명, 마지막 라인은 완전 불투명
 *
 * ```kotlin
 * for (lineIndex in 0 until textLayout.lineCount) {
 *     val alpha = textStyle.color.alpha * lineIndex.toFloat() / textLayout.lineCount
 *     val lineText = text.substring(
 *         textLayout.getLineStart(lineIndex),
 *         textLayout.getLineEnd(lineIndex)
 *     )
 *     drawText(
 *         textMeasurer = textMeasurer,
 *         text = lineText,
 *         topLeft = Offset(
 *             textLayout.getLineLeft(lineIndex),
 *             textLayout.getLineTop(lineIndex)
 *         ),
 *         style = textStyle.copy(color = textStyle.color.copy(alpha = alpha))
 *     )
 * }
 * ```
 *
 * **2. WarpedText (웨이브 텍스트)**
 *
 * 각 문자가 사인파처럼 위아래로 움직이는 효과입니다.
 *
 * 구현 전략:
 * - 각 문자의 바운딩 박스를 가져옴
 * - sin 함수를 사용하여 각 문자의 Y 오프셋 계산
 * - withTransform으로 translate 변환 적용
 *
 * ```kotlin
 * for (charIndex in startCharIndex until endCharIndex) {
 *     val rect = textLayout.getBoundingBox(charIndex)
 *     val char = textLayout.layoutInput.text[charIndex].toString()
 *     
 *     withTransform({
 *         translate(
 *             left = 0f,
 *             top = 5 * sin(charIndex * 0.7).toFloat()
 *         )
 *     }) {
 *         drawText(
 *             textMeasurer = textMeasurer,
 *             text = char,
 *             topLeft = Offset(rect.left, rect.top),
 *             style = textStyle
 *         )
 *     }
 * }
 * ```
 *
 * **3. AnimatedWarpedText (애니메이션 웨이브 텍스트)**
 *
 * 웨이브 효과에 애니메이션을 추가하여 위아래로 움직이게 합니다.
 *
 * 구현 전략:
 * - rememberInfiniteTransition으로 무한 애니메이션 생성
 * - 사인파의 진폭(amplitude)을 애니메이션
 * - -5에서 +5 사이를 반복
 *
 * ```kotlin
 * val infiniteTransition = rememberInfiniteTransition()
 * val amplitude by infiniteTransition.animateFloat(
 *     initialValue = -5f,
 *     targetValue = 5f,
 *     animationSpec = infiniteRepeatable(
 *         animation = tween(2000, easing = LinearEasing),
 *         repeatMode = RepeatMode.Reverse
 *     )
 * )
 * 
 * // 그리기 시 amplitude 사용
 * translate(
 *     left = 0f,
 *     top = amplitude * sin(charIndex * 0.7).toFloat()
 * )
 * ```
 *
 * **4. TypewriterText (타이핑 텍스트)**
 *
 * 타자기처럼 문자가 하나씩 나타나는 효과입니다.
 *
 * 구현 전략:
 * - Animatable로 현재 표시할 문자 수 애니메이션
 * - 0에서 텍스트 길이까지 부드럽게 증가
 * - 각 라인에서 표시할 문자만큼만 substring으로 잘라서 그리기
 *
 * ```kotlin
 * val animatedCharacterCount = remember { Animatable(0f) }
 * LaunchedEffect(text) {
 *     animatedCharacterCount.animateTo(
 *         targetValue = text.length.toFloat(),
 *         animationSpec = tween(
 *             durationMillis = text.length * 50,
 *             easing = LinearEasing
 *         )
 *     )
 * }
 * 
 * val visibleChars = animatedCharacterCount.value.toInt()
 * for (lineIndex in 0 until lines) {
 *     if (visibleChars > startCharIndex) {
 *         val displayedEndIndex = minOf(endCharIndex, visibleChars)
 *         val displayedText = text.substring(startCharIndex, displayedEndIndex)
 *         drawText(...)
 *     }
 * }
 * ```
 *
 * === TextMeasurer 사용 시 주의사항 ===
 *
 * **BoxWithConstraints 사용**
 *
 * TextMeasurer에 제약 조건을 전달하려면 BoxWithConstraints를 사용해야 합니다.
 *
 * ```kotlin
 * BoxWithConstraints(modifier) {
 *     val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
 *     
 *     val textLayout = textMeasurer.measure(
 *         text = text,
 *         style = textStyle,
 *         constraints = constraints  // BoxWithConstraints의 constraints
 *     )
 * }
 * ```
 *
 * **remember 사용**
 *
 * TextLayoutResult는 비용이 큰 연산이므로 remember로 캐싱해야 합니다.
 *
 * ```kotlin
 * val textLayout = remember(text, textStyle, constraints) {
 *     textMeasurer.measure(...)
 * }
 * ```
 *
 * === Canvas 크기 설정 ===
 *
 * TextLayoutResult의 size를 사용하여 Canvas 크기를 정확하게 설정합니다.
 *
 * ```kotlin
 * val canvasSize = with(LocalDensity.current) {
 *     DpSize(
 *         textLayout.size.width.toDp(),
 *         textLayout.size.height.toDp()
 *     )
 * }
 * 
 * Canvas(modifier = Modifier.size(canvasSize)) {
 *     // 그리기
 * }
 * ```
 *
 * === 좌표 시스템 ===
 *
 * **라인 좌표 이해하기**
 *
 * - `getLineLeft()`: 라인의 시작 X 좌표
 * - `getLineRight()`: 라인의 끝 X 좌표
 * - `getLineTop()`: 라인의 상단 Y 좌표
 * - `getLineBottom()`: 라인의 하단 Y 좌표
 *
 * **문자 바운딩 박스**
 *
 * - `getBoundingBox(charIndex)`: 특정 문자를 둘러싼 Rect 반환
 * - `rect.left`, `rect.top`: 문자의 왼쪽 상단 좌표
 * - `rect.right`, `rect.bottom`: 문자의 오른쪽 하단 좌표
 *
 * === 변환 (Transform) ===
 *
 * **withTransform 사용**
 *
 * Canvas에서 임시 변환을 적용할 때 사용합니다.
 *
 * ```kotlin
 * withTransform({
 *     translate(left = dx, top = dy)
 *     rotate(degrees = angle)
 *     scale(scaleX = sx, scaleY = sy)
 * }) {
 *     drawText(...)
 * }
 * ```
 *
 * === 성능 고려사항 ===
 *
 * 1. **측정 캐싱**: TextLayoutResult는 remember로 캐싱
 * 2. **불필요한 재측정 방지**: 의존성 배열 최소화
 * 3. **문자별 그리기 주의**: 많은 문자를 개별적으로 그리면 성능 저하
 * 4. **애니메이션 최적화**: 필요한 부분만 애니메이션 적용
 *
 * === 실전 활용 사례 ===
 *
 * **1. 읽기 앱**
 * - 페이드 텍스트: 스크롤 시 상단 텍스트 페이드 아웃
 * - 타이핑 효과: 스토리 읽기 모드
 *
 * **2. 게임 UI**
 * - 웨이브 텍스트: 마법 텍스트 효과
 * - 흔들림 효과: 데미지 표시
 *
 * **3. 프레젠테이션 앱**
 * - 타이핑 애니메이션: 슬라이드 전환
 * - 페이드 인/아웃: 텍스트 강조
 *
 * **4. 메시징 앱**
 * - 타이핑 인디케이터
 * - 메시지 출현 애니메이션
 *
 * === 한계와 대안 ===
 *
 * **문자별 그리기 비용**
 *
 * 많은 문자를 개별적으로 그리면 성능에 영향을 줄 수 있습니다.
 * 대안: 라인별로 그리거나, 필요한 부분만 커스텀 렌더링
 *
 * **복잡한 텍스트 스타일**
 *
 * AnnotatedString의 모든 기능이 지원되지 않을 수 있습니다.
 * 대안: 기본 Text 컴포넌트와 조합 사용
 *
 * === 다른 활용 패턴 ===
 *
 * **1. 그라디언트 텍스트**
 * - 각 라인마다 다른 색상 적용
 * - 문자별로 색상 그라디언트
 *
 * **2. 아웃라인 텍스트**
 * - 같은 텍스트를 여러 번 그려서 아웃라인 효과
 * - strokeWidth 조정
 *
 * **3. 반짝이는 텍스트**
 * - 각 문자에 순차적으로 하이라이트
 * - 애니메이션과 알파 조합
 *
 * **4. 3D 텍스트 효과**
 * - 여러 레이어에 오프셋 적용
 * - 그림자 효과
 *
 * === 요약 ===
 *
 * Jetpack Compose의 저수준 텍스트 API를 사용하면
 * 다양한 커스텀 텍스트 효과를 구현할 수 있습니다.
 *
 * 핵심 포인트:
 * - TextMeasurer로 텍스트 측정
 * - TextLayoutResult로 상세 정보 접근
 * - Canvas에서 자유롭게 그리기
 * - 라인별 또는 문자별 제어 가능
 * - 애니메이션과 변환 적용 가능
 * - 성능을 위해 캐싱과 최적화 필수
 */

object CustomTextRenderingGuide {
    const val GUIDE_INFO = """
        Custom Text Rendering with Jetpack Compose
        
        핵심 API:
        - TextMeasurer: 텍스트 측정
        - TextLayoutResult: 레이아웃 정보
        - Canvas: 실제 그리기
        
        구현 예제:
        - FadedText: 페이드 효과
        - WarpedText: 웨이브 효과
        - AnimatedWarpedText: 애니메이션 웨이브
        - TypewriterText: 타이핑 효과
        
        주요 기능:
        - 라인별 제어 (lineCount, getLineStart/End)
        - 문자별 제어 (getBoundingBox)
        - 좌표 정보 (getLineLeft/Right/Top/Bottom)
        - 변환 (translate, rotate, scale)
        
        주의사항:
        - remember로 캐싱
        - BoxWithConstraints로 제약 조건 전달
        - 문자별 그리기 성능 고려
        
        출처: https://segunfamisa.com/posts/exploring-custom-text-rendering-in-compose
    """
}
