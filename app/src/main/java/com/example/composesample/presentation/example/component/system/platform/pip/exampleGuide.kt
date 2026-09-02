package com.example.composesample.presentation.example.component.system.platform.pip

/**
 * System/Platform/PiP 예제 참고 자료
 *
 * ## PictureInPictureExampleUI (Picture-in-Picture compat)
 * - PiP 개요: https://developer.android.com/develop/ui/views/picture-in-picture
 * - PictureInPictureParams: https://developer.android.com/reference/android/app/PictureInPictureParams
 * - PictureInPictureParamsCompat(core 1.18.0): https://developer.android.com/reference/androidx/core/app/PictureInPictureParamsCompat
 * - 자동 진입(autoEnter, API 31+): https://developer.android.com/develop/ui/views/picture-in-picture#auto-pip
 *
 * 핵심 개념:
 * - androidx.core **1.18.0** 신규 클래스 묶음: PictureInPictureParamsCompat(+Builder) /
 *   PictureInPictureProvider / PictureInPictureUiStateCompat / OnPictureInPictureUiStateChangedProvider /
 *   DisplayShapeCompat. aar-metadata 는 minCompileSdk=36 / minAGP=8.9.1.
 * - Builder 는 9개 필드(enabled=autoEnter, aspectRatio, actions, sourceRectHint, seamlessResize,
 *   closeAction, expandedAspectRatio, title, subTitle)를 **API 게이팅 없이** 받는다. 버전 분기는
 *   toPictureInPictureParams()(@RequiresApi(26)) 한 곳으로 모인다.
 * - ⚠️ 변환 시 잘라내는 규칙(바이트코드 확인): SDK_INT ≥ 33 → Api33Impl.create(9개 전부) /
 *   ≥ 31 → Api31Impl.create(aspectRatio·actions·sourceRectHint·seamlessResize·enabled = 5개) /
 *   그 외 → Api26Impl.create(aspectRatio·actions·sourceRectHint = 3개).
 *   **잘린 필드는 예외 없이 조용히 사라진다.**
 * - ⚠️ PictureInPictureParams 의 getter 9종(getAspectRatio/getTitle/getSubtitle/getActions/
 *   getSourceRectHint/getCloseAction/getExpandedAspectRatio/isSeamlessResizeEnabled/isAutoEnterEnabled)은
 *   **전부 API 33 부터**다(클래스 자체는 26부터). SDK 의 platforms/android-36/data/api-versions.xml 에서
 *   since="33" 으로 확인. 그래서 되읽기 카드는 API 33+ 에서만 동작한다.
 * - 실측(SM-A725F/Android 13, API 33): 9개 getter 전부 호출 가능하고 compat 이 넣은 값을 그대로 반환.
 *   플랫폼 toString() 은 `hasSetActions=true`(actions 를 한 번도 설정하지 않았는데도 — compat 이 항상
 *   빈 리스트를 넘기기 때문. getActions() 는 0개로 정직하다), `isLaunchIntoPip=false` 를 함께 찍는다.
 * - ⚠️ 종횡비 허용 범위(실측): 예외 메시지는
 *   "setPictureInPictureParams: Aspect ratio is too extreme (must be between 0.418410 and 2.390000)".
 *   239:100(=2.39) 통과 / 240:100(=2.4) 거부 / **4184:10000(=0.4184) 도 거부**.
 *   하한 0.418410 은 1/2.39 = 0.41841004… 를 반올림 표기한 값이라, 메시지와 같아 보이는 0.4184 는 미달이다.
 * - ⚠️ 상태 요구가 API 별로 다르다(실측): 정지 상태의 액티비티에서
 *   enterPictureInPictureMode() 는 IllegalStateException("Activity must be resumed to enter
 *   picture-in-picture") 를 던지지만, **같은 상태에서 setPictureInPictureParams() 는 성공**한다.
 *   → autoEnter(setEnabled(true), API 31+) 용 파라미터는 화면이 보이지 않아도 미리 심어 둘 수 있다.
 * - 매니페스트 전제: `android:supportsPictureInPicture="true"` 가 없으면 진입 자체가 되지 않고,
 *   `android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"` 이 없으면
 *   PiP 진입/복귀마다 액티비티가 재생성된다. 이 예제를 위해 BlogExampleActivity 에 두 속성을 추가했다.
 * - PiP 모드 감지는 액티비티를 수정하지 않고 화면에서 구독한다 — ComponentActivity 가
 *   OnPictureInPictureModeChangedProvider 를 구현하므로 addOnPictureInPictureModeChangedListener 로
 *   PictureInPictureModeChangedInfo 를 받는다(DisposableEffect 에서 해제).
 * - PictureInPictureProvider(core 1.18.0)는 액티비티가 직접 구현하는 인터페이스로,
 *   enterPictureInPictureMode(PictureInPictureParamsCompat)/setPictureInPictureParams(compat) 2개를 갖는다.
 *   이 프로젝트의 activity 1.10.0 ComponentActivity 는 아직 구현하지 않으므로, 예제는 compat 으로 만든
 *   파라미터를 변환해 플랫폼 API 를 직접 호출한다.
 * - PictureInPictureUiStateCompat(isStashed API 33+, isTransitioningToPip API 35+)는 PiP 창이
 *   화면 가장자리로 치워졌는지/진입 애니메이션 중인지를 알려준다. 콜백은 API 31+ 의
 *   onPictureInPictureUiStateChanged 라 액티비티 수정이 필요해 이 예제 범위 밖에 둔다.
 * - ⚠️ 미검증: 실제 PiP 창 진입/렌더는 이 사이클에서 자동 검증하지 못했다. 연결된 실기기(SM-A725F)가
 *   키가드로 잠겨 있어 ActivityScenario·createAndroidComposeRule 어느 쪽으로도 액티비티가 RESUMED 로
 *   올라가지 못했고(덕분에 위의 IllegalStateException 을 측정할 수 있었다), 대체로 부팅한 API 34
 *   에뮬레이터는 앱 APK 설치가 유지되지 않아 액티비티 해석 자체가 실패했다.
 *   파라미터 구성·변환·되읽기·종횡비 검증·상태 요구 차이는 모두 실기기에서 측정했다.
 */
