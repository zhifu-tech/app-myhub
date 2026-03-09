package tech.zhifu.app.myhub.feature.settings

import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingState
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingState
import tech.zhifu.app.myhub.ui.State

sealed class SettingsUiState(
    val state: State,
) {
    object InitGlobalPending : SettingsUiState(
        state = State.initGlobalLoading(
            module = State.Module.SETTINGS,
        )
    )

    data class ResultOutputCompleted(
        val themeSettingState: ThemeSettingState,
        val languageSettingState: LanguageSettingState,
        val inlineMessage: String = ""
    ) : SettingsUiState(
        state = State.resultOutputCompleted(
            module = State.Module.SETTINGS,
        )
    )

    data class ResultErrorDisabled(
        val message: String = "",
        val canRetry: Boolean = true,
    ) : SettingsUiState(
        state = State.resultErrorDisabled(
            module = State.Module.SETTINGS,
        )
    )
}
