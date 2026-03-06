package tech.zhifu.app.myhub.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.language.Language
import tech.zhifu.app.myhub.theme.AppTheme

/**
 * Preview 函数 - SettingsScreen 浅色主题
 */
@Preview
@Composable
private fun SettingsScreenLightPreview() {
    AppTheme(darkTheme = false) {
        var isDarkMode by remember { mutableStateOf(false) }
        var currentLanguage by remember { mutableStateOf(Language.English) }
        var showLanguageDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Appearance & Language",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                ThemeSettingItem(
                    isDarkMode = isDarkMode,
                    onThemeChanged = { isDarkMode = it }
                )

                LanguageSettingItem(
                    currentLanguage = currentLanguage,
                    onLanguageClick = { showLanguageDialog = true }
                )
            }
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { currentLanguage = it },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }
}

/**
 * Preview 函数 - SettingsScreen 深色主题
 */
@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    AppTheme(darkTheme = true) {
        var isDarkMode by remember { mutableStateOf(true) }
        var currentLanguage by remember { mutableStateOf(Language.SimplifiedChinese) }
        var showLanguageDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("设置") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "外观与语言",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                ThemeSettingItem(
                    isDarkMode = isDarkMode,
                    onThemeChanged = { isDarkMode = it }
                )

                LanguageSettingItem(
                    currentLanguage = currentLanguage,
                    onLanguageClick = { showLanguageDialog = true }
                )
            }
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguage = currentLanguage,
                onLanguageSelected = { currentLanguage = it },
                onDismiss = { showLanguageDialog = false }
            )
        }
    }
}

