---
name: rugged-scanning
description: Read barcodes in an Android app on rugged devices with a built-in scanner, through DataWedge intents (the default) or EMDK, with a profile created from code, a test path that needs no device, and a camera fallback for other phones. Use when the app must scan barcodes on a rugged handheld, when the user mentions DataWedge, EMDK, a hardware scan trigger, or TC, MC, or EC series devices.
---

# Rugged scanning

Rugged Android handhelds have a built-in barcode scanner. On devices that ship DataWedge, two routes read it from an app:

| | DataWedge (default) | EMDK |
| --- | --- | --- |
| What it is | A preinstalled service on devices that ship it. The app sends and receives Android intents | A Java library, from the maker's Maven repository, that the app calls directly |
| Needs in the app | Nothing to install: only intents | A Gradle `compileOnly` dependency, a manifest `uses-library`, and the EMDKManager lifecycle |
| Works on JVM tests | Yes, with a fake broadcast | Only behind your own interface |
| The maker's advice | Use it for all apps that need barcode scanning | Keeps working, but the maker recommends DataWedge for barcodes |
| Choose it when | Almost always | You also need EMDK-only features, such as applying MX settings from code |

Start with DataWedge. The `--scanner` switch of `new-android-app` already adds it. Everything below that
says "the template" refers to its `:scanner` module.

## 1. Start from the template

```bash
node "<plugin>/skills/start/new-android-app/scripts/new-app.mjs" my-app ~/dev --scanner
```

You get the `:scanner` module: `Scanner` (interface: `scans`, `setEnabled`, `softTrigger`, `onScan`),
`DataWedgeScanner`, and `FakeScanner`. `App.kt` calls `DataWedgeScanner(this).onScan(appScope) { ... }`, and the
template adds an item for each scan. Replace that line with your app's reaction to a scan. Keep screens
talking to `Scanner`, never to DataWedge directly, so the JVM tests can use `FakeScanner`.

## 2. How DataWedge works

DataWedge works with profiles. A profile has input (Barcode), processing, and output plugins. A profile
can be tied to apps, but an app can have only one profile. DataWedge watches the foreground app and
activates its profile. `Profile0` is the default for apps with no profile. DataWedge comes pre-installed on
the devices that support it.

`DataWedgeScanner` creates the app's profile the first time it starts, by broadcasting `SET_CONFIG`:

- Action `com.symbol.datawedge.api.ACTION`, package `com.symbol.datawedge`, extra
  `com.symbol.datawedge.api.SET_CONFIG` holding a bundle: `PROFILE_NAME`, `PROFILE_ENABLED`, `CONFIG_MODE`
  (`CREATE_IF_NOT_EXIST`, `OVERWRITE`, or `UPDATE`), `PLUGIN_CONFIG`, and `APP_LIST`
  (`PACKAGE_NAME` and `ACTIVITY_LIST`, where `"*"` means all activities).
- Plugin `BARCODE` with `scanner_selection=auto` and `scanner_input_enabled=true`.
- Plugin `INTENT` with `intent_output_enabled=true`, `intent_action=<package>.SCAN`,
  `intent_category=android.intent.category.DEFAULT`, and `intent_delivery=2` (broadcast, an `Int`).
- Plugin `KEYSTROKE` with `keystroke_output_enabled=false`, so a scan does not also type into a field.
- Nearly every value is a `String`, even `"true"`. `intent_delivery` is the one `Int`.

`CREATE_IF_NOT_EXIST` leaves an existing profile alone, so IT's changes survive. After you change the
profile in code, delete it in the DataWedge app (or use `OVERWRITE` while developing), or the old one stays.

Each scan arrives as a broadcast to `<package>.SCAN` with these extras:

| Extra | Meaning |
| --- | --- |
| `com.symbol.datawedge.data_string` | The barcode text |
| `com.symbol.datawedge.label_type` | The symbology, like `LABEL-TYPE-EAN13` |
| `com.symbol.datawedge.source` | `scanner` for the scanner or camera, and others for MSR, serial, and so on |

## 3. Gotchas

- **Receiver flag.** DataWedge is another app, so register the receiver with `RECEIVER_EXPORTED`
  (`ContextCompat.registerReceiver`). Android 13 added the export flags, and an app that targets Android 14 or
  later must pass one.
- **Package visibility.** The `:scanner` manifest declares `<queries><package android:name="com.symbol.datawedge"/></queries>`
  so the app can see DataWedge on Android 11 and later. The DataWedge docs do not require it; it is harmless.
- **Commands are not queued.** DataWedge may ignore an API command while it is busy, and right after a reboot.
  If setup matters, send `GET_VERSION_INFO` or `GET_DATAWEDGE_STATUS` first and wait for the result.
- **Android 14 delay.** Intents to DataWedge can take about half a second. The DataWedge docs suggest `sendOrderedBroadcast()` when
  that hurts.
