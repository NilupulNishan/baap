---
name: performance
description: Make an Android app fast by measuring startup time, jank, list scrolling, app size, and memory, then fixing the cause. Use when the app starts slowly, a tap lags, scrolling stutters, the APK grows, memory grows, or the user asks to optimize or profile.
---

# Performance

Fast means three numbers on a mid-range phone: the app shows its first
screen in under 1 second from a cold start, every tap shows its result in
under 100 ms, and scrolling drops no frames. Every change in this skill
starts and ends with a **measurement**, so you know it helped.

## The loop

```text
reproduce -> measure (release-like build) -> find the line -> fix -> measure again -> commit with the numbers
```

1. **Reproduce.** Write the exact steps, or a test that runs the slow action.
2. **Measure a release-like build.** Debug builds are slower and point at
   the wrong code. Use a build with R8 on and `debuggable false`, on a real
   device when you can. Emulators and the JVM tests are for behavior, not speed.
3. **Find the line.** Pick the tool for the symptom:

   | Symptom | Tool |
   | --- | --- |
   | Slow start | `adb shell am start -W -n <package>/.MainActivity` prints `TotalTime`. For repeatable numbers, a Macrobenchmark with `StartupTimingMetric` |
   | Stutter while scrolling | `JankStats` (`androidx.metrics:metrics-performance`), or a Perfetto trace (system tracing on the device, then ui.perfetto.dev) |
   | Too many recompositions | Layout Inspector's recomposition counts in Android Studio, and the Compose compiler reports (below) |
   | A big APK | `apkanalyzer apk summary app-release.apk` and `apkanalyzer files list` (in the SDK's `cmdline-tools`) |
   | Memory grows | Android Studio's memory profiler: two heap dumps, compare. LeakCanary in debug builds |
   | Disk or network on the main thread | StrictMode (below) |

4. **Fix** with the rules below.
5. **Measure again** with the same steps. Put before and after in the commit
   message: "Cold start 820 ms, was 1.4 s".

## The rules that fix most problems

- **Show first, work after.** Update the screen in the same frame as the tap,
  then save. The Room write does not block the UI.
- **Lists render only what shows.** Use `LazyColumn` with `key = { it.id }` and a
  `contentType`, so rows are reused and moves do not recompose everything.
- **Keep startup light.** `Application.onCreate` builds the container and nothing
  else. Open Room and DataStore lazily on first use, and move SDK setup to a
  background coroutine.
- **Baseline Profiles** precompile the paths people use. Add them when a
  Macrobenchmark shows startup or first scroll costs more than you want
  (`androidx.profileinstaller` plus the `androidx.baselineprofile` Gradle plugin).
- **Read state late.** Pass a lambda (`Modifier.offset { ... }`) to animations so only
  the draw phase reruns, not the whole composable.
- **Heavy work leaves the main thread.** Parsing an import, image decoding, and
  sorting thousands of rows go in `Dispatchers.Default`.
- **Images at display size.** Decode a thumbnail, not the full photo.
- **Few dependencies.** Check the size a library adds to the APK before you
  keep it, and prefer the platform.
- **Database queries use an index** on columns you filter or sort, once a table
  passes a few thousand rows.
- **No polling without a reason.** Use WorkManager or a `Flow` from Room.

## Tools you set up once

Compose compiler reports show which composables are skippable. In
`app/build.gradle.kts`:

```kotlin
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}
```

StrictMode in debug builds catches disk and network access on the main
thread. In `App.onCreate`, guard it with the debuggable flag, since AGP does
not generate `BuildConfig` unless you ask for it:

```kotlin
if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
}
```

LeakCanary: `debugImplementation("com.squareup.leakcanary:leakcanary-android:<current version>")`.
It reports leaks on its own in debug builds.

R8 is on in the template's release build (`isMinifyEnabled` and `isShrinkResources`).
After you add a library, run `./gradlew assembleRelease` and open the app: a missing
keep rule shows up as a crash only in release.

Done when the slow action has a measured before and after on a release-like
build, the after meets the three numbers at the top, and the commit message names both.
