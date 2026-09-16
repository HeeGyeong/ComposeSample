# Compose Hot Reload (HotSwan) Guide

> ## Current Status — Enabled on `main` with HotSwan 2.0.0 (re-enabled 2026-09-15)
>
> **History.** From 2026-06-16 to 2026-09-15 the plugin line in `app/build.gradle` was commented out, because
> hotswan-compiler 1.2.1 broke the Kotlin 2.4.0 compiler:
>
> ```
> java.lang.ClassCastException: IrGenerationExtension$Companion cannot be cast to ProjectExtensionDescriptor
>   at com.skydoves.compose.hotswan.compiler.pre.PreComposePluginRegistrar.registerExtensions
> ```
>
> (an Internal compiler error on `:coordinator:compileDebugKotlin`; commenting out that single line was enough for the
> rest of the project to build on 2.4.0).
>
> **Compatible releases that followed** (JetBrains Marketplace change notes): **1.3.5** targets the Kotlin 2.4.0 compiler
> API and requires Kotlin 2.4.0+, **1.3.7** fixes a launch crash on Kotlin 2.4.0, and **2.0.0** (2026-09-13) replaces the
> runtime with an interpreter engine and lists Kotlin 2.3.x–2.4.x.
>
> **Why 2.0.0 rather than 1.3.7:** the Gradle plugin version must match the IDE plugin version, and the Marketplace installs
> 2.0.0 by default — pinning 1.3.7 would force every developer to side-load an old IDE plugin.
>
> **Verified on this project (Kotlin 2.4.20 / AGP 8.13.2 / Gradle 8.13), 2026-09-15:**
> - `./gradlew assembleDebug` passes with only the catalog version bump and the `app/build.gradle` line uncommented — zero source changes
> - HotSwan tasks exist only in the debug task graph (`transformDebugHotswanClasses`, `extractDebugInterpClasses`,
>   `packageDebugInterpAssets` on both `:app` and `:coordinator`); `./gradlew :app:assembleRelease --dry-run` contains none
> - The debug APK gains `libhotswan_interp.so` (arm64-v8a / armeabi-v7a / x86_64) and about 1,700 `assets/hotswan-v2*` entries
> - Installed on a real device (SM-A725F / API 33), the app launches without a crash and logcat shows
>   `HotSwanV2: v2 server listening on 127.0.0.1:8601`
>
> **Not verified:** an actual edit-and-reload round trip from the IDE — that needs the IDE plugin, which a CLI session cannot drive.
>
> **⚠️ Outside the official support matrix:** the 2.0.0 release notes list **AGP 9.x** (plus IntelliJ IDEA / Android Studio
> 2025.1+ and device API 28+). This project is on AGP 8.13.2; the build and the runtime start-up work, but if hot reload
> misbehaves, suspect AGP first — either fall back to 1.3.7 (Gradle **and** IDE plugin together) or upgrade AGP.

## Overview

Compose Hot Reload (HotSwan) is a development tool that, when you save a `.kt` file, applies the changes to a real device/emulator **within 1 second without restarting the app**.

- Navigation stack, scroll position, form input values, and ViewModel data are all preserved
- Constant values (padding, color, string) are applied within ~50ms by skipping compilation (Literal Patching)

## Behavior Pipeline (1.x)

```
1. Detect file change → identify module
2. Incrementally compile only the changed code
3. Extract only changed classes via DEX comparison
4. Swap classes in memory with a native agent
5. Recompose only the affected Compose scopes
```

> **2.0 changed step 4.** Per the 2.0.0 release notes the runtime no longer relies on swapping classes through the Android
> runtime; it runs HotSwan's own interpreter, which is also what lets Composables be added, removed or reordered in place.
> What this shows up as in this project's build: `transformDebugHotswanClasses` strips `*_INTERP` sibling classes and
> substitutes coroutine-impl shims, `packageDebugInterpAssets` packages interpreter baseline classes under
> `assets/hotswan-v2/classes/`, and the native interpreter ships as `libhotswan_interp.so`.

## Supported Scope

| Supported item | Notes |
|----------------|-------|
| Composable function bodies | text, color, layout, logic |
| Non-Composable functions | ViewModel, utilities |
| Adding and rearranging new Composables | |
| XML resource values | |
| data class properties | |
| extension / suspend functions | |

### What 2.0 leaves native (from this project's build log)

