---
name: app-brief
description: One-page brief (docs/BRIEF.md) for a new Android app, written before any code. Use when the user has an Android app idea, wants to replace an app or habit, wants an app for company-managed or rugged devices, or the research and clone skills are done.
argument-hint: "[idea in a few words]"
---

# App brief

Write `docs/BRIEF.md` in a folder named after the app
(`<parent>/<app-name>/docs/BRIEF.md`; ask for the parent folder, default
the current folder). Every later skill reads it. Keep it to one page.

## 1. Name and read what exists

Pick the name first: a lowercase name for the folder (`tasks`, `daily-log`),
a display title (`Daily Log`), and a package name (`com.acme.dailylog`). The
package becomes the applicationId, which cannot change after the first Play
release, so ask for the real one if the user has it. The folder, the
research, and the scaffold all use these.

If `docs/research/` or `docs/screens/INDEX.md` exist, read them first.
Skip every question they already answer.

## 2. Interview

Ask a few questions at a time. If the user asked you to decide everything,
answer each question yourself from the research, write the choice in the
brief, and note "decided by the agent" next to it.

1. What do you do today, and which app or habit does it replace?
2. What are the three things you do most often? These become the main pages.
3. What data does it hold? Name the nouns: tasks, notes, recipes, invoices.
4. Is there a reference app you like? Then the `clone` skill comes next.
5. Which devices: phone, tablet, or both? Portrait, landscape, or both?
6. Where does the data live? Default: on the device in Room, no accounts, no
   server. A server only when the data must be shared or reach another device.
7. Will it run on company-managed or rugged devices (for example warehouse
   handhelds with a built-in scanner)? A yes adds the enterprise skills (step 5).
8. Which three things are out of scope for version one?
9. What should it look like? Note a brand or accent color, and any app they
   name. No answer means a clean Material 3 look with a blue accent people
   can change in Settings.

## 3. Cut scope

A personal app has one user. Remove what exists only for other users:
accounts, sharing, teams, sync servers, billing, onboarding, analytics. If
the user wants data on two devices, note "later: export and import, then
sync".

Version one is one afternoon of agent time: one shell, two to five main
pages, one data model with two to four types. Smart lists that filter one
model (Today, Upcoming, Someday) count as one page.

## 4. Write docs/BRIEF.md

```markdown
# <App Title>

One sentence: what it is and who it is for.

## Replaces
<app or habit>

## Main jobs
1. ...
2. ...
3. ...

## Data
- <Type>: <fields>

## Shape
<bottom navigation app / single screen tool / list and detail>, <phone / tablet / both>,
<portrait / landscape / both>. minSdk 26. Data on the device (Room). No accounts.

## Distribution
<Google Play, APK sideload, or company-managed through an MDM>. Package: <com.acme.app>

## Device hooks
<camera, barcode scanner, NFC, notifications, background work, widgets, share, files, or "none">

## Reference
<app name, or "none">

## Look
<Material 3 with an accent (name it), or "like <app>", and why>

## Not in version one
- ...
```

## 5. Hand off

If the app runs on company-managed or rugged devices, note it under
Distribution and Device hooks. Call the Skill tool with `managed-devices`
for the device modes, and with `rugged-scanning` if it scans barcodes. Then
create the app with `--scanner` (see `new-android-app`).

Call the Skill tool with `user-stories`, or with `new-android-app` if the
user wants to see the app running first.

Done when `docs/BRIEF.md` exists, fits on one page, and has every section.
