# __APP_TITLE__

A local-first Android app in Kotlin and Jetpack Compose (Material 3). Data lives
on the device: Room for records, DataStore for settings. There is no server.
Package `__APP_PACKAGE__`, minSdk 26.

## Layout

All paths are under `app/src/main/kotlin/<package path>/`.

- `App.kt` the `Application` and `AppContainer`, built by hand (no DI library).
- `MainActivity.kt` edge-to-edge, splash screen, then `AppRoot`.
- `Pages.kt` the top-level pages. Snapshots and the self-test visit every entry. Add a new page here first.
- `ui/AppRoot.kt` theme, back stack (Navigation 3), Undo snackbar. `ui/AppShell.kt` bottom bar or rail, top bar, FAB.
- `ui/*Screen.kt` one file per screen. `ui/ItemsViewModel.kt` state for the lists.
- `data/` Room: `Item` (the record), `ItemDao`, `AppDatabase`, `ItemRepository`. A schema change is a new `version` plus a migration.
- `settings/SettingsStore.kt` DataStore preferences (accent, wallpaper colors).
- `lib/` rules with no Android classes. Unit tests sit in `app/src/test`, same package.
- `ui/theme/` every color, gap, shape, and duration. `res/` strings, icons, the launcher icon.
- `app/src/test/` JVM tests (Robolectric): `SelfTest` (behavior), `SnapTest` (screenshots), `IconTest` (launcher icon PNGs), `GridLintTest`, `LayoutProbeTest`.
- `app/src/androidTest/` `SmokeTest`, for a device or emulator. Not part of `check`.
- `docs/` the brief, the screen index, and the user stories, when they exist.

## Build and check

- Debug build: `./gradlew assembleDebug` (Windows: `gradlew.bat`). Fix every error and warning.
- Everything: `./gradlew check` runs lint, unit tests, the self-test, the grid checks, and the snapshots. It exits 0.
- Self-test: `./gradlew selftest`. Add a test for every story with behavior.
- Snapshots: `./gradlew snap` writes `shots/<page>-light.png` and `-dark.png` for every page, plus `-tablet` versions and the launcher icon (`icon-512.png`, `icon-48.png`, `icon-themed.png`). Look at them after every UI change.
- Grid checks: `./gradlew grid`. Both parts print PASS in the test output.
- Install on a device: `./gradlew installDebug`. Device tests: `./gradlew connectedDebugAndroidTest`.
- A change is done when the build has no errors, `check` passes, and you looked at the light and dark snapshots.
- First run downloads Gradle and Robolectric jars. Machine setup: run `node <baap plugin>/skills/start/new-android-app/scripts/doctor.mjs`, or see `setup-toolchain.md` in the new-android-app skill.

## Test data

- Tests build their own container (`testContainer` in `app/src/test`) with an in-memory database and a temp settings file.
- Seed data (`Seed.kt`) lives only in `src/test`, so the app never ships it and tests never touch real data.
- Never seed, snapshot, or test against the app's real database (`app.db`).

## Design rules

- Every color, gap, shape, and duration comes from `ui/theme/`. Never raw values in screens.
- Colors come from `MaterialTheme.colorScheme` roles. Gaps come from `MaterialTheme.spacing`. All `dp` numbers are multiples of 4 (1.dp and 2.dp for hairlines).
- Mark layout parts with `Modifier.gridProbe("area/name")`. Parts in one area share a leading edge.
- Light and dark follow the system. Check both in every snapshot.
- Touch targets are at least 48 dp. Every icon-only button has a `contentDescription` that says what it does.
- Visible text lives in `res/values/strings.xml`.
- Empty states say what the page is for and offer the first action as a button.
- Destructive actions offer Undo (a Snackbar) instead of a confirmation.
- Back works everywhere: system back and predictive back pop one screen.

## Skills

The baap plugin has a skill for each step: `material-design`, `design-system`,
`layout-grid`, `ux-design`, `kotlin-compose`, `android-integrations`,
`performance`, `build-and-verify`, `test-android-app`, `app-icon`,
`user-stories`, `improve-loop`, `release`, and for company-managed devices
`managed-devices`. Use them.

## Git

- Commit after each working change, with a message that says what the user can now do.
- Never commit `keystore.properties`, `*.jks`, `local.properties`, or `shots/`.
