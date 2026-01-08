package tech.zhifu.app.myhub.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module
import tech.zhifu.app.myhub.AppViewModel
import tech.zhifu.app.myhub.analytics.AnalyticsManager
import tech.zhifu.app.myhub.analytics.di.AppCoroutineScope
import tech.zhifu.app.myhub.analytics.di.analyticsModule
import tech.zhifu.app.myhub.component.card.di.cardModule
import tech.zhifu.app.myhub.dashboard.di.dashboardModule
import tech.zhifu.app.myhub.datastore.repository.di.repositoryModule
import tech.zhifu.app.myhub.logger.LoggerConfig
import tech.zhifu.app.myhub.logger.di.loggerModule
import tech.zhifu.app.myhub.profile.di.profileModule
import tech.zhifu.app.myhub.settings.di.settingsModule

fun initKoin(platformSpecificConfig: (KoinApplication.() -> Unit)? = null) {
    val appScope = AppCoroutineScope()

    val koinApplication = startKoin {
        // 应用平台特定配置（如果提供）
        platformSpecificConfig?.invoke(this)

        modules(
            loggerModule {
                LoggerConfig(
                    appName = "Myhub",
                    useAndroidLogger = true,
                )
            }, platformModule(),
            // Data module dependencies
            repositoryModule,
            settingsModule(),
            dashboardModule(),
            profileModule(),
            // Component modules
            cardModule,
            // Analytics module
            analyticsModule(),
            // App module
            module {
                // 提供 AppCoroutineScope（用于 analytics 初始化）
                single<AppCoroutineScope> { appScope }
                // 提供 ViewModel 使用的 CoroutineScope
                // 使用 Dispatchers.Default 作为默认调度器
                factory<CoroutineScope> {
                    CoroutineScope(Dispatchers.Default)
                }
                // App ViewModel（注入 AnalyticsService，如果可用）
                factory<AppViewModel> {
                    AppViewModel(
                        settingsRepository = get(),
                        coroutineScope = get(),
                        analyticsService = getOrNull(),
                    )
                }
            })
    }

    // 初始化统计服务（延迟初始化，避免阻塞应用启动）
    appScope.launch {
        try {
            // 等待 Koin 初始化完成后再获取 AnalyticsManager
            delay(100)
            koinApplication.koin.get<AnalyticsManager>().initialize()
        } catch (e: Exception) {
            // 统计初始化失败不应影响应用启动
            println("Failed to initialize analytics: ${e.message}")
        }
    }
}
