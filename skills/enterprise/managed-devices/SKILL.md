---
name: managed-devices
description: Make an Android app run well on company-managed devices, covering Android Enterprise device modes, managed configurations that any EMM can set, kiosk (lock task) mode, private distribution, and testing with a test device policy controller. Use when the app runs on company-owned or rugged devices, an IT admin must configure the app, the app must run as a kiosk, or the brief says MDM, EMM, or fleet.
---

# Managed devices

Companies run fleets of Android devices through an EMM or MDM console. The console pushes apps and
settings, and can lock a device to one app. This skill makes the app a good citizen on those devices.
It is vendor-neutral: steps for staging rugged devices are in `rugged-device-config`.

## 1. Know the device mode

Ask the user which mode the devices use, because it decides what the app may do:

| Mode | What it is | What it means for the app |
| --- | --- | --- |
| Work profile | A separate work space on a personal device | The app runs in the work profile, apart from personal apps. Do not assume it can see personal data |
| Fully managed | A company-owned device used only for work | IT controls apps and policies on the whole device |
| Dedicated device | A fully managed device that serves a specific purpose (a scanner, a kiosk, a checkout) | Often locked to one or a few apps (lock task mode) |

Write the mode under `## Distribution` in `docs/BRIEF.md`.

## 2. Accept settings from IT: managed configurations

A managed configuration lets any EMM set options in the app with no code on the EMM side. Declare the
options, read them, and react when they change.

1. Declare them in `app/src/main/res/xml/app_restrictions.xml`. The app can have one such file:

   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <restrictions xmlns:android="http://schemas.android.com/apk/res/android">
       <restriction
           android:key="server_url"
           android:title="@string/restriction_server_url"
           android:description="@string/restriction_server_url_desc"
           android:restrictionType="string"
           android:defaultValue="" />
       <restriction
           android:key="allow_manual_entry"
           android:title="@string/restriction_manual_entry"
           android:restrictionType="bool"
           android:defaultValue="true" />
   </restrictions>
   ```

   `android:restrictionType` is `bool`, `string`, `integer`, `choice`, `multi-select`, `hidden`, `bundle`, or
   `bundle_array`. Keys are literal strings, not resources. Titles and descriptions use string resources.

2. Point the manifest at it, inside `<application>`:

   ```xml
   <meta-data android:name="android.content.APP_RESTRICTIONS" android:resource="@xml/app_restrictions" />
   ```

3. Read it from `RestrictionsManager`, in a small class in `settings/` so tests can fake it:

   ```kotlin
   val manager = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
   val restrictions = manager.applicationRestrictions
   val serverUrl = if (restrictions.containsKey("server_url")) restrictions.getString("server_url") else ""
   ```

   The bundle holds only keys the admin set, so check `containsKey` and fall back to your default. Read it in
   `onResume` and cache it: the call reads storage.

4. React to changes. Register a receiver for `Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED` in code
   (`ContextCompat.registerReceiver` with `RECEIVER_NOT_EXPORTED`) while the app is in the foreground, and
   unregister it when paused. The system does not deliver it to a receiver declared in the manifest.

Check: a JVM test passes a `Bundle` into the class that reads restrictions and asserts the defaults and the
overrides. Then set the values in Test DPC (step 5) and watch the app change.

## 3. Kiosk: lock task mode

Lock task mode pins a device to an allowed set of apps and hides the system UI you choose. It is not screen
pinning, which a user can leave at will.

- IT (the device policy controller) allows packages with `DevicePolicyManager.setLockTaskPackages(...)` and
  picks what stays visible with `setLockTaskFeatures(...)` (for example `LOCK_TASK_FEATURE_HOME`,
  `LOCK_TASK_FEATURE_NOTIFICATIONS`). Your app cannot do this for itself.
- The app checks it is allowed with `DevicePolicyManager.isLockTaskPermitted(context.packageName)`, and
  may call `startLockTask()` on its own activity from `onResume`. Never start it while the device is locked.
  Declare `android:lockTaskMode="if_whitelisted"` on the activity if it should start in lock task mode when allowed.
- Test for it with `ActivityManager.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED`.
- In a kiosk, give people a way out for admins only (a hidden gesture plus a PIN), and make sure every
  screen has an in-app way back, because the system bars may be gone.

## 4. Distribute privately

- Apps for one company go out as a private app through Managed Google Play, or the MDM pushes the APK
  (see Play Console help for private apps, and `release` for building).
- A device policy controller can install APKs with `PackageInstaller` on fully managed devices. Get the app
  developer's permission first, and never use it to install Play apps.
- Keep the same `applicationId` and signing key across releases, so updates install over the old app.

## 5. Test without a fleet

Install Test DPC (Google's sample device policy controller, source at github.com/googlesamples/android-testdpc)
on a test device or emulator and set it up as a work profile or device owner. It lets you push managed
configuration values, allowlist lock task packages, and check the app under each mode. To make your own DPC the
owner of a device with no accounts on it (development only):

```bash
adb shell dpm set-device-owner com.example.dpc/.MyDeviceAdminReceiver
```

Done when the brief names the device mode, every option IT may set is in `app_restrictions.xml`, has a default
and a test, the app reacts when the values change, a kiosk run was checked with Test DPC or a real device, and
no setting needs a server unless the brief says so.

## Sources

- https://developer.android.com/work/overview
- https://developer.android.com/work/managed-configurations
- https://developer.android.com/work/dpc/dedicated-devices
- https://developer.android.com/work/dpc/dedicated-devices/lock-task-mode
- https://developer.android.com/work/dpc/dedicated-devices/cookbook
- https://github.com/googlesamples/android-testdpc
