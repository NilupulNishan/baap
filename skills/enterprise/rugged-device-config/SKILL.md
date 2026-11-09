---
name: rugged-device-config
description: Configure rugged Android devices for an app, splitting what the app does itself from what IT does with StageNow, MX, OEMConfig through an EMM, Enterprise Home Screen, and exported DataWedge profiles. Use when a rugged device needs staging, permissions granted without prompts, a kiosk home screen, a DataWedge profile deployed to a fleet, or the user mentions StageNow, MX, OEMConfig, or EHS.
---

# Rugged device configuration

Rugged devices carry settings that an ordinary Android app cannot change: scanner profiles, power rules,
which apps may run, the home screen. IT sets them. The app developer's job is to say what the app needs,
and to ship it in a form IT can deploy. The products named here come from the device maker and are trademarks of their owners.

## 1. Split the work: the app does it, or IT does it

| Need | Who | How |
| --- | --- | --- |
| Read a barcode | The app | DataWedge intents (`rugged-scanning`) |
| Create the app's own DataWedge profile | The app, on first run | `SET_CONFIG` from code |
| Deploy a tuned DataWedge profile to a fleet | IT | An exported profile file (step 3) |
| Settings for the app itself (server URL, options) | IT, through any EMM | Managed configurations (`managed-devices`) |
| Grant a runtime permission with no prompt | IT | MX `AccessMgr`, or an EMM (step 4) |
| Device settings (Wi-Fi, power, display, USB) | IT | StageNow, or OEMConfig through an EMM |
| Lock the device to a few apps | IT | Enterprise Home Screen or lock task mode (step 5) |
| Install the app | IT | An EMM, or StageNow |

Write the IT column as a one-page `docs/DEPLOYMENT.md` in the app: each setting, the value, and why.
It is the handover to the person who stages the devices.

## 2. The tools in one paragraph each

- **MX (MX Management System).** An XML-based framework with one common interface for managing the capabilities of
  the maker's Android devices. Each feature area is a CSP, with names like `AccessMgr`, `AppMgr`, `PowerMgr`, `UiMgr`,
  `UsbMgr`, `WirelessMgr`. The tools below send MX XML to the device. Check a feature's minimum MX version and Android
  version in the MX feature matrix before you rely on it.
- **StageNow.** The maker's tool, run on a Windows PC, that builds profiles (XML) for configuring its devices and
  delivers them, for example as barcodes the device scans. The maker says it can be downloaded for free.
  Good for a handful of devices, or the first setup before an EMM takes over.
- **OEMConfig.** The maker's way to set the same device settings through an EMM, using Android managed configurations. It
  is an Android app published by the maker plus a schema, and it is not pre-installed: the EMM installs it from Google Play
  and pushes transactions to it. Prefer it for fleets, since the EMM already manages everything else.
- **Enterprise Home Screen (EHS).** A free app from the maker that replaces the stock launcher so admins control which apps
  and settings people can reach, with no custom code. It reads an XML file.
- **EMDK Profile Manager.** A tool in the EMDK plugin for Android Studio that generates the code to read and set
  device features from an app. Use it only when the app itself must change device settings (rare).

## 3. Deploy a DataWedge profile

Build and test the profile on one device in the DataWedge app, then export it:

- Exports go to `/storage/emulated/0/Download/`: `datawedge.db` for the whole configuration, or
  `dwprofile_<profilename>.db` for one profile.
- IT can place a file in `/enterprise/device/settings/datawedge/autoimport`. DataWedge watches that folder
  and imports and activates whatever appears there. An EMM cannot write into the scoped-storage app folders, so
  The DataWedge docs point EMM tools at `/enterprise/device/settings/datawedge` or `/data/tmp/public`.
- Prefer the profile your app creates from code (`rugged-scanning`) for simple apps. Use an imported file when IT needs
  settings the code does not set: symbologies, decoder tuning, data formatting.

## 4. Permissions with no prompt

Dialogs slow a worker with a scanner in one hand. MX `AccessMgr` can pre-grant a runtime permission to an app: its
"Permission Access Action" takes `Allow`, `Deny`, `Allow User to choose`, `Verify`, or `Do Nothing`, with the package name,
the app signature, and the permission name. The MX feature matrix lists the minimum MX version, and a separate
parameter for all dangerous permissions on newer versions. Put each permission your app asks for into `docs/DEPLOYMENT.md`
so IT can allow them, and still ask in the app when IT has not (`android-integrations`).

## 5. Kiosk and home screen

Two routes, pick one:

- Lock task mode, when an EMM acts as the device policy controller (`managed-devices`).
- EHS, on supported devices without lock task: install `com.zebra.mdna.enterprisehomescreen` (EHS 5.0 and later), put its
  settings file at `/enterprise/usr/enterprisehomescreen.xml`, launch it, set it as the default home app, wait about 10 seconds,
  and reboot. Check the EHS setup page for the current steps before you write them into a handover.

## 6. Stage a test device

1. Install StageNow on a Windows PC (from the maker's support site; expect to sign in, and note the maker requires email
   multi-factor authentication for its single-sign-on sites from July 1, 2026).
2. Create a profile with the settings from `docs/DEPLOYMENT.md`, and stage the device.
3. Install the app, open it, and walk through the stories on the device.

The StageNow pages do not list the delivery methods for the current version in one place. Read its "about" page for the
supported ones.

Done when `docs/DEPLOYMENT.md` lists every device-level setting, permission, and file the app needs, says who does each, the
profile file (if any) was exported from a tested device, and a test device staged from that page runs the app.

## Sources

- https://techdocs.zebra.com/mx/overview
- https://techdocs.zebra.com/mx/
- https://techdocs.zebra.com/stagenow/latest/about/
- https://techdocs.zebra.com/oemconfig/15-3/about
- https://techdocs.zebra.com/oemconfig/15-3/setup
- https://techdocs.zebra.com/ehs/latest/guide/about/
- https://techdocs.zebra.com/ehs/8-0/guide/setup
- https://techdocs.zebra.com/datawedge/latest/guide/settings/
- https://techdocs.zebra.com/datawedge/latest/guide/admin/
- https://techdocs.zebra.com/emdk-for-android/15-1/guide/gettingstarted
- https://www.zebra.com/us/en/support-downloads/software/mobile-computer-software/stagenow.html
- https://www.zebra.com/mfa-sso
