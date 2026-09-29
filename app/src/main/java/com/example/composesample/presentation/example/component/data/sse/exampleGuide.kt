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
 * - 화면 이탈 시 EventSource close + 코루틴 취소로 리소스 정리
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
 */
