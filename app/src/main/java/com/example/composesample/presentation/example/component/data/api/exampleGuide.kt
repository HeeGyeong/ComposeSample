package com.example.composesample.presentation.example.component.data.api

/**
 * Data/API 예제 참고 자료
 *
 * ## ApiExampleViewModel / ApiExampleUseCaseViewModel (직접 호출 vs UseCase 대조쌍)
 * - 공식 문서: https://developer.android.com/topic/architecture/domain-layer
 * 핵심 개념:
 * - ApiExampleViewModel: ViewModel이 API 인터페이스를 직접 호출하는 형태(교육용 안티/대조 패턴)
 * - ApiExampleUseCaseViewModel: domain UseCase를 경유하는 클린 아키텍처 형태
 * - 두 예제는 "도메인 레이어 유무" 차이를 비교하기 위한 의도적 대조쌍 (ARCHITECTURE.md 참고)
 * - domain Repository는 suspend fun이 도메인 타입을 반환 (retrofit2.Call 미노출, ARCH-01/02)
 *
 * ## KtorExampleUI (Ktor Client)
 * - 공식 문서: https://ktor.io/docs/client-create-and-configure.html
 * 핵심 개념:
 * - HttpClient + ContentNegotiation(Gson) + Logging + DefaultRequest + timeout 구성
 * - Retrofit과의 비교: suspend 기반 동일하나 엔진/플러그인 구성 방식이 다름
 *
 * ## KtorAdvancedConfigExampleUI (Auth 토큰 리프레시 / HttpRequestRetry 재시도 — MockEngine 시연)
 * - 공식 문서(Auth): https://ktor.io/docs/client-bearer-auth.html
 * - 공식 문서(Retry): https://ktor.io/docs/client-request-retry.html
 * - 공식 문서(MockEngine): https://ktor.io/docs/client-testing.html
 * 핵심 개념:
 * - MockEngine: 실서버/실네트워크 없이 요청을 코드로 받아 응답을 정의하는 가짜 엔진. 401/503 분기를 결정적으로 재현
 * - install(Auth) { bearer { loadTokens / refreshTokens } }: 401 수신 시 refreshTokens 가 자동 호출되어 토큰 갱신 후 재요청
 *   - 트리거 조건: 401 응답에 WWW-Authenticate: Bearer 헤더가 있어야 BearerAuthProvider 가 동작
 *   - sendWithoutRequest { true }: 401 을 기다리지 않고 첫 요청부터 토큰을 선제 전송
 * - install(HttpRequestRetry) { retryOnServerErrors; delayMillis/exponentialDelay }: 5xx 에 지수 백오프로 재시도
 * - 데모 목적상 MockEngine 을 메인 소스셋에 배치(실서비스는 보통 test 소스셋). 의존성: ktor-client-mock, ktor-client-auth
 *
 * ## ApiDisconnectExampleUI (연결 해제 처리)
 * - 공식 문서: https://developer.android.com/training/monitoring-device-state/connectivity-status-type
 * 핵심 개념:
 * - 네트워크 상태 감지 + 요청 중 연결 해제 시나리오 처리
 * - 코루틴 취소/예외 전파를 통한 안전한 정리
 */
