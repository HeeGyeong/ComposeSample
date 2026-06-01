# Known Limitations / Deferrals

A human-readable collection of **intentional deferrals, version constraints, and items planned for splitting** that were previously scattered across code TODO comments and the work memory. Each item also records "why it was not fixed right now."

---

## Deferred version upgrades

### DI-01 — Koin 3.5.x upgrade deferred
- **Current**: pinned to Koin 3.2.2 (`libs.versions.toml`)
- **Why**: upgrading to 3.5.x requires ① a `getViewModel(owner=)` API change and ② removing the navigation-compose transitive dependency from `koin-androidx-compose`, which touches many call sites. Deferred to a separate PR because the impact is large.
- Related: the `TODO(DI-01)` comment in `libs.versions.toml`.

### biometric pinned to alpha05
- **Current**: `androidx.biometric:biometric(-compose):1.4.0-alpha05`
- **Why**: alpha06+ requires compileSdk 36, but the project uses compileSdk 35. We adopted alpha05, the last version compatible with 35. (alpha06+ also changes the `biometricRequest` API to vararg + CustomOption, requiring call-site changes.)

### ffmpeg commented out
- **Current**: the encoding/decoding example under `system/media/ffmpeg` is fully commented out
- **Why**: version-compatibility issues with the ffmpeg-kit library. Non-working code is kept as comments rather than deleted (project policy: keep version-incompatible examples as comments instead of removing them).

---

## Planned for splitting (separate PR)

### ARCH-02 — Split core-dependencies per module
- **Current**: `config.gradle`/`core-dependencies.gradle` are applied commonly. domain was split out by ARCH-04, but `core`/`coordinator` still apply them commonly.
- **Why**: minimizing per-module dependencies is desirable but has a wide impact, so it is deferred to a separate PR.

### UTIL-01 — Split ConstValue / ExampleRouter
- **Current**: `ConstValue.kt` (150+ constants) and `ExampleRouter.kt` (150+ import lines, 130+ when branches) have grown large.
- **Why**: both files are edited on every example addition, becoming a merge-conflict hotspot. Splitting by category is needed but has a large impact, so it is deferred to a separate PR.
- Related: the `TODO(UTIL-01)` comment in `ConstValue.kt`.

---

## Naming / convention conflict (awaiting user decision)

### CONV-05 — Room Entity naming (`UserData`)
- **Current**: `data.db.UserData` is a Room `@Entity`.
- **Conflict**: the common Android convention is `*Entity`/`*DTO`, but the file-naming-convention table in `CLAUDE.md` lists `UserData.kt` as the canonical Data-class example, conflicting with the project convention. Not changed automatically; the decision is deferred.

---

## Material 2 remnants (M3 migration in progress)

### M3-BULK 3rd pass — 32 files still on Material2
- **Current**: the 1st/2nd bulk migrations (~80 files) are done. The remaining 32 files require API model changes and must be rewritten per domain.
- **Classification**: `BottomSheet*`/`ModalBottomSheet*` (state-model change), `Drawer` (→`ModalNavigationDrawer`), `TopAppBar` (Slot/scrollBehavior differences), `Scaffold`, `Snackbar`, `BottomNavigation` (→`NavigationBar`), `ScrollableTabRow`, etc.
- **Why**: simple import replacement is not enough; call sites must be rewritten, so per-domain PR splitting is recommended. The common header `MainUIComponent.kt` was already migrated by M3-05 (excluded).
- **Intentional remnants**: some M2 examples (BottomSheet/SwipeToDismiss) are kept for comparison with their M3 replacements — see "Intentionally Kept Exceptions" in [`ARCHITECTURE.md`](../../../../../../../../ARCHITECTURE.md).

---

## Documentation

### Documentation language — standardized to English (decided)
- **Decision**: all md documents — including the module READMEs (`data/.../DataREADME.md`, `domain/.../DomainREADME.md`) and the rule documents under `docs/` — are written in **English**. (The 2026.06 work first translated the `docs/` rules to Korean, then standardized everything to English by user decision.)
- **Note**: this is a documentation-language choice and does not change the code-comment rule. Code comments are still written in **Korean** (see "Coding Rules" in `CLAUDE.md`), and commit messages are still written in **Korean**.
- **Minor**: `DataREADME.md` does not yet reflect the ARCH-03 `UserCacheRepository` abstraction layer.

---

> This list is updated as progress is made. When an item is resolved, remove its section or mark it "done".
