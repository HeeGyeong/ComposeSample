package com.example.composesample.presentation.example.component.interaction.swipe

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Swipe to Dismiss with Material 3 Guide
 *
 * 출처: https://proandroiddev.com/swipe-to-dismiss-with-compose-material-3-38445e0143f7
 *
 * Material 3의 SwipeToDismissBox를 사용하여 스와이프 제스처로 아이템을 삭제하는 방법을 설명합니다.
 *
 * === Material 2 vs Material 3 비교 ===
 * 
 * Material 2 (androidx.compose.material):
 * - `SwipeToDismiss` composable
 * - `DismissState` / `rememberDismissState()`
 * - `DismissValue`: Default, DismissedToEnd, DismissedToStart
 * - `DismissDirection`: StartToEnd, EndToStart
 * - `FractionalThreshold` (Deprecated)
 *
 * Material 3 (androidx.compose.material3):
 * - `SwipeToDismissBox` composable
 * - `SwipeToDismissBoxState` / `rememberSwipeToDismissBoxState()`
 * - `SwipeToDismissBoxValue`: Settled, StartToEnd, EndToStart
 * - `SwipeToDismissBoxDefaults.positionalThreshold` (새로운 threshold 방식)
 * - enableDismissFromStartToEnd, enableDismissFromEndToStart로 방향 제어
 *
 * === 핵심 API ===
 * 
 * 1. SwipeToDismissBox:
 *    - Material 3의 스와이프-투-디스미스 컴포넌트.
 *    - `state`: SwipeToDismissBoxState로 스와이프 상태 관리.
 *    - `backgroundContent`: 스와이프 시 배경에 보이는 콘텐츠 (아이콘, 색상 등).
 *    - `content`: 실제 스와이프 가능한 콘텐츠.
 *    - `enableDismissFromStartToEnd`: 왼쪽에서 오른쪽 스와이프 허용 여부.
 *    - `enableDismissFromEndToStart`: 오른쪽에서 왼쪽 스와이프 허용 여부.
 *
 * 2. SwipeToDismissBoxState:
 *    - `currentValue`: 현재 상태 값 (Settled, StartToEnd, EndToStart).
 *    - `targetValue`: 목표 상태 값.
 *    - `requireOffset()`: 현재 스와이프 오프셋.
 *    - `reset()`: 상태를 Settled로 리셋.
 *    - `dismiss()`: 스와이프 애니메이션 완료.
 *    - `confirmValueChange: (SwipeToDismissBoxValue) -> Boolean`: 상태 변경 승인 콜백.
 *
 * 3. SwipeToDismissBoxValue:
 *    - `Settled`: 스와이프가 완료되지 않은 기본 상태.
 *    - `StartToEnd`: 왼쪽에서 오른쪽으로 스와이프 완료.
 *    - `EndToStart`: 오른쪽에서 왼쪽으로 스와이프 완료.
 *
 * 4. SwipeToDismissBoxDefaults:
 *    - `positionalThreshold`: 스와이프 임계값 설정 (기본값: 56.dp).
 *
 * === 구현 예시 ===
 * 
 * 1. 기본 Swipe to Dismiss:
 *    - `rememberSwipeToDismissBoxState()`로 상태 생성.
 *    - `backgroundContent`에 삭제 아이콘과 배경색 표시.
 *    - `content`에 실제 아이템 표시.
 *
 * 2. 양방향 Swipe:
 *    - StartToEnd: 즐겨찾기 추가/제거 등의 동작.
 *    - EndToStart: 아이템 삭제 동작.
 *    - `currentValue`로 방향 구분하여 다른 아이콘/색상 표시.
 *
 * 3. 조건부 Swipe:
 *    - `confirmValueChange` 콜백에서 특정 조건일 때만 스와이프 허용.
 *    - 예: 특정 아이템은 삭제 불가능하도록 설정.
 *
 * 4. 애니메이션 커스터마이징:
 *    - `requireOffset()`으로 현재 오프셋 값 확인.
 *    - 오프셋에 따라 배경 색상, 아이콘 크기 등을 동적으로 변경.
 *
 * === 주요 개선 사항 (Material 2 대비) ===
 * 
 * 1. 더 직관적인 API:
 *    - `SwipeToDismissBox`라는 명확한 이름.
 *    - `enableDismissFromStartToEnd` / `enableDismissFromEndToStart`로 방향 제어 간소화.
 *
 * 2. 더 나은 Threshold 설정:
 *    - `FractionalThreshold` (Deprecated) 대신 `positionalThreshold` 사용.
 *    - dp 단위로 직관적으로 설정 가능.
 *
 * 3. 향상된 상태 관리:
 *    - `reset()`, `dismiss()` 함수로 상태 제어가 더 명확.
 *
 * 4. Material 3 디자인 가이드 준수:
 *    - Material 3의 디자인 토큰과 스타일 자동 적용.
 *
 * === 사용 시 주의사항 ===
 * 
 * 1. LazyColumn/LazyRow에서 사용 시:
 *    - 아이템 삭제 후 LaunchedEffect에서 delay를 주어 애니메이션 완료 대기.
 *    - 아이템 삭제 시 상태 관리를 정확히 해야 recomposition 이슈 방지.
 *
 * 2. 접근성(Accessibility):
 *    - 스와이프 외에도 삭제 버튼 등의 대체 수단 제공 필요.
 *    - `contentDescription` 등 접근성 속성 추가.
 *
 * 3. 성능:
 *    - 많은 아이템이 있을 경우 `key` 파라미터 사용하여 recomposition 최적화.
 *
 * === 요약 ===
 * - Material 3의 `SwipeToDismissBox`는 Material 2 대비 더 직관적이고 강력한 API 제공.
 * - `SwipeToDismissBoxState`로 스와이프 상태 관리, `confirmValueChange`로 조건부 스와이프 구현.
 * - `positionalThreshold`로 스와이프 임계값 설정, `enableDismissFrom*`로 방향 제어.
 * - `backgroundContent`와 `content`로 명확한 레이어 구분.
 */
object SwipeToDismissM3Guide {
    const val GUIDE_INFO = """
        Swipe to Dismiss with Material 3
        
        이 예제는 Material 3의 SwipeToDismissBox를 사용하여
        스와이프 제스처로 아이템을 삭제하거나 다른 동작을 수행하는 방법을 보여줍니다.
        
        핵심 API:
        - SwipeToDismissBox: Material 3의 스와이프-투-디스미스 컴포넌트
        - SwipeToDismissBoxState: 스와이프 상태 관리
        - SwipeToDismissBoxValue: Settled, StartToEnd, EndToStart
        
        구현된 기능:
        1. Basic Swipe to Dismiss: 간단한 단방향 스와이프 삭제
        2. Two-Way Swipe: 양방향 스와이프로 각기 다른 동작
        3. Conditional Swipe: 특정 조건에서만 스와이프 허용
        4. Animated Background: 스와이프에 따라 변하는 배경 애니메이션
        
        Material 2 대비 개선사항:
        - 더 직관적인 API (SwipeToDismissBox)
        - positionalThreshold로 dp 단위 임계값 설정
        - enableDismissFrom*로 방향 제어 간소화
        - reset(), dismiss() 함수로 명확한 상태 제어
    """
}

@Preview
@Composable
fun SwipeToDismissM3GuidePreview() {
    // Preview for the guide content if needed
}
