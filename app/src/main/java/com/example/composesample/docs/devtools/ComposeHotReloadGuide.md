# Compose Hot Reload (HotSwan) Guide

> ## Current Status — Enabled on `main` with HotSwan 2.1.0 (re-enabled 2026-09-15 on 2.0.0, patched 2026-09-22, 2.1.0 on 2026-10-01)
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
> **2.0.0 → 2.0.2 (2026-09-22).** 2.0.2 fixes "an app could crash at launch on Kotlin 2.4" — a class holding a property
> declared with `by` over a state value failed during the first composition, surfacing elsewhere as a Compose layout
> error. This project is on Kotlin 2.4.20, so the patch applies. 2.0.1 only fixed Compose Desktop (Android unaffected).
> Checks run before committing the bump:
> - Only the debug runtime changed (`interpreter-runtime` / `-core` / `protocol` 2.0.0 → 2.0.2); the release runtime
>   classpath still has no HotSwan reference
> - `SKIPPED` stayed at 40 declarations with an identical list (forced recompile of `:app` and `:coordinator`)
> - Full render audit on a real device, run on both versions: 179 registry screens rendered with identical root sizes
>   (0 exceptions, no 0x0), and the 5 always-animating screens rendered identically under a real clock (see the test
>   guide for why those five need a different harness)
> - The checked-exception wrapping described below is **unchanged in 2.0.2** (re-measured)
>
> No HotSwan IDE plugin was installed on this machine at the time. The Marketplace installs the latest version, so the
> Gradle plugin follows the latest patch to stay matched.
>
> **Not verified:** an actual edit-and-reload round trip from the IDE — that needs the IDE plugin, which a CLI session cannot drive.
>
> **AGP 9.4.1 (2026-09-29) — now inside the official support matrix.** The 2.0.0 release notes list **AGP 9.x** (plus
> IntelliJ IDEA / Android Studio 2025.1+ and device API 28+); until this date the project ran it on AGP 8.13.2. Moving to
> Gradle 9.8.0 / AGP 9.4.1 with built-in Kotlin (compileSdk 37) changed nothing HotSwan-visible:
> - Forced recompile of `:app` and `:coordinator`: **the same 42 `SKIPPED` declarations** before and after (list diffed)
> - All Kotlin classes in the Android modules stay at class-file major 61 with zero `SwitchBootstraps` (see below)
> - On the device the app starts and logs `HotSwanV2: v2 server listening on 127.0.0.1:8601`, then registers 1,842
>   interpreter baseline classes
> - Full render audit: all 187 registry screens render with a non-zero root size
> The debug APK now also carries D8 API stubs for framework classes newer than minSdk (`SYNTHETIC`, `<clinit>` throws
> `NoClassDefFoundError`); they are unrelated to HotSwan and the release APK is unchanged.
>
> **2.0.2 → 2.1.0 (2026-10-01).** The 2.1.0 release notes (2026-09-28) are mostly IDE and iOS work; the Android-relevant
> fixes are saves that silently vanished, screens showing "applied" over stale content and state leaking into neighbouring
> composables. No requirement changed. Checks run before committing the bump (SM-A725F / Android 13):
> - Only the debug runtime changed (`interpreter-runtime` / `-core` / `protocol` 2.0.2 → 2.1.0); the release runtime
>   classpath is unchanged and still has no HotSwan reference
> - Forced recompile of `:app` and `:coordinator`: **the same 49 `SKIPPED` declarations** before and after (list diffed)
> - The app starts, logs `HotSwanV2: v2 server listening on 127.0.0.1:8601` and registers 1,936 interpreter baseline classes;
>   the debug variant's merged minSdk is still 26
> - Full render audit: all 191 registry screens render with a non-zero root size, no composition error swallowed, no crash
>   (same result as on 2.0.2 earlier that day)
> - The Power-Assert problem described below is **unchanged in 2.1.0**: with the exclusions removed the example fails with the
>   same `NotImplementedError`, so the two `exclude(...)` lines stay
>
> No HotSwan IDE plugin was installed in Android Studio on this machine at the time (2025.3.4). Install the 2.1.0 IDE plugin
> to match the Gradle plugin before using hot reload.

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

