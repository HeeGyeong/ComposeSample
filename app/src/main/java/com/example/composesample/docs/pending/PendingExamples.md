# Pending Example List (based on Android Weekly #723) — ✅ all items complete (archive)

> Written: 2026-04-20
> Source: https://androidweekly.net/issues/issue-723
> **Status: all items in this document have been handled (completed/excluded). Pending example management has moved to the Claude Code memory (`project_pending_examples.md`); this document is kept as the #723 review log.**

The final handling results for the 5 items selected from reviewing Android Weekly #723.

---

## 🔴 High priority

### 1. Navigation 3 ViewModel Scope ✅ Done (commit: 5b9b954a)
- **Category**: `architecture/navigation/`
- **File name**: `Nav3ViewModelScopeExampleUI.kt`
- **Content**: Nav2 Auto-Scope vs Nav3 default behavior vs NavKey-based Store mapping restoration — simulated in pure Compose state (avoiding the Nav3 alpha dependency)
- **Source**: medium.com/@domen.lanisnik (Issue #723)

### 2. Koin Compiler Plugin (Compile Safety) ✅ Done (commit: a1582247)
- **Category**: `architecture/development/di/`
- **File name**: `KoinCompilerPluginExampleUI.kt`
- **Content**: manual DSL vs annotations (@Module/@Single/@Factory/@KoinViewModel/@Named) comparison, KSP-generated code preview, gradual migration strategy
- **Source**: blog.insert-koin.io (Issue #723)
- **Related doc**: `docs/di/DIRules.md`

---

## 🟡 Medium priority

### 3. Gemini Nano On-Device AI (ML Kit GenAI) ✅ Done (commit: 2f4db6b4)
- **Category**: `system/ai/`
- **File name**: `GeminiNanoExampleUI.kt`
- **Content**: 4-state Feature Availability simulation + Summarization Mock + Nano↔Cloud hybrid routing code (Mock responses to avoid a real-device dependency)
- **Source**: medium.com/@Y4583L (Issue #723)

### 4. Halogen — Runtime Material 3 Theme Generation ❌ Excluded
- **Reason**: external library + AI API key dependency. Excluded because the Foundation Style API (#23) covers design tokens with a self-contained implementation.
- **Source**: github.com/himattm/halogen (Issue #723)

### 5. Airbnb Month Picker Dial (based on ChromaDial) ✅ Done (commit: 474cae52)
- **Category**: `ui/canvas/`
- **File name**: `MonthPickerDialExampleUI.kt`
- **Content**: Canvas polar coordinates (cos/sin) placement + atan2 angle computation + detectDragGestures + spring snap animation + snapshotFlow selection sync
- **Source**: sinasamaki.com (Issue #723)

---

## Progress Principles (preserved)

1. Proceed in priority order
2. Follow the 4-step "How to Add a New Example" in CLAUDE.md for each example
3. After completion, verify build → commit → push
4. Mark completed items with ✅ + commit hash in the `project_pending_examples.md` memory

> New candidates from later issues (Android Weekly #724 onward) are managed in the `project_pending_examples.md` memory, not in this file.
