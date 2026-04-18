package com.example.composesample.presentation.example.component.ui.visibility

/**
 * From View.INVISIBLE to Modifier.visible: Rethinking Visibility in Jetpack Compose
 *
 * 출처: https://proandroiddev.com/from-view-invisible-to-modifier-visible-rethinking-visibility-%EF%B8%8Fin-jetpack-compose-7957650e4d70
 *
 * === VisibilityExampleUI에서 다루는 내용 ===
 *
 * 이 예제는 Compose에서 가시성(Visibility)을 처리하는 다양한 방법을 보여줍니다:
 *
 * 주요 데모:
 * 1. GoneVsInvisibleCard()
 *    - View 시스템의 GONE vs INVISIBLE 개념 비교
 *    - Compose에서의 대응 방법 시연
 *
 * 2. ConditionalCompositionCard()
 *    - if 조건문으로 Composable 제거 (GONE 효과)
 *    - 가장 일반적인 Compose 패턴
 *
 * 3. AlphaModifierCard()
 *    - Modifier.alpha(0f)로 투명하게 만들기 (INVISIBLE 효과)
 *    - 공간은 유지하면서 보이지 않게
 *
 * 4. AnimatedVisibilityCard()
 *    - AnimatedVisibility로 애니메이션과 함께 표시/숨김
 *    - Enter/Exit 애니메이션 커스터마이징
 *
 * 5. CustomVisibilityModifierCard()
 *    - 커스텀 Modifier.visible() 확장 함수
 *    - View 시스템과 유사한 API 제공
 *
 * === View 시스템 vs Compose ===
 *
 * 1. View 시스템의 Visibility
 *    - VISIBLE: 보이고, 공간 차지
 *    - INVISIBLE: 안 보이지만, 공간 차지
 *    - GONE: 안 보이고, 공간도 차지 안 함
 *
 *    view.visibility = View.VISIBLE
 *    view.visibility = View.INVISIBLE
 *    view.visibility = View.GONE
 *
 * 2. Compose의 접근 방식
 *    Compose에는 visibility 속성이 없습니다.
 *    대신 다른 패턴을 사용합니다:
 *
 *    A. GONE 효과 (조건부 컴포지션)
 *       if (isVisible) {
 *           MyComposable()
 *       }
 *       // 컴포저블 자체가 컴포지션에서 제거됨
 *
 *    B. INVISIBLE 효과 (Alpha 수정)
 *       Box(
 *           modifier = Modifier.alpha(if (isVisible) 1f else 0f)
 *       ) {
 *           MyComposable()
 *       }
 *       // 공간은 유지, 투명하게 만듦
 *
 *    C. AnimatedVisibility (애니메이션 포함)
 *       AnimatedVisibility(visible = isVisible) {
 *           MyComposable()
 *       }
 *       // 애니메이션과 함께 표시/숨김
 *
 * === 왜 Compose에는 visibility가 없는가? ===
 *
 * 1. 선언적 UI 철학
 *    - View: 명령형 (상태 변경)
 *    - Compose: 선언적 (상태에 따라 UI 재구성)
 *
 *    // View (명령형)
 *    if (condition) {
 *        view.visibility = View.VISIBLE
 *    } else {
 *        view.visibility = View.GONE
 *    }
 *
 *    // Compose (선언적)
 *    if (condition) {
 *        MyComposable()  // 조건이 참이면 존재
 *    }
 *    // 조건이 거짓이면 아예 없음
 *
 * 2. 컴포지션 트리
 *    - Compose는 UI를 트리 구조로 관리
 *    - 조건이 false면 트리에서 완전히 제거
 *    - GONE과 유사하지만 더 효율적
 *
 * 3. 리컴포지션
 *    - 상태가 변경되면 영향받는 부분만 다시 그림
 *    - visibility 속성 없이도 자연스럽게 처리
 *
 * === 각 방법의 특징 ===
 *
 * 1. 조건부 컴포지션 (if)
 *    장점:
 *    - 가장 직관적
 *    - 메모리 효율적 (없으면 생성 안 함)
 *    - 리소스 해제 자동
 *
 *    단점:
 *    - 상태 초기화 (remember 값 손실)
 *    - 애니메이션 없음
 *
 *    if (isVisible) {
 *        var count by remember { mutableStateOf(0) }
 *        // isVisible이 false → true 될 때 count는 0으로 초기화
 *        Counter(count)
 *    }
 *
 * 2. Modifier.alpha()
 *    장점:
 *    - 공간 유지
 *    - 상태 유지 (remember 값 보존)
 *    - 레이아웃 안정성
 *
 *    단점:
 *    - 보이지 않아도 리소스 사용
 *    - 클릭 이벤트 여전히 활성화 (주의!)
 *
 *    Box(
 *        modifier = Modifier
 *            .alpha(if (isVisible) 1f else 0f)
 *            .clickable(enabled = isVisible) { }  // 클릭 비활성화 필요
 *    ) {
 *        MyComposable()
 *    }
 *
 * 3. AnimatedVisibility
 *    장점:
 *    - 부드러운 전환 애니메이션
 *    - Enter/Exit 커스터마이징
 *    - 사용자 경험 향상
 *
 *    단점:
 *    - 애니메이션 중 리소스 사용
 *    - 복잡한 설정 필요할 수 있음
 *
 *    AnimatedVisibility(
 *        visible = isVisible,
 *        enter = fadeIn() + expandVertically(),
 *        exit = fadeOut() + shrinkVertically()
 *    ) {
 *        MyComposable()
 *    }
 *
 * === 커스텀 Modifier.visible() ===
 *
 * View 시스템과 유사한 API를 원한다면 확장 함수를 만들 수 있습니다:
 *
 * enum class Visibility {
 *     Visible,
 *     Invisible,
 *     Gone
 * }
 *
 * fun Modifier.visible(visibility: Visibility): Modifier {
 *     return when (visibility) {
 *         Visibility.Visible -> this
 *         Visibility.Invisible -> this.alpha(0f)
 *         Visibility.Gone -> this.then(Modifier.size(0.dp))  // 또는 조건부 컴포지션 사용
 *     }
 * }
 *
 * // 사용
 * Box(modifier = Modifier.visible(Visibility.Invisible)) {
 *     Text("Hidden but takes space")
 * }
 *
 * === 실전 패턴 ===
 *
 * 1. 로딩 상태
 *    @Composable
 *    fun LoadingContent(isLoading: Boolean) {
 *        Box {
 *            // 콘텐츠는 항상 존재
 *            Content(modifier = Modifier.alpha(if (isLoading) 0.3f else 1f))
 *
 *            // 로딩 인디케이터는 조건부
 *            if (isLoading) {
 *                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
 *            }
 *        }
 *    }
 *
 * 2. 토글 콘텐츠
 *    @Composable
 *    fun ExpandableCard(expanded: Boolean) {
 *        Column {
 *            Header()
 *
 *            AnimatedVisibility(visible = expanded) {
 *                Content()  // 부드럽게 펼쳐짐
 *            }
 *        }
 *    }
 *
 * 3. 조건부 버튼
 *    @Composable
 *    fun ConditionalButton(enabled: Boolean) {
 *        Button(
 *            onClick = { },
 *            enabled = enabled,
 *            modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
 *        ) {
 *            Text("Submit")
 *        }
 *    }
 *
 * 4. 플레이스홀더 유지
 *    @Composable
 *    fun ProfileImage(imageUrl: String?) {
 *        Box(modifier = Modifier.size(48.dp)) {
 *            if (imageUrl != null) {
 *                AsyncImage(model = imageUrl, ...)
 *            } else {
 *                // 플레이스홀더로 공간 유지
 *                Icon(Icons.Default.Person, ...)
 *            }
 *        }
 *    }
 *
 * === 선택 가이드 ===
 *
 * Q1: 공간을 유지해야 하나요?
 * - YES → Modifier.alpha(0f) 또는 Box로 감싸기
 * - NO → 조건부 컴포지션 (if)
 *
 * Q2: 애니메이션이 필요한가요?
 * - YES → AnimatedVisibility
 * - NO → if 또는 alpha
 *
 * Q3: 상태를 유지해야 하나요?
 * - YES → alpha 또는 AnimatedVisibility
 * - NO → if (가장 효율적)
 *
 * Q4: 클릭 이벤트를 차단해야 하나요?
 * - YES → alpha 사용 시 clickable(enabled = false) 추가
 * - NO → alpha만 사용
 *
 * === 요약 ===
 *
 * View 시스템 → Compose 대응:
 * - VISIBLE → 그냥 Composable 호출
 * - INVISIBLE → Modifier.alpha(0f)
 * - GONE → 조건부 컴포지션 (if)
 *
 * 추천 패턴:
 * - 기본: 조건부 컴포지션 (if)
 * - 공간 유지: Modifier.alpha(0f)
 * - 애니메이션: AnimatedVisibility
 * - 커스텀: Modifier.visible() 확장 함수
 */

object VisibilityGuide {
    const val GUIDE_INFO = """
        Visibility in Jetpack Compose
        
        View 시스템 → Compose 대응:
        - VISIBLE → Composable 호출
        - INVISIBLE → Modifier.alpha(0f)
        - GONE → 조건부 컴포지션 (if)
        
        주요 패턴:
        - if (condition) { Composable() }
        - Modifier.alpha(0f/1f)
        - AnimatedVisibility
        - 커스텀 Modifier.visible()
        
        선택 기준:
        - 공간 유지? → alpha
        - 애니메이션? → AnimatedVisibility
        - 효율성? → if
        
        출처: https://proandroiddev.com/from-view-invisible-to-modifier-visible-rethinking-visibility-%EF%B8%8Fin-jetpack-compose-7957650e4d70
    """
}
