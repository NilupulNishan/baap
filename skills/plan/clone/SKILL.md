---
name: clone
description: Walk through an existing Android app on a device or emulator, screenshot every screen, menu, and dialog, write a screen index, and turn it into user stories. Use when the user wants to clone, rebuild, replace, or copy the design of an app, or says "screenshot every screen".
argument-hint: "<name or package of the app>"
---

# Clone

Output: a folder of screenshots, one index that describes everything the
app shows, and a backlog built from it.

```text
docs/screens/
  INDEX.md
  01-inbox.png
  02-today.png
  ...
docs/screens/tree/   (optional view trees, 01-inbox.xml)
docs/STORIES.md      (from the user-stories skill)
```

## 1. Pick the tool

| You have | Use |
| --- | --- |
| A device or emulator with the app installed | `adb`, below |
| A web app as the reference | Claude in Chrome or the built-in browser, with the user's own sign-in |
| An iOS app, or no device | Ask the user for screenshots of each screen |

Check the device first: `adb devices` lists one entry with the state
`device`. Open the app on it.

## 2. Agree on privacy

Tell the user you will see everything in the app. Suggest a demo account or
sample data. Look, do not touch: open screens and menus, press Back to close
dialogs. Type, create, change, or delete only when the user says so.
Never type a password: the user signs in.

## 3. Save real files

Save each screen at full resolution to `docs/screens/`:

```bash
adb exec-out screencap -p > docs/screens/01-inbox.png
```

The view tree describes structure better than pixels do. Dump it and pull it:

```bash
adb shell uiautomator dump /sdcard/window_dump.xml
adb pull /sdcard/window_dump.xml docs/screens/tree/01-inbox.xml
```

Use one device size for the whole walk. For a tablet reference, repeat the
two or three main screens on a tablet or a large emulator.

## 4. Walk the app in this order

1. The default screen after sign-in.
2. Every bottom navigation or drawer destination, top to bottom.
3. The top bar overflow menu on each screen.
4. Every view mode (list, grid, calendar), sort menu, filter menu.
5. One item opened in detail: fields, sub-items, bottom sheets.
6. Creation flows: the FAB, quick-add, inline editors. Empty and half filled
   in, then Back.
7. Long press and swipe actions on an item.
8. The share sheet entry: what the app offers in Share and "Open with".
9. Settings: every screen.
10. Empty states.
11. Dark mode: `adb shell cmd uimode night yes`, then the two or three main
    screens again. Undo it with `adb shell cmd uimode night no`.
12. Large text: `adb shell settings put system font_scale 1.3`, then the main
    screens again. Undo it with `adb shell settings put system font_scale 1.0`.

Name files `NN-screen-or-state.png` in capture order. The walk is
exhaustive when every destination, menu, and settings screen has a file.

## Lessons from real walkthroughs

- Most actions hide in long press menus, swipe actions, and overflow menus.
  Press and hold rows, and swipe them.
- Settings and the help screen list features faster than tapping does. Read
  them and link them in the index.
- Note any change in the user's data during the walk, even if you are not
  sure you caused it, and tell them.
- Reset the text size and night mode when you finish.

## 5. Write docs/screens/INDEX.md

One section per screenshot:

```markdown
## 03-upcoming.png

Screen: Upcoming. Reached from: bottom navigation > Upcoming.
Layout: top bar with month picker; week strip; tasks grouped by day, each
day a section with a heading and an add button.
Elements: task row (checkbox, title, description line, date, tag, priority
color).
Actions: complete, open, long press to select, add to a day.
Gestures seen: swipe right to complete.
```

End with three lists:

- **Navigation map**: destinations and where each leads.
- **Command map**: every menu item, long press action, swipe, and shortcut.
- **Data model guess**: the nouns and fields you saw.

Describe structure and behavior. Take no text, logo, or icon from the app:
rebuilding for personal use is fine, copying a brand is not. Keep the PNGs
and trees out of public repos (add `docs/screens/` contents to `.gitignore`).

## 6. Turn it into stories

Call the Skill tool with `user-stories`. Every screen in the index ends up
linked from at least one story, or listed under `## Dropped`.

Done when INDEX.md has a section per PNG, the three maps exist, and
`docs/STORIES.md` covers every screen.
