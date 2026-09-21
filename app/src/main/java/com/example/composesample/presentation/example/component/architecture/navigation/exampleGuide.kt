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
 *
 * ## Navigation3ExampleUI 추가 참고 자료 (Navigation3Guide.kt에서 이관)
 * - 출처: https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html
 *
 * 핵심 개념:
 * - Navigation 3는 "Building blocks approach"로 작고 분리된 API를 조합해 사용하는 철학
 * - Nav2는 NavController가 백스택을 내부 상태로 관리했지만, Nav3는 개발자가 backStack을
 *   Compose State(List<NavKey>)로 직접 소유해 Single Source of Truth를 확보
 * - NavDisplay는 backStack 변화를 관찰해 entryProvider의 when 분기로 현재 화면만 표시
 * - Multiple back stacks(탭별 독립 백스택), Scenes API(어댑티브 레이아웃), Deep Link 처리 등은
 *   빌딩 블록을 조합해 앱에서 직접 구현
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
 * - 본 예제는 androidx.navigation3 의존성 없이 순수 Compose 상태로 동작 차이만 시뮬레이션한다
 *   (프로젝트 전체에 `import androidx.navigation3` 는 0건). Nav2 대비 ViewModel 스코프 차이를
 *   대조하는 것이 목적이라 라이브러리 API 사용법 자체는 다루지 않는다.
 *   (CLAUDE.md ViewModel 범위 규칙과 연계)
 * - ⚠️ 단계 표기 정정(2026-09-07 실측): Navigation 3 는 더 이상 alpha 가 아니다.
 *   **stable 1.1.x 라인(최신 1.1.7)이 1.2.0 알파/베타 라인과 병행**하며,
 *   navigation3-ui-android:1.1.7 의 aar-metadata 는 minCompileSdk=36 이라 이 프로젝트에서 채택 가능하다.
 *   다만 1.2.0-beta01 은 minCompileSdk=37 을 요구해 현재(compileSdk 36)로는 쓸 수 없다.
 *   → 시뮬레이션을 유지하는 이유는 라이브러리 단계가 아니라 위에 적은 예제의 목적이다.
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
 * - 본 예제는 라이브러리/Hilt 의존성 없이 "직렬화 → 프로세스 종료 → 역직렬화" 흐름만
 *   순수 Compose 상태로 시뮬레이션한다. 크래시의 원인과 회피 패턴을 보이는 것이 목적이라
 *   라이브러리 API 자체는 다루지 않는다.
 * - ⚠️ 단계 표기 정정(2026-09-07 실측): Navigation 3 는 alpha 가 아니라 stable 1.1.7 이 있다
 *   (자세한 버전 사정은 위 Nav3 ViewModel Scope 항목의 주의사항 참조).
  *
 * ## NavigationEvent Dispatcher (androidx.navigationevent — back/forward 양방향 내비게이션 이벤트)
 * - 공식 문서: https://developer.android.com/reference/androidx/navigationevent/package-summary
 * - API 문서: https://developer.android.com/reference/androidx/navigationevent/NavigationEventDispatcher
 * 핵심 개념:
 * - 구성: 입력원(NavigationEventInput) → 디스패처(NavigationEventDispatcher) → 핸들러(NavigationEventHandler) → 앱 콜백.
 *   플랫폼 제스처(OnBackInvokedInput)와 직접 주입(DirectNavigationEventInput)이 같은 디스패처를 공유한다
 * - PredictiveBackHandler 와의 차이: back 한 방향/진행률만이 아니라 **back+forward 양방향**, **currentInfo·backInfo·forwardInfo**,
 *   **transitionState** 를 함께 다룬다
 * - Compose 통합: rememberNavigationEventState(currentInfo, backInfo, forwardInfo) 로 상태를 만들고
 *   NavigationEventHandler(state, isBackEnabled, onBackCompleted, onBackCancelled, isForwardEnabled, onForwardCompleted, onForwardCancelled)
 *   로 붙인다. NavigationBackHandler / NavigationForwardHandler 는 한 방향 축약형
 * - **진행률은 콜백으로 오지 않는다** — 컴포저블 핸들러의 콜백은 완료/취소뿐이고, 중간 진행은
 *   state.transitionState 가 InProgress 일 때의 latestEvent.progress 로 읽는다
 * - CompositionLocal 필수: 핸들러는 LocalNavigationEventDispatcherOwner 에서 디스패처를 찾고 없으면
 *   IllegalStateException("No NavigationEventDispatcher was provided via LocalNavigationEventDispatcherOwner").
 *   rememberNavigationEventDispatcherOwner 는 parent 기본값이 그 Local 이라, 루트를 만들 때는 parent = null 을 명시해야 한다
 *   (아니면 "...If you intended to create a root dispatcher, explicitly pass null as the parent.")
 * - NavigationEventInfo 는 인터페이스가 아니라 **추상 클래스** → `data class X(...) : NavigationEventInfo()` 로 상속
 * - 순서 규칙: NavigationEventHistory.mergedHistory = backInfo + currentInfo + forwardInfo 를 그대로 이어 붙인다
 *   → backInfo 는 오래된 것부터 담아야 이력이 시간순으로 맞는다. 실측: back=[홈,목록]·current=상세·forward=[설정] 이면
 *   mergedHistory=[홈,목록,상세,설정], currentIndex=2
 * - 상수(바이트코드 확인): TRANSITIONING_UNKNOWN=0 / TRANSITIONING_FORWARD=1 / **TRANSITIONING_BACK=-1**,
 *   EDGE_LEFT=0 / EDGE_RIGHT=1 / EDGE_NONE=2(NavigationEvent 의 기본값), PRIORITY_OVERLAY=0 / PRIORITY_DEFAULT=1
 * - 실기기 실측(SM-A725F/Android 13): ① Idle → InProgress(direction=-1) → 완료 후 Idle 복귀
 *   ② isBackEnabled=false 인 핸들러에 시작+완료를 주입하면 콜백 0회(예외 없이 사라진다)
 *   ③ **순서는 강제되지 않는다** — backStarted 없이 backCompleted 만 불러도 콜백이 오고, backProgressed 만 부르면 무시된다
 * - 의존성 주의: navigationevent-compose 1.1.2 의 pom 은 compose ui/runtime 1.11.2 를 요구한다.
 *   BOM 2026.05.00(1.11.1) 시절에는 높은 쪽이 이겨 ui/runtime 만 1.11.2 로 올라가는 패치 스큐가 있었고
 *   (foundation/animation/material 은 1.11.1 유지), BOM 2026.06.01(1.11.4) 상향 후에는 BOM 이 더 높아 스큐가 사라졌다.
 *   실제로 쓰는 API(androidx.compose.runtime.HostDefaultKey)는 1.11.1 에도 있으므로 호환 요구일 뿐이다
