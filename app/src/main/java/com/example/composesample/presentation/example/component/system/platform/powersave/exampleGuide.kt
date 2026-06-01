package com.example.composesample.presentation.example.component.system.platform.powersave

/**
 * System/Platform/PowerSave 예제 참고 자료
 *
 * ## PowerSaveModeExampleUI (절전 모드 감지 & 배터리 최적화)
 * - 공식 문서: https://developer.android.com/develop/background-work/background-tasks/power-management
 * 핵심 개념:
 * - PowerManager.isPowerSaveMode 로 현재 절전 모드 여부 조회
 * - ACTION_POWER_SAVE_MODE_CHANGED BroadcastReceiver 로 변경 실시간 감지
 * - DisposableEffect(Unit) { register; onDispose { unregister } } 로 receiver 누수 방지 (RECEIVER-01)
 *   - Composable 이탈 시 observer가 안전하게 해제되도록 DisposableEffect 사용
 * - 절전 모드에 따라 애니메이션/백그라운드 작업 강도 조절
 */
