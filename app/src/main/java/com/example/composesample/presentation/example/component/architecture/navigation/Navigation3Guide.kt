package com.example.composesample.presentation.example.component.architecture.navigation

/**
 * Jetpack Navigation 3 Guide
 * 
 * Navigation 3: Compose State 기반의 새로운 네비게이션 라이브러리
 * 
 * 출처: https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html
 * 
 * === Navigation3ExampleUI에서 다루는 내용 ===
 * 
 * 이 예제는 Navigation 3의 핵심 개념을 시각적으로 보여줍니다:
 * 
 * 주요 데모:
 * - NavDisplay: Compose state 기반 화면 표시
 * - Back Stack 관리: 직접적인 스택 제어
 * - 화면 전환: 선언적 네비게이션
 * - Migration: Nav2에서 Nav3로 전환
 * 
 * === Navigation 3 개요 ===
 * 
 * 1. 등장 배경
 *    - Nav2는 7년 전 설계 (2018년)
 *    - 현대적인 Reactive + Declarative UI에 최적화되지 않음
 *    - 내부 상태로 인한 Single Source of Truth 문제
 *    - 유연성과 커스터마이징 부족
 * 
 * 2. Navigation 3의 핵심 철학
 *    "Building blocks approach"
 *    - 작고 분리된 API들을 제공
 *    - 조합하여 복잡한 기능 구현
 *    - 원하는 대로 커스터마이징 가능
 *    - Nav3 자체도 이 빌딩 블록으로 구성됨
 * 
 * 3. 주요 변화
 *    Nav2:
 *    - NavController: 내부 상태 관리
 *    - NavHost: 화면 표시
 *    - 암시적 백스택 관리
 *    
 *    Nav3:
 *    - 사용자가 직접 상태 관리 (Compose State)
 *    - NavDisplay: 단순히 상태 관찰 후 UI 업데이트
 *    - 명시적 백스택 제어
 * 
 * === 핵심 개념 ===
 * 
 * 1. NavKey
 *    화면을 나타내는 키:
 *      data class HomeScreen(val userId: String = "") : NavKey
 *      data class DetailScreen(val itemId: String) : NavKey
 *      object SettingsScreen : NavKey
 *    
 *    특징:
 *    - 각 화면의 고유 식별자
 *    - 파라미터를 프로퍼티로 포함 가능
 *    - Serializable하여 Process death 대응
 * 
 * 2. NavDisplay
 *    화면을 표시하는 컴포저블:
 *      NavDisplay(
 *          backStack = backStack,
 *          entryProvider = { key ->
 *              when (key) {
 *                  is HomeScreen -> HomeContent()
 *                  is DetailScreen -> DetailContent(key.itemId)
 *                  SettingsScreen -> SettingsContent()
 *              }
 *          }
 *      )
 *    
 *    동작:
 *    - backStack의 변화를 관찰
 *    - 현재 키에 해당하는 화면 표시
 *    - Compose State 기반으로 자동 리컴포지션
 * 
 * 3. Back Stack 관리
 *    직접 상태 관리:
 *      class NavigationState {
 *          var backStack by mutableStateOf(listOf<NavKey>(HomeScreen()))
 *              private set
 *          
 *          fun navigateTo(key: NavKey) {
 *              backStack = backStack + key
 *          }
 *          
 *          fun popBackStack() {
 *              if (backStack.size > 1) {
 *                  backStack = backStack.dropLast(1)
 *              }
 *          }
 *      }
 *    
 *    장점:
 *    - 완전한 제어권
 *    - Single Source of Truth
 *    - 테스트 용이
 * 
 * 4. rememberNavBackStack
 *    Nav3가 제공하는 헬퍼 함수:
 *      val backStack = rememberNavBackStack(
 *          initialKeys = listOf(HomeScreen())
 *      )
 *    
 *    기능:
 *    - Process death 시 복원
 *    - SavedStateHandle 통합
 *    - 기본적인 백스택 관리
 * 
 * === Nav2에서 Nav3로 Migration ===
 * 
 * 1. 의존성 추가
 *    build.gradle:
 *    implementation("androidx.navigation:navigation-compose:3.0.0")
 * 
 * 2. NavKey 구현
 *    Nav2:
 *    sealed class Screen(val route: String) {
 *        object Home : Screen("home")
 *        data class Detail(val id: String) : Screen("detail/{id}")
 *    }
 *    
 *    Nav3:
 *    sealed interface AppScreen : NavKey
 *    object HomeScreen : AppScreen
 *    data class DetailScreen(val id: String) : AppScreen
 * 
 * 3. NavController → NavigationState
 *    Nav2:
 *    val navController = rememberNavController()
 *    navController.navigate(Screen.Detail("123").route)
 *    
 *    Nav3:
 *    val navigationState = remember { NavigationState() }
 *    navigationState.navigateTo(DetailScreen("123"))
 * 
 * 4. NavHost → NavDisplay
 *    Nav2:
 *    NavHost(navController, startDestination = "home") {
 *        composable("home") { HomeScreen() }
 *        composable("detail/{id}") { backStackEntry ->
 *            DetailScreen(backStackEntry.arguments?.getString("id"))
 *        }
 *    }
 *    
 *    Nav3:
 *    NavDisplay(
 *        backStack = navigationState.backStack,
 *        entryProvider = { key ->
 *            when (key) {
 *                HomeScreen -> HomeContent()
 *                is DetailScreen -> DetailContent(key.id)
 *            }
 *        }
 *    )
 * 
 * 5. 백 버튼 처리
 *    Nav2:
 *    BackHandler(enabled = navController.previousBackStackEntry != null) {
 *        navController.popBackStack()
 *    }
 *    
 *    Nav3:
 *    BackHandler(enabled = navigationState.canGoBack) {
 *        navigationState.popBackStack()
 *    }
 * 
 * === 주요 기능 ===
 * 
 * 1. 화면 애니메이션
 *    전역 설정:
 *    NavDisplay(
 *        backStack = backStack,
 *        entryProvider = { key -> ... },
 *        enterTransition = { slideInHorizontally() },
 *        exitTransition = { slideOutHorizontally() }
 *    )
 *    
 *    개별 설정:
 *    when (key) {
 *        is DetailScreen -> {
 *            AnimatedContent(
 *                targetState = key,
 *                transitionSpec = { fadeIn() with fadeOut() }
 *            ) {
 *                DetailContent(it.id)
 *            }
 *        }
 *    }
 * 
 * 2. Multiple Back Stacks
 *    여러 개의 독립적인 백스택:
 *    class MultipleBackStackState {
 *        var currentTab by mutableStateOf(Tab.HOME)
 *        val homeBackStack = mutableStateListOf<NavKey>(HomeScreen())
 *        val profileBackStack = mutableStateListOf<NavKey>(ProfileScreen())
 *        
 *        val currentBackStack: List<NavKey>
 *            get() = when (currentTab) {
 *                Tab.HOME -> homeBackStack
 *                Tab.PROFILE -> profileBackStack
 *            }
 *    }
 * 
 * 3. Adaptive Layouts (Scenes API)
 *    List-Detail 패턴:
 *    SceneHost(
 *        currentScene = if (isLargeScreen) TwoPaneScene else SinglePaneScene,
 *        listPane = { ListContent() },
 *        detailPane = { DetailContent() }
 *    )
 * 
 * 4. Deep Links
 *    Manual handling:
 *    fun handleDeepLink(uri: Uri) {
 *        val screen = when (uri.path) {
 *            "/home" -> HomeScreen()
 *            "/detail" -> DetailScreen(uri.getQueryParameter("id") ?: "")
 *            else -> null
 *        }
 *        screen?.let { navigationState.navigateTo(it) }
 *    }
 * 
 * === 실전 사용 사례 ===
 * 
 * 1. 기본 네비게이션
 *    sealed interface AppScreen : NavKey {
 *        object Home : AppScreen
 *        data class Profile(val userId: String) : AppScreen
 *        object Settings : AppScreen
 *    }
 *    
 *    class AppNavigationState {
 *        var backStack by mutableStateOf(listOf<AppScreen>(AppScreen.Home))
 *            private set
 *        
 *        fun navigateToProfile(userId: String) {
 *            backStack = backStack + AppScreen.Profile(userId)
 *        }
 *        
 *        fun navigateToSettings() {
 *            backStack = backStack + AppScreen.Settings
 *        }
 *        
 *        fun goBack() {
 *            if (backStack.size > 1) {
 *                backStack = backStack.dropLast(1)
 *            }
 *        }
 *    }
 * 
 * 2. ViewModel과 통합
 *    class NavigationViewModel : ViewModel() {
 *        private val _backStack = MutableStateFlow(listOf<AppScreen>(AppScreen.Home))
 *        val backStack: StateFlow<List<AppScreen>> = _backStack
 *        
 *        fun navigateTo(screen: AppScreen) {
 *            _backStack.value = _backStack.value + screen
 *        }
 *        
 *        fun popBackStack() {
 *            if (_backStack.value.size > 1) {
 *                _backStack.value = _backStack.value.dropLast(1)
 *            }
 *        }
 *    }
 *    
 *    @Composable
 *    fun App(viewModel: NavigationViewModel = viewModel()) {
 *        val backStack by viewModel.backStack.collectAsState()
 *        
 *        NavDisplay(
 *            backStack = backStack,
 *            entryProvider = { key -> ... }
 *        )
 *    }
 * 
 * 3. 결과 반환
 *    Event 방식:
 *    class NavigationState {
 *        var resultCallback: ((Any) -> Unit)? = null
 *        
 *        fun navigateForResult(screen: NavKey, onResult: (Any) -> Unit) {
 *            resultCallback = onResult
 *            backStack = backStack + screen
 *        }
 *        
 *        fun popWithResult(result: Any) {
 *            resultCallback?.invoke(result)
 *            resultCallback = null
 *            popBackStack()
 *        }
 *    }
 *    
 *    Shared State 방식:
 *    class NavigationState {
 *        var sharedResult by mutableStateOf<Any?>(null)
 *        
 *        fun popWithResult(result: Any) {
 *            sharedResult = result
 *            popBackStack()
 *        }
 *    }
 * 
 * 4. Modularization
 *    각 모듈별 NavKey 정의:
 *    // :feature:home module
 *    sealed interface HomeNavKey : NavKey {
 *        object Home : HomeNavKey
 *        data class Details(val id: String) : HomeNavKey
 *    }
 *    
 *    // :feature:profile module
 *    sealed interface ProfileNavKey : NavKey {
 *        object Profile : ProfileNavKey
 *        object EditProfile : ProfileNavKey
 *    }
 *    
 *    // :app module
 *    sealed interface AppNavKey : NavKey
 *    
 *    @Composable
 *    fun AppNavDisplay(backStack: List<NavKey>) {
 *        NavDisplay(
 *            backStack = backStack,
 *            entryProvider = { key ->
 *                when (key) {
 *                    is HomeNavKey -> HomeNavHost(key)
 *                    is ProfileNavKey -> ProfileNavHost(key)
 *                }
 *            }
 *        )
 *    }
 * 
 * === Nav3의 장점 ===
 * 
 * 1. Single Source of Truth
 *    - 네비게이션 상태를 직접 소유
 *    - 다른 UI 상태와 동일하게 관리
 *    - 일관된 상태 관리 패턴
 * 
 * 2. 완전한 제어
 *    - 백스택을 직접 조작
 *    - 원하는 대로 커스터마이징
 *    - 복잡한 네비게이션 로직 구현 가능
 * 
 * 3. Compose 친화적
 *    - Compose State 기반
 *    - 선언적 UI와 자연스럽게 통합
 *    - 리액티브 프로그래밍 패러다임 준수
 * 
 * 4. 테스트 용이
 *    - 네비게이션 로직을 독립적으로 테스트
 *    - UI 없이 상태만 테스트 가능
 *    - Mocking 불필요
 * 
 * 5. 유연성
 *    - 빌딩 블록 조합
 *    - 원하는 부분만 사용
 *    - 기존 솔루션과 혼합 가능
 * 
 * === Recipes Repository ===
 * 
 * 공통 사용 사례를 위한 레시피:
 * 1. Multiple back stacks (탭 네비게이션)
 * 2. Modularization and dependency injection
 * 3. Passing arguments to ViewModels
 * 4. Returning results (events & shared state)
 * 5. Deep links handling
 * 6. Koin integration
 * 
 * Recipes가 인기를 얻으면 라이브러리에 통합 가능
 * 
 * === 실전 팁 ===
 * 
 * 1. 백스택 크기 관리
 *    fun navigateToAndClearTop(screen: NavKey) {
 *        backStack = listOf(backStack.first(), screen)
 *    }
 * 
 * 2. 조건부 네비게이션
 *    fun navigateIfAllowed(screen: NavKey) {
 *        if (userIsLoggedIn) {
 *            backStack = backStack + screen
 *        } else {
 *            backStack = backStack + LoginScreen
 *        }
 *    }
 * 
 * 3. Process Death 대응
 *    val backStack = rememberSaveable(
 *        saver = listSaver(
 *            save = { it.map { key -> key.serialize() } },
 *            restore = { it.map { str -> NavKey.deserialize(str) } }
 *        )
 *    ) { mutableStateOf(listOf(HomeScreen())) }
 * 
 * 4. 애니메이션 최적화
 *    // 뒤로 가기 시 다른 애니메이션
 *    val isPopping = remember { mutableStateOf(false) }
 *    
 *    NavDisplay(
 *        backStack = backStack,
 *        entryProvider = { key -> ... },
 *        enterTransition = {
 *            if (isPopping.value) slideInHorizontally { -it }
 *            else slideInHorizontally { it }
 *        }
 *    )
 * 
 * === Nav2 vs Nav3 비교 ===
 * 
 * Nav2:
 * ✓ 성숙하고 안정적
 * ✓ 풍부한 생태계
 * ✓ 많은 샘플 코드
 * ✗ 내부 상태 관리
 * ✗ 커스터마이징 제한
 * ✗ Compose와 완벽히 통합되지 않음
 * 
 * Nav3:
 * ✓ Compose 친화적
 * ✓ 완전한 제어
 * ✓ Single Source of Truth
 * ✓ 유연한 커스터마이징
 * ✓ 테스트 용이
 * ✗ 새로운 개념 학습 필요
 * ✗ 생태계 구축 중
 * 
 * === 마이그레이션 전략 ===
 * 
 * 1. 점진적 마이그레이션
 *    - 새 화면부터 Nav3 사용
 *    - 기존 화면은 Nav2 유지
 *    - 점진적으로 전환
 * 
 * 2. AI Agent 활용
 *    - Android Studio의 Agent Mode 사용
 *    - Migration guide를 컨텍스트로 제공
 *    - 자동 마이그레이션 시도
 *    - 결과를 신중히 검토
 * 
 * 3. 테스트 우선
 *    - 마이그레이션 전 테스트 작성
 *    - 동작 검증
 *    - 점진적 리팩토링
 * 
 * === 요약 ===
 * 
 * Navigation 3:
 * - Compose State 기반
 * - 직접적인 백스택 제어
 * - Building blocks approach
 * - Single Source of Truth
 * - 완전한 커스터마이징
 * 
 * 사용 시기:
 * - 새 프로젝트 시작
 * - Compose 중심 앱
 * - 복잡한 네비게이션 필요
 * - 완전한 제어 원함
 * 
 * Nav2 유지:
 * - 기존 프로젝트 안정성
 * - 빠른 개발 필요
 * - 표준 패턴만 사용
 */

object Navigation3Guide {
    const val GUIDE_INFO = """
        Jetpack Navigation 3
        
        핵심 변화:
        - Compose State 기반
        - 직접 백스택 관리
        - NavDisplay: 상태 관찰 후 UI 업데이트
        - Single Source of Truth
        
        주요 개념:
        - NavKey: 화면 식별자
        - NavDisplay: 화면 표시 컴포저블
        - Building blocks: 조합 가능한 API
        
        출처: https://android-developers.googleblog.com/2025/11/jetpack-navigation-3-is-stable.html
    """
}
