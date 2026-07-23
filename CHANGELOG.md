# Changelog

Version and example-addition history for ComposeSample, newest first.
(See the "Latest updates" section in `README.md` for a summary.)

---

## 2026.07
- New examples added
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
- Architecture cleanup
  - Converted `RefreshViewModel`/`DataCacheViewModel` from `AndroidViewModel` to plain `ViewModel` — neither used the injected `Application` (VM-ANDROIDVM-01)
  - Consolidated a byte-identical `SectionCard` composable duplicated across 4 `system/security` example files into a shared `SecurityUiComponents.kt` (CODE-DUP-01)
- Dependency migration
  - Removed Glide and Coil2 entirely; migrated to Coil3 (`coil3`, `coil3-gif`) as the sole image loader across `FlexBoxUI`/`LottieExampleUI` (DEP-VERSION-01)

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
