package tech.zhifu.app.myhub.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import tech.zhifu.app.myhub.feature.settings.content.InlineMessage
import tech.zhifu.app.myhub.feature.settings.content.ResultOutputCompleted
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingItem
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingState
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingItem
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingState
import tech.zhifu.app.myhub.language.Language
import tech.zhifu.app.myhub.theme.AppTheme
import tech.zhifu.app.myhub.ui.PreviewPhoneLightDark
import tech.zhifu.app.myhub.ui.content.InitGlobalPending
import tech.zhifu.app.myhub.ui.content.ResultErrorDisabled

@PreviewPhoneLightDark
@Composable
fun SettingsScreen_InitGlobalPending() {
    AppTheme {
        val uiState = SettingsUiState.InitGlobalPending
        SettingsScreen(
            state = uiState.state,
            initGlobalPending = { modifier ->
                InitGlobalPending(
                    modifier = modifier.fillMaxSize()
                )
            },
            resultErrorDisabled = {},
            resultOutputCompleted = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun SettingsScreen_ResultErrorDisabled() {
    AppTheme {
        val uiState = SettingsUiState.ResultErrorDisabled(
            message = "未知错误，请稍后再试！",
            canRetry = true,
        )
        SettingsScreen(
            state = uiState.state,
            initGlobalPending = {},
            resultErrorDisabled = { modifier ->
                ResultErrorDisabled(
                    message = uiState.message,
                    canRetry = uiState.canRetry,
                    onRetry = {},
                    modifier = modifier.fillMaxSize()
                )
            },
            resultOutputCompleted = {},
        )
    }
}

@PreviewPhoneLightDark
@Composable
fun SettingsScreen_ResultOutputCompleted() {
    AppTheme {
        val uiState = SettingsUiState.ResultOutputCompleted(
            themeSettingState = ThemeSettingState(
                isDarkMode = true
            ),
            languageSettingState = LanguageSettingState(
                language = Language.SimplifiedChinese
            )
        )
        SettingsScreen(
            state = uiState.state,
            initGlobalPending = {},
            resultErrorDisabled = {},
            resultOutputCompleted = { modifier ->
                ResultOutputCompleted(
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
