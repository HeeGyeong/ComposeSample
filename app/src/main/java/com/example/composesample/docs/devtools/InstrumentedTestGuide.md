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

## Related

- [`ComposeHotReloadGuide.md`](ComposeHotReloadGuide.md) — HotSwan behaviour. It was ruled out as a cause here.
- [`/CLAUDE.md`](../../../../../../../../CLAUDE.md) — build/commit workflow rules.
