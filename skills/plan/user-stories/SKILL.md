---
name: user-stories
description: Prioritized backlog of user stories with checkable acceptance criteria (docs/STORIES.md) for an Android app, built from a brief, research, or screen index. Use when app-brief or clone is done, or the user asks for user stories, a backlog, a feature list, or "what do we build first".
---

# User stories

Screens show what an app looks like. Stories say what a person does with
it. You build and test stories, not screens.

Read `docs/BRIEF.md`, `docs/research/*.md`, and `docs/screens/INDEX.md`
where they exist. Write `docs/STORIES.md`.

## Story format

```markdown
### S-012 Complete a task with a swipe
As the user, I swipe a task to the right to complete it.
Screens: 01-inbox.png, 02-today.png
Depends: S-004
Acceptance:
- [snap] The row shows a check and fades out within half a second.
- [self] An "Undo" Snackbar shows; tapping it brings the task back.
- [unit] A recurring task moves to its next date.
- [manual] Back from the detail screen returns to the list at the same place.
Status: todo
```

- One action per story. "Rename a project from its menu", not "Manage projects".
- Every acceptance line starts with how it is checked: `[snap]` snapshot in
  `shots/`, `[unit]` JVM unit test, `[self]` Compose self-test in
  `SelfTest.kt`, `[device]` an instrumented test on a device or emulator,
  `[manual]` a person must try it. "Works well" is not checkable.
- `Screens:` links reference screenshots, so `improve-loop` knows what to
  compare. Without a reference app, name the snapshot pages that will show
  the story. A dialog, bottom sheet, or menu needs its own snapshot state.
- `Depends:` (optional) lists stories that must be done first.
- Keep a control and the only surface that shows it in one story: a field
  that lives only in the add dialog ships with the dialog.
- Status: `todo`, `doing`, `done`, or `dropped`.

## Android acceptance lines

Add the ones that fit to every story that adds a screen:

- Back: system back and predictive back pop one screen, and the list keeps its place.
- Rotation: the screen survives rotation with its input and scroll position.
- Process death: text typed into a field survives the app being killed in the background.
- TalkBack: every icon-only control has a content description that says what it does.
- Font scale: the screen works at 1.3 times text size with no clipped text.
- Small and large: it works at 360 dp wide and on a tablet.

## Milestones

Each milestone ends with an app the user can use for a day.

1. **M1 Core loop**: the one thing the app is for, end to end. Data
   survives closing the app.
2. **M2 Organize**: the main structure, navigation pages, moving items.
3. **M3 Time**: dates, Today and Upcoming, recurrence, reminders.
4. **M4 Speed**: quick-add, search, swipe actions, keyboard support.
5. **M5 Android**: notifications, widgets, share, export and import, backup.
6. **M6 Polish**: empty states, icon, settings, tablet layout.

Rename milestones to fit the app. Inside a milestone, stories that others
depend on come first.

## Seed data

Seed data lives in `app/src/test/kotlin/<package path>/Seed.kt`, so it
never ships. Every attribute it sets (icons, colors, notes) is something a
story lets the user create in the app. Otherwise snapshots show features
no one can use.

## Leave things out

Drop what exists only for other people or for the vendor: accounts,
sharing, teams, integrations, upgrade prompts, gamification. List them
under `## Dropped`, one line each, so the decision is visible.

## UX notes

For each M1 story that adds a screen, an action, or words, call the Skill
tool with `ux-design`. It adds a `UX:` block under the acceptance lines and
starts `docs/GLOSSARY.md`, so every story uses the same names. Later
milestones get their UX notes in `improve-loop`, just before they are built.

## Size check

M1 has 5 to 10 stories. Over 60 stories in total means version one is too
big: move stories to `## Later`.

Done when every main job in the brief, every "Version one: yes" row in the
research matrix, and every screen in the index maps to a story or a
`Dropped` line, and every M1 story with UI has a `UX:` block.
