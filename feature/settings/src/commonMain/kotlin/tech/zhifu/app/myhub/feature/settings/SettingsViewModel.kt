package tech.zhifu.app.myhub.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.feature.settings.settings.languageSetting
import tech.zhifu.app.myhub.feature.settings.settings.themeSetting
import tech.zhifu.app.myhub.language.Language
import tech.zhifu.app.myhub.language.toLanguage
import tech.zhifu.app.myhub.local.customAppLocale
import tech.zhifu.app.myhub.local.customAppThemeIsDark
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger

/**
 * Settings ViewModel
 *
 * 管理 Settings 页面的状态和业务逻辑
 * 使用新的设置架构（Setting 接口）
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val logger = logger("Settings")

    private val themeSetting = settingsRepository.themeSetting
    private val languageSetting = settingsRepository.languageSetting
    private val _showLanguageDialog = MutableStateFlow(false)

    /**
     * UI 状态（响应式）
     * 自动从设置项 Flow 中组合生成
     */
    val uiState: StateFlow<SettingsUiState> = combine(
        themeSetting.observe(),
        languageSetting.observe(),
        _showLanguageDialog
    ) { isDark, languageCode, showDialog ->
        val currentLanguage = languageCode.toLanguage()

        // 同步到全局状态
        customAppLocale = languageCode
        customAppThemeIsDark = isDark

        SettingsUiState(
            currentLanguage = currentLanguage,
            isDarkMode = isDark,
            isLoading = false,
            error = null,
            showLanguageDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    init {
        // 初始化时加载设置值
        viewModelScope.launch {
            try {
                logger.info { "Loading settings" }
                themeSetting.get()
                languageSetting.get()
                logger.info { "Settings loaded successfully" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to load settings: ${e.message}"
                }
            }
        }
    }

    /**
     * 更新语言设置
     */
    fun updateLanguage(language: Language) {
        logger.info { "Updating language to ${language.code}" }

        viewModelScope.launch {
            try {
                languageSetting.set(language.code)
                // 同步更新全局状态
                customAppLocale = language.code
                logger.info { "Language updated successfully" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to update language: ${e.message}"
                }
            }
        }
    }

    /**
     * 更新主题设置
     */
    fun updateTheme(isDarkMode: Boolean) {
        logger.info { "Updating theme to darkMode=$isDarkMode" }

        viewModelScope.launch {
            try {
                themeSetting.set(isDarkMode)
                logger.info { "Theme updated successfully" }
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to update theme: ${e.message}"
                }
            }
        }
    }

    /**
     * 显示语言选择对话框
     */
    fun showLanguageDialog() {
        _showLanguageDialog.value = true
    }

    /**
     * 隐藏语言选择对话框
     */
    fun hideLanguageDialog() {
        _showLanguageDialog.value = false
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        // 错误状态现在由 UI State 管理，如果需要可以添加
    }
}
