# CLAUDE.md — ComposeSample Project Guide

A guide that helps Claude Code write consistent code in this project.

---

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: Clean Architecture + MVVM
- **DI**: Koin
- **Network**: Retrofit + Gson, Ktor
- **Local DB**: Room
- **Async**: Coroutine + Flow
- **Target SDK**: 35 / Min SDK: 24
- **Kotlin**: 2.4.0 / **ComposeBom**: 2026.05.00

---

## Module Structure

```
ComposeSample
├── app        # UI layer (Compose, ViewModel, DI modules)
├── data       # Data layer (Repository implementations, API, Room)
├── domain     # Domain layer (Repository interfaces, UseCase, Model) — pure Kotlin
├── Core       # Navigation interface
└── Coordinator # Coordinator pattern implementation
```

`presentation` package structure inside `app`:
```
presentation/example/
├── component/
│   ├── ui/           # UI components & layout
│   ├── interaction/  # Gestures & click events
│   ├── navigation/   # Navigation
│   ├── data/         # Network & cache
│   ├── system/       # System integration (platform, media, background)
│   └── architecture/ # Architecture patterns & dev tools
├── list/             # ExampleObject list definitions
├── ExampleRouter.kt  # Example routing (when-expression)
└── ExampleObjectList.kt # Aggregation of the full example list
```

---

## How to Add a New Example

Always follow the steps below when adding an example.

### Step 1: Add a constant to `ConstValue.kt`
```kotlin
// app/src/main/java/com/example/composesample/util/ConstValue.kt
const val NewFeatureExample = "newFeatureExample"
```
When adding a constant, also update the comment for that section:
```kotlin
// ==================== Test examples ====================
// Test(UI test TDD), RecompositionTest(...), NewFeature(new feature description)  ← added
const val NewFeatureExample = "newFeatureExample"
```

### Step 2: Add an ExampleObject to `Examples20XX.kt`
```kotlin
// app/.../presentation/example/list/Examples2026.kt
ExampleObject(
    lastUpdate = "26. MM. DD",  // two-digit year, e.g. "26. 03. 22"
    title = "Example title",
    description = "Example description",
    blogUrl = "",  // Blog links belong only in exampleGuide.kt; this must always be an empty string
    exampleType = ConstValue.NewFeatureExample
)
```

When adding the first example of a new year/month, also update `UpdateDate` in `ConstValue.kt`:
```kotlin
const val UpdateDate = "26년 3월"  // update to the current month
```

`ExampleObjectList.kt` aggregates the per-year files, so when adding a new year file (`Examples20XX.kt`), register it there as well.

### Step 3: Create the UI file
```
app/.../component/{category}/{subcategory}/NewFeatureExampleUI.kt
```
- **File name**: `*ExampleUI.kt`
- **Function name**: `fun NewFeatureExampleUI(onBackEvent: () -> Unit)`
- **Reference URLs only in `exampleGuide.kt`** (do not put blog/official links directly in the UI file)

```kotlin
// exampleGuide.kt example
/**
 * NewFeature Example reference
 * - Official docs: https://...
 */
```

### Step 4: Add routing to `ExampleRouter.kt`
```kotlin
// add import
import com.example.composesample.presentation.example.component.{category}.NewFeatureExampleUI
import com.example.composesample.util.ConstValue.NewFeatureExample

// add to the when-expression
NewFeatureExample -> {
    NewFeatureExampleUI(onBackEvent)
}
```

---

## File Naming Conventions

| Type | Rule | Example |
|------|------|---------|
| Example UI file | `*ExampleUI.kt` | `ReboundExampleUI.kt` |
| Example UI function | `fun *ExampleUI(onBackEvent: () -> Unit)` | `fun ReboundExampleUI(...)` |
| Reference file | `exampleGuide.kt` | (URLs and key-concept KDoc only) |
| ViewModel | `*ViewModel.kt` | `PlayerViewModel.kt` |
| Data class | `*Data.kt` / `*ListData.kt` | `UserData.kt` |

