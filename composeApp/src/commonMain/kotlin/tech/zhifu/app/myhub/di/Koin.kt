package tech.zhifu.app.myhub.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.datastore.repository.UserRepository
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.settings.SettingsViewModel
import tech.zhifu.app.myhub.settings.data.impl.SettingsRepositoryImpl
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStore
import tech.zhifu.app.myhub.settings.data.store.LocalSettingStoreImpl
import tech.zhifu.app.myhub.settings.domain.SettingsRepository
import tech.zhifu.app.myhub.settings.settings.LanguageSetting
import tech.zhifu.app.myhub.settings.settings.ThemeSetting

fun initKoin(platformSpecificConfig: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        // 应用平台特定配置（如果提供）
        platformSpecificConfig?.invoke(this)

        modules(
            platformModule(),
            // Data module dependencies
            repositoryModule,
            module {
                // 提供 ViewModel 使用的 CoroutineScope
                // 使用 Dispatchers.Default 作为默认调度器
                factory<CoroutineScope> {
                    CoroutineScope(Dispatchers.Default)
                }
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
                
                // Dashboard ViewModel
                factoryOf(::DashboardViewModel)
                
                // Settings ViewModel（使用新的设置架构）
                factory {
                    SettingsViewModel(
                        coroutineScope = get(),
                        settingsRepository = get()
                    )
                }
            }
        )
    }
}
