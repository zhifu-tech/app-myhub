package tech.zhifu.app.myhub.feature.settings.content.language

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
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
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_display_language
import tech.zhifu.app.myhub.ui.state.language.Language
import tech.zhifu.app.myhub.ui.state.language.collectLanguage
import tech.zhifu.app.myhub.ui.state.language.updateLanguage

@Composable
fun LanguageSettingItem(
    viewModel: SettingsViewModel,
) {
    val language by viewModel.collectLanguage()
    val showDialog = remember { mutableStateOf(false) }

    LanguageSettingItemContent(
        language = language,
        onClick = {
            showDialog.value = true
        }
    )

    LanguageSelectionDialog(
        visible = showDialog.value,
        language = language,
        onSelected = { selected: Language ->
            viewModel.updateLanguage(selected)
            showDialog.value = false
        },
        onDismiss = {
            showDialog.value = false
        },
    )
}

@Composable
internal fun LanguageSettingItemContent(
    language: Language,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        ListItem(
            headlineContent = {
                Text(text = stringResource(Res.string.feature_settings_display_language))
            },
            supportingContent = {
                Text(text = stringResource(language.labelToken()))
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}
