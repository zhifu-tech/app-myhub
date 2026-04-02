package tech.zhifu.app.myhub.ui.state.theme

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

fun ThemeState.initThemeStateFlow(): StateFlow<Theme> =
    userPreferencesStateFlow
        .map { prefs ->
            Theme.fromWire(prefs?.theme)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope(),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Theme.System
        )


fun ThemeState.updateTheme(
    theme: String,
) = viewModelScope().launch {
    userRepository
        .updateUserPreferencesTheme(
            userId = userStateFlow.value?.id.orEmpty(),
            theme = theme
        )
}
