package com.example.composesample.presentation.example.component.architecture.development.language

/**
 * Kotlin Sealed Classes and Interfaces Guide
 * 
 * Sealed class와 sealed interface를 활용한 타입 안전한 상태 관리 가이드
 * 
 * 출처: https://proandroiddev.com/kotlin-sealed-classes-and-interface-9a90f80d4983
 * 
 * === Sealed Classes와 Sealed Interfaces ===
 * 
 * 1. Sealed Classes란?
 *    - 제한된 클래스 계층 구조를 정의
 *    - 모든 하위 클래스가 컴파일 타임에 알려짐
 *    - when 표현식에서 else 분기 불필요 (exhaustive)
 *    - 추상 클래스의 특별한 형태
 * 
 * 2. Sealed Classes의 특징
 *    컴파일 타임 안전성:
 *      - 모든 하위 타입이 컴파일러에 알려짐
 *      - when 표현식에서 모든 케이스 처리 강제
 *      - 런타임 오류 방지
 *    
 *    제한된 상속:
 *      - 같은 패키지/모듈 내에서만 상속 가능
 *      - 외부에서 임의로 확장 불가
 *      - API 설계의 명확성 향상
 *    
 *    타입 계층:
 *      sealed class Result<out T> {
 *          data class Success<T>(val data: T) : Result<T>()
 *          data class Error(val message: String) : Result<Nothing>()
 *          object Loading : Result<Nothing>()
 *      }
 * 
 * 3. Sealed Interfaces (Kotlin 1.5+)
 *    - Sealed class와 유사하지만 인터페이스
 *    - 다중 상속 가능
 *    - 더 유연한 타입 계층 구조
 *    
 *    예시:
 *      sealed interface UiState
 *      sealed interface Loadable
 *      
 *      data class Loading(val progress: Int) : UiState, Loadable
 *      data class Success(val data: String) : UiState
 *      data class Error(val error: Throwable) : UiState, Loadable
 * 
 * 4. when 표현식과의 완벽한 조합
 *    Exhaustive Checking:
 *      fun handle(result: Result<String>) = when (result) {
 *          is Result.Success -> result.data
 *          is Result.Error -> result.message
 *          Result.Loading -> "Loading..."
 *          // else 불필요! 모든 케이스가 처리됨
 *      }
 *    
 *    컴파일 타임 검증:
 *      - 새로운 하위 타입 추가 시 when 표현식에서 컴파일 에러
 *      - 실수로 케이스를 빠뜨리는 것 방지
 *      - 안전한 리팩토링 가능
 * 
 * 5. 실전 사용 사례
 *    1. API 응답 상태:
 *       sealed class ApiResponse<out T> {
 *           data class Success<T>(val data: T) : ApiResponse<T>()
 *           data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
 *           object Loading : ApiResponse<Nothing>()
 *           object Empty : ApiResponse<Nothing>()
 *       }
 *    
 *    2. UI 상태:
 *       sealed interface UiState {
 *           object Idle : UiState
 *           object Loading : UiState
 *           data class Content(val items: List<Item>) : UiState
 *           data class Error(val message: String) : UiState
 *       }
 *    
 *    3. 네비게이션 이벤트:
 *       sealed class NavigationEvent {
 *           object Back : NavigationEvent()
 *           data class ToDetail(val id: String) : NavigationEvent()
 *           data class ToExternal(val url: String) : NavigationEvent()
 *       }
 *    
 *    4. 폼 검증:
 *       sealed class ValidationResult {
 *           object Valid : ValidationResult()
 *           data class Invalid(val errors: List<String>) : ValidationResult()
 *       }
 * 
 * 6. Sealed Class vs Enum
 *    Enum의 제한:
 *      - 모든 인스턴스가 싱글톤
 *      - 상태를 가질 수 없음 (companion object 제외)
 *      - 제네릭 타입 파라미터 불가
 *    
 *    Sealed Class의 장점:
 *      - 각 하위 타입이 다른 데이터를 가질 수 있음
 *      - 제네릭 타입 파라미터 사용 가능
 *      - 더 복잡한 계층 구조 표현 가능
 *    
 *    예시 비교:
 *      // Enum - 제한적
 *      enum class Status { SUCCESS, ERROR, LOADING }
 *      
 *      // Sealed - 유연함
 *      sealed class Status {
 *          data class Success(val data: String) : Status()
 *          data class Error(val exception: Exception) : Status()
 *          object Loading : Status()
 *      }
 * 
 * 7. object vs data class vs class
 *    object (싱글톤):
 *      - 상태가 없는 경우
 *      - 하나의 인스턴스만 필요
 *      object Loading : Result<Nothing>()
 *    
 *    data class (데이터 보유):
 *      - 상태를 가지는 경우
 *      - equals, hashCode, toString 자동 생성
 *      data class Success<T>(val data: T) : Result<T>()
 *    
 *    class (일반 클래스):
 *      - 복잡한 로직이 필요한 경우
 *      - 상속이 필요한 경우
 *      class Error(val message: String, val cause: Throwable?) : Result<Nothing>()
 * 
 * 8. 제네릭과의 조합
 *    공변성 (out):
 *      sealed class Result<out T> {
 *          data class Success<T>(val data: T) : Result<T>()
 *          data class Error(val message: String) : Result<Nothing>()
 *      }
 *    
 *    반공변성 (in):
 *      sealed class Consumer<in T> {
 *          data class StringConsumer(val handler: (String) -> Unit) : Consumer<String>()
 *          object AnyConsumer : Consumer<Any>()
 *      }
 * 
 * 9. 중첩 Sealed 계층
 *    계층적 상태 표현:
 *      sealed class UiState {
 *          object Loading : UiState()
 *          
 *          sealed class Success : UiState() {
 *              data class WithData(val data: List<Item>) : Success()
 *              object Empty : Success()
 *          }
 *          
 *          sealed class Error : UiState() {
 *              data class Network(val code: Int) : Error()
 *              data class Generic(val message: String) : Error()
 *          }
 *      }
 * 
 * 10. 실전 패턴: Result Wrapper
 *     API 호출 결과 래핑:
 *       sealed class Result<out T> {
 *           data class Success<T>(val data: T) : Result<T>()
 *           data class Error(
 *               val exception: Exception,
 *               val message: String? = null
 *           ) : Result<Nothing>()
 *           object Loading : Result<Nothing>()
 *       }
 *       
 *       suspend fun fetchData(): Result<User> = try {
 *           val user = api.getUser()
 *           Result.Success(user)
 *       } catch (e: Exception) {
 *           Result.Error(e)
 *       }
 * 
 * 11. 실전 패턴: UI Event
 *     단방향 데이터 흐름:
 *       sealed class UiEvent {
 *           data class ShowSnackbar(val message: String) : UiEvent()
 *           data class Navigate(val route: String) : UiEvent()
 *           object NavigateBack : UiEvent()
 *           data class ShowDialog(val title: String, val message: String) : UiEvent()
 *       }
 *       
 *       viewModel.uiEvents.collect { event ->
 *           when (event) {
 *               is UiEvent.ShowSnackbar -> showSnackbar(event.message)
 *               is UiEvent.Navigate -> navController.navigate(event.route)
 *               UiEvent.NavigateBack -> navController.popBackStack()
 *               is UiEvent.ShowDialog -> showDialog(event.title, event.message)
 *           }
 *       }
 * 
 * 12. 실전 패턴: State Management
 *     화면 상태 관리:
 *       sealed interface ScreenState {
 *           object Initial : ScreenState
 *           object Loading : ScreenState
 *           data class Content(
 *               val items: List<Item>,
 *               val isRefreshing: Boolean = false
 *           ) : ScreenState
 *           data class Error(
 *               val message: String,
 *               val canRetry: Boolean = true
 *           ) : ScreenState
 *       }
 * 
 * 13. 테스트 용이성
 *     명확한 상태 정의:
 *       @Test
 *       fun `when loading state, show progress`() {
 *           val state = UiState.Loading
 *           assertTrue(state is UiState.Loading)
 *       }
 *       
 *       @Test
 *       fun `when error state, show error message`() {
 *           val state = UiState.Error("Network error")
 *           val message = when (state) {
 *               is UiState.Error -> state.message
 *               else -> fail("Expected Error state")
 *           }
 *           assertEquals("Network error", message)
 *       }
 * 
 * 14. 안티패턴
 *     ❌ 너무 많은 하위 타입:
 *       sealed class Action {
 *           object Action1 : Action()
 *           object Action2 : Action()
 *           // ... 20개 이상의 액션
 *       }
 *       // 관리가 어려워짐
 *     
 *     ❌ Sealed 없이 추상 클래스 사용:
 *       abstract class Result  // when에서 else 필요
 *     
 *     ❌ 불필요한 sealed 사용:
 *       sealed class SingleOption {
 *           object OnlyOne : SingleOption()
 *       }
 *       // 의미 없음, 일반 object 사용
 * 
 * 15. 베스트 프랙티스
 *     ✓ 명확한 네이밍:
 *       sealed class AuthState  // 좋음
 *       sealed class State      // 너무 일반적
 *     
 *     ✓ 적절한 계층 구조:
 *       - 3-7개 정도의 하위 타입이 이상적
 *       - 너무 많으면 그룹화 고려
 *     
 *     ✓ 불변성 유지:
 *       - data class는 val 사용
 *       - 상태 변경은 새 인스턴스 생성
 *     
 *     ✓ 문서화:
 *       /**
 *        * Represents the result of an API call
 *        * @see Success when the call succeeds
 *        * @see Error when the call fails
 *        */
 *       sealed class ApiResult
 * 
 * 16. Compose와의 통합
 *     상태 기반 UI:
 *       @Composable
 *       fun Screen(state: ScreenState) {
 *           when (state) {
 *               ScreenState.Loading -> LoadingIndicator()
 *               is ScreenState.Content -> ContentList(state.items)
 *               is ScreenState.Error -> ErrorView(state.message)
 *           }
 *       }
 *     
 *     ViewModel에서 상태 관리:
 *       class MyViewModel : ViewModel() {
 *           private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
 *           val state: StateFlow<ScreenState> = _state
 *           
 *           fun loadData() {
 *               viewModelScope.launch {
 *                   _state.value = ScreenState.Loading
 *                   _state.value = try {
 *                       val data = repository.getData()
 *                       ScreenState.Content(data)
 *                   } catch (e: Exception) {
 *                       ScreenState.Error(e.message ?: "Unknown error")
 *                   }
 *               }
 *           }
 *       }
 * 
 * 17. 성능 고려사항
 *     when 표현식 최적화:
 *       - 컴파일러가 최적화된 코드 생성
 *       - enum보다 약간 느릴 수 있지만 무시할 수준
 *       - 타입 안전성과 유연성이 성능 차이를 상쇄
 *     
 *     객체 할당:
 *       - object는 싱글톤이므로 할당 없음
 *       - data class는 매번 새 인스턴스 생성
 *       - 불변성을 위해 필요한 트레이드오프
 * 
 * 18. 마이그레이션 팁
 *     Enum에서 Sealed로:
 *       // Before
 *       enum class Status { SUCCESS, ERROR }
 *       
 *       // After
 *       sealed class Status {
 *           object Success : Status()
 *           data class Error(val message: String) : Status()
 *       }
 *     
 *     추상 클래스에서 Sealed로:
 *       // Before
 *       abstract class Result
 *       class Success : Result()
 *       class Error : Result()
 *       
 *       // After
 *       sealed class Result {
 *           object Success : Result()
 *           object Error : Result()
 *       }
 * 
 * 19. 디버깅 팁
 *     toString 활용:
 *       - data class는 자동으로 유용한 toString
 *       - 로그에서 상태 확인 용이
 *       Log.d("State", "Current: $state")
 *     
 *     when 디버깅:
 *       fun handle(state: UiState) = when (state) {
 *           is UiState.Loading -> {
 *               Log.d("State", "Loading")
 *               showLoading()
 *           }
 *           is UiState.Content -> {
 *               Log.d("State", "Content with ${state.items.size} items")
 *               showContent(state.items)
 *           }
 *           is UiState.Error -> {
 *               Log.e("State", "Error: ${state.message}")
 *               showError(state.message)
 *           }
 *       }
 * 
 * 20. 요약
 *     Sealed Classes:
 *       - 제한된 타입 계층 구조
 *       - Exhaustive when 표현식
 *       - 컴파일 타임 안전성
 *     
 *     Sealed Interfaces:
 *       - 다중 상속 가능
 *       - 더 유연한 구조
 *       - Kotlin 1.5+
 *     
 *     핵심 사용처:
 *       - API 응답 상태
 *       - UI 상태 관리
 *       - 이벤트 처리
 *       - 네비게이션
 * 
 * === 실전 예제 패턴 (from ExampleUI) ===
 * 
 * 21. API 응답 상태 관리 패턴
 *     ApiResult<T>로 API 호출 결과를 타입 안전하게 관리:
 *     
 *       sealed class ApiResult<out T> {
 *           data class Success<T>(
 *               val data: T, 
 *               val timestamp: Long = System.currentTimeMillis()
 *           ) : ApiResult<T>()
 *           data class Error(
 *               val message: String, 
 *               val code: Int = -1
 *           ) : ApiResult<Nothing>()
 *           object Loading : ApiResult<Nothing>()
 *           object Empty : ApiResult<Nothing>()
 *       }
 *     
 *     사용 예시:
 *       var apiState by remember { mutableStateOf<ApiResult<List<String>>>(ApiResult.Empty) }
 *       
 *       scope.launch {
 *           apiState = ApiResult.Loading
 *           delay(2000)
 *           apiState = ApiResult.Success(listOf("Item 1", "Item 2", "Item 3"))
 *       }
 *       
 *       when (apiState) {
 *           is ApiResult.Success -> ShowSuccessUI(apiState.data)
 *           is ApiResult.Error -> ShowErrorUI(apiState.message, apiState.code)
 *           ApiResult.Loading -> ShowLoadingUI()
 *           ApiResult.Empty -> ShowEmptyStateUI()
 *       }
 *     
 *     특징:
 *       - timestamp를 포함하여 데이터 신선도 추적 가능
 *       - error code를 통한 상세한 에러 처리
 *       - Empty 상태로 초기 상태 명확히 표현
 * 
 * 22. UI 상태 관리 패턴
 *     화면 전체 상태를 UiState로 관리:
 *     
 *       sealed class UiState {
 *           object Idle : UiState()
 *           object Loading : UiState()
 *           data class Content(
 *               val items: List<String>, 
 *               val count: Int
 *           ) : UiState()
 *           data class Error(
 *               val message: String, 
 *               val canRetry: Boolean = true
 *           ) : UiState()
 *       }
 *     
 *     사용 예시:
 *       var uiState by remember { mutableStateOf<UiState>(UiState.Idle) }
 *       
 *       // 데이터 로드
 *       scope.launch {
 *           uiState = UiState.Loading
 *           delay(2000)
 *           uiState = UiState.Content(
 *               items = List(5) { "Item ${it + 1}" },
 *               count = 5
 *           )
 *       }
 *       
 *       // UI 렌더링
 *       when (uiState) {
 *           UiState.Idle -> Text("Ready to load data")
 *           UiState.Loading -> CircularProgressIndicator()
 *           is UiState.Content -> {
 *               Text("Content (${uiState.count} items)")
 *               uiState.items.forEach { item -> ListItem(item) }
 *           }
 *           is UiState.Error -> {
 *               ErrorMessage(uiState.message)
 *               if (uiState.canRetry) RetryButton()
 *           }
 *       }
 *     
 *     특징:
 *       - Idle 상태로 초기화 명확히 구분
 *       - canRetry 플래그로 재시도 가능 여부 제어
 *       - count와 items를 함께 관리하여 데이터 일관성 유지
 * 
 * 23. Sealed Interface 다중 상속 패턴
 *     여러 sealed interface를 구현하여 유연한 타입 계층 구조 생성:
 *     
 *       sealed interface NetworkState
 *       sealed interface CacheState
 *       
 *       data class OnlineState(
 *           val connectionType: String
 *       ) : NetworkState, CacheState
 *       
 *       data class OfflineState(
 *           val reason: String
 *       ) : NetworkState
 *       
 *       data class CachedData(
 *           val items: List<String>
 *       ) : CacheState
 *     
 *     사용 예시:
 *       var networkState by remember { mutableStateOf<NetworkState>(OnlineState("WiFi")) }
 *       
 *       when (networkState) {
 *           is OnlineState -> {
 *               Text("Connected via ${networkState.connectionType}")
 *               Text("Implements: NetworkState, CacheState")
 *           }
 *           is OfflineState -> {
 *               Text("Offline: ${networkState.reason}")
 *               Text("Implements: NetworkState")
 *           }
 *       }
 *     
 *     장점:
 *       - OnlineState는 NetworkState와 CacheState 모두 구현
 *       - 다중 관심사를 하나의 타입으로 표현 가능
 *       - 더 유연한 타입 체계 구축
 *       - Kotlin 1.5+에서 지원
 * 
 * 24. Navigation Event 패턴
 *     네비게이션 이벤트를 sealed class로 타입 안전하게 처리:
 *     
 *       sealed class NavigationEvent {
 *           object Back : NavigationEvent()
 *           data class ToDetail(val id: String) : NavigationEvent()
 *           data class ToExternal(val url: String) : NavigationEvent()
 *           data class ShowDialog(
 *               val title: String, 
 *               val message: String
 *           ) : NavigationEvent()
 *       }
 *     
 *     사용 예시:
 *       var events by remember { mutableStateOf<List<NavigationEvent>>(emptyList()) }
 *       
 *       // 이벤트 발생
 *       events = events + NavigationEvent.Back
 *       events = events + NavigationEvent.ToDetail("123")
 *       events = events + NavigationEvent.ToExternal("https://example.com")
 *       
 *       // 이벤트 처리
 *       events.forEach { event ->
 *           when (event) {
 *               NavigationEvent.Back -> navController.popBackStack()
 *               is NavigationEvent.ToDetail -> navController.navigate("detail/${event.id}")
 *               is NavigationEvent.ToExternal -> openBrowser(event.url)
 *               is NavigationEvent.ShowDialog -> showDialog(event.title, event.message)
 *           }
 *       }
 *     
 *     특징:
 *       - 파라미터 없는 이벤트는 object 사용 (Back)
 *       - 파라미터 있는 이벤트는 data class 사용
 *       - 타입 안전하게 네비게이션 파라미터 전달
 *       - 이벤트 로그 추적 용이
 * 
 * 25. Nested Sealed Classes 패턴
 *     계층적 상태를 중첩된 sealed class로 표현:
 *     
 *       sealed class PaymentStatus {
 *           object Pending : PaymentStatus()
 *           
 *           sealed class InProgress : PaymentStatus() {
 *               data class Processing(val progress: Int) : InProgress()
 *               data class Verifying(val attempts: Int) : InProgress()
 *           }
 *           
 *           sealed class Completed : PaymentStatus() {
 *               data class Success(val transactionId: String) : Completed()
 *               data class Failed(val reason: String) : Completed()
 *           }
 *       }
 *     
 *     사용 예시:
 *       var paymentStatus by remember { mutableStateOf<PaymentStatus>(PaymentStatus.Pending) }
 *       
 *       // 결제 프로세스 진행
 *       scope.launch {
 *           paymentStatus = PaymentStatus.Pending
 *           delay(500)
 *           
 *           paymentStatus = PaymentStatus.InProgress.Processing(30)
 *           delay(800)
 *           
 *           paymentStatus = PaymentStatus.InProgress.Processing(70)
 *           delay(800)
 *           
 *           paymentStatus = PaymentStatus.InProgress.Verifying(1)
 *           delay(1000)
 *           
 *           paymentStatus = PaymentStatus.Completed.Success("TX-${System.currentTimeMillis()}")
 *       }
 *       
 *       // 상태별 UI 렌더링
 *       when (paymentStatus) {
 *           PaymentStatus.Pending -> {
 *               StatusIndicator("대기 중", Color.Gray, "⏳")
 *           }
 *           is PaymentStatus.InProgress.Processing -> {
 *               StatusIndicator("처리 중", Color.Blue, "⚙️")
 *               Text("진행률: ${paymentStatus.progress}%")
 *           }
 *           is PaymentStatus.InProgress.Verifying -> {
 *               StatusIndicator("검증 중", Color.Orange, "🔍")
 *               Text("시도: ${paymentStatus.attempts}회")
 *           }
 *           is PaymentStatus.Completed.Success -> {
 *               StatusIndicator("완료", Color.Green, "✅")
 *               Text("거래 ID: ${paymentStatus.transactionId}")
 *           }
 *           is PaymentStatus.Completed.Failed -> {
 *               StatusIndicator("실패", Color.Red, "❌")
 *               Text("사유: ${paymentStatus.reason}")
 *           }
 *       }
 *     
 *     장점:
 *       - 복잡한 상태 머신을 계층적으로 표현
 *       - InProgress, Completed 등 상위 그룹으로 분류
 *       - 각 단계별 세부 상태를 명확히 구분
 *       - 상태 전환 로직을 단계적으로 관리
 * 
 * 26. When Expression의 Exhaustive 특성
 *     Sealed class는 when 표현식에서 else 분기가 불필요:
 *     
 *       컴파일 타임 안전성:
 *         fun handle(result: ApiResult<String>) = when (result) {
 *             is ApiResult.Success -> result.data
 *             is ApiResult.Error -> result.message
 *             ApiResult.Loading -> "Loading..."
 *             ApiResult.Empty -> "No data"
 *             // else 불필요! 모든 케이스가 처리됨
 *         }
 *     
 *       새로운 타입 추가 시:
 *         sealed class ApiResult<out T> {
 *             data class Success<T>(val data: T) : ApiResult<T>()
 *             data class Error(val message: String) : ApiResult<Nothing>()
 *             object Loading : ApiResult<Nothing>()
 *             object Empty : ApiResult<Nothing>()
 *             object Cached : ApiResult<Nothing>()  // 새로 추가
 *         }
 *         
 *         // 기존 when 표현식에서 컴파일 에러 발생!
 *         // Cached 케이스를 처리하지 않았다고 알려줌
 *     
 *     장점:
 *       - 모든 케이스 처리를 컴파일러가 강제
 *       - 리팩토링 시 누락된 케이스를 컴파일 에러로 발견
 *       - 런타임 오류 방지
 *       - 안전한 코드 유지보수
 * 
 * 27. Compose UI 패턴 통합
 *     Sealed class와 Jetpack Compose의 완벽한 조합:
 *     
 *       상태 기반 UI 렌더링:
 *         @Composable
 *         fun ApiResultView(state: ApiResult<List<String>>) {
 *             when (state) {
 *                 is ApiResult.Success -> {
 *                     Column {
 *                         Row(verticalAlignment = Alignment.CenterVertically) {
 *                             Icon(Icons.Filled.CheckCircle, tint = Color.Green)
 *                             Text("Success", fontWeight = FontWeight.Bold)
 *                         }
 *                         state.data.forEach { item ->
 *                             Text("• $item")
 *                         }
 *                     }
 *                 }
 *                 is ApiResult.Error -> {
 *                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
 *                         Icon(Icons.Filled.Warning, tint = Color.Red)
 *                         Text("Error: ${state.message}")
 *                     }
 *                 }
 *                 ApiResult.Loading -> {
 *                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
 *                         CircularProgressIndicator()
 *                         Text("Loading...")
 *                     }
 *                 }
 *                 ApiResult.Empty -> {
 *                     Text("버튼을 눌러 API 호출 시뮬레이션")
 *                 }
 *             }
 *         }
 *       
 *       remember와 mutableStateOf 활용:
 *         @Composable
 *         fun MyScreen() {
 *             var apiState by remember { 
 *                 mutableStateOf<ApiResult<List<String>>>(ApiResult.Empty) 
 *             }
 *             val scope = rememberCoroutineScope()
 *             
 *             Column {
 *                 Button(onClick = {
 *                     scope.launch {
 *                         apiState = ApiResult.Loading
 *                         delay(2000)
 *                         apiState = ApiResult.Success(listOf("Data"))
 *                     }
 *                 }) {
 *                     Text("Load Data")
 *                 }
 *                 
 *                 ApiResultView(apiState)
 *             }
 *         }
 *       
 *       특징:
 *         - 상태 변경 시 자동 리컴포지션
 *         - when 표현식으로 선언적 UI 작성
 *         - 타입 안전한 상태 관리
 *         - 명확한 UI-상태 매핑
 * 
 * 28. 실전 팁: 주요 사용처 요약
 *     
 *     1. API 응답 상태:
 *        - Success/Error/Loading/Empty 구분
 *        - 타임스탬프, 에러 코드 등 추가 정보 포함
 *        - 제네릭 타입으로 재사용성 향상
 *     
 *     2. UI 상태 관리:
 *        - Idle/Loading/Content/Error 패턴
 *        - 재시도 가능 여부, 아이템 개수 등 메타데이터
 *        - 화면 전체 상태를 하나의 sealed class로 표현
 *     
 *     3. 이벤트 처리:
 *        - 네비게이션 이벤트 (Back, ToDetail, ToExternal)
 *        - UI 이벤트 (ShowDialog, ShowSnackbar)
 *        - 사용자 액션 이벤트
 *     
 *     4. 네트워크 상태:
 *        - Online/Offline 구분
 *        - 연결 타입 정보 (WiFi, Cellular)
 *        - Sealed interface로 다중 관심사 표현
 *     
 *     5. 복잡한 프로세스:
 *        - 결제, 인증 등 다단계 프로세스
 *        - Nested sealed class로 계층적 표현
 *        - 각 단계별 진행 상황 추적
 * 
 * 29. 컴포넌트 설계 패턴
 *     
 *     재사용 가능한 상태 컴포넌트:
 *       @Composable
 *       fun <T> LoadingStateView(
 *           state: ApiResult<T>,
 *           onSuccess: @Composable (T) -> Unit,
 *           onError: @Composable (String) -> Unit = { Text("Error: $it") },
 *           onLoading: @Composable () -> Unit = { CircularProgressIndicator() },
 *           onEmpty: @Composable () -> Unit = { Text("No data") }
 *       ) {
 *           when (state) {
 *               is ApiResult.Success -> onSuccess(state.data)
 *               is ApiResult.Error -> onError(state.message)
 *               ApiResult.Loading -> onLoading()
 *               ApiResult.Empty -> onEmpty()
 *           }
 *       }
 *       
 *       // 사용
 *       LoadingStateView(
 *           state = apiState,
 *           onSuccess = { data -> DataList(data) }
 *       )
 *     
 *     버튼 액션 패턴:
 *       Row {
 *           Button(onClick = {
 *               scope.launch {
 *                   apiState = ApiResult.Loading
 *                   delay(2000)
 *                   apiState = ApiResult.Success(listOf("Item 1", "Item 2"))
 *               }
 *           }) {
 *               Text("Success")
 *           }
 *           
 *           Button(onClick = {
 *               scope.launch {
 *                   apiState = ApiResult.Loading
 *                   delay(1500)
 *                   apiState = ApiResult.Error("Network timeout")
 *               }
 *           }) {
 *               Text("Error")
 *           }
 *       }
 * 
 * 30. 베스트 프랙티스 종합
 *     
 *     ✓ 제한된 클래스 계층 구조:
 *       - 모든 하위 타입이 컴파일 타임에 알려짐
 *       - when 표현식에서 else 분기 불필요
 *       - 타입 안전한 상태 관리 가능
 *       - 리팩토링 시 컴파일 에러로 누락 방지
 *     
 *     ✓ 적절한 타입 선택:
 *       - object: 상태 없는 싱글톤 (Loading, Empty, Pending)
 *       - data class: 상태를 가진 타입 (Success, Error, Content)
 *       - sealed class: 하위 계층이 있는 경우 (InProgress, Completed)
 *     
 *     ✓ 제네릭 활용:
 *       - ApiResult<T>로 재사용성 향상
 *       - out T로 공변성 지원
 *       - Nothing 타입으로 에러/로딩 상태 표현
 *     
 *     ✓ 추가 정보 포함:
 *       - timestamp: 데이터 신선도 추적
 *       - code: 에러 코드
 *       - canRetry: 재시도 가능 여부
 *       - count: 아이템 개수
 *     
 *     ✓ Compose 통합:
 *       - remember + mutableStateOf로 상태 관리
 *       - rememberCoroutineScope로 비동기 작업
 *       - when 표현식으로 선언적 UI
 *       - 자동 리컴포지션 활용
 * 
 * === 추가 실전 예제 ===
 * 
 * 31. Sealed Class vs Enum 비교 예제
 *     실제 UI로 Enum과 Sealed Class의 차이점을 체험:
 *     
 *       Enum의 제한사항:
 *         enum class StatusEnum {
 *             SUCCESS, ERROR, LOADING
 *         }
 *         
 *         문제점:
 *         - 상태를 가질 수 없음 (모든 인스턴스가 싱글톤)
 *         - 제네릭 타입 파라미터 사용 불가
 *         - 추가 정보(에러 메시지, 데이터 등) 저장 불가
 *         - 각 상태에 대한 컨텍스트 정보를 별도로 관리해야 함
 *     
 *       Sealed Class의 장점:
 *         sealed class StatusSealed {
 *             data class Success(
 *                 val data: String,
 *                 val timestamp: Long
 *             ) : StatusSealed()
 *             data class Error(
 *                 val exception: Exception,
 *                 val retryCount: Int
 *             ) : StatusSealed()
 *             object Loading : StatusSealed()
 *         }
 *         
 *         장점:
 *         - 각 타입이 다른 데이터를 보유할 수 있음
 *         - Success는 data와 timestamp를 가짐
 *         - Error는 exception과 retryCount를 가짐
 *         - Loading은 상태 없이 object로 표현
 *     
 *     UI 구현 예시:
 *       var useEnum by remember { mutableStateOf(true) }
 *       var enumStatus by remember { mutableStateOf(StatusEnum.SUCCESS) }
 *       var sealedStatus by remember { 
 *           mutableStateOf<StatusSealed>(StatusSealed.Loading) 
 *       }
 *       
 *       // Enum 사용 시
 *       when (enumStatus) {
 *           StatusEnum.SUCCESS -> Text("현재 상태: SUCCESS")
 *           StatusEnum.ERROR -> Text("현재 상태: ERROR")
 *           StatusEnum.LOADING -> Text("현재 상태: LOADING")
 *       }
 *       // ⚠️ 추가 정보 없음, 단순 상태만 표현
 *       
 *       // Sealed 사용 시
 *       when (sealedStatus) {
 *           is StatusSealed.Success -> {
 *               Text("✓ Success 상태")
 *               Text("📦 데이터: ${sealedStatus.data}")
 *               Text("⏰ 시간: ${formatTime(sealedStatus.timestamp)}")
 *           }
 *           is StatusSealed.Error -> {
 *               Text("✗ Error 상태")
 *               Text("❌ 에러: ${sealedStatus.exception.message}")
 *               Text("🔄 재시도 횟수: ${sealedStatus.retryCount}")
 *           }
 *           StatusSealed.Loading -> {
 *               Text("⏳ Loading 상태")
 *           }
 *       }
 *       // ✓ 상태와 함께 풍부한 컨텍스트 정보 표시
 *     
 *     실전 비교:
 *       Enum 적합한 경우:
 *       - 단순한 열거형 값만 필요
 *       - 상태에 대한 추가 정보 불필요
 *       - 예: DayOfWeek, Color, Direction
 *       
 *       Sealed Class 적합한 경우:
 *       - 상태와 함께 데이터 필요
 *       - 각 상태마다 다른 정보 보유
 *       - 예: API 응답, UI 상태, 결제 상태
 * 
 * 32. 제네릭 타입 파라미터 활용 예제
 *     하나의 ApiResult<T>로 여러 타입의 데이터 처리:
 *     
 *       타입 정의:
 *         data class User(val id: String, val name: String, val email: String)
 *         data class Product(val id: String, val name: String, val price: Double)
 *         
 *         sealed class ApiResult<out T> {
 *             data class Success<T>(val data: T) : ApiResult<T>()
 *             data class Error(val message: String) : ApiResult<Nothing>()
 *             object Loading : ApiResult<Nothing>()
 *             object Empty : ApiResult<Nothing>()
 *         }
 *     
 *       다양한 타입에 재사용:
 *         // String 타입
 *         var stringResult by remember { 
 *             mutableStateOf<ApiResult<String>>(ApiResult.Empty) 
 *         }
 *         scope.launch {
 *             stringResult = ApiResult.Loading
 *             delay(1500)
 *             stringResult = ApiResult.Success("Hello, Kotlin!")
 *         }
 *         
 *         // User 타입
 *         var userResult by remember { 
 *             mutableStateOf<ApiResult<User>>(ApiResult.Empty) 
 *         }
 *         scope.launch {
 *             userResult = ApiResult.Loading
 *             delay(1500)
 *             userResult = ApiResult.Success(
 *                 User(
 *                     id = "user_123",
 *                     name = "김개발",
 *                     email = "dev@example.com"
 *                 )
 *             )
 *         }
 *         
 *         // Product 타입
 *         var productResult by remember { 
 *             mutableStateOf<ApiResult<Product>>(ApiResult.Empty) 
 *         }
 *         scope.launch {
 *             productResult = ApiResult.Loading
 *             delay(1500)
 *             productResult = ApiResult.Success(
 *                 Product(
 *                     id = "prod_456",
 *                     name = "Kotlin 프로그래밍",
 *                     price = 35000.0
 *                 )
 *             )
 *         }
 *     
 *       UI 렌더링 (String):
 *         when (stringResult) {
 *             is ApiResult.Success -> {
 *                 Text("✓ 데이터: ${stringResult.data}")
 *             }
 *             is ApiResult.Error -> {
 *                 Text("✗ 에러: ${stringResult.message}")
 *             }
 *             ApiResult.Loading -> {
 *                 CircularProgressIndicator()
 *                 Text("Loading...")
 *             }
 *             ApiResult.Empty -> {
 *                 Text("버튼을 눌러 데이터 로드")
 *             }
 *         }
 *       
 *       UI 렌더링 (User):
 *         when (userResult) {
 *             is ApiResult.Success -> {
 *                 val user = userResult.data
 *                 Column {
 *                     Text("✓ User 정보:")
 *                     Text("• ID: ${user.id}")
 *                     Text("• Name: ${user.name}")
 *                     Text("• Email: ${user.email}")
 *                 }
 *             }
 *             is ApiResult.Error -> Text("✗ 에러: ${userResult.message}")
 *             ApiResult.Loading -> {
 *                 CircularProgressIndicator()
 *                 Text("Loading...")
 *             }
 *             ApiResult.Empty -> Text("버튼을 눌러 데이터 로드")
 *         }
 *       
 *       UI 렌더링 (Product):
 *         when (productResult) {
 *             is ApiResult.Success -> {
 *                 val product = productResult.data
 *                 Column {
 *                     Text("✓ Product 정보:")
 *                     Text("• ID: ${product.id}")
 *                     Text("• Name: ${product.name}")
 *                     Text("• Price: ${String.format("%,.0f", product.price)}원")
 *                 }
 *             }
 *             is ApiResult.Error -> Text("✗ 에러: ${productResult.message}")
 *             ApiResult.Loading -> {
 *                 CircularProgressIndicator()
 *                 Text("Loading...")
 *             }
 *             ApiResult.Empty -> Text("버튼을 눌러 데이터 로드")
 *         }
 *     
 *       제네릭 타입의 핵심 장점:
 *         1. 코드 재사용성:
 *            - ApiResult<T>를 한 번만 정의
 *            - String, User, Product 등 모든 타입에 사용
 *            - DRY(Don't Repeat Yourself) 원칙 준수
 *         
 *         2. 타입 안전성:
 *            - 컴파일 타임에 타입 체크
 *            - ApiResult<String>은 String만 담을 수 있음
 *            - 타입 캐스팅 오류 방지
 *         
 *         3. 유연성:
 *            - 새로운 타입 추가 시 ApiResult 수정 불필요
 *            - 도메인 모델 변경에 유연하게 대응
 *            - 확장 가능한 아키텍처
 *         
 *         4. 명확한 의도:
 *            - ApiResult<User>: User 데이터를 다룬다는 것이 명확
 *            - 코드 가독성 향상
 *            - 유지보수 용이
 *     
 *       실전 활용 팁:
 *         // Repository 계층
 *         interface UserRepository {
 *             suspend fun getUser(id: String): ApiResult<User>
 *             suspend fun getUsers(): ApiResult<List<User>>
 *         }
 *         
 *         interface ProductRepository {
 *             suspend fun getProduct(id: String): ApiResult<Product>
 *             suspend fun getProducts(): ApiResult<List<Product>>
 *         }
 *         
 *         // ViewModel 계층
 *         class UserViewModel : ViewModel() {
 *             private val _userState = MutableStateFlow<ApiResult<User>>(ApiResult.Empty)
 *             val userState: StateFlow<ApiResult<User>> = _userState
 *             
 *             fun loadUser(id: String) {
 *                 viewModelScope.launch {
 *                     _userState.value = ApiResult.Loading
 *                     _userState.value = repository.getUser(id)
 *                 }
 *             }
 *         }
 *         
 *         // UI 계층
 *         @Composable
 *         fun UserScreen(viewModel: UserViewModel) {
 *             val userState by viewModel.userState.collectAsState()
 *             
 *             when (userState) {
 *                 is ApiResult.Success -> UserDetail(userState.data)
 *                 is ApiResult.Error -> ErrorView(userState.message)
 *                 ApiResult.Loading -> LoadingView()
 *                 ApiResult.Empty -> EmptyView()
 *             }
 *         }
 * 
 * 33. 인터랙티브 예제의 교육적 가치
 *     
 *     실제 동작하는 UI 예제의 장점:
 *       1. 즉각적인 피드백:
 *          - 버튼 클릭으로 상태 변경 체험
 *          - Loading → Success 전환 과정 시각화
 *          - 각 상태의 UI 렌더링 확인
 *       
 *       2. 비교 학습:
 *          - Enum과 Sealed를 토글하며 비교
 *          - 동일한 작업에서 차이점 명확히 확인
 *          - 실전 적용 가능성 판단
 *       
 *       3. 타입별 차이:
 *          - String, User, Product 각각 테스트
 *          - 제네릭의 유연성 직접 체험
 *          - 다양한 데이터 구조 이해
 *       
 *       4. 실전 시뮬레이션:
 *          - 실제 API 호출과 유사한 흐름
 *          - Loading 상태의 중요성 인식
 *          - Error 핸들링 패턴 학습
 *     
 *     학습 순서 권장:
 *       1단계: Introduction Card
 *          - Sealed Class 기본 개념 이해
 *          - 주요 특징과 사용처 파악
 *       
 *       2단계: API Result Demo
 *          - 가장 기본적인 패턴 실습
 *          - Success/Error/Loading 사이클 체험
 *       
 *       3단계: UI State Demo
 *          - 화면 상태 관리 이해
 *          - Idle, Retry 등 추가 상태 학습
 *       
 *       4단계: Sealed vs Enum
 *          - 두 방식의 장단점 비교
 *          - 적절한 선택 기준 이해
 *       
 *       5단계: Generic Type Demo
 *          - 제네릭의 강력함 체험
 *          - 다양한 타입 처리 방법 학습
 *       
 *       6단계: Advanced (Nested, Interface)
 *          - 복잡한 시나리오 대응
 *          - 고급 패턴 습득
 */

object SealedClassInterfaceGuide {
    const val GUIDE_INFO = """
        Kotlin Sealed Classes & Interfaces
        
        핵심 개념:
        - 제한된 클래스 계층 구조
        - Exhaustive when 표현식
        - 컴파일 타임 타입 안전성
        - 다중 상속 지원 (Sealed Interface)
        
        주요 사용처:
        - API 응답 상태 (Success/Error/Loading)
        - UI 상태 관리
        - 이벤트 처리
        - 네비게이션
        
        장점:
        - 타입 안전성
        - 명확한 상태 표현
        - 리팩토링 안전성
        
        출처: https://proandroiddev.com/kotlin-sealed-classes-and-interface-9a90f80d4983
    """
    
    const val SEALED_CLASS = """
        === Sealed Class ===
        
        1. 정의:
           sealed class Result<out T> {
               data class Success<T>(val data: T) : Result<T>()
               data class Error(val message: String) : Result<Nothing>()
               object Loading : Result<Nothing>()
           }
        
        2. 특징:
           - 모든 하위 타입이 컴파일 타임에 알려짐
           - when 표현식에서 else 불필요
           - 같은 패키지/모듈 내에서만 상속 가능
        
        3. when과 조합:
           fun handle(result: Result<String>) = when (result) {
               is Result.Success -> result.data
               is Result.Error -> result.message
               Result.Loading -> "Loading..."
           }
    """
    
    const val SEALED_INTERFACE = """
        === Sealed Interface (Kotlin 1.5+) ===
        
        1. 정의:
           sealed interface UiState
           sealed interface Loadable
           
           data class Loading(val progress: Int) : UiState, Loadable
           data class Success(val data: String) : UiState
           data class Error(val error: Throwable) : UiState, Loadable
        
        2. 장점:
           - 다중 상속 가능
           - 더 유연한 타입 계층 구조
           - Sealed class와 동일한 exhaustive checking
        
        3. 사용 시기:
           - 여러 sealed 타입을 조합해야 할 때
           - 믹스인 패턴이 필요할 때
           - 더 유연한 설계가 필요할 때
    """
    
    const val PRACTICAL_PATTERNS = """
        === 실전 패턴 ===
        
        1. API 응답:
           sealed class ApiResponse<out T> {
               data class Success<T>(val data: T) : ApiResponse<T>()
               data class Error(val code: Int, val message: String) : ApiResponse<Nothing>()
               object Loading : ApiResponse<Nothing>()
           }
        
        2. UI 상태:
           sealed interface ScreenState {
               object Loading : ScreenState
               data class Content(val items: List<Item>) : ScreenState
               data class Error(val message: String) : ScreenState
           }
        
        3. 이벤트:
           sealed class NavigationEvent {
               object Back : NavigationEvent()
               data class ToDetail(val id: String) : NavigationEvent()
               data class ToExternal(val url: String) : NavigationEvent()
           }
        
        4. ViewModel 통합:
           class MyViewModel : ViewModel() {
               private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
               val state: StateFlow<ScreenState> = _state
               
               fun loadData() {
                   viewModelScope.launch {
                       _state.value = try {
                           ScreenState.Content(repository.getData())
                       } catch (e: Exception) {
                           ScreenState.Error(e.message ?: "Error")
                       }
                   }
               }
           }
    """
    
    const val VS_ENUM = """
        === Sealed Class vs Enum ===
        
        Enum의 제한:
        - 모든 인스턴스가 싱글톤
        - 상태를 가질 수 없음
        - 제네릭 타입 파라미터 불가
        
        Sealed Class의 장점:
        - 각 하위 타입이 다른 데이터 보유 가능
        - 제네릭 타입 파라미터 사용 가능
        - 더 복잡한 계층 구조 표현
        
        예시:
        // Enum
        enum class Status { SUCCESS, ERROR }
        
        // Sealed Class
        sealed class Status {
            data class Success(val data: String) : Status()
            data class Error(val exception: Exception) : Status()
        }
    """
    
    const val BEST_PRACTICES = """
        === 베스트 프랙티스 ===
        
        1. 명확한 네이밍:
           ✓ sealed class AuthState
           ✗ sealed class State
        
        2. 적절한 하위 타입 수:
           - 3-7개가 이상적
           - 너무 많으면 그룹화 고려
        
        3. 불변성 유지:
           - data class는 val 사용
           - 상태 변경은 새 인스턴스 생성
        
        4. object vs data class:
           - object: 상태 없음, 싱글톤
           - data class: 상태 보유, 매번 생성
        
        5. Compose 통합:
           @Composable
           fun Screen(state: ScreenState) {
               when (state) {
                   ScreenState.Loading -> LoadingView()
                   is ScreenState.Content -> ContentView(state.items)
                   is ScreenState.Error -> ErrorView(state.message)
               }
           }
        
        6. 테스트:
           @Test
           fun `verify success state`() {
               val state = ScreenState.Content(listOf())
               assertTrue(state is ScreenState.Content)
           }
    """
}