*/

/**
 * Nav3 SceneStrategy 레이어드 바텀시트 Example 참고 자료
 *
 * - Navigation 3 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/navigation3
 * - SceneStrategy API: https://developer.android.com/reference/kotlin/androidx/navigation3/scene/SceneStrategy
 * - OverlayScene API: https://developer.android.com/reference/kotlin/androidx/navigation3/scene/OverlayScene
 *   (KDoc 의 `@sample androidx.navigation3.ui.samples.AnimatedBottomSheetSample` 이 이 예제와 같은 주제다)
 *
 * 의존성:
 * - 이 예제가 프로젝트 최초의 실제 navigation3 사용이다(navigation3-ui / navigation3-runtime 1.1.7).
 *   위의 Navigation3 / Nav3ViewModelScope / Nav3SavedStateHandle 은 여전히 의존성 없는 시뮬레이션이다.
 * - 1.1.7 aar-metadata = minCompileSdk 36 / minAGP 8.9.1. ui pom 의 요구(activity-compose 1.12.0 · lifecycle 2.10.0 ·
 *   compose 1.11.2 · navigationevent 1.1.2 · savedstate 1.4.0)가 전부 현재 해석 버전 이하라 전이 끌어올림이 없다.
 *   1.2.0 라인은 minCompileSdk=37 이라 채택하지 않는다.
 *
 * 핵심 개념 (1.1.7 소스 기준):
 * - SceneStrategy<T> 는 `SceneStrategyScope<T>.calculateScene(entries): Scene<T>?` 하나짜리 fun interface.
 *   null 이면 "이 전략은 해당 없음" — NavDisplay(sceneStrategies = listOf(...)) 가 앞에서부터 시도하고
 *   전부 null 이면 SinglePaneSceneStrategy 로 떨어진다. `then` 연산자와 단일 `sceneStrategy` 파라미터 오버로드는 deprecated.
 * - 판단 근거는 엔트리 메타데이터: `NavMetadataKey<V>` 를 정의하고 `metadata { put(Key, value) }` 로 엔트리에 싣고,
 *   전략에서 `entry.metadata[Key]` 로 읽는다. 라이브러리의 DialogSceneStrategy 가 정확히 이 모양이다.
 * - OverlayScene 의 overlaidEntries 는 "아래에서 따로 그려질 엔트리". SceneState 가 결과가 OverlayScene 인 동안
 *   overlaidEntries 로 전략을 재귀 호출해 overlayScenes 를 모으고, 오버레이가 아닌 마지막 씬이 currentScene(기본 씬)이 된다.
 *   overlaidEntries 가 비면 require 로 크래시("Overlaid entries from ... must not be empty").
 * - onRemove(): pop 된 뒤 컴포지션을 떠나기 전에 호출되는 suspend 함수. 퇴장 애니메이션은 여기서 — 반환될 때까지 화면에 남는다.
 *   실측: 220ms 트윈의 onRemove 가 244~277ms 걸렸고, "시트 전부 닫기"로 2장을 동시에 빼면 두 onRemove 가 병렬로 돌았다(265·269ms).
 * - NavDisplay 는 오버레이를 key 로 추적해 같은 key 의 재계산 인스턴스는 버리고 처음 인스턴스를 계속 그린다
 *   (그래서 Animatable 을 씬에 둬도 유지되지만, 생성자로 구운 값은 갱신되지 않는다).
 * - Lifecycle 상한: 오버레이가 하나라도 있으면 기본 씬은 STARTED(전환 중에도 STARTED), 오버레이 중 맨 위만 RESUMED.
 *   가려진 화면은 dispose 되지 않는다 — 컴포지션은 살아 있고 RESUMED 만 빠진다.
 *
 * ⚠️ back 처리 함정 (이 예제의 핵심 발견):
 * - NavDisplay 의 NavigationBackHandler 는 `isBackEnabled = currentScene.previousEntries.isNotEmpty()` 이고
 *   완료 시 `repeat(entries.size - currentScene.previousEntries.size) { onBack() }` 을 한다. currentScene 은 기본 씬이다.
 *   → [Home, 시트] 이면 핸들러가 꺼져 back 이 액티비티로 샌다. [Home, 상세, 시트, 시트] 이면 back 한 번에 3개가 pop 된다.
 * - 실기기 대조 실험(SM-A725F/Android 13, 시트 핸들러를 임시로 끈 빌드): ① [Home, 필터#1] + back → BlogExampleActivity 가
 *   통째로 닫혀 MainActivity 로 나갔다 ② [Home, 상세#1, 정렬#2, 확인#3] + back → [Home] (3개 동시 pop).
 *   핸들러를 켠 정식 빌드에서는 두 경우 모두 맨 위 시트 1장만 닫혔다.
 * - DialogScene 은 Dialog 창이 back 을 받아 onDismissRequest 로 처리하므로 이 문제가 없다.
 *   같은 창 안의 레이어로 그리는 오버레이는 씬 content 안에서 NavigationBackHandler 를 직접 등록해야 하고,
 *   나중에 등록된 핸들러가 우선하므로 맨 위 오버레이만 isBackEnabled = true 로 두면 한 장씩 닫힌다.
 *
 * ⚠️ z-order 함정 (실기기 SM-A725F 에서 발견):
 * - NavDisplay 는 `currentOverlayScenes` 에 **처음 등장한 key 를 append** 하고(LaunchedEffect) `fastForEachReversed` 로 그린다.
 *   overlayScenes 는 [위, …, 아래] 순이라 한 번에 여러 장이 생기면 맞게 그려지지만, 한 장씩 push 하면 목록이
 *   [필터(먼저), 정렬(나중)] 이 되어 정렬을 먼저·필터를 나중에 그린다 → **새로 쌓은 시트가 아래에 깔린다.**
 * - Dialog 는 창 생성 순서로 z-order 가 정해져 영향이 없다. 같은 창 레이어는 `Modifier.zIndex(백스택 인덱스)` 로 바로잡는다.
 * - NavDisplay 에는 자체 Box 가 없다 — AnimatedContent(기본 씬)와 오버레이 content 를 **호출한 쪽 레이아웃에 형제로** 내보낸다.
 *   그래서 zIndex 가 형제끼리 먹는다. 같은 이유로 겹치지 않는 부모(Column 등)에 두면 오버레이가 기본 씬과 겹치지 않을 것이다
 *   (소스 구조에서의 추론, 미실측) → Box 안에 둔다.
 *
 * 이 예제의 선택:
 * - 백스택은 `mutableStateListOf<SceneDemoKey>()` — NavDisplay 는 T : Any 라 NavKey 가 필수가 아니다.
 *   rememberNavBackStack 은 @Serializable NavKey + SavedStateConfiguration(polymorphic 모듈)을 요구해
 *   kotlinx-serialization 컴파일러 플러그인이 추가로 필요하므로 쓰지 않았다(대가: 프로세스 종료 후 백스택 복원 없음).
 * - contentKey 를 라벨로 지정(`entry<K>(clazzContentKey = { it.label })`)해 씬 key·로그를 사람이 읽을 수 있게 했다.
 *   기본 contentKey 는 Pair("$key", "${key::class}") 다.
 */
