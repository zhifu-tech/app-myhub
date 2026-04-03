package tech.zhifu.app.myhub.ui.state.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState

fun <VH> VH.createLanguageStateFlow(): StateFlow<Language>
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : LanguageState {
    return userPreferencesStateFlow
        .map { it.language.toLanguage() }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Language.ZH_CN
        )
}

fun <VH> VH.updateLanguage(language: Language)
    where VH : ViewModel,
          VH : UserPreferencesState,
          VH : LanguageState {
    val userId = userPreferencesStateFlow.value.userId
    viewModelScope.launch {
        userRepository.updateUserPreferencesLanguage(
            userId = userId,
            language = language.languageTag,
        )
    }
}
