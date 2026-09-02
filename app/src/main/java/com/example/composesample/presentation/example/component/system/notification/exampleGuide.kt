package com.example.composesample.presentation.example.component.system.notification

/**
 * System/Notification 예제 참고 자료
 *
 * ## LiveUpdateNotificationExampleUI (Android 16 Live Updates 알림)
 * - Live Updates 개요: https://developer.android.com/about/versions/16/features/progress-centric-notifications
 * - NotificationCompat.ProgressStyle: https://developer.android.com/reference/androidx/core/app/NotificationCompat.ProgressStyle
 * - Notification.ProgressStyle(플랫폼): https://developer.android.com/reference/android/app/Notification.ProgressStyle
 * - 알림 채널/권한: https://developer.android.com/develop/ui/views/notifications/channels
 *
 * 핵심 개념:
 * - Live Updates 는 "진행 중인 일"을 알림 그늘 밖(상태 바 칩·잠금 화면)으로 승격시키는 Android 16 기능이다.
 *   그 진행 상황을 그리는 스타일이 ProgressStyle 이고, androidx.core 1.17.0 부터 compat 으로 제공된다.
 * - setProgress(max, progress, indeterminate) 는 단색 막대 하나뿐이지만 ProgressStyle 은
 *   세그먼트(길이+색), 포인트(위치 마커), 트래커/시작/끝 아이콘, styledByProgress 를 갖는다.
 * - ⚠️ progressMax 에는 설정자가 없다. getProgressMax() 가 세그먼트 길이의 합을 그 자리에서 계산하며
 *   (길이 0 이하는 제외, Math.addExact 오버플로 시 100 으로 폴백), 세그먼트가 없으면 100 이다.
 *   → 진행률의 축이 세그먼트 구성에 종속된다.
 * - ⚠️ API 36 미만 폴백(바이트코드 확인): ProgressStyle.apply() 는 SDK_INT >= 36 이면 플랫폼
 *   Notification.ProgressStyle 로 전부 이관하고, 미만이면
 *   Notification.Builder.setProgress(getProgressMax(), min(progress, max), indeterminate) 한 줄로 축약한다.
 *   세그먼트·포인트·아이콘은 렌더되지 않는다.
 * - 다만 값이 사라지는 것은 아니다. NotificationCompatBuilder.build() 가 Style.addCompatExtras(extras) 를
 *   호출해 android.progressSegments / android.progressPoints / android.progress / android.progressMax /
 *   android.progressIndeterminate / android.styledByProgress / android.progressTrackerIcon 을 extras 에 싣는다.
 *   → 하위 버전 기기에서도 빌드된 Notification 의 extras 를 읽으면 무엇이 담겼는지 실측할 수 있다(예제 3번 카드).
 * - ⚠️ 다만 extras 의 android.progress 는 "렌더된 값"이 아니라 "요청값"이다. 폴백의 setProgress() 가
 *   min(progress, max) 를 먼저 쓰고, 그 뒤 addCompatExtras() 가 원본 progress 로 덮어쓰기 때문.
 *   실측(SM-A725F/Android 13): progress=999, max=120 → extras 의 android.progress 는 999.
 * - 실측(SM-A725F/Android 13, API 33) 요약: canPostPromotedNotifications()=false,
 *   세그먼트 4개·포인트 2개·트래커 아이콘·requestPromotedOngoing=true·shortCriticalText="12분" 이
 *   모두 extras 에 남고, COMPAT_TEMPLATE 은 androidx.core.app.NotificationCompat${'$'}ProgressStyle 이었다.
 * - progressMax 실측: 빈 스타일 100 / 배달 시나리오 4구간(20+40+15+45) 120 / 세그먼트 제거 100 /
 *   Segment(0)+Segment(30) 은 30(길이 0 은 합계에서 빠지지만 목록이 비지는 않으므로 기본값 100 이 아니다).
 * - setRequestPromotedOngoing(true) 에는 버전 분기가 없다. extras 에 android.requestPromotedOngoing
 *   boolean 을 넣는 것이 전부이며, 실제 승격 판단은 Android 16 시스템 몫이다.
 * - setShortCriticalText(String) 은 상태 바 칩에 들어갈 짧은 문구다. SDK_INT < 36 이면 extras
 *   (android.shortCriticalText)에만 남는다.
 * - NotificationManagerCompat.canPostPromotedNotifications() 는 SDK_INT < 36 이면 플랫폼을 호출하지 않고
 *   무조건 false 를 반환한다 → false 를 "사용자가 승격을 껐다"로 해석하면 안 된다.
 * - 알림 자체의 전제 조건: 채널 생성(NotificationChannelCompat 은 API 26 미만 no-op),
 *   API 33+ 의 POST_NOTIFICATIONS 권한(없으면 notify() 가 예외 없이 무시됨), 진행 알림이면 setOngoing(true).
 * - 진행률이 바뀔 때마다 notify() 를 부르면 시스템이 알림 갱신을 스로틀링한다 — 갱신 간격을 둬야 한다.
 *   (같은 이유로 프로젝트의 LocationTrackingService 도 5초 간격으로만 알림을 다시 그린다.)
 * - 빌드 환경: ProgressStyle 은 androidx.core 1.17.0 이 요구하는 compileSdk 36 이 필요하다.
 *   이 프로젝트는 targetSdk 는 35 로 두고 compileSdk 만 36 으로 올렸다.
 */
