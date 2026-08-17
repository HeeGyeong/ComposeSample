# Changelog

Version and example-addition history for ComposeSample, newest first.
(See the "Latest updates" section in `README.md` for a summary.)

---

## 2026.08
- New examples added
  - LazyList `contentType` Reuse-Pool Trap — shows why passing a per-item unique value as `contentType` prevents slot reclamation: the reuse policy retains up to 7 slots *per contentType*, so one bucket per item never meets the cleanup condition. Compares null / `item::class` / per-item value modes and measures surviving payloads via `WeakReference` after `System.gc()`
  - Compose Grid API — Compose 1.11 experimental non-lazy 2D track layout (CSS Grid inspired): 6 track sizes (`Fixed`/`Percentage`/`Flex(fr)`/`Auto`/`MinContent`/`MaxContent`) plus `minmax`, `gap`/`columnGap`/`rowGap`, automatic vs explicit `Modifier.gridItem(row, column, span)` placement including the `IntRange` overload, `GridFlow.Row`/`Column` cursor direction, and a live count of alive child compositions against `LazyVerticalGrid` to show Grid is not lazy. Named areas (`area()`) are Compose 1.12+ and out of scope
  - Compose MediaQuery API — Compose 1.11 experimental declarative environment queries. Covers the activation trap first: `LocalUiMediaScope` has no default and the platform only provides it while `ComposeUiFlags.isMediaQueryIntegrationEnabled` (default `false`) is on, while `obtainUiMediaScope()` is Kotlin `internal` so there is no way to supply it manually — the flag must be set before `setContent` (done in `BlogExampleActivity.onCreate`). Shows all 8 `UiMediaScope` properties with live device values, measures the recomposition-scope difference between `mediaQuery` (re-evaluates on every change) and `derivedMediaQuery` (only when the result flips) using a simulated scope driven by a width slider, and branches layout on width + posture + pointer precision
  - Paging3 `RemoteMediator` (offline-first paging) — network + DB dual source with the DB as the single source of truth: the Room-generated `PagingSource` only ever reads the DB, and `RemoteMediator` writes fetched pages into it, so a dropped network still serves whatever is cached. Covers the `LoadType` REFRESH/PREPEND/APPEND branching, why a separate RemoteKeys table is needed (a DB-only `PagingSource` cannot know the next *network* page, so per-item prev/next keys are stored and looked up from the boundary item), atomic item+key updates inside `withTransaction`, `MediatorResult.Success(endOfPaginationReached)`, and the `initialize()` cache gate (`SKIP_INITIAL_REFRESH` vs `LAUNCH_INITIAL_REFRESH`). Surfaces `loadState.source` (DB) and `loadState.mediator` (network) as two separately observable axes — on network failure only `mediator` goes to `Error`. Adds the `androidx.room:room-paging` dependency (same version as Room 2.8.4), declared in the app module only, since a Room DAO returning `PagingSource` fails KSP without it
  - Composition Observer (why did this recompose) — the runtime observation APIs, which answer a different question from the project's existing recomposition examples: those instrument with `SideEffect` counters and answer "how many times", while `CompositionObserver.onScopeInvalidated(scope, state)` answers "which state invalidated which scope". Attaches via `currentComposer.composition` + `setObserver` (whose return handle is nullable) and renders a causal log of the 7 callbacks. Pairs it with the state-layer APIs and measures, on a real device, that their coverage is complementary rather than overlapping: a plain global write (a button `onClick`) is seen by `registerGlobalWriteObserver` and `registerApplyObserver` but **not** by `observeSnapshots` — its read/write observers are injected in `onPreCreate`, which only runs when a new snapshot is created — whereas a write inside `withMutableSnapshot` is seen by `observeSnapshots` but not by `registerGlobalWriteObserver`. Since composition itself runs inside its own snapshot, that second row is exactly why `observeSnapshots` is the only route to composition-internal writes. Also demonstrates live that re-assigning an equal value is invisible to all three (the setter skips the write entirely), and documents the callback contract — callbacks run mid-composition and under the global snapshot lock, so the example only appends to a pre-allocated ring buffer in O(1) and copies to snapshot state outside composition
  - Slot Tree Inspector (composition structure dump) — walks `currentComposer.compositionData` recursively and resolves each `CompositionGroup` into a function name, source file, line number and parameter names via `parseSourceInformation`. This is the structural counterpart to the Composition Observer example (event axis: *why* did it recompose) and fills exactly the gap that one left, where invalidated scopes could only be labelled S1/S2 with an identity hash. Device measurements drive the wording: (a) reading during composition silently returns a tree that is missing whatever is being built right now — 316 groups mid-composition vs 331 from the click handler after adding one row, and an entirely empty tree (`isEmpty=true`) when the slot table is new — so traversal must happen outside composition; (b) without `collectParameterInformation()` the structure is complete but all 316 groups have a null `sourceInfo`, because the runtime only records it when `inserting && sourceMarkersEnabled` (so groups inserted before the call are never backfilled, while subtrees inserted later are recorded normally); (c) each `LazyColumn` item is its own subcomposition with a non-null parent, which is why the flag enabled in one card does not leak into the next and the "collection OFF" control card works at all. Also covers the `sourceInfo` grammar (`C(fn)N(params)line@offsetLlength:File.kt#hash`, `CC` for inline, `*` for repeatable call sites, `:c#` for inline-class parameters), that `parseSourceInformation` returns null rather than throwing on malformed input, and the observer effect — the inspector's own result table joins the same composition, so rescanning without changing anything inflates the total group count. Uses only `runtime-android` APIs (`@OptIn(ComposeToolingApi::class)` alone is enough), deliberately avoiding `ui-tooling-data`'s `asTree()`/`Group`, which is debug-only and would break release builds
  - 2D Path Animation (Arc / Spline) — the path-shape axis, which the project's existing animation examples do not cover: `SpringTweenSnapExampleUI` and `AnimationsShowcaseExampleUI` are both about "how a value changes over time" (1D easing), while these specs decide "what shape the trajectory takes between two points" when the value is 2D. Rather than comparing curves by eye, each spec is wrapped in `TargetBasedAnimation` and sampled with `getValueFromNanos()` at 121 points, so the curve drawn on the Canvas *is* the spec's output, and a linearly-driven progress dot rides each path to expose the timing difference on top of the shape difference. Device measurements (SM-A725F/API 33) drive the wording rather than assumption: (a) `ArcMode.ArcLinear` produces coordinates identical to `tween` (t=0.25 → (47.3, 47.3)), so it is the "no arc" mode, while `ArcAbove` (72.6, 13.7) spreads horizontally first and `ArcBelow` (13.7, 72.6) is its mirror image; (b) `keyframes` and `keyframesWithSpline` both pass through the waypoint exactly at t=0.5 — the only difference is the corner, with the spline bending earlier and wider ((50, -62.5) vs (50, -40)) to remove it; (c) inside `keyframes`, `value at time using ArcMode.X` applies to the segment *leaving* that keyframe, not the one arriving at it, so attaching it to the last keyframe does nothing. Also pins the opt-in boundary by probe compile: `ArcAnimationSpec` needs `ExperimentalAnimationSpecApi` and `DeferredTargetAnimation` needs `ExperimentalAnimatableApi`, but `keyframesWithSpline` and `using ArcMode` are stable and need no opt-in — the gating sits on the `ArcAnimationSpec` class, not on `ArcMode` itself, which is why the segment-arc demo function carries no `@OptIn` at all. The `DeferredTargetAnimation` section pairs `approachLayout` with a live readout of the lookahead target against the current value, and documents the measured contract (first `updateTarget` settles immediately with `isIdle` still true; a later one leaves the return value behind while `pendingTarget` jumps ahead) plus the requirement that the supplied `CoroutineScope` carry a `MonotonicFrameClock`, confirmed by the `IllegalStateException` stack trace raised when it does not
  - Flow Layout Overflow Control — the overflow-handling axis that the existing `FlowRowLayoutExampleUI` does not cover (that example is entirely about wrapping; `maxLines`, `overflow`, `ContextualFlow*`, and `expandIndicator` appear zero times in its 545 lines). Covers `maxLines` row limiting, `FlowRowOverflow.expandIndicator { }` for a "+N more" chip, `FlowRowOverflow.expandOrCollapseIndicator(expandIndicator, collapseIndicator, minRowsToShowCollapse, minHeightToShowCollapse)` for a paired expand/collapse indicator, and `ContextualFlowRow(itemCount, maxLines) { index -> }` index-based lazy composition, plus `FlowRowOverflowScope.totalItemCount`/`shownItemCount` inside the indicator slot. A `SideEffect`-instrumented side-by-side card is the centerpiece: with the same 40-item list and `maxLines = 2`, plain `FlowRow` composes all 40 items regardless of what is visible (its content lambda is a bare `forEach`, so nothing withholds composition), while `ContextualFlowRow` — which uses `SubcomposeMeasureScope` internally — only composes however many items actually get placed, and the two live counters make that difference directly visible on screen rather than asserted in prose. Also documents an API-state finding: in the project's resolved `foundation-layout:1.11.1`, every overload that accepts a `FlowRowOverflow`/`ContextualFlowRowOverflow` (on `FlowRow`, `FlowColumn`, `ContextualFlowRow`, `ContextualFlowColumn`) is `@Deprecated("The overflow parameter has been deprecated")` with no `replaceWith` and no non-deprecated alternative that still accepts an overflow object — confirmed via `javap` bytecode inspection, not release notes. Since there is no substitute, `@Suppress("DEPRECATION")` is applied only to the private composables that actually pass an `overflow` argument, each with a one-line comment explaining why, and the plain-`FlowRow` half of the comparison card intentionally uses the non-deprecated `maxLines`-only overload to keep the suppressed surface minimal
  - Recomposer Registry Observation — the composition-lifecycle axis one level above the project's existing internals examples: Composition Observer answers *why* a scope inside one composition invalidated, Slot Tree Inspector answers *what shape* one composition's slot tree is, and this example answers *how many compositions exist and when they are created/destroyed*, via `Recomposer.runningRecomposers: StateFlow<Set<RecomposerInfo>>` (no opt-in required — confirmed by `javap`, no `RequiresOptIn` gating on the property) and `RecomposerInfo.observe(CompositionRegistrationObserver)` (`@OptIn(ExperimentalComposeRuntimeApi::class)`, nullable return handle — the same trap as `Composition.setObserver()`). Demonstrates live that attaching the observer replays `onCompositionRegistered` for every composition already registered at that instant rather than only future events, and that each `LazyColumn` item is its own subcomposition so adding/removing list rows or scrolling produces real-time registration/unregistration events. A dedicated pitfall card contrasts `RecomposerInfo.changeCount` against a manual click counter side by side — device measurement showed `changeCount` stays `0` across clicks and scrolling, so despite its name it is not a global recomposition counter
  - Perfetto Coroutine/Flow Tracing — a new axis distinct from the project's existing performance examples (Inline Value Class / Stability Annotations are compile-time concepts; this is runtime timing observability). Contrasts `Trace.beginSection`/`endSection`, which are paired via a per-thread stack and therefore only safe within a single thread, against `Trace.beginAsyncSection`/`endAsyncSection`, which correlate by `(label, cookie)` and stay safe across thread hops, plus `Trace.setCounter` for a time-varying counter track. The centerpiece is a live, on-device demonstration rather than an assertion: launching 8 concurrent spans on `Dispatchers.Default` and recording `Thread.currentThread().name` immediately before `beginSection` and immediately before the matching `endSection` (with a `delay()` suspension in between) surfaces real thread-pool worker reassignment, flagged per-entry as a mismatch; the async-section card runs the identical shape but shows the mismatch is harmless there since correlation does not depend on thread identity. Also surfaces that `androidx.tracing:tracing` was already pulled transitively into `debugRuntimeClasspath` (via another library) but was absent from `debugCompileClasspath`, so calling `Trace` directly required adding an explicit dependency — confirmed by diffing the two classpaths rather than assumed
  - IME Interactive Control — the existing `ImeStateUtil.rememberImeState()` only exposes a single boolean (`WindowInsets.isImeVisible`); this example covers the two axes it does not touch. `Modifier.imeNestedScroll()` hands the leftover scroll delta at a list's scroll bounds to the keyboard's own show/hide animation, so a drag gesture can pull the IME open or closed interactively (API 30+ only — the modifier is a documented no-op below `Build.VERSION_CODES.R`, and the example surfaces that with a live `Build.VERSION.SDK_INT` check rather than a static note). `WindowInsets.Companion.imeAnimationSource`/`imeAnimationTarget` expose the before/after inset values of an in-flight IME animation, and since `WindowInsets.Companion.ime` is the interpolated current value, `(current - source) / (target - source)` reproduces the system keyboard animation's real progress on every frame; a custom badge is offset by that same progress to demonstrate syncing arbitrary UI to it instead of guessing at a matching `animateFloatAsState` curve
