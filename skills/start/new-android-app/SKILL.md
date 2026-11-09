---
name: new-android-app
description: Create a local-first Android app from a working template (Kotlin, Jetpack Compose, Material 3, Room, Robolectric self-test and snapshots, git) that builds the same on Windows, macOS, and Linux. Use when the user wants to start a new Android app or scaffold an Android project.
argument-hint: "<app-name> [parent-folder] [--title \"App Title\"] [--package com.acme.app] [--scanner]"
allowed-tools: Bash(node *new-app.mjs *) Bash(node *doctor.mjs) Bash(./gradlew *) Bash(git *) Read Write Edit
---

# New Android app

The template builds, tests, and runs as is. The first check proves the
toolchain works before you write feature code.

## What the template is

A single-module Android app in Kotlin with Jetpack Compose and Material 3.
Data lives on the device: Room for records, DataStore for settings. There is
no server and no sign-in. Tests run on the JVM through Robolectric, so no
emulator is needed. minSdk is 26.

```text
<app-name>/
  settings.gradle.kts  build.gradle.kts  gradle.properties  gradlew  gradlew.bat
  gradle/libs.versions.toml   every library version, in one place
  CLAUDE.md            build, check, and design rules for agents
  app/
    build.gradle.kts   plugins, Room schema folder, test tasks: snap, selftest, grid
    schemas/           exported Room schemas, one per database version
    src/main/kotlin/<package path>/
      App.kt           Application and AppContainer, built by hand (no Hilt)
      MainActivity.kt  Pages.kt   the pages; snapshots visit every one
      data/            Item, ItemDao, AppDatabase, ItemRepository
      settings/        SettingsStore (DataStore: accent, wallpaper colors)
      lib/             ItemTitle.kt, pure Kotlin, with a unit test next to it
      ui/              AppRoot, AppShell, ItemsScreen, ItemDetailScreen, SettingsScreen,
                       ItemsViewModel, Route (Navigation 3 back stack)
      ui/theme/        Theme, Color, Accents, Type, Shape, Spacing, Motion
    src/main/res/      strings, themes, adaptive launcher icon (with themed layer), vector icons
    src/test/kotlin/<package path>/
      SelfTest.kt      behavior: add, rename, star, delete and Undo, restart, back
      SnapTest.kt      a PNG per page, light and dark, phone and tablet
      GridLintTest.kt  LayoutProbeTest.kt   4 dp grid checks
      TestApp.kt  Seed.kt   test container and sample data; never the real database
    src/androidTest/   SmokeTest.kt, for a device or emulator
```

## Steps

1. Check the machine: `node "${CLAUDE_SKILL_DIR}/scripts/doctor.mjs"` prints
   `PASS doctor`. On a FAIL line, do its `fix:` and run it again. A machine
   with no JDK or SDK: read [setup-toolchain.md](setup-toolchain.md).

2. Pick the name (lowercase, digits, and dashes) and a title. If
   `docs/BRIEF.md` exists, use its name. The package defaults to
   `com.example.<name>`. Pass `--package` with the real one when the user
   has it, because the applicationId cannot change after the first Play
   release.

3. Run the script with the parent folder. It copies the template, fills in
   the name, title, and package, writes `local.properties` when the SDK is
   in its default place, runs `assembleDebug`, runs `git init`, and commits.
   It keeps a folder that holds only a brief.

   ```bash
   node "${CLAUDE_SKILL_DIR}/scripts/new-app.mjs" daily-log ~/dev --title "Daily Log" --package com.acme.dailylog
   ```

   Add `--scanner` when the app scans with a rugged device's built-in scanner (see `rugged-scanning`).
   The first build downloads Gradle and the libraries and takes several
   minutes. Run it in the background.

4. Run every check, then look at the snapshots (`build-and-verify` skill):

   ```bash
   cd ~/dev/daily-log
   ./gradlew check     # lint, unit tests, self-test, grid checks, snapshots
   ```

   On Windows use `.\gradlew.bat` in cmd and PowerShell. The command exits 0,
   and `shots/` holds `<page>-light.png` and `<page>-dark.png` for every page.
   The first run also downloads Robolectric's Android jars.

5. Run the app for the user. With a device or emulator attached, `./gradlew
   installDebug`, then open it. Without one, the snapshots are the proof.

6. If `docs/BRIEF.md` exists, rename `Item` to the brief's main type and the
   pages in `Pages.kt` to the brief's pages. Nothing else: features go
   through `improve-loop`, one story at a time. Check, snapshot, commit.

## Versions the template pins

The versions live in `gradle/libs.versions.toml`. Change them together:
KSP must match the Kotlin version, and Gradle must satisfy the Android
Gradle Plugin. `compileSdk` is 37 because the pinned AndroidX libraries ask
for it; `targetSdk` stays 36, which Robolectric supports. AGP 9 compiles
Kotlin itself, so the template applies no `kotlin-android` plugin.

## When to add a server

The device database covers most apps. Add a server only when the brief says
data is shared between people or must reach another device. Then keep the
app as is and put the server in `server/` with its own build, so the
self-test can still run the app alone.

## When the build fails

- Fix the first error first. Later errors often come from it.
- `Dependency ... requires ... compile against version N`: raise `compileSdk`
  in `app/build.gradle.kts` to N and install that platform
  (`sdkmanager "platforms;android-N.0"`). Leave `targetSdk` alone.
- `SDK location not found`: set `ANDROID_HOME`, or write `sdk.dir=C:/path/to/Sdk`
  in `local.properties`. Run `doctor.mjs` to see which one is missing.
- `'gradlew.bat' is not recognized` on Windows: use `.\gradlew.bat`.
- `Unsupported class file major version` or a JDK error: the JDK is too old
  or too new. Point `JAVA_HOME` at JDK 17 to 21 and run `./gradlew --stop`.
- A KSP error that names a Kotlin version: the `ksp` plugin version in
  `libs.versions.toml` does not match `kotlin`. Use the KSP release for your Kotlin.
- The first `check` seems stuck: Robolectric is downloading Android jars.
  Wait, and check the network if it stays that way for ten minutes.
- `OutOfMemoryError` or the system kills Gradle: close other programs, run
  `./gradlew --no-daemon check`, and lower `org.gradle.jvmargs` in `gradle.properties`.
- Windows: `Unable to delete directory` or a locked file: run `./gradlew --stop`
  and retry. Antivirus scanning `build/` causes the same error.
- Compose or Kotlin errors in your own code: call the Skill tool with `kotlin-compose`.

Done when `./gradlew check` exits 0, the snapshots show every page in light
and dark, and the first commit exists.
