package com.example.composesample.presentation.example.component.architecture.development.concurrency

/**
 * withContext(Dispatchers.IO) vs launch(Dispatchers.IO) Guide
 * 
 * 출처: https://proandroiddev.com/the-real-difference-between-withcontext-dispatchers-io-and-launch-dispatchers-io-b70ec00a33f2
 * 
 * === WithContextExampleUI에서 다루는 내용 ===
 * 
 * 이 예제는 withContext, launch, async/await의 실제 차이를 시각적으로 보여줍니다:
 * 
 * 주요 데모:
 * - withContext: 순차 실행과 결과 반환
 * - launch: 병렬 실행과 Fire-and-Forget
 * - async/await: 병렬 실행과 결과 수집 (NEW!)
 * - 예외 처리 차이 (NEW!)
 * - 실행 시간 비교
 * - 실전 사용 케이스
 * 
 * === 핵심 차이점 ===
 * 
 * 1. withContext(Dispatchers.IO)
 *    특징:
 *    - suspend 함수 (일시 중단)
 *    - 작업 완료를 기다림 (await)
 *    - 결과값 반환 가능
 *    - 순차적 실행
 *    - 구조화된 동시성 (Structured Concurrency)
 *    
 *    사용 시기:
 *    - 결과값이 필요한 경우
 *    - 작업 완료를 기다려야 하는 경우
 *    - 순차적 실행이 필요한 경우
 *    
 *    예시:
 *    suspend fun fetchUserData(): User {
 *        return withContext(Dispatchers.IO) {
 *            // 네트워크 요청
 *            api.getUser()  // 결과 반환
 *        }
 *    }
 * 
 * 2. launch(Dispatchers.IO)
 *    특징:
 *    - 새로운 코루틴 시작 (코루틴 빌더)
 *    - 작업 완료를 기다리지 않음 (Fire-and-Forget)
 *    - 결과값 반환 불가 (Job 반환)
 *    - 병렬 실행 가능
 *    - 독립적인 생명주기
 *    
 *    사용 시기:
 *    - 결과값이 필요 없는 경우
 *    - Fire-and-Forget 작업
 *    - 병렬 실행이 필요한 경우
 *    - 로깅, 분석 이벤트 전송 등
 *    
 *    예시:
 *    fun logEvent(event: String) {
 *        viewModelScope.launch(Dispatchers.IO) {
 *            // 분석 이벤트 전송
 *            analytics.log(event)  // 결과 무시
 *        }
 *    }
 * 
 * === 상세 비교 ===
 * 
 * 1. 실행 모델
 *    withContext:
 *    - 현재 코루틴의 컨텍스트를 전환
 *    - 같은 코루틴 내에서 실행
 *    - 호출자는 완료를 기다림
 *    
 *    viewModelScope.launch {  // Main
 *        val result = withContext(Dispatchers.IO) {  // IO로 전환
 *            heavyWork()  // IO 스레드에서 실행
 *        }  // Main으로 복귀
 *        updateUI(result)  // Main에서 실행
 *    }
 *    
 *    launch:
 *    - 새로운 코루틴 생성
 *    - 독립적으로 실행
 *    - 호출자는 즉시 다음 코드 실행
 *    
 *    viewModelScope.launch {  // Main
 *        launch(Dispatchers.IO) {  // 새 코루틴 (IO)
 *            heavyWork()  // 병렬 실행
 *        }
 *        // 즉시 실행됨 (기다리지 않음)
 *        updateUI()  // Main에서 실행
 *    }
 * 
 * 2. 결과 반환
 *    withContext:
 *    - 블록의 마지막 표현식이 반환값
 *    - 타입 안전 (Type-safe)
 *    
 *    suspend fun <T> fetchData(): T {
 *        return withContext(Dispatchers.IO) {
 *            // 타입 T를 반환
 *            repository.getData()
 *        }
 *    }
 *    
 *    launch:
 *    - Job 객체 반환 (결과값 X)
 *    - 취소/완료 대기만 가능
 *    
 *    val job: Job = launch(Dispatchers.IO) {
 *        repository.getData()  // 반환값 무시됨
 *    }
 *    job.join()  // 완료 대기만 가능
 * 
 * 3. 예외 처리 (실무에서 매우 중요!)
 *    
 *    추가된 이유:
 *    - 실무에서 네트워크 에러, 파싱 에러 등 처리 시 필수
 *    - launch와 withContext의 예외 처리 방식이 다름을 모르면 버그 발생
 *    - 예외가 제대로 처리되지 않으면 앱 크래시 또는 조용한 실패
 *    
 *    withContext:
 *    - 예외가 호출자로 전파 (suspend 함수 특성)
 *    - try-catch로 직접 처리 가능
 *    - 동기 코드처럼 작동
 *    
 *    try {
 *        val result = withContext(Dispatchers.IO) {
 *            if (networkError) throw IOException("Network error")
 *            fetchData()
 *        }
 *        // 성공 처리
 *        updateUI(result)
 *    } catch (e: IOException) {
 *        // ✅ 예외를 여기서 잡을 수 있음!
 *        showNetworkError()
 *    }
 *    
 *    launch:
 *    - 예외가 내부에서만 발생 (새로운 코루틴)
 *    - 호출 위치에서 try-catch 불가
 *    - 내부에서 처리하거나 CoroutineExceptionHandler 필요
 *    
 *    // ❌ 잘못된 방법
 *    try {
 *        viewModelScope.launch(Dispatchers.IO) {
 *            if (networkError) throw IOException("Network error")
 *            val data = fetchData()
 *        }
 *    } catch (e: IOException) {
 *        // 여기서 못 잡음! (이미 다른 코루틴)
 *    }
 *    
 *    // ✅ 올바른 방법 1: 내부에서 처리
 *    viewModelScope.launch(Dispatchers.IO) {
 *        try {
 *            if (networkError) throw IOException("Network error")
 *            val data = fetchData()
 *            updateUI(data)
 *        } catch (e: IOException) {
 *            showNetworkError()
 *        }
 *    }
 *    
 *    // ✅ 올바른 방법 2: CoroutineExceptionHandler
 *    val handler = CoroutineExceptionHandler { _, exception ->
 *        when (exception) {
 *            is IOException -> showNetworkError()
 *            else -> showGenericError()
 *        }
 *    }
 *    launch(Dispatchers.IO + handler) {
 *        riskyOperation()
 *    }
 *    
 *    async:
 *    - await() 시점에 예외 발생
 *    - withContext와 유사하게 처리 가능
 *    
 *    val deferred = async(Dispatchers.IO) {
 *        if (networkError) throw IOException("Network error")
 *        fetchData()
 *    }
 *    
 *    try {
 *        val result = deferred.await()  // 여기서 예외 발생
 *        updateUI(result)
 *    } catch (e: IOException) {
 *        // ✅ await() 시점에 예외가 발생하므로 여기서 잡을 수 있음
 *        showNetworkError()
 *    }
 *    
 *    확인 포인트:
 *    1. withContext: suspend 함수 → 예외 전파 O
 *       → 호출한 곳에서 try-catch 가능
 *    
 *    2. launch: 새 코루틴 → 예외 전파 X
 *       → 내부에서 try-catch 필요
 *       → 또는 CoroutineExceptionHandler
 *    
 *    3. async: await() 시점에 예외 발생
 *       → await()를 try-catch로 감싸기
 *    
 *    4. 실무 팁: 에러 UI 표시가 필요하면
 *       withContext/async가 더 편리
 * 
 * 4. 구조화된 동시성
 *    withContext:
 *    - 부모 코루틴의 일부
 *    - 부모 취소 시 함께 취소
 *    - 부모는 자식 완료를 기다림
 *    
 *    viewModelScope.launch {  // 부모
 *        withContext(Dispatchers.IO) {  // 자식
 *            // 부모 취소 시 함께 취소됨
 *            longRunningTask()
 *        }
 *        // withContext 완료 후 실행
 *    }
 *    
 *    launch:
 *    - 독립적인 코루틴
 *    - 명시적 관계 설정 필요
 *    - 완료 대기가 기본이 아님
 *    
 *    viewModelScope.launch {  // 부모
 *        val job = launch(Dispatchers.IO) {  // 형제
 *            longRunningTask()
 *        }
 *        // job.join() 없으면 기다리지 않음
 *    }
 * 
 * === 실전 사용 케이스 ===
 * 
 * 1. withContext 사용
 *    
 *    A. 데이터 로딩 후 UI 업데이트
 *    viewModelScope.launch {
 *        _uiState.value = Loading
 *        
 *        try {
 *            val data = withContext(Dispatchers.IO) {
 *                repository.fetchData()  // 결과 필요
 *            }
 *            _uiState.value = Success(data)  // data 사용
 *        } catch (e: Exception) {
 *            _uiState.value = Error(e.message)
 *        }
 *    }
 *    
 *    B. 순차적 API 호출
 *    suspend fun getUserProfile(userId: String): Profile {
 *        // 1단계: 사용자 정보 가져오기
 *        val user = withContext(Dispatchers.IO) {
 *            userApi.getUser(userId)
 *        }
 *        
 *        // 2단계: 프로필 정보 가져오기 (user 필요)
 *        val profile = withContext(Dispatchers.IO) {
 *            profileApi.getProfile(user.profileId)
 *        }
 *        
 *        return profile
 *    }
 *    
 *    C. 파일 읽기/쓰기
 *    suspend fun saveToFile(data: String): Boolean {
 *        return withContext(Dispatchers.IO) {
 *            try {
 *                file.writeText(data)
 *                true  // 성공 여부 반환
 *            } catch (e: IOException) {
 *                false
 *            }
 *        }
 *    }
 *    
 *    D. 데이터베이스 쿼리
 *    suspend fun getUser(id: Int): User? {
 *        return withContext(Dispatchers.IO) {
 *            database.userDao().getUserById(id)  // 결과 반환
 *        }
 *    }
 * 
 * 2. launch 사용
 *    
 *    A. 분석 이벤트 로깅
 *    fun trackEvent(event: String) {
 *        viewModelScope.launch(Dispatchers.IO) {
 *            // 결과 필요 없음, Fire-and-Forget
 *            analytics.logEvent(event)
 *        }
 *    }
 *    
 *    B. 캐시 업데이트
 *    fun onDataChanged(newData: Data) {
 *        viewModelScope.launch(Dispatchers.IO) {
 *            // 백그라운드에서 캐시 업데이트
 *            cache.update(newData)
 *        }
 *    }
 *    
 *    C. 병렬 작업
 *    viewModelScope.launch {
 *        // 3개 작업을 병렬로 실행
 *        val job1 = launch(Dispatchers.IO) { fetchNews() }
 *        val job2 = launch(Dispatchers.IO) { fetchWeather() }
 *        val job3 = launch(Dispatchers.IO) { fetchStocks() }
 *        
 *        // 모두 완료 대기
 *        job1.join()
 *        job2.join()
 *        job3.join()
 *        
 *        updateUI()
 *    }
 *    
 *    D. 주기적 작업
 *    fun startPeriodicSync() {
 *        viewModelScope.launch(Dispatchers.IO) {
 *            while (isActive) {
 *                syncData()
 *                delay(60_000)  // 1분마다
 *            }
 *        }
 *    }
 * 
 * === 성능 비교 ===
 * 
 * 1. 순차 실행 (withContext)
 *    viewModelScope.launch {
 *        val result1 = withContext(Dispatchers.IO) {
 *            task1()  // 1초
 *        }
 *        val result2 = withContext(Dispatchers.IO) {
 *            task2()  // 1초
 *        }
 *        // 총 2초 소요
 *    }
 * 
 * 2. 병렬 실행 (launch)
 *    viewModelScope.launch {
 *        val job1 = launch(Dispatchers.IO) { task1() }  // 1초
 *        val job2 = launch(Dispatchers.IO) { task2() }  // 1초
 *        job1.join()
 *        job2.join()
 *        // 총 1초 소요 (병렬 실행)
 *    }
 * 
 * 3. async/await (병렬 + 결과 반환)
 *    viewModelScope.launch {
 *        val deferred1 = async(Dispatchers.IO) { task1() }
 *        val deferred2 = async(Dispatchers.IO) { task2() }
 *        
 *        val result1 = deferred1.await()  // 결과 받기
 *        val result2 = deferred2.await()  // 결과 받기
 *        // 총 1초 소요 + 결과 활용 가능
 *    }
 * 
 * === 흔한 실수 ===
 * 
 * 1. ❌ launch로 결과 받으려는 시도
 *    // 잘못된 코드
 *    fun fetchData(): User {
 *        var user: User? = null
 *        viewModelScope.launch(Dispatchers.IO) {
 *            user = api.getUser()  // Race condition!
 *        }
 *        return user!!  // NPE 위험!
 *    }
 *    
 *    ✅ 올바른 코드
 *    suspend fun fetchData(): User {
 *        return withContext(Dispatchers.IO) {
 *            api.getUser()
 *        }
 *    }
 * 
 * 2. ❌ withContext를 Fire-and-Forget으로 사용
 *    // 비효율적
 *    viewModelScope.launch {
 *        withContext(Dispatchers.IO) {
 *            analytics.log("event")  // 결과 불필요한데 기다림
 *        }
 *        // 다음 코드 실행까지 지연
 *    }
 *    
 *    ✅ 올바른 코드
 *    viewModelScope.launch(Dispatchers.IO) {
 *        analytics.log("event")  // 즉시 다음 코드 실행
 *    }
 * 
 * 3. ❌ launch 내 예외를 try-catch로 처리
 *    // 작동하지 않음
 *    try {
 *        viewModelScope.launch(Dispatchers.IO) {
 *            riskyOperation()  // 예외 발생
 *        }
 *    } catch (e: Exception) {
 *        // 여기서 catch 안 됨!
 *    }
 *    
 *    ✅ 올바른 코드 (Option 1: CoroutineExceptionHandler)
 *    val handler = CoroutineExceptionHandler { _, exception ->
 *        handleError(exception)
 *    }
 *    viewModelScope.launch(Dispatchers.IO + handler) {
 *        riskyOperation()
 *    }
 *    
 *    ✅ 올바른 코드 (Option 2: 내부에서 처리)
 *    viewModelScope.launch(Dispatchers.IO) {
 *        try {
 *            riskyOperation()
 *        } catch (e: Exception) {
 *            handleError(e)
 *        }
 *    }
 * 
 * 4. ❌ 불필요한 코루틴 생성
 *    // 비효율적
 *    suspend fun fetchData(): User {
 *        return coroutineScope {
 *            withContext(Dispatchers.IO) {
 *                api.getUser()
 *            }
 *        }
 *    }
 *    
 *    ✅ 올바른 코드
 *    suspend fun fetchData(): User {
 *        return withContext(Dispatchers.IO) {
 *            api.getUser()
 *        }
 *    }
 * 
 * === async vs launch vs withContext (상세 비교) ===
 * 
 * 1. async
 *    특징:
 *    - Deferred<T> 반환 (결과 + Job)
 *    - await()로 결과 가져오기
 *    - 병렬 실행 + 결과 반환
 *    - 예외가 await() 시점에 발생
 *    
 *    추가된 이유:
 *    - launch: 병렬 O, 결과 X
 *    - withContext: 병렬 X, 결과 O
 *    - async: 병렬 O, 결과 O ✨ (둘의 장점 결합)
 *    
 *    사용 시기:
 *    - 여러 작업을 병렬로 실행하면서 결과도 필요할 때
 *    - 여러 API를 동시에 호출하여 성능 향상
 *    
 *    확인 포인트:
 *    1. async는 Deferred<T> 반환 (결과 컨테이너)
 *    2. await()로 실제 결과값 추출
 *    3. 여러 async 동시 시작 = 병렬 실행
 *    4. 실행시간: 가장 긴 작업만큼만 소요
 *    5. 모든 결과를 모아서 처리 가능
 *    
 *    예시:
 *    suspend fun loadProfile(): Profile {
 *        return coroutineScope {
 *            // 3개 작업을 동시에 시작
 *            val user = async(Dispatchers.IO) { fetchUser() }      // 1초
 *            val posts = async(Dispatchers.IO) { fetchPosts() }    // 1초
 *            val friends = async(Dispatchers.IO) { fetchFriends() } // 1초
 *            
 *            // 모든 결과를 기다림 (총 1초만 소요)
 *            Profile(
 *                user = user.await(),     // 결과 수집
 *                posts = posts.await(),   // 결과 수집
 *                friends = friends.await() // 결과 수집
 *            )
 *        }
 *    }
 *    
 *    성능 비교:
 *    - withContext (순차): 1초 + 1초 + 1초 = 3초
 *    - async (병렬): max(1초, 1초, 1초) = 1초
 *    
 *    예외 처리:
 *    try {
 *        val deferred = async(Dispatchers.IO) { riskyOperation() }
 *        val result = deferred.await()  // 여기서 예외 발생
 *    } catch (e: Exception) {
 *        // ✅ await() 시점에 예외가 전파되므로 처리 가능
 *    }
 * 
 * 2. launch
 *    특징:
 *    - Job 반환 (결과 X)
 *    - Fire-and-Forget
 *    - 병렬 실행
 *    - 예외가 내부에 갇힘
 *    
 *    사용 시기:
 *    - 결과가 필요 없고 병렬 실행하고 싶을 때
 *    
 *    예시:
 *    launch(Dispatchers.IO) { logEvent() }
 *    launch(Dispatchers.IO) { updateCache() }
 * 
 * 3. withContext
 *    특징:
 *    - T 반환 (결과)
 *    - 순차 실행
 *    - 컨텍스트 전환
 *    - 예외가 호출자에게 전파
 *    
 *    사용 시기:
 *    - 결과가 필요하고 순차적으로 실행하고 싶을 때
 *    
 *    예시:
 *    val user = withContext(Dispatchers.IO) { fetchUser() }
 *    val posts = withContext(Dispatchers.IO) { fetchPosts(user.id) }
 * 
 * === 선택 가이드 (개선됨) ===
 * 
 * 질문 1: 결과값이 필요한가?
 * - YES → 질문 2로
 * - NO → launch
 * 
 * 질문 2: 병렬 실행이 필요한가? (결과값이 필요한 경우)
 * - YES → async/await (여러 작업 동시 실행 + 결과 수집)
 * - NO → withContext (순차 실행 + 결과 반환)
 * 
 * 질문 3: 예외 처리를 어디서 할 것인가?
 * - 호출한 곳에서 → withContext 또는 async/await
 * - 내부에서 → launch (+ try-catch 또는 Handler)
 * 
 * 질문 4: 작업 완료를 기다려야 하는가?
 * - YES + 결과 필요 + 순차 → withContext
 * - YES + 결과 필요 + 병렬 → async/await
 * - YES + 결과 불필요 → launch + join()
 * - NO → launch (Fire-and-Forget)
 * 
 * 플로우차트:
 * 
 * 결과값 필요?
 * │
 * ├─ YES → 병렬 실행 필요?
 * │        │
 * │        ├─ YES → async/await 사용
 * │        │        (여러 API 동시 호출 + 결과 수집)
 * │        │
 * │        └─ NO  → withContext 사용
 * │                 (단일 작업 + 결과 반환)
 * │
 * └─ NO  → launch 사용
 *          (Fire-and-Forget, 로깅, 분석 등)
 * 
 * === 요약 ===
 * 
 * withContext(Dispatchers.IO):
 * - 순차 실행
 * - 결과 반환
 * - 완료 대기
 * - 예외 전파
 * - 구조화된 동시성
 * 
 * 사용 예:
 * - API 호출 후 결과 사용
 * - DB 쿼리
 * - 파일 읽기/쓰기
 * - 순차적 작업
 * 
 * launch(Dispatchers.IO):
 * - 병렬 실행
 * - 결과 무시 (Job만 반환)
 * - 즉시 다음 코드 실행
 * - CoroutineExceptionHandler 필요
 * - 독립적 생명주기
 * 
 * 사용 예:
 * - 분석 로깅
 * - 캐시 업데이트
 * - 병렬 작업
 * - Fire-and-Forget
 */

object WithContextGuide {
    const val GUIDE_INFO = """
        withContext vs launch vs async/await
        
        핵심 차이:
        - withContext: 순차, 결과 반환, 완료 대기, 예외 전파
        - launch: 병렬, Fire-and-Forget, Job 반환, 예외 내부 처리
        - async/await: 병렬, 결과 수집, await() 대기, 예외 전파
        
        선택 기준:
        - 순차 + 결과 필요 → withContext
        - Fire-and-Forget → launch
        - 병렬 + 결과 필요 → async/await
        - 예외를 호출한 곳에서 처리 → withContext/async
        
        새롭게 추가된 내용:
        1. async/await 데모: 병렬 실행과 결과 수집을 모두 지원
           - launch의 병렬 실행 + withContext의 결과 반환
           - 여러 API를 동시에 호출하여 성능 향상
        
        2. 예외 처리 차이: 실무에서 매우 중요
           - withContext: 호출자에게 예외 전파 (외부 try-catch 가능)
           - launch: 내부에서만 예외 발생 (내부 처리 필요)
           - async: await() 시점에 예외 발생
        
        출처: https://proandroiddev.com/the-real-difference-between-withcontext-dispatchers-io-and-launch-dispatchers-io-b70ec00a33f2
    """
}
