package __APP_PACKAGE__.ui.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import __APP_PACKAGE__.R

/** The accents people pick in Settings. Each is the seed of a full color scheme. */
enum class Accent(val seed: Color, @StringRes val label: Int) {
    Blue(Color(0xFF1F6FEB), R.string.accent_blue),
    Green(Color(0xFF1E8E3E), R.string.accent_green),
    Orange(Color(0xFFC2410C), R.string.accent_orange),
    Purple(Color(0xFF7C4DFF), R.string.accent_purple),
    Rose(Color(0xFFD6336C), R.string.accent_rose),
    Teal(Color(0xFF0F8B8D), R.string.accent_teal);

    companion object {
        fun fromKey(key: String?): Accent = entries.firstOrNull { it.name == key } ?: Blue
    }
}
