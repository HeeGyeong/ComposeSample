package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Shared Element Transitions Guide
 *
 * 출처: https://medium.com/@kmpbits/master-compose-shared-element-transitions-a-smooth-ui-journey-fc483172531a
 *
 * Jetpack Compose의 Shared Element Transitions를 사용하여 화면 간 요소를 부드럽게 전환하는 방법을 설명합니다.
 *
 * === 핵심 개념 ===
 * 
 * Shared Element Transition은 두 화면(또는 상태) 간에 공통 요소가 있을 때,
 * 해당 요소를 부드럽게 전환하여 시각적 연속성을 제공하는 애니메이션 기법입니다.
 * 
 * 예시:
 * - 리스트 아이템을 클릭하면 상세 화면으로 확장되는 애니메이션
 * - 썸네일 이미지를 클릭하면 전체 화면으로 확대되는 애니메이션
 * - 탭 전환 시 선택된 아이템이 부드럽게 이동하는 애니메이션
 *
 * === 핵심 API ===
 * 
 * 1. SharedTransitionLayout:
 *    - Shared Element Transition의 최상위 컨테이너.
 *    - 모든 공유 요소는 이 레이아웃 내부에 있어야 합니다.
 *    - `SharedTransitionScope`를 제공하여 하위 컴포저블에서 Modifier를 사용할 수 있게 합니다.
 *
 * 2. Modifier.sharedElement():
 *    - 공유할 요소에 적용하는 Modifier.
 *    - `key`: 요소를 식별하는 고유 키 (양쪽 화면에서 동일해야 함).
 *    - `animatedVisibilityScope`: AnimatedContent 또는 AnimatedVisibility의 Scope.
 *    - `renderInOverlayDuringTransition`: 전환 중 오버레이에서 렌더링 여부.
 *
 * 3. Modifier.sharedBounds():
 *    - 공유할 영역의 경계를 정의.
 *    - 크기와 위치가 변경될 때 부드럽게 애니메이션.
 *    - `boundsTransform`: 커스텀 애니메이션 스펙 적용 가능.
 *
 * 4. Modifier.skipToLookaheadSize():
 *    - 레이아웃 측정을 건너뛰고 최종 크기로 즉시 이동.
 *    - 깜빡임 없이 부드러운 전환을 위해 사용.
 *
 * 5. AnimatedContent / AnimatedVisibility:
 *    - Shared Element를 포함하는 컨테이너.
 *    - `targetState`로 현재 표시할 상태를 지정.
 *    - `transitionSpec`으로 진입/퇴장 애니메이션 커스터마이징 가능.
 *
 * === 구현 패턴 ===
 * 
 * 기본 패턴:
 * ```kotlin
 * SharedTransitionLayout {
 *     AnimatedContent(targetState = showDetails) { isShowingDetails ->
 *         if (isShowingDetails) {
 *             DetailScreen(
 *                 modifier = Modifier.sharedElement(
 *                     rememberSharedContentState(key = "item-key"),
 *                     animatedVisibilityScope = this
 *                 )
 *             )
 *         } else {
 *             ListScreen(
 *                 modifier = Modifier.sharedElement(
 *                     rememberSharedContentState(key = "item-key"),
 *                     animatedVisibilityScope = this
 *                 )
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * === 구현 예시 ===
 * 
 * 1. 이미지 확대 전환:
 *    - 썸네일 → 전체 화면 이미지로 부드럽게 확대.
 *    - `sharedElement`로 이미지를 공유.
 *    - `sharedBounds`로 배경 컨테이너도 함께 애니메이션.
 *
 * 2. 리스트 → 상세 화면:
 *    - 리스트 아이템을 클릭하면 상세 화면으로 확장.
 *    - 이미지, 제목, 설명 등 여러 요소를 동시에 전환.
 *    - `renderInOverlayDuringTransition = false`로 겹침 방지.
 *
 * 3. 탭 전환:
 *    - 선택된 탭이 부드럽게 이동.
 *    - `boundsTransform`으로 spring 애니메이션 적용.
 *
 * 4. 카드 플립:
 *    - 카드의 앞뒤면을 전환하면서 크기와 위치도 변경.
 *    - `rotationY` 애니메이션과 함께 사용.
 *
 * === 주요 매개변수 ===
 * 
 * sharedElement() 매개변수:
 * - `state: SharedContentState`: 공유 요소의 상태 (key로 식별).
 * - `animatedVisibilityScope: AnimatedVisibilityScope`: 현재 AnimatedContent/Visibility의 Scope.
 * - `boundsTransform: BoundsTransform`: 크기/위치 변경 시 애니메이션 스펙.
 * - `placeHolderSize: PlaceHolderSize`: 전환 중 placeholder 크기.
 * - `renderInOverlayDuringTransition: Boolean`: 오버레이 렌더링 여부 (기본값: true).
 * - `zIndexInOverlay: Float`: 오버레이에서의 z-index.
 * - `clipInOverlayDuringTransition: OverlayClip`: 오버레이 클리핑 설정.
 *
 * sharedBounds() 매개변수:
 * - `sharedContentState: SharedContentState`: 공유 영역의 상태.
 * - `animatedVisibilityScope: AnimatedVisibilityScope`: 현재 Scope.
 * - `boundsTransform: BoundsTransform`: 경계 변경 애니메이션.
 * - `resizeMode: ResizeMode`: 크기 조정 모드 (ScaleToBounds, RemeasureToBounds).
 * - `clipInOverlayDuringTransition: OverlayClip`: 클리핑 설정.
 *
 * === 사용 시 주의사항 ===
 * 
 * 1. 고유 키(Key) 관리:
 *    - 각 공유 요소는 고유한 key를 가져야 합니다.
 *    - 양쪽 화면에서 동일한 key를 사용해야 전환이 작동합니다.
 *    - 동적 리스트에서는 아이템 ID를 key로 사용하세요.
 *
 * 2. AnimatedVisibilityScope:
 *    - sharedElement는 AnimatedContent 또는 AnimatedVisibility 내부에서만 사용 가능.
 *    - `this@AnimatedContent`로 Scope를 전달해야 합니다.
 *
 * 3. 성능 최적화:
 *    - `renderInOverlayDuringTransition = false`로 불필요한 오버레이 렌더링 방지.
 *    - `skipToLookaheadSize()`로 레이아웃 재측정 최소화.
 *    - 복잡한 레이아웃에서는 `rememberSharedContentState`를 remember로 캐싱.
 *
 * 4. 중첩 전환:
 *    - 여러 요소를 동시에 전환할 때 z-index 관리 필요.
 *    - `zIndexInOverlay`로 레이어 순서 제어.
 *
 * 5. Navigation과 통합:
 *    - Navigation Compose와 함께 사용할 때는 NavHost의 Scope 전달.
 *    - `composable` 블록 내부에서 SharedTransitionLayout 사용.
 *
 * 6. 클리핑 설정:
 *    - `clipInOverlayDuringTransition`으로 전환 중 클리핑 제어.
 *    - 기본값은 부모 경계로 클리핑되므로 필요시 해제.
 *
 * === 커스터마이징 ===
 * 
 * BoundsTransform 커스터마이징:
 * ```kotlin
 * val boundsTransform = BoundsTransform { initialBounds, targetBounds ->
 *     tween(durationMillis = 500, easing = FastOutSlowInEasing)
 * }
 * ```
 *
 * Spring 애니메이션:
 * ```kotlin
 * val boundsTransform = BoundsTransform { _, _ ->
 *     spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
 * }
 * ```
 *
 * === 요약 ===
 * - `SharedTransitionLayout`으로 공유 전환 영역 생성.
 * - `Modifier.sharedElement()`로 공유할 요소 지정.
 * - `rememberSharedContentState(key)`로 고유 키 생성.
 * - `AnimatedContent`로 상태 전환 관리.
 * - `boundsTransform`으로 애니메이션 커스터마이징.
 * - Navigation, LazyList 등과 통합 가능.
 */
object SharedElementTransitionGuide {
    const val GUIDE_INFO = """
        Shared Element Transitions - Jetpack Compose
        
        이 예제는 Jetpack Compose의 Shared Element Transitions를 사용하여
        화면 간 요소를 부드럽게 전환하는 방법을 보여줍니다.
        
        핵심 API:
        - SharedTransitionLayout: 공유 전환의 최상위 컨테이너
        - Modifier.sharedElement(): 공유할 요소에 적용
        - Modifier.sharedBounds(): 공유 영역의 경계 정의
        - AnimatedContent: 상태 전환 관리
        
        구현된 효과:
        1. Image Expansion: 썸네일 → 전체 화면 이미지 확대
        2. List to Detail: 리스트 아이템 → 상세 화면 확장
        3. Tab Transition: 선택된 탭이 부드럽게 이동
        4. Card Flip: 카드 앞뒤면 전환
        
        주요 개념:
        - 고유 키(Key)로 공유 요소 식별
        - AnimatedVisibilityScope 전달
        - boundsTransform으로 애니메이션 커스터마이징
        - renderInOverlayDuringTransition으로 렌더링 최적화
        - Navigation, LazyList와 통합 가능
    """
}

@Preview
@Composable
fun SharedElementTransitionGuidePreview() {
    // Preview for the guide content if needed
}
