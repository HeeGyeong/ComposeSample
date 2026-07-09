package com.example.composesample.presentation.example.component.ui.notification

/**
 * Notification 예제 참고 자료
 *
 * ## SnapNotifyExampleUI (Compose Snackbar 실무 활용)
 * 핵심 개념:
 * - SnackbarHostState + rememberCoroutineScope + Scaffold(snackbarHost = { SnackbarHost(state) })로 기본 구조를 구성하고 showSnackbar()로 메시지를 표시
 * - actionLabel과 showSnackbar()의 반환값(SnackbarResult.ActionPerformed)으로 액션 버튼·실행취소(Undo) 패턴 구현
 * - 실전 활용 사례: 폼 검증 실패/성공 피드백, 네트워크 작업 로딩→완료 알림, 장바구니 추가/취소, 파일 삭제→복구
 * - 상태 호이스팅(SnackbarHostState를 상위에서 관리)과 remember로 불필요한 재생성을 막고, 코루틴 스코프 생명주기 관리로 메모리 누수 방지
 */
