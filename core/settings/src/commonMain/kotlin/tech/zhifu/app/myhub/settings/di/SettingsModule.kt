package tech.zhifu.app.myhub.settings.di

import org.koin.dsl.module
import tech.zhifu.app.myhub.settings.LocalSettingStore
import tech.zhifu.app.myhub.settings.LocalSettingStoreImpl

/**
 * Core Settings 模块的依赖注入配置
 */
val coreSettingsModule = module {
    single<LocalSettingStore> {
        LocalSettingStoreImpl()
    }
}
