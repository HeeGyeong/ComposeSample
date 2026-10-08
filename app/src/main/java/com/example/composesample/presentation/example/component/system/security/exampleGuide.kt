package com.example.composesample.presentation.example.component.system.security

/**
 * App Security 실무 예제 참고 자료
 *
 * - 본 예제 출처: https://technotalkative.com/android-app-security-practical-steps-every-developer-must-follow/
 * - OkHttp CertificatePinner: https://lysine.dev/okhttp/features/https/#certificate-pinning-kt-java
 * - Network security config (선언형 pin-set): https://developer.android.com/privacy-and-security/security-config#CertificatePinning
 * - AndroidKeyStore 시스템: https://developer.android.com/privacy-and-security/keystore
 * - EncryptedSharedPreferences (Jetpack Security): https://developer.android.com/topic/security/data
 * - Play Integrity API: https://developer.android.com/google/play/integrity
 *
 * 핵심 개념 요약
 *
 * 1) Certificate Pinning
 *  - 서버가 사용하는 인증서(또는 그 상위 CA)의 공개키 해시(SHA-256 SPKI)를 클라이언트에 사전 박제
 *  - CertificatePinner.Builder().add(host, "sha256/...", "sha256/...") → OkHttpClient.Builder().certificatePinner(...)
 *  - 핀 불일치 시 OkHttp 가 SSLPeerUnverifiedException 으로 연결 차단
 *  - 운영 시 주의: 인증서 갱신 주기를 고려해 backup pin 을 함께 등록해야 핸드오버 가능
 *
 *  핀 값을 얻는 방법 (예제가 ①에서 실제로 하는 일)
 *   - 핀을 걸지 않은 클라이언트로 한 번 접속해 Response.handshake.peerCertificates 를 읽고,
 *     각 인증서를 CertificatePinner.pin(certificate) 에 넣으면 등록할 "sha256/..." 문자열이 나온다
 *   - 또는 아무 값이나 틀린 핀으로 찔러보면, SSLPeerUnverifiedException 메시지의
 *     "Peer certificate chain" 에 서버가 제시한 실제 핀이 전부 찍혀 나온다
 *   - 운영에서는 이 값을 빌드 시점에 뽑아 상수/설정에 박아둔다 (런타임 조회는 핀의 의미가 없다 —
 *     중간자가 끼어든 연결에서 조회하면 중간자의 핀을 그대로 신뢰하게 되기 때문)
 *
 *  코드 경로 vs 선언형 경로
 *   - CertificatePinner: OkHttp 를 쓰는 요청에만 적용, 클라이언트 단위로 다르게 걸 수 있다
 *   - res/xml/network_security_config.xml 의 <pin-set>: 매니페스트에 연결하면 앱 전체 트래픽에 적용
 *     · expiration 속성을 넘기면 그 시점부터 핀 검사를 건너뛴다 — 핀 갱신을 놓쳤을 때
 *       앱이 통신 불가로 죽는 대신 검사가 풀리도록 하는 안전장치
 *     · 전역이라 핀이 틀리면 앱의 모든 네트워크가 끊기므로 이 예제에서는 코드 블록으로만 다룬다
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
 *  - Certificate Pinning: 실제 TLS 요청을 보낸다(시뮬레이션 아님). 다만 핀 값을 하드코딩하지 않고
 *    매번 ①에서 조회해 쓰는데, 이는 인증서가 갱신되면 핀도 바뀌어 예제가 깨지기 때문이다.
 *    같은 이유로 이 섹션은 계측 테스트 대상으로 삼지 않는다 — 인증서 갱신일에 CI 가 실패한다.
 *    선언형 <pin-set> 경로는 앱 전역 설정이라 코드 블록으로만 보여주고 실제로 적용하지는 않는다.
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

/**
 * API 요청 서명(HMAC) + 재전송 방지 예제 참고 자료
 *
 * - 본 예제 출처: https://lopez-manas.com/articles/2026-09-02_securing-android-sdks-a-defense-in-depth/
 * - OkHttp Interceptor: https://lysine.dev/okhttp/features/interceptors/
 * - javax.crypto.Mac: https://developer.android.com/reference/javax/crypto/Mac
 * - MessageDigest.isEqual (상수 시간 비교): https://developer.android.com/reference/java/security/MessageDigest#isEqual(byte[],%20byte[])
 * - AWS SigV4 canonical request (같은 설계의 실제 사례): https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html
 *
 * 핵심 개념 요약
 *
 * 1) canonical string — 무엇을 서명 대상에 넣는가
 *  - 메서드 / 경로 / 타임스탬프 / nonce / **바디의 SHA-256** 을 줄바꿈으로 이어 붙인다
 *  - 줄바꿈 구분자가 필요한 이유: 구분 없이 이어 붙이면 a="xy",b="z" 와 a="x",b="yz" 가 같은 문자열이 되어
 *    서로 다른 요청이 같은 서명을 갖는 모호함(canonicalization 취약점)이 생긴다
 *  - 바디 해시를 넣어야 헤더만 베끼고 바디를 바꾸는 변조가 서명 검사에서 걸린다
 *
 * 2) 검사 순서 — 신선도 → nonce → 서명
 *  - HMAC 계산이 가장 비싸므로 값싼 검사로 먼저 걸러낸다(계산 자원 자체가 공격 표면이다)
 *  - 타임스탬프 창은 초 단위면 기기 시계 오차로 정상 요청이 튕기고, 시간 단위면 재전송 여지가 커진다 → 분 단위
 *  - 서명이 유효해도 창 밖이면 거절한다 — "유효한 서명"과 "지금 유효한 요청"은 다른 질문이다
 *
 * 3) 재전송 방지 — 서명만으로는 막지 못한다
 *  - 가로챈 요청은 서명이 멀쩡하므로 그대로 다시 보내면 통과한다
 *  - 서버가 nonce 를 기억해 두 번째부터 거절해야 비로소 한 번만 유효해진다
 *  - nonce 저장소에는 허용 창보다 조금 긴 TTL 이 필요하다(없으면 무한히 커지고, 창보다 짧으면 재전송이 되살아난다)
 *
 * 4) 상수 시간 비교
 *  - == 비교는 첫 불일치에서 즉시 반환 → 맞는 바이트가 많을수록 응답이 미세하게 느려진다
 *  - 공격자는 그 차이를 반복 측정해 서명을 앞에서부터 한 바이트씩 맞춰 나갈 수 있다(timing attack)
 *  - 결과와 무관하게 전부 읽고 XOR 을 OR 로 누적하는 비교를 쓴다 — JDK 는 MessageDigest.isEqual 로 제공
 *
 * 5) 이 층이 막지 못하는 것 (defense-in-depth 의 '한 층'일 뿐)
 *  - 키가 앱 안에 있으면 추출 즉시 서명 위조가 가능하다. 문자열 난독화는 정적 분석만 늦출 뿐 Frida 같은 동적 계측은 막지 못한다
 *  - 요청 서명은 인증 수단이 아니라 자동화된 대량 남용의 단가를 올리는 층이다
 *  - 기기 무결성은 별개 축 → Play Integrity(같은 폴더 AppSecurityExample 의 ③ 섹션)
 *
 * 본 예제의 단순화 포인트
 *  - **네트워크를 쓰지 않는다**: 서명 인터셉터 뒤에 "서버" 역할 검증 인터셉터를 두고, 그 인터셉터가
 *    chain.proceed() 대신 Response 를 직접 만들어 반환한다. 실제 인터셉터 파이프라인은 그대로 쓰면서
 *    외부 서버 없이 4분기(정상/바디 변조/만료/재전송)를 실동작으로 보여주기 위한 구조다.
 *  - 공유 비밀 키를 소스 상수로 둔다 — 실제 앱에서는 하면 안 되는 일이며, 그 한계를 화면 4번 카드에서 다룬다.
 *  - nonce 저장소는 메모리 Set(TTL 없음). 화면을 벗어나면 사라진다.
 *  - **상수 시간 비교는 시간을 재지 않고 '읽은 바이트 수'를 센다** — 디버그 빌드는 인터프리터로 돌아
 *    ns 측정이 실행마다 요동치므로(프로젝트의 Room 벤치마크가 같은 이유로 배율이 튀었다),
 *    결정적인 지표로 성질 자체를 보여준다.
 */