- Example rework
  - Foundation Style API — reworked to actually use the experimental `androidx.compose.foundation.style` API. The previous version described this API as "bundling design tokens into one Immutable object propagated through a single CompositionLocal", which is a different topic: the real axis is per-component state-based styling (the CSS pseudo-class counterpart). Now demonstrates `Modifier.styleable` + the `Style { }` receiver DSL, the `pressed`/`hovered`/`focused`/`checked`/`selected`/`disabled` variants, the declaration-order-is-precedence trap (there is no CSS-like specificity — the last matching declaration of a property wins), `animate(spec, style)` transitions against an instant-snap control, a custom `StyleStateKey` with its own predicate, and a live recomposition count contrasting `styleable` (state read in the Modifier.Node) with conditional `Modifier` chaining (state read in composition). The token-propagation part was kept as an appendix with corrected wording and renamed types (`AppStyle` → `AppTokens`, `LocalAppStyle` → `LocalAppTokens`)
- Convention / structure cleanup
  - Replaced 16 hardcoded blog URLs in the example lists with the `blogUrl(postId)` helper so the base URL lives only in `BlogUrlHelper.kt`; updated the CLAUDE.md rule (new example starts with `""` → helper once published → raw URLs forbidden) (CONV-08)
  - Fixed the Shimmer sub-category parent to carry the group constant instead of a leaf constant, which had silently duplicated a child entry; documented the second registration path (`subCategoryList()`) as "Step 2-1" (REG-DUAL-01)
