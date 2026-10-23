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
 * - BottomSheetScaffold + SideEffect로 피커의 setCurrentExpanded와 바텀시트 확장 상태 동기화
 * - deselectUri() 호출만으로는 onUriPermissionRevoked가 자동 트리거되지 않음 → 앱 state를 직접 갱신해야 함(피커=선택 가능 항목 소유, 앱=선택 결과 표현·영속화 소유라는 오너십 분리 함정 주의)
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
 */
