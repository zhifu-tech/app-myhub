package tech.zhifu.app.myhub.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.settings.content.Error
import tech.zhifu.app.myhub.feature.settings.content.InlineMessage
import tech.zhifu.app.myhub.feature.settings.content.Content
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingItem
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingState
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingItem
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingState
import tech.zhifu.app.myhub.language.Language
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.PreviewPhoneLightDark

@PreviewPhoneLightDark
@Composable
fun SettingsScreen_Loading() {
    AppTheme {
        val uiState = SettingsUiState.Loading
        SettingsScreen(
            state = uiState.state,
            error = {},
            content = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun SettingsScreen_Error() {
    AppTheme {
        val uiState = SettingsUiState.Error(
            message = "未知错误，请稍后再试！",
            canRetry = true,
        )
        SettingsScreen(
            state = uiState.state,
            error = { modifier ->
                Error(
                    message = uiState.message,
                    canRetry = uiState.canRetry,
                    onRetry = {},
                    modifier = modifier.fillMaxSize()
                )
            },
            content = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun SettingsScreen_Content() {
    AppTheme {
        val uiState = SettingsUiState.Content(
            themeSettingState = ThemeSettingState(
                isDarkMode = true
            ),
            languageSettingState = LanguageSettingState(
                language = Language.SimplifiedChinese
            )
        )
        SettingsScreen(
            state = uiState.state,
            error = {},
            content = { modifier ->
                Content(
                    modifier = modifier,
                    themeSettingItem = {
                        ThemeSettingItem(
                            isDarkMode = uiState.themeSettingState.isDarkMode,
                            enabled = true,
                            onThemeChanged = {}
                        )
                    },
                    languageSettingsItem = {
                        LanguageSettingItem(
                            language = uiState.languageSettingState.language,
                            enabled = true,
                            onLanguageClick = {}
                        )
                    },
                    inlineMessage = {
                        InlineMessage(
                            message = "未知错误，请稍后再试！",
                            onClick = {},
                        )
                    }
                )
            },
        )
    }
}
