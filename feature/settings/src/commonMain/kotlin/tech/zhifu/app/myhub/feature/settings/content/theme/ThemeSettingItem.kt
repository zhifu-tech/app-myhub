package tech.zhifu.app.myhub.feature.settings.content.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_theme_mode
import tech.zhifu.app.myhub.ui.state.theme.Theme
import tech.zhifu.app.myhub.ui.state.theme.updateTheme

@Composable
fun ThemeSettingItem(
    viewModel: SettingsViewModel
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val showDialog = remember { mutableStateOf(false) }

    ThemeSettingItemContent(
        theme = theme,
        onClick = {
            showDialog.value = true
        }
    )

    ThemeSelectionDialog(
        visible = showDialog.value,
        selectedTheme = theme,
        onSelected = { selected ->
            viewModel.updateTheme(theme = selected.value)
            showDialog.value = false
        },
        onDismiss = {
            showDialog.value = false
        }
    )
}

@Composable
internal fun ThemeSettingItemContent(
    theme: Theme,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        ListItem(
            headlineContent = {
                Text(stringResource(Res.string.feature_settings_theme_mode))
            },
            supportingContent = {
                Text(text = stringResource(theme.labelToken()))
            },
            leadingContent = { Icon(Icons.Default.Palette, null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
