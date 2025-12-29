package tech.zhifu.app.myhub.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.zhifu.app.myhub.AppViewModel
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.dashboard.di.dashboardModule
import tech.zhifu.app.myhub.profile.di.profileModule
import tech.zhifu.app.myhub.settings.di.settingsModule

fun initKoin(platformSpecificConfig: (KoinApplication.() -> Unit)? = null) {
    startKoin {
        // 应用平台特定配置（如果提供）
        platformSpecificConfig?.invoke(this)

        modules(
            platformModule(),
            // Data module dependencies
            repositoryModule,
            settingsModule(),
            dashboardModule(),
            profileModule(),
            module {
                // 提供 ViewModel 使用的 CoroutineScope
                // 使用 Dispatchers.Default 作为默认调度器
                factory<CoroutineScope> {
                    CoroutineScope(Dispatchers.Default)
                }

                // App ViewModel
                factoryOf(::AppViewModel)
            }
        )
    }
}
