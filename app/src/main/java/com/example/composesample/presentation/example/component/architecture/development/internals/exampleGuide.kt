package com.example.composesample.presentation.example.component.architecture.development.internals

/**
 * How Compose Works (Compose 내부 동작) 예제 참고 자료
 *
 * ## 출처
 * - Android Weekly #732 — How Compose Works
 *
 * ## 공식 문서 / 권장 자료
 * - Compose 단계(컴포지션·레이아웃·그리기): https://developer.android.com/develop/ui/compose/phases
 * - Compose 멘탈 모델: https://developer.android.com/develop/ui/compose/mental-model
 * - 상태와 Jetpack Compose: https://developer.android.com/develop/ui/compose/state
 * - 커스텀 레이아웃(Layout/MeasurePolicy): https://developer.android.com/develop/ui/compose/layouts/custom
 * - graphicsLayer / 그리기 단계: https://developer.android.com/develop/ui/compose/graphics/draw/modifiers
 *
 * ## 핵심 개념 요약
 * 1) 컴파일러 변환
 *    - Compose Compiler 플러그인이 @Composable 함수에 숨은 Composer 파라미터와 $changed 비트마스크를 추가
 *    - 함수 본문을 startRestartGroup(key)/endRestartGroup() 그룹으로 감싸 호출 위치(identity)를 부여
 *    - 입력이 불변이면 skipToGroupEnd() 로 스킵, endRestartGroup().updateScope { } 가 리컴포즈 람다를 등록
 *
 * 2) SlotTable
 *    - 컴포지션 결과(그룹 구조 · remember 값 · 방출된 LayoutNode)를 선형 배열(gap buffer)에 저장
 *    - 리컴포지션 시 그룹 키로 같은 위치를 찾아 슬롯을 재사용하고 변경분만 갱신
 *    - 위치 기반 식별 때문에 조건부/반복 호출에서 위치가 어긋나면 key() 로 안정적 identity 부여 필요
 *
 * 3) Snapshot 읽기 추적 (read-tracking)
 *    - MutableState 읽기를 스냅샷 시스템이 기록 → 값 변경 시 그 state 를 읽은 Composable 만 무효화
 *    - 안정(stable) 파라미터를 가진 Composable 은 입력 불변 시 skip → 불필요한 리컴포지션 방지
 *
 * 4) Layout Pipeline
 *    - measure: 부모가 Constraints 전달 → 자식이 측정되어 Placeable 반환(원칙적으로 단일 패스)
 *    - place: Placeable 을 (x, y) 좌표에 배치(placeRelative 는 LTR/RTL 자동 반영)
 *    - draw: Canvas 에 그리기. graphicsLayer/drawBehind 로 draw 단계만 저렴하게 갱신 가능
 *
 * ## 본 예제 구현 메모
 * - 런타임 내부 클래스(Composer/SlotTable 실물)를 직접 호출하지 않고 개념 + CodeBlock + 동등 시뮬레이션으로 재현
 * - Snapshot 읽기 추적 카드만 실제 동작 시연: 두 독립 state 와 plain CompositionCounter 로
 *   "변경된 state 를 읽은 카드만 리컴포즈됨"을 컴포지션 횟수로 실측 (snapshot state 가 아닌 plain 필드라 추가 무효화 없음)
 */

