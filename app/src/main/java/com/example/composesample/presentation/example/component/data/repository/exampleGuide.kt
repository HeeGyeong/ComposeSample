package com.example.composesample.presentation.example.component.data.repository

/**
 * Data/Repository 예제 참고 자료
 *
 * ## AdvancedRepositoryPatternExampleUI (Memory → Disk → Network 우선순위 해석)
 * 핵심 개념:
 * - 조회 우선순위: Memory(즉시) → Disk(300ms 시뮬레이션) → Network(900ms 시뮬레이션)
 * - Cache population: 하위 계층에서 찾은 값을 상위 계층에도 채워 넣어 다음 조회를 가속
 * - forceRefresh=true 는 Memory/Disk 를 모두 건너뛰고 Network 로 직행(pull-to-refresh 시나리오)
 * - invalidateMemory()/invalidateDisk() 는 서로 독립 — 디스크만 무효화해도 메모리가 살아있으면
 *   계속 MEMORY 히트가 나온다 (실제 앱에서 캐시 계층을 설계할 때 흔히 놓치는 지점)
 * - ArticleRepository 인터페이스는 domain 모듈에 위치 — ArticleData 가 순수 Kotlin 모델(Room @Entity 아님)이라
 *   가능한 배치. data/cache 예제의 UserCacheRepository(UserData 가 Room @Entity 라 data 레이어에 있어야 함)와
 *   대조해서 보면 Clean Architecture 의 "레이어는 모델의 성격에 따라 결정된다"는 점을 확인할 수 있다.
 * - Disk/Network 계층은 실제 Room/Retrofit 대신 인메모리 맵 + delay 로 시뮬레이션(KtorAdvancedConfig 의
 *   MockEngine, FeatureFlag 의 fetchRemote() 와 동일한 방식) — 이 예제의 목적은 "우선순위 해석 패턴" 자체를
 *   보여주는 것이라 실제 I/O 기술은 의도적으로 배제했다.
 */
