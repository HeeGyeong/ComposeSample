# ARCHITECTURE.md — Architecture Decision Record

Records ComposeSample's architecture structure and **"why it is designed this way."**
For rules (how to write code) see `CLAUDE.md`; for API listings see the per-module READMEs.

---

## Module Structure

```
ComposeSample
├── app         # UI layer (Compose, ViewModel, DI modules, examples)
├── data        # Data layer (Repository implementations, API, Room)
├── domain      # Domain layer (Repository interfaces, UseCase, Model) — pure Kotlin(JVM)
├── Core        # Navigation interface
└── Coordinator # Coordinator pattern implementation
```

## Layer Dependency Rules

| Layer | Allowed dependencies | Forbidden |
|-------|----------------------|-----------|
| domain | none (pure Kotlin) | Android framework, Retrofit, Gson |
| data | domain | presentation |
| app(presentation) | domain, data | - |

- Dependencies are wired at runtime via Koin; compile-time dependencies never go beyond the table above.
- `Coordinator`/`Core` are auxiliary modules for abstracting screen transitions.

---

## Key Architecture Decisions (ARCH refactoring)

### ARCH-01 / ARCH-02 — Remove Retrofit/Gson from domain
- **Decision**: domain Repository interfaces, which used to return `retrofit2.Call`, now expose `suspend fun`s that return domain types. `MovieResponse`/`PostData` match their property names to the JSON keys to drop the `@SerializedName`(Gson) dependency.
- **Why**: if framework types leak into the domain contract, the domain becomes tied to the network implementation, breaking its purity and testability.

### ARCH-03 — Remove presentation → data direct reference (DataCache)
- **Decision**: `DataCacheViewModel`, which used to reference `RoomSingleton` directly, was separated into a `UserCacheRepository` (interface) + `UserCacheRepositoryImpl` (delegating to ExampleDao) abstraction registered in `RepositoryModule`.
- **Trade-off**: because `UserData` is a Room `@Entity` it cannot live in the pure-Kotlin domain, so the **abstraction interface was placed in the data layer, not domain** (mapping to a domain model was kept out of scope). The layer rule (presentation depends on an abstraction instead of referencing data directly) is still honored.

### ARCH-04 — Convert the domain module to pure Kotlin(JVM)
- **Decision**: domain was converted from `com.android.library` + `kotlin.android` to `kotlin("jvm")`. The config.gradle/core-dependencies.gradle application was removed to block transitive Room/Retrofit/Ktor dependencies.
- **Why**: after confirming domain has zero external dependencies (only suspend/List), framework dependencies are made **physically impossible** at the build-system level. The androidTest template, unsupported in pure jvm, was removed; only `testImplementation(junit)` is kept.

### ARCH-05 — Move UI-only models from domain to app
- **Decision**: moved `ExampleObject`/`ExampleMoveType` from `domain.model` to the app `presentation.example.model` package (imports updated across 8 referencing files).
- **Why**: example-list items and the move type are not business models but **UI/app-only concepts**. Only pure business models remain in domain.

### Auxiliary decisions
- **DI-04**: the Koin registration of `ApiExampleViewModel` is now explicit via `named()` arguments (application/postApiInterface/ktorClient) instead of positional → removes the risk of argument-order mistakes.
- **CONV-04**: the UseCase invocation convention was unified from `execute()` to `operator fun invoke()`.

---

## Intentionally Kept Exceptions (educational)

Some code was **deliberately excluded** from refactoring. We make "why it was not changed" explicit.

- **`ApiExampleViewModel` ↔ `ApiExampleUseCaseViewModel`**: a **contrast pair** comparing a direct API call vs going through a UseCase (noted in KDoc). Cleaning up only one side would lose the comparison, so both are kept.
- **The `legacy/` area** (MovieViewModel/SubActivityViewModel/SubUI, etc.): intentionally preserved as learning material showing older patterns. Isolated in the `legacy` package.
- **Material 2 demo examples** (BottomSheet M2, SwipeToDismiss M2, etc.): intentionally kept to compare **side by side** with their M3 replacement examples (made explicit with `@Suppress("DEPRECATION")` + comments).

---

## Known Limitations / Deferrals

For deferred version upgrades, items planned for splitting, etc., see [`docs/KnownLimitations.md`](app/src/main/java/com/example/composesample/docs/KnownLimitations.md).
