package __APP_PACKAGE__.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import __APP_PACKAGE__.R
import __APP_PACKAGE__.settings.SettingsState
import __APP_PACKAGE__.ui.theme.Accent
import __APP_PACKAGE__.ui.theme.spacing

@Composable
fun SettingsScreen(
    settings: SettingsState,
    onAccent: (Accent) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.l, vertical = spacing.s),
    ) {
        Text(
            stringResource(R.string.settings_appearance),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.gridProbe("settings/header"),
        )
        Spacer(Modifier.height(spacing.l))
        Text(
            stringResource(R.string.settings_accent),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.gridProbe("settings/accent"),
        )
        Spacer(Modifier.height(spacing.s))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(spacing.s),
        ) {
            Accent.entries.forEach { accent ->
                AccentSwatch(accent, selected = accent == settings.accent, onClick = { onAccent(accent) })
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(Modifier.height(spacing.l))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = settings.dynamicColor,
                        role = Role.Switch,
                        onValueChange = onDynamicColor,
                    )
                    .gridProbe("settings/dynamic"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_dynamic_color), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.settings_dynamic_color_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.dynamicColor, onCheckedChange = null, modifier = Modifier.padding(start = spacing.l))
            }
        }
        Spacer(Modifier.height(spacing.l))
        Text(
            stringResource(R.string.settings_theme_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AccentSwatch(accent: Accent, selected: Boolean, onClick: () -> Unit) {
    val description = stringResource(R.string.accent_description, stringResource(accent.label))
    Box(
        modifier = Modifier
            .size(MaterialTheme.spacing.touch)
            .clip(CircleShape)
            .background(accent.seed)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Color.White)
        }
    }
}
