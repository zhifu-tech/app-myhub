package tech.zhifu.app.myhub.feature.settings.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tech.zhifu.app.myhub.settings.di.coreSettingsModule
import tech.zhifu.app.myhub.settings.LocalSettingStore
import tech.zhifu.app.myhub.datastore.repository.user.UserRepository
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.data.impl.SettingsRepositoryImpl
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.feature.settings.content.language.LanguageSetting
import tech.zhifu.app.myhub.feature.settings.content.theme.ThemeSetting

fun settingsModule() = module {
    // 包含 core/settings 模块（提供 LocalSettingStore）
    includes(coreSettingsModule)

    factory<SettingsRepository> {
        SettingsRepositoryImpl().apply {
            val localStore = get<LocalSettingStore>()
            val userRepository = get<UserRepository>()
            // 注册设置项
            register(ThemeSetting(localStore, userRepository))
            register(LanguageSetting(localStore, userRepository))
        }
    }
    viewModel {
        SettingsViewModel(
            settingsRepository = get()
        )
    }
}
