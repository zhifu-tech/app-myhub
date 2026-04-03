package tech.zhifu.app.myhub.feature.settings.content.theme

import tech.zhifu.app.myhub.feature.settings.resources.Res
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_theme_dark
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_theme_light
import tech.zhifu.app.myhub.feature.settings.resources.feature_settings_theme_system
import tech.zhifu.app.myhub.ui.state.theme.Theme

fun Theme.labelToken() = when (this) {
    Theme.Light -> Res.string.feature_settings_theme_light
    Theme.Dark -> Res.string.feature_settings_theme_dark
    Theme.System -> Res.string.feature_settings_theme_system
}
