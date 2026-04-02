package com.example.composesample.presentation.example.component.architecture.pattern.mvi

/**
 * MVI (Model-View-Intent) Example 참고 자료
 *
 * - Android MVI 가이드: https://developer.android.com/topic/architecture/ui-layer
 *
 * MVI 구성 요소:
 * - Model (UiState)  — UI가 렌더링하는 불변 상태 (data class / sealed interface)
 * - View             — UiState를 소비하는 Composable, Intent를 ViewModel로 전달
 * - Intent (UiEvent) — 사용자 액션 또는 시스템 이벤트 (sealed class)
 *
 * 단방향 데이터 흐름 (UDF):
 * ```
 * View → (Intent/Event) → ViewModel → (새 UiState) → View
 * ```
 *
 * 추가 구성 요소 (3-layer MVI):
 * - UiEffect(SideEffect) — 1회성 이벤트 (토스트, 화면 전환, 스낵바)
 *   SharedFlow로 관리 (StateFlow는 재방출되므로 부적합)
 *
 * 상태 설계 원칙:
 * - UiState는 항상 완전한 스냅샷 — 부분 상태 없음
 * - copy()를 활용한 불변 상태 변환
 * - Loading / Success / Error를 sealed class로 표현 (또는 개별 flag)
 *
 * Compose와 연동:
 * - collectAsStateWithLifecycle()로 생명주기 인식 수집
 * - LaunchedEffect로 UiEffect 수신: viewModel.effect.collect { handle(it) }
 */
