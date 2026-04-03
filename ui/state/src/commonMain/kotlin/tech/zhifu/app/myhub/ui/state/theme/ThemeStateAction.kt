package tech.zhifu.app.myhub.ui.state.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

fun <VH> VH.createThemeStateFlow(): StateFlow<Theme>
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : ThemeState {
    return userPreferencesStateFlow
        .map { prefs -> Theme.fromWire(prefs.theme) ?: Theme.System }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Theme.System
        )
}

fun <VH> VH.updateTheme(theme: String)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : ThemeState {
    val targetTheme = Theme.fromWire(theme) ?: return
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository
            .updateUserPreferencesTheme(
                userId = userId,
                theme = targetTheme.value
            )
    }
}
