# ComposeSample App Module

## Overview
A module that contains UI implementation examples using Jetpack Compose and a variety of feature implementations. Designed according to Clean Architecture principles.

## Project Structure
```
app
├── application       # Application class for Koin initialization
├── coordinator       # Coordinator pattern Initializer
├── di                # dependency injection modules
├── docs              # source rule/guide documents (origin of Cursor Rules)
├── presentation      # UI layer (Activity, Compose UI)
│   ├── example       # collection of example screens
│   │   ├── component # example components by category
│   │   │   ├── ui              # UI component examples
│   │   │   ├── interaction     # interaction examples
│   │   │   ├── data            # data handling examples
│   │   │   ├── system          # system/platform API examples
│   │   │   └── architecture    # architecture pattern examples
│   │   ├── list      # example list (classified by year)
│   │   └── model     # app-only models (ExampleObject/ExampleMoveType, moved domain→app by ARCH-05)
│   └── legacy        # screens implemented in the older style
└── util              # utility classes
```
> The local database (Room) lives in the `data` module, not in app.

## Main Screens
- **MainActivity**: the main screen that navigates to example code and legacy code
- **BlogExampleActivity**: the list screen of various feature implementation examples

## Example Categories

### UI components (`component/ui`)
| Package | Content |
|---------|---------|
| `accessibility` | accessibility support (Large Content Viewer, Accessible Focus Indicator) |
| `button` | button styles and interaction (ButtonGroup) |
| `canvas` | Canvas shapes/animation, Dial, Compose Loaders math curves, Particle Emitter, Month Picker Dial |
| `graphics` | graphics effects such as Motion Blur, New Shadow API (Compose 1.9) |
| `shader` | AGSL Shader Live Tuning (real-time RuntimeShader uniform tweaking) |
| `style` | Foundation Style API (single-CompositionLocal design tokens, Compose 1.11) |
| `material3` | Material 3 Expressive (new 1.4.0 components such as SecureTextField) |
| `layout/animation` | AnimatedVisibility/AnimatedContent, Shared Element (+Debug Tooling), Spring/Tween/Snap, Animations Showcase |
| `layout/adaptive` | Adaptive Layout (WindowSizeClass adaptive) |
| `layout/custom` | Custom Layout (Layout composable + MeasurePolicy) |
| `layout/bottomsheet` | ModalBottomSheet, Collapsed BottomSheet |
| `layout/drawer` | side Drawer menu |
| `layout/flexbox` | official FlowRow/FlowColumn Flexbox |
| `layout/header` | Fancy TopAppBar, scroll-linked header |
| `layout/lazycolumn` | LazyColumn optimization, ReverseLazyColumn, LazyStaggeredGrid |
| `layout/modifier` | Modifier Order in Compose |
| `layout/pager` | HorizontalPager, VerticalPager |
| `layout/topappbar` | Custom TopAppBarScrollBehavior |
| `media/image` | image loading and handling |
| `media/lottie` | Lottie animation |
| `media/picker` | Photo Picker, Embedded Photo Picker |
| `media/shimmer` | loading Shimmer effect |
| `navigation` | Navigation3 nested routing (NestedRoutesNav3) |
| `notification` | SnapNotify (Snackbar simplification) |
| `scroll` | custom scroll behavior, nested scroll |
| `shapes` | CardCorners (corner styles) |
| `tab` | Responsive TabRow (SubcomposeLayout) |
| `text` | text styling/AutoSizing, Rich Content, TextField Max Length, Document Editing, Syntax Highlighting |
| `visibility` | conditional show/hide |

### Interaction (`component/interaction`)
| Package | Content |
|---------|---------|
| `clickevent` | click event handling patterns |
| `drag` | drag gestures |
| `refresh` | Pull-to-Refresh |
| `sticker` | drag/pinch/rotate/spring-physics sticker canvas |
| `swipe` | Swipe to Dismiss (Material 3) |

### Data handling (`component/data`)
| Package | Content |
|---------|---------|
| `api` | Retrofit/Ktor API communication, UseCase pattern, network status detection |
| `cache` | Room local caching/CRUD/real-time search (UserCacheRepository abstraction) |
| `paging` | Paging 3 implementation, infinite scroll |
| `room` | Room deep dive — FTS4 vs LIKE search, Database Indices, Multi-Table Inserts |
| `sse` | Server-Sent Events |

