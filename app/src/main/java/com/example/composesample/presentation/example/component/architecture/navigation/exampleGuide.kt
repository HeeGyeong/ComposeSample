package com.example.composesample.presentation.example.component.architecture.navigation

/**
 * Navigation3 Example 참고 자료
 *
 * - Navigation3 공식 문서: https://developer.android.com/guide/navigation/navigation-3
 * - GitHub: https://github.com/androidx/androidx/tree/androidx-main/navigation3
 *
 * 핵심 개념:
 * - Navigation3는 기존 NavController 기반 Navigation2를 대체하는 새 Compose 전용 라이브러리
 * - NavEntry<T>: 화면을 나타내는 타입 안전 엔트리 (sealed class 활용 권장)
 * - NavDisplay: 현재 NavEntry를 렌더링하는 Composable
 * - BackStack: NavKey 목록을 State로 직접 관리 (rememberNavBackStack — 키를 직렬화해 재생성 · 프로세스 종료 뒤 복원)
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
 *   1.2.0 라인은 minCompileSdk=37 이라 당시(compileSdk 36)에는 쓸 수 없었다 — 2026-09-29 compileSdk 37 상향으로
 *   채택 조건을 갖췄고, **2026-10-01 1.2.0 stable 로 올렸다**(아래 Nav3SceneStrategy 의존성 절 참고).
 *   → 시뮬레이션을 유지하는 이유는 라이브러리 단계가 아니라 위에 적은 예제의 목적이다.
 */

