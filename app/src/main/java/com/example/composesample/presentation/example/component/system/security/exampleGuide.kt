package com.example.composesample.presentation.example.component.system.security

/**
 * App Security 실무 예제 참고 자료
 *
 * - 본 예제 출처: https://technotalkative.com/android-app-security-practical-steps-every-developer-must-follow/
 * - OkHttp CertificatePinner: https://square.github.io/okhttp/features/https/#certificate-pinning
 * - AndroidKeyStore 시스템: https://developer.android.com/privacy-and-security/keystore
 * - EncryptedSharedPreferences (Jetpack Security): https://developer.android.com/topic/security/data
 * - Play Integrity API: https://developer.android.com/google/play/integrity
 *
 * 핵심 개념 요약
 *
 * 1) Certificate Pinning
 *  - 서버가 사용하는 인증서(또는 그 상위 CA)의 공개키 해시(SHA-256)를 클라이언트에 사전 박제
 *  - OkHttpClient.Builder().certificatePinner(...) 로 적용
 *  - 핀 불일치 시 OkHttp 가 SSLPeerUnverifiedException 으로 연결 차단
 *  - 운영 시 주의: 인증서 갱신 주기를 고려해 backup pin 을 함께 등록해야 핸드오버 가능
 *
 * 2) AndroidKeyStore + AES-GCM (EncryptedSharedPreferences 내부 동작)
 *  - KeyGenParameterSpec 으로 setBlockModes(GCM), setEncryptionPaddings(NONE), keySize(256)
 *  - AES 키는 KeyStore 외부로 export 불가능 → 루팅 단말이라도 키 자체는 추출 어려움
 *  - IV(GCM)는 매 암호화마다 재생성하여 ciphertext 와 함께 저장 필요
 *  - GCM 태그(128bit)가 인증값 역할 — ciphertext 변조 시 복호화 실패
 *
 * 3) Play Integrity API
 *  - Google Play 서비스가 단말/앱 무결성을 verdict 페이로드로 반환
 *  - 주요 필드:
 *      • appRecognitionVerdict          : PLAY_RECOGNIZED 만 통과 처리
 *      • deviceRecognitionVerdict       : MEETS_STRONG/DEVICE_INTEGRITY 권장 (루팅 = MEETS_BASIC 이하)
 *      • appLicensingVerdict            : LICENSED (라이선스 확인)
 *      • nonceMatched (서버 측 검증)    : 재전송 공격 방지 — 매 요청마다 서버가 발급한 nonce 일치 여부
 *  - 응답은 JWT(JWS) 형태로 서명되어 오며, **서버에서** 디코딩/검증해야 안전 (클라이언트 디코딩은 변조 가능)
 *
 * 본 예제의 단순화 포인트
 *  - Certificate Pinning: 자가 서명 HeldCertificate 로 서버/공격자 인증서를 동적으로 생성해 핀 비교만 시연
 *  - Secure Storage: 디스크 저장 없이 메모리에서만 ciphertext/IV 를 보관 (실제는 EncryptedSharedPreferences 사용 권장)
 *  - Play Integrity: 네트워크 호출 없이 Mock JSON 으로 verdict 필드 형태만 보여줌
 */

