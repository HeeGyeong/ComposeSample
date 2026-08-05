# Changelog

Version and example-addition history for ComposeSample, newest first.
(See the "Latest updates" section in `README.md` for a summary.)

---

## 2026.08
- New examples added
  - LazyList `contentType` Reuse-Pool Trap — shows why passing a per-item unique value as `contentType` prevents slot reclamation: the reuse policy retains up to 7 slots *per contentType*, so one bucket per item never meets the cleanup condition. Compares null / `item::class` / per-item value modes and measures surviving payloads via `WeakReference` after `System.gc()`
  - Compose Grid API — Compose 1.11 experimental non-lazy 2D track layout (CSS Grid inspired): 6 track sizes (`Fixed`/`Percentage`/`Flex(fr)`/`Auto`/`MinContent`/`MaxContent`) plus `minmax`, `gap`/`columnGap`/`rowGap`, automatic vs explicit `Modifier.gridItem(row, column, span)` placement including the `IntRange` overload, `GridFlow.Row`/`Column` cursor direction, and a live count of alive child compositions against `LazyVerticalGrid` to show Grid is not lazy. Named areas (`area()`) are Compose 1.12+ and out of scope
  - Compose MediaQuery API — Compose 1.11 experimental declarative environment queries. Covers the activation trap first: `LocalUiMediaScope` has no default and the platform only provides it while `ComposeUiFlags.isMediaQueryIntegrationEnabled` (default `false`) is on, while `obtainUiMediaScope()` is Kotlin `internal` so there is no way to supply it manually — the flag must be set before `setContent` (done in `BlogExampleActivity.onCreate`). Shows all 8 `UiMediaScope` properties with live device values, measures the recomposition-scope difference between `mediaQuery` (re-evaluates on every change) and `derivedMediaQuery` (only when the result flips) using a simulated scope driven by a width slider, and branches layout on width + posture + pointer precision
- Convention / structure cleanup
  - Replaced 16 hardcoded blog URLs in the example lists with the `blogUrl(postId)` helper so the base URL lives only in `BlogUrlHelper.kt`; updated the CLAUDE.md rule (new example starts with `""` → helper once published → raw URLs forbidden) (CONV-08)
  - Fixed the Shimmer sub-category parent to carry the group constant instead of a leaf constant, which had silently duplicated a child entry; documented the second registration path (`subCategoryList()`) as "Step 2-1" (REG-DUAL-01)
- Documentation fixes
  - Corrected the "Step 4: register routing" instructions across 4 documents — routing is a `ExampleUiRegistry.kt` map lookup (154 entries), not an `ExampleRouter.kt` when-expression, and an unregistered type does not break the build but silently falls back to a Dummy screen (DOC-ROUTE-01)

## 2026.07
- New examples added
  - Realtime Waveform Canvas (ECG/PPG) — separates a fixed 250Hz sample rate from a variable frame rate by carrying the fractional remainder across `withFrameNanos` frames; fixed-size `FloatArray` ring buffer for zero per-frame allocation; Sweep vs Scroll render modes; waveform state read in the draw phase so recomposition stays at 0
  - Background Location Tracking (Foreground Service + WorkManager) — permission as a *sequence* (foreground location → notifications → background location, the last one only via app settings on Android 11+); a real `foregroundServiceType="location"` service that keeps running when the app is backgrounded; contrasted with `CoroutineWorker` to show why WorkManager cannot replace continuous tracking
  - Screenshot Detection — Android 14 `registerScreenCaptureCallback` vs legacy MediaStore `ContentObserver` comparison
  - Advanced Repository Pattern — Memory→Disk→Network priority-resolving repository
  - RememberObserver / Composition Lifecycle — contrasts removing a composable from composition vs. triggering recomposition only, to observe `onRemembered`/`onForgotten` firing; DisposableEffect comparison; `rememberCoroutineScope` internals reproduced conceptually
  - Media3 (ExoPlayer) Video Playback — `AndroidView` + `PlayerView` embedding with real playback; auto-pause on backgrounding via `OnLifecycleEvent(ON_STOP)`
  - IPC / Exported Component Security Diagnostics — runtime `PackageManager` scan of this app's own exported components; `PendingIntent` `FLAG_MUTABLE`/`FLAG_IMMUTABLE` tamper comparison with a live broadcast; `signature`-level permission enforcement as a code reference
