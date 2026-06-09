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

/**
 * Nav3 SavedStateHandle 크래시 & 복원 Example 참고 자료
 *
 * - Why SavedStateHandle Crashes in Jetpack Navigation 3 (Ahmed Tikiwa, Android Weekly #730)
 * - Navigation3 공식 문서: https://developer.android.com/guide/navigation/navigation3
 * - SavedStateHandle: https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate
 *
 * 핵심 개념 정리:
 *
 * 1) 왜 크래시가 나는가
 *    - Nav3 백스택은 State<List<NavKey>> 이고, process death 복원을 위해 각 NavKey 는
 *      SavedState(Bundle)로 직렬화/역직렬화된다.
 *    - NavKey 에 람다·런타임 객체·비직렬화 필드를 가진 "복합 객체"를 담으면,
 *      복원(역직렬화) 단계에서 해당 필드를 되돌리지 못해 크래시가 발생한다.
 *    - 동일한 원인이 SavedStateHandle 로 복합 객체를 전달할 때도 적용된다.
 *
 * 2) 해결 패턴 — 식별자(id)만 전달
 *    - NavKey/SavedStateHandle 에는 String/Int 등 직렬화 가능한 식별자만 담는다.
 *    - 실제 객체는 ViewModel 이 SavedStateHandle 의 id 를 읽어 Repository 에서 다시 조회한다.
 *    - 원문은 Hilt Assisted Injection 으로 id 를 VM 에 주입하지만,
 *      본 프로젝트에서는 Koin 으로 Repository 를 주입(single { UserRepository() })하고
 *      viewModel { DetailViewModel(get(), get()) } 형태로 각색한다.
 *
 * 3) 대안 — 키 자체를 직렬화 가능하게
 *    - 꼭 객체를 담아야 한다면 @Parcelize / kotlinx @Serializable 로 NavKey 를 만들고
 *      모든 필드를 직렬화 가능한 타입으로 제한한다(람다·Context·View 참조 금지).
 *
 * 주의사항:
 * - Navigation 3 는 alpha 단계 → 본 예제는 라이브러리/Hilt 의존성 없이
 *   "직렬화 → 프로세스 종료 → 역직렬화" 흐름만 순수 Compose 상태로 시뮬레이션한다.
 */
