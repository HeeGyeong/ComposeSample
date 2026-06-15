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
  - 2026.06: Architecture refactoring and documentation/quality improvements (domain converted to pure Kotlin(JVM), ExampleObject moved domain→app, DataCache abstraction, MainUIComponent migrated to M3, docs updated + new ARCHITECTURE/KnownLimitations/LICENSE). Upgraded to Kotlin 2.4.0 + KSP 2.3.9 (HotSwan/Compose Hot Reload disabled — incompatible with 2.4.0), ComposeBom 2026.05.00 + Material 1.11.1.
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
- Coil Compose 2.5.0

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
- **animation**: Compose animation, Shared Element Transition, AnimatedContent deep dive (tab switch, counter, state transition, transitionSpec gallery), Spring/Tween/Snap/Keyframes comparison (physics-based bounce, time-based easing, instant transition, per-segment custom)
- **bottomsheet**: BottomSheet, ModalBottomSheet, custom BottomSheet
- **drawer**: Navigation Drawer, Modal Drawer
- **flexbox**: FlexBox layout and responsive design, official FlowRow/FlowColumn Flexbox (CSS-Flexbox-inspired wrapping, maxItemsInEachRow limit, weight space distribution)
- **header**: Sticky Header tied to scroll state
- **lazycolumn**: LazyColumn performance optimization, FlingBehavior customization, targetSDK 35 support, ReverseLazyColumn, LazyStaggeredGrid waterfall grid (dynamic height, filtering animation)
- **pager**: ViewPager and page transitions
- **topappbar**: FancyTopAppBar (Collapsing Toolbar, various scroll behaviors)
- **adaptive**: Adaptive Layout — adaptive layouts for phone/tablet/foldable via WindowSizeClass (Compact/Medium/Expanded)
- **custom**: Custom Layout — custom layout that measures/places directly with the Layout composable and MeasurePolicy

**media**:
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
- **button**: ButtonGroup (Material 3 Expressive)
- **canvas**: Canvas shapes & animation, Dial component, Motion Blur (spinning wheel), Compose Loaders mathematical-curve loading animations (Rose/Lissajous/Lemniscate/Spirograph/Cardioid/Butterfly — 6 curves)
- **graphics**: New Shadow API (Compose 1.9)
- **navigation**: Navigation3 nested routing (NestedRoutesNav3)
- **notification**: SnapNotify (Snackbar simplification library)
- **scroll**: custom TopAppBarScrollBehavior, nested scroll
- **shapes**: CardCorners (corner styles)
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
- **paging**: paging and infinite scroll
- **sse**: Server-Sent Events and real-time data streaming

### **system** - system integration & platform
**platform**:
- **file**: file selection and SAF (Storage Access Framework) handling
- **haptic**: Haptic Feedback (LocalHapticFeedback vs HapticFeedbackConstants comparison and per-API-level support range)
- **intent**: Intent handling and data sharing between apps
- **language**: localization, system language settings, in-app language change
- **powersave**: power-save mode detection and battery optimization
- **predictiveback**: Predictive Back Gesture (Android 14+ Flow-based real-time animation of edge-swipe progress)
- **quicksettings**: Quick Settings Tile
- **shortcut**: app shortcuts (dynamic, static, pin)
- **version**: Android SDK version handling (targetSDK 34 permission handling)
- **webview**: WebView implementation and JavaScript interface

**deeplink**:
- **Dynamic App Links**: control deep-linking behavior in real time without an app update via the server's Digital Asset Links JSON (Android 15+)

**media**:
- **ffmpeg**: video/audio encoding/decoding (commented out due to library compatibility issues as of 2025.06)
- **recorder**: audio/video recording and media recording state management

**background**:
- **workmanager**: background work and task scheduling

**ui**:
- **widget**: Glance widget (App Widget)

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
- **flow**: FlatMap vs FlatMapLatest comparison
- **init**: initialization logic and state management, app startup optimization (App Startup / Baseline Profile / Koin lazy initialization)
- **language**: Sealed Class Interface (type-safe hierarchy), Name-Based Destructuring (Kotlin 2.3.20 name-based destructuring)
- **performance**: Inline Value Class (performance optimization), Stability Annotations (preventing unnecessary recomposition with @Stable/@Immutable)
- **preview**: Compose Preview features, @Preview internals (rendering pipeline, LocalInspectionMode, MultiPreview), Preview-only Annotation (restricting Preview-only Composables at compile time with @RequiresOptIn)
- **rebound**: role-based recomposition budget monitoring
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
