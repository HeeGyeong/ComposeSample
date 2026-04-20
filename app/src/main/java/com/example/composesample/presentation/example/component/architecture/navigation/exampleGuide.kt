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

/**
 * Nav3 ViewModel Scope Example 참고 자료
 *
 * - Scope of ViewModels in Compose Navigation 3 (Domen Lanišnik):
 *   https://medium.com/@domen.lanisnik (Android Weekly #723)
 *
 * 핵심 개념 정리:
 *
 * 1) Navigation 2 의 자동 스코프
 *    - NavBackStackEntry 가 ViewModelStoreOwner, LifecycleOwner, SavedStateRegistryOwner 역할을 동시에 수행
 *    - hiltViewModel() / viewModel() 이 destination 단위로 캐시됨
 *    - destination 이 pop 되면 ViewModelStore.clear() 가 자동 호출되어 VM 이 정리됨
 *
 * 2) Navigation 3 의 기본 동작
 *    - 백스택이 State<List<NavKey>> 형태로 관리되는 순수 상태
 *    - NavKey 자체는 ViewModelStoreOwner 가 아님 → 자동 스코프가 없음
 *    - Composable 내부에서 viewModel() 을 호출하면 상위 ViewModelStoreOwner(Activity 등)에 바인딩되어
 *      pop 이후에도 ViewModel 이 남고, 다음 진입 시 상태가 재사용되어 혼동 유발
 *
 * 3) 스코프 복원 패턴 (lifecycle 확장)
 *    - NavKey → ViewModelStoreOwner 매핑을 Composable 외부에서 관리
 *    - 백스택이 변할 때 diff 로 살아있는 키만 유지, 사라진 키의 Store.clear() 호출
 *    - 실제 구현에서는 rememberSaveableStateHolder 와 DisposableEffect 를 함께 사용
 *
 * 주의사항:
 * - Navigation 3 는 현재 alpha 단계이며, 본 예제는 라이브러리 의존성 없이
 *   순수 Compose 상태로 동작 차이만 시뮬레이션한다. (CLAUDE.md ViewModel 범위 규칙과 연계)
 */
