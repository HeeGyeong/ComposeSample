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
 *
 * ## MetricStyleNotificationExampleUI (Notification MetricStyle, androidx.core 1.19)
 * - core 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/core
 * - NotificationCompat.MetricStyle: https://developer.android.com/reference/androidx/core/app/NotificationCompat.MetricStyle
 * - NotificationCompat.Metric: https://developer.android.com/reference/androidx/core/app/NotificationCompat.Metric
 * - Notification.MetricStyle(플랫폼, API 37): https://developer.android.com/reference/android/app/Notification.MetricStyle
 * - 소스(javadoc 원문): https://dl.google.com/android/maven2/androidx/core/core/1.19.1/core-1.19.1-sources.jar
 *
 * 핵심 개념:
 * - 운동 기록·타이머·날씨처럼 시간에 따라 바뀌는 수치를 "라벨 + 값" 지표로 묶는 스타일. 펼친 알림에 최대 3개가
 *   보이고, 승격(FLAG_PROMOTED_ONGOING) 알림이면 critical 지표 하나가 상태 바 칩에 들어갈 수 있다(javadoc).
 *   setLargeIcon 의 큰 아이콘은 이 스타일에서 표시되지 않는다.
 * - 값 타입 6종: FixedInt(값, 단위) · FixedFloat(값, 단위, 소수 자릿수 min..max, 0..6, 기본 0..2) ·
 *   FixedText(값, 단위) · FixedDate(LocalDate, AUTOMATIC=0/LONG_DATE=1/SHORT_DATE=2) · FixedTime(LocalTime) ·
 *   TimeDifference(forTimer/forStopwatch = Instant 또는 elapsedRealtime 기준 실시간 갱신,
 *   forPausedTimer/forPausedStopwatch = Duration 고정값, 포맷 ADAPTIVE=1/CHRONOMETER=2).
 * - Metric(value, label, semanticStyle): semantic style 은 NotificationCompat.SEMANTIC_STYLE_INFO/SAFE/CAUTION/DANGER
 *   (1~4, 기본 UNSPECIFIED=0). 승격 알림에서만 색으로 적용된다. 텍스트용은 createSemanticStyleAnnotation(style)
 *   (android.text.Annotation, 키 android.app.notification.semanticStyle).
 * - ⚠️ API 계약(core 1.19.1 바이트코드·소스 확인):
 *   · MetricStyle·Metric 클래스는 java.time 때문에 @RequiresApi(26). 이 프로젝트는 desugaring 을 쓰지 않으므로
 *     minSdk 24 의 release 에서는 26 미만 분기가 필요하다.
 *   · MetricStyle() 생성자와 TimeDifference 의 6개 팩토리는 @RequiresApi(37) → lint(NewApi)가 37 분기를 강제한다.
 *     javadoc 은 "하위 버전에서는 content text 만 보인다"고 설명하지만, lint 를 지키면 37 미만에서는 MetricStyle
 *     객체 자체를 만들지 않게 된다.
 *   · apply() 는 validate() 후 SDK_INT ≥ 37 일 때만 Api37Impl.toPlatformStyle() 로 플랫폼 스타일을 붙이고,
 *     미만에서는 아무것도 하지 않는다. ProgressStyle 의 setProgress() 같은 축약 렌더가 없다
 *     → 37 미만 폴백(content text, 크로노미터)은 앱 몫이다.
 *   · addCompatExtras() 는 SDK_INT < 37 일 때만 android.metrics(Bundle 목록) · android.metrics.criticalIndex 를 싣는다.
 *     37 이상에서는 바로 return 한다(플랫폼 스타일이 대신 들고 있다).
 *   · validate() 는 지표 0개면 IllegalArgumentException("A MetricStyle must have at least one Metric") —
 *     SDK 분기 앞이라 37 미만에서도 build() 가 실패한다.
 *   · Metric 생성자는 label 이 공백이면 IAE("Metric label is required"). label·unit 은 생성 시점에
 *     String 으로 바뀐다(safeCharSequenceToString — 스팬 제거, 5120자 절삭).
 *   · FixedFloat 자릿수는 0..6, min ≤ max 를 Preconditions 로 검사. TimeDifference 포맷은 1..2 만 허용
 *     (0 이면 "Invalid format: 0").
 *   · setCriticalMetric(index) 는 범위를 검사하지 않는다(기본 0, METRIC_INDEX_NONE=-1). 범위 밖이면
 *     getCriticalMetric() 이 null. 37 미만 extras 에는 정수가 그대로, Api37Impl 은
 *     getMetrics().indexOf(getCriticalMetric()) 를 넘긴다 → 범위 밖은 -1, 같은 지표(equals)가 둘이면 첫 번째로 바뀐다.
 *   · FixedTime 은 생성자에서 truncatedTo(SECONDS), 표시는 시:분만. extras 에는 toSecondOfDay(long) 로 저장.
 *     FixedDate 는 toEpochDay, TimeDifference 는 zeroTime=toEpochMilli · pausedDuration=toMillis 로 저장돼
 *     ms 아래가 사라진다.
 * - 37 미만에서 extras 에 싣는 이유: 공개 생성자 NotificationCompat.Builder(context, notification) 가 기존 알림에서
 *   Builder 를 되살릴 때 COMPAT_TEMPLATE(androidx.core.app.NotificationCompat${'$'}MetricStyle) 로 스타일을 만들고
 *   restoreFromCompatExtras() 로 지표를 복원한다. Style.extractStyleFromNotification() 은 @RestrictTo 라 앱에서 쓰지 않는다.
 * - 37 미만 폴백 권장 구성(예제의 buildMetricNotification): content text 에 지표 요약 +
 *   시간 지표는 setWhen + setUsesChronometer(+ 타이머면 setChronometerCountDown(true)) 로 시스템이 갱신하게 한다.
 *   폴백 문구 자체는 발행 시점 값으로 고정된다.
 * - lint 는 잘못된 값 중 FixedFloat 자릿수 7(Range)·TimeDifference format 0(WrongConstant)만 컴파일 시점에 오류로
 *   잡는다. min 3 > max 1 같은 인자 사이 관계·공백 label·범위 밖 critical 인덱스는 통과한다(예제는 측정용으로 억제).
 * - 실측(SM-A725F / Android 13, API 33):
 *   · 앱 경로로 실제 발행한 알림(dumpsys notification): android.metrics 없음, android.text = 폴백 요약,
 *     배달(critical = 타이머) 시나리오에서 showChronometer=true · chronometerCountDown=true · when = 타이머 끝 시각.
 *     폴백을 끄면 android.text=null · showChronometer=false 로 제목만 남는다.
 *   · 같은 입력의 강제 부착 빌드: android.text 없음, android.metrics 4개(러닝)/3개(배달),
 *     criticalIndex 0/1, COMPAT_TEMPLATE = NotificationCompat${'$'}MetricStyle. 원소 예: 거리 · semantic=2 ·
 *     FixedFloat(5.27, km, 2..2) / 남은 시간 · semantic=3 · TimeDifference(zeroTime=epoch ms, countDown=true, format=1).
 *   · critical 5(지표 3개): getCriticalMetric()=null, 플랫폼에 넘길 값 -1, extras 와 왕복 뒤 모두 5.
 *     같은 지표를 끝에 하나 더 붙여 마지막(3·4)을 critical 로 지정하면 플랫폼에 넘길 값은 0.
 *   · 생성 검증: 지표 0개 build()·공백 label·자릿수 3/1·자릿수 7·format 0 → 전부 IllegalArgumentException,
 *     setCriticalMetric(5) 는 예외 없음, FixedTime(09:30:15.5) → 09:30:15,
 *     Instant(…, nano 123456789) → extras zeroTime …123ms(456789ns 손실).
 *   · Builder(context, notification) 왕복: 지표 4→4 · criticalIndex 0→0 · 원소까지 동일.
 *   · 지표 렌더·칩·semantic 색은 API 37 기기에서만 확인 가능(미검증).
 */
