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
 */
