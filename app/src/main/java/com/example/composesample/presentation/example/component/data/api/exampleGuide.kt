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
 * - (4번 카드) 오리진 페일오버 — 출처: Erfan Moghaddam, Designing Safer API Failover in an Android App(AW #747)
 *   https://dev.to/erfan-moghaddam/designing-safer-api-failover-in-an-android-app-136i
 *   · HTTP Semantics 9.2.2(멱등·재시도): https://www.rfc-editor.org/rfc/rfc9110.html#section-9.2.2
 *   · 상태 기계: Primary →(자격 있는 실패) Secondary →(실패) Unavailable →(쿨다운 경과 + 새 요청) Primary. 잠금 안에서만 전이
 *   · 실패는 '그 요청이 실제로 쓴 오리진'으로만 판정 — 확인이 없으면 동시에 실패한 주 오리진 요청이 이미 넘어간 보조까지 Unavailable 로 만든다
 *   · 관리 오리진만 scheme·host·port 교체(경로·쿼리 유지), 다른 호스트는 그대로. Ktor URLBuilder.port 는 미지정 시 0 → 실효 포트로 비교
 *   · 자동 재생은 GET·HEAD·OPTIONS 의 502~504·전송 실패만. 400·401·404 는 전환 안 함. POST 의 모호한 실패는 호출자에게
 *   · 다시 보낸 요청의 실패도 보고(보조 실패 → Unavailable)하지만 세 번째 시도는 없다. 취소는 잡지 않는다(폴백 없음)
 *   · 쿨다운은 SystemClock.elapsedRealtime(단조·절전 중에도 흐름) — 예제·테스트는 가짜 시계. 재시도 게이트일 뿐 상태 점검이 아니다
 *   · OkHttp 인터셉터로 만들면 다시 proceed 하기 전에 이전 응답을 닫아야 한다. Ktor 는 HttpRequestRetry 처럼 원본 복사본으로 proceed
 *   · 같은 판정을 단위 테스트 OriginFailoverTest 로 고정. HotSwan 은 플러그인 람다의 checked IOException 을 감싸 debug 에서 제외(build.gradle)
 *
 * ## ApiDisconnectExampleUI (연결 해제 처리)
 * - 공식 문서: https://developer.android.com/training/monitoring-device-state/connectivity-status-type
 * 핵심 개념:
 * - 네트워크 상태 감지 + 요청 중 연결 해제 시나리오 처리
 * - 코루틴 취소/예외 전파를 통한 안전한 정리
 */