### System / Platform API (`component/system`)
| Package | Content |
|---------|---------|
| `ai` | Gemini Nano on-device AI (ML Kit GenAI, Mock simulation) |
| `security` | App Security (Cert Pinning/Secure Storage/Play Integrity), Hardware-Backed Keystore |
| `background/workmanager` | WorkManager background work |
| `deeplink` | deep link handling, Dynamic App Links (Android 15+) |
| `media/ffmpeg` | FFmpeg media handling (commented out due to library compatibility issues) |
| `media/recorder` | audio/video recording |
| `platform/biometric` | biometric auth (BiometricPrompt, Compose) |
| `platform/file` | file system access (SAF) |
| `platform/haptic` | Haptic Feedback (per-API-level support comparison) |
| `platform/intent` | Intent usage |
| `platform/language` | in-app language change |
| `platform/powersave` | power-save mode handling |
| `platform/predictiveback` | Predictive Back Gesture (Android 14+) |
| `platform/quicksettings` | Quick Settings Tile |
| `platform/shortcut` | app shortcuts (dynamic/static/pin) |
| `platform/version` | per-API-version branching |
| `platform/webview` | WebView integration |
| `ui/widget` | Glance home-screen widget |

### Architecture patterns (`component/architecture`)
| Package | Content |
|---------|---------|
| `lifecycle` | lifecycle-aware components such as AutoCloseable (automatic resource cleanup) |
| `modularization` | multi-module structure example |
| `navigation` | Navigation3, NestedRoutesNav3 |
| `state` | SnapshotFlow, Compose Snapshot System, Per-Item ViewModels |
| `pattern/compositionLocal` | CompositionLocal data flow, Static/Dynamic comparison, tree visualization |
| `pattern/coroutine` | coroutine basics/internals, withContext vs launch |
| `pattern/effect` | SideEffect, LaunchedEffect, SnapshotFlow, rememberCoroutineScope |
| `pattern/mvi` | MVI architecture pattern (unidirectional data flow) |
| `pattern/remember` | rememberSaveable/rememberUpdatedState/derivedStateOf comparison |
| `pattern/retain` | Compose 1.10 Retain API (state retention without a ViewModel) |
| `development/compose17` | Compose 1.7 new features (Graphics Layer, LookaheadScope, etc.) |
| `development/concurrency` | concurrency handling, Coroutine Bridges (suspendCoroutine) |
| `development/coordinator` | Coordinator pattern screen transitions |
| `development/cursor` | Cursor IDE / AI coding assistant usage examples |
| `development/di` | Koin Compiler Plugin (annotation-based compile safety) |
| `development/featureflag` | Type-Safe Feature Flag (sealed registry + debug override) |
| `development/flow` | FlatMap vs FlatMapLatest comparison |
| `development/init` | app initialization/startup optimization (App Startup / Baseline Profile) |
| `development/language` | Sealed Class Interface, Name-Based Destructuring |
| `development/performance` | Inline Value Class, Stability Annotations |
| `development/preview` | @Preview internals, Preview-only Annotation (@RequiresOptIn) |
| `development/rebound` | recomposition budget allocation and detection |
| `development/test` | UI test TDD, recomposition detection, Coroutine Flow (Turbine), Screenshot (Paparazzi/Roborazzi), Compose UI Testing |
| `development/type` | type system examples (variable types/compile-time optimization) |

## Dependency Injection
Uses Koin. Define modules in the `di` package and register all modules in `InjectModules.kt`.

## How to Add an Example
1. Add a constant to `ConstValue.kt`
2. Add an `ExampleObject` entry to `Examples20XX.kt`
3. Create the UI file (`*ExampleUI.kt`) and `exampleGuide.kt` under the relevant `component/` category
4. Add routing to `ExampleRouter.kt`

For detailed rules, see `CLAUDE.md` (the 4-step "How to Add a New Example") and the `docs/` folder.

## Notes
- Always verify Compose-version and Kotlin-version compatibility
- Declare required permissions in `AndroidManifest.xml`
- Adding new libraries requires team discussion
- The Domain layer uses pure Kotlin only
- The Presentation layer uses Compose only
- Keep a clear separation between layers
