package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.*
import tech.zhifu.app.local.LocalAppLocale
import tech.zhifu.app.local.customAppLocale

enum class Language(val code: String, val region: String?, val label: String) {
    English("en", null, "English"),
    SimplifiedChinese("zh-CN", "CN", "简体中文"),
    TraditionalChinese("zh-TW", "TW", "繁體中文"),
    Japanese("ja", null, "日本語")
}

val LocalAppLanguage = compositionLocalOf { Language.English }

@Composable
fun ProvideAppLanguage(content: @Composable () -> Unit) {
    val currentLocale = LocalAppLocale.current
    val currentLanguage = remember(currentLocale) {
        Language.entries.find { it.code == currentLocale }
            ?: Language.entries.find { currentLocale.startsWith(it.code.split("-")[0]) }
            ?: Language.English
    }

    CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
        content()
    }
}

fun updateAppLanguage(language: Language) {
    customAppLocale = language.code
}