/**
 * RememberObserver / Composition Lifecycle 예제 참고 자료
 *
 * ## 출처
 * - Inside Compose Side Effects — Jaewoong Eum (doveletter.dev)
 * - Android Weekly #735
 *
 * ## 공식 문서 / 권장 자료
 * - RememberObserver: https://developer.android.com/reference/kotlin/androidx/compose/runtime/RememberObserver
 * - Side-effects in Compose: https://developer.android.com/develop/ui/compose/side-effects
 * - remember: https://developer.android.com/develop/ui/compose/state#remember
 *
 * ## 핵심 개념 요약
 * - `RememberObserver` 는 `remember { }` 로 기억시킨 객체가 구현할 수 있는 3가지 콜백을 제공한다
 *   1) onRemembered — 컴포지션에 커밋되어 진입할 때(최초 remember 또는 재진입)
 *   2) onForgotten — 컴포지션에서 완전히 제거될 때(조건부 제거, 스코프 종료 등)
 *   3) onAbandoned — onRemembered 없이 컴포지션 자체가 커밋 실패로 폐기될 때
 * - 리컴포지션(Composable body 재실행) 자체는 이 콜백을 다시 유발하지 않는다 — remember 슬롯이 유지되는 한
 *   객체 인스턴스는 재생성되지 않으며, 오직 실제로 컴포지션 트리를 벗어났다 다시 들어올 때만 반응한다
 * - DisposableEffect(key) 는 key 변경마다 반응하는 부수 효과 블록인 반면, RememberObserver 는 remember 되는
 *   '값 객체' 자신이 생명주기를 구현 — 파라미터 변경에 영향받지 않고 객체의 컴포지션 멤버십에만 묶인다
 * - Compose 런타임 내부에서 rememberCoroutineScope() 가 이 패턴으로 구현되어 있어, onForgotten 시점에
 *   scope.cancel() 이 자동 호출된다(Composable 이탈 시 launch 된 코루틴이 자동 취소되는 이유)
 *
 * ## 본 예제 구현 메모
 * - 외부 라이브러리 미사용, 순수 androidx.compose.runtime.RememberObserver 인터페이스만 사용
 * - LifecycleDemoCard 가 실제 동작 시연: 자식을 컴포지션에서 추가/제거해 onRemembered/onForgotten 이벤트 로그를 실측하고,
 *   "리컴포지션만 유발" 버튼으로 remember 슬롯이 유지되는 동안은 콜백이 재발화하지 않음을 컴포지션 카운터로 대조
 * - onAbandoned 는 컴포지션 커밋 실패 시나리오라 안전하게 실동작 데모로 재현하기 어려워 개념 설명 + CodeBlock 으로만 제공
 * - rememberCoroutineScope() 내부 구현은 실제 Compose 런타임 소스를 그대로 옮긴 것이 아닌 개념적 재현(HowComposeWorks 와 동일 관례)
 */

