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

### UTIL-01 — ExampleRouter split (done) / ConstValue split (decided: won't-do)
- **ExampleRouter — Done** (commit da927855): the 146-branch `when` monolith was extracted into a registry map (`exampleUiRegistry` in `ExampleUiRegistry.kt`). `ExampleRouter.kt` shrank from 974 to ~106 lines and now only handles move-type dispatch + a registry lookup. Adding an example is now a one-line map entry. Behavior is fully preserved (SDK gating for SSE/ReverseLazyColumn, the `onBackButtonClick` param for SafFile, the Dummy fallback; key set verified 146 = 146).
- **ConstValue — decided: won't-do (no longer a pending task)**: `ConstValue.kt` (150+ constants) stays a single file. Splitting it would break **355 `ConstValue.Xxx` qualified references across 15 files plus ~140 member imports** for a purely cosmetic gain (file length). A Kotlin `object` cannot span multiple files, so any split forces top-level constants and mass reference churn. Benefit < risk → kept as-is permanently; a centralized constants file is the accepted project pattern. This is a closed decision, not deferred work — no need to re-evaluate in future scans.
- Related: the `[UTIL-01 — 결정 완료]` note in `ConstValue.kt` (the prior `TODO` marker was removed since this is no longer pending work).

---

## Naming / convention conflict (awaiting user decision)

### CONV-05 — Room Entity naming (`UserData`)
- **Current**: `data.db.UserData` is a Room `@Entity`.
- **Conflict**: the common Android convention is `*Entity`/`*DTO`, but the file-naming-convention table in `CLAUDE.md` lists `UserData.kt` as the canonical Data-class example, conflicting with the project convention. Not changed automatically; the decision is deferred.

---

## Material 2 remnants (M3 migration complete — intentional remnants only)

### M3-BULK — migration complete, 4 intentional remnants kept
- **Current**: the bulk migrations (1st/2nd/3rd passes) are **complete**. Only **4 files** intentionally remain on Material2, kept for side-by-side comparison with their M3 replacements (**do not re-migrate**):
  - `ui/layout/bottomsheet/BottomSheetUI.kt`
  - `ui/layout/bottomsheet/BottomSheetContent.kt`
  - `ui/layout/bottomsheet/ModalBottomSheetUI.kt`
  - `interaction/swipe/SwipeToDismissUI.kt`
- **History**: the 3rd pass (completed 2026-06-12) migrated the remaining files per domain — `Drawer` (→`ModalNavigationDrawer`), `TopAppBar`, `Scaffold`, `Snackbar`, `BottomNavigation` (→`NavigationBar`), `ScrollableTabRow`, `PullToRefreshBox`, etc. The common header `MainUIComponent.kt` was migrated earlier by M3-05.
- **Scan note**: `grep "import androidx.compose.material."` still matches ~100 files, but those are `material.icons` / `material.ripple` usages — legitimate even in M3 projects — **not** Material2 components. Only the 4 files above import actual M2 components.
- **Intentional remnants**: see "Intentionally Kept Exceptions" in [`ARCHITECTURE.md`](../../../../../../../../ARCHITECTURE.md).

---

## Documentation

### Documentation language — standardized to English (decided)
- **Decision**: all md documents — including the module READMEs (`data/.../DataREADME.md`, `domain/.../DomainREADME.md`) and the rule documents under `docs/` — are written in **English**. (The 2026.06 work first translated the `docs/` rules to Korean, then standardized everything to English by user decision.)
- **Note**: this is a documentation-language choice and does not change the code-comment rule. Code comments are still written in **Korean** (see "Coding Rules" in `CLAUDE.md`), and commit messages are still written in **Korean**.
- **Minor**: `DataREADME.md` does not yet reflect the ARCH-03 `UserCacheRepository` abstraction layer.

---

> This list is updated as progress is made. When an item is resolved, remove its section or mark it "done".
