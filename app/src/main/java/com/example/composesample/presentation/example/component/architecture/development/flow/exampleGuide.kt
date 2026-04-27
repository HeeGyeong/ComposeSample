package com.example.composesample.presentation.example.component.architecture.development.flow

/**
 * Flow 연산자 예제 참고 자료
 *
 * - 공식 문서: https://kotlinlang.org/docs/flow.html
 *
 * ## flatMap 계열 연산자 비교
 * 핵심 개념:
 * - flatMapConcat: 이전 Flow 완료 후 다음 시작. 순서 보장. (직렬 실행)
 * - flatMapMerge: 모든 Flow 동시 실행. 순서 미보장. (병렬 실행)
 * - flatMapLatest: 새 값 도착 시 이전 Flow 취소 후 새로 시작. (최신값 유지)
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
 */