/**
 * Nav3 SavedStateHandle 크래시 & 복원 Example 참고 자료
 *
 * - Why SavedStateHandle Crashes in Jetpack Navigation 3 (Ahmed Tikiwa, Android Weekly #730)
 * - Navigation3 공식 문서: https://developer.android.com/guide/navigation/navigation-3
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
 *   실제로 쓰는 API(androidx.compose.runtime.HostDefaultKey)는 1.11.1 에도 있으므로 호환 요구일 뿐이다(1.2.0 pom 도 1.11.2)
 * - **1.2.0 신규(2026-10-07, 6번 카드 — 1.2.0 sources jar 기준)**:
 *   · `NavigationEventInput.hasEnabledHandlers`·`hasEnabledBackHandlers`·`hasEnabledForwardHandlers`(public 읽기, private set — 스냅샷 상태 아님)
 *     + protected open `onHasEnabledHandlersChanged`·`onHasEnabledBackHandlersChanged`·`onHasEnabledForwardHandlersChanged`.
 *     KDoc 권장 패턴: 입력원은 이 값이 true 일 때만 제스처 감지를 켠다. 화면 갱신은 콜백에서 스냅샷 상태로 옮겨야 한다
 *   · `NavigationEventDispatcherOwner(dispatcher)` 팩토리(public) — 손으로 만든 디스패처를 Local 로 넘길 때 익명 Owner 대신
 *   · `NavigationEventDispatcher(onBackCompletedFallback, onForwardCompletedFallback)` — 받을 핸들러가 없으면 fallback 이 받는다
 *   · `NavigationEventInfo.title`·`url` — `@ExperimentalNavigationEventApi`, 웹 호스트(브라우저 탭 제목·주소창)용으로 Android 소비자 없음.
 *     ⚠️ 같은 이름의 `val title` 을 가진 하위 클래스는 1.2.0 에서 컴파일이 깨진다("hides member of supertype") → override + opt-in
 *   · 테스트용 `TestNavigationEventDispatcherOwner.navigationEventInput`·`onForwardCompletedFallback`(navigationevent-testing — 프로젝트 미사용)
 *   · 동작 변화: 디스패처에서 뗀 입력원으로 보내면 1.1.x 는 IllegalStateException, 1.2.0 은 조용히 반환(KDoc 의 @throws 는 옛 문구) ·
 *     fallback 전파 수정 · `LocalNavigationEventDispatcherOwner` 가 Local·View 트리에 Owner 가 없으면 LocalContext 의 ContextWrapper 사슬에서
 *     한 번 더 찾는다(예외 문구 2종은 그대로)
 * - 6번 카드 실측(SM-A725F/Android 13, debug·release 동일): 입력원 부착 직후 콜백 5회, 이후 핸들러 토글마다 2회(any + 방향) ·
 *   back 켬/forward 끔에서 back 보내기 → 핸들러 1, forward 보내기 → forward fallback 1 · back 끄면 any·back·forward 전부 false, back 보내기 → back fallback 1 ·
 *   forward 켬 → any·forward true, 보내기 → 핸들러 1 · 뗀 입력원으로 back → 예외 없이 아무도 받지 않음
 * - 1.1.2 → 1.2.0 상향(DEP-CURRENCY-06) 전후로 이 예제의 주입 시나리오 값 57줄이 debug·release 모두 같았다
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
 * - 이 예제가 프로젝트 최초의 실제 navigation3 사용이다(navigation3-ui / navigation3-runtime, 도입 1.1.7 → 2026-10-01 1.2.0).
 *   위의 Navigation3 / Nav3ViewModelScope / Nav3SavedStateHandle 은 의존성 없는 시뮬레이션이었다 — 2026-11-20 #117 에서
 *   세 예제와 NestedRoutesNav3 에 실물 카드를 더했다(시뮬레이션 카드는 Nav2 대비 개념 대조용으로 남김, 아래 #117 절).
 * - 1.1.7 aar-metadata = minCompileSdk 36 / minAGP 8.9.1. ui pom 의 요구(activity-compose 1.12.0 · lifecycle 2.10.0 ·
 *   compose 1.11.2 · navigationevent 1.1.2 · savedstate 1.4.0)가 전부 현재 해석 버전 이하라 전이 끌어올림이 없다.
 *   1.2.0 라인(minCompileSdk=37)은 2026-09-29 compileSdk 37 상향으로 채택 가능해졌다.
 * - **1.2.0 상향(2026-10-01)**: aar-metadata minCompileSdk 37 / minAGP 9.1.0(현재 37 / 9.4.1). 해석 버전 변화는 navigation3 4개 좌표뿐.
 *   1.2.0 의 신규(Result API·Deep Link API·NavigationBackHandler 연결 헬퍼)와 수정(SceneState 의 이전 씬 계산,
 *   중첩 오버레이 재애니메이션, pop 된 항목이 즉시 사라짐)이 이 예제의 관찰과 맞닿아, back 핸들러 없는 같은 구조의 오버레이로
 *   상향 전후를 실기기에서 대조했다 — **결과 동일**: Home 위 시트의 back 은 액티비티로 새고, 상세 위 시트 2장은 back 한 번에
 *   3개가 pop 되며, 기본 씬 STARTED · 맨 위 오버레이 RESUMED · 아래 오버레이 STARTED, 시트 2장 동시 제거 시 onRemove 는 위→아래 순.
 *   그래서 아래 함정 설명은 1.2.0 에서도 그대로다.
 *
 * 핵심 개념 (1.1.7 소스 기준, 1.2.0 에서 실기기 대조로 같은 동작 확인):
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
 *
 * ## Nav3DeepLinkResultExampleUI (Navigation 3 1.2.0 — Deep Link API · Result API 실물)
 * - 출처:
 *   - Android 블로그(2026-10-28, Android Dev Summit): https://developer.android.com/blog/posts/build-it-your-way-a-next-era-of-android-development
 *     ('Navigation 3 1.2 is stable with deep links support')
 *   - 릴리스 노트 1.2.0 의 Result API · Deep Link API 절: https://developer.android.com/jetpack/androidx/releases/navigation3
 *   - deeplink 패키지: https://developer.android.com/reference/kotlin/androidx/navigation3/runtime/deeplink/package-summary
 *   - result 패키지: https://developer.android.com/reference/kotlin/androidx/navigation3/runtime/result/package-summary
 * - 의존성 추가 없음 — navigation3-runtime/ui 1.2.0(이미 사용) · kotlinx-serialization 플러그인(#123 에서 적용). 키는 @Serializable +
 *   NavKey 라 rememberNavBackStack(Android 는 리플렉션 직렬화)으로 재생성·프로세스 종료 뒤 복원된다 — debug(HotSwan)에서도 동작.
 *
 * 핵심 개념 (navigation3-runtime-android 1.2.0 sources):
 * - DeepLinkRequest(uri 문자열 · DeepLinkUri · Intent): Intent 판은 data → uri, type·action·extras → RequestExtras.
 *   uri 와 extras 가 둘 다 비면 생성자 require 로 예외. DeepLinkUri 는 Android 에서 android.net.Uri 의 typealias 라 JVM 단위 테스트로는
 *   매칭을 돌릴 수 없다(이 예제의 단위 테스트는 패턴 표·백스택 규칙만).
 * - UriDeepLinkMatcher(DeepLinkUri(패턴), Key.serializer()): 스킴 없는 패턴은 'http[s]' 정규식, authority 는 대소문자만 무시하고 정확히 같아야
 *   한다(커스텀 스킴 composesample://order/{no} 의 order 는 authority). 경로·쿼리·프래그먼트에서 뽑은 인자를 DeepLinkDecoder 로 키에 넣는다.
 * - 고르기: 여러 매처 중 하나를 고르는 헬퍼는 없다. MatchResult 가 Comparable 이라 앱이 맞은 것 중 큰 것을 고른다 —
 *   UriMatchResult 는 정확한 경로 > 경로 인자 수 > 인자 유무 > 인자 총수. withBackStack 결과(BackStackMatchResult)는 감싼 결과로 비교.
 * - withBackStack { result -> List<K> }: 매칭될 때 합성 백스택을 만든다(lazy). 조건부도 된다(이 예제: ref=push 면 Category 생략).
 * - StaticKeyDeepLinkMatcher(key, filters): 필터(actionFilter · mimeTypeFilter)만 보고 고정 키 — URI 없는 공유 인텐트용.
 * - Result API: rememberResultEventBus() 를 끌어올려 rememberResultEventBusNavEntryDecorator(bus) 로 엔트리마다 LocalResultEventBus 를
 *   준다. 보내기 sendResult<T>(값), 받기 ResultEffect<T> { }(이벤트) · conflateAsState<T>(기본값)(상태). 키는 T::class.toString() 이고
 *   키마다 Channel(BUFFERED, SUSPEND) 하나 + receiveAsFlow — 방송이 아니라 분배다. sendResult 는 trySend.
 *
 * ### 실측 (Pixel 4 API 30 에뮬레이터, debug·release 같음 — 판정 표 19줄 diff 0)
 * - products/new: Product(id="new")와 NewArrivals 가 둘 다 맞고 정확한 경로인 NewArrivals 가 선택된다.
 * - composesample://order/4821 → Order(4821) · composesample://app/order/4821 → 맞는 매처 없음(authority app) — 수동 파싱 예제
 *   (DynamicAppLinks '실제 수신' 탭)의 'order 가 host' 함정이 패턴에서는 처음부터 authority 로 표현된다.
 * - orders/abc → Int 변환 실패로 null · search?q=&page= → Search(query="", page=1)(String 은 빈 문자열, Int 는 기본값) ·
 *   q=%ED%95%9C%EA%B8%80 → "한글" · http://SHOP.example.com/promo/... → Promo(http 허용·authority 대소문자 무시·와일드카드) · ftp → 없음.
 * - 공유 Intent(ACTION_SEND, text/plain, URI 없음) → ShareInbox · 보기 Intent(ACTION_VIEW, products/7) → Product(7).
 * - Boolean 'True' 거부·'true' 통과(toBooleanStrict) · enum 'price' 거부·'PRICE' 통과 · 기본값 없는 Int? 는 값이 없으면 누락으로 실패.
 * - ⚠ Map 필드가 있는 키(Bundle)는 null 이 아니라 match() 가 IllegalArgumentException("Cannot decode field [tags] ...")을 던졌다 —
 *   UriDeepLinkMatcher.matchArguments 는 DeepLinkDecoderException 만 잡는다. 매처 루프는 try 로 감싼다.
 * - 시스템 뒤로가기는 연 백스택을 하나씩 줄이고(NavDisplay onBack), Home 하나만 남으면 예제를 닫고 목록으로 돌아간다
 *   (BlogExampleScreen 의 호스트 BackHandler — 2026-11-18 UX-SYSBACK-01 전에는 BlogExampleActivity 가 끝났다).
 * - 결과: Checkout 이 안 보일 때 보낸 10개는 버퍼에 쌓였다가 다시 그려질 때 먼저 붙은 ResultEffect 가 전부 가져갔다(같은 키의
 *   conflateAsState 0). Checkout 이 보일 때 보내 두 수집기가 모두 기다리던 중이면 나뉘었다 — ResultEffect 9 · conflateAsState #2
 *   (첫 trySend 가 첫 대기자를, 둘째가 둘째 대기자를 깨우고 나머지는 버퍼에 남아 먼저 다시 돈 ResultEffect 가 가져감). conflateAsState 는
 *   같은 값(#2)이 다시 오면 가져가고도 State 가 바뀌지 않아 보이지 않았다.
 * - 버퍼: 안 보일 때 70개 → 64개 도착, 65~70 은 trySend 실패로 조용히 사라졌다.
 * - 재생성(다크 모드 전환 후 원래 값 복원)·프로세스 종료(am kill → 최근 앱, 새 pid): 백스택은 Home › Checkout › AddressPicker 그대로,
 *   버스는 새 인스턴스(identityHashCode 이력) — AddressPicker 에서 보내 둔 3개가 돌아간 뒤 0개 도착. KDoc 의
 *   'not saved across configuration changes or process death' 그대로.
 *
 * ## #117 실물 카드 — Navigation3 · Nav3ViewModelScope · Nav3SavedStateHandle · NestedRoutesNav3
 * - 범위: 시뮬레이션 4종(약 2,735줄)을 지우지 않고 화면마다 '✅ 실물' 카드 하나를 더했다(사용자 승인 2026-11-20 — "제약은 다 승인,
 *   크래시 · 사이드 이펙트 없이"). 시뮬레이션 코드는 그대로, 사실과 달라진 안내 문구 3곳만 고쳤다
 *   (Nav3 의 엔트리 ViewModel 은 rememberSaveableStateHolder + DisposableEffect 가 아니라 rememberViewModelStoreNavEntryDecorator,
 *   rememberNavBackStack<RouteType>() 이라는 제네릭 함수는 없다).
 * - 새 의존성: androidx.lifecycle:lifecycle-viewmodel-navigation3 2.11.1 — aar minCompileSdk 37 · minAGP 9.1.0(충족),
 *   module 의존(activity-compose 1.12.0 · compose runtime 1.10.4 · navigation3-runtime 1.0.1 · savedstate 1.3.2)이 전부 프로젝트보다
 *   낮아 해석 diff 는 이 아티팩트 2좌표 추가뿐(app 5구성 + data · Core · Coordinator)
 * - 공식 문서: https://developer.android.com/guide/navigation/navigation-3/basics
 *   · 상태 저장: https://developer.android.com/guide/navigation/navigation-3/save-state
 *   · navigation3 runtime(rememberDecoratedNavEntries · rememberSaveableStateHolderNavEntryDecorator):
 *     https://developer.android.com/reference/kotlin/androidx/navigation3/runtime/package-summary
 *   · lifecycle-viewmodel-navigation3: https://developer.android.com/reference/kotlin/androidx/lifecycle/viewmodel/navigation3/package-summary
 *   · rememberViewModelStoreOwner · rememberViewModelStoreProvider: https://developer.android.com/reference/kotlin/androidx/lifecycle/viewmodel/compose/package-summary
 *   · lifecycle 2.11 릴리스 노트(범위 ViewModel · Navigation3 연동): https://developer.android.com/jetpack/androidx/releases/lifecycle
 *   · 여러 백스택 레시피: https://github.com/android/nav3-recipes
 * - 소스(androidx-main): https://github.com/androidx/androidx/blob/androidx-main/lifecycle/lifecycle-viewmodel-navigation3/src/commonMain/kotlin/androidx/lifecycle/viewmodel/navigation3/ViewModelStoreNavEntryDecorator.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/navigation3/navigation3-runtime/src/commonMain/kotlin/androidx/navigation3/runtime/DecoratedNavEntries.kt
 *   — 아래 규칙은 프로젝트 해석 버전 navigation3 1.2.2 · lifecycle-viewmodel-navigation3 · lifecycle-viewmodel-compose 2.11.1 의
 *     sources jar 로 확인했다
 * - 규칙:
 *   · ViewModelStoreNavEntryDecorator: 엔트리마다 rememberViewModelStoreOwner(entry.contentKey, provider) 를 LocalViewModelStoreOwner 로 준다.
 *     onPop(contentKey) 에서 provider.clearKey → 그 엔트리 저장소의 ViewModel 이 onCleared. SavedStateHandle 을 쓰려면
 *     rememberSaveableStateHolderNavEntryDecorator 와 함께 쓴다(KDoc). removeViewModelStoreOnPop 매개변수는 2.11 에서 무시 · deprecated
 *   · 키로 만든 owner 의 저장소는 '컴포지션을 떠난다고' 비워지지 않는다 — clearKey 나 provider 가 사라질 때만. provider 는
 *     rememberViewModelStoreProvider 를 부른 자리가 컴포지션을 떠날 때 정리(부모가 회전을 넘기면 회전은 넘긴다)
 *   · onPop 은 PrepareBackStack 의 DisposableEffect onDispose 에서 불린다(적용 단계 — 여기서 스냅숏 상태를 써도 된다)
 *   · rememberDecoratedNavEntries 는 백스택 하나당 하나. 여러 백스택이면 탭마다 따로 부르고 NavDisplay(entries = …) 에 지금 탭 것만 준다 —
 *     다른 탭의 엔트리는 NavDisplay 밖으로 빠져도 onPop 이 오지 않아 저장 상태 · ViewModel 이 남는다
 *   · NavDisplay(backStack, entryDecorators, entryProvider) 는 안에서 rememberDecoratedNavEntries 를 부른다 — 이것을 화면 최상위로 올리면
 *     LazyColumn 카드가 스크롤로 폐기돼도 장식 상태가 남는다(이 화면들의 실물 카드 셋이 이렇게 한다)
 *   · Android 의 rememberNavBackStack(vararg NavKey) 는 SavedStateConfiguration 없이 리플렉션으로 직렬화기를 찾는다 — 키는 @Serializable + NavKey
 *   · @Serializable 키에 () -> Unit 필드를 넣어도 컴파일된다 — 플러그인이 Function0 에 PolymorphicSerializer 를 붙인다(javap). 저장할 때
 *     'Serializer for subclass … is not found in the polymorphic scope of Function0' SerializationException. 시뮬레이션 카드의 '복원 때 크래시' 가
 *     실물에서는 '저장 때 크래시' 다
 *   · rememberSaveable 은 호출 위치로 저장 키를 정한다 — 탭 넷의 rememberNavBackStack · 데코레이터를 반복문 한 자리에서 부르지 않고 따로 부른다
 * - 설계: ViewModel 은 액티비티 저장소에 만들지 않는다 — ViewModel 스코프 카드는 방식마다 rememberViewModelStoreOwner() 를 부모로 쓰고
 *   (방식을 바꾸면 key(mode) 아래가 떠나며 그 방식의 ViewModel 이 정리된다. 처음엔 손으로 viewModelStore.clear() 를 불렀는데,
 *   데코레이터 provider 가 부모 저장소에 둔 보관 객체까지 지울 수 있어 바꿨다). SavedStateHandle · 탭 카드는 데코레이터 기본 부모(액티비티)를 쓰되
 *   provider 가 예제 화면 컴포지션에 묶여 예제를 닫으면 정리된다
 * - 실측(헤드리스 에뮬, 실기기는 쓰지 않았다 — Pixel 4 API 30 debug · release 디버그 키 서명, 화면 글자를 단계마다 덤프):
 *   · Navigation3: Profile(user_123) → Detail #7 → #8, 시스템 뒤로 가기 1번 = 이 백스택 하나 줄임 · 다크 모드 왕복 뒤 그대로 ·
 *     am kill(pid 없음) → 최근 앱 복귀 뒤 Home › Profile(user_123) › Detail(#7) 그대로 · '처음으로' 뒤 시스템 뒤로 가기 = 예제 닫힘
 *   · ViewModel 스코프: 데코레이터 있음 Home #1 · Detail 1 #2 · Detail 2 #3, Pop → '#3 onCleared'. 없음으로 바꾸면 #1 · #2 정리,
 *     Home #4 를 Detail 1 도 '#4 (처음 만든 화면: Home)' 로 함께 쓰고 count 공유, Pop 해도 기록 없음. 다시 바꾸면 #4 정리
 *   · SavedStateHandle: Alice 읽음 2 → 다크 모드 왕복 2 · am kill → 최근 앱 2. 뒤로 → 다시 Alice 0. u-999 '찾을 수 없음'.
 *     임시 프로브(커밋 안 함)로 람다 키를 백스택에 넣으면 넣을 때는 멀쩡하고 HOME(상태 저장)에서 FATAL SerializationException
 *   · 탭: Home/1 · Home/2(#3) → Plans/1(#5) → Home 으로 돌아오면 Home/2 #3 그대로, 탭 전환만으로 onCleared 0. Pop → '#3 onCleared',
 *     Home/1 #2 · rememberSaveable 1 그대로. 카드를 위로 멀리 스크롤했다 돌아와도 같음
 * - 함정:
 *   · 기기 스크립트의 앵커 '✅ 실물' 이 NestedRoutes 안내 카드 문구에도 들어 있어 엉뚱한 카드로 갔다 → 카드 제목 앞부분까지 앵커로
 *   · 프로세스 종료 확인: am kill 뒤 pidof 는 종료 코드 1(정상), 최근 앱 화면은 앱 포커스가 아니라 안전 탭 헬퍼가 거부 → input tap 직접
 */