package tech.zhifu.app.myhub.feature.settings.content.language

import tech.zhifu.app.myhub.language.Language

data class LanguageSettingState(
    val language: Language,
    val showLanguageDialog: Boolean = false,
    val isSubmitting: Boolean = false,
)
