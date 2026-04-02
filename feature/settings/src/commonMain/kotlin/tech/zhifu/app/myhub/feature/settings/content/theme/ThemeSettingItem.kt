package tech.zhifu.app.myhub.feature.settings.content.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_dark_mode
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_off
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_on
import tech.zhifu.app.myhub.ui.state.theme.collectDarkThemeState
import tech.zhifu.app.myhub.ui.state.theme.updateTheme

@Composable
fun ThemeSettingItemRoute(
    viewModel: SettingsViewModel
) {
    val isDarkMode by viewModel.collectDarkThemeState()
    ThemeSettingItem(
        isDarkMode = isDarkMode,
        enabled = true,
        onThemeChanged = { isDarkMode ->
            viewModel
                .updateTheme(
                    theme = if (isDarkMode) "dark" else "light",
                )
        }
    )
}

@Composable
internal fun ThemeSettingItem(
    isDarkMode: Boolean,
    enabled: Boolean = true,
    onThemeChanged: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.feature_settings_dark_mode)) },
            supportingContent = {
                Text(
                    if (isDarkMode)
                        stringResource(Res.string.feature_settings_on)
                    else
                        stringResource(Res.string.feature_settings_off)
                )
            },
            leadingContent = { Icon(Icons.Default.Palette, null) },
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    enabled = enabled,
                    onCheckedChange = onThemeChanged
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
