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
 */
