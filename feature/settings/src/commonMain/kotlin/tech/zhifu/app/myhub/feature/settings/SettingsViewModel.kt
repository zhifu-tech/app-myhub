package tech.zhifu.app.myhub.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSettingState
import tech.zhifu.app.myhub.feature.settings.content.language.languageSetting
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSettingState
import tech.zhifu.app.myhub.feature.settings.content.theme.themeSetting
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.language.Language
import tech.zhifu.app.myhub.language.toLanguage
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger

class SettingsViewModel(
    settingsRepository: SettingsRepository
) : ContainerHost<SettingsUiState, SettingsSideEffect>, ViewModel() {
    private val logger = logger("Settings")

    private val themeSetting = settingsRepository.themeSetting
    private val languageSetting = settingsRepository.languageSetting

    override val container: Container<SettingsUiState, SettingsSideEffect> = container(
        initialState = SettingsUiState.InitGlobalPending
    ) {
        initInternal()
    }

    val uiState: SettingsUiState
        get() = container.stateFlow.value

    @Composable
    fun <R> collectFieldAsState(selector: (SettingsUiState) -> R): State<R> {
        return container.stateFlow
            .map(selector)
            .distinctUntilChanged()
            .collectAsState(initial = selector(uiState))
    }

    fun retry() {
        val state = uiState as? SettingsUiState.ResultErrorDisabled ?: return
        if (!state.canRetry) return
        viewModelScope.launch {
            initInternal()
        }
    }

    fun update(language: Language) {
        val state = (uiState as? SettingsUiState.ResultOutputCompleted)
            ?.languageSettingState
            ?.takeIf { it.isSubmitting.not() }
            ?: return
        if (state.language != language) {
            viewModelScope.launch {
                updateLanguageInternal(language)
            }
        }
    }

    fun updateTheme(isDarkMode: Boolean) {
        val state = (uiState as? SettingsUiState.ResultOutputCompleted)
            ?.themeSettingState
            ?.takeIf { it.isSubmitting.not() }
            ?: return
        if (state.isDarkMode != isDarkMode) {
            viewModelScope.launch {
                updateThemeInternal(isDarkMode)
            }
        }
    }

    /**
     * 显示语言选择对话框
     */
    fun showLanguageDialog() = intent {
        reduce {
            val state = (state as? SettingsUiState.ResultOutputCompleted)
                ?: return@reduce state
            state.copy(
                languageSettingState = state.languageSettingState.copy(
                    showLanguageDialog = true
                )
            )
        }
    }

    /**
     * 隐藏语言选择对话框
     */
    fun hideLanguageDialog() = intent {
        reduce {
            val state = (state as? SettingsUiState.ResultOutputCompleted)
                ?: return@reduce state
            state.copy(
                languageSettingState = state.languageSettingState.copy(
                    showLanguageDialog = false
                )
            )
        }
    }

    /**
     * 清除完成态内联错误
     */
    fun clearError() = intent {
        reduce {
            val state = (state as? SettingsUiState.ResultOutputCompleted)
                ?: return@reduce state
            state.copy(
                inlineMessage = ""
            )
        }
    }

    private suspend fun initInternal() {
        intent {
            reduce {
                SettingsUiState.InitGlobalPending
            }
        }
        runCatching {
            SettingsUiState.ResultOutputCompleted(
                themeSettingState = ThemeSettingState(
                    isDarkMode = themeSetting.get(),
                ),
                languageSettingState = LanguageSettingState(
                    language = languageSetting.get().toLanguage()
                ),
            )
        }.onSuccess { loadedState ->
            intent {
                reduce {
                    loadedState
                }
            }
        }.onFailure { throwable ->
            logger.error(throwable) {
                "Failed to load settings: ${throwable.message}"
            }
            intent {
                reduce {
                    SettingsUiState.ResultErrorDisabled(
                        message = throwable.message ?: "Failed to load settings",
                        canRetry = true
                    )
                }
            }
        }
    }

    private suspend fun updateLanguageInternal(language: Language) {
        intent {
            reduce {
                val state = (state as? SettingsUiState.ResultOutputCompleted)
                    ?: return@reduce state
                state.copy(
                    languageSettingState = state.languageSettingState.copy(
                        isSubmitting = true,
                    ),
                    inlineMessage = "",
                )
            }
        }
        runCatching {
            languageSetting.set(language.code)
        }.onSuccess {
            intent {
                reduce {
                    val state = (state as? SettingsUiState.ResultOutputCompleted)
                        ?: return@reduce state
                    state.copy(
                        languageSettingState = state.languageSettingState.copy(
                            language = language,
                            isSubmitting = false,
                        ),
                        inlineMessage = "",
                    )
                }
            }
        }.onFailure { throwable ->
            logger.error(throwable) {
                "Failed to update language: ${throwable.message}"
            }
            intent {
                reduce {
                    val state = (state as? SettingsUiState.ResultOutputCompleted)
                        ?: return@reduce state
                    state.copy(
                        languageSettingState = state.languageSettingState.copy(
                            isSubmitting = false
                        ),
                        inlineMessage = throwable.message ?: "Failed to update language"
                    )
                }
            }
        }
    }

    private suspend fun updateThemeInternal(isDarkMode: Boolean) {
        intent {
            reduce {
                val state = (state as? SettingsUiState.ResultOutputCompleted)
                    ?: return@reduce state
                state.copy(
                    themeSettingState = state.themeSettingState.copy(
                        isSubmitting = true
                    ),
                    inlineMessage = ""
                )
            }
        }

        runCatching {
            themeSetting.set(isDarkMode)
        }.onSuccess {
            logger.info { "Theme updated successfully" }
            intent {
                reduce {
                    val state = (state as? SettingsUiState.ResultOutputCompleted)
                        ?: return@reduce state
                    state.copy(
                        themeSettingState = state.themeSettingState.copy(
                            isDarkMode = isDarkMode,
                            isSubmitting = false,
                        ),
                        inlineMessage = "",
                    )
                }
            }
        }.onFailure { throwable ->
            logger.error(throwable) {
                "Failed to update theme: ${throwable.message}"
            }
            intent {
                reduce {
                    val state = (state as? SettingsUiState.ResultOutputCompleted)
                        ?: return@reduce state
                    state.copy(
                        themeSettingState = state.themeSettingState.copy(
                            isSubmitting = false,
                        ),
                        inlineMessage = throwable.message ?: "Failed to update theme"
                    )
                }
            }
        }
    }
}
