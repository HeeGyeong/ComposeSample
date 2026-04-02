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
 */
