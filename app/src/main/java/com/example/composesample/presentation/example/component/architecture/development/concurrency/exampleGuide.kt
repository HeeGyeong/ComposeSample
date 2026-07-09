package com.example.composesample.presentation.example.component.architecture.development.concurrency

/**
 * 코루틴 동시성 예제 참고 자료
 *
 * ## Coroutines Internals (코루틴 내부 동작 원리)
 * - 공식 문서: https://kotlinlang.org/docs/coroutines-guide.html
 * - KotlinConf 발표: https://youtu.be/YrrUCSi72E8
 * - 출처: https://proandroiddev.com/inside-kotlin-coroutines-state-machines-continuations-and-structured-concurrency-b8d3d4e48e62
 * 핵심 개념:
 * - CPS(Continuation Passing Style): 코루틴은 컴파일 시 상태 머신으로 변환됨
 * - Continuation: 일시 중단 지점 이후 실행 흐름을 캡처한 콜백 객체
 * - suspend 함수 = 실행 중단이 가능한 함수. 재개 시 Continuation.resumeWith() 호출
 * - CoroutineDispatcher: 코루틴이 실행될 스레드/스레드 풀 결정 (Main/IO/Default/Unconfined)
 * - State Machine 변환: suspend 함수 내 각 지점(label)마다 지역 변수를 Continuation 필드로 저장, COROUTINE_SUSPENDED 반환 시 즉시 중단
 * - Structured Concurrency: 부모는 모든 자식 완료까지 대기, 부모 취소 시 자식 전파 취소, 자식 실패 시 부모도 취소(SupervisorJob 예외)
 *
 * ## WithContext (컨텍스트 전환 패턴)
 * - 공식 문서: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html
 * - 출처: https://proandroiddev.com/the-real-difference-between-withcontext-dispatchers-io-and-launch-dispatchers-io-b70ec00a33f2
 * 핵심 개념:
 * - withContext(Dispatchers.IO) { } : 현재 코루틴 내에서 디스패처만 전환 (새 코루틴 미생성)
 * - launch vs withContext: launch는 새 코루틴 생성, withContext는 같은 코루틴에서 전환
 * - withContext는 결과값 반환 가능 (val result = withContext(IO) { computeResult() })
 * - UI 업데이트: withContext(Dispatchers.Main) { view.text = result }
 * - 예외 처리 차이: withContext/async는 호출자에게 예외 전파(try-catch 가능), launch는 예외가 내부에 갇혀 CoroutineExceptionHandler 또는 내부 try-catch 필요
 * - 선택 기준: 결과 불필요=launch, 결과+순차=withContext, 결과+병렬=async/await
 *
 * ## CoroutineBridges (콜백 → suspend 변환 패턴)
 * - 출처: https://www.revenuecat.com/blog/engineering/kotlin-coroutine-bridge/
 * 핵심 개념:
 * - suspendCoroutine { cont -> } : 콜백 기반 API를 suspend 함수로 래핑. 성공 시 cont.resume(value), 실패 시 cont.resumeWithException(e)
 * - suspendCancellableCoroutine { cont -> } : 취소 전파 지원. cont.invokeOnCancellation { } 블록에서 리소스 정리
 * - suspendCoroutine vs suspendCancellableCoroutine: 취소 가능 여부 차이. 장기 실행 작업엔 후자 권장
 * - invokeOnCancellation: 코루틴 취소 시 콜백 해제, 연결 종료 등 정리 작업 수행 지점
 *
 * ## Race Condition (공유 가변 상태 보호 전략)
 * - 공식 문서: https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html
 * - 출처: How to Prevent Race Conditions in Coroutines (Dave Leeds, Android Weekly #730)
 * 핵심 개념:
 * - Race condition: 여러 코루틴이 공유 가변 상태를 동시에 read-modify-write 하면 갱신이 겹쳐 유실됨 (counter++ 는 원자적이지 않음)
 * - AtomicInteger/AtomicLong: CAS(compare-and-set) 기반 원자 연산. 단일 값 카운터/플래그에 가장 가볍고 빠름 (락 없음)
 * - Mutex.withLock { }: 코루틴 친화적 상호 배제 락. 여러 상태를 함께 일관되게 갱신할 때. 경합 시 직렬화 비용 발생
 * - 단일 스레드 confinement: Dispatchers.Default.limitedParallelism(1) 디스패처에 갱신을 한정해 동시 접근 자체를 제거
 * - MutableStateFlow.update { }: CAS 루프로 안전하게 상태 갱신. Compose 에 노출하는 상태에 적합
 * - 주의: @Synchronized/Java ReentrantLock 은 스레드를 블로킹 → 코루틴에는 Mutex 사용
 *
 * ## Select Expressions (여러 suspending 작업 경쟁 선택)
 * - 공식 문서: https://kotlinlang.org/docs/select-expression.html
 * - 출처: The Task Shapes the Strategy: Kotlin Select Expressions in Practice (Android Weekly #731)
 * 핵심 개념:
 * - select { }: 여러 절(clause) 중 가장 먼저 준비되는 하나만 선택해 그 블록을 실행. 나머지 절은 무시됨
 * - Deferred.onAwait { }: 여러 async 결과 중 먼저 끝난 것을 채택 (최속 미러 응답, 헤지 요청 패턴)
 * - onTimeout(ms) { }: 다른 절이 한도 내 준비되지 않으면 발동 → 폴백 값 반환 (withTimeoutOrNull 보다 유연하게 조합 가능)
 * - Channel.onReceiveCatching { }: 여러 채널을 멀티플렉싱해 도착 순서대로 수신. 채널이 닫혀도 예외 대신 ChannelResult(닫힘) 반환 → 종료 안전 감지
 * - select 는 선택된 절만 실행하므로, 채택되지 않은 Deferred/작업은 직접 cancel() 로 정리해야 리소스 누수가 없음
 * - 한 번의 select 는 절 하나만 선택 → 반복 수신은 while 루프로 감싸고, 모든 소스가 닫히면 종료
 * - onTimeout/일부 채널 API 는 @ExperimentalCoroutinesApi 옵트인 필요
 */
