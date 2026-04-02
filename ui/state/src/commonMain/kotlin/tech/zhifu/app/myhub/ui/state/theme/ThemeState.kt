package tech.zhifu.app.myhub.ui.state.theme

import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

interface ThemeState : UserPreferencesState {
    val themeStateFlow: StateFlow<Theme>
}

enum class Theme(val value: String) {
    Light("light"),
    Dark("dark"),
    System("system");

    companion object {
        fun fromWire(value: String?): Theme =
            entries.firstOrNull { it.value == value?.trim()?.lowercase() }
                ?: System
    }
}