---

## Build Environment

- Both `gradlew` (Unix) and `gradlew.bat` (Windows) exist
- Uses the Java 21 toolchain (`javaVersion = "21"` in `gradle/libs.versions.toml`)
- Android SDK path: see `sdk.dir` in `local.properties`
- CLI build command: `./gradlew assembleDebug`

---

## Response Rule for Example-List Queries

When the user asks what to build next — e.g. **"작성할 예제를 알려줘"** (tell me which example to write) or **"다음 예제 리스트"** (next example list) —
**always show the example list and the improvement list together, in clearly separated sections.**

```
## Example List
- Incomplete example items from the `project_pending_examples.md` memory (including priority/constraints)

## Improvement List
- Unhandled improvement items from the `project_code_improvements.md` memory (by 🔴/🟡/🟢 priority)
```

- Clearly separate the two sections under distinct headings (do not mix examples and improvement items in one table)
- Tag each item with its identifier / file path / constraints
- If every improvement item is "none", write "No outstanding improvement items" in the Improvement List section

---

## Pre-work Rule

When the user types **"사전작업"** (pre-work), perform the procedure below:

1. **Understand the whole project**
   - `ConstValue.kt` — review all registered example constants
   - `Examples20XX.kt` files — check recently added examples
   - `ExampleRouter.kt` — check for missing routing entries
   - Main source files — look for deprecated APIs, dead code, architecture violations

2. **Classify improvement items**
   - 🔴 High: build errors / architecture violations / deprecated APIs
   - 🟡 Medium: missing error handling / code-pattern inconsistencies
   - 🟢 Low: naming / import cleanup / dead code

3. **Check upcoming examples**
   - Review the pending example status based on the `project_pending_examples.md` memory

4. **Update memory**
   - `project_code_improvements.md` — add newly found improvement items, move completed ones
   - `project_pending_examples.md` — remove completed examples, add new candidates
   - Sync the `MEMORY.md` index

5. **Report results**
   - Summarize improvement items as a table by priority
   - Distinguish work that can proceed immediately from work that has constraints

---

## Pending-Example Progress Rule

When the user types **"NN번 항목을 진행해주세요"** (please proceed with item NN) — e.g. "23번 항목을 진행해주세요" — perform the procedure below:

### Procedure

```
[START]
1. Find item NN in the `project_pending_examples.md` memory
   - Confirm category / planned file path / content / constraints
2. Implement the example
   - Follow the 4-step "How to Add a New Example" procedure (ConstValue → Examples20XX → UI file → Router)
3. Build verification (`./gradlew assembleDebug`)
   - On failure, fix and rebuild; repeat until it succeeds
4. Commit → `git push origin main`
5. Run the pre-work procedure
6. If the pre-work finds 🔴/🟡/🟢 improvement items → fix immediately → build → commit → push → back to step 5
7. If the pre-work finds all "none", end the loop
8. Tidy memory
   - Move item NN to the "completed items" section in `project_pending_examples.md` (record commit hash, file path, key content)
   - Update the `MEMORY.md` index (reflect remaining incomplete count and recent completion notes)
9. Final report (including the process-verification result table)
[END]
```

### Notes

- Always run `git push origin main` after each commit (commit and push always go together).
- During the pre-work loop, skip items with constraints (e.g. requiring a version upgrade) and only handle items that can be fixed immediately.
- Always tidy memory last, after the pre-work loop ends.

---

## Repeated-Improvement Rule for Newly Found Items

When the user types **"신규 발견 항목을 개선해주세요"** (please improve the newly found items), run the loop below:

### Loop Procedure

```
[LOOP START]
1. If the current memory (project_code_improvements.md) has improvement items, fix them all
   → build verification → commit → push
2. Run the pre-work procedure (full code scan + routing check)
3. If new improvement items are found → go back to step 1 and repeat
4. No new improvement items → end the loop
[LOOP END]
```

### Termination Condition

