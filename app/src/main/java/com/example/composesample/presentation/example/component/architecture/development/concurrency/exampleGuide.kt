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
 */