- Legacy subsystem removal (LEGACY-RM)
  - Removed the entire legacy subsystem (`presentation/legacy/` 24 files + 10 Activities) and its RxJava dependency completely
- Dead code / documentation cleanup
  - Removed 45 unreferenced `*Guide.kt` learning-guide files; each file's source URL was absorbed into the sibling `exampleGuide.kt` (GUIDE-DEAD-01)
  - Removed the dead parameterized `EventSource` Koin registration in `NetworkModule` (DI-DEAD-01)
  - Removed the dead `ApiInterface` + related Koin/Retrofit registrations left over from the RxJava removal (API-DEAD-01); renamed `ItemDTO.kt` → `UserData.kt` to match its actual contents (CONV-06)
  - Removed the unused `sh.calvin.reorderable` dependency (DEP-DEAD-02)
  - Removed 6 unreferenced functions from `KtorApiExtensions`/`AudioRecorderUtil` (DEAD-FUNC-01/02)
  - Removed 4 commented-out dead code blocks (CODE-DEAD-02)
  - Removed unused imports across 5 files (CODE-IMPORT-01)
  - Moved 13 reference URLs out of 5 `*ExampleUI.kt` files' KDoc into the sibling `exampleGuide.kt` (CONV-07)
  - Removed unused imports left over from the `SectionCard` consolidation across the 4 security files (CODE-DUP-CLEANUP-01) and 3 more unused imports elsewhere (CODE-IMPORT-02)
  - Removed 4 commented-out dead code blocks (CODE-DEAD-03) and 2 unreferenced private composables (DEAD-FUNC-03)
- Existing example enhancement
  - Init Case Test — the UI was a 39-line empty screen while its ViewModel already held three loading-trigger patterns (`LaunchedEffect` / `init{}` / `onStart + stateIn(WhileSubscribed)`). Rebuilt the screen to observe *when and how often* each one fires, with a subscribe/unsubscribe toggle that lets you re-subscribe before or after the 5s timeout. ViewModel code unchanged (INIT-UI-01)
- Deprecated API migration
  - Migrated the Material3 1.4.0 deprecated Tab APIs in 2 files — `TabRow`→`SecondaryTabRow`, `ScrollableTabRow`→`SecondaryScrollableTabRow`, `Modifier.tabIndicatorOffset`→`TabIndicatorScope.tabIndicatorOffset`. Secondary (not Primary) was chosen to preserve the existing full-width indicator behavior (DEP-M3TAB-01)
  - Migrated the Compose test rules to `androidx.compose.ui.test.junit4.v2.*` across 3 androidTest files and 2 example code snippets, so the examples no longer teach the deprecated API (TEST-DEPRECATED-01)
- Test infrastructure
  - Fixed stale import paths in 3 androidTest files, restoring `:app:compileDebugAndroidTestKotlin` after the instrumentation source set had been uncompilable for over a year following the 2025-07 package reorganization (TEST-STALE-01)
  - Corrected `implementation(libs.bundles.androidTest)` → `androidTestImplementation(...)`, removing instrumentation-test artifacts from the production runtime classpath of all 4 modules that apply the shared dependency script (GRADLE-SCOPE-01)
