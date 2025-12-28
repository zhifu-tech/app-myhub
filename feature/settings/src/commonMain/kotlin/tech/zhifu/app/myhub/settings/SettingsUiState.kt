package tech.zhifu.app.myhub.settings

import tech.zhifu.app.myhub.ui.utils.Language

/**
 * Settings UI状态
 */
data class SettingsUiState(
    val currentLanguage: Language = Language.English,
    val isDarkMode: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLanguageDialog: Boolean = false
)

