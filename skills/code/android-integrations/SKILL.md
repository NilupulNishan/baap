---
name: android-integrations
description: Android integrations for an app that reaches outside its own screens, such as notifications, background work, files, share, clipboard, camera barcode scanning, widgets, shortcuts, deep links, backup, and the permission each one needs. Use when an app must remind people, run in the background, save or open files, scan barcodes, share content, add a widget, or talk to the operating system.
---

# Android integrations

An app feels native when it reminds, shares, opens and saves files, and keeps
its data. Each integration below says what it needs, where it works, and how
to check it. Look up the current version of any library on its release page
before you add it, and put the version in `gradle/libs.versions.toml`.

Check the API level before you build on a call: the reference at
developer.android.com says "Added in API level N" on every method. minSdk is
26, so anything newer needs a version check or a fallback.

## Rules for every integration

- Ask for a permission at the moment people use the feature, after a tap.
  Never at launch.
- Show why first: one line in the app ("Reminders need to send you
  notifications"), then the system dialog (a rationale screen).
- Handle "denied" without nagging. Explain once how to turn it on in system
  settings, and keep the app working without it.
- Prefer a system picker or an intent over a permission: the photo picker,
  the document picker, and the Google code scanner need none.
- Check each one on a phone with the oldest Android you support and on the newest.

## Notifications

- Android 13 (API 33) and later: request `POST_NOTIFICATIONS` at runtime with
  `ActivityResultContracts.RequestPermission()`, after a tap.
- Every notification belongs to a channel (required from Android 8, so always).
  Create channels once at startup with `NotificationManagerCompat.createNotificationChannel`.
- Build with `NotificationCompat.Builder`, give it a small icon (a flat vector,
  not the launcher icon), and a `PendingIntent` that opens the right screen. Use
  `PendingIntent.FLAG_IMMUTABLE`.
- Check: `adb shell dumpsys notification --noredact` lists it, and the self-test
  asserts the builder code with Robolectric (`ShadowNotificationManager`).

## Background work

Use WorkManager (`androidx.work:work-runtime-ktx`) for work that must finish even
if the app closes: export, cleanup, a reminder check. Details, limits, and
exact alarms are in [background-and-widgets.md](background-and-widgets.md).

## Files

| Need | API |
| --- | --- |
| Save a file the user picks the place for | `ActivityResultContracts.CreateDocument("application/json")` |
| Open a file | `ActivityResultContracts.OpenDocument()` |
| Pick a photo or video | `ActivityResultContracts.PickVisualMedia()` (the photo picker, no permission) |
| Pick a folder | `ActivityResultContracts.OpenDocumentTree()` |

These use the Storage Access Framework, so the app needs no storage
permission. Do not ask for `READ_EXTERNAL_STORAGE`. Keep app-private files in
`context.filesDir` (backed up) or `cacheDir` (not).

## Export and import

Every app with data gets both in Settings. Export writes every table as one
JSON file with a version number, through `CreateDocument`. Import reads it
through `OpenDocument`, checks the version and the shape, and writes nothing
until the whole file is valid. This is the real backup.

## Backup

`android:allowBackup`, `dataExtractionRules`, and `fullBackupContent` are set in
the template manifest and point at `res/xml/backup_rules.xml` and
`data_extraction_rules.xml`. Edit them to include the Room file and DataStore,
and exclude caches and anything secret. Auto Backup is a convenience: export and
import stay the promise.

## Share

- Out: `Intent(Intent.ACTION_SEND)` with `type = "text/plain"` and
  `EXTRA_TEXT`, wrapped in `Intent.createChooser(...)`.
- In: an `<intent-filter>` on the activity for `android.intent.action.SEND`,
  category `android.intent.category.DEFAULT`, and a `<data android:mimeType=...>`.
  Read `intent.getStringExtra(Intent.EXTRA_TEXT)` in `onCreate` and `onNewIntent`.
- Check: `adb shell am start -a android.intent.action.SEND -t text/plain --es android.intent.extra.TEXT hello <package>/.MainActivity`.

## Clipboard

Copy with `LocalClipboard.current.setClipEntry(ClipEntry(ClipData.newPlainText(label, text)))`
from a coroutine. Android 13 and later shows its own "Copied" confirmation, so
show yours only on older versions. Avoid reading the clipboard without a user tap.

## Barcodes from the camera

On any phone, scan with the Google code scanner (`play-services-code-scanner`,
`GmsBarcodeScanning.getClient(context).startScan()`). It needs no camera
permission and no screen of your own. Use CameraX with ML Kit only when you need a
custom viewfinder or a continuous scan. On rugged devices use the built-in
scanner instead: call the Skill tool with `rugged-scanning`.

## Widgets and shortcuts

Glance widgets, static and dynamic shortcuts, and pinned shortcuts are in
[background-and-widgets.md](background-and-widgets.md).

## Deep links and predictive back

- A deep link is an `<intent-filter>` with `android.intent.action.VIEW`, the
  categories `DEFAULT` and `BROWSABLE`, and a `<data android:scheme="..." />`. Parse the
  `Uri` in the activity and push the matching `Route` onto the back stack. Check:
  `adb shell am start -a android.intent.action.VIEW -d "<scheme>://item/1"`.
- Predictive back is on in the template (`android:enableOnBackInvokedCallback="true"`).
  Use `BackHandler` only where back must do something other than pop one screen.

Done when each integration the brief lists works on the oldest and newest Android
you support, asks for its permission after a tap, shows a reason first, still works
when the permission is denied, and has a self-test or a named manual check.
