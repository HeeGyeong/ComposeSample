# 의존성 주입 아키텍처 문서

## 목차
- [핵심 사양](#핵심-사양)
- [모듈 사양](#모듈-사양)
- [구현 규칙](#구현-규칙)
- [사용 예시](#사용-예시)
- [검증 규칙](#검증-규칙)
- [테스트 전략](#테스트-전략)
- [성능 모니터링](#성능-모니터링)

## 핵심 사양

### 프레임워크 구성
```
DI_FRAMEWORK: Koin
SYNTAX: Koin DSL
```

## 모듈 사양

### 1. 진입점 모듈 (`InjectModules.kt`)
```kotlin
// 패턴: 모듈 통합
val KoinModules = listOf(
    apiModule,
    viewModelModule,
    networkModule,
    ktorModule
)
```

### 2. API 모듈 (`ApiModule.kt`)
```kotlin
// 패턴: Retrofit 구성
구성 요소:
- HTTP 클라이언트 설정
- API 인터페이스 구현
- 인터셉터 구성

// 구현
single<Retrofit>(named("DomainName")) {
    Retrofit.Builder()
        .baseUrl("DomainUrl")
        .client(get())
        .addConverterFactory(get<GsonConverterFactory>())
        .build()
}

single<APIInterface>(named("InterfaceName")) {
    get<Retrofit>(named(DomainName)).create(APIInterface::class.java)
}
```

### 3. ViewModel 모듈 (`ViewModelModule.kt`)
```kotlin
// 패턴: ViewModel 의존성 제공
규칙:
- 선언당 하나의 ViewModel
- API 의존성에는 named 한정자 사용
- SavedStateHandle은 parametersOf로 전달

// 구현
viewModel { 
    SampleViewModel(
        api = get(named("ApiName")),
        state = get()
    )
}
```

### 4. 네트워크 모듈 (`NetworkModule.kt`)
```kotlin
// 패턴: 네트워크 유틸리티 제공
기능:
- 네트워크 상태 모니터링
- 인터셉터 관리
- 유틸리티 함수

// 구현 방법
클래스 수준: private val network: NetworkUtil by inject()
Compose 수준: val network: NetworkUtil = get()
```

## 구현 규칙

### 1. 초기화
```kotlin
// 필수: Application 클래스에서 초기화
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Application)
            modules(communityKoinModules)
        }
    }
}
```

### 2. 의존성 주입 규칙
```
네이밍 규칙:
- API 의존성: named 한정자 반드시 사용
- ViewModel: 파스칼 케이스 네이밍

스코프 규칙:
- single: 전역 싱글톤
- factory: 주입할 때마다 새 인스턴스
- viewModel: 생명주기 인식 인스턴스
```

### 3. 모범 사례
```
필수 사례:
1. API 의존성:
   - named 한정자를 반드시 사용
   - base URL을 반드시 명시
   - 에러 처리를 반드시 포함

2. ViewModel 의존성:
   - 필요 시 SavedStateHandle을 반드시 처리
   - 생명주기 인식을 반드시 준수
   - 클린 아키텍처를 구현할 것을 권장

3. 네트워크 유틸리티:
   - 네트워크 상태를 반드시 모니터링
   - 연결 변경을 반드시 처리
   - 에러 처리를 반드시 구현
```

### 4. 에러 처리
```
에러 처리 규칙:
1. 네트워크 에러:
   - NetworkStatusLiveData로 모니터링
   - 연결 타임아웃 처리
   - 재시도 로직 구현

2. 의존성 에러:
   - 의미 있는 에러 메시지 제공
   - 순환 의존성 처리
   - 폴백 메커니즘 구현
```

## 사용 예시

### 1. API 의존성
```kotlin
// 패턴: API 의존성을 가진 ViewModel
// Koin은 모듈에서 named() 한정자를 사용 — @Named 애노테이션은 사용하지 않음
class SampleViewModel(
    private val api: ApiInterface  // ViewModelModule에서 named()로 주입
) : ViewModel()

// ViewModelModule.kt
viewModel { SampleViewModel(api = get(named("apiName"))) }
```

### 2. 전역 상태 사용
```kotlin
// 패턴: 전역 상태 접근
class SampleComponent {
    private val globalState: GlobalState by inject()
}
```

### 3. 네트워크 유틸리티
```kotlin
// 패턴: 네트워크 유틸리티 사용
class NetworkAwareComponent {
    private val networkUtil: NetworkUtil by inject()
    private val networkViewModel: NetworkViewModel by viewModel()
}
```

## 검증 규칙

1. **의존성 선언**
   - Koin DSL을 반드시 사용
   - 네이밍 컨벤션을 반드시 준수
   - 적절한 스코프를 반드시 명시

2. **모듈 구성**
   - 관심사를 반드시 분리
   - 단일 책임을 반드시 유지
   - 의존성을 반드시 문서화

3. **테스트 고려사항**
   - 의존성 모킹을 반드시 허용
   - 테스트 구성을 반드시 지원
   - 단위 테스트 포함을 권장

## 테스트 전략

- **의존성 모킹**: 단위 테스트에서 MockK나 Mockito 같은 라이브러리로 의존성을 모킹합니다.
- **통합 테스트**: 애플리케이션 컨텍스트에서 의존성이 올바르게 연결되었는지 검증하는 통합 테스트를 구성합니다.

## 성능 모니터링

- **시작 시간**: Android Profiler로 DI가 앱 시작 시간에 미치는 영향을 측정합니다.
- **메모리 사용량**: DI가 메모리 누수나 과도한 오버헤드를 유발하지 않는지 메모리 사용량을 모니터링합니다.
