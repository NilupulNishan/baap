---
name: design-system
description: Accent color and design system for an Android app, from the seed color and the accent picker in Settings to Kotlin theme tokens plus a design/DESIGN-SYSTEM.md that says which token to use when. Use when the user wants a brand or accent color, wants people to pick a color, when an app has more than a few screens, when screens drift apart or animations feel inconsistent, or when the user asks for themes, a style guide, or design tokens.
---

# Design system

A design system is a short list of **tokens** in code and one page that
says which token to use when. Every screen uses tokens, never raw values.
Then a change in one place changes the whole app, and an agent can review
a screen against one page.

Start from `material-design`. The template already has the base, all in
`app/src/main/kotlin/<package path>/ui/theme/`:

- `Accents.kt`: the `Accent` enum. Each entry is a seed color and a label.
- `Color.kt`: `accentColorScheme(seed, dark)` builds a full Material 3 color scheme from one seed.
- `Type.kt`, `Shape.kt`, `Motion.kt`: the type scale, corner shapes, and durations.
- `Spacing.kt`: gaps on the 4 dp grid, read as `MaterialTheme.spacing.l`.
- `Theme.kt`: `AppTheme(accent, dynamicColor, darkTheme)`, which picks the scheme.
- `settings/SettingsStore.kt` and `ui/SettingsScreen.kt`: Settings > Appearance, where people
  pick the accent and turn on "Use wallpaper colors". Light and dark follow the system.

## 1. Pick the accent

Read `## Look` in `docs/BRIEF.md`. With no color named, keep `Blue`.

For a brand color, add an entry to `Accent` with its seed and a string label. The seed becomes
`primary`; the container, surface, and "on" colors are derived from it.

Check the result, because a seed that is too light or too dull makes weak contrast:

- White (`onPrimary`) on the seed passes 4.5:1. If it does not (yellow, mint, cyan), pick a deeper
  seed, or set `onPrimary` to a dark color for that accent in `Color.kt`.
- Text in `onPrimaryContainer` on `primaryContainer` passes 4.5:1 in light and dark.

To compare colors, change `Accent.Blue` in the snapshot start state, or set the accent in
`SnapTest`, and run `./gradlew snap` for two or three seeds. Show the user the PNGs in `shots/`.
Keep the picker, so people can still choose their own color.

Check: each accent passes the contrast checks above in light and dark (read the PNGs and
use a contrast checker on the two colors).

## 2. Collect

New app with no screens yet? Skip this step: the template's tokens are the defaults.

Existing app: read every composable. List each raw value you find: `Color(0x...)`, `Color.White`,
alpha values, `dp` paddings, `RoundedCornerShape(n.dp)`, `sp` sizes, `tween(n)` durations. Group them.
Most apps have 3 to 5 of each where 1 or 2 are meant.

## 3. Write the tokens

All tokens live in `ui/theme/`, grouped by file:

| Group | Where | Tokens |
| --- | --- | --- |
| Spacing | `Spacing.kt` | `xs 4`, `s 8`, `m 12`, `l 16`, `xl 24`, `xxl 32`, `touch 48`, `fabClearance 88` |
| Shapes | `Shape.kt` | `small 8` rows and chips, `medium 12` cards, `large 16` sheets and dialogs |
| Type | `Type.kt` | the Material type scale, with the app's font family |
| Colors | `Color.kt`, `Accents.kt` | roles from the scheme; the accent seed |
| Motion | `Motion.kt` | `Short 150`, `Medium 250`, `Long 400` (milliseconds) |

Add each token when the app first needs it. A new token needs a name that says its job
(`rowHeight`), not its value (`gap16`).

## 4. Write design/DESIGN-SYSTEM.md

One section per group above. Each section says which token to use when, and the rules that go
with it. Keep it under 100 lines. Rules that transfer to most apps:

- Never a raw color, radius, gap, or duration in a screen. Use a token.
- Colors come from `MaterialTheme.colorScheme` roles. Text on a filled surface uses its
  `on` role, never a fixed white.
- Every role has a light and a dark value, because the scheme builds both.
- Use the standard components. Never rebuild a button or a switch from a `Box`: it loses focus,
  semantics, and TalkBack.
- Inner corners are concentric: inner radius = outer radius - inset.
- Every animation uses the motion tokens, so the "remove animations" setting applies everywhere.
- Lists that filter as people type do not animate. Speed wins.
- Never animate a screen's first frame.
- One primary action per screen or dialog.

## 5. Migrate

Replace raw values one screen per commit. After each, run `./gradlew snap`
(`build-and-verify` skill). The screen looks the same or better, never worse.

## 6. Point agents at it

Add one line to the app's `CLAUDE.md`: "Design rules: `design/DESIGN-SYSTEM.md`. Use its tokens;
never raw colors, shapes, gaps, or durations."

Done when this finds no hits outside `ui/theme/`:

```bash
grep -rnE 'Color\(0x|Color\.(White|Black|Red|Blue|Gray)|RoundedCornerShape\([0-9]|tween\([0-9]' app/src/main/kotlin | grep -v '/ui/theme/'
```

and `./gradlew grid` passes for the gaps, and DESIGN-SYSTEM.md has a section for every token group.
