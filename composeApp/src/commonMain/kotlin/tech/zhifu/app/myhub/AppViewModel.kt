package tech.zhifu.app.myhub

import androidx.lifecycle.ViewModel
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.state.theme.ThemeState
import tech.zhifu.app.myhub.ui.state.theme.initThemeStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.initUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.initUserPreferencesStatFlow

class AppViewModel(
    override val userRepository: UserRepository,
) : ViewModel(),
    UserState,
    UserPreferencesState,
    ThemeState {
    override val userStateFlow = initUserStateFlow()
    override val userPreferencesStateFlow = initUserPreferencesStatFlow()
    override val themeStateFlow = initThemeStateFlow()
}

