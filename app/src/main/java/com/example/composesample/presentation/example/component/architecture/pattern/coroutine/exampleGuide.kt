package com.example.composesample.presentation.example.component.architecture.pattern.coroutine

/**
 * Coroutine Example 참고 자료
 *
 * - 공식 문서: https://developer.android.com/kotlin/coroutines
 * - Kotlin 코루틴 가이드: https://kotlinlang.org/docs/coroutines-guide.html
 *
 * 코루틴 스코프 비교:
 * - GlobalScope         — 앱 전체 생명주기, 거의 사용하지 않음 (메모리 누수 위험)
 * - viewModelScope      — ViewModel이 clear될 때 자동 취소 (UI 관련 작업 권장)
 * - lifecycleScope      — Activity/Fragment 생명주기와 연동
 * - rememberCoroutineScope — Composable 컴포지션 이탈 시 취소 (Compose 전용)
 *
 * Dispatcher 비교:
 * - Dispatchers.Main    — UI 스레드, Composable 상태 업데이트
 * - Dispatchers.IO      — 파일 I/O, 네트워크, DB 작업
 * - Dispatchers.Default — CPU 집약적 작업 (파싱, 정렬 등)
 * - Dispatchers.Unconfined — 호출 스레드에서 즉시 실행, 테스트용
 *
 * 구조적 동시성 (Structured Concurrency):
 * - 부모 코루틴 취소 시 모든 자식 취소
 * - 자식 실패 시 부모와 형제 코루틴도 취소 (SupervisorJob 제외)
 * - coroutineScope { }: 모든 자식 완료 후 반환, 예외 전파
 * - supervisorScope { }: 자식 실패가 다른 자식에 영향 없음
 *
 * CoroutinesInternals:
 * - 코루틴은 Continuation 객체로 컴파일 (상태 머신 변환)
 * - suspend 함수 = Continuation<T> 파라미터가 추가된 일반 함수
 * - withContext: 디스패처 전환 후 완료, launch: 새 코루틴 시작 (fire-and-forget)
 */
