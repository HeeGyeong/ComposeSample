package com.example.composesample.presentation.example.component.system.background.location

/**
 * System/Background/Location 예제 참고 자료
 *
 * ## BackgroundLocationExampleUI (Foreground Service + WorkManager 위치 추적)
 * - Foreground Service 개요: https://developer.android.com/develop/background-work/services/fgs
 * - 서비스 타입 선언: https://developer.android.com/develop/background-work/services/fgs/service-types
 * - Android 14 FGS 변경사항: https://developer.android.com/about/versions/14/changes/fgs-types-required
 * - Android 12 백그라운드 시작 제한: https://developer.android.com/about/versions/12/foreground-services
 * - 백그라운드 위치 권한: https://developer.android.com/develop/sensors-and-location/location/permissions/background
 * - 알림 런타임 권한(API 33+): https://developer.android.com/develop/ui/views/notifications/notification-permission
 * - WorkManager expedited work: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#expedited
 *
 * ### 권한 단계 (순서를 건너뛸 수 없음)
 * - ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION — 앱이 보이는 동안의 위치.
 *   Android 14+ 는 location 타입 FGS 를 시작하는 시점에 이 권한이 granted 여야 한다(아니면 SecurityException).
 * - POST_NOTIFICATIONS (API 33+) — 없어도 서비스는 실행되지만 FGS 알림이 보이지 않는다.
 *   "서비스 실행 가능 여부"와 "알림 표시 여부"는 별개라는 점이 헷갈리기 쉬운 지점.
 * - ACCESS_BACKGROUND_LOCATION (API 29+) — Android 10 은 런타임 다이얼로그로 요청 가능하지만,
 *   Android 11+ 는 다이얼로그가 뜨지 않아 앱 설정 화면으로 유도해 "항상 허용"을 직접 고르게 해야 한다.
 *   Foreground Service 로 추적하는 경우에는 이 권한이 없어도 동작한다.
 *
 * ### Service 수명주기에서 자주 깨지는 지점
 * - 5초 룰: startForegroundService() 이후 5초 안에 startForeground() 미호출 시
 *   ForegroundServiceDidNotStartInTimeException 으로 프로세스 종료.
 * - START_STICKY 재생성 시 onStartCommand 의 intent 가 null 로 들어온다(액션 분기에서 반드시 처리).
 * - Android 12+ 는 앱이 백그라운드일 때 서비스 시작 시 ForegroundServiceStartNotAllowedException.
 * - onDestroy 에서 LocationManager.removeUpdates + CoroutineScope.cancel 을 함께 수행.
 *
 * ### minSdk 24 호환 주의
 * - LocationListener 는 API 30 부터 onStatusChanged/onProviderEnabled/onProviderDisabled 에
 *   기본 구현이 생겼다. API 29 이하에서는 여전히 추상 메서드이므로, SAM 변환으로 만든 리스너를
 *   넘기면 시스템 콜백 시 AbstractMethodError 가 발생한다 → 익명 객체로 4개 모두 구현할 것.
 *
 * ### WorkManager 로는 대체할 수 없는 이유
 * - PeriodicWorkRequest 최소 주기는 15분이며, 시스템이 배터리 상태에 따라 더 미룬다.
 * - expedited work 는 API 30 이하에서 androidx.work 의 SystemForegroundService 를 통해 승격되는데,
 *   work-runtime 2.9.1 의 매니페스트는 그 서비스에 foregroundServiceType 을 선언하지 않는다.
 *   위치 타입으로 승격하려면 라이브러리 서비스 선언을 앱 매니페스트에서 override 해야 한다.
 * - 반대로 실행 조건(Constraints: 충전 중 / 배터리 여유 / 네트워크)과 프로세스 사망 후 재실행 보장은
 *   Service 로는 직접 만들 수 없는 WorkManager 의 강점이다.
 */
