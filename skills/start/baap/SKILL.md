---
name: baap
description: Build any Android app. Start here to pick the next skill for a local-first Android app, including company-managed and rugged devices.
disable-model-invocation: true
---

# Build any Android app

Find where the user is on the path below, then call the Skill tool with the
skill for that step. One skill per call.

```text
idea -> research -> clone (optional) -> app-brief -> user-stories -> new-android-app
     -> material-design + design-system + app-icon -> improve-loop (ux, code, design, verify, commit) -> release -> repeat
```

Enterprise skills plug in at any step when the app runs on company-managed or rugged devices.

Pick the app's name first (app-brief, step 1) and create
`<parent>/<app-name>/docs/` before research, so every skill writes into the
same folder. Design comes right after the scaffold: tokens from the first
screen, and the real icon in every snapshot.

## Plan

| The user has | Skill |
| --- | --- |
| A vague idea, or asks "what should I build" | `research`, then `app-brief` |
| An app they want to replace or rebuild | `clone` |
| A brief or a screen index, no backlog | `user-stories` |

## Build

| The user wants to | Skill |
| --- | --- |
| Start a new project | `new-android-app` |
| Write or fix Kotlin and Compose code | `kotlin-compose` |
| Reach outside the app: notifications, background work, files, share, widgets, camera barcodes | `android-integrations` |
| Make it fast: slow start, janky scroll, big APK, memory | `performance` |
| Refactor, clean up, or check the code as it grows | `improve-architecture` |

## Design

| The user wants to | Skill |
| --- | --- |
| Decide how a feature works and what it says: names, flow, dialogs, accessibility | `ux-design` |
| Make it look and feel like a well made Material 3 app | `material-design` |
| An accent or brand color, a color picker, tokens, one set of shapes and motion | `design-system` |
| Fix spacing and alignment, or check the layout on the grid | `layout-grid` |
| A launcher icon, themed icon, or Play Store icon | `app-icon` |

## Ship

| The user wants to | Skill |
| --- | --- |
| Build it and look at every screen | `build-and-verify` |
| Tests, a self-test, accessibility checks, or device tests | `test-android-app` |
| Work through the backlog on its own | `improve-loop` |
| Sign it and put it on Google Play, an APK, or company devices | `release` |

## Enterprise (optional)

| The user says | Skill |
| --- | --- |
| The app runs on company-managed devices, needs IT settings, or runs as a kiosk | `managed-devices` |
| Scan barcodes with a rugged device's built-in scanner, DataWedge, or EMDK | `rugged-scanning` |
| Stage rugged devices, StageNow, OEMConfig, MX, or a home screen for a fleet | `rugged-device-config` |
| Print labels to a label printer, ZPL, or Link-OS | `label-printing` |
| Read RFID tags | `rfid-reading` |

Create the app with `--scanner` (see `new-android-app`) when it scans with a built-in scanner.

## Done when

You named the step, called its skill, and told the user which skill comes
after it.
