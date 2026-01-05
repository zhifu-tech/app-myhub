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
import tech.zhifu.app.myhub.language.Language
import tech.zhifu.app.myhub.language.getLocalizedLabel
import tech.zhifu.app.myhub.platform.resources.settings
import tech.zhifu.app.myhub.platform.resources.Res as PlatformRes

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

            // 主题设置
            ThemeSettingItem(
                isDarkMode = uiState.isDarkMode,
                onThemeChanged = { viewModel.updateTheme(it) }
            )

            // 语言设置
            LanguageSettingItem(
                currentLanguage = uiState.currentLanguage,
                onLanguageClick = { viewModel.showLanguageDialog() }
            )
        }
    }

    // 语言选择对话框
    if (uiState.showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = uiState.currentLanguage,
            onLanguageSelected = { viewModel.updateLanguage(it) },
            onDismiss = { viewModel.hideLanguageDialog() }
        )
    }
}

/**
 * 主题设置项
 */
@Composable
fun ThemeSettingItem(
    isDarkMode: Boolean,
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
                    onCheckedChange = onThemeChanged
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

/**
 * 语言设置项
 */
@Composable
internal fun LanguageSettingItem(
    currentLanguage: Language,
    onLanguageClick: () -> Unit
) {
    Surface(
        onClick = onLanguageClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.feature_settings_display_language)) },
            supportingContent = { Text(currentLanguage.getLocalizedLabel()) },
            leadingContent = { Icon(Icons.Default.Language, null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

/**
 * 语言选择对话框
 */
@Composable
internal fun LanguageSelectionDialog(
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                            selected = (language == currentLanguage),
                            onClick = { onLanguageSelected(language) }
                        )
                        Text(
                            text = language.getLocalizedLabel(),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_settings_close))
            }
        }
    )
}

