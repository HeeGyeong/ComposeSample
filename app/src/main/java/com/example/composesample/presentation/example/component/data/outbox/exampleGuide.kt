package com.example.composesample.presentation.example.component.data.outbox

/**
 * Data/Outbox 예제 참고 자료
 *
 * ## OfflineOutboxExampleUI (오프라인 아웃박스 전송 — 멱등 키)
 * - 출처: Manuel Lorenzo, "Design a chat app (Android and iOS): outbox, idempotent sends and sync"(Android Weekly #749):
 *   https://mobiledesignrounds.com/mobile-system-design/chat-app/
 *   WebSocket 수신·FCM 힌트 동기화·iOS 송신 루프는 서버·Firebase 가 필요해 빼고, 아웃박스 송신 경로만 구현했다
 * - WorkManager 작업 정의(백오프·제약): https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work
 * - WorkManager 작업 체이닝(실패·취소 전파): https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/chain-work
 * - ExistingWorkPolicy: https://developer.android.com/reference/androidx/work/ExistingWorkPolicy
 * - Room @Upsert: https://developer.android.com/reference/androidx/room/Upsert
 * - 오프라인 우선 앱 설계: https://developer.android.com/topic/architecture/data-layer/offline-first
 * - Ktor MockEngine: https://ktor.io/docs/client-testing.html
 * 핵심 개념(실측: 에뮬레이터 Pixel 4 API 30 debug·release — 실기기 미연결, JVM 단위 테스트 OutboxEngineTest 12건):
 * - 보내기 = Room 에 PENDING 행(기기에서 만든 clientId 가 기본 키) 저장 → 말풍선이 바로 보인다 → 워커가 그 행을 보낸다
 * - 재시도는 행에 저장된 clientId 를 그대로 쓴다 → 서버가 이미 가진 id 면 저장하지 않고 원래 seq 를 돌려준다(멱등).
 *   응답 유실 실험: 같은 id 는 서버 1건(시도 2 에 "원래 seq 1 반환"), 워커가 시도마다 새 id 를 만들면 seq 1·2 로 2건 — 약 11~12초(백오프 10초)
 * - 응답 유실 직후 앱 종료: HOME 3초 뒤 adb shell am kill → 다음 시도 시각에 시스템이 새 프로세스로 워커를 띄워 같은 id 로 재전송,
 *   서버 기록의 pid 가 다른 두 요청(유실 · 원래 seq 반환), 다시 열면 SENT. HOME 직후 1초에는 am kill 해도 프로세스가 남았다
 * - 순서는 서버가 매긴 seq 로 — 기기 시계는 쓰지 않는다. 일시 장애(503) 때 대화별 체인은 첫째 → 둘째를 지키되 둘째가 약 11초 기다리고,
 *   메시지마다 따로 걸면 둘째가 1초 안에 먼저 저장돼 둘째 → 첫째로 뒤집힌다. 응답만 잃은 경우는 첫째가 이미 저장돼 순서가 유지된다
 * - 영구 실패 뒤 체인: 워커가 Result.failure() 면 함께 매단 둘째는 정책과 무관하게 작업 FAILED(행은 PENDING 으로 멈춤),
 *   끝난 뒤 등록한 셋째는 APPEND 면 곧바로 FAILED · APPEND_OR_REPLACE 면 새 체인으로 SENT. 행만 FAILED + success() 면 둘째·셋째 모두 SENT
 * - @Upsert(Room 2.8.5 EntityUpsertAdapter): insert 가 고유성 위반이면 기본 키로 UPDATE 한다 → 자동 증가 키 + clientId 고유 인덱스
 *   설계는 중복 도착 세 번이 0행 UPDATE 로 조용히 버려져 seq 가 null 로 남는다. @Insert(REPLACE) 는 반영되지만 local_id 가 1 → 4 로 바뀐다
 * - 재시도 분류: IOException·408·429·5xx 재시도 / 그 밖 4xx 영구 → FAILED / 401 은 토큰 1회 갱신. 예외는 원인 사슬로 본다 —
 *   MockEngine 람다가 던진 LostResponseException(IOException)은 debug(HotSwan) 단위 테스트에서 UndeclaredThrowableException 두 겹에 감싸여 나왔다
 * - 백오프: EXPONENTIAL · 최소 10초(MIN_BACKOFF_MILLIS, 기본 30초) → 10·20·40초, 4번째도 재시도 사유면 FAILED
 * - 행 저장과 작업 등록은 다른 DB 라 한 트랜잭션이 아니다 → 화면을 열 때 작업 없는 PENDING 행을 다시 건다
 * - 구현 메모: 실험의 완료 판정은 작업 상태를 먼저, 행을 나중에 읽는다 — 반대로 읽으면 두 읽기 사이에 워커가 끝날 때
 *   옛 PENDING 행 + 끝난 작업을 '멈춤'으로 잘못 판정한다(release 에서 실제로 났다)
 */
