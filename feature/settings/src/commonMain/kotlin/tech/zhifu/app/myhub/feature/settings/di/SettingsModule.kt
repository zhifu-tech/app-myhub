package tech.zhifu.app.myhub.feature.settings.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.feature.settings.SettingsViewModel
import tech.zhifu.app.myhub.feature.settings.data.impl.SettingsRepositoryImpl
import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.feature.settings.data.store.LocalSettingStoreImpl
import tech.zhifu.app.myhub.feature.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.feature.settings.settings.LanguageSetting
import tech.zhifu.app.myhub.feature.settings.settings.ThemeSetting

fun settingsModule() = module {
    factory<LocalSettingStore> {
        LocalSettingStoreImpl()
    }

    factory<SettingsRepository> {
        SettingsRepositoryImpl().apply {
            val localStore = get<LocalSettingStore>()
            val userRepository = get<UserRepository>()
            // 注册设置项
            register(ThemeSetting(localStore, userRepository))
            register(LanguageSetting(localStore, userRepository))
        }
    }
    factory {
        SettingsViewModel(
            coroutineScope = get(),
            settingsRepository = get()
        )
    }
}