When the pre-work finds **🔴 High / 🟡 Medium / 🟢 Low all "none"**, end the loop and print the message below:

```
✅ No newly found items. All improvement items are complete.
```

### Notes

- After each fix, always complete build verification → commit → push before running the next pre-work.
- Intentionally excluded items (educational code, etc.) are excluded from improvement targets.
- During the loop, skip items with constraints (e.g. requiring a version upgrade) and only handle items that can be fixed immediately.

---

## Post-work Build Verification Rule

After completing any code add/modify/improve work, **always** follow the procedure below:

1. Run the `./gradlew assembleDebug` build
2. If a compile error occurs, analyze the cause and fix it immediately
3. Repeat 1-2 until the build succeeds
4. After confirming a successful build, proceed to commit
5. **Run `git push origin main` immediately after committing** (commit and push always go together)

After the work is done, **always** print the process-verification result table in the format below:

```
| Step | Content | Result |
|------|---------|--------|
| 1. Code change | Summary of changed files and items | Done |
| 2. Build run | ./gradlew assembleDebug | ✅ Success or ❌ Failure |
| 3. Error fix | Errors found and fixes made (omit if none) | Done |
| 4. Rebuild   | ./gradlew assembleDebug | ✅ Success (omit if none) |
| 5. Commit    | Commit hash | Done |
| 6. Push      | git push origin main | Done |
```

- If the build succeeds on the first try, omit steps 3 and 4
- If error fixes/rebuilds repeat multiple times, add one row per occurrence

---

## Commit Message Convention

- Use **Korean**
- Prefixes: `feat:` / `fix:` / `refactor:` / `chore:` / `docs:`
- Example: `feat: Rebound 리컴포지션 모니터링 예제 추가`

---

## Coding Rules

### Common
- **Write comments in Korean**
- Do not modify code that was not requested (minimize scope)
- Understand the existing patterns first, then implement in the same way

### Data classes
```kotlin
// Server fields: snake_case → client fields: camelCase
data class UserData(
    @SerializedName("user_id") val userId: String,
    @SerializedName("is_active") val isActive: Boolean = false,
    val items: List<String> = emptyList()
)

// When passing between Android components
@Parcelize
data class EntityData(
    @SerializedName("entity_id") val entityId: String
) : Parcelable
```

### ViewModel / UI separation
```kotlin
// ❌ Do not put business logic in the UI
@Composable
fun Screen() {
    Button(onClick = { fetchData() }) { Text("Load") }
}

// ✅ Delegate to the ViewModel
class FeatureViewModel : ViewModel() {
    fun fetchData() { ... }
}

@Composable
fun Screen(viewModel: FeatureViewModel) {
    Button(onClick = { viewModel.fetchData() }) { Text("Load") }
}
```

### ViewModel scope rule
- Use a single ViewModel per top-level feature (e.g. Live)
- Do not split sub-features (Streaming, Chat, Settings) into separate ViewModels

---

## DI (Koin)

```kotlin
// InjectModules.kt — module aggregation
val KoinModules = listOf(apiModule, viewModelModule, networkModule, ktorModule)

// ApiModule.kt
single<Retrofit>(named("DomainName")) {
    Retrofit.Builder().baseUrl("url").client(get()).build()
}

// ViewModelModule.kt
viewModel { FeatureViewModel(api = get(named("ApiName"))) }
```

- Always use the `named()` qualifier for API dependencies
- `single`: global singleton / `factory`: new instance each time / `viewModel`: lifecycle-aware

---

## Layer Rules

| Layer | Allowed dependencies | Forbidden |
|-------|----------------------|-----------|
| domain | none (pure Kotlin) | Android framework |
| data | domain | presentation |
| app(presentation) | domain, data | - |

---

## Detailed Rule Document Locations

For the full document list, first see the document index at **`app/src/main/java/com/example/composesample/docs/README.md`**. The main documents are:

