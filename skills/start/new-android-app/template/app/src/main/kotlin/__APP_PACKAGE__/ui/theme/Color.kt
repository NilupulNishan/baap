package __APP_PACKAGE__.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

private val Ink = Color(0xFF1B1B1F)
private val InkVariant = Color(0xFF44474F)
private val Paper = Color(0xFFFFFFFF)
private val NightPaper = Color(0xFF121316)
private val NightInk = Color(0xFFE3E2E6)
private val NightInkVariant = Color(0xFFC4C6D0)

/**
 * Builds a Material 3 color scheme from one seed color. Surfaces carry a faint
 * tint of the seed, so the accent shows up everywhere without shouting.
 * The "Use wallpaper colors" switch in Settings replaces this with the system scheme.
 */
fun accentColorScheme(seed: Color, dark: Boolean): ColorScheme =
    if (dark) darkScheme(seed) else lightScheme(seed)

private fun lightScheme(seed: Color): ColorScheme {
    val secondary = lerp(seed, Color(0xFF5F6368), 0.55f)
    val surface = lerp(Paper, seed, 0.02f)
    return lightColorScheme(
        primary = seed,
        onPrimary = Color.White,
        primaryContainer = lerp(seed, Paper, 0.82f),
        onPrimaryContainer = lerp(seed, Color.Black, 0.65f),
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = lerp(seed, Paper, 0.88f),
        onSecondaryContainer = lerp(seed, Color.Black, 0.7f),
        tertiary = secondary,
        onTertiary = Color.White,
        tertiaryContainer = lerp(seed, Paper, 0.88f),
        onTertiaryContainer = lerp(seed, Color.Black, 0.7f),
        background = surface,
        onBackground = Ink,
        surface = surface,
        onSurface = Ink,
        surfaceVariant = lerp(Paper, seed, 0.1f),
        onSurfaceVariant = InkVariant,
        surfaceTint = seed,
        outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0),
        surfaceContainerLowest = Paper,
        surfaceContainerLow = lerp(Paper, seed, 0.03f),
        surfaceContainer = lerp(Paper, seed, 0.05f),
        surfaceContainerHigh = lerp(Paper, seed, 0.08f),
        surfaceContainerHighest = lerp(Paper, seed, 0.11f),
    )
}

private fun darkScheme(seed: Color): ColorScheme {
    val primary = lerp(seed, Color.White, 0.45f)
    val secondary = lerp(primary, Color(0xFF9AA0A6), 0.55f)
    val surface = lerp(NightPaper, seed, 0.04f)
    return darkColorScheme(
        primary = primary,
        onPrimary = lerp(seed, Color.Black, 0.75f),
        primaryContainer = lerp(seed, Color.Black, 0.45f),
        onPrimaryContainer = lerp(seed, Color.White, 0.8f),
        secondary = secondary,
        onSecondary = lerp(seed, Color.Black, 0.8f),
        secondaryContainer = lerp(seed, Color.Black, 0.6f),
        onSecondaryContainer = lerp(seed, Color.White, 0.85f),
        tertiary = secondary,
        onTertiary = lerp(seed, Color.Black, 0.8f),
        tertiaryContainer = lerp(seed, Color.Black, 0.6f),
        onTertiaryContainer = lerp(seed, Color.White, 0.85f),
        background = surface,
        onBackground = NightInk,
        surface = surface,
        onSurface = NightInk,
        surfaceVariant = lerp(NightPaper, seed, 0.14f),
        onSurfaceVariant = NightInkVariant,
        surfaceTint = primary,
        outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF44474F),
        surfaceContainerLowest = lerp(NightPaper, Color.Black, 0.4f),
        surfaceContainerLow = lerp(NightPaper, seed, 0.06f),
        surfaceContainer = lerp(NightPaper, seed, 0.09f),
        surfaceContainerHigh = lerp(NightPaper, seed, 0.12f),
        surfaceContainerHighest = lerp(NightPaper, seed, 0.15f),
    )
}
