package com.example.composesample.presentation.example.component.system.platform.biometric

/**
 * Biometric Auth in Compose 참고 자료
 *
 * ## androidx.biometric:biometric-compose:1.4.0-alpha05
 * - 출처: https://navczydev.medium.com/biometric-auth-in-compose-made-easy-the-new-library-you-need-29814270506d
 * - 공식 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/biometric
 *
 * ### 버전 선택 메모
 * - 1.4.0-alpha05 — `minCompileSdk=35` (현재 프로젝트에서 사용 가능)
 * - 1.4.0-alpha06 이상 — `minCompileSdk=36` 필요
 * - 1.1.0(stable)은 Activity 기반 `BiometricPrompt`만 제공하며 Compose 통합 API 없음
 *
 * ### 핵심 API
 * - `rememberAuthenticationLauncher(resultCallback)` — Composable, `AuthenticationResultLauncher` 반환
 *   - 내부적으로 `LocalViewModelStoreOwner`, `LocalLifecycleOwner`, `LocalContext`를 요구하므로 ComponentActivity 안에서 호출되어야 함
 * - `AuthenticationRequest.biometricRequest(title, authFallback) { ... }` — DSL 빌더
 *   - alpha05의 `Biometric.Fallback`: `DeviceCredential`(PIN/Pattern/Password), `NegativeButton(text)`(다이얼로그 좌측 취소 버튼)
 *   - alpha06+에서는 `CustomOption(displayName)` 추가 + 폴백을 vararg로 여러 개 지정 가능
 *   - 빌더 옵션: `setSubtitle`, `setMinStrength(Class2()|Class3())`, `setIsConfirmationRequired`
 * - `AuthenticationResultLauncher.launch(request)` / `cancel()`
 *
 * ### 결과 sealed 분기 (`AuthenticationResult` — alpha05 기준)
 * - `Success(crypto, authType)` — 성공. `authType`은 BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC 등
 * - `Error(errorCode, errString)` — NegativeButton 클릭, 사용자 취소, 하드웨어 실패, 잠금 등
 *   (errorCode는 `BiometricPrompt.ERROR_*` 상수)
 * - alpha06+에서는 `CustomFallbackSelected(fallback)` 분기 추가
 *
 * ### 시도별 콜백 — `onAuthAttemptFailed`
 * - 잘못된 지문/얼굴 등 매 시도마다 호출. 시스템 UI가 이미 "Not recognized" 메시지를 표시하므로
 *   여기서 Toast/Dialog를 띄우는 것은 보통 안티 패턴 (분석/통계 용도로만 사용)
 *
 * ### 가용성 사전 점검 — `BiometricManager`
 * - `BiometricManager.from(context).canAuthenticate(authenticators)` 결과 코드:
 *   - `BIOMETRIC_SUCCESS` — 즉시 사용 가능
 *   - `BIOMETRIC_ERROR_NO_HARDWARE` — 하드웨어 자체가 없음
 *   - `BIOMETRIC_ERROR_HW_UNAVAILABLE` — 일시적 불가
 *   - `BIOMETRIC_ERROR_NONE_ENROLLED` — 등록된 생체 정보 없음 (설정 화면 유도 필요)
 *   - `BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED` — 시스템 보안 업데이트 필요
 *
 * ### 강도(Strength)와 호환성 메모
 * - `Class2()`(Weak) — 얼굴/지문 등 광범위 지원. CryptoObject 사용 불가
 * - `Class3()`(Strong) — Crypto와 결합 가능, 단 API 28/29에서 DeviceCredential 폴백과 동시 사용 불가
 * - API 30 미만에서는 `credentialRequest`(Credential-only) 미지원
 *
 * ### 알파 단계 주의
 * - 1.4.x 라인은 전부 alpha — 향후 시그니처가 바뀔 수 있으므로 릴리스 노트 확인 후 업그레이드 권장
 * - alpha06으로 올릴 경우 (1) `compileSdk=36` 필요 (2) `biometricRequest`가 vararg fallback으로 변경되어
 *   호출 사이트 마이그레이션 필요
 */
