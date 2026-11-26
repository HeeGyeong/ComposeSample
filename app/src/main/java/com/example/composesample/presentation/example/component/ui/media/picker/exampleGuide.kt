package com.example.composesample.presentation.example.component.ui.media.picker

/**
 * Media/Picker 예제 참고 자료
 *
 * ## EmbeddedPhotoPickerExampleUI (Embedded Photo Picker 개념 시연)
 * - 출처: https://android-developers.googleblog.com/2026/01/httpsandroid-developers.googleblog.com202506android-embedded-photo-picker.html%20.html
 * 핵심 개념:
 * - 시스템 Photo Picker를 앱 UI 안에 직접 임베드해 화면 이탈(전체 화면 전환) 없이 사진/영상 선택
 * - EmbeddedPhotoPickerFeatureInfo(Builder)로 최대 선택 수·순서 유지·강조색 설정, rememberEmbeddedPhotoPickerState로 선택완료/권한부여/권한해제 콜백 관리
 * - 선택한 항목에만 URI 접근 권한 부여(READ_MEDIA_IMAGES/VIDEO 불필요)로 프라이버시 강화, Android 14(API 34)+ SDK Extensions 15+ 필요
 *
 * ## EmbeddedPickerComposeExampleUI (Embedded Photo Picker의 Compose 통합 심화)
 * - 출처: https://victorbrandalise.com/embedded-android-photo-picker-in-jetpack-compose/
 * 핵심 개념:
 * - SurfaceView.setChildSurfacePackage로 시스템이 직접 렌더링(기존 Photo Picker와 동일한 보안 속성 유지), Activity가 resumed 상태 유지되어 선택 변경에 실시간 반응 가능
 * - BottomSheetScaffold + SideEffect로 피커의 펼침 상태(alpha01 setCurrentExpanded → alpha03 state.isExpanded)와 바텀시트 확장 상태 동기화
 * - (원문 주장 · alpha01 시절) deselectUri() 호출만으로는 onUriPermissionRevoked가 자동 트리거되지 않음 → 앱 state를 직접 갱신해야 함.
 *   alpha03 실물(아래 #154 절)에서는 deselectUri 뒤 약 0.5초에 회수 콜백이 왔다 — 앱 목록은 회수 콜백 한 곳에서 지우고,
 *   앱 목록만 지우면 피커에 선택 · 권한이 남는다는 오너십 분리(피커=선택 가능 항목, 앱=선택 결과 표현·영속화)는 그대로
 *
 * ## #154 두 예제의 '실물' 탭 — photopicker-embedded(-compose) 1.0.0-alpha03 (EmbeddedPickerRealCards · EmbeddedPickerRealLab)
 * - 공식 문서(임베디드 포토 피커): https://developer.android.com/training/data-storage/shared/photo-picker/embedded
 * - 릴리스 노트(alpha03 — photopicker · photopicker-compose · photopicker-testing 이 -embedded · -embedded-compose · -embedded-testing 으로,
 *   rememberEmbeddedPhotoPickerState 의 inputs 제거 → key 블록, onError · surfacePackage · notifyVisibilityChanged): https://developer.android.com/jetpack/androidx/releases/photopicker
 * - API(FeatureInfo.Builder — 메서드별 U 확장 요구치): https://developer.android.com/reference/android/widget/photopicker/EmbeddedPhotoPickerFeatureInfo.Builder
 * - 플랫폼 소스(SurfaceView.clearChildSurfacePackage · onDetachedFromWindow → releaseSurfaces(true)):
 *   https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-release/core/java/android/view/SurfaceView.java
 * - 라이브러리 소스(EmbeddedPhotoPicker 의 update 블록 — androidx-main 도 같은 코드):
 *   https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/photopicker/photopicker-embedded-compose/
 * 핵심 개념(실측: Pixel 8 API 36 에뮬레이터 — 이 작업에서 만든 AVD, Google Play 이미지 r07 · U 확장 17 · 피커 서비스 com.google.android.photopicker,
 * debug. 데모 사진 CS154_01~08.jpg 8장을 adb 로 넣고 scan_volume. Pixel Tablet API 35 는 U 확장 13 이라 조건 카드만):
 * - 조건: @RequiresApi(34) + @RequiresExtension(UPSIDE_DOWN_CAKE, 15), 라이브러리 minSdk 24. 분기는 SDK_INT >= 34 && SdkExtensions.getExtensionVersion(U) >= 15
 *   를 직접 써야 lint 가 가드로 본다. FeatureInfo 메서드별 요구 확장: 기본 15 · 하이라이트 19 · 펼친 채 시작 21 · 선택 파라미터 22 · 시작 탭 23.
 *   기존 가용성 배지는 SDK_INT >= 34 만 봐서 확장 13 인 API 35 기기도 '가용' 이었다 → 확장까지 보도록 고침
 * - 권한 콜백: 누를 때마다 onUriPermissionGranted 1회, URI content://media/picker/0/com.android.providers.media.photopicker/media/<id>, 받자마자 원본 바이트 그대로 읽힘.
 *   deselectUri → 약 0.5초 뒤 onUriPermissionRevoked · state.selectedMedia 에서 빠짐 · 피커 체크 해제와 순서 번호 재정렬, 회수 뒤 읽기 SecurityException
 * - 설정은 세션을 열 때 한 번: EmbeddedPhotoPicker 의 LaunchedEffect 키가 state · provider 뿐이라 바뀐 featureInfo 는 열린 세션에 안 간다(max 3 → 1 로 바꿔도
 *   세 번째 선택 OK · 네 번째 'Select up to 3 items'). View 판 setEmbeddedPhotoPickerFeatureInfo 도 세션이 없을 때만 연다 → setProvider 로 닫고 다시 연다
 * - 크래시(라이브러리 함정, API 36+): 같은 state 로 옛 세션이 살아 있는 동안 새 EmbeddedPhotoPicker 를 만들면(key · if 분기 · 부모 이동) 새 SurfaceView 의
 *   update 가 옛 surfacePackage 를 붙이고, 옛 SurfaceView 가 떨어지며 그 패키지를 release 하고, 세션 종료로 값이 null 이 되면 update 의
 *   clearChildSurfacePackage()(BAKLAVA+ 분기)가 풀린 패키지의 SurfaceControl(null)을 reparent → NullPointerException 으로 앱 종료.
 *   API 34 · 35 는 그 분기가 없어 죽지 않는다(소스). 우회: 피커를 치우고 surfacePackage == null 을 기다린 뒤 다시 놓는다(0.5~1.1초) — 또는 state 도 key 안에서 새로
 * - 다시 연 세션: Compose 판은 state.selectedMedia 를 미리 선택으로 넘겨 콜백 한 번에 다시 받고, 순서 번호는 고른 순서가 아니라 id 순. View 판은 선택이 비어
 *   있고(setPreSelectedUris 는 앱 몫) 회수 콜백 없이 앞 URI 가 계속 읽힌다
 * - max 1 은 체크 표시를 그리지 않는다(누르면 권한 · 다시 누르면 회수 · 다른 것은 'Select up to 1 items') — 미리 선택 3개로 max 1 세션을 열어도 표시 0
 * - 닫을 때 오류: 서비스가 메시지 없는 IllegalStateException(ParcelableException 으로 감싸 옴)을 onError · onSessionError 로 보낼 때가 있다
 *   (Compose 9번 중 4번 · View 2번 중 2번, 닫기 요청 뒤 20~600ms). 콜백이 state · 뷰 하나에 묶여 세션을 가리지 않으므로 '오류 → 대체 UI' 만 하면
 *   정상 재개에서도 넘어간다 — 탭은 닫기 요청 1초 안 오류를 닫은 세션 것으로 본다
 * - 오류 뒤: runSession 은 끝나지만 LaunchedEffect 키가 그대로라 새 세션을 열지 않는다(가짜 Provider 계측 — 0.5초 뒤에도 세션 1개 · surfacePackage null)
 * - 가시성: Compose 판은 스스로 notifyVisibilityChanged 를 안 부른다(소스 — 앱이 수명 주기로, 서비스 로그로 확인). View 판은 setVisibility ·
 *   onWindowVisibilityChanged 훅이 자동 전달(GONE → VISIBLE 에서 피커 그대로 복귀)
 * - 스크롤 안 배치: setZOrderOnTop SurfaceView 라 스크롤 영역 밖 헤더 · 탭 위에 그대로 그려진다(Compose clip 이 다른 서피스에 안 걸림) — 피커는 고정 영역 · 시트에
 * - 펼침: isExpanded = true 면 Photos · Albums 탭과 '선택한 사진에만 접근' 안내. 확장 21 미만은 라이브러리가 세션을 연 뒤 notifyPhotoPickerExpanded 로 보냄
 * - photopicker-activity(PickVisualMedia 이관)는 activity 1.14.0-alpha03 을 요구해 넣지 않았다
 * - HotSwan: PickerLabState 의 기본 인자 now = SystemClock::uptimeMillis 를 인터프리터가 Function0 필드에 넣지 못해 IllegalArgumentException →
 *   Compose 가 live edit 오류로 삼켜 화면이 멈췄다(탭 · 뒤로 가기 무반응). build.gradle 에서 PickerLabState 만 제외
 *
 * ## SystemPhotoPickerExampleUI (시스템 Photo Picker 실물 — PickVisualMedia / PickMultipleVisualMedia)
 * - 공식 문서(Photo picker — 기기 조건 · Google Play 서비스 백포트 · 영구 접근): https://developer.android.com/training/data-storage/shared/photopicker
 * - API: https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.PickVisualMedia
 * - API: https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.PickMultipleVisualMedia
 * - API(ACTION_PICK_IMAGES · EXTRA_PICK_IMAGES_* · getPickImagesMaxLimit · setRequireOriginal): https://developer.android.com/reference/android/provider/MediaStore
 * - 릴리스 노트(photopicker 1.0.0-alpha03 — PickVisualMedia 이관 · setLocationMetadataAccessRequested): https://developer.android.com/jetpack/androidx/releases/photopicker
 * - 공식 문서(미디어 위치 정보 — ACCESS_MEDIA_LOCATION · setRequireOriginal): https://developer.android.com/training/data-storage/shared/media
 * - 공식 문서(자동으로 보이는 패키지 — media provider): https://developer.android.com/training/package-visibility/automatic
 * - 공식 문서(SDK 확장 — getExtensionVersion): https://developer.android.com/guide/sdk-extensions
 * 핵심 개념(실측: Pixel Tablet API 35 에뮬레이터(SDK 확장 13) debug·release / Pixel 4 API 30 에뮬레이터(확장 R 0) debug /
 * Galaxy A72 SM-A725F API 33(확장 22) debug — 실기기는 피커를 열고 취소만,
 * 고르기는 에뮬레이터에 adb 로 넣은 데모 사진 CS153_*.jpg(GPS EXIF 4장 + 없는 1장 + GIF)로):
 * - createIntent 단계(activity 1.13.0 소스): ① ACTION_PICK_IMAGES(API 33+, 또는 30~32 + 확장 R 2) → ② ACTION_SYSTEM_FALLBACK_PICK_IMAGES 를
 *   처리하는 시스템 앱(MATCH_SYSTEM_ONLY) → ③ ACTION_OPEN_DOCUMENT. GMS 액션 상수는 남아 있지만 쓰이지 않는다. 문서의 Google Play 서비스 백포트가
 *   ②로 잡히는지는 확인하지 못했다(API 28 에뮬레이터가 adb offline 으로 부팅되지 않음). API 30 에뮬레이터(확장 R 0, GMS 26.22)는 대체 액션·GMS 액션
 *   처리 앱이 0 이라 ③ — isPhotoPickerAvailable(context) = false, getPickImagesMaxLimit 없음
 * - ③단계 인텐트: 한 장은 type(이미지 전체) 만, 여러 장은 + ALLOW_MULTIPLE = true. 최대 개수 검사가 없어 요청 1 도 예외 없이 열린다(① 기기에서는 예외).
 *   취소는 RESULT_CANCELED. API 30 DocumentsUI 는 adb input 탭으로 항목이 골라지지 않아(목록 보기 전환은 됨) 고른 결과는 API 35 의 DocumentsUI 로 쟀다
 * - 처리 앱: A72 는 ACTION_PICK_IMAGES 처리 앱이 둘이고 실제로 뜬 것은 우선순위 110 인 새 com.google.android.photopicker 앱(포커스 창 이름으로 확인).
 *   <queries> 가 없으면 앱에는 media provider(자동으로 보이는 패키지)의 PhotoPickerActivity 만 보여 resolveActivity 가 다른 앱을 가리켰고,
 *   DocumentsUI 는 '처리 앱 없음' 이었다. 실행은 선언과 무관
 * - 옵션(API 35): 한 장 계약은 MAX·IN_ORDER 엑스트라를 안 보낸다. LAUNCH_TAB 은 늘 붙고(사진 1 / 앨범 0) 앨범 탭으로 열렸다. GIF 만(type image/gif)·동영상(type 동영상 전체)
 *   필터 반영, 둘 다는 type null. 강조색은 setAccentColor 를 부를 때만 붙고, 진한 보라·아주 밝은 노랑 모두 탭 칩에 그대로 칠해졌다
 * - 여러 장 최대 개수 = min(생성자, 요청). 요청 101 + 기본 생성자(=한도 100) → MAX 100 으로 조용히 잘림. 생성자 101 + 요청 101, 또는 요청 1 →
 *   createIntent 의 require 가 IllegalArgumentException — launcher.launch 안에서 던지므로 잡지 않으면 크래시
 * - 순서: 썸네일을 3→1→2 로 탭하면 IN_ORDER 를 꺼도 켜도 결과는 탭한 순서(25·23·24). 켜면 타일에 1·2·3 번호가 보이는 것만 다르다(MediaProvider 피커, API 35)
 * - 결과 모양: 한 장도 data + clipData 1개, 여러 장은 data null + clipData. URI content://media/picker/0/com.android.providers.media.photopicker/media/<id>,
 *   플래그 READ | PERSISTABLE, DISPLAY_NAME 은 원래 파일명이 아니라 '<id>.jpg'. 취소는 RESULT_CANCELED · data null(A72 새 피커 포함)
 * - 권한 수명(debug): 같은 화면·HOME → am kill → 다시 열어 복원(새 pid) = 읽힘 / 시스템 BACK 으로 끝내고 새로 연 화면(같은 pid)·force-stop =
 *   SecurityException "Calling uid ( … ) does not have permission to access picker uri". 문서의 '앱이 멈출 때까지' 와 달리 받은 액티비티에 묶인다(#152 연락처
 *   피커와 같음). takePersistableUriPermission 성공 → force-stop 뒤에도 읽힘 → release 하면 다시 SecurityException
 * - 위치(앱의 READ_MEDIA_* · ACCESS_MEDIA_LOCATION 모두 없음): 피커 URI 는 GPS 값과 N/E 글자를 0 으로 덮는다(태그 자리 유지, getLatLong = 0,0).
 *   촬영 시각·기종은 그대로. 같은 사진을 DocumentsUI 최근 이미지(content://com.android.providers.media.documents/document/image%3A<id>)로 받으면
 *   위치가 원본 그대로 남고 이름도 원래 파일명, 플래그 READ | WRITE | PERSISTABLE. 피커 URI 에 MediaStore.setRequireOriginal →
 *   UnsupportedOperationException "Require Original is not supported for Picker URI"
 * - photopicker-activity alpha03 의 setLocationMetadataAccessRequested 는 확장 T 23 이상에서만 MediaStore.EXTRA_REQUEST_LOCATION_METADATA_ACCESS 를
 *   붙인다(그 상수는 compileSdk 37.0 jar 에 없다). A72 는 T 22, 에뮬레이터는 13 이라 둘 다 무시되는 기기
 * - 구현 메모: EXIF 는 프레임워크 ExifInterface(InputStream) — lint ExifInterface 경고(옛 플랫폼 파서 보안 버그, androidx 판 권장)를 의존성을 늘리지 않으려 억제.
 *   결과는 StartActivityForResult + contract.createIntent/parseResult 로 받아 원래 resultCode·플래그를 함께 본다(parseResult 는 final 이라 상속으로는 못 본다)
 *
 * ## SystemPhotoPickerExampleUI 6번 카드 — 올리기 전 위치 정리(LocationCleanupCard · LocationCleanupLab)
 * - API(saveAttributes · getLatLong · setAttribute(tag, null)): https://developer.android.com/reference/android/media/ExifInterface
 * - API(방향 규칙 getRotationDegrees · isFlipped — 좌우 반전 먼저, 그다음 회전): https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface
 * - 명세(PNG eXIf 청크 · 청크 CRC): https://www.w3.org/TR/png-3/
 * - 배경(Null Island — 위도·경도 0 이 실제 위치처럼 쓰이는 현상): https://en.wikipedia.org/wiki/Null_Island
 * 핵심 개념(실측: Pixel Tablet API 35 에뮬레이터 debug·release / Pixel 4 API 30 에뮬레이터 debug(형식 시험은 release 도).
 * 데모 사진 CS155_*: 1600×1200 저장 + Orientation 6 인 세로 JPEG, PNG(eXIf) · WebP · HEIC(sips 변환), 모두 GPS 서울 + 고도 38m):
 * - 0 위치: 피커 결과는 GPS 태그 7개(VersionID · 위도·경도와 방향 글자 · 고도와 기준)의 자리를 남기고 값만 0 으로 덮는다(JPEG·WebP·HEIC, 파일 크기 그대로).
 *   프레임워크 getLatLong 은 방향 글자가 비어도 양수로 변환해 true · (0, 0) — 'getLatLong 이 true 면 위치' 로 쓰면 Null Island 를 올린다.
 *   usableLocation 은 GpsState.PRESENT 일 때만 값을 준다(단위 테스트)
 * - 내보내기 4가지(받은 URI 는 캐시로 복사해 사본에서만 — 문서 선택기 결과는 WRITE 권한도 있어 그 URI 에 쓰면 원본이 바뀐다):
 *   ① 그대로 ② GPS_TAG_NAMES(프레임워크 TAG_GPS_* 31개)를 setAttribute(null) + saveAttributes — 방향·촬영 시각·기종은 남는다(JPEG 91 B 감소)
 *   ③ BitmapFactory 디코딩 → JPEG 90 — EXIF 전부 소실, 방향 태그가 없어 세로 사진이 1600×1200 가로로 눕는다. 태그가 없으면 프레임워크는
 *   Orientation 0 을 채워 돌려준다(addDefaultValuesForCompatibility) ④ 방향을 Matrix 로 픽셀에 먼저 반영한 뒤 JPEG — 1200×1600 으로 바로 선다
 * - saveAttributes 형식: 파일 경로(또는 쓰기 가능한 FD)로 연 ExifInterface 만 저장. HEIC 는 읽기만 되고 저장은 IOException
 *   'only supports saving attributes for JPEG, PNG, and WebP formats'(API 35). API 30 은 WebP 도 거절('on JPEG or PNG formats') —
 *   앱이 만든 JPEG·PNG·WebP 로 쓰기 → 지우기를 하는 시험 버튼이 API 35 셋 다 성공, API 30 WebP 만 실패. API 30 미만은 재지 못했다
 *   (Pixel_3 AVD 가 adb offline). 저장이 안 되는 형식은 재인코딩만 위치를 지운다
 * - 함정(PNG, API 35 피커): 피커가 eXIf 청크 안 GPS 값 20바이트를 0 으로 바꾸고 청크 CRC 는 고치지 않는다. 프레임워크 ExifInterface 는
 *   'Encountered invalid CRC value for PNG-EXIF chunk' 를 로그로만 남기고 EXIF 전체를 버려 GPS·기종·촬영 시각이 모두 없는 것처럼 읽히고,
 *   saveAttributes 도 지원하지 않는 형식으로 거절한다. 같은 PNG 를 문서 선택기로 받으면 정상. 카드는 pngExifCrcOk 로 CRC 를 직접 계산해 ⚠ 를 보인다
 * - 표시 메모: BYTE 형식 태그(GPSVersionID)는 getAttribute 가 바이트를 글자로 돌려줘 0 으로 덮인 값이 NUL 네 글자가 된다 — 화면에 NUL 이 있으면
 *   uiautomator dump 가 통째로 실패하므로 printable 로 \xNN 표기
 */
