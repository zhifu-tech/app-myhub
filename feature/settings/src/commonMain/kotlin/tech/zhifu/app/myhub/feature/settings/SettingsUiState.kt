package tech.zhifu.app.myhub.feature.settings

sealed class SettingsUiState(val state: State) {
    object Content : SettingsUiState(state = State.CONTENT)
    enum class State {
        CONTENT,
    }
}
