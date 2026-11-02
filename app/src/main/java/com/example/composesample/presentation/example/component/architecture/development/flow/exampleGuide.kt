package com.example.composesample.presentation.example.component.architecture.development.flow

/**
 * Flow 연산자 예제 참고 자료
 *
 * - 공식 문서: https://kotlinlang.org/docs/flow.html
 *
 * ## flatMap 계열 연산자 비교
 * - 출처: https://proandroiddev.com/understanding-flatmap-vs-flatmaplatest-a-deep-dive-061994b7ffc4
 * 핵심 개념:
 * - flatMapConcat: 이전 Flow 완료 후 다음 시작. 순서 보장. (직렬 실행)
 * - flatMapMerge: 모든 Flow 동시 실행. 순서 미보장. (병렬 실행, concurrency 파라미터로 동시 개수 제어)
 * - flatMapLatest: 새 값 도착 시 이전 Flow 취소 후 새로 시작. (최신값 유지, switchMap과 동일 개념)
 *
 * 사용 가이드:
 * - 검색 자동완성 → flatMapLatest (이전 쿼리 취소)
 * - 파일 다운로드 목록 → flatMapMerge (병렬 처리)
 * - 의존 관계 있는 API 연속 호출 → flatMapConcat (순서 보장)
 *
 * 주의: flatMapConcat은 Coroutines 1.7+에서 deprecated →
 *       transform { ... emitAll(flow) } 패턴 또는 flatMapLatest 권장
 *
 * ## buffer / conflate / debounce / sample 비교
 * - buffer(capacity): 업스트림과 다운스트림을 분리. 업스트림은 즉시 emit, 다운스트림은 자기 속도로 처리.
 *   기본 capacity = 64. BufferOverflow.SUSPEND/DROP_OLDEST/DROP_LATEST 선택 가능.
 * - conflate(): buffer(CONFLATED)와 동일. 다운스트림이 처리 중일 때 새 값이 오면 이전 값을 덮어쓴다.
 *   "최신값만 중요"한 UI 상태 동기화에 유용.
 * - debounce(timeoutMillis): 마지막 emit 후 timeout 동안 새 값이 없을 때만 다운스트림으로 전달.
 *   검색창 타이핑 등 "사용자가 멈춘 순간"이 중요한 입력에 사용.
 * - sample(periodMillis): 주기적으로 가장 최근 값을 샘플링. 변화량이 많은 스트림에서 일정 간격 갱신.
 *
 * 선택 기준:
 * - 모든 값을 처리하되 처리 속도 보장 → buffer
 * - 최신 값만 처리 (중간 값 손실 OK) → conflate
 * - 사용자 입력 종료 후 한 번만 → debounce
 * - 일정 간격으로 최신 상태만 → sample
 *
 * 출처: Dave Leeds, "Kotlin Flows: Buffer, Conflate, and Debounce"
 *       https://www.youtube.com/watch?v=rHYlUGC109I
 *
 * ## onEachBatch — 크기/시간 기준 배치 집계
 * Flow 표준 라이브러리에는 없는 커스텀 연산자. 원소를 maxSize 또는 timeout 기준으로
 * List 로 묶어 일괄 처리한다. DB bulk insert·네트워크 배치 전송처럼 "호출당 고정 오버헤드"가
 * 큰 작업에서 호출 횟수를 줄여 처리량을 높이는 데 사용.
 *
 * 구현 핵심:
 * - produceIn 으로 업스트림을 채널에 버퍼링 (emit/처리 속도 분리)
 * - withTimeoutOrNull(timeout) { channel.receiveCatching() } 로 시간 윈도우 구현
 * - 첫 원소 도착 시 윈도우 시작 → maxSize 도달 시 즉시 flush, timeout 경과 시 미완성 배치 flush
 * - 업스트림 종료(isClosed) 시 남은 원소를 마지막 배치로 flush (손실 없음)
 *
 * buffer/conflate 와의 차이:
 * - buffer: 원소 1건씩 그대로 전달, 속도만 분리 (개수 보존)
 * - conflate: 처리 중 중간 값 버리고 최신값만 (개수 손실)
 * - onEachBatch: 여러 원소를 List 로 묶어 일괄 처리 (개수 보존 + 호출 횟수 감소)
 *
 * 출처: kotlin-coroutines-recipes/onEachBatch.kt (Marcin Moskała)
 *       https://github.com/MarcinMoskala/kotlin-coroutines-recipes
 *
 * ## Flow 함정 — 콜드 Flow 다중 수집 · shareIn 설정 · catch 가 잡는 범위
 * - 출처: Kaustubh Deshmukh, "Kotlin Flow: Bugs That Look Correct at First" (Android Weekly #751)
 *   https://kt.academy/article/kaustubh_flow_bugs_that_look_correct_at_first
 * - shareIn 공식 문서: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/share-in.html
 * - SharingStarted.WhileSubscribed: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-sharing-started/-companion/-while-subscribed.html
 * - catch 공식 문서: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/catch.html
 *
 * 글의 네 가지 중 이 예제가 다루는 것(나머지 둘은 기존 예제로 넘긴다):
 * - 콜드 Flow 다중 수집 → FlowPitfallsExampleUI 1·2번 카드
 * - catch 의 범위 → 3·4번 카드
 * - conflate 값 누락 → FlowOperatorsExampleUI 3번 카드
 * - stateIn(WhileSubscribed) 가 구독자 없이 업스트림을 시작하지 않음 → init/InitTestExampleUI
 *
 * 실측(coroutines 1.11.0 — JVM 단위 테스트 FlowPitfallsLabTest 는 가상 시간, 화면은 실제 시간·Main 디스패처):
 * - 콜드 Flow 를 두 곳에서 수집 → API 2회, 두 화면이 서로 다른 응답(v1·v2). shareIn(WhileSubscribed(), replay = 1) → 1회, 둘 다 v1
 * - 늦게 붙은 구독자: replay = 0 → 받은 값 없음 / replay = 1 → 캐시된 v1, 업스트림 재실행 없음
 * - 구독자 0 → 재구독: WhileSubscribed() 는 업스트림을 다시 돌리고(API 2회) 옛 값 v1 을 먼저, 새 v2 를 이어 준다
 *   (replayExpirationMillis 기본값이 Long.MAX_VALUE 라 멈춰도 캐시가 남는다).
 *   stopTimeoutMillis = 5_000 안에 재구독 → 재실행 없이 v1 만. replayExpirationMillis = 0 → 옛 값 없이 v2 만
 * - catch 위치(x = 1): catch → onEach 순서면 catch 0회 · IllegalStateException(Boom!) 이 collect 밖으로 /
 *   onEach → catch 순서면 catch 1회 · -1 수신 / collect 람다 예외는 catch 0회 · 밖으로
 * - catch 와 취소: 수집 코루틴 취소 → catch 0회 / withTimeout 이 수집을 감쌈 → catch 0회, TimeoutCancellationException 이 밖으로 /
 *   withTimeout 이 상류 안 → catch 1회(TimeoutCancellationException), -1 수신 /
 *   그 핸들러에서 CancellationException 을 다시 던지면 launch 가 취소로 끝나 부모에 실패를 전하지 않는다
 *   (catchImpl 은 '수집 코루틴의 Job 이 취소됐고 그 원인과 같은 예외'일 때만 다시 던진다 — 취소되지 않은 상태의 CancellationException 은 잡는다)
 *
 * 글의 예제 오류(그대로 돌려 확인):
 * - catch 예제는 flowOf(1000) 에 if (it == 1) 이라 어느 순서에서도 예외가 나지 않는다(둘 다 1000 만 수신).
 *   화면은 실패 값을 1 로 바꾸고, x = 1000 칩으로 원문 그대로의 결과도 보여 준다
 * - conflate 예제의 '고친 코드'는 conflate 를 지우면서 delay(500) 도 delay(100) 으로 함께 바꿨다(설명 없음)
 * - 맺음말은 '다섯 가지 질문'이라 하고 네 가지만 적었다
 */
