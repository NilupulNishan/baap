# Background work, widgets, and shortcuts

Read from [SKILL.md](SKILL.md) when the app needs work while closed, a home
screen widget, or launcher shortcuts.

## WorkManager

Add `androidx.work:work-runtime-ktx`. Write a `CoroutineWorker`:

```kotlin
class CleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // call into the repository here
        return Result.success()
    }
}
```

- One-off: `WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<CleanupWorker>().build())`.
- Repeating: `PeriodicWorkRequestBuilder<CleanupWorker>(1, TimeUnit.DAYS)`, enqueued with
  `enqueueUniquePeriodicWork(name, ExistingPeriodicWorkPolicy.KEEP, request)`. The
  shortest period is 15 minutes, and the system picks the exact time.
- Constraints: `Constraints.Builder().setRequiresCharging(true)` and the like.
- Workers reach the app through `applicationContext as App` and its `container`.
- Test with `androidx.work:work-testing` (`TestListenableWorkerBuilder`) on the JVM.

## Exact times (reminders)

WorkManager is not exact. A reminder at 9:00 sharp needs `AlarmManager` with
`setExactAndAllowWhileIdle`, which on Android 12 and later needs the
`SCHEDULE_EXACT_ALARM` permission (and from Android 14 it is off by default for
new installs). Prefer an inexact reminder ("around 9") and say so in the app.
Re-schedule alarms after reboot with a receiver for `BOOT_COMPLETED`.

## Foreground work

Work the user can see (a long export with progress) runs in a foreground service
with a notification and a declared `foregroundServiceType`. WorkManager can do
this for you with `setForeground(...)` inside a worker.

## Widgets with Glance

Add `androidx.glance:glance-appwidget`. Three parts:

1. A `GlanceAppWidget` with `provideGlance` that reads from Room and returns Glance composables.
2. A `GlanceAppWidgetReceiver` subclass that points at the widget.
3. A manifest `<receiver>` for the `android.appwidget.action.APPWIDGET_UPDATE`
   action, with `<meta-data android:name="android.appwidget.provider" android:resource="@xml/widget_info" />`,
   and `res/xml/widget_info.xml` that names size and the initial layout.

Update it with `MyWidget().updateAll(context)` after the data changes. Widgets use
Glance composables (`GlanceModifier`, `Text`, `Column`), not regular Compose.

## Shortcuts

- Static: `res/xml/shortcuts.xml` with `<shortcut android:shortcutId="add" ...>` and an
  `<intent>`, and `<meta-data android:name="android.app.shortcuts" android:resource="@xml/shortcuts" />`
  on the launcher activity.
- Dynamic or pinned: `ShortcutManagerCompat.pushDynamicShortcut` and
  `ShortcutManagerCompat.requestPinShortcut` with a `ShortcutInfoCompat`.
- Check: long press the launcher icon.