/**
 * Hardware-Backed Keystore 검증 예제 참고 자료
 *
 * - 본 예제 출처: https://medium.com/gitconnected/verifying-hardware-backed-keystore
 * - KeyInfo: https://developer.android.com/reference/android/security/keystore/KeyInfo
 * - KeyProperties.SECURITY_LEVEL_*: https://developer.android.com/reference/android/security/keystore/KeyProperties
 * - StrongBox 개요: https://source.android.com/docs/security/best-practices/hardware
 *
 * 핵심 개념 요약
 *
 * 1) KeyInfo 조회
 *  - SecretKeyFactory.getInstance(algorithm, "AndroidKeyStore") 로 팩토리 획득
 *  - factory.getKeySpec(key, KeyInfo::class.java) 로 KeyInfo 추출
 *  - 이 KeyInfo 는 키의 메타정보를 담고 있으며, "어디에 키가 보관됐는지" 진단할 수 있음
 *
 * 2) API 분기
 *  - API 23~30 (M~R) : isInsideSecureHardware (Boolean) — 단순 in/out 판정만 가능
 *  - API 31+ (S+)   : securityLevel (Int) — SOFTWARE / TRUSTED_ENVIRONMENT / STRONGBOX / UNKNOWN_SECURE 4단계 구분
 *  - 31+ 에서는 isInsideSecureHardware 가 deprecated 처리됨 → @Suppress("DEPRECATION") 또는 분기 호출 필요
 *
 * 3) StrongBox vs TEE
 *  - TEE: 메인 프로세서 안의 격리된 실행 환경 (ARM TrustZone 등). 대부분의 최신 단말이 지원
 *  - StrongBox: TEE 와는 별도의 보안 칩 (Pixel 의 Titan M 등). API 28+, 일부 단말만 지원
 *  - setIsStrongBoxBacked(true) 가 미지원 단말에서는 StrongBoxUnavailableException 으로 실패 — try/catch 후 TEE 폴백 필수
 *
 * 4) 검증 시점
 *  - 결제, 본인 인증, MDM, DRM 같은 민감 동작 전에 한 번 검증
 *  - SOFTWARE 키는 루팅 단말에서 메모리/디스크 dump 로 키 자체가 추출될 수 있음
 *  - 진단 결과는 캐싱해 매 동작마다 키 생성 비용을 발생시키지 않도록 함 (본 예제는 시연 위해 매번 생성)
 *
 * 본 예제의 단순화 포인트
 *  - 매 클릭마다 키를 삭제/재생성 (deleteEntry → generateKey) — 환경 변화를 즉시 반영하기 위함
 *  - 실제 운영에서는 한 번 생성한 키를 재사용하고 진단 결과만 캐싱 권장
 *  - 에뮬레이터에서는 대부분 SOFTWARE 로 떨어지며, 실기기(Pixel/Galaxy 등)에서 TEE/STRONGBOX 결과 확인 가능
 */

/**
 * Screenshot Detection 예제 참고 자료
 *
 * - Android 14 동작 변경: https://developer.android.com/about/versions/14/behavior-changes-14#detect-screenshots
 * - Activity.registerScreenCaptureCallback: https://developer.android.com/reference/android/app/Activity#registerScreenCaptureCallback(java.util.concurrent.Executor,android.app.Activity.ScreenCaptureCallback)
 * - MediaStore.Images: https://developer.android.com/reference/android/provider/MediaStore.Images
 * - ContentObserver: https://developer.android.com/reference/android/database/ContentObserver
 * - 런타임 권한(사진/미디어): https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions
 *
 * 핵심 개념 요약
 *
 * 1) Android 14+ 콜백 (registerScreenCaptureCallback)
 *  - Activity.registerScreenCaptureCallback(executor, callback) 로 등록, unregisterScreenCaptureCallback 으로 해제
 *  - callback.onScreenCaptured() 는 이 Activity 가 화면에 보이는 동안 실제로 캡처될 때만 호출됨
 *  - 별도 권한 불필요 — 시스템이 캡처 이벤트를 직접 통지
 *  - 화면 녹화(레코딩)는 감지 대상이 아님(레코딩 감지는 MediaProjection 콜백 별도 필요)
 *
 * 2) 레거시 MediaStore ContentObserver
 *  - contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer)
 *  - onChange(selfChange, uri) 콜백에서 최신 삽입 이미지의 DISPLAY_NAME/RELATIVE_PATH(API 29+) 또는 DATA(그 미만)를 조회
 *  - 파일명·경로에 "screenshot" 문자열이 포함되는지로 휴리스틱 판정 — 사진 편집 앱 저장본 등 오탐 가능
 *  - READ_MEDIA_IMAGES(API 33+) 또는 READ_EXTERNAL_STORAGE(API 32 이하) 런타임 권한 필요
 *
 * 3) 실무 적용
 *  - minSdk 가 34 미만이면 SDK_INT 분기로 두 방식을 병행(34+ 콜백, 미만 ContentObserver 폴백)
 *  - 결제/DRM 콘텐츠 등 민감 화면 보호 목적이면 캡처 감지 시 워터마크 오버레이·경고 다이얼로그 등으로 대응
 *  - FLAG_SECURE 로 애초에 캡처 자체를 차단하는 방법도 있으나, 이 예제는 "감지" 자체가 목적인 시나리오(로깅/경고)를 다룸
 *
 * 본 예제의 단순화 포인트
 *  - 두 방식 모두 화면 내 토글 버튼으로 수동 등록/해제 (실제 운영에서는 Activity onResume/onPause 생명주기에 연동 권장)
 *  - ContentObserver 는 이미지 삽입마다 매번 최신 1건을 재조회 — 대량 삽입 시 놓치는 이벤트가 있을 수 있어 로깅/분석 용도로만 권장
 */

