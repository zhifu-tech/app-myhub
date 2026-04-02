package tech.zhifu.app.myhub.feature.settings

import tech.zhifu.app.myhub.ui.state.theme.ThemeState

sealed class SettingsUiState(val state: State) {

//    object Idle : SettingsUiState(state = State.IDLE)

//    object Loading : SettingsUiState(state = State.LOADING)

    object Content : SettingsUiState(state = State.CONTENT)
//    (
//        val themeSettingState: ThemeSettingState,
//        val languageSettingState: LanguageSettingState,
//        val aiProviderSettingState: AiProviderSettingState,
//        val inlineMessage: String = ""
//    )
//
//    data class Error(
//        val message: String = "",
//        val canRetry: Boolean = true,
//    ) : SettingsUiState(state = State.ERROR)

    enum class State {
        CONTENT,
    }
}
