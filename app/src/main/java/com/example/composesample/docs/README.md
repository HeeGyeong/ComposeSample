# 📚 docs Index

A collection of rule/guide documents for the ComposeSample project, organized by audience and purpose.

## Rule documents (for humans / Claude Code)

| Document | Content | Matching `.cursor/rules` |
|----------|---------|--------------------------|
| [`ui/UIRules.md`](ui/UIRules.md) | UI system guide — ViewModel/UI separation, one ViewModel per top-level feature, state management, performance optimization | `comprehensive-ui-guide.mdc` |
| [`di/DIRules.md`](di/DIRules.md) | DI architecture — Koin module specs, named qualifiers, scopes, error handling | `dependency-management.mdc` |
| [`data/DataRules.md`](data/DataRules.md) | Data classes — naming, @SerializedName/@Parcelize, nullability/defaults | `data-rules.mdc` |

## Prompt guides (for AI code generation)

| Document | Content | Matching `.cursor/rules` |
|----------|---------|--------------------------|
| [`prompt/CreateAPIGuide.md`](prompt/CreateAPIGuide.md) | API creation prompt guide | `api-creation-guide.mdc` |
| [`prompt/CreateAPIAndUIBindingGuide.md`](prompt/CreateAPIAndUIBindingGuide.md) | API + UI binding guide | `api-ui-binding.mdc` |
| [`prompt/CreateUICodeSnippet.md`](prompt/CreateUICodeSnippet.md) | UI code generation snippet | (none) |

## Tooling / operations guides

| Document | Content |
|----------|---------|
| [`claudecode/ClaudeCodeGuide.md`](claudecode/ClaudeCodeGuide.md) | Using Claude Code — CLAUDE.md/Memory/Hooks/prompt patterns |
| [`devtools/ComposeHotReloadGuide.md`](devtools/ComposeHotReloadGuide.md) | Compose Hot Reload(HotSwan) install/behavior/version requirements |
| [`pending/PendingExamples.md`](pending/PendingExamples.md) | (archive) Android Weekly #723 example candidate review log |

## Project root documents

| Document | Content |
|----------|---------|
| [`/CLAUDE.md`](../../../../../../../../CLAUDE.md) | Claude Code project guide — tech stack, 4-step example addition, workflow rules |
| [`/README.md`](../../../../../../../../README.md) | project introduction, dev environment, component example list |
| `/ARCHITECTURE.md` | architecture decision record (layer rules, ARCH refactoring background) |
| `/CHANGELOG.md` | version/example-addition change history |
| `/docs/KnownLimitations.md` (this folder) | known limitations/deferrals (deferred version upgrades, etc.) |

## Relationship to `.cursor/rules`

`.cursor/rules/*.mdc` are **Cursor IDE-only** rule files (English, with frontmatter). They only partially map 1:1 by topic to the docs above, and the following 4 **exist only in `.cursor/rules`** with no counterpart under docs/:

- `code-style.mdc` — Kotlin & Compose code style
- `performance-optimization.mdc` — performance optimization
- `project-structure.mdc` — Clean Architecture structure
- `testing-guide.mdc` — testing guide

When checking rules, refer to both locations.
