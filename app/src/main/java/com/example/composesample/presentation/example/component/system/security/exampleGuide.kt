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
