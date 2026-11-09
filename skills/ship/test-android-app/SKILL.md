---
name: test-android-app
description: Test an Android app at four levels, from JVM unit tests to a Robolectric Compose self-test, accessibility checks, and device tests for what the JVM cannot do. Use when adding logic, fixing a bug, before a release, or when the user asks for tests, end-to-end checks, or "make sure it works".
---

# Test an Android app

Four levels, cheapest first. Each new story gets the cheapest level that can prove its acceptance
criteria. A bug fix starts with a test that fails.

| Level | Proves | Speed |
| --- | --- | --- |
| Unit tests (JUnit, `src/test`) | Rules, parsing, data transforms in `lib/` | Milliseconds |
| Self-test (Robolectric and Compose, `SelfTest.kt`) | Real flows on the JVM: taps, text, restart, Back | Seconds |
| Accessibility checks | Labels, touch targets, contrast | Seconds to a minute |
| Device tests (`src/androidTest`) | Camera, a real scanner, notifications, the real system UI | Minutes |

Snapshots (`build-and-verify` skill) prove how it looks. This skill proves how it behaves.
Everything above the last row runs in `./gradlew check`, with no emulator.

## 1. Unit tests

Logic lives in `lib/` with no Android classes, so tests run in milliseconds. A test file sits next to
its module, in the same package under `app/src/test/kotlin/`.

```kotlin
class ItemTitleTest {
    @Test fun trims_and_collapses_spaces() = assertEquals("Buy milk", clean("  Buy   milk "))
    @Test fun falls_back_when_empty() = assertEquals("Untitled", clean("   "))
}
```

```bash
./gradlew testDebugUnitTest
```

- Pass the clock in (`now: Instant`). Never read `System.currentTimeMillis()` inside logic you test.
- A parser that removes text from what the user typed needs at least 20 negative rows: real titles
  that contain the words it looks for.
- Table tests for anything with many cases. Keep the table in `docs/research/<topic>.md` too, and
  add a row there before changing behavior.

## 2. Self-test

`SelfTest.kt` runs the real UI on the JVM with Robolectric and `createComposeRule()`. Each test builds
its own container (`testContainer(context)`): an in-memory database and a temp settings file, so it
never touches real data.

```kotlin
@Test
fun stars_an_item_and_finds_it_on_the_starred_page() {
    runBlocking { env.database.seed() }
    compose.setContent { AppRoot(env.container) }
    compose.waitForText(seedItems.first().title)

    compose.onAllNodesWithContentDescription("Star")[0].performClick()
    compose.onNodeWithTag(Page.Starred.tag).performClick()
    compose.waitForText(seedItems.first { it.starred }.title)
}
```

```bash
./gradlew selftest
```

- Find nodes by text, content description, or test tag, the way a person and TalkBack find them. A test
  that cannot find a control by its description has found an accessibility bug.
- Never use `Thread.sleep`. Use `waitUntil` (the template's `waitForText` and `waitForTag`), which retries.
- Lazy lists compose only the visible rows. Scroll (`performScrollToIndex`) before you look for a row
  that may be off screen.
- Prove data survives: close the container, open a new one on the same database file, and read again
  (`StorageSelfTest`).

Checks worth adding to any self-test:

- Every page in `Pages.kt` opens with no crash (`SnapTest` covers it).
- Add creates a row that is visible. After a delete, Undo brings it back.
- Back pops one screen and returns to the list.
- The real database is never seeded: tests only use `testContainer`.

## 3. Accessibility checks

Compose 1.8 and later can run the Android Accessibility Test Framework checks (missing labels, small
touch targets, low contrast) inside a UI test. Add `androidTest` or `test` dependency
`androidx.compose.ui:ui-test-junit4-accessibility` and, in a test:

```kotlin
compose.enableAccessibilityChecks()
compose.setContent { AppRoot(container) }
compose.onNodeWithText("Add item").performClick()   // checks run on actions
compose.onRoot().tryPerformAccessibilityChecks()    // or run them on demand
```

Run the checks on a device or emulator (`src/androidTest`) first. If the JVM run also works for your
setup, move it into `SelfTest`. Run them in light and dark: contrast fails in one mode more often than in both.

The checks find about a third of real problems. Also walk each new flow with TalkBack once, and check
the snapshots at 1.3 times font scale.

## 4. Device tests

For what only a device can prove: the camera, a real hardware scanner, notifications, widgets, permission
dialogs. Tests in `app/src/androidTest/` use `AndroidJUnit4` and `createComposeRule()` (see
`SmokeTest.kt`).

```bash
./gradlew connectedDebugAndroidTest      # needs an attached device or emulator
```

No physical device in CI? Declare a Gradle Managed Device, which Gradle creates, boots, and shuts down
(API 27 and later):

```kotlin
android {
    testOptions {
        managedDevices {
            localDevices {
                create("pixel2api30") {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"   // "aosp" for the full image
                }
            }
        }
    }
}
```

Then run `./gradlew pixel2api30DebugAndroidTest`. Use the in-memory database in device tests too, never `app.db`.

## Runs that never hang

- One Gradle build at a time, and `--no-daemon` in CI and on low-memory machines.
- An emulator that fails to boot within a few minutes is a failed run: put a timeout on the CI step.
- First run on a machine: Robolectric downloads Android jars, and Gradle downloads its distribution.
- Windows: stop the daemon (`./gradlew --stop`) if a file is locked.
- `isIncludeAndroidResources = true` and `robolectric.properties` (`sdk=36`) are already set in the template.
  If Robolectric does not support your `targetSdk`, pin the highest it supports and keep `compileSdk` as is.

Done when every acceptance line of the story maps to a passing unit test, self-test step, device test,
or a named manual check, the accessibility check passes on the screens it touched, and the bug fix has a
test that failed before the fix.