- **Results.** Add the extras `SEND_RESULT` and `COMMAND_IDENTIFIER` to a command and register a receiver for
  `com.symbol.datawedge.api.RESULT_ACTION` (category `android.intent.category.DEFAULT`) to learn whether it worked.
- **Exclusive hardware.** The DataWedge docs say control of the barcode scanning hardware is exclusive, so only one profile at a time owns the scanner.

## 4. Control the scanner from code

- Soft trigger: extra `com.symbol.datawedge.api.SOFT_SCAN_TRIGGER` with `START_SCANNING`, `STOP_SCANNING`, or
  `TOGGLE_SCANNING`. It works only when Barcode input is on in the active profile. `Scanner.softTrigger()` does this.
- Turn the scanner off for a screen (a dialog, a settings page): extra `com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN`
  with `DISABLE_PLUGIN`, then `ENABLE_PLUGIN`. `SUSPEND_PLUGIN` and `RESUME_PLUGIN` also exist. It changes
  nothing in the stored profile. `Scanner.setEnabled(false)` does this; call it from `onPause` and `onResume`.
- Keystroke output (a fallback for old apps): the profile types the scan into the focused field. On Android 10 and later the
  keyboard disappears briefly during a scan.

## 5. Test without a device

- JVM: use `FakeScanner` and call `scan("0123456789")`. `ScanTest` shows both the fake and a real broadcast on Robolectric.
- Any phone or emulator with the app installed (the receiver is exported, so adb can reach it):

  ```bash
  adb shell am broadcast -a <package>.SCAN --es com.symbol.datawedge.data_string 0123456789 --es com.symbol.datawedge.label_type LABEL-TYPE-EAN13
  ```

- A real device with DataWedge: press the trigger and watch the item appear. A test on real hardware is the only proof the profile works.

## 6. Phones without a scanner

DataWedge commands go nowhere on a device that does not have it, so `scans` never emits. Give those phones
a camera scan button behind the same `Scanner` interface (call the Skill tool with `android-integrations`:
the Google code scanner), and pick the implementation at startup: DataWedge if `com.symbol.datawedge` is installed,
the camera otherwise.

## 7. EMDK route

Use it only when DataWedge cannot do the job.

1. Gradle: add the repository `https://zebratech.jfrog.io/artifactory/EMDK-Android/` to `dependencyResolutionManagement`
   in `settings.gradle.kts`, and the dependency `compileOnly("com.symbol:emdk:9.1.1")` (look up the current version in the EMDK setup page).
2. Manifest: `<uses-permission android:name="com.symbol.emdk.permission.EMDK" />`, `<queries><package android:name="com.symbol.emdk.emdkservice" /></queries>`
   for Android 11 and later, and inside `<application>` `<uses-library android:name="com.symbol.emdk" android:required="false" />`
   (`required="false"` is the standard Android attribute that lets the app install on devices without the library).
3. Lifecycle: `EMDKManager.getEMDKManager(applicationContext, listener)` from the main thread. The listener
   (`EMDKManager.EMDKListener`) gets `onOpened(emdkManager)`, where you call `emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE)`
   to get a `BarcodeManager`, then `barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)`. It also gets `onClosed()`.
   Call `emdkManager.release()` in `onDestroy` and `onClosed`.
4. Wrap all of it in a class that implements `Scanner`, so the screens do not change.

EMDK runs only on devices from its maker. The maker requires email multi-factor authentication for its single-sign-on sites,
such as its support center, from July 1, 2026. Its pages do not say which downloads sit behind that
sign-in, so expect to sign in for tools hosted there.

Done when scans reach the app through `Scanner` only, `ScanTest` passes, the adb broadcast adds an item on a
phone or emulator, a test on a device with DataWedge is done or named as a manual check, a phone without a scanner has the camera path,
and the scanner is disabled while a dialog is open.

## Sources

- https://techdocs.zebra.com/datawedge/latest/guide/about/
- https://techdocs.zebra.com/datawedge/latest/guide/profiles/
- https://techdocs.zebra.com/datawedge/latest/guide/api/overview/
- https://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
- https://techdocs.zebra.com/datawedge/latest/guide/api/softscantrigger/
- https://techdocs.zebra.com/datawedge/latest/guide/api/scannerinputplugin/
- https://techdocs.zebra.com/datawedge/latest/guide/api/getversioninfo/
- https://techdocs.zebra.com/datawedge/latest/guide/api/getdatawedgestatus/
- https://techdocs.zebra.com/datawedge/latest/guide/output/intent/
- https://techdocs.zebra.com/datawedge/latest/guide/output/keystroke/
- https://techdocs.zebra.com/emdk-for-android/15-1/guide/setup
- https://techdocs.zebra.com/emdk-for-android/15-1/guide/barcode_scanning_guide
- https://techdocs.zebra.com/emdk-for-android/15-1/tutorial/tutBasicScanningAPI
- https://www.zebra.com/mfa-sso
