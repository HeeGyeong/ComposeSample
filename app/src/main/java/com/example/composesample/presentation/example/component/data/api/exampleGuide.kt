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
 * ## GsonMissingFieldExampleUI (응답 필드 누락 — Gson 이 Kotlin 기본값·널 안전성을 우회)
 * - Gson Troubleshooting(Default field values not present): https://github.com/google/gson/blob/main/Troubleshooting.md#default-field-values-missing
 * - GsonBuilder.disableJdkUnsafe(): https://www.javadoc.io/doc/com.google.code.gson/gson/latest/com.google.gson/com/google/gson/GsonBuilder.html
 * - Kotlin 인자 없는 생성자(파라미터 전부 기본값일 때만): https://kotlinlang.org/docs/classes.html#classes-without-constructors
 * - 출처: Adit Lal, Harness Engineering(AW #748) — "A Boolean in a network model that should have been Boolean?":
 *   https://aditlal.dev/harness-engineering/
 * - 출처: The Pulse, Firebase's global outage(AW #748) — "Backend data anomalies should not cause app-side crashes":
 *   https://blog.pragmaticengineer.com/the-pulse-firebases-global-outage-poor-response/
 * 핵심 개념:
 * - Gson 은 인자 없는 생성자가 있을 때만 생성자를 부른다. 없으면 JDK Unsafe 로 생성자 없이 만들고 JSON 에 있는 키만 채운다
 * - Kotlin 은 생성자 파라미터 '전부'에 기본값이 있을 때만 인자 없는 생성자를 만든다 → 하나라도 없으면 누락 키는 null·false·0
 * - 전부 기본값이어도 명시적 JSON null 은 non-null 필드에 들어온다(원시 타입 Boolean·Int 필드는 null 을 건너뛰어 기본값 유지)
 * - 모르는 enum 값("FAMILY")·대소문자 다른 값("pro")은 null, 리스트 안 null 원소도 List<String> 에 그대로
 * - Kotlin 은 프로퍼티를 읽을 때 null 검사를 하지 않는다 → 파싱은 성공하고 NPE 는 쓰는 줄에서:
 *   hashCode(Set·Map 키)·copy()(생성자 파라미터 검사)·멤버 호출·for 순회·enum when·non-null 파라미터 함수.
 *   toString·문자열 템플릿·equals 는 "null" 을 그대로 보여 준다. 엘비스(?:)는 USELESS_ELVIS 경고지만 null 검사가 남는다
 * - Boolean 필드는 "안 보냄"을 false 로 만든다. Boolean? 은 구분하지만 문자열은 Boolean.parseBoolean("Y") = false,
 *   숫자 1 은 JsonSyntaxException(응답 전체 실패) → Y/N 플래그는 String? 으로 받아 매퍼에서 해석
 * - disableJdkUnsafe(): 생성자 없는 생성을 파싱 시점의 JsonIOException 으로 바꾼다. 인자 없는 생성자가 있는 클래스의 명시적 null 은 못 막는다
 * - 해법(CLAUDE.md·DataRules.md): 응답 모델은 전부 nullable + 매퍼에서 requireNotNull(필드 이름)·orEmpty·명시적 기본값.
 *   enum 은 문자열로 받아 매퍼에서 해석(원문 보존). 비정상 응답을 단위 테스트(GsonMissingFieldTest)에 넣는다
 * - Retrofit GsonConverterFactory.create() 와 Ktor ContentNegotiation { gson() } 모두 같은 결과(화면 5번 카드)
 * - 측정: 2026-10-13 Gson 2.14.0 · Kotlin 2.4.21, JVM 단독 컴파일과 실기기(SM-A725F) debug·release
 *   (2026-10-26 Gson 2.15.0 상향 후 실기기 debug 재측정 — 5응답 × 세 모델·매퍼·12가지 사용·플래그·disableJdkUnsafe·3경로 결과 동일)
 *
 * ## ApiDisconnectExampleUI (연결 해제 처리)
 * - 공식 문서: https://developer.android.com/training/monitoring-device-state/connectivity-status-type
 * 핵심 개념:
 * - 네트워크 상태 감지 + 요청 중 연결 해제 시나리오 처리
 * - 코루틴 취소/예외 전파를 통한 안전한 정리
 */
