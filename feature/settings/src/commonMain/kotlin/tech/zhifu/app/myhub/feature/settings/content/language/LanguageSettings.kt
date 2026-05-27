package tech.zhifu.app.myhub.feature.settings.content.language

import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_language_english
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_language_hans
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_language_hant
import tech.zhifu.app.myhub.ui.state.language.Language

fun Language.labelToken() = when (this) {
    Language.ZH_CN -> Res.string.feature_settings_language_hans
    Language.ZH_TW -> Res.string.feature_settings_language_hant
    else -> Res.string.feature_settings_language_english
}
