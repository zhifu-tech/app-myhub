package tech.zhifu.app.myhub.ui.state.language

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository

interface LanguageState {
    val userRepository: UserRepository
    val language: StateFlow<Language>
}

@Immutable
enum class Language(val languageTag: String) {
    ZH_CN(languageTag = "zh-CN"),
    ZH_TW(languageTag = "zh-TW"),
    EN(languageTag = "en");
}

fun String?.toLanguage(): Language {
    if (this.isNullOrBlank()) {
        return Language.ZH_CN
    }
    return when (this.trim()) {
        Language.ZH_CN.languageTag -> Language.ZH_CN
        Language.ZH_TW.languageTag -> Language.ZH_TW
        Language.EN.languageTag -> Language.EN
        else -> when (this.substringBefore('-', "").lowercase()) {
            "zh" -> Language.ZH_CN
            "en" -> Language.EN
            else -> Language.EN
        }
    }
}
