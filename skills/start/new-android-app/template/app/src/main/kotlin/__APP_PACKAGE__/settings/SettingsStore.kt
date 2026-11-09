package __APP_PACKAGE__.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import __APP_PACKAGE__.ui.theme.Accent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SettingsState(
    val accent: Accent = Accent.Blue,
    val dynamicColor: Boolean = false,
)

/** Small preferences (accent, switches) live in DataStore. Records live in Room. */
class SettingsStore(private val dataStore: DataStore<Preferences>) {
    val state: Flow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            accent = Accent.fromKey(prefs[ACCENT]),
            dynamicColor = prefs[DYNAMIC_COLOR] ?: false,
        )
    }

    suspend fun setAccent(accent: Accent) {
        dataStore.edit { it[ACCENT] = accent.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    private companion object {
        val ACCENT = stringPreferencesKey("accent")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }
}
