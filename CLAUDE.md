# CLAUDE.md — ComposeSample 프로젝트 가이드

Claude Code가 이 프로젝트에서 일관된 코드를 작성할 수 있도록 돕는 가이드입니다.

---

## 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose + Material3
- **아키텍처**: Clean Architecture + MVVM
- **DI**: Koin
- **네트워크**: Retrofit + Gson, Ktor
- **로컬 DB**: Room
- **비동기**: Coroutine + Flow
- **Target SDK**: 35 / Min SDK: 24
- **Kotlin**: 2.1.0 / **ComposeBom**: 2025.01.00

---

## 모듈 구조

```
ComposeSample
├── app        # UI 레이어 (Compose, ViewModel, DI 모듈)
├── data       # 데이터 레이어 (Repository 구현, API, Room)
├── domain     # 도메인 레이어 (Repository 인터페이스, UseCase, Model) — 순수 Kotlin
├── Core       # Navigation 인터페이스
└── Coordinator # Coordinator 패턴 구현
```

`app` 내 presentation 패키지 구조:
```
presentation/example/
├── component/
│   ├── ui/           # UI 컴포넌트 & 레이아웃
│   ├── interaction/  # 제스처 & 클릭 이벤트
│   ├── navigation/   # 내비게이션
│   ├── data/         # 네트워크 & 캐시
│   ├── system/       # 시스템 통합 (플랫폼, 미디어, 백그라운드)
│   └── architecture/ # 아키텍처 패턴 & 개발 도구
├── list/             # ExampleObject 목록 정의
├── ExampleRouter.kt  # 예제 라우팅 (when-expression)
└── ExampleObjectList.kt # 전체 예제 목록 통합
```

---

## 새 예제 추가 방법

예제를 추가할 때는 반드시 아래 순서를 따릅니다.

### 1단계: `ConstValue.kt`에 상수 추가
```kotlin
// app/src/main/java/com/example/composesample/util/ConstValue.kt
const val NewFeatureExample = "newFeatureExample"
```
상수 추가 시 해당 섹션의 주석도 함께 업데이트합니다:
```kotlin
// ==================== 테스트 예제 ====================
// Test(UI 테스트 TDD), RecompositionTest(...), NewFeature(새 기능 설명)  ← 추가
const val NewFeatureExample = "newFeatureExample"
```

### 2단계: `Examples20XX.kt`에 ExampleObject 추가
```kotlin
// app/.../presentation/example/list/Examples2026.kt
ExampleObject(
    lastUpdate = "26. MM. DD",  // 두 자리 연도, 예: "26. 03. 22"
    title = "예제 제목",
    description = "예제 설명",
    blogUrl = "",  // 블로그 링크는 exampleGuide.kt에만 기재, 여기는 반드시 빈 문자열
    exampleType = ConstValue.NewFeatureExample
)
```

새로운 연도/월에 처음 예제를 추가할 경우 `ConstValue.kt`의 `UpdateDate`도 갱신합니다:
```kotlin
const val UpdateDate = "26년 3월"  // 현재 월로 업데이트
```

`ExampleObjectList.kt`에서 연도별 파일을 통합하므로 새 연도 파일(`Examples20XX.kt`) 추가 시 해당 파일에도 등록합니다.

### 3단계: UI 파일 생성
```
app/.../component/{카테고리}/{subcategory}/NewFeatureExampleUI.kt
```
- **파일명**: `*ExampleUI.kt`
- **함수명**: `fun NewFeatureExampleUI(onBackEvent: () -> Unit)`
- **참고 URL은 `exampleGuide.kt`에만** (블로그/공식 링크를 UI 파일에 직접 포함하지 않음)

```kotlin
// exampleGuide.kt 예시
/**
 * NewFeature Example 참고 자료
 * - 공식 문서: https://...
 */
```

### 4단계: `ExampleRouter.kt`에 라우팅 추가
```kotlin
// import 추가
import com.example.composesample.presentation.example.component.{카테고리}.NewFeatureExampleUI
import com.example.composesample.util.ConstValue.NewFeatureExample

// when-expression에 추가
NewFeatureExample -> {
    NewFeatureExampleUI(onBackEvent)
}
```

---

## 파일 네이밍 컨벤션

