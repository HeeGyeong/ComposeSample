package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.tree

/**
 * CompositionLocal Tree Visualization 참고 자료
 *
 * - CompositionLocal 개요: https://developer.android.com/develop/ui/compose/compositionlocal
 *
 * 컴포지션 트리와 CompositionLocal:
 * - 컴포지션 트리는 Composable 호출 계층을 나타내는 노드 구조
 * - 각 노드는 부모의 CompositionLocal 값을 상속하거나 재정의(Provider)할 수 있음
 * - LocalCompositionLocalContext로 현재 노드의 전체 CompositionLocal 상태를 캡처 가능
 *
 * 룩업(Lookup) 동작:
 * - current를 읽을 때 Compose는 가장 가까운 상위 Provider를 탐색
 * - Provider가 없으면 defaultFactory에서 기본값 반환
 * - 탐색 방향: 항상 부모 → 루트 방향 (형제 노드 접근 불가)
 *
 * 데이터 흐름 시각화:
 * - CompositionLocalTreeExampleUI: 트리 구조에서 Local 값 전파, 섀도잉, 룩업을 시각적으로 표현
 * - 특정 서브트리에만 다른 테마/설정 적용하는 패턴 시연
 *
 * 실무 활용:
 * - 다크/라이트 테마 전환: MaterialTheme → LocalColorScheme
 * - 화면 방향별 레이아웃: LocalConfiguration.current.orientation
 * - 테스트 환경 주입: CompositionLocalProvider로 목 데이터 제공
 *
 * ## CompositionLocalTreeExampleUI 추가 참고 자료 (CompositionLocalTreeGuide.kt에서 이관)
 * - 출처: https://dev.to/bansalayush/visualizing-compositionlocal-in-the-composition-tree-2jkg
 *
 * 핵심 개념:
 * - CompositionLocalProvider는 데이터를 자식에게 복사하지 않고, 해당 트리 노드에 "locals map"을 부착
 * - .current 호출 시 Compose는 현재 노드에서 부모 방향으로 룩업하며 값을 찾고, 없으면 기본값(defaultFactory) 사용
 * - 중첩된 Provider가 같은 키를 다시 제공하면 자식 쪽 값이 우선 적용되는 섀도잉(Shadowing)이 발생
 * - 내부적으로 Slot Table(평탄화된 배열)에 노드·locals map·parent 인덱스가 저장되어 트리 탐색에 사용됨
 */
