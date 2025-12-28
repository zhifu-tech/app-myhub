package tech.zhifu.app.myhub.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_appearance_language
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_close
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_dark_mode
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_display_language
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_off
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_on
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_select_language
import tech.zhifu.app.myhub.platform.compose.resources.settings
import tech.zhifu.app.myhub.ui.utils.Language
import tech.zhifu.app.myhub.platform.compose.resources.Res as PlatformRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinInject<SettingsViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(PlatformRes.string.settings)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.feature_settings_appearance_language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 主题设置项
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.feature_settings_dark_mode)) },
                    supportingContent = {
                        Text(
                            if (uiState.isDarkMode)
                                stringResource(Res.string.feature_settings_on)
                            else
                                stringResource(Res.string.feature_settings_off)
                        )
                    },
                    leadingContent = { Icon(Icons.Default.Palette, null) },
                    trailingContent = {
                        Switch(
                            checked = uiState.isDarkMode,
                            onCheckedChange = { viewModel.updateTheme(it) }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // 语言设置项
            Surface(
                onClick = { viewModel.showLanguageDialog() },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.feature_settings_display_language)) },
                    supportingContent = { Text(uiState.currentLanguage.label) },
                    leadingContent = { Icon(Icons.Default.Language, null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }

    if (uiState.showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLanguageDialog() },
            title = { Text(stringResource(Res.string.feature_settings_select_language)) },
            text = {
                Column {
                    Language.entries.forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (language == uiState.currentLanguage),
                                onClick = {
                                    viewModel.updateLanguage(language)
                                }
                            )
                            Text(
                                text = language.label,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideLanguageDialog() }) {
                    Text(stringResource(Res.string.feature_settings_close))
                }
            }
        )
    }
}