/**
 * IPC / Exported Component 보안 진단 예제 참고 자료
 *
 * - PackageManager.getPackageInfo: https://developer.android.com/reference/android/content/pm/PackageManager#getPackageInfo(java.lang.String,%20android.content.pm.PackageManager.PackageInfoFlags)
 * - ComponentInfo.exported: https://developer.android.com/reference/android/content/pm/ComponentInfo#exported
 * - PendingIntent FLAG_MUTABLE/FLAG_IMMUTABLE: https://developer.android.com/reference/android/app/PendingIntent#FLAG_IMMUTABLE
 * - Intent.fillIn: https://developer.android.com/reference/android/content/Intent#fillIn(android.content.Intent,%20int)
 * - Custom permissions (protectionLevel): https://developer.android.com/guide/topics/permissions/defining
 *
 * 핵심 개념 요약 — CLAUDE.md 컨벤션 실험: 이 예제는 UI 본문에 서술형 설명 카드를 두는 대신,
 * 아래처럼 실제 소스 코드의 KDoc/inline 주석에 판단 근거를 적어두고 화면에는 "코드/주석 참고" 포인터만 남긴다.
 *
 * 1) Manifest 진단 (실동작)
 *  - exported/permission 은 매니페스트에 고정되는 값이라 런타임 토글 데모는 불가능하지만,
 *    PackageManager.getPackageInfo(packageName, GET_ACTIVITIES|GET_SERVICES|GET_RECEIVERS|GET_PROVIDERS) 로
 *    이 앱 자신의 컴포넌트 목록을 실시간 스캔하는 것은 가능 — 판단 기준은 IpcExportedComponentExampleUI.kt 의
 *    scanExportedComponents() 함수 KDoc 참고.
 *
 * 2) PendingIntent Mutability (실동작)
 *  - FLAG_IMMUTABLE 로 만든 PendingIntent 는 send(Context, int, Intent) 에 넘긴 fillIn 인자가 통째로 무시됨
 *  - FLAG_MUTABLE(또는 API 31 미만 기본 동작) 은 fillIn 이 그대로 적용 — 유출된 PendingIntent 를 손에 넣은
 *    제3자가 내부 Intent 의 extras 를 조작할 수 있다는 뜻. 상세 시나리오는 sendTamperedBroadcast() 함수 KDoc 참고.
 *
 * 3) Permission Enforcement (CodeBlock, 실행 없음)
 *  - protectionLevel="signature" 커스텀 권한을 선언하고 exported 컴포넌트의 android:permission 에 지정하면
 *    같은 서명으로 빌드된 앱끼리만 호출을 허용할 수 있음 — 코드는 IpcExportedComponentExampleUI.kt 의
 *    PermissionEnforcementSection() CodeBlock 참고.
 */
