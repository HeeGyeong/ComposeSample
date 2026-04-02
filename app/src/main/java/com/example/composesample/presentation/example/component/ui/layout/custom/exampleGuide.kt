package com.example.composesample.presentation.example.component.ui.layout.custom

/**
 * Custom Layout (MeasurePolicy) 예제 참고 자료
 *
 * - 공식 문서: https://developer.android.com/develop/ui/compose/layouts/custom
 * - MeasurePolicy API: https://developer.android.com/reference/kotlin/androidx/compose/ui/layout/MeasurePolicy
 *
 * 핵심 개념:
 * - Layout composable: 자식 측정(measure)과 배치(layout) 두 단계로 직접 제어
 * - MeasurePolicy: measure { measurables, constraints → layout(w, h) { placeRelative } }
 * - measurable.measure(constraints): 자식 Composable을 지정 제약으로 측정 → Placeable 반환
 * - placeable.placeRelative(x, y): RTL 환경을 고려한 좌표 기반 배치
 *
 * SubcomposeLayout과의 차이:
 * - Layout: 모든 자식을 한 번에 측정. 단순 커스텀 레이아웃에 적합
 * - SubcomposeLayout: 일부 자식 측정 결과를 기반으로 나머지 자식 구성. 복잡한 동적 레이아웃에 사용
 *   (예: ResponsiveTabRow, LazyColumn)
 *
 * 구현 패턴:
 * ```kotlin
 * Layout(content = content) { measurables, constraints ->
 *     val placeables = measurables.map { it.measure(constraints) }
 *     val width = placeables.maxOf { it.width }
 *     val height = placeables.sumOf { it.height }
 *     layout(width, height) {
 *         var y = 0
 *         placeables.forEach { placeable ->
 *             placeable.placeRelative(0, y)
 *             y += placeable.height
 *         }
 *     }
 * }
 * ```
 */
