# Contributing to ComposeSample

Thanks for your interest in contributing. This project is a curated collection of Jetpack Compose examples and practical issue solutions, organized by feature on top of Clean Architecture.

This guide summarizes the conventions used in the project. For the full project guide, see [CLAUDE.md](CLAUDE.md); for the document index, see [docs/README.md](app/src/main/java/com/example/composesample/docs/README.md).

---

## Tech Stack

- **Language**: Kotlin 2.4.0
- **UI**: Jetpack Compose + Material3 (ComposeBom 2026.05.00)
- **Architecture**: Clean Architecture + MVVM
- **DI**: Koin
- **Network**: Retrofit + Gson, Ktor
- **Local DB**: Room
- **Async**: Coroutine + Flow
- **Target SDK**: 35 / **Min SDK**: 24 / **Java**: 21

---

## Module Structure

```
ComposeSample
├── app         # UI layer (Compose, ViewModel, DI modules)
├── data        # Data layer (Repository implementations, API, Room)
├── domain      # Domain layer (Repository interfaces, UseCase, Model) — pure Kotlin(JVM)
├── Core        # Navigation interface
└── Coordinator # Coordinator pattern implementation
```

### Layer Dependency Rules

| Layer | Allowed dependencies | Forbidden |
|-------|----------------------|-----------|
| domain | none (pure Kotlin) | Android framework |
| data | domain | presentation |
| app(presentation) | domain, data | - |

See [ARCHITECTURE.md](ARCHITECTURE.md) for the rationale behind these rules and the intentional educational exceptions.

---

## How to Add a New Example

Always follow these four steps in order.

### Step 1: Add a constant to `ConstValue.kt`

```kotlin
// app/src/main/java/com/example/composesample/util/ConstValue.kt
const val NewFeatureExample = "newFeatureExample"
```

Also update the section comment, and when adding the first example of a new year/month, update `UpdateDate`.

### Step 2: Add an `ExampleObject` to `Examples20XX.kt`

```kotlin
// app/.../presentation/example/list/Examples2026.kt
ExampleObject(
    lastUpdate = "26. MM. DD",   // two-digit year, e.g. "26. 06. 18"
    title = "Example title",
    description = "Example description",
    blogUrl = "",                // always empty — links live only in exampleGuide.kt
    exampleType = ConstValue.NewFeatureExample
)
```

When adding a new year file (`Examples20XX.kt`), register it in `ExampleObjectList.kt` as well.

### Step 3: Create the UI file

```
app/.../component/{category}/{subcategory}/NewFeatureExampleUI.kt
```

- **File name**: `*ExampleUI.kt`
- **Function signature**: `fun NewFeatureExampleUI(onBackEvent: () -> Unit)`
- Reference/blog URLs go only in `exampleGuide.kt` (not in the UI file).

### Step 4: Add routing to `ExampleRouter.kt`

```kotlin
import com.example.composesample.presentation.example.component.{category}.NewFeatureExampleUI
import com.example.composesample.util.ConstValue.NewFeatureExample

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

## Coding Rules

- **Write code comments in Korean.**
- Do not modify code outside the scope of your change (keep the change focused).
- Understand the existing patterns first, then implement in the same way.
- Keep business logic in the ViewModel; do not put it in `@Composable` UI.
- Use a single ViewModel per top-level feature (do not split sub-features into separate ViewModels).

### Data classes

```kotlin
// Server fields: snake_case → client fields: camelCase
data class UserData(
    @SerializedName("user_id") val userId: String,
    @SerializedName("is_active") val isActive: Boolean = false,
    val items: List<String> = emptyList()
)
```

### DI (Koin)

- Always use the `named()` qualifier for API dependencies.
- `single`: global singleton / `factory`: new instance each time / `viewModel`: lifecycle-aware.

For details, see [DataRules.md](app/src/main/java/com/example/composesample/docs/data/DataRules.md), [DIRules.md](app/src/main/java/com/example/composesample/docs/di/DIRules.md), and [UIRules.md](app/src/main/java/com/example/composesample/docs/ui/UIRules.md).

---

## Build & Verification

Always verify the build before committing.

```bash
./gradlew assembleDebug
```

If a compile error occurs, fix it and rebuild until it succeeds. CI runs `assembleDebug` and `testDebugUnitTest` on every push and PR to `main` (see [`.github/workflows/android-build.yml`](.github/workflows/android-build.yml)).

---

## Commit Message Convention

- Write commit messages in **Korean**.
- Prefixes: `feat:` / `fix:` / `refactor:` / `chore:` / `docs:`
- Example: `feat: Rebound 리컴포지션 모니터링 예제 추가`

---

## Pull Request Checklist

Before opening a PR, confirm:

- [ ] `./gradlew assembleDebug` succeeds.
- [ ] For a new example, all four steps are done (ConstValue → Examples20XX → UI file → Router).
- [ ] The UI function follows the `*ExampleUI(onBackEvent: () -> Unit)` pattern.
- [ ] Reference URLs are placed only in `exampleGuide.kt`.
- [ ] Layer dependency rules are respected.
- [ ] Code comments are written in Korean; the commit message follows the convention above.