- Documentation fixes
  - Corrected the "Step 4: register routing" instructions across 4 documents — routing is a `ExampleUiRegistry.kt` map lookup (154 entries), not an `ExampleRouter.kt` when-expression, and an unregistered type does not break the build but silently falls back to a Dummy screen (DOC-ROUTE-01)
  - Added the `ui/autofill`, `ui/shader`, and `ui/style` packages to the README "Component Examples" catalog — all three existed only in the one-line 2026.05 history and were never listed in the catalog (DOC-DRIFT-06)
  - Ran the directory↔catalog diff across *every* component category instead of just `ui/`, which surfaced 12 more packages that had never been listed: `ui/media/image`, `data/repository`, `data/room`, `system/ai`, `system/security`, `system/media/video`, `system/background/location`, `system/platform/biometric`, and `architecture/development/{di,featureflag,internals,strictmode}`. Also expanded the `data/paging` entry to cover the new RemoteMediator example (DOC-DRIFT-07)
  - Added Recomposer Registry Observation to the README "Component Examples" `internals` catalog line, and found that the catalog line was never updated for Slot Tree Inspector either — it only listed How Compose Works / RememberObserver / Composition Observer even though Slot Tree Inspector shipped a full cycle earlier. Backfilled both in the same pass (DOC-DRIFT-12)

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
