package com.example.composesample.presentation.example.component.data.download

/**
 * Data/Download 예제 참고 자료
 *
 * ## RangeDownloadExampleUI (이어받기·분할 다운로드 — HTTP Range)
 * - 출처: linroid/Ketch(Android Weekly #748) — "How a download works": https://github.com/linroid/Ketch/blob/main/docs/developers.md
 *   Ketch 라이브러리(KMP·RC 단계)는 쓰지 않고 같은 단계를 Ktor 클라이언트 + RandomAccessFile 로 직접 구현했다
 * - Ketch 아키텍처(다운로드 파이프라인·토큰 버킷·TaskStore): https://github.com/linroid/Ketch/blob/main/docs/architecture.md
 * - HTTP Range 요청(RFC 9110 §14): https://www.rfc-editor.org/rfc/rfc9110#name-range-requests
 * - If-Range(RFC 9110 §13.1.5): https://www.rfc-editor.org/rfc/rfc9110#name-if-range
 * - MDN HTTP range requests: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Range_requests
 * - Ktor MockEngine: https://ktor.io/docs/client-testing.html
 * 핵심 개념(실측: SM-A725F debug·release, JVM 단위 테스트 RangeDownloadTest 10건):
 * - Resolve: HEAD 로 Content-Length·Accept-Ranges·ETag·Last-Modified. HEAD 를 거부(405)하면 GET Range: bytes=0-0 →
 *   206 Content-Range "bytes 0-0/전체" 로 크기·Range 지원을 함께 안다(본문 1B). Accept-Ranges 가 없으면 연결 1개
 * - Plan · Download: 크기를 연결 수로 나눠 구간마다 연결 하나가 Range 로 받고, 미리 setLength 한 .part 파일의
 *   제 위치에 FileChannel.write(buf, position) — 위치 지정 쓰기라 연결끼리 잠금이 필요 없다
 * - Throttle: 토큰 버킷 하나를 모든 연결이 나눠 쓴다 → 256KiB/s 에서 연결 1개 7.87초 · 4개 7.86초(평균 260KiB/s 동일).
 *   모자라면 빚을 지고 그 시간만큼 쉰다(청크가 버킷보다 커도 멈추지 않음)
 * - Persist · Resume: 구간별 진행을 .state(JSON)에 300ms 마다·멈출 때 저장(임시 파일 → 이름 바꾸기). 프로세스를 끝내도
 *   다시 열면 50% 를 되살려 남은 1008KiB 만 받는다. 상태 파일은 nullable 저장 모델 + 매퍼로 읽어 필드가 빠지면 처음부터
 * - 재개 확인 3가지 × 서버 파일 교체 시점 2가지: If-Range 는 둘 다 맞음(ETag 가 다르면 200 전체 → 처음부터),
 *   재개 전 HEAD 비교는 HEAD 와 범위 요청 사이 교체를 놓쳐 섞임(크기 2MiB 그대로 · v1 약 85만 B + v2 약 124만 B),
 *   확인 없음은 둘 다 섞임 → 판정과 전송이 한 요청인 If-Range 에는 경쟁 창이 없다(Ketch 문서는 방법을 적지 않는다)
 * - 함정: 200(전체)을 206 처럼 멈춘 위치에 이어 쓰면 843,776B + 2MiB = 2,940,928B. 끊긴 응답(받은 길이 < 요청 길이)을
 *   완료로 치면 크기는 맞고 구멍(0)이 남아 v1 과 같은 바이트 약 123만 → 크기 검사로는 못 잡고 CRC·해시로 확인
 * - Complete: 다 받은 뒤에야 .part → 최종 이름(덜 받은 파일이 완성본처럼 보이지 않게), .state 삭제. 취소는 .part·.state 삭제
 * - 구현 메모: 가짜 서버(FakeFileServer)는 synchronized 블록으로 한 요청이 한 버전만 보게 한다 — @Synchronized 는 debug(HotSwan)
 *   재작성 아래에서 상호 배제가 사라졌다(ComposeHotReloadGuide). 재시작 신호 예외는 RuntimeException(checked 면 HotSwan 이 감쌈).
 *   엔진 3클래스는 HotSwan 제외(6가지 시나리오 debug 34.4초 → 7.0초, release 3.4초)
 */
