---
name: build-and-verify
description: Build an Android app, run its checks, and look at light and dark snapshots of every screen before calling a change done. Use when a UI code change is made, when a Gradle build fails, or when the user asks to run, screenshot, or check the app.
allowed-tools: Bash(./gradlew *) Bash(.\gradlew.bat *) Bash(adb *) Bash(git *) Read
---

# Build and verify

A change is done when four things are true:

1. `./gradlew assembleDebug` finishes with no errors or warnings.
2. You looked at the light and dark snapshots of every screen you touched.
3. The snapshots match what the user story or reference screenshot asks for.
4. The grid checks pass (`layout-grid` skill).

On Windows, run `.\gradlew.bat` in cmd and PowerShell. `./gradlew` works in Git Bash.

App not made from the `new-android-app` template? Copy `SnapTest.kt`, `GridLintTest.kt`,
`LayoutProbeTest.kt`, `TestApp.kt`, `Seed.kt`, and the `snap`, `selftest`, and `grid` tasks from
the template's `app/build.gradle.kts`. Change the pages in `Pages.kt` to the app's screens.

Never report a UI change as done from the code alone. Code that compiles can still render an
empty column, clipped text, or unreadable dark mode.

## 1. Build

```bash
./gradlew assembleDebug      # the debug APK, in app/build/outputs/apk/debug/
```

Read the errors, fix them, and build again. Fix the first error first: later errors often come
from it. Treat a Kotlin or Android lint warning as an error to fix, not a note.

## 2. Snapshot

```bash
./gradlew snap
```

Robolectric renders every page in `Pages.kt` on the JVM, in light and dark, at phone size and at
tablet width. Each run uses a test container with an in-memory database and seed data
(`Seed.kt`), so the user's real data is never touched and the seed is never stale. It writes
`shots/<page>-light.png`, `shots/<page>-dark.png`, and the `-tablet` versions, plus the launcher icon.

- A dialog, sheet, or menu needs its own state: add a test to `SnapTest.kt` that opens it and
  saves `shots/<page>-<state>-<mode>.png`.
- Seed data: empty screens only show empty states. `Seed.kt` fills the test database. Grow it
  with the app: 10 to 20 items with long and short titles, every status, and only what a person
  can create in the app.

## 3. Check the grid

```bash
./gradlew grid
```

Both parts print PASS in the test output. On a FAIL, call the Skill tool with `layout-grid`: it has
the rules, the overlay, and a table of fixes.

## 4. Look

Open every PNG of the screens you touched with the Read tool, and check, in this order:

- Nothing is blank, clipped, or overlapping. Text fits. Long titles truncate. The navigation
  shows all its items.
- Dark mode is readable. No fixed white or black surfaces.
- The screen matches the story or the reference screenshot (layout, labels, order).
- The `material-design` rules hold: one accent for selection, the primary action, and focus;
  tokens only; empty states that invite the first action.
- Phone shows a bottom bar and tablet shows a rail.

Describe what you see in one or two sentences per screen in your report, and name the PNG paths so
the user can open them.

## 5. Behavior

Snapshots show state, not behavior. Run `./gradlew selftest` and add a test for each new action.
The `test-android-app` skill covers unit tests, the self-test, accessibility checks, and device tests.

`./gradlew check` runs lint, the unit tests, the self-test, the snapshots, and the grid checks in one go.
It exits 0.

## 6. Show the user

With a phone or emulator attached (`adb devices` lists it as `device`):

```bash
./gradlew installDebug
adb shell am start -n <package>/.MainActivity
```

The installed app uses its own real database, not the test container.

## 7. Commit

When the build is clean and the snapshots look right, commit with a message that says what the
user can now do: "Add tasks from the quick-add sheet", not "Update composables". Never commit
`shots/`, `local.properties`, `*.jks`, or `keystore.properties`.

Done when the four conditions at the top hold and the change is committed.
