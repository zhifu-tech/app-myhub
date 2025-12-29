package tech.zhifu.app.myhub.language

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.platform.resources.Res
import tech.zhifu.app.myhub.platform.resources.platform_language_english
import tech.zhifu.app.myhub.platform.resources.platform_language_japanese
import tech.zhifu.app.myhub.platform.resources.platform_language_simplified_chinese
import tech.zhifu.app.myhub.platform.resources.platform_language_traditional_chinese

/**
 * 支持的语言枚举
 */
enum class Language(val code: String, val region: String?) {
    English("en", null),
    SimplifiedChinese("zh-CN", "CN"),
    TraditionalChinese("zh-TW", "TW"),
    Japanese("ja", null)
}

/**
 * 将语言代码字符串转换为 Language 枚举
 *
 * @return 匹配的 Language，如果未找到则返回 English
 */
fun String.toLanguage(): Language {
    return Language.entries.find { it.code == this }
        ?: Language.entries.find { this.startsWith(it.code.split("-")[0]) }
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
        Language.English -> stringResource(Res.string.platform_language_english)
        Language.SimplifiedChinese -> stringResource(Res.string.platform_language_simplified_chinese)
        Language.TraditionalChinese -> stringResource(Res.string.platform_language_traditional_chinese)
        Language.Japanese -> stringResource(Res.string.platform_language_japanese)
    }
}

