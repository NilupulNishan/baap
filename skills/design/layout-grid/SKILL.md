---
name: layout-grid
description: Grid and alignment rules for an Android app, with three checks that catch misalignment (a lint for off-grid dp values, a layout probe that measures real boxes in the Robolectric tests, and a grid overlay). Use when building or reviewing a custom screen, card, row, form, or dialog, after every change that touched layout, or when the user says something looks misaligned, crooked, cramped, or unevenly spaced.
---

# Layout grid

Agents guess numbers. A guessed `padding(13.dp)` looks almost right and
makes the whole app feel slightly off. This skill replaces guessing with
one scale and checks that fail when something is off.

The template has it in place: the spacing tokens in `ui/theme/Spacing.kt`,
`GridLintTest.kt` and `LayoutProbeTest.kt` in `app/src/test/`, and
`Modifier.gridProbe(...)` in `ui/Route.kt`. `./gradlew grid` runs both checks.
For an app made before this skill, copy those files from the `new-android-app` skill's template.

## The rules

1. Every padding, gap, and size comes from `MaterialTheme.spacing`: `xs 4`,
   `s 8`, `m 12`, `l 16`, `xl 24`, `xxl 32`, `touch 48`. Raw `dp` numbers are 1, 2,
   or a multiple of 4.
2. Screens keep a 16 dp margin (`l`). Cards pad 16. Related controls sit 8 apart,
   groups 16 to 24, sections 32.
3. Each column has one leading edge. Titles, text, cards, and buttons in a column
   start on the same x. Indent on purpose, by a step of the scale.
4. Let layout do the aligning:
   - Stacks: `Column(verticalArrangement = Arrangement.spacedBy(spacing.s))`. Use
     `spacedBy`, not padding on each child.
   - Rows with a label and a value or switch: `Row` with `Modifier.weight(1f)` on the text.
   - Text of different sizes in one row: `verticalAlignment = Alignment.Bottom` with
     `Modifier.alignByBaseline()`.
   - Numbers in a column: tabular figures (`fontFeatureSettings = "tnum"`), aligned to the end.
5. Dialogs and sheets take the Material 3 defaults. Controls take their height from
   padding and the type scale, with `minimumInteractiveComponentSize` for touch (48 dp).
6. Nested corners are concentric: inner radius = outer radius - padding.
7. `Modifier.offset`, `Modifier.absoluteOffset`, `graphicsLayer { translationX }`, and `Box`
   overlays move a node out of the flow, so nothing lines up with it. Use them only for
   overlays, motion, and drag previews, with `// grid-ok <why>` on the line.
8. Long text uses `maxLines = 1` and `overflow = TextOverflow.Ellipsis`, with the full
   value on the detail screen. Nothing clips.

## Check 1: lint the code

`GridLintTest` scans `src/main/kotlin` for `N.dp` and fails on any `N` that is not 1, 2,
or a multiple of 4. Run it after every change that touched layout:

```bash
./gradlew grid
# FAIL .../ItemsScreen.kt:52: off grid (10.dp): .padding(horizontal = 10.dp)
```

Fix each line with a token. A line that must keep another number gets `// grid-ok` and the reason.

## Check 2: probe the real layout

`LayoutProbeTest` measures real boxes. Mark what you lay out yourself with
`Modifier.gridProbe("area/name")`, which sets the test tag `grid-probe:area/name`:

```kotlin
Text(title, modifier = Modifier.gridProbe("settings/header"))
Row(modifier = Modifier.gridProbe("items/row")) { ... }
```

The test opens each page, finds every probe, and checks that each:

1. Starts on the 4 dp grid (x and y, within half a dp).
2. Shares one leading edge with the other probes in its area (the part before the `/`).

Add a probe for each new custom screen, card, and row, and add a test case that opens the
screen and calls `check(...)` with its area name. The output reads:

```text
PASS all-layout: 12 probes
FAIL settings-layout: edge settings has 2 leading edges: header 16.0, accent 20.0
```

## Check 3: look at the grid

When a check fails and the cause is not clear, draw the grid over the screen in a snapshot.
Add this helper for one run (not committed), and apply it to the root `Surface` in `AppRoot.kt`:

```kotlin
fun Modifier.gridOverlay(step: Dp = 8.dp): Modifier = drawWithContent {
    drawContent()
    val stepPx = step.toPx()
    val line = Color(0x26FF0080)
    var x = 0f
    while (x < size.width) { drawLine(line, Offset(x, 0f), Offset(x, size.height)); x += stepPx }
    var y = 0f
    while (y < size.height) { drawLine(line, Offset(0f, y), Offset(size.width, y)); y += stepPx }
}
```

It needs `androidx.compose.ui.draw.drawWithContent`, `Color`, `Offset`, and `Dp`. Run
`./gradlew snap`, open the PNG with the Read tool, then delete the helper and the modifier.

## Fix a failure

| Message | Usual cause | Fix |
| --- | --- | --- |
| starts at x=23.0 dp, off the grid | a raw padding on one child | remove it, use the parent's `spacedBy` and padding |
| has 2 leading edges | a centered column, or one child with extra padding | `horizontalAlignment = Alignment.Start`, remove the extra padding |
| off grid (10.dp) | a guessed number | a `MaterialTheme.spacing` token |
| no probes found for area | the screen did not open, or the tag is missing | check the test opens the page; add `gridProbe` |
| edges differ by 4 dp | text inside a row is inset by an icon on one row only | give the leading slot a fixed width on every row |

Done when `./gradlew grid` passes, and every custom screen and card you touched has probes on
its main parts and a case in `LayoutProbeTest`.