`assembleDebug` prints `w: [HotSwan v2] SKIPPED ...` for **40 declarations — 28 methods and 12 whole classes** — that
cannot be copied to the interpreter. They keep working, but edits to them need a full build. These `w:` lines are plugin
diagnostics, **not compiler warnings on the source** (the project's warning scan counts only `w: file://` lines).

| Reason | Count | Skipped unit | Examples in this project |
|--------|-------|--------------|--------------------------|
| `VIEW_RECEIVER_SUBTYPE` | 20 | method | `super.onCreate()` in activities (`MainActivity`, `BlogExampleActivity`, …), Room `*_Impl.createOpenDelegate`/`clearAllTables`, non-public `ViewModel` members |
| `FRAMEWORK_ENTRY_SUBCLASS` | 11 | whole class | `BaseApplication`, `LocationTrackingService`, the five Quick Settings `*TileService`s, the four Glance `*WidgetReceiver`s |
| `GENERIC_DECLARATION` | 8 | method | functions declaring type parameters, e.g. `onEachBatch<T>`, `measureInline<T>` |
| `HELPER_SUPER_CTOR_NEW_ARG` | 1 | whole class | `RubberBandOverscrollEffect` (the custom overscroll example). A field initialiser that constructs an object — here an `Animatable` plus an anonymous `Modifier.Node()` — lands in the same static `hotswanInit` helper as the super-constructor delegation, which the JVM verifier rejects (`Expected value generated with NEW`). Any class whose constructor builds objects into its fields can land here. |

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

**Fix applied:** `config.gradle` pins the Kotlin/Java bytecode target to 17 (`kotlin { compilerOptions { jvmTarget } }` since AGP 9 built-in Kotlin, formerly `kotlinOptions.jvmTarget = '17'` +
`compileOptions` 17) while keeping the Java 21 toolchain. `SwitchBootstraps` is a JDK 21 API, so targeting 17 stops the
lowering at the source — all 26 classes came back clean and the three screens render again. Raising the target back to 21
reintroduces the problem for every type-checking `when` in the project, so it should wait for interpreter support.

### Checked exceptions leaving a plain lambda are wrapped in `UndeclaredThrowableException`

The interpreter runs an ordinary (non-inline, non-suspend) Kotlin lambda as a `java.lang.reflect.Proxy`
(`InterpreterLambda`). `FunctionN.invoke` declares no checked exceptions, so when a **checked** exception escapes the
lambda, `Proxy` wraps it in `java.lang.reflect.UndeclaredThrowableException`. Any caller that catches the checked type
misses it. Debug builds only — the release variant has no HotSwan transform, and a plain JVM unit test is unaffected.

Where it bites: `runInterruptible { Thread.sleep(…) }`. When the coroutine is cancelled, the thread is interrupted and
`Thread.sleep` throws `InterruptedException`. `runInterruptible` is supposed to catch that and turn it into a
`CancellationException`, but it receives the wrapper instead. The wrapper then escapes as an ordinary failure, and the
**debug app crashes** when you cancel.

Measured on 2026-09-22 (SM-A725F / Android 13, HotSwan 2.0.0; the plain-lambda row re-measured on 2.0.2 with the same
result). The lambdas lived in app code and were called from an
instrumented test:

| Lambda that throws `IOException` | Caught by `catch (e: IOException)`? |
|---|---|
| plain `val block: () -> Unit = { throw … }; block()` | ❌ `UndeclaredThrowableException` |
| inline (`listOf(1).forEach { throw … }`) | ✅ |
| suspend (`withContext(Dispatchers.IO) { throw … }`) | ✅ |
| suspend (`coroutineScope { throw … }`) | ✅ |
| **suspend function-typed parameter** (`fun run(block: suspend () -> T)`, called as `block()`) | ❌ `UndeclaredThrowableException` (2026-09-23) |

**The dividing line is not plain vs. suspend — it is whether the lambda exists as a value.** The two ✅ suspend rows
above are `withContext`/`coroutineScope` call sites, and both functions are `inline`, so the lambda body is compiled
into the caller and no `Proxy` is involved. A `suspend () -> T` **parameter** is still a function-typed value, so it is
still run as a `Proxy` and still wraps. Measured on 2026-09-23 (SM-A725F / Android 13, HotSwan 2.0.2) while building the
Certificate Pinning example: OkHttp's `SSLPeerUnverifiedException` escaping a `block: () -> StepResult` parameter was
wrapped, and converting the parameter to `suspend () -> StepResult` did **not** help.

So Retrofit/Ktor calls wrapped in `withContext` are fine. The risk is limited to function-typed values that let a
checked exception escape. Two workarounds, both in use here:

- Catch the exception inside the lambda and rethrow it unchecked — `Proxy` passes unchecked exceptions through
  untouched. `StructuredConcurrencyGuardrailExampleUI` does this and rethrows `CancellationException(...).initCause(e)`,
  which is the same translation `runInterruptible` makes.
- Classify by walking the **cause chain** instead of the outermost type, so the wrapper is transparent.
  `AppSecurityViewModel.toLog()` does this; it also survives wrapping introduced by OkHttp interceptors or coroutines,
  which is why it is the better default for network error handling.

### Kotlin Power-Assert breaks under the interpreter — two classes are excluded

The Power-Assert compiler plugin (`org.jetbrains.kotlin.plugin.power-assert`, added for `PowerAssertExample`) rewrites
call sites and replaces the intrinsic `PowerAssert.explanation` inside `@PowerAssert` functions. Code that HotSwan runs
in debug does not get those rewrites right. Measured on 2026-10-01 (SM-A725F / Android 13, HotSwan 2.0.2, Kotlin 2.4.20; the
`NotImplementedError` re-measured on 2.1.0 the same day — unchanged):

| Symptom | Where | With the class excluded |
|---|---|---|
| `NotImplementedError: Intrinsic property! Make sure the Power-Assert compiler plugin is applied` | a `@PowerAssert` function body that reads `PowerAssert.explanation` — on the device **and** in JVM unit tests (`debugUnitTest`) | explanation is delivered |
| A side-effecting sub-expression is evaluated **three times** (`powerCheck(counter.incrementAndGet() == 5)` → 3) | a Power-Assert-transformed call site | evaluated once, as in release |

Building with `-Photswan.dispatchRewriteEnabled=false` also gives the correct count, which isolates the cause to the
dispatch rewrite. `app/build.gradle` therefore excludes the two classes involved, by exact class name:

```groovy
hotSwanCompiler {
    exclude("com.example.composesample.presentation.example.component.architecture.development.test.PowerAssertChecksKt")
    exclude("com.example.composesample.presentation.example.component.architecture.development.test.PowerAssertViewModel")
}
```

Edits to those two classes need a full build. Any new `@PowerAssert` function, or a class that calls one, should be
added to the list. The transformed `kotlin.assert` / `kotlin.test` calls in this project's test code produced the
expected diagrams without any exclusion (JVM unit test and on-device instrumented test).

### Pausable composition in lazy prefetch does not pause — one more file is excluded

`LazyListCacheWindowExampleUI` measures whether a heavy LazyColumn item (12 child composables × 1.5 ms of busy work)
that is prefetched while the list is scrolling gets split across frames. Foundation's prefetch pauses only at the
start of a newly inserted restartable composable call, when the time left before the next frame runs out. Measured on
2026-10-02 (SM-A725F / Android 13, HotSwan 2.1.0, Compose 1.12.1), the same 4-second scroll in the real app:

| Build | Items composed by prefetch while scrolling | Split into 2 frames |
|---|---|---|
| debug, HotSwan as configured | 1 (about 50 ms in one chunk) | 0 |
| debug, `-Photswan.dispatchRewriteEnabled=false` | 5 | 2 |
| debug, file excluded (current setup) | 5 | 3 |
| release (debug-key signed) | 8 | 6 |

Even the first prefetched item, which had no learned timing to stop it, ran to completion in one chunk under the
dispatch rewrite, so the pause points were not reached. The file facade is therefore excluded:

```groovy
exclude("com.example.composesample.presentation.example.component.ui.layout.lazycolumn.LazyListCacheWindowExampleUIKt")
```

The rest of that example (cache window sizes, visibility callbacks) gave the same numbers before and after the
exclusion. Note that an instrumented test did not reproduce the split at all, even with the file excluded and frames
advanced one by one at 12 ms intervals: every prefetched item came out in one chunk. The likely reason (not verified)
is that the View does not draw at the real frame rate under the test clock, so the prefetch scheduler sees an idle
frame and grants an unlimited budget. Measure this kind of behaviour in the real app.

### Side effect: the debug variant's minSdk becomes 26

`interpreter-runtime:2.0.0` declares `minSdkVersion="26"`, and the plugin adds it to debug only. The merged manifests
therefore differ by variant — verified in `app/build/outputs/logs/manifest-merger-*-report.txt` on 2026-09-16:

| Variant | Merged `minSdkVersion` |
|---------|------------------------|
| debug   | **26** (raised by the HotSwan runtime) |
| release | 24 (the value declared in `libs.versions.toml`) |

Two consequences worth knowing:

1. Debug builds cannot be installed on API 24-25 devices. Release builds still can.
2. `lint` analyses the debug variant, so it reports 23 `ObsoleteSdkInt` warnings ("SDK_INT is always >= 26") for guards
   that are **still required by the release variant**. Do not delete those guards on lint's advice.

## Version Requirements

- **HotSwan 2.0.x–2.1.x (current: 2.1.0):** Kotlin 2.3.x–2.4.x, AGP 9.x (official), IntelliJ IDEA / Android Studio 2025.1+, device API 28+ — runs here on Kotlin 2.4.20 / AGP 9.4.1 / Gradle 9.8.0 (see "Current Status")
- **HotSwan 1.3.5–1.3.7:** Kotlin 2.4.0 or later (projects on Kotlin 2.2.x–2.3.x should stay on 1.3.4)
- **HotSwan 1.2.1:** Kotlin 2.3.x — fails at compiler-extension registration on Kotlin 2.4.0
- The Gradle plugin adds its runtime dependency and the required compiler flags itself — no extra dependency is needed. The only configuration block in this project is the `hotSwanCompiler { exclude(...) }` list for the Power-Assert example (see above)

## Installation (Gradle setup already applied to the project)

### 1. Install the IDE plugin
`Settings → Plugins → Marketplace` → search **"Compose HotSwan"** → Install → restart the IDE

### 2. Gradle setup

**libs.versions.toml:**
```toml
[plugins]
hotswan-compiler = { id = "com.github.skydoves.compose.hotswan.compiler", version = "2.0.2" }
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

- **The IDE and Gradle plugin versions must match** (current Gradle plugin: 2.1.0)
- When you update the IDE plugin, also change the version in `libs.versions.toml`
- Structural changes (class hierarchy changes, adding interfaces, etc.) may require an app restart
- Hot Reload is a development convenience tool and does not affect the final build — its tasks are absent from the release task graph

## References

- Official site: https://hotswan.dev
- Install guide: https://hotswan.dev/install
- Blog: https://hotswan.dev/blog/compose-hot-reload
- JetBrains Marketplace: https://plugins.jetbrains.com/plugin/30551-compose-hotswan/
- Issue tracker: https://github.com/skydoves/compose-hotswan-issuetracker
