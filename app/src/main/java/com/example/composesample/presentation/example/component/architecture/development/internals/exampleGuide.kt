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
