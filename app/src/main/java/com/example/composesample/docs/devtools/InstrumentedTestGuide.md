# Instrumented Test Guide (`connectedDebugAndroidTest`)

How to run this project's on-device tests, and the one environment condition that silently breaks all of the
Compose-rule ones.

---

## The device screen must be on and unlocked

**Before running `connectedDebugAndroidTest`, wake the device and dismiss the keyguard.**

```bash
adb shell input keyevent KEYCODE_WAKEUP   # turn the screen on
adb shell input keyevent KEYCODE_MENU     # dismiss a non-secure keyguard
./gradlew :app:connectedDebugAndroidTest
```

If the screen is off, every test that uses a Compose rule (`createComposeRule`, `createAndroidComposeRule`) fails with:

```
java.lang.IllegalStateException: No compose hierarchies found in the app. Possible reasons include:
(1) the Activity that calls setContent did not launch; (2) setContent was not called;
(3) setContent was called before the ComposeTestRule ran.
```

The message lists three causes and the real one is **none of them**. The activity does launch and `setContent` is
called; the activity simply never gains window focus, so its `ComposeView` is never measured and no compose root is
registered for the test framework to find.

### Verifying the state

`mWakefulness=Awake` is **not sufficient** — the device can be awake with the keyguard or the notification shade in
front, which reproduces the failure exactly. Check what actually holds focus:

```bash
adb shell dumpsys power  | grep mWakefulness     # Awake vs Dozing
adb shell dumpsys window | grep mCurrentFocus    # what is in front right now
```

A good state looks like `mWakefulness=Awake` plus an `mCurrentFocus` naming the launcher or the app under test.
A bad state is `Dozing`, or a focus of `NotificationShade` / a keyguard window.

### How this was confirmed (2026-09-18, TEST-COMPOSE-HARNESS-01)

Same commit, same APK, same tests — only the screen state differed:

| Device state | `InitTestExampleUITest` |
|---|---|
| screen off (`Dozing`, focus `NotificationShade`) | 3 failures, all `No compose hierarchies found` |
| screen on + unlocked (`Awake`) | all pass |

Across the whole suite the failure count went from 11 to 5 purely by turning the screen on, and the exception
disappeared entirely. The earlier investigation had measured the decisive clue without recognising it: the activity's
`ComposeView` was `0x0` with `hasWindowFocus=false`, while the decor and content views had real sizes.

`WorkManagerTestExampleTest` (work-testing, no UI) passes either way, which is why the suite never failed completely.

### Why this is an operating rule and not a code change

Forcing the screen on from the test process is possible — `WAKE_LOCK` + `DISABLE_KEYGUARD` in the androidTest
manifest plus a custom `AndroidJUnitRunner` — but it adds permissions and test infrastructure to work around a local
device state. The project runs these tests from a developer machine against a connected handset, so the two `adb`
commands above are enough. Revisit this if the suite ever runs unattended in CI.

---

## Selector hygiene

A harness outage hides real failures. When the screen-off problem was fixed, five genuine failures surfaced that had
been invisible for weeks (`TEST-SELECTOR-DRIFT-01`), both of them selector drift rather than product bugs:

- **Text selectors go stale when a screen grows.** `UITestExampleUI` demonstrates three ways to write a click handler,
  and all three buttons share one `text` state, so `onNodeWithText("Hello")` matches three nodes and
  `assertExists()` fails with *"Expected exactly '1' node but found '3' nodes"*. Prefer `onNodeWithTag` where the
  screen can carry a tag; use `onAllNodesWithText(...).onFirst()` when it cannot.
- **Shared components change their semantics for every screen at once.** CONV-09 (commit `7d525732`) unified 47
  hand-made headers into `MainHeader`, whose back icon carries `contentDescription = "Back"`. Three tests still looked
  for `onNodeWithContentDescription("")` and could no longer find anything. When a shared component's semantics
  change, grep the androidTest sources for the old value in the same commit.

---

## Screens that never reach idle under the Compose test rule

Five registry screens never let the Compose test rule reach idle: `agslShaderTuningExample`, `flingBehaviorExample`,
`particleEmitterExample`, `sharedElementDebugToolingExample` and `waveformCanvasExample`. They do not all behave the
same way on a real clock (`dumpsys gfxinfo`, frames per 2 s, measured 2026-09-22 on SM-A725F):

| Screen | Real clock at rest | Why the harness never idles |
|---|---|---|
| `agslShaderTuningExample` | ~124 | the shader animates on every frame by design |
| `waveformCanvasExample` | ~35 (debug interpreter limits the rate) | a `withFrameNanos` loop generates the signal by design |
| `sharedElementDebugToolingExample` | 0 since `654ac484` (was ~124) | its duplicate-key demo kept the shared transition "active" forever; the demo is now off until the user switches it on |
| `flingBehaviorExample`, `particleEmitterExample` | 0 | idle on a real clock; only the test clock keeps them busy |

Every harness step that waits for idle hangs on the first two groups. The stack is **not** blocked: the main thread
keeps cycling through `MessageQueue.nativePollOnce`, so the harness never reports a failure. It just waits until the
runner kills it.

| Harness call | What waits | Observed |
|---|---|---|
| `createAndroidComposeRule` teardown (auto clock) | `TestMonotonicFrameClock` issues frames back to back | `waveformCanvasExample` held the runner for the full 900 s test timeout |
| Same rule with `mainClock.autoAdvance = false` | `Instrumentation.waitForIdleSync()` in teardown | `sharedElementDebugToolingExample` (before the fix) never returned |
| `ActivityScenario.onActivity { }` / `close()` | also `waitForIdleSync()` (ActivityScenario.java:806) | same screen hung inside `onActivity` |

Under the Compose rule, animations advance only on the test clock. `dumpsys gfxinfo` therefore reports 0 frames there
even for a screen that animates, so measure rendering on a real clock (below), not inside the rule.

To audit these screens, drive them without any idle wait:

1. `ActivityScenario.launch(ComponentActivity::class.java)` and one `onActivity { setContent { … } }`. This call returns,
   because the content is not drawing yet.
2. Wait on a real clock with `Thread.sleep`, then read the screen with `uiAutomation.takeScreenshot()` and
   `uiAutomation.executeShellCommand("dumpsys gfxinfo <pkg>")`. Neither goes through the main thread.
3. Leave the screen with `input keyevent KEYCODE_BACK` rather than `close()`.

Also:

- For a stack dump when a test hangs without root, add an outer JUnit rule that starts a daemon thread and logs
  `Thread.getAllStackTraces()` after N seconds. Catch `InterruptedException` in that thread's sleep; otherwise the
  interrupt that stops it at the end of the test kills the process.
- Click with `performSemanticsAction(SemanticsActions.OnClick)` instead of `performClick()` on screens whose layout
  moves while loading, such as paging lists. An injected touch can land on whatever slid under the old coordinates.
- To reach a Compose control on a real clock (outside the rule), walk `uiAutomation.rootInActiveWindow`
  (`AccessibilityNodeInfo`) and tap its screen bounds with `input tap`. `uiautomator dump` does not work while the
  instrumentation holds the UiAutomation connection.

---

## Related

- [`ComposeHotReloadGuide.md`](ComposeHotReloadGuide.md) — HotSwan behaviour. It was ruled out as a cause here.
- [`/CLAUDE.md`](../../../../../../../../CLAUDE.md) — build/commit workflow rules.
