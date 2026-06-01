package com.example.composesample.presentation.example.component.data.sse

/**
 * Data/SSE 예제 참고 자료
 *
 * ## SSEExampleUI (Server-Sent Events 실시간 스트리밍)
 * - SSE 표준(MDN): https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
 * - okhttp-eventsource: https://github.com/launchdarkly/okhttp-eventsource
 * 핵심 개념:
 * - SSE: 서버 → 클라이언트 단방향 텍스트 스트림 (WebSocket과 달리 단방향, HTTP 기반)
 * - EventSource로 이벤트 수신 → callbackFlow/Channel 로 Compose State에 브릿지
 * - StateFlow.update { } 패턴으로 누적 메시지를 불변 리스트로 갱신
 * - 화면 이탈 시 EventSource close + 코루틴 취소로 리소스 정리
 */
