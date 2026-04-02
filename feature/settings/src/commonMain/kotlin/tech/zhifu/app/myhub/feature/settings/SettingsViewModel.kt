package tech.zhifu.app.myhub.feature.settings

import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.ui.design.util.ViewModelContainerHost
import tech.zhifu.app.myhub.ui.state.theme.ThemeState
import tech.zhifu.app.myhub.ui.state.theme.initThemeStateFlow
import tech.zhifu.app.myhub.ui.state.user.UserState
import tech.zhifu.app.myhub.ui.state.user.initUserStateFlow
import tech.zhifu.app.myhub.ui.state.user.preferences.UserPreferencesState
import tech.zhifu.app.myhub.ui.state.user.preferences.initUserPreferencesStatFlow

class SettingsViewModel(
    override val userRepository: UserRepository,
) : ViewModelContainerHost<SettingsUiState, SettingsSideEffect>(),
    UserState,
    UserPreferencesState,
    ThemeState {

    override val container: Container<SettingsUiState, SettingsSideEffect> = container(
        initialState = SettingsUiState.Content
    )
    override val userStateFlow = initUserStateFlow()
    override val userPreferencesStateFlow = initUserPreferencesStatFlow()
    override val themeStateFlow = initThemeStateFlow()

//    //    private val themeSetting = settingsRepository.themeSetting
////    private val languageSetting = settingsRepository.languageSetting
//    private val aiModeSetting = settingsRepository.aiModeSetting
//    private val aiDirectEndpointSetting = settingsRepository.aiDirectEndpointSetting
//    private val aiDirectModelSetting = settingsRepository.aiDirectModelSetting
//    private val aiDirectApiKeySetting = settingsRepository.aiDirectApiKeySetting
//    private val aiTimeoutMsSetting = settingsRepository.aiTimeoutMsSetting
//
//    private val aiMaxRetriesSetting = settingsRepository.aiMaxRetriesSetting

//    @Composable
//    fun <R> collectFieldAsState(selector: (SettingsUiState) -> R): State<R> {
//        return container.stateFlow
//            .map(selector)
//            .distinctUntilChanged()
//            .collectAsState(initial = selector(uiState))
//    }
//
//    fun retry() {
//        val state = uiState as? SettingsUiState.Error ?: return
//        if (!state.canRetry) return
//        viewModelScope.launch {
//            initInternal()
//        }
//    }

//    fun update(language: Language) {
//        val state = (uiState as? SettingsUiState.Content)
//            ?.languageSettingState
//            ?.takeIf { it.isSubmitting.not() }
//            ?: return
//        if (state.language != language) {
//            viewModelScope.launch {
//                updateLanguageInternal(language)
//            }
//        }
//    }

//    fun updateTheme(isDarkMode: Boolean) {
//        val state = (uiState as? SettingsUiState.Content)
//            ?.themeSettingState
//            ?.takeIf { it.isSubmitting.not() }
//            ?: return
//        if (state.isDarkMode != isDarkMode) {
//            viewModelScope.launch {
//                updateThemeInternal(isDarkMode)
//            }
//        }
//    }
//
//    fun updateAiMode(mode: String) = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content) ?: return@reduce state
//            state.copy(
//                aiProviderSettingState = state.aiProviderSettingState.copy(
//                    mode = mode,
//                    validationMessage = "",
//                )
//            )
//        }
//    }
//
//    fun updateAiDirectEndpoint(value: String) = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content) ?: return@reduce state
//            state.copy(
//                aiProviderSettingState = state.aiProviderSettingState.copy(
//                    directEndpoint = value,
//                    validationMessage = "",
//                )
//            )
//        }
//    }
//
//    fun updateAiDirectModel(value: String) = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content) ?: return@reduce state
//            state.copy(
//                aiProviderSettingState = state.aiProviderSettingState.copy(
//                    directModel = value,
//                    validationMessage = "",
//                )
//            )
//        }
//    }
//
//    fun updateAiDirectApiKey(value: String) = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content) ?: return@reduce state
//            state.copy(
//                aiProviderSettingState = state.aiProviderSettingState.copy(
//                    directApiKey = value,
//                    validationMessage = "",
//                )
//            )
//        }
//    }
//
//    fun updateAiTimeoutMs(value: String) = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content) ?: return@reduce state
//            state.copy(
//                aiProviderSettingState = state.aiProviderSettingState.copy(
//                    timeoutMs = value,
//                    validationMessage = "",
//                )
//            )
//        }
//    }
//
//    fun updateAiMaxRetries(value: String) = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content) ?: return@reduce state
//            state.copy(
//                aiProviderSettingState = state.aiProviderSettingState.copy(
//                    maxRetries = value,
//                    validationMessage = "",
//                )
//            )
//        }
//    }
//
//    fun saveAiProviderSettings() {
//        val current = (uiState as? SettingsUiState.Content)?.aiProviderSettingState ?: return
//        val validation = validateAiProviderState(current)
//        if (validation != null) {
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content) ?: return@reduce state
//                    state.copy(
//                        aiProviderSettingState = state.aiProviderSettingState.copy(
//                            validationMessage = validation
//                        )
//                    )
//                }
//            }
//            return
//        }
//        viewModelScope.launch {
//            saveAiProviderSettingsInternal(current)
//        }
//    }
//
//    /**
//     * 显示语言选择对话框
//     */
//    fun showLanguageDialog() = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content)
//                ?: return@reduce state
//            state.copy(
//                languageSettingState = state.languageSettingState.copy(
//                    showLanguageDialog = true
//                )
//            )
//        }
//    }
//
////    /**
////     * 隐藏语言选择对话框
////     */
////    fun hideLanguageDialog() = intent {
////        reduce {
////            val state = (state as? SettingsUiState.Content)
////                ?: return@reduce state
////            state.copy(
////                languageSettingState = state.languageSettingState.copy(
////                    showLanguageDialog = false
////                )
////            )
////        }
////    }
//
//    /**
//     * 清除完成态内联错误
//     */
//    fun clearError() = intent {
//        reduce {
//            val state = (state as? SettingsUiState.Content)
//                ?: return@reduce state
//            state.copy(
//                inlineMessage = ""
//            )
//        }
//    }
//
//    private suspend fun initInternal() {
//        intent {
//            reduce {
//                SettingsUiState.Loading
//            }
//        }
//        runCatching {
////            SettingsUiState.Content(
//////                themeSettingState = ThemeSettingState(
//////                    isDarkMode = themeSetting.get(),
//////                ),
//////                languageSettingState = LanguageSettingState(
//////                    language = languageSetting.get().toLanguage()
//////                ),
////                aiProviderSettingState = AiProviderSettingState(
////                    mode = aiModeSetting.get(),
////                    directEndpoint = aiDirectEndpointSetting.get(),
////                    directModel = aiDirectModelSetting.get(),
////                    directApiKey = aiDirectApiKeySetting.get(),
////                    timeoutMs = aiTimeoutMsSetting.get().toString(),
////                    maxRetries = aiMaxRetriesSetting.get().toString(),
////                ),
////            )
//        }.onSuccess { loadedState ->
//            intent {
//                reduce {
//                    loadedState
//                }
//            }
//        }.onFailure { throwable ->
//            logger.error(throwable) {
//                "Failed to load settings: ${throwable.message}"
//            }
//            intent {
//                reduce {
//                    SettingsUiState.Error(
//                        message = throwable.message ?: "Failed to load settings",
//                        canRetry = true
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun updateLanguageInternal(language: Language) {
//        intent {
//            reduce {
//                val state = (state as? SettingsUiState.Content)
//                    ?: return@reduce state
//                state.copy(
//                    languageSettingState = state.languageSettingState.copy(
//                        isSubmitting = true,
//                    ),
//                    inlineMessage = "",
//                )
//            }
//        }
//        runCatching {
//            languageSetting.set(language.code)
//        }.onSuccess {
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content)
//                        ?: return@reduce state
//                    state.copy(
//                        languageSettingState = state.languageSettingState.copy(
//                            language = language,
//                            isSubmitting = false,
//                        ),
//                        inlineMessage = "",
//                    )
//                }
//            }
//        }.onFailure { throwable ->
//            logger.error(throwable) {
//                "Failed to update language: ${throwable.message}"
//            }
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content)
//                        ?: return@reduce state
//                    state.copy(
//                        languageSettingState = state.languageSettingState.copy(
//                            isSubmitting = false
//                        ),
//                        inlineMessage = throwable.message ?: "Failed to update language"
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun updateThemeInternal(isDarkMode: Boolean) {
//        intent {
//            reduce {
//                val state = (state as? SettingsUiState.Content)
//                    ?: return@reduce state
//                state.copy(
//                    themeSettingState = state.themeSettingState.copy(
//                        isSubmitting = true
//                    ),
//                    inlineMessage = ""
//                )
//            }
//        }
//
//        runCatching {
//            themeSetting.set(isDarkMode)
//        }.onSuccess {
//            logger.info { "Theme updated successfully" }
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content)
//                        ?: return@reduce state
//                    state.copy(
//                        themeSettingState = state.themeSettingState.copy(
//                            isDarkMode = isDarkMode,
//                            isSubmitting = false,
//                        ),
//                        inlineMessage = "",
//                    )
//                }
//            }
//        }.onFailure { throwable ->
//            logger.error(throwable) {
//                "Failed to update theme: ${throwable.message}"
//            }
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content)
//                        ?: return@reduce state
//                    state.copy(
//                        themeSettingState = state.themeSettingState.copy(
//                            isSubmitting = false,
//                        ),
//                        inlineMessage = throwable.message ?: "Failed to update theme"
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun saveAiProviderSettingsInternal(current: AiProviderSettingState) {
//        intent {
//            reduce {
//                val state = (state as? SettingsUiState.Content) ?: return@reduce state
//                state.copy(
//                    aiProviderSettingState = state.aiProviderSettingState.copy(
//                        isSubmitting = true,
//                        validationMessage = "",
//                    ),
//                    inlineMessage = "",
//                )
//            }
//        }
//        runCatching {
//            aiModeSetting.set(current.mode.uppercase())
//            aiDirectEndpointSetting.set(current.directEndpoint.trim())
//            aiDirectModelSetting.set(current.directModel.trim())
//            aiDirectApiKeySetting.set(current.directApiKey.trim())
//            aiTimeoutMsSetting.set(current.timeoutMs.toLong())
//            aiMaxRetriesSetting.set(current.maxRetries.toInt())
//        }.onSuccess {
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content) ?: return@reduce state
//                    state.copy(
//                        aiProviderSettingState = state.aiProviderSettingState.copy(
//                            isSubmitting = false,
//                            validationMessage = "",
//                        ),
//                        inlineMessage = "",
//                    )
//                }
//            }
//        }.onFailure { throwable ->
//            intent {
//                reduce {
//                    val state = (state as? SettingsUiState.Content) ?: return@reduce state
//                    state.copy(
//                        aiProviderSettingState = state.aiProviderSettingState.copy(
//                            isSubmitting = false,
//                            validationMessage = throwable.message ?: "Failed to save AI settings",
//                        ),
//                        inlineMessage = throwable.message ?: "Failed to save AI settings",
//                    )
//                }
//            }
//        }
//    }
//
//    private fun validateAiProviderState(state: AiProviderSettingState): String? {
//        val mode = state.mode.uppercase()
//        if (mode !in setOf("DISABLED", "SERVER_GATEWAY", "DIRECT_API")) {
//            return "Invalid mode"
//        }
//        val timeout = state.timeoutMs.toLongOrNull()
//        if (timeout == null || timeout <= 0) {
//            return "timeoutMs must be > 0"
//        }
//        val retries = state.maxRetries.toIntOrNull()
//        if (retries == null || retries < 0 || retries > 5) {
//            return "maxRetries must be between 0 and 5"
//        }
//        if (mode == "DIRECT_API") {
//            if (state.directEndpoint.isBlank()) return "direct endpoint required for DIRECT_API"
//            if (state.directModel.isBlank()) return "direct model required for DIRECT_API"
//            if (state.directApiKey.isBlank()) return "direct apiKey required for DIRECT_API"
//        }
//        return null
//    }
}
