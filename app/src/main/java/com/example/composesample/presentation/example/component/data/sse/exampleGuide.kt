package com.example.composesample.presentation.example.component.data.sse

/**
 * Data/SSE 예제 참고 자료
 *
 * ## SSEExampleUI (Server-Sent Events 실시간 스트리밍)
 * - SSE 표준(MDN): https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
 * - okhttp-eventsource: https://github.com/launchdarkly/okhttp-eventsource
 * - Wikimedia User-Agent 정책: https://meta.wikimedia.org/wiki/User-Agent_policy
 * 핵심 개념:
 * - SSE: 서버 → 클라이언트 단방향 텍스트 스트림 (WebSocket과 달리 단방향, HTTP 기반)
 * - EventSource로 이벤트 수신 → callbackFlow/Channel 로 Compose State에 브릿지
 * - StateFlow.update { } 패턴으로 누적 메시지를 불변 리스트로 갱신
 * - 화면 이탈 시 EventSource close + 코루틴 취소로 리소스 정리 — ViewModel.onCleared() 에서 닫는다.
 *   viewModelScope 가 먼저 취소되므로 스코프 안의 종료 로직에 기대면 안 된다(2026-09-29 실측: 이탈 6초 뒤에도
 *   eventsource 스레드 2개 생존 → onCleared 추가 후 1초 안에 0)
 * - Wikimedia 스트림은 식별 가능한 User-Agent 가 없으면 403 — ConnectStrategy.http(uri).header() 로 UA 를 직접 보낸다
 *
 * okhttp-eventsource 5.0.0 (2026-09-29, 3.0.0 에서 상향):
 * - 4.0 에서 API 재설계: EventHandler → background.BackgroundEventHandler(콜백 이름 동일),
 *   EventSource.Builder(handler, URI) → BackgroundEventSource.Builder(handler, EventSource.Builder(ConnectStrategy)),
 *   headers()/reconnectTime() → ConnectStrategy.header() / RetryDelayStrategy.defaultStrategy().initialDelay()
 * - 4.0 부터 EventSource 는 자체 스레드를 만들지 않는다(동기 I/O). 콜백 방식은 BackgroundEventSource 가 스레드를 맡는다
 * - ⚠️ 동작 차이: BackgroundEventSource.close() 는 closed 플래그를 먼저 세우고 이후 이벤트를 버린다 →
 *   **호출자가 닫으면 onClosed 가 오지 않는다**(3.x 는 왔다, 바이트코드·실기기 확인). onClosed 는 서버가 끊은 경우만.
 *   그래서 연결 종료 상태는 closeSSEConnection() 에서 직접 반영하고, 종료 뒤 늦게 도착한 메시지는 버린다
 * - pom 이 okhttp 4.12.0 을 요구하지만 프로젝트는 5.4.0 을 해석 — okhttp 5 위에서 정상 동작 실측
 *
 * ## (보강) Ktor 3 SSE 클라이언트 — 같은 스트림을 Flow 로 (#105, Ktor 3.6.0)
 * - 공식 문서: https://ktor.io/docs/client-server-sent-events.html
 * - 구성: `HttpClient(OkHttp) { install(SSE) }` → `client.sse(url, request = { header(UserAgent, …) }) { incoming.take(10).collect { } }`.
 *   이벤트는 `ServerSentEvent(data, event, id, retry, comments)` — okhttp-eventsource 의 MessageEvent(eventName·lastEventId·data)와 같은 필드
 * - 같은 OkHttp 위에서 비교하려고 OkHttp 엔진을 쓴다(SSECapability 지원). MockEngine 은 SSE 를 지원하지 않아 실제 스트림으로 잰다
 * - 3.6.0 SSEConfig: `reconnectionTime`(기본 3s) · `maxReconnectionAttempts`(기본 **0 = 재연결 안 함**) · `showCommentEvents()` ·
 *   `showRetryEvents()` · `bufferPolicy`. okhttp-eventsource 는 RetryDelayStrategy 로 기본 재연결한다
 *
 * ### 실측 (SM-A725F · Android 13, Wikimedia recentchange, debug 실기기)
 * - 스레드: eventsource 는 전용 스레드 2개(`okhttp-eventsource-events`·`-stream`)를 쓰고 close 1초 뒤 0. Ktor 는 전용 스레드 없이
 *   OkHttp 스레드(`OkHttp Dispatcher`·`OkHttp stream.wikimedia.org`·`… onSettings`)만 쓴다
 * - 종료: Ktor 는 take(10) 이 끝나면 블록을 빠져나오며 세션이 닫히고, 중간 종료는 Job 취소 — 종료 뒤 처리된 이벤트 0개.
 *   eventsource 는 종료를 정한 뒤에도 close() 가 끝날 때까지 받은 이벤트가 콜백으로 계속 온다(0~15개, 실행마다 다름) →
 *   종료를 정한 순간 플래그를 세워 거른다(참조가 비워질 때까지 기다리면 10개에서 멈췄는데 11개가 반영됐다)
 * - 화면 이탈(ViewModel 정리): Ktor 는 viewModelScope 취소로 수집이 멈추고 onCleared 에서 클라이언트를 닫으면 연결 스레드가 사라진다.
 *   eventsource 는 onCleared 의 close() 로 멈춘다. 둘 다 1.5초 안에 연결 스레드가 정리되고 이벤트 수가 더 늘지 않았다
 * - 첫 이벤트까지 1~2초(네트워크 상태에 따른 참고값)
 */
