package tech.zhifu.app.myhub.feature.settings

import tech.zhifu.app.myhub.language.Language

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

