package com.example.composesample.presentation.example.component.interaction.clickevent

/**
 * Interaction/ClickEvent 예제 참고 자료
 *
 * ## ClickEventUI (클릭 이벤트 처리 & 중복 방지)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/touch-input/pointer-input/tap-and-press
 * 핵심 개념:
 * - 빠른 연속 탭(더블 클릭) 중복 처리 방지: 마지막 클릭 시각을 기억해 throttle
 * - clickable vs pointerInput(detectTapGestures): 단순 클릭 vs 제스처 세분화
 * - remember로 마지막 클릭 타임스탬프 보존, 일정 간격 미만 클릭 무시
 * - 디바운스/스로틀 차이: 디바운스는 마지막 입력만, 스로틀은 일정 주기 1회만 처리
 */
