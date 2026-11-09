---
name: kotlin-compose
description: Kotlin and Jetpack Compose code rules for a local-first Android app, covering state, effects, coroutines, Room, forms, hardware keys, and fixes for common compile and runtime errors. Use when writing, reviewing, or debugging Kotlin or Compose code in an Android app, or on a compile error, a recomposition loop, lost state, or a Room error.
---

# Kotlin and Compose

Rules for code an agent writes and a person can still read a year later.
They assume the `new-android-app` template: Compose with Material 3, a
ViewModel per list screen, Room for data, DataStore for settings, no DI
library.

## Where code goes

| Code | Place (under `app/src/main/kotlin/<package path>/`) |
| --- | --- |
| Rules, parsing, formatting, data transforms | `lib/`, plain Kotlin, no Android classes. Tests in `app/src/test`, same package |
| Room entities, DAOs, the database, the repository | `data/` |
| One screen | `ui/<Name>Screen.kt`, one screen per file |
| State for a screen | `ui/<Name>ViewModel.kt` |
| Colors, gaps, shapes, durations | `ui/theme/` |
| Settings that survive restarts | `settings/SettingsStore.kt` |
| Strings the user reads | `res/values/strings.xml` |

Keep each file under about 300 lines. Split by feature (`Projects` with its
rows and dialog), not by kind.

## State

1. Data that must survive the app closing lives in Room. A ViewModel exposes
   it as `StateFlow` and the screen reads it with `collectAsStateWithLifecycle()`.
   Never copy it into `remember`.
2. Settings live in DataStore, through `SettingsStore`.
3. UI state (selection, an open dialog, a draft) lives in `remember` in the
   lowest composable that needs it. Use `rememberSaveable` for what must
   survive rotation and process death: text in a field, the selected tab.
4. Derive, do not store: a filtered list or a count is computed from the state.
5. Hoist state: a composable takes `value` and `onChange` and owns nothing
   it does not have to.

ViewModel pattern:

```kotlin
val items: StateFlow<List<Item>> = repository.observeAll()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
```

## Effects

- `LaunchedEffect(key)` runs a suspend block when `key` changes. Use it for
  one-time work tied to the composition, like focusing a field.
- `DisposableEffect(key)` is for listeners and callbacks. It must end with
  `onDispose { ... }` that removes them.
- `rememberCoroutineScope()` is for work that starts from a click.
- Do not compute a value in an effect. Compute it in composition.
- Never read a `State` you did not key on inside a long-lived effect. Use
  `rememberUpdatedState` for a callback that changes.

## Coroutines

- Room suspend functions and `Flow` queries run off the main thread. Never
  call Room on the main thread (no `allowMainThreadQueries`).
- Use `viewModelScope` in a ViewModel, `appScope` (from `App`) for work that
  must outlive a screen, and `Dispatchers.IO` only for blocking calls
  such as files.
- Inject dispatchers only when a test needs control of them.
- Cancellation is normal. Do not catch `CancellationException` and swallow it.

## Room

- Queries return `Flow` for lists the screen watches, and `suspend` for one-off reads and writes.
- A schema change is a new `version` in `@Database` plus a migration. Use
  `autoMigrations = [AutoMigration(from = 1, to = 2)]` for added columns, tables,
  and renames you declare, and a `Migration` object for the rest. The schemas
  exported to `app/schemas/` are what migrations are checked against: commit them.
- A new column has a default, so old rows still load.
- Ids: a `Long` with `autoGenerate = true` is fine for a local-only app. Use a
  `UUID` string when the data will ever be exported and merged.
- Index only columns you sort or filter on.
- Several writes that belong together go in a `@Transaction` function.
- Never ship a `fallbackToDestructiveMigration()`: it deletes people's data.

## Forms and keys

- Give every text field a label, and the right `KeyboardOptions` (`ImeAction.Done`
  for the last field, `Next` between fields, `KeyboardType.Number` for numbers),
  with `KeyboardActions` to match.
- Save as people type for fields on an item (title, notes). Use a button for
  things created all at once.
- Move focus with `FocusRequester` and `LocalFocusManager.current.clearFocus()`.
- Rugged devices have hardware keys and keypads. Handle them with
  `Modifier.onKeyEvent { ... }` or `onPreviewKeyEvent`, return `true` only when you handled the key.
- Touch targets are at least 48 dp. Every icon-only button has a `contentDescription`.

## Recomposition

- Pass stable types: `List` from the standard library is treated as unstable
  unless the compiler can prove otherwise, so prefer immutable data classes,
  and check the Compose compiler report when a screen is slow
  (call the Skill tool with `performance`).
- Lists: `LazyColumn { items(list, key = { it.id }, contentType = { "row" }) { ... } }`.
- Do not allocate in composition on every frame: wrap objects in `remember`.
- Read state as late as possible: pass a lambda (`{ offset }`) to modifiers
  that animate.

## Common errors

| Error | Fix |
| --- | --- |
| `Unresolved reference` for a Compose function | A missing import. Android Studio adds them, an agent does not: add `androidx.compose...` by hand |
| `@Composable invocations can only happen from the context of a @Composable function` | A composable called from a callback or `remember`. Call it in composition and pass the result in |
| `Cannot access database on the main thread` | A DAO call outside a coroutine. Move it into `viewModelScope.launch` or a suspend function |
| `Room cannot verify the data integrity` | The schema changed without a version bump or migration. Bump `version`, add the migration, keep the schema file |
| `Flow invariant is violated: Emission from another coroutine is detected` | `emit` was called from another context (often inside `withContext`). Use `flowOn(Dispatchers.IO)` on the upstream instead |
| A screen resets on rotation | State held in `remember`. Use `rememberSaveable`, or move it into the ViewModel |
| Snackbar never shows | `showSnackbar` runs in a coroutine that was cancelled with its screen. Launch it from a scope that outlives the screen (the template uses `AppRoot`'s scope) |
| A lazy list jumps after an insert | The item key is not stable. Use the database id, never the index |

Done when `./gradlew assembleDebug` has no errors or warnings, `./gradlew check`
passes, no Room call runs on the main thread, and every new effect that
registers something has an `onDispose`.
