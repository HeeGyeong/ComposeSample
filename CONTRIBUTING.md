# ComposeSample 기여 가이드

관심을 가져주셔서 감사합니다. 이 프로젝트는 Jetpack Compose 예제와 실무에서 마주친 이슈 해결법을 Clean Architecture 위에서 기능별로 정리한 저장소입니다.

이 문서는 프로젝트에서 사용하는 컨벤션을 요약한 것입니다. 전체 가이드는 [CLAUDE.md](CLAUDE.md)를, 문서 인덱스는 [docs/README.md](app/src/main/java/com/example/composesample/docs/README.md)를 참고하세요.

---

## 기술 스택

- **언어**: Kotlin 2.4.0
- **UI**: Jetpack Compose + Material3 (ComposeBom 2026.05.00)
- **아키텍처**: Clean Architecture + MVVM
- **DI**: Koin
- **네트워크**: Retrofit + Gson, Ktor
- **로컬 DB**: Room
- **비동기**: Coroutine + Flow
- **Target SDK**: 35 / **Min SDK**: 24 / **Java**: 21

---

## 모듈 구조

```
ComposeSample
├── app         # UI 레이어 (Compose, ViewModel, DI 모듈)
├── data        # 데이터 레이어 (Repository 구현체, API, Room)
├── domain      # 도메인 레이어 (Repository 인터페이스, UseCase, Model) — 순수 Kotlin(JVM)
├── core        # 네비게이션 인터페이스
└── coordinator # Coordinator 패턴 구현체
```

### 레이어 의존성 규칙

| 레이어 | 허용 의존성 | 금지 |
|-------|----------------------|-----------|
| domain | 없음 (순수 Kotlin) | Android 프레임워크 |
| data | domain | presentation |
| app(presentation) | domain, data | - |

이 규칙의 배경과 의도적인 교육용 예외 사항은 [ARCHITECTURE.md](ARCHITECTURE.md)를 참고하세요.

---

## 신규 예제 추가 방법

항상 아래 4단계를 순서대로 따라주세요.

### 1단계: `ConstValue.kt`에 상수 추가

```kotlin
// app/src/main/java/com/example/composesample/util/ConstValue.kt
const val NewFeatureExample = "newFeatureExample"
```

해당 섹션 주석도 함께 갱신하고, 새로운 연/월의 첫 예제를 추가하는 경우 `UpdateDate`도 갱신하세요.

### 2단계: `Examples20XX.kt`에 `ExampleObject` 추가

```kotlin
// app/.../presentation/example/list/Examples2026.kt
ExampleObject(
    lastUpdate = "26. MM. DD",   // 두 자리 연도, 예: "26. 06. 18"
    title = "예제 제목",
    description = "예제 설명",
    blogUrl = "",                // 아직 게시글이 없으므로 빈 값으로 시작
    exampleType = ConstValue.NewFeatureExample
)
```

**`blogUrl` 규칙**: 새 예제는 항상 `blogUrl = ""`으로 시작합니다. 설명 게시글이 실제로 발행되면 `list/BlogUrlHelper.kt`의 `blogUrl(postId)` 헬퍼로 교체하세요(값이 비어있지 않아야 카드에 "Explain Blog" 버튼이 표시됩니다). **원본 URL 문자열을 직접 하드코딩하지 마세요** — base URL은 `BlogUrlHelper.kt`에만 존재해야 합니다. 학습용 참고 URL은 `exampleGuide.kt`에만 작성합니다.

새로운 연도 파일(`Examples20XX.kt`)을 추가하는 경우 `ExampleObjectList.kt`에도 등록해야 합니다.

### 2-1단계: 서브카테고리 그룹인 경우

그룹으로 묶이는 예제는 **두 번째 등록 경로**를 사용합니다: 부모는 `Examples20XX.kt`에, 자식들은 `ExampleObjectList.kt`의 `subCategoryList()`에 등록합니다.

```kotlin
// 부모 (Examples20XX.kt) — subCategory와 exampleType 모두 그룹 상수를 사용
ExampleObject(subCategory = ConstValue.Shimmer, title = "...", description = "...",
    exampleType = ConstValue.Shimmer)

// 자식 (ExampleObjectList.kt, subCategoryList())
ExampleObject(subCategory = ConstValue.Shimmer, title = "...", description = "...",
    exampleType = ConstValue.UIShimmerExample)  // leaf 상수 → 4단계 라우팅 필요
```

부모의 `exampleType`은 라우팅되지 않습니다(`subCategory`가 비어있지 않으면 카드가 화면 이동 대신 자식 목록을 엽니다). 따라서 부모에 leaf 상수를 넣으면 자식 항목이 조용히 중복됩니다. 자식만 4단계 라우팅이 필요합니다. 기존 그룹: `Shimmer` / `FlingBehavior` / `BottomSheet` / `NavigationDraw` / `Compose17FeaturesExample`.

### 3단계: UI 파일 생성

```
app/.../component/{category}/{subcategory}/NewFeatureExampleUI.kt
```

