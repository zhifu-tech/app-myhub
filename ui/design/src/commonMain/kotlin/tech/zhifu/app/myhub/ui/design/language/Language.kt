package tech.zhifu.app.myhub.ui.design.language

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.ui.design.resources.Res
import tech.zhifu.app.myhub.ui.design.resources.language_english
import tech.zhifu.app.myhub.ui.design.resources.language_simplified_chinese

enum class Language(val code: String) {
    English("en"),
    SimplifiedChinese("zh-CN"),
}

object AppLocale {
    const val EN = "en"
    const val ZH = "zh"

    // App-level default locale when no user/system preference is available.
    const val DEFAULT = ZH
}

fun normalizeLanguageTag(tag: String?): String {
    val normalized = tag
        ?.trim()
        ?.replace('_', '-')
        ?.takeIf { it.isNotEmpty() }
        ?: return AppLocale.DEFAULT

    return when (val lower = normalized.lowercase()) {
        "en", "en-us", "en-gb" -> AppLocale.EN
        "zh", "zh-cn", "zh-hans", "zh-sg" -> AppLocale.ZH
        else -> when (lower.substringBefore('-')) {
            "en" -> AppLocale.EN
            "zh" -> AppLocale.ZH
            else -> AppLocale.EN
        }
    }
}

/**
 * 将语言代码字符串转换为 Language 枚举
 *
 * @return 匹配的 Language，如果未找到则返回 English
 */
fun String.toLanguage(): Language {
    val normalized = normalizeLanguageTag(this)
    return Language.entries.find { it.code == normalized }
        ?: Language.entries.find { normalized.startsWith(it.code.split("-")[0]) }
        ?: Language.English
}

/**
 * 将 Language 枚举转换为语言代码字符串
 *
 * @return 语言代码（如 "en", "zh-CN"）
 */
fun Language.toCode(): String = this.code

/**
 * 获取语言的本地化显示名称
 *
 * @return 根据当前语言环境返回对应的语言名称
 */
@Composable
fun Language.getLocalizedLabel(): String {
    return when (this) {
        Language.English -> stringResource(Res.string.language_english)
        Language.SimplifiedChinese -> stringResource(Res.string.language_simplified_chinese)
    }
}
