package tech.zhifu.app.myhub.feature.settings

import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingState
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingState

enum class State {
    LOADING, CONTENT, ERROR
}

sealed class SettingsUiState(val state: State) {
    object Loading : SettingsUiState(state = State.LOADING)

    data class Content(
        val themeSettingState: ThemeSettingState,
        val languageSettingState: LanguageSettingState,
        val inlineMessage: String = ""
    ) : SettingsUiState(state = State.CONTENT)

    data class Error(
        val message: String = "",
        val canRetry: Boolean = true,
    ) : SettingsUiState(state = State.ERROR)
}
