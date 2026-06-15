# Changelog

Version and example-addition history for ComposeSample, newest first.
(See the "Latest updates" section in `README.md` for a summary.)

---

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
- Versions: upgraded to ComposeBom 2026.05.00 + Material 1.11.1

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
  - ⚠️ Temporarily disabled on the `chore/kotlin-2.4-upgrade` branch: HotSwan 1.2.1 is incompatible with Kotlin 2.4.0 (compiler-extension `ClassCastException`). Active on `main` (Kotlin 2.3.20). See `docs/devtools/ComposeHotReloadGuide.md`.
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
