package com.example.composesample.presentation.example.component.architecture.modularization

/**
 * Pragmatic Modularization: The Case for Wiring Modules Guide
 * 
 * 실용적인 모듈화 전략과 Wiring Modules 패턴
 * 
 * 출처: https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611
 * 
 * === ModularizationExampleUI에서 다루는 내용 ===
 * 
 * 이 예제는 모듈화 전략을 시각적으로 보여줍니다:
 * 
 * 주요 데모:
 * - Wiring Module: 모듈 간 의존성 연결
 * - Feature Module: 독립적인 기능 모듈
 * - Module 통신: 인터페이스 기반 통신
 * - DI Integration: 의존성 주입과 통합
 * 
 * === 모듈화란? ===
 * 
 * 1. 모듈화의 필요성
 *    문제점:
 *    - 모놀리식 앱: 빌드 시간 증가
 *    - 강한 결합: 변경 영향 범위 불명확
 *    - 낮은 재사용성: 코드 중복
 *    - 팀 협업 어려움: 충돌 빈번
 *    
 *    해결:
 *    - 기능별 모듈 분리
 *    - 명확한 경계와 인터페이스
 *    - 병렬 빌드로 속도 향상
 *    - 독립적인 개발 가능
 * 
 * 2. 모듈 타입
 *    App Module:
 *    - 앱의 진입점
 *    - 모든 모듈 통합
 *    - Application, Activity 포함
 *    
 *    Feature Module:
 *    - 특정 기능 구현
 *    - UI, ViewModel, UseCase
 *    - 다른 Feature에 의존하지 않음
 *    
 *    Core Module:
 *    - 공통 유틸리티
 *    - 네트워크, DB 클라이언트
 *    - 모든 모듈이 사용
 *    
 *    Wiring Module:
 *    - 모듈 간 연결
 *    - 의존성 해결
 *    - DI 설정
 * 
 * === Wiring Module의 필요성 ===
 * 
 * 1. 문제 상황
 *    Feature A가 Feature B의 기능 필요:
 *    
 *    ❌ 잘못된 접근:
 *    // :feature:a/build.gradle
 *    dependencies {
 *        implementation(project(":feature:b"))
 *    }
 *    
 *    문제점:
 *    - Feature A가 Feature B에 강하게 결합
 *    - Feature B 변경 시 Feature A 영향
 *    - 순환 의존성 발생 가능
 *    - 모듈 재사용 불가
 * 
 * 2. Wiring Module 접근
 *    ✅ 올바른 접근:
 *    
 *    // :feature:a/build.gradle
 *    dependencies {
 *        implementation(project(":feature:a-api"))
 *    }
 *    
 *    // :feature:b/build.gradle
 *    dependencies {
 *        implementation(project(":feature:b-api"))
 *    }
 *    
 *    // :wiring/build.gradle
 *    dependencies {
 *        implementation(project(":feature:a"))
 *        implementation(project(":feature:b"))
 *    }
 *    
 *    장점:
 *    - Feature 간 직접 의존 없음
 *    - API를 통한 간접 통신
 *    - Wiring Module이 연결 담당
 *    - 독립적인 테스트 가능
 * 
 * === Wiring Module 패턴 ===
 * 
 * 1. API Module
 *    Feature의 공개 인터페이스:
 *    
 *    // :feature:profile-api
 *    interface ProfileNavigator {
 *        fun navigateToProfile(userId: String)
 *    }
 *    
 *    interface ProfileRepository {
 *        suspend fun getProfile(userId: String): Profile
 *    }
 *    
 *    특징:
 *    - 인터페이스만 포함
 *    - 구현체 없음
 *    - 가벼운 의존성
 * 
 * 2. Feature Module
 *    실제 구현:
 *    
 *    // :feature:profile
 *    class ProfileNavigatorImpl : ProfileNavigator {
 *        override fun navigateToProfile(userId: String) {
 *            // 실제 네비게이션 로직
 *        }
 *    }
 *    
 *    class ProfileRepositoryImpl : ProfileRepository {
 *        override suspend fun getProfile(userId: String): Profile {
 *            // 실제 데이터 로직
 *        }
 *    }
 * 
 * 3. Wiring Module
 *    의존성 연결:
 *    
 *    // :wiring
 *    @Module
 *    class WiringModule {
 *        @Provides
 *        fun provideProfileNavigator(
 *            impl: ProfileNavigatorImpl
 *        ): ProfileNavigator = impl
 *        
 *        @Provides
 *        fun provideProfileRepository(
 *            impl: ProfileRepositoryImpl
 *        ): ProfileRepository = impl
 *    }
 * 
 * === 실전 구조 ===
 * 
 * 1. 프로젝트 구조
 *    app/
 *    ├── :app                        (앱 진입점)
 *    │   └── dependencies:
 *    │       ├── :wiring
 *    │       └── :feature:*
 *    │
 *    ├── :wiring                     (모듈 연결)
 *    │   └── dependencies:
 *    │       ├── :feature:home
 *    │       ├── :feature:profile
 *    │       └── :feature:settings
 *    │
 *    ├── :feature:home               (홈 기능)
 *    │   └── dependencies:
 *    │       ├── :feature:home-api
 *    │       ├── :feature:profile-api
 *    │       └── :core
 *    │
 *    ├── :feature:home-api           (홈 API)
 *    │
 *    ├── :feature:profile            (프로필 기능)
 *    │   └── dependencies:
 *    │       ├── :feature:profile-api
 *    │       └── :core
 *    │
 *    ├── :feature:profile-api        (프로필 API)
 *    │
 *    └── :core                       (공통 코드)
 * 
 * 2. 의존성 흐름
 *    :app → :wiring → :feature:* → :feature:*-api → :core
 *    
 *    특징:
 *    - 단방향 의존성
 *    - Feature 간 직접 의존 없음
 *    - API를 통한 간접 통신
 * 
 * === 구현 예제 ===
 * 
 * 1. Profile API 정의
 *    // :feature:profile-api/ProfileApi.kt
 *    interface ProfileApi {
 *        fun navigateToProfile(userId: String)
 *        suspend fun getProfileName(userId: String): String
 *    }
 * 
 * 2. Profile Feature 구현
 *    // :feature:profile/ProfileApiImpl.kt
 *    class ProfileApiImpl(
 *        private val navController: NavController,
 *        private val repository: ProfileRepository
 *    ) : ProfileApi {
 *        override fun navigateToProfile(userId: String) {
 *            navController.navigate("profile/$userId")
 *        }
 *        
 *        override suspend fun getProfileName(userId: String): String {
 *            return repository.getProfile(userId).name
 *        }
 *    }
 * 
 * 3. Home Feature에서 사용
 *    // :feature:home/HomeViewModel.kt
 *    class HomeViewModel(
 *        private val profileApi: ProfileApi  // API만 의존
 *    ) : ViewModel() {
 *        fun onUserClick(userId: String) {
 *            profileApi.navigateToProfile(userId)
 *        }
 *        
 *        suspend fun loadUserName(userId: String): String {
 *            return profileApi.getProfileName(userId)
 *        }
 *    }
 * 
 * 4. Wiring Module에서 연결
 *    // :wiring/ProfileWiring.kt
 *    @Module
 *    @InstallIn(SingletonComponent::class)
 *    object ProfileWiring {
 *        @Provides
 *        @Singleton
 *        fun provideProfileApi(
 *            navController: NavController,
 *            repository: ProfileRepository
 *        ): ProfileApi = ProfileApiImpl(navController, repository)
 *    }
 * 
 * === DI 통합 (Hilt/Koin) ===
 * 
 * 1. Hilt 방식
 *    // :feature:profile-api
 *    interface ProfileApi {
 *        fun navigate()
 *    }
 *    
 *    // :feature:profile
 *    class ProfileApiImpl @Inject constructor() : ProfileApi {
 *        override fun navigate() { }
 *    }
 *    
 *    // :wiring
 *    @Module
 *    @InstallIn(SingletonComponent::class)
 *    interface ProfileModule {
 *        @Binds
 *        fun bindProfileApi(impl: ProfileApiImpl): ProfileApi
 *    }
 * 
 * 2. Koin 방식
 *    // :wiring
 *    val wiringModule = module {
 *        single<ProfileApi> {
 *            ProfileApiImpl(
 *                navController = get(),
 *                repository = get()
 *            )
 *        }
 *    }
 * 
 * === 장점 ===
 * 
 * 1. 느슨한 결합 (Loose Coupling)
 *    - Feature 간 직접 의존 제거
 *    - API를 통한 추상화
 *    - 구현 변경 시 영향 최소화
 * 
 * 2. 높은 응집도 (High Cohesion)
 *    - 관련 기능이 모듈에 집중
 *    - 명확한 책임 분리
 *    - 코드 이해 용이
 * 
 * 3. 테스트 용이성
 *    - Feature 독립 테스트
 *    - Mock API로 테스트
 *    - 통합 테스트 간소화
 * 
 * 4. 빌드 성능
 *    - 병렬 빌드 가능
 *    - 변경 영향 범위 제한
 *    - 증분 빌드 효과적
 * 
 * 5. 팀 협업
 *    - 모듈별 담당 명확
 *    - 충돌 감소
 *    - 코드 리뷰 범위 축소
 * 
 * === 단점과 트레이드오프 ===
 * 
 * 1. 초기 복잡도
 *    - 모듈 구조 설계 필요
 *    - DI 설정 증가
 *    - 학습 곡선
 *    
 *    해결:
 *    - 점진적 모듈화
 *    - 템플릿 활용
 *    - 문서화
 * 
 * 2. 보일러플레이트
 *    - API 인터페이스 정의
 *    - Impl 클래스 생성
 *    - Wiring 코드 작성
 *    
 *    해결:
 *    - 코드 제너레이션
 *    - IDE 템플릿
 *    - Convention Plugin
 * 
 * 3. 작은 프로젝트 오버헤드
 *    - 소규모 앱에는 과함
 *    - 초기 개발 속도 저하
 *    
 *    해결:
 *    - 프로젝트 규모 고려
 *    - 필요 시점에 도입
 *    - 핵심 Feature만 분리
 * 
 * === 모듈화 전략 ===
 * 
 * 1. 언제 모듈화할까?
 *    모듈화 고려 시점:
 *    ✓ 팀 규모 3명 이상
 *    ✓ Feature가 5개 이상
 *    ✓ 빌드 시간 5분 이상
 *    ✓ 코드베이스 10만 줄 이상
 *    ✓ 여러 앱 간 코드 공유
 *    
 *    모듈화 불필요:
 *    ✗ 1-2명 팀
 *    ✗ 단순한 앱
 *    ✗ 프로토타입
 *    ✗ 빠른 출시 필요
 * 
 * 2. 점진적 모듈화
 *    Step 1: Core 분리
 *    - 공통 유틸리티
 *    - 네트워크, DB
 *    
 *    Step 2: 첫 Feature 분리
 *    - 가장 독립적인 Feature
 *    - API 패턴 확립
 *    
 *    Step 3: 다른 Feature 분리
 *    - 패턴 반복 적용
 *    - Wiring Module 구축
 *    
 *    Step 4: 최적화
 *    - 의존성 정리
 *    - 빌드 성능 튜닝
 * 
 * 3. 모듈 크기 가이드
 *    너무 작음 (< 500 줄):
 *    - 관리 오버헤드
 *    - 과도한 분리
 *    
 *    적정 크기 (500-5000 줄):
 *    - 이해 가능한 범위
 *    - 빌드 성능 균형
 *    
 *    너무 큼 (> 5000 줄):
 *    - 추가 분리 고려
 *    - Sub-Feature 분리
 * 
 * === 실전 패턴 ===
 * 
 * 1. Navigation 통합
 *    // :feature:home-api
 *    interface HomeNavigator {
 *        fun navigateToHome()
 *    }
 *    
 *    // :wiring
 *    class HomeNavigatorImpl(
 *        private val navController: NavController
 *    ) : HomeNavigator {
 *        override fun navigateToHome() {
 *            navController.navigate("home")
 *        }
 *    }
 * 
 * 2. Data 공유
 *    // :feature:user-api
 *    interface UserProvider {
 *        suspend fun getCurrentUser(): User
 *    }
 *    
 *    // :feature:profile
 *    class ProfileViewModel(
 *        private val userProvider: UserProvider
 *    ) : ViewModel() {
 *        suspend fun loadProfile() {
 *            val user = userProvider.getCurrentUser()
 *            // use user
 *        }
 *    }
 * 
 * 3. Event 통신
 *    // :feature:cart-api
 *    interface CartEventPublisher {
 *        fun onItemAdded(itemId: String)
 *    }
 *    
 *    // :feature:home
 *    class HomeViewModel(
 *        private val cartPublisher: CartEventPublisher
 *    ) : ViewModel() {
 *        fun addToCart(itemId: String) {
 *            cartPublisher.onItemAdded(itemId)
 *        }
 *    }
 * 
 * === Convention Plugin ===
 * 
 * 1. 반복 설정 자동화
 *    // buildSrc/FeaturePlugin.kt
 *    class FeaturePlugin : Plugin<Project> {
 *        override fun apply(target: Project) {
 *            target.run {
 *                plugins.apply("com.android.library")
 *                plugins.apply("kotlin-android")
 *                plugins.apply("kotlin-kapt")
 *                
 *                dependencies {
 *                    add("implementation", project(":core"))
 *                    add("implementation", "androidx.lifecycle:...")
 *                }
 *            }
 *        }
 *    }
 *    
 *    // :feature:home/build.gradle.kts
 *    plugins {
 *        id("feature-plugin")
 *    }
 * 
 * 2. 모듈 템플릿
 *    - Android Studio Template
 *    - Script로 자동 생성
 *    - 일관된 구조 유지
 * 
 * === 요약 ===
 * 
 * Wiring Module 패턴:
 * - Feature 간 직접 의존 제거
 * - API를 통한 간접 통신
 * - Wiring Module이 연결 담당
 * - DI와 자연스러운 통합
 * 
 * 핵심 원칙:
 * - 느슨한 결합
 * - 높은 응집도
 * - 단방향 의존성
 * - 명확한 경계
 * 
 * 적용 시기:
 * - 중대형 프로젝트
 * - 다인 팀
 * - 장기 유지보수
 * - 코드 재사용 필요
 */

object ModularizationGuide {
    const val GUIDE_INFO = """
        Pragmatic Modularization
        
        핵심 패턴:
        - Wiring Module: 모듈 간 연결
        - API Module: 공개 인터페이스
        - Feature Module: 실제 구현
        - 단방향 의존성
        
        장점:
        - 느슨한 결합
        - 독립적 테스트
        - 빠른 빌드
        - 팀 협업 용이
        
        출처: https://proandroiddev.com/pragmatic-modularization-the-case-for-wiring-modules-c936d3af3611
    """
}
