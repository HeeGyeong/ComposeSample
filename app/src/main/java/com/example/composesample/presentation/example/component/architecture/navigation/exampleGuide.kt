package com.example.composesample.presentation.example.component.architecture.navigation

/**
 * Navigation3 Example 참고 자료
 *
 * - Navigation3 공식 문서: https://developer.android.com/guide/navigation/navigation3
 * - GitHub: https://github.com/androidx/androidx/tree/androidx-main/navigation/navigation3
 *
 * 핵심 개념:
 * - Navigation3는 기존 NavController 기반 Navigation2를 대체하는 새 Compose 전용 라이브러리
 * - NavEntry<T>: 화면을 나타내는 타입 안전 엔트리 (sealed class 활용 권장)
 * - NavDisplay: 현재 NavEntry를 렌더링하는 Composable
 * - BackStack: NavEntry 목록을 State로 직접 관리 (remember { mutableStateListOf() })
 *
 * Navigation2 vs Navigation3 비교:
 * - Navigation2: NavController.navigate(route: String), XML/DSL 정의, 내부 백스택 관리
 * - Navigation3: 백스택 = 직접 관리하는 State<List<NavEntry>>, 타입 안전, Composable 친화적
 *
 * 중첩 라우팅 (NestedRoutesNav3):
 * - 중첩된 NavDisplay로 탭/섹션별 독립 백스택 구현
 * - 각 NavWrapperManager가 자체 ViewModel 스코프를 가짐
 *
 * 주의사항:
 * - 현재 실험적(Experimental) API — 프로덕션 적용 전 안정화 여부 확인 필요
 * - Navigation2와 동시 혼용 시 백스택 충돌 가능
 */