- Architecture cleanup
  - Converted `RefreshViewModel`/`DataCacheViewModel` from `AndroidViewModel` to plain `ViewModel` — neither used the injected `Application` (VM-ANDROIDVM-01)
  - Consolidated a byte-identical `SectionCard` composable duplicated across 4 `system/security` example files into a shared `SecurityUiComponents.kt` (CODE-DUP-01)
  - Applied a `named("jsonplaceholder")` qualifier to the Ktor `HttpClient` Koin registration and its single consumer, matching the project's DI convention (DI-NAMED-01)
- Bug fixes
  - Removed two paths in the newly added `LocationTrackingService` that violated the `startForeground()` 5-second contract — a stop path that woke the service via `startForegroundService()` without ever promoting it, and a permission pre-check that returned early before promotion. Also fixed duplicate listener registration on `onStartCommand` re-entry (FGS-CONTRACT-01)
- Compiler warnings
  - Removed the last "Expression is unused" warnings in main sources (CODE-WARN-01) and 3 "No cast needed" warnings in the unit-test source set (TEST-WARN-01), reaching zero kotlinc warnings across all modules and source sets under forced recompilation
- Dependency migration
  - Removed Glide and Coil2 entirely; migrated to Coil3 (`coil3`, `coil3-gif`) as the sole image loader across `FlexBoxUI`/`LottieExampleUI` (DEP-VERSION-01)
  - Cleaned up unused/duplicated dependency declarations (DEP-DEAD-03, DEP-DUP-01, DEP-VERSION-02)

## 2026.06
- Architecture refactoring and documentation/quality improvements
  - Converted the domain module to pure Kotlin(JVM) — removed Android/Retrofit/Gson dependencies
  - Moved ExampleObject/ExampleMoveType from domain to the app `presentation.example.model` package
  - Removed presentation→data direct reference in DataCache (UserCacheRepository abstraction)
  - Unified UseCase `execute()` → `operator invoke()`
  - Made the ApiExampleViewModel Koin registration explicit via `named`
  - Migrated MainUIComponent from Material1 to Material3
  - Translated the UI/DI/Data rule documents to Korean, then updated all md docs (DomainREADME/README/AppREADME/PendingExamples/ClaudeCodeGuide); later migrated all docs to English
  - New docs: `docs/README.md` (index), `ARCHITECTURE.md`, `docs/KnownLimitations.md`, `LICENSE` (MIT)
  - Added 30 exampleGuide.kt category files
- New examples added (2026-06-17 ~ 2026-06-29)
  - Kotlin 2.4 Language Features — collection literals / context parameters (CodeBlock-only, no global opt-in)
  - How Compose Works — compiler transform / SlotTable / snapshot read-tracking / layout pipeline walkthrough
  - Coil 3 Image Loading — AsyncImage state, memory cache policy + `dataSource` tracking, ImageLoader customization
  - Preview-Driven Screenshot Testing — locale × fontScale × theme matrix derived live from `@Preview`
  - Freehand Drawing / Signature Canvas — Canvas + `detectDragGestures`, MVI intent/reducer for undo/redo
- Dependency cleanup: removed the end-of-life `accompanist-systemuicontroller` dependency, replaced with platform Window APIs (EDGE-01)
- Versions: upgraded to Kotlin 2.4.0 + KSP 2.3.9 (2026-06-16), ComposeBom 2026.05.00 + Material 1.11.1
  - The Kotlin 2.4.0 bump required disabling the HotSwan (Compose Hot Reload) plugin — hotswan-compiler 1.2.1 is incompatible with the 2.4.0 compiler-extension API. All other modules build with zero source changes. See `docs/devtools/ComposeHotReloadGuide.md`.

