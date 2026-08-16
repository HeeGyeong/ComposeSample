# ComposeSample

## Table of Contents
- [Introduction](#introduction)
- [Development Environment](#development-environment)
- [Key Libraries](#key-libraries)
- [Project Structure](#project-structure)
- [Cursor Rules Setup](#cursor-rules-setup)
- [Key Components](#key-components)
- [Key Features](#key-features)
- [Component Examples](#component-examples)
- [Notes](#notes)
- [More Details](#more-details)

## Introduction
A project that collects samples of issues encountered while studying and applying Jetpack Compose in practice, along with various commonly used features.

It is built on Clean Architecture, and components are systematically classified by feature so you can easily find the example you want.

- **Latest updates** (full history in [CHANGELOG.md](CHANGELOG.md))
  - 2026.08: New examples added (LazyList `contentType` Reuse-Pool Trap, Compose Grid API, Compose MediaQuery API, Paging3 `RemoteMediator` offline-first paging, Composition Observer — runtime observation APIs answering *why* a scope recomposed rather than how many times, with a device-measured matrix showing `registerGlobalWriteObserver` and `observeSnapshots` cover complementary write paths, Slot Tree Inspector — walks `compositionData` and resolves each slot group to a function name/file/line/parameters via `parseSourceInformation`, measuring on device that reading mid-composition hides whatever is currently being built, that `collectParameterInformation()` is what makes names appear at all, and that each `LazyColumn` item is a separate subcomposition), 2D Path Animation (Arc / Spline) — the path-shape axis rather than the value-over-time axis, drawing each spec's real output by sampling it with `TargetBasedAnimation`, with device measurements showing `ArcLinear` is coordinate-identical to `tween`, that `keyframes` and `keyframesWithSpline` both hit the waypoint exactly and differ only in the corner, and that `using ArcMode` applies to the segment leaving a keyframe; also pins the opt-in boundary — `keyframesWithSpline` and `using ArcMode` are stable while `ArcAnimationSpec` and `DeferredTargetAnimation` are not), Flow Layout Overflow Control — `maxLines` plus `FlowRowOverflow.expandIndicator`/`expandOrCollapseIndicator` "+N more" indicators and `ContextualFlowRow`'s index-based lazy composition, with a `SideEffect`-instrumented side-by-side comparison proving plain `FlowRow` composes every item regardless of `maxLines` while `ContextualFlowRow` only composes what actually gets placed; the `overflow`-accepting overload is `@Deprecated` in the project's resolved foundation-layout 1.11.1 with no replacement, so `@Suppress("DEPRECATION")` is scoped to just those call sites), Recomposer Registry Observation — one level above the existing internals examples (Composition Observer = why one scope invalidated, Slot Tree Inspector = what shape one composition's slot tree is): via `Recomposer.runningRecomposers` (no opt-in needed) and `RecomposerInfo.observe(CompositionRegistrationObserver)` (`@OptIn(ExperimentalComposeRuntimeApi::class)`, nullable handle), shows live that attaching replays registration for every already-live composition rather than only future events, that each `LazyColumn` item is its own subcomposition producing real-time register/unregister events, and that `RecomposerInfo.changeCount` — despite its name — measured `0` across clicks and scrolling on device, so it is not a global recomposition counter). Convention/structure cleanup — 16 hardcoded blog URLs replaced with the `blogUrl(postId)` helper (CONV-08) and the Shimmer sub-category parent fixed to carry the group constant (REG-DUAL-01). Corrected the "register routing" instructions across 4 documents: routing is an `ExampleUiRegistry.kt` map lookup, not an `ExampleRouter.kt` when-expression, and an unregistered type silently falls back to a Dummy screen (DOC-ROUTE-01). Reworked the Foundation Style API example to use the real `androidx.compose.foundation.style` API (`Modifier.styleable` + `Style { }` DSL, state variants, `animate()` transitions, custom `StyleStateKey`) and corrected its description — the previous version explained an unrelated design-token propagation pattern, which is kept as an appendix. Added the missing `ui/autofill`, `ui/shader`, and `ui/style` packages to the Component Examples catalog (DOC-DRIFT-06), then extended that directory↔catalog diff beyond `ui/` to every category and filled in 12 more never-listed packages — `ui/media/image`, `data/repository`, `data/room`, `system/ai`, `system/security`, `system/media/video`, `system/background/location`, `system/platform/biometric`, `architecture/development/di`, `featureflag`, `internals`, `strictmode` (DOC-DRIFT-07).
  - 2026.07: New examples added (Realtime Waveform Canvas, Background Location Tracking, Screenshot Detection, Advanced Repository Pattern, RememberObserver/Composition Lifecycle, Media3(ExoPlayer) Video Playback, IPC/Exported Component Security Diagnostics). Removed the legacy subsystem entirely (24 files + 10 Activities, incl. its RxJava dependency). Dead code/doc cleanup (45 unreferenced `*Guide.kt` files, dead Koin registrations, dead functions/imports/dependencies, reference URLs moved out of `*ExampleUI.kt` into `exampleGuide.kt`) and an architecture cleanup (`AndroidViewModel` → plain `ViewModel` where `Application` was unused, Koin `named()` qualifier for the Ktor client). Migrated deprecated APIs (Material3 Tab family, Compose test rules → `junit4.v2`) and restored the instrumentation source set after a year of stale imports (TEST-STALE-01, GRADLE-SCOPE-01). Fixed two `startForeground()` 5-second contract violations in the new location service (FGS-CONTRACT-01). Migrated the image-loading stack from Glide + Coil2 to Coil3 as the sole loader. Consolidated a byte-identical `SectionCard` composable duplicated across 4 security example files into a shared component. Reached zero kotlinc warnings across all modules and source sets.
  - 2026.06: Architecture refactoring and documentation/quality improvements (domain converted to pure Kotlin(JVM), ExampleObject moved domain→app, DataCache abstraction, MainUIComponent migrated to M3, docs updated + new ARCHITECTURE/KnownLimitations/LICENSE). New examples added (Kotlin 2.4 Language Features, How Compose Works, Coil 3 Image Loading, Preview-Driven Screenshot Testing, Freehand Drawing). Upgraded to Kotlin 2.4.0 + KSP 2.3.9 (HotSwan/Compose Hot Reload disabled — incompatible with 2.4.0), ComposeBom 2026.05.00 + Material 1.11.1.
  - 2026.05: New examples added (Accessible Focus Indicator, Document Editing TextField, Syntax Highlighting, Particle Emitter, Animations Showcase, Hardware-Backed Keystore, Shared Element Debug Tooling, Foundation Style API, Month Picker Dial, App Security, AGSL Shader, Type-Safe Feature Flag, Per-Item ViewModels, Room FTS4/Indices/Multi-Table, etc.)
  - 2026.04: New examples added (Adaptive Layout, Custom Layout, Dynamic App Links, Screenshot/Compose UI Testing, Predictive Back, Spring/Tween/Snap, Haptic, Stability Annotations, Rich Content, FlowRow/Column, Coroutine Bridges, Compose Loaders, etc.). Compose Hot Reload applied. Upgraded to Kotlin 2.3.20 + AGP 8.13.2. (Hot Reload was later disabled in 2026.06 — see below.)

## Development Environment
- Kotlin 2.4.0
- Android Studio
- AGP 8.13.2 / Gradle 8.13
- ComposeBom 2026.05.00
- Target SDK 35
- Min SDK 24
- Java 21

## Key Libraries
- Room 2.8.4
- Koin 3.2.2
- WorkManager 2.9.1
- ViewModel 2.9.1
- Material 1.11.1
- Material3 1.4.0
- Lottie Compose 6.0.0
- Coil3 3.1.0

## Project Structure

```
ComposeSample
├── app
│ ├── presentation # UI layer (Activity, Compose UI)
│ │ ├─ example # example feature package
│ │ │ └─ component # component examples
│ │ │   ├── ui # UI components & layout
│ │ │   ├── interaction # user interaction & gestures
│ │ │   ├── navigation # navigation
│ │ │   ├── data # data management & network
│ │ │   ├── system # system integration & platform
│ │ │   └── architecture # architecture & dev tools
│ │ └─ legacy  # legacy feature package
│ ├── coordinator # Coordinator pattern Initializer
│ ├── di # dependency injection
│ ├── util # utility classes
│ └── model # UI model classes
│
├── Coordinator
│ └── coordinator # Coordinator pattern Initializer
│
├── Core
│ └── navigation # Coordinator interface
│
├── data
│ ├── api # API interfaces
│ ├── repository # Repository implementations
│ ├── db # local database
│ └── model # Data models
│
└── domain
  ├── repository # Repository interfaces
  ├── usecase # UseCase definitions
  └── model # domain models
 
```

## AI Coding Assistant Setup

This project provides **AI coding assistant rules** for both **Cursor IDE** and **Claude Code** users.

### Claude Code
The `CLAUDE.md` file at the project root defines architecture rules, file naming conventions, how to add examples, and more, and is loaded automatically at the start of a Claude Code session.

### Cursor Rules Setup

**AI coding assistant rules** are also provided for developers using Cursor IDE.

The `.cursor/rules` directory contains 9 mdc files so that Cursor AI can automatically understand the project's architecture and coding style and generate consistent code.

### Rules File Layout

```
.cursor/rules/
├── data-rules.mdc                  # Data class implementation rules
├── api-creation-guide.mdc          # API creation guide
├── api-ui-binding.mdc              # API-UI binding rules
├── code-style.mdc                  # Kotlin & Compose code style
├── comprehensive-ui-guide.mdc      # Comprehensive UI system guide
├── dependency-management.mdc       # Koin dependency injection guide
├── performance-optimization.mdc    # Performance optimization guide
├── project-structure.mdc           # Clean Architecture structure guide
└── testing-guide.mdc               # Testing guide
```

### Key Rule Topics

- **Architecture**: Clean Architecture + MVVM pattern
- **UI framework**: Jetpack Compose + Material3
- **Dependency injection**: Koin framework
- **Coding style**: Kotlin naming conventions, Korean comments
- **Data classes**: @SerializedName, @Parcelize annotation rules
- **Performance**: Compose recomposition, memory management
- **Testing**: unit test, UI test coverage

### Rule Document Reference

Rule content is split across two locations.

- **`app/src/main/java/com/example/composesample/docs/`** — detailed rule documents for humans/Claude Code (`DataRules`, `DIRules`, `UIRules`, etc.) and prompt guides.
- **`.cursor/rules/*.mdc`** — Cursor IDE-only rules (with frontmatter, in English). Four of them — `code-style`, `performance-optimization`, `project-structure`, `testing-guide` — **exist only in `.cursor/rules`** and have no counterpart under docs/.

The two sources only partially map 1:1 by topic, so refer to both when checking rules. (Document index: `docs/README.md`)

## Key Components
- **MainActivity**: the most basic Compose usage example
- **BlogExampleActivity**: a variety of feature implementations applicable in practice
  - BottomSheet
  - Navigation Drawer
  - LazyColumn
  - WorkManager
  - Permission handling
  - WebView
  - Drag & Drop
  - and many other practical examples

## Key Features
1. **UI components**
   - Various UI component examples such as BottomSheet, Navigation Drawer
   - UI preview using Compose Preview
   - Custom animations and transition effects

2. **State management**
   - State management using ViewModel
   - Handling Compose State and Side Effects
   - Using LaunchedEffect, RememberCoroutineScope

3. **Performance optimization**
   - LazyColumn optimization
   - Preventing memory leaks
   - Minimizing recomposition

## Component Examples

### **ui** - UI components & layout
**layout**:
- **animation**: Compose animation, Shared Element Transition, AnimatedContent deep dive (tab switch, counter, state transition, transitionSpec gallery), Spring/Tween/Snap/Keyframes comparison (physics-based bounce, time-based easing, instant transition, per-segment custom), 2D path animation (`ArcAnimationSpec`/`ArcMode` arcs, `keyframesWithSpline` waypoint smoothing, per-segment `using ArcMode`, `DeferredTargetAnimation` + `approachLayout` — paths drawn by sampling the spec itself)
- **bottomsheet**: BottomSheet, ModalBottomSheet, custom BottomSheet
- **drawer**: Navigation Drawer, Modal Drawer
- **flexbox**: FlexBox layout and responsive design, official FlowRow/FlowColumn Flexbox (CSS-Flexbox-inspired wrapping, maxItemsInEachRow limit, weight space distribution), Flow overflow control (`maxLines`, `FlowRowOverflow.expandIndicator`/`expandOrCollapseIndicator`, `ContextualFlowRow` index-based lazy composition, device-measured composed-item-count contrast against plain `FlowRow`)
- **header**: Sticky Header tied to scroll state
- **lazycolumn**: LazyColumn performance optimization, FlingBehavior customization, targetSDK 35 support, ReverseLazyColumn, LazyStaggeredGrid waterfall grid (dynamic height, filtering animation), LazyList `contentType` reuse-pool trap (per-item contentType explodes reuse buckets so slots are never reclaimed — measured with `WeakReference` after GC)
- **pager**: ViewPager and page transitions
- **topappbar**: FancyTopAppBar (Collapsing Toolbar, various scroll behaviors)
- **adaptive**: Adaptive Layout — adaptive layouts for phone/tablet/foldable via WindowSizeClass (Compact/Medium/Expanded); Compose MediaQuery API — declarative environment queries (Compose 1.11 experimental) covering window size, foldable posture, pointer precision, keyboard kind and viewing distance
- **custom**: Custom Layout — custom layout that measures/places directly with the Layout composable and MeasurePolicy
- **grid**: Compose Grid API — non-lazy 2D track layout (Compose 1.11 experimental): 6 track sizes + minmax, gap, automatic vs explicit `gridItem` placement with spans, `GridFlow` direction, and a live comparison against `LazyVerticalGrid` showing Grid composes every child
- **modifier**: Modifier Order — how modifier ordering changes layout, drawing, and hit-testing

**media**:
- **image**: Coil 3 image loading (AsyncImage, GIF decoding, caching and placeholder/error states)
- **lottie**: Lottie animation implementation and control
- **picker**: Embedded Photo Picker, BottomSheet integration and URI lifetime management
- **shimmer**: UI Shimmer, Text Shimmer loading effects

**text**:
- Text styling, AutoSizing, custom TextMeasurer rendering
- TextOverflow (Start/Middle Ellipsis), LocalContext string anti-pattern
- Rich Content in Text Input (pasting images/files via contentReceiver — handling by source: keyboard, clipboard, drag & drop)
- TextField Max Length hidden bug (a bug where InputTransformation is not applied to programmatic changes + the LaunchedEffect+snapshotFlow solution)

**material3**:
- Material 3 Expressive (new in 1.4.0) — SecureTextField/OutlinedSecureTextField (password input + 3 TextObfuscationModes)

**others**:
- **accessibility**: Large Content Viewer (iOS-style accessibility, keyboard & screen reader support)
- **autofill**: Compose Autofill via the semantics API (`contentType` hints + `LocalAutofillManager` commit/cancel)
- **button**: ButtonGroup (Material 3 Expressive)
- **canvas**: Canvas shapes & animation, Dial component, Motion Blur (spinning wheel), Compose Loaders mathematical-curve loading animations (Rose/Lissajous/Lemniscate/Spirograph/Cardioid/Butterfly — 6 curves)
- **graphics**: New Shadow API (Compose 1.9)
- **navigation**: Navigation3 nested routing (NestedRoutesNav3)
- **notification**: SnapNotify (Snackbar simplification library)
- **scroll**: custom TopAppBarScrollBehavior, nested scroll
- **shader**: AGSL Shader Live Tuning (API 33+ `RuntimeShader` + `graphicsLayer` renderEffect, live uniform sliders and shader-source recompilation)
- **shapes**: CardCorners (corner styles)
- **style**: Foundation Style API (Compose 1.11 experimental) — `Modifier.styleable` + `Style { }` DSL, state variants, `animate()` transitions, custom `StyleStateKey`
- **tab**: ResponsiveTabRow (SubcomposeLayout responsive tabs)
- **visibility**: Visibility handling patterns

### **interaction** - user interaction & gestures
- **clickevent**: handling various click events and duplicate prevention
- **drag**: LazyColumn drag and drop with item reordering
- **refresh**: Pull-to-Refresh implementation and refresh animation
- **sticker**: sticker canvas (drag, pinch resize, rotate, spring physics, peel-off animation)
- **swipe**: Swipe to Dismiss, Material 3 SwipeToDismissBox

### **navigation** - navigation
- Bottom Navigation implementation
- Navigation3 (new Navigation component)
- NestedRoutesNav3 (nested routing)

### **data** - data management & network
- **api**: Retrofit API calls, UseCase pattern, disconnection handling
- **cache**: Room local data caching and CRUD, real-time search
- **paging**: paging and infinite scroll; Paging3 `RemoteMediator` offline-first paging (network + DB dual source with the DB as the single source of truth — `LoadType` REFRESH/PREPEND/APPEND branching, a RemoteKeys table, `initialize()` cache gating, and `loadState.source` vs `loadState.mediator` as two separate axes)
- **repository**: Advanced Repository Pattern — Memory → Disk → Network multi-source priority resolution and cache population
- **room**: Room `@Fts4` MATCH search vs `LIKE '%q%'` full scan, `@Index` single/composite index query performance, multi-table insert via DAO interface inheritance + `withTransaction`
- **sse**: Server-Sent Events and real-time data streaming

### **system** - system integration & platform
**platform**:
- **file**: file selection and SAF (Storage Access Framework) handling
- **haptic**: Haptic Feedback (LocalHapticFeedback vs HapticFeedbackConstants comparison and per-API-level support range)
- **intent**: Intent handling and data sharing between apps
- **language**: localization, system language settings, in-app language change
- **powersave**: power-save mode detection and battery optimization
- **predictiveback**: Predictive Back Gesture (Android 14+ Flow-based real-time animation of edge-swipe progress)
- **biometric**: Biometric Authentication (biometric-compose alpha — Compose integration)
- **quicksettings**: Quick Settings Tile
- **shortcut**: app shortcuts (dynamic, static, pin)
- **version**: Android SDK version handling (targetSDK 34 permission handling)
- **webview**: WebView implementation and JavaScript interface

**deeplink**:
- **Dynamic App Links**: control deep-linking behavior in real time without an app update via the server's Digital Asset Links JSON (Android 15+)

**media**:
- **ffmpeg**: video/audio encoding/decoding (commented out due to library compatibility issues as of 2025.06)
- **recorder**: audio/video recording and media recording state management
- **video**: Media3 (ExoPlayer) video playback — integrating `PlayerView` into Compose for network video

**background**:
- **location**: Background Location Tracking — a real `foregroundServiceType="location"` service, permission handling as a sequence (foreground → notifications → background), contrasted with `CoroutineWorker` to show why WorkManager cannot replace continuous tracking
- **workmanager**: background work and task scheduling

**ui**:
- **widget**: Glance widget (App Widget)

**others**:
- **ai**: Gemini Nano on-device inference (AICore)
- **security**: App Security diagnostics (certificate pinning, Play Integrity mock), Hardware-Backed Keystore, IPC/Exported Component security, Screenshot Detection

### **architecture** - architecture & dev tools
**pattern**:
- **compositionLocal**: CompositionLocal basics, Static/Dynamic comparison, tree visualization
- **coroutine**: coroutine basics, internals, withContext vs launch comparison
- **effect**: Side Effect handling (LaunchedEffect, SideEffect, SnapshotFlow, etc.)
- **mvi**: MVI architecture pattern and unidirectional data flow
- **remember**: rememberSaveable (survives rotation), rememberUpdatedState (latest callback), derivedStateOf (computation optimization) comparison
- **retain**: state retention without a ViewModel via the Compose retain API (Compose 1.10)

**development**:
- **compose17**: Compose 1.7 new features (Graphics Layer, Path Graphics, LookaheadScope, etc.)
- **concurrency**: coroutine internals, withContext pattern, Coroutine Bridges (converting callback-based APIs to suspend functions with suspendCoroutine/suspendCancellableCoroutine)
- **coordinator**: Coordinator Pattern implementation
- **cursor**: Cursor IDE-related examples (using AI coding assistants)
- **di**: Koin Compiler Plugin (compile-time DI resolution without KSP)
- **featureflag**: Type-Safe Feature Flag (compile-time safe flag definition and rollout control)
- **flow**: FlatMap vs FlatMapLatest comparison
- **init**: initialization logic and state management, app startup optimization (App Startup / Baseline Profile / Koin lazy initialization)
- **internals**: How Compose Works (Composition/Layout/Draw phases), RememberObserver and composition lifecycle (onRemembered/onForgotten/onAbandoned measured live), Composition Observer (`CompositionObserver` causal log of which state invalidated which scope, contrasted with the `Snapshot` observation APIs and their complementary write-path coverage), Slot Tree Inspector (walks `compositionData` and resolves each slot group to a function name/file/line/parameters via `parseSourceInformation`), Recomposer Registry Observation (`Recomposer.runningRecomposers` + `RecomposerInfo.observe(CompositionRegistrationObserver)` to watch compositions register/unregister process-wide)
- **language**: Sealed Class Interface (type-safe hierarchy), Name-Based Destructuring (Kotlin 2.3.20 name-based destructuring)
- **performance**: Inline Value Class (performance optimization), Stability Annotations (preventing unnecessary recomposition with @Stable/@Immutable)
- **preview**: Compose Preview features, @Preview internals (rendering pipeline, LocalInspectionMode, MultiPreview), Preview-only Annotation (restricting Preview-only Composables at compile time with @RequiresOptIn)
- **rebound**: role-based recomposition budget monitoring
- **strictmode**: StrictMode policy-violation detection (main-thread disk/network I/O, leaked closeables)
- **test**: UI test TDD, recomposition detection, Coroutine Flow Testing (Turbine), Screenshot Testing (Paparazzi/Roborazzi), Compose UI Testing (test pattern guide for createComposeRule, onNodeWithTag, performClick, etc.)
- **type**: variable type usage and compile-time optimization

**others**:
- **lifecycle**: AutoCloseable (automatic resource cleanup)
- **modularization**: modularization strategy
- **navigation**: Navigation3, NestedRoutesNav3
- **state**: SnapshotFlow (State → Flow conversion), Compose Snapshot System (State<T> internals — Snapshot isolation model, derivedStateOf optimization, atomic state change with withMutableSnapshot)

### **etc.**
- Various other examples likely to be used in practice

## Notes
- Some examples (e.g. permission-related) may require basic setup
- On Compose versions 1.4.0-alpha04 or lower, there may be keyboard-related issues
- The basic logic needed for real apps is implemented so it can be reused as-is
- As library versions are updated, some implemented features may stop working
- Examples that are no longer version-compatible are not removed but kept fully commented out
- **API keys**: external API keys such as the Naver API must be set separately in `local.properties` (`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)
- **Cursor Rules**: the mdc files in `.cursor/rules` only work in Cursor IDE and have no effect in other IDEs

## More Details
- **Installing and running the app**: cloning the project and building/installing the app directly lets you see the various components and UI examples on a real device, which is more convenient. Try out animations, gestures, and interactions that are hard to grasp from code alone.
- **Example explanations**: detailed explanations for each example are available on the [Tistory Blog](https://heegs.tistory.com/category/Android/Jetpack).
- **Rule documents**: detailed rules are split across `app/src/main/java/com/example/composesample/docs/` (for humans/Claude) and `.cursor/rules/*.mdc` (Cursor-only). The two sources only partially map, so refer to both. See `docs/README.md` for the full document list.
- **AI coding assistant**: rules applied automatically when using Cursor IDE help generate consistent code.

## License

This project is licensed under the [MIT License](LICENSE). You are free to use it for learning and reference.