`assembleDebug` prints `w: [HotSwan v2] SKIPPED ...` for **39 declarations — 28 methods and 11 whole classes** — that
cannot be copied to the interpreter. They keep working, but edits to them need a full build. These `w:` lines are plugin
diagnostics, **not compiler warnings on the source** (the project's warning scan counts only `w: file://` lines).

| Reason | Count | Skipped unit | Examples in this project |
|--------|-------|--------------|--------------------------|
| `VIEW_RECEIVER_SUBTYPE` | 20 | method | `super.onCreate()` in activities (`MainActivity`, `BlogExampleActivity`, …), Room `*_Impl.createOpenDelegate`/`clearAllTables`, non-public `ViewModel` members |
| `FRAMEWORK_ENTRY_SUBCLASS` | 11 | whole class | `BaseApplication`, `LocationTrackingService`, the five Quick Settings `*TileService`s, the four Glance `*WidgetReceiver`s |
| `GENERIC_DECLARATION` | 8 | method | functions declaring type parameters, e.g. `onEachBatch<T>`, `measureInline<T>` |

### What the interpreter cannot execute — JDK 21 `typeSwitch` (fixed by pinning jvmTarget to 17)

Unlike the `SKIPPED` list above, this one is silent. Kotlin lowers a type-checking `when` — both
`when (x) { is A -> … }` and the subject-less `when { x is A -> … }` — to a `java.lang.runtime.SwitchBootstraps.typeSwitch`
invokedynamic **when the JVM target is 21**. The 2.0.0 interpreter does not implement that bootstrap method and throws
`InterpreterInternalError: INVOKEDYNAMIC not yet implemented (unknown BSM … typeSwitch)` at runtime.

There is no crash and no build warning: the composition fails, is swallowed as "Error was captured in composition while
live edit was enabled" in logcat, and **the screen renders as a 0x0 semantics tree** — an empty screen.

Measured on 2026-09-16 (SM-A725F / Android 13): 26 of the 1687 instrumented classes carried the bytecode, and the three
screens whose copy sits on a composition path — `PictureInPictureExampleUI`, `ScreenshotDetectionExampleUI`,
`FeatureFlagExampleUI` (all reached through `findActivity()` / status rows) — rendered empty. Screens whose type switch
only runs on interaction (`SealedDomainErrorExampleUI`, `MVIExampleViewModel.onEvent`) rendered normally.

**Fix applied:** `config.gradle` pins the Kotlin/Java bytecode target to 17 (`kotlinOptions.jvmTarget = '17'` +
`compileOptions` 17) while keeping the Java 21 toolchain. `SwitchBootstraps` is a JDK 21 API, so targeting 17 stops the
lowering at the source — all 26 classes came back clean and the three screens render again. Raising the target back to 21
reintroduces the problem for every type-checking `when` in the project, so it should wait for interpreter support.

## Version Requirements

- **HotSwan 2.0.0 (current):** Kotlin 2.3.x–2.4.x, AGP 9.x (official), IntelliJ IDEA / Android Studio 2025.1+, device API 28+ — runs here on Kotlin 2.4.20 / AGP 8.13.2 (see "Current Status")
- **HotSwan 1.3.5–1.3.7:** Kotlin 2.4.0 or later (projects on Kotlin 2.2.x–2.3.x should stay on 1.3.4)
- **HotSwan 1.2.1:** Kotlin 2.3.x — fails at compiler-extension registration on Kotlin 2.4.0
- The Gradle plugin adds its runtime dependency and the required compiler flags itself — no extra dependency or configuration block is needed

## Installation (Gradle setup already applied to the project)

### 1. Install the IDE plugin
`Settings → Plugins → Marketplace` → search **"Compose HotSwan"** → Install → restart the IDE

### 2. Gradle setup

**libs.versions.toml:**
```toml
[plugins]
hotswan-compiler = { id = "com.github.skydoves.compose.hotswan.compiler", version = "2.0.0" }
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

- **The IDE and Gradle plugin versions must match** (current Gradle plugin: 2.0.0)
- When you update the IDE plugin, also change the version in `libs.versions.toml`
- Structural changes (class hierarchy changes, adding interfaces, etc.) may require an app restart
- Hot Reload is a development convenience tool and does not affect the final build — its tasks are absent from the release task graph

## References

- Official site: https://hotswan.dev
- Install guide: https://hotswan.dev/install
- Blog: https://hotswan.dev/blog/compose-hot-reload
- JetBrains Marketplace: https://plugins.jetbrains.com/plugin/30551-compose-hotswan/
- Issue tracker: https://github.com/skydoves/compose-hotswan-issuetracker
