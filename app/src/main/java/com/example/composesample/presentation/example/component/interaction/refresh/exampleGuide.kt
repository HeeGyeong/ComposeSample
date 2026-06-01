package com.example.composesample.presentation.example.component.interaction.refresh

/**
 * Interaction/Refresh 예제 참고 자료
 *
 * ## PullToRefreshUI (Pull-to-Refresh)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/components/pull-to-refresh
 * 핵심 개념:
 * - Material3 PullToRefreshBox / pullToRefresh modifier 로 당겨서 새로고침 구현
 * - isRefreshing 상태를 ViewModel이 보유, onRefresh 콜백에서 데이터 재요청
 * - 새로고침 인디케이터 애니메이션과 스크롤 제스처 연동
 * - 비동기 작업 완료 시 isRefreshing=false 로 인디케이터 종료
 */
