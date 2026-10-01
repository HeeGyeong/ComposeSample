package com.example.composesample.presentation.example.component.architecture.state

/**
 * Compose State / Snapshot System 참고 자료
 *
 * --- Compose Snapshot System ---
 * - 공식 문서: https://developer.android.com/develop/ui/compose/state
 * - Snapshot: Compose의 상태 격리 메커니즘 (git처럼 State를 복사해 독립적으로 수정)
 * - withMutableSnapshot { }: 여러 State를 원자적으로 변경 (중간 리컴포지션 발생 없음)
 * - derivedStateOf { }: 의존 State가 변경될 때만 재계산 (과도한 리컴포지션 방지)
 * - Snapshot.takeSnapshot(): Composition 외부(Worker Thread 등)에서 상태를 안전하게 읽는 법
 *
 * Snapshot 계층 구조:
 * - GlobalSnapshot: 전체 앱의 최상위 Snapshot, 리컴포지션 스케줄러가 여기서 변경을 감지
 * - MutableSnapshot: apply() 호출 시 부모 Snapshot에 변경사항을 병합
 * - 충돌(conflict): 같은 State를 두 Snapshot이 동시에 수정 시 mergePolicy로 해결
 *
 * --- SnapshotFlow ---
 * - snapshotFlow { state.value }: Snapshot 읽기를 Flow로 변환
 * - distinctUntilChanged()가 기본 내장되어 동일 값 중복 방출 없음
 * - LaunchedEffect 안에서 collect 하여 Side Effect 처리에 활용
 * - 공식 문서: https://developer.android.com/develop/ui/compose/side-effects#snapshotFlow
 *
 * 핵심 개념:
 * - State<T>는 내부적으로 StateStateRecord를 통해 Snapshot별 값을 관리
 * - Composition은 매 프레임 GlobalSnapshot을 통해 변경된 State를 감지하고 리컴포지션을 예약
 * - derivedStateOf는 내부적으로 DerivedSnapshotState로 구현되며, 읽기 시점에 의존성을 등록
 *
 * --- Per-Item ViewModels in Compose ---
 * - 참고: https://saurabharora.dev/posts/per-item-viewmodels-in-compose/
 * - 핵심: LazyColumn 의 각 아이템에 독립 ViewModel 스코프를 부여
 * - 패턴: CompositionLocalProvider(LocalViewModelStoreOwner provides itemOwner) 로 store 교체
 *         → viewModel() 호출은 그대로 두고 store 출처만 갈아끼움
 * - 메모리 정리: DisposableEffect onDispose 에서 store.clear() 로 ViewModel.onCleared() 호출
 *
 * --- (보강 #121) lifecycle 2.11 공식 per-key API ---
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/lifecycle (2.11.0-alpha02 도입, beta01 에서 키 유무 오버로드 분리)
 * - `rememberViewModelStoreProvider(parent = LocalViewModelStoreOwner.current)` + `rememberViewModelStoreOwner(key, provider)` →
 *   `CompositionLocalProvider(LocalViewModelStoreOwner provides owner) { viewModel { … } }`.
 *   `ViewModelStoreProvider` 는 lifecycle-viewmodel-savedstate, 두 remember 함수는 lifecycle-viewmodel-compose 에 있다
 * - 동작(소스 + 실측): Provider 는 부모 Store 안에 상태 보관용 ViewModel 을 두고(키 없으면 호출 위치의 composite key hash 로),
 *   키별 Store 를 그 안에 모은다 → **구성 변경에서 살아남는다**. owner 는 토큰만 잡았다 놓고 clearKey 는 부르지 않는다 →
 *   **아이템을 데이터에서 지워도 자동으로 비워지지 않는다**(clearKey 를 직접). Provider 가 컴포지션을 떠날 때 부모 수명주기가
 *   CREATED 이상이면 clearAllKeys(), 재생성 중(DESTROYED)이면 건너뛴다
 * - 실측(Galaxy A72, 실제 앱 + 계측 프로브):
 *   · 액티비티 재생성(다크 모드 전환 2회): 수작업 전부 새 인스턴스 / 공식 같은 인스턴스·카운트 유지
 *   · 아이템 삭제: 수작업 즉시 onCleared / 공식은 clearKey 없으면 남고(화면을 떠날 때 정리), 있으면 즉시 정리
 *   · 접기(컴포지션 이탈)·스크롤로 섹션이 화면 밖에 나감: 수작업 비워짐 → 새 인스턴스 / 공식 유지
 *   · 화면 이탈: 공식도 clearAllKeys 로 전부 정리(지운 채 남아 있던 것 포함)
 * - 함정(실측):
 *   · 수작업 구현의 forEach 에 key() 가 없으면 중간 아이템을 지울 때 그 뒤 아이템들의 store 까지 비워진다(기존 구현 버그 → key() 로 수정)
 *   · Provider 를 스크롤로 사라지는 lazy 항목 안에 두면 그 항목이 빠질 때 clearAllKeys 로 전부 비워진다 → LazyColumn·Pager 밖에 둔다
 *   · 계측 테스트에서 재생성 후 setContent 를 늦게 부르면 저장 상태 복원이 안 돼 composite key hash 가 바뀌고 키 없는 Provider 가
 *     이전 상태를 못 찾는다(rememberSaveable 도 초기화) — 실제 앱처럼 onCreate 안에서 setContent 해야 정상 측정
 * - 이 비교를 화면에서 하려고 BlogExampleActivity 의 열린 예제 상태를 rememberSaveable 로 바꿨다(EX-CONFIG-RESET-01, `25235a1e`) —
 *   그 전에는 구성 변경 때 예제가 닫히고 목록으로 돌아갔다
 */
