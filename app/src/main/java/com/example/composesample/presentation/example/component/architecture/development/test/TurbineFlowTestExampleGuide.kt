package com.example.composesample.presentation.example.component.architecture.development.test

/**
 * Coroutine Flow Testing (Turbine) Example 참고 자료
 *
 * - 원문: https://programminghard.dev/dont-learn-coroutine-testing-with-turbine/
 *
 * 핵심 개념:
 * - Turbine 이전에 코루틴 테스트 기초(runTest, 가상 시간)를 먼저 이해해야 함
 * - awaitItem() 체이닝은 과명세화(over-specification)를 유발함
 * - StateFlow 테스트: 상태별 독립 테스트 + runCurrent() / advanceUntilIdle()
 * - SharedFlow/단방향 이벤트 스트림에서는 Turbine이 적합
 *
 * 테스트 디스패처 비교:
 * - StandardTestDispatcher: 명시적 진행 제어(runCurrent, advanceUntilIdle)
 * - UnconfinedTestDispatcher: 즉시 실행 (초기 상태 검증에 편리)
 */