## 2026.05
- New examples added
  - Accessible Focus Indicator — 4 keyboard/D-pad focus visualization patterns + IndicationNodeFactory + DrawModifierNode custom indication
  - Document Editing TextField — TextFieldState deep dive (undoState/selection manipulation/AnnotatedString preview/multi-cursor simulation)
  - Syntax Highlighting — AnnotatedString + regex tokenizer Kotlin code highlighting mini demo
  - Particle Emitter — Canvas + withFrameNanos physics particle system (fireworks/stardust) + Canvas vs Layout trade-off
  - Animations Showcase — compare 4 sections simultaneously with duration/easing sliders
  - Hardware-Backed Keystore — per-API-level secure hardware verification
  - Shared Element Debug Tooling (Compose 1.11)
  - Foundation Style API (Compose 1.11) — single-CompositionLocal design tokens
  - Month Picker Dial — Canvas polar coordinates + drag snap
  - App Security — Cert Pinning/Secure Storage/Play Integrity comparison
  - AGSL Shader Live Tuning — real-time RuntimeShader uniform tweaking
  - Type-Safe Feature Flag — sealed registry + debug override
  - Per-Item ViewModels — per-item ViewModelStoreOwner in LazyColumn
  - Room FTS4 vs LIKE search performance comparison
  - Room Database Indices — single/composite index benchmark
  - Multi-Table Inserts in Room — DAO interface inheritance + transaction

## 2026.04
- New examples added: LazyStaggeredGrid waterfall grid, Adaptive Layout WindowSizeClass, Custom Layout MeasurePolicy, Dynamic App Links, Screenshot Testing (Paparazzi/Roborazzi), Compose Snapshot System, Compose UI Testing, Predictive Back Gesture, Spring/Tween/Snap animation, Haptic Feedback, Stability Annotations, Rich Content in Text Input, official FlowRow/FlowColumn Flexbox, Preview-only Annotation (@RequiresOptIn), Coroutine Bridges (suspendCoroutine), Compose Loaders math-curve loading, TextField Max Length hidden bug, Kotlin Name-Based Destructuring, Material 3 Expressive SecureTextField, Modifier Order in Compose, Flow Operators (buffer/conflate/debounce/sample), Multi-Table Inserts in Room (DAO interface inheritance), etc.
- Applied Compose Hot Reload (HotSwan) Gradle plugin
  - ⚠️ Disabled on `main` since 2026-06-16: HotSwan 1.2.1 is incompatible with Kotlin 2.4.0 (compiler-extension `ClassCastException`). Re-enable once a 2.4.0-compatible release ships. See `docs/devtools/ComposeHotReloadGuide.md`.
- Versions: upgraded to Kotlin 2.3.20 + AGP 8.13.2 + Compose BOM 2026.03.01 + Material3 1.4.0

## 2026.03
- New examples added: MotionBlur, LargeContentViewer, LocalContextStrings, EmbeddedPicker Compose integration, Rebound recomposition monitoring, Coroutine Flow Testing with Turbine, Compose Preview Internals, Remember Patterns, Startup Optimization, AnimatedContent deep dive, etc.

## 2026.02
- New examples added: Transition, Dial, Photo Picker, Sticker Canvas, etc.

## 2026.01
- UI component examples added: Quick Setting, TopAppBar, Canvas Shapes, Responsive TabRow, etc.

## 2025.12
- Advanced Compose examples added: ButtonGroup, WithContext, Path Hit, Recomposition, etc.

## 2025.11
- Kotlin pattern examples added: Sealed Class Interface, coroutine internals, Modularization, etc.

## 2025.10
- Examples added: CompositionLocal, AutoCloseable, Inline, etc.

## 2025.09
- New examples added: Navigation3, Shadow API, SnapNotify, card corner styles, etc.

## 2025.08
- New examples added: Text AutoSizing, etc.

## 2025.07
- Reorganized the component package structure by top-level category

## 2025.06
- Version updates and added Cursor Rules mdc files (9 rule files)

## 2025.03
- Added source documents for Cursor IDE

## 2025.02
- targetSDK 35 UI support

## 2024.12
- Authored toml file and changed gradle

## 2024.11
- Updated README.md, authored per-domain README.md files

## 2024.08
- Version update and version handling

## 2024.06
- Migrated to Clean Architecture structure

## 2024.04
- Main screen UI/UX improvements
