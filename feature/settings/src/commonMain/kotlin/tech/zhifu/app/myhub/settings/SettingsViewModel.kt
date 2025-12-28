package tech.zhifu.app.myhub.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.local.customAppLocale
import tech.zhifu.app.myhub.local.customAppThemeIsDark
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.ui.utils.Language
import tech.zhifu.app.myhub.ui.utils.updateAppLanguage

/**
 * Settings ViewModel
 *
 * 管理 Settings 页面的状态和业务逻辑
 */
class SettingsViewModel(
    private val coroutineScope: CoroutineScope
) {
    private val logger = logger("Settings")

    private val _uiState = MutableStateFlow<SettingsUiState>(
        SettingsUiState(
            currentLanguage = Language.English,
            isDarkMode = true,
            isLoading = false,
            error = null,
            showLanguageDialog = false
        )
    )

    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * 加载设置
     * 从本地存储加载配置并同步到 UI 状态
     */
    private fun loadSettings() {
        logger.info { "Loading settings from local storage" }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        coroutineScope.launch {
            try {
                val config = SettingsManager.loadConfig()

                // 确定当前语言
                val currentLanguage = config.language?.let { langCode ->
                    Language.entries.find { it.code == langCode }
                        ?: Language.entries.find { langCode.startsWith(it.code.split("-")[0]) }
                        ?: Language.English
                } ?: Language.English

                // 更新 UI 状态
                _uiState.value = _uiState.value.copy(
                    currentLanguage = currentLanguage,
                    isDarkMode = config.isDarkMode,
                    isLoading = false
                )

                // 同步到全局状态
                customAppLocale = config.language
                customAppThemeIsDark = config.isDarkMode

                logger.info { "Settings loaded: language=${currentLanguage.code}, darkMode=${config.isDarkMode}" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to load settings: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load settings: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新语言设置
     */
    fun updateLanguage(language: Language) {
        logger.info { "Updating language to ${language.code}" }

        coroutineScope.launch {
            try {
                val currentConfig = SettingsManager.loadConfig()
                val newConfig = currentConfig.copy(language = language.code)

                SettingsManager.saveConfig(newConfig)

                // 更新全局状态
                updateAppLanguage(language)

                // 更新 UI 状态
                _uiState.value = _uiState.value.copy(
                    currentLanguage = language,
                    showLanguageDialog = false
                )

                logger.info { "Language updated successfully" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to update language: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update language: ${e.message}"
                )
            }
        }
    }

    /**
     * 更新主题设置
     */
    fun updateTheme(isDarkMode: Boolean) {
        logger.info { "Updating theme to darkMode=$isDarkMode" }

        coroutineScope.launch {
            try {
                val currentConfig = SettingsManager.loadConfig()
                val newConfig = currentConfig.copy(isDarkMode = isDarkMode)

                SettingsManager.saveConfig(newConfig)

                // 更新全局状态
                customAppThemeIsDark = isDarkMode

                // 更新 UI 状态
                _uiState.value = _uiState.value.copy(
                    isDarkMode = isDarkMode
                )

                logger.info { "Theme updated successfully" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to update theme: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update theme: ${e.message}"
                )
            }
        }
    }

    /**
     * 显示语言选择对话框
     */
    fun showLanguageDialog() {
        _uiState.value = _uiState.value.copy(showLanguageDialog = true)
    }

    /**
     * 隐藏语言选择对话框
     */
    fun hideLanguageDialog() {
        _uiState.value = _uiState.value.copy(showLanguageDialog = false)
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

