# Compose Hot Reload (HotSwan) Guide

## Overview

Compose Hot Reload (HotSwan) is a development tool that, when you save a `.kt` file, applies the changes to a real device/emulator **within 1 second without restarting the app**.

- Navigation stack, scroll position, form input values, and ViewModel data are all preserved
- Constant values (padding, color, string) are applied within ~50ms by skipping compilation (Literal Patching)

## Behavior Pipeline (5 steps)

```
1. Detect file change → identify module
2. Incrementally compile only the changed code
3. Extract only changed classes via DEX comparison
4. Swap classes in memory with a native agent
5. Recompose only the affected Compose scopes
```

## Supported Scope

| Supported item | Notes |
|----------------|-------|
| Composable function bodies | text, color, layout, logic |
| Non-Composable functions | ViewModel, utilities |
| Adding and rearranging new Composables | |
| XML resource values | |
| data class properties | |
| extension / suspend functions | |

## Version Requirements

- HotSwan 1.2.1 requires Kotlin 2.3.0 or higher → **the project applied Kotlin 2.3.20 (2026-04-13)**
- The Gradle plugin is **already applied to the project**. You only need to install the IDE plugin separately to use it.

## Installation (Gradle setup already applied to the project)

### 1. Install the IDE plugin
`Settings → Plugins → Marketplace` → search **"Compose HotSwan"** → Install → restart the IDE

### 2. Gradle setup

**libs.versions.toml:**
```toml
[plugins]
hotswan-compiler = { id = "com.github.skydoves.compose.hotswan.compiler", version = "1.2.1" }
```

**root build.gradle:**
```groovy
alias(libs.plugins.hotswan.compiler) apply false
```

**app/build.gradle:**
```groovy
alias(libs.plugins.hotswan.compiler)
```

### 3. IDE configuration
`Settings → Tools → Compose HotSwan`
- Module Path: `:app` (default)
- App Package Name: `com.example.composesample` (matches applicationId)

## Notes

- **Kotlin 2.3.0 or higher required** — the project applied Kotlin 2.3.20
- **The IDE and Gradle plugin versions must match** (current Gradle plugin: 1.2.1)
- When you update the IDE plugin, also change the version in `libs.versions.toml`
- Structural changes (class hierarchy changes, adding interfaces, etc.) may require an app restart
- Hot Reload is a development convenience tool and does not affect the final build

## References

- Official site: https://hotswan.dev
- Install guide: https://hotswan.dev/install
- Blog: https://hotswan.dev/blog/compose-hot-reload
- JetBrains Marketplace: https://plugins.jetbrains.com/plugin/30551-compose-hotswan/
- Issue tracker: https://github.com/skydoves/compose-hotswan-issuetracker
