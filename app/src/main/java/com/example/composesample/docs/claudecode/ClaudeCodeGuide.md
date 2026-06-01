# Claude Code Usage Guide

A guide for using Claude Code effectively in this project.

---

## CLAUDE.md

A markdown file at the project root, loaded automatically at the start of a Claude Code session.
Even without explicit instructions, Claude always works with the project's coding conventions, architecture rules, and file structure in mind.

### CLAUDE.md sections in this project

| Section | Content |
|---------|---------|
| Tech stack | Kotlin 2.3.20 / ComposeBom 2026.05.00 / Target SDK 35 |
| Module structure | app / data / domain / Core / Coordinator |
| How to add an example | 4-step workflow (ConstValue → ExampleObject → UI → Router) |
| File naming | `*ExampleUI.kt` / `exampleGuide.kt` conventions |
| Build environment | `./gradlew assembleDebug` (CLI) or Android Studio |
| Commit convention | Korean + feat/fix/refactor/chore/docs prefix |

### Tips for writing CLAUDE.md

```markdown
# Project name

## Tech stack
- language, framework, key libraries

## File naming conventions
- state the rules

## How to add a new feature
- step-by-step workflow

## Build environment
- build command (CLI/IDE), toolchain, SDK path, etc.

## Commit message convention
- language, prefix rules
```

- ✅ Include concrete example code
- ✅ State what NOT to do as well
- ✅ Always document the build method/constraints (this project can be built via the `./gradlew assembleDebug` CLI)

---

## Memory System

A file-based memory system that persists across conversations.
Stored under `~/.claude/projects/<project>/memory/`.

### The 4 memory types

| Type | Purpose |
|------|---------|
| `user` | user role, preferences, knowledge level — to tailor responses |
| `feedback` | working-style corrections and confirmations — store both "don't do this" and "this is right" |
| `project` | ongoing work, deadlines, decisions — track fast-changing state |
| `reference` | locations of external systems — Linear, Slack, dashboard URLs, etc. |

### Memory usage example in this project

```markdown
---
name: pending example list
type: project
---

## 1. LazyStaggeredGrid ← can proceed immediately
- category: ui/layout/lazycolumn
- status: pending
```

The priority list is kept between example-adding tasks, so work can continue across sessions.

### What NOT to store

- Code patterns, file paths — verifiable directly from the code
- git history — `git log` / `git blame` is authoritative
- Debugging solutions — already reflected in the code
- Temporary task state — use the in-session Tasks tool

---

## Hooks

Shell commands that run automatically before/after a tool runs or on session events.
Configured in `~/.claude/settings.json` or `.claude/settings.json`.

### Main hook events

| Event | Purpose |
|-------|---------|
| `PreToolUse` | before a tool runs — validation, can block |
| `PostToolUse` | after a tool runs — auto-run formatters, linters |
| `Stop` | when Claude finishes a response — notifications, logging |
| `SessionStart` | at session start — environment initialization |
| `PreCompact` | before context compaction — confirm what to preserve |

### Example: auto-format after saving a file

```json
"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "ktlint --format $FILE"
  }]
}]
```

### settings.json configuration in this project

| Item | Value |
|------|-------|
| `attribution.commit` | `""` — removes the Co-Authored-By signature |
| `defaultMode` | `acceptEdits` — auto-approve file edits |
| `allow` | `Bash(git:*)`, `Bash(ssh:*)` — auto-allow |

---

## Cursor vs Claude Code

| Item | Cursor | Claude Code |
|------|--------|-------------|
| Rule files | `.cursorrules` / `.mdc` | `CLAUDE.md` (auto-loaded) |
| Memory | none (sessions disconnected) | file-based Auto Memory |
| Hooks | none | PreToolUse / PostToolUse, etc. |
| Permission control | none | settings.json allow/deny |
| Context | file references inside the IDE | CLI + full filesystem access |

---

## Effective Prompt Patterns in This Project

### Adding an example
```
"Plan first before adding the X example"
→ understand existing patterns, then implement consistently
```

### Check before working
```
"Read the existing related files before working"
→ prevents duplicate implementation or convention mismatch
```

### Using memory
```
"Save this list to memory"
→ keeps priorities for the next session
```

### Commit control
```
"After verifying the build, go ahead and commit/push"
→ verify with `./gradlew assembleDebug`, then commit/push
```

---

## Notes

- Verify the build via `./gradlew assembleDebug` (CLI) or Android Studio
- Without CLAUDE.md, you would have to explain the project rules every time
- Memory is a point-in-time snapshot — it may diverge from code changes
- Do not store sensitive information (API keys, etc.) in memory
