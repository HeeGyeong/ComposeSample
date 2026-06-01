package com.example.composesample.presentation.example.component.system.platform.haptic

/**
 * System/Platform/Haptic 예제 참고 자료
 *
 * ## HapticFeedbackExampleUI (햅틱 피드백)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/touch-input/haptics
 * - 출처: https://medium.com (Android Weekly 햅틱 비교)
 * 핵심 개념:
 * - LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress) — Compose 표준 API
 * - View.performHapticFeedback(HapticFeedbackConstants.XXX) — 플랫폼 상수 기반 (더 다양한 종류)
 * - API 레벨별 지원 범위 차이: 일부 HapticFeedbackConstants는 상위 API에서만 동작
 * - Compose HapticFeedbackType vs 플랫폼 Constants 매핑 비교
 */
