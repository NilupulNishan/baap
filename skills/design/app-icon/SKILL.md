---
name: app-icon
description: App icon for an Android app, drawn as adaptive icon layers (foreground, background, monochrome) and rendered to PNGs for review and for Google Play, then checked at launcher size and in themed-icon mode. Use when the user wants an app icon, a launcher icon, a better icon, a splash icon, or a Play Store icon.
allowed-tools: Bash(./gradlew snap) Bash(./gradlew.bat snap) Read Edit Write
---

# App icon

The icon is three vector layers in `app/src/main/res/`, and a test renders them
to PNGs you can look at:

| File | Used by |
| --- | --- |
| `drawable/ic_launcher_foreground.xml` | The symbol, on a 108 dp canvas. Also the splash screen icon |
| `values/themes.xml` (`ic_launcher_background`) | The background color of the icon and the splash screen |
| `drawable/ic_launcher_monochrome.xml` | The one-color symbol that Android 13 themed icons tint |
| `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml` | The adaptive icon: background, foreground, monochrome |
| `shots/icon-512.png` | Rendered by `IconTest`. Upload this to Google Play as the store icon |

`./gradlew snap` runs `IconTest`, which writes `shots/icon-512.png`, `shots/icon-48.png` (launcher
size), and `shots/icon-themed.png` (the monochrome layer on white).

## 1. Pick the idea

Read `docs/BRIEF.md`. The icon shows one object or symbol for the app's main job: a check for
tasks, a page for notes, a pot for recipes. Write three ideas, pick one, and say why in one line.

- One symbol, no words, no more than two or three shapes.
- It reads at 48 px on a launcher. Test this first: a detail that vanishes at 48 px goes.
- Its own idea. No logo or icon from another app, and no vendor logo.

## 2. Draw the layers

Android crops every adaptive icon with a mask (a circle, squircle, or rounded square, chosen by
the phone), so the layers follow fixed rules:

- Canvas: 108 by 108 dp. Only the middle 72 by 72 dp always shows. Keep the symbol inside the
  66 dp diameter circle at the center (the safe zone), so no mask cuts it.
- **Background**: a solid color or a simple shape, full bleed. It is a color resource here.
- **Foreground**: the symbol as a vector with `android:viewportWidth="108"` and
  `android:viewportHeight="108"`, centered, in simple fills. No text, no lines thinner than 4 units.
- **Monochrome**: the same symbol in one color (any opaque color) and no background, same canvas
  and safe zone. Android 13 and later tint it with the wallpaper colors for themed icons.

Edit the three files. A path in `pathData` is in the 108 by 108 coordinate space. Material Symbols
(Apache 2.0) are a good source for the symbol.

## 3. Splash screen

The template's splash theme (`Theme.App.Starting`, from `core-splashscreen`) uses the foreground layer as
`windowSplashScreenAnimatedIcon` on the icon's background color. An icon with no background of its own
is 288 by 288 dp and must fit a circle 192 dp across. If the symbol is cut off on the splash, scale the
symbol down in the foreground layer.

## 4. Render and review

```bash
./gradlew snap       # Windows cmd and PowerShell: .\gradlew.bat snap
```

Open these with the Read tool:

1. `shots/icon-512.png`: the full icon.
2. `shots/icon-48.png`: still readable at launcher size.
3. `shots/icon-themed.png`: the symbol is a clean single shape, with nothing in it that relies on a second color.

Check: the symbol is clear at 48 px, it sits inside the center 66 dp circle (the bleed edge shows none of
it), and the themed shape is recognizable. Fix the vectors and render again until all three hold.

## 5. Store icon

Google Play asks for a 512 by 512 px PNG. Upload `shots/icon-512.png`: it is the full-bleed square, so
Play applies its own rounding. Do not add rounded corners or a shadow yourself.

Done when the three layers and both mipmap files exist, `shots/icon-512.png`, `icon-48.png`, and
`icon-themed.png` look right, the splash shows the symbol uncut, and the icon change is committed.
