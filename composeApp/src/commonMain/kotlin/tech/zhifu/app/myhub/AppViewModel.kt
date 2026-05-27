package tech.zhifu.app.myhub

import androidx.lifecycle.ViewModel
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.state.language.LanguageState
import tech.zhifu.app.myhub.ui.state.language.createLanguageStateFlow
import tech.zhifu.app.myhub.ui.state.theme.ThemeState
import tech.zhifu.app.myhub.ui.state.theme.createThemeStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.createUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.createUserPreferencesStatFlow

class AppViewModel(
    override val userRepository: UserRepository,
) : ViewModel(),
    UserState,
    UserPreferencesState,
    ThemeState,
    LanguageState {
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val theme = createThemeStateFlow()
    override val language = createLanguageStateFlow()
}

