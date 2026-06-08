package com.example.composesample.presentation.example.component.architecture.development.concurrency

/**
 * 코루틴 동시성 예제 참고 자료
 *
 * ## Coroutines Internals (코루틴 내부 동작 원리)
 * - 공식 문서: https://kotlinlang.org/docs/coroutines-guide.html
 * - KotlinConf 발표: https://youtu.be/YrrUCSi72E8
 * 핵심 개념:
 * - CPS(Continuation Passing Style): 코루틴은 컴파일 시 상태 머신으로 변환됨
 * - Continuation: 일시 중단 지점 이후 실행 흐름을 캡처한 콜백 객체
 * - suspend 함수 = 실행 중단이 가능한 함수. 재개 시 Continuation.resumeWith() 호출
 * - CoroutineDispatcher: 코루틴이 실행될 스레드/스레드 풀 결정 (Main/IO/Default/Unconfined)
 *
 * ## WithContext (컨텍스트 전환 패턴)
 * - 공식 문서: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-context.html
 * 핵심 개념:
 * - withContext(Dispatchers.IO) { } : 현재 코루틴 내에서 디스패처만 전환 (새 코루틴 미생성)
 * - launch vs withContext: launch는 새 코루틴 생성, withContext는 같은 코루틴에서 전환
 * - withContext는 결과값 반환 가능 (val result = withContext(IO) { computeResult() })
 * - UI 업데이트: withContext(Dispatchers.Main) { view.text = result }
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
 */
