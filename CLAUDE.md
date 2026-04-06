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

## 빌드 환경

- `gradlew` (Unix) 및 `gradlew.bat` (Windows) 모두 존재
- Java 21 툴체인 사용 (`gradle/libs.versions.toml`의 `javaVersion = "21"`)
- Android SDK 경로: `local.properties`의 `sdk.dir` 참조
- CLI 빌드 명령: `./gradlew assembleDebug`

---

## 사전작업 규칙

사용자가 **"사전작업"** 이라고 입력하면 아래 절차를 수행합니다:

1. **프로젝트 전체 파악**
   - `ConstValue.kt` — 등록된 예제 상수 전체 확인
   - `Examples20XX.kt` 파일들 — 최근 추가된 예제 확인
   - `ExampleRouter.kt` — 라우팅 누락 항목 확인
   - 주요 소스 파일 — deprecated API, dead code, 아키텍처 위반 탐색

2. **개선 항목 분류**
   - 🔴 높음: 빌드 오류 / 아키텍처 위반 / deprecated API
   - 🟡 중간: 에러 처리 누락 / 코드 패턴 불일치
   - 🟢 낮음: 네이밍 / import 정리 / dead code

3. **추가 예정 예제 확인**
   - 메모리의 `project_pending_examples.md` 기준으로 대기 중인 예제 현황 파악

4. **메모리 업데이트**
   - `project_code_improvements.md` — 새로 발견된 개선 항목 추가, 완료 항목 이동
   - `project_pending_examples.md` — 완료된 예제 제거, 새 후보 추가
   - `MEMORY.md` 인덱스 동기화

5. **결과 보고**
   - 우선순위별 개선 항목 표로 정리
   - 즉시 진행 가능한 작업과 제약 있는 작업 구분하여 제시

---

## 작업 후 빌드 검증 규칙

코드 추가/수정/개선 작업이 완료된 후에는 **반드시** 아래 절차를 따릅니다:

1. `./gradlew assembleDebug` 빌드 실행
2. 컴파일 에러 발생 시 즉시 원인 분석 후 수정
3. 빌드 성공할 때까지 1-2 반복
4. 빌드 성공 확인 후 커밋 진행
5. **커밋 완료 즉시 `git push origin main` 실행** (커밋과 푸시는 항상 함께)

작업이 끝난 후에는 **반드시** 아래 형식의 프로세스 검증 결과 표를 출력합니다:

```
| 단계 | 내용 | 결과 |
|------|------|------|
| 1. 코드 수정 | 변경 파일 및 항목 요약 | 완료 |
| 2. 빌드 실행 | ./gradlew assembleDebug | ✅ 성공 or ❌ 실패 |
| 3. 에러 수정 | 발생한 에러 및 수정 내용 (없으면 생략) | 완료 |
| 4. 재빌드   | ./gradlew assembleDebug | ✅ 성공 (없으면 생략) |
| 5. 커밋     | 커밋 해시 | 완료 |
| 6. 푸시     | git push origin main | 완료 |
```

- 빌드가 1회에 성공하면 3·4단계는 생략
- 에러 수정·재빌드가 여러 번 반복된 경우 해당 횟수만큼 행을 추가

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
- **Claude Code 활용**: `app/src/main/java/com/example/composesample/docs/claudecode/ClaudeCodeGuide.md`