- **Document index**: `app/src/main/java/com/example/composesample/docs/README.md`
- **Architecture decisions**: `ARCHITECTURE.md` (root; layer rules and ARCH refactoring background)
- **Known limitations / deferrals**: `app/src/main/java/com/example/composesample/docs/KnownLimitations.md`
- **Data classes**: `app/src/main/java/com/example/composesample/docs/data/DataRules.md`
- **DI (Koin)**: `app/src/main/java/com/example/composesample/docs/di/DIRules.md`
- **UI rules**: `app/src/main/java/com/example/composesample/docs/ui/UIRules.md`
- **API creation guide**: `app/src/main/java/com/example/composesample/docs/prompt/CreateAPIGuide.md`
- **API+UI binding**: `app/src/main/java/com/example/composesample/docs/prompt/CreateAPIAndUIBindingGuide.md`
- **UI code generation**: `app/src/main/java/com/example/composesample/docs/prompt/CreateUICodeSnippet.md`
- **Using Claude Code**: `app/src/main/java/com/example/composesample/docs/claudecode/ClaudeCodeGuide.md`
- **Compose Hot Reload**: `app/src/main/java/com/example/composesample/docs/devtools/ComposeHotReloadGuide.md`
- **Example candidate review log (archive)**: `app/src/main/java/com/example/composesample/docs/pending/PendingExamples.md`
- **Spec-first workflow (optional)**: `.claude/docs/workflow/WORKFLOW.md` (spec → 승인 → 구현 방법론. 빌드/커밋 규칙은 이 CLAUDE.md가 우선)

---

## Multi-agent Harnesses & Agents (`.claude/`)

이 프로젝트에는 read-only 멀티에이전트 하네스(스킬)와 전용 에이전트가 등록되어 있습니다. 모두 **사용자가 명시 호출**할 때만 실행하며(토큰 비용이 큼), 앱 코드를 변경하지 않습니다(분석/제안 전용).

### Skills (`.claude/skills/`)

| 스킬 | 호출 | 용도 |
|------|------|------|
| `explore-codebase` | `/explore-codebase <대상>` | **탐색형** — 서브시스템 병렬 탐색 → 아키텍처 맵·실행 경로 합성 |
| `impact-analyze` | `/impact-analyze [대상\|diff]` | **검증형** — 변경 영향(크래시/사이드이펙트)을 차원별 탐지 + adversarial 검증 → 분석 섹션 초안 |
| `design-options` | `/design-options <문제>` | **결정형** — 관점이 다른 설계 N개 독립 생성 → judge panel 심사 → 비교표·추천안 |
| `find-similar-bugs` | `/find-similar-bugs <설명\|커밋\|diff>` | **전파탐지형** — 수정한 버그 패턴을 추상화해 코드베이스 내 형제 버그 탐지 (탐지까지만) |
| `qa-verify` | `/qa-verify [버전]` | **감사형** — GitHub QA/bug 이슈(milestone+라벨) ↔ fix 커밋 대조로 수정 누락·불완전을 빌드 배포 전 검출 (`gh` CLI read-only) |

- 사용 시점/연계: 방향 미결정 → `design-options`, 구조 파악 → `explore-codebase`, 변경 안전성 검증 → `impact-analyze`, fix 후 전파 확인 → `find-similar-bugs`. 방향 결정 후 변경면을 `impact-analyze`로 넘기는 체인이 자연스럽습니다.
- 실행 전 반드시 **"<하네스명> 하네스로 ~를 수행합니다"** 한 줄을 고지합니다.

### Agents (`.claude/agents/`)

| 에이전트 | 용도 |
|----------|------|
| `android-reviewer` | Compose + Koin + Clean Architecture 기준 엄격 코드 리뷰 (레이어 의존성·Koin `named()`·UI-로직 분리 pre-check) |
| `android-ui` | `*ExampleUI(onBackEvent)` 패턴·Material3 기준 Compose UI 생성 (비즈니스 로직 미포함) |

> 추적 범위: `.claude/` 중 `skills/`·`agents/`·`docs/`만 git 추적됩니다 (`settings.local.json`·`specs/`는 무시).
