# Compose Hot Reload (HotSwan) Guide

> ## ⚠️ Current Status — Temporarily Disabled (branch `chore/kotlin-2.4-upgrade`)
>
> The HotSwan Gradle plugin application in `app/build.gradle` is **commented out** because
> **hotswan-compiler 1.2.1 is incompatible with Kotlin 2.4.0**.
>
> During the Kotlin 2.4.0 upgrade verification (2026-06-16), the plugin's compiler registrar threw:
>
> ```
> java.lang.ClassCastException: IrGenerationExtension$Companion cannot be cast to ProjectExtensionDescriptor
>   at com.skydoves.compose.hotswan.compiler.pre.PreComposePluginRegistrar.registerExtensions
> ```
>
> This caused an Internal compiler error on `:coordinator:compileDebugKotlin`. Kotlin 2.4.0 changed the
> compiler extension registration API, which HotSwan 1.2.1 does not support.
>
> **Verification result:** commenting out this single line lets the entire project (KSP 2.3.9 / Room 2.8.4 /
> Compose compiler 2.4.0 / all app·data·domain·core·coordinator code) build successfully on Kotlin 2.4.0 with
> **zero source changes**. HotSwan is the sole blocker.
>
> **Impact:** Hot Reload is a development-only convenience tool, so disabling it does **not** affect any example's
> behavior or the final build — only the in-IDE hot-reload experience is unavailable.
>
> **Action:** re-enable the plugin (uncomment in `app/build.gradle` + bump the version in `libs.versions.toml`)
> once a hotswan-compiler release that supports Kotlin 2.4.0 is available.
>
> On the `main` branch (Kotlin 2.3.20) the plugin remains **active**.

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
- ⚠️ **HotSwan 1.2.1 does NOT support Kotlin 2.4.0** — it fails at compiler-extension registration (see "Current Status" above). The `chore/kotlin-2.4-upgrade` branch keeps the plugin disabled until a 2.4.0-compatible release ships.
- The Gradle plugin is **already applied to the project** (on `main`). You only need to install the IDE plugin separately to use it.

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
