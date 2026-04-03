package tech.zhifu.app.myhub.feature.settings

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.design.util.ViewModelContainerHost
import tech.zhifu.app.myhub.ui.state.ai.AIProviderState
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
) : ViewModelContainerHost<SettingsUiState, SettingsSideEffect>(),
    UserState,
    UserPreferencesState,
    ThemeState,
    LanguageState,
    AIProviderState {

    override val container: Container<SettingsUiState, SettingsSideEffect> =
        container(initialState = SettingsUiState.Content)
    override val userStateFlow = createUserStateFlow()
    override val userPreferencesStateFlow = createUserPreferencesStatFlow()
    override val languageStateFlow = createLanguageStateFlow()
    override val themeStateFlow = createThemeStateFlow()
    override val aiProviderStateFlow = createAIProviderStateFlow()
}