| 종류 | 규칙 | 예시 |
|------|------|------|
| 예제 UI 파일 | `*ExampleUI.kt` | `ReboundExampleUI.kt` |
| 예제 UI 함수 | `fun *ExampleUI(onBackEvent: () -> Unit)` | `fun ReboundExampleUI(...)` |
| 참고 자료 파일 | `exampleGuide.kt` | (URL, 핵심 개념 KDoc만) |
| ViewModel | `*ViewModel.kt` | `PlayerViewModel.kt` |
| Data 클래스 | `*Data.kt` / `*ListData.kt` | `UserData.kt` |

---

## 빌드 환경 제약

- `gradlew.bat`만 존재 (Unix `gradlew` 스크립트 없음)
- Java 17 툴체인 필요 → **CLI 빌드 불가, Android Studio에서만 빌드 가능**
- 커맨드라인 빌드 검증 없이 커밋/푸시 진행 (사용자 승인 필요 시 확인)

---

## 커밋 메시지 컨벤션

- **한국어** 사용
- 접두어: `feat:` / `fix:` / `refactor:` / `chore:` / `docs:`
- 예: `feat: Rebound 리컴포지션 모니터링 예제 추가`

---

## 코딩 규칙

### 공통
- **주석은 한국어**로 작성
- 요청하지 않은 코드는 수정하지 않음 (범위 최소화)
- 기존 패턴을 먼저 파악한 후 동일한 방식으로 구현

### Data 클래스
```kotlin
// 서버 필드: snake_case → 클라이언트 필드: camelCase
data class UserData(
    @SerializedName("user_id") val userId: String,
    @SerializedName("is_active") val isActive: Boolean = false,
    val items: List<String> = emptyList()
)

// Android 컴포넌트 간 전달 시
@Parcelize
data class EntityData(
    @SerializedName("entity_id") val entityId: String
) : Parcelable
```

### ViewModel / UI 분리
```kotlin
// ❌ UI에 비즈니스 로직 포함 금지
@Composable
fun Screen() {
    Button(onClick = { fetchData() }) { Text("Load") }
}

// ✅ ViewModel에 위임
class FeatureViewModel : ViewModel() {
    fun fetchData() { ... }
}

@Composable
fun Screen(viewModel: FeatureViewModel) {
    Button(onClick = { viewModel.fetchData() }) { Text("Load") }
}
```

### ViewModel 범위 규칙
- 상위 기능(예: Live) 단위로 하나의 ViewModel 사용
- 하위 기능(Streaming, Chat, Settings)을 별도 ViewModel로 분리하지 않음

---

## DI (Koin)

```kotlin
// InjectModules.kt — 모듈 통합
val KoinModules = listOf(apiModule, viewModelModule, networkModule, ktorModule)

// ApiModule.kt
single<Retrofit>(named("DomainName")) {
    Retrofit.Builder().baseUrl("url").client(get()).build()
}

// ViewModelModule.kt
viewModel { FeatureViewModel(api = get(named("ApiName"))) }
```

- API 의존성에는 반드시 `named()` 한정자 사용
- `single`: 전역 싱글톤 / `factory`: 매번 새 인스턴스 / `viewModel`: 생명주기 인식

---

## 레이어 규칙

| 레이어 | 의존 허용 | 금지 |
|--------|-----------|------|
| domain | 없음 (순수 Kotlin) | Android 프레임워크 |
| data | domain | presentation |
| app(presentation) | domain, data | - |

---

## 상세 규칙 문서 위치

더 자세한 내용은 아래 문서를 참조하세요:

- **Data 클래스**: `app/src/main/java/com/example/composesample/docs/data/DataRules.md`
- **DI (Koin)**: `app/src/main/java/com/example/composesample/docs/di/DIRules.md`
- **UI 규칙**: `app/src/main/java/com/example/composesample/docs/ui/UIRules.md`
- **API 생성 가이드**: `app/src/main/java/com/example/composesample/docs/prompt/CreateAPIGuide.md`
- **API+UI 바인딩**: `app/src/main/java/com/example/composesample/docs/prompt/CreateAPIAndUIBindingGuide.md`
- **UI 코드 생성**: `app/src/main/java/com/example/composesample/docs/prompt/CreateUICodeSnippet.md`
