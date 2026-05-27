package tech.zhifu.app.myhub.feature.settings.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.settings.di.coreSettingsModule

fun settingsModule() = module {
    // 包含 core/settings 模块（提供 LocalSettingStore）
    includes(coreSettingsModule)

    viewModelOf(::SettingsViewModel)
}
