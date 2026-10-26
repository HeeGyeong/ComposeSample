package com.example.composesample.presentation.example.component.interaction.swipe

/**
 * SwipeToDismissM3ExampleUI 참고 자료
 *
 * - 출처: https://proandroiddev.com/swipe-to-dismiss-with-compose-material-3-38445e0143f7
 *
 * 핵심 개념:
 * - Material 3의 SwipeToDismissBox는 Material 2의 SwipeToDismiss를 대체하는 컴포넌트
 *   (DismissState → SwipeToDismissBoxState, DismissValue → SwipeToDismissBoxValue: Settled/StartToEnd/EndToStart)
 * - enableDismissFromStartToEnd / enableDismissFromEndToStart로 스와이프 방향을 개별 제어
 * - confirmValueChange 콜백으로 조건부 스와이프(특정 아이템 삭제 불가 등) 구현 가능 — 단 material3 1.4.0 에서 대체 없이 deprecated
 *   (막을 방향은 enableDismissFrom… 으로 앵커에서 빼고, 완료 처리는 SwipeToDismissBox(onDismiss = …)). 이 예제는 거부/허용 로직을
 *   보이려고 @Suppress("DEPRECATION") 로 유지한다
 * - M2 의 FractionalThreshold(Deprecated) 자리는 rememberSwipeToDismissBoxState(positionalThreshold = { totalDistance -> … }) —
 *   전체 거리(px)를 받아 임계 거리(px)를 돌려주는 함수다(기본 SwipeToDismissBoxDefaults.positionalThreshold, 이 예제는 기본값)
 * - 상태는 suspend 함수 reset()(이 예제의 되돌리기) · dismiss(direction) · snapTo(value) 로 직접 바꿀 수 있다
 * - LazyColumn에서 사용 시 삭제 애니메이션 완료까지 delay를 주고 key 파라미터로 recomposition 최적화 필요
 */
