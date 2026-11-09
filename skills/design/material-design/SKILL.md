---
name: material-design
description: Design rules that make an Android app look and feel like a well made Material 3 app, covering principles, layouts, navigation, top bars, type, color roles, dark theme, edge-to-edge, motion, large screens, and a review checklist. Use when building or reviewing any screen, list, top bar, bottom bar, dialog, or settings screen, or when the user asks for a cleaner, more polished, or better designed Android app.
---

# Material design

An app is a tool people return to every day, not a page they read once.
Design for the tenth visit: dense enough to work in, calm enough to look
at all day, and quick with one hand.

- App-wide tokens (colors, shapes, motion): call the Skill tool with
  `design-system`.
- How a feature works and what it says (names, flow, dialogs, errors,
  accessibility): call the Skill tool with `ux-design`.
- Spacing, alignment, and the checks that catch misalignment: call the
  Skill tool with `layout-grid`.

## Principles

Use them as the review lens for every screen:

1. **Purpose**: each feature costs people time and attention. Cut what does not earn it.
2. **Content first**: the UI frames the user's data. Chrome is quiet; content has the contrast.
3. **Familiar**: things that look the same work the same. Standard components, standard icons, standard gestures.
4. **Forgiving**: Undo over confirmations. Nothing is lost by a wrong tap.
5. **Fast**: the result of every action shows in the same frame.
6. **Reachable**: the main action sits within thumb reach. Back works everywhere.
7. **Craft**: aligned edges, consistent spacing, layouts that survive resizing and large text.

Every screen answers: Where am I? What can I do? Where can I go?

## Before you write screens

Write these under `## Design plan` in `docs/BRIEF.md`, or in
`docs/DESIGN.md` when the brief would pass one page:

1. **Archetype**: bottom navigation app (a few top-level destinations),
   list and detail, single screen tool (one canvas or form), or feed.
   Most personal apps are a bottom navigation app with list and detail.
2. **Information architecture**: top-level destinations (three to five), at most two levels.
3. **Command map**: every action, where it lives (FAB, top bar, overflow, long press, swipe).
4. **Empty states**: what each screen shows before there is data.
5. **Feeling**: the one feeling the app aims for (calm, quick, confident).
   Motion, words, and color all reinforce it.

## Layout and window size

Design for the window, not the device. Read the width class with
`currentWindowAdaptiveInfo().windowSizeClass` (from `material3-adaptive`):

| Width | Class | Navigation | Layout |
| --- | --- | --- | --- |
| Under 600 dp | Compact | Bottom bar | One pane; detail replaces the list |
| 600 to 839 dp | Medium | Rail | List and detail side by side when it fits |
| 840 dp and wider | Expanded | Rail or drawer | List and detail, or a supporting pane |

- `NavigationSuiteScaffold` (the template's `AppShell`) already switches between bar and rail.
- Canonical layouts: **list-detail** (`ListDetailPaneScaffold`), **supporting
  pane** (`SupportingPaneScaffold`), and **feed**. Use them instead of hand-rolled split screens.
- Text columns stop at about 600 dp wide. Wider screens get more margin, not longer lines.
- Phone landscape and foldables count: test them with the `-tablet` snapshots and an emulator that resizes.
- One primary action per screen: the FAB, or a filled button in a dialog.

## Top app bars and the FAB

- Small top app bar for most screens; medium or large when a title needs room, with
  `TopAppBarDefaults.exitUntilCollapsedScrollBehavior()`.
- Back arrow only on screens that are not top-level. Title names the screen.
- Actions in the bar: at most two icons, the rest in the overflow menu.
- The FAB is for the one main creation action. Use an extended FAB when the verb needs words.
  Hide or move it on screens where it does not apply (the template hides it on Settings).

## Type

- Use the type scale through `MaterialTheme.typography` (display, headline, title, body, label, each in
  large, medium, small), never raw `sp` values in screens. Tokens live in `ui/theme/Type.kt`.
- Body medium for text, label for buttons and chips, title for list row titles. Weight and color
  make hierarchy before size does.
- Text is in `sp` so it scales. Check 1.3 times font scale.
- Sentence case for labels and text, title case for buttons and titles.

## Color roles

- Colors come from `MaterialTheme.colorScheme` roles, never a hex in a screen:
  `primary` and `onPrimary` for the main action, `primaryContainer` for selection and the FAB,
  `secondaryContainer` for the selected nav item, `surface` and `surfaceContainer*` for panels,
  `onSurface` and `onSurfaceVariant` for text in two levels, `outline` for borders, `error` for errors.
- One accent. It marks selection, the primary action, and focus. Not headings or borders.
- Dynamic color (wallpaper colors, Android 12 and later) is a user switch in Settings, off by default,
  so the brand accent shows.
- Text never lighter than 4.5:1 against its background. Meaning never rests on color alone.

## Dark theme

- Every role has a light and a dark value from the scheme. Check every snapshot in both.
- Dark surfaces are dark gray, not black. Higher surfaces (`surfaceContainerHigh`) are lighter.
- Never a fixed white or black in a component.
- Light and dark follow the system (`isSystemInDarkTheme()`).

## Edge-to-edge and insets

- Call `enableEdgeToEdge()` in `MainActivity` before `setContent`.
- `Scaffold` passes `innerPadding`: apply it to the content. Use `WindowInsets.safeDrawing`
  for custom layouts and `Modifier.imePadding()` for forms the keyboard covers.
- Scrolling lists draw behind the bars, with `contentPadding = innerPadding`.
- Check the notch, the gesture bar, and the keyboard on a real device or emulator.

## Motion

- Motion explains a change: where a row went, where a screen came from. Never decoration.
- Durations and easing come from `ui/theme/Motion.kt`. Short for small changes (about 100 to 200 ms),
  medium for screens (about 300 ms). Nothing slower in everyday use.
- Lists that filter as people type do not animate.
- Respect the system "remove animations" setting.
- Predictive back is on (`android:enableOnBackInvokedCallback="true"`): the back gesture shows the
  screen underneath. Navigation 3's `NavDisplay` handles it for pushed screens.

## Components

- Standard Material 3 components first: `Button`, `OutlinedTextField`, `Switch`, `AlertDialog`,
  `ModalBottomSheet`, `NavigationSuiteScaffold`. They bring focus, semantics, and TalkBack for free.
- Icons are one set (Material Symbols), one weight, 24 dp. Every icon-only button has a content description.
- Ripples and focus indicators stay on. Do not remove them.

## Review checklist

On every snapshot, check in this order:

1. Where am I, what can I do, where can I go: each answered at a glance.
2. Nothing clipped, overlapping, or blank. Long titles truncate to one line.
3. One accent, used for selection, the primary action, and focus only.
4. Both modes readable. No fixed white or black.
5. Edges line up (`layout-grid` checks pass).
6. Empty states invite the first action.
7. Phone and tablet snapshots both make sense: bar on the phone, rail on the tablet.

Done when the design plan exists, and every snapshot of the screens you
touched passes the review checklist in light and dark, on phone and tablet.
