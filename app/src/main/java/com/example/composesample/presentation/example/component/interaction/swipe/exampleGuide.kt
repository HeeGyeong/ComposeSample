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
 * - confirmValueChange 콜백으로 조건부 스와이프(특정 아이템 삭제 불가 등) 구현 가능
 * - positionalThreshold(dp 단위)가 Deprecated된 FractionalThreshold를 대체, reset()/dismiss()로 상태 명시적 제어
 * - LazyColumn에서 사용 시 삭제 애니메이션 완료까지 delay를 주고 key 파라미터로 recomposition 최적화 필요
 */
