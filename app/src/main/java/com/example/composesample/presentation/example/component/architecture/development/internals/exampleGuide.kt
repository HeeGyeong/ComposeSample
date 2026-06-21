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
