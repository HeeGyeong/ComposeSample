package com.example.composesample.presentation.example.component.system.platform.powersave

/**
 * System/Platform/PowerSave 예제 참고 자료
 *
 * ## PowerSaveModeExampleUI — '절전 모드' 탭 (절전 모드 감지 & 배터리 최적화)
 * - 공식 문서(Doze·App Standby): https://developer.android.com/training/monitoring-device-state/doze-standby
 * - 전원 관리 개요(배터리 세이버·App Standby 버킷, Android 9 기준): https://developer.android.com/about/versions/pie/power
 * 핵심 개념:
 * - PowerManager.isPowerSaveMode 로 현재 절전 모드 여부 조회
 * - ACTION_POWER_SAVE_MODE_CHANGED BroadcastReceiver 로 변경 실시간 감지
 * - DisposableEffect(Unit) { register; onDispose { unregister } } 로 receiver 누수 방지 (RECEIVER-01)
 *   - Composable 이탈 시 observer가 안전하게 해제되도록 DisposableEffect 사용
 * - 절전 모드에 따라 애니메이션/백그라운드 작업 강도 조절
 *
 * ## PowerSaveModeExampleUI — '백그라운드 제한 진단' 탭 (BackgroundRestrictionTab · ViewModel · Core)
 * - 출처(AW #750, Rushi Patel): https://signops.dev/blog/push-notifications-xiaomi-oppo-vivo/
 * - 전원 관리 자원 제한 표(버킷·기기 상태별 작업·알람·네트워크·FCM): https://developer.android.com/topic/performance/power/power-details
 * - 앱 대기 버킷: https://developer.android.com/topic/performance/appstandby
 * - ActivityManager(isBackgroundRestricted · getHistoricalProcessExitReasons · getHistoricalProcessStartReasons): https://developer.android.com/reference/android/app/ActivityManager
 * - ApplicationExitInfo: https://developer.android.com/reference/android/app/ApplicationExitInfo
 * - ApplicationStartInfo: https://developer.android.com/reference/android/app/ApplicationStartInfo
 * - UsageStatsManager(getAppStandbyBucket · queryEventsForSelf): https://developer.android.com/reference/android/app/usage/UsageStatsManager
 * - 제조사별 절전 설정 안내(공개 API 없음): https://dontkillmyapp.com/
 * 핵심 개념:
 * - 막는 층: 배터리 세이버 · Doze(깊은 isDeviceIdleMode / 가벼운 isDeviceLightIdleMode, API 33) · 앱 대기 버킷(getAppStandbyBucket, 28)
 *   · 사용자의 '제한'(isBackgroundRestricted, 28) · 배터리 최적화 예외(isIgnoringBatteryOptimizations) · 강제 중지(종료 기록 30 · wasForceStopped 35)
 *   · 저전력 대기(isLowPowerStandbyEnabled 33 · isExemptFromLowPowerStandby 34)
 * - 버킷·제한·예외 목록은 바뀌어도 브로드캐스트가 없다 → 화면 복귀(LifecycleResumeEffect)마다 다시 읽는다.
 *   세이버·Doze·가벼운 Doze·저전력 대기는 런타임 등록 리시버로 받는다(ViewModel 의 이름 있는 BroadcastReceiver)
 * - 앱이 앞에 있으면 버킷은 거의 늘 ACTIVE — 백그라운드 때의 버킷은 queryEventsForSelf 의 STANDBY_BUCKET_CHANGED 이력으로(권한 불필요)
 * - REASON_USER_REQUESTED 는 강제 중지·최근 앱에서 제거·am kill·(Android 14 전) 업데이트에 함께 쓰인다 — 시스템 설명 문자열로만 갈린다.
 *   REASON_USER_STOPPED 는 '사용자가 앱을 멈춤'이 아니라 멀티 유저 기기의 사용자 중지
 * - getHistoricalProcessStartReasons 의 첫 기록이 이번 실행이라는 보장은 없다 → START_TIMESTAMP_FORK 를 Process.getStartUptimeMillis 와 대조(currentStart)
 * - 설정 '백그라운드 사용 허용' 끔 = RUN_ANY_IN_BACKGROUND ignore = isBackgroundRestricted true / '제한 없음' = 배터리 최적화 예외 + 버킷 5(숨은 EXEMPTED)
 * - ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 직접 요청은 Play 정책상 제한 → 목록 화면(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)·앱 정보로 보낸다
 * - 프레임워크 상수(STANDBY_BUCKET_* · REASON_* · START_REASON_*)는 값으로 옮겨 lint InlinedApi 를 피하고, 단위 테스트가 원본 상수와 대조한다
 */
