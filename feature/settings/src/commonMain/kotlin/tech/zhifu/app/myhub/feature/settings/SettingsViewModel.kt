package tech.zhifu.app.myhub.feature.settings

import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.state.ai.ProviderState
import tech.zhifu.app.myhub.ui.state.ai.createAIProviderStateFlow
import tech.zhifu.app.myhub.ui.state.language.LanguageState
import tech.zhifu.app.myhub.ui.state.language.createLanguageStateFlow
import tech.zhifu.app.myhub.ui.state.theme.ThemeState
import tech.zhifu.app.myhub.ui.state.theme.createThemeStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.createUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.createUserPreferencesStatFlow

class SettingsViewModel(
    override val userRepository: UserRepository,
) : ViewModel(),
    ContainerHost<SettingsUiState, SettingsSideEffect>,
    UserState,
    UserPreferencesState,
    ThemeState,
    LanguageState,
    ProviderState {

    override val container: Container<SettingsUiState, SettingsSideEffect> =
        container(initialState = SettingsUiState.Content)
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val language = createLanguageStateFlow()
    override val theme = createThemeStateFlow()
    override val providerRoutingConfig = createAIProviderStateFlow()
}