/**
 * Composition Observer (컴포지션 관찰 API) 예제 참고 자료
 *
 * ## 출처
 * - Compose State Has No Name — Adit Lal
 * - Android Weekly #739
 *
 * ## 공식 문서 / 권장 자료
 * - CompositionObserver: https://developer.android.com/reference/kotlin/androidx/compose/runtime/tooling/CompositionObserver
 * - Snapshot: https://developer.android.com/reference/kotlin/androidx/compose/runtime/snapshots/Snapshot
 * - Compose 상태와 스냅샷 시스템: https://developer.android.com/develop/ui/compose/state
 * - 리컴포지션 디버깅(Layout Inspector): https://developer.android.com/develop/ui/compose/tooling/layout-inspector
 *
 * ## 핵심 개념 요약
 * 1) CompositionObserver — 컴포지션 축
 *    - `currentComposer.composition` 으로 현재 Composable 이 속한 컴포지션을 얻고 `setObserver()` 로 부착
 *    - 콜백 7종: onBeginComposition / onScopeEnter / onReadInScope / onScopeExit /
 *      onScopeInvalidated / onScopeDisposed / onEndComposition
 *    - 이 중 `onScopeInvalidated(scope, value)` 가 "왜 리컴포즈됐는가"를 답하는 유일한 콜백이다.
 *      기존 리컴포지션 예제들(SideEffect 수동 계측)은 "몇 번"만 답하므로 축이 다르다.
 *
 * 2) Snapshot 관찰 API — 상태 축
 *    - `Snapshot.observeSnapshots(SnapshotObserver)` (실험) — 스냅샷 생성에 개입해 read/write 옵저버를 주입
 *    - `Snapshot.registerGlobalWriteObserver { }` (stable) — 전역 스냅샷의 쓰기만 관찰
 *    - `Snapshot.registerApplyObserver { changed, snapshot -> }` (stable) — 변경이 전역에 적용되는 커밋 시점
 *    - **세 API 의 커버리지는 겹치지 않고 상보적이다**(아래 실측 매트릭스 참조) — 어느 하나로 모든 쓰기를 볼 수 없다
 *
 * 3) 관찰로 완전한 로깅이 불가능한 이유(4가지 벽)
 *    - ① 동일 값 쓰기: 값이 동등하면 setter 가 실제 쓰기를 건너뛰어 write 옵저버가 호출되지 않는다(설계상 불가)
 *    - ② 이전 값 복구: 이전 값을 담은 StateRecord 필드가 internal 이고 레코드가 재사용되어 접근 불가
 *    - ③ 컴포지션 중 쓰기: 컴포지션은 격리 스냅샷에서 진행되어 전역 write 옵저버에 안 잡힘
 *      → `observeSnapshots` 가 유일한 경로
 *    - ④ 이름 부재: 콜백은 상태/스코프 "객체"만 넘겨주므로 그대로는 @1387209841 로만 보인다
 *
 * ## 바이트코드/프로브로 확정한 사항 (Compose 1.11.1)
 * - `Composition.setObserver()` 는 `androidx.compose.runtime.tooling` 의 확장함수이며 **반환이 nullable** 이다
 *   (`ObservableComposition.setObserver()` 멤버 쪽과 시그니처가 다르다)
 * - `onReadInScope(scope, value: Any)` 와 `onScopeInvalidated(scope, value: Any?)` 는 **value 의 nullability 가 다르다**
 * - `IdentifiableRecomposeScope.identity` 는 `@ComposeToolingApi` opt-in 이 별도로 필요하다
 * - `currentComposer.recomposeScope` 는 `@InternalComposeApi` 라 예제에서 사용하지 않는다
 *   (관찰자 콜백이 넘겨주는 scope 로 충분하다)
 *
 * ## 실기기 계측으로 확정한 런타임 동작 (Compose 1.11.1, SM-A725F / API 33)
 * 임시 계측 테스트(`CompositionObserverProbeTest`, 검증 후 삭제)로 측정한 사실이며 추정이 아니다.
 *
 * 1) 콜백 발화 순서 — 상태를 바꾸면 `[invalidated, begin, read, end]` 순으로 기록된다.
 *    즉 **무효화가 먼저 일어나고(쓰기 시점), 그 다음 패스에서 스코프가 재실행되며 구독이 다시 형성**된다.
 *    `setObserver` 반환 handle 은 실제로 non-null 이었다(관찰자 미부착 상태였으므로).
 *
 * 2) 쓰기 경로 × 관찰 API 발화 매트릭스 — **커버리지가 상보적이다**
 *
 *    | 쓰기 경로                       | registerGlobalWriteObserver | registerApplyObserver | observeSnapshots(write) |
 *    |--------------------------------|-----------------------------|-----------------------|-------------------------|
 *    | 전역 직접 쓰기(버튼 onClick)     | 발화                         | 발화                   | **침묵**                 |
 *    | `withMutableSnapshot { }` 안    | **침묵**                     | 발화                   | 발화                     |
 *    | 같은 값 재대입                   | 침묵                         | 침묵                   | 침묵                     |
 *
 *    - `observeSnapshots` 의 read/write 옵저버는 `onPreCreate` 로 **새 스냅샷이 생성될 때** 주입되므로,
 *      새 스냅샷을 만들지 않는 전역 직접 쓰기에는 관여하지 못한다. "실험 API 라서 더 많이 본다"가 아니다.
 *    - 컴포지션도 자기 스냅샷 안에서 진행되므로 위 표의 2행이 곧 컴포지션 내부 쓰기의 대역이며,
 *      이것이 벽 ③("컴포지션 중 쓰기는 observeSnapshots 만이 경로")의 실측 근거다.
 *    - 3행이 벽 ①의 실측 근거다.
 *
 * ## 본 예제 구현 메모
 * - 관찰자 콜백은 컴포지션 도중 + 전역 스냅샷 락 아래에서 실행되므로, 콜백에서는 사전 할당 링버퍼(EventRing)에
 *   O(1) 적재만 하고 화면 반영(mutableStateListOf 쓰기)은 클릭 핸들러에서만 수행한다.
 *   콜백에서 스냅샷 상태에 직접 쓰면 "컴포지션 중 쓰기"가 되어 무한 리컴포지션으로 번진다.
 * - setObserver 는 컴포지션 하나 전체에 붙어 화면의 모든 스코프 이벤트가 들어오므로,
 *   StateNameRegistry 에 등록된 상태가 관여한 이벤트만 남기는 필터를 둔다(identity 비교, 등록 3개라 사실상 O(1)).
 * - 스코프에 사람이 읽을 수 있는 함수명을 붙이는 것은 이 API 범위 밖이다(슬롯 트리의 sourceInfo 파싱 영역).
 *   여기서는 S1/S2 별칭과 identity 해시로만 구분한다.
 * - ⚠️ 스레드 계약: `registerGlobalWriteObserver`/`registerApplyObserver` 는 **쓰기를 일으킨 스레드에서** 호출된다.
 *   이 예제는 모든 쓰기가 onClick(메인 스레드)에서만 일어나므로 EventRing 을 동기화하지 않았지만,
 *   백그라운드에서 상태를 쓰는 실제 앱에 이 패턴을 옮길 때는 링버퍼를 스레드 안전하게 만들어야 한다.
 *   (CompositionObserver 쪽 콜백은 컴포지션 스레드에서만 오므로 이 문제가 없다.)
 * - SnapshotObserverCard 의 버튼 3개는 위 매트릭스 3행에 1:1로 대응한다
 *   ("전역에 직접 쓰기" / "스냅샷 안에서 쓰기" / "같은 값 쓰기").
 *   특히 "스냅샷 안에서 쓰기"는 컴포지션 내부 쓰기를 안전하게 관측하기 위한 대역이다
 *   — 컴포지션 도중 실제로 상태에 쓰면 무한 리컴포지션이 되므로 그 자체를 시연할 수는 없다.
 */