- **파일명**: `*ExampleUI.kt`
- **함수 시그니처**: `fun NewFeatureExampleUI(onBackEvent: () -> Unit)`
- 참고/블로그 URL은 UI 파일이 아닌 `exampleGuide.kt`에만 작성합니다.

### 4단계: `ExampleUiRegistry.kt`에 라우팅 등록

라우팅은 **when-expression이 아니라 map 조회**입니다 — `ExampleRouter.kt`는 `ExampleMoveType`만 분기하고, 실제 타입은 `exampleUiRegistry`에서 조회합니다. 등록하지 않은 타입은 빌드 실패 대신 "Dummy" 화면으로 조용히 대체되므로 이 단계를 반드시 확인하세요.

```kotlin
// app/.../presentation/example/ExampleUiRegistry.kt
import com.example.composesample.presentation.example.component.{category}.NewFeatureExampleUI
import com.example.composesample.util.ConstValue.NewFeatureExample

val exampleUiRegistry: Map<String, @Composable (onBackEvent: () -> Unit) -> Unit> = mapOf(
    // ...
    NewFeatureExample to { onBackEvent -> NewFeatureExampleUI(onBackEvent) },
)
```

Activity 기반 예제(드묾)는 레지스트리를 건너뜁니다: `ExampleObject`에 `moveType = ExampleMoveType.ACTIVITY`를 설정하고, `ExampleRouter.kt`의 `ExampleMoveType.ACTIVITY` 분기에 `startActivity` 케이스를 추가하세요.

---

## 파일 네이밍 컨벤션

| 종류 | 규칙 | 예시 |
|------|------|---------|
| 예제 UI 파일 | `*ExampleUI.kt` | `ReboundExampleUI.kt` |
| 예제 UI 함수 | `fun *ExampleUI(onBackEvent: () -> Unit)` | `fun ReboundExampleUI(...)` |
| 참고 문서 파일 | `exampleGuide.kt` | (URL과 핵심 개념 KDoc만) |
| ViewModel | `*ViewModel.kt` | `PlayerViewModel.kt` |
| 데이터 클래스 | `*Data.kt` / `*ListData.kt` | `UserData.kt` |

---

## 코딩 규칙

- **코드 주석은 한글로 작성합니다.**
- 요청받은 범위 밖의 코드는 수정하지 않습니다(변경 범위를 최소화).
- 기존 패턴을 먼저 파악한 뒤 동일한 방식으로 구현합니다.
- 비즈니스 로직은 ViewModel에 두고, `@Composable` UI에는 넣지 않습니다.
- 최상위 기능당 ViewModel 1개를 사용합니다(하위 기능을 별도 ViewModel로 나누지 않습니다).

### 데이터 클래스

```kotlin
// 서버 필드: snake_case → 클라이언트 필드: camelCase
data class UserData(
    @SerializedName("user_id") val userId: String,
    @SerializedName("is_active") val isActive: Boolean = false,
    val items: List<String> = emptyList()
)
```

### DI (Koin)

- API 의존성에는 항상 `named()` qualifier를 사용합니다.
- `single`: 전역 싱글톤 / `factory`: 호출마다 새 인스턴스 / `viewModel`: 생명주기 인식.

자세한 내용은 [DataRules.md](app/src/main/java/com/example/composesample/docs/data/DataRules.md), [DIRules.md](app/src/main/java/com/example/composesample/docs/di/DIRules.md), [UIRules.md](app/src/main/java/com/example/composesample/docs/ui/UIRules.md)를 참고하세요.

---

## 빌드 및 검증

커밋 전에는 항상 빌드를 확인하세요.

```bash
./gradlew assembleDebug
```

컴파일 오류가 발생하면 수정 후 성공할 때까지 다시 빌드합니다. `main`에 대한 모든 push/PR에서 CI가 `assembleDebug`와 `testDebugUnitTest`를 실행합니다([`.github/workflows/android-build.yml`](.github/workflows/android-build.yml) 참고).

---

## 커밋 메시지 컨벤션

- 커밋 메시지는 **한글**로 작성합니다.
- 접두사: `feat:` / `fix:` / `refactor:` / `chore:` / `docs:`
- 예시: `feat: Rebound 리컴포지션 모니터링 예제 추가`

---

## Pull Request 체크리스트

PR을 올리기 전에 아래를 확인하세요.

- [ ] `./gradlew assembleDebug`가 성공합니다.
- [ ] 신규 예제라면 4단계(ConstValue → Examples20XX → UI 파일 → Router)를 모두 진행했습니다.
- [ ] UI 함수가 `*ExampleUI(onBackEvent: () -> Unit)` 패턴을 따릅니다.
- [ ] 참고 URL은 `exampleGuide.kt`에만 작성했습니다.
- [ ] 레이어 의존성 규칙을 지켰습니다.
- [ ] 코드 주석은 한글로 작성했고, 커밋 메시지는 위 컨벤션을 따릅니다.
