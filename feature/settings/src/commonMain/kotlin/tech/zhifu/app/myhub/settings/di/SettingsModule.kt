package tech.zhifu.app.myhub.settings.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.settings.SettingsViewModel
import tech.zhifu.app.myhub.settings.data.impl.SettingsRepositoryImpl
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStoreImpl
import tech.zhifu.app.myhub.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.settings.settings.LanguageSetting
import tech.zhifu.app.myhub.settings.settings.ThemeSetting

fun settingsModule() = module {
    // 本地设置存储
    single<LocalSettingStore> {
        LocalSettingStoreImpl()
    }

    // 设置仓库（注册所有设置项）
    single<SettingsRepository> {
        val repository = SettingsRepositoryImpl()
        val localStore = get<LocalSettingStore>()
        val userRepository: UserRepository? = try {
            getOrNull<UserRepository>()
        } catch (e: Exception) {
            null
        }

        // 注册设置项
        repository.register(ThemeSetting(localStore, userRepository))
        repository.register(LanguageSetting(localStore, userRepository))

        repository
    }
    // Settings ViewModel（使用新的设置架构）
    factory {
        SettingsViewModel(
            coroutineScope = get(),
            settingsRepository = get()
        )
    }

}
