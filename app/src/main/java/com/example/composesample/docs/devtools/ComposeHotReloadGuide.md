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
misses it. Debug builds only — the release variant has no HotSwan transform. A JVM unit test is affected too when the
lambda lives in app code: `testDebugUnitTest` runs the debug variant's transformed classes (measured 2026-10-12 with
`OriginFailoverTest`, see below); only lambdas written in the test sources themselves are left alone.

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

### A framework-class callback that writes a composable's `by` local crashes with `NoSuchMethodError`

Inside a composable, an anonymous object that **extends a framework class** — `ContentObserver`, `BroadcastReceiver`,
a `Handler` subclass — and assigns a `var x by remember { mutableStateOf(...) }` local of that composable crashes in
debug **at the moment the system calls it**:
`NoSuchMethodError: No static method access$<function>$lambda$N(MutableState;…)V in class …Kt`. The write goes through a
synthetic accessor on the file facade, and under the dispatch rewrite that accessor is not there when the framework
enters the native method body. Release and a debug build with `-Photswan.dispatchRewriteEnabled=false` are fine.

Found on 2026-10-07 while adding the click-sound section to `HapticFeedbackExampleUI` (#108): a `ContentObserver` for
`Settings.System.SOUND_EFFECTS_ENABLED` crashed the debug app as soon as the setting changed (reproduced twice). A probe
file then separated the cases (SM-A725F / Android 13, HotSwan 2.1.0; each write wrapped in `try/catch` and logged):

| Anonymous object writing a composable's `by` local | Who calls it | Result |
|---|---|---|
| `Runnable`, `ContentObserver`, `BroadcastReceiver`, an app abstract class | app code, directly (`o.onChange(false)`), at once or 2 s later | ✅ |
| `ContentObserver` registered on a real setting | system (setting changed) | ❌ `NoSuchMethodError` |
| `BroadcastReceiver` (own-app broadcast) | system | ❌ `NoSuchMethodError` |
| `Handler` subclass (`handleMessage`) | system (`Looper`) | ❌ `NoSuchMethodError` |
| `Choreographer.FrameCallback` (interface) | system | ✅ |
| `Handler.Callback` (interface) | system | ✅ |

Whether the composable returns a value, is `private`, or uses `DisposableEffect` vs `remember` made no difference; reads
and writes from interface callbacks invoked by libraries (`AuthenticationResultCallback` in the biometric example,
`Player.Listener` in the Media3 example) also work. **The fix used here:** move the observer into a plain state-holder
class whose own `mutableStateOf` property it writes (`ClickSoundEnvironment`), and let the composable only `remember` it
and start/stop it in a `DisposableEffect`. A scan of the project found no other framework-class anonymous object writing
a composable local (the five anonymous objects that touch one all implement interfaces or sit inside code-sample strings).

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

### A per-frame drawing loop is 20x slower under the dispatch rewrite — two more classes are excluded

`DisplayRefreshRateExampleUI` measures frame intervals with a `withFrameNanos` loop and redraws a 90-bar interval chart
on every frame. Under HotSwan the chart's draw phase alone took longer than a whole frame, so the thing being measured
(the frame interval) collapsed. Measured on 2026-10-06 (SM-A725F / Android 13, 60 Hz, HotSwan 2.1.0, Compose 1.12.1)
with `dumpsys gfxinfo <pkg> framestats` over 2 seconds:

| Build | Frames in 2 s | Draw phase median | Late frames (> 25 ms) |
|---|---|---|---|
| debug, HotSwan as configured | 44 | 44.5 ms | 43 |
| debug, `-Photswan.dispatchRewriteEnabled=false` | 120 | 2.3 ms | 1 |
| debug, file facade excluded only | 112 | 14.4 ms | 19 |
| debug, file facade + `FramePacingMeter` excluded (current setup) | 120 | 1.8 ms | 1 |
| release (debug-key signed) | 120 | 1.0 ms | 1 (0 after `cmd package compile -m speed`) |

Excluding the file facade was not enough: the measuring class that the frame loop and the chart call on every frame
(`record()` once, `recentAt()` 90 times) is a separate class, so it is excluded by its own name:

```groovy
exclude("com.example.composesample.presentation.example.component.system.platform.display.DisplayRefreshRateExampleUIKt")
exclude("com.example.composesample.presentation.example.component.system.platform.display.FramePacingMeter")
```

The FrameMetrics card added to the same screen later (#134) follows the same rule: its per-frame listener
(`FrameMetricsRecorder`, called on a `HandlerThread` for every rendered frame), the aggregator it feeds (`FrameStageMeter`)
and the file facade (`FrameStageMetricsKt`) are excluded so the card measures the same code release runs. This was done
as a precaution under the rule above, not because a HotSwan-specific difference was measured.

The one late frame left in every 2-second window is not HotSwan: it is the frame that inserts a row into the result
table, a path that runs once every 2 seconds and stays cold under JIT. It disappears in a release build after
`adb shell cmd package compile -m speed -f com.example.composesample`.

The Pixelate example (#137) excludes its drawing code for the same reason, after measuring the difference. Each pixelated
text draws its content into two offscreen bitmaps on every redraw; with the rewrite the measured draw took about 5.5 ms
(plain `drawContent` 0.33 ms), with the three classes below excluded 0.8-1.0 ms (0.17 ms), and the release build after
`compile -m speed` 0.40-0.55 ms (0.12 ms) on the same Galaxy A72. `PixelBuffers` (the bitmaps per size) and `PixelateNode`
(the `Modifier.Node` variant) are separate classes from the file facade that holds the shared `drawPixelated` function:

```groovy
exclude("com.example.composesample.presentation.example.component.ui.graphics.PixelateModifierKt")
exclude("com.example.composesample.presentation.example.component.ui.graphics.PixelateNode")
exclude("com.example.composesample.presentation.example.component.ui.graphics.PixelBuffers")
```

The Range download example (#139) excludes its download engine for the same reason. The "resume check" card runs six
scenarios, each downloading a 2 MiB file in 16 KiB chunks and then comparing the result byte by byte with two versions;
on the same Galaxy A72 the six took 34.4 s under the rewrite, 7.0 s with the three classes below excluded, and 3.4 s in
the release build after `compile -m speed`:

```groovy
exclude("com.example.composesample.presentation.example.component.data.download.RangeDownloadKt")
exclude("com.example.composesample.presentation.example.component.data.download.FakeFileServer")
exclude("com.example.composesample.presentation.example.component.data.download.RangeDownloader")
```

### `@Synchronized` loses its mutual exclusion under the dispatch rewrite — use a `synchronized` block

Found while writing the Range download example (#139). Its fake server's `handle()` was marked `@Synchronized` so that
one request sees one file version; four connections call it at the same time. In `testDebugUnitTest` (debug classes,
HotSwan 2.1.0, Kotlin 2.4.21) the requests overlapped: the sequence numbers assigned inside the method were printed out of
order, and three requests read the old file while a fourth was replacing it. The same probe passed with
`-Photswan.dispatchRewriteEnabled=false`, and also under the rewrite once the body was wrapped in a `synchronized(lock) { }`
block instead of the annotation:

| Build | `@Synchronized fun handle()` | `fun handle() = synchronized(lock) { … }` |
|---|---|---|
| debug classes, HotSwan as configured | ❌ overlapping calls | ✅ one at a time |
| debug classes, `-Photswan.dispatchRewriteEnabled=false` | ✅ one at a time | ✅ |

The annotation only sets the method's `ACC_SYNCHRONIZED` flag, while the block compiles to explicit monitor
instructions in the body. In app code that runs under the rewrite, prefer the block (or a `Mutex` in coroutines).

### A Ktor client plugin that throws a checked `IOException` — two more classes are excluded

The origin-failover card of `KtorAdvancedConfigExampleUI` (#130) is a Ktor client plugin: its `on(Send)` lambda throws
`OriginUnavailableException` (an `IOException`) while the cooldown is running, and the `MockEngine` handlers that stand
in for the two servers throw `IOException("connection reset")` to imitate a broken write. Both lambdas are values, so
under the dispatch rewrite the exception reached the caller as `UndeclaredThrowableException` and the scenario's
`catch (e: IOException)` missed it. Measured on 2026-10-12 (HotSwan 2.1.0, Kotlin 2.4.21) in `testDebugUnitTest`:

| Build | Cooldown scenario | Unit test |
|---|---|---|
| debug classes, HotSwan as configured | `UndeclaredThrowableException` (cause `OriginUnavailableException`) | 1 of 2 fails |
| debug classes, file facade and `TwoOrigins` excluded (current setup) | `OriginUnavailableException` caught as `IOException` | 2 of 2 pass |

On the device the debug and release builds then show the same 10 of 10 results. The excluded names:

```groovy
exclude("com.example.composesample.presentation.example.component.data.api.OriginFailoverKt")
exclude("com.example.composesample.presentation.example.component.data.api.TwoOrigins")
```

Ordinary Ktor calls are not affected — the exception has to escape a lambda that exists as a value. A real failover
plugin in a debug build would hit the same wrapping, which is one more reason to classify network errors by their cause
chain (see the section on checked exceptions above).

### Caller-sensitive reflection fails under the interpreter — one more class is excluded

Found while writing the sensitive-types example (#144). Its copy() card reads `Class.getDeclaredMethods()` and calls
`Method.invoke` on a data class's generated `copy`. In `testDebugUnitTest` (debug classes, HotSwan 2.1.0, Kotlin 2.4.21)
the interpreter's reflection bridge threw before the method was even found:

```
java.lang.IllegalAccessException: Attempt to lookup caller-sensitive method using restricted lookup object
    at java.lang.invoke.MethodHandles$Lookup.unreflect(...)
    at com.skydoves.compose.hotswan.interpreter.runtime.ReflectionArtBridge.resolveMethodHandleReflectively(...)
```

| Build | `getDeclaredMethods` / `Method.invoke` |
|---|---|
| debug classes, HotSwan as configured | ❌ `IllegalAccessException` (restricted lookup object) |
| debug classes, `-Photswan.dispatchRewriteEnabled=false` | ✅ 6 of 6 tests pass |
| debug classes, `CopyVisibilityProbe` excluded (current setup) | ✅ 6 of 6; device debug and release show the same rows |

Only the reflection calls were moved into one object so that the rest of the file still hot-reloads:

```groovy
exclude("com.example.composesample.presentation.example.component.architecture.development.language.CopyVisibilityProbe")
```

Plain `Class` lookups that are not caller-sensitive (`javaClass.simpleName`, `::class.java`) were not affected.

### A `private var` in an `object` or at the top level reads back its initial value under the dispatch rewrite

Found while writing the clipboard / text processing example (#146). `ProcessTextLog` (an `object`) numbered its records
with `private var nextId = 1` and `val id = nextId++`; on the device every record came out as `#1`, and updating "record
1" changed all of them. `testDebugUnitTest` reproduced it, and the same test passed with
`-Photswan.dispatchRewriteEnabled=false`. Narrowing it down took three probe rounds (HotSwan 2.1.0, Kotlin 2.4.21, debug
unit tests); the decisive one writes 9 and reads the property back in the same function:

| Declaration | `n = 9; return n` under the rewrite | `dispatchRewriteEnabled=false` |
|---|---|---|
| `object { private var n = 5 }` | ❌ `5` | ✅ `9` |
| `object { private var s: String? = "init" }` (`s = "new"`) | ❌ `"init"` | ✅ `"new"` |
| `object { private var n: Int; init { n = 5 } }` | ❌ `5` | ✅ `9` |
| top-level `private var n = 5` | ❌ `5` | ✅ `9` |
| `object { private var n = 0 }` · top-level `private var n = 0` | ✅ `9` | ✅ `9` |
| `object { var n = 5 }` (public) | ✅ `9` | ✅ `9` |

So the write is not what goes missing — reading the property returns the value it was initialized with. Every earlier
failure fits that: `nextId++` from 1 gives `[1, 1]`, `n += 1; return n - 1` from 1 gives `[0, 0]`, and reassigning a
`private var list = listOf()` always reads an empty list. Properties that start at the type's default (`0`, `false`,
`null`) behaved normally — `++seq` from 0, `@Volatile private var holder: Holder? = null` with lazy creation, and plain
setters all gave the expected values — as did `internal`/public properties and a `companion object`'s private
property. There is no error and no `SKIPPED` line in the build log.

In app code that runs under the rewrite, keep mutable singleton state in a `val` holder (`AtomicInteger`,
`MutableStateFlow`, a mutable collection), start a private `var` at its default value, or make it non-private.
`ProcessTextLog` now uses `AtomicInteger`. Prework axis ㉘ looks for private `var`s in an `object` or a file that have a
non-default initializer (or an `init` assignment) and are assigned later; on 2026-10-22 it found none besides the fixed one
(the two hits of a broader first version, `seq = 0` in `PerItemViewModelExampleUI` and `holder = null` in `OutboxWork`,
start at defaults and showed correct values on the device).

### A function that returns a library value class with a private constructor breaks the debug compile

Found while writing the adaptive multi-pane example (#118). A small mapper
`private fun BackMode.toBehavior(): BackNavigationBehavior = when (this) { … }` returned
`androidx.compose.material3.adaptive.navigation.BackNavigationBehavior`, a `value class` whose constructor is private.
`:app:compileDebugKotlin` failed under HotSwan 2.1.0 with

```
e: org.jetbrains.kotlin.fir.pipeline.IrGenerationExtensionException: B3: value class BackNavigationBehavior has no
   primary constructor — cannot build dispatch return bridge for toBehavior.
```

The dispatch rewrite needs to rebuild the value class to return it from the interpreter path, and it cannot reach that
constructor. A throwaway probe confirmed the scope: a plain, non-composable `internal fun probeBehavior(…): BackNavigationBehavior`
fails `:app:compileDebugKotlin` with the same message, while `:app:compileReleaseKotlin` compiles it. Looking the value up instead of returning
it from our own function avoids the bridge: the example keeps a `Map<BackMode, BackNavigationBehavior>` and calls
`getValue(mode)` where it needs one, and its composables take the app's own enum instead of the library value class.

### Stack traces from a debug build show no app frames — read crashes and ANRs from release

Found while writing the process exit post-mortem example (#159). The same deliberate failures — a crash thrown from a
click handler and an ANR from a click handler that sleeps on the main thread — were triggered in debug (HotSwan 2.1.0 as
configured) and in release (debug-key signed) on a Galaxy A72 (API 33) and a Pixel Tablet API 35 emulator:

| Trace | debug, HotSwan as configured | release |
|---|---|---|
| Crash stack in logcat (`AndroidRuntime: FATAL EXCEPTION`, Galaxy A72) | 71 frames, **0 app frames**, 16 `com.skydoves.compose.hotswan.interpreter.runtime.*` frames — the exception is created through `Constructor.newInstance` by the interpreter | 55 frames, 4 app frames on top (`triggerCrash`, the click lambda) |
| ANR trace, `"main"` thread (`ApplicationExitInfo.getTraceInputStream`, both devices) | 94–95 frames, **0 app frames** — `Thread.sleep` is followed by `MethodHandle.invokeExact` / `Transformers$…` frames | 61–62 frames, 5 app frames — `blockMainThreadForAnr` at line 6–7, then `triggerAnr`, the click lambda, Compose `performClick` |

The exception message and type are unchanged, so "what" still reads correctly; "where" does not. Anything that groups
or symbolicates by the top app frame — a crash reporter, an ANR upload, a log search for a class name — sees nothing of
the app in a debug build. When the location matters, reproduce in release (or exclude the class from the rewrite and
compare). The example's reading card and guide state the same numbers.

### Side effect: the interpreter baseline needs a large heap — the debug variant sets `largeHeap`

On start, HotSwan fills an interpreter baseline with the app's class bytecode on a background thread
(`hotswan-baseline-fill`, "registered 2391 interpreter baseline class(es)" on 2026-10-22). As the examples grew, a debug
build used about 160 MB of Java heap right after start (Galaxy A72, `dumpsys meminfo` Dalvik heap allocated), and on
devices whose `dalvik.vm.heapgrowthlimit` is 192m — the Pixel 7 Pro API 34 and Pixel Tablet API 35 AVDs — that thread
died with `OutOfMemoryError` every time, taking the app down before the first screen (DEBUG-HEAP-OOM-01, found while
measuring #146). `app/src/debug/AndroidManifest.xml` now sets `android:largeHeap="true"`, which raises the limit to
`heapsize` (512–576 MB) for the debug variant only; the merged release manifest has no `largeHeap`
(`aapt2 dump xmltree`). With it the Pixel Tablet API 35 AVD starts the debug app, finishes the baseline fill and opens
examples. Because debug now has far more headroom than release, measure memory behaviour in release.

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
