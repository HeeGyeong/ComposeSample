package com.example.composesample.presentation.example.component.system.platform.biometric

/**
 * Biometric Auth in Compose 참고 자료
 *
 * ## androidx.biometric:biometric-compose:1.4.0-alpha07
 * - 출처: https://navczydev.medium.com/biometric-auth-in-compose-made-easy-the-new-library-you-need-29814270506d
 * - 공식 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/biometric
 *
 * ### 버전 선택 메모
 * - 1.4.0-alpha07 — `minCompileSdk=36` + **`minCompileMinorSdk=1`**(= 36.1) / `minAGP=8.9.1`, pom 전이 끌어올림 0
 *   (AGP 8.13 은 minor 를 검사하지 않아 compileSdk 36 에서 통과했지만 AGP 9.4 는 36.1 이상을 요구한다 — 현재 compileSdk 37)
 * - 2026-09-29 alpha05 → alpha07 상향. 상향 전후 실기기(SM-A725F/API 33) 결과 동일
 * - 1.1.0(stable)은 Activity 기반 `BiometricPrompt`만 제공하며 Compose 통합 API 없음
 *
 * ### 핵심 API
 * - `rememberAuthenticationLauncher(resultCallback)` — Composable, `AuthenticationResultLauncher` 반환
 *   - 내부적으로 `LocalViewModelStoreOwner`, `LocalLifecycleOwner`, `LocalContext`를 요구하므로 ComponentActivity 안에서 호출되어야 함
 * - `AuthenticationRequest.biometricRequest(title, vararg authFallbacks) { ... }` — DSL 빌더
 *   - 앱이 쓸 수 있는 폴백은 `DeviceCredential`(PIN/Pattern/Password)과 `CustomOption(text, iconType)` 두 가지
 *   - alpha05 의 `NegativeButton(text)` 는 사라졌고, 대응하는 `DefaultCancel`·`OverriddenDeviceCredential` 은
 *     바이트코드상 public 이지만 Kotlin `internal` 이라 앱에서 만들 수 없다(컴파일 에러로 확인)
 *   - 폴백을 하나도 주지 않으면 라이브러리가 시스템 문구의 기본 취소 버튼(DefaultCancel)을 붙인다
 *   - 검증: 최대 4개(5개면 IllegalArgumentException), DeviceCredential 은 하나만 — 둘 다 실기기 실측
 *   - 폴백이 2개 이상일 때 전부 표시되는 것은 `SDK_INT_FULL >= 3600001`(Android 16 QPR2)뿐이다.
 *     그 미만에서는 **첫 번째 폴백만 쓰이고 나머지는 조용히 버려진다**(`multipleFallbackOptionsValid`, 바이트코드 확인).
 *     API 33 에서 여러 개를 넣어도 크래시는 없다(실측)
 *   - `CustomOption` 아이콘: `ICON_TYPE_PASSWORD`/`QR_CODE`/`ACCOUNT`/`GENERIC`(기본값)
 *   - 빌더 옵션: `setSubtitle`, `setMinStrength(Class2|Class3())`, `setIsConfirmationRequired`
 * - `AuthenticationResultLauncher.launch(request)` / `cancel()`
 *
 * ### 결과 sealed 분기 (`AuthenticationResult` — alpha07 기준)
 * - `Success(crypto, authType)` — 성공. `authType`은 BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC 등
 * - `Error(errorCode, errString)` — 기본 취소 버튼, 사용자 취소, 하드웨어 실패, 잠금 등
 *   (errorCode는 `BiometricPrompt.ERROR_*` 상수. 지문 미등록 기기에서는 다이얼로그 없이 바로 11)
 * - `CustomFallbackSelected(fallback)` — `CustomOption` 폴백을 눌렀을 때. Error 가 아니다
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
 * - alpha05 → alpha07 마이그레이션: `NegativeButton("취소")` 제거(폴백 생략 또는 `CustomOption`),
 *   `title =`/`authFallback =` 이름 인자 대신 위치 인자, `when(result)` 에 `CustomFallbackSelected` 분기 추가
 *
 * ### 실기기 검증 한계
 * - 보유 기기(SM-A725F)는 지문 미등록이라 생체 다이얼로그 시나리오는 전부 Error 11 로 끝난다.
 *   CustomOption 을 눌러 CustomFallbackSelected 가 오는 경로는 지문을 등록한 기기에서 수동 확인이 필요하다
 * - DeviceCredential 프롬프트는 보안 창이라 screencap/uiautomator 로 내용을 볼 수 없다
 */
