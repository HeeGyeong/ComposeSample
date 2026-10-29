package com.example.composesample.presentation.example.component.architecture.pattern.molecule

/**
 * Molecule Presenter Example 참고 자료
 *
 * - Molecule(Cash App): https://github.com/cashapp/molecule
 * - The state of managing state (with Compose) — Jake Wharton: https://code.cash.app/the-state-of-managing-state-with-compose
 * - Compose & kotlinx.html — Jake Wharton(Compose runtime 을 UI 밖 트리에 쓰기): https://jakewharton.com/compose-and-kotlinx-html/
 * - MonotonicFrameClock: https://developer.android.com/reference/kotlin/androidx/compose/runtime/MonotonicFrameClock
 *
 * 핵심 개념:
 * - Molecule 은 Compose runtime(Recomposer + Composition)만 UI 없이 띄워 @Composable 함수의 반환값을 Flow/StateFlow 로 내보낸다.
 *   노드를 만들지 않으므로 Applier 는 Unit 을 받는 빈 구현이다(molecule-runtime 2.2.0 소스).
 * - 진입점 두 가지:
 *   · CoroutineScope.launchMolecule(mode, context) { ... } : StateFlow — 스코프에 컴포지션 하나를 띄운다(핫).
 *     첫 컴포지션을 호출 안에서 끝내므로 StateFlow 의 첫 값이 곧 프리젠터의 첫 결과다.
 *   · moleculeFlow(mode) { ... } : Flow — 수집할 때마다 컴포지션을 새로 만든다(콜드).
 * - RecompositionMode:
 *   · ContextClock — 컨텍스트의 MonotonicFrameClock 이 프레임을 줄 때 재구성. Android 화면용은 Molecule 의
 *     AndroidUiDispatcher.Main(메인 디스패처 + Choreographer 시계)을 컨텍스트에 더한다. 시계가 없으면 Recomposer 가
 *     "A MonotonicFrameClock is not available in this CoroutineContext" 로 실패한다(viewModelScope 그대로는 안 된다).
 *   · Immediate — 바뀔 때마다 바로 재구성. 단위 테스트(moleculeFlow + Turbine)와 프레임과 무관한 곳에 쓴다.
 * - SnapshotNotifier(2.2.0): 기본 WhileActive 는 전역 스냅샷 쓰기 관찰자를 등록해 apply 알림을 직접 보낸다.
 *   External 은 Compose UI 처럼 이미 알림을 보내는 쪽이 있을 때만 쓴다.
 *
 * 예제가 보여 주는 것(실측은 화면의 각 카드):
 * 1. 같은 이벤트를 Flow(combine·flatMapLatest·stateIn) 프리젠터와 Molecule 프리젠터에 흘려 모델·전이·검색 수 대조
 * 2. Immediate / ContextClock 에서 쓰기 패턴(한 번에 · yield · 프레임마다 · 다른 스레드)별 방출 수
 * 3. launchMolecule(핫) 와 moleculeFlow(콜드)를 두 수집기로 볼 때의 컴포지션·검색 수
 * 4. 시계 없는 ContextClock 의 실패가 처리기와 호출자에게 각각 어떤 예외로 가는지
 */
